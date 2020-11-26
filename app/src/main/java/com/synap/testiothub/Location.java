/*
 * Created by jerry for Synap INC on 25/11/20 14:46
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 25/11/20 14:46
 */

package com.synap.testiothub;

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