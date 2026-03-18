package com.timetable.repository;

import com.timetable.entity.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {
    List<ClassSection> findBySemesterId(Long semesterId);
}
