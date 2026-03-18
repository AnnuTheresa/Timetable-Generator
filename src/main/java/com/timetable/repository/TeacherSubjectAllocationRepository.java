package com.timetable.repository;

import com.timetable.entity.TeacherSubjectAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherSubjectAllocationRepository extends JpaRepository<TeacherSubjectAllocation, Long> {
    List<TeacherSubjectAllocation> findBySemesterIdAndAcademicYear(Long semesterId, String academicYear);
}
