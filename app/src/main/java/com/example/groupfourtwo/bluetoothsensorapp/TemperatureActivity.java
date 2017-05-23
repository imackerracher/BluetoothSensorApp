package com.example.groupfourtwo.bluetoothsensorapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class TemperatureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        //Get the intent that started this activity
        Intent intent = getIntent();
        LineChart lineChart;





        /*referenz in main.xml*/
            lineChart = (LineChart) findViewById(R.id.lineChart);

            Legend l = lineChart.getLegend();
            l.setFormSize(10f); // set the size of the legend forms/shapes

            l.setTextSize(12f);
            l.setTextColor(Color.BLACK);
            l.setXEntrySpace(5f); // set the space between the legend entries on the x-axis
            l.setYEntrySpace(5f); // set the space between the legend entries on the y-axis
            l.setEnabled(false); //The Legend is DISABLED

            XAxis xAxis = lineChart.getXAxis();
            //xAxis.setAxisMinimum(200f);


            int numDataPoints = 10000;
            int gapSize = 500;
            int gapPosition = 6000;



        /* deklaration of the Value-Lists */
            ArrayList<String> xAxes = new ArrayList<>();
            Float[] yAxes2 = new Float[numDataPoints]; //static



        /*put some Values on the Xaxes*/
            for (int i = 0; i < numDataPoints; i++)
                xAxes.add(i, String.valueOf(i));


        /*1 testdataset*/


        /*y = sin(x) data with gaps*/
            for (int i = 0; i < gapPosition; i++)
                yAxes2[i] = (float) Math.sin((float) i / 300);

            for (int i = gapPosition + gapSize; i < numDataPoints; i++)
                yAxes2[i] = (float) Math.sin((float) i / 300);


        /* split the graph in sections */
            ArrayList<Entry> yAxes2_1 = new ArrayList<>();
            ArrayList<Entry> yAxes2_2 = new ArrayList<>();

            for (int i = 0; i < gapPosition; i++) {
                yAxes2_1.add(new Entry(i, yAxes2[i]));

            }


            for (int i = gapPosition + gapSize; i < numDataPoints; i++) {
                yAxes2_2.add(new Entry(i, yAxes2[i]));
            }

            /*
            String[] xaxes = new String[xAxes.size()];

            for (int i = 0; i < xAxes.size(); i++)
                xaxes[i] = xAxes.get(i).toString();

            */
            ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();


            LineDataSet lineDataSet2 = new LineDataSet(yAxes2_1, "Humidity");
            lineDataSet2.setColor(Color.RED);


            LineDataSet lineDataSet3 = new LineDataSet(yAxes2_2, " ");
            lineDataSet3.setColor(Color.RED);


            lineDataSets.add(lineDataSet2);
            lineDataSets.add(lineDataSet3);


            lineChart.setData(new LineData(lineDataSets));


            //## Restraining what's visible
            lineChart.setVisibleXRangeMaximum(20000f); // allow 200 values to be displayed at once on the x-axis, not more
            lineChart.setVisibleXRangeMinimum(20f);

            // lineChart.animateX(3000); // Animation that shows the values from left to right


            lineChart.setBackgroundColor(Color.WHITE); //BackgroundColour



    }
}
