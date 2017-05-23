package com.example.groupfourtwo.bluetoothsensorapp.Data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class for creating a new database and the connection to an existing database.
 * Uses the provided services of abstract class {@link SQLiteOpenHelper}.
 *
 * @author Stefan Erk
 * @version 1.2
 */

class DatabaseHelper extends SQLiteOpenHelper {

    /* debugging only */
    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    /**
     * The singleton object of DatabaseHelper.
     */
    private static DatabaseHelper instance;


    /**
     * Return the helper object to manage creating a new or connecting to an existing database.
     * If no object exists yet a new one is created (singleton pattern).
     *
     * @param context the context of the calling activity
     * @return  the instance
     */
    static DatabaseHelper getInstance(Context context) {
        if (instance == null)
            // as database helper is global, use application context instead of activity context.
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }


    /**
     * Creates a new helper object of an SQLite database.
     * @see DatabaseHelper
     *
     * @param context  the context of the calling activity
     */
    private DatabaseHelper(Context context) {
        super(context, DatabaseContract.DB_NAME, null, DatabaseContract.DB_VERSION);
    }


    /**
     * Called when the database is created for the first time. Initializes all necessary tables.
     *
     * @param sqLiteDatabase  the database
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            Log.d(LOG_TAG, "Creating a new database...");
            sqLiteDatabase.execSQL(DatabaseContract.SQL_CREATE_MEASUREMENT);
            sqLiteDatabase.execSQL(DatabaseContract.SQL_CREATE_RECORD);
            sqLiteDatabase.execSQL(DatabaseContract.SQL_CREATE_SENSOR);
            sqLiteDatabase.execSQL(DatabaseContract.SQL_CREATE_USER);
        }
        catch (SQLException e) {
            Log.e(LOG_TAG, "Error while creating tables: ", e);
        }
    }


    /**
     * Called when the database is upgraded from a lower version.
     * Makes necessary modification operations on the tables.
     *
     * @param sqLiteDatabase  the database
     * @param i               old version
     * @param i1              new version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try {
            Log.d(LOG_TAG, "Updating database from version " + i + " to version " + i1 + "...");
            sqLiteDatabase.execSQL(DatabaseContract.SQL_DROP_MEASUREMENT);
            sqLiteDatabase.execSQL(DatabaseContract.SQL_DROP_RECORD);
            sqLiteDatabase.execSQL(DatabaseContract.SQL_DROP_SENSOR);
            sqLiteDatabase.execSQL(DatabaseContract.SQL_DROP_USER);
            onCreate(sqLiteDatabase);
        }
        catch (SQLException e) {
            Log.e(LOG_TAG, "Error while updating tables: ", e);
        }

    }
}
