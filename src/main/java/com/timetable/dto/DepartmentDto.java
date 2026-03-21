package com.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentDto {
    private Long id;
    @NotBlank
    private String name;
}
