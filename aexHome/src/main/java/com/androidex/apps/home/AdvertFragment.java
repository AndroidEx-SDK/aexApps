package com.androidex.apps.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.apps.home.utils.AssetsUtil;
import com.androidex.apps.home.utils.MacUtil;
import com.androidex.apps.home.utils.RebutSystem;
import com.androidex.apps.home.view.CircleTextProgressbar;
import com.androidex.common.IniReader;
import com.androidex.common.OnMultClickListener;
import com.androidex.logger.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.androidex.apps.home.utils.AssetsUtil.getBitmapFromFile;

public class AdvertFragment extends Fragment implements OnMultClickListener {
    public View psetview = null;
    //统一文件存储路径便于管理
    public String advertPath = "/mnt/sdcard/advertpic/";
    public String ukeyPath = "/mnt/usbhost1/";
    public String sdcardPath = "advertpic";
    public String iniNmae = "advert.ini";
    public String configname = "/mnt/sdcard/advertpic/advert.ini";
    public String defaultImagePath = "imagelist/a1.jpg";
    public static int ONCLICKTIMES = 5;
    public static final String TAG = "AssetsUtil";
    public static final String NULL_PATH = "/mnt/sdcard/advertpic/null";
    private static String IMAGEPATH_NAME = "imagelist/a%d.jpg";

    Bitmap bm = null;
    public ImageView iview;
    private CircleTextProgressbar progressbar;
    private TextView tv_ip;
    private TextView tv_sdkVersion;
    private TextView tv_start_num;
    private TextView tv_maturing;
    private FullscreenActivity activity;
    public String[] result = new String[256];
    public int advertnum = 0;
    public int nSeconds = 5;
    public int count = 0;
    public int imagename = 1;        //默认广告图片名字的序号
    public int nPicCount = 0;
    Handler handler = new Handler();
    Runnable runnable;
    SDcardLinsenerReceiver receiver;

    public CircleTextProgressbar.OnCountdownProgressListener progressListener = new CircleTextProgressbar.OnCountdownProgressListener() {
        @Override
        public void onProgress(int what, int progress) {
            if (what == 2) {
                progressbar.setText(progress + "s");
            }
            if (progress == 0) {
                //activity.delayedHide(1000);
                Intent intent = new Intent(FullscreenActivity.ActionControlBar);
                Intent intent_gone = new Intent(FullscreenActivity.action_Viewpager_gone);
                intent.putExtra("flag", "hide");
                intent.putExtra("bar", true);
                startPlayPic();
                psetview.getContext().sendBroadcast(intent);
                psetview.getContext().sendBroadcast(intent_gone);
                progressbar.setText("30s");
            }
        }
    };


    public AdvertFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //LinearLayout.LayoutParams wvParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        if (psetview == null) {
            psetview = inflater.inflate(R.layout.advert_main, null);
        }
        activity = (FullscreenActivity) getActivity();
        progressbar = (CircleTextProgressbar) psetview.findViewById(R.id.progressbar);
        tv_ip = (TextView) psetview.findViewById(R.id.tv_ip);
        tv_sdkVersion = (TextView) psetview.findViewById(R.id.tv_sdkversion);
        tv_start_num = (TextView) psetview.findViewById(R.id.tv_start_num);
        tv_maturing = (TextView) psetview.findViewById(R.id.tv_maturing);
        progressbar.setCountdownProgressListener(2, progressListener);
        progressbar.setTimeMillis(30 * 1000);
        progressbar.reStart();
        FullscreenActivity.registerMultClickListener(psetview, this);
        tv_ip.setText(MacUtil.getHostIP());
        tv_sdkVersion.setText(activity.hwservice.getSdkVersion());
        try {
            JSONObject jsonObject = new JSONObject(activity.hwservice.getUserInfo());
            String string = jsonObject.optString("times");
            String startTime = jsonObject.optString(RebutSystem.startTime);
            String endTime = jsonObject.optString(RebutSystem.endTime);
            if ("".equals(string)) {
                string = "1";
            }
            tv_start_num.setText(String.format("开机次数:s%",string));
            if (!"".equals(startTime) && !"".equals(endTime)) {
                tv_maturing.setText(String.format("测试时长:s%",RebutSystem.getTextHours(startTime, endTime)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return psetview;
    }

    @Override
    public void onDestroyView() {
        if (psetview != null) {
            ViewGroup parentViewGroup = (ViewGroup) (psetview.getParent());
            if (null != parentViewGroup) {
                parentViewGroup.removeView(psetview);
            }
        }
        super.onDestroyView();
    }

    /**
     * 获得屏幕显示的参数，返回宽度x高度字符串。程序可以利用这个参数根据屏幕分辨率选择播放不同的图片。
     *
     * @return 宽度x高度 字符串
     */
    public String getDisplayInfo() {
            /*WindowManager wm = (WindowManager) this.getActivity().getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
			int height = wm.getDefaultDisplay().getHeight();
*/
        WindowManager manager = this.getActivity().getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;

        return String.format("%dx%d", width, height);
    }

    public void startPlayPic() {
        //创建广告播放存储配置和图片的根目录
        File ff = new File(advertPath);
        if (!ff.exists()) {
            Log.i("adplay", String.format("File %s not exists,Create it now.", advertPath));
            if (!ff.mkdirs()) {
                Log.e("adplay", String.format("Create %s fail.", advertPath));
            }
        }

        //获取图片控件
        iview = (ImageView) psetview.findViewById(R.id.picsw);

        //全屏显示
        iview.setScaleType(ImageView.ScaleType.FIT_XY);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        receiver = new SDcardLinsenerReceiver();
        psetview.getContext().registerReceiver(receiver, filter);
        //拷贝配置文件
        String srcName = getSDPath() + "/" + iniNmae;
        String dstName = advertPath + iniNmae;
        copyFile(srcName, dstName);
        //showToast("开始读取配置!");
        //读取配置
        if (readfile() != null) serachFiles(advertPath);
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    //依次显示图片序列
                    showPic();
                    //定时刷新
                    handler.postDelayed(this, nSeconds * 1000);
                }
            };
            handler.postDelayed(runnable, 1000); //开始定时器
        }
    }

    public void showPic() {
        // 防止内存泄露
        if (bm != null) {
            bm.recycle();
        }
        //serachFiles(advertPath);
        if (count >= advertnum) {
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

        //判断文件名字是否为空,为空就从assets中读取图片
        if (filename.equals(NULL_PATH)) {
            if (imagename > 0 && imagename < 8) {
                filename = String.format(IMAGEPATH_NAME, imagename);
                android.util.Log.e("filename=====", filename);
                imagename++;
            } else if (imagename == 8) {
                filename = String.format(IMAGEPATH_NAME, imagename);
                imagename = 1;
            } else {
                filename = defaultImagePath;
            }
            bm = AssetsUtil.getImgFromAssets(getContext(), filename);
        } else {
            //横屏版本
            bm = getBitmapFromFile(filename, 1920, 1080);
            //竖屏版本
            //bm = getBitmapFromFile(filename, 1080,1920);
            count++;
        }
        //获取图片源
        iview.setImageBitmap(bm);
        //设置透明
        //iview.setAlpha(255);
    }

    public void serachFiles(String dir) {
        advertnum = 0;
        int number = 0;
        File root = new File(dir); //输入文件夹路径
        File[] filesOrDirs = root.listFiles(); //获取该文件夹下的所有文件（夹）
        if (filesOrDirs != null) {
            for (int i = 0; i < filesOrDirs.length; i++) {
                if (!filesOrDirs[i].isDirectory()) {//如果不是文件夹,说明是文件
                    String strPicName = filesOrDirs[i].getName();
                    android.util.Log.e("strPicName", strPicName);
                    if (strPicName.indexOf(".jpg") > 0) {
                        result[number] = strPicName; //把文件名存储在String[]中
                        advertnum++;
                        number++;
                    }
                }
            }
        }
    }

    public void checkfile() {
        //停止定时器
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        //删除之前文件
        delAllFile(advertPath);
        String fileName = ukeyPath + sdcardPath;
        copyFolder(fileName, advertPath);
        String strTip = "复制U盘中文件成功 "
                + nPicCount;
        showToast(strTip);
        showToast("请拔出U盘!");
        //读取配置
        if (readfile() != null) serachFiles(advertPath);
        //开始定时器
        handler.postDelayed(runnable, 1000);
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {     //如果SD卡存在，则获取跟目录
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    public void showToast(String string) {
        Toast.makeText(psetview.getContext().getApplicationContext(), string, Toast.LENGTH_SHORT).show();
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
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            System.out.println("删除文件夹操作出错");
            e.printStackTrace();
        }
    }

    @Override
    public boolean OnMultClick(View view, int times) {
        progressbar.setTimeMillis(30 * 1000);
        progressbar.reStart();
        if (times == ONCLICKTIMES) {
            Intent intent = new Intent(FullscreenActivity.ActionControlBar);
            intent.putExtra("flag", "toggle");
            intent.putExtra("bar", true);
            psetview.getContext().sendBroadcast(intent);
            //showToast("发送广播");

            handler.removeCallbacks(runnable);
            runnable = null;
            return true;
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (psetview.getContext() != null) {
            psetview.getContext().unregisterReceiver(receiver);
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
                checkfile();
            } else if (intent.getAction().equals("android.intent.action.MEDIA_REMOVED")) {
                Toast.makeText(psetview.getContext().getApplicationContext(), "U盘移出", Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals("android.intent.action.MEDIA_UNMOUNTED")) {
                //Toast.makeText(getApplicationContext(), "U盘插入异常！",Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals("android.intent.action.MEDIA_BAD_REMOVAL")) {
                //Toast.makeText(getApplicationContext(), "U盘移出异常！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //读取配置
    public String readfile() {
        try {
            IniReader ini = new IniReader(configname);
            String strSeconds = ini.getValue("config", "seconds");
            System.out.println(strSeconds);
            //showToast("读取配置中设置时间间隔秒数为" + strSeconds);
            nSeconds = Integer.parseInt(strSeconds);
            if (nSeconds <= 0) {
                nSeconds = 5;
            }
            return "1";
        } catch (IOException e1) {
            //showToast("默认配置时间间隔秒数为3!");
            android.util.Log.d("readfile()方法中", " 读不到文件:" + configname);
            return null;
        } finally {

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
        FileInputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (!oldfile.exists()) { //文件不存在时
                inStream = new FileInputStream(oldPath); //读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");

        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (fs != null) {
                    fs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
