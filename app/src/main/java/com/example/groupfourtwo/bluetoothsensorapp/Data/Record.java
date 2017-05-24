package com.example.groupfourtwo.bluetoothsensorapp.Data;

import java.util.Objects;

/**
 * Wrapper for information about a continuous set of measurements forming a record.
 *
 * @author Stefan Erk
 * @version 1.2
 */

public class Record {

    /**
     * The unique identifier of the recording session.
     */
    private long id;

    /**
     * The MAC address of the sensor that generated the measurement.
     */
    private final Sensor sensor;

    /**
     * The id of the user who received the measurement.
     */
    private final User user;

    /**
     * The instant in time when the record began.
     */
    private final long begin;

    /**
     * The moment in time when the record ended. -1 if record is still running.
     */
    private long end;


    /**
     * Constructs a new record. Either by firstly connecting or loading from database.
     *
     * @param id       the unique id of the record
     * @param sensor   the associated sensor
     * @param user     the associated user
     * @param begin    when the record started
     * @param end      when the record ended, -1 if running
     */
    Record(long id, Sensor sensor, User user, long begin, long end) {

        Objects.requireNonNull(sensor, "Sensor must not be null.");
        Objects.requireNonNull(user, "User must not be null.");

        if (id <= 0)
            throw new IllegalArgumentException("Id must be positive.");
        if (begin <= 0)
            throw new IllegalArgumentException("Begin cannot be negative");
        if (end > 0 && end < begin)
            throw new IllegalArgumentException("End cannot lay in past of begin.");

        this.id = id;
        this.sensor = sensor;
        this.user = user;
        this.begin = begin;
        this.end = end;
    }


    /**
     * Start a new record at this very moment.
     * Note: As the record is not saved in the database, no valid id nor end point is assigned yet.
     *
     * @param sensor  the associated sensor
     * @param user    the associated user
     * @return  a new running record
     */
    public static Record startRecord(Sensor sensor, User user) {
        return new Record(-1, sensor, user, System.currentTimeMillis(), -1);
    }


    /**
     * Return the record's unique id.
     *
     * @return  the record's id
     */
    public long getId() {
        return id;
    }


    /**
     * Return the sensor the record was generated with.
     *
     * @return  the record's sensor
     */
    public Sensor getSensor() {
        return sensor;
    }


    /**
     * Return the user the record was generated by.
     *
     * @return  the record's user
     */
    public User getUser() {
        return user;
    }


    /**
     * Return the instant of time when the record was started.
     *
     * @return  the begin point
     */
    public long getBegin() {
        return begin;
    }


    /**
     * Return the instant of time when the record was finished.
     *
     * @return  the end point
     */
    public long getEnd() {
        return end;
    }


    /**
     * Tells whether this is a record still running.
     *
     * @return  whether the record is running
     */
    public boolean isRunning() {
        return end <= 0;
    }


    /**
     * Set the recording's id after being inserted into the database.
     */
    void setId(long id) {
        if (this.id > 0)
            throw new IllegalStateException("Id cannot be set after being assigned by database.");

        this.id = id;
    }


    /**
     * Finish the record. May only be called when the record is just running.
     */
    public void stop() {
        if (end > 0)
            throw new IllegalStateException("Cannot stop a record that was already finished.");

        end = System.currentTimeMillis();
    }
}
