package com.example.groupfourtwo.bluetoothsensorapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.groupfourtwo.bluetoothsensorapp.Data.Measurement;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Sensor;
import com.example.groupfourtwo.bluetoothsensorapp.Data.User;

import java.util.Date;

/**
 * Central interface between persistent data of the database and the running application.
 * Contains methods to access the database and to save and access data.
 *
 * @author Stefan Erk
 * @version 1.0
 */

public class DataManager {

    /* debugging only */
    private static final String LOG_TAG = DataManager.class.getSimpleName();

    /**
     * An SQLite database the application is connected to containing all persistent data.
     */
    private SQLiteDatabase database;

    /**
     * Takes care of the connection to the database.
     */
    private DatabaseHelper dbHelper;

    /**
     * Creates a new DataManager object. Passes its context to create a new database helper.
     *
     * @param context  context of the calling activity
     */
    public DataManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Open the connection to an SQLite database.
     * If there is no existing database yet, a new one is created.
     */
    public void open() {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d(LOG_TAG, "Successfully opened database from: " + database.getPath());
        }
        catch (SQLiteException e) {
            Log.e(LOG_TAG, "Error while opening database", e);
        }
    }

    /**
     * Closes the connection to an open SQLite database.
     */
    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Database was closed.");
    }

    /**
     * Insert a new measurement and its contained information into the database.
     *
     * @param measurement  the measurement to save
     */
    public void saveMeasurement(Measurement measurement) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.MeasurementData.COLUMN_SENSOR_ID, measurement.getSensor());
        values.put(DatabaseContract.MeasurementData.COLUMN_USER_ID, measurement.getUser());
        values.put(DatabaseContract.MeasurementData.COLUMN_TIME, measurement.getTime().getTime());
        values.put(DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS, measurement.getBrightness());
        values.put(DatabaseContract.MeasurementData.COLUMN_DISTANCE, measurement.getDistance());
        values.put(DatabaseContract.MeasurementData.COLUMN_HUMIDITY, measurement.getHumidity());
        values.put(DatabaseContract.MeasurementData.COLUMN_PRESSURE, measurement.getPressure());
        values.put(DatabaseContract.MeasurementData.COLUMN_TEMPERATURE, measurement.getTemperature());

        long id = database.insert(DatabaseContract.MeasurementData.TABLE_MEASUREMENT, null, values);
        Log.d(LOG_TAG, "Inserted new measurement: " + id);
    }

    /**
     * Calculate the total number of measurements that are saved in the database.
     *
     * @return  number of saved measurements
     */
    public int getNumberOfEntries() {
        String[] column = {DatabaseContract.MeasurementData._ID};
        Cursor cursor = database.query(DatabaseContract.MeasurementData.TABLE_MEASUREMENT,
                column, null, null, null, null, null);
        int result = cursor.getCount();
        cursor.close();
        return result;
    }

    /**
     * Insert a new sensor into the database.
     *
     * @param sensor  the sensor to save
     */
    public void saveSensor(Sensor sensor) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.SensorData._ID, sensor.getId());
        values.put(DatabaseContract.SensorData.COLUMN_NAME, sensor.getName());
        values.put(DatabaseContract.SensorData.COLUMN_KNOWN_SINCE, sensor.getKnownSince().getTime());

        long id = database.insert(DatabaseContract.SensorData.TABLE_SENSOR, null, values);
        Log.d(LOG_TAG, "Inserted new sensor: " + id);
    }

    /**
     * Insert a new user profile into the database.
     *
     * @param user  the user to save
     */
    public void saveUser(User user) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.UserData._ID, user.getId());
        values.put(DatabaseContract.UserData.COLUMN_NAME, user.getName());

        long id = database.insert(DatabaseContract.SensorData.TABLE_SENSOR, null, values);
        Log.d(LOG_TAG, "Inserted new user: " + id);
    }

    /**
     * Retrieve the measured values and meta data of a whole measurement from an database request.
     *
     * @param cursor  cursor pointing to the desired entry
     * @return  measurement containing the retrieved data
     */
    public Measurement cursorToMeasurement(Cursor cursor) {
        int indexID = cursor.getColumnIndex(DatabaseContract.MeasurementData._ID);
        int indexSensor = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_SENSOR_ID);
        int indexUser = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_USER_ID);
        int indexTime = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_TIME);
        int indexBrightness = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS);
        int indexDistance = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_DISTANCE);
        int indexHumidity = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_HUMIDITY);
        int indexPressure = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_PRESSURE);
        int indexTemperature = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_TEMPERATURE);

        long id = cursor.getLong(indexID);
        long sensor = cursor.getLong(indexSensor);
        long user = cursor.getLong(indexUser);
        Date time = new Date(cursor.getLong(indexTime));
        float brightness = cursor.getFloat(indexBrightness);
        float distance = cursor.getFloat(indexDistance);
        float humidity = cursor.getFloat(indexHumidity);
        float pressure = cursor.getFloat(indexPressure);
        float temperature = cursor.getFloat(indexTemperature);

        return new Measurement(
                id, sensor, user, time, brightness, distance, humidity, pressure, temperature);
    }
}
