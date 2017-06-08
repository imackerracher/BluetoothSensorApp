package com.example.groupfourtwo.bluetoothsensorapp.Data;

import static com.example.groupfourtwo.bluetoothsensorapp.Data.DatabaseContract.*;

/**
 * Sets predefined parameters for each type of data collected by the application.
 *
 * @author Stefan Erk
 */

public enum Measure {
    BRIGHTNESS (MeasurementData.COLUMN_BRIGHTNESS),
    DISTANCE (MeasurementData.COLUMN_DISTANCE),
    HUMIDITY (MeasurementData.COLUMN_HUMIDITY),
    PRESSURE (MeasurementData.COLUMN_PRESSURE),
    TEMPERATURE (MeasurementData.COLUMN_TEMPERATURE);

    /**
     * The column of the database the measure is stored in.
     */
    public final String column;

    Measure(String column) {
        this.column = column;
    }
}
