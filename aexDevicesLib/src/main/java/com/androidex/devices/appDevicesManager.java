package com.androidex.devices;

import android.content.Context;

import com.androidex.aexapplibs.appLibsDevices;

/**
 *
 */

/**
 * Created by yangjun on 2016/10/26.
 */

public class appDevicesManager extends appLibsDevices {
    private static appDevicesManager mDevicesManager = null;
    /**
     * app的配置类
     */
    public appDevicesConfig mConfig = null;
    /**
     * 为app提供服务的硬件接口类
     */
    public appDevicesService mHwservice = null;
    /**
     * 打印机接口类
     */
    public aexddPrinter mPrinter = null;
    /**
     * 银行卡读卡器接口类
     */
    public aexddPoscReader mBankCardReader = null;
    /**
     * 燃气电力读卡器接口类
     */
    public aexddPoscReader mCasCardReader = null;
    /**
     * 非接触读卡器类
     */
    public aexddNfcReader mNfcReader = null;
    /**
     * 加密密码键盘接口类
     */
    public aexddPasswordKeypad mPasswordKeypad = null;

    public appDevicesManager(Context ctx) {
        super(ctx);
        mConfig = new appDevicesConfig(ctx);
        mHwservice = new appDevicesService(ctx);

        mPrinter = new aexddB58Printer(ctx,mConfig.mConfigPrinter);
        mBankCardReader = new aexddMT318Reader(ctx,mConfig.mConfigBankReader);
        mCasCardReader = new aexddMT318Reader(ctx,mConfig.mConfigCasReader);
        if (mConfig.mConfigPasswordKeypad!=null){
            mPasswordKeypad = new aexddKMY350(ctx, mConfig.mConfigPasswordKeypad);
        }

        mNfcReader = new aexddAndroidNfcReader(ctx);

    }

    public void setContext(Context ctx)
    {
        mConfig.setContext(ctx);
        mHwservice.setContext(ctx);
        mPrinter.setContext(ctx);
        mBankCardReader.setContext(ctx);
        mCasCardReader.setContext(ctx);
        mPasswordKeypad.setContext(ctx);
        mNfcReader.setContext(ctx);
    }

    public static final appDevicesManager getDevicesManager(Context ctx)
    {
        if(mDevicesManager == null){
            mDevicesManager = new appDevicesManager(ctx);
        }else{
            mDevicesManager.setContext(ctx);
        }
        return mDevicesManager;
    }
}
