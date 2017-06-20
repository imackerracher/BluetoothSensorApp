package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.groupfourtwo.bluetoothsensorapp.data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.graph.DrawGraph;

import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.HOUR;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.BRIGHTNESS;

public class BrightnessActivity extends AppCompatActivity {

    private final static String TAG = MeasurementsActivity.class.getSimpleName();
    private static final int SENSOR_SELECTION_REQUEST = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness);

        Interval interval = HOUR;
        Measure measure1 = BRIGHTNESS;
        long begin = System.currentTimeMillis()- interval.length;

        DrawGraph drawGraph = new DrawGraph(this,measure1,null,interval,begin);

        drawGraph.draw(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.measurements_settings) {
            Intent intent = new Intent(this, MeasurementsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.timespan_settings) {
            Intent intent = new Intent(this, TimespanActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.sensor_settings) {
            Intent intent = new Intent(this, SensorSettingsActivity.class);
            intent.putExtra("prevActivity", "BrightnessActivity");
            startActivityForResult(intent, SENSOR_SELECTION_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SENSOR_SELECTION_REQUEST) {
            if (resultCode == RESULT_OK) {
                String returnValue = data.getStringExtra("sensor_selection");
                Log.d(TAG, "result: " + returnValue);
            }
        }
    }

}
