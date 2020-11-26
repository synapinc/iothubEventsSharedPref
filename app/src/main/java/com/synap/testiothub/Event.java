/*
 * Created by jerry for Synap INC on 26/11/20 12:28
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 26/11/20 12:28
 */

package com.synap.testiothub;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Event {

    @SerializedName("eventType")
    e_DeviceEventDataType e_deviceEventDataType;
    @SerializedName("reasons")
    List<e_EventReasons> eventReasons;

}
