package com.example.groupfourtwo.bluetoothsensorapp.Data;

import java.util.Date;
import java.util.Objects;

/**
 * Wrapper for the tuple of different values and their corresponding metadata.
 *
 * @author Stefan Erk
 * @version 1.0
 */

public class Measurement {

    /**
     * The unique id of the measurement.
     */
    private long id;

    /**
     * The MAC address of the sensor that generated the measurement.
     */
    private Sensor sensor;

    /**
     * The id of the user who received the measurement.
     */
    private User user;

    /**
     * The date and time when the measurement was taken.
     */
    private Date time;

    /**
     * The measured value of brightness.
     */
    private float brightness;

    /**
     * The distance to the sensor at the moment of measuring.
     */
    private float distance;

    /**
     * The measured value of humidity.
     */
    private float humidity;

    /**
     * The measured value of pressure.
     */
    private float pressure;

    /**
     * The measured value of temperature.
     */
    private float temperature;


    /**
     * Creates a completely new measurement object just received.
     * Note: Field id is set by the database, therefore set to 0 here.
     *
     * @param sensor       the corresponding sensor
     * @param user         the corresponding user
     * @param time         the time when the measurement was taken
     * @param brightness   the measured brightness
     * @param distance     the distance to the sensor
     * @param humidity     the measured humidity
     * @param pressure     the measured pressure
     * @param temperature  the measured temperature
     */
    public Measurement(Sensor sensor, User user, Date time, float brightness,
                float distance, float humidity, float pressure, float temperature) {

        Objects.requireNonNull(sensor, "Sensor must not be null.");
        Objects.requireNonNull(user, "User must not be null.");
        Objects.requireNonNull(time, "Time must not be null.");

        this.sensor = sensor;
        this.user = user;
        this.time = time;
        this.brightness = brightness;
        this.distance = distance;
        this.humidity = humidity;
        this.pressure = pressure;
        this.temperature = temperature;
    }


    /**
     * Recreates a new measurement object with information from the database.
     *
     * @param id           the measurement's unique id
     * @param sensor       the corresponding sensor
     * @param user         the corresponding user
     * @param time         the time when the measurement was taken
     * @param brightness   the measured brightness
     * @param distance     the distance to the sensor
     * @param humidity     the measured humidity
     * @param pressure     the measured pressure
     * @param temperature  the measured temperature
     */
    Measurement(long id, Sensor sensor, User user, Date time, float brightness,
                       float distance, float humidity, float pressure, float temperature) {

        Objects.requireNonNull(sensor, "Sensor must not be null.");
        Objects.requireNonNull(user, "User must not be null.");
        Objects.requireNonNull(time, "Time must not be null.");

        this.id = id;
        this.sensor = sensor;
        this.user = user;
        this.time = time;
        this.brightness = brightness;
        this.distance = distance;
        this.humidity = humidity;
        this.pressure = pressure;
        this.temperature = temperature;
    }


    /**
     * Returns the id of the measurement
     *
     * @return  the measurement's id
     */
    public long getId() {
        return id;
    }


    /**
     * Returns the corresponding sensor.
     *
     * @return  the corresponding sensor
     */
    public Sensor getSensor() {
        return sensor;
    }


    /**
     * Returns the corresponding user.
     *
     * @return  the corresponding user
     */
    public User getUser() {
        return user;
    }


    /**
     * The time when the measurement was taken.
     *
     * @return  the measurement's time
     */
    public Date getTime() {
        return time;
    }


    /**
     * Returns the value of brightness.
     *
     * @return  the value of brightness
     */
    public float getBrightness() {
        return brightness;
    }


    /**
     * Returns the distance to the sensor.
     *
     * @return  the distance of the sensor
     */
    public float getDistance() {
        return distance;
    }


    /**
     * Returns the value of humidity.
     *
     * @return  the value of brightness
     */
    public float getHumidity() {
        return humidity;
    }


    /**
     * Returns the value of pressure.
     *
     * @return  the value of pressure
     */
    public float getPressure() {
        return pressure;
    }


    /**
     * Returns the value of temperature.
     *
     * @return  the value of temperature
     */
    public float getTemperature() {
        return temperature;
    }
}
