package com.androidex.face.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.androidex.face.utils.UserInfo;

import java.util.ArrayList;

/**
 * Created by cts on 17/4/7.
 * 对人脸数据表的操作类
 */

public class FaceDao {
    /**
     * 单例模式
     */
    private  DbOpenHelper helper;

    private static FaceDao faceDao;

    private FaceDao(Context context) {
        helper = new DbOpenHelper(context);
    }
    public static FaceDao getInstance(Context context){
        if (faceDao==null){
            faceDao = new FaceDao(context);
        }
        return faceDao;
    }

    /**
     * 遍历查询所有数据
     */
    public ArrayList<UserInfo> getUserinfo(){
        ArrayList<UserInfo> users = null;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query("face", null, null, null, null, null, null);
        if (c != null) {
            users = new ArrayList<UserInfo>();
            while (c.moveToNext()) {
                UserInfo user = new UserInfo();
                user.username = c.getString(c.getColumnIndex("name"));
                user.facepath = c.getString(c.getColumnIndex("imgPic"));
                users.add(user);
            }
            c.close();
        }
        db.close();
        return users;
    }

    /**
     * 插入数据
     */
    public long  insertUserinfo(String username,String imgpic){
        long rowId  = -1;
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name",username);
        values.put("imgPic",imgpic);
        db.insert("face",null,values);
        return rowId;
    }

    /**
     * 删除数据
     */

}
