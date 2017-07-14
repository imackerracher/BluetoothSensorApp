package com.example.groupfourtwo.bluetoothsensorapp.graph;


import android.content.Context;
import android.widget.TextView;


import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;

import static android.graphics.Color.BLACK;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.BRIGHTNESS;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.DISTANCE;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.HUMIDITY;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.PRESSURE;
import static com.example.groupfourtwo.bluetoothsensorapp.data.Measure.TEMPERATURE;


/**
 * Created by kim on 23.06.17.
 */

public class CustomMarkerView extends MarkerView {
    private TextView tvContent;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        // find your layout components
        tvContent = (TextView) findViewById( R.id.tvContent );
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        DecimalFormat mFormat = new DecimalFormat("###,###,##0.00");
        tvContent.setText( "" + mFormat.format(e.getY()));

        // this will perform necessary layouting
        super.refreshContent(e, highlight);
    }

    private MPPointF mOffset;

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }




}
