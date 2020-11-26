/*
 * Created by jerry for Synap INC on 26/11/20 11:30
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 26/11/20 11:30
 */

package com.synap.testiothub;

import java.util.List;

public class EventRoot {

    String serial; //for IOTHub temporary
    int eventSequence;
    AimData aimData = new AimData();

    List<Event> events;


}
