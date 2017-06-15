package com.androidex.face.door;

/**
 * Created by yangjun on 16/6/6.
 * 主要服务类,DoorLock主要提供开门,关门指令以及上报门开和关闭的事件.
 */
/*
public class DoorLock extends Service {
    public static final String TAG = "DoorLock";
    public static final String DoorLockOpenDoor          = "DoorLockOpenDoor";
    private DoorLockServiceBinder mDoorLockServiceBinder;
    private NotifityBroadCast mNotifityBroadCast;
    private static DoorLock mServiceInstance = null;
    public static DoorLock getInstance()
    {
        return mServiceInstance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mServiceInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDoorLockServiceBinder = new DoorLockServiceBinder();
        mNotifityBroadCast = new NotifityBroadCast();
        //注册广播
        initBroadCast();
        return super.onStartCommand(intent, flags, startId);
    }

    */
/**
     * 注册广播
     *
     *
     *//*

    private void initBroadCast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FaceCardActivity.DOOR_ACTION);
        registerReceiver(mNotifityBroadCast,intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class DoorLockServiceBinder {
        String rkeyDev = "/dev/rkey";
        int ident = 0;

        */
/**
         * 开门指令
         * @param index     门的序号,主门=0,副门=1
         * @param delay     延迟关门的时间,0表示不启用延迟关门,大于0表示延迟时间,延迟时间为delay*150ms
         * @return          大于0表示成功,实际上等于9表示真正的成功,因为返回值表示写入的数据,开门指令长度为9.
         *//*

        public int openDoor(int index, int delay){
            kkfile rkey = new kkfile();

            if(index < 0 || index > 0xFE) index = 0;
            if(ident < 0 || ident > 0xFE) ident = 0;
            if(delay < 0 || delay > 0xFE) delay = 0;
            String cmd = String.format("FB%02X2503%02X01%02X00FE",ident,index,delay);
            int r = rkey.native_file_writeHex(rkeyDev,cmd);
            if(r > 0) {
                SoundPoolUtil.getSoundPoolUtil().loadVoice(getBaseContext(),4681);
            }
            Log.d(TAG,"r="+r);
            return r > 0?1:0;
        }

        public int closeDoor(int index){
            kkfile rkey = new kkfile();
            if(index < 0 || index > 0xFE) index = 0;
            if(ident < 0 || ident > 0xFE) ident = 0;
            String cmd = String.format("FB%02X2503%02X000000FE",ident,index);
            int r = rkey.native_file_writeHex(rkeyDev,cmd);
            return r > 0 ? 1:0;
        }
    }

    public class NotifityBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (FaceCardActivity.DOOR_ACTION.equals(action)){//收到开门广播
                if (mDoorLockServiceBinder!=null){//开门
                    mDoorLockServiceBinder.openDoor(0xF0,0x40);
                }
            }
        }
    }
}
*/
