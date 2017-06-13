package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.groupfourtwo.bluetoothsensorapp.data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.graph.DrawGraph;

import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.DAY;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.HUMIDITY;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.PRESSURE;


public class PressureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pressure);
        setContentView(R.layout.activity_pressure);
        //Get the intent that started this activity
        Intent intent = getIntent();

        Interval interval = DAY;
        Measure measure1 = PRESSURE;
        Measure measure2 = HUMIDITY;
        long begin = System.currentTimeMillis()- interval.length;

        DrawGraph drawGraph = new DrawGraph(this,measure1,measure2,interval,begin);
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
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
