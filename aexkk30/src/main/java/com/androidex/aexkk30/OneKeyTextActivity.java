package com.androidex.aexkk30;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.androidex.aexkk30.fragment.CameraFragment;
import com.androidex.aexkk30.fragment.RecordVoiceFragment;
import com.androidex.aexkk30.utils.NetWork;

/**
 * 一键测试
 * Created by cts on 17/6/6.
 */
public class OneKeyTextActivity extends AppCompatActivity {
    public String TAG = "OneKeyTextActivity";
    public static final String action_Viewpager_gone = "com.androidex.action.viewpager.gone";
    public static final String action_start_text = "com.androidex.action.start.text";
    public static final String action_start_wifi_text = "com.androidex.action.start.wifi.text";
    public static final String action_start_print_text = "com.androidex.action.start.print.text";
    public static final String action_start_network_text = "com.androidex.action.start.network.text";
    private NextBrodcastResive nbr;
    private static TextView tv_result;
    private static TextView tv_result_err;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onekeytext);
        initView();
        initBroadCast();
        startText();
    }

    private void initView() {
        tv_result = (TextView) findViewById(R.id.tv_result);
        tv_result_err = (TextView) findViewById(R.id.tv_result_err);

    }

    public static void setTextResult(String string, boolean flag) {
        if (!flag) {
            tv_result.append(string + "\n");
//            SpannableStringBuilder builder = new SpannableStringBuilder(tv_result.getText().toString());
//            //ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色
//            ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
//            builder.setSpan(redSpan, str.length(), result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            tv_result.setText(builder);
        }else {
            tv_result_err.append(string+"\n");
        }

    }

    public static void setTextColor(String str) {


    }

    public void initBroadCast() {
        nbr = new NextBrodcastResive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action_Viewpager_gone);
        intentFilter.addAction(action_start_text);//启动自动测试程序
        intentFilter.addAction(action_start_wifi_text);//启动wifi测试页面
        intentFilter.addAction(action_start_network_text);//启动以太网测试页面
        intentFilter.addAction(action_start_print_text);//启动打印机测试
        registerReceiver(nbr, intentFilter);
    }

    /**
     * 启动自动测试
     */
    private void startText() {
        CameraFragment.instance().show(getSupportFragmentManager(), "camerafragment");//相机测试
        // showDialog(getVedioFragments(), true);//视频播放测试程序
        // NetWork.wifiManger(this);
        //netWorkText();  //以太网测试
        //NetWork.netWorkManger(this);
        //VedioFragment.Instance().show(getSupportFragmentManager(),"recordvoicefragment");
        //printText();//打印机测试
    }

    /**
     * 实现viewpager切换Fragment的广播
     */
    private class NextBrodcastResive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case action_start_text://启动测试程序
                    Log.d(TAG, "启动测试程序");
                    startText();
                    break;

                case action_start_wifi_text://启动WIFI测试
                    NetWork.wifiManger(OneKeyTextActivity.this);
                    break;

                case action_start_network_text://启动以太网测试
                    NetWork.netWorkManger(OneKeyTextActivity.this);
                    break;

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1001://系统wifi返回键
                AlertDialog.Builder builder = new AlertDialog.Builder(OneKeyTextActivity.this);
                builder.setCancelable(false);
                builder.setMessage("wifi网络是否正常")
                        .setPositiveButton("正常", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("wifi网络", "wifi网络OK");
                                setTextResult("wifi网络OK",false);
                                NetWork.netWorkManger(OneKeyTextActivity.this);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("NG", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("wifi网络", "wifi网络失败");
                                setTextResult("wifi网络失败",true);
                                NetWork.netWorkManger(OneKeyTextActivity.this);
                                dialog.dismiss();
                            }
                        }).show();
                break;

            case 1002://以太网返回
                netWorkText();
                break;
        }
    }

    public void netWorkText() {
        boolean isCon = NetWork.isConnect(this);
        if (isCon) {
            Log.d(TAG, "以太网测试成功");
            setTextResult("以太网测试成功",false);
            RecordVoiceFragment.instance().show(getSupportFragmentManager(), "recordvoicefragment");
        } else {
            //弹出对话框
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(OneKeyTextActivity.this);
            builder.setCancelable(false);
            builder.setMessage("请确认关闭无线网并插入网线")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (NetWork.isConnect(OneKeyTextActivity.this)) {
                                Log.d(TAG, "以太网测试成功");
                                setTextResult("以太网测试成功",false);
                            } else {
                                Log.e(TAG, "以太网测试失败");
                                setTextResult("以太网测试失败",true);
                            }
                            RecordVoiceFragment.instance().show(getSupportFragmentManager(), "recordvoicefragment");
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            netWorkText();
                        }
                    }).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nbr);




    }
}
