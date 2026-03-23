package com.timetable.controller;

import com.timetable.dto.GenerateTimetableRequest;
import com.timetable.dto.SwapSlotsRequest;
import com.timetable.dto.TimetableGridDto;
import com.timetable.dto.TimetableSlotDto;
import com.timetable.dto.UpdateSlotRequest;
import com.timetable.repository.TimetableVersionRepository;
import com.timetable.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;
    private final TimetableVersionRepository timetableVersionRepository;

    @PostMapping("/generate")
    public Object generate(@Valid @RequestBody GenerateTimetableRequest request) {
        return timetableService.generateTimetable(request);
    }

    @GetMapping("/version/{versionId}")
    public TimetableGridDto getTimetable(@PathVariable Long versionId) {
        return timetableService.getTimetableGrid(versionId);
    }

    @GetMapping("/semester/{semesterId}/versions")
    public List<Map<String, Object>> listVersions(@PathVariable Long semesterId) {
        return timetableVersionRepository.findBySemesterIdOrderByVersionNumberDesc(semesterId)
                .stream()
                .map(v -> Map.<String, Object>of(
                        "id", v.getId(),
                        "versionNumber", v.getVersionNumber(),
                        "status", v.getStatus(),
                        "createdAt", v.getCreatedAt().toString()))
                .toList();
    }

    @PatchMapping("/slot")
    public TimetableSlotDto updateSlot(@Valid @RequestBody UpdateSlotRequest request) {
        return timetableService.updateSlot(request);
    }

    @PostMapping("/slot/swap")
    public ResponseEntity<Void> swapSlots(@Valid @RequestBody SwapSlotsRequest request) {
        timetableService.swapSlots(request);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/version/{versionId}/activate")
public ResponseEntity<Void> activateVersion(@PathVariable Long versionId) {
    timetableService.activateVersion(versionId);
    return ResponseEntity.noContent().build();
}

@DeleteMapping("/version/{versionId}")
public ResponseEntity<Void> deleteVersion(@PathVariable Long versionId) {
    timetableService.deleteVersion(versionId);
    return ResponseEntity.noContent().build();
}
}
