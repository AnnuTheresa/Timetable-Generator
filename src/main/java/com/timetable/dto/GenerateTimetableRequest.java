package com.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateTimetableRequest {
    @NotNull
    private Long semesterId;
    @NotBlank
    private String academicYear;
    /** If true and conflict exists, allow modifying existing timetable. */
    private boolean modifyExistingOnConflict;
}
