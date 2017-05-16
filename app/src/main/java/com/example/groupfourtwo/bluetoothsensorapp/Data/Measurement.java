package com.example.groupfourtwo.bluetoothsensorapp.Data;

import java.util.Date;

/**
 * Wrapper for the tuple of different values and their corresponding metadata.
 *
 * @author Stefan Erk
 * @version 1.0
 */

public class Measurement {
    private long id;
    private long sensor;
    private long user;
    private Date time;
    private float brightness;
    private float distance;
    private float humidity;
    private float pressure;
    private float temperature;

    public Measurement(long id, long sensor, long user, Date time, float brightness,
                       float distance, float humidity, float pressure, float temperature) {
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

    public long getId() {
        return id;
    }

    public long getSensor() {
        return sensor;
    }

    public long getUser() {
        return user;
    }

    public Date getTime() {
        return time;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getDistance() {
        return distance;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public float getTemperature() {
        return temperature;
    }
}
