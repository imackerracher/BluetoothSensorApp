package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.groupfourtwo.bluetoothsensorapp.Data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.Graph.DrawGraph;

import java.util.ArrayList;


import static com.example.groupfourtwo.bluetoothsensorapp.Data.Interval.HOUR;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Interval.WEEK;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Measure.TEMPERATURE;

public class TemperatureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        //Get the intent that started this activity
        Intent intent = getIntent();

        Interval interval = WEEK;
        Measure measure1 = TEMPERATURE;

        long begin = System.currentTimeMillis()- 1000l*60*60*24*10; //vor über einer Woche
        begin = System.currentTimeMillis()- 1000l*60*60*24*1; //vor X Tagen



        DrawGraph drawGraph = new DrawGraph(this,measure1,null,interval,begin);
        drawGraph.draw(this);
    }
}
