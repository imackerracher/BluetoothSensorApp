package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
}
