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

import static com.example.groupfourtwo.bluetoothsensorapp.data.Interval.*;

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
    private int backgroundColour = Color.WHITE;
    private boolean allNull = true;

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


        long offset =  1448841600000l; // time in milliseconds to 1.Jan 2016
        long offset2 = 1723680000000l;


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
        x.setPointsPerMinute(60/(interval.step/1000));
        x.setStartInSec((begin - offset) ); //+
        xAxis.setValueFormatter(x);

        /**
         * Generate Y-Axis format
         * Per default, all data that is added to the chart plots against the left YAxis of the
         * chart. If not further specified and enabled, the right YAxis is adjusted to represent
         * the same scale as the left axis.
         */

        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();

        rightAxis.setEnabled(false);

        if(measure1 != Measure.PRESSURE)
            leftAxis.setAxisMinimum(0f); // start at zero

        leftAxis.setTextSize(12f);
        MyYAxisValueFormatter y = new MyYAxisValueFormatter(measure1);
        leftAxis.setValueFormatter(y);







        /**
         * Access to Database NOT USED so far
         */
        DataManager dataManager = DataManager.getInstance(context);

        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Float> yAxes2;

        if (record == null) {
            yAxes2 = dataManager.getValuesFromInterval(measure1, interval, begin);
            System.out.println("0000000000000000000000000000000000000000000000000000000000000000000000000000000");
            System.out.println(yAxes2.get(1));
        }else {
            yAxes2 = dataManager.getValuesFromRecord(measure1, record);
        }
        dataManager.close();




        /**
         * Some Data generated for testing USED
         */

        int numDataPoints = dataPointCount;
        int gapSize = dataPointCount/10;
        int gapPosition = dataPointCount/3;


                /* Generating empty y-Value Array */
        ArrayList<Float> yAxes = new ArrayList<>(); //static
        Random randomGenerator = new Random();


        yAxes.add(null);

        for (int i = 1; i < gapPosition; i++)
            yAxes.add( (float) Math.sin(((double) i + randomGenerator.nextInt(25))/200) *3 +4);

        for (int i = gapPosition; i < gapPosition + gapSize; i++)
            yAxes.add(null);


        for (int i = gapPosition + gapSize; i < numDataPoints; i++)
            yAxes.add(  (float) Math.sin(((double) i + randomGenerator.nextInt(25))/200) *3 +4);



        /**
        * Copy graph in Entry-List
        * */
        ArrayList<Entry> yAxes2_1 = new ArrayList<>();
        ArrayList<Entry> yAxes2_2 = new ArrayList<>();

        for (int i = 0; i < dataPointCount; i++)
            if (yAxes2.get(i) != null)
                allNull = false;

        for (int i = 0; i < dataPointCount; i++) {
            if(yAxes2.get(i) != null)
                yAxes2_1.add(new Entry(i, yAxes2.get(i)));
        }




        if(allNull)
            return;

        /**
        * Generate the lineDataSets for Visualisation
        * */
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();


        LineDataSet lineDataSet2 = new LineDataSet(yAxes2_1, "1");

        lineDataSet2.setColors(createColorArray(yAxes2_1, measure1));





        //LineDataSet lineDataSet3 = new LineDataSet(yAxes2_2, "2");
        //setLineColor(lineDataSet3, measure1);


        lineDataSets.add(lineDataSet2);
        //lineDataSets.add(lineDataSet3);


        lineChart.setData(new LineData(lineDataSets));




        /**
         * Restraining what's visible
         **/
        lineChart.setVisibleXRangeMaximum((float) dataPointCount);
        lineChart.setVisibleXRangeMinimum(10f);

        //lineChart.animateX(1000); // Animation that shows the values from left to right

        lineDataSet2.setDrawValues(true); //Default is true
        lineDataSet2.setDrawCircles(false); //Default is true
        lineChart.setMaxVisibleValueCount(20);

        lineChart.setDrawBorders(true); //Border around the Graph
        lineChart.setBorderColor( Color.BLACK);
        lineChart.setBorderWidth(2f);
        lineChart.setNoDataText("Sorry, there is no Data in this time slot");

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


}