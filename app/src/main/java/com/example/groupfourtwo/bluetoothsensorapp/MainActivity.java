package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.BluetoothLeService;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.BluetoothMainActivity;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.ControlActivity;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.DatabaseUpdateService;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measurement;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.BrightnessActivity;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.HumidityActivity;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.PressureActivity;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.TemperatureActivity;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private DatabaseUpdateService mDatabaseUpdateService;

    //Values to be displayed in the home screen next to the sensor icons
    TextView currentTemperature;
    TextView currentBrightness;
    TextView currentPressure;
    TextView currentHumidity;

    ToggleButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Tie the values in content_main.xml to the text views in this file
        currentTemperature = (TextView) findViewById(R.id.textViewCurrentTemperature);
        currentBrightness = (TextView) findViewById(R.id.textViewCurrentBrightness);
        currentPressure = (TextView) findViewById(R.id.textViewCurrentPressure);
        currentHumidity = (TextView) findViewById(R.id.textViewCurrentHumidity);
        button = (ToggleButton) findViewById(R.id.toggleStartStopRecord);
        button.setVisibility(View.GONE);
        setLatestSensorValues();


        Intent databaseServiceIntent = new Intent(this, DatabaseUpdateService.class);
        bindService(databaseServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(bleDataReceiver, makeBleDataIntentFilter());
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Intent intent;

        switch (item.getItemId()) {
            case R.id.nav_manage_sensors:
                intent = new Intent(this, ManageSensorsActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_clear_data:
                intent = new Intent(this, ClearDataActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_manage_connection:
                intent = new Intent(this, BluetoothMainActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_export_import:
                intent = new Intent(this, StorageActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;

            default:
                Log.d(LOG_TAG, "Unknown navigation item id");
                return false;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseUpdateService.stopUpdating();
        unbindService(mServiceConnection);
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(bleDataReceiver, makeBleDataIntentFilter());
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(LOG_TAG, "onNewIntent");
        boolean b = intent.getBooleanExtra("BUTTON", true);
        if (!b)
            button.setVisibility(View.VISIBLE);
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

    public void changeToHumidity(View view) {
        Intent intent = new Intent(this, HumidityActivity.class);
        startActivity(intent);
    }

    public void startStopRecord(View view) {
        if (button.isChecked()) {
            Log.d(LOG_TAG, "start record");
            mDatabaseUpdateService.startUpdating();
        } else {
            Log.d(LOG_TAG, "stop record");
            mDatabaseUpdateService.stopUpdating();
        }
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
            switch (action) {
                case BluetoothLeService.ACTION_TEMP_DATA:
                    setTemperature(intent.getFloatExtra(BluetoothLeService.EXTRA_DATA, 0f));
                    break;

                case BluetoothLeService.ACTION_LUX_DATA:
                    setBrightness(intent.getFloatExtra(BluetoothLeService.EXTRA_DATA, 0f));
                    break;

                case BluetoothLeService.ACTION_BARO_DATA:
                    setPressure(intent.getFloatExtra(BluetoothLeService.EXTRA_DATA, 0f));
                    break;

                case BluetoothLeService.ACTION_HUM_DATA:
                    setHumidity(intent.getFloatExtra(BluetoothLeService.EXTRA_DATA, 0f));
                    break;

                default:
                    Log.d(LOG_TAG, "Unknown broadcast action received.");
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


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(LOG_TAG, "onServiceConnected()");
            mDatabaseUpdateService = ((DatabaseUpdateService.LocalBinder) service).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDatabaseUpdateService = null;
        }
    };
}
