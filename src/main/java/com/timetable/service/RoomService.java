package com.timetable.service;

import com.timetable.dto.RoomDto;
import com.timetable.entity.Department;
import com.timetable.entity.Room;
import com.timetable.repository.DepartmentRepository;
import com.timetable.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<RoomDto> findAll() {
        return roomRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomDto findById(Long id) {
        return roomRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
    }

    @Transactional
    public RoomDto create(RoomDto dto) {
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        Room r = Room.builder()
                .name(dto.getName())
                .isLab(dto.getIsLab())
                .department(dept)
                .build();
        return toDto(roomRepository.save(r));
    }

    @Transactional
    public RoomDto update(Long id, RoomDto dto) {
        Room r = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
        r.setName(dto.getName());
        r.setIsLab(dto.getIsLab());
        if (dto.getDepartmentId() != null) {
            r.setDepartment(departmentRepository.getReferenceById(dto.getDepartmentId()));
        }
        return toDto(roomRepository.save(r));
    }

    @Transactional
    public void delete(Long id) {
        roomRepository.deleteById(id);
    }

    private RoomDto toDto(Room r) {
        RoomDto dto = new RoomDto();
        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setIsLab(r.getIsLab());
        dto.setDepartmentId(r.getDepartment().getId());
        return dto;
    }
}
