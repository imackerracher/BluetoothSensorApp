package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.BluetoothLeService;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.BluetoothMainActivity;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.DbExportImport;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measurement;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;
import com.example.groupfourtwo.bluetoothsensorapp.data.Sensor;
import com.example.groupfourtwo.bluetoothsensorapp.data.User;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    //Values to be displayed in the home screen next to the sensor icons
    TextView currentTemperature;
    TextView currentBrightness;
    TextView currentPressure;
    TextView currentHumidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Tie the values in content_main.xml to the textviews in this file
        currentTemperature = (TextView) findViewById(R.id.textViewCurrentTemperature);
        currentBrightness = (TextView) findViewById(R.id.textViewCurrentBrightness);
        currentPressure = (TextView) findViewById(R.id.textViewCurrentPressure);
        currentHumidity = (TextView) findViewById(R.id.textViewCurrentHumidity);

        setLatestSensorValues();

        registerReceiver(bleDataReceiver, makeBleDataIntentFilter());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage_sensors) {
            //changeToManageSensors();
            Intent intent = new Intent(this, ManageSensorsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_clear_data) {
            Intent intent = new Intent(this, ClearDataActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_manage_connection) {
            Intent intent = new Intent(this, BluetoothMainActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        stopService(gattServiceIntent);
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(bleDataReceiver, makeBleDataIntentFilter());
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(bleDataReceiver);
    }


    //Methods that handle the selection of the respective sensor in the home screen
    public void changeToTemperature(View view) {
        Intent intent = new Intent(this, TemperatureActivity.class);
        startActivity(intent);
    }


    public void changeToBrightness(View view) {
        Intent intent = new Intent(this, BrightnessActivity.class);
        startActivity(intent);
    }

    public void changeToPressure(View view) {
        Intent intent = new Intent(this, PressureActivity.class);
        startActivity(intent);
    }

    public void changeToHumidity(View view) {/*
        Intent intent = new Intent(this, HumidityActivity.class);
        startActivity(intent);*/
        DbExportImport.exportDb(this);
        Log.d(LOG_TAG, "Datenbank wurde exportiert.");
    }


    //Sets the values in the home screen next to the sensor icon to the last measured value
    public void setLatestSensorValues() {
        DataManager dataManager = DataManager.getInstance(this);
        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Measurement measurement = dataManager.getLatestMeasurement();
        dataManager.close();

        if (measurement != null) {
            setTemperature(measurement.getTemperature());
            setBrightness(measurement.getBrightness());
            setPressure(measurement.getPressure());
            setHumidity(measurement.getHumidity());
        }
    }

    public void setTemperature(Float t) {
        currentTemperature.setText(String.format(Locale.ENGLISH, "%.2f °C", t));
    }

    public void setBrightness(Float t) {
        currentBrightness.setText(String.format(Locale.ENGLISH, "%.2f lm", t));
    }

    public void setHumidity(Float t) {
        currentHumidity.setText(String.format(Locale.ENGLISH, "%.2f %%", t));
    }

    public void setPressure(Float t) {
        currentPressure.setText(String.format(Locale.ENGLISH, "%.2f hPa", t));
    }


    private final BroadcastReceiver bleDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //Log.d(LOG_TAG, "Broadcast received.");
            if (BluetoothLeService.ACTION_TEMP_DATA.equals(action)) {
                setTemperature(intent.getFloatExtra(BluetoothLeService.EXTRA_DATA,0.0f));

            } else if (BluetoothLeService.ACTION_HUM_DATA.equals(action)) {
                setHumidity(intent.getFloatExtra(BluetoothLeService.EXTRA_DATA,0.0f));

            } else if (BluetoothLeService.ACTION_BARO_DATA.equals(action)) {
                setPressure(intent.getFloatExtra(BluetoothLeService.EXTRA_DATA,0.0f));

            } else if (BluetoothLeService.ACTION_LUX_DATA.equals(action)) {
                setBrightness(intent.getFloatExtra(BluetoothLeService.EXTRA_DATA,0.0f));
            }
        }
    };

    private static IntentFilter makeBleDataIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_TEMP_DATA);
        intentFilter.addAction(BluetoothLeService.ACTION_HUM_DATA);
        intentFilter.addAction(BluetoothLeService.ACTION_BARO_DATA);
        intentFilter.addAction(BluetoothLeService.ACTION_LUX_DATA);
        return intentFilter;

    }
}
