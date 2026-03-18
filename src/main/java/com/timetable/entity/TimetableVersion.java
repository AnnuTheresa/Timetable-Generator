package com.timetable.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timetable_versions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"semester_id", "version_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, ARCHIVED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "timetableVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimetableSlot> slots = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
