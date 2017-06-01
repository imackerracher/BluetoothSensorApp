package com.example.groupfourtwo.bluetoothsensorapp.Graph;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

/**
 * Created by kim on 23.05.17.
 */



public class DrawGraph {


    Context c;
    public DrawGraph(Context context) {
        c = context;
    }

    private int datapointCount = 10801;

    public void draw(Activity activity) {

        LineChart lineChart;



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

        xAxis.setValueFormatter(new MyXAxisValueFormatter(lineChart));





        int numDataPoints = datapointCount;
        int gapSize = datapointCount/10;
        int gapPosition = datapointCount/3;



        /* deklaration of the Value-Lists */
   //     ArrayList<String> xAxes = new ArrayList<>();
        Float[] yAxes = new Float[numDataPoints]; //static



        /*1 testdataset*/


        /*y = sin(x) data with gaps*/
        for (int i = 0; i < gapPosition; i++)
            yAxes[i] = (float) i;
            //yAxes2[i] = (float) Math.sin((float) i / 300);

        for (int i = gapPosition + gapSize; i < numDataPoints; i++)
            yAxes[i] = (float) i;
            //yAxes2[i] = (float) Math.sin((float) i / 300);




        /* split the graph in sections */
        ArrayList<Entry> yAxes2_1 = new ArrayList<>();
        ArrayList<Entry> yAxes2_2 = new ArrayList<>();

        for (int i = 0; i < gapPosition; i++) {
            yAxes2_1.add(new Entry(i, yAxes[i]));

        }


        for (int i = gapPosition + gapSize; i < numDataPoints; i++) {
            yAxes2_2.add(new Entry(i, yAxes[i]));
        }

        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();


        LineDataSet lineDataSet2 = new LineDataSet(yAxes2_1, null);
        lineDataSet2.setColor(Color.RED);


        LineDataSet lineDataSet3 = new LineDataSet(yAxes2_2, null);
        lineDataSet3.setColor(Color.RED);


        lineDataSets.add(lineDataSet2);
        lineDataSets.add(lineDataSet3);

        lineChart.setData(new LineData(lineDataSets));





        //## Restraining what's visible
        lineChart.setVisibleXRangeMaximum((float) datapointCount); // allow 10800 values to be displayed at once on the x-axis, not more
        lineChart.setVisibleXRangeMinimum(10f);

        // lineChart.animateX(3000); // Animation that shows the values from left to right


        lineChart.setBackgroundColor(Color.WHITE); //BackgroundColour

    }
}
