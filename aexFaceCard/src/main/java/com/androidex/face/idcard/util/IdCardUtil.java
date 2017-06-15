package com.androidex.face.idcard.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;

import com.androidex.face.idcard.Application;
import com.androidex.face.idcard.ReaderSerialPort;
import com.synjones.idcard.IDCard;
import com.synjones.idcard.IDcardReader;
import com.synjones.multireaderlib.MultiReader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

/**
 * Created by cts on 17/3/24.
 * 身份证阅读器相关的操作类
 */

public class IdCardUtil {
    private static final String TAG = "IdCardUtil";

    public static final int READ = 0x01;
    public static final int NOREAD = 0x0;
    private Application mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    protected InputStream mInputStream;
    private Context mContext;
    public static String bmpPath;//读取的身份证照片位图地址
    private String wltPath;//读取的身份证照片流地址

    private ReaderSerialPort rsp;
    private MultiReader reader = MultiReader.getReader();
    private IDcardReader idreader;
    private IDCard idCard;

    private ReadCardThread ReadCardThreadhandler;

    private Bitmap bmp;
    private FileOutputStream fos;

    private boolean reading = false;

    /**
     * 构造器初始化一些参数
     *
     * @param context
     */
    public IdCardUtil(Context context, BitmapCallBack bitmapCallBack) {
        this.mContext = context;
        this.bitmapCallBack = bitmapCallBack;
        mApplication = (Application) mContext.getApplicationContext();
        bmpPath = mContext.getFileStreamPath("photo.bmp").getAbsolutePath();
        wltPath = mContext.getFileStreamPath("photo.wlt").getAbsolutePath();
        try {
            mSerialPort = mApplication.getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开阅读器
     */
    public void openIdCard() {
        Log.d(TAG, "openIdCard: " + mContext);
        rsp = new ReaderSerialPort((Activity) mContext, mOutputStream, mInputStream);
        reader.setDataTransInterface(rsp);
        idreader = new IDcardReader(reader);
        idreader.open(mContext);
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (idreader != null) {
            idreader.close();
        }
    }

    public BitmapCallBack bitmapCallBack;

    /**
     * 回调接口
     */
    public interface BitmapCallBack {
        void callBack(int a);
    }

    /**
     * 读取阅读器
     */
    public void readIdCard() {
        //开启子线程读卡
        if (reading == false) {
            reading = true;
            ReadCardThreadhandler = new ReadCardThread();
            ReadCardThreadhandler.start();
        } else {
            closeReadThread();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public IDCard getIdCard() {
        return idCard;
    }

    /**
     * 读阅读器的线程
     */
    class ReadCardThread extends Thread {
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            while (reading) {
                //发送消息出去
                idCard = idreader.getIDCardFp();
                if (idCard != null) {
                    com.synjones.bluetooth.DecodeWlt mydw = new com.synjones.bluetooth.DecodeWlt();
                    //将读出的照片数据写入文件
                    try {
                        fos = new FileOutputStream(wltPath);
                        fos.write(idCard.getWlt());

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //调用解码库：
                    //wltpath上面写入的照片数据的wlt文件
                    //bmpPath：生成的bmp文件
                    int result = mydw.Wlt2Bmp(wltPath, bmpPath);
                    //取出bitmap文件
                    bmp = BitmapFactory.decodeFile(bmpPath);
                    idCard.setPhoto(bmp);
                    bitmapCallBack.callBack(READ);
                } else {
                    bitmapCallBack.callBack(NOREAD);
                    continue;
                }

            }
        }

        public void stopRead() {
            reading = false;
            try {
                join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭读阅读器的线程
     */
    public void closeReadThread() {
        if (ReadCardThreadhandler != null) {
            ReadCardThreadhandler.stopRead();
            ReadCardThreadhandler = null;
        }
    }

    /**
     * 关闭阅读器
     */
    public void closeIdCard() {
        if (idreader != null) {
            idreader.close();
            idreader = null;
        }
    }

}
