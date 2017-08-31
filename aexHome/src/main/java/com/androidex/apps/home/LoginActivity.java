package com.androidex.apps.home;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.compat.PackageManagerCompat;

import java.io.File;

public class LoginActivity extends AppCompatActivity {
    private Handler mHandler;
    private Runnable mRunnable;
    private File[] plugins;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InstallAPK();
        mHandler=new Handler();
        mRunnable=new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent intent = new Intent(LoginActivity.this, GridviewActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        };
        mHandler.postDelayed(mRunnable, 2000);
    }
    private void InstallAPK() {
        //获取插件,plugin为sdcard下的文件夹名称
        File file = new File(Environment.getExternalStorageDirectory(), "/plugin");
        plugins = file.listFiles();
        //没有插件
        if (plugins == null || plugins.length == 0) {
            return;
        } else {//安装插件
            //i的最大值为文件夹内apk的数量
            for (int i=0;i<2;i++){
                try {
                    PluginManager.getInstance().installPackage(plugins[i].getAbsolutePath(), PackageManagerCompat.INSTALL_REPLACE_EXISTING);
                } catch (RemoteException e) {

                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(mHandler!=null&&mRunnable!=null){
            mHandler.removeCallbacks(mRunnable);
        }
    }
}
