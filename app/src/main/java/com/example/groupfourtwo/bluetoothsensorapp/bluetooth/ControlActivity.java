package com.example.groupfourtwo.bluetoothsensorapp.bluetooth;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.groupfourtwo.bluetoothsensorapp.main.MainActivity;
import com.example.groupfourtwo.bluetoothsensorapp.R;
import static com.example.groupfourtwo.bluetoothsensorapp.bluetooth.SensortagUUIDs.*;

import java.util.UUID;

/**
 * Bluetooth Connection Control Activity
 *
 * @author Tobias Nusser, Patrick Reichle
 * @version 1.0
 */

public class ControlActivity extends Activity {

    private final static String TAG = ControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_CONNECT = "CONNECT";


    private ProgressBar spinner;
    private boolean connect = false;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG,"onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                // Unable to initialize Bluetooth
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (connect) {
                Log.d(TAG, "connect");
                mBluetoothLeService.connect(mDeviceAddress);
            }
            else {
                Log.d(TAG, "disconnect");
                mBluetoothLeService.close();
                spinner.setVisibility(View.GONE);
                Intent intent = new Intent(BluetoothLeService.ACTION_RESET_BUTTON);
                sendBroadcast(intent);
                Intent intent1 = new Intent(ControlActivity.this, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //i.putExtra("BUTTON", false);
                startActivity(intent1);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG,"onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            boolean mConnected = false;
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;

                //Intent intent2 = new Intent(BluetoothLeService.ACTION_SET_BUTTON);
                //sendBroadcast(intent2);
                finish();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                for (UUID id : gattServices) {
                    subscribe(id);
                }
                spinner.setVisibility(View.GONE);
                Intent intent1 = new Intent(context, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                Intent intent2 = new Intent(BluetoothLeService.ACTION_SET_BUTTON);
                //sendBroadcast(intent2);
                finish();
            }
        }
    };

    private void subscribe(UUID id) {
        mBluetoothLeService.subscribe(id);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity_control);

        final Intent intent = getIntent();
        String mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        connect = intent.getBooleanExtra(EXTRAS_CONNECT, true);

        spinner = (ProgressBar) findViewById(R.id.bluetooth_spinner);
        spinner.setVisibility(View.VISIBLE);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        //if (mBluetoothLeService != null) {
        //    final boolean result = mBluetoothLeService.connect(mDeviceAddress);
        //    Toast.makeText(this, "result:" + result, Toast.LENGTH_SHORT).show();
        //}
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        unbindService(mServiceConnection);
        //mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;

    }


    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(UUID.fromString(uuid));
        return name == null ? defaultName : name;
    }





}
