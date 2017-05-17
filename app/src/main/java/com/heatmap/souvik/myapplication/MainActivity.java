package com.heatmap.souvik.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.maxmind.geoip2.exception.GeoIp2Exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private EditText urlText;
    private Button downloadButton;
    private Button useDefaultButton;
    private TextView textView;


    private static  String DEFAULT_FILEPATH= Environment.getExternalStorageDirectory()+"/geoapp/datafiles";
    private static  String DEFAULT_SAVEFOLDER=Environment.getExternalStorageDirectory()+"/geoapp/datafiles";
    private static final String DEFAULT_URL= "https://s3-us-west-2.amazonaws.com/greedygamemedia/test/test_ip_ts.txt";
    private static final String TAG="tag";

    private HashMap<Integer, ArrayList<String>> ipMap = null;
    private HashMap<Integer, ArrayList<LatLng>> dataSet = null;
    private RequestQueue queue = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlText = (EditText)findViewById(R.id.urlText);
        downloadButton = (Button)findViewById(R.id.downloadButton);
        useDefaultButton = (Button)findViewById(R.id.defaultButton);
        queue = Volley.newRequestQueue(this);
        textView = (TextView)findViewById(R.id.progressText);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUrl = urlText.getText().toString();
                textView.setText("Downloading File");
                new DownLoadFileAsync(newUrl).execute();
            }
        });
        useDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("Downloading File");
                new DownLoadFileAsync(DEFAULT_URL).execute();
            }
        });
        isInternetAvailable();
        isStoragePermissionGranted();
    }

    private void readFile(String filePath){
        textView.setText("Download complete!! Mapping IP to time.");
       new ReadFileAsync(filePath).execute();

    }

    private void decodeIp(){
        textView.setText("Mapping complete!! Decoding IP");
        new DecodeIpAsync().execute();
    }

    private void launchActivity(){
        Intent intent = new Intent(this, MapsActivity2.class);
        startActivity(intent);
    }
    private class DownLoadFileAsync extends AsyncTask<Void, Void, Boolean> {
        private String url;
        private String fileName;

        public DownLoadFileAsync(String url) {

            this.url = url;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                fileName = DownloadFile.downloadFile(url, DEFAULT_SAVEFOLDER);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(fileName==null)
                return false;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                readFile(DEFAULT_SAVEFOLDER+'/'+fileName);
            }

        }
    }
    private class ReadFileAsync extends AsyncTask<Void, Void, Boolean>{

        private String filePath="";
        public ReadFileAsync(String filepath){
            this.filePath = filepath;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ipMap = FileReaderApplication.readFile(this.filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(ipMap!=null)
                return true;
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                decodeIp();
            }
        }
    }

    private class DecodeIpAsync extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                dataSet = new Ipdecoder().decoder(ipMap, 0, MainApplication.getMaxTime(), queue);
                //System.out.println(dataSet.get(0).get(1).latitude);
                MainApplication.setDataMap(dataSet);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeoIp2Exception e) {
                e.printStackTrace();
            }
            if(dataSet!=null)
                return true;
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                launchActivity();
            }
        }
    }
    private void isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
             if(!ipAddr.equals("")){
                 Toast.makeText(this,"Connected to internet", Toast.LENGTH_SHORT);
             }
             else {
                 Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT);
             }

        } catch (Exception e) {

        }

    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}
