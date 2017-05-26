package com.example.groupfourtwo.bluetoothsensorapp.BluetoothConnection;

/**
 * Bluetooth Low Energy Connection Service Activity
 *
 * @author Tobias Nusser
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
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

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

    public final static String ACTION_GATT_CONNECTED = "company.bluettest2.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "company.bluettest2.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "company.bluettest2.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "company.bluettest2.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "company.bluettest2.EXTRA_DATA";

    // Test UUIDs
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final String uuidBtSigBase = "0000****-0000-1000-8000-00805f9b34fb";
    private static final String uuidTiBase = "f000****-0451-4000-b000-000000000000";
    public static final UUID OAD_SERVICE_UUID = UUID.fromString("f000ffc0-0451-4000-b000-000000000000");
    public static final UUID CC_SERVICE_UUID = UUID.fromString("f000ccc0-0451-4000-b000-000000000000");

    // Implements callback methods for GATT events
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                // Discover available GATT Services
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                // Debug status
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();

        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));

                Log.v(TAG, String.format("%02X ", byteChar));
            }
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }

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
        // Closing GATT-Service
        close();
        return super.onUnbind(intent);
    }

    // Initializes a reference to the Bluetoothadapter
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


    // Connects to the GATT server hosted on the Bluetooth LE device.
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            return false;
        }
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
        Toast.makeText(this, "Debug: Establish connection", Toast.LENGTH_SHORT).show();
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    // Disconnects a connection between GATT-Server (device) and GATT-Client
    public void disconnect() {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    // Closing GATT-Service after a disconnect
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    // Request a read on a BluetoothGattCharacteristic
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    // Enables or disables notification on a given characteristic
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // Specific SensorTag Services.
        // CHANGE UUIDS         !!!
        // JUST EXAMPLE UUIDS   !!!
        if (OAD_SERVICE_UUID.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    OAD_SERVICE_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    // Retrieves a list of supported GATT services on the connected device
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}

