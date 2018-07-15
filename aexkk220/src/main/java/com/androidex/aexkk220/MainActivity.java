package com.androidex.aexkk220;

import android.os.Bundle;

import com.androidex.aexkk220.utils.MacUtil;
import com.androidex.aexlibs.hwService;
import com.androidex.common.AndroidExActivityBase;
import com.androidex.logger.Log;
import com.androidex.sealtalk.aexkk220.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.androidex.aexapplibs.appLibsService.AEX_PARAMETERS_BTMAC;
import static com.androidex.aexapplibs.appLibsService.AEX_PARAMETERS_FLAG0;
import static com.androidex.aexapplibs.appLibsService.AEX_PARAMETERS_FLAG1;
import static com.androidex.aexapplibs.appLibsService.AEX_PARAMETERS_LANMAC;
import static com.androidex.aexapplibs.appLibsService.AEX_PARAMETERS_UUID;
import static com.androidex.aexapplibs.appLibsService.AEX_PARAMETERS_WLANMAC;

public class MainActivity extends AndroidExActivityBase {
    public static final String AEX_PG0 = "/sys/class/aexgpio/mcu/pg0";
    public static final String AEX_PG1 = "/sys/class/aexgpio/mcu/pg1";
    public static final String AEX_PG2 = "/sys/class/aexgpio/mcu/pg2";
    public static final String AEX_USBPORT = "/sys/monitor/usb_port/config/idpin_debug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initConfig();
    }

    private void initConfig() {
        // Log.d(TAG, runShellCommand(String.format("echo \"0x34\" > %s", hwService.aexp_flag0)));
        // Log.d(TAG, runShellCommand(String.format("echo \"0x0C\" > %s", hwService.aexp_flag1)));
        // Log.d(TAG, runShellCommand(String.format("echo \"\" > %s", hwservice.AEX_PARAMETERS_BDUART)));
        //hwservice.writeHex(AEX_PARAMETERS_LANMAC, MacUtil.getNETMacAddress());//此处调用会报错导致机器重启
        //hwservice.writeHex(AEX_PARAMETERS_BTMAC, MacUtil.getBTMacAddress());
        //hwservice.writeHex(AEX_PARAMETERS_WLANMAC, MacUtil.getWIFIMacAddress(this));

        Log.d(TAG, String.format("uuid: %s", MacUtil.getAndroidExParameter(AEX_PARAMETERS_UUID)));
        Log.d(TAG, String.format("flag0:%s", MacUtil.getAndroidExParameter(AEX_PARAMETERS_FLAG0)));
        Log.d(TAG, String.format("flag1:%s", MacUtil.getAndroidExParameter(AEX_PARAMETERS_FLAG1)));
        Log.d(TAG, String.format("lan_mac:%s", MacUtil.getAndroidExParameter(AEX_PARAMETERS_LANMAC)));
        Log.d(TAG, String.format("bt_mac:%s", MacUtil.getAndroidExParameter(AEX_PARAMETERS_BTMAC)));
        Log.d(TAG, String.format("wlan_mac:%s", MacUtil.getAndroidExParameter(AEX_PARAMETERS_WLANMAC)));

        String flag0 = String.format("echo \"0x2200\" > %s", hwService.aexp_flag0);
        String flag1 = String.format("echo \"0x0070\" > %s", hwService.aexp_flag1);
        String LtePowerOn = String.format("echo \"1\" > %s", AEX_PG0);//LTE 电源    默认上啦打开电源
        String LtePowerOff = String.format("echo \"0\" > %s", AEX_PG0);//
        String ClipperChipPowerOn = String.format("echo \"1\" > %s", AEX_PG1);//加密芯片电源   默认上啦打开电源
        String ClipperChipPowerOff = String.format("echo \"0\" > %s", AEX_PG1);//加密芯片电源   默认上啦打开电源
        String ClipperChipModeOn = String.format("echo \"1\" > %s", AEX_PG2);//加密模式的切换
        String ClipperChipModeOff = String.format("echo \"0\" > %s", AEX_PG2);//加密模式的切换
        String Address_Eth1 = String.format("busybox ifconfig eth1 hw ether %s", MacUtil.getEth1Mac());//设置eth1 MAC地址,冷启动时USB网卡eth1是没有MAC地址的
        String IP_Eth0 = String.format("busybox ifconfig eth0 %s netmask %s", "172.30.16.30", "255.255.255.0");//IP和网关需要自己根据需要去定义
        String IP_Eth1 = String.format("busybox ifconfig eth1 %s netmask %s", "172.30.16.31", "255.255.255.0");//IP和网关需要自己根据需要去定义  要先设置eth1的mac地址再设置IP
        String SetRoute = String.format("busybox route add gw %s", "172.30.16.1");//设置路由    由于设备上有两个网口和一个4G，因此路由要根据实际情况设置才可以正常工作
        String USB_HOST = String.format("echo \"0\" > %s", AEX_USBPORT);//切换为HOST模式,加密芯片需要使用这个模式
        String USB_OTG = String.format("echo \"1\" > %s", AEX_USBPORT);//切换为OTG模式
        //execRootCommand();
        execShellCommand(flag0, hwService.aexp_flag0);
        execShellCommand(flag1, hwService.aexp_flag1);
        execShellCommand(LtePowerOn, AEX_PG0);
        execShellCommand(ClipperChipPowerOn, AEX_PG1);
//        execShellCommand(Address_Eth1, null);
//        execShellCommand(IP_Eth0, null);
//        execShellCommand(IP_Eth1, null);
//        execShellCommand(SetRoute, null);
        runShellCommand(Address_Eth1);
        runShellCommand(IP_Eth0);
        runShellCommand(IP_Eth1);
        runShellCommand(SetRoute);
        //setAndroidExParameter();
    }

    /**
     * 运行root命令  在KK220固件145上运行失败
     *
     * @param
     */
    public void execRootCommand() {
        String writeFlag1 = String.format("echo \"0x0090\" > %s", hwService.aexp_flag1);
        Log.d(TAG, "execRootCommand : " + hwservice.execRootCommand(writeFlag1));
        Log.d(TAG, String.format("execRootCommand  flag1:%s ", MacUtil.getAndroidExParameter(hwService.aexp_flag1)));
    }

    /**
     * 运行shell指令
     *
     * @param
     */
    public void execShellCommand(String cmd, String path) {
        Log.d(TAG, "execShellCommand : " + hwservice.execShellCommand(cmd));
        if (path != null) {
            Log.d(TAG, String.format("cat  %s: %s", path, MacUtil.getAndroidExParameter(path)));
        }
    }

    public void setAndroidExParameter() {
        Log.d(TAG, "setAndroidExParameter : " + hwservice.setAndroidExParameter(hwService.aexp_flag1, "0x0090"));
        //Log.d(TAG, String.format("setAndroidExParameter flag1: %s ", hwservice.getAndroidExParameter(hwService.aexp_flag1)));
        Log.d(TAG, String.format("setAndroidExParameter flag1: %s ", MacUtil.getAndroidExParameter(hwService.aexp_flag1)));
    }

    public String runShellCommand(String cmd) {
        String ret = "";
        byte[] retBytes = new byte[2048];
        Log.d(TAG, String.format("runShellCommand(%s)", cmd));
        try {
            cmd += "\n";
            Process exeEcho1 = Runtime.getRuntime().exec("su");
            OutputStream ot = exeEcho1.getOutputStream();
            ot.write(cmd.getBytes());
            ot.flush();
            ot.close();
            InputStream in = exeEcho1.getInputStream();
            int r = in.read(retBytes);
            if (r > 0)
                ret = new String(retBytes, 0, r);
        } catch (IOException e) {
            Log.e("AexService", "shell cmd wrong:" + e.toString());
        }
        return ret;
    }
}
