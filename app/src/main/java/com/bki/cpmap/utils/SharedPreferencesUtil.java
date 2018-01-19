package com.bki.cpmap.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SharedPreferencesUtil {

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static List<Object> getListObject(String key, Class<?> mClass, Context context) {
        List<String> objStrings = getListString(key, context);
        List<Object> objects = new ArrayList<>();

        for (String jObjString : objStrings) {
            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
            Object value = gson.fromJson(jObjString, mClass);
            objects.add(value);
        }
        return objects;
    }

    public static void putListObject(String key, List<Object> objArray, Context context) {
        checkForNullKey(key);
        List<String> objStrings = new ArrayList<>();
        for (Object obj : objArray) {
            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
            objStrings.add(gson.toJson(obj));
        }
        putListString(key, objStrings, context);
    }


    /**
     * Get String value from SharedPreferences at 'key'. If key not found, return ""
     *
     * @param key SharedPreferences key
     * @return String value at 'key' or "" (empty String) if key not found
     */
    public static String getString(String key, Context context) {
        return getSharedPreferences(context).getString(key, "");
    }

    /**
     * Get parsed ArrayList of String from SharedPreferences at 'key'
     *
     * @param key SharedPreferences key
     * @return ArrayList of String
     */
    public static ArrayList<String> getListString(String key, Context context) {
        return new ArrayList<>(Arrays.asList(TextUtils.split(getSharedPreferences(context).getString(key, ""), "‚‗‚")));
    }


    /**
     * Put String value into SharedPreferences with 'key' and save
     *
     * @param key   SharedPreferences key
     * @param value String value to be added
     */
    public static void putString(String key, String value, Context context) {
        checkForNullKey(key);
        checkForNullValue(value);
        getSharedPreferences(context).edit().putString(key, value).apply();
    }

    /**
     * Put ArrayList of String into SharedPreferences with 'key' and save
     *
     * @param key        SharedPreferences key
     * @param stringList ArrayList of String to be added
     */
    private static void putListString(String key, List<String> stringList, Context context) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        getSharedPreferences(context).edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     *
     * @param key the pref key
     */
    private static void checkForNullKey(String key) {
        if ( key == null ) {
            throw new NullPointerException();
        }
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     *
     * @param value the pref value
     */
    private static void checkForNullValue(String value) {
        if ( value == null ) {
            throw new NullPointerException();
        }
    }
}
