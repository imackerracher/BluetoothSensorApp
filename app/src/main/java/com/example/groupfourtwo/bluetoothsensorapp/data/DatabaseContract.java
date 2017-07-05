package com.example.groupfourtwo.bluetoothsensorapp.data;

import android.provider.BaseColumns;

/**
 * Defines required constants that represent the database and special SQL queries.
 *
 * @author Stefan Erk
 * @version 1.2
 */

final class DatabaseContract {

    /* No object should be created from this class. */
    private DatabaseContract() {}

    /**
     * The name of the database.
     */
    static final String DB_NAME = "sensor_tag.db";

    /**
     * The current version of the database.
     * <p>Is incremented every time when structural changes are applied to the database.
     */
    static final int DB_VERSION = 2;


    /**
     * Defines the names of the table and columns that contain the measured data.
     * @see BaseColumns
     */
    static final class MeasurementData implements BaseColumns {
        static final String TABLE_MEASUREMENT = "measurement";
        static final String COLUMN_RECORD_ID = "recordID";
        static final String COLUMN_TIME = "time";
        static final String COLUMN_BRIGHTNESS = "brightness";
        static final String COLUMN_DISTANCE = "distance";
        static final String COLUMN_HUMIDITY = "humidity";
        static final String COLUMN_PRESSURE = "pressure";
        static final String COLUMN_TEMPERATURE = "temperature";

        static final String[] ALL_COLUMNS = {
                _ID,
                COLUMN_RECORD_ID,
                COLUMN_TIME,
                COLUMN_BRIGHTNESS,
                COLUMN_DISTANCE,
                COLUMN_HUMIDITY,
                COLUMN_PRESSURE,
                COLUMN_TEMPERATURE
        };
    }


    /**
     * Defines the names of the table and columns that contain the metadata of a record.
     * @see BaseColumns
     */
    static final class RecordData implements BaseColumns {
        static final String TABLE_RECORD = "record";
        static final String COLUMN_SENSOR_ID = "sensorID";
        static final String COLUMN_USER_ID = "userID";
        static final String COLUMN_BEGIN = "begin";
        static final String COLUMN_END = "end";

        static final String[] ALL_COLUMNS = {
                _ID,
                COLUMN_SENSOR_ID,
                COLUMN_USER_ID,
                COLUMN_BEGIN,
                COLUMN_END
        };
    }


    /**
     * Defines the names of the table and columns that contain the sensor data.
     * @see BaseColumns
     */
    static final class SensorData implements BaseColumns {
        static final String TABLE_SENSOR = "sensor";
        static final String COLUMN_NAME = "sensorName";
        static final String COLUMN_KNOWN_SINCE = "knownSince";

        static final String[] ALL_COLUMNS = {
                _ID,
                COLUMN_NAME,
                COLUMN_KNOWN_SINCE
        };
    }


    /**
     * Defines the names of the table and columns that contain the user data.
     * @see BaseColumns
     */
    static final class UserData implements BaseColumns {
        static final String TABLE_USER = "user";
        static final String COLUMN_NAME = "userName";

        static final String[] ALL_COLUMNS = {
                _ID,
                COLUMN_NAME
        };
    }


    /**
     * Defines the SQL statement to create the measurement table.
     */
    static final String SQL_CREATE_MEASUREMENT =
            "CREATE TABLE " + MeasurementData.TABLE_MEASUREMENT + " (" +
                    MeasurementData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MeasurementData.COLUMN_RECORD_ID + " INTEGER NOT NULL, " +
                    MeasurementData.COLUMN_TIME + " NUMERIC NOT NULL, " +
                    MeasurementData.COLUMN_BRIGHTNESS + " REAL, " +
                    MeasurementData.COLUMN_DISTANCE + " REAL, " +
                    MeasurementData.COLUMN_HUMIDITY + " REAL, " +
                    MeasurementData.COLUMN_PRESSURE + " REAL, " +
                    MeasurementData.COLUMN_TEMPERATURE + " REAL);";


    /**
     * Defines the SQL statement to create the record table.
     */
    static final String SQL_CREATE_RECORD =
            "CREATE TABLE " + RecordData.TABLE_RECORD + " (" +
                    RecordData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RecordData.COLUMN_SENSOR_ID + " INTEGER NOT NULL, " +
                    RecordData.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    RecordData.COLUMN_BEGIN + " NUMERIC NOT NULL, " +
                    RecordData.COLUMN_END + " NUMERIC NOT NULL);";


    /**
     * Defines the SQL statement to create the sensor table.
     */
    static final String SQL_CREATE_SENSOR =
            "CREATE TABLE " + SensorData.TABLE_SENSOR + " (" +
                    SensorData._ID + " INTEGER PRIMARY KEY, " +
                    SensorData.COLUMN_NAME + " TEXT NOT NULL, " +
                    SensorData.COLUMN_KNOWN_SINCE + " NUMERIC NOT NULL);";


    /**
     * Defines the SQL statement to create the user table.
     */
    static final String SQL_CREATE_USER =
            "CREATE TABLE " + UserData.TABLE_USER + " (" +
                    UserData._ID + " INTEGER PRIMARY KEY, " +
                    UserData.COLUMN_NAME + " TEXT NOT NULL);";


    /**
     * Defines the SQL statement to delete the measurement table.
     */
    static final String SQL_DROP_MEASUREMENT =
            "DROP TABLE IF EXISTS " + MeasurementData.TABLE_MEASUREMENT;


    /**
     * Defines the SQL statement to delete the record table.
     */
    static final String SQL_DROP_RECORD =
            "DROP TABLE IF EXISTS " + RecordData.TABLE_RECORD;


    /**
     * Defines the SQL statement to delete the sensor table.
     */
    static final String SQL_DROP_SENSOR =
            "DROP TABLE IF EXISTS " + SensorData.TABLE_SENSOR;


    /**
     * Defines the SQL statement to delete the user table.
     */
    static final String SQL_DROP_USER =
            "DROP TABLE IF EXISTS " + UserData.TABLE_USER;


    /**
     * Defines the template of an SQL statement to retrieve preprocessed data of a time span.
     */
    static final String SQL_SELECT_VALUES_FROM_INTERVAL =
            "SELECT %s / %d AS %s, AVG(%s) AS %s FROM %s " +
                    "WHERE %s BETWEEN ? AND ? GROUP BY %s ORDER BY %s";


    /**
     * Defines the template of an SQL statement to retrieve preprocessed data of a record.
     */
    static final String SQL_SELECT_VALUES_FROM_RECORD =
            "SELECT %s / %d AS %s, AVG(%s) AS %s FROM %s " +
                    "WHERE %s = ? GROUP BY %s ORDER BY %s";


    /**
     * Defines the template of an SQL statement to retrieve preprocessed data of a record.
     */
    static final String SQL_SELECT_NUMBER_OF =
            "SELECT COUNT(%s) AS %s FROM %s";

    /**
     * Defines the template of an SQL statement to calculate the total recording time.
     */
    static final String SQL_TOTAL_RECORDING_TIME = "SELECT SUM(%s - %s) AS %s FROM %s WHERE %s > ?";

    /**
     *
     */
    static final String COLUMN_STEPS = "steps";


    /**
     *
     */
    static final String COLUMN_VALUE = "value";


    /**
     *
     */
    static final String COLUMN_SUM = "sum";

}
