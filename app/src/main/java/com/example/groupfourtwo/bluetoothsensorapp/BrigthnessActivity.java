package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.groupfourtwo.bluetoothsensorapp.Data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.Graph.DrawGraph;

import static com.example.groupfourtwo.bluetoothsensorapp.Data.Interval.DAY;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Interval.WEEK;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Measure.BRIGHTNESS;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.Measure.TEMPERATURE;

public class BrigthnessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brigthness);

        //Get the intent that started this activity
        Intent intent = getIntent();


        Interval interval = WEEK;
        Measure measure1 = BRIGHTNESS;
        long begin = 1723680000000l + 1000l*(60l*60l*24l*365l); // 1.Jan.2017

        DrawGraph drawGraph = new DrawGraph(this,measure1,null,interval,begin);
        drawGraph.draw(this);
    }
}
