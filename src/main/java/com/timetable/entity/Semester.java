package com.timetable.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "semesters", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "semester_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber; // 1 to 8

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClassSection> classSections = new ArrayList<>();

    // NEW: Subjects belong to semester
    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();

    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeacherSubjectAllocation> teacherSubjectAllocations = new ArrayList<>();

    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimetableVersion> timetableVersions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}