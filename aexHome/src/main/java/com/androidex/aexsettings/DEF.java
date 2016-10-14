package com.androidex.aexsettings;

public class DEF {

	static final public String version				= "1.1";												//软件版本
	static final public String zbdevicesid			= "108105";												//
	static final public String zbdevicesapptype		= "terminal";											//终端类型
	static final public String zbdevicesSocket 		= "114.80.245.187:40009";								//服务器地址
	static final public String defaultZGBpath		= "/dev/ttyS1";											//无线模块配置
	static final public String onekeyboardpath		= "/dev/ttyUSB0";										//一体键盘配置
	static final public String zbdevicesServer 		= "http://114.80.245.187/zbdevices/zbdevices/index.php";	//对应的服务端API接口
	static final public String zbdevicesConfpath		= "/sdcard/zbdevices";
	static final public String zbdevicesConf 			= "/sdcard/zbdevices/log/config.conf";						//的配置文件
	static final public String zbdevicesPath 			= "/sdcard/log/zbdevices";								//存放日志的目录

	static final public int KEY_NULL	= 0x0;		//无按键
	static final public int KEY_K1   	= 0x01;		//K1
	static final public int KEY_K2	    = 0x02;		//K2
	static final public int KEY_K3	    = 0x03;		//K3
	static final public int KEY_K4	    = 0x04;		//K4
	static final public int KEY_K5	    = 0x05;		//K5

}
