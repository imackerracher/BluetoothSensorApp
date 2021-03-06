package com.example.groupfourtwo.bluetoothsensorapp.graph;

import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by kim on 07.06.17.
 */

public class MyYAxisValueFormatter implements IAxisValueFormatter {

    private DecimalFormat mFormat;
    private Measure measure1;

    public MyYAxisValueFormatter(Measure measure) {
        measure1 = measure;
        // format values to 1 decimal digit
        mFormat = new DecimalFormat("###,###,##0.0");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)

        return mFormat.format(value) + measure1.unit;

    }

}