package com.example.groupfourtwo.bluetoothsensorapp.Data;

import java.util.Date;

/**
 * Wrapper for information about the sensor chip generating a measurement.
 *
 * @author Stefan Erk
 * @version 1.0
 */

public class Sensor {
    private long id;
    private String name;
    private Date knownSince;

    public Sensor(long id, String name, Date knownSince) {
        this.id = id;
        this.name = name;
        this.knownSince = knownSince;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getKnownSince() {
        return knownSince;
    }

    public void setName(String name) {
        if (name.length() == 0 || name.length() > 50)
            throw new IllegalArgumentException("Name must be between 1 and 50 characters long.");

        this.name = name;
    }
}
