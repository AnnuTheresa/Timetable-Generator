package com.timetable.service;

import com.timetable.dto.TeacherDto;
import com.timetable.entity.Department;
import com.timetable.entity.Teacher;
import com.timetable.repository.DepartmentRepository;
import com.timetable.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<TeacherDto> findAll() {
        return teacherRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeacherDto> findByDepartment(Long departmentId) {
        return teacherRepository.findByDepartmentId(departmentId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherDto findById(Long id) {
        return teacherRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + id));
    }

    @Transactional
    public TeacherDto create(TeacherDto dto) {
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        Teacher t = Teacher.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .department(dept)
                .build();
        t = teacherRepository.save(t);
        return toDto(t);
    }

    @Transactional
    public TeacherDto update(Long id, TeacherDto dto) {
        Teacher t = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + id));
        t.setName(dto.getName());
        t.setEmail(dto.getEmail());
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            t.setDepartment(dept);
        }
        return toDto(teacherRepository.save(t));
    }

    @Transactional
    public void delete(Long id) {
        teacherRepository.deleteById(id);
    }

    private TeacherDto toDto(Teacher t) {
        TeacherDto dto = new TeacherDto();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setEmail(t.getEmail());
        dto.setDepartmentId(t.getDepartment().getId());
        return dto;
    }
}
