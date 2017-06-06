package com.example.groupfourtwo.bluetoothsensorapp.Data;

/**
 * Sets predefined parameters for each type of data collected by the application.
 *
 * @author Stefan Erk
 */

public enum Measure {
    BRIGHTNESS (DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS),
    DISTANCE (DatabaseContract.MeasurementData.COLUMN_DISTANCE),
    HUMIDITY (DatabaseContract.MeasurementData.COLUMN_HUMIDITY),
    PRESSURE (DatabaseContract.MeasurementData.COLUMN_PRESSURE),
    TEMPERATURE (DatabaseContract.MeasurementData.COLUMN_TEMPERATURE);

    public final String column;

    Measure(String column) {
        this.column = column;
    }
}
