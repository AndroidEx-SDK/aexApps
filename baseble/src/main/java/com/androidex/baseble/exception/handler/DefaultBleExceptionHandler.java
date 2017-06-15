package com.androidex.baseble.exception.handler;

import com.androidex.baseble.exception.ConnectException;
import com.androidex.baseble.exception.GattException;
import com.androidex.baseble.exception.InitiatedException;
import com.androidex.baseble.exception.OtherException;
import com.androidex.baseble.exception.TimeoutException;
import com.androidex.baseble.utils.BleLog;

/**
 * @Description: 异常默认处理
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 16/8/14 10:35.
 */
public class DefaultBleExceptionHandler extends BleExceptionHandler {
    @Override
    protected void onConnectException(ConnectException e) {
        BleLog.e(e.getDescription());
    }

    @Override
    protected void onGattException(GattException e) {
        BleLog.e(e.getDescription());
    }

    @Override
    protected void onTimeoutException(TimeoutException e) {
        BleLog.e(e.getDescription());
    }

    @Override
    protected void onInitiatedException(InitiatedException e) {
        BleLog.e(e.getDescription());
    }

    @Override
    protected void onOtherException(OtherException e) {
        BleLog.e(e.getDescription());
    }
}
