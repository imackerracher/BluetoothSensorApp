package com.example.groupfourtwo.bluetoothsensorapp.data;

/**
 * Sets predefined parameters for standardized intervals to be displayed.
 *
 * @author Stefan Erk
 */

public enum Interval {
    //             length     step points
    HOUR  (      3600000L,    1000, 3600), // 1/s
    DAY   (     86400000L,   60000, 1440), // 1/min
    WEEK  (    604800000L,  600000, 1008), // 6/h
    MONTH (   2678400000L, 3600000,  744), // 1/h
    MAX   (Long.MAX_VALUE, 3600000, Integer.MAX_VALUE);


    /**
     * The length of the selected interval in milliseconds.
     */
    public final long length;

    /**
     * The temporal resolution in which multiple data is merged to one point in the graph.
     */
    public final int step;

    /**
     * The maximum number of data points, such that length = step * points.
     */
    public final int points;

    /**
     * Initializes the Interval constants with their values.
     *
     * @param length  the length of the interval
     * @param step    the length of one data unit
     * @param points  the maximum of aggregated data points
     */
    Interval(long length, int step, int points) {
        this.length = length;
        this.step = step;
        this.points = points;
    }


    /**
     * Decide which standardized Interval to use according to a given time span.
     *
     * @param length  the length of the given time span
     * @return  the Interval with the next bigger or equal length
     */
    public static Interval fromLength(long length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Interval length has to be positive.");
        } else if (length <= HOUR.length) {
            return HOUR;
        } else if (length <= DAY.length) {
            return DAY;
        } else if (length <= WEEK.length) {
            return WEEK;
        } else if (length <= MONTH.length) {
            return MONTH;
        } else {
            return MAX;
        }
    }
}
