package com.example.groupfourtwo.bluetoothsensorapp.Data;

import java.util.Date;
import java.util.Objects;

/**
 * Wrapper for information about the sensor chip generating a measurement.
 *
 * @author Stefan Erk
 * @version 1.0
 */

public class Sensor {

    /**
     * The unique identifier according to the sensor's bluetooth MAC address.
     */
    private long id;

    /**
     * A describing name of the sensor.
     */
    private String name;

    /**
     * The moment in time when this exact sensor was first connected to.
     */
    private Date knownSince;


    /**
     * Constructs a new sensor object. Either by firstly connecting or loading from database.
     *
     * @param id          unique MAC address
     * @param name        the sensor's name
     * @param knownSince  datetime of first connection
     */
    public Sensor(long id, String name, Date knownSince) {

        Objects.requireNonNull(name, "Name must not be null.");
        Objects.requireNonNull(knownSince, "KnownSince must not be null.");
        if (name.length() == 0 || name.length() > 50)
            throw new IllegalArgumentException("Name must be between 1 and 50 characters long.");

        this.id = id;
        this.name = name;
        this.knownSince = knownSince;
    }


    /**
     * Return the sensor's unique MAC address.
     *
     * @return  the sensor's id
     */
    public long getId() {
        return id;
    }


    /**
     * Return the sensors given name.
     *
     * @return  the sensor's name
     */
    public String getName() {
        return name;
    }


    /**
     * Return the datetime when the sensor was first connected to.
     *
     * @return  the datetime of first connection
     */
    public Date getKnownSince() {
        return knownSince;
    }


    /**
     * Modify the name of the sensor.
     * The name must neither be null nor empty nor longer than 50 characters.
     *
     * @param name  the new name
     */
    void setName(String name) {
        Objects.requireNonNull(name, "Parameter 'name' must not be null.");

        if (name.length() == 0 || name.length() > 50)
            throw new IllegalArgumentException("Name must be between 1 and 50 characters long.");

        this.name = name;
    }
}
