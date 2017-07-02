package com.example.groupfourtwo.bluetoothsensorapp.data;

import static android.graphics.Color.*;
import static com.example.groupfourtwo.bluetoothsensorapp.data.DatabaseContract.*;

/**
 * Sets predefined parameters for each type of data collected by the application.
 *
 * @author Stefan Erk
 */

public enum Measure {
    BRIGHTNESS (YELLOW, MeasurementData.COLUMN_BRIGHTNESS, "lm"),
    DISTANCE (BLACK, MeasurementData.COLUMN_DISTANCE, "m"),
    HUMIDITY (CYAN, MeasurementData.COLUMN_HUMIDITY, "%"),
    PRESSURE (LTGRAY, MeasurementData.COLUMN_PRESSURE, "hPa"),
    TEMPERATURE (RED, MeasurementData.COLUMN_TEMPERATURE, "°C");


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

    Measure(int color, String column, String unit) {
        this.color = color;
        this.column = column;
        this.unit = unit;
    }



}
