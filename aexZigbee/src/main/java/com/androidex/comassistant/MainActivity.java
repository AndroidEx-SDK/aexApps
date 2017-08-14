package com.androidex.comassistant;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidex.comassistant.util.*;
import com.androidex.plugins.kkserial;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static String PORT_ADDR_PASSWORD_KEYPAD_1 = "/dev/ttyMT2,38400,N,1,8";
    public static String PORT_ADDR_PASSWORD_KEYPAD_2 = "/dev/ttyHSL1,38400,N,1,8";
    public static String PORT_ADDR_PASSWORD_KEYPAD_3 = "/dev/ttyS4,38400,N,1,8";
    public static String PORT_ADDR = PORT_ADDR_PASSWORD_KEYPAD_1;
    private ScannerController controller;
    private kkserial serial;
    private TextView tv_show, tv_config, tv_type, tv_broadType;
    private Button btn_send, btn_start, btn_root, btn_type, btn_broadType, btn_setBroad1, btn_setBroad2, btn_setNet,btn_powerOn;
    private Button btn_sendbroadcast1, btn_catConfig, btn_clear, btn_bunchPlanting, btn_multicast, btn_setMulticast_target;
    private EditText et_send, et_short_adress, et_broadcast, et_Multicast;
    private ReadThread mReadThread;
    protected int mSerialFd;
    private int devices = -1;
    private ToggleButton toggleButton;
    int buitder_type_item = 0;
    int buitder_typeConfig_item = 0;
    int builder_net = 0;
    String multicast;
    String broadcast;
    String short_adress;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    DispRecData((ComBean) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        if (serial == null) {
            serial = new kkserial(this);
        }

        initView();
        getDevicesCode();

    }

    private void initStartConfig() {
        mSerialFd = serial.serial_open(PORT_ADDR);
        Log.e("xxxmSerialFd", mSerialFd + "");
        openVCC();
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

    private void getDevicesCode() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择设备");
        builder.setCancelable(false);
        builder.setSingleChoiceItems(R.array.spinner_devices, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        devices = 0;
                        PORT_ADDR = PORT_ADDR_PASSWORD_KEYPAD_1;

                        initStartConfig();//在选择完设备之后再运行
                        break;
                    case 1:
                        devices = 1;
                        PORT_ADDR = PORT_ADDR_PASSWORD_KEYPAD_2;
                        if (controller == null) {
                            controller = new ScannerController();
                        }
                        boolean jurisdiction = controller.getSerialportJurisdiction();

                        if (jurisdiction) {
                            showMessage("串口配置成功");
                            initStartConfig();//在选择完设备之后再运行
                        } else {
                            showMessage("串口配置失败");
                        }
                        break;

                    case 2:
                        devices = 2;
                        PORT_ADDR = PORT_ADDR_PASSWORD_KEYPAD_3;
                        initStartConfig();//在选择完设备之后再运行

                        break;
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void initView() {
        tv_show = (TextView) findViewById(R.id.tv_show);
        tv_type = (TextView) findViewById(R.id.tv_type);
        tv_broadType = (TextView) findViewById(R.id.tv_broadType);
        tv_config = (TextView) findViewById(R.id.tv_config);
        et_send = (EditText) findViewById(R.id.et_send);
        et_short_adress = (EditText) findViewById(R.id.et_short_adress);
        et_broadcast = (EditText) findViewById(R.id.et_broadcast);
        et_Multicast = (EditText) findViewById(R.id.et_Multicast);

        btn_send = (Button) findViewById(R.id.btn_send);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_root = (Button) findViewById(R.id.btn_root);
        btn_type = (Button) findViewById(R.id.btn_type);
        btn_broadType = (Button) findViewById(R.id.btn_broadType);
        btn_setBroad1 = (Button) findViewById(R.id.btn_setBroad1);//设置本地组播号
        btn_setBroad2 = (Button) findViewById(R.id.btn_setBroad2);//设置目标短地址
        btn_setMulticast_target = (Button) findViewById(R.id.btn_setMulticast_target);//设置目标组播号
        btn_setNet = (Button) findViewById(R.id.btn_setNet);//设置入网允许状态
        btn_sendbroadcast1 = (Button) findViewById(R.id.btn_sendbroadcast1);
        btn_bunchPlanting = (Button) findViewById(R.id.btn_bunchPlanting);
        btn_multicast = (Button) findViewById(R.id.btn_multicast);
        btn_catConfig = (Button) findViewById(R.id.btn_catConfig);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_powerOn = (Button) findViewById(R.id.btn_powerOn);

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        btn_send.setOnClickListener(this);
        btn_start.setOnClickListener(this);
        btn_root.setOnClickListener(this);
        btn_type.setOnClickListener(this);
        btn_broadType.setOnClickListener(this);
        btn_setBroad1.setOnClickListener(this);
        btn_setBroad2.setOnClickListener(this);
        btn_sendbroadcast1.setOnClickListener(this);
        btn_bunchPlanting.setOnClickListener(this);
        btn_setMulticast_target.setOnClickListener(this);
        btn_setNet.setOnClickListener(this);
        btn_powerOn.setOnClickListener(this);

        btn_multicast.setOnClickListener(this);
        btn_catConfig.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        toggleButton.setChecked(true);
        toggleButton.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());

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
                // byte[] bt = str.getBytes();
                Log.e("xxx输入的指令:", str);
                //Log.e("xxx发送的指令:", bt + "");

                serial.serial_writeHex(mSerialFd, str);
                // serial.serial_writeHex(mSerialFd, "5aaa0b0105");
//              String serial_readHex = serial.serial_readHex(mSerialFd, 20, 3 * 1000);
//              Log.e("xxx读取到的数据:", serial_readHex + "");
                break;
            case R.id.btn_start:
                contral("00");
                break;
            case R.id.btn_root:
                contral("01");
                break;

            case R.id.btn_clear:
                tv_show.setText("接收到的指令：\n");

                break;
            case R.id.btn_powerOn:
                closeVCC();
                openVCC();

                break;

            case R.id.btn_type://节点类型
                final AlertDialog.Builder builder_type = new AlertDialog.Builder(this);
                builder_type.setTitle("请选择节点类型");
                builder_type.setCancelable(true);

                builder_type.setSingleChoiceItems(R.array.spinner_value1, buitder_type_item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        buitder_type_item = i;
                        switch (i) {
                            case 0:
                                showMessage("选择了协调器");
                                serial.serial_writeHex(mSerialFd, "5aaa010100");
                                tv_type.setText("协调器");
                                break;
                            case 1:
                                showMessage("选择了路由器");
                                serial.serial_writeHex(mSerialFd, "5aaa010101");
                                tv_type.setText("路由器");
                                break;

                            case 2:
                                showMessage("选择了终端");
                                serial.serial_writeHex(mSerialFd, "5aaa010102");
                                tv_type.setText("终端");
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder_type.show();
                break;
            case R.id.btn_broadType:
                final AlertDialog.Builder builder_broadType = new AlertDialog.Builder(this);
                builder_broadType.setTitle("请选择透传方式类型");
                builder_broadType.setCancelable(true);
                builder_broadType.setSingleChoiceItems(R.array.spinner_BroadCast, buitder_typeConfig_item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        buitder_typeConfig_item = i;
                        switch (i) {
                            case 0:
                                showMessage("选择了广播");
                                serial.serial_writeHex(mSerialFd, "5aaa090100");//广播
                                tv_broadType.setText("广播");
                                break;

                            case 1:
                                showMessage("选择了点播");
                                serial.serial_writeHex(mSerialFd, "5aaa090101");//点播
                                tv_broadType.setText("点播");
                                break;

                            case 2:
                                showMessage("选择了组播");
                                serial.serial_writeHex(mSerialFd, "5aaa090102");//组播
                                tv_broadType.setText("组播");
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder_broadType.show();
                break;

            case R.id.btn_setBroad1://设置本地组播号
                multicast = et_Multicast.getText().toString().trim();
                if (multicast.length() == 4) {
                    serial.serial_writeHex(mSerialFd, "5aaa0602" + multicast);//
                } else {
                    showMessage("本地组播号必须为4位字符");
                }
                break;

            case R.id.btn_setBroad2://设置目标短地址
                short_adress = et_short_adress.getText().toString().trim();
                if (short_adress.length() == 4) {
                    serial.serial_writeHex(mSerialFd, "5aaa0702" + short_adress);//
                } else {
                    showMessage("目标短地址必须为4位字符");
                }
                break;

            case R.id.btn_setMulticast_target://设置目标组播号

                multicast = et_Multicast.getText().toString().trim();
                if (multicast.length() == 4) {
                    serial.serial_writeHex(mSerialFd, "5aaa0802" + multicast);//
                } else {
                    showMessage("目标组播号必须为4位字符");
                }

                break;


            case R.id.btn_setNet://设置入网允许状态

                final AlertDialog.Builder builder_setNet = new AlertDialog.Builder(this);
                builder_setNet.setTitle("请设置入网允许状态");
                builder_setNet.setCancelable(true);

                builder_setNet.setSingleChoiceItems(R.array.spinner_setNet, builder_net, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        builder_net = i;
                        switch (i) {
                            case 0:
                                showMessage("设备上电允许加入,连上往后禁止加入");
                                serial.serial_writeHex(mSerialFd, "5aaa0b0100");

                                break;
                            case 1:
                                showMessage("设备上电允许加入,连上网后一直允许加入");
                                serial.serial_writeHex(mSerialFd, "5aaa0b0105");
                                tv_type.setText("路由器");
                                break;

                            case 2:
                                showMessage("设备上电允许加入,连上网后所有路由设备禁止加入");
                                serial.serial_writeHex(mSerialFd, "5aaa0b0106");
                                tv_type.setText("终端");
                                break;
                            case 3:
                                showMessage("设备上电允许加入,所有路由设备允许加入 10s");
                                serial.serial_writeHex(mSerialFd, "5aaa0b0107");
                                break;
                            case 4:
                                showMessage("设备上电允许加入,所有路由设备允许加入 20s");
                                serial.serial_writeHex(mSerialFd, "5aaa0b0108");
                                break;
                            case 5:
                                showMessage("设备上电不允许加入,连上往后禁止加入");
                                serial.serial_writeHex(mSerialFd, "5aaa0b0110");
                                break;
                            case 6:
                                showMessage("设备上电不允许加入,连上网后一直允许加入");
                                serial.serial_writeHex(mSerialFd, "5aaa0b0115");
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder_setNet.show();

                break;
            case R.id.btn_sendbroadcast1://发送广播
                broadcast = et_broadcast.getText().toString().trim();
                if (broadcast.length() > 0) {

                    showMessage("发送广播内容为：" + "5aaaa101" + broadcast);

                } else {
                    if (devices == 0) {
                        serial.serial_writeHex(mSerialFd, "5aaaa10101");//
                        showMessage("发送广播默认内容为：" + "5aaaa10101");
                    } else if (devices == 1) {

                        serial.serial_writeHex(mSerialFd, "5aaaa10102");//
                        showMessage("发送广播默认内容为：" + "5aaaa10102");
                    } else {
                        serial.serial_writeHex(mSerialFd, "5aaaa10103");//
                        showMessage("发送广播默认内容为：" + "5aaaa10103");
                    }
                }

                break;
            case R.id.btn_bunchPlanting://发送点播

                short_adress = et_short_adress.getText().toString().trim();
                broadcast = et_broadcast.getText().toString().trim();

                if (short_adress.length() < 3) {
                    showMessage("目标短地址错误请重新输入");
                    return;
                }
                if (broadcast.length() > 0) {
                    serial.serial_writeHex(mSerialFd, "5aaaa204" + short_adress + broadcast);
                    showMessage("发送点播内容为：" + "5aaaa204" + short_adress + broadcast);

                } else {
                    if (devices == 0) {
                        serial.serial_writeHex(mSerialFd, "5aaaa203" + short_adress + "01");//
                        showMessage("发送点播默认内容为：" + "5aaaa203" + short_adress + "01");
                    } else if (devices == 1) {

                        serial.serial_writeHex(mSerialFd, "5aaaa203" + short_adress + "02");//
                        showMessage("发送点播默认内容为：" + "5aaaa203" + short_adress + "02");
                    } else {
                        serial.serial_writeHex(mSerialFd, "5aaaa203" + short_adress + "03");//
                        showMessage("发送点播默认内容为：" + "5aaaa203" + short_adress + "03");
                    }
                }


                break;
            case R.id.btn_multicast://发送组播
                multicast = et_Multicast.getText().toString().trim();
                broadcast = et_broadcast.getText().toString().trim();
                if (multicast.length() <= 2) {
                    showMessage("目标组播号错误，请重新输入");
                    return;
                }
                if (broadcast.length() > 0) {
                    serial.serial_writeHex(mSerialFd, "5aaaa304" + multicast + broadcast);//
                    showMessage("发送组播播内容为：" + "5aaaa204" + multicast + broadcast);

                } else {
                    if (devices == 0) {
                        serial.serial_writeHex(mSerialFd, "5aaaa303" + multicast + "01");//
                        showMessage("发送组播默认内容为：" + "5aaaa303" + multicast + "01");
                    } else if (devices == 1) {

                        serial.serial_writeHex(mSerialFd, "5aaaa303" + multicast + "02");//
                        showMessage("发送组播默认内容为：" + "5aaaa303" + multicast + "02");
                    } else {
                        serial.serial_writeHex(mSerialFd, "5aaaa303" + multicast + "03");//
                        showMessage("发送组播默认内容为：" + "5aaaa303" + multicast + "03");
                    }
                }

                break;
            case R.id.btn_catConfig://查看参数

                final AlertDialog.Builder builder_catConfig = new AlertDialog.Builder(this);
                builder_catConfig.setTitle("请选择需要查看的参数");
                builder_catConfig.setCancelable(true);
                builder_catConfig.setSingleChoiceItems(R.array.spinner_config, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i) {
                            case 0:
                                showMessage("节点类型");
                                serial.serial_writeHex(mSerialFd, "5aaab1");
                                break;
                            case 1:
                                showMessage("PAN_ID");
                                serial.serial_writeHex(mSerialFd, "5aaab2");
                                break;

                            case 2:
                                showMessage("Channel");
                                serial.serial_writeHex(mSerialFd, "5aaab3");
                                break;
                            case 3:
                                showMessage("波特率");
                                serial.serial_writeHex(mSerialFd, "5aaab4");
                                break;
                            case 4:
                                showMessage("发射功率");
                                serial.serial_writeHex(mSerialFd, "5aaab5");
                                break;
                            case 5:
                                showMessage("本地组播号");
                                serial.serial_writeHex(mSerialFd, "5aaab6");
                                break;
                            case 6:
                                showMessage("目标短地址");
                                serial.serial_writeHex(mSerialFd, "5aaab7");
                                break;
                            case 7:
                                showMessage("目标组播号");
                                serial.serial_writeHex(mSerialFd, "5aaab8");
                                break;
                            case 8:
                                showMessage("全透传发送方式");
                                serial.serial_writeHex(mSerialFd, "5aaab9");
                                break;
                            case 9:
                                showMessage("MAC地址");
                                serial.serial_writeHex(mSerialFd, "5aaaba");
                                break;
                            case 10:
                                showMessage("本地短地址");
                                serial.serial_writeHex(mSerialFd, "5aaabb");
                                break;
                            case 11:
                                showMessage("网络密匙");
                                serial.serial_writeHex(mSerialFd, "5aaabc");
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder_catConfig.show();

                break;
        }
    }

    public void contral(String str) {
        switch (str) {
            case "00"://模块重启,参数不变
                serial.serial_writeHex(mSerialFd, "5aaa0001" + str);

                break;
            case "01"://Data 为模块恢复出厂设置
                serial.serial_writeHex(mSerialFd, "5aaa0001" + str);
                tv_broadType.setText("点播");
                tv_type.setText("路由器");
                buitder_type_item = 1;
                buitder_typeConfig_item = 1;
                showMessage("恢复出厂设置");
                break;
            case "02"://模块清除保存在本地的网络信息
                serial.serial_writeHex(mSerialFd, "5aaa0001" + str);
                break;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeVCC();
        mSerialFd = 0;
    }

    /**
     * 打开VCC
     */
    public void openVCC() {
        if (devices == 0) {
            if (controller == null) {
                controller = new ScannerController();
            }
            controller.initGPIO();
            controller.openScanner();
            showMessage("打开设备1VCC");
        } else if (devices == 1) {
            if (controller == null) {
                controller = new ScannerController();
            }
            boolean b = controller.openVCC2();
            if (b) {
                showMessage("设备2VCC打开成功");
            } else {
                showMessage("设备2VCC打开失败");
            }
        } else if (devices == 2) {


        } else {
            showMessage("未找到设备");
            Log.e("xxx", "未找到设备");
        }
    }

    /**
     * 关闭VCC
     */
    public void closeVCC() {
        if (controller != null) {
            if (devices == 0) {
                controller.close();
            } else if (devices == 1) {
                controller.closeVCC2();
            }
        }
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

                        Log.e("xxxPORT_ADDR波特率:", PORT_ADDR);
                        Message message = handler.obtainMessage();
                        message.what = 0x01;
                        message.obj = ComRecData;
                        handler.sendMessage(message);

                        Log.e("xxx读取到的数据:", data + "");
                    }
                    try {
                        Thread.sleep(50);//延时50ms
                    } catch (InterruptedException e) {
                        Log.e("mReadThread", "xxx线程开启8");
                        e.printStackTrace();
                    }
                } catch (Throwable e) {
                    Log.e("mReadThread", "xxx线程开启7");
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    class ToggleButtonCheckedChangeEvent implements ToggleButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.toggleButton:
                    if (toggleButton.isChecked()) {
                        mSerialFd = serial.serial_open(PORT_ADDR);
                        Log.e("xxxmSerialFd", mSerialFd + "");
                        if (mSerialFd > 0) {
                            showMessage("xxxtoggleButton:串口已打开");
                            toggleButton.setSelected(true);
                            if (mReadThread == null) {
                                mReadThread = new ReadThread();
                                mReadThread.start();
                            }
                        } else {
                            showMessage("xxxtoggleButton:串口打开失败");
                            toggleButton.setChecked(false);
                            serial.serial_close(mSerialFd);
                        }
                    } else {
                        showMessage("串口已关闭");
                        serial.serial_close(mSerialFd);
                        mSerialFd = 0;
                        toggleButton.setSelected(false);
                    }
                    break;
            }
        }
    }

    public void showMessage(String str) {
        if (str != null) {
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            Log.e("xxxToast", str);
        } else {
            Log.e("xxxToast", "Toast is not null");
        }

    }
}
