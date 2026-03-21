package com.timetable.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class TimetableGridDto {
    private Long timetableVersionId;
    private Long semesterId;
    private Integer versionNumber;
    private List<String> timeSlotLabels;
    private List<String> dayLabels;
    private Map<Long, List<List<TimetableSlotDto>>> data;
}
