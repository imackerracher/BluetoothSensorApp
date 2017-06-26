package com.example.groupfourtwo.bluetoothsensorapp.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import static com.example.groupfourtwo.bluetoothsensorapp.data.DatabaseContract.*;

public class DbExportImport {

    private static final String TAG = DbExportImport.class.getName();

    // Directory that files are to be read from and written to
    private static final File DATABASE_DIRECTORY =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "bluetooth_sensor_app");

    /** File path of Db to be imported **/
    private static final File IMPORT_FILE = new File(DATABASE_DIRECTORY, "sensor_tag");

    private static final String PACKAGE_NAME = "com.example.groupfourtwo.bluetoothsensorapp";

    /** Contains: /data/data/com.example.groupfourtwo.bluetoothsensorapp/databases/sensor_tag **/
    private static final File DATA_DIRECTORY_DATABASE =
            new File(Environment.getDataDirectory() +
                    "/user/0/" + PACKAGE_NAME +
                    "/databases/" + DB_NAME);


    /** Saves the application database to the
     * export directory under sensor_tag.db **/
    public static  boolean exportDb(Context context){
        if(!SdIsPresent()) return false;

        String filename = "sensor_tag.db";

        File exportDir = DATABASE_DIRECTORY;
        File file = new File(exportDir, filename);

        if (!exportDir.exists()) {
            boolean t = exportDir.mkdirs();
            if (t) {
                Log.d(TAG, "directory erstellt");
            } else {
                Log.d(TAG, "kein directory erstellt");
            }
        }

        try {/*
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
            File source = new File(dbHelper.getReadableDatabase().getPath());
            dbHelper.close();
            */
            file.createNewFile();
            copyFile(DATA_DIRECTORY_DATABASE, file);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Fehler: ", e);
            e.printStackTrace();
            return false;
        }
    }


    /** Replaces current database with the IMPORT_FILE if
     * import database is valid and of the correct type **/
    public  static boolean restoreDb(){
        if(!SdIsPresent()) return false;

        File exportFile = DATA_DIRECTORY_DATABASE;
        File importFile = IMPORT_FILE;

        if(!checkDbIsValid(importFile)) return false;

        if (!importFile.exists()) {
            Log.d(TAG, "File does not exist");
            return false;
        }

        try {
            exportFile.createNewFile();
            copyFile(importFile, exportFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /** Imports the file at IMPORT_FILE **/
    protected static boolean importIntoDb(Context ctx){
        if(!SdIsPresent()) return false;

        File importFile = IMPORT_FILE;

        if(!checkDbIsValid(importFile)) return false;

        try{
            SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase
                    (importFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            DataManager dataManager = DataManager.getInstance(ctx);
            dataManager.open();

            copySensors(sqlDb, dataManager);

            copyUsers(sqlDb, dataManager);

            copyRecords(sqlDb, dataManager);

            copyMeasurements(sqlDb, dataManager);

            sqlDb.close();
            dataManager.close();
        } catch( Exception e ){
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /** Given an SQLite database file, this checks if the file
     * is a valid SQLite database and that it contains all the
     * columns represented by DbAdapter.ALL_COLUMN_KEYS **/
    private static boolean checkDbIsValid( File db ){
        try{
            SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase
                    (db.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            Cursor cursor;

            cursor = sqlDb.query(true, MeasurementData.TABLE_MEASUREMENT,
                    null, null, null, null, null, null, null
            );
            checkColumns(cursor, MeasurementData.ALL_COLUMNS);
            cursor.close();

            cursor = sqlDb.query(true, RecordData.TABLE_RECORD,
                    null, null, null, null, null, null, null
            );
            checkColumns(cursor, RecordData.ALL_COLUMNS);
            cursor.close();

            cursor = sqlDb.query(true, SensorData.TABLE_SENSOR,
                    null, null, null, null, null, null, null
            );
            checkColumns(cursor, SensorData.ALL_COLUMNS);
            cursor.close();

            cursor = sqlDb.query(true, UserData.TABLE_USER,
                    null, null, null, null, null, null, null
            );
            checkColumns(cursor, UserData.ALL_COLUMNS);
            cursor.close();

            sqlDb.close();
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Database valid but not the right type");
            e.printStackTrace();
            return false;
        } catch (SQLiteException e) {
            Log.d(TAG, "Database file is invalid.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "checkDbIsValid encountered an exception");
            e.printStackTrace();
            return false;
        }

        return true;
    }


    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            outChannel.close();
        }
    }


    /** Returns whether an SD card is present and writable **/
    private static boolean SdIsPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }


    private static void checkColumns(Cursor cursor, String[] columns) {
        // Every column should have an index in the table
        for (String s : columns) {
            cursor.getColumnIndexOrThrow(s);
        }
    }


    private static void copyMeasurements(SQLiteDatabase sqlDb, DataManager dataManager) {
        Cursor cursor = sqlDb.query(true, MeasurementData.TABLE_MEASUREMENT,
                null, null, null, null, null, null, null
        );

        final int indexRecord = cursor.getColumnIndex(MeasurementData.COLUMN_RECORD_ID);
        final int indexTime = cursor.getColumnIndex(MeasurementData.COLUMN_TIME);
        final int indexBrightness = cursor.getColumnIndex(MeasurementData.COLUMN_BRIGHTNESS);
        final int indexDistance = cursor.getColumnIndex(MeasurementData.COLUMN_DISTANCE);
        final int indexHumidity = cursor.getColumnIndex(MeasurementData.COLUMN_HUMIDITY);
        final int indexPressure = cursor.getColumnIndex(MeasurementData.COLUMN_PRESSURE);
        final int indexTemperature = cursor.getColumnIndex(MeasurementData.COLUMN_TEMPERATURE);

        // Adds all measurements in cursor to current database
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            dataManager.saveMeasurement(
                    new Measurement(
                            -1, // some invalid value, not used
                            dataManager.findRecord(cursor.getLong(indexRecord)),
                            cursor.getLong(indexTime),
                            cursor.getFloat(indexBrightness),
                            cursor.getFloat(indexDistance),
                            cursor.getFloat(indexHumidity),
                            cursor.getFloat(indexPressure),
                            cursor.getFloat(indexTemperature) )
            );
        }
        cursor.close();
    }


    private static void copyRecords(SQLiteDatabase sqlDb, DataManager dataManager) {
        Cursor cursor = sqlDb.query(true, RecordData.TABLE_RECORD,
                null, null, null, null, null, null, null
        );

        final int indexSensor = cursor.getColumnIndex(RecordData.COLUMN_SENSOR_ID);
        final int indexUser = cursor.getColumnIndex(RecordData.COLUMN_USER_ID);
        final int indexBegin = cursor.getColumnIndex(RecordData.COLUMN_BEGIN);
        final int indexEnd = cursor.getColumnIndex(RecordData.COLUMN_END);

        // Adds all Records in cursor to current database
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            dataManager.saveRecord(
                    new Record(
                            -1, // some invalid value, not used
                            dataManager.findSensor(cursor.getLong(indexSensor)),
                            dataManager.findUser(cursor.getLong(indexUser)),
                            cursor.getLong(indexBegin),
                            cursor.getLong(indexEnd) )
            );
        }
        cursor.close();
    }


    private static void copySensors(SQLiteDatabase sqlDb, DataManager dataManager) {
        Cursor cursor = sqlDb.query(true, SensorData.TABLE_SENSOR,
                null, null, null, null, null, null, null
        );

        final int indexSensorID = cursor.getColumnIndex(SensorData._ID);
        final int indexSensorName = cursor.getColumnIndex(SensorData.COLUMN_NAME);
        final int indexKnownSince = cursor.getColumnIndex(SensorData.COLUMN_KNOWN_SINCE);

        // Adds all Sensors in cursor to current database iff not known yet.
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            final long id = cursor.getLong(indexSensorID);

            if (dataManager.findSensor(id) == null) {
                dataManager.saveSensor(
                        new Sensor(
                                cursor.getLong(indexSensorID),
                                cursor.getString(indexSensorName),
                                cursor.getLong(indexKnownSince) )
                );
            }
        }
        cursor.close();
    }


    private static void copyUsers(SQLiteDatabase sqlDb, DataManager dataManager) {
        Cursor cursor = sqlDb.query(true, UserData.TABLE_USER,
                null, null, null, null, null, null, null
        );

        final int indexUserID = cursor.getColumnIndex(UserData._ID);
        final int indexUserName = cursor.getColumnIndex(UserData.COLUMN_NAME);

        // Adds all Users in cursor to current database iff not known yet.
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            final long id = cursor.getLong(indexUserID);

            if (dataManager.findUser(id) == null) {
                dataManager.saveUser(
                        new User(
                                cursor.getLong(indexUserID),
                                cursor.getString(indexUserName) )
                );
            }
        }
        cursor.close();
    }
}
