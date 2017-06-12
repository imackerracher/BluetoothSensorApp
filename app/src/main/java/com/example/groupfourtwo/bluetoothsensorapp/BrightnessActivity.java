package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.groupfourtwo.bluetoothsensorapp.data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.graph.DrawGraph;

import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.HOUR;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.BRIGHTNESS;

public class BrightnessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness);

        //Get the intent that started this activity
        Intent intent = getIntent();


        Interval interval = HOUR;
        Measure measure1 = BRIGHTNESS;
        long begin = System.currentTimeMillis()- interval.length;

        DrawGraph drawGraph = new DrawGraph(this,measure1,null,interval,begin);
        drawGraph.draw(this);
    }
}
