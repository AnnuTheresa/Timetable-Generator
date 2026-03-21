package com.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherDto {
    private Long id;
    @NotBlank
    private String name;
    private String email;
    @NotNull
    private Long departmentId;
}
