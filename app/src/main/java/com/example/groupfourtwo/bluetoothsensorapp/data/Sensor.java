package com.example.groupfourtwo.bluetoothsensorapp.data;

import java.util.Objects;

/**
 * Wrapper for information about the sensor chip generating a measurement.
 *
 * @author Stefan Erk
 * @version 1.2
 */

public class Sensor {

    public static final Sensor SENSOR_DUMMY = new Sensor(0, "Dummy Sensor", 0);

    /**
     * The unique identifier according to the sensor's bluetooth MAC address.
     */
    private final long id;

    /**
     * A describing name of the sensor.
     */
    private String name;

    /**
     * The moment in time when this exact sensor was first connected to.
     */
    private final long knownSince;


    /**
     * Derive a device's bluetooth address as a number format instead from a string.
     *
     * @param s  the address of the device as a hexadecimal string.
     * @return  the parsed address as a long
     */
    public static long parseAddress(String s) {
        return Long.parseLong(s.replaceAll(":", ""), 16);
    }


    /**
     * Constructs a new sensor object. Either by firstly connecting or loading from database.
     *
     * @param id          unique MAC address
     * @param name        the sensor's name
     * @param knownSince  instant of time of first connection
     */
    public Sensor(long id, String name, long knownSince) {

        Objects.requireNonNull(name, "Name must not be null.");
        if (name.trim().length() == 0 || name.length() > 50) {
            throw new IllegalArgumentException("Name must be between 1 and 50 characters long.");
        }

        if (id < 0) {
            throw new IllegalArgumentException("Id must not be negative.");
        }

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
     * Return the instant in time when the sensor was first connected to.
     *
     * @return  the instant in time of first connection
     */
    public long getKnownSince() {
        return knownSince;
    }


    /**
     * Modify the name of the sensor.
     * The name must neither be null nor empty nor longer than 50 characters.
     *
     * @param name  the new name
     */
    void setName(String name) {
        Objects.requireNonNull(name, "Name must not be null.");

        if (name.trim().length() == 0 || name.length() > 50) {
            throw new IllegalArgumentException("Name must be between 1 and 50 characters long.");
        }

        this.name = name;
    }
}
