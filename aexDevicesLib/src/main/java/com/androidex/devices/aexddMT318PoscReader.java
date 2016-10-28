package com.androidex.devices;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by yangjun on 2016/10/24.
 */

public class aexddMT318PoscReader extends aexddPoscReader {
    public aexddMT318PoscReader(Context ctx) {
        super(ctx);
    }

    public aexddMT318PoscReader(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    @Override
    public String getDeviceName() {
        return "多合一读卡器MT318";
    }
}
