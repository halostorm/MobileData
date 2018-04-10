package com.ustc.wsn.mydataapp.bean;
/**
 * Created by halo on 2017/7/1.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class StoreData {

    private File rawFile;
    private File locFile;
    private File z7RawFile;
    private String rawList;


    private BufferedWriter rawWriter;

    public StoreData(boolean GPS,boolean Sensor) {
        if(Sensor) {
            rawFile = outputFile.getrawFile();
            rawList = new String();

            try {
                rawWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rawFile, true)));

            } catch (IOException e4) {
                e4.printStackTrace();
            }

            z7RawFile = outputFile.getz7RawFile();
        }
        if(GPS){
            locFile = outputFile.getlocFile();
        }
    }

    public File newRawDataFile() {
        rawFile = outputFile.getrawFile();
        try {
            rawWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rawFile, true)));

        } catch (IOException e4) {
            e4.printStackTrace();
        }
        return rawFile;
    }

    public File getRawDataFile() {

        return rawFile;
    }

    public File getz7RawDataFile() {

        return z7RawFile;
    }

    public File getNewz7RawDataFile() {
        z7RawFile = outputFile.getz7RawFile();
        return z7RawFile;
    }


    public void storeDataRaw(String[] rawData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        int WriteSize = rawData.length;
        for (int i = 0; i < WriteSize; i++) {
            if (rawData[i] != null) {
                // String gyroData = String.valueOf(time) + "\t" + gData[0]
                // + "\t" + gData[1] + "\t" + gData[2] + "\n";
                rawList += rawData[i] + "\n";
            }
            rawWriter.write(rawList);
            rawWriter.flush();
            rawList = new String();
            // gyroWriter.close();
        }

    }

    public void storeLocation(String locData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        try {
            BufferedWriter locWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(locFile, true)));
            if (locData != null) {
                locWriter.write(locData + "\n");
                locWriter.flush();
                // locWriter.close();
                // Log.i("Store Location", locData.toString());
            }
        } catch (IOException e4) {
            e4.printStackTrace();
        }

    }
}
