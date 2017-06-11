package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.groupfourtwo.bluetoothsensorapp.Data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.Graph.DrawGraph;

import static com.example.groupfourtwo.bluetoothsensorapp.Data.Interval.DAY;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Interval.WEEK;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Measure.HUMIDITY;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Measure.TEMPERATURE;

public class HumidityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humidity);

        //Get the intent that started this activity
        Intent intent = getIntent();

        Interval interval = DAY;
        Measure measure1 = HUMIDITY;
        long begin = System.currentTimeMillis()- interval.length;

        DrawGraph drawGraph = new DrawGraph(this,measure1,null,interval,begin);
        drawGraph.draw(this);
    }
}
