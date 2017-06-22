package com.androidex.textairkiss;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidex.common.AndroidExActivityBase;
import com.tencent.wechat.Cloud;

public class MainActivity extends AndroidExActivityBase {
    public static final String license = "7784000A2F57C7B86C7915A035B169B54682188C9B7233873E577DEA1A529C8B3FD85D7A7D06EE04690A8B00B117AD87BF8AC4C4D06BC993018E66A40D9401B2314DBB4BD895B81390B54CA3306A6E29";

    static {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("wxcloud");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MyAccessibilityService.class);
        startService(intent);

        findViewById(R.id.tv_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Uri uri = Uri.fromFile(new File("/sdcard/DCIM/ComAssistant.apk"));
//                Intent localIntent = new Intent(Intent.ACTION_VIEW);
//                localIntent.setDataAndType(uri, "application/vnd.android.package-archive");
//                startActivity(localIntent);
//                android.util.Log.e(TAG, "BroadcastReceive：UpdateService");

               // onForwardToAccessibility(view);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        hwservice.execRootCommand("pm -r install /sdcard/DCIM/ComAssistant.apk");
                        android.util.Log.e(TAG, "BroadcastReceive1：UpdateService");
                    }
                }).start();
                android.util.Log.e(TAG, "BroadcastReceive2：UpdateService");
            }
        });
//        boolean init = Cloud.init(license);
//        showToast(init+"");
//        showToast(Cloud.getSDKVersion());
        android.util.Log.e(TAG, "BroadcastReceive3：UpdateService");
    }

    public void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        Log.e("MainActivity", "===" + str);

    }

    public void onForwardToAccessibility(View view) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Cloud.release();
    }
}
