package com.example.groupfourtwo.bluetoothsensorapp.visualization;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;

import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.*;

/**
 * Handler for the sensor settings. Ensures ability to choose two sensor visualizations.
 *
 * @author Tobias Nusser
 * @version 1.1
 */

public class SensorSettingsActivity extends AppCompatActivity {

    /* debugging only */
    private static final String TAG = RecordsActivity.class.getSimpleName();

    /**
     * Temperature Switch to de-/activate in visualization
     */
    private Switch tempSwitch;

    /**
     * Humidity Switch to de-/activate in visualization
     */
    private Switch humiditySwitch;

    /**
     * Brightness Switch to de-/activate in visualization
     */
    private Switch brightSwitch;

    /**
     * Pressure Switch to de-/activate in visualization
     */
    private Switch pressureSwitch;

    /**
     * Previous visualized measure in the visualization
     */
    Measure previous;

    /**
     * Previous visualized measure saved in local variable
     */
    Measure prevAdditional;

    /**
     * Button for accepting changes and switching the activity back to the visualization with the
     * parameters chosen through the switches
     */
    Button saveBtn;

    /**
     * Booleans for previous activated visualizations
     */
    boolean prevTemp = false;
    boolean prevHum = false;
    boolean prevBri = false;
    boolean prevPres = false;


    /**
     * Method which handles the interaction with the switches
     *
     * @param savedInstanceState Bundle object containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            previous = Measure.valueOf(extras.getString(VisualizationActivity.MAIN_MEASURE));
            Log.d(TAG, "Main measure / calling Activity " + previous);
            String addName = extras.getString(VisualizationActivity.ADD_MEASURE);
            if (addName != null) {
                prevAdditional = Measure.valueOf(addName);
            }
        }


        tempSwitch = (Switch) findViewById(R.id.switch1);
        brightSwitch = (Switch) findViewById(R.id.switch2);
        pressureSwitch = (Switch) findViewById(R.id.switch3);
        humiditySwitch = (Switch) findViewById(R.id.switch4);

        // Set the switch for the main measure, cannot be deactivated.
        switch (previous) {
            case TEMPERATURE:
                tempSwitch.setChecked(true);
                tempSwitch.setClickable(false);
                prevTemp = true;
                break;
            case BRIGHTNESS:
                brightSwitch.setChecked(true);
                brightSwitch.setClickable(false);
                prevBri = true;
                break;
            case HUMIDITY:
                humiditySwitch.setChecked(true);
                humiditySwitch.setClickable(false);
                prevHum = true;
                break;
            case PRESSURE:
                pressureSwitch.setChecked(true);
                pressureSwitch.setClickable(false);
                prevPres = true;
                break;
            default:
                Log.d(TAG, "Debug previous: " + previous );
        }

        // Event-Listener for the temperature switch
        tempSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (tempSwitch.isChecked()) {
                    if (humiditySwitch.isChecked()) {
                        pressureSwitch.setClickable(false);
                        brightSwitch.setClickable(false);
                    } else if (pressureSwitch.isChecked()) {
                        humiditySwitch.setClickable(false);
                        brightSwitch.setClickable(false);
                    } else if (brightSwitch.isChecked()) {
                        pressureSwitch.setClickable(false);
                        humiditySwitch.setClickable(false);
                    }
                } else {
                    if (humiditySwitch.isChecked()) {
                        pressureSwitch.setClickable(true);
                        brightSwitch.setClickable(true);
                    } else if (pressureSwitch.isChecked()) {
                        humiditySwitch.setClickable(true);
                        brightSwitch.setClickable(true);
                    } else if (brightSwitch.isChecked()) {
                        pressureSwitch.setClickable(true);
                        humiditySwitch.setClickable(true);
                    }
                }
            }
        });


        // Event-Listener for the humidity switch
        humiditySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (humiditySwitch.isChecked()) {
                    if (tempSwitch.isChecked()) {
                        pressureSwitch.setClickable(false);
                        brightSwitch.setClickable(false);
                    } else if (pressureSwitch.isChecked()) {
                        tempSwitch.setClickable(false);
                        brightSwitch.setClickable(false);
                    } else if (brightSwitch.isChecked()) {
                        pressureSwitch.setClickable(false);
                        tempSwitch.setClickable(false);
                    }
                } else {
                    if (tempSwitch.isChecked()) {
                        pressureSwitch.setClickable(true);
                        brightSwitch.setClickable(true);
                    } else if (pressureSwitch.isChecked()) {
                        tempSwitch.setClickable(true);
                        brightSwitch.setClickable(true);
                    } else if (brightSwitch.isChecked()) {
                        pressureSwitch.setClickable(true);
                        tempSwitch.setClickable(true);
                    }
                }
            }
        });

        // Event-Listener for the brightness switch
        brightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (brightSwitch.isChecked()) {
                    if (tempSwitch.isChecked()) {
                        pressureSwitch.setClickable(false);
                        humiditySwitch.setClickable(false);
                    } else if (pressureSwitch.isChecked()) {
                        tempSwitch.setClickable(false);
                        humiditySwitch.setClickable(false);
                    } else if (humiditySwitch.isChecked()) {
                        pressureSwitch.setClickable(false);
                        tempSwitch.setClickable(false);
                    }
                } else {
                    if (tempSwitch.isChecked()) {
                        pressureSwitch.setClickable(true);
                        humiditySwitch.setClickable(true);
                    } else if (pressureSwitch.isChecked()) {
                        tempSwitch.setClickable(true);
                        humiditySwitch.setClickable(true);
                    } else if (humiditySwitch.isChecked()) {
                        pressureSwitch.setClickable(true);
                        tempSwitch.setClickable(true);
                    }
                }
            }
        });

        // Event-Listener for the pressure switch
        pressureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (pressureSwitch.isChecked()) {
                    if (tempSwitch.isChecked()) {
                        brightSwitch.setClickable(false);
                        humiditySwitch.setClickable(false);
                    } else if (brightSwitch.isChecked()) {
                        tempSwitch.setClickable(false);
                        humiditySwitch.setClickable(false);
                    } else if (humiditySwitch.isChecked()) {
                        brightSwitch.setClickable(false);
                        tempSwitch.setClickable(false);
                    }
                } else {
                    if (tempSwitch.isChecked()) {
                        brightSwitch.setClickable(true);
                        humiditySwitch.setClickable(true);
                    } else if (brightSwitch.isChecked()) {
                        tempSwitch.setClickable(true);
                        humiditySwitch.setClickable(true);
                    } else if (humiditySwitch.isChecked()) {
                        brightSwitch.setClickable(true);
                        tempSwitch.setClickable(true);
                    }
                }
            }
        });


        // If there was already a previous additional measure, mark it as activated, too.
        if (prevAdditional != null) {
            switch (prevAdditional) {
                case TEMPERATURE:
                    tempSwitch.performClick();
                    break;
                case BRIGHTNESS:
                    brightSwitch.performClick();
                    break;
                case HUMIDITY:
                    humiditySwitch.performClick();
                    break;
                case PRESSURE:
                    pressureSwitch.performClick();
                    break;
                default:
                    Log.d(TAG, "Bad prevAdditional measure: " + prevAdditional);
            }
        }

        // Clicking the "save" button will invoke the intent with the additional measure to visualize
        saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sensor_res;
                if (tempSwitch.isChecked() && !prevTemp) {
                    sensor_res = TEMPERATURE.name();
                } else if (humiditySwitch.isChecked() && !prevHum) {
                    sensor_res = HUMIDITY.name();
                } else if (brightSwitch.isChecked() && !prevBri) {
                    sensor_res = BRIGHTNESS.name();
                } else if (pressureSwitch.isChecked() && !prevPres)  {
                    sensor_res = PRESSURE.name();
                } else {
                    sensor_res = null;
                }
                Intent intent = new Intent();
                intent.putExtra(VisualizationActivity.RESULT_MEASURE, sensor_res);
                setResult(RESULT_OK, intent);
                Log.d(TAG, "New additional measure: " + sensor_res);
                finish();
            }
        });

    }

}
