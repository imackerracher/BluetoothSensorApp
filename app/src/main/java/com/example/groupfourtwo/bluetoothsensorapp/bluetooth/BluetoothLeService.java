package com.example.groupfourtwo.bluetoothsensorapp.bluetooth;

/**
 * Bluetooth Low Energy Connection Service Activity
 *
 * @author Tobias Nusser, Patrick Reichle
 * @version 1.0
 */



import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.example.groupfourtwo.bluetoothsensorapp.R;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.example.groupfourtwo.bluetoothsensorapp.bluetooth.SensortagUUIDs.*;
import static com.example.groupfourtwo.bluetoothsensorapp.bluetooth.Conversions.*;

import java.util.List;

// Service for managing connection and data communication with a Gatt server hosted on BLE device
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static final byte ENABLE_SENSOR = (byte) 0x01;

    private static final Queue<Object> sWriteQueue = new ConcurrentLinkedQueue<Object>();
    private static boolean sIsWriting = false;

    private boolean connected = false;


    public final static String ACTION_GATT_CONNECTED = "company.bluettest2.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "company.bluettest2.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "company.bluettest2.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "company.bluettest2.ACTION_DATA_AVAILABLE";
    public final static String ACTION_HUM_DATA = "company.bluettest2.ACTION_HUM_DATA";
    public final static String ACTION_TEMP_DATA = "company.bluettest2.ACTION_TEMP_DATA";
    public final static String ACTION_BARO_DATA = "company.bluettest2.ACTION_BARO_DATA";
    public final static String ACTION_LUX_DATA = "company.bluettest2.ACTION_LUX_DATA";
    public final static String ACTION_SENSOR_ADDRESS = "company.bluettest2.ACTION_SENSOR_ADDRESS";
    public final static String ACTION_SET_BUTTON = "company.bluettest2.ACTION_SET_BUTTON";
    public final static String ACTION_RESET_BUTTON = "company.bluettest2.ACTION_RESET_BUTTON";
    public final static String EXTRA_DATA = "company.bluettest2.EXTRA_DATA";
    public final static String EXTRA_ADDRESS = "company.bluettest2.EXTRA_ADDRESS";

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    // Implements callback methods for GATT events
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //connected = true;
                //Log.d(TAG, "connected = " + connected);
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                broadcastAddress(gatt.getDevice().getAddress());
                // Discover available GATT Services
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //connected = false;
                //Log.d(TAG, "connected = " + connected);
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
                Log.d(TAG,"DISCONNECTED");
                //TODO: disable button
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.d(TAG,"onServicesDiscovered");
            } else {
                // Debug status
            }
        }

        //Not needed, characteristics are read via notifications
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        /**
         * Called everytime a characteristic was written.
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            sIsWriting = false;
            nextWrite();
        }

        /**
         * Called everytime a descriptor was written.
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            sIsWriting = false;
            nextWrite();
        }

        /**
         * Is called everytime the value in a subscribed characteristic changes.
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //Log.d(TAG, "DATA AVAILABLE");
            String action = new String();
            final byte[] raw = characteristic.getValue();
            float[] data;
            Float val = 0.0f;
            UUID id = characteristic.getUuid();
            if (id.equals(UUID_HUM_DATA)) {
                data = conversionHum(raw);
                action = ACTION_HUM_DATA;
                val = data[0];
                //mDatabaseUpdateService.setHumidity(val);
            } else if (id.equals(UUID_IR_TEMP_DATA)) {
                data = conversionIRTemp(raw);
                action = ACTION_TEMP_DATA;
                val = data[0];
                //mDatabaseUpdateService.setTemp(val);
            } else if (id.equals(UUID_BAROMETER_DATA)) {
                data = conversionBaro(raw);
                action = ACTION_BARO_DATA;
                val = data[0];
                //mDatabaseUpdateService.setPressure(val);
            } else if (id.equals(UUID_LUXMETER_DATA)) {
                data = conversionLux(raw);
                action = ACTION_LUX_DATA;
                val = data[0];
                //mDatabaseUpdateService.setBrightness(val);
            }

            broadcastUpdate(action, characteristic, val);
        }
    };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);

    }

    private void broadcastAddress(final String address) {
        final Intent intent = new Intent(ACTION_SENSOR_ADDRESS);
        intent.putExtra(EXTRA_ADDRESS, address);
        sendBroadcast(intent);

    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic, final float data) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, data);
        sendBroadcast(intent);
    }

    // Service Binder
    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"onUnbind()");
        // Closing GATT-Service
        //close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the Bluetoothadapter
     */
    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Toast.makeText(this, "Debug: Initialize Failure", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Debug: Initialize Failure", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     */
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            return false;
        }

        /*if (connected) {
            disconnect();
            close();
            Intent intent = new Intent(ACTION_RESET_BUTTON);
            sendBroadcast(intent);
        }*/
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            // No device found
            return false;
        }
        // Establish GATT-Server and GATT-Client connection
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //Toast.makeText(this, "Debug: Establish connection", Toast.LENGTH_SHORT).show();
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    /**
     * Disconnects a connection between GATT-Server (device) and GATT-Client
     */
    public void disconnect() {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * Closing GATT-Service after a disconnect
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Enable and subscribe a given sensor
     */
    public void subscribe(UUID id) {
        byte[] b = new byte[1];
        BluetoothGattService service = mBluetoothGatt.getService(id);
        if (service == null) {
            Toast.makeText(this, "Failed to find Service" + '\n' + lookup(id), Toast.LENGTH_SHORT).show();
        } else {
            BluetoothGattCharacteristic dataChar = service.getCharacteristic(data.get(id));
            BluetoothGattCharacteristic configChar = service.getCharacteristic(config.get(id));
            if (configChar == null || dataChar == null) {
                Toast.makeText(this, "Failed to find Characteristic" + '\n' + lookup(id), Toast.LENGTH_SHORT).show();
            } else {
                BluetoothGattDescriptor configDescr = dataChar.getDescriptor(UUID_CCC);
                if (configDescr == null) {
                    Toast.makeText(this, "Failed to find Descriptor" + '\n' + lookup(id), Toast.LENGTH_SHORT).show();
                } else {
                    mBluetoothGatt.setCharacteristicNotification(dataChar, true);
                    b[0] = ENABLE_SENSOR;
                    configChar.setValue(b);
                    write(configChar);
                    configDescr.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    write(configDescr);
                }
            }
        }

    }

    /**
     * Add new write request to queue or execute if queue is empty.
     */
    private synchronized void write(Object o) {
        if (sWriteQueue.isEmpty() && !sIsWriting) {
            doWrite(o);
        } else {
            sWriteQueue.add(o);
        }
    }

    /**
     * Start new write after another write has been finished.
     */
    private synchronized void nextWrite() {
        if (!sWriteQueue.isEmpty() && !sIsWriting) {
            doWrite(sWriteQueue.poll());
        }
    }

    /**
     * Execute write request.
     */
    private synchronized void doWrite(Object o) {
        if (o instanceof BluetoothGattCharacteristic) {
            sIsWriting = true;
            mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic) o);
        } else if (o instanceof BluetoothGattDescriptor) {
            sIsWriting = true;
            mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor) o);
        } else {
            nextWrite();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() , BLEservice stopped");
        close();
        //stopUpdating();
    }

}

