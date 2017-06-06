package com.example.groupfourtwo.bluetoothsensorapp.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Central interface between persistent data of the database and the running application.
 * Contains methods to access the database and to save and access data.
 *
 * @author Stefan Erk
 * @version 1.2
 */

public class DataManager {

    public enum Measure {
        BRIGHTNESS (DatabaseContract.MeasurementData.COLUMN_BRIGHTNESS),
        DISTANCE (DatabaseContract.MeasurementData.COLUMN_DISTANCE),
        HUMIDITY (DatabaseContract.MeasurementData.COLUMN_HUMIDITY),
        PRESSURE (DatabaseContract.MeasurementData.COLUMN_PRESSURE),
        TEMPERATURE (DatabaseContract.MeasurementData.COLUMN_TEMPERATURE);

        private final String column;

        Measure(String column) {
            this.column = column;
        }
    }

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

    /* debugging only */
    private static final String LOG_TAG = DataManager.class.getSimpleName();

    /**
     * Single data manager object, to access and operate on the database.
     */
    private static DataManager instance;

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
    private DataManager(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }


    /**
     * Get the global instance of the Data Manager object.
     *
     * @param context  the context of the calling activity
     * @return  the instance of the data manager
     */
    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context.getApplicationContext());
            Log.d(LOG_TAG, "Created new data manager object.");
        }
        return instance;
    }


    /**
     * Open the connection to an SQLite database.
     * If there is no existing database yet, a new one is created.
     */
    public synchronized void open() {
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
    public synchronized void close() {
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
     * Retrieve a list of all values of a measure in a certain interval.
     *
     * @param measure   the measure of values to retrieve
     * @param interval  the interval to retrieve
     * @param begin     the start point of the interval
     * @return  all values in the interval, empty list if no entry was found
     */
    public ArrayList<Float> getValuesFromInterval(Measure measure, Interval interval, long begin) {
        long end = begin + interval.length;

        String[] select = {DatabaseContract.MeasurementData.COLUMN_TIME, measure.column};

        String where = DatabaseContract.MeasurementData.COLUMN_TIME + " BETWEEN " +
                begin + " AND " + end;

        // SELECT TIME, measure.column FROM MEASUREMENT WHERE TIME BETWEEN begin AND end ORDER BY TIME
        Cursor cursor = database.query(DatabaseContract.MeasurementData.TABLE_MEASUREMENT, select,
                where, null, null, null, DatabaseContract.MeasurementData.COLUMN_TIME);

        ArrayList<Float> data = cursorToList(cursor, measure.column, begin, end, interval.step);
        cursor.close();
        return data;
    }


    /**
     * Retrieve a list of the values of a measure from a certain record.
     *
     * @param measure  the measure of values to retrieve
     * @param record   the record to analyse
     * @return  all values of the record, empty list if no entry was found
     */
    public ArrayList<Float> getValuesFromRecord(Measure measure, Record record) {
        String[] select = {DatabaseContract.MeasurementData.COLUMN_TIME, measure.column};

        String where = DatabaseContract.MeasurementData.COLUMN_RECORD_ID + " = " + record.getId();

        // SELECT TIME, measure.column FROM MEASUREMENT WHERE RECORD_ID = record.id ORDER BY TIME
        Cursor cursor = database.query(DatabaseContract.MeasurementData.TABLE_MEASUREMENT, select,
                where, null, null, null, DatabaseContract.MeasurementData.COLUMN_TIME);

        long begin = record.getBegin();
        long end = record.getEnd();

        int step = Interval.HOUR.step;
        if (end - begin > Interval.HOUR.length)
            step = Interval.DAY.step;
        if (end - begin > Interval.DAY.length)
            step = Interval.WEEK.step;

        ArrayList<Float> data = cursorToList(cursor, measure.column, begin, end, step);
        cursor.close();
        return data;
    }


    /**
     * Fill a List with all values that a cursor contains. Group if multiple values at a time.
     *
     * @param cursor  the cursor pointing to the database
     * @param column  the column of values to retrieve
     * @param begin   the start point of data
     * @param end     the end point of data
     * @param step    the temporal resolution of results
     * @return  a list of values from the cursor
     */
    private ArrayList<Float> cursorToList(Cursor cursor, String column,
                                          long begin, long end, int step) {
        int indexTime = cursor.getColumnIndex(DatabaseContract.MeasurementData.COLUMN_TIME);
        int indexValue = cursor.getColumnIndex(column);

        // The length of the requested interval determines number of data points.
        int duration = (int) (end - begin);
        ArrayList<Float> data = new ArrayList<>(duration / step);

        if (!cursor.moveToFirst())
            return data;

        // Fill list with values from cursor according to timeline.
        for (long i = begin; i < end; i += step) {
            int noOfValues = 0;
            float sum = 0f;
            // Take arithmetic mean of all values that are in one step size.
            while (!cursor.isAfterLast()  &&  cursor.getLong(indexTime) < i + step) {
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
        values.put(DatabaseContract.MeasurementData.COLUMN_TIME, measurement.getTime());
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
        record.stop();

        ContentValues values = new ContentValues();

        values.put(DatabaseContract.RecordData.COLUMN_SENSOR_ID, record.getSensor().getId());
        values.put(DatabaseContract.RecordData.COLUMN_USER_ID, record.getUser().getId());
        values.put(DatabaseContract.RecordData.COLUMN_BEGIN, record.getBegin());
        values.put(DatabaseContract.RecordData.COLUMN_END, record.getEnd());

        // INSERT INTO RECORD VALUES (sensor, user, begin, end)
        long id = database.insert(DatabaseContract.RecordData.TABLE_RECORD, null, values);
        Log.d(LOG_TAG, "Inserted new record: " + id + " " + record.getId());

        record.setId(id);
        records.put(id, record);
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
        values.put(DatabaseContract.SensorData.COLUMN_KNOWN_SINCE, sensor.getKnownSince());

        // INSERT INTO SENSOR VALUES (id, name, knownSince)
        long id = database.insert(DatabaseContract.SensorData.TABLE_SENSOR, null, values);
        Log.d(LOG_TAG, "Inserted new sensor: " + id);

        sensors.put(sensor.getId(), sensor);
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

        users.put(user.getId(), user);
    }


    /**
     * Start a new record at this very moment.
     * Note: As the record is still running, no valid id nor end point is assigned yet.
     *
     * @param sensor  the associated sensor
     * @param user    the associated user
     * @return  a new running record
     */
    public Record startRecord(Sensor sensor, User user) {
        Record record = new Record(-1, sensor, user, System.currentTimeMillis(), Long.MIN_VALUE);
        saveRecord(record);
        return record;
    }


    /**
     * Stop a running Record.
     *
     * @param record  the record that shall be stopped
     */
    public void stopRecord(Record record) {
        record.stop();
        updateRecord(record);
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
        long time = cursor.getLong(indexTime);
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
        long begin = cursor.getLong(indexBegin);
        long end = cursor.getLong(indexEnd);

        return new Record(id, sensor, user, begin, end);
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
        long knownSince = cursor.getLong(indexKnownSince);

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


    /**
     * Replace the data of a record with a new version.
     *
     * @param record  the new version of the record
     */
    private void updateRecord(Record record) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.RecordData._ID, record.getId());
        values.put(DatabaseContract.RecordData.COLUMN_SENSOR_ID, record.getSensor().getId());
        values.put(DatabaseContract.RecordData.COLUMN_USER_ID, record.getUser().getId());
        values.put(DatabaseContract.RecordData.COLUMN_BEGIN, record.getBegin());
        values.put(DatabaseContract.RecordData.COLUMN_END, record.getEnd());

        // UPDATE SENSOR SET END = end WHERE ID = id
        long id = database.update(DatabaseContract.RecordData.TABLE_RECORD, values,
                DatabaseContract.RecordData._ID + " = " + record.getId(), null);
        Log.d(LOG_TAG, "Updated sensor: " + id);

        records.put(id, record);
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
        values.put(DatabaseContract.SensorData.COLUMN_KNOWN_SINCE, sensor.getKnownSince());

        // UPDATE SENSOR SET NAME = name, KNOWN_SINCE = knownSince WHERE ID = id
        long id = database.update(DatabaseContract.SensorData.TABLE_SENSOR, values,
                DatabaseContract.SensorData._ID + " = " + sensor.getId(), null);
        Log.d(LOG_TAG, "Updated sensor: " + id);

        sensors.put(id, sensor);
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

        users.put(id, user);
    }


    /**
     * Adds automatic inserting to a HashMap of records in case the item was not found.
     */
    public class RecordHashMap extends HashMap<Long, Record> {
        /**
         * Find and return a record with the given key in the map.
         * If no object was found, fetch record from database and insert it into the map.
         *
         * @param key  id of the record to search
         * @return  the record with the given id
         */
        public Record getOrPut(long key) {
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
    public class SensorHashMap extends HashMap<Long, Sensor> {
        /**
         * Find and return a sensor with the given key in the map.
         * If no object was found, fetch sensor from database and insert it into the map.
         *
         * @param key  id of the sensor to search
         * @return  the sensor with the given id
         */
        public Sensor getOrPut(long key) {
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
    public class UserHashMap extends HashMap<Long, User> {
        /**
         * Find and return a user with the given key in the map.
         * If no object was found, fetch user from database and insert it into the map.
         *
         * @param key  id of the user to search
         * @return  the user with the given id
         */
        public User getOrPut(long key) {
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
}
