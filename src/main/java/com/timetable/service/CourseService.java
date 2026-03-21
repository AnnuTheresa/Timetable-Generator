package com.timetable.service;

import com.timetable.dto.CourseDto;
import com.timetable.entity.Course;
import com.timetable.entity.Department;
import com.timetable.repository.CourseRepository;
import com.timetable.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<CourseDto> findAll() {
        return courseRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto findById(Long id) {
        return courseRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
    }

    @Transactional
    public CourseDto create(CourseDto dto) {
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        Course c = Course.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .department(dept)
                .build();
        return toDto(courseRepository.save(c));
    }

    @Transactional
    public CourseDto update(Long id, CourseDto dto) {
        Course c = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
        c.setName(dto.getName());
        c.setCode(dto.getCode());
        if (dto.getDepartmentId() != null) {
            c.setDepartment(departmentRepository.getReferenceById(dto.getDepartmentId()));
        }
        return toDto(courseRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        courseRepository.deleteById(id);
    }

    private CourseDto toDto(Course c) {
        CourseDto dto = new CourseDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setCode(c.getCode());
        dto.setDepartmentId(c.getDepartment().getId());
        return dto;
    }
}
