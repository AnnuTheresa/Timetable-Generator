package com.timetable.service;

import com.timetable.dto.DepartmentDto;
import com.timetable.entity.Department;
import com.timetable.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentDto> findAll() {
        return departmentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentDto findById(Long id) {
        return departmentRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
    }

    @Transactional
    public DepartmentDto create(DepartmentDto dto) {
        Department d = Department.builder().name(dto.getName()).build();
        return toDto(departmentRepository.save(d));
    }

    @Transactional
    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department d = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
        d.setName(dto.getName());
        return toDto(departmentRepository.save(d));
    }

    @Transactional
    public void delete(Long id) {
        departmentRepository.deleteById(id);
    }

    private DepartmentDto toDto(Department d) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        return dto;
    }
}
