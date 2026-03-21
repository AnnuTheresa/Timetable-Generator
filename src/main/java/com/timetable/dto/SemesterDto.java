package com.timetable.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class SemesterDto {
    private Long id;
    @NotNull
    private Long courseId;
    @NotNull
    @Min(1)
    @Max(8)
    private Integer semesterNumber;
}
