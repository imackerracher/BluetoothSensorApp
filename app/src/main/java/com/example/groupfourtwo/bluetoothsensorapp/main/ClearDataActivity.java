package com.example.groupfourtwo.bluetoothsensorapp.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

/**
 * Activity for clearing single or all recordings
 *
 * @author Tobias Nusser, Stefan Erk
 * @version 1.1
 */

public class ClearDataActivity extends AppCompatActivity {

    /* debugging only */
    private final static String TAG = RecordsActivity.class.getSimpleName();

    /**
     * DataManager instance for database operations like show recordings and delete recordings
     */
    DataManager dataManager;

    /**
     * List of recordings from the database
     */
    ListView recordList;

    /**
     * Displays the saved recordings and ensures the ability to delete one or all recordings.
     *
     * @param savedInstanceState Bundle object containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_data);

        dataManager = DataManager.getInstance(this);

        showAllRecords();

        recordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Record record = (Record) adapterView.getItemAtPosition(i);
                Log.d(TAG, "On item clicked " + record.toString());
                final AlertDialog.Builder builder = new AlertDialog.Builder(ClearDataActivity.this);

                builder.setTitle(R.string.attention);
                builder.setMessage("Do you really want to delete this recording?");

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dataManager.open();
                            dataManager.deleteRecord(record);
                            dataManager.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        showAllRecords();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            }
        });

        Button button = (Button) findViewById(R.id.deleteallbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ClearDataActivity.this);

                builder.setTitle(R.string.attention);
                builder.setMessage("Do you really want to delete all gathered data?\n" +
                        "This cannot be undone!");

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dataManager.open();
                            Log.d(TAG, "Deleted all records.");
                            dataManager.deleteAll();
                            dataManager.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        showAllRecords();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
                showAllRecords();
            }
        });
    }

    /**
     * Getting all the saved recordings and displaying them in a ListView
     */
    private void showAllRecords() {
        try {
            dataManager.open();
            final List<Record> allSensors = dataManager.getAllRecords();
            dataManager.close();
            final ListAdapter myAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, allSensors);
            recordList = (ListView) findViewById(R.id.records_listview2);
            recordList.setAdapter(myAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
