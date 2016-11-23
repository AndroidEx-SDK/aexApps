package com.androidex.devices;

import android.content.Context;

import com.androidex.apps.aexdeviceslib.R;
import com.androidex.logger.Log;

import org.json.JSONObject;

/**
 * Created by cts on 16/11/21.
 */

public class aexddZTC70 extends aexddKMY350 {
      static
      {
            try {
                  System.loadLibrary("appDevicesLibs");
            } catch (UnsatisfiedLinkError e) {
                  Log.d("ZTC70", "appDevicesLibs.so library not found!");
            }
      }

      public aexddZTC70(Context ctx) {
            super(ctx);
      }

      public aexddZTC70(Context ctx, JSONObject args) {
            super(ctx, args);
      }

      @Override
      public String getDeviceName() {

            return mContext.getString(R.string.DEVICE_READER_ZTC70);
      }
}
