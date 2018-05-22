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
    public aexddPbocReader mBankCardReader = null;
    /**
     * 电动读卡器接口类
     */
    public aexddPbocReader mCRT310CardReader = null;

    /**
     * 燃气电力读卡器接口类
     */
    public aexddPbocReader mCasCardReader = null;
    /**
     * 非接触读卡器类
     */
    public aexddNfcReader mNfcReader = null;
    /**
     * 加密密码键盘接口类
     */
    public aexddPasswordKeypad mPasswordKeypad = null;
    public aexddPasswordKeypad mZTPasswordKeypad = null;
    /**
     * 指纹仪接口类
     */
    public aexddBiovo mX3Biovo = null;

    public appDevicesManager(Context ctx) {
        super(ctx);
        mConfig = new appDevicesConfig(ctx);
        mHwservice = new appDevicesService(ctx);
        mNfcReader = aexddAndroidNfcReader.getInstance(ctx);

        mPrinter = new aexddB58Printer(ctx, mConfig.mConfigPrinter);
        mBankCardReader = new aexddMT319Reader(ctx, mConfig.mConfigBankReader);
        //mCasCardReader = new aexddLCC1Reader(ctx, mConfig.mConfigCasReader);//莱卡、
        mCasCardReader = new aexddMT319Reader(ctx,mConfig.mConfigCasReader);//燃气卡
        mPasswordKeypad = new aexddKMY350(ctx, mConfig.mConfigPasswordKeypad);
        mZTPasswordKeypad = new aexddZTC70(ctx, mConfig.mConfigPasswordKeypad);
        mX3Biovo = new aexddX3Biovo(ctx, mConfig.mConfigBiovo);//指纹仪
        mCRT310CardReader = new aexddCRT310Reader(ctx, mConfig.mConfigBiovo);//电动读卡

    }

    public void setContext(Context ctx) {
        mConfig.setContext(ctx);
        mHwservice.setContext(ctx);
        mPrinter.setContext(ctx);
        mBankCardReader.setContext(ctx);
        mCasCardReader.setContext(ctx);
        mPasswordKeypad.setContext(ctx);
        mNfcReader.setContext(ctx);
        mX3Biovo.setContext(ctx);
        mCRT310CardReader.setContext(ctx);
    }

    public static final appDevicesManager getDevicesManager(Context ctx) {
        if (mDevicesManager == null) {
            mDevicesManager = new appDevicesManager(ctx);
        } else {
            mDevicesManager.setContext(ctx);
        }
        return mDevicesManager;
    }
}
