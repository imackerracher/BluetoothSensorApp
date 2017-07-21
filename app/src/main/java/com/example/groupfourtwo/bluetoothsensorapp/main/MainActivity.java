package com.example.groupfourtwo.bluetoothsensorapp.main;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.BluetoothLeService;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.BluetoothMainActivity;
import com.example.groupfourtwo.bluetoothsensorapp.bluetooth.DatabaseUpdateService;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measurement;
import com.example.groupfourtwo.bluetoothsensorapp.data.Sensor;
import com.example.groupfourtwo.bluetoothsensorapp.data.User;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.BrightnessActivity;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.HumidityActivity;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.PressureActivity;
import com.example.groupfourtwo.bluetoothsensorapp.visualization.TemperatureActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.support.v4.content.FileProvider.getUriForFile;
import static com.example.groupfourtwo.bluetoothsensorapp.bluetooth.DatabaseUpdateService.*;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.*;

/**
 * The welcome and home screen of the application. Holds connection to the bluetooth services.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int SCREEN_ON = 0;
    private static final int SCREEN_OFF = 1;

    private long userId;

    private DatabaseUpdateService mDatabaseUpdateService;
    private DataManager dataManager;

    //Values to be displayed in the home screen next to the sensor icons
    private TextView currentTemperature;
    private TextView currentBrightness;
    private TextView currentPressure;
    private TextView currentHumidity;

    private ToggleButton buttonStartStop;

    /**
     * Whether the display shall be dimmed according to device settings.
     */
    private boolean displayTimeout = true;
    private boolean connected = false;

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
        buttonStartStop = (ToggleButton) findViewById(R.id.toggleStartStopRecord);
        buttonStartStop.setVisibility(View.GONE);

        dataManager = DataManager.getInstance(this);
        try {
            @SuppressLint("HardwareIds")
            String macAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
            userId = Sensor.parseAddress(macAddress);

            dataManager.open();
            if (!dataManager.existsUser(userId)) {
                showWelcomeDialog();
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }

        Intent databaseServiceIntent = new Intent(this, DatabaseUpdateService.class);
        databaseServiceIntent.putExtra(KEY_USER_ID, userId);
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

            alertDialogBuilder.setTitle("Exit app?");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Running records will be closed.")
                    .setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();

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
                intent.putExtra(BluetoothMainActivity.EXTRAS_CONNECT_BUTTON, !connected);
                startActivity(intent);
                break;

            case R.id.nav_export_import:
                intent = new Intent(this, StorageActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_help:
                File manual = getManual();
                if (manual == null) {
                    Toast.makeText(this, "Manual could not be found.", Toast.LENGTH_LONG).show();
                    break;
                }

                Uri contentUri = getUriForFile(this, "com.example.fileprovider", manual);
                intent = new Intent(ACTION_VIEW);
                intent.setDataAndType(contentUri, "application/pdf");
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(
                            this, "No Application found to display manual.",
                            Toast.LENGTH_LONG
                    ).show();
                }
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
        // Either show the icon to disable or enable display timeout.
        menu.getItem(SCREEN_ON).setVisible(!displayTimeout);
        menu.getItem(SCREEN_OFF).setVisible(displayTimeout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.screen_on:
                if (!displayTimeout){
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    displayTimeout = true;
                    Toast.makeText(this, "Display will be turned off.", Toast.LENGTH_SHORT).show();
                }
                invalidateOptionsMenu();
                return true;

            case R.id.screen_off:
                if (displayTimeout) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    displayTimeout = false;
                    Toast.makeText(this, "Display will stay turned on.", Toast.LENGTH_SHORT).show();
                }
                invalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bleDataReceiver);
        mDatabaseUpdateService.stopUpdating();
        if (mDatabaseUpdateService != null) {
            mDatabaseUpdateService.stopUpdating();
        }
        unbindService(mServiceConnection);
        Log.d(LOG_TAG, "onDestroy");
        Intent intent = new Intent(this,BluetoothLeService.class);
        stopService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setLatestSensorValues();
        //registerReceiver(bleDataReceiver, makeBleDataIntentFilter());
        Log.d(LOG_TAG, "onResume");
    }

    /*@Override
    protected void onNewIntent(Intent intent) {
        Log.d(LOG_TAG, "onNewIntent");
        boolean b = intent.getBooleanExtra("BUTTON", true);
        if (!b)
            button.setVisibility(View.VISIBLE);
    }*/



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

    /*
     * Broadcast receiver for various events.
     * ACTION_*_DATA: received new value, update UI
     * ACTION_SET_BUTTON: connected to sensor, enable start-/stopbutton
     * ACTION_GATT_DISCONNECTED: disconnected from sensor, disable start-/stopbutton,
     * close record if running.
     * ACTION_STATE_CHANGED: sent from BluetoothAdapter everytime its state changes,
     * if Bluetooth is getting turned off close record if running, disable button.
     */
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

                case BluetoothLeService.ACTION_SET_BUTTON:
                    buttonStartStop.setVisibility(View.VISIBLE);
                    break;

                case BluetoothLeService.ACTION_RESET_BUTTON:
                    buttonStartStop.setChecked(false);
                    mDatabaseUpdateService.stopUpdating();
                    break;

                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    connected = false;
                    buttonStartStop.setChecked(false);
                    buttonStartStop.setVisibility(View.GONE);
                    mDatabaseUpdateService.stopUpdating();
                    break;

                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    connected = true;
                    buttonStartStop.setVisibility(View.VISIBLE);
                    break;

                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                        buttonStartStop.setVisibility(View.GONE);
                        buttonStartStop.setChecked(false);
                        mDatabaseUpdateService.stopUpdating();
                        Log.d(LOG_TAG, "BLUETOOTH TURNED OFF");
                        stopBleService();
                    }
                    break;


                default:
                    Log.d(LOG_TAG, "Unknown broadcast action received.");
            }
        }
    };

    private void stopBleService() {
        Intent intent = new Intent(this,BluetoothLeService.class);
        stopService(intent);
    }

    private static IntentFilter makeBleDataIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_TEMP_DATA);
        intentFilter.addAction(BluetoothLeService.ACTION_HUM_DATA);
        intentFilter.addAction(BluetoothLeService.ACTION_BARO_DATA);
        intentFilter.addAction(BluetoothLeService.ACTION_LUX_DATA);
        intentFilter.addAction(BluetoothLeService.ACTION_SET_BUTTON);
        intentFilter.addAction(BluetoothLeService.ACTION_RESET_BUTTON);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
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


    /**
     * Shows a welcome dialog on the first app start, to set the device profile name.
     *
     * @return  a welcome dialog to enter a user name
     */
    private AlertDialog showWelcomeDialog() {

        final Context context = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        @SuppressLint("InflateParams")
        View dialogsView = inflater.inflate(R.layout.dialog_edit_name, null);

        final EditText editTextNewName = (EditText) dialogsView.findViewById(R.id.editText_new_name);

        builder.setView(dialogsView)
                .setTitle("Welcome")
                .setMessage("This seems to be your first visit.\nPlease enter a name.")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = editTextNewName.getText().toString();
                        if (name.trim().isEmpty() || name.length() > 50) {
                            Toast.makeText(context, "Invalid name please select again.",
                                    Toast.LENGTH_LONG).show();
                            showWelcomeDialog();
                        }

                        // Tell database to save the new user.
                        DataManager dataManager = DataManager.getInstance(context);
                        try {
                            dataManager.open();
                            dataManager.saveUser(new User(userId, name));
                        } catch (IOException | IllegalArgumentException e) {
                            e.printStackTrace();
                            return;
                        } finally {
                            dataManager.close();
                        }

                        dialog.dismiss();
                    }
                });
        return builder.show();
    }


    /**
     * Build a public pdf file that contains the user guide of the application to be displayed
     * with any pdf viewer (like Adobe Acrobat) that is installed on the device.     *
     *
     * @return  the abstract file of the application's user guide
     */
    private File getManual() {
        File manualDir = new File(getCacheDir(), "manual");
        File manual = new File(manualDir, "manual.pdf");
        if (!manualDir.exists() && !manualDir.mkdir()) {
            return null;
        }
        if (!manual.exists()) {
            InputStream res = null;
            OutputStream dest = null;
            try {
                res = getResources().openRawResource(R.raw.manual);
                dest = new FileOutputStream(manual);

                byte[] buf = new byte[1024];
                int len;
                while ((len = res.read(buf, 0, 1024)) > 0) {
                    dest.write(buf, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (res != null) {
                    try {
                        res.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dest != null) {
                    try {
                        dest.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return manual;
    }
}
