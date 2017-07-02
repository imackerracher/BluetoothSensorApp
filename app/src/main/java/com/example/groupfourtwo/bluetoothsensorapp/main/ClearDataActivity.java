package com.example.groupfourtwo.bluetoothsensorapp.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.RecordsActivity;

import java.io.IOException;
import java.util.List;

public class ClearDataActivity extends AppCompatActivity {

    private final static String TAG = RecordsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_data);

        final DataManager dataManager = DataManager.getInstance(this);
        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final List<Record> recordList = dataManager.getAllRecords();
        dataManager.close();

        final ListAdapter myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recordList);
        ListView listView = (ListView) findViewById(R.id.records_listview2);
        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Record record = (Record) adapterView.getItemAtPosition(i);
                Log.d(TAG, "On item clicked " + record.toString());
                dataManager.deleteRecord(record);
            }
        });

        Button button = (Button) findViewById(R.id.deleteallbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recordList.isEmpty()) {
                    for (Record r : recordList) {
                        dataManager.deleteRecord(r);
                    }
                    Log.d(TAG, "Deleted all");
                }
            }
        });
    }
}
