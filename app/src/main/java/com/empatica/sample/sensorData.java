package com.empatica.sample;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class sensorData {
    public float data;
    public Double timeStamp;

    public sensorData() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public sensorData(float data,Double timeStamp) {
        this.data = data;
        this.timeStamp = timeStamp;
    }
}
