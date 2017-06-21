package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.groupfourtwo.bluetoothsensorapp.data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.graph.DrawGraph;

import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.DAY;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.WEEK;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.BRIGHTNESS;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.HUMIDITY;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.PRESSURE;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.TEMPERATURE;


public class PressureActivity extends AppCompatActivity {

    private final static String TAG = MeasurementsActivity.class.getSimpleName();
    private static final int SENSOR_SELECTION_REQUEST = 1;
    private long end = System.currentTimeMillis();
    private long begin = end - DAY.length;
    private DrawGraph drawGraph = new DrawGraph(this,PRESSURE,null,begin,end);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pressure);
        setContentView(R.layout.activity_pressure);
        //Get the intent that started this activity
        Intent intent = getIntent();

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
            intent.putExtra("prevActivity", "PressureActivity");
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
                createNewGraph(returnValue);
                Log.d(TAG, "result: " + returnValue);
                drawGraph.draw(this);
            }
        }
    }

    private void createNewGraph(String returnValue) {
        Measure measure;

        switch (returnValue) {
            case "HumidityActivity": measure = HUMIDITY;
                break;
            case "PressureActivity": measure = PRESSURE;
                break;
            case "BrightnessActivity": measure = BRIGHTNESS;
                break;
            case "TemperatureActivity": measure = TEMPERATURE;
                break;
            default: measure = PRESSURE;
        }

        drawGraph.setMeasure2(measure);
    }

}
