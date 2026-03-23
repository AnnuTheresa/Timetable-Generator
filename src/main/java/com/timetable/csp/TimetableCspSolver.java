package com.timetable.csp;

import java.util.*;
import java.util.stream.Collectors;

public class TimetableCspSolver {
   
    private static final int FRIDAY = 5;
    private final CspContext context;
    private String lastFailureReason;

    public TimetableCspSolver(CspContext context) {
        this.context = context;
    }

    public String getLastFailureReason() { return lastFailureReason; }

    // ── Period index layout ──────────────────────────────────────────────────
    //
    // Mon–Thu  (8 slots, indices 0–7):
    //   0=P1, 1=Break, 2=P2, 3=P3, 4=Lunch, 5=P4, 6=P5, 7=P6
    //   Assignable: {0, 2, 3, 5, 6, 7}
    //   Lab morning  start=0 → {0, 2, 3}
    //   Lab afternoon start=5 → {5, 6, 7}
    //
    // Friday   (8 slots, indices 0–7):
    //   0=P1, 1=P2, 2=Break, 3=P3, 4=P4, 5=Lunch, 6=P5, 7=P6
    //   Assignable: {0, 1, 3, 4, 6, 7}
    //   Lab forenoon start=0 → {0, 1, 3, 4}  (4 periods, same hours as Mon–Thu 3-period block)
    //   Friday afternoon has only 2 slots → lab NOT allowed there
    // ────────────────────────────────────────────────────────────────────────

    /** Returns true if this period index is a break or lunch slot for the given day. */
    private static boolean isBreakOrLunch(int day, int period) {
        if (day == FRIDAY) return period == 2 || period == 5;
        return period == 1 || period == 4;
    }

    
    private static int[] labPeriods(int day, int start) {
        if (day == FRIDAY) return new int[]{0, 1, 3, 4};
        if (start == 0)    return new int[]{0, 2, 3};
        return                    new int[]{5, 6, 7};
    }

    /** All assignable (non-break, non-lunch) period indices for a day. */
    private static int[] assignablePeriods(int day) {
        if (day == FRIDAY) return new int[]{0, 1, 3, 4, 6, 7};
        return                    new int[]{0, 2, 3, 5, 6, 7};
    }

    
    private static int[] labStarts(int day) {
        return (day == FRIDAY) ? new int[]{0} : new int[]{0, 5};
    }


    public CspAssignment.Solution solve() {
        lastFailureReason = null;
        for (int attempt = 0; attempt < 500; attempt++) {
            List<CspAssignment> result = attempt(new Random(attempt * 13L + 11));
            if (result != null) {
                System.out.println("Solved on attempt " + (attempt + 1));
                return new CspAssignment.Solution(result, 0);
            }
        }
        lastFailureReason = "Could not generate timetable after 500 attempts.";
        return null;
    }


    private List<CspAssignment> attempt(Random rng) {

        
        Set<String> teacherBusy = new HashSet<>();
        Set<String> roomBusy    = new HashSet<>();
        Set<String> sectionBusy = new HashSet<>();

        
        if (context.getExistingAssignments() != null) {
            for (CspContext.ExistingAssignment ea : context.getExistingAssignments()) {
                if (ea.getTeacherId() != null)
                    teacherBusy.add(ea.getDayOfWeek() + "," + ea.getPeriodIndex()
                            + "," + ea.getTeacherId());
                if (ea.getRoomId() != null)
                    roomBusy.add(ea.getDayOfWeek() + "," + ea.getPeriodIndex()
                            + "," + ea.getRoomId());
                if (ea.getClassSectionId() != null)
                    sectionBusy.add(ea.getClassSectionId() + "," + ea.getDayOfWeek()
                            + "," + ea.getPeriodIndex());
            }
        }
        if (context.getNotRequiredSlots() != null) {
    for (CspContext.SlotKey nr : context.getNotRequiredSlots()) {
        sectionBusy.add(nr.getClassSectionId() + ","
                + nr.getDayOfWeek() + "," + nr.getPeriodIndex());
    }
}

        List<CspAssignment> allAssignments = new ArrayList<>();

        
    if (context.getLockedSlots() != null) {
    for (CspContext.LockedSlot ls : context.getLockedSlots()) {
        int[] periods = ls.getLabBlockId() != null
                ? labPeriods(ls.getDayOfWeek(), ls.getPeriodIndex())
                : new int[]{ls.getPeriodIndex()};

        for (int p : periods) {
            if (ls.getTeacherId() != null &&
                    teacherBusy.contains(ls.getDayOfWeek() + "," + p
                            + "," + ls.getTeacherId())) {
                lastFailureReason = "Locked slot conflict: Teacher ID "
                        + ls.getTeacherId()
                        + " is already assigned in another semester on day "
                        + ls.getDayOfWeek() + " period " + p
                        + ". Please unlock that slot before regenerating.";
                return null;
            }
            if (ls.getRoomId() != null &&
                    roomBusy.contains(ls.getDayOfWeek() + "," + p
                            + "," + ls.getRoomId())) {
                lastFailureReason = "Locked slot conflict: Room ID "
                        + ls.getRoomId()
                        + " is already in use in another semester on day "
                        + ls.getDayOfWeek() + " period " + p
                        + ". Please unlock that slot before regenerating.";
                return null;
            }
        }

        for (int p : periods) {
            sectionBusy.add(ls.getClassSectionId() + "," + ls.getDayOfWeek() + "," + p);
            if (ls.getTeacherId() != null)
                teacherBusy.add(ls.getDayOfWeek() + "," + p + "," + ls.getTeacherId());
            if (ls.getRoomId() != null)
                roomBusy.add(ls.getDayOfWeek() + "," + p + "," + ls.getRoomId());
        }

        allAssignments.add(CspAssignment.builder()
                .classSectionId(ls.getClassSectionId())
                .dayOfWeek(ls.getDayOfWeek())
                .periodIndex(ls.getPeriodIndex())
                .subjectId(ls.getSubjectId())
                .teacherId(ls.getTeacherId())
                .roomId(ls.getRoomId())
                .labBlockId(ls.getLabBlockId())
                .build());
    }
}

        for (CspContext.ClassSectionInfo section : context.getClassSections()) {
            long sid = section.getId();

            List<long[]> queue = buildQueue(sid, rng);
            if (queue == null) return null;

            List<long[]> labQueue    = queue.stream().filter(e -> e[3] == 1)
                    .collect(Collectors.toList());
            List<long[]> theoryQueue = queue.stream().filter(e -> e[3] == 0)
                    .collect(Collectors.toList());

            for (long[] labItem : labQueue) {
                long teacherId = labItem[1];
                Long roomId    = labItem[2] < 0 ? null : labItem[2];
                long subjectId = labItem[0];

                boolean placed = false;

                List<int[]> labOptions = new ArrayList<>();
                for (int day = 1; day <= 5; day++) {
                    for (int start : labStarts(day)) {
                        labOptions.add(new int[]{day, start});
                    }
                }
                Collections.shuffle(labOptions, rng);

                for (int[] opt : labOptions) {
                    int day   = opt[0];
                    int start = opt[1];
                    int[] lp  = labPeriods(day, start);

                    boolean ok = true;
                    for (int p : lp) {
                        if (sectionBusy.contains(sid + "," + day + "," + p))         { ok = false; break; }
                        if (teacherBusy.contains(day + "," + p + "," + teacherId))   { ok = false; break; }
                        if (roomId != null && roomBusy.contains(day+","+p+","+roomId)) { ok = false; break; }
                    }
                    if (!ok) continue;

                    
                    String labBlockId = "lab-" + subjectId + "-" + day + "-" + start;

                    for (int p : lp) {
                        sectionBusy.add(sid + "," + day + "," + p);
                        teacherBusy.add(day + "," + p + "," + teacherId);
                        if (roomId != null) roomBusy.add(day + "," + p + "," + roomId);
                        allAssignments.add(CspAssignment.builder()
                                .classSectionId(sid).dayOfWeek(day).periodIndex(p)
                                .subjectId(subjectId).teacherId(teacherId).roomId(roomId)
                                .labBlockId(labBlockId).build());
                    }
                    placed = true;
                    break;
                }

                if (!placed) return null; // retry
            }

            List<int[]> freeSlots = new ArrayList<>();
            for (int day = 1; day <= 5; day++) {
                for (int period : assignablePeriods(day)) {
                    if (!sectionBusy.contains(sid + "," + day + "," + period)) {
                        freeSlots.add(new int[]{day, period});
                    }
                }
            }
            freeSlots.sort((a, b) -> {
                int aPeriodRank = getPeriodRank(a[0], a[1]);
             int bPeriodRank = getPeriodRank(b[0], b[1]);
              if (aPeriodRank != bPeriodRank) return aPeriodRank - bPeriodRank;
              return Integer.compare(a[0], b[0]);
            });
          

            theoryQueue = interleave(theoryQueue);

            if (freeSlots.size() < theoryQueue.size()) {
                System.out.println("MISMATCH: section=" + sid
                        + " freeSlots=" + freeSlots.size()
                        + " theoryQueue=" + theoryQueue.size()
                        + " labQueue=" + labQueue.size());
                return null;
            }

           boolean[] itemPlaced = new boolean[theoryQueue.size()];
           int placedCount = 0;

           Map<Long, Set<Integer>> subjectDaysUsed = new HashMap<>();

    for (int si = 0; si < freeSlots.size(); si++) {
    if (si >= theoryQueue.size()) break;

    int day    = freeSlots.get(si)[0];
    int period = freeSlots.get(si)[1];

    Long prevSubj = getSubjectAt(allAssignments, sid, day, period - 1);
    Long nextSubj = getSubjectAt(allAssignments, sid, day, period + 1);

    long[] best       = null;
    int    bestIdx    = -1;
    boolean bestOnSameDay = false;

    for (int ti = 0; ti < theoryQueue.size(); ti++) {
        if (itemPlaced[ti]) continue;
        long[] item = theoryQueue.get(ti);
        long itemTeacherId = item[1];

        if (teacherBusy.contains(day + "," + period + "," + itemTeacherId)) continue;
        if (sectionBusy.contains(sid + "," + day + "," + period)) continue;

        boolean consecutive = (prevSubj != null && prevSubj == item[0])
                || (nextSubj != null && nextSubj == item[0]);
        if (consecutive) continue;

        boolean sameDay = subjectDaysUsed
                .getOrDefault(item[0], Collections.emptySet())
                .contains(day);

        if (best == null) {
            best = item;
            bestIdx = ti;
            bestOnSameDay = sameDay;
            if (!sameDay) break;
        } else if (!sameDay && bestOnSameDay) {
            best = item;
            bestIdx = ti;
            bestOnSameDay = false;
            break;
        }
    }

    if (best == null) {
        for (int ti = 0; ti < theoryQueue.size(); ti++) {
            if (itemPlaced[ti]) continue;
            long[] item = theoryQueue.get(ti);
            long itemTeacherId = item[1];
            if (teacherBusy.contains(day + "," + period + "," + itemTeacherId)) continue;
            if (sectionBusy.contains(sid + "," + day + "," + period)) continue;
            best = item;
            bestIdx = ti;
            break;
        }
    }

    if (best == null) return null;

    itemPlaced[bestIdx] = true;
    placedCount++;

    subjectDaysUsed.computeIfAbsent(best[0], k -> new HashSet<>()).add(day);

    long itemTeacherId = best[1];
    Long roomId        = best[2] < 0 ? null : best[2];

    sectionBusy.add(sid + "," + day + "," + period);
    teacherBusy.add(day + "," + period + "," + itemTeacherId);
    if (roomId != null) roomBusy.add(day + "," + period + "," + roomId);

    allAssignments.add(CspAssignment.builder()
            .classSectionId(sid).dayOfWeek(day).periodIndex(period)
            .subjectId(best[0]).teacherId(itemTeacherId).roomId(roomId)
            .build());
}

if (placedCount != theoryQueue.size()) return null;
        }

        return allAssignments;
    }


   

    private Long getSubjectAt(List<CspAssignment> assignments,
                               long sectionId, int day, int period) {
        for (CspAssignment a : assignments) {
            if (a.getClassSectionId() == sectionId
                    && a.getDayOfWeek() == day
                    && a.getPeriodIndex() == period) {
                return a.getSubjectId();
            }
        }
        return null;
    }

    private List<long[]> interleave(List<long[]> items) {
        Map<Long, List<long[]>> bySubject = new LinkedHashMap<>();
        for (long[] item : items) {
            bySubject.computeIfAbsent(item[0], k -> new ArrayList<>()).add(item);
        }
        List<long[]> result = new ArrayList<>();
        boolean added = true;
        while (added) {
            added = false;
            for (List<long[]> group : bySubject.values()) {
                if (!group.isEmpty()) {
                    result.add(group.remove(0));
                    added = true;
                }
            }
        }
        return result;
    }
private static int getPeriodRank(int day, int period) {
    int[] periods = assignablePeriods(day);
    for (int i = 0; i < periods.length; i++) {
        if (periods[i] == period) return i;
    }
    return period;
}

    
    private List<long[]> buildQueue(long sectionId, Random rng) {
    List<long[]> queue = new ArrayList<>();
    List<CspContext.ResourceInfo> labRooms =
            context.getLabRooms() != null ? context.getLabRooms() : List.of();

    Map<Long, Integer> lockedCountBySubject = new HashMap<>();
    if (context.getLockedSlots() != null) {
        for (CspContext.LockedSlot ls : context.getLockedSlots()) {
            if (ls.getClassSectionId().equals((Long) sectionId) && ls.getSubjectId() != null) {
                lockedCountBySubject.merge(ls.getSubjectId(), 1, Integer::sum);
            }
        }
    }

    for (CspContext.SubjectRequirement req : context.getSubjectRequirements()) {
        List<CspContext.AllocationInfo> allocs = context.getAllocations().stream()
                .filter(a -> Objects.equals(a.getSubjectId(), req.getSubjectId())
                          && Objects.equals(a.getClassSectionId(), sectionId))
                .collect(Collectors.toList());

        if (allocs.isEmpty()) {
            System.out.println("WARNING: No allocation found for subject="
                    + req.getSubjectId() + " section=" + sectionId);
            continue;
        }

        CspContext.AllocationInfo alloc = allocs.get(rng.nextInt(allocs.size()));

        int alreadyLocked = lockedCountBySubject.getOrDefault(req.getSubjectId(), 0);
        int toSchedule    = req.getPeriodsPerWeek() - alreadyLocked;

        if (toSchedule <= 0) continue;

        for (int i = 0; i < toSchedule; i++) {
            long roomId;
            if (req.isLab()) {
                if (alloc.getRoomId() != null) {
                    roomId = alloc.getRoomId();
                } else if (!labRooms.isEmpty()) {
                    roomId = labRooms.get(rng.nextInt(labRooms.size())).getId();
                } else {
                    roomId = -1;
                }
            } else {
                roomId = alloc.getRoomId() != null ? alloc.getRoomId() : -1;
            }

            queue.add(new long[]{
                    req.getSubjectId(),
                    alloc.getTeacherId(),
                    roomId,
                    req.isLab() ? 1L : 0L
            });
        }
    }
    return queue;
}
    public List<CspContext.LockedSlot> extractLockedSlots(List<CspAssignment> sol) {
        List<CspContext.LockedSlot> list = new ArrayList<>();
        for (CspAssignment a : sol)
            list.add(new CspContext.LockedSlot(
                    a.getClassSectionId(), a.getDayOfWeek(), a.getPeriodIndex(),
                    a.getSubjectId(), a.getTeacherId(), a.getRoomId(), a.getLabBlockId()));
        return list;
    }
}
