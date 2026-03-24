package com.timetable.service;

import com.timetable.config.TimeFramework;
import com.timetable.csp.CspAssignment;
import com.timetable.csp.CspContext;
import com.timetable.csp.TimetableCspSolver;
import com.timetable.dto.*;
import com.timetable.entity.*;
import com.timetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final SemesterRepository semesterRepository;
    private final ClassSectionRepository classSectionRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectAllocationRepository allocationRepository;
    private final RoomRepository roomRepository;
    private final TimetableVersionRepository timetableVersionRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public Object generateTimetable(GenerateTimetableRequest request) {
        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));

        List<ClassSection> sections = classSectionRepository.findBySemesterId(semester.getId());
        if (sections.isEmpty())
            throw new IllegalStateException("No class sections defined for this semester");

        List<TeacherSubjectAllocation> allocations = allocationRepository
                .findBySemesterIdAndAcademicYear(semester.getId(), request.getAcademicYear());
        if (allocations.isEmpty())
            throw new IllegalStateException(
                    "No teacher-subject allocations for this semester and academic year");

        Set<Long> subjectIds = allocations.stream()
                .map(a -> a.getSubject().getId()).collect(Collectors.toSet());
List<Subject> subjects = subjectRepository.findAllById(subjectIds)
        .stream()
        .filter(s -> s.getSemester().getId().equals(semester.getId()))
        .collect(Collectors.toList());

        List<CspContext.SubjectRequirement> requirements = subjects.stream()
                .map(s -> CspContext.SubjectRequirement.builder()
                        .subjectId(s.getId())
                        .subjectName(s.getName())
                        .lab(s.getIsLab())
                        .periodsPerWeek(s.getPeriodsPerWeek())
                        .build())
                .toList();

        List<CspContext.ClassSectionInfo> sectionInfos = sections.stream()
                .map(cs -> CspContext.ClassSectionInfo.builder()
                        .id(cs.getId())
                        .displayName(cs.getCourse().getCode() + " "
                                + cs.getSemester().getSemesterNumber() + "-"
                                + cs.getSectionName())
                        .build())
                .toList();

       
        List<CspContext.AllocationInfo> allocationInfos = new ArrayList<>();
for (TeacherSubjectAllocation a : allocations) {
    for (ClassSection cs : sections) {
        allocationInfos.add(CspContext.AllocationInfo.builder()
                .teacherId(a.getTeacher().getId())
                .subjectId(a.getSubject().getId())
                .classSectionId(cs.getId())
                .roomId(a.getRoom() != null ? a.getRoom().getId() : null)
                .build());
    }
}

        Long departmentId = semester.getCourse().getDepartment().getId();
        List<Room> labRooms = roomRepository.findByDepartmentIdAndIsLab(departmentId, true);

        long theorySubjectCount = subjects.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getIsLab())).count();
        if (theorySubjectCount == 0) {
            return conflictResponse(
                    "No theory subjects allocated for this semester.",
                    request.isModifyExistingOnConflict());
        }

        boolean hasLabSubjects = subjects.stream()
                .anyMatch(s -> Boolean.TRUE.equals(s.getIsLab()));
        if (hasLabSubjects && labRooms.isEmpty()) {
            return conflictResponse(
                    "No lab rooms found. Add at least one lab room for lab subjects.",
                    request.isModifyExistingOnConflict());
        }

        List<CspContext.ResourceInfo> labRoomInfos = labRooms.stream()
                .map(r -> CspContext.ResourceInfo.builder()
                        .id(r.getId()).name(r.getName()).build())
                .toList();


        List<CspContext.ExistingAssignment> existingAssignments = new ArrayList<>();


        System.out.println("DEBUG: modifyExistingOnConflict=" + request.isModifyExistingOnConflict());
        System.out.println("DEBUG: semesterId=" + semester.getId());

        if (!request.isModifyExistingOnConflict()) {
           List<TimetableSlot> otherSlots = timetableSlotRepository
                 .findSlotsByOtherSemestersInDepartment(departmentId, semester.getId());

           System.out.println("DEBUG: slots from other semesters=" + otherSlots.size());

            for (TimetableSlot slot : otherSlots) {
                existingAssignments.add(CspContext.ExistingAssignment.builder()
                        .teacherId(slot.getTeacher() != null
                                ? slot.getTeacher().getId() : null)
                        .roomId(slot.getRoom() != null
                                ? slot.getRoom().getId() : null)
                        .dayOfWeek(slot.getDayOfWeek())
                        .periodIndex(slot.getPeriodIndex())
                        .classSectionId(slot.getClassSection().getId())
                        .build());
            }
            System.out.println("DEBUG existing room IDs: " + 
              existingAssignments.stream()
                  .map(e -> String.valueOf(e.getRoomId()))
                  .collect(Collectors.joining(", ")));
        }

        List<TimetableVersion> existingVersions = timetableVersionRepository
                .findBySemesterIdOrderByVersionNumberDesc(semester.getId());

        List<CspContext.LockedSlot> lockedSlots    = new ArrayList<>();
        Set<CspContext.SlotKey>     notRequiredSlots = new HashSet<>();

        TimetableVersion activeVersion = existingVersions.stream()
                .filter(v -> "ACTIVE".equals(v.getStatus()))
                .findFirst().orElse(null);

        if (activeVersion != null) {
            List<TimetableSlot> activeSlots = timetableSlotRepository
                    .findByTimetableVersionId(activeVersion.getId());

            
            Set<String> seenLabBlocks = new HashSet<>();

            for (TimetableSlot slot : activeSlots) {

                if (Boolean.TRUE.equals(slot.getIsLocked())) {
                    if (slot.getLabBlockId() != null) {
                        
                        String blockKey = slot.getClassSection().getId()
                                + "-" + slot.getLabBlockId();
                        if (!seenLabBlocks.contains(blockKey)) {
                            seenLabBlocks.add(blockKey);
                            int startPeriod = activeSlots.stream()
                                    .filter(s -> s.getLabBlockId() != null
                                            && s.getLabBlockId().equals(slot.getLabBlockId())
                                            && s.getClassSection().getId()
                                                .equals(slot.getClassSection().getId())
                                            && s.getDayOfWeek().equals(slot.getDayOfWeek()))
                                    .mapToInt(TimetableSlot::getPeriodIndex)
                                    .min()
                                    .orElse(slot.getPeriodIndex());

                            lockedSlots.add(CspContext.LockedSlot.builder()
                                    .classSectionId(slot.getClassSection().getId())
                                    .dayOfWeek(slot.getDayOfWeek())
                                    .periodIndex(startPeriod) // ← canonical start index
                                    .subjectId(slot.getSubject() != null
                                            ? slot.getSubject().getId() : null)
                                    .teacherId(slot.getTeacher() != null
                                            ? slot.getTeacher().getId() : null)
                                    .roomId(slot.getRoom() != null
                                            ? slot.getRoom().getId() : null)
                                    .labBlockId(slot.getLabBlockId())
                                    .build());
                        }
                    } else {
                        lockedSlots.add(CspContext.LockedSlot.builder()
                                .classSectionId(slot.getClassSection().getId())
                                .dayOfWeek(slot.getDayOfWeek())
                                .periodIndex(slot.getPeriodIndex())
                                .subjectId(slot.getSubject() != null
                                        ? slot.getSubject().getId() : null)
                                .teacherId(slot.getTeacher() != null
                                        ? slot.getTeacher().getId() : null)
                                .roomId(slot.getRoom() != null
                                        ? slot.getRoom().getId() : null)
                                .labBlockId(null)
                                .build());
                    }
                }

                if (Boolean.TRUE.equals(slot.getNotRequired())) {
                    notRequiredSlots.add(CspContext.SlotKey.of(
                            slot.getClassSection().getId(),
                            slot.getDayOfWeek(),
                            slot.getPeriodIndex()));
                }
            }
        }

        System.out.println("=== LOCKED SLOTS (" + lockedSlots.size() + ") ===");
        for (CspContext.LockedSlot ls : lockedSlots) {
            System.out.println("  section=" + ls.getClassSectionId()
                    + " day=" + ls.getDayOfWeek()
                    + " period=" + ls.getPeriodIndex()
                    + " subj=" + ls.getSubjectId()
                    + " teacher=" + ls.getTeacherId()
                    + " labBlock=" + ls.getLabBlockId());
        }
        System.out.println("=== NOT REQUIRED (" + notRequiredSlots.size() + ") ===");
        System.out.println("=== EXISTING ASSIGNMENTS FROM OTHER SEMESTERS: "
                + existingAssignments.size() + " ===");

        CspContext context = CspContext.builder()
                .semesterId(semester.getId())
                .academicYear(request.getAcademicYear())
                .classSections(sectionInfos)
                .subjectRequirements(requirements)
                .allocations(allocationInfos)
                .theoryRooms(List.of())
                .labRooms(labRoomInfos)
                .existingAssignments(existingAssignments)
                .lockedSlots(lockedSlots)
                .notRequiredSlots(notRequiredSlots)
                .teacherAvailability(List.of())
                .preferredSlots(Set.of())
                .build();

        TimetableCspSolver solver = new TimetableCspSolver(context);
        CspAssignment.Solution solution = solver.solve();

        if (solution == null) {
            String reason = solver.getLastFailureReason();
            return conflictResponse(
                    reason != null ? reason : "Timetable could not be generated.",
                    request.isModifyExistingOnConflict());
        }

        int nextVersion = existingVersions.isEmpty()
                ? 1 : existingVersions.get(0).getVersionNumber() + 1;

        for (TimetableVersion v : existingVersions) {
            if ("ACTIVE".equals(v.getStatus())) {
                v.setStatus("INACTIVE");
                timetableVersionRepository.save(v);
            }
        }

        TimetableVersion version = TimetableVersion.builder()
                .semester(semester)
                .versionNumber(nextVersion)
                .status("ACTIVE")
                .build();
        version = timetableVersionRepository.save(version);

        
        Set<String> lockedTheoryKeys = new HashSet<>(); 
        Set<String> lockedLabBlockKeys = new HashSet<>(); 

        for (CspContext.LockedSlot ls : lockedSlots) {
            if (ls.getLabBlockId() != null) {
                lockedLabBlockKeys.add(ls.getClassSectionId() + "," + ls.getLabBlockId());
            } else {
                lockedTheoryKeys.add(ls.getClassSectionId() + ","
                        + ls.getDayOfWeek() + "," + ls.getPeriodIndex());
            }
        }

        Set<String> notRequiredKeys = notRequiredSlots.stream()
                .map(k -> k.getClassSectionId() + "," + k.getDayOfWeek()
                        + "," + k.getPeriodIndex())
                .collect(Collectors.toSet());

        for (CspAssignment a : solution.getAssignments()) {
            ClassSection cs      = classSectionRepository.getReferenceById(a.getClassSectionId());
            Subject subj         = a.getSubjectId() != null
                    ? subjectRepository.getReferenceById(a.getSubjectId()) : null;
            Teacher teacherRef   = a.getTeacherId() != null
                    ? teacherRepository.getReferenceById(a.getTeacherId()) : null;
            Room roomRef         = a.getRoomId() != null
                    ? roomRepository.getReferenceById(a.getRoomId()) : null;

            boolean wasLocked;
            if (a.getLabBlockId() != null) {
                wasLocked = lockedLabBlockKeys.contains(
                        a.getClassSectionId() + "," + a.getLabBlockId());
            } else {
                wasLocked = lockedTheoryKeys.contains(
                        a.getClassSectionId() + "," + a.getDayOfWeek()
                                + "," + a.getPeriodIndex());
            }

            boolean wasNotRequired = notRequiredKeys.contains(
                    a.getClassSectionId() + "," + a.getDayOfWeek()
                            + "," + a.getPeriodIndex());

            TimetableSlot slot = TimetableSlot.builder()
                    .timetableVersion(version)
                    .dayOfWeek(a.getDayOfWeek())
                    .periodIndex(a.getPeriodIndex())
                    .classSection(cs)
                    .subject(subj)
                    .teacher(teacherRef)
                    .room(roomRef)
                    .isLocked(wasLocked)
                    .notRequired(wasNotRequired)
                    .labBlockId(a.getLabBlockId())
                    .build();

            timetableSlotRepository.save(slot);
        }

        return toTimetableGridDto(version);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TimetableGridDto getTimetableGrid(Long timetableVersionId) {
        TimetableVersion version = timetableVersionRepository.findById(timetableVersionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Timetable version not found"));
        return toTimetableGridDto(version);
    }

    private TimetableGridDto toTimetableGridDto(TimetableVersion version) {
        List<TimetableSlot> slots = timetableSlotRepository
                .findByTimetableVersionId(version.getId());
        TimetableGridDto dto = new TimetableGridDto();
        dto.setTimetableVersionId(version.getId());
        dto.setSemesterId(version.getSemester().getId());
        dto.setVersionNumber(version.getVersionNumber());
        dto.setDayLabels(List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));
        dto.setTimeSlotLabels(List.of("P1", "Break", "P2", "P3", "Lunch", "P4", "P5", "P6"));

        Map<Long, List<List<TimetableSlotDto>>> data = new HashMap<>();
        Set<Long> sectionIds = slots.stream()
                .map(s -> s.getClassSection().getId()).collect(Collectors.toSet());

        for (Long sectionId : sectionIds) {
            List<List<TimetableSlotDto>> rows = new ArrayList<>();
            for (int day = 1; day <= TimeFramework.DAYS_PER_WEEK; day++) {
                List<TimetableSlotDto> cells = new ArrayList<>();
                for (int period = 0; period < TimeFramework.PERIODS_PER_DAY; period++) {
                    final int d = day, p = period;
                    TimetableSlot slot = slots.stream()
                            .filter(s -> s.getClassSection().getId().equals(sectionId)
                                    && s.getDayOfWeek() == d
                                    && s.getPeriodIndex() == p)
                            .findFirst().orElse(null);
                    cells.add(slot != null ? toSlotDto(slot) : null);
                }
                rows.add(cells);
            }
            data.put(sectionId, rows);
        }
        dto.setData(data);
        return dto;
    }

    private TimetableSlotDto toSlotDto(TimetableSlot s) {
        TimetableSlotDto dto = new TimetableSlotDto();
        dto.setId(s.getId());
        dto.setTimetableVersionId(s.getTimetableVersion().getId());
        dto.setDayOfWeek(s.getDayOfWeek());
        dto.setPeriodIndex(s.getPeriodIndex());
        dto.setClassSectionId(s.getClassSection().getId());
        dto.setClassSectionName(s.getClassSection().getCourse().getCode() + " "
                + s.getClassSection().getSemester().getSemesterNumber() + "-"
                + s.getClassSection().getSectionName());
        if (s.getSubject() != null) {
            dto.setSubjectId(s.getSubject().getId());
            dto.setSubjectName(s.getSubject().getName());
        }
        if (s.getTeacher() != null) {
            dto.setTeacherId(s.getTeacher().getId());
            dto.setTeacherName(s.getTeacher().getName());
        }
        if (s.getRoom() != null) {
            dto.setRoomId(s.getRoom().getId());
            dto.setRoomName(s.getRoom().getName());
        }
        dto.setIsLocked(Boolean.TRUE.equals(s.getIsLocked()));
        dto.setNotRequired(Boolean.TRUE.equals(s.getNotRequired()));
        dto.setLabBlockId(s.getLabBlockId());
        return dto;
    }

    // ── Update / swap ────────────────────────────────────────────────────────

    @Transactional
    public TimetableSlotDto updateSlot(UpdateSlotRequest request) {
        TimetableSlot slot = timetableSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        if (request.getSubjectId() != null)
            slot.setSubject(subjectRepository.getReferenceById(request.getSubjectId()));
        if (request.getTeacherId() != null)
            slot.setTeacher(teacherRepository.getReferenceById(request.getTeacherId()));
        if (request.getRoomId() != null)
            slot.setRoom(roomRepository.getReferenceById(request.getRoomId()));
        if (request.getIsLocked() != null)
            slot.setIsLocked(request.getIsLocked());
        if (request.getNotRequired() != null)
            slot.setNotRequired(request.getNotRequired());
        slot = timetableSlotRepository.save(slot);
        return toSlotDto(slot);
    }

    @Transactional
    public void swapSlots(SwapSlotsRequest request) {
        TimetableSlot s1 = timetableSlotRepository.findById(request.getSlotId1())
                .orElseThrow(() -> new IllegalArgumentException("Slot 1 not found"));
        TimetableSlot s2 = timetableSlotRepository.findById(request.getSlotId2())
                .orElseThrow(() -> new IllegalArgumentException("Slot 2 not found"));
        if (!s1.getTimetableVersion().getId().equals(s2.getTimetableVersion().getId()))
            throw new IllegalArgumentException(
                    "Slots must belong to the same timetable version");

        Subject subj1 = s1.getSubject();
        Teacher t1    = s1.getTeacher();
        Room r1       = s1.getRoom();

        s1.setSubject(s2.getSubject());
        s1.setTeacher(s2.getTeacher());
        s1.setRoom(s2.getRoom());

        s2.setSubject(subj1);
        s2.setTeacher(t1);
        s2.setRoom(r1);

        timetableSlotRepository.save(s1);
        timetableSlotRepository.save(s2);
    }


    private Map<String, Object> conflictResponse(String message, boolean modifyExisting) {
        Map<String, Object> c = new HashMap<>();
        c.put("conflict", true);
        c.put("message", message);
        c.put("modifyExistingOnConflict", modifyExisting);
        return c;
    }
    @Transactional
public void activateVersion(Long versionId) {
    TimetableVersion toActivate = timetableVersionRepository.findById(versionId)
            .orElseThrow(() -> new IllegalArgumentException("Version not found"));

    timetableVersionRepository
        .findBySemesterIdOrderByVersionNumberDesc(toActivate.getSemester().getId())
        .stream()
        .filter(v -> "ACTIVE".equals(v.getStatus()))
        .forEach(v -> {
            v.setStatus("INACTIVE");
            timetableVersionRepository.save(v);
        });

    toActivate.setStatus("ACTIVE");
    timetableVersionRepository.save(toActivate);
}

@Transactional
public void deleteVersion(Long versionId) {
    TimetableVersion version = timetableVersionRepository.findById(versionId)
            .orElseThrow(() -> new IllegalArgumentException("Version not found"));
    if ("ACTIVE".equals(version.getStatus()))
        throw new IllegalStateException(
            "Cannot delete the active version. Activate another version first.");
    timetableSlotRepository.deleteByTimetableVersionId(versionId);
    timetableVersionRepository.delete(version);
}
}
