package com.example.seriport;

import java.io.FileDescriptor;

import jni.util.Utils;

/**
 * Created by simon on 2016/7/8.
 */
public class SerialPort {
    static {
        try {
            Utils.PrintLog(5,"JNI", "try to load libserial_port.so");
            System.loadLibrary("serial_port");
            // 加载本地库,也就是JNI生成的libxxx.so文件，下面再说。
        } catch (UnsatisfiedLinkError ule) {
            Utils.PrintLog(5,"JNI", "WARNING: Could not load libserial_port.so");
        }
    }
    //打开读卡器
    public static native FileDescriptor open(String path, int baudrate, int flags);
    // 关闭读卡器
    public static native void close();
}
