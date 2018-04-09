package com.ustc.wsn.mydataapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.bean.FileBean;
import com.ustc.wsn.mydataapp.dao.SQLOperate;
import com.ustc.wsn.mydataapp.db.MessageDBHelper;
import com.ustc.wsn.mydataapp.utils.GsonUtils;
import com.ustc.wsn.mydataapp.utils.MD5Util;
import com.ustc.wsn.mydataapp.utils.z7Compression;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by chong on 2017/8/25.
 */


public class AutoUploadSeriver extends Service {
    protected final String TAG = AutoUploadSeriver.this.toString();
    private static boolean UploadThreadDisabled = false;

    private final String PORT_URL = "http://sdkapi.geotmt.com/upload";

    private final int MESSAGE_0 = 0;
    private final int MESSAGE_1 = 1;

    private final int MESSAGE_2 = 2;

    private final int MESSAGE_3 = 3;

    private final int MAX_ERROR_COUNT = 3;

    private final int FIRST_RUN_DLY_TIME = 5000;
    private final int SCANNING_FILE_CYCLE = 10000;
    private final int STOP_TIME = 100;
    private List<UploadBean> mUploadBeen = new ArrayList<>();
    private SQLOperate mSqlOperate;
    private List<FileBean> mFileBeans;
    private String mFoldersPath;
    private String mUserId;
    private Thread mThread;

    private Throwable mThrowable;

    private int mErrorCount;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_1:
                    mFileBeans = mSqlOperate.getAll();
                    checkFolders(mFoldersPath);
                    checkData();
                    break;
                case MESSAGE_2:
                    checkData();
                    break;
                case MESSAGE_3:
                    checkExists(mFoldersPath);
                    break;
                case MESSAGE_0:
                    //finish();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        UploadThreadDisabled = false;
        mFoldersPath = checkNull(intent.getStringExtra("foldersPath"));
        mUserId = checkNull(intent.getStringExtra("userId"));

        MessageDBHelper dbHelper = new MessageDBHelper(this);
        mSqlOperate = new SQLOperate(dbHelper);
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_3, FIRST_RUN_DLY_TIME);
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        UploadThreadDisabled = true;
        if (mHandler != null) {
            mHandler = null;
            //Context context = null;
            //Toast.makeText(context, "已停止", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkFolders(String path) {
        File tempFile = new File(path);

        File[] files = tempFile.listFiles();

        if (files == null) {
            if (path.equals(mFoldersPath)) mSqlOperate.clear();
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                checkFolders(file.getPath());
            } else if (file.isFile()) {
                checkFile(file, path);
            }
        }
    }

    private void checkFile(File file, String foldersPath) {

        if (file.length() == 0) return;

        boolean isAdd = false;
        //check if the file has been upload
        if (mFileBeans != null && mFileBeans.size() != 0) {
            for (FileBean fileBean : mFileBeans) {
                if (fileBean.getFileName().equals(file.getName())) {
                    isAdd = true;
                    break;
                }
            }
        }

        if (isAdd) return;
        //Log.d(TAG,"doing");

        Log.d(TAG,foldersPath);

        if (file.getName().contains("raw") && file.getName().contains(".txt")) {
            //判断文件是否是当前正在写入的文件
            long lastModifiedTime = file.lastModified();
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastModifiedTime > 5 * 60 * 60 * 1000) {
                //Log.d(TAG,file.getPath());
                String inputPath = file.getPath();
                StringTokenizer st = new StringTokenizer(file.getPath(), ".");
                String z7Name = st.nextToken();
                File z7Raw = new File(z7Name + "_.7z");
                String outputPath = z7Raw.getPath();
                try {
                    z7Compression.z7(inputPath, outputPath);
                    //Log.d(TAG,"packdone");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                file.delete();

                mUploadBeen.add(new UploadBean(z7Raw.getName(), z7Raw.getPath(), new File(foldersPath).getName(), foldersPath));
            }
            return;
        }
        mUploadBeen.add(new UploadBean(file.getName(), file.getPath(), new File(foldersPath).getName(), foldersPath));
    }

    private void checkExists(String path) {
        if (!new File(path).exists()) {
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_3, FIRST_RUN_DLY_TIME);
            }
            return;
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MESSAGE_1);
        }
    }


    private void uploadFile(UploadBean uploadBeen) {

        RequestParams requestParams = new RequestParams(PORT_URL);
        requestParams.addBodyParameter("logfile", new File(uploadBeen.getFilePath()));
        requestParams.addBodyParameter("dir", uploadBeen.getParentName());
        requestParams.addBodyParameter("userId", mUserId);
        requestParams.addBodyParameter("token", MD5Util.encode(mUserId + uploadBeen.getFileName()));


        x.http().post(requestParams, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String json) {
                UploadJson uploadJson = GsonUtils.fromData(json, UploadJson.class);
                if (uploadJson.getCode() != 200) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_2, FIRST_RUN_DLY_TIME);
                    }
                    return;
                }
                Toast.makeText(AutoUploadSeriver.this, uploadJson.getDescription(), Toast.LENGTH_LONG).show();

                UploadBean uploadBean = mUploadBeen.get(0);
                mSqlOperate.add(new FileBean(uploadBean.getFileName()));
                mUploadBeen.remove(0);

                if (mUploadBeen.size() > 0) {
                    uploadFile(mUploadBeen.get(0));
                } else {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_1, SCANNING_FILE_CYCLE);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                if (mThrowable != null) {
                    mErrorCount = mThrowable.getClass() == throwable.getClass() ? ++mErrorCount : 0;
                }
                if (mErrorCount == MAX_ERROR_COUNT) {
                    mUploadBeen.remove(0);
                    mErrorCount = 0;
                }
                mThrowable = throwable;
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(MESSAGE_2, FIRST_RUN_DLY_TIME);
                }
            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private Thread newThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (UploadThreadDisabled == false) {
                        uploadFile(mUploadBeen.get(0));
                    }
                } catch (Exception e) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_2, FIRST_RUN_DLY_TIME);
                    }
                    e.printStackTrace();
                }
                Log.d(TAG, "Uploading");
            }
        });
    }

    private void checkData() {
        if (mUploadBeen.size() > 0) {
            if (mThread == null || !mThread.isAlive()) {
                (mThread = newThread()).start();
            }
        } else {
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_1, SCANNING_FILE_CYCLE);
            }
        }
    }

    private String checkNull(String text) {
        return text == null ? "" : text;
    }

    //////////////////////////////////////////////////////////////// UploadJson Class
    public class UploadJson {

        /**
         * code : 200
         * descript : sss
         */

        private int code;
        private String descript;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDescription() {
            return descript;
        }

        public void setDescription(String description) {
            this.descript = description;
        }
    }

    /////////////////////////////////////////////////////////// UploadBean Class
    class UploadBean {

        private String fileName;
        private String filePath;
        private String parentName;
        private String parentPath;

        public UploadBean(String fileName, String filePath, String parentName, String parentPath) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.parentName = parentName;
            this.parentPath = parentPath;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getParentPath() {
            return parentPath;
        }

        public void setParentPath(String parentPath) {
            this.parentPath = parentPath;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getParentName() {
            return parentName;
        }

        public void setParentName(String parentName) {
            this.parentName = parentName;
        }
    }


}
