/*
 * Created by jerry for Synap INC on 25/11/20 17:34
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 25/11/20 17:34
 */

package com.synap.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 *  to store and retrieve settings
 */
public class SynapSharedPreferences {

    private Context mContext;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final String mPreferences = "MyPrefs" ;
    int lastEventNumber = 0;

    public SynapSharedPreferences(Context context) {
        this.mContext = context;
        sharedPreferences = mContext.getSharedPreferences(mPreferences, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Update last event number
     * @return
     */
    public int incrementEventNumber() {
        lastEventNumber = getIntSetting(e_PreferencesKeys.LastEventNumber);
        lastEventNumber += 1;
        changIntSetting(e_PreferencesKeys.LastEventNumber,lastEventNumber);
        return getIntSetting(e_PreferencesKeys.LastEventNumber);
    }

    /**
     * To retrieve a boolean value
     * @param key
     * @param value
     */
    public void changeBooleanSetting(e_PreferencesKeys key, Boolean value){
        editor.putBoolean(String.valueOf(key),value);
        editor.apply();
    }
    /**
     * To set a boolean value
     * @param key
     */
    public boolean getBooleanSetting (e_PreferencesKeys key) {

        boolean value = sharedPreferences.getBoolean(String.valueOf(key),false);
        return value;

    }

    /**
     * Set a string value
     * @param key
     * @param value
     */
    public void changeStringSetting(e_PreferencesKeys key, String value){
        editor.putString(String.valueOf(key),value);
        editor.apply();
    }

    /**
     * Retrieve a string value
     * @param key
     * @return
     */
    public String getStringSetting (e_PreferencesKeys key) {

        String value = sharedPreferences.getString(String.valueOf(key),"");
        return value;
    }

    /**
     * Set an integer value
     * @param key
     * @param value
     */
    public void changIntSetting(e_PreferencesKeys key, int value){
        editor.putInt(String.valueOf(key),value);
        editor.apply();
    }

    /**
     * Retrieve an integer value
     * @param key
     * @return
     */
    public int getIntSetting (e_PreferencesKeys key) {

        int value = sharedPreferences.getInt(String.valueOf(key),-1);
        return value;

    }

}
