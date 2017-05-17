package com.heatmap.souvik.myapplication;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by souvik on 13/5/17.
 */

public class MainApplication extends Application {

    private  static HashMap<Integer, ArrayList<LatLng>> mapData;
    private static int minTime;
    private static int maxTime;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static void setDataMap(HashMap<Integer, ArrayList<LatLng>> map){
        mapData = map;
    }
    public static HashMap<Integer, ArrayList<LatLng>> getDataMap(){
        return mapData;
    }
    public static void setMinTime(int time){
        minTime = time;
    }
    public static void setMaxTime(int time){
        maxTime = time;
    }
    public static int getMinTime(){
        return minTime;
    }
    public static int getMaxTime(){
        return maxTime;
    }

    public static Context getContext(){
        return context;
    }

}
