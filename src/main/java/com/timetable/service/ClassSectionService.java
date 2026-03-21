package com.timetable.service;

import com.timetable.dto.ClassSectionDto;
import com.timetable.entity.ClassSection;
import com.timetable.entity.Course;
import com.timetable.entity.Semester;
import com.timetable.repository.ClassSectionRepository;
import com.timetable.repository.CourseRepository;
import com.timetable.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassSectionService {

    private final ClassSectionRepository classSectionRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public List<ClassSectionDto> findAll() {
        return classSectionRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassSectionDto> findBySemesterId(Long semesterId) {
        return classSectionRepository.findBySemesterId(semesterId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClassSectionDto findById(Long id) {
        return classSectionRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("ClassSection not found: " + id));
    }

    @Transactional
    public ClassSectionDto create(ClassSectionDto dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Semester semester = semesterRepository.findById(dto.getSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));
        ClassSection cs = ClassSection.builder()
                .course(course)
                .semester(semester)
                .sectionName(dto.getSectionName())
                .build();
        return toDto(classSectionRepository.save(cs));
    }

    @Transactional
    public ClassSectionDto update(Long id, ClassSectionDto dto) {
        ClassSection cs = classSectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ClassSection not found: " + id));
        cs.setSectionName(dto.getSectionName());
        if (dto.getCourseId() != null) cs.setCourse(courseRepository.getReferenceById(dto.getCourseId()));
        if (dto.getSemesterId() != null) cs.setSemester(semesterRepository.getReferenceById(dto.getSemesterId()));
        return toDto(classSectionRepository.save(cs));
    }

    @Transactional
    public void delete(Long id) {
        classSectionRepository.deleteById(id);
    }

    private ClassSectionDto toDto(ClassSection cs) {
        ClassSectionDto dto = new ClassSectionDto();
        dto.setId(cs.getId());
        dto.setCourseId(cs.getCourse().getId());
        dto.setSemesterId(cs.getSemester().getId());
        dto.setSectionName(cs.getSectionName());
        return dto;
    }
}
