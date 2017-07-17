package com.example.groupfourtwo.bluetoothsensorapp.visualization;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;
import com.example.groupfourtwo.bluetoothsensorapp.graph.DrawGraph;

import java.io.IOException;
import java.util.Locale;

import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.HOUR;


/**
 * Displays graphics of the data collected by the sensors.
 *
 * @author all
 */

public class VisualizationActivity extends AppCompatActivity {

    private static final String TAG = VisualizationActivity.class.getSimpleName();

    /* for declaring result requests on activity calls */
    private static final int RECORD_SELECTION_REQUEST = 2;
    private static final int TIME_SPAN_SELECTION_REQUEST = 3;
    private static final int SENSOR_SELECTION_REQUEST = 4;

    /* According to the item order in layout file. */
    private static final int REFRESH_BUTTON_INDEX = 3;

    /**
     * Formatting template for {@link #showInfo()}
     */
    private static final String SELECTION_INFO =
            "Record:\t%s\nSensor:\t%s\nStart:\t\t\t\t%tF  %<tR\nEnd:\t\t\t\t\t%tF  %<tR%s\nadded:\t\t%s";

    /* for declaring intent extras on activity calls */
    static final String MAIN_MEASURE = "main_measure";
    static final String ADD_MEASURE = "add_measure";
    static final String RESULT_BEGIN = "begin_selection";
    static final String RESULT_END = "end_selection";
    static final String RESULT_RECORD = "record_selection";
    static final String RESULT_MEASURE = "measure_selection";

    /**
     * The main measure to display, that is set by clicking in the main activity.
     */
    private final Measure mainMeasure;

    /**
     * The additional measure that is selected from
     */
    private Measure addMeasure;

    /**
     * The current record that is selected.
     */
    private Record record;

    /**
     * The start point of the current selection.
     */
    private long begin;

    /**
     * The end point of the current selection.
     */
    private long end;

    /**
     * Whether the refresh button is to be shown in the toolbar.
     * Only true if a running record is displayed.
     */
    private boolean refreshVisible = false;

    /**
     * The graph object that draws the requested data.
     */
    private DrawGraph drawGraph;

    /**
     * Sets the main measure according to which activity was called from main.
     *
     * @param measure  the measure that shall always be displayed
     */
    protected VisualizationActivity(Measure measure) {
        mainMeasure = measure;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);
        // Set activity title in usual style e.g.: Temperature instead of TEMPERATURE
        setTitle(mainMeasure.toString());

        end = System.currentTimeMillis();
        begin = end - HOUR.length;

        if (savedInstanceState != null) {
            Log.d(TAG, "restore old state");
            String sndMeasure = savedInstanceState.getString(ADD_MEASURE);
            if (sndMeasure != null) {
                addMeasure = Measure.valueOf(sndMeasure);
            }

            long recordId = savedInstanceState.getLong(RESULT_RECORD);
            if (recordId > 0) {
                DataManager dataManager = DataManager.getInstance(this);
                try {
                    dataManager.open();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                record = dataManager.findRecord(recordId);
                dataManager.close();
                begin = record.getBegin();
                end = record.getEnd();
                refreshVisible = record.isRunning();
            } else {
                begin = savedInstanceState.getLong(RESULT_BEGIN, begin);
                end = savedInstanceState.getLong(RESULT_END, end);
            }
        }

        drawGraph = new DrawGraph(this, mainMeasure, addMeasure, record, begin, end);
        drawGraph.draw(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.visualization, menu);
        menu.getItem(REFRESH_BUTTON_INDEX).setVisible(refreshVisible);
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

            case R.id.refresh:
                if (record != null) {
                    end = record.getEnd();
                }
                drawGraph.refresh();
                drawGraph.draw(this);
                return true;

            case R.id.info:
                showInfo();
                return true;

            case R.id.zoom_out:
                recreate();
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
                    refreshVisible = record.isRunning();
                    invalidateOptionsMenu();
                    drawGraph.setRecord(record);
                }
                Log.d(TAG, "result: " + record);
                break;

            case TIME_SPAN_SELECTION_REQUEST:
                begin = data.getLongExtra(RESULT_BEGIN, begin);
                end = data.getLongExtra(RESULT_END, end);
                record = null;
                drawGraph.setTimeSpan(begin, end);
                refreshVisible = false;
                invalidateOptionsMenu();
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


    /**
     * Show a message with details about the current selection including
     * <ul>
     *     <li> the selected record
     *     <li> the sensor that recorded the displayed data
     *     <li> the displayed time frame in date and time
     *     <li> if this is a single record: whether it is still running
     *     <li> the secondary measure that is displayed
     * </ul>
     */
    private void showInfo() {
        String details = String.format(Locale.ENGLISH,
                SELECTION_INFO,
                record == null ? "all" : "#" + record.getId(),
                record == null ? "all" : record.getSensor().getName(),
                begin,
                end,
                record != null && record.isRunning() ? "  (running)" : "",
                addMeasure != null ? addMeasure : "none"
        );

        new AlertDialog.Builder(this)
                .setTitle("Current selection")
                .setMessage(details)
                .setCancelable(true)
                .show();
    }
}
