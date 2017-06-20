package com.androidex.textairkiss;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.wechat.Cloud;

public class MainActivity extends AppCompatActivity {
    public static final String license="7784000A2F57C7B86C7915A035B169B54682188C9B7233873E577DEA1A529C8B3FD85D7A7D06EE04690A8B00B117AD87BF8AC4C4D06BC993018E66A40D9401B2314DBB4BD895B81390B54CA3306A6E29";
    static {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("wxcloud");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean init = Cloud.init(license);
        showToast(init+"");

        showToast(Cloud.getSDKVersion());

    }
    public void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        Log.e("MainActivity", "===" + str);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Cloud.release();
    }
}
