package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ManageConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_connection);

        //Get the intent that started this activity
        Intent intent = getIntent();
    }
}
