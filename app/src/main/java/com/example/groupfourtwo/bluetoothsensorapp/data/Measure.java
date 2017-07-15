package com.example.groupfourtwo.bluetoothsensorapp.data;

import static android.graphics.Color.*;
import static com.example.groupfourtwo.bluetoothsensorapp.data.DatabaseContract.MeasurementData.*;

/**
 * Sets predefined parameters for each type of data collected by the application.
 *
 * @author Stefan Erk
 */

public enum Measure {
    BRIGHTNESS  (YELLOW, COLUMN_BRIGHTNESS,  "lx" ),
    DISTANCE    (BLACK,  COLUMN_DISTANCE,    "m"  ),
    HUMIDITY    (CYAN,   COLUMN_HUMIDITY,    "%"  ),
    PRESSURE    (LTGRAY, COLUMN_PRESSURE,    "hPa"),
    TEMPERATURE (RED,    COLUMN_TEMPERATURE, "°C" );


    /**
     * The color that is used to draw the graph.
     */
    public final int color;

    /**
     * The column of the database the measure is stored in.
     */
    public final String column;

    /**
     * The unit of the measure.
     */
    public final String unit;

    /**
     * Initializes the Interval constants with their values.
     *
     * @param color   the color used in the graph
     * @param column  the name of the referenced column in the database
     * @param unit    the physical unit in which the data is taken
     */
    Measure(int color, String column, String unit) {
        this.color = color;
        this.column = column;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
}
