package com.androidex.comassistant;

import android.util.Log;

import java.io.IOException;


public class ScannerController {
    int iStatus = 0;//0关机，1开机，2扫码中 3未扫码
    Process p;

    ///*	//T1
    String retLevelmode = "echo -wmode 19 0 > /sys/devices/virtual/misc/mtgpio/pin\n";
    String retLeveldir = "echo -wdir 19 1 > /sys/devices/virtual/misc/mtgpio/pin\n";
    String retLevelen = "echo -wpen 19 0 > /sys/devices/virtual/misc/mtgpio/pin\n";
    String retLevelOn = "echo -wdout 19 1 > /sys/devices/virtual/misc/mtgpio/pin\n";
    String retLevelOff = "echo -wdout 19 0 > /sys/devices/virtual/misc/mtgpio/pin\n";

    String retPowermode = "echo -wmode 1 0 > /sys/devices/virtual/misc/mtgpio/pin\n";
    String retPowerdir = "echo -wdir 1 1 > /sys/devices/virtual/misc/mtgpio/pin\n";
    String retPoweren = "echo -wpen 1 0 > /sys/devices/virtual/misc/mtgpio/pin\n";
    String retPowerOn = "echo -wdout 1 1 > /sys/devices/virtual/misc/mtgpio/pin\n";
    String retPowerOff = "echo -wdout 1 1 > /sys/devices/virtual/misc/mtgpio/pin\n";


    String retTXmode = "echo -wmode 57 3 > /sys/devices/virtual/misc/mtgpio/pin\n";
    //String retTXdir = "echo -wdir 57 1 > /sys/devices/virtual/misc/mtgpio/pin\n";
    String retTXen = "echo -wpen 57 0 > /sys/devices/virtual/misc/mtgpio/pin\n";
    //String retTXScan = "echo -wdout 57 0 > /sys/devices/virtual/misc/mtgpio/pin\n";
    //String retTXScan = "echo -wdout 57 1 > /sys/devices/virtual/misc/mtgpio/pin\n";

    String retGPIO_1 = "echo 1 > /d/gpio100\n";
    String retGPIO_0 = "echo 1 > /d/gpio97\n";
    //String retGPIO71msk = "lookat -s 0x80 0x40280204\n";
    //String retGPIO71dir = "lookat -s 0x80 0x40280208\n";
    //String retGPIO71wrdata = "lookat -s 0x00 0x40280200\n";

    /*******
     * 设备2的操作指令
     ******/
    String devices2_serialport_jurisdiction = "chmod 666 /dev/ttyHSL1\n";//串口文件权限申请
    String devices2_serialport_setenforce = "setenforce 0\n";//串口文件权限申请

    String devices2_vcc = "echo 1 > /sys/class/leds/gps_pwr/brightness\n";
    String devices2_vcc2 = "echo 1 > /sys/class/leds/gps_rst/brightness\n";

    String devices2_vcc_close = "echo 0 > /sys/class/leds/gps_pwr/brightness\n";
    String devices2_vcc2_close = "echo 0 > /sys/class/leds/gps_rst/brightness\n";

    public ScannerController() {

    }

    public void initGPIO() {
        try {

            this.p = Runtime.getRuntime().exec("sh");

            this.p.getOutputStream().write(retGPIO_1.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(retGPIO_0.getBytes());
            this.p.getOutputStream().flush();

            Log.e("xxxxScannerController:", "gpio初始化成功");
        } catch (Exception e) {
            Log.e("xxxxScannerController:", "gpio初始化失败");
        }
    }

    /**
     * 无扫描头的设备，打开VCC
     */
    public boolean openVCC2() {
        try {

            this.p = Runtime.getRuntime().exec("sh");

            this.p.getOutputStream().write(devices2_vcc.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(devices2_vcc2.getBytes());
            this.p.getOutputStream().flush();

            Log.e("xxxxScannerController:", "设备2--VCC打开成功");
        } catch (Exception e) {
            Log.e("xxxxScannerController:", "设备2-VCC打开失败");
            return false;
        }
        return true;
    }

    /**
     * 串口申请权限
     *
     * @return
     */
    public boolean getSerialportJurisdiction() {

        try {

//            this.p = Runtime.getRuntime().exec("su");
//
//            this.p.getOutputStream().write(devices2_serialport_jurisdiction.getBytes());
//            this.p.getOutputStream().flush();
//
//            this.p.getOutputStream().write(devices2_serialport_setenforce.getBytes());
//            this.p.getOutputStream().flush();

            String s = MyFunc.runShellCommand(devices2_serialport_jurisdiction);
            String s1 = MyFunc.runShellCommand(devices2_serialport_setenforce);

//            String command = "chmod 777 " + "/dev/ttyHSL1";
//            Runtime runtime = Runtime.getRuntime();
//            runtime.exec(command);

//            CommandExecution.CommandResult commandResult = CommandExecution.execCommand(devices2_serialport_jurisdiction, true);
//
//            CommandExecution.CommandResult commandResult1 = CommandExecution.execCommand(devices2_serialport_setenforce, true);
//            if (commandResult.result>0){
//
//                Log.e("xxxxcommandResult", commandResult.successMsg+"===commandResult1:"+commandResult1.successMsg);
//            }else {
//                Log.e("xxxxcommandResult", commandResult.errorMsg+"===commandResult1:"+commandResult1.successMsg);
//            }


            Log.e("xxxxS:", s + "===S1" + s1);

            Log.e("xxxxScannerController:", "设备2-串口申请权限成功");
        } catch (Exception e) {
            Log.e("xxxxScannerController:", "设备2-串口申请权限失败");
            return false;
        }
        return true;

    }


    /**
     * 无扫描头的设备，关闭VCC
     */
    public boolean closeVCC2() {

        try {

            this.p = Runtime.getRuntime().exec("sh");

            this.p.getOutputStream().write(devices2_vcc_close.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(devices2_vcc2_close.getBytes());
            this.p.getOutputStream().flush();

            Log.e("xxxxScannerController:", "设备2--VCC关闭");
        } catch (Exception e) {
            Log.e("xxxxScannerController:", "设备2-VCC关闭失败");
            return false;
        }
        return true;
    }


    public boolean openScanner() {
        //Process p;
        try {
            //////////////////Trig//////////////////////////
            this.p = Runtime.getRuntime().exec("sh");

            this.p.getOutputStream().write(retGPIO_1.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(retGPIO_0.getBytes());
            this.p.getOutputStream().flush();

            ////////////////////Power/////////////////////////////
            this.p.getOutputStream().write(retPowermode.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(retPowerdir.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(retPoweren.getBytes());
            this.p.getOutputStream().flush();


            this.p.getOutputStream().write(retPowerOn.getBytes());
            this.p.getOutputStream().flush();

            //////////////////Level Shifer//////////////////////////
            this.p.getOutputStream().write(retLevelmode.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(retLeveldir.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(retLevelen.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(retLevelOn.getBytes());
            this.p.getOutputStream().flush();

            //////////////////tx //////////////////////////
            this.p.getOutputStream().write(retTXmode.getBytes());
            this.p.getOutputStream().flush();

            this.p.getOutputStream().write(retTXen.getBytes());
            this.p.getOutputStream().flush();
            Log.e("xxxxScannerController:", "打开VCC供电成功");

        } catch (IOException e) {
            Log.e("xxxxScannerController:", "打开VCC供电失败");

            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        this.iStatus = 1;

        return true;
    }

    public boolean close() {
        //Process p;  
        //FOR T1
        try {
            //p = Runtime.getRuntime().exec("sh");
            this.p.getOutputStream().write(retLevelOff.getBytes());
            this.p.getOutputStream().flush();
            Log.e("xxxxScannerController:", "关闭VCC供电成功");
            //GC(this.p);
        } catch (IOException e) {
            Log.e("xxxxScannerController:", "关闭VCC供电失败");
            // TODO Auto-generated catch block  
            e.printStackTrace();
        }


        try {
            //p = Runtime.getRuntime().exec("sh");
            this.p.getOutputStream().write(retPowerOff.getBytes());
            this.p.getOutputStream().flush();
            Log.e("xxxxScannerController:", "关闭VCC供电成功");
            //GC(this.p);
        } catch (IOException e) {
            Log.e("xxxxScannerController:", "关闭VCC供电失败");
            // TODO Auto-generated catch block  
            e.printStackTrace();
        }
        return true;
    }

}
 
