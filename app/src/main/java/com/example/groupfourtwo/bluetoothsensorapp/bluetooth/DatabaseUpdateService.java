package com.example.groupfourtwo.bluetoothsensorapp.bluetooth;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measurement;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;
import com.example.groupfourtwo.bluetoothsensorapp.data.User;
import com.example.groupfourtwo.bluetoothsensorapp.data.Sensor;

import java.io.IOException;

/**
 * Created by patrick on 06.06.17.
 */

public class DatabaseUpdateService extends Service {

    private String TAG = "databaseUpdateService";

    private User user = new User(1, "dummyUser");
    private Sensor sensor;
    private Record record;
    private long startTime;

    private String sensorAddress;

    private Float temp;
    private Float humidity;
    private Float pressure;
    private Float brightness;
    private long UPDATE_INTERVAL = 1000;

    private boolean isUpdating = false;


    private DataManager dataManager;
    // Service Binder
    public class LocalBinder extends Binder {
        public DatabaseUpdateService getService() {
            return DatabaseUpdateService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");
        //stopUpdating();
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataManager = DataManager.getInstance(this);
        Log.d(TAG, "Created new data manager object.");
        //startUpdating();
        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Open Database");
        Log.d(TAG, "onCreate() , service started...");
        registerReceiver(bleDataReceiver, makeBleDataIntentFilter());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataManager.close();
        //stopUpdating();
        unregisterReceiver(bleDataReceiver);
        Log.d(TAG, "onDestroy() , service stopped...");
    }

    public void startUpdating() {
        if (!isUpdating) {
            isUpdating = true;
            handler.postDelayed(runnable, 0);
            startTime = System.currentTimeMillis();
            sensor = dataManager.findSensor(Sensor.parseAddress(sensorAddress));
            if (sensor == null) {//sensor not in database -> add new Sensor
                sensor = new Sensor(Sensor.parseAddress(sensorAddress), "defaultName", startTime);
                dataManager.saveSensor(sensor);
                Log.d(TAG, "saved Sensor :" + sensorAddress);
            }
            record = dataManager.startRecord(sensor, user);
            Log.d(TAG, "startUpdating()");
        }
    }

    public void stopUpdating() {
        if (isUpdating) {
            isUpdating = false;
            Log.d(TAG, "stopUpdating()");
            dataManager.stopRecord(record);
            handler.removeCallbacks(runnable);
        }
    }
    private Handler handler = new Handler();


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long t1 = 0;
            //long t2 = 0;
            if (temp != null  && brightness != null && humidity != null && pressure != null) {
                t1 = System.currentTimeMillis();
                dataManager.saveMeasurement(Measurement.newMeasurement(
                        record, System.currentTimeMillis(), brightness, 0.0f, humidity, pressure, temp));
                //t2 = System.currentTimeMillis();
            }
            handler.postDelayed(this, Math.max(0, UPDATE_INTERVAL - (System.currentTimeMillis() - t1)));

        }
    };
    public void setTemp(float t) {
        //Log.d(TAG, "setTemp");
        temp = t;
    }

    public void setPressure(float t) {
        pressure = t;
    }

    public void setBrightness(float t) {
        brightness = t;
    }

    public void setHumidity(float t) {
        humidity = t;
    }

    public void setSensorAddress(String s) {
        Log.d(TAG, "setSensorAddress " + s);
        sensorAddress = s;
    }

    private final BroadcastReceiver bleDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothLeService.ACTION_TEMP_DATA:
                    temp = intent.getFloatExtra(BluetoothLeService.EXTRA_DATA, 0f);
                    break;

                case BluetoothLeService.ACTION_LUX_DATA:
                    brightness = intent.getFloatExtra(BluetoothLeService.EXTRA_DATA, 0f);
                    break;

                case BluetoothLeService.ACTION_BARO_DATA:
                    pressure = intent.getFloatExtra(BluetoothLeService.EXTRA_DATA, 0f);
                    break;

                case BluetoothLeService.ACTION_HUM_DATA:
                    humidity = intent.getFloatExtra(BluetoothLeService.EXTRA_DATA, 0f);
                    break;

                default:
                    Log.d(TAG, "Unknown broadcast action received.");
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
