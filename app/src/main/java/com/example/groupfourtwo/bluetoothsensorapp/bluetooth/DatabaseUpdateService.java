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

    private static final String TAG = "databaseUpdateService";

    public static final String KEY_USER_ID = "user_id";

    private User user;
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
        long userId = intent.getLongExtra(KEY_USER_ID, 0);
        if (dataManager.existsUser(userId)) {
            user = dataManager.findUser(userId);
        } else {
            user = new User(userId, "defaultName");
        }
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataManager = DataManager.getInstance(this);
        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        registerReceiver(bleDataReceiver, makeBleDataIntentFilter());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataManager.close();
        unregisterReceiver(bleDataReceiver);
    }

    public void startUpdating() {
        if (!isUpdating) {
            isUpdating = true;
            handler.post(updateTask);
            startTime = System.currentTimeMillis();

            long sensorId = Sensor.parseAddress(sensorAddress);

            if (dataManager.existsSensor(sensorId)) {
                sensor = dataManager.findSensor(sensorId);
            } else {//sensor not in database -> add new Sensor
                sensor = new Sensor(sensorId, "new Sensor: " + sensorAddress, startTime);
                dataManager.saveSensor(sensor);
                Log.d(TAG, "saved Sensor :" + sensorAddress);
            }
            record = dataManager.startRecord(sensor, user);
        }
    }

    public void stopUpdating() {
        if (isUpdating) {
            isUpdating = false;
            Log.d(TAG, "stopUpdating()");
            dataManager.stopRecord(record);
            handler.removeCallbacks(updateTask);
        }
    }
    private Handler handler = new Handler();


    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            long t1 = 0;
            if (temp != null  && brightness != null && humidity != null && pressure != null) {
                t1 = System.currentTimeMillis();
                dataManager.saveMeasurement(Measurement.newMeasurement(
                        record, System.currentTimeMillis(), brightness, 0.0f, humidity, pressure, temp));
            }
            handler.postDelayed(this, Math.max(0, UPDATE_INTERVAL - (System.currentTimeMillis() - t1)));

        }
    };

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
                case BluetoothLeService.ACTION_SENSOR_ADDRESS:
                    sensorAddress = intent.getStringExtra(BluetoothLeService.EXTRA_ADDRESS);
                    Log.d(TAG, sensorAddress);
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
        intentFilter.addAction(BluetoothLeService.ACTION_SENSOR_ADDRESS);
        return intentFilter;

    }


}
