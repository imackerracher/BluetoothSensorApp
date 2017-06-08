package com.example.groupfourtwo.bluetoothsensorapp.BluetoothConnection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.groupfourtwo.bluetoothsensorapp.BluetoothConnection.BluetoothLeService;
import com.example.groupfourtwo.bluetoothsensorapp.Data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Measurement;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Record;
import com.example.groupfourtwo.bluetoothsensorapp.Data.User;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Sensor;

import java.io.IOException;

/**
 * Created by patrick on 06.06.17.
 */

public class DatabaseUpdateService extends Service {

    private String TAG = "databaseUpdateService";

    private User user = new User(1, "dummyUser");
    private Sensor sensor = new Sensor(2,"dummySensor",123123);
    private Record record = new Record(3,sensor,user,123,12345);

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


}
