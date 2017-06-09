package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.groupfourtwo.bluetoothsensorapp.Data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.Graph.DrawGraph;

import static com.example.groupfourtwo.bluetoothsensorapp.Data.Interval.DAY;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Interval.HOUR;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Measure.PRESSURE;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Measure.TEMPERATURE;


public class PressureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pressure);
        setContentView(R.layout.activity_temperature);
        //Get the intent that started this activity
        Intent intent = getIntent();

        Interval interval = HOUR;
        Measure measure1 = PRESSURE;
        long begin = System.currentTimeMillis()- 1000l*60*60*24*1; ; // 1.Jan.2016

        DrawGraph drawGraph = new DrawGraph(this,measure1,null,interval,begin);
        drawGraph.draw(this);

    }
}
