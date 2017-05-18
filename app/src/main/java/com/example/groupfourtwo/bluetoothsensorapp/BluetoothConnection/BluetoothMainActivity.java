package com.example.groupfourtwo.bluetoothsensorapp.BluetoothConnection;

/**
 * Bluetooth Connection Main Activity
 *
 * @author Tobias Nusser
 * @version 1.0
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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    List<BluetoothDevice> listBluetoothDevice;
    ListAdapter adapterLeScanResult;
    Button startScanningButton;
    Button stopScanningButton;
    ListView listViewLE;

    private boolean mScanning = false;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewLE = (ListView)findViewById(R.id.lelist);
        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        stopScanningButton = (Button) findViewById(R.id.StopScanButton);

        // Start scanning if Scann-button is clicked
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
        stopScanningButton.setVisibility(View.INVISIBLE);

        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // If Bluetooth isn't enabled yet, ask for permission to enable it
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }

        // If Access Coarse Location Permission isn't granted yet, a prompt shows up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Diese App benötigt Standortinformationen");
                builder.setMessage("Bitte aktivieren sie Standortdienste um Geräte in ihrer Nähe zu finden");
                builder.setPositiveButton(android.R.string.ok, null);
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

    // ItemClickListener in ListView. Get detailed information about the selected device and either go back or connect to device
    AdapterView.OnItemClickListener scanResultOnItemClickListener =
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);

                    String msg = device.getAddress() + "\n" + device.getBluetoothClass().toString();

                    new AlertDialog.Builder(BluetoothMainActivity.this)
                            .setTitle(device.getName())
                            .setMessage(msg)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setNeutralButton("Verbinden", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final Intent intent = new Intent(BluetoothMainActivity.this, ControlActivity.class);
                                    intent.putExtra(ControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                                    intent.putExtra(ControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());

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

    // Get scan result and add it to BluetoothDevice list
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addBluetoothDevice(result.getDevice());
        }
    };

    // GPS Location request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Standortinformationen wurden erlaubt.");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Achtung");
                    builder.setMessage("Da die Standortinformationen nicht abgerufen werden können, werden Geräte in ihrer Nähe nicht gefunden.");
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

    // Start the scan process
    public void startScanning() {
        mScanning = true;
        System.out.println("start scanning");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.startScan(leScanCallback);
            }
        });
    }


    // Stop the scan process
    public void stopScanning() {
        mScanning = false;
        System.out.println("stopping scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(leScanCallback);
            }
        });
    }

    // Add the scanned device to the listBluetoothDevice list
    private void addBluetoothDevice(BluetoothDevice device){
        if(!listBluetoothDevice.contains(device)){
            listBluetoothDevice.add(device);
            listViewLE.invalidateViews();
        }
    }
}
