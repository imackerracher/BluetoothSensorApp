package com.example.groupfourtwo.bluetoothsensorapp.main;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;

import java.io.IOException;
import java.util.Locale;

import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.DAY;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.HOUR;

/**
 * Delivers information about the application to the user.
 *
 * @author Stefan Erk
 */

public class AboutActivity extends AppCompatActivity {

    /**
     * Retrieves data from the database.
     */
    private DataManager dataManager;

    /**
     * Displays the number of sensors saved in the databse.
     */
    private TextView noOfSensors;

    /**
     * Displays the number of finished records saved in the databse.
     */
    private TextView noOfRecords;

    /**
     * Displays the number of measurements saved in the databse.
     */
    private TextView noOfMeasurements;

    /**
     * Displays the sum of durations of all finished records.
     */
    private TextView recordingTime;

    /**
     * The number of sensors that are saved in the database.
     */
    private int sensors;

    /**
     * The number of records that are saved in the database.
     */
    private int records;

    /**
     * The number of measurements that are saved in the database.
     */
    private int measurements;

    /**
     * The total recording time, summed over all finished records.
     */
    private long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        dataManager = DataManager.getInstance(this);

        noOfSensors = (TextView) findViewById(R.id.no_of_sensors);
        noOfRecords = (TextView) findViewById(R.id.no_of_records);
        noOfMeasurements = (TextView) findViewById(R.id.no_of_measurements);
        recordingTime = (TextView) findViewById(R.id.total_recording_time);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Calculate the statistics and save them in the corresponding fields.
        new Statistics().doInBackground(null);

        // Display the values just calculated.
        noOfSensors.setText(getString(R.string.no_of_sensors, sensors));
        noOfRecords.setText(getString(R.string.no_of_records, records));
        noOfMeasurements.setText(getString(R.string.no_of_measurements, measurements));
        recordingTime.setText(getString(R.string.recording_time, formatDuration(time)));
    }

    /**
     * Formats a duration of time into a string that shows this duration in minutes, hours
     * and days if necessary.
     *
     * @param duration  the duration in milliseconds
     * @return  a string representation of the duration
     */
    private static String formatDuration(long duration) {
        if (duration < DAY.length) {
            return String.format(Locale.ENGLISH, "%dh, %dmin",
                    duration / HOUR.length, (duration / 60000) % 60 );
        } else {
            return String.format(Locale.ENGLISH, "%dd, %dh, %dmin",
                    duration / DAY.length, (duration / HOUR.length) % 24, (duration / 60000) % 60);
        }
    }

    /**
     * A background task to calculate the values displayed as the app's statistics.
     */
    private class Statistics extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                dataManager.open();

                sensors = dataManager.getNoOfSensors();
                records = dataManager.getNoOfRecords();
                measurements = dataManager.getNoOfMeasurements();
                time = dataManager.getTotalRecordingTime();

                dataManager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
