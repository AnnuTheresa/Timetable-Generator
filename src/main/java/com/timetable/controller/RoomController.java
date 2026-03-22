package com.timetable.controller;

import com.timetable.dto.RoomDto;
import com.timetable.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<RoomDto> findAll() {
        return roomService.findAll();
    }

    @GetMapping("/{id}")
    public RoomDto findById(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @PostMapping
    public RoomDto create(@Valid @RequestBody RoomDto dto) {
        return roomService.create(dto);
    }

    @PutMapping("/{id}")
    public RoomDto update(@PathVariable Long id, @Valid @RequestBody RoomDto dto) {
        return roomService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
