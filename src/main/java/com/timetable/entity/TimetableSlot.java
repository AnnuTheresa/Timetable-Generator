package com.timetable.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "timetable_slots", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"timetable_version_id", "class_section_id", "day_of_week", "period_index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_version_id", nullable = false)
    private TimetableVersion timetableVersion;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=Monday .. 5=Friday

    @Column(name = "period_index", nullable = false)
    private Integer periodIndex; // 0..5 (Period 1 to 6)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject; // null if slot is empty or not required

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = false;

    @Column(name = "not_required", nullable = false)
    private Boolean notRequired = false; // user marked as "not required" for this slot

    /** For lab blocks: same lab_block_id links the 3 consecutive slots. */
    @Column(name = "lab_block_id")
    private String labBlockId;

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
