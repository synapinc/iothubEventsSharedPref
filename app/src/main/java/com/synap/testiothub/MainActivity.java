/*
 * Created by jerry for Synap INC on 25/11/20 14:19
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 25/11/20 14:19
 */

package com.synap.testiothub;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.synap.preferences.SynapSharedPreferences;
import com.synap.preferences.e_PreferencesKeys;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Synap Logger >>> : ";
    Button sendAimButton;

    AimHubMessage aimHubMessage;

    TextView txtSerial;
    TextView txtMAC;
    TextView txtLocation;
    TextView txtDateTime;
    TextView txtEventType;
    TextView txtEventNumber;

    SwitchCompat swVehicle;
    SwitchCompat swTamper;
    SwitchCompat swDistraction;
    SwitchCompat swFatigue;
    SwitchCompat swDiagnostic;

    List<Double> points = new ArrayList<Double>();
    Location location = new Location();
    GeoLocation geoLocation = new GeoLocation();

    EventRoot eventRoot = new EventRoot();
    AimData aimData = new AimData();

    Event fatigueEvent = new Event();
    Event distractionEvent = new Event();
    Event tamperEvent = new Event();
    Event diagnosticEvent = new Event();
    Event vehicleEvent = new Event();

    List<Event> eventsList = new ArrayList<>();

    List<e_EventReasons> fatigueReasonList = new ArrayList<>();
    List<e_EventReasons> vehicleReasonList = new ArrayList<>();
    List<e_EventReasons> distractionReasonList = new ArrayList<>();
    List<e_EventReasons> tamperReasonList = new ArrayList<>();
    List<e_EventReasons> diagnosticReasonList = new ArrayList<>();

    SynapSharedPreferences synapSharedPreferences;
    int currentEventNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtDateTime = findViewById(R.id.txtDateTime);
        txtSerial = findViewById(R.id.txtSerial);
        txtEventType = findViewById(R.id.txtEventType);
        txtLocation = findViewById(R.id.txtLocation);
        txtMAC = findViewById(R.id.txtMAC);
        txtEventNumber = findViewById(R.id.txtEventNumber);

        synapSharedPreferences = new SynapSharedPreferences(this);
        currentEventNumber = (Integer) synapSharedPreferences.getIntSetting(e_PreferencesKeys.LastEventNumber);
        txtEventNumber.setText(String.valueOf(currentEventNumber));

        sendAimButton = findViewById(R.id.sendAim);

        //vehicle switch
        swVehicle = findViewById(R.id.switch1);
        boolean sve = synapSharedPreferences.getBooleanSetting(e_PreferencesKeys.SendVehicleEvent);
        swVehicle.setChecked(sve);

        swVehicle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (swVehicle.isChecked()) {
                    synapSharedPreferences.changeBooleanSetting(e_PreferencesKeys.SendVehicleEvent, true);

                }
                if (!swVehicle.isChecked()) {
                    synapSharedPreferences.changeBooleanSetting(e_PreferencesKeys.SendVehicleEvent, false);

                }
            }
        });

        //end of UI initialization

        points.add(29.0);
        points.add(30.0);

        location.setCoordinates(points);
        geoLocation.setLocation(location);

        sendAimButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    createAFreshEvent();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                if (synapSharedPreferences.getBooleanSetting(e_PreferencesKeys.SendVehicleEvent)) {
                    swVehicle.setChecked(synapSharedPreferences.getBooleanSetting(e_PreferencesKeys.SendVehicleEvent));
                    eventsList.add(vehicleEvent);
                }

                eventsList.add(tamperEvent);
                eventsList.add(distractionEvent);
                eventsList.add(diagnosticEvent);
                eventsList.add(fatigueEvent);

                eventRoot.events = eventsList;

                currentEventNumber = synapSharedPreferences.getIntSetting(e_PreferencesKeys.LastEventNumber);
                eventRoot.eventSequence = currentEventNumber;
                String msgStr = serialize(eventRoot);
                aimHubMessage.sendMessages(msgStr);
                txtEventNumber.setText(String.valueOf(synapSharedPreferences.getIntSetting(e_PreferencesKeys.LastEventNumber)));
            }
        });
    }

    public String serialize(Object data) {

        Gson gson = new Gson();
        String msgStr = gson.toJson(data);
        return msgStr;

    }

    private static String getCurrentUtcTime() {
        //TimeZone tz = TimeZone.getTimeZone("UTC");
        // TODO see if we can store timezone in settings and set from there
        // maybe get timezone from network
        TimeZone tz = TimeZone.getTimeZone("Africa/Johannesburg");
        // Quoted "Z" to indicate UTC, no timezone offset
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        return nowAsISO;
    }

    //https://stackoverflow.com/questions/11705906/programmatically-getting-the-mac-of-an-android-device
    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }


    private void createAFreshEvent() throws IOException, URISyntaxException {

        aimHubMessage = new AimHubMessage(this);

        eventRoot = new EventRoot();
        aimData = new AimData();

        fatigueEvent = new Event();
        distractionEvent = new Event();
        tamperEvent = new Event();
        diagnosticEvent = new Event();
        vehicleEvent = new Event();

        eventsList = new ArrayList<>();

        fatigueReasonList = new ArrayList<>();
        vehicleReasonList = new ArrayList<>();
        distractionReasonList = new ArrayList<>();
        tamperReasonList = new ArrayList<>();
        diagnosticReasonList = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        eventRoot.serial = Build.getSerial();

        //set the aim data
        eventRoot.aimData.localDateTime = getCurrentUtcTime();
        eventRoot.aimData.macAddress = getMacAddress();
        eventRoot.aimData.location = geoLocation;

        fatigueReasonList.add(e_EventReasons.SLEEP_DETECTED);
        fatigueEvent.e_deviceEventDataType = e_DeviceEventDataType.Fatigue;
        fatigueEvent.eventReasons = fatigueReasonList;

        diagnosticReasonList.add(e_EventReasons.HEARTBEAT);
        diagnosticEvent.e_deviceEventDataType = e_DeviceEventDataType.Diagnostic;
        diagnosticEvent.eventReasons = diagnosticReasonList;

        tamperReasonList.add(e_EventReasons.CAMERA_OBSTRUCTED);
        tamperEvent.e_deviceEventDataType = e_DeviceEventDataType.Tamper;
        tamperEvent.eventReasons = tamperReasonList;

        distractionReasonList.add(e_EventReasons.DISTRACTION_BY_PHONE);
        distractionEvent.e_deviceEventDataType = e_DeviceEventDataType.Distraction;
        distractionEvent.eventReasons = distractionReasonList;

        vehicleReasonList.add(e_EventReasons.OVER_SPEED_LIMIT);
        vehicleEvent.e_deviceEventDataType = e_DeviceEventDataType.Vehicle;
        vehicleEvent.eventReasons = vehicleReasonList;

    }
}