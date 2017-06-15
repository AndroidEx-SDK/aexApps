package com.tencent.devicedemo.interfac;

/**
 * Created by xinshuhao on 16/7/24.
 */
public interface NetworkCallBack<T>
{
    public void onSuccess(T t);
    public void onFailure(String error);
}

