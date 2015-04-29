/*******************************************************************************
 * Copyright (c) 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Mike Robertson - initial contribution
 *******************************************************************************/
package com.ibm.iot.android.iotstarter.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ibm.iot.android.iotstarter.IoTStarterApplication;
import com.ibm.iot.android.iotstarter.R;
import com.ibm.iot.android.iotstarter.RemoteSensorManager;
import com.ibm.iot.android.iotstarter.activities.MainActivity;
import com.ibm.iot.android.iotstarter.data.Sensor;
import com.ibm.iot.android.iotstarter.data.SensorDataPoint;
import com.ibm.iot.android.iotstarter.events.BusProvider;
import com.ibm.iot.android.iotstarter.events.SensorRangeEvent;
import com.ibm.iot.android.iotstarter.events.SensorUpdatedEvent;
import com.ibm.iot.android.iotstarter.utils.Constants;
import com.ibm.iot.android.iotstarter.utils.MessageFactory;
import com.ibm.iot.android.iotstarter.utils.MqttHandler;
import com.ibm.iot.android.iotstarter.utils.TopicFactory;
import com.squareup.otto.Subscribe;

import java.util.LinkedList;

/**
 * The IoT Fragment is the main fragment of the application that will be displayed while the device is connected
 * to IoT. From this fragment, users can send text event messages. Users can also see the number
 * of messages the device has published and received while connected.
 */
public class IoTFragment extends IoTStarterFragment {
    private final static String TAG = IoTFragment.class.getName();
    private long sensorId;
    private Sensor sensor;

    /**************************************************************************
     * Fragment functions for establishing the fragment
     **************************************************************************/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.iot, container, false);
    }

    /**
     * Called when the fragment is resumed.
     */
    @Override
    public void onResume() {
        Log.d(TAG, ".onResume() entered");

        super.onResume();
        app = (IoTStarterApplication) getActivity().getApplication();
        app.setCurrentRunningActivity(TAG);

        if (broadcastReceiver == null) {
            Log.d(TAG, ".onResume() - Registering iotBroadcastReceiver");
            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, ".onReceive() - Received intent for iotBroadcastReceiver");
                    processIntent(intent);
                }
            };
        }

        getActivity().getApplicationContext().registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.APP_ID + Constants.INTENT_IOT));


        sensorId = 1;
        sensor = RemoteSensorManager.getInstance(getActivity()).getSensor(sensorId);

        initialiseSensorData();

        // initialise
        initializeIoTActivity();
    }

    /**
     * Called when the fragment is destroyed.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, ".onDestroy() entered");

        try {
            getActivity().getApplicationContext().unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException iae) {
            // Do nothing
        }
        super.onDestroy();
    }

    /**
     * Initializing onscreen elements and shared properties
     */
    private void initializeIoTActivity() {
        Log.d(TAG, ".initializeIoTFragment() entered");

        context = getActivity().getApplicationContext();

        updateViewStrings();

        // setup button listeners
        Button button = (Button) getActivity().findViewById(R.id.sendText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendText();
            }
        });
    }

    /**
     * Update strings in the fragment based on IoTStarterApplication values.
     */
    @Override
    protected void updateViewStrings() {
        Log.d(TAG, ".updateViewStrings() entered");
        // DeviceId should never be null at this point.
        if (app.getDeviceId() != null) {
            ((TextView) getActivity().findViewById(R.id.deviceIDIoT)).setText(app.getDeviceId());
        } else {
            ((TextView) getActivity().findViewById(R.id.deviceIDIoT)).setText("-");
        }

        // Update publish count view.
        processPublishIntent();

        // Update receive count view.
        processReceiveIntent();

        int unreadCount = app.getUnreadCount();
        ((MainActivity) getActivity()).updateBadge(getActivity().getActionBar().getTabAt(2), unreadCount);
    }

    /**************************************************************************
     * Functions to handle button presses
     **************************************************************************/

    /**
     * Handle pressing of the send text button. Prompt the user to enter text
     * to send.
     */
    private void handleSendText() {
        Log.d(TAG, ".handleSendText() entered");
        if (app.getConnectionType() != Constants.ConnectionType.QUICKSTART) {
            final EditText input = new EditText(context);
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.send_text_title))
                    .setMessage(getResources().getString(R.string.send_text_text))
                    .setView(input)
                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editable value = input.getText();
                            String messageData = MessageFactory.getTextMessage(value.toString());
                            MqttHandler mqtt = MqttHandler.getInstance(context);
                            mqtt.publish(TopicFactory.getEventTopic(Constants.TEXT_EVENT), messageData, false, 0);
                        }
                    }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.send_text_title))
                    .setMessage(getResources().getString(R.string.send_text_invalid))
                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
        }
    }

    /**************************************************************************
     * Functions to process intent broadcasts from other classes
     **************************************************************************/

    /**
     * Process the incoming intent broadcast.
     * @param intent The intent which was received by the fragment.
     */
    private void processIntent(Intent intent) {
        Log.d(TAG, ".processIntent() entered");

        // No matter the intent, update log button based on app.unreadCount.
        updateViewStrings();

        String data = intent.getStringExtra(Constants.INTENT_DATA);
        assert data != null;
        if (data.equals(Constants.INTENT_DATA_PUBLISHED)) {
            processPublishIntent();
        } else if (data.equals(Constants.INTENT_DATA_RECEIVED)) {
            processReceiveIntent();
        } else if (data.equals(Constants.ACCEL_EVENT)) {
            processAccelEvent();
        } else if (data.equals(Constants.COLOR_EVENT)) {
            Log.d(TAG, "Updating background color");
            getView().setBackgroundColor(app.getColor());
        } else if (data.equals(Constants.ALERT_EVENT)) {
            String message = intent.getStringExtra(Constants.INTENT_DATA_MESSAGE);
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.alert_dialog_title))
                    .setMessage(message)
                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
        }
    }

    /**
     * Intent data contained INTENT_DATA_PUBLISH
     * Update the published messages view based on app.getPublishCount()
     */
    private void processPublishIntent() {
        Log.v(TAG, ".processPublishIntent() entered");
        String publishedString = this.getString(R.string.messages_published);
        publishedString = publishedString.replace("0",Integer.toString(app.getPublishCount()));
        ((TextView) getActivity().findViewById(R.id.messagesPublishedView)).setText(publishedString);
    }

    /**
     * Intent data contained INTENT_DATA_RECEIVE
     * Update the received messages view based on app.getReceiveCount();
     */
    private void processReceiveIntent() {
        Log.v(TAG, ".processReceiveIntent() entered");
        String receivedString = this.getString(R.string.messages_received);
        receivedString = receivedString.replace("0",Integer.toString(app.getReceiveCount()));
        ((TextView) getActivity().findViewById(R.id.messagesReceivedView)).setText(receivedString);
    }

    /**
     * Update acceleration view strings
     */
    private void processAccelEvent() {
        Log.v(TAG, ".processAccelEvent()");
        float[] accelData = app.getAccelData();
        ((TextView) getActivity().findViewById(R.id.accelX)).setText("x: " + accelData[0]);
        ((TextView) getActivity().findViewById(R.id.accelY)).setText("y: " + accelData[1]);
        ((TextView) getActivity().findViewById(R.id.accelZ)).setText("z: " + accelData[2]);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
    }

    private void initialiseSensorData() {
        LinkedList<SensorDataPoint> dataPoints = sensor.getDataPoints();

        if (dataPoints == null || dataPoints.isEmpty()) {
            Log.w("sensor data", "no data found for sensor " + sensor.getId() + " " + sensor.getName());
            return;
        }


        LinkedList<Float>[] normalisedValues = new LinkedList[dataPoints.getFirst().getValues().length];
        LinkedList<Integer>[] accuracyValues = new LinkedList[dataPoints.getFirst().getValues().length];

        for (int i = 0; i < normalisedValues.length; ++i) {
            normalisedValues[i] = new LinkedList<Float>();
            accuracyValues[i] = new LinkedList<Integer>();
        }


        for (SensorDataPoint dataPoint : dataPoints) {

            for (int i = 0; i < dataPoint.getValues().length; ++i) {
                //changeText("V="+ dataPoint.getValues()[i] ,i);
                //float normalised = (dataPoint.getValues()[i] - sensor.getMinValue()) / spread;
                //normalisedValues[i].add(normalised);
                //accuracyValues[i].add(dataPoint.getAccuracy());
            }
        }


        //this.sensorview.setNormalisedDataPoints(normalisedValues, accuracyValues);
        //this.sensorview.setZeroLine((0 - sensor.getMinValue()) / spread);

        //this.sensorview.setMaxValueLabel(MessageFormat.format("{0,number,#}", sensor.getMaxValue()));
        //this.sensorview.setMinValueLabel(MessageFormat.format("{0,number,#}", sensor.getMinValue()));

    }

    @Subscribe
    public void onSensorUpdatedEvent(SensorUpdatedEvent event) {
        Log.d(TAG, "onSensorUpdatedEvent in fragment: sensor ID" + event.getSensor().getId());
        if (event.getSensor().getId() == this.sensor.getId()) {

            float[] accelData = event.getDataPoint().getValues();

            ((TextView) getActivity().findViewById(R.id.accelXWatch)).setText("Wx: " + accelData[0]);
            ((TextView) getActivity().findViewById(R.id.accelYWatch)).setText("Wy: " + accelData[1]);
            ((TextView) getActivity().findViewById(R.id.accelZWatch)).setText("Wz: " + accelData[2]);

            if(app != null){
                app.setAccelDataWatch(accelData);
            }

            /*
            for (int i = 0; i < event.getDataPoint().getValues().length; ++i) {
                //textvalue1.setText("V=");
                changeText("W="+ event.getDataPoint().getValues()[i] ,i);
                //textvalue1.setText("V=" + event.getDataPoint().getValues()[i]);
                //float normalised = (event.getDataPoint().getValues()[i] - sensor.getMinValue()) / spread;
                //this.sensorview.addNewDataPoint(normalised, event.getDataPoint().getAccuracy(), i);
            }
            */
        }
    }

    @Subscribe
    public void onSensorRangeEvent(SensorRangeEvent event) {
        if (event.getSensor().getId() == this.sensor.getId()) {
            initialiseSensorData();
        }
    }

    private void changeText(String text, int i){
        switch (i){
            case 0:
                Log.d(TAG, "CHANGING TEXT FOR i = " + i + "; " + text);
                //textvalue1.setText(text + "; I= " + i);
                break;
            case 1:
                Log.d(TAG, "CHANGING TEXT FOR i = " + i + "; " + text);
                //textvalue2.setText(text + "; I= " + i);
                break;
            case 2:
                Log.d(TAG, "CHANGING TEXT FOR i = " + i + "; " + text);
                //textvalue3.setText(text + "; I= " + i);
                break;
            default:
                break;
        }

    }
}
