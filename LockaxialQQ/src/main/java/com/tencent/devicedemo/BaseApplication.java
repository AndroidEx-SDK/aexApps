package com.tencent.devicedemo;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;

import com.lidroid.xutils.HttpUtils;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;

/**
 * Created by xinshuhao on 16/7/24.
 */
public class BaseApplication extends Application  implements Thread.UncaughtExceptionHandler
{
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private static BaseApplication application;
    private HttpUtils httpUtils;

    @Override
    public void onCreate()
    {
       // setStrictMode();

        application = this;

        initHttp();

        initImageLoader();

        super.onCreate();
    }

    public static BaseApplication getApplication()
    {
        return application;
    }

    public void initHttp()
    {
        httpUtils = new HttpUtils("utf-8");
        httpUtils.configRequestThreadPoolSize(5);
        httpUtils.configSoTimeout(30000);
        httpUtils.configResponseTextCharset("utf-8");
        httpUtils.configRequestRetryCount(3);
    }

    private void initImageLoader()
    {
        imageLoader = ImageLoader.getInstance();

        String cachePath = null;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            cachePath =
                    Environment.getExternalStorageDirectory().getPath()
                            + "/qqlock/cache";
        } else
        {
            cachePath = getCacheDir().getPath() + "/qqlock/cache";
        }

        File cacheFile = new File(cachePath);

        cacheFile.mkdirs();

        int memoryCacheSize = (int) Runtime.getRuntime().maxMemory() / 8;

        ImageLoaderConfiguration configuration =
                new ImageLoaderConfiguration.Builder(getApplicationContext())
                        .threadPoolSize(5)
                        .memoryCacheSize(memoryCacheSize)
                        .diskCacheFileCount(500)
                        .diskCache(new UnlimitedDiskCache(cacheFile))
                        .build();

        imageLoader.init(configuration);

        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.mipmap.bg1)
                .showImageOnLoading(R.mipmap.bg1)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build();


    }

    public HttpUtils getHttpUtils()
    {
        return this.httpUtils;
    }

    public ImageLoader getImageLoader()
    {
        return imageLoader;
    }

    public DisplayImageOptions getDisplayOptions()
    {
        return options;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setStrictMode()
    {
        if (Integer.valueOf(Build.VERSION.SDK) > 3)
        {
            // Log.d(LOG_TAG, "Enabling StrictMode policy over Sample application");
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectNetwork().penaltyLog()
                            // .penaltyDeath()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                    .penaltyLog().build());

            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                    .penaltyLog().penaltyDeath().build());
        }
    }



    // 捕获系统运行停止错误
    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
        // System.exit(0);

        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());

        PendingIntent restartIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

        // 退出程序
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, restartIntent); // 1秒钟后重启应用

        System.exit(0);

    }
}
