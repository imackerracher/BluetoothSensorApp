package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.example.groupfourtwo.bluetoothsensorapp.BluetoothConnection.BluetoothLeService;
import com.example.groupfourtwo.bluetoothsensorapp.BluetoothConnection.BluetoothMainActivity;
import com.example.groupfourtwo.bluetoothsensorapp.Data.DataManager;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private DataManager dataManager;



    //Values to be displayed in the homescreen next to the sensor icons
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

        //dataManager = DataManager.getInstance(this);
        //Log.d(LOG_TAG, "Created new data manager object.");


        //Tie the values in content_main.xml to the textviews in this file
        currentTemperature = (TextView) findViewById(R.id.textViewCurrentTemperature);
        currentBrightness = (TextView) findViewById(R.id.textViewCurrentBrightness);
        currentPressure = (TextView) findViewById(R.id.textViewCurrentPressure);
        currentHumidity = (TextView) findViewById(R.id.textViewCurrentHumidity);

        setCurrentSensorValues();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(bleDataReceiver, makeBleDataIntentFilter());
        //Log.d(LOG_TAG, "Opening connection to Database...");
        //dataManager.open();
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(bleDataReceiver);
        //Log.d(LOG_TAG, "Closing connection to Database...");
        //dataManager.close();
    }


    //Methods that handle the selection of the respective sensor in the homescreen
    public void changeToTemperature(View view) {
        Intent intent = new Intent(this, TemperatureActivity.class);
        startActivity(intent);
    }


    public void changeToBrightness(View view) {
        Intent intent = new Intent(this, BrigthnessActivity.class);
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


    //Sets the values in the homescreen next to the sensor icon to the current/last measured value
    public void setCurrentSensorValues() {
        currentTemperature.setText("30°C");
        currentBrightness.setText("970 hPa");
        currentPressure.setText("10000 lm");
        currentHumidity.setText("55 %");
    }

    public void setTemperature(Float t) {
        currentTemperature.setText(t.toString() + "°C");
    }

    public void setBrightness(Float t) {
        currentBrightness.setText(t.toString() + "lm");
    }

    public void setHumidity(Float t) {
        currentHumidity.setText(t.toString() + "%");
    }

    public void setPressure(Float t) {
        currentPressure.setText(t.toString() + "hPa");
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
