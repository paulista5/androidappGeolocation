package com.heatmap.souvik.myapplication;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.maps.model.LatLng;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Created by souvik on 12/5/17.
 */

public class Ipdecoder {

    private static final String MYURL = "http://lowcost-env.dfxysx2z3v.ap-south-1.elasticbeanstalk.com/getCordinates";
    private  double lat ;
    private  double longt ;

    private  void setCordinates(double latitude, double longitude){
        this.lat = latitude;
        this.longt = longitude;

    }
    public JSONObject convertToJson(ArrayList<String> map)  {
            JSONArray arrayip = new JSONArray();
            for(int j=0;map!=null&&j<map.size();j++){
                String ip = map.get(j);
                JSONObject jsonIp = new JSONObject();
                try {
                    jsonIp.put("ip", ip);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayip.put(jsonIp);
            }
            JSONObject timeIpmap = new JSONObject();
            try {
                timeIpmap.put("ipList", arrayip);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        System.out.println(timeIpmap.toString());
        return timeIpmap;
    }

    public JSONObject postRequest(JSONObject obj){

        String urlParameter = MYURL;
        URL url=null;
        HttpURLConnection urlConn=null;
        DataOutputStream printout;
        DataInputStream input;
        try {
            url = new URL(urlParameter);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoInput (true);
            urlConn.setDoOutput (true);
            urlConn.setUseCaches (false);
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-Type","application/json");
            //urlConn.setRequestProperty("Accept", "application/json");

            //urlConn.setRequestProperty("Host", "android.schoolportal.gr");
            urlConn.connect();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try{
            printout = new DataOutputStream(urlConn.getOutputStream ());
            printout.write(obj.toString().getBytes("UTF-8"));
            printout.flush ();
            printout.close ();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        int responseCode=-1;
        try {
            responseCode = urlConn.getResponseCode();
            System.out.println(responseCode);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("\nSending 'POST' request to URL : " + url);
        //System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in=null;
        String inputLine;
        StringBuffer response = new StringBuffer();
        try {
            in = new BufferedReader(
                    new InputStreamReader(urlConn.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //print result
        //System.out.println(response.toString());

        try {
            return new JSONObject(response.toString());
        } catch (JSONException e) {
            return null;
        }

    }



    public  HashMap<Integer, ArrayList<LatLng>> decoder(HashMap<Integer, ArrayList<String>> map, int start, int last, RequestQueue queue) throws IOException, GeoIp2Exception {

        HashMap<Integer, ArrayList<LatLng>> finalMap = new HashMap<>();
            for(int i = start; i<map.size(); i++){
                JSONObject obj = convertToJson(map.get(i));
                JSONObject finalObj = postRequest(obj);
                JSONArray array = null;
                try {
                    array = finalObj.getJSONArray("list");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for(int j = 0;array!=null&&j<array.length();j++) {
                    JSONObject cordinates = null;
                    try {
                        cordinates = array.getJSONObject(j);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(cordinates!=null){
                        LatLng tempCord = null;
                        try {
                            tempCord = new LatLng(cordinates.getDouble("latitude"), cordinates.getDouble("longitude"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(finalMap.get(i) == null && tempCord!=null){
                            ArrayList<LatLng> temp = new ArrayList<>();
                            temp.add(tempCord);
                            finalMap.put(i, temp);
                        }
                        else{
                            ArrayList<LatLng> temp =  finalMap.remove(i);
                            temp.add(tempCord);
                            finalMap.put(i, temp);
                        }
                    }

                }
                //Toast.makeText(MainApplication.getContext(), i+" file(s) downloaded", Toast.LENGTH_SHORT).show();
            }
        return finalMap;
    }
}
