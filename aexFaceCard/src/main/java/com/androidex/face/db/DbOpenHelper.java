package com.androidex.face.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by cts on 17/4/7.
 * 创建数据库
 */

public class DbOpenHelper extends SQLiteOpenHelper{

    public DbOpenHelper(Context context) {
        super(context, "Userinfo.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建数据表
        ContentValues values = new ContentValues();
        //用户信息表
        db.execSQL("create table face("
                + "_id integer primary key autoincrement,"
                + "name text ,"
                + "imgPic text"
                +")");

        db.execSQL("create table cardinfo(" +
                "_id integer primary key autoincrement," +
                "name text," +
                "imgPic text" +
                "sex text" +
                "nation text" +
                "birthday text" +
                "address text" +
                "idnum text" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
