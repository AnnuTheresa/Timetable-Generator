package com.timetable.controller;

import com.timetable.dto.ClassSectionDto;
import com.timetable.service.ClassSectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/class-sections")
@RequiredArgsConstructor
public class ClassSectionController {

    private final ClassSectionService classSectionService;

    @GetMapping
    public List<ClassSectionDto> findAll(@RequestParam(required = false) Long semesterId) {
        if (semesterId != null) return classSectionService.findBySemesterId(semesterId);
        return classSectionService.findAll();
    }

    @GetMapping("/{id}")
    public ClassSectionDto findById(@PathVariable Long id) {
        return classSectionService.findById(id);
    }

    @PostMapping
    public ClassSectionDto create(@Valid @RequestBody ClassSectionDto dto) {
        return classSectionService.create(dto);
    }

    @PutMapping("/{id}")
    public ClassSectionDto update(@PathVariable Long id, @Valid @RequestBody ClassSectionDto dto) {
        return classSectionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classSectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
