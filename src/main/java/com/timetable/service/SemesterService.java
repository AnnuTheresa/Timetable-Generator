package com.timetable.service;

import com.timetable.dto.SemesterDto;
import com.timetable.entity.Course;
import com.timetable.entity.Semester;
import com.timetable.repository.CourseRepository;
import com.timetable.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<SemesterDto> findAll() {
        return semesterRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SemesterDto> findByCourseId(Long courseId) {
        return semesterRepository.findByCourseId(courseId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SemesterDto findById(Long id) {
        return semesterRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found: " + id));
    }

    @Transactional
    public SemesterDto create(SemesterDto dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Semester s = Semester.builder()
                .course(course)
                .semesterNumber(dto.getSemesterNumber())
                .build();
        return toDto(semesterRepository.save(s));
    }

    @Transactional
    public SemesterDto update(Long id, SemesterDto dto) {
        Semester s = semesterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found: " + id));
        s.setSemesterNumber(dto.getSemesterNumber());
        if (dto.getCourseId() != null) {
            s.setCourse(courseRepository.getReferenceById(dto.getCourseId()));
        }
        return toDto(semesterRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        semesterRepository.deleteById(id);
    }

    private SemesterDto toDto(Semester s) {
        SemesterDto dto = new SemesterDto();
        dto.setId(s.getId());
        dto.setCourseId(s.getCourse().getId());
        dto.setSemesterNumber(s.getSemesterNumber());
        return dto;
    }
}
