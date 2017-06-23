package com.example.groupfourtwo.bluetoothsensorapp.graph;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.example.groupfourtwo.bluetoothsensorapp.data.DataManager;
import com.example.groupfourtwo.bluetoothsensorapp.data.Interval;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.example.groupfourtwo.bluetoothsensorapp.data.Record;
import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.util.ArrayList;

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

    private static final String LOG_TAG = DrawGraph.class.getSimpleName();

    private Context context;
    private Measure measure1, measure2;
    private long begin;
    private long end;
    private Record record;
    private int backgroundColour = Color.BLACK;
    private int textColour = Color.WHITE;
    private LineChart lineChart;

    public DrawGraph(Context context , Measure measure1, Measure measure2,
                     long begin, long end) {
        this.context = context;
        this.measure1 = measure1;
        this.measure2 = measure2;
        this.begin = begin;
        this.end = end;
        record = null;
    }


    public void draw(Activity activity) {

        Interval interval = fromLength(end - begin);
        long length = (end - begin);
        // maximal number of dataPoints that can be displayed
        int dataPointCount = (int) (length / ((long) (interval.step))) + 1;



        long offset =  1448841600000L; // time in milliseconds to 1.Jan 2016


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
        xAxis.setTextColor(textColour);


        /**
         * Generate the Values on the X-Axis
         */
        MyXAxisValueFormatter x = new MyXAxisValueFormatter(lineChart);

        x.setPointsPerHour(3600/(interval.step/1000)); // Maximal number of data points per Hour
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
        leftAxis.setTextColor(textColour);

        rightAxis.setEnabled(false);
        if(measure2 != null) {
            rightAxis.setEnabled(true);
            rightAxis.setValueFormatter(y2);
            rightAxis.setDrawGridLines(false); // no grid lines
            rightAxis.setTextColor(textColour);
            //rightAxis.setAxisMinimum(0f);
            //rightAxis.setAxisMaximum(20f);
        }


        IMarker mv = new CustomMarkerView(context, R.layout.marker_view);

        lineChart.setMarker(mv);


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
            yAxes1 = dataManager.getValuesFromInterval(measure1, begin, end);
        } else {
            yAxes1 = dataManager.getValuesFromRecord(measure1, record);
        }

        if (measure2 != null) {
            if (record == null) {
                yAxes2 = dataManager.getValuesFromInterval(measure2, begin, end);
            } else {
                yAxes2 = dataManager.getValuesFromRecord(measure2, record);
            }
        } else
            yAxes2 = null;

        dataManager.close();

        if(yAxes1 == null  ||  yAxes1.isEmpty()) {
            return;
        }


        /*
         * Some Data generated for testing NOT USED
         *

        int numDataPoints = dataPointCount;
        int gapSize = dataPointCount/10;
        int gapPosition = dataPointCount/3;

                //Generating empty y-Value Array
        ArrayList<Float> yAxesTest = new ArrayList<>();

        // genarate data for Testing ************
        Random randomGenerator = new Random();
        yAxesTest.add(null);

        for (int i = 1; i < gapPosition; i++)
            yAxesTest.add( (float) Math.sin(((double) i + randomGenerator.nextInt(25))/200) *3 +4);

        for (int i = gapPosition; i < gapPosition + gapSize; i++)
            yAxesTest.add(null);


        for (int i = gapPosition + gapSize; i < numDataPoints; i++)
            yAxesTest.add(  (float) Math.sin(((double) i + randomGenerator.nextInt(25))/200) *3 +4);

        */






        /**
        * Generate the lineDataSets for Visualisation
        * */
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();


        LineDataSet lineDataSet1 = new LineDataSet(yAxes1, "1");
        lineDataSet1.setColors(createColorArray(yAxes1, measure1));
        lineDataSet1.setHighLightColor(measure1.color);
        lineDataSets.add(lineDataSet1);

        LineDataSet lineDataSet2 = new LineDataSet(yAxes2,"2");
        if (measure2 != null) {
            lineDataSet2.setColors(createColorArray(yAxes2, measure2));
            lineDataSet2.setAxisDependency(RIGHT);
            lineDataSet2.setHighLightColor(measure2.color);
            lineDataSets.add(lineDataSet2);

        }




        //LineDataSet lineDataSet3 = new LineDataSet(yAxes2_2, "2");
        //setLineColor(lineDataSet3, measure1);




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
        lineChart.setBorderColor( textColour);
        lineChart.setBorderWidth(1f);
        //lineChart.setNoDataText("Sorry, there is no Data in this time slot");

        lineChart.setKeepScreenOn(true);
        lineChart.setKeepPositionOnRotation(true);

        lineChart.setBackgroundColor(backgroundColour); //BackgroundColour of the whole background

    }


    private int[] createColorArray(ArrayList<Entry> YAxis , Measure measure) {
        int size = YAxis.size();
        int[] colorArray = new int[size];
        int i = 0;


        while (i < size-1) {
            if(YAxis.get(i).getX() - YAxis.get(i+1).getX() == -1)
                colorArray[i] = measure.color;
            else
                colorArray[i] = backgroundColour;

            i++;
        }

        return colorArray;
    }

    public void setMeasure2(Measure measure) {
        if (measure != measure1) {
            measure2 = measure;
        } else {
            measure2 = null;
        }
        Log.d(LOG_TAG, "Measure 2 wurde auf " + measure2 + " gestellt.");
        lineChart.notifyDataSetChanged(); // let the chart know it's data changed
        lineChart.invalidate(); // refresh
    }

    public void setRecord(Record record) {
        if (record != null) {
            this.record = record;
            begin = record.getBegin();
            end = record.getEnd();
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        }
    }

    public void setTimeSpan(long begin, long end) {
        if (end <= begin) {
            throw new IllegalArgumentException("Begin must lay in the past of end.");
        }
        if (end - begin > MONTH.length) {
            throw new IllegalArgumentException("Time span too long.");
        }
        this.end = end;
        this.begin = end;
        record = null;
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }




}
