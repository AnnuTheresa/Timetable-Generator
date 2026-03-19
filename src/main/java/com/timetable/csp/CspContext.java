package com.timetable.csp;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class CspContext {

    private Long semesterId;
    private String academicYear;
    private List<ClassSectionInfo> classSections;
    private List<SubjectRequirement> subjectRequirements;
    private List<AllocationInfo> allocations;
    private List<ResourceInfo> theoryRooms;
    private List<ResourceInfo> labRooms;
    private List<ExistingAssignment> existingAssignments;
    private List<LockedSlot> lockedSlots;
    private Set<SlotKey> notRequiredSlots;
    private List<TeacherSlotAvailability> teacherAvailability;
    private Set<SlotKey> preferredSlots;

    @Data
    @Builder
    public static class ClassSectionInfo {
        private Long id;
        private String displayName;
    }

    @Data
    @Builder
    public static class SubjectRequirement {
        private Long subjectId;
        private String subjectName;
        private boolean lab;
        /**
         * Theory: number of individual periods per week (e.g. 3 or 4).
         * Lab:    number of lab BLOCKS per week (almost always 1).
         *         One block = 3 periods on Mon–Thu, 4 periods on Friday forenoon.
         */
        private int periodsPerWeek;
    }

    @Data
    @Builder
    public static class AllocationInfo {
        private Long teacherId;
        private Long subjectId;
        /**
         * FIX: classSectionId added so the solver can filter allocations
         * per section and avoid assigning the wrong teacher to a section.
         */
        private Long classSectionId;
        /** For lab subjects: allocated lab room id. Null for theory. */
        private Long roomId;
    }

    @Data
    @Builder
    public static class ResourceInfo {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    public static class ExistingAssignment {
        private Long teacherId;
        private Long roomId;
        private int dayOfWeek;
        private int periodIndex;
        private Long classSectionId;
    }

    @Data
    @Builder
    public static class LockedSlot {
        private Long classSectionId;
        private int dayOfWeek;
        private int periodIndex;
        private Long subjectId;
        private Long teacherId;
        private Long roomId;
        private String labBlockId;
    }

    @Data
    @Builder
    public static class SlotKey {
        private Long classSectionId;
        private int dayOfWeek;
        private int periodIndex;

        public static SlotKey of(Long classSectionId, int day, int period) {
            return SlotKey.builder()
                    .classSectionId(classSectionId)
                    .dayOfWeek(day)
                    .periodIndex(period)
                    .build();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SlotKey slotKey = (SlotKey) o;
            return dayOfWeek == slotKey.dayOfWeek
                    && periodIndex == slotKey.periodIndex
                    && java.util.Objects.equals(classSectionId, slotKey.classSectionId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(classSectionId, dayOfWeek, periodIndex);
        }
    }

    @Data
    @Builder
    public static class TeacherSlotAvailability {
        private Long teacherId;
        private int dayOfWeek;
        private int periodIndex;
        private boolean available;
    }
}
