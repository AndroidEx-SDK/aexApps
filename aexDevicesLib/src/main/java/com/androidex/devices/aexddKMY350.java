package com.androidex.devices;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by yangjun on 2016/10/24.
 */

public class aexddKMY350 extends aexddPasswordKeypad {
    public aexddKMY350(Context ctx) {
        super(ctx);
    }

    public aexddKMY350(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    @Override
    public String getDeviceName() {
        return "凯名扬密码键盘KMY350";
    }
}
