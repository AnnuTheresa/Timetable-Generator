package com.timetable.service;

import com.timetable.dto.SubjectDto;
import com.timetable.entity.Semester;
import com.timetable.entity.Subject;
import com.timetable.repository.SemesterRepository;
import com.timetable.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public List<SubjectDto> findAll() {
        return subjectRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubjectDto findById(Long id) {
        return subjectRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + id));
    }

    @Transactional
    public SubjectDto create(SubjectDto dto) {

        Semester semester = semesterRepository.findById(dto.getSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));

        Subject s = Subject.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .isLab(dto.getIsLab())
                .periodsPerWeek(dto.getPeriodsPerWeek())
                .semester(semester)
                .build();

        return toDto(subjectRepository.save(s));
    }

    @Transactional
    public SubjectDto update(Long id, SubjectDto dto) {

        Subject s = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + id));

        s.setName(dto.getName());
        s.setCode(dto.getCode());
        s.setIsLab(dto.getIsLab());
        s.setPeriodsPerWeek(dto.getPeriodsPerWeek());

        if (dto.getSemesterId() != null) {
            s.setSemester(semesterRepository.getReferenceById(dto.getSemesterId()));
        }

        return toDto(subjectRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        subjectRepository.deleteById(id);
    }

    private SubjectDto toDto(Subject s) {

        SubjectDto dto = new SubjectDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setCode(s.getCode());
        dto.setIsLab(s.getIsLab());
        dto.setPeriodsPerWeek(s.getPeriodsPerWeek());
        dto.setSemesterId(s.getSemester().getId());

        return dto;
    }
    @Transactional(readOnly = true)
      public List<SubjectDto> findBySemesterId(Long semesterId) { 
           return subjectRepository.findBySemesterId(semesterId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}