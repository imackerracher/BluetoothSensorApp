package com.example.groupfourtwo.bluetoothsensorapp.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

/**
 * Central interface between persistent data of the database and the running application.
 * Contains methods to access the database and to save and access data.
 *
 * @author Stefan Erk
 * @version 1.2
 */

public class DataManager {

    public enum Measure {BRIGHTNESS, DISTANCE, HUMIDITY, PRESSURE, TEMPERATURE}
    public enum Interval {HOUR, DAY, WEEK}

    private static final long MILLIS_PER_HOUR = 3600000L;
    private static final long MILLIS_PER_DAY = 86400000L;
    private static final long MILLIS_PER_WEEK = 604800000L;

    /* debugging only */
    private static final String LOG_TAG = DataManager.class.getSimpleName();

    /**
     * An SQLite database the application is connected to that contains all persistent data.
     */
    private SQLiteDatabase database;

    /**
     * Takes care of the connection to the database.
     */
    private DatabaseHelper dbHelper;

    /**
     * A cache of all records that have been referenced in this session.
     */
    private RecordHashMap records  = new RecordHashMap();

    /**
     * A cache of all sensors that have been referenced in this session.
     */
    private SensorHashMap sensors = new SensorHashMap();

    /**
     * A cache of all users that have been referenced in this session.
     */
    private UserHashMap users = new UserHashMap();


    /**
     * Creates a new DataManager object. Uses its context to create a new database helper.
     *
     * @param context  context of the calling activity
     */
    public DataManager(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
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
            throw e;
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
     * Get the measurement that was inserted lastly into the database.
     *
     * @return  the measurement lastly inserted
     */
    public Measurement getLatestMeasurement() {
        String[] select = {DatabaseContract.MeasurementData._ID,
                DatabaseContract.MeasurementData.COLUMN_RECORD_ID,
                DatabaseContract.MeasurementData.COLUMN_TIME,
                DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS,
                DatabaseContract.MeasurementData.COLUMN_DISTANCE,
                DatabaseContract.MeasurementData.COLUMN_HUMIDITY,
                DatabaseContract.MeasurementData.COLUMN_PRESSURE,
                DatabaseContract.MeasurementData.COLUMN_TEMPERATURE};

        // SELECT * FROM MEASUREMENT ORDER BY ID DESC
        Cursor cursor = database.query(DatabaseContract.MeasurementData.TABLE_MEASUREMENT, select,
                null, null, null, null, DatabaseContract.MeasurementData._ID + " DESC");

        if (!cursor.moveToFirst()) // empty cursor -> not a single entry in database
            return null;

        Measurement measurement = cursorToMeasurement(cursor);
        cursor.close();

        Log.d(LOG_TAG, "Retrieved latest measurement " + measurement.getId() + " from database.");
        return measurement;
    }


    /**
     * Retrieve a collection of all values of a certain measure and interval of time.
     *
     * @param measure   the measure of values
     * @param interval  the interval to retrieve
     * @return  all values in the interval
     */
    public ArrayList<Float> getValueInterval(Measure measure, Interval interval, Date begin) {
        Date end;
        int step;
        switch (interval) {
            case HOUR:
                end = new Date(begin.getTime() + MILLIS_PER_HOUR);
                step = 1000;
                break;
            case DAY:
                end = new Date(begin.getTime() + MILLIS_PER_DAY);
                step = 10000;
                break;
            case WEEK:
                end = new Date(begin.getTime() + MILLIS_PER_WEEK);
                step = 60000;
                break;
            default:
                throw new IllegalArgumentException("undefined Interval");
        }

        String column;
        switch (measure) {
            case BRIGHTNESS:
                column = DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS;
                break;
            case DISTANCE:
                column = DatabaseContract.MeasurementData.COLUMN_DISTANCE;
                break;
            case HUMIDITY:
                column = DatabaseContract.MeasurementData.COLUMN_HUMIDITY;
                break;
            case PRESSURE:
                column = DatabaseContract.MeasurementData.COLUMN_PRESSURE;
                break;
            case TEMPERATURE:
                column = DatabaseContract.MeasurementData.COLUMN_TEMPERATURE;
                break;
            default:
                throw new IllegalArgumentException("undefined Measure");
        }

        return getValueInterval(column, begin, end, step);
    }


    /**
     * Retrieve a collection of the same measure in a certain interval with variable step size.
     *
     * @param column  the column of the measure
     * @param begin   the start point of the interval
     * @param end     the end point of the interval
     * @param step    the step size to extract values
     * @return  all values in the interval, empty list if no entry was found
     */
    private ArrayList<Float> getValueInterval(String column, Date begin, Date end, int step) {
        String[] select = {DatabaseContract.MeasurementData.COLUMN_TIME, column};

        String where = DatabaseContract.MeasurementData.COLUMN_TIME + " BETWEEN " +
                begin.getTime() + " AND " + end.getTime();

        // SELECT TIME, column FROM MEASUREMENT WHERE TIME BETWEEN begin AND end ORDER BY TIME
        Cursor cursor = database.query(DatabaseContract.MeasurementData.TABLE_MEASUREMENT, select,
                where, null, null, null, DatabaseContract.MeasurementData.COLUMN_TIME);

        int indexTime = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_TIME);
        int indexValue = cursor.getColumnIndex(column);

        // The length of the requested interval determines number of data points.
        int duration = (int) (end.getTime() - begin.getTime());
        ArrayList<Float> data = new ArrayList<>(duration / step);

        if (!cursor.moveToFirst())
            return data;

        for (long i = begin.getTime(); i < end.getTime(); i += step) {
            int noOfValues = 0;
            float sum = 0f;
            // Take arithmetic mean of all values that are in one step size.
            while (!cursor.isAfterLast() && cursor.getLong(indexTime) < i+step) {
                sum += cursor.getFloat(indexValue);
                ++noOfValues;
                cursor.moveToNext();
            }
            // Save value or else mark as missing.
            if (noOfValues == 0)
                data.add(null);
            else
                data.add(sum / noOfValues);
        }
        cursor.close();
        return data;
    }


    /**
     * Calculate the total number of measurements that are saved in the database.
     *
     * @return  number of saved measurements
     */
    public int getNoOfMeasurements() {
        String[] select = {DatabaseContract.MeasurementData._ID};

        // SELECT COUNT(ID) FROM MEASUREMENT
        Cursor cursor = database.query(DatabaseContract.MeasurementData.TABLE_MEASUREMENT,
                select, null, null, null, null, null);

        int result = cursor.getCount();
        cursor.close();
        return result;
    }


    /**
     * Calculate the total number of records that are saved in the database.
     *
     * @return  number of saved records
     */
    public int getNoOfRecords() {
        String[] select = {DatabaseContract.RecordData._ID};

        // SELECT COUNT(ID) FROM RECORD
        Cursor cursor = database.query(DatabaseContract.RecordData.TABLE_RECORD,
                select, null, null, null, null, null);

        int result = cursor.getCount();
        cursor.close();
        return result;
    }


    /**
     * Calculate the total number of sensors that are saved in the database.
     *
     * @return  number of saved sensors
     */
    public int getNoOfSensors() {
        String[] select = {DatabaseContract.SensorData._ID};

        // SELECT COUNT(ID) FROM SENSOR
        Cursor cursor = database.query(DatabaseContract.SensorData.TABLE_SENSOR,
                select, null, null, null, null, null);

        int result = cursor.getCount();
        cursor.close();
        return result;
    }


    /**
     * Calculate the total number of users that are saved in the database.
     *
     * @return  number of saved users
     */
    public int getNoOfUsers() {
        String[] select = {DatabaseContract.UserData._ID};

        // SELECT COUNT(ID) FROM USER
        Cursor cursor = database.query(DatabaseContract.UserData.TABLE_USER,
                select, null, null, null, null, null);

        int result = cursor.getCount();
        cursor.close();
        return result;
    }


    /**
     * Insert a new measurement and its information into the database.
     * Note: id will be ignored by database and replaced with insertion id.
     *
     * @param measurement  the measurement to save
     */
    public void saveMeasurement(Measurement measurement) {
        if (measurement.getId() != -1)
            throw new IllegalArgumentException("Measurement was already inserted some other time.");

        ContentValues values = new ContentValues();

        values.put(DatabaseContract.MeasurementData.COLUMN_RECORD_ID, measurement.getRecord().getId());
        values.put(DatabaseContract.MeasurementData.COLUMN_TIME, measurement.getTime().getTime());
        values.put(DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS, measurement.getBrightness());
        values.put(DatabaseContract.MeasurementData.COLUMN_DISTANCE, measurement.getDistance());
        values.put(DatabaseContract.MeasurementData.COLUMN_HUMIDITY, measurement.getHumidity());
        values.put(DatabaseContract.MeasurementData.COLUMN_PRESSURE, measurement.getPressure());
        values.put(DatabaseContract.MeasurementData.COLUMN_TEMPERATURE, measurement.getTemperature());

        // INSERT INTO MEASUREMENT VALUES (record, time, ..., pressure, temperature)
        long id = database.insert(DatabaseContract.MeasurementData.TABLE_MEASUREMENT, null, values);
        Log.d(LOG_TAG, "Inserted new measurement: " + id);

        measurement.setId(id);
    }


    /**
     * Insert a new record into the database.
     *
     * @param record  the record to save
     */
    public void saveRecord(Record record) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.RecordData.COLUMN_SENSOR_ID, record.getSensor().getId());
        values.put(DatabaseContract.RecordData.COLUMN_USER_ID, record.getUser().getId());
        values.put(DatabaseContract.RecordData.COLUMN_BEGIN, record.getBegin().getTime());
        values.put(DatabaseContract.RecordData.COLUMN_END, record.getEnd().getTime());

        // INSERT INTO RECORD VALUES (sensor, user, begin, end)
        long id = database.insert(DatabaseContract.RecordData.TABLE_RECORD, null, values);
        Log.d(LOG_TAG, "Inserted new record: " + id + " " + record.getId());

        record.setId(id);
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

        // INSERT INTO SENSOR VALUES (id, name, knownSince)
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

        // INSERT INTO USER VALUES (id, name)
        long id = database.insert(DatabaseContract.UserData.TABLE_USER, null, values);
        Log.d(LOG_TAG, "Inserted new user: " + id);
    }


    /**
     * Alter the name of a sensor.
     *
     * @param sensor  the sensor to rename
     * @param name    the new name
     */
    public void renameSensor(Sensor sensor, String name) {
        sensor.setName(name);
        updateSensor(sensor);
    }


    /**
     * Alter the name of a user.
     *
     * @param user  the user to rename
     * @param name  the new name
     */
    public void renameUser(User user, String name) {
        user.setName(name);
        updateUser(user);
    }


    /**
     * Replace the data of a sensor with a new version.
     *
     * @param sensor  the new version of the sensor
     */
    private void updateSensor(Sensor sensor) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.SensorData._ID, sensor.getId());
        values.put(DatabaseContract.SensorData.COLUMN_NAME, sensor.getName());
        values.put(DatabaseContract.SensorData.COLUMN_KNOWN_SINCE, sensor.getKnownSince().getTime());

        // UPDATE SENSOR SET NAME = name, KNOWN_SINCE = knownSince WHERE ID = id
        long id = database.update(DatabaseContract.SensorData.TABLE_SENSOR, values,
                DatabaseContract.SensorData._ID + " = " + sensor.getId(), null);
        Log.d(LOG_TAG, "Updated sensor: " + id);
    }


    /**
     * Replace the data of a user with a new version.
     *
     * @param user  the new version of the user
     */
    private void updateUser(User user) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.UserData._ID, user.getId());
        values.put(DatabaseContract.UserData.COLUMN_NAME, user.getName());

        // UPDATE USER SET NAME = name WHERE ID = id
        long id = database.update(DatabaseContract.UserData.TABLE_USER, values,
                DatabaseContract.UserData._ID + " = " + user.getId(), null);
        Log.d(LOG_TAG, "Updated user: " + id);
    }


    /**
     * Retrieve the measured values and meta data of a whole measurement from a database request.
     *
     * @param cursor  cursor pointing to the desired entry
     * @return  measurement containing the given row
     */
    private Measurement cursorToMeasurement(Cursor cursor) {
        int indexID = cursor.getColumnIndex(DatabaseContract.MeasurementData._ID);
        int indexRecord = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_RECORD_ID);
        int indexTime = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_TIME);
        int indexBrightness = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS);
        int indexDistance = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_DISTANCE);
        int indexHumidity = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_HUMIDITY);
        int indexPressure = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_PRESSURE);
        int indexTemperature = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_TEMPERATURE);

        long id = cursor.getLong(indexID);
        Record record = records.getOrPut(cursor.getLong(indexRecord));
        Date time = new Date(cursor.getLong(indexTime));
        float brightness = cursor.getFloat(indexBrightness);
        float distance = cursor.getFloat(indexDistance);
        float humidity = cursor.getFloat(indexHumidity);
        float pressure = cursor.getFloat(indexPressure);
        float temperature = cursor.getFloat(indexTemperature);

        return new Measurement(
                id, record, time, brightness, distance, humidity, pressure, temperature);
    }


    /**
     * Creates a new record object from the information retrieved of a table entry.
     *
     * @param cursor  the cursor pointing to table entry
     * @return  record object from given row
     */
    private Record cursorToRecord(Cursor cursor) {
        int indexID = cursor.getColumnIndex(DatabaseContract.RecordData._ID);
        int indexSensor = cursor.getColumnIndex(DatabaseContract.RecordData.COLUMN_SENSOR_ID);
        int indexUser = cursor.getColumnIndex(DatabaseContract.RecordData.COLUMN_USER_ID);
        int indexBegin = cursor.getColumnIndex(DatabaseContract.RecordData.COLUMN_BEGIN);
        int indexEnd = cursor.getColumnIndex(DatabaseContract.RecordData.COLUMN_END);

        long id = cursor.getLong(indexID);
        Sensor sensor = sensors.getOrPut(cursor.getLong(indexSensor));
        User user = users.getOrPut(cursor.getLong(indexUser));
        Date begin = new Date(cursor.getLong(indexBegin));
        Date end = new Date(cursor.getLong(indexEnd));

        return new Record(id, sensor, user, begin, end, false);
    }


    /**
     * Creates a new sensor object from the information retrieved of a table entry.
     *
     * @param cursor  the cursor pointing to table entry
     * @return  sensor object from given row
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
     * @return  user object from given row
     */
    private User cursorToUser(Cursor cursor) {
        int indexID = cursor.getColumnIndex(DatabaseContract.UserData._ID);
        int indexName = cursor.getColumnIndex(DatabaseContract.UserData.COLUMN_NAME);

        long id = cursor.getLong(indexID);
        String name = cursor.getString(indexName);

        return new User(id, name);
    }


    /**
     * Adds automatic inserting to a HashMap of records in case the item was not found.
     */
    private class RecordHashMap extends HashMap<Long, Record> {
        /**
         * Find and return a record with the given key in the map.
         * If no object was found, fetch record from database and insert it into the map.
         *
         * @param key  id of the record to search
         * @return  the record with the given id
         */
        private Record getOrPut(long key) {
            Record record = this.get(key);
            if (record == null) // wanted sensor not in map -> search in database
                record = findRecord(key);
            if (record != null) { // wanted sensor was found -> cache and return
                records.put(record.getId(), record);
                Log.d(LOG_TAG, "Added record " + record.getId() + " to cache.");
            }
            return record;
        }
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
        private Sensor getOrPut(long key) {
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
        private User getOrPut(long key) {
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
        String[] select = {DatabaseContract.MeasurementData._ID,
                DatabaseContract.MeasurementData.COLUMN_RECORD_ID,
                DatabaseContract.MeasurementData.COLUMN_TIME,
                DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS,
                DatabaseContract.MeasurementData.COLUMN_DISTANCE,
                DatabaseContract.MeasurementData.COLUMN_HUMIDITY,
                DatabaseContract.MeasurementData.COLUMN_PRESSURE,
                DatabaseContract.MeasurementData.COLUMN_TEMPERATURE};

        // SELECT * FROM MEASUREMENT WHERE ID = id
        Cursor cursor = database.query(DatabaseContract.MeasurementData.TABLE_MEASUREMENT, select,
                DatabaseContract.MeasurementData._ID + " = " + id, null, null, null, null);

        if (!cursor.moveToFirst()) // empty cursor -> object not in database
            return null;

        Measurement measurement = cursorToMeasurement(cursor);
        cursor.close();

        Log.d(LOG_TAG, "Retrieved measurement " + id + " from database.");
        return measurement;
    }


    /**
     * Search for and return the record with the given id in the database.
     *
     * @param id  the id of the wanted record
     * @return  the wanted record
     */
    private Record findRecord(long id) {
        String[] select = {DatabaseContract.RecordData._ID,
                DatabaseContract.RecordData.COLUMN_SENSOR_ID,
                DatabaseContract.RecordData.COLUMN_USER_ID,
                DatabaseContract.RecordData.COLUMN_BEGIN,
                DatabaseContract.RecordData.COLUMN_END};

        // SELECT * FROM RECORD WHERE ID = id
        Cursor cursor = database.query(DatabaseContract.RecordData.TABLE_RECORD, select,
                DatabaseContract.RecordData._ID + " = " + id, null, null, null, null);

        if (!cursor.moveToFirst()) { // empty cursor -> object not in database
            Log.d(LOG_TAG, "Record " + id + " not found");
            return null;
        }

        Record record = cursorToRecord(cursor);
        cursor.close();

        Log.d(LOG_TAG, "Retrieved record " + id + " from database.");
        return record;
    }


    /**
     * Search for and return the sensor with the given id in the database.
     *
     * @param id  the id of the wanted sensor
     * @return  the wanted sensor
     */
    private Sensor findSensor(long id) {
        String[] select = {DatabaseContract.SensorData._ID,
                DatabaseContract.SensorData.COLUMN_NAME,
                DatabaseContract.SensorData.COLUMN_KNOWN_SINCE};

        // SELECT * FROM SENSOR WHERE ID = id
        Cursor cursor = database.query(DatabaseContract.SensorData.TABLE_SENSOR, select,
                DatabaseContract.SensorData._ID + " = " + id, null, null, null, null);

        if (!cursor.moveToFirst()) { // empty cursor -> object not in database
            Log.d(LOG_TAG, "Sensor " + id + " not found");
            return null;
        }

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
        String[] select = {DatabaseContract.UserData._ID,
                DatabaseContract.UserData.COLUMN_NAME};

        // SELECT * FROM USER WHERE ID = id
        Cursor cursor = database.query(DatabaseContract.UserData.TABLE_USER, select,
                DatabaseContract.UserData._ID + " = " + id, null, null, null, null);

        if (!cursor.moveToFirst()) {// empty cursor -> object not in database
            Log.d(LOG_TAG, "User " + id + " not found");
            return null;
        }

        User user = cursorToUser(cursor);
        cursor.close();

        Log.d(LOG_TAG, "Retrieved user " + id + " from database.");
        return user;
    }

}
