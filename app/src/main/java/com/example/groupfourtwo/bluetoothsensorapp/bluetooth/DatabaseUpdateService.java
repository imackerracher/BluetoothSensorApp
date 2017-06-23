package com.example.groupfourtwo.bluetoothsensorapp.bluetooth;

import android.app.Service;
import android.content.Intent;
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


    private DataManager dataManager;
    // Service Binder
    public class LocalBinder extends Binder {
        DatabaseUpdateService getService() {
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

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataManager.close();
        //stopUpdating();
        Log.d(TAG, "onDestroy() , service stopped...");
    }

    public void startUpdating() {
        handler.postDelayed(runnable, 0);
        startTime = System.currentTimeMillis();
        sensor = dataManager.findSensor(Sensor.parseAddress(sensorAddress));
        if (sensor == null) {//sensor not in database -> add new Sensor
            sensor = new Sensor(Sensor.parseAddress(sensorAddress), "defaultName", startTime);
            dataManager.saveSensor(sensor);
        }
        record = dataManager.startRecord(sensor, user);
        Log.d(TAG, "startUpdating()");
    }

    public void stopUpdating() {
        Log.d(TAG, "stopUpdating()");
        dataManager.stopRecord(record);
        handler.removeCallbacks(runnable);
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
        Log.d(TAG, "setSensorAddress" + s);
        sensorAddress = s;
    }


}
