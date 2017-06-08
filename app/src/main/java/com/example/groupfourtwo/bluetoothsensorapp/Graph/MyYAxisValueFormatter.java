package com.example.groupfourtwo.bluetoothsensorapp.Graph;

import com.example.groupfourtwo.bluetoothsensorapp.Data.Measure;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

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
        switch (measure1) {
            case TEMPERATURE: return mFormat.format(value) + " °C";

            case HUMIDITY: return mFormat.format(value) + " %";

            case BRIGHTNESS: return mFormat.format(value) + " lm";

            case PRESSURE: return mFormat.format(value) + " hPa";

            case DISTANCE: return mFormat.format(value) + " m";

            default: return "missing measure";
        }

    }

}