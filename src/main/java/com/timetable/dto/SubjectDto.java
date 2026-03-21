package com.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SubjectDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    @NotNull
    private Boolean isLab;
    @NotNull
    @Positive
    private Integer periodsPerWeek;
    @NotNull
    private Long semesterId;
}
