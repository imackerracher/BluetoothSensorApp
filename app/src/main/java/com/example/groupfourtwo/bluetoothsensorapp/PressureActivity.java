package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.groupfourtwo.bluetoothsensorapp.Graph.DrawGraph;


import com.example.groupfourtwo.bluetoothsensorapp.Graph.DrawGraph;

public class PressureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pressure);
        setContentView(R.layout.activity_temperature); //TODO: crashes with activity_pressure
        //Get the intent that started this activity
        Intent intent = getIntent();

        DrawGraph drawGraph = new DrawGraph();
        drawGraph.draw(this);

    }
}
