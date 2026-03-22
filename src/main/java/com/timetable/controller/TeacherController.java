package com.timetable.controller;

import com.timetable.dto.TeacherDto;
import com.timetable.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public List<TeacherDto> findAll(@RequestParam(required = false) Long departmentId) {
        if (departmentId != null) return teacherService.findByDepartment(departmentId);
        return teacherService.findAll();
    }

    @GetMapping("/{id}")
    public TeacherDto findById(@PathVariable Long id) {
        return teacherService.findById(id);
    }

    @PostMapping
    public TeacherDto create(@Valid @RequestBody TeacherDto dto) {
        return teacherService.create(dto);
    }

    @PutMapping("/{id}")
    public TeacherDto update(@PathVariable Long id, @Valid @RequestBody TeacherDto dto) {
        return teacherService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
