package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * @author Tobias Nusser
 * @version 1.0
 */

public class SensorSettingsActivity extends AppCompatActivity {

    private final static String TAG = MeasurementsActivity.class.getSimpleName();
    private Switch tempSwitch;
    private Switch humiditySwitch;
    private Switch brightSwitch;
    private Switch pressureSwitch;
    String previous  = "";
    Button saveBtn;
    String sensor_res;
    boolean prevTemp = false;
    boolean prevHum = false;
    boolean prevBri = false;
    boolean prevPres = false;

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
            previous = extras.getString("prevActivity");
            Log.d(TAG, "Previous Activity " + previous);
        }


        tempSwitch = (Switch) findViewById(R.id.switch1);
        brightSwitch = (Switch) findViewById(R.id.switch2);
        pressureSwitch = (Switch) findViewById(R.id.switch3);
        humiditySwitch = (Switch) findViewById(R.id.switch4);

        switch (previous) {
            case "TemperatureActivity":
                tempSwitch.setChecked(true);
                tempSwitch.setClickable(false);
                prevTemp = true;
                break;
            case "BrightnessActivity":
                brightSwitch.setChecked(true);
                brightSwitch.setClickable(false);
                prevBri = true;
                break;
            case "HumidityActivity":
                humiditySwitch.setChecked(true);
                humiditySwitch.setClickable(false);
                prevHum = true;
                break;
            case "PressureActivity":
                pressureSwitch.setChecked(true);
                pressureSwitch.setClickable(false);
                prevPres = true;
                break;
            default:
                Log.d(TAG, "Debug previous: " + previous );
        }


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


        saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tempSwitch.isChecked() && !(prevTemp)) {
                    sensor_res = "TemperatureActivity";
                } else if (humiditySwitch.isChecked() && !(prevHum)) {
                    sensor_res = "HumidityActivity";
                    Log.d(TAG, sensor_res);
                } else if (brightSwitch.isChecked() && !(prevBri)) {
                    sensor_res = "BrightnessActivity";
                } else if (pressureSwitch.isChecked() && !(prevPres))  {
                    sensor_res = "PressureActivity";
                } else {
                    sensor_res="";
                }
                Intent intent = new Intent();
                intent.putExtra("sensor_selection",sensor_res);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

}
