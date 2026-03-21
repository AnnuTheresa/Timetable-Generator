package com.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassSectionDto {
    private Long id;
    @NotNull
    private Long courseId;
    @NotNull
    private Long semesterId;
    @NotBlank
    private String sectionName;
}
