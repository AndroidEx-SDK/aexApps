package com.tencent.devicedemo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by xinshuhao on 16/7/27.
 */
public class DissmissDialog  extends AlertDialog {
    private int FLAG_DISMISS = 1;
    private boolean flag = true;
    private String message;
    private Context context;
    protected DissmissDialog(Context context, int theme,String mes) {
        super(context, theme);
        this.message=mes;
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dissmissdiglog);
        TextView textView=(TextView)findViewById(R.id.dissmiss_dia_message);
        textView.setText(message);
    }

    @Override
    public void show() {
        super.show();
        mThread.start();
    }


    @Override
    public void dismiss() {
        super.dismiss();
        flag = false;
    }

    private Thread mThread = new Thread(){
        @Override
        public void run() {
            super.run();
            while(flag){
                try {
                    Thread.sleep(2000);
                    Message msg = mHandler.obtainMessage();
                    msg.what = FLAG_DISMISS;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == FLAG_DISMISS)
                dismiss();
        }

    };

}
