package com.ustc.wsn.mobileData.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MessageDBHelper extends SQLiteOpenHelper {

    public static final String MESSAGE_DATA_NAME = "imchat-db";
    public static final int MESSAGE_DATA_VERSION = 1;
    public static final String TABLE_NAME = "message_data";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "_text";
    public static final String COLUMN_NAME = "_name";
    public static final String COLUMN_TIME = "_time";
    public static final String COLUMN_UID = "_uid";
    public static final String COLUMN_FROM = "_from";


    public MessageDBHelper(Context context) {
        super(context, MESSAGE_DATA_NAME, null, MESSAGE_DATA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        try{
            String CREAT_FRIEND_TABLE = "Create table "+ TABLE_NAME + "("+COLUMN_ID+" integer primary key autoincrement,"
                    + COLUMN_NAME +" text" +")";
            db.execSQL(CREAT_FRIEND_TABLE);
        }catch (Exception e){
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
