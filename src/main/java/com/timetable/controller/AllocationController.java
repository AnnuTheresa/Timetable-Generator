package com.timetable.controller;

import com.timetable.dto.TeacherSubjectAllocationDto;
import com.timetable.service.TeacherSubjectAllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final TeacherSubjectAllocationService allocationService;

    @GetMapping
    public List<TeacherSubjectAllocationDto> findBySemesterAndYear(
            @RequestParam Long semesterId,
            @RequestParam String academicYear) {
        return allocationService.findBySemesterAndAcademicYear(semesterId, academicYear);
    }

    @PostMapping
    public TeacherSubjectAllocationDto create(@Valid @RequestBody TeacherSubjectAllocationDto dto) {
        return allocationService.create(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        allocationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
