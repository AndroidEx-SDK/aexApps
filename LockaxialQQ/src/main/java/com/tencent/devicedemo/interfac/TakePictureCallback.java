package com.tencent.devicedemo.interfac;

/**
 * Created by cts on 17/6/8.
 */

public  interface TakePictureCallback {
    public void beforeTakePickture(final String thisValue, final boolean isCall, String uuid);

    public void afterTakePickture(final String thisValue, String fileUrl, final boolean isCall, String uuid);
}
