package com.example.groupfourtwo.bluetoothsensorapp.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.BluetoothLeService;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.BluetoothMainActivity;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.DatabaseUpdateService;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measurement;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.BrightnessActivity;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.HumidityActivity;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.PressureActivity;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.TemperatureActivity;

import java.io.IOException;
import java.util.Locale;

import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private DatabaseUpdateService mDatabaseUpdateService;
    private DataManager dataManager;

    //Values to be displayed in the home screen next to the sensor icons
    TextView currentTemperature;
    TextView currentBrightness;
    TextView currentPressure;
    TextView currentHumidity;

    ToggleButton buttonStartStop;

    /**
     * Whether the display shall be dimmed according to device settings.
     */
    private boolean displayTimeout = true;

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

        dataManager = DataManager.getInstance(this);

        //Tie the values in content_main.xml to the text views in this file
        currentTemperature = (TextView) findViewById(R.id.textViewCurrentTemperature);
        currentBrightness = (TextView) findViewById(R.id.textViewCurrentBrightness);
        currentPressure = (TextView) findViewById(R.id.textViewCurrentPressure);
        currentHumidity = (TextView) findViewById(R.id.textViewCurrentHumidity);
        buttonStartStop = (ToggleButton) findViewById(R.id.toggleStartStopRecord);
        buttonStartStop.setVisibility(View.GONE);


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
            //super.onBackPressed();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("Exit app?");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Running records will be closed.")
                    .setCancelable(false)
                    .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

        }
        //finish();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.screen_on:
                if (displayTimeout) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    displayTimeout = false;
                    Toast.makeText(this, "Display will stay turned on.", Toast.LENGTH_SHORT).show();
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    displayTimeout = true;
                    Toast.makeText(this, "Display will be turned off.", Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseUpdateService.stopUpdating();
        unbindService(mServiceConnection);
        Log.d(LOG_TAG, "onDestroy");
        Intent intent = new Intent(this,BluetoothLeService.class);
        stopService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setLatestSensorValues();
        registerReceiver(bleDataReceiver, makeBleDataIntentFilter());
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(LOG_TAG, "onNewIntent");
        boolean b = intent.getBooleanExtra("BUTTON", true);
        if (!b)
            buttonStartStop.setVisibility(View.VISIBLE);
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
        if (buttonStartStop.isChecked()) {
            Log.d(LOG_TAG, "start record");
            mDatabaseUpdateService.startUpdating();
        } else {
            Log.d(LOG_TAG, "stop record");
            mDatabaseUpdateService.stopUpdating();
        }
    }

    /**
     * Sets the values in the home screen next to the sensor icon to the value lastly measured.
     */
    public void setLatestSensorValues() {
        dataManager = DataManager.getInstance(this);
        try {
            dataManager.open();
            Measurement measurement = dataManager.getLatestMeasurement();
            dataManager.close();

            if (measurement != null) {
                setTemperature(measurement.getTemperature());
                setBrightness(measurement.getBrightness());
                setPressure(measurement.getPressure());
                setHumidity(measurement.getHumidity());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTemperature(Float t) {
        currentTemperature.setText(String.format(Locale.ENGLISH, "%.2f %s", t, TEMPERATURE.unit));
    }

    public void setBrightness(Float t) {
        currentBrightness.setText(String.format(Locale.ENGLISH, "%.2f %s", t, BRIGHTNESS.unit));
    }

    public void setHumidity(Float t) {
        currentHumidity.setText(String.format(Locale.ENGLISH, "%.2f %s", t, HUMIDITY.unit));
    }

    public void setPressure(Float t) {
        currentPressure.setText(String.format(Locale.ENGLISH, "%.2f %s", t, PRESSURE.unit));
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
