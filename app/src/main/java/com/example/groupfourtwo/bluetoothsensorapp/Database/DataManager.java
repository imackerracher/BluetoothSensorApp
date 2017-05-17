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

import java.util.HashMap;
import java.util.Date;

/**
 * Central interface between persistent data of the database and the running application.
 * Contains methods to access the database and to save and access data.
 *
 * @author Stefan Erk
 * @version 1.1
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
     * A cache of all sensors that have been referenced in this session.
     */
    private SensorHashMap sensors;

    /**
     * A cache of all users that have been referenced in this session.
     */
    private UserHashMap users;

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
     * Close the connection to an open SQLite database.
     */
    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Database was closed.");
    }


    /**
     * Calculate the total number of measurements that are saved in the database.
     *
     * @return  number of saved measurements
     */
    public int getNoOfMeasurements() {
        String[] column = {DatabaseContract.MeasurementData._ID};
        Cursor cursor = database.query(DatabaseContract.MeasurementData.TABLE_MEASUREMENT,
                column, null, null, null, null, null);
        int result = cursor.getCount();
        cursor.close();
        return result;
    }


    /**
     * Insert a new measurement with its information into the database.
     *
     * @param measurement  the measurement to save
     */
    public void saveMeasurement(Measurement measurement) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.MeasurementData.COLUMN_SENSOR_ID, measurement.getSensor().getId());
        values.put(DatabaseContract.MeasurementData.COLUMN_USER_ID, measurement.getUser().getId());
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
     * Insert a new user into the database.
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


    public void updateSensor(Sensor sensor) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.SensorData._ID, sensor.getId());
        values.put(DatabaseContract.SensorData.COLUMN_NAME, sensor.getName());
        values.put(DatabaseContract.SensorData.COLUMN_KNOWN_SINCE, sensor.getKnownSince().getTime());

        long id = database.update(DatabaseContract.SensorData.TABLE_SENSOR, values,
                DatabaseContract.SensorData._ID + " =" + sensor.getId(), null);
        Log.d(LOG_TAG, "Updated sensor: " + id);
    }


    /**
     * Retrieve the measured values and meta data of a whole measurement from a database request.
     *
     * @param cursor  cursor pointing to the desired entry
     * @return  measurement containing the row's data
     */
    private Measurement cursorToMeasurement(Cursor cursor) {
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
        Sensor sensor = sensors.getOrPut(cursor.getLong(indexSensor));
        User user = users.getOrPut(cursor.getLong(indexUser));
        Date time = new Date(cursor.getLong(indexTime));
        float brightness = cursor.getFloat(indexBrightness);
        float distance = cursor.getFloat(indexDistance);
        float humidity = cursor.getFloat(indexHumidity);
        float pressure = cursor.getFloat(indexPressure);
        float temperature = cursor.getFloat(indexTemperature);

        return new Measurement(
                id, sensor, user, time, brightness, distance, humidity, pressure, temperature);
    }


    /**
     * Creates a new sensor object from the information retrieved of a table entry.
     *
     * @param cursor  the cursor pointing to table entry
     * @return  sensor object from row
     */
    private Sensor cursorToSensor(Cursor cursor) {
        int indexID = cursor.getColumnIndex(DatabaseContract.SensorData._ID);
        int indexName = cursor.getColumnIndex(DatabaseContract.SensorData.COLUMN_NAME);
        int indexKnownSince = cursor.getColumnIndex(DatabaseContract.SensorData.COLUMN_KNOWN_SINCE);

        long id = cursor.getLong(indexID);
        String name = cursor.getString(indexName);
        Date knownSince = new Date(cursor.getLong(indexKnownSince));

        return new Sensor(id, name, knownSince);
    }


    /**
     * Creates a new user object from the information retrieved of a table entry.
     *
     * @param cursor  the cursor pointing to table entry
     * @return  user object from row
     */
    private User cursorToUser(Cursor cursor) {
        int indexID = cursor.getColumnIndex(DatabaseContract.UserData._ID);
        int indexName = cursor.getColumnIndex(DatabaseContract.UserData.COLUMN_NAME);

        long id = cursor.getLong(indexID);
        String name = cursor.getString(indexName);

        return new User(id, name);
    }


    /**
     * Adds automatic inserting to a HashMap of sensors in case the item was not found.
     */
    private class SensorHashMap extends HashMap<Long, Sensor> {
        /**
         * Find and return a sensor with the given key in the map.
         * If no object was found, fetch sensor from database and insert it into the map.
         *
         * @param key  id of the sensor to search
         * @return  the sensor with the given id
         */
        private Sensor getOrPut(Long key) {
            Sensor sensor = this.get(key);
            if (sensor == null) // wanted sensor not in map -> search in database
                sensor = findSensor(key);
            if (sensor != null) { // wanted sensor was found -> cache and return
                sensors.put(sensor.getId(), sensor);
                Log.d(LOG_TAG, "Added sensor " + sensor.getId() + " to cache.");
            }
            return sensor;
        }
    }


    /**
     * Adds automatic inserting to a HashMap of users in case the item was not found.
     */
    private class UserHashMap extends HashMap<Long, User> {
        /**
         * Find and return a user with the given key in the map.
         * If no object was found, fetch user from database and insert it into the map.
         *
         * @param key  id of the user to search
         * @return  the user with the given id
         */
        private User getOrPut(Long key) {
            User user = this.get(key);
            if (user == null) // wanted user not in map -> search in database
                user = findUser(key);
            if (user != null) { // wanted user was found -> cache and return it
                users.put(user.getId(), user);
                Log.d(LOG_TAG, "Added user " + user.getId() + " to cache.");
            }
            return user;
        }
    }


    /**
     * Search for and return the measurement with the given id in the database.
     *
     * @param id  the id of the wanted measurement
     * @return  the wanted measurement
     */
    private Measurement findMeasurement(long id) {
        String[] columns = {DatabaseContract.MeasurementData._ID,
                DatabaseContract.MeasurementData.COLUMN_SENSOR_ID,
                DatabaseContract.MeasurementData.COLUMN_USER_ID,
                DatabaseContract.MeasurementData.COLUMN_TIME,
                DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS,
                DatabaseContract.MeasurementData.COLUMN_DISTANCE,
                DatabaseContract.MeasurementData.COLUMN_HUMIDITY,
                DatabaseContract.MeasurementData.COLUMN_PRESSURE,
                DatabaseContract.MeasurementData.COLUMN_TEMPERATURE};

        Cursor cursor = database.query(DatabaseContract.MeasurementData.TABLE_MEASUREMENT, columns,
                DatabaseContract.MeasurementData._ID + " = " + id, null, null, null, null);

        if (!cursor.moveToFirst()) // empty cursor -> object not in database
            return null;

        Measurement measurement = cursorToMeasurement(cursor);
        cursor.close();

        Log.d(LOG_TAG, "Retrieved measurement " + id + " from database.");
        return measurement;
    }


    /**
     * Search for and return the sensor with the given id in the database.
     *
     * @param id  the id of the wanted sensor
     * @return  the wanted sensor
     */
    private Sensor findSensor(long id) {
        String[] columns = {DatabaseContract.SensorData._ID,
                DatabaseContract.SensorData.COLUMN_NAME,
                DatabaseContract.SensorData.COLUMN_KNOWN_SINCE};

        Cursor cursor = database.query(DatabaseContract.SensorData.TABLE_SENSOR, columns,
                DatabaseContract.SensorData._ID + " = " + id, null, null, null, null);

        if (!cursor.moveToFirst()) // empty cursor -> object not in database
            return null;

        Sensor sensor = cursorToSensor(cursor);
        cursor.close();

        Log.d(LOG_TAG, "Retrieved sensor " + id + " from database.");
        return sensor;
    }


    /**
     * Search for and return the user with the given id in the database.
     *
     * @param id  the id of the wanted user
     * @return  the wanted user
     */
    private User findUser(long id) {
        String[] columns = {DatabaseContract.UserData._ID,
                DatabaseContract.UserData.COLUMN_NAME};

        Cursor cursor = database.query(DatabaseContract.UserData.TABLE_USER, columns,
                DatabaseContract.UserData._ID + " = " + id, null, null, null, null);

        if (!cursor.moveToFirst()) // empty cursor -> object not in database
            return null;

        User user = cursorToUser(cursor);
        cursor.close();

        Log.d(LOG_TAG, "Retrieved user " + id + " from database.");
        return user;
    }

}
