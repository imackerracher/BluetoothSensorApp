package com.example.groupfourtwo.bluetoothsensorapp.data;

import static android.graphics.Color.*;
import static com.example.groupfourtwo.bluetoothsensorapp.data.DatabaseContract.*;

/**
 * Sets predefined parameters for each type of data collected by the application.
 *
 * @author Stefan Erk
 */

public enum Measure {
    BRIGHTNESS (MAGENTA, MeasurementData.COLUMN_BRIGHTNESS),
    DISTANCE (BLACK, MeasurementData.COLUMN_DISTANCE),
    HUMIDITY (BLUE, MeasurementData.COLUMN_HUMIDITY),
    PRESSURE (GRAY, MeasurementData.COLUMN_PRESSURE),
    TEMPERATURE (RED, MeasurementData.COLUMN_TEMPERATURE);

    /**
     * The color that is used to draw the graph.
     */
    public final int color;

    /**
     * The column of the database the measure is stored in.
     */
    public final String column;

    Measure(int color, String column) {
        this.color = color;
        this.column = column;
    }
}
