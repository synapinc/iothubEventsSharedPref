/*
 * Created by jerry for Synap INC on 26/11/20 18:05
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 26/11/20 17:49
 */

package com.synap.iothub;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Event {

    @SerializedName("eventType")
    public e_DeviceEventDataType e_deviceEventDataType;
    @SerializedName("reasons")
    public List<e_EventReasons> eventReasons;

}
