package com.example.groupfourtwo.bluetoothsensorapp.graph;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

/**
 * Caches the data that is used to draw the graph, to reduce database operations.
 *
 * @author Kim
 */

class BufferStorage {

    // Eine (versteckte) Klassenvariable vom Typ der eigenen Klasse
    private static BufferStorage instance;

    private ArrayList<Entry> yAxes1Buffer;
    private ArrayList<Entry> yAxes2Buffer;


    // Verhindere die Erzeugung des Objektes über andere Methoden
    private BufferStorage() { }
    // Eine Zugriffsmethode auf Klassenebene, welches dir '''einmal''' ein konkretes
    // Objekt erzeugt und dieses zurückliefert.
    public static synchronized BufferStorage getInstance() {
        if (instance == null) {
            instance = new BufferStorage();
        }
        return instance;
    }

    ArrayList<Entry> getyAxes1Buffer() {
        return yAxes1Buffer;
    }

    void setyAxes1Buffer(ArrayList<Entry> yAxes1Buffer) {
        this.yAxes1Buffer = yAxes1Buffer;
    }

    ArrayList<Entry> getyAxes2Buffer() {
        return yAxes2Buffer;
    }

    void setyAxes2Buffer(ArrayList<Entry> yAxes2Buffer) {
        this.yAxes2Buffer = yAxes2Buffer;
    }

}
