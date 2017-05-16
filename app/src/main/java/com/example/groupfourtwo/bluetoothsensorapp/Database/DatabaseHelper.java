package com.example.groupfourtwo.bluetoothsensorapp.Database;

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
 * @version 1.0
 */

class DatabaseHelper extends SQLiteOpenHelper {

    /* debugging only */
    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    /**
     * Creates a new helper object to manage creating and connection to a database.
     * @see DatabaseHelper
     *
     * @param context  the context of the calling activity
     */
    DatabaseHelper(Context context) {
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
            sqLiteDatabase.execSQL(DatabaseContract.SQL_DROP_MEASUREMENT);
            sqLiteDatabase.execSQL(DatabaseContract.SQL_DROP_MEASUREMENT);
            onCreate(sqLiteDatabase);
        }
        catch (SQLException e) {
            Log.e(LOG_TAG, "Error while updating tables: ", e);
        }

    }
}
