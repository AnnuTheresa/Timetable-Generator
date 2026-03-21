package com.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomDto {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private Boolean isLab;
    @NotNull
    private Long departmentId;
}
