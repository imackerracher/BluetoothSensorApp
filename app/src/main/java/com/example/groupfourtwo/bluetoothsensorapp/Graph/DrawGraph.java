package com.example.groupfourtwo.bluetoothsensorapp.Graph;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import com.example.groupfourtwo.bluetoothsensorapp.Data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.Data.Record;
import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import static com.example.groupfourtwo.bluetoothsensorapp.Data.Interval.*;

/**
 * Created by kim on 23.05.17.
 */



public class DrawGraph {


    private Context context;
    private Measure measure1, measure2;
    private Interval interval;
    private long begin;
    private Record record;

    public DrawGraph(Context context , Measure measure1, Measure measure2,
                     Interval interval, long begin) {
        this.context = context;
        this.measure1 = measure1;
        this.measure2 = measure2;
        this.interval = interval;
        this.begin = begin;
        record = null;
    }


    public DrawGraph(Context context , Measure measure1, Measure measure2,
                     Record record) {
        this.context = context;
        begin = record.getBegin();
        long end = record.getEnd();

        int step = HOUR.step;
        if (end - begin > HOUR.length)
            step = DAY.step;
        if (end - begin > DAY.length)
            step = WEEK.step;
    }


    public void draw(Activity activity) {

        int dataPointCount = (interval.length / interval.step);

        LineChart lineChart;


        long offset = 1723680000000l;


        /*referenz in main.xml*/
        lineChart = (LineChart) activity.findViewById(R.id.lineChart);
        

        Legend l = lineChart.getLegend();
        l.setEnabled(false); //The Legend is DISABLED

        lineChart.setDescription(null);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(0f); //smallest value that is displayed on the XAxis
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //Choose between TOP, BOTTOM, BOTH_SIDED, TOP_INSIDE or BOTTOM_INSIDE
        xAxis.setTextSize(10f);


        /**
         * Generate the Values on the X-Axis
         */
        MyXAxisValueFormatter x = new MyXAxisValueFormatter(lineChart);
        x.setPointsPerMinute(60/(interval.step/1000));
        x.setStartInSec(60*60*24*365 + 60*60*24*150);
        xAxis.setValueFormatter(x);








        /* Generating empty y-Value Array */
        Float[] yAxes = new Float[dataPointCount]; //static


        /**
         * Access to Database NOT USED
         */
        DataManager dataManager = DataManager.getInstance(context);
            dataManager.open();

        ArrayList<Float> yAxes2;

        if( record == null)
            yAxes2 = dataManager.getValuesFromInterval(measure1, interval, begin);
        else
            yAxes2 = dataManager.getValuesFromRecord(measure1, record);

        dataManager.close();




        /**
         * Some Data generated for testing USED
         */

        int numDataPoints = dataPointCount;
        int gapSize = dataPointCount/10;
        int gapPosition = dataPointCount/3;


        for (int i = 0; i < gapPosition; i++)
            yAxes[i] = (float) i;

        for (int i = gapPosition + gapSize; i < numDataPoints; i++)
            yAxes[i] = (float) i;



        /**
        * Split the graph in section
        * */
        ArrayList<Entry> yAxes2_1 = new ArrayList<>();
        ArrayList<Entry> yAxes2_2 = new ArrayList<>();

        for (int i = 0; i < gapPosition; i++) {
            yAxes2_1.add(new Entry(i, yAxes[i]));

        }


        for (int i = gapPosition + gapSize; i < numDataPoints; i++) {
            yAxes2_2.add(new Entry(i, yAxes[i]));
        }



        /**
        * Generate the lineDataSets for Visualisation
        * */
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();


        LineDataSet lineDataSet2 = new LineDataSet(yAxes2_1, null);
        lineDataSet2.setColor(Color.RED);


        LineDataSet lineDataSet3 = new LineDataSet(yAxes2_2, null);
        lineDataSet3.setColor(Color.RED);


        lineDataSets.add(lineDataSet2);
        lineDataSets.add(lineDataSet3);

        lineChart.setData(new LineData(lineDataSets));





        /**
         * Restraining what's visible
         **/
        lineChart.setVisibleXRangeMaximum((float) dataPointCount);
        lineChart.setVisibleXRangeMinimum(10f);

        // lineChart.animateX(3000); // Animation that shows the values from left to right


        lineChart.setBackgroundColor(Color.WHITE); //BackgroundColour

    }


}
