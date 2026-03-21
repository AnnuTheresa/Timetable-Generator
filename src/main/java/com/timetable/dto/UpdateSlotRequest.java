package com.timetable.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSlotRequest {
    @NotNull
    private Long slotId;
    private Long subjectId;
    private Long teacherId;
    private Long roomId;
    private Boolean isLocked;
    private Boolean notRequired;
}
