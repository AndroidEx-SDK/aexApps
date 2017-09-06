package com.androidex.apps.aexAdvert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidex.common.DummyContent;
import com.androidex.common.IniReader;
import com.androidex.logger.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final String ARG_ITEM_ID = "item_id";
    public static final String TAG = "Advert";
    DummyContent.DummyItem mItem;

    //统一文件存储路径便于管理
    public String advertPath = "/mnt/sdcard/advertpic/";
    String localPath = "file:///android_asset/photo/";
    public String ukeyPath = "/storage/uhost1";
    public String sdcardPath = "advertpic";
    public String iniNmae = "advert.ini";
    public String configname = "/mnt/sdcard/advertpic/advert.ini";
    Bitmap bm = null;
    public ImageView iview;//   图片控件
    public String[] result;
    public int advertnum = 0;
    public int nSeconds = 3;
    public int count = 0;
    public int nPicCount = 0;

    SDcardLinsenerReceiver receiver;
    Handler handler = new Handler();

    public MainActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advert_main);
        RegisterRecerver();//注册广播
        createFolder(ukeyPath);
        startPlayPic();
    }

    public void showDialog(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("确认")
                .setMessage(msg)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    public void startPlayPic() {
        //创建广告播放存储配置和图片的根目录
        File ff = new File(advertPath);
        if (!ff.exists()) {
            Log.i(TAG, String.format("File %s not exists,Create it now.", advertPath));
            if (!ff.mkdirs()) {
                Log.e(TAG, String.format("Create %s fail.", advertPath));
            }
        }

        result = new String[256];
        //拷贝配置文件
        String srcName = getSDPath() + "/" + iniNmae;
        String dstName = advertPath + iniNmae;
        copyFile(srcName, dstName);
        showToast("开始读取配置!");
        //读取配置
        readfile();
        //查找图片
        serachFiles(advertPath);
        //开始定时器
        handler.postDelayed(runnable, 3000);
    }

    private void RegisterRecerver() {
        //获取图片控件
        iview = (ImageView) findViewById(R.id.picsw);
        showToast("开始读取配置11111");
        //全屏显示
        //iview.setScaleType(ImageView.ScaleType.FIT_XY);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        showToast("开始读取配置222222");
        receiver = new SDcardLinsenerReceiver();
        registerReceiver(receiver, filter);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //依次显示图片序列
            showPic();
            //定时刷新
            handler.postDelayed(this, nSeconds * 1000);
        }
    };

    public void showPic() {
        // 防止内存泄露
        if (bm != null) {
            bm.recycle();
        }
        //serachFiles(advertPath);
        if (count >= advertnum || count < 0) {
            count = 0;
        }
        String filename = null;
        for (int i = count; i <= advertnum; i++) {
            filename = advertPath + result[count];
            if (filename.indexOf(".jpg") > 0) {
                break;
            }
            count++;
        }
        //获取图片源
        WindowManager wm = this.getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();        //横屏版本
        //bm = getBitmapFromFile(filename, 1920,1080);
        //竖屏版本
        //bm = getBitmapFromFile(filename, 1080,1920);
        bm = getBitmapFromFile(filename, width, height);
        iview.setImageBitmap(bm);
        count++;
        //设置透明
        //iview.setAlpha(255);
    }

    private Bitmap getBitmapFromFile(String dst, int width, int height) {
        // TODO Auto-generated method stub
        if (null != dst) {
            BitmapFactory.Options opts = null;
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst, opts);
                // 计算图片缩放比例
                final int minSideLength = Math.min(width, height);
                opts.inSampleSize = computeSampleSize(opts, minSideLength,
                        width * height);
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;
            }
            try {
                return BitmapFactory.decodeFile(dst, opts);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int computeSampleSize(BitmapFactory.Options options,
                                 int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public void serachFiles(String dir) {
        advertnum = 0;
        int number = 0;
        File root = new File(dir); //输入文件夹路径
        File[] filesOrDirs = root.listFiles(); //获取该文件夹下的所有文件（夹）
        for (int i = 0; i < filesOrDirs.length; i++) {
            if (!filesOrDirs[i].isDirectory()) {//如果不是文件夹,说明是文件
                String strPicName = filesOrDirs[i].getName();
                if (strPicName.indexOf(".jpg") > 0) {
                    result[number] = strPicName; //把文件名存储在String[]中
                    advertnum++;
                    number++;
                }
            }
        }
    }

    public void checkfile(String pathString) {
        //停止定时器
        handler.removeCallbacks(runnable);
        //删除之前文件
        delAllFile(advertPath);
        String fileName = pathString + sdcardPath;
        copyFolder(fileName, advertPath);
        String strTip = "复制U盘中文件成功 " + nPicCount;
        showToast(strTip);
        showToast("请拔出U盘!");
        //读取配置
        readfile();
        //搜索图片
        serachFiles(advertPath);
        //开始定时器
        handler.postDelayed(runnable, 5000);
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist)      //如果SD卡存在，则获取跟目录
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    public void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
        android.util.Log.e("MainActivity", "====="+string);
    }

    public void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
            }
        }
    }

    public void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            System.out.println("删除文件夹操作出错");
            e.printStackTrace();
        }
    }

    /**
     * ** 监听U盘插拔
     */
    private class SDcardLinsenerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.MEDIA_MOUNTED")) {
                showToast("U盘插入");
                String path = intent.getDataString();
                String pathString = path.split("file://")[1];
                showToast(pathString);
                checkfile(pathString);
            } else if (intent.getAction().equals("android.intent.action.MEDIA_REMOVED")) {
                showToast("U盘移出");
            } else if (intent.getAction().equals("android.intent.action.MEDIA_UNMOUNTED")) {
                showToast("U盘插入异常！");
                //Toast.makeText(getApplicationContext(), "U盘插入异常！",Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals("android.intent.action.MEDIA_BAD_REMOVAL")) {
                showToast("U盘移出异常！");
                //Toast.makeText(getApplicationContext(), "U盘移出异常！",Toast.LENGTH_SHORT).show();
            } else {
                showToast("开始读取配置444444");
            }
        }
    }

    //读取配置
    public void readfile() {
        try {
            IniReader ini = new IniReader(configname);
            String strSeconds = ini.getValue("config", "seconds");
            System.out.println(strSeconds);
            showToast("读取配置中设置时间间隔秒数为" + strSeconds);
            nSeconds = Integer.parseInt(strSeconds);
            if (nSeconds <= 0) {
                nSeconds = 3;
            }
        } catch (IOException e1) {
            showToast("默认配置时间间隔秒数为3!");
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (!oldfile.exists()) { //文件不存在时
                FileInputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    //System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        }
    }

    public void createFolder(String path) {
        File ffp = new File(String.format("%s/test", path));
        if (!ffp.exists()) {
            //路径不存在创建之
            System.out.printf("%s does not exist ,try to create \r\n", path);
            ffp.mkdirs();
        }
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public void copyFolder(String oldPath, String newPath) {
        //初始化拷贝个数
        nPicCount = 0;
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    nPicCount++;
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }

    }
}
