package com.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by cts on 17/6/21.
 */

public class InstallUtil {
    /**
     * 安装APK工具类
     * @param context       上下文
     * @param filePath      文件路径
     * @param authorities   Manifest中配置provider的authorities字段
     * @param callBack      安装界面成功调起的回调
     */
    public static void installAPK(Context context, String filePath, String authorities, InstallCallBack callBack) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            File apkFile = new File(filePath);
            if (Build.VERSION.SDK_INT >= 24) {//兼容7.0
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, authorities, apkFile);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            context.startActivity(intent);
            if (callBack != null) {
                callBack.onSuccess();
            }
        } catch (Exception e) {
            if (callBack != null) {
                callBack.onFail(e);
            }
        }
    }


    /**
     * 获取app缓存路径    SDCard/Android/data/你的应用的包名/cache
     *
     * @param context
     * @return
     */
    public String getCachePath(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            //外部存储可用
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            //外部存储不可用
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }
    public interface InstallCallBack {

        void onSuccess();

        void onFail(Exception e);
    }
}
