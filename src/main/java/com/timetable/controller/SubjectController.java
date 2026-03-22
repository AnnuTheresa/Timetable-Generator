package com.timetable.controller;

import com.timetable.dto.SubjectDto;
import com.timetable.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public List<SubjectDto> findAll() {
        return subjectService.findAll();
    }

    @GetMapping("/{id}")
    public SubjectDto findById(@PathVariable Long id) {
        return subjectService.findById(id);
    }

    @PostMapping
    public SubjectDto create(@Valid @RequestBody SubjectDto dto) {
        return subjectService.create(dto);
    }

    @PutMapping("/{id}")
    public SubjectDto update(@PathVariable Long id, @Valid @RequestBody SubjectDto dto) {
        return subjectService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
