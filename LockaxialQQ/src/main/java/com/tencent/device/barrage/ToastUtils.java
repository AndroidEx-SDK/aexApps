
package com.tencent.device.barrage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * 显示Toast提示的工具类
 * 
 * @author milesxia
 */
public class ToastUtils {

    protected static ToastUtils mToastUtil = null;

    protected Toast mToast = null;

    /**
     * 获取实例
     * 
     * @return
     */
    public static ToastUtils getInstance() {
        if (mToastUtil == null) {
            mToastUtil = new ToastUtils();
        }
        return mToastUtil;
    }

    protected Handler handler = new Handler(Looper.getMainLooper());
    
    /**
     * 显示Toast，多次调用此函数时，Toast显示的时间不会累计，并且显示内容为最后一次调用时传入的内容
     * 持续时间默认为short
     * @param context 调用者context
     * @param tips 要显示的内容
     *            {@link Toast#LENGTH_LONG}
     */
    public void showToast(final Context context, final String tips){
    	showToast(context, tips, Toast.LENGTH_SHORT);
    }
    
    @SuppressWarnings("ucd")
    public void showToast(final Context context, final int tips){
        showToast(context, tips, Toast.LENGTH_SHORT);
    }
    /**
     * 显示Toast，多次调用此函数时，Toast显示的时间不会累计，并且显示内容为最后一次调用时传入的内容
     * 
     * @param context 调用者context
     * @param tips 要显示的内容
     * @param duration 持续时间，参见{@link Toast#LENGTH_SHORT}和
     *            {@link Toast#LENGTH_LONG}
     */
    public void showToast(final Context context, final String tips, final int duration) {
        if (TextUtils.isEmpty(tips)) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(context, tips, duration);
                    mToast.show();
                } else {
                    //mToast.cancel();
                    //mToast.setView(mToast.getView());
                    mToast.setText(tips);
                    mToast.setDuration(duration);
                    mToast.show();
                }
            }
        });
    }
    
    public void showToast(final Context context, final int tips, final int duration) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(context, tips, duration);
                    mToast.show();
                } else {
                    //mToast.cancel();
                    //mToast.setView(mToast.getView());
                    mToast.setText(tips);
                    mToast.setDuration(duration);
                    mToast.show();
                }
            }
        });
    }
}
