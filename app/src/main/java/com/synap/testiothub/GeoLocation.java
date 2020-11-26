/*
 * Created by jerry for Synap INC on 25/11/20 14:19
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 25/11/20 09:25
 */

package com.synap.testiothub;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GeoLocation {

    @SerializedName("location")
    @Expose
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
