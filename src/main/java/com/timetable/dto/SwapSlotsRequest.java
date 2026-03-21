package com.timetable.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwapSlotsRequest {
    @NotNull
    private Long slotId1;
    @NotNull
    private Long slotId2;
}
