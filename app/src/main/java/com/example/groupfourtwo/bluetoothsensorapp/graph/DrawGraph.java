package com.example.groupfourtwo.bluetoothsensorapp.graph;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;
import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.example.groupfourtwo.bluetoothsensorapp.R.id.end;
import static com.example.groupfourtwo.bluetoothsensorapp.R.id.lineChart;
import static com.example.groupfourtwo.bluetoothsensorapp.R.id.right;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.*;
import static com.github.mikephil.charting.components.YAxis.AxisDependency.RIGHT;

/**
 * Contains methods and parameters that are used to draw the graph
 * Created by kim on 23.05.17.
 * @author kim
 * @version 1.0
 *
 */

//Ideas: make the parts with missing Values White or Red.

public class DrawGraph {


    private Context context;
    private Measure measure1, measure2;
    private Interval interval;
    private long begin;
    private Record record;
    private int step;
    private int backgroundColour = Color.WHITE;
    private  LineChart lineChart;

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

        this.step = HOUR.step;

        if (end - begin > HOUR.length)
            step = DAY.step;
        if (end - begin > DAY.length)
            step = WEEK.step;
    }


    public void draw(Activity activity) {

        int dataPointCount; // maximal number of dataPoints that can be displayed

        if (record == null)
            dataPointCount = (interval.length / interval.step);
        else
            dataPointCount = ((int) (begin - end) / step);



        long offset =  1448841600000l; // time in milliseconds to 1.Jan 2016


        /*reference in main.xml*/
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
        x.setPointsPerMinute(60/(interval.step/1000)); // Maximal number of data points per minute
        x.setStartInSec((begin - offset) ); // The start in milliseconds since 1st Jan 2016 in UTC
        xAxis.setValueFormatter(x);

        /**
         * Generate Y-Axis format
         */

        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        MyYAxisValueFormatter y1 = new MyYAxisValueFormatter(measure1);
        MyYAxisValueFormatter y2 = new MyYAxisValueFormatter(measure2);


        leftAxis.setTextSize(12f);
        leftAxis.setValueFormatter(y1);

        rightAxis.setEnabled(false);
        if(measure2 != null) {
            rightAxis.setEnabled(true);
            rightAxis.setValueFormatter(y2);
            rightAxis.setDrawGridLines(false); // no grid lines
            //rightAxis.setAxisMinimum(0f);
            //rightAxis.setAxisMaximum(20f);
        }




        /**
         * Access to Database USED
         */
        DataManager dataManager = DataManager.getInstance(context);

        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Entry> yAxes1;
        ArrayList<Entry> yAxes2;

        if (record == null) {
            yAxes1 = dataManager.getValuesFromInterval(measure1, interval, begin);
        }else {
            yAxes1 = dataManager.getValuesFromRecord(measure1, record);
        }

        if (measure2 != null) {
            if (record == null) {
                yAxes2 = dataManager.getValuesFromInterval(measure2, interval, begin);
            } else {
                yAxes2 = dataManager.getValuesFromRecord(measure2, record);
            }
        } else
            yAxes2 = null;

        dataManager.close();

        if(yAxes1 == null) {
            return;
        }


        /**
         * Some Data generated for testing NOT USED
         */
        /***************************************/

        int numDataPoints = dataPointCount;
        int gapSize = dataPointCount/10;
        int gapPosition = dataPointCount/3;

                /* Generating empty y-Value Array */
        ArrayList<Float> yAxesTest = new ArrayList<>();

        /* genarate data for Testing************/
        Random randomGenerator = new Random();
        yAxesTest.add(null);

        for (int i = 1; i < gapPosition; i++)
            yAxesTest.add( (float) Math.sin(((double) i + randomGenerator.nextInt(25))/200) *3 +4);

        for (int i = gapPosition; i < gapPosition + gapSize; i++)
            yAxesTest.add(null);


        for (int i = gapPosition + gapSize; i < numDataPoints; i++)
            yAxesTest.add(  (float) Math.sin(((double) i + randomGenerator.nextInt(25))/200) *3 +4);

        /************************************/






        /**
        * Generate the lineDataSets for Visualisation
        * */
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();


        LineDataSet lineDataSet1 = new LineDataSet(yAxes1, "1");
        lineDataSet1.setColors(createColorArray(yAxes1, measure1));

        LineDataSet lineDataSet2 = new LineDataSet(yAxes2,"2");
        if (measure2 != null) {
            lineDataSet2.setColors(createColorArray(yAxes2, measure2));
            lineDataSet2.setAxisDependency(RIGHT);
        }




        //LineDataSet lineDataSet3 = new LineDataSet(yAxes2_2, "2");
        //setLineColor(lineDataSet3, measure1);


        lineDataSets.add(lineDataSet1);
        lineDataSets.add(lineDataSet2);
        //lineDataSets.add(lineDataSet3);


        lineChart.setData(new LineData(lineDataSets));




        /**
         * Restraining what's visible
         **/
        lineChart.setVisibleXRangeMaximum((float) dataPointCount);
        lineChart.setVisibleXRangeMinimum(10f);

        //lineChart.animateX(1000); // Animation that shows the values from left to right

        lineDataSet1.setDrawValues(true); //Default is true
        lineDataSet1.setDrawCircles(false); //Default is true
        lineDataSet2.setDrawCircles(false);
        lineChart.setMaxVisibleValueCount(20);

        lineChart.setDrawBorders(true); //Border around the Graph
        lineChart.setBorderColor( Color.BLACK);
        lineChart.setBorderWidth(1f);
        //lineChart.setNoDataText("Sorry, there is no Data in this time slot");

        lineChart.setKeepScreenOn(true);
        lineChart.setKeepPositionOnRotation(true);

        lineChart.setBackgroundColor(backgroundColour); //BackgroundColour of the whole background

    }

    private int getLineColor(Measure measure) {
        switch (measure) {
            case TEMPERATURE:   return Color.RED;

            case HUMIDITY:      return Color.BLUE;

            case BRIGHTNESS:    return Color.MAGENTA;

            case PRESSURE:      return Color.GRAY;

            case DISTANCE:      return Color.BLACK;

            default:            return Color.YELLOW;
        }
    }

    private int[] createColorArray(ArrayList<Entry> YAxis , Measure measure) {
        int size = YAxis.size();
        int[] colorArray = new int[size];
        int lineColor = getLineColor(measure);
        int i = 0;


        while (i < size-1) {
            if(YAxis.get(i).getX() - YAxis.get(i+1).getX() == -1)
                colorArray[i] = lineColor;
            else
                colorArray[i] = backgroundColour;

            i++;
        }

        return colorArray;
    }

    public void setMeasure2(Measure measure) {
        measure2 = measure;
        lineChart.notifyDataSetChanged(); // let the chart know it's data changed
        lineChart.invalidate(); // refresh
    }




}
