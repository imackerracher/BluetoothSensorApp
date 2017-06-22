package com.example.groupfourtwo.bluetoothsensorapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;


import java.io.IOException;
import java.util.List;

import static android.R.attr.id;

/**
 * @author Tobias Nusser
 * @version 1.0
 */

public class RecordsActivity extends AppCompatActivity {

    private final static String TAG = RecordsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurements);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DataManager dataManager = DataManager.getInstance(this);
        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Record> recordList = dataManager.getAllRecords();
        dataManager.close();

        final ListAdapter myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1 , recordList);
        ListView listView = (ListView) findViewById(R.id.measurements_listview);
        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "On item clicked " + String.valueOf(adapterView.getItemAtPosition(i)));
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (id == android.R.id.home) {
            finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
