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


public class DrawGraph {

    private static final String LOG_TAG = DrawGraph.class.getSimpleName();

    /**
     * Offset in milliseconds from epoch to 1. Jan 2016 UTC
     */
    private static final long EPOCH_TO_2016 = 1451606400000L;
    private static final int TRANSPARENT = Color.argb(0, 0, 0, 0);

    private static final boolean CHECK_MEASURE_1 = true;
    private static final boolean CHECK_MEASURE_2 = false;

    private Context context;
    private Measure measure1, measure2;
    private long begin;
    private long end;
    private Record record;
    private int backgroundColour;
    private int textColour;
    private LineChart lineChart;

    private DataManager dataManager;
    private BufferStorage buf;

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
        dataManager = DataManager.getInstance(context);
        buf = BufferStorage.getInstance();
    }


    public boolean draw(Activity activity) {

        Interval interval = fromLength(end - begin);
        long length = (end - begin);
        // maximal number of dataPoints that can be displayed
        int dataPointCount = (int) (length / ((long) (interval.step))) + 1;







        /*reference in visualization.xml*/
        lineChart = (LineChart) activity.findViewById(R.id.lineChart);


        Legend l = lineChart.getLegend();
        l.setEnabled(false); //The Legend is DISABLED

        lineChart.setDescription(null);

        setDay(false);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(0f); //smallest value that is displayed on the XAxis
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //Choose between TOP, BOTTOM, BOTH_SIDED, TOP_INSIDE or BOTTOM_INSIDE
        xAxis.setTextSize(10f);
        xAxis.setTextColor(textColour);


        /*
         * Generate the Values on the X-Axis
         */
        MyXAxisValueFormatter x = new MyXAxisValueFormatter(lineChart);

        x.setPointsPerHour(3600/(interval.step/1000)); // Maximal number of data points per Hour
        x.setStartInSec(begin - EPOCH_TO_2016); // The start in milliseconds since 1st Jan 2016 in UTC
        xAxis.setValueFormatter(x);

        /*
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
        }





        /*
         * Access to Database and Buffer
         */
        try {
            dataManager.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Entry> yAxes1;
        ArrayList<Entry> yAxes2;


        Log.d(LOG_TAG, "" + isBuffered(CHECK_MEASURE_1));
        if (isBuffered(CHECK_MEASURE_1)) {
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
            Log.d(LOG_TAG, "" + isBuffered(CHECK_MEASURE_2));
            if (isBuffered(CHECK_MEASURE_2)) {
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

        buf.measure1 = measure1;
        buf.measure2 = measure2;
        buf.record = record;
        buf.begin = begin;
        buf.end = end;


        if(yAxes1 == null  ||  yAxes1.isEmpty()) {
            lineChart.setData(null);
            return false;
        }






        /*
         * Generate the lineDataSets for Visualisation
         */
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




        lineChart.setData(new LineData(lineDataSets));




        /*
         * Restraining what's visible
         */
        lineChart.setVisibleXRangeMaximum((float) dataPointCount);
        lineChart.setVisibleXRangeMinimum(10f);

        lineChart.animateX(1000); // Animation that shows the values from left to right

        lineDataSet1.setDrawValues(true); //Default is true
        lineDataSet1.setValueTextColor(textColour);
        lineDataSet1.setDrawCircles(false); //Default is true
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setValueTextColor(textColour);
        lineChart.setMaxVisibleValueCount(20);

        lineChart.setDrawBorders(true); //Border around the Graph
        lineChart.setBorderColor( textColour);
        lineChart.setBorderWidth(1f);

        lineChart.setKeepScreenOn(true);
        lineChart.setKeepPositionOnRotation(true);

        lineChart.setBackgroundColor(backgroundColour); //BackgroundColour of the whole background

        return true;
    }


    private int[] createColorArray(ArrayList<Entry> YAxis , Measure measure) {
        int size = YAxis.size();
        int[] colorArray = new int[size];
        int i = 0;


        while (i < size-1) {
            if(YAxis.get(i).getX() - YAxis.get(i+1).getX() == -1)
                colorArray[i] = measure.color;
            else
                colorArray[i] = TRANSPARENT;

            i++;
        }

        return colorArray;
    }

    public void refresh() {
        if (record != null) {
            end = record.getEnd();
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        }
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
        if (end - begin > MAX.length) {
            throw new IllegalArgumentException("Time span too long.");
        }
        this.end = end;
        this.begin = begin;
        record = null;
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }


    private int brighter(int color, float factor) {
        float hsv[] = new float[3];
        Color.RGBToHSV( Color.red(color),Color.green(color), Color.blue(color)  , hsv);
        hsv[1] *= factor;
        hsv[1] = Math.min(1.0f, hsv[1]);
        return Color.HSVToColor(hsv);
    }

    private void setDay(Boolean day) {

        IMarker mvd = new CustomMarkerView(context, R.layout.marker_view_day);
        IMarker mvn = new CustomMarkerView(context, R.layout.marker_view_night);


        if (day) {
            backgroundColour = Color.WHITE;
            textColour = Color.BLACK;
            lineChart.setMarker(mvd);
        } else {
            backgroundColour = Color.BLACK;
            textColour = Color.WHITE;
            lineChart.setMarker(mvn);
        }
    }

    private boolean isBuffered(boolean whichMeasure) {
        if (whichMeasure && buf.measure1 != measure1) {
            return false;
        } else if (!whichMeasure && buf.measure2 != measure2) {
            return false;
        }

        if (record != null  &&  (buf.record != record  ||  record.isRunning())) {
            return false;
        } else if (record == null  &&  (buf.begin != begin  ||  buf.end != end)) {
            return false;
        }

        return true;
    }


}
