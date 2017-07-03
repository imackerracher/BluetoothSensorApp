package com.example.groupfourtwo.bluetoothsensorapp.main;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Sensor;

import java.io.IOException;
import java.util.List;

public class ManageSensorsActivity extends AppCompatActivity {

    private static final String LOG_TAG = ManageSensorsActivity.class.getSimpleName();

    ListView sensorList;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sensors);

        showAllSensors();

        sensorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Sensor sensor = (Sensor) adapterView.getItemAtPosition(i);
                Log.d(LOG_TAG, "On item clicked " + sensor.toString());
                createEditSensorDialog(sensor, getApplication()).show();
            }
        });
    }


    private AlertDialog createEditSensorDialog(final Sensor sensor, final Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogsView = inflater.inflate(R.layout.dialog_edit_sensor, null);

        final EditText editTextNewName = (EditText) dialogsView.findViewById(R.id.editText_new_name);
        editTextNewName.setText(sensor.getName());

        builder.setView(dialogsView)
                .setTitle("edit Name")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String Name = editTextNewName.getText().toString();

                        // Tell database to alter the name of the sensor.
                        DataManager dataManager = DataManager.getInstance(context);
                        try {
                            dataManager.open();
                            dataManager.renameSensor(sensor, Name);
                            dataManager.close();
                        } catch (IOException | IllegalArgumentException e) {
                            e.printStackTrace();
                            return;
                        }
                        showAllSensors();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder.show();
    }


    private void showAllSensors() {
        DataManager dataManager = DataManager.getInstance(this);
        try {
            dataManager.open();
            final List<Sensor> allSensors = dataManager.getAllSensors();
            dataManager.close();
            final ListAdapter myAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1 , allSensors);
            sensorList = (ListView) findViewById(R.id.sensors_listview);
            sensorList.setAdapter(myAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
