package com.ustc.wsn.mydataapp.bean;
/**
 * Created by halo on 2017/7/1.
 */

import com.ustc.wsn.mydataapp.detectorservice.outputFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class StoreData {

    // private Context context;
    // private File sdCardDir;
    private File accFile;
    private File magFile;
    private File gyroFile;

    private File accFileTrans;
    private File magFileTrans;
    private File gyroFileTrans;

    private File Combine;
    private File Attitude;
    private File rawFile;
    private File locFile;
    private File z7RawFile;
    private File z7CombineFile;
    private File pathFile;

    private String accList;
    private String gyroList;
    private String magList;

    private String accListTrans;
    private String gyroListTrans;
    private String magListTrans;

    private String CombineList;
    private String rawList;
    private String AttitudeList;

    private String pathList;

    private BufferedWriter accWriter;
    private BufferedWriter magWriter;
    private BufferedWriter gyroWriter;

    private BufferedWriter accWriterTrans;
    private BufferedWriter magWriterTrans;
    private BufferedWriter gyroWriterTrans;
    private BufferedWriter CombineWriter;
    private BufferedWriter AttitudeWriter;
    private BufferedWriter rawWriter;
    private BufferedWriter pathWriter;
    File dir;

    public StoreData() {
        /*
		 * if (Environment.getExternalStorageState().equals(
		 * Environment.MEDIA_MOUNTED)) { sdCardDir =
		 * Environment.getExternalStorageDirectory();// 鑾峰彇SDCard鐩綍
		 * 
		 * String dirPath = sdCardDir.getPath() + "/DetectorService"; dir = new
		 * File(dirPath); if (!dir.exists()) dir.mkdirs(); // Log.i("鍒涘缓瀛樺偍鐩綍",
		 * "--------------------"); } else { File temp =
		 * Environment.getDataDirectory(); dir = new
		 * File(temp+"/DetectorService"); }
		 */

		/*
		accFile = outputFile.getaccFile();
		magFile = outputFile.getmagFile();
		gyroFile = outputFile.getgyroFile();
		Combine = outputFile.getCombineFile();
		Attitude = outputFile.getattitudeFile();
		*/

		/*
		 * accFileTrans = outputFile.getaccTransFile(); gyroFileTrans =
		 * outputFile.getgyroTransFile(); magFileTrans =
		 * outputFile.getmagTransFile();
		 */
        rawFile = outputFile.getrawFile();
        locFile = outputFile.getlocFile();
        z7RawFile = outputFile.getz7RawFile();
        pathFile = outputFile.getPathFile();

        accList = new String();
        gyroList = new String();
        magList = new String();
        CombineList = new String();
        accListTrans = new String();
        gyroListTrans = new String();
        magListTrans = new String();
        AttitudeList = new String();
        rawList = new String();
        pathList = new String();
		/*
		try {
			accWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(accFile, true)));

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			magWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(magFile, true)));

		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			gyroWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(gyroFile, true)));

		} catch (IOException e3) {
			e3.printStackTrace();
		}

		try {
			CombineWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Combine, true)));

		} catch (IOException e4) {
			e4.printStackTrace();
		}
		try {
			AttitudeWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Attitude, true)));

		} catch (IOException e4) {
			e4.printStackTrace();
		}
		*/
        try {
            rawWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rawFile, true)));

        } catch (IOException e4) {
            e4.printStackTrace();
        }

        try {
            pathWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFile, true)));

        } catch (IOException e4) {
            e4.printStackTrace();
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

    public File getPathDataFile() {

        return pathFile;
    }

    public File getz7RawDataFile() {

        return z7RawFile;
    }

    public File getNewz7RawDataFile() {
        z7RawFile = outputFile.getz7RawFile();
        return z7RawFile;
    }

    public File getz7CombineDataFile() {
        z7CombineFile = outputFile.getz7CombineFile();
        return z7CombineFile;
    }

    public File newCombineDataFile() {
        Combine = outputFile.getCombineFile();
        try {
            CombineWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Combine, true)));

        } catch (IOException e4) {
            e4.printStackTrace();
        }
        return Combine;
    }

    public File getCombineDataFile() {
        return Combine;
    }

    public void storeDataAccelerator(String[] accData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        // acc.add(accData.toString());// = accData.toString();
        int WriteSize = accData.length;
        for (int i = 0; i < WriteSize; i++) {
            if (accData[i] != null) {

                // String accData = String.valueOf(time) + "\t" + aData[0]
                // + "\t" + aData[1] + "\t" + aData[2] + "\n";
                // for(int i = 0; i<= acc_i; i++){
                accList += accData[i] + "\n";
            }
            accWriter.write(accList);
            // }
            accWriter.flush();
            accList = new String();
            // accWriter.close();
            // Log.i("Store data of Accelerator", accData);
        }
    }

    public void storeDataMagnetic(String[] magData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        int WriteSize = magData.length;
        for (int i = 0; i < WriteSize; i++) {
            if (magData[i] != null) {
                // String gyroData = String.valueOf(time) + "\t" + gData[0]
                // + "\t" + gData[1] + "\t" + gData[2] + "\n";
                magList += magData[i] + "\n";
            }
            magWriter.write(magList);
            magWriter.flush();
            magList = new String();

            // magWriter.close();
            // Log.i("Store data of Gyro", gyroData);
        }
    }

    public void storeDataGyroscope(String[] gyroData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        int WriteSize = gyroData.length;
        for (int i = 0; i < WriteSize; i++) {
            if (gyroData[i] != null) {
                // String gyroData = String.valueOf(time) + "\t" + gData[0]
                // + "\t" + gData[1] + "\t" + gData[2] + "\n";
                gyroList += gyroData[i] + "\n";
            }
            gyroWriter.write(gyroList);
            gyroWriter.flush();
            gyroList = new String();
            // gyroWriter.close();
        }

    }

    public void storeDataAcceleratorTrans(String[] accData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        // acc.add(accData.toString());// = accData.toString();
        int WriteSize = accData.length;
        for (int i = 0; i < WriteSize; i++) {
            if (accData[i] != null) {

                // String accData = String.valueOf(time) + "\t" + aData[0]
                // + "\t" + aData[1] + "\t" + aData[2] + "\n";
                // for(int i = 0; i<= acc_i; i++){
                accListTrans += accData[i] + "\n";
            }
            accWriterTrans.write(accListTrans);
            // }
            accWriterTrans.flush();
            accListTrans = new String();
            // accWriter.close();
            // Log.i("Store data of Accelerator", accData);
        }
    }

    public void storeDataMagneticTrans(String[] magData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        int WriteSize = magData.length;
        for (int i = 0; i < WriteSize; i++) {
            if (magData[i] != null) {
                // String gyroData = String.valueOf(time) + "\t" + gData[0]
                // + "\t" + gData[1] + "\t" + gData[2] + "\n";
                magListTrans += magData[i] + "\n";
            }
            magWriterTrans.write(magListTrans);
            magWriterTrans.flush();
            magListTrans = new String();

            // magWriter.close();
            // Log.i("Store data of Gyro", gyroData);
        }
    }

    public void storeDataGyroscopeTrans(String[] gyroData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        int WriteSize = gyroData.length;
        for (int i = 0; i < WriteSize; i++) {
            if (gyroData[i] != null) {
                // String gyroData = String.valueOf(time) + "\t" + gData[0]
                // + "\t" + gData[1] + "\t" + gData[2] + "\n";
                gyroListTrans += gyroData[i] + "\n";
            }
            gyroWriterTrans.write(gyroListTrans);
            gyroWriterTrans.flush();
            gyroListTrans = new String();
            // gyroWriter.close();
        }

    }

    public void storeDataCombine(String[] combineData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        int WriteSize = combineData.length;
        for (int i = 0; i < WriteSize; i++) {
            if (combineData[i] != null) {
                // String gyroData = String.valueOf(time) + "\t" + gData[0]
                // + "\t" + gData[1] + "\t" + gData[2] + "\n";
                CombineList += combineData[i] + "\n";
            }
            CombineWriter.write(CombineList);
            CombineWriter.flush();
            CombineList = new String();
            // gyroWriter.close();
        }

    }

    public void storeDataAttitude(String[] attitudeData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        int WriteSize = attitudeData.length;
        for (int i = 0; i < WriteSize; i++) {
            if (attitudeData[i] != null) {
                // String gyroData = String.valueOf(time) + "\t" + gData[0]
                // + "\t" + gData[1] + "\t" + gData[2] + "\n";
                AttitudeList += attitudeData[i] + "\n";
            }
            AttitudeWriter.write(AttitudeList);
            AttitudeWriter.flush();
            AttitudeList = new String();
            // gyroWriter.close();
        }

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

    public void storeDataPath(String pathData) throws IOException {
        // SimpleDateFormat formatter = new SimpleDateFormat(
        // "yyyy/MM/dd HH:mm:ss:sss");
        // Date curDate = new Date(time);
        if (pathData != null) {
            // String gyroData = String.valueOf(time) + "\t" + gData[0]
            // + "\t" + gData[1] + "\t" + gData[2] + "\n";
            rawList += pathData;
        }
        rawWriter.write(rawList);
        rawWriter.flush();
        rawList = new String();
        //rawWriter.close();

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
