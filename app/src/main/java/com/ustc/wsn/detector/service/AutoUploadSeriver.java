package com.ustc.wsn.detector.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.ustc.wsn.detector.bean.FileBean;
import com.ustc.wsn.detector.dao.SQLOperate;
import com.ustc.wsn.detector.db.MessageDBHelper;
import com.ustc.wsn.detector.utils.GsonUtils;
import com.ustc.wsn.detector.utils.MD5Util;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chong on 2017/8/25.
 */

public class AutoUploadSeriver extends Service {

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
            }
        }
    };


    private final String PORT_URL = "http://sdkapi.geotmt.com/upload";

    private final int MESSAGE_1 = 1;

    private final int MESSAGE_2 = 2;

    private final int MESSAGE_3 = 3;

    private final int MAX_ERROR_COUNT = 3;

    private final int FIRST_RUN_DLY_TIME = 5000;
    private final int SCANNING_FILE_CYCLE = 10000;
    private List<UploadBean> mUploadBeen = new ArrayList<>();
    private SQLOperate mSqlOperate;
    private List<FileBean> mFileBeans;
    private String mFoldersPath;
    private String mUserId;
    private Thread mThread;

    private Throwable mThrowable;

    private int mErrorCount;


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
        if (mFileBeans != null && mFileBeans.size() != 0)
            for (FileBean fileBean : mFileBeans) {
                if (fileBean.getFileName().equals(file.getName())) {
                    isAdd = true;
                    break;
                }
            }
        if (isAdd) return;

        if (file.getName().contains("raw") && file.getName().contains(".txt")) {
            //判断文件是否是当前正在写入的文件
            long lastModifiedTime = file.lastModified();
            long currentTime = System.currentTimeMillis();
            if(currentTime - lastModifiedTime>5*60*60*1000)
            {
                mUploadBeen.add(new UploadBean(file.getName(), file.getPath(), new File(foldersPath).getName(), foldersPath));
            }
            return;
        }
        mUploadBeen.add(new UploadBean(file.getName(), file.getPath(), new File(foldersPath).getName(), foldersPath));
    }

    private void checkExists(String path) {
        if (!new File(path).exists()) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_3, FIRST_RUN_DLY_TIME);
            return;
        }
        mHandler.sendEmptyMessage(MESSAGE_1);
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
                    mHandler.sendEmptyMessageDelayed(MESSAGE_2, FIRST_RUN_DLY_TIME);
                    return;
                }
                Toast.makeText(AutoUploadSeriver.this, uploadJson.getDescription(), Toast.LENGTH_LONG).show();

                UploadBean uploadBean = mUploadBeen.get(0);
                mSqlOperate.add(new FileBean(uploadBean.getFileName()));
                mUploadBeen.remove(0);

                if (mUploadBeen.size() > 0) {
                    uploadFile(mUploadBeen.get(0));
                } else {
                    mHandler.sendEmptyMessageDelayed(MESSAGE_1, SCANNING_FILE_CYCLE);
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
                mHandler.sendEmptyMessageDelayed(MESSAGE_2, FIRST_RUN_DLY_TIME);
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
                    uploadFile(mUploadBeen.get(0));
                } catch (Exception e) {
                    mHandler.sendEmptyMessageDelayed(MESSAGE_2, FIRST_RUN_DLY_TIME);
                    e.printStackTrace();
                }
            }
        });
    }


    private void checkData() {
        if (mUploadBeen.size() > 0) {
            if (mThread == null || !mThread.isAlive()) (mThread = newThread()).start();
        } else {
            mHandler.sendEmptyMessageDelayed(MESSAGE_1, SCANNING_FILE_CYCLE);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mFoldersPath = checkNull(intent.getStringExtra("foldersPath"));
        mUserId = checkNull(intent.getStringExtra("userId"));

        MessageDBHelper dbHelper = new MessageDBHelper(this);
        mSqlOperate = new SQLOperate(dbHelper);

        mHandler.sendEmptyMessageDelayed(MESSAGE_3, FIRST_RUN_DLY_TIME);

        return START_STICKY;
    }


    private String checkNull(String text) {
        return text == null ? "" : text;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


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
