package ru.scorpio92.filemanager.Main.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by scorpio92 on 08.03.16.
 */
public class SettingsUtils {
    //получение настроек SharedPreferences
    public static Boolean getBooleanSettings(Context c, String pref_name) {
        try {
            SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(c);
            if (mSettings.contains(pref_name)) {
                return mSettings.getBoolean(pref_name, false);
            }
        } catch (Exception e) {
            Log.e("getBooleanSettings", null, e);
        }
        return null;
    }

    public static String getStringSettings(Context c, String pref_name) {
        try {
            SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(c);
            if (mSettings.contains(pref_name)) {
                return mSettings.getString(pref_name, null);
            }
        } catch (Exception e) {
            Log.e("getStringSettings", null, e);
        }
        return null;
    }

    public static Integer getIntSettings(Context c, String pref_name) {
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(c);
        if (mSettings.contains(pref_name)) {
            return mSettings.getInt(pref_name, 0);
        }
        return 0;
    }

    //получаем из строки массив ArrayList
    public static ArrayList<String> getArrayFromString(String s) {
        try {
            if(s!=null) {
                if (!s.trim().equals("")) {
                    String[] stringMassive = s.split(",");
                    ArrayList<String> array = new ArrayList<String>();
                    Collections.addAll(array, stringMassive);
                    return array;
                }
            }
        } catch (Exception e) {
            Log.e("getArrayFromString", null, e);
        }
        return null;
    }
}
