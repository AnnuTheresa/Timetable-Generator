package com.timetable.csp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result of CSP solving: one assignment per filled slot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CspAssignment {

    private Long classSectionId;
    private int dayOfWeek;
    private int periodIndex;
    private Long subjectId;
    private Long teacherId;
    private Long roomId;
    private String labBlockId; // for lab blocks: same id for 3 consecutive slots

    /** All assignments (including locked ones) in order. */
    public static class Solution {
        private List<CspAssignment> assignments;
        private double softScore; // higher is better

        public Solution(List<CspAssignment> assignments, double softScore) {
            this.assignments = assignments;
            this.softScore = softScore;
        }

        public List<CspAssignment> getAssignments() {
            return assignments;
        }

        public double getSoftScore() {
            return softScore;
        }
    }
}
