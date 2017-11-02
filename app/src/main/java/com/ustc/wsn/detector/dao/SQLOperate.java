package com.ustc.wsn.detector.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ustc.wsn.detector.bean.FileBean;
import com.ustc.wsn.detector.db.MessageDBHelper;

import java.util.ArrayList;
import java.util.List;

import static com.ustc.wsn.detector.db.MessageDBHelper.COLUMN_NAME;
import static com.ustc.wsn.detector.db.MessageDBHelper.COLUMN_UID;

public class SQLOperate {
  private SQLiteDatabase db = null;
  private SQLiteOpenHelper mSQliteOpenHelper = null;
  private List<FileBean> mFileBeen = new ArrayList<>();
  private SQLiteDatabase mDb;

  private String tabName;

  public SQLOperate(SQLiteOpenHelper SQliteOpenHelper) {
    mSQliteOpenHelper = SQliteOpenHelper;
    tabName = MessageDBHelper.TABLE_NAME;
  }

  public FileBean queryByUid(String uid) {
    FileBean fileBean = new FileBean();
    SQLiteDatabase db = mSQliteOpenHelper.getWritableDatabase();
    Cursor query = db
        .query(tabName, new String[]{COLUMN_NAME},
                MessageDBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(uid)}, null, null, null);
    while (query.moveToNext()) {
      String query_name = query.getString(query.getColumnIndex(COLUMN_NAME));
      fileBean.setFileName(query_name);
    }
    query.close();
    closeDb(db);
    return fileBean;
  }

  public void add(FileBean fileBean) {
    try {
      SQLiteDatabase db = mSQliteOpenHelper.getWritableDatabase();
      ContentValues contentValues = new ContentValues();
      contentValues.put(COLUMN_NAME, fileBean.getFileName());
      db.insert(tabName, null, contentValues);
      closeDb(db);
    }catch (Exception e){

    }

  }

  public void delete(String fileName) {
    SQLiteDatabase db = mSQliteOpenHelper.getWritableDatabase();
    db.delete(tabName, COLUMN_NAME + "=" + fileName, null);
    closeDb(db);
  }

  public void clear(){
    mSQliteOpenHelper.getWritableDatabase().execSQL("DELETE FROM " + tabName);
  }

  public void upData(FileBean fileBean) {
    SQLiteDatabase db = mSQliteOpenHelper.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(COLUMN_NAME, fileBean.getFileName() + "");
    db.update(tabName, contentValues, COLUMN_UID + "=" + fileBean.getFileName(), null);
    closeDb(db);
  }

  private void closeDb(SQLiteDatabase db) {
    if (db.isOpen()) {
      db.close();
    }
  }

  public List<FileBean> getAll() {
    SQLiteDatabase db = mSQliteOpenHelper.getWritableDatabase();
    try {
      Cursor cursor = db.rawQuery("select * from " + tabName, null);
      List<FileBean> mateList = queryForAll(cursor);
      closeDb(db);
      return mateList;
    }catch (Exception e){
    }
    return new ArrayList<>();
  }

  public List<FileBean> queryForAll(Cursor cursor) {
    if (cursor.moveToFirst()) {
      if (cursor.getCount() == 1) {
        FileBean fileBean = new FileBean();
        fileBean.setFileName(cursor.getString(1));
        mFileBeen.add(fileBean);
        return mFileBeen;
      }
    }
    while (cursor.moveToNext()) {
        FileBean fileBean = new FileBean();
        fileBean.setFileName(cursor.getString(1));
        mFileBeen.add(fileBean);
    }
    return mFileBeen;
  }
}
