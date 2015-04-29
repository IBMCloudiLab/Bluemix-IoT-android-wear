package com.ibm.iot.android.iotstarter.events;

import com.ibm.iot.android.iotstarter.data.Sensor;

public class SensorRangeEvent {
    private Sensor sensor;

    public SensorRangeEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
