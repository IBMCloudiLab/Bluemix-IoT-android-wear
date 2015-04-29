package com.ibm.iot.android.iotstarter.events;

import com.ibm.iot.android.iotstarter.data.Sensor;
import com.ibm.iot.android.iotstarter.data.SensorDataPoint;

public class SensorUpdatedEvent {
    private Sensor sensor;
    private SensorDataPoint sensorDataPoint;

    public SensorUpdatedEvent(Sensor sensor, SensorDataPoint sensorDataPoint) {
        this.sensor = sensor;
        this.sensorDataPoint = sensorDataPoint;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public SensorDataPoint getDataPoint() {
        return sensorDataPoint;
    }
}
