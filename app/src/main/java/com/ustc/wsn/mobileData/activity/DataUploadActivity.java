package com.ustc.wsn.mobileData.activity;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ustc.wsn.mobileData.R;
import com.ustc.wsn.mobileData.bean.outputFile;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataUploadActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_upload);
        Button button = (Button) findViewById(R.id.btnDataUpload);
        //button.setText("send data and write file");
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new Thread(new NetWorUpload(getAssets())).start();
            }
        });
    }

    class NetWorUpload implements Runnable {
        AssetManager assetManager;

        /**
         *
         */
        public NetWorUpload(AssetManager assetManager) {
            this.assetManager = assetManager;
        }

        @Override
        public void run() {
            HttpClient client = new DefaultHttpClient();
            HttpPost postMethod = new HttpPost("http://192.168.1.101:8080/upload");

            //File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = outputFile.getUserDir();
            String picturePath = file.getAbsolutePath();
            try {
                byte[] buffer = new byte[1024 * 10];
                InputStream is = assetManager.open("one.jpg");
                File output = new File(picturePath + "/one.jpg");
                Log.d("MSG", output.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(output);
                int readCount = 0;
                Log.d("MSG", is.available() + "");
                while ((readCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, readCount);
                    Log.i("MSG", readCount + "");
                }
                fos.close();
                MultipartEntity entity = new MultipartEntity();
                FileBody contentFile = new FileBody(output);
                entity.addPart("uploadFile", contentFile);

                StringBody username = new StringBody("halo");
                entity.addPart("username", username);

                StringBody password = new StringBody("12009032");
                entity.addPart("password", password);

                postMethod.setEntity(entity);
                HttpResponse response = client.execute(postMethod);
                HttpEntity httpEntity = response.getEntity();
                String state = EntityUtils.toString(httpEntity);
                Log.i("MSG", state);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
