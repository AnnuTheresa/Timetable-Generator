package com.timetable.repository;

import com.timetable.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    List<Semester> findByCourseId(Long courseId);
}
