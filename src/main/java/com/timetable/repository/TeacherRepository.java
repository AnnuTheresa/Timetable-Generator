package com.timetable.repository;

import com.timetable.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findByDepartmentId(Long departmentId);
}
