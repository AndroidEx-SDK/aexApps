package com.tencent.wechat;


import android.util.Log;

public class Cloud {

	private static final String LOG_TAG = "Cloud";
	
	public static final int WECHAT_CLOUD_SERVICE = 0x0001;
	
	private static int EVENT_VALUE_LOGIN 	= 1;
	private static int EVENT_VALUE_LOGOUT 	= 2;
	
	/*
	 * 初始化微信直连SDK，只需调用一次
	 * deviceLicence:
	 * 		设备证书，直连设备在后台授权时由微信生成并返回的证书字符串
	 * 返回值:
	 * 		true表示成功，false为失败，一般是设备证书错误
	 */
	public static native boolean init(String deviceLicence);
	
	/*
	 * 向微信硬件云平台发送业务数据
	 * funcid:
	 * 		使用的微信云平台业务，参照SDK说明文档，对于微信硬件平台能力服务的为：WECHAT_CLOUD_SERVICE
	 * body:
	 * 		要发送的数据内容
	 * 返回值:
	 * 		非0表示成功，返回的值作为改请求的taskid
	 */
	public static native int sendDataToServer(int funcid, String body);
	
	/*
	 * 使用微信硬件平台的固件升级服务，用于查询是否有更新
	 * body:
	 * 		按微信固件升级服务业务要求填充的数据
	 * 返回值:
	 * 		非0表示成功，返回的值作为改请求的taskid
	 */
	public static native int checkUpdate(String body);
	
	/*
	 * 查询设备的VenderId
	 * 输入参数:
	 * 		无
	 * 返回值:
	 * 		设备的VenderId
	 */
	public static native String getVenderId();
	
	/*
	 * 查询设备的DeviceId
	 * 输入参数:
	 * 		无
	 * 返回值:
	 * 		设备的DeviceId
	 */
	public static native String getDeviceId();
	
	/*
	 * 查询SDK的版本号
	 * 输入参数:
	 * 		无
	 * 返回值:
	 * 		直连SDK的库版本
	 */
	public static native String getSDKVersion();
	
	/*
	 * 关闭微信直连SDK，释放资源
	 */
	public static native void release();
	
	/*
	 * 设备onResponseCallback函数，注意此函数由运行于JNI层的线程来调用，建议此方法中不要直接处理业务，而是将数据发送给工作线程来处理
	 * 
	 * taskid:
	 * 		对应所发起的请求taskid
	 * errcode:
	 * 		错误码，0为正常，非0表示出错
	 * funcid:
	 * 		使用的微信云平台业务，参照SDK说明文档，对于微信硬件平台能力服务的为：WECHAT_CLOUD_SERVICE
	 * data:
	 * 		服务器的响应数据
	 */
	public static void onResponseCallback(int taskid, int errcode, int funcid, byte[] data) {
		Log.d(LOG_TAG, "Receive resp:" + taskid + ", errcode:" + errcode + ", funcid:" + funcid);
		String dataString = new String(data);
		Log.d(LOG_TAG, "Data:" + dataString + ", Data len:" + data.length);
	}
	
	/*
	 * 设备onNotifyCallback函数，注意此函数由运行于JNI层的线程来调用，建议此方法中不要直接处理业务，而是将数据发送给工作线程来处理
	 * 
	 * funcid:
	 * 		使用的微信云平台业务，参照SDK说明文档，对于微信硬件平台能力服务的为：WECHAT_CLOUD_SERVICE
	 * data:
	 * 		服务器的推送数据
	 */
	public static void onNotifyCallback(int funcid, byte[] data) {
		Log.d(LOG_TAG, "Receive push notify funcid:" + funcid);
		String dataString = new String(data);
		Log.d(LOG_TAG, "Data:" + dataString + ", Data len:" + data.length);
	}
	
	/*
	 * 设备onNotifyCallback函数，注意此函数由运行于JNI层的线程来调用，建议此方法中不要直接处理业务，而是将数据发送给工作线程来处理
	 * 
	 * funcid:
	 * 		使用的微信云平台业务，参照SDK说明文档，对于微信硬件平台能力服务的为：WECHAT_CLOUD_SERVICE
	 * data:
	 * 		服务器的推送数据
	 */
	public static void onEventCallback(int event) {
		Log.d(LOG_TAG, "Receive event:" + event);
		if (event == EVENT_VALUE_LOGIN) {
			Log.d(LOG_TAG, "Device is login!!");
		}
		else if (event == EVENT_VALUE_LOGOUT){
			Log.d(LOG_TAG, "Device is logout!!");
		}
		else {
			Log.d(LOG_TAG, "Receive unknown event:" + event);
		}
	}
}
