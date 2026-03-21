package com.timetable.dto;

import lombok.Data;

@Data
public class TimetableSlotDto {
    private Long id;
    private Long timetableVersionId;
    private Integer dayOfWeek;
    private Integer periodIndex;
    private Long classSectionId;
    private String classSectionName;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private Long roomId;
    private String roomName;
    private Boolean isLocked;
    private Boolean notRequired;
    private String labBlockId;
}
