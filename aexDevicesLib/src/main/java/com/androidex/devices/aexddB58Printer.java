package com.androidex.devices;

import android.content.Context;

import com.androidex.aexapplibs.R;

import org.json.JSONObject;

/**
 * Created by yangjun on 2016/10/24.
 */

public class aexddB58Printer extends aexddPrinter {
    public aexddB58Printer(Context ctx) {
        super(ctx);
    }

    public aexddB58Printer(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    @Override
    public String getDeviceName() {
        return mContext.getString(R.string.DEVICE_PRINTER_B58);
    }
}
