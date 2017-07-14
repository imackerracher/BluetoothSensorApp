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

    /**
     * Offset in milliseconds from epoch to 1. Jan 2016 UTC
     */
    private final static long EPOCH_TO_2016 = 1451606400000L;

    private Context context;
    private Measure measure1, measure2;
    private long begin;
    private long end;
    private Record record;
    private int backgroundColour = Color.BLACK;
    private int textColour = Color.WHITE;
    private static int transparent = Color.argb(0, 0, 0, 0);
    private LineChart lineChart;

    private boolean isBuffered1 = false;
    private boolean isBuffered2 = false;

    public DrawGraph(Context context, Measure measure1, Measure measure2,
                     Record record, long begin, long end) {
        this.context = context;
        this.measure1 = measure1;
        this.measure2 = measure2;
        if (record != null) {
            this.record = record;
            this.begin = record.getBegin();
            this.end = record.getEnd();
        } else {
            this.begin = begin;
            this.end = end;
        }
    }


    public void draw(Activity activity) {

        Interval interval = fromLength(end - begin);
        long length = (end - begin);
        // maximal number of dataPoints that can be displayed
        int dataPointCount = (int) (length / ((long) (interval.step))) + 1;







        /*reference in visualization.xml*/
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
        x.setStartInSec(begin - EPOCH_TO_2016); // The start in milliseconds since 1st Jan 2016 in UTC
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
        leftAxis.setTextColor(measure1.color);

        rightAxis.setEnabled(false);
        if(measure2 != null) {
            rightAxis.setEnabled(true);
            rightAxis.setValueFormatter(y2);
            rightAxis.setDrawGridLines(false); // no grid lines
            rightAxis.setTextColor(measure2.color);
            //rightAxis.setAxisMinimum(0f);
            //rightAxis.setAxisMaximum(20f);
        }


        IMarker mv = new CustomMarkerView(context, R.layout.marker_view);

        lineChart.setMarker(mv);


        /**
         * Access to Database and Buffer
         */
        DataManager dataManager = DataManager.getInstance(context);
        BufferStorage buf = BufferStorage.getInstance();

        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Entry> yAxes1;
        ArrayList<Entry> yAxes2;


        if (isBuffered1) {
            yAxes1 = buf.getyAxes1Buffer();
        } else {
            if (record == null) {
                yAxes1 = dataManager.getValuesFromInterval(measure1, begin, end);
                buf.setyAxes1Buffer(yAxes1);
            } else {
                yAxes1 = dataManager.getValuesFromRecord(measure1, record);
                buf.setyAxes1Buffer(yAxes1);
            }
        }


        if (measure2 != null) {
            if (isBuffered2) {
                yAxes2 = buf.getyAxes2Buffer();
            } else {
                if (record == null) {
                    yAxes2 = dataManager.getValuesFromInterval(measure2, begin, end);
                    buf.setyAxes2Buffer(yAxes2);
                } else {
                    yAxes2 = dataManager.getValuesFromRecord(measure2, record);
                    buf.setyAxes2Buffer(yAxes2);
                }
            }
        } else {
            yAxes2 = null;
        }




        dataManager.close();

        if(yAxes1 == null  ||  yAxes1.isEmpty()) {
            return;
        }






        /**
         * Generate the lineDataSets for Visualisation
         * */
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();


        LineDataSet lineDataSet1 = new LineDataSet(yAxes1, "1");
        lineDataSet1.setColors(createColorArray(yAxes1, measure1));
        lineDataSet1.setHighLightColor(brighter(measure1.color, 0.3f));
        lineDataSets.add(lineDataSet1);

        LineDataSet lineDataSet2 = new LineDataSet(yAxes2,"2");
        if (measure2 != null) {
            lineDataSet2.setColors(createColorArray(yAxes2, measure2));
            lineDataSet2.setAxisDependency(RIGHT);
            lineDataSet2.setHighLightColor(brighter(measure2.color, 0.3f));
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
        lineDataSet1.setValueTextColor(textColour);
        lineDataSet1.setDrawCircles(false); //Default is true
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setValueTextColor(textColour);
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
                colorArray[i] = transparent;
                //colorArray[i] = backgroundColour;

            i++;
        }

        return colorArray;
    }

    public void refresh() {
        if (record != null) {
            end = record.getEnd();
        }
        isBuffered1 = false;
        isBuffered2 = false;
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public void setMeasure2(Measure measure) {
        if (measure != measure1) {
            measure2 = measure;
        } else {
            measure2 = null;
        }
        isBuffered2 = false;
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
        refresh();
    }

    public void setTimeSpan(long begin, long end) {
        if (end <= begin) {
            throw new IllegalArgumentException("Begin must lay in the past of end.");
        }
        if (end - begin > MAX.length) {
            throw new IllegalArgumentException("Time span too long.");
        }
        this.end = end;
        this.begin = begin;
        record = null;
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

        refresh();
    }

    public int brighter(int color, float factor) {
        float hsv[] = new float[3];
        Color.RGBToHSV( Color.red(color),Color.green(color), Color.blue(color)  , hsv);
        hsv[1] *= factor;
        hsv[1] = Math.min(1.0f, hsv[1]);
        int rgb = Color.HSVToColor(hsv);
        //rgb = (255 & 0xff) << 24 | (Color.red(rgb) & 0xff) << 16 | ((Color.green(rgb) & 0xff) << 16 | ((Color.blue(rgb))) & 0xff);
        System.out.println("kimsDebugging:" + Color.alpha(rgb) +"red: " + Color.red(rgb) +" green: " + Color.green(rgb) +" blue: " + Color.blue(rgb));
        return rgb;
    }




}
