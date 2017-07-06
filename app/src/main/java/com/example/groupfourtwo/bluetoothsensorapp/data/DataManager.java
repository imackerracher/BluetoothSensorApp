package com.example.groupfourtwo.bluetoothsensorapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.util.LongSparseArray;

import com.github.mikephil.charting.data.Entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.groupfourtwo.bluetoothsensorapp.data.DatabaseContract.*;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.*;

/**
 * Central interface between persistent data of the database and the running application.
 * Contains methods to access the database and to save and access data.
 *
 * @author Stefan Erk
 * @version 1.2
 */

public class DataManager {

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
    private LongSparseArray<Record> records  = new LongSparseArray<>();

    /**
     * A cache of all sensors that have been referenced in this session.
     */
    private LongSparseArray<Sensor> sensors = new LongSparseArray<>();

    /**
     * A cache of all users that have been referenced in this session.
     */
    private LongSparseArray<User> users = new LongSparseArray<>();


    /**
     * Creates a new DataManager object. Uses its context to create a new database helper.
     *
     * @param context  context of the calling activity
     */
    private DataManager(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }


    /**
     * Get an own instance of the Data Manager object.
     * <p>
     * Note: Use getInstance because of using application context.
     *
     * @param context  the context of the calling activity
     * @return  the instance of the data manager
     */
    public static synchronized DataManager getInstance(Context context) {
        DataManager newInstance = new DataManager(context.getApplicationContext());
        Log.d(LOG_TAG, "Created new data manager object.");
        return newInstance;
    }


    /**
     * Open the connection to an SQLite database.
     * <p>
     * If there is no existing database yet, a new one is created.
     */
    public synchronized void open() throws IOException {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d(LOG_TAG, "Successfully opened database from: " + database.getPath());
        }
        catch (SQLiteException e) {
            Log.e(LOG_TAG, "Error while opening database", e);
            throw new IOException(e);
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
     * Checks whether an entry of the given id exists in the sensor table already.
     *
     * @param id  the id of the sensor
     * @return  whether the sensor has an entry in the database
     */
    public boolean existsSensor(long id) {
        return  searchSensor(id) != null;
    }


    /**
     * Checks whether a user entry of the given id exists in the user table already.
     *
     * @param id  the id of the sensor
     * @return  whether the sensor has an entry in the database
     */
    public boolean existsUser(long id) {
        return  searchUser(id) != null;
    }


    /**
     * Fetch a collection of all records that have been saved in the database.
     *
     * @return  a list of all saved records
     */
    public ArrayList<Record> getAllRecords() {
        String[] select = RecordData.ALL_COLUMNS;

        // SELECT * FROM RECORD ORDER BY ID DESC
        Cursor cursor = database.query(RecordData.TABLE_RECORD, select,
                null, null, null, null, RecordData._ID + " DESC");

        ArrayList<Record> result = new ArrayList<>(cursor.getCount());

        // For every entry in the record table, insert that record into the list.
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Record record = cursorToRecord(cursor);
            result.add(record);
            cursor.moveToNext();
        }
        cursor.close();

        return result;
    }


    /**
     * Fetch a collection of all sensors that have been saved in the database.
     *
     * @return  a list of all saved sensors
     */
    public ArrayList<Sensor> getAllSensors() {
        String[] select = SensorData.ALL_COLUMNS;

        // SELECT * FROM SENSOR ORDER BY NAME
        Cursor cursor = database.query(SensorData.TABLE_SENSOR, select,
                null, null, null, null, SensorData.COLUMN_NAME);

        ArrayList<Sensor> result = new ArrayList<>(cursor.getCount());

        // For every entry in the sensor table, insert that sensor into the list.
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Sensor sensor = cursorToSensor(cursor);
            result.add(sensor);
            cursor.moveToNext();
        }
        cursor.close();

        return result;
    }


    /**
     * Find and return the {@link Record} with the given id.
     * <p>
     * If not already present in the cache, the record is fetched from the database and
     * inserted into the former.
     * If the database contains no matching row, a {@link Record#RECORD_DUMMY} is returned.
     *
     * @param id  id of the record to search
     * @return  the record with the given id or dummy if none was found
     */
    public Record findRecord(long id) {
        Record record = records.get(id);
        if (record == null) { // wanted sensor not in map -> search in database
            record = searchRecord(id);
            if (record != null) { // wanted sensor was found -> cache and return
                records.put(record.getId(), record);
                Log.d(LOG_TAG, "Added record " + record.getId() + " to cache.");
            } else {
                record = Record.RECORD_DUMMY;
            }
        }
        return record;
    }


    /**
     * Find and return the {@link Sensor} with the given id.
     * <p>
     * If not already present in the cache, the sensor is fetched from the database and
     * inserted into the former.
     * If the database contains no matching row, a {@link Sensor#SENSOR_DUMMY} is returned.
     *
     * @param id  id of the sensor to search
     * @return  the sensor with the given id or dummy if none was found
     */
    public Sensor findSensor(long id) {
        Sensor sensor = sensors.get(id);
        if (sensor == null) { // wanted sensor not in map -> search in database
            sensor = searchSensor(id);
            if (sensor != null) { // wanted sensor was found -> cache and return
                sensors.put(sensor.getId(), sensor);
                Log.d(LOG_TAG, "Added sensor " + sensor.getId() + " to cache.");
            } else {
                sensor = Sensor.SENSOR_DUMMY;
            }
        }
        return sensor;
    }


    /**
     * Find and return the {@link User} with the given id.
     * <p>
     * If not already present in the cache, the user is fetched from the database and
     * inserted into the former.
     * If the database contains no matching row, a {@link User#USER_DUMMY} is returned.
     *
     * @param id  id of the sensor to search
     * @return  the sensor with the given id or dummy if none was found
     */
    public User findUser(long id) {
        User user = users.get(id);
        if (user == null) { // wanted user not in map -> search in database
            user = searchUser(id);
            if (user != null) { // wanted user was found -> cache and return it
                users.put(user.getId(), user);
                Log.d(LOG_TAG, "Added user " + user.getId() + " to cache.");
            } else {
                user = User.USER_DUMMY;
            }
        }
        return user;
    }


    /**
     * Get the measurement that was inserted lastly into the database.
     *
     * @return  the measurement lastly inserted
     */
    public Measurement getLatestMeasurement() {
        String[] select = MeasurementData.ALL_COLUMNS;

        // SELECT * FROM MEASUREMENT ORDER BY ID DESC
        Cursor cursor = database.query(MeasurementData.TABLE_MEASUREMENT, select,
                null, null, null, null, MeasurementData._ID + " DESC");

        if (!cursor.moveToFirst()) { // empty cursor -> not a single entry in database
            return null;
        }

        Measurement measurement = cursorToMeasurement(cursor);
        cursor.close();

        Log.d(LOG_TAG, "Retrieved latest measurement " + measurement.getId() + " from database.");
        return measurement;
    }


    /**
     * Retrieve a list of all values of a certain measure in the given interval.
     *
     * @param measure  the measure of values to retrieve
     * @param begin    the start point of the interval
     * @param end      the end point of the interval
     * @return  all values in the interval, null if no entry was found
     */
    public ArrayList<Entry> getValuesFromInterval(Measure measure, long begin, long end) {
        if (end <= begin) {
            throw new IllegalArgumentException("End point must lay in future of begin.");
        }

        // Insert the variables into the template SQL statement.
        String query = String.format(Locale.ENGLISH, SQL_SELECT_VALUES_FROM_INTERVAL,
                fromLength(end - begin).step,
                measure.column
        );

        String[] args = {Long.toString(begin), Long.toString(end)};

        /* SELECT TIME / interval.step AS "STEPS", AVG(measure.column) AS "VALUE"
         * FROM MEASUREMENTS
         * WHERE TIME BETWEEN begin AND end
         * GROUP BY STEPS
         * SORT BY STEPS */
        Cursor cursor = database.rawQuery(query, args);

        long start = begin / fromLength(end - begin).step;
        ArrayList<Entry> data = cursorToList(cursor, start);

        cursor.close();
        return data;
    }


    /**
     * Retrieve a list of the values of a measure from a certain record.
     *
     * @param measure  the measure of values to retrieve
     * @param record   the record to analyse
     * @return  all values of the record, null if no entry was found
     */
    public ArrayList<Entry> getValuesFromRecord(Measure measure, Record record) {

        long begin = record.getBegin();
        // If record is still running, take all values received so far.
        long end = record.isRunning() ? System.currentTimeMillis() : record.getEnd();

        // Insert the variables into the template SQL statement.
        String query = String.format(Locale.ENGLISH, SQL_SELECT_VALUES_FROM_RECORD,
                fromLength(end - begin).step,
                measure.column
                );
        String[] args = {Long.toString(record.getId())};

        /* SELECT TIME / interval.step AS "STEPS", AVG(measure.column) AS "VALUE"
         * FROM MEASUREMENTS
         * WHERE RECORD_ID = record.getId()
         * GROUP BY STEPS
         * SORT BY STEPS */
        Cursor cursor = database.rawQuery(query, args);

        long start = begin / fromLength(end - begin).step;
        ArrayList<Entry> data = cursorToList(cursor, start);

        cursor.close();
        return data;
    }


    /**
     * Calculate the total number of measurements that are saved in the database.
     *
     * @return  number of saved measurements
     */
    public int getNoOfMeasurements() {
        String query = String.format(SQL_SELECT_NUMBER_OF,
                MeasurementData._ID,
                COLUMN_SUM,
                MeasurementData.TABLE_MEASUREMENT
        );

        // SELECT COUNT(_ID) AS "INDEX" FROM MEASUREMENT
        Cursor cursor = database.rawQuery(query, null);

        int index = cursor.getColumnIndexOrThrow(COLUMN_SUM);
        cursor.moveToFirst();

        int result = cursor.getInt(index);
        cursor.close();
        return result;
    }


    /**
     * Calculate the total number of records that are saved in the database.
     *
     * @return  number of saved records
     */
    public int getNoOfRecords() {
        String query = String.format(SQL_SELECT_NUMBER_OF,
                RecordData._ID,
                COLUMN_SUM,
                RecordData.TABLE_RECORD
        );

        // SELECT COUNT(_ID) AS "INDEX" FROM RECORD
        Cursor cursor = database.rawQuery(query, null);

        int index = cursor.getColumnIndexOrThrow(COLUMN_SUM);
        cursor.moveToFirst();

        int result = cursor.getInt(index);
        cursor.close();
        return result;
    }


    /**
     * Calculate the total number of sensors that are saved in the database.
     *
     * @return  number of saved sensors
     */
    public int getNoOfSensors() {
        String query = String.format(SQL_SELECT_NUMBER_OF,
                SensorData._ID,
                COLUMN_SUM,
                SensorData.TABLE_SENSOR
        );

        // SELECT COUNT(_ID) AS "INDEX" FROM SENSOR
        Cursor cursor = database.rawQuery(query, null);

        int index = cursor.getColumnIndexOrThrow(COLUMN_SUM);
        cursor.moveToFirst();

        int result = cursor.getInt(index);
        cursor.close();
        return result;
    }


    /**
     * Calculate the total number of users that are saved in the database.
     *
     * @return  number of saved users
     */
    public int getNoOfUsers() {
        String query = String.format(SQL_SELECT_NUMBER_OF,
                UserData._ID,
                COLUMN_SUM,
                UserData.TABLE_USER
        );

        // SELECT COUNT(_ID) AS "INDEX" FROM USER
        Cursor cursor = database.rawQuery(query, null);

        int index = cursor.getColumnIndexOrThrow(COLUMN_SUM);
        cursor.moveToFirst();

        int result = cursor.getInt(index);
        cursor.close();
        return result;
    }


    /**
     * Calculate the summarized duration of all recordings in the database.
     * Only records that have been stopped are considered in the calculation.
     *
     * @return  total time of records
     */
    public long getTotalRecordingTime() {

        String[] args = {Long.toString(Long.MIN_VALUE)};

        // SELECT SUM(END - BEGIN) AS "SUM" FROM USER WHERE END <> Long.MIN_VALUE;
        Cursor cursor = database.rawQuery(SQL_TOTAL_RECORDING_TIME, args);

        int index = cursor.getColumnIndexOrThrow(COLUMN_SUM);
        cursor.moveToFirst();

        long result = cursor.getLong(index);
        cursor.close();
        return result;
    }


    /**
     * Delete all recorded data. This includes all entries in the Record and Measurement table.
     * User and Sensor data is not deleted.
     */
    public void deleteAll() {
        int measurements = database.delete(MeasurementData.TABLE_MEASUREMENT, null, null);
        int records = database.delete(RecordData.TABLE_RECORD, null, null);
        Log.d(LOG_TAG, "Deleted " + measurements + "Measurements and " + records + " Records.");
    }


    /**
     * Delete the data of a certain record from the database
     *
     * @param record  the record to delete
     */
    public void deleteRecord(Record record) {
        // First delete all data rows that have been collected in that record.
        database.delete(MeasurementData.TABLE_MEASUREMENT,
                MeasurementData.COLUMN_RECORD_ID + " = " + record.getId(), null);
        Log.d(LOG_TAG, "Deleted record No " + record.getId());

        // Then delete the actual record in the record table
        int count = database.delete(RecordData.TABLE_RECORD,
                RecordData._ID + " = " + record.getId(), null);
        Log.d(LOG_TAG, "Deleted " + count + " corresponding measurements.");
    }


    /**
     * Insert a new measurement and its information into the database.
     * <p>
     * Note: id will be ignored by database and replaced with insertion id.
     *
     * @param measurement  the measurement to save
     */
    public long saveMeasurement(Measurement measurement) {
        if (measurement.getId() != -1) {
            throw new IllegalArgumentException("Measurement was already inserted some other time.");
        }

        ContentValues values = new ContentValues();

        values.put(MeasurementData.COLUMN_RECORD_ID, measurement.getRecord().getId());
        values.put(MeasurementData.COLUMN_TIME, measurement.getTime());
        values.put(MeasurementData.COLUMN_BRIGHTNESS, measurement.getBrightness());
        values.put(MeasurementData.COLUMN_DISTANCE, measurement.getDistance());
        values.put(MeasurementData.COLUMN_HUMIDITY, measurement.getHumidity());
        values.put(MeasurementData.COLUMN_PRESSURE, measurement.getPressure());
        values.put(MeasurementData.COLUMN_TEMPERATURE, measurement.getTemperature());

        // INSERT INTO MEASUREMENT VALUES (record, time, ..., pressure, temperature)
        long id = database.insert(MeasurementData.TABLE_MEASUREMENT, null, values);
        Log.d(LOG_TAG, "Inserted new measurement: " + id);

        measurement.setId(id);
        return id;
    }


    /**
     * Insert a new measurement by its information into the database.
     *
     * @param recordId     the id of the respective record
     * @param time         the time when the measurement was taken
     * @param brightness   the brightness value
     * @param distance     the distance value
     * @param humidity     the humidity value
     * @param pressure     the pressure value
     * @param temperature  the temperature value
     */
    long saveMeasurement(long recordId, long time, float brightness, float distance,
                                float humidity, float pressure, float temperature) {
        ContentValues values = new ContentValues();

        values.put(MeasurementData.COLUMN_RECORD_ID, recordId);
        values.put(MeasurementData.COLUMN_TIME, time);
        values.put(MeasurementData.COLUMN_BRIGHTNESS, brightness);
        values.put(MeasurementData.COLUMN_DISTANCE, distance);
        values.put(MeasurementData.COLUMN_HUMIDITY, humidity);
        values.put(MeasurementData.COLUMN_PRESSURE, pressure);
        values.put(MeasurementData.COLUMN_TEMPERATURE, temperature);

        // INSERT INTO MEASUREMENT VALUES (record, time, ..., pressure, temperature)
        long id = database.insert(MeasurementData.TABLE_MEASUREMENT, null, values);
        Log.d(LOG_TAG, "Inserted new measurement: " + id);
        return id;
    }


    /**
     * Insert a new record into the database.
     *
     * @param record  the record to save
     */
    long saveRecord(Record record) {
        ContentValues values = new ContentValues();

        values.put(RecordData.COLUMN_SENSOR_ID, record.getSensor().getId());
        values.put(RecordData.COLUMN_USER_ID, record.getUser().getId());
        values.put(RecordData.COLUMN_BEGIN, record.getBegin());
        values.put(RecordData.COLUMN_END, record.getEnd());

        // INSERT INTO RECORD VALUES (sensor, user, begin, end)
        long id = database.insert(RecordData.TABLE_RECORD, null, values);
        Log.d(LOG_TAG, "Inserted new record: " + id);

        record.setId(id);
        records.put(id, record);
        return id;
    }


    /**
     * Insert a new record by its parameters into the database.
     *
     * @param sensorId  the id of the respective sensor
     * @param userId    the id of the respective user
     * @param begin     the start point of the record
     * @param end       the end point of the record
     */
    long saveRecord(long sensorId, long userId, long begin, long end) {
        ContentValues values = new ContentValues();

        values.put(RecordData.COLUMN_SENSOR_ID, sensorId);
        values.put(RecordData.COLUMN_USER_ID, userId);
        values.put(RecordData.COLUMN_BEGIN, begin);
        values.put(RecordData.COLUMN_END, end);

        // INSERT INTO RECORD VALUES (sensor, user, begin, end)
        long id = database.insert(RecordData.TABLE_RECORD, null, values);
        Log.d(LOG_TAG, "Inserted new record: " + id);
        return id;
    }


    /**
     * Insert a new sensor into the database.
     *
     * @param sensor  the sensor to save
     */
    public void saveSensor(Sensor sensor) {
        ContentValues values = new ContentValues();

        values.put(SensorData._ID, sensor.getId());
        values.put(SensorData.COLUMN_NAME, sensor.getName());
        values.put(SensorData.COLUMN_KNOWN_SINCE, sensor.getKnownSince());

        // INSERT INTO SENSOR VALUES (id, name, knownSince)
        long id = database.insert(SensorData.TABLE_SENSOR, null, values);
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

        values.put(UserData._ID, user.getId());
        values.put(UserData.COLUMN_NAME, user.getName());

        // INSERT INTO USER VALUES (id, name)
        long id = database.insert(UserData.TABLE_USER, null, values);
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
     * Fill a list with all values that a cursor contains.
     *
     * @param cursor  the cursor pointing to the database
     * @param start   the first possible entry of the time column
     * @return  a list of values from the cursor
     */
    private ArrayList<Entry> cursorToList(Cursor cursor, long start) {
        int indexTime = cursor.getColumnIndex(COLUMN_STEPS);
        int indexValue = cursor.getColumnIndex(COLUMN_VALUE);

        Log.d(LOG_TAG, cursor.getCount() + " rows received from database.");

        // Cursor might be empty. Return nothing if no entries were found.
        if (!cursor.moveToFirst()) {
            Log.d(LOG_TAG, "No data available in this selection.");
            return new ArrayList<>();
        }

        // For every row in the result table an Entry point is added to the output.
        ArrayList<Entry> data = new ArrayList<>(cursor.getCount());

        // Fill list with values from cursor according to resolution of data points.
        for (int i = 0; !cursor.isAfterLast(); ++i) {
            if (start + i == cursor.getLong(indexTime)) {
                data.add(new Entry(i, cursor.getFloat(indexValue)));
                cursor.moveToNext();
            }
        }

        return data;
    }

    /**
     * Retrieve the measured values and meta data of a whole measurement from a database request.
     *
     * @param cursor  cursor pointing to the desired entry
     * @return  measurement containing the given row
     */
    private Measurement cursorToMeasurement(Cursor cursor) {
        final int indexID = cursor.getColumnIndex(MeasurementData._ID);
        final int indexRecord = cursor.getColumnIndex(MeasurementData.COLUMN_RECORD_ID);
        final int indexTime = cursor.getColumnIndex(MeasurementData.COLUMN_TIME);
        final int indexBrightness = cursor.getColumnIndex(MeasurementData.COLUMN_BRIGHTNESS);
        final int indexDistance = cursor.getColumnIndex(MeasurementData.COLUMN_DISTANCE);
        final int indexHumidity = cursor.getColumnIndex(MeasurementData.COLUMN_HUMIDITY);
        final int indexPressure = cursor.getColumnIndex(MeasurementData.COLUMN_PRESSURE);
        final int indexTemperature = cursor.getColumnIndex(MeasurementData.COLUMN_TEMPERATURE);

        return new Measurement(
                cursor.getLong(indexID),
                findRecord(cursor.getLong(indexRecord)),
                cursor.getLong(indexTime),
                cursor.getFloat(indexBrightness),
                cursor.getFloat(indexDistance),
                cursor.getFloat(indexHumidity),
                cursor.getFloat(indexPressure),
                cursor.getFloat(indexTemperature)
        );
    }


    /**
     * Creates a new record object from the information retrieved of a table entry.
     *
     * @param cursor  the cursor pointing to table entry
     * @return  record object from given row
     */
    private Record cursorToRecord(Cursor cursor) {
        final int indexID = cursor.getColumnIndex(RecordData._ID);
        final int indexSensor = cursor.getColumnIndex(RecordData.COLUMN_SENSOR_ID);
        final int indexUser = cursor.getColumnIndex(RecordData.COLUMN_USER_ID);
        final int indexBegin = cursor.getColumnIndex(RecordData.COLUMN_BEGIN);
        final int indexEnd = cursor.getColumnIndex(RecordData.COLUMN_END);

        return new Record(
                cursor.getLong(indexID),
                findSensor(cursor.getLong(indexSensor)),
                findUser(cursor.getLong(indexUser)),
                cursor.getLong(indexBegin),
                cursor.getLong(indexEnd)
        );
    }


    /**
     * Creates a new sensor object from the information retrieved of a table entry.
     *
     * @param cursor  the cursor pointing to table entry
     * @return  sensor object from given row
     */
    private Sensor cursorToSensor(Cursor cursor) {
        final int indexID = cursor.getColumnIndex(SensorData._ID);
        final int indexName = cursor.getColumnIndex(SensorData.COLUMN_NAME);
        final int indexKnownSince = cursor.getColumnIndex(SensorData.COLUMN_KNOWN_SINCE);

        return new Sensor(
                cursor.getLong(indexID),
                cursor.getString(indexName),
                cursor.getLong(indexKnownSince)
        );
    }


    /**
     * Creates a new user object from the information retrieved of a table entry.
     *
     * @param cursor  the cursor pointing to table entry
     * @return  user object from given row
     */
    private User cursorToUser(Cursor cursor) {
        final int indexID = cursor.getColumnIndex(UserData._ID);
        final int indexName = cursor.getColumnIndex(UserData.COLUMN_NAME);

        return new User(
                cursor.getLong(indexID),
                cursor.getString(indexName)
        );
    }


    /**
     * Search for and return the measurement with the given id in the database.
     *
     * @param id  the id of the wanted measurement
     * @return  the wanted measurement
     */
    private Measurement searchMeasurement(long id) {
        String[] select = MeasurementData.ALL_COLUMNS;

        // SELECT * FROM MEASUREMENT WHERE ID = id
        Cursor cursor = database.query(MeasurementData.TABLE_MEASUREMENT, select,
                MeasurementData._ID + " = " + id, null, null, null, null);

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
    private Record searchRecord(long id) {
        String[] select = RecordData.ALL_COLUMNS;

        // SELECT * FROM RECORD WHERE ID = id
        Cursor cursor = database.query(RecordData.TABLE_RECORD, select,
                RecordData._ID + " = " + id, null, null, null, null);

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
    private Sensor searchSensor(long id) {
        String[] select = SensorData.ALL_COLUMNS;

        // SELECT * FROM SENSOR WHERE ID = id
        Cursor cursor = database.query(SensorData.TABLE_SENSOR, select,
                SensorData._ID + " = " + id, null, null, null, null);

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
    private User searchUser(long id) {
        String[] select = UserData.ALL_COLUMNS;

        // SELECT * FROM USER WHERE ID = id
        Cursor cursor = database.query(UserData.TABLE_USER, select,
                UserData._ID + " = " + id, null, null, null, null);

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

        values.put(RecordData._ID, record.getId());
        values.put(RecordData.COLUMN_SENSOR_ID, record.getSensor().getId());
        values.put(RecordData.COLUMN_USER_ID, record.getUser().getId());
        values.put(RecordData.COLUMN_BEGIN, record.getBegin());
        values.put(RecordData.COLUMN_END, record.getEnd());

        // UPDATE SENSOR SET END = end WHERE ID = id
        long id = database.update(RecordData.TABLE_RECORD, values,
                RecordData._ID + " = " + record.getId(), null);
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

        values.put(SensorData._ID, sensor.getId());
        values.put(SensorData.COLUMN_NAME, sensor.getName());
        values.put(SensorData.COLUMN_KNOWN_SINCE, sensor.getKnownSince());

        // UPDATE SENSOR SET NAME = name, KNOWN_SINCE = knownSince WHERE ID = id
        long id = database.update(SensorData.TABLE_SENSOR, values,
                SensorData._ID + " = " + sensor.getId(), null);
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

        values.put(UserData._ID, user.getId());
        values.put(UserData.COLUMN_NAME, user.getName());

        // UPDATE USER SET NAME = name WHERE ID = id
        long id = database.update(UserData.TABLE_USER, values,
                UserData._ID + " = " + user.getId(), null);
        Log.d(LOG_TAG, "Updated user: " + id);

        users.put(id, user);
    }


    /**
     * Import all Measurements that belong to a record into the database.
     *
     * @param cursor    a cursor containing all measurement rows that belong to the record
     * @param recordId  the new id of the record
     */
    boolean insertMeasurements(Cursor cursor, long recordId) {
        final int indexTime = cursor.getColumnIndexOrThrow(MeasurementData.COLUMN_TIME);
        final int indexBrightness = cursor.getColumnIndexOrThrow(MeasurementData.COLUMN_BRIGHTNESS);
        final int indexDistance = cursor.getColumnIndexOrThrow(MeasurementData.COLUMN_DISTANCE);
        final int indexHumidity = cursor.getColumnIndexOrThrow(MeasurementData.COLUMN_HUMIDITY);
        final int indexPressure = cursor.getColumnIndexOrThrow(MeasurementData.COLUMN_PRESSURE);
        final int indexTemperature = cursor.getColumnIndexOrThrow(MeasurementData.COLUMN_TEMPERATURE);

        try {
            database.beginTransaction();
            SQLiteStatement query = database.compileStatement(SQL_INSERT_MEASUREMENTS);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                query.clearBindings();
                query.bindLong(1, recordId);
                query.bindLong(2, cursor.getLong(indexTime));
                query.bindDouble(3, cursor.getFloat(indexBrightness));
                query.bindDouble(4, cursor.getFloat(indexDistance));
                query.bindDouble(5, cursor.getFloat(indexHumidity));
                query.bindDouble(6, cursor.getFloat(indexPressure));
                query.bindDouble(7, cursor.getFloat(indexTemperature));
                query.executeInsert();
            }

            database.setTransactionSuccessful();
            return true;

        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        } finally {
            database.endTransaction();
        }
    }
}
