package com.androidex.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by simon on 2016/7/11.
 */


public class SqlUtil {
    static final int SZ_FP_TEMPLATE_SIZE = (570);
    public static final String DATABASE_FILE_NAME="residential.db";
    public static final int DATABASE_VERSION=3;
    private SQLiteDatabase db=null;
    private SqlHelper sqlHelper=null;
    //private ArrayList<FingerData> fingerList=new ArrayList<FingerData>();

    public SqlUtil(Context context){
        sqlHelper=new SqlHelper(context);
        db=sqlHelper.getWritableDatabase();
    }

    public void close(){
        if(db!=null){
            db.close();
            db=null;
        }
    }
   /* public void changeFinger(JSONArray data){
        if(data!=null){
            for(int i=0;i<data.length();i++){
                try {
                    JSONObject fingerItem = data.getJSONObject(i);
                    int lockIndex = fingerItem.getInt("lockIndex");
                    int lockId=fingerItem.getInt("lockId");
                    int userId=fingerItem.getInt("userId");
                    int employeeId=fingerItem.getInt("employeeId");
                    String finger = fingerItem.getString("finger");
                    String state=fingerItem.getString("state");
                    if(state.equals("N")||state.equals("F")||state.equals("G")){
                        writeFinger(lockId,lockIndex,userId,employeeId,finger);
                    }else if(state.equals("D") || state.equals("R")){
                        removeFinger(lockId,lockIndex,userId,employeeId);
                    }else if(state.equals("U")){
                        updateFinger(lockId,lockIndex,userId,employeeId,finger);
                    }
                }catch(JSONException e){
                }
            }
        }
    }*/

    public void changeCard(JSONArray data){
        if(data!=null){
            for(int i=0;i<data.length();i++){
                try {
                    JSONObject cardItem = data.getJSONObject(i);
                    int lockIndex = cardItem.getInt("lockIndex");
                    int lockId=cardItem.getInt("lockId");
                    String cardNo=cardItem.getString("cardNo");
                    String state=cardItem.getString("state");
                    if(state.equals("N")||state.equals("F") || state.equals("G")){
                        writeCard(lockId,lockIndex,cardNo);
                    }else if(state.equals("D") || state.equals("R")){
                        removeCard(lockId,lockIndex,cardNo);
                    }
                }catch(JSONException e){
                }
            }
        }
    }

    public void clearDeviceData(){
        try
        {
            db.execSQL("DELETE FROM RE_CARD", new Object[] {});
        }finally{
        }

       /* try
        {
            db.execSQL("DELETE FROM RE_FINGER", new Object[] {});
        }finally{
        }*/
        //fingerList.clear();
    }
    protected void writeCard(int lockId,int lockIndex,String cardNo){
        try
        {
            db.execSQL("INSERT INTO RE_CARD(lockId,lockIndex,cardNo)"
                    + " VALUES(?, ?, ?)", new Object[] {lockId,lockIndex,cardNo});
        }finally{
        }
    }

    protected void removeCard(int lockId,int lockIndex,String cardNo){
        db.execSQL("DELETE FROM RE_CARD where lockId=? and cardNo=?" ,new Object[] {lockId,cardNo});
    }

    protected byte[] convertData(String data){
        /*
        JSONArray array=null;
        byte[] byteData=null;
        try {
            array = new JSONArray(data);
        }catch(JSONException e){}

        if(array!=null){
            byteData=new byte[array.length()];
            for(int i=0;i<array.length();i++){
                try {
                    byteData[i] = (byte)array.getInt(i);
                }catch(JSONException e){}
            }
        }*/
        byte byteData[]=android.util.Base64.decode(data, Base64.DEFAULT);
        return byteData;
    }

   /* public void initFingerList(){
        int fingerNum=getFingerNum();
        System.out.println(fingerNum);
//        if(fingerNum!=0){
//            clearDeviceData();
//        }
//        Thread thread=new Thread(){
//            public void run() {
//                try {
//                    initTestFinger();
//                }catch(Exception e){
//                }
//            }
//        };
//        thread.start();
    }*/

    /*public void loadFingerList(){
        String sql="select userId,employeeId,finger from RE_FINGER";
        Cursor cursor = db.rawQuery(sql,new String[]{});
        while(cursor.moveToNext()){
            FingerData fingerData=new FingerData();
            fingerData.userId=cursor.getInt(0);
            fingerData.employeeId=cursor.getInt(1);
            fingerData.finger= cursor.getBlob(2);
            fingerList.add(fingerData);
        }
        cursor.close();
    }*/

   /* protected void initTestFinger(){
        File[] files=new File[0];
        String SDCard= Environment.getExternalStorageDirectory()+"";
        String dir=SDCard+"/"+ DeviceConfig.LOCAL_FILE_PATH+"2";
        File path=new File(dir);
        if(path.isDirectory()){
            files=path.listFiles();
        }
        for(int i=0;i<files.length;i++){
            String fileName=files[i].getName();
            fileName=fileName.substring(0,fileName.indexOf("."));
            int userId= Integer.parseInt(fileName);
            int lockId=0;
            int lockIndex=userId;
            byte[] data=initTestFinger(files[i]);
            writeFinger(lockId,lockIndex,userId,0,data);
        }
    }*/

    protected byte[] initTestFinger(File file){
        byte[] fingerData=new byte[SZ_FP_TEMPLATE_SIZE];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fingerData,0,SZ_FP_TEMPLATE_SIZE);
            fileInputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return fingerData;
    }

   /* protected void writeFinger(int lockId,int lockIndex,int userId,int employeeId,String finger){
        byte[] fingerData=convertData(finger);
        writeFinger(lockId,lockIndex,userId,employeeId,fingerData);
    }*/

  /*  protected void writeFinger(int lockId,int lockIndex,int userId,int employeeId,byte[] fingerData){
        try
        {
            db.execSQL("INSERT INTO RE_FINGER(lockId,lockIndex,userId,employeeId,finger)"
                    + " VALUES(?, ?, ?, ?, ?)", new Object[] {lockId,
                    lockIndex,userId,employeeId,fingerData});
        }finally{
        }
        FingerData fData=new FingerData();
        fData.userId=userId;
        fData.employeeId=employeeId;
        fData.finger=fingerData;
        fingerList.add(fData);
    }*/

   /* protected void updateFinger(int lockId,int lockIndex,int userId,int employeeId,String finger){
        try
        {
            byte[] fingerData=convertData(finger);
            db.execSQL("UPDATE RE_FINGER set finger=? where lockId=? and userId=? and employeeId=?", new Object[] {fingerData,lockId,userId,employeeId});
            removeFingerFromList(userId,employeeId);
            FingerData fData=new FingerData();
            fData.userId=userId;
            fData.employeeId=employeeId;
            fData.finger=fingerData;
            fingerList.add(fData);
        }finally{
        }
    }*/

   /* protected void removeFingerFromList(int userId,int employeeId){
        int length=fingerList.size();
        for(int i=0;i<length;i++){
            FingerData fingerData=fingerList.get(i);
            if(fingerData.userId==userId&&fingerData.employeeId==employeeId){
                fingerList.remove(i);
                break;
            }
        }
    }*/

    /*protected void removeFinger(int lockId,int lockIndex,int userId,int employeeId){
        db.execSQL("DELETE FROM RE_FINGER where lockId=? and userId=? and employeeId=?" ,new Object[] {lockId,userId,employeeId});
        removeFingerFromList(userId,employeeId);
    }*/

    /*protected void clearFinger(){
        db.execSQL("DELETE FROM RE_FINGER" ,new Object[]{});
        fingerList.clear();
    }*/

    public void clearCard(){
        db.execSQL("DELETE FROM RE_CARD" ,new Object[]{});
    }
    public void insertCard(String cardNo,int lockId){
        db.execSQL("INSERT INTO RE_CARD(lockId,lockIndex,cardNo)"
                + " VALUES(?, ?, ?)", new Object[] {lockId,null,cardNo});
    }

    public boolean checkCardAvailable(String cardNo){
        Cursor cursor = db.rawQuery("select count(*) as cardNum from RE_CARD where cardNo=?",new String[]{cardNo});
        boolean result=false;
        if(cursor.moveToFirst()) {
            int cardNum = cursor.getInt(cursor.getColumnIndex("cardNum"));
            if(cardNum>0){
                result=true;
            }
        }
        cursor.close();
        return result;
    }

   /* public int getFingerNum(){
        return fingerList.size();
//        Cursor cursor = db.rawQuery("select count(*) as fingerNum from RE_FINGER",new String[]{});
//        int fingerNum=0;
//        if(cursor.moveToFirst()) {
//            fingerNum = cursor.getInt(cursor.getColumnIndex("fingerNum"));
//        }
//        cursor.close();
//        return fingerNum;
    }*/

//    private List<byte[]> getFingers(int from, int length){
//        String sql=null;
//        if(from==0&&length==0){
//            sql="select finger from RE_FINGER";
//        }else{
//            sql="select finger from RE_FINGER LIMIT ? OFFSET ?";
//        }
//
//        Cursor cursor = db.rawQuery(sql,new String[]{});
//        List<byte[]> fingerList=new ArrayList<byte[]>();
//        while(cursor.moveToNext())
//        {
//            byte[] fingerData= cursor.getBlob(0);
//            fingerList.add(fingerData);
//        }
//        cursor.close();
//        return fingerList;
//    }

   /* public FingerData checkFinger(byte[] thisFinger,IFingerCheck iFingerCheck,int limit,int offset){
        int listLength=getFingerNum();
        FingerData thisFingerData=null;
        try {
            for(int i=0;i<limit;i++){
                if(iFingerCheck.isFingerChecking()){
                    int index=offset+i;
                    if(index<listLength){
                        FingerData fingerData=fingerList.get(index);
                        if (iFingerCheck.checkFinger(thisFinger,fingerData.finger)) {
                            thisFingerData =fingerData;
                            break;
                        }
                    }else{
                        break;
                    }
                }else{
                    break;
                }
            }
        }catch(Exception e){
        }
        return thisFingerData;
    }*/

//    public int checkFinger(byte[] thisFinger,IFingerCheck iFingerCheck,int limit,int offset){
//        String sql="select finger,userId from RE_FINGER LIMIT ? OFFSET ?";
//        Cursor cursor = db.rawQuery(sql,new String[]{String.valueOf(limit),String.valueOf(offset)});
//        List<byte[]> fingerList=new ArrayList<byte[]>();
//        int findUserId=0;
//        try {
//            while (cursor.moveToNext() && iFingerCheck.isFingerChecking()) {
//                byte[] fingerData = cursor.getBlob(0);
//                int userId=cursor.getInt(1);
//                if (iFingerCheck.checkFinger(thisFinger, fingerData)) {
//                    findUserId =userId;
//                    break;
//                }
//            }
//        }catch(Exception e){
//        }
//        cursor.close();
//        return findUserId;
//    }
}

class SqlHelper extends SQLiteOpenHelper {

    public SqlHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                     int version, DatabaseErrorHandler errorHandler){
        super(context, name, factory, version, errorHandler);
    }

    public SqlHelper(Context context){
        super(context,SqlUtil.DATABASE_FILE_NAME, null, SqlUtil.DATABASE_VERSION);
    }

    protected void createDatabase(SQLiteDatabase db){
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("CREATE TABLE IF NOT EXISTS RE_CARD (");
        stringBuffer.append("lockId INT ,");
        stringBuffer.append("lockIndex INT ,");
        stringBuffer.append("cardNo TEXT)");

        // 执行创建表的SQL语句
        try {
            db.execSQL(stringBuffer.toString());
        }catch(Exception e){
            e.printStackTrace();
        }

        stringBuffer = new StringBuffer();

        stringBuffer.append("CREATE TABLE IF NOT EXISTS RE_FINGER (");
        stringBuffer.append("lockId INT ,");
        stringBuffer.append("lockIndex INT ,");
        stringBuffer.append("userId INT ,");
        stringBuffer.append("employeeId INT ,");
        stringBuffer.append("finger BLOB)");

        // 执行创建表的SQL语句
        try {
            db.execSQL(stringBuffer.toString());
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 构建创建表的SQL语句（可以从SQLite Expert工具的DDL粘贴过来加进StringBuffer中）
        createDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if(oldVersion==1){
            StringBuffer stringBuffer = new StringBuffer();

            stringBuffer.append("ALTER TABLE RE_FINGER MODIFY finger BLOB;");

            // 执行创建表的SQL语句
            try {
                sqLiteDatabase.execSQL(stringBuffer.toString());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if(oldVersion==2){
            StringBuffer stringBuffer = new StringBuffer();

            stringBuffer.append("ALTER TABLE RE_FINGER Add column employeeId int;");

            // 执行创建表的SQL语句
            try {
                sqLiteDatabase.execSQL(stringBuffer.toString());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
