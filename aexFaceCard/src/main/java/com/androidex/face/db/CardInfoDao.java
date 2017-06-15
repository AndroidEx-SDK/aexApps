package com.androidex.face.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.androidex.face.utils.CardInfo;

import java.util.ArrayList;

/**
 * Created by cts on 17/4/7.
 * 对人脸数据表的操作类
 */

public class CardInfoDao {
    /**
     * 单例模式
     */
    private  DbOpenHelper helper;

    private static CardInfoDao faceDao;
    private SQLiteDatabase db;

    private CardInfoDao(Context context) {
        helper = new DbOpenHelper(context);
    }
    public static CardInfoDao getInstance(Context context){
        if (faceDao==null){
            faceDao = new CardInfoDao(context);
        }

        return faceDao;
    }

    /**
     *
     *   db.execSQL("create table cardinfo(" +
     "_id integer primary key autoincrement," +
     "name text," +
     "imgPic text" +
     "sex text" +
     "nation text" +
     "birthday text" +
     "address text" +
     "idnum text"
     );
     * 遍历查询所有数据
     */
    public ArrayList<CardInfo> getCardInfo(){
        ArrayList<CardInfo> cardInfos = null;
        db = helper.getReadableDatabase();
        Cursor c = db.query("cardinfo", null, null, null, null, null, null);
        if (c != null) {
            cardInfos = new ArrayList<CardInfo>();
            while (c.moveToNext()) {
                CardInfo mCardInfo = new CardInfo();
                mCardInfo.name = c.getString(c.getColumnIndex("name"));
                mCardInfo.imgPic = c.getString(c.getColumnIndex("imgPic"));
                mCardInfo.sex = c.getString(c.getColumnIndex("sex"));
                mCardInfo.nation = c.getString(c.getColumnIndex("nation"));
                mCardInfo.birthday = c.getString(c.getColumnIndex("birthday"));
                mCardInfo.address = c.getString(c.getColumnIndex("address"));
                mCardInfo.idnum = c.getString(c.getColumnIndex("idnum"));
                mCardInfo.head = c.getString(c.getColumnIndex("head"));
                cardInfos.add(mCardInfo);
            }
            c.close();
        }
        db.close();
        return cardInfos;
    }

    public CardInfo queryCardInfo(String idnum){
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("cardinfo", null, "idnum=?",
                new String[]{idnum}, null, null, null);
        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String imgPic = cursor.getString(cursor.getColumnIndex("imgPic"));
            String sex = cursor.getString(cursor.getColumnIndex("sex"));
            String nation = cursor.getString(cursor.getColumnIndex("nation"));
            String birthday = cursor.getString(cursor.getColumnIndex("birthday"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            String head = cursor.getString(cursor.getColumnIndex("head"));
            CardInfo cardInfo = new CardInfo(name,imgPic,sex,nation,birthday,address,idnum,head);
            return cardInfo;
        }
        return null;
    }

    /**
     * 插入数据
     */
    public long  insertUserinfo(String name, String imgPic, String sex, String nation, String birthday, String address, String idnum){
        long rowId  = -1;
        if (queryCardInfo(idnum)==null){
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name",name);
            values.put("imgPic",imgPic);
            values.put("sex",sex);
            values.put("nation",nation);
            values.put("birthday",birthday);
            values.put("address",address);
            values.put("idnum",idnum);

            db.insert("cardinfo",null,values);
            Log.e("==CardInfoDao","插入成功");
        }else {
            Log.e("==CardInfoDao","重复插入");
        }

        return rowId;
    }
}
