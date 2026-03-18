package com.timetable.repository;

import com.timetable.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByDepartmentId(Long departmentId);
    List<Room> findByDepartmentIdAndIsLab(Long departmentId, Boolean isLab);
}
