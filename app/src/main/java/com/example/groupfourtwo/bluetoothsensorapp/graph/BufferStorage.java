package com.example.groupfourtwo.bluetoothsensorapp.graph;

import com.example.groupfourtwo.bluetoothsensorapp.data.Measure;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

/**
 * Created by kim on 03.07.17.
 */

public class BufferStorage {

    // Eine (versteckte) Klassenvariable vom Typ der eigenen Klasse
    private static BufferStorage instance;

    private ArrayList<Entry> yAxes1Buffer;
    private ArrayList<Entry> yAxes2Buffer;
    private float begin1Buffer;
    private float end1Buffer;
    private float begin2Buffer;
    private float end2Buffer;
    private Measure measure1;
    private Measure measure2;


    // Verhindere die Erzeugung des Objektes über andere Methoden
    private BufferStorage() { }
    // Eine Zugriffsmethode auf Klassenebene, welches dir '''einmal''' ein konkretes
    // Objekt erzeugt und dieses zurückliefert.
    public static BufferStorage getInstance () {
        if (BufferStorage.instance == null) {
            BufferStorage.instance = new BufferStorage ( );
        }
        return BufferStorage.instance;
    }


    public ArrayList<Entry> getyAxes1Buffer() {
        return yAxes1Buffer;
    }

    public void setyAxes1Buffer(ArrayList<Entry> yAxes1Buffer) {
        this.yAxes1Buffer = yAxes1Buffer;
    }

    public ArrayList<Entry> getyAxes2Buffer() {
        return yAxes2Buffer;
    }

    public void setyAxes2Buffer(ArrayList<Entry> yAxes2Buffer) {
        this.yAxes2Buffer = yAxes2Buffer;
    }

    public float getBegin2Buffer() {
        return begin2Buffer;
    }

    public void setBegin2Buffer(float begin2Buffer) {
        this.begin2Buffer = begin2Buffer;
    }

    public float getEnd1Buffer() {
        return end1Buffer;
    }

    public void setEnd1Buffer(float end1Buffer) {
        this.end1Buffer = end1Buffer;
    }

    public float getBegin1Buffer() {
        return begin1Buffer;
    }

    public void setBegin1Buffer(float begin1Buffer) {
        this.begin1Buffer = begin1Buffer;
    }

    public float getEnd2Buffer() {
        return end2Buffer;
    }

    public void setEnd2Buffer(float end2Buffer) {
        this.end2Buffer = end2Buffer;
    }


    public Measure getMeasure1() {
        return measure1;
    }

    public Measure getMeasure2() {
        return measure2;
    }

    public void setMeasure1(Measure measure1) {
        this.measure1 = measure1;
    }

    public void setMeasure2(Measure measure2) {
        this.measure2 = measure2;
    }

}
