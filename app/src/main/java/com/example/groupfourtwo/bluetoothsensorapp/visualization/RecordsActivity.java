package com.example.groupfourtwo.bluetoothsensorapp.visualization;

import android.content.Intent;
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

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;

import java.io.IOException;
import java.util.List;

/**
 * Activity for displaying recordings in a list and the ability to select recordings and display
 * them in the visualization.
 *
 * @author Tobias Nusser
 * @version 1.0
 */

public class RecordsActivity extends AppCompatActivity {

    /* debugging only */
    private final static String TAG = RecordsActivity.class.getSimpleName();

    /**
     * onCreate method which gets called when opening the recordings tab
     *
     * @param savedInstanceState Bundle object containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

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
        final List<Record> recordList = dataManager.getAllRecords();
        dataManager.close();

        final ListAdapter myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1 , recordList);
        ListView listView = (ListView) findViewById(R.id.records_listview);
        listView.setAdapter(myAdapter);

        /**
         * ItemClickListener for the selection of the recordings which should get visualized
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Record record = (Record) adapterView.getItemAtPosition(i);
                Log.d(TAG, "On item clicked " + record.toString());
                Intent intent = new Intent();
                intent.putExtra(VisualizationActivity.RESULT_RECORD, record.getId());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * Handling the selection of navigation elements
     *
     * @param item selected menu item for further navigation
     * @return item called with the overlying super instance
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
