package com.ibm.iot.android.iotstarter.events;

import com.ibm.iot.android.iotstarter.data.Sensor;

public class NewSensorEvent {
    private Sensor sensor;

    public NewSensorEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
