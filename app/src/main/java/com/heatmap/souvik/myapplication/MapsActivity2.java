package com.heatmap.souvik.myapplication;

import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener, View.OnClickListener {



    private SeekBar seekBar;
    private FloatingActionButton fab;
    private GoogleMap map;

    private HashMap<Integer, ArrayList<LatLng>> dataSet;
    private int maxTime=0;
    private int timer = 0;
    private boolean pollingThreadRunning=false;

    private static final int ALT_HEATMAP_RADIUS = 10;
    private static final double ALT_HEATMAP_OPACITY = 0.4;
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS={
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };
    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    private HeatmapTileProvider provider;
    private TileOverlay overlay;

    private static final int POLLING_INTERVAL = 1000;
    private boolean playingState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        this.fab = (FloatingActionButton)findViewById(R.id.fabPlay);
        this.fab.setOnClickListener(this);
        this.seekBar = (SeekBar)findViewById(R.id.mapSeekBar);
        seekBar.setOnSeekBarChangeListener(this);
        dataSet = MainApplication.getDataMap();
        maxTime = MainApplication.getMaxTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMap();
        initializeSeekBar();
    }

    private void setupMap(){
        ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(map == null){
            map = googleMap;
        }
    }

    private void initializeSeekBar(){
        // System.out.println("maxtime "+maxTime);
        seekBar.setMax(maxTime);
        seekBar.setProgress(0);
        seekBar.setClickable(true);
    }

    private void updateSeekBar(int progress){
        System.out.println(progress);
        seekBar.setProgress(progress);
    }
    private void startPollingThread(){
        new Thread(){
            public void run(){
                while(pollingThreadRunning && timer < maxTime){
                    final ArrayList<LatLng> data = dataSet.get(timer);
//                    Log.d("data", dataSet);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateHeatmap(data);
                            updateSeekBar(timer);
                        }
                    });
                    try{
                        timer++;
                        if(timer>=maxTime){
                            pollingThreadRunning = false;
                            playingState = false;
                            timer = 0;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fab.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                                    updateSeekBar(timer);
                                }
                            });
                        }
                        Thread.sleep(POLLING_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }

    private void updateHeatmap( ArrayList<LatLng> coordinates){
        //System.out.println(coordinates.get(0).latitude);
        if(provider == null && map!=null){
            provider = new HeatmapTileProvider.Builder().data(coordinates).build();
            overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            provider.setGradient(ALT_HEATMAP_GRADIENT);
        }
        else{
            provider.setData(coordinates);
            overlay.clearTileCache();
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            timer = progress;
            //updateSeekBar(timer);
            ArrayList<LatLng> data = dataSet.get(timer);
            updateHeatmap(data);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        if(!playingState){
            fab.setImageResource(R.drawable.ic_pause_white_48dp);
            playingState= !playingState;
            pollingThreadRunning=true;
            startPollingThread();
        }
        else {
            fab.setImageResource(R.drawable.ic_play_arrow_white_48dp);
            playingState= !playingState;
            pollingThreadRunning = false;
        }
    }

}
