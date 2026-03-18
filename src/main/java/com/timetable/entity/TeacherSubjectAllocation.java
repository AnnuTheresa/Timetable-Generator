package com.timetable.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "teacher_subject_allocations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"teacher_id", "subject_id", "semester_id", "academic_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSubjectAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "academic_year", nullable = false)
    private String academicYear; // e.g. "2024-2025"

    /** For lab subjects: allocated lab room. Null for theory. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
