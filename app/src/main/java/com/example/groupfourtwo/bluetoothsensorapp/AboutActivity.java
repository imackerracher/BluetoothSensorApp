package com.example.groupfourtwo.bluetoothsensorapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.DAY;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.HOUR;

/**
 * Delivers information about the application to the user.
 *
 * @author Stefan Erk
 */

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        DataManager dataManager = DataManager.getInstance(this);
        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int sensors = dataManager.getNoOfSensors();
        TextView noOfSensors = (TextView) findViewById(R.id.no_of_sensors);
        noOfSensors.setText(getString(R.string.no_of_sensors, sensors));

        int records = dataManager.getNoOfRecords();
        TextView noOfRecords = (TextView) findViewById(R.id.no_of_records);
        noOfRecords.setText(getString(R.string.no_of_records, records));

        int measurements = dataManager.getNoOfMeasurements();
        TextView noOfMeasurements = (TextView) findViewById(R.id.no_of_measurements);
        noOfMeasurements.setText(getString(R.string.no_of_measurements, measurements));

        List<Record> allRecords = dataManager.getAllRecords();
        dataManager.close();

        long time = 0;
        for (Record r : allRecords) {
            if (r.isRunning()) {
                time += System.currentTimeMillis() - r.getBegin();
            } else {
                time += r.getEnd() - r.getBegin();
            }
        }

        TextView recordingTime = (TextView) findViewById(R.id.total_recording_time);
        recordingTime.setText(getString(R.string.recording_time, formatDuration(time)));
    }


    /**
     * Formats a duration of time into a string that shows that duration in minutes, hours
     * and days if necessary.
     *
     * @param duration  the duration in milliseconds
     * @return  a string representation of the duration
     */
    private String formatDuration(long duration) {
        if (duration < DAY.length) {
            return String.format(Locale.ENGLISH, "%dh, %dmin",
                    duration / HOUR.length, (duration / 60000) % 60 );
        } else {
            return String.format(Locale.ENGLISH, "%dd, %dh, %dmin",
                    duration / DAY.length, (duration / HOUR.length) % 24, (duration / 60000) % 60);
        }
    }
}
