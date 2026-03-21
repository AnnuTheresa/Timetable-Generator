package com.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherSubjectAllocationDto {
    private Long id;
    @NotNull
    private Long teacherId;
    @NotNull
    private Long subjectId;
    @NotNull
    private Long semesterId;
    @NotBlank
    private String academicYear;
    private Long roomId;
}
