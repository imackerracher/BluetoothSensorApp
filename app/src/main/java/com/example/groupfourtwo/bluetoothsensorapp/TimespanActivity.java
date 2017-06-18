package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import java.util.Calendar;

/**
 * @author Tobias Nusser, Ian Mackerracher
 * @version 1.0
 */

public class TimespanActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = MeasurementsActivity.class.getSimpleName();

    Button btnDatePickerStart, btnTimePickerStart, btnDatePickerEnd, btnTimePickerEnd, btnDisplayTimeframe;
    EditText txtDateStart, txtTimeStart, txtDateEnd, txtTimeEnd;
    private int mYearStart, mMonthStart, mDayStart, mHourStart, mMinuteStart, mYearEnd, mMonthEnd, mDayEnd, mHourEnd, mMinuteEnd;
    private boolean dateStart = false;
    private boolean timeStart = false;
    private boolean dateEnd = false;
    private boolean timeEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timespan);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnDatePickerStart=(Button)findViewById(R.id.btn_date_start);
        btnTimePickerStart=(Button)findViewById(R.id.btn_time_start);
        txtDateStart=(EditText)findViewById(R.id.in_date_start);
        txtTimeStart=(EditText)findViewById(R.id.in_time_start);

        btnDatePickerStart.setOnClickListener(this);
        btnTimePickerStart.setOnClickListener(this);


        btnDatePickerEnd=(Button)findViewById(R.id.btn_date_end);
        btnTimePickerEnd=(Button)findViewById(R.id.btn_time_end);
        txtDateEnd=(EditText)findViewById(R.id.in_date_end);
        txtTimeEnd=(EditText)findViewById(R.id.in_time_end);

        btnDatePickerEnd.setOnClickListener(this);
        btnTimePickerEnd.setOnClickListener(this);

        btnDisplayTimeframe=(Button)findViewById(R.id.btn_save_display);
        btnDisplayTimeframe.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == btnDatePickerStart) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYearStart = c.get(Calendar.YEAR);
            mMonthStart = c.get(Calendar.MONTH);
            mDayStart = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            txtDateStart.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYearStart, mMonthStart, mDayStart);
            datePickerDialog.show();
            dateStart = true;
        }
        if (v == btnTimePickerStart) {

            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHourStart = c.get(Calendar.HOUR_OF_DAY);
            mMinuteStart = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            txtTimeStart.setText(hourOfDay + ":" + minute);
                        }
                    }, mHourStart, mMinuteStart, true);
            timePickerDialog.show();
            timeStart = true;
        }

        if (v == btnDatePickerEnd) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYearEnd = c.get(Calendar.YEAR);
            mMonthEnd = c.get(Calendar.MONTH);
            mDayEnd = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            txtDateEnd.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYearEnd, mMonthEnd, mDayEnd);
            datePickerDialog.show();
            dateEnd = true;
        }
        if (v == btnTimePickerEnd) {

            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHourEnd = c.get(Calendar.HOUR_OF_DAY);
            mMinuteEnd = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            txtTimeEnd.setText(hourOfDay + ":" + minute);
                        }
                    }, mHourEnd, mMinuteEnd, true);
            timePickerDialog.show();
            timeEnd = true;
        }

        if (v == btnDisplayTimeframe) {

            if (dateStart == false || timeStart == false || dateEnd == false || timeStart == false) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Select Valid Time Span");
                builder.setCancelable(true);
                AlertDialog alert = builder.create();
                alert.show();
            }
            else {
                //Needs the previous activity
                Intent intent = new Intent(this, TemperatureActivity.class);
                startActivity(intent);

            }

        }
    }
}