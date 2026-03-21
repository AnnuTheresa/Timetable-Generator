package com.timetable.service;

import com.timetable.dto.TeacherSubjectAllocationDto;
import com.timetable.entity.Room;
import com.timetable.entity.Semester;
import com.timetable.entity.Subject;
import com.timetable.entity.Teacher;
import com.timetable.entity.TeacherSubjectAllocation;
import com.timetable.repository.RoomRepository;
import com.timetable.repository.SemesterRepository;
import com.timetable.repository.SubjectRepository;
import com.timetable.repository.TeacherRepository;
import com.timetable.repository.TeacherSubjectAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherSubjectAllocationService {

    private final TeacherSubjectAllocationRepository allocationRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<TeacherSubjectAllocationDto> findBySemesterAndAcademicYear(Long semesterId, String academicYear) {
        return allocationRepository.findBySemesterIdAndAcademicYear(semesterId, academicYear)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public TeacherSubjectAllocationDto create(TeacherSubjectAllocationDto dto) {
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
        Semester semester = semesterRepository.findById(dto.getSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));
        Room room = (dto.getRoomId() != null)
                ? roomRepository.findById(dto.getRoomId()).orElse(null) : null;
        TeacherSubjectAllocation a = TeacherSubjectAllocation.builder()
                .teacher(teacher)
                .subject(subject)
                .semester(semester)
                .academicYear(dto.getAcademicYear())
                .room(room)
                .build();
        return toDto(allocationRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        allocationRepository.deleteById(id);
    }

    private TeacherSubjectAllocationDto toDto(TeacherSubjectAllocation a) {
        TeacherSubjectAllocationDto dto = new TeacherSubjectAllocationDto();
        dto.setId(a.getId());
        dto.setTeacherId(a.getTeacher().getId());
        dto.setSubjectId(a.getSubject().getId());
        dto.setSemesterId(a.getSemester().getId());
        dto.setAcademicYear(a.getAcademicYear());
        dto.setRoomId(a.getRoom() != null ? a.getRoom().getId() : null);
        return dto;
    }
}
