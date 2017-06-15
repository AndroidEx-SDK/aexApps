package com.androidex.comassistant;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.comassistant.util.ComBean;
import com.androidex.plugins.kkserial;

/**
 * T4指纹仪调试类
 * Created by cts on 17/5/15.
 */

public class T4MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static String PORT_ADDR = "/dev/ttyS4,19200,N,1,8";
    private kkserial serial;
    private ReadThread mReadThread;
    protected int mSerialFd;
    public EditText et_send;
    private Button btn_send;
    private Button btn_clear;
    public TextView tv_show;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    DispRecData((ComBean) msg.obj);

                    Log.e("=====Handler", "Handler");
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t4main);
        if (serial == null) {
            serial = new kkserial(this);
        }

        initView();
        initStartConfig();
    }


    private void initView() {
        tv_show = (TextView) findViewById(R.id.tv_show);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        et_send = (EditText) findViewById(R.id.et_send);
        btn_send.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mSerialFd <= 0) {
            Toast.makeText(this, "请先打开串口", Toast.LENGTH_LONG).show();
            Log.e("xxx串口未打开", "");
            return;
        }
        switch (v.getId()) {
            case R.id.btn_send:
                String str = et_send.getText().toString().trim();
                showMessage("发送的指令为："+str);
                serial.serial_writeHex(mSerialFd, str);
                showMessage("发送的指令为："+str);
                break;
            case R.id.btn_clear:

                tv_show.setText("接收到的指令：\n");
                break;
        }
    }

    private void initStartConfig() {
        mSerialFd = serial.serial_open(PORT_ADDR);
        Log.e("xxxmSerialFd", mSerialFd + "");

        if (mSerialFd > 0) {
            Log.e("MainActivity", "xxx串口打开成功");
            showMessage("串口打开成功");
        } else {
            Log.e("MainActivity", "xxx串口打开失败");
            showMessage("串口打开失败");
            return;
        }
        if (mReadThread == null) {
            mReadThread = new ReadThread();
            mReadThread.start();
        }
    }

    public void DispRecData(ComBean ComRecData) {
        StringBuilder sMsg = new StringBuilder();
        sMsg.append(ComRecData.sRecTime);
        sMsg.append("[");
        sMsg.append(ComRecData.sComPort);
        sMsg.append("]");

        sMsg.append("[Hex] ");
        sMsg.append(MyFunc.ByteArrToHex(ComRecData.bRec));

        sMsg.append("\r\n");
        tv_show.append(sMsg);
        Log.e("xxx显示数据：", sMsg.toString());
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    byte[] data = serial.serial_read(mSerialFd, 20, 3 * 1000);
                    // String data = serial.native_serial_readHex(mSerialFd, 20, 3 * 1000);
                    if (data == null) continue;
                    if (data.length > 0) {
                        ComBean ComRecData = new ComBean(PORT_ADDR, data, data.length);
                        Message message = handler.obtainMessage();
                        message.what = 0x01;
                        message.obj = ComRecData;
                        handler.sendMessage(message);
                    }
                    try {
                        Thread.sleep(50);//延时50ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public void showMessage(String str) {
        if (str != null) {
            Toast.makeText(T4MainActivity.this, str, Toast.LENGTH_SHORT).show();
            Log.e("xxxToast", str);
        } else {
            Log.e("xxxToast", "Toast is not null");
        }
    }
}
