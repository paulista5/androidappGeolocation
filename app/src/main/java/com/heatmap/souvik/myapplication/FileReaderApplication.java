package com.heatmap.souvik.myapplication;

/**
 * Created by souvik on 12/5/17.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FileReaderApplication {

    public static HashMap<Integer, ArrayList<String>>readFile(String fileName) throws IOException {

        HashMap<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();
        BufferedReader br = null;
        FileReader fr = null;
        int timer = 0;
        try{
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);

            String currentLine;

            while(br != null && (currentLine = br.readLine()) != null){
                int i;
                StringBuffer ip = new StringBuffer();
                for(i = 0; i<currentLine.length(); i++){
                    if(currentLine.charAt(i) == ','){
                        break;
                    }
                    ip.append(currentLine.charAt(i));
                }
                //Log.d("d",ip.toString());
                int count = 0;
                int minute = 0;
                //int prevminute = 0;
                for(;i<currentLine.length();i++){
                    if(currentLine.charAt(i) == ':'){
                        count++;
                        if(count == 2){
                            minute  = ((int)currentLine.charAt(i-2) - 48)*10 + ((int)currentLine.charAt(i-1) - 48);
                            //System.out.println(minute);
                        }
                    }
                    if(count == 2)
                        break;
                    //System.out.println(minute);
                }
                System.out.println(minute);
                if(!map.containsKey(minute)){
                    ArrayList<String> temp = new ArrayList<String>();
                    temp.add(ip.toString());
                    map.put(minute, temp);
                }
                else{
                    ArrayList<String> temp = map.remove(minute);
                    temp.add(ip.toString());
                    map.put(minute, temp);
                }
                timer = minute;
            }
            MainApplication.setMaxTime(timer);
            //System.out.println(timer);
        } catch(IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                if(br!=null)
                    br.close();
                if(fr!=null)
                    fr.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }

        }

        return map;
    }
}
