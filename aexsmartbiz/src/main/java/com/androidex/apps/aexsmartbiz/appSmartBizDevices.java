package com.androidex.apps.aexSmartBiz;

import android.content.Context;

import com.androidex.aexapplibs.appLibsDevices;
import com.androidex.devices.aexddB58Printer;
import com.androidex.devices.aexddMT318PoscReader;
import com.androidex.devices.aexddNfcReader;
import com.androidex.devices.aexddPasswordKeypad;
import com.androidex.devices.aexddPoscReader;
import com.androidex.devices.aexddPrinter;

/**
 * Created by yangjun on 2016/10/26.
 */

public class appSmartBizDevices extends appLibsDevices {
    /**
     * app的配置类
     */
    public appSmartBizConfig mConfig = null;
    /**
     * 为app提供服务的硬件接口类
     */
    public appSmartBizService mHwservice = null;
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

    public appSmartBizDevices(Context ctx) {
        super(ctx);
        mConfig = new appSmartBizConfig(ctx);
        mHwservice = new appSmartBizService(ctx);

        mPrinter = new aexddB58Printer(ctx,mConfig.mConfigPrinter);
        mBankCardReader = new aexddMT318PoscReader(ctx,mConfig.mConfigBankReader);
        mCasCardReader = new aexddMT318PoscReader(ctx,mConfig.mConfigCasReader);
        mNfcReader = new aexddNfcReader(ctx,mConfig.mConfigPasswordKeypad);

    }
}
