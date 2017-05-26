package com.example.groupfourtwo.bluetoothsensorapp.Data;

import java.util.Objects;

/**
 * Wrapper for the tuple of different values and their corresponding metadata.
 *
 * @author Stefan Erk
 * @version 1.2
 */

public class Measurement {

    /**
     * The unique id of the measurement. -1 for objects that are not contained within the database.
     */
    private long id;

    /**
     * The record during which the measurement was recorded.
     */
    private final Record record;

    /**
     * The instant in time when the measurement was taken.
     */
    private final long time;

    /**
     * The measured value of brightness.
     */
    private final float brightness;

    /**
     * The distance to the sensor at the moment of measuring.
     */
    private final float distance;

    /**
     * The measured value of humidity.
     */
    private final float humidity;

    /**
     * The measured value of pressure.
     */
    private final float pressure;

    /**
     * The measured value of temperature.
     */
    private final float temperature;


    /**
     * Creates a new measurement. Only to be used for recreation from database context.
     *
     * @param id           the measurement's unique id
     * @param record       the record the measurement was taken at
     * @param time         the time when the measurement was taken
     * @param brightness   the measured brightness
     * @param distance     the distance to the sensor
     * @param humidity     the measured humidity
     * @param pressure     the measured pressure
     * @param temperature  the measured temperature
     */
    Measurement(long id, Record record, long time, float brightness, float distance,
                float humidity, float pressure, float temperature) {

        Objects.requireNonNull(record, "Record must not be null.");

        this.id = id;
        this.record = record;
        this.time = time;
        this.brightness = brightness;
        this.distance = distance;
        this.humidity = humidity;
        this.pressure = pressure;
        this.temperature = temperature;
    }


    /**
     * Creates a completely new measurement object just received.
     * Note: As the measurement is not saved in the database yet, id is set to -1.
     *
     * @param record       the record the measurement was taken at
     * @param time         the time when the measurement was taken
     * @param brightness   the measured brightness
     * @param distance     the distance to the sensor
     * @param humidity     the measured humidity
     * @param pressure     the measured pressure
     * @param temperature  the measured temperature
     */
    public static Measurement newMeasurement(Record record, long time, float brightness, float distance,
                                      float humidity, float pressure, float temperature) {

        Objects.requireNonNull(record, "Record must not be null.");

        return new Measurement(-1, record, time, brightness, distance, humidity, pressure, temperature);
    }


    /**
     * Returns the id of the measurement.
     *
     * @return  the measurement's id
     */
    public long getId() {
        return id;
    }


    /**
     * Returns the corresponding record.
     *
     * @return  the corresponding record
     */
    public Record getRecord() {
        return record;
    }


    /**
     * The time when the measurement was taken.
     *
     * @return  the measurement's time
     */
    public long getTime() {
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


    /**
     * Set the measurements id after being inserted into the database.
     *
     * @param id  the measurement's actual id
     */
    void setId(long id) {
        if (this.id > 0)
            throw new IllegalStateException("Id cannot be set after being assigned by database");

        this.id = id;
    }
}
