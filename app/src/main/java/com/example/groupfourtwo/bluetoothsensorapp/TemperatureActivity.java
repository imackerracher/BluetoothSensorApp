package com.example.groupfourtwo.bluetoothsensorapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.groupfourtwo.bluetoothsensorapp.Data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.Graph.DrawGraph;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import static com.example.groupfourtwo.bluetoothsensorapp.Data.DataManager.Interval.HOUR;
import static com.example.groupfourtwo.bluetoothsensorapp.Data.DataManager.Measure.TEMPERATURE;

public class TemperatureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        //Get the intent that started this activity
        Intent intent = getIntent();

        DataManager.Interval interval = HOUR;
        DataManager.Measure measure1 = TEMPERATURE;
        long begin = 1000l;

        DrawGraph drawGraph = new DrawGraph(this,measure1,null,interval,begin);
        drawGraph.draw(this);
    }
}
