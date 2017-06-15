package com.androidex.baseble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.androidex.baseble.callback.IBleCallback;
import com.androidex.baseble.callback.IConnectCallback;
import com.androidex.baseble.callback.scan.PeriodLScanCallback;
import com.androidex.baseble.callback.scan.PeriodMacScanCallback;
import com.androidex.baseble.callback.scan.PeriodNameScanCallback;
import com.androidex.baseble.callback.scan.PeriodScanCallback;
import com.androidex.baseble.common.BleConstant;
import com.androidex.baseble.common.State;
import com.androidex.baseble.exception.BleException;
import com.androidex.baseble.exception.ConnectException;
import com.androidex.baseble.exception.GattException;
import com.androidex.baseble.exception.InitiatedException;
import com.androidex.baseble.exception.OtherException;
import com.androidex.baseble.exception.TimeoutException;
import com.androidex.baseble.model.BluetoothLeDevice;
import com.androidex.baseble.utils.BleLog;
import com.androidex.baseble.utils.HexUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @Description: Bluetooth操作类
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 16/8/5 20:42.
 */
public class ViseBluetooth {

    public static final int DEFAULT_SCAN_TIME = 20000;
    public static final int DEFAULT_CONN_TIME = 10000;
    public static final int DEFAULT_OPERATE_TIME = 5000;

    private static final int MSG_WRITE_CHA = 1;
    private static final int MSG_WRITE_DES = 2;
    private static final int MSG_READ_CHA = 3;
    private static final int MSG_READ_DES = 4;
    private static final int MSG_READ_RSSI = 5;
    private static final int MSG_CONNECT_TIMEOUT = 6;

    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattDescriptor descriptor;
    private IConnectCallback connectCallback;
    private IBleCallback tempBleCallback;
    private IBleCallback receiveBleCallback;
    private volatile Set<IBleCallback> bleCallbacks = new LinkedHashSet<>();
    private State state = State.DISCONNECT;
    private int scanTimeout = DEFAULT_SCAN_TIME;
    private int connectTimeout = DEFAULT_CONN_TIME;
    private int operateTimeout = DEFAULT_OPERATE_TIME;

    private static ViseBluetooth viseBluetooth;
    public static ViseBluetooth getInstance(){
        if(viseBluetooth == null){
            synchronized (ViseBluetooth.class){
                if (viseBluetooth == null) {
                    viseBluetooth = new ViseBluetooth();
                }
            }
        }
        return viseBluetooth;
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_CONNECT_TIMEOUT){
                IConnectCallback connectCallback = (IConnectCallback) msg.obj;
                if(connectCallback != null && state != State.CONNECT_SUCCESS){
                    close();
                    connectCallback.onConnectFailure(new TimeoutException());
                }
            } else{
                IBleCallback bleCallback = (IBleCallback) msg.obj;
                if (bleCallback != null) {
                    bleCallback.onFailure(new TimeoutException());
                    removeBleCallback(bleCallback);
                }
            }
            msg.obj = null;
        }
    };
    private BluetoothGattCallback coreGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            BleLog.i("onConnectionStateChange  status: " + status + " ,newState: " + newState +
                    "  ,thread: " + Thread.currentThread().getId());
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                state = State.DISCONNECT;
                if (handler != null) {
                    handler.removeMessages(MSG_CONNECT_TIMEOUT);
                }
                if (connectCallback != null) {
                    close();
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if(status == 0){
                                connectCallback.onDisconnect();
                            } else{
                                connectCallback.onConnectFailure(new ConnectException(gatt, status));
                            }
                        }
                    });
                }
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                state = State.CONNECT_PROCESS;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            BleLog.i("onServicesDiscovered  status: " + status);
            if (handler != null) {
                handler.removeMessages(MSG_CONNECT_TIMEOUT);
            }
            if(status == 0){
                bluetoothGatt = gatt;
                state = State.CONNECT_SUCCESS;
                if (connectCallback != null) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            connectCallback.onConnectSuccess(gatt, status);
                        }
                    });
                }
            } else{
                state = State.CONNECT_FAILURE;
                if (connectCallback != null) {
                    close();
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            connectCallback.onConnectFailure(new ConnectException(gatt, status));
                        }
                    });
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            BleLog.i("onCharacteristicRead  status: " + status + ", data:" + HexUtil.encodeHexStr(characteristic.getValue()));
            if (bleCallbacks == null) {
                return;
            }
            if (handler != null) {
                handler.removeMessages(MSG_READ_CHA);
            }
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    for (IBleCallback<BluetoothGattCharacteristic> bleCallback : bleCallbacks) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            bleCallback.onSuccess(characteristic, 0);
                        } else {
                            bleCallback.onFailure(new GattException(status));
                        }
                    }
                    removeBleCallback(tempBleCallback);
                }
            });
        }

        //接收到消息后执行这里
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            BleLog.i("onCharacteristicChanged data:" + HexUtil.encodeHexStr(characteristic.getValue()));
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (receiveBleCallback != null) {
                        receiveBleCallback.onSuccess(characteristic, 0);
                    }
                }
            });
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            BleLog.i("onDescriptorRead  status: " + status + ", data:" + HexUtil.encodeHexStr(descriptor.getValue()));
            if (bleCallbacks == null) {
                return;
            }
            if (handler != null) {
                handler.removeMessages(MSG_READ_DES);
            }
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    for (IBleCallback<BluetoothGattDescriptor> bleCallback : bleCallbacks) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            bleCallback.onSuccess(descriptor, 0);
                        } else {
                            bleCallback.onFailure(new GattException(status));
                        }
                    }
                    removeBleCallback(tempBleCallback);
                }
            });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            BleLog.i("onCharacteristicWrite  status: " + status + ", data:" + HexUtil.encodeHexStr(characteristic.getValue()));
            if (bleCallbacks == null) {
                return;
            }
            if (handler != null) {
                handler.removeMessages(MSG_WRITE_CHA);
            }
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    for (IBleCallback<BluetoothGattCharacteristic> bleCallback : bleCallbacks) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            bleCallback.onSuccess(characteristic, 0);
                        } else {
                            bleCallback.onFailure(new GattException(status));
                        }
                    }
                    removeBleCallback(tempBleCallback);
                }
            });
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, final int status) {
            BleLog.i("onReadRemoteRssi  status: " + status + ", rssi:" + rssi);
            if (bleCallbacks == null) {
                return;
            }
            if (handler != null) {
                handler.removeMessages(MSG_READ_RSSI);
            }
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (status == BluetoothGatt.GATT_SUCCESS){
                        callBackRssi.onSuccess(rssi);
                    }else{
                        callBackRssi.onFailure(new GattException(status));
                    }
                    /*for (IBleCallback<Integer> bleCallback : bleCallbacks) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            bleCallback.onSuccess(rssi, 0);
                        } else {
                            bleCallback.onFailure(new GattException(status));
                        }
                    }
                    removeBleCallback(tempBleCallback);*/
                }
            });
        }
    };

    public interface CallBackRssi{
        public void onSuccess(int rssi);
        public void onFailure(BleException bleException);
    }
    public CallBackRssi callBackRssi;
    public void setCallBackRssi(CallBackRssi callBackRssi){
        this.callBackRssi = callBackRssi;
    }

    private ViseBluetooth() {
    }

    public void init(Context context){
        if (this.context == null) {
            this.context = context.getApplicationContext();
            bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    /*==================Android API 18 Scan========================*/
    @Deprecated
    public void startLeScan(BluetoothAdapter.LeScanCallback leScanCallback){
        if (bluetoothAdapter != null) {
            bluetoothAdapter.startLeScan(leScanCallback);
            state = State.SCAN_PROCESS;
        }
    }

    @Deprecated
    public void stopLeScan(BluetoothAdapter.LeScanCallback leScanCallback){
        if (bluetoothAdapter != null) {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    @Deprecated
    public void startScan(PeriodScanCallback periodScanCallback){
        if (periodScanCallback == null) {
            throw new IllegalArgumentException("this PeriodScanCallback is Null!");
        }
        periodScanCallback.setViseBluetooth(this).setScan(true).setScanTimeout(scanTimeout).scan();
    }

    @Deprecated
    public void stopScan(PeriodScanCallback periodScanCallback){
        if (periodScanCallback == null) {
            throw new IllegalArgumentException("this PeriodScanCallback is Null!");
        }
        periodScanCallback.setViseBluetooth(this).setScan(false).removeHandlerMsg().scan();
    }

    /*==================Android API 21 Scan========================*/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startLeScan(ScanCallback leScanCallback){
        if (bluetoothAdapter != null && bluetoothAdapter.getBluetoothLeScanner() != null) {
            bluetoothAdapter.getBluetoothLeScanner().startScan(leScanCallback);
            state = State.SCAN_PROCESS;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startLeScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback leScanCallback){
        if (bluetoothAdapter != null && bluetoothAdapter.getBluetoothLeScanner() != null) {
            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, leScanCallback);
            state = State.SCAN_PROCESS;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopLeScan(ScanCallback leScanCallback){
        if (bluetoothAdapter != null && bluetoothAdapter.getBluetoothLeScanner() != null) {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScan(PeriodLScanCallback periodScanCallback){
        if (periodScanCallback == null) {
            throw new IllegalArgumentException("this PeriodScanCallback is Null!");
        }
        periodScanCallback.setViseBluetooth(this).setScan(true).setScanTimeout(scanTimeout).scan();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScan(List<ScanFilter> filters, ScanSettings settings, PeriodLScanCallback periodScanCallback){
        if (periodScanCallback == null) {
            throw new IllegalArgumentException("this PeriodScanCallback is Null!");
        }
        periodScanCallback.setViseBluetooth(this).setScan(true).setScanTimeout(scanTimeout).setFilters(filters).setSettings(settings).scan();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopScan(PeriodLScanCallback periodScanCallback){
        if (periodScanCallback == null) {
            throw new IllegalArgumentException("this PeriodScanCallback is Null!");
        }
        periodScanCallback.setViseBluetooth(this).setScan(false).removeHandlerMsg().scan();
    }

    /*==================connect========================*/
    public synchronized BluetoothGatt connect(BluetoothDevice bluetoothDevice, boolean autoConnect, IConnectCallback connectCallback){
        if (bluetoothDevice == null || connectCallback == null) {
            throw new IllegalArgumentException("this BluetoothDevice or IConnectCallback is Null!");
        }
        if (handler != null) {
            Message msg = handler.obtainMessage(MSG_CONNECT_TIMEOUT, connectCallback);
            handler.sendMessageDelayed(msg, connectTimeout);
        }
        this.connectCallback = connectCallback;
        state = State.CONNECT_PROCESS;
        return bluetoothDevice.connectGatt(this.context, autoConnect, coreGattCallback);
    }

    public void connect(BluetoothLeDevice bluetoothLeDevice, boolean autoConnect, IConnectCallback connectCallback){
        if (bluetoothLeDevice == null) {
            throw new IllegalArgumentException("this BluetoothLeDevice is Null!");
        }
        connect(bluetoothLeDevice.getDevice(), autoConnect, connectCallback);
    }

    public void connectByName(String name, final boolean autoConnect, final IConnectCallback connectCallback){
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Illegal Name!");
        }
        startScan(new PeriodNameScanCallback(name) {
            @Override
            public void onDeviceFound(final BluetoothLeDevice bluetoothLeDevice) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        connect(bluetoothLeDevice, autoConnect, connectCallback);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectCallback != null) {
                            connectCallback.onConnectFailure(new TimeoutException());
                        }
                    }
                });
            }
        });
    }

    public void connectByMac(String mac, final boolean autoConnect, final IConnectCallback connectCallback){
        if (mac == null || mac.split(":").length != 6) {
            throw new IllegalArgumentException("Illegal MAC!");
        }
        startScan(new PeriodMacScanCallback(mac) {
            @Override
            public void onDeviceFound(final BluetoothLeDevice bluetoothLeDevice) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        connect(bluetoothLeDevice, autoConnect, connectCallback);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectCallback != null) {
                            connectCallback.onConnectFailure(new TimeoutException());
                        }
                    }
                });
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void connectByLName(String name, final boolean autoConnect, final IConnectCallback connectCallback){
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Illegal Name!");
        }
        List<ScanFilter> bleScanFilters = new ArrayList<>();
        bleScanFilters.add(new ScanFilter.Builder().setDeviceName(name).build());
        startScan(bleScanFilters, null, new PeriodLScanCallback() {
            @Override
            public void onDeviceFound(final BluetoothLeDevice bluetoothLeDevice) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        connect(bluetoothLeDevice, autoConnect, connectCallback);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectCallback != null) {
                            connectCallback.onConnectFailure(new TimeoutException());
                        }
                    }
                });
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void connectByLMac(String mac, final boolean autoConnect, final IConnectCallback connectCallback){
        if (mac == null || mac.split(":").length != 6) {
            throw new IllegalArgumentException("Illegal MAC!");
        }
        List<ScanFilter> bleScanFilters = new ArrayList<>();
        bleScanFilters.add(new ScanFilter.Builder().setDeviceAddress(mac).build());
        startScan(bleScanFilters, null, new PeriodLScanCallback() {
            @Override
            public void onDeviceFound(final BluetoothLeDevice bluetoothLeDevice) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        connect(bluetoothLeDevice, autoConnect, connectCallback);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectCallback != null) {
                            connectCallback.onConnectFailure(new TimeoutException());
                        }
                    }
                });
            }
        });
    }

    /*=================main operate========================*/
    public ViseBluetooth withUUID(UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID) {
        if (serviceUUID != null && bluetoothGatt != null) {
            service = bluetoothGatt.getService(serviceUUID);
        }
        if (service != null && characteristicUUID != null) {
            characteristic = service.getCharacteristic(characteristicUUID);
        }
        if (characteristic != null && descriptorUUID != null) {
            descriptor = characteristic.getDescriptor(descriptorUUID);
        }
        return this;
    }

    public ViseBluetooth withUUIDString(String serviceUUID, String characteristicUUID, String descriptorUUID) {
        return withUUID(formUUID(serviceUUID), formUUID(characteristicUUID), formUUID(descriptorUUID));
    }

    private UUID formUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }

    public boolean writeCharacteristic(byte[] data, IBleCallback<BluetoothGattCharacteristic> bleCallback) {
        return writeCharacteristic(getCharacteristic(), data, bleCallback);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data,
                                       final IBleCallback<BluetoothGattCharacteristic> bleCallback) {
        if(characteristic == null){
            if(bleCallback != null){
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        bleCallback.onFailure(new OtherException("this characteristic is null!"));
                        removeBleCallback(bleCallback);
                    }
                });
            }
            return false;
        }
        BleLog.i(characteristic.getUuid() + " characteristic write bytes: "
                + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
        listenAndTimer(bleCallback, MSG_WRITE_CHA);
        characteristic.setValue(data);
        return handleAfterInitialed(getBluetoothGatt().writeCharacteristic(characteristic), bleCallback);
    }

    public boolean writeDescriptor(byte[] data, IBleCallback<BluetoothGattDescriptor> bleCallback) {
        return writeDescriptor(getDescriptor(), data, bleCallback);
    }

    public boolean writeDescriptor(BluetoothGattDescriptor descriptor, byte[] data, final IBleCallback<BluetoothGattDescriptor> bleCallback) {
        if(descriptor == null){
            if(bleCallback != null){
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        bleCallback.onFailure(new OtherException("this descriptor is null!"));
                        removeBleCallback(bleCallback);
                    }
                });
            }
            return false;
        }
        BleLog.i(descriptor.getUuid() + " descriptor write bytes: "
                + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
        listenAndTimer(bleCallback, MSG_WRITE_DES);
        descriptor.setValue(data);
        return handleAfterInitialed(getBluetoothGatt().writeDescriptor(descriptor), bleCallback);
    }

    public boolean readCharacteristic(IBleCallback<BluetoothGattCharacteristic> bleCallback) {
        return readCharacteristic(getCharacteristic(), bleCallback);
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic, final IBleCallback<BluetoothGattCharacteristic> bleCallback) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            setCharacteristicNotification(getBluetoothGatt(), characteristic, false, false);
            listenAndTimer(bleCallback, MSG_READ_CHA);
            return handleAfterInitialed(getBluetoothGatt().readCharacteristic(characteristic), bleCallback);
        } else {
            if (bleCallback != null) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        bleCallback.onFailure(new OtherException("Characteristic [is not] readable!"));
                        removeBleCallback(bleCallback);
                    }
                });
            }
            return false;
        }
    }

    public boolean readDescriptor(IBleCallback<BluetoothGattDescriptor> bleCallback) {
        return readDescriptor(getDescriptor(), bleCallback);
    }

    public boolean readDescriptor(BluetoothGattDescriptor descriptor, IBleCallback<BluetoothGattDescriptor> bleCallback) {
        listenAndTimer(bleCallback, MSG_READ_DES);
        return handleAfterInitialed(getBluetoothGatt().readDescriptor(descriptor), bleCallback);
    }

    public boolean readRemoteRssi(IBleCallback<Integer> bleCallback) {
        listenAndTimer(bleCallback, MSG_READ_RSSI);
        return handleAfterInitialed(getBluetoothGatt().readRemoteRssi(), bleCallback);
    }

    public boolean enableCharacteristicNotification(IBleCallback<BluetoothGattCharacteristic> bleCallback, boolean isIndication) {
        return enableCharacteristicNotification(getCharacteristic(), bleCallback, isIndication);
    }

    public boolean enableCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                    final IBleCallback<BluetoothGattCharacteristic> bleCallback,
                                                    boolean isIndication) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            receiveBleCallback = bleCallback;
            return setCharacteristicNotification(getBluetoothGatt(), characteristic, true, isIndication);
        } else {
            if (bleCallback != null) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        bleCallback.onFailure(new OtherException("Characteristic [not supports] readable!"));
                        removeBleCallback(bleCallback);
                    }
                });
            }
            return false;
        }
    }

    public boolean enableDescriptorNotification(IBleCallback<BluetoothGattDescriptor> bleCallback) {
        return enableDescriptorNotification(getDescriptor(), bleCallback);
    }

    public boolean enableDescriptorNotification(BluetoothGattDescriptor descriptor, IBleCallback<BluetoothGattDescriptor> bleCallback) {
        receiveBleCallback = bleCallback;
        return setDescriptorNotification(getBluetoothGatt(), descriptor, true);
    }

    public boolean setNotification(boolean enable, boolean isIndication) {
        return setNotification(getBluetoothGatt(), getCharacteristic(), getDescriptor(), enable, isIndication);
    }

    public boolean setNotification(BluetoothGatt gatt,
                                   BluetoothGattCharacteristic characteristic,
                                   BluetoothGattDescriptor descriptor, boolean enable, boolean isIndication) {
        return setCharacteristicNotification(gatt, characteristic, enable, isIndication)
                && setDescriptorNotification(gatt, descriptor, enable);
    }

    public boolean setCharacteristicNotification(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean enable,
                                                 boolean isIndication) {
        if (gatt != null && characteristic != null) {
            BleLog.i("Characteristic set notification value: " + enable);
            boolean success = gatt.setCharacteristicNotification(characteristic, enable);
            if (enable) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BleConstant.CLIENT_CHARACTERISTIC_CONFIG));
                if(descriptor != null){
                    if(isIndication){
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    } else{
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    }
                    gatt.writeDescriptor(descriptor);
                    BleLog.i("Characteristic set notification is Success!");
                }
            }
            return success;
        }
        return false;
    }

    public boolean setDescriptorNotification(BluetoothGatt gatt,
                                             BluetoothGattDescriptor descriptor,
                                             boolean enable) {
        if (gatt != null && descriptor != null) {
            BleLog.i("Descriptor set notification value: " + enable);
            if (enable) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    private boolean handleAfterInitialed(boolean initiated, final IBleCallback bleCallback) {
        if (bleCallback != null) {
            if (!initiated) {
                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                }
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        bleCallback.onFailure(new InitiatedException());
                        removeBleCallback(bleCallback);
                    }
                });
            }
        }
        return initiated;
    }

    private synchronized void listenAndTimer(final IBleCallback bleCallback, int what) {
        if (bleCallbacks != null && handler != null) {
            this.tempBleCallback = bleCallback;
            bleCallbacks.add(bleCallback);
            Message msg = handler.obtainMessage(what, bleCallback);
            handler.sendMessageDelayed(msg, operateTimeout);
        }
    }

    public boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            if (handler != null) {
                handler.post(runnable);
            }
        }
    }

    public boolean isConnected(){
        if(state == State.CONNECT_SUCCESS){
            return true;
        } else{
            return false;
        }
    }

    public synchronized void removeBleCallback(IBleCallback bleCallback){
        if (bleCallbacks != null && bleCallbacks.size() > 0) {
            bleCallbacks.remove(bleCallback);
        }
    }

    public synchronized boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && bluetoothGatt != null) {
                final boolean success = (Boolean) refresh.invoke(getBluetoothGatt());
                BleLog.i("Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            BleLog.e("An exception occured while refreshing device", e);
        }
        return false;
    }

    public synchronized void disconnect(){
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    public synchronized void close(){
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }

    public synchronized void clear(){
        disconnect();
        refreshDeviceCache();
        close();
        if (bleCallbacks != null) {
            bleCallbacks.clear();
        }
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
    }

    /*==================get and set========================*/
    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public Set<IBleCallback> getBleCallbacks() {
        return bleCallbacks;
    }

    public BluetoothGattService getService() {
        return service;
    }

    public ViseBluetooth setService(BluetoothGattService service) {
        this.service = service;
        return this;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public ViseBluetooth setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
        return this;
    }

    public BluetoothGattDescriptor getDescriptor() {
        return descriptor;
    }

    public ViseBluetooth setDescriptor(BluetoothGattDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public int getOperateTimeout() {
        return operateTimeout;
    }

    public ViseBluetooth setOperateTimeout(int operateTimeout) {
        this.operateTimeout = operateTimeout;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public ViseBluetooth setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getScanTimeout() {
        return scanTimeout;
    }

    public ViseBluetooth setScanTimeout(int scanTimeout) {
        this.scanTimeout = scanTimeout;
        return this;
    }

    public State getState() {
        return state;
    }

    public ViseBluetooth setState(State state) {
        this.state = state;
        return this;
    }
}
