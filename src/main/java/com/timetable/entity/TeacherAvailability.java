package com.timetable.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "teacher_availability", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"teacher_id", "day_of_week", "period_index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=Monday .. 5=Friday

    @Column(name = "period_index", nullable = false)
    private Integer periodIndex; // 0..5

    @Column(nullable = false)
    private Boolean available = true;

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
