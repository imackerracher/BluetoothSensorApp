package com.example.groupfourtwo.bluetoothsensorapp.Data;

/**
 * Wrapper for information about the user whose application received the data.
 *
 * @author Stefan Erk
 * @version 1.0
 */

public class User {

    /**
     * The unique id of the user.
     */
    private long id;

    /**
     * The describing name of the user.
     */
    private String name;


    /**
     * Creates a new user object.
     *
     * @param id    the user's id
     * @param name  the user's name
     */
    public User(long id, String name) {
        this.id = id;
        this.name = name;
    }


    /**
     * Return the user's id.
     *
     * @return  the user's id
     */
    public long getId() {
        return id;
    }


    /**
     * Return the user's name.
     *
     * @return  the user's name
     */
    public String getName() {
        return name;
    }
}
