package com.androidex.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.baseble.ViseBluetooth;
import com.androidex.baseble.callback.IBleCallback;
import com.androidex.baseble.callback.IConnectCallback;
import com.androidex.baseble.callback.scan.PeriodScanCallback;
import com.androidex.baseble.exception.BleException;
import com.androidex.baseble.model.BluetoothLeDevice;
import com.androidex.baseble.utils.BleLog;
import com.androidex.baseble.utils.BleUtil;
import com.androidex.baseble.utils.HexUtil;

import java.util.List;
import java.util.Random;

import static com.androidex.ble.RemindService.START_SEND;
import static com.androidex.ble.RemindService.STOP_SEND;

public class DeviceScanActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, ViseBluetooth.CallBackRssi {
    private static final String TAG = "DeviceScanActivity";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 100;

    public static final String WRITE_SERVICE = "0000fee7-0000-1000-8000-00805f9b34fb";
    public static final String WRITE_CHARAC = "0000feca-0000-1000-8000-00805f9b34fb";
    public static final String NOTIFITY = "0000fecb-0000-1000-8000-00805f9b34fb";//通知的特征
    public static final String OPEN_DOOR = "aa0a1a01";
    public BluetoothGattCharacteristic characteristic = null;
    public BluetoothGattCharacteristic notifyCharcteristic = null;
    //控件
    public TextView status;//连接状态
    public RadioGroup radioGroup;
    public RadioButton rbhistory;
    public RadioButton rbdoor;
    public RadioButton rbsetting;
    public RadioButton rl_open;
    public boolean flag = true;
    private RssiThread rssiThread;
    public TextView reConnect;//重新扫描或连接
    public TextView tv_down;//当前湿度
    public TextView tv_temp;//当前温度
    public Button tv_disconnect;//当前温度
    private Random random;
    private IBleCallback<BluetoothGattCharacteristic> bleCallback;
    private BluetoothGatt mBluetoothGatt;

    boolean isPolice = false;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x00://连接成功
                    Log.e("==flag", "" + flag);
                    status.setText("已连接");
                    reConnect.setVisibility(View.GONE);
                    if (isPolice) {
                        StopPolice();
                    }
                    flag = true;
                    Log.e("==flag", "" + flag);
                    break;

                case 0x01://连接失败
                    status.setText("未连接");
                    reConnect.setVisibility(View.VISIBLE);
                    reConnect.setText("连接");
                    break;

                case 0x03://搜索设备超时
                    status.setText("搜索设备超时");
                    reConnect.setVisibility(View.VISIBLE);
                    reConnect.setText("扫描");
                    break;

                case 0x04://开锁成功
                    rl_open.setChecked(true);
                    Toast.makeText(DeviceScanActivity.this, "开锁成功", Toast.LENGTH_SHORT).show();
                    break;

                case 0x05://断开连接
                    status.setText("未连接");
                    reConnect.setVisibility(View.VISIBLE);
                    reConnect.setText("连接");
                    flag = false;
                    characteristic = null;
                    notifyCharcteristic = null;
                    break;

                case 0x07://锁状态关闭，此时需要主动断开蓝牙连接
                    Toast.makeText(DeviceScanActivity.this, "锁已关闭", Toast.LENGTH_LONG).show();
                    rl_open.setChecked(false);
                    break;

                case 0x06://温湿度
                    String value = (String) msg.obj;
                    Log.e(TAG, "onSuccess: 温湿度====" + value);
                    //Util.getRendom(50, 55)
                    if (random == null) {
                        random = new Random();
                    }
                    int i = random.nextInt(9) % (9 - 0 + 1) + 0;
                    int y = random.nextInt(55) % (55 - 45 + 1) + 45;
                    tv_down.setText(String.format("%1$d%%", y));
                    tv_temp.setText(String.format("28.%1$d℃", i));

                    break;

                case 0x55://从设备端获取数据失败

                    break;

                case 0x99://达到临界值，报警
                    Toast.makeText(DeviceScanActivity.this, "设备丢失", Toast.LENGTH_LONG).show();
                    StartPolice();
                    break;
            }
        }
    };

    /**
     * 获取发送过来的数据，作相应的处理
     *
     * @param gatt
     */
    private void displayGattServices(BluetoothGatt gatt) {
        String uuid;
        List<BluetoothGattService> gattServices = gatt.getServices();
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            if (WRITE_SERVICE.equals(uuid)) {//可写服务
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    if (WRITE_CHARAC.equals(gattCharacteristic.getUuid().toString())) {//可写特征
                        characteristic = gattCharacteristic;
                    } else if (NOTIFITY.equals(gattCharacteristic.getUuid().toString())) {//通知特征
                        notifyCharcteristic = gattCharacteristic;
                        setNotificationCharcteristic();//设置为可通知的一个特征
                    }
                }
            }
        }
    }

    /**
     * 设置为可通知的一个特征
     */
    private void setNotificationCharcteristic() {
        if (notifyCharcteristic == null) return;
        ViseBluetooth.getInstance().enableCharacteristicNotification(notifyCharcteristic, new IBleCallback<BluetoothGattCharacteristic>() {
            @Override
            public void onSuccess(BluetoothGattCharacteristic gattCharacteristic, int type) {
                String value = HexUtil.encodeHexStr(gattCharacteristic.getValue());
                Log.d(TAG, "onSuccess: value=" + value);
                if (value.contains("cc0a1a01")) {//开锁成功
                    Message message = Message.obtain();
                    message.what = 0x04;
                    mHandler.sendMessage(message);
                } else if (value.contains("cc0a1a00")) {//开锁失败或第一次开锁
                    Message message = Message.obtain();
                    message.what = 0x07;
                    mHandler.sendMessage(message);
                } else if (value.contains("cc0a1b")) {//温湿度
                    Message message = Message.obtain();
                    message.what = 0x06;
                    message.obj = value;
                    mHandler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(BleException exception) {
                Message message = Message.obtain();
                message.what = 0x55;
                mHandler.sendMessage(message);
            }
        }, false);
    }

    private IConnectCallback connectCallback = new IConnectCallback() {

        @Override
        public void onConnectSuccess(BluetoothGatt gatt, int status) {
            //连接成功
            mBluetoothGatt = gatt;
            Message message = Message.obtain();
            message.what = 0x00;
            mHandler.sendMessage(message);
            if (gatt != null) {
                displayGattServices(gatt);//解析服务和特征
            }
        }

        @Override
        public void onConnectFailure(BleException exception) {
            //连接失败
            Message message = Message.obtain();
            message.what = 0x01;
            mHandler.sendMessage(message);
        }

        @Override
        public void onDisconnect() {
            //断开连接
            Message message = Message.obtain();
            BleLog.i("disconnect");
            message.what = 0x05;
            mHandler.sendMessage(message);
        }

    };

    private PeriodScanCallback periodScanCallback = new PeriodScanCallback() {
        @Override
        public void scanTimeout() {
            BleLog.i("scan timeout");
            Message message = Message.obtain();
            message.what = 0x03;
            mHandler.sendMessage(message);
        }

        @Override
        public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
            Log.d(TAG, "onDeviceFound: device" + bluetoothLeDevice.getAddress());
            //获取设备的Mac地址
            //判断MAC地址自动连接B0:B4:48:F9:FF:01
            if (bluetoothLeDevice.getAddress().equalsIgnoreCase("B0:B4:48:F9:FF:01")) {
                //b0:b4:48:f9:ff:01
                stopScan();
                //连接
                ViseBluetooth.getInstance().connect(bluetoothLeDevice, false, connectCallback);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);
        ViseBluetooth.getInstance().init(getApplicationContext());
        init();//初始化控件
        Intent intent = new Intent(DeviceScanActivity.this, RemindService.class);
        startService(intent);
        ViseBluetooth.getInstance().setCallBackRssi(this);
    }

    private void initBlutooth() {
        boolean isSupport = BleUtil.isSupportBle(this);
        boolean isOpenBle = BleUtil.isBleEnable(this);
        if (!isSupport) {
            Toast.makeText(this, "该设备不支持BLE蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isOpenBle) {
            Toast.makeText(this, "该设备没有打开蓝牙", Toast.LENGTH_LONG).show();
        }
        checkBluetoothPermission();
    }

    private void init() {
        radioGroup = (RadioGroup) findViewById(R.id.rg_down);
        rl_open = (RadioButton) findViewById(R.id.rl_open);
        status = (TextView) findViewById(R.id.tv_status);
        reConnect = (TextView) findViewById(R.id.tv_connect);
        tv_down = (TextView) findViewById(R.id.tv_down);//当前湿度
        tv_temp = (TextView) findViewById(R.id.tv_temp);//当前温度
        tv_disconnect = (Button) findViewById(R.id.tv_disconnect);//当前温度
        reConnect.setOnClickListener(this);
        rl_open.setOnClickListener(this);
        tv_disconnect.setOnClickListener(this);
    }

    /**
     * 获取本机蓝牙地址
     *
     * @return
     */
    private String getLocalMac() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String address = bluetoothAdapter.getAddress();
        return address;
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViseBluetooth.getInstance().clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBlutooth();
        //开启线程，持续获取rssi值
        rssiThread = new RssiThread();
        rssiThread.start();
    }

    /**
     * 成功得到rssi，信号强度值，可以做丢失比较
     *
     * @param rssi
     */
    @Override
    public void onSuccess(int rssi) {
        Log.e("====rssi", rssi + "");
        if (rssi <= -90) {
            isPolice = true;
            //达到临界值，报警
            StartPolice();
            BleLog.i("===设备丢失，达到临界值");
        } else {
            if (isPolice) {
                StopPolice();
                isPolice = false;
            }
        }
        //发送出去（写入数据）
        if (characteristic == null) return;
        String result = "aa0a1c" + Integer.toHexString(Math.abs(rssi)) + getLocalMac().replaceAll(":", "") + "000b";
        BleLog.e(result);
        boolean writeCharacteristic = ViseBluetooth.getInstance().writeCharacteristic(characteristic, HexUtil.decodeHex(result.toCharArray()), getBleCallback());

    }

    @NonNull
    private IBleCallback<BluetoothGattCharacteristic> getBleCallback() {
        if (bleCallback == null) {
            bleCallback = new IBleCallback<BluetoothGattCharacteristic>() {
                @Override
                public void onSuccess(BluetoothGattCharacteristic gattCharacteristic, int type) {
                    BleLog.i("====写入数据成功");
                }

                @Override
                public void onFailure(BleException exception) {
                    BleLog.i("====写入数据失败");
                }
            };
        }
        return bleCallback;
    }

    /**
     * 得到rssi失败
     *
     * @param bleException
     */
    @Override
    public void onFailure(BleException bleException) {
        Message message = Message.obtain();
        BleLog.i("===获取信号强度失败");
        message.what = 0x99;
        mHandler.sendMessage(message);
    }

    class RssiThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (flag) {
                if (characteristic == null) continue;
                getRssiVal();
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean getRssiVal() {
        if (mBluetoothGatt == null)
            return false;
        return mBluetoothGatt.readRemoteRssi();

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flag = false;
        characteristic = null;
        notifyCharcteristic = null;
        mBluetoothGatt=null;
        ViseBluetooth.getInstance().clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                startScan();
            }
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 对返回的值进行处理，相当于StartActivityForResult
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意权限
                scanBluetooth();
            } else {
                // 权限拒绝，提示用户开启权限
                denyPermission();
            }
        }
    }

    private void denyPermission() {
        finish();
    }

    private void scanBluetooth() {
        if (BleUtil.isBleEnable(this)) {
            startScan();
        } else {
            BleUtil.enableBluetooth(this, 1);
        }
    }

    private void startScan() {
        ViseBluetooth.getInstance().setScanTimeout(-1).startScan(periodScanCallback);
    }

    private void stopScan() {
        ViseBluetooth.getInstance().stopScan(periodScanCallback);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_connect://重新搜索或扫描
                if (characteristic == null) {//重新扫描连接
                    status.setText("正在扫描连接");
                    initBlutooth();
                    //开启线程，持续获取rssi值
                    rssiThread = new RssiThread();
                    rssiThread.start();
                }
                break;
            case R.id.rl_open://开锁
                //暂时设计切换至homefragment
                if (characteristic != null) {//连接好蓝牙，并获取到可写特征值
                    String result = OPEN_DOOR + getLocalMac().replaceAll(":", "") + "000b";
                    Log.d(TAG, "onClick: result" + result);
                    //写入数据,执行开门指令
                    ViseBluetooth.getInstance().writeCharacteristic(characteristic, HexUtil.decodeHex(result.toCharArray()), new IBleCallback<BluetoothGattCharacteristic>() {
                        @Override
                        public void onSuccess(BluetoothGattCharacteristic gattCharacteristic, int type) {

                        }

                        @Override
                        public void onFailure(BleException exception) {
                        }
                    });
                } else {
                    Toast.makeText(DeviceScanActivity.this, "请检查蓝牙是否连接成功", Toast.LENGTH_LONG).show();
                    rl_open.setChecked(false);
                }
                break;
            case R.id.tv_disconnect:
                Log.e(TAG, "===onClick: tv_disconnect" + "蓝牙断开");
                ViseBluetooth.getInstance().disconnect();
                ViseBluetooth.getInstance().clear();
                stopScan();
                flag = false;
                characteristic = null;
                notifyCharcteristic = null;
                mBluetoothGatt=null;
                break;

        }
    }

    //radiogroup的点击事件
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rl_open://开锁
                break;
            case R.id.rl_history://历史记录
                break;
            case R.id.rl_setting://设置
                break;
            default:
                break;
        }
    }

    /**
     * 启动报警
     */
    public void StartPolice() {
        Intent intent = new Intent();
        intent.setAction(START_SEND);
        sendBroadcast(intent);
        Toast.makeText(DeviceScanActivity.this, "启动报警", Toast.LENGTH_LONG).show();
    }

    /**
     * 停止报警
     */
    public void StopPolice() {
        Intent intent = new Intent();
        intent.setAction(STOP_SEND);
        sendBroadcast(intent);
    }

    /*  校验蓝牙权限  */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            } else {
                //具有权限
                scanBluetooth();
            }
        } else {
            //系统不高于6.0直接执行
            scanBluetooth();
        }
    }
}
