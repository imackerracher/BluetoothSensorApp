package com.example.groupfourtwo.bluetoothsensorapp.Data;

/**
 * Wrapper for information about the user whose application received the data.
 *
 * @author Stefan Erk
 * @version 1.0
 */

public class User {

    private long id;
    private String name;

    public User(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
