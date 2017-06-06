package com.example.groupfourtwo.bluetoothsensorapp.Data;

/**
 * Sets predefined parameters for each possible interval to be displayed.
 *
 * @author Stefan Erk
 */

public enum Interval {
    HOUR (3600000, 1000),
    DAY (86400000, 10000),
    WEEK (604800000, 60000);

    public final int length;
    public final int step;

    Interval(int length, int step) {
        this.length = length;
        this.step = step;
    }
}
