package com.androidex;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.androidex.plugins.OnBackCall;
import com.androidex.plugins.kkfile;
import com.tencent.device.TXDataPoint;
import com.tencent.device.TXDeviceService;

import java.util.Arrays;
import java.util.HashMap;


/**
 * Created by yangjun on 16/6/6.
 * 锁相开源门禁机软件的主要服务类,DoorLock主要提供开门,关门指令以及上报门开和关闭的事件.
 */
public class DoorLock extends Service implements OnBackCall{

    public static final String TAG = "DoorLock";
    public static final String mDoorSensorAction = "com.android.action.doorsensor";

    private DoorLockServiceBinder mDoorLock;

    /**
     * 当门的状态改变时的事件定义
     */
    public static final String DoorLockStatusChange 	 = "DoorLockStatusChange";
    /**
     * DoorLock通过DoorLockOpenDoor广播获得开门指令并发送给门禁控制器
     */
    public static final String DoorLockOpenDoor          = "DoorLockOpenDoor";
    public static final String actionRunReboot           = "com.androidex.REBOOT";
    public static final String actionRunShutdown         = "com.androidex.ACTION_REQUEST_SHUTDOWN";
    private NotifyReceiver mReceiver;
    private static DoorLock mServiceInstance = null;
    private boolean mPlugedShutdown = false;

    public static DoorLock getInstance()
    {
        return mServiceInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDoorLock = new DoorLockServiceBinder();
        mReceiver = new NotifyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(mDoorSensorAction);
        filter.addAction(DoorLockOpenDoor);
        filter.addAction(TXDeviceService.OnReceiveDataPoint);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(actionRunReboot);
        filter.addAction(actionRunShutdown);
        registerReceiver(mReceiver, filter);
        int r = mDoorLock.openDoor(1,16);
        if(r == 9)
            Toast.makeText(DoorLock.this, String.format("Open %d,delay %ds close.",1,16*150/1000), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(DoorLock.this, String.format("Open door 1 fail return %d.",r), Toast.LENGTH_SHORT).show();

        Log.d(TAG,String.format("open door %d",r));
        /*AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.set(4, System.currentTimeMillis() + 480000,   //从现在起30s
                PendingIntent.getBroadcast(getApplicationContext(), 100, new Intent(actionRunReboot), PendingIntent.FLAG_UPDATE_CURRENT));
        */
    }

    public void runSetAlarm(long wakeupTime) {
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent;

        /**
         * 下面的代码演示如何实现定时事件,可以用于定时重启,先设置好重启时间,然后再关机.
         *
         * AlarmManager.ELAPSED_REALTIME            表示闹钟在手机睡眠状态下不可用，该状态下闹钟使用相对时间（相对于系统启动开始），状态值为3；
         * AlarmManager.ELAPSED_REALTIME_WAKEUP     表示闹钟在睡眠状态下会唤醒系统并执行提示功能，该状态下闹钟也使用相对时间，状态值为2；
         * AlarmManager.RTC                         表示闹钟在睡眠状态下不可用，该状态下闹钟使用绝对时间，即当前系统时间，状态值为1；
         * AlarmManager.RTC_WAKEUP                  表示闹钟在睡眠状态下会唤醒系统并执行提示功能，该状态下闹钟使用绝对时间，状态值为0；
         * AlarmManager.POWER_OFF_WAKEUP            表示闹钟在手机关机状态下也能正常进行提示功能，所以是5个状态中用的最多的状态之一，该状态下
         *                                          闹钟也是用绝对时间，状态值为4；不过本状态好像受SDK版本影响，某些版本并不支持；
         *
         * RTC闹钟和ELAPSED_REALTIME最大的差别就是前者可以通过修改手机时间触发闹钟事件，后者要通过真实时间的流逝，即使在休眠状态，时间也会被计算。
         */
        /*Intent intent = new Intent();
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(4,  wakeupTime, pendingIntent);*/
    }

    public void runReboot() {
        //首先要关闭程序提供的服务,以免关机时程序还需要写入数据,导致存属区损坏
        Intent intent = new Intent();
        intent.setAction("com.androidex.action.reboot");
        sendBroadcast(intent);
    }

    public void runShutdown() {
        //首先要关闭程序提供的服务,以免关机时程序还需要写入数据,导致存属区损坏
        Intent intent = new Intent();
        intent.setAction("com.androidex.action.shutdown");
        sendBroadcast(intent);
    }

    public void setPlugedShutdown() {
        mPlugedShutdown = true;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
        mServiceInstance = null;
        mDoorLock = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);
        mServiceInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDoorLock;
    }

    @Override
    public void onBackCallEvent(int code, String args) {
        Log.v("onBackCallEvent",args);
    }

    public class DoorLockServiceBinder extends IDoorLockInterface.Stub {
        String rkeyDev = "/dev/rkey";
        int ident = 0;

        /**
         * 开门指令
         * @param index     门的序号,主门=0,副门=1
         * @param delay     延迟关门的时间,0表示不启用延迟关门,大于0表示延迟时间,延迟时间为delay*150ms
         * @return          大于0表示成功,实际上等于9表示真正的成功,因为返回值表示写入的数据,开门指令长度为9.
         */
        public int openDoor(int index, int delay){
            kkfile rkey = new kkfile();

            if(index < 0 || index > 0xFE) index = 0;
            if(ident < 0 || ident > 0xFE) ident = 0;
            if(delay < 0 || delay > 0xFE) delay = 0;
            String cmd = String.format("FB%02X2503%02X01%02X00FE",ident,index,delay);
            int r = rkey.writeHex(rkeyDev,cmd);
            if(r > 0) {
                SoundPoolUtil.getSoundPoolUtil().loadVoice(getBaseContext(),011111);
            }
            return r > 0?1:0;
        }
        public int closeDoor(int index){
            kkfile rkey = new kkfile();

            if(index < 0 || index > 0xFE) index = 0;
            if(ident < 0 || ident > 0xFE) ident = 0;
            String cmd = String.format("FB%02X2503%02X000000FE",ident,index);
            int r = rkey.writeHex(rkeyDev,cmd);
            return r > 0 ? 1:0;
        }
    }

    public class NotifyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mDoorSensorAction)){
                String doorsensor = intent.getStringExtra("doorsensor");
                UEventMap mds = new UEventMap(doorsensor);

                Log.d(TAG, String.format("%s\t Door sensor=%s\n",mds.get("doorsensor"), mds.toString()));

                Intent ds_intent = new Intent();
                ds_intent.setAction(DoorLock.DoorLockStatusChange);
                ds_intent.putExtra("doorsensor",mds.get("doorsensor"));
                sendBroadcast(ds_intent);
            } else if(intent.getAction().equals(DoorLockOpenDoor)) {
                int index = intent.getIntExtra("index", 0);
                int status = intent.getIntExtra("status", 0);

                if (status != 0) {

                    mDoorLock.openDoor(0xF0, 0x40);
                    //mDoorLock.openDoor(1, 0x20);
                } else {
                    int result = mDoorLock.closeDoor(index);
                }
            } else if(intent.getAction().equals(actionRunReboot)){
                runReboot();
            } else if(intent.getAction().equals(actionRunShutdown)){
                runShutdown();
            } else if(intent.getAction().equals(TXDeviceService.OnReceiveDataPoint)){
                Log.d(TAG, "onReceive: 执行了吗?");
                Long from = intent.getExtras().getLong("from", 0);
                Parcelable[] arrayDataPoint = intent.getExtras().getParcelableArray("datapoint");
                for (int i = 0; i < arrayDataPoint.length; ++i) {
                    TXDataPoint dp = (TXDataPoint)(arrayDataPoint[i]);
                    try {
                        switch((int) dp.property_id) {
                            case 1600006:   //主门开锁
                            {
                                TXDataPoint dpa[] = new TXDataPoint[1];
                                int status = Integer.parseInt(dp.property_val);
                                Intent ds_intent = new Intent();
                                ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                                ds_intent.putExtra("index",0);
                                ds_intent.putExtra("status",status);
                                sendBroadcast(ds_intent);



                                dpa[0] = dp;
                                Log.d(TAG, "onReceive: "+ Arrays.toString(dpa));
                                TXDeviceService.reportDataPoint(dpa);
                            }
                            break;
                            case 100003101: //副门开锁
                            {
                                int status = Integer.parseInt(dp.property_val);
                                Intent ds_intent = new Intent();
                                ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                                ds_intent.putExtra("index",1);
                                ds_intent.putExtra("status",status);
                                sendBroadcast(ds_intent);
                            }
                            break;
                        }

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
                /* 接收电池信息的函数,如果需要外电断电时关闭机器,可以在这里做
                 * “status”（int类型）…状态，定义值是BatteryManager.BATTERY_STATUS_XXX。
                 * “health”（int类型）…健康，定义值是BatteryManager.BATTERY_HEALTH_XXX。
                 * “present”（boolean类型） “level”（int类型）…电池剩余容量 “scale”（int类型）…电池最大值。通常为100。
                 * “icon-small”（int类型）…图标ID。
                 * “plugged”（int类型）…连接的电源插座，定义值是BatteryManager.BATTERY_PLUGGED_XXX。
                 * “voltage”（int类型）…mV。 “temperature”（int类型）…温度，0.1度单位。例如 表示197的时候，意思为19.7度。
                 * “technology”（String类型）…电池类型，例如，Li-ion等等。
                 */
                int status = intent.getIntExtra("status", 0);
                int health = intent.getIntExtra("health", 0);
                boolean present = intent.getBooleanExtra("present", false);
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 0);
                int icon_small = intent.getIntExtra("icon-small", 0);
                int plugged = intent.getIntExtra("plugged", 0);
                int voltage = intent.getIntExtra("voltage", 0);
                int temperature = intent.getIntExtra("temperature", 0);
                String technology = intent.getStringExtra("technology");
                String statusString = "";

                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        statusString = "unknown";
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString = "charging";      //正在充电
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = "discharging";   //停止充电
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = "not charging";  //没有充电
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = "full";          //电池充满
                        break;
                }

                String healthString = "";
                switch (health) {
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        healthString = "unknown";
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        healthString = "good";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        healthString = "overheat";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        healthString = "dead";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        healthString = "voltage";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        healthString = "unspecified failure";
                        break;
                }

                String acString = "";
                switch (plugged) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        acString = "plugged ac";                                    //电源供电
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        acString = "plugged usb";                                   //USB供电

                        break;
                    default:
                        acString = "plugged battery only";                          //
                        if(mPlugedShutdown) {
                            runShutdown();                                                        // 当检测出是电池供电时，直接执行关机程序
                        }
                        break;
                }
				/*
				Log.v("status", statusString);
                Log.v("health", healthString);
				Log.v("present", String.valueOf(present));
				Log.v("level", String.valueOf(level));
				Log.v("scale", String.valueOf(scale));
				Log.v("icon_small", String.valueOf(icon_small));
				Log.v("plugged", acString);
				Log.v("voltage", String.valueOf(voltage));
				Log.v("temperature", String.valueOf(temperature));
				Log.v("technology", technology);
				*/
            }
        }
    }

    public static final class UEventMap {
        // collection of key=value pairs parsed from the uevent message
        private final HashMap<String,String> mMap = new HashMap<String,String>();

        public UEventMap(String message) {
            int offset = 0;
            int length = message.length();

            if(length == 0)return;
            if(message.substring(0,1).equals("{")){
                message = message.substring(1);
            }
            if(message.substring(message.length() - 1,message.length()).equals("}")){
                message = message.substring(0,message.length() - 1);
            }
            length = message.length();
            while (offset < length) {
                int equals = message.indexOf('=', offset);
                int at = message.indexOf(',', offset);
                if (at < 0) break;

                if (equals > offset && equals < at) {
                    // key is before the equals sign, and value is after
                    mMap.put(message.substring(offset, equals).trim(),
                            message.substring(equals + 1, at).trim());
                }

                offset = at + 1;
            }
        }

        public String get(String key) {
            return mMap.get(key);
        }

        public String get(String key, String defaultValue) {
            String result = mMap.get(key);
            return (result == null ? defaultValue : result);
        }

        public String toString() {
            return mMap.toString();
        }
    }

}
