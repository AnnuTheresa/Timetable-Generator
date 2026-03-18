package com.timetable.repository;

import com.timetable.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {

    List<TimetableSlot> findByTimetableVersionId(Long timetableVersionId);

    /**
     * Returns all slots from ACTIVE timetable versions belonging to other semesters
     * in the same course. Used to detect teacher conflicts across semesters.
     *
     * FIX: The original query used a max(version_number) subquery which did NOT
     * check version status, so it could return slots from INACTIVE versions.
     * Now we filter directly by status = 'ACTIVE' — since only one version per
     * semester is ever active at a time, this is simpler and correct.
     */
    @Query(value =
        "SELECT ts.* FROM timetable_slots ts " +
        "INNER JOIN timetable_versions tv ON ts.timetable_version_id = tv.id " +
        "INNER JOIN semesters s ON tv.semester_id = s.id " +
        "WHERE s.course_id = :courseId " +
        "AND tv.semester_id != :excludeSemesterId " +
        "AND tv.status = 'ACTIVE' " +
        "AND ts.teacher_id IS NOT NULL",
        nativeQuery = true)
    List<TimetableSlot> findSlotsByOtherSemestersInCourse(
            @Param("courseId") Long courseId,
            @Param("excludeSemesterId") Long excludeSemesterId);
}
