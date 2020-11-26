/*
 * Created by jerry for Synap INC on 26/11/20 18:05
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 26/11/20 16:24
 */

package com.synap.iothub;

import java.util.List;

public class EventRoot {

    public String serial; //TODO remove this for IOTHub temporary
    public int eventSequence;
    public AimData aimData = new AimData();
    public List<Event> events;


}
