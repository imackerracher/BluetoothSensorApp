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
    private Sensor sensor = new Sensor(2,"dummySensor",123123);
    private Record record = new Record(3,sensor,user,123,12345);

    private String sensorAddress;

    private Float temp;
    private Float humidity;
    private Float pressure;
    private Float brightness;

    //private Measurement m = Measurement.newMeasurement(record, System.currentTimeMillis(), brightness, 0.0f, humidity, pressure, temp);

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
        handler.removeCallbacks(runnable);
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataManager = DataManager.getInstance(this);
        Log.d(TAG, "Created new data manager object.");
        startUpdating();
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
        handler.removeCallbacks(runnable);
        Log.d(TAG, "onDestroy() , service stopped...");
    }

    public void startUpdating() {
        handler.postDelayed(runnable, 0);
        Log.d(TAG, "startUpdating()");

    }
    private Handler handler = new Handler();


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (temp != null  && brightness != null && humidity != null && pressure != null)
                dataManager.saveMeasurement(Measurement.newMeasurement(
                        record,System.currentTimeMillis(),brightness,0.0f,humidity,pressure,temp));
            handler.postDelayed(this, 1000);
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
