package com.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    @NotNull
    private Long departmentId;
}
