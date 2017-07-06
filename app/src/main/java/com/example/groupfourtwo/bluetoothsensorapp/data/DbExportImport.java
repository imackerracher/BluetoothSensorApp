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

/**
 * Provides methods to export, import or merge the application's database from or into
 * the external storage.
 *
 * @author Stefan Erk
 */
public class DbExportImport {

    private static final String TAG = DbExportImport.class.getSimpleName();

    /**
     * The name of the application directory on the external storage.
     */
    private static final String PACKAGE_NAME = "groupfourtwo.bluetoothsensorapp";

    /**
     * Directory that files are to be read from and written to.
     */
    private static final File EXTERNAL_DATABASE_DIRECTORY =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            //new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
              //      PACKAGE_NAME);

    /**
     * The file that is used for a backup, as well as a source to restore the internal database.
     */
    private static final File EXPORT_IMPORT_FILE =
            new File(EXTERNAL_DATABASE_DIRECTORY, "sensor_tag.db");


    /**
     * Saves the application database to the {@link #EXPORT_IMPORT_FILE}, namely
     * Documents/groupfourtwo.bluetoothsensorapp/sensor_tag.db
     *
     * @param context  the calling activity
     * @return  whether the export succeeded
     */
    public static boolean exportDb(Context context){
        if(!storageIsPresent()) {
            return false;
        }

        File exportDir = EXTERNAL_DATABASE_DIRECTORY;
        File exportFile = EXPORT_IMPORT_FILE;

        if (!exportDir.exists()) {
            if (exportDir.mkdirs()) {
                Log.d(TAG, "Created necessary path directories.");
            } else {
                Log.d(TAG, "Failed while creating path.");
                return false;
            }
        }

        try {
            if (!exportFile.exists() && !exportFile.createNewFile()) {
                Log.d(TAG, "Destination of backup does not exist or could not be created.");
                return false;
            }
            copyFile(getDatabaseFile(context), exportFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Replaces the current database with the {@link #EXPORT_IMPORT_FILE} iff that database
     * is valid and of the correct type. This deletes all data gathered by the application.
     *
     * @param context  the calling activity
     * @return  whether the operation succeeded
     */
    public static boolean restoreDb(Context context){
        if(!storageIsPresent()) {
            return false;
        }

        File database = getDatabaseFile(context);
        File importFile = EXPORT_IMPORT_FILE;

        if(!checkDbIsValid(importFile)) {
            Log.d(TAG, "Import database is not valid");
            return false;
        }

        if (!importFile.exists()) {
            Log.d(TAG, "File does not exist");
            return false;
        }

        try {
            if (!database.exists()  &&  !database.createNewFile()) {
                Log.d(TAG, "Internal database could not be attained.");
                return false;
            }
            copyFile(importFile, database);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Imports the database that is saved in the IMPORT_EXPORT_FILE into the application database,
     * such that the two databases are merged into one, while not touching the data already
     * gathered by the application.
     * <p>
     * In the case of conflicts between rows that contain a natural primary key (MAC-Address)
     * the imported version is ignored and the internal version is kept.
     * <p>
     * The operation fails if either the import file is not found or the import database is
     * in any manner not valid or compatible with the application database.
     *
     * @param context  the calling activity
     * @return  whether the import has succeeded
     */
    public static boolean importIntoDb(Context context){
        if(!storageIsPresent()) return false;

        File importFile = EXPORT_IMPORT_FILE;

        if(!checkDbIsValid(importFile)) {
            return false;
        }

        try{
            SQLiteDatabase importDb = SQLiteDatabase.openDatabase(
                    importFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            DataManager dataManager = DataManager.getInstance(context);
            dataManager.open();

            /* Import every table into the application database.
             * Note the order of operations such that a table is imported only after
             * all tables that it references have already been imported.
             */
            importSensors(importDb, dataManager);

            importUsers(importDb, dataManager);

            // imports both records and measurements in parallel
            importRecords(importDb, dataManager);

            importDb.close();
            dataManager.close();
        } catch( Exception e ){
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /**
     * Compares the columns that are contained within a cursor to a given schema.
     *
     * @param cursor   the cursor containing the actual table schema
     * @param columns  the predefined table schema
     */
    private static void checkColumns(Cursor cursor, String[] columns) {
        // Every column should have an index in the table
        for (String s : columns) {
            cursor.getColumnIndexOrThrow(s);
        }
    }


    /**
     * Given an SQLite database file, this checks whether the file is a valid SQLite database
     * and that it contains all the tables with their respective columns according to
     * {@link DatabaseContract.MeasurementData}, {@link DatabaseContract.RecordData},
     * {@link DatabaseContract.SensorData}, {@link DatabaseContract.UserData}
     *
     * @return  whether the given database is valid
     */
    private static boolean checkDbIsValid(File db) {
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
            Log.d(TAG, "Database valid but not the right format");
            e.printStackTrace();
            return false;
        } catch (SQLiteException e) {
            Log.d(TAG, "Database file is invalid.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "checkDbIsValid encountered an unknown exception");
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /**
     * Replaces the data of a given file with the data of another one.
     *
     * @param src   the file data is to be read from
     * @param dest  the file the read data is saved into
     * @throws IOException  if the transfer happens to trigger an exception
     */
    private static void copyFile(File src, File dest) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dest).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
            outChannel.truncate(inChannel.size());
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }


    /**
     * Returns a file containing the path to the application's internal database.
     * If no database existed yet, a new one is created automatically.
     *
     * @param context  the calling activity
     * @return  the file of the application database
     */
    private static File getDatabaseFile(Context context) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        File database = new File(dbHelper.getReadableDatabase().getPath());
        dbHelper.close();
        return database;
    }


    /**
     * Imports all of the external measurement table into the application's table.
     * <p>
     * The database to be read from must be opened before passing it and has to be checked
     * for compatibility in respect to the application database, represented by an open
     * {@link DataManager}.
     *
     * @param importDb     a valid and readable {@link SQLiteDatabase}
     * @param dataManager  an opened instance of DataManager
     * @param oldId        the former id of the respective record from the imported database
     * @param newId        the new id of the record as it was set after the import
     */
    private static void importMeasurements(SQLiteDatabase importDb, DataManager dataManager,
                                           long oldId, long newId) {

        String where = MeasurementData.COLUMN_RECORD_ID + " = " + oldId;

        // SELECT * FROM MEASUREMENT WHERE RECORD_ID = oldID;
        Cursor cursor = importDb.query(MeasurementData.TABLE_MEASUREMENT,
                null, where, null, null, null, null);

        // Adds all measurements in cursor to current database
        dataManager.insertMeasurements(cursor, newId);
        cursor.close();
    }


    /**
     * Imports the content of the external record table into the application's table.
     * <p>
     * The database to be read from must be opened before passing it and has to be checked
     * for compatibility in respect to the application database, represented by an open
     * {@link DataManager}.
     *
     * @param importDb     a valid and readable {@link SQLiteDatabase}
     * @param dataManager  an opened instance of DataManager
     */
    private static void importRecords(SQLiteDatabase importDb, DataManager dataManager) {
        Cursor cursor = importDb.query(RecordData.TABLE_RECORD, null, null, null, null, null, null);

        final int indexID = cursor.getColumnIndex(RecordData._ID);
        final int indexSensor = cursor.getColumnIndex(RecordData.COLUMN_SENSOR_ID);
        final int indexUser = cursor.getColumnIndex(RecordData.COLUMN_USER_ID);
        final int indexBegin = cursor.getColumnIndex(RecordData.COLUMN_BEGIN);
        final int indexEnd = cursor.getColumnIndex(RecordData.COLUMN_END);

        // Adds all Records in cursor to current database
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            long oldId = cursor.getLong(indexID);
            long newId = dataManager.saveRecord(
                    cursor.getLong(indexSensor),
                    cursor.getLong(indexUser),
                    cursor.getLong(indexBegin),
                    cursor.getLong(indexEnd)
            );
            importMeasurements(importDb, dataManager, oldId, newId);
        }
        cursor.close();
    }


    /**
     * Imports the content of the external sensor table into the application's table.
     * <p>
     * The database to be read from must be opened before passing it and has to be checked
     * for compatibility in respect to the application database, represented by an open
     * {@link DataManager}.
     *
     * @param importDb     a valid and readable {@link SQLiteDatabase}
     * @param dataManager  an opened instance of DataManager
     */
    private static void importSensors(SQLiteDatabase importDb, DataManager dataManager) {
        Cursor cursor = importDb.query(true, SensorData.TABLE_SENSOR,
                null, null, null, null, null, null, null
        );

        final int indexSensorID = cursor.getColumnIndex(SensorData._ID);
        final int indexSensorName = cursor.getColumnIndex(SensorData.COLUMN_NAME);
        final int indexKnownSince = cursor.getColumnIndex(SensorData.COLUMN_KNOWN_SINCE);

        // Adds all Sensors in cursor to current database iff not known yet.
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            final long id = cursor.getLong(indexSensorID);

            if (!dataManager.existsSensor(id)) {
                dataManager.saveSensor(
                        new Sensor(
                                cursor.getLong(indexSensorID),
                                cursor.getString(indexSensorName),
                                cursor.getLong(indexKnownSince) )
                );
            }
            cursor.moveToNext();
        }
        cursor.close();
    }



    /**
     * Imports the content of the external user table into the application's table.
     * <p>
     * The database to be read from must be opened before passing it and has to be checked
     * for compatibility in respect to the application database, represented by an open
     * {@link DataManager}.
     *
     * @param importDb     a valid and readable {@link SQLiteDatabase}
     * @param dataManager  an opened instance of DataManager
     */
    private static void importUsers(SQLiteDatabase importDb, DataManager dataManager) {
        Cursor cursor = importDb.query(UserData.TABLE_USER, null, null, null, null, null, null);

        final int indexUserID = cursor.getColumnIndex(UserData._ID);
        final int indexUserName = cursor.getColumnIndex(UserData.COLUMN_NAME);

        // Adds all Users in cursor to current database iff not known yet.
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            final long id = cursor.getLong(indexUserID);

            if (!dataManager.existsUser(id)) {
                dataManager.saveUser(
                        new User(
                                cursor.getLong(indexUserID),
                                cursor.getString(indexUserName) )
                );
            }
            cursor.moveToNext();
        }
        cursor.close();
    }


    /**
     * Checks whether the external storage is present and writable.
     * <p>
     * The actual location of the storage can vary depending on System and user settings.
     *
     * @return  whether the storage is present
     */
    private static boolean storageIsPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

}
