package com.timetable.repository;

import com.timetable.entity.TimetableVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableVersionRepository extends JpaRepository<TimetableVersion, Long> {

    List<TimetableVersion> findBySemesterIdOrderByVersionNumberDesc(Long semesterId);
}
