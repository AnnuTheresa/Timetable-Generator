package com.timetable.config;

import java.util.List;

/**
 * Mon–Thu:
 *   0 = P1  09:00–10:00
 *   1 = Break              (non-assignable)
 *   2 = P2  10:15–11:15
 *   3 = P3  11:15–12:15
 *   4 = Lunch              (non-assignable)
 *   5 = P4  13:15–14:15
 *   6 = P5  14:15–15:15
 *   7 = P6  15:15–16:15
 *
 * Friday:
 *   0 = P1  09:00–09:50
 *   1 = P2  09:50–10:40
 *   2 = Break              (non-assignable)
 *   3 = P3  10:50–11:40
 *   4 = P4  11:40–12:30
 *   5 = Lunch              (non-assignable)
 *   6 = P5  14:00–15:00
 *   7 = P6  15:00–16:00
 */
public final class TimeFramework {

    public static final int DAYS_PER_WEEK   = 5;
    public static final int PERIODS_PER_DAY = 8; // includes break + lunch slots

    public static final List<Integer> MON_THU_ASSIGNABLE = List.of(0, 2, 3, 5, 6, 7);

    public static final List<Integer> FRIDAY_ASSIGNABLE  = List.of(0, 1, 3, 4, 6, 7);

    public static final List<Integer> MON_THU_NON_ASSIGNABLE = List.of(1, 4);

    public static final List<Integer> FRIDAY_NON_ASSIGNABLE  = List.of(2, 5);

    public static final int LAB_BLOCK_PERIODS = 3;

    public static final int FRIDAY = 5;

   
    public static String periodLabel(int day, int periodIndex) {
        if (day == FRIDAY) {
            return switch (periodIndex) {
                case 0 -> "09:00–09:50";
                case 1 -> "09:50–10:40";
                case 2 -> "Break";
                case 3 -> "10:50–11:40";
                case 4 -> "11:40–12:30";
                case 5 -> "Lunch";
                case 6 -> "14:00–15:00";
                case 7 -> "15:00–16:00";
                default -> "";
            };
        }
        // Mon–Thu
        return switch (periodIndex) {
            case 0 -> "09:00–10:00";
            case 1 -> "Break";
            case 2 -> "10:15–11:15";
            case 3 -> "11:15–12:15";
            case 4 -> "Lunch";
            case 5 -> "13:15–14:15";
            case 6 -> "14:15–15:15";
            case 7 -> "15:15–16:15";
            default -> "";
        };
    }

   
    public static boolean isNonAssignable(int day, int periodIndex) {
        if (day == FRIDAY) return periodIndex == 2 || periodIndex == 5;
        return periodIndex == 1 || periodIndex == 4;
    }

    private TimeFramework() {}
}