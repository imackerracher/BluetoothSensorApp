package com.example.groupfourtwo.bluetoothsensorapp.bluetooth;

/**
 * Scans for bluetooh devices and connects with selected ones
 *
 * @author Tobias Nusser
 * @version 1.2
 */

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.groupfourtwo.bluetoothsensorapp.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothMainActivity extends AppCompatActivity {

    /* Debugging only */
    private final static String TAG = BluetoothMainActivity.class.getSimpleName();

    /**
     * A bluetooth manager which brings necessary methods for searching and connecting to devices
     */
    BluetoothManager bluetoothManager;

    /**
     * Bluetooth adapter which handles the connection
     */
    BluetoothAdapter bluetoothAdapter;

    /**
     * Scanner instance, which is able to scan for devices
     */
    BluetoothLeScanner bluetoothLeScanner;

    /**
     * A cache for all BLE devices which got found
     */
    List<BluetoothDevice> listBluetoothDevice;

    /**
     * Listadapter to connect BLE device cache with ListView
     */
    ListAdapter adapterLeScanResult;

    /**
     * Start scan button
     */
    Button startScanningButton;

    /**
     * Stop scan button
     */
    Button stopScanningButton;

    /**
     * Disconnect button
     */
    Button disconnectButton;

    /**
     * ListView for the BLE devices which got scanned
     */
    ListView listViewLE;

    /**
     * Boolean value for checking the LEScanners state
     */
    private boolean mScanning = false;

    /**
     * Request values for diverse requests
     */
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    /**
     * Extras connect information
     */
    public static final String EXTRAS_CONNECT_BUTTON = "EXTRAS_CONNECT";

    /**
     * Called when "Manage Connection" gets opened
     * @param savedInstanceState Bundle object containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity_main);
        listViewLE = (ListView)findViewById(R.id.lelist);
        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        disconnectButton = (Button) findViewById(R.id.DisconnectButton);

        Intent intent = getIntent();
        boolean connect = intent.getBooleanExtra(EXTRAS_CONNECT_BUTTON, true);

        if (connect)
            disconnectButton.setVisibility(View.GONE);
        else
            startScanningButton.setVisibility(View.GONE);

        // Start scanning if Scan-button is clicked
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        // Start scanning if Stop-button is clicked
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnect();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);


        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            // If Bluetooth isn't enabled yet, ask for permission to enable it
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            }

            boolean gps_enabled = isLocationEnabled(this);
            Log.d(TAG, "Debug: gps_enabled = " + gps_enabled);
            // If GPS is disabled a prompt shows up to activate GPS in the settings
            if(!gps_enabled) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage(this.getResources().getString(R.string.gps_not_enabled));
                dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent gpsOptionsIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                    }
                });
                dialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
                dialog.show();
            }
        }

        // If Access Coarse Location Permission isn't granted yet, a prompt shows up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.request_gps_title));
                builder.setMessage(getString(R.string.request_gps_message));
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
            }
        }


        // Add scan results to the result adapter and update the listView for the GUI
        listBluetoothDevice = new ArrayList<>();
        adapterLeScanResult = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listBluetoothDevice);
        listViewLE.setAdapter(adapterLeScanResult);
        listViewLE.setOnItemClickListener(scanResultOnItemClickListener);
    }


    /**
     * ItemClickListener in ListView. Get detailed information about the selected device and either go back or connect to device
     * Stop scan after connecting to a device
     */
    AdapterView.OnItemClickListener scanResultOnItemClickListener =
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);

                    String msg = device.getAddress() + "\n" + device.getBluetoothClass().toString();

                    new AlertDialog.Builder(BluetoothMainActivity.this)
                            .setTitle(device.getName())
                            .setMessage(msg)
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton(getString(R.string.connect), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final Intent intent = new Intent(BluetoothMainActivity.this, ControlActivity.class);
                                    intent.putExtra(ControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                                    intent.putExtra(ControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                                    intent.putExtra(ControlActivity.EXTRAS_CONNECT, true);

                                    if (mScanning) {
                                        stopScanning();
                                        mScanning = false;
                                    }
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
            };


    /**
     * If a device gets found, method gets invoked and calls the addBluetoothDevice method
     * which adds the found devices to a list
     */
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addBluetoothDevice(result.getDevice());
        }
    };


    /**
     * Method which gets invoked when the user answers a permission request
     *
     * @param requestCode identifier for the permission request
     * @param permissions list of permissions we requested
     * @param grantResults list of permissions which we got from the request
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Debug: Location Information granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(this.getString(R.string.attention));
                    builder.setMessage(this.getString(R.string.location_request_denied));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
            }
        }
    }


    /**
     * Start scan for BLE devices
     */
    public void startScanning() {
        mScanning = true;
        Log.d(TAG, "Debug: Start Scanning");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                if (!(bluetoothLeScanner == null)) {
                    bluetoothLeScanner.startScan(leScanCallback);
                }
                else {
                    Log.d(TAG, "Debug: BLE Scanner Object empty");
                }
            }
        });
    }


    /**
     * Stop scan for BLE devices
     */
    public void stopScanning() {
        mScanning = false;
        Log.d(TAG, "Debug: Stop Scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (!(bluetoothLeScanner == null)) {
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
                else {
                    Log.d(TAG, "Debug: BLE Scanner Object empty");
                }
            }
        });
    }

    /**
     * Disconnects from a BLE device
     */
    private void disconnect() {
        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra(ControlActivity.EXTRAS_CONNECT, false);
        startActivity(intent);
    }


    /**
     * Adds the scanned BLE devices to the BLE device list
     * @param device which got found by the scan-method
     */
    private void addBluetoothDevice(BluetoothDevice device){
        if(!listBluetoothDevice.contains(device)){
            listBluetoothDevice.add(device);
            listViewLE.invalidateViews();
        }
    }


    /**
     * Checks if Location Services are enabled
     * @param context of the current state of the application
     * @return boolean if GPS is enabled = true otherwise false
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }
}
