package com.empatica.sample;

import com.google.firebase.database.IgnoreExtraProperties;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@IgnoreExtraProperties
public class user {
    public String message;
    public Date timeStamp;
    public boolean isDone;

    public user() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public user(String message, Date timeStamp) {
        this.message = message;
        this.timeStamp = timeStamp;
        this.isDone = false;
    }
}
