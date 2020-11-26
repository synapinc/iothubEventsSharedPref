/*
 * Created by jerry for Synap INC on 26/11/20 18:05
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 25/11/20 14:47
 */

package com.synap.iothub;

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
