/*
 * Created by jerry for Synap INC on 26/11/20 18:05
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 26/11/20 13:10
 */

package com.synap.iothub;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("type")
    @Expose
    private String type = "Point";
    @SerializedName("coordinates")
    @Expose
    private List<Double> coordinates = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

}