package com.brocast;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;

import com.androidex.DoorLock;
import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;
import com.tencent.devicedemo.AudioRecordActivity;
import com.tencent.devicedemo.BinderListAdapter;
import com.tencent.devicedemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cts on 17/4/27.
 * 是否进入引导页的广播接收器
 */

public class NotifyReceiverQQ extends BroadcastReceiver{
    private static final String TAG = "NotifyReceiverQQ";
    Parcelable[] listTemp1;
    public Context ctx;
    public BinderListAdapter mAdapter;
    public ImageView iv_bind;
    public AlertDialog dialog;
    public CallBack mCallBack;

    public NotifyReceiverQQ(Context ctx, BinderListAdapter mAdapter, ImageView iv_bind, AlertDialog dialog) {
        this.ctx = ctx;
        this.mAdapter = mAdapter;
        this.iv_bind = iv_bind;
        this.dialog = dialog;
    }

    private void showAlert(String strTitle, String strMsg) {
        // TODO Auto-generated method stub
        AlertDialog dialogError;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx).setTitle(strTitle).setMessage(strMsg).setPositiveButton("取消", null).setNegativeButton("确定", null);
        dialogError = builder.create();
        dialogError.show();
    }

    public Parcelable[] getListTemp1() {
        return listTemp1;
    }

    public interface CallBack{
        void callBackList(Parcelable[] list);
    }

    public void setmCallBack(CallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == TXDeviceService.BinderListChange) {
            Parcelable[] listTemp = intent.getExtras().getParcelableArray("binderlist");
            mCallBack.callBackList(listTemp);
            Log.d(TAG, "=====mCallBack.callBackList：" + listTemp);
            List<TXBinderInfo> binderList = new ArrayList<TXBinderInfo>();
            for (int i = 0; i < listTemp.length; ++i) {
                TXBinderInfo binder = (TXBinderInfo) (listTemp[i]);
                binderList.add(binder);
            }
            if (mAdapter != null) {
                mAdapter.freshBinderList(binderList);
            }

            if (binderList.size() > 0) {
                if (iv_bind!=null){
                    Log.d(TAG, "" + ": +++++++");
                    iv_bind.setImageDrawable(ctx.getResources().getDrawable(R.mipmap.binder_default_head));
                }
            } else {
                if (iv_bind!=null){
                    Log.d(TAG, "onReceive: ----------");
                    iv_bind.setImageDrawable(ctx.getResources().getDrawable(R.mipmap.bind_offline));
                }
            }
        } else if (intent.getAction() == TXDeviceService.OnEraseAllBinders) {
            int resultCode = intent.getExtras().getInt(TXDeviceService.OperationResult);
            if (0 != resultCode) {
                showAlert("解除绑定失败", "解除绑定失败，错误码:" + resultCode);
            } else {
                showAlert("解除绑定成功", "解除绑定成功!!!");
                if (iv_bind!=null){
                    Log.d(TAG, "onReceive: ==========");
                    iv_bind.setImageDrawable(ctx.getResources().getDrawable(R.mipmap.bind_offline));
                }
            }

        } else if (intent.getAction() == DoorLock.DoorLockStatusChange) {
            //门禁状态改变事件
            //showAlert("门禁状态改变",intent.getStringExtra("doorsensor"));
            String doorsendor = String.format("doorsensor=%s", intent.getStringExtra("doorsensor"));
            Log.d("NotifyReceiverQQ", doorsendor);

        } else if (intent.getAction() == TXDeviceService.wifisetting) {
           // if (!NetWork.isNetworkAvailable(ctx))
               // ctx.startActivity(new Intent(ctx, WifiConnActivity.class));
        } else if (intent.getAction().equals(DoorLock.DoorLockOpenDoor)) {
            if (dialog != null && dialog.isShowing()) {
                try {
                    Thread.sleep(3000);/*休眠三秒*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        } else if (intent.getAction().equals(TXDeviceService.voicereceive)) {
            String filepath = intent.getStringExtra("filepath");
            if ("".equals(filepath)) return;
            Intent intent1 = new Intent(ctx, AudioRecordActivity.class);
            intent1.putExtra("filepath", filepath);
            ctx.startActivity(intent1);

        } else if (intent.getAction().equals(TXDeviceService.isconnected)) {
            String ishave = intent.getStringExtra("ishave");
            if (!"".equals(ishave) && ishave.equals("yes")) {
                Log.d(TAG, "onReceive: 0000000000");
                iv_bind.setImageDrawable(ctx.getResources().getDrawable(R.mipmap.binder_default_head));
            } else {
                Log.d(TAG, "onReceive: 9999999999");
                iv_bind.setImageDrawable(ctx.getResources().getDrawable(R.mipmap.bind_offline));
            }

        }
    }
}
