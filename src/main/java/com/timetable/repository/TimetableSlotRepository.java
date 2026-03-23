package com.timetable.repository;

import com.timetable.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {

    List<TimetableSlot> findByTimetableVersionId(Long timetableVersionId);
    void deleteByTimetableVersionId(Long timetableVersionId);
    
    @Query("SELECT ts FROM TimetableSlot ts " +
           "JOIN FETCH ts.teacher " +
           "LEFT JOIN FETCH ts.room " +
           "JOIN ts.timetableVersion tv " +
           "JOIN tv.semester s " +
           "WHERE s.course.id = :courseId " +
           "AND tv.semester.id != :excludeSemesterId " +
           "AND tv.status = 'ACTIVE' " +
           "AND ts.teacher IS NOT NULL")
    List<TimetableSlot> findSlotsByOtherSemestersInCourse(
            @Param("courseId") Long courseId,
            @Param("excludeSemesterId") Long excludeSemesterId);
}