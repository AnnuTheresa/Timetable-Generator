package com.timetable.controller;

import com.timetable.dto.SemesterDto;
import com.timetable.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    public List<SemesterDto> findAll(@RequestParam(required = false) Long courseId) {
        if (courseId != null) return semesterService.findByCourseId(courseId);
        return semesterService.findAll();
    }

    @GetMapping("/{id}")
    public SemesterDto findById(@PathVariable Long id) {
        return semesterService.findById(id);
    }

    @PostMapping
    public SemesterDto create(@Valid @RequestBody SemesterDto dto) {
        return semesterService.create(dto);
    }

    @PutMapping("/{id}")
    public SemesterDto update(@PathVariable Long id, @Valid @RequestBody SemesterDto dto) {
        return semesterService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        semesterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
