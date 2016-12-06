package com.androidex.devices;

import android.content.Context;

import com.androidex.logger.Log;

import org.json.JSONObject;

/**
 * Created by cts on 16/12/1.
 */

public class aexddLCC1Reader extends aexddPbocReader {
    static {
        try {
            System.loadLibrary("appDevicesLibs");
        } catch (UnsatisfiedLinkError e) {
            Log.d("LKC1", "appDevicesLibs.so library not found!");
        }
    }

    public static final String TAG = "LKC1";

    public aexddLCC1Reader(Context ctx) {
        super(ctx);
    }

    public aexddLCC1Reader(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    @Override
    public int ReciveDataLoop() {
        return 0;
    }

    @Override
    public byte[] pbocReadPacket(int i) {
        return new byte[0];
    }

    @Override
    public int pbocReadCardLoop(int i) {
        return 0;
    }

    @Override
    public void pbocSendCmd(byte[] bytes, int i) {

    }

    @Override
    public void pbocSendHexCmd(String s) {

    }

    @Override
    public String getCardStatusString(int i) {
        return null;
    }

    @Override
    public boolean selfTest() {
        return false;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public int queryStatus() {
        return 0;
    }

    @Override
    public JSONObject getTrackInfo() {
        return null;
    }

    @Override
    public boolean clearTrackInfo() {
        return false;
    }

    @Override
    public int cpuPOR() {
        return 0;
    }

    @Override
    public int cpuReset() {
        return 0;
    }

    @Override
    public int cpuHibernate() {
        return 0;
    }

    @Override
    public byte[] cpuApdu(byte[] bytes) {
        return new byte[0];
    }


}
