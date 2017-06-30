package com.example.groupfourtwo.bluetoothsensorapp.visualization;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;
import com.example.groupfourtwo.bluetoothsensorapp.graph.DrawGraph;

import java.io.IOException;
import java.util.Locale;


/**
 * Displays graphics of the data collected by the sensors.
 *
 * @author all
 */

public class VisualizationActivity extends AppCompatActivity {

    private static final String TAG = VisualizationActivity.class.getSimpleName();

    private static final int RECORD_SELECTION_REQUEST = 2;
    private static final int TIME_SPAN_SELECTION_REQUEST = 3;
    private static final int SENSOR_SELECTION_REQUEST = 4;

    static final String MAIN_MEASURE = "main_measure";
    static final String ADD_MEASURE = "add_measure";
    static final String RESULT_BEGIN = "begin_selection";
    static final String RESULT_END = "end_selection";
    static final String RESULT_RECORD = "record_selection";
    static final String RESULT_MEASURE = "measure_selection";

    private final Measure mainMeasure;
    private Measure addMeasure;

    private Record record;

    private long begin;
    private long end;

    private DrawGraph drawGraph;

    protected VisualizationActivity(Measure measure) {
        mainMeasure = measure;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);

        end = System.currentTimeMillis();
        begin = end - Interval.DAY.length;

        if (savedInstanceState != null) {
            Log.d(TAG, "restore old state");
            String sndMeasure = savedInstanceState.getString(ADD_MEASURE);
            if (sndMeasure != null) {
                addMeasure = Measure.valueOf(sndMeasure);
            }

            long recordId = savedInstanceState.getLong(RESULT_RECORD);
            if (recordId > 0) {
                DataManager dataManager = DataManager.getInstance(this);
                record = dataManager.findRecord(recordId);
                end = record.getEnd();
                begin = record.getBegin();
            } else {
                end = savedInstanceState.getLong(RESULT_END, end);
                begin = savedInstanceState.getLong(RESULT_BEGIN, begin);
            }
        }

        drawGraph = new DrawGraph(this, mainMeasure, addMeasure, record, begin, end);
        drawGraph.draw(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Handle action bar item clicks here. The action bar will
         * automatically handle clicks on the Home/Up button, so long
         * as you specify a parent activity in AndroidManifest.xml */
        Intent intent;
        switch (item.getItemId()) {
            case R.id.measurements_settings:
                intent = new Intent(this, RecordsActivity.class);
                startActivityForResult(intent, RECORD_SELECTION_REQUEST);
                return true;

            case R.id.timespan_settings:
                intent = new Intent(this, TimespanActivity.class);
                startActivityForResult(intent, TIME_SPAN_SELECTION_REQUEST);
                return true;

            case R.id.sensor_settings:
                intent = new Intent(this, SensorSettingsActivity.class);
                intent.putExtra(MAIN_MEASURE, mainMeasure.name());
                if (addMeasure != null) {
                    intent.putExtra(ADD_MEASURE, addMeasure.name());
                }
                startActivityForResult(intent, SENSOR_SELECTION_REQUEST);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK  ||  data == null) {
            // For any unexpected case, we do not have a handler yet.
            return;
        }
        // Check which request we are responding to
        switch (requestCode) {
            case RECORD_SELECTION_REQUEST:
                long id = data.getLongExtra(RESULT_RECORD, -1);
                if (id > 0) {
                    try {
                        DataManager dataManager = DataManager.getInstance(this);
                        dataManager.open();
                        record = dataManager.findRecord(id);
                        dataManager.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    begin = record.getBegin();
                    end = record.getEnd();
                    drawGraph.setRecord(record);
                }
                Log.d(TAG, "result: " + record);
                break;

            case TIME_SPAN_SELECTION_REQUEST:
                begin = data.getLongExtra(RESULT_BEGIN, begin);
                end = data.getLongExtra(RESULT_END, end);
                record = null;
                drawGraph.setTimeSpan(begin, end);
                Log.d(TAG, String.format(Locale.ENGLISH, "result: %tF %<tT - %tF %<tT", begin, end));
                break;

            case SENSOR_SELECTION_REQUEST:
                String returnValue = data.getStringExtra(RESULT_MEASURE);
                if (returnValue == null || returnValue.equals(mainMeasure.name())) {
                    addMeasure = null;
                } else {
                    addMeasure = Measure.valueOf(returnValue);
                }
                drawGraph.setMeasure2(addMeasure);
                Log.d(TAG, "result: " + addMeasure);
                break;

            default:
                Log.e(TAG, "Received an unknown request code in onActivityResult.");
        }
        drawGraph.draw(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        if (addMeasure != null) {
            outState.putString(ADD_MEASURE, addMeasure.name());
        }
        if (record != null) {
            outState.putLong(RESULT_RECORD, record.getId());
        } else {
            outState.putLong(RESULT_BEGIN, begin);
            outState.putLong(RESULT_END, end);
        }
    }

}
