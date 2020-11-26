/*
 * Created by jerry for Synap INC on 26/11/20 18:05
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 26/11/20 15:38
 */

package com.synap.iothub;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.synap.preferences.SynapSharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class AimHubMessage {

    public static final int METHOD_THROWS = 403;
    //IOT server codes
    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_DEFINED = 404;
    private final Handler handler = new Handler();

    protected Context context;

    IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    private Message sendMessage;
    private String lastException;
    private int receiptsConfirmedCount = 0;
    private int sendFailuresCount = 0;
    private int msgReceivedCount = 0;
    private int sendMessagesInterval = 5000;
    private DeviceClient client;
    private int msgSentCount = 0;
    private Thread sendThread;

    SynapSharedPreferences synapSharedPreferences;

    public AimHubMessage(Context _context) throws IOException, URISyntaxException {
        this.context = _context;
        initClient();
        synapSharedPreferences = new SynapSharedPreferences(context);

    }

    public void stop() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    sendThread.interrupt();
                    client.closeNow();
                    System.out.println("Shutting down...");
                } catch (Exception e) {
                    lastException = "Exception while closing IoTHub connection: " + e;
                }
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("DefaultLocale")
    public void sendMessages(String msgStr) {
        // you can send any data to the server as long as it is in json format
        // the connection string will determine access to the server
        // TODO finalise data structure to send

        try {

            sendMessage = new Message(msgStr);
            sendMessage.setMessageId(java.util.UUID.randomUUID().toString());

            //data.writeToFile("Passed ," + msgStr);

            EventCallback eventCallback = new EventCallback();

            client.sendEventAsync(sendMessage, eventCallback, msgSentCount);
            msgSentCount++;

        } catch (Exception e) {
            System.err.println("Exception while sending event: " + e);
            //data.writeToFile("Failed ," + msgStr);
        }
    }

    /**
     * Makes connection with com.synap.IOTHub
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    private void initClient() throws URISyntaxException, IOException {
        String connString = "HostName=demo-synapinc-iothub.azure-devices.net;DeviceId=synap-aim-g1;SharedAccessKey=NTA6ODA6NEE6QTY6NDA6Rjc=";
        client = new DeviceClient(connString, protocol);

        try {
            client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());
            client.open();
            MessageCallback callback = new MessageCallback();
            client.setMessageCallback(callback, null);
            client.subscribeToDeviceMethod(new SampleDeviceMethodCallback(),
                    null,
                    new DeviceMethodStatusCallBack(), null);
        } catch (Exception e) {
            System.err.println("Exception while opening IoTHub connection: " + e.getMessage());
            client.closeNow();
            System.out.println("Shutting down...");
        }
    }

    private int method_setSendMessagesInterval(Object methodData) throws UnsupportedEncodingException, JSONException {
        String payload = new String((byte[]) methodData, "UTF-8").replace("\"", "");
        JSONObject obj = new JSONObject(payload);
        sendMessagesInterval = obj.getInt("sendInterval");
        return METHOD_SUCCESS;
    }

    private int method_default(Object data) {
        System.out.println("invoking default method for this device");
        // Insert device specific code here
        return METHOD_NOT_DEFINED;
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null) {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED) {
                //connection was lost, and is not being re-established. Look at provided exception for
                // how to resolve this issue. Cannot send messages until this issue is resolved, and you manually
                // re-open the device client
            } else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING) {
                //connection was lost, but is being re-established. Can still send messages, but they won't
                // be sent until the connection is re-established
            } else if (status == IotHubConnectionStatus.CONNECTED) {
                //Connection was successfully re-established. Can send messages.
            }
        }
    }

    class EventCallback implements IotHubEventCallback {
        @SuppressLint("SetTextI18n")
        public void execute(IotHubStatusCode status, Object context) {
            Integer i = context instanceof Integer ? (Integer) context : 0;
            System.out.println("IOT Response is : " + status.name());

            if ((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY)) {
                receiptsConfirmedCount++; //TODO write these to files
                synapSharedPreferences.incrementEventNumber();

                System.out.println("Passed " + receiptsConfirmedCount);
            } else {
                sendFailuresCount++;
                System.out.println("Failed :" + sendFailuresCount);
            }
        }
    }

    /**
     * Message Received
     */
    class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback {
        @SuppressLint("SetTextI18n")
        public IotHubMessageResult execute(Message msg, Object context) {
            System.out.println("Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            msgReceivedCount++;
            System.out.println(msgReceivedCount);
            System.out.println("Extra information : " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            return IotHubMessageResult.COMPLETE;
        }
    }

    protected static class DeviceMethodStatusCallBack implements IotHubEventCallback {
        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected class SampleDeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context) {
            DeviceMethodData deviceMethodData;
            try {
                switch (methodName) {
                    case "setSendMessagesInterval": {
                        int status = method_setSendMessagesInterval(methodData);
                        deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                        break;
                    }
                    default: {
                        int status = method_default(methodData);
                        deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                    }
                }
            } catch (Exception e) {
                int status = METHOD_THROWS;
                deviceMethodData = new DeviceMethodData(status, "Method Throws " + methodName);
            }
            return deviceMethodData;
        }
    }

}
