package com.tencent.device;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.system.StructPollfd;
import android.util.Log;
import android.widget.Toast;

import com.androidex.plugins.kkaexparams;
import com.tencent.av.VideoService;
import com.tencent.device.barrage.BarrageContext;
import com.tencent.device.barrage.BarrageMsg;
import com.tencent.device.barrage.BarrageMsg.GroupMsg;
import com.tencent.device.barrage.IBarrageListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.androidex.GetUserInfo.get_authinfo;

/**
 * @author dennyfeng 
 *
 */
public class TXDeviceService extends Service
{
    static String TAG = "TXDeviceService";  

    static
    {
        try 
        {
            System.loadLibrary("stlport_shared");
            System.loadLibrary("txdevicesdk");
            System.loadLibrary("TVSDK");
        } 
        catch (UnsatisfiedLinkError e)
        {
            e.printStackTrace();
        }
    }
    static final public int MAX_PARAM_SIZE          = 4096;

    public static final int NETWORK_TYPE_WIFI     = 1;	//wifi网络以及除了移动、联通、电信、香港之外的网络
    
    public static final int NETWORK_TYPE_MOBILE   = 2;	//移动网络 

    public static final int NETWORK_TYPE_UNICOM   = 3;	//联通网络 
    
    public static final int NETWORK_TYPE_TELECOM  = 4;	//电信网络
    
    public static final int NETWORK_TYPE_HONGKONG = 5;	//香港
    
    public static final int SDK_RUN_MODE_DEFAULT = 0;	//SDK运行在正常模式
    
    public static final int SDK_RUN_MODE_LOW_POWER = 1;	//SDK运行在低功耗模式


    public static final int transfer_channeltype_FTN          = 1;
    public static final int transfer_channeltype_MINI         = 2;

    public static final int transfer_filetype_image           = 1;    //图片文件
    public static final int transfer_filetype_video           = 2;    //视频文件
    public static final int transfer_filetype_audio           = 3;    //语音文件
    public static final int transfer_filetype_other           = 4;    //其它文件

    public static final String wifisetting 	 = "WifiSettingMain";   //wifi设置
    public static final String voicereceive 	 = "voicereceive";   //接受语音
    public static final String isconnected 	 = "isconnected";   //是否连接

    public static final String BinderListChange 	 = "BinderListChange";   //绑定列表变化
    
    public static final String OnEraseAllBinders  = "OnEraseAllBinders";  //解除所有用户绑定的回调通知

    public static final String OnReceiveDataPoint = "OnReceiveDataPoint"; //收到DataPoint消息				
    
    public static final String OperationResult    = "OperationResult";    //操作结果
    
    public static final String OTAOnNewPkgCome    = "OTAOnNewPkgCome";
    
    public static final String OTAOnDownloadProgress = "OTAOnDownloadProgress";
    
    public static final String OTAOnDownloadComplete = "OTAOnDownloadComplete";
    
    public static final String OTAOnUpdateConfirm    = "OTAOnUpdateConfirm"; 
    
    public static final boolean NetworkSettingMode 	= false;				// 打开此开关可以演示配网模式

    public static final String OnReceiveWifiInfo 	= "__OnReceiveWifiInfo__"; 
 
    public static final String WifiInfo_SSID 		= "WifiInfo_SSID";
 
    public static final String WifiInfo_PASS 		= "WifiInfo_PASS";
    
    public static final String WifiInfo_IP 			= "WifiInfo_IP";
    
    public static final String WifiInfo_PORT 		= "WifiInfo_PORT";
 

    private static TXDeviceService mServiceInstance = null;
    
    private TXDeviceServiceBinder mServiceBinder = null;
    
    private Handler mToastMessageHandler = null;
    
    public static TXDeviceService getInstance()
    {
        return mServiceInstance;
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return mServiceBinder;
    }
    
    @Override
    public void onCreate()
    {
    		super.onCreate();
    		mServiceBinder = new TXDeviceServiceBinder();
    }
    
    @SuppressWarnings("deprecation")
    @Override   
    public void onStart(Intent intent, int startId)
    {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        
        mServiceInstance = this;  
        initJNI();   
        
        if (mToastMessageHandler == null) {
        		mToastMessageHandler = new Handler();
        }

        initDevice();
    }    

    @Override
    public void onDestroy()    
    {
        // TODO Auto-generated method stub
        Log.i(TAG, "TXDeviceService: onDestroy"); 
        super.onDestroy();
        mServiceInstance = null;
        mServiceBinder = null;
    }

    /**
     * 获取设备的GUID
     * @return  字符串表示的Serial
     */
    static public String get_serial()
    {
        kkaexparams aexparams = new kkaexparams();

        return aexparams.get_serial();
    }



    public void initDevice()
    {
        kkaexparams aexparams = new kkaexparams();
        JSONObject authinfo = get_authinfo();
        String strGUID;
        String strLicense;
        String srvPubKey;

        // SDK测试
        if(authinfo.optString("pid","").isEmpty()) {
            try {
                authinfo.put("pid", 1700003316);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(authinfo.optString("sn","").isEmpty()) {
            try {
                authinfo.put("sn",get_serial());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(authinfo.optString("license","").isEmpty()) {
            try {
                authinfo.put("license","304602210083D57DF91BCD947C52E07EF23E515CEE832B11E489D3CE37E73D0B7CB2203B340221008E0F8E924C2407E476E3837A2FF25E5FDABFCAE079494B29204A1253D400AD15");
            } catch (JSONException e) {
                e.printStackTrace();
            }  
        }
        if(authinfo.optString("pubkey","").isEmpty()) {
            try {
                authinfo.put("pubkey","0404EF63FA5206195F2B2464508E36AE80539A9D8BAFFA21A21AA9980C7B678A8A56E6F6AC7238A1FAB1E745AFA35301DF");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            String userinfo = authinfo.toString(1);
            Log.d(TAG,String.format("authInfo=[%d]%s \n",userinfo.length(),userinfo));
            aexparams.set_userinfo(userinfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        strGUID = authinfo.optString("sn");
        srvPubKey = authinfo.optString("pubkey");
        strLicense = authinfo.optString("license");
        int pid = authinfo.optInt("pid");
        Log.d(TAG,strGUID);
        Log.d(TAG, srvPubKey);
        Log.d(TAG,strLicense);
        Log.d(TAG,pid+"");
        init("锁向门禁机", strLicense.getBytes(), strGUID, srvPubKey, pid, 1, NETWORK_TYPE_WIFI, SDK_RUN_MODE_DEFAULT,
        		this.getCacheDir().getAbsolutePath(), 102400, this.getCacheDir().getAbsolutePath(), 1024000,this.getCacheDir().getAbsolutePath() + "/", 1024000);
        initOTA(5*60, this.getCacheDir().getAbsolutePath()+"/ota.apk");
    }

    /**
     *
     */
    public native void initJNI();
    

    // 初始化电视
    // 参数说明：
    // 1、deviceName：设备名称
    // 2、license： 设备授权码
    // 3、serialNumber：设备序列号，长度不超过16字节
    // 4、srvPubKey: 即通后台分配的公钥
    // 5、productId ：产品ID，厂商向腾讯开放平台部申请
    // 6、productVersion：产品版本
    // 7、networkType：网络类型，取值必须是NETWORK_TYPE_XXXX类型
    // 8、runMode：运行模式，取值必须是SDK_RUN_MODE_XXX类型
    // 9、sysPath： 系统路径，SDK会在该目录下写入保证正常运行必需的配置信息；
    // 10、sysCapacity：系统路径下存储空间大小，SDK对该目录的存储空间要求小（最小大小：10K，建议大小：100K），SDK写入次数较少，读取次数较多
    // 11、appPath： 应用路径，SDK会在该目录下写入运行过程中的异常错误信息
    // 12、appCapacity：应用路径下存储空间大小，SDK对该目录的存储空间要求较大（最小大小：500K，建议大小：1M），SDK写入次数较多，读取次数较少
    // 13、tmpPath： 临时路径，SDK会在该目录下写入临时文件
    // 14、tmpCapacity：临时路径下存储空间大小，SDK对该目录的存储空间要求很大，建议尽可能大一些
    public native void init(String deviceName, byte [] license, String serialNumber, String srvPubKey, long productId, int productVersion, int networkType, int runMode, 
    		String sysPath, long sysCapacity, String appPath, long appCapacity, String tmpPath, long tmpCapacity);

    // 获取SDK版本信息
    // [0] main_verison
    // [1] sub_version
    // [2] build_no
    public native int[] getSDKVersion();
    
    //上传SDK log
    public native void uploadSDKLog();

    //获取绑定列表（返回null时表示正在拉取，需要监听onBinderListChange以得到拉取结果）
    public static native TXBinderInfo[] getBinderList();
 
    // 发送富媒体消息
    // 参数统一说明：  
    // 1、file_path：文件本地路径
    // 2、duration：音频时长
    // 3、thumb_path：缩略图路径
    // 4、title： 手Q上显示的消息标题
    // 5、digest： 手Q上显示的描述消息
    // 6、guide_words：手Q上的引导wording
    // 7、msgId: 动态消息ID，请到配置平台进行注册，没有注册过的ID的消息无法送到手机QQ
    // 8、targetIds: 接收者的tinyid, 填空表示发送给所有绑定者
    // 返回值：long类型的cookie值，可以根据该cookie值在OnRichMsgSendProgress和OnRichMsgSendRet回调中查询进度信息和结果信息
    public static native long sendAudioMsg(String file_path, int duration, int msgId, long[] targetIds);
    public static native long sendVideoMsg(String file_path, String thumb_path, String title, String digest, String guide_words, int msgId, long[] targetIds);
    public static native long sendPictureMsg(String file_path, String thumb_path, String title, String digest, String guide_words, int msgId, long[] targetIds);
    // file_path_url / thumb_path_url:表示可以发送本地文件，或URL
    public static native long sendVideoURLMsg(int msgId, String file_path_url, String thumb_path_url, String title, String digest, String guide_words, long[] targetIds);
    // jump_url 不为空时，该消息点击会跳转到对应页面。不需要使用该功能时传null 即可
    public static native long sendPictureURLMsg(int msgId, String file_path_url, String thumb_path_url, String title, String digest, String guide_words, String jump_url, long[] targetIds);
    
    // 发送强提醒通知
    // 参数说明：
    // digest: 显示在通知界面中的文字
    // msgId:  动态消息ID，请到配置平台进行注册，没有注册过的ID的消息无法送到手机QQ
    // targetIds: 接收者的tinyid,填空表示发送给所有绑定者
    public static native void sendNotifyMsg(String digest, int msgId, long[] targetIds);
    
    // 发送文本消息
    // 参数说明：
    // text:  要发送的文本
    // msgId: 动态消息ID，请到配置平台进行注册，没有注册过的ID的消息无法送到手机QQ
    // targetIds: 接收者的tinyid,填空表示发送给所有绑定者
    public static native void sendTextMsg(String text, int msgId, long[] targetIds);
    
    
    //发送DataPoint
    //接口频率限制1s1次
    //返回发送cookie，发送结果通过onAckDataPointResult异步返回
    public static native int ackDataPoint(long to, TXDataPoint[] arrayDataPoint);
    
    //上报DataPoint
    //接口频率限制1s1次
    //返回发送cookie，发送结果通过onReportDataPointResult异步返回
    public static native int reportDataPoint(TXDataPoint[] arrayDataPoint);

    // 文件传输通道
    // 如下三个函数均返回一个cookie值，这个cookie值可以用于：
    // 1 通过cancelTransfer取消传输
    // 2 文件传输通知onTransferProgress onTransferComplete onFileCome也含有相应的cookie值
    // channeltype : 传输通道类型 transfer_channeltype_FTN transfer_channeltype_MINI
    // fileType : 传输文件类型 transfer_filetype_image transfer_filetype_video transfer_filetype_audio transfer_filetype_other
    // 上传文件
    public static native long uploadFile(String file_path, int channeltype, int fileType);                            
    //下载文件 
    public static native long downloadFile(byte [] file_key);

    // 从小文件通道下载
    public static native long downloadMiniFile(String file_key, int file_type, String mini_token);

    //发送文件到其他端: buff_with_file & bussiness_name : 发送到对端时，对端可根据bussiness_name，对接收到的文件做不同的处理，buff_with_file可以携带其他参数和信息
    public static native long sendFileTo(long target_id,  String file_path, byte [] buff_extra, String business_name); 
    
    //取消传输
    public static native void cancelTransfer(long transfer_cookie);       
    
    //注册文件传输过滤关键字（即只有注册了businessName，才能在下面的“文件传输回调”中收到这个businessName相关的文件）
    public static native void regFileTransferFilter(String strBusinessName);

    //解绑所有绑定者（须在登录成功之后调用）
    public static native int eraseAllBinders();
    
    // 初始化ota升级模块
    // 参数说明：
    // replaceTimeout: 升级替换文件超时时间，从onUpdateConfirm之后开始计算，超过指定时间手q认为升级超时,单位秒
    // targetPathName: 升级文件下载的位置，包含文件名的全路径
    public static native void initOTA(int replaceTimeout, String targetPathName);
    
    // 升级完成，给手机qq一个结果回复
    // 参数说明：
    // resultCode: 0表示成功，1表示失败
    // errorMsg: 升级错误信息描述
    public static native void ackOtaResult(int resultCode, String errorMsg);
    
    
    // 功用：wifi配网信息解析，启动解析模块
    // 参数说明：
    //     key:         for smartlink配网, 设备的GUID 16字符的字符串。
    //     samplerate:  for 声波配网, 设备实际录音的采样率，填的不对，会导致解声波信息失败。
    //     mode:        1 for 声波配网,
    //                  2 for smartlink配网,(暂不支持，超级管理员权限获取遇到难题)。
    //                  3 同时支持声波配网和smartlink配网
    public static native int startWifiDecoder(String key, int samplerate, int mode);

    // 功用：填充wav数据。
    //    wav： wav 是PCM 16bit 单声道的，size < 2048Byte
    public static native void fillVoiceWavData(byte[] wav);
    
    // 功用：wifi配网信息解析，停止解析模块
    // 参数说明：
    public static native int stopWifiDecoder();
    
    // 配网完成，且初始化完SDK后，通知手Q设备已经联网。
    // ip：同步过来的ip
    // port: 同步过来的port
    // 参见: onReceiveWifiInfo回调说明。
    public native void ackApp(int ip, int port);

    // 功用：开启(benable=true)/关闭(benable=false)近场连接
    public static native void enableLanScan(boolean benable);
    
    // 生成二维码url(根据init时传入的pid,sn,license生成)
    public static native String getQRCodeUrl();

    // 获取小文件key对应的下载URL
    public static native String getMiniDownloadURL(String file_key, int file_type);


    // 功用：Wifi配网信息解析成功通知回调
    // 参数说明：
    //     ssid： wifi的ssid
    //     pwd：  wifi的密码
    //     ip：   手Q的IP ， 用于ackapp通知手Q设备已联网
    //     port： 手Q的端口
    private void onReceiveWifiInfo(String ssid, String pwd, int ip, int port)
    {
        Log.i(TAG, "onReceiveWifiInfo: ssid[" + ssid +"] , password["+ pwd +"], ip["+ip+"], port["+port+"]");

        // 广播通知
        Intent intent = new Intent();
        intent.setAction(TXDeviceService.OnReceiveWifiInfo);
        Bundle bundle = new Bundle();

        bundle.putInt(TXDeviceService.WifiInfo_IP, ip);
        bundle.putInt(TXDeviceService.WifiInfo_PORT, port);
        bundle.putString(TXDeviceService.WifiInfo_SSID, ssid);
        bundle.putString(TXDeviceService.WifiInfo_PASS, pwd);

        intent.putExtras(bundle);
        sendBroadcast(intent);

    }

    //解绑所有用户回调通知
    private void onEraseAllBinders(int error)
    {
        Log.i(TAG, "onEraseAllBinders: error =  " + error);
        if (error == 0)
        {
            showToastMessage("解绑所有用户成功");
        }
        else
        {
            showToastMessage("解绑所有用户失败");
        }
        
        Intent intent = new Intent();
        intent.setAction(TXDeviceService.OnEraseAllBinders);
        Bundle bundle = new Bundle();
        bundle.putInt(TXDeviceService.OperationResult, error);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }
    
    // 成功上传设备注册信息到服务器（用于跨网绑定模式下是否展示二维码）
    private void onWlanUploadRegInfoSuccess()
    {
    	Log.i(TAG, "onWlanUploadRegInfoSuccess: ");
    	showToastMessage("成功上传设备注册信息到服务器");
    	
    	String strUrl = getQRCodeUrl();
    	Log.i(TAG, "getQRCodeUrl:" + strUrl);
    }

    //设备绑定回调，id：表示设备的DIN；error表示错误码：0表示设备被成功绑定（之前未被绑定），1表示设备之前已经被绑定，其它表示绑定出错
    private void onBindCallback(long id, int error)
    {
        Log.i(TAG, "onBindCallback: " + id + " " + error);
        if (error == 0)
        {
            showToastMessage("设备被绑定成功");
            Intent intent=new Intent();
            intent.setAction(TXDeviceService.isconnected);
            intent.putExtra("ishave","yes");
            sendBroadcast(intent);

        }
        else  
        {
            showToastMessage("设备已经被绑定");
        }
    }
    
    //绑定者列表变化，error：错误码，0表示绑定者列表刷新成功，其它表示刷新失败
    private void onBinderListChange(int error, TXBinderInfo [] listBinder)
    {
        if (error == 0) 
        {
            showToastMessage("绑定者列表刷新成功");
            Log.i(TAG, "BinderList Fresh Success");
        }
        else
        {
            showToastMessage("绑定者列表刷新失败");
            Log.i(TAG, "BinderList Fresh Failed");
        }
        
        if (null == listBinder)
        {   
            Log.i(TAG, "onBinderListChange: listBinder is null ");
            return;
        }               
           
        Log.i(TAG, "onBinderListChange: count = " + listBinder.length);
        for (int i = 0; i < listBinder.length; ++i)
        {   
            if (null == listBinder[i])
            {   
                Log.i(TAG, "onBinderListChange: listBinder[" + i + "] is null ");
                continue;
            }
            Log.i(TAG, "onBinderListChange: " + listBinder[i].binder_type + " " + listBinder[i].tinyid + " " + " " + listBinder[i].getNickName() + " "  + listBinder[i].binder_gender + " " + listBinder[i].head_url);
        }
        
        Intent intent = new Intent();
        intent.setAction(TXDeviceService.BinderListChange);
        Bundle bundle = new Bundle();
        bundle.putParcelableArray("binderlist", listBinder);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }
   
    //设备登录回调，error表示错误码：0表示登录成功，其它表示登录出错
    private void onLoginComplete(int error)
    {
        Log.i(TAG, "onLoginComplete: error =  " + error);
        if (error == 0)
        {
            showToastMessage("登录成功");
        }
        else
        {
            showToastMessage("登录失败");
            Intent intent=new Intent();
            //设置 Intent 的动作
            intent.setAction(TXDeviceService.wifisetting);
            sendBroadcast(intent);
        }
    }
     
    //上线成功后回调：电视端的所有其它操作都应该在上线成功以后才可以进行
    private void onOnlineSuccess()
    {    
        Log.i(TAG, "onOnlineSuccess "); 
        showToastMessage("上线成功");

        //拉取绑定列表
        getBinderList(); 
    }       
    
    //下线回调
    private void onOfflineSuccess()
    {
        Log.i(TAG, "onOfflineSuccess");
        showToastMessage("### 离线 ###");
        Intent intent=new Intent();
        //设置 Intent 的动作
        intent.setAction(TXDeviceService.wifisetting);
        sendBroadcast(intent);

    }
        
    //富媒体消息（Audio，Video，Picture）发送进度信息
    private void OnRichMsgSendProgress(int cookie, long transfer_progress, long max_transfer_progress)
    {
        Log.i(TAG, "OnRichMsgSendProgress: cookie = " + cookie + " progress = " + transfer_progress + " max_progress = " + max_transfer_progress);
    }
    
    //富媒体消息（Audio，Video，Picture）发送结果信息
    private void OnRichMsgSendRet(int cookie, int err_code)
    {
        Log.i(TAG, "OnRichMsgSendRet: cookie = " + cookie + " err_code = " + err_code);
    }
    
    //收到DataPoint
    private void onReceiveDataPoint(long from, TXDataPoint [] arrayDataPoint) throws Exception
    {
        if (null == arrayDataPoint)
        {
            Log.i(TAG, "onReceiveDataPoint: arrayDataPoint is null ");
            return;
        }
 
        Log.i(TAG, "onReceiveDataPoint: from = " + from);
        for (int i = 0; i < arrayDataPoint.length; ++i)
        {
            Log.i(TAG, "onReceiveDataPoint: " + arrayDataPoint[i].property_id +  " " + arrayDataPoint[i].property_val+ " " +
                           arrayDataPoint[i].sequence + " " + arrayDataPoint[i].ret_code);
            
            String strText = "收到DataPoint Property ID：" + arrayDataPoint[i].property_id + "   Property Value：" + arrayDataPoint[i].property_val;
            showToastMessage(strText);
        }

        Intent intent = new Intent();
        intent.setAction(TXDeviceService.OnReceiveDataPoint);
        Bundle bundle = new Bundle();
        bundle.putLong("from", from);
        bundle.putParcelableArray("datapoint", arrayDataPoint);
        intent.putExtras(bundle);
        sendBroadcast(intent);
        //回复所有的DataPoint
        ackDataPoint(from,arrayDataPoint);
        reportDataPoint(arrayDataPoint);
    }
    
    // ackDataPoint发送结果通知  
    private void onAckDataPointResult(int cookie, long u64UIN, int err_code)
    {
    		String strLog = "onAckDataPointResult: cookie = " + cookie + " u64Uin = " + u64UIN + " err_code = " + err_code;
    		Log.i(TAG, strLog);
    		showToastMessage(strLog);
    }
    
    //reportDataPoint发送结果通知
    private void onReportDataPointResult(int cookie, int err_code)
    {
		String strLog = "onReportDataPointResult: cookie = " + cookie + " err_code = " + err_code;
		Log.i(TAG, strLog);
		showToastMessage(strLog);
    }

    //文件传输回调：进度信息
    private void onTransferProgress(long transfer_cookie, long transfer_progress, long max_transfer_progress)
    {
        Log.i(TAG, "onTransferProgress: cookie = " + transfer_cookie + " progress = " + transfer_progress + " max_progress = " + max_transfer_progress);
    }
 
    //文件传输回调：结果信息
    private void onTransferComplete(long transfer_cookie, int err_code, TXFileTransferInfo info)
    {
        String extra_buffer = new String(info.buffer_extra);
        Log.i(TAG, "onTransferComplete: cookie = " + transfer_cookie + " err_code = " + err_code + " business_name:" + info.business_name + " extra_buffer:" + extra_buffer + "   file_path" + info.file_path);
        String strText = "收到文件，business_name:" + info.business_name + " extra_buffer:" + extra_buffer + "   file_path" + info.file_path;
        showToastMessage(strText);

        Intent intent=new Intent();
        intent.setAction(TXDeviceService.voicereceive);
        intent.putExtra("filepath",info.file_path);
        sendBroadcast(intent);
    }
    
    //文件传输回调：收到文件
    private void onReceiveFile(long transfer_cookie, long from)
    {   
        Log.i(TAG, "onReceiveFile:  cookie = " + transfer_cookie + " From = " + from + " ");
    } 
    
	// 有设备可用的新固件版本，手机会将查询到的固件包信息通知给设备
	// 参数说明：
	// pkg_size: 新的升级固件包的大小
	// title + desc: 升级描述信息，如果您的智能设备没有显示屏，可以忽略
	// target_version: 目标版本的版本号
	// return: 返回 0，sdk将会开始启动升级包下载， 返回1，表明设备端拒绝升级（一般是磁盘剩余空间问题）
  	private int onNewPkgCome(long from, long pkgSize, String title, String desc, int targetVer) {
  		Intent intent = new Intent();
        intent.setAction(TXDeviceService.OTAOnNewPkgCome);
        Bundle bundle = new Bundle();
        bundle.putLong("from", pkgSize);
        bundle.putLong("pkgSize", pkgSize);
        bundle.putString("title", title);
        bundle.putString("desc", desc);
        bundle.putInt("targetVer", targetVer);
        intent.putExtras(bundle);
        sendBroadcast(intent);
        return 0;
  	}
  	
	// 设备下载升级文件，并且实时地将进度通知给手机QQ
  	// 参数说明：
	// download_size： 当前已经下载到的文件大小，单位字节
	// total_size：    文件总计大小，您可以用  download_size/total_size 来计算百分比
  	private void onDownloadProgress(long currentProgress, long maxProgress) {
  		Intent intent = new Intent();
        intent.setAction(TXDeviceService.OTAOnDownloadProgress);
        Bundle bundle = new Bundle();
        bundle.putLong("current", currentProgress);
        bundle.putLong("count", maxProgress);
        intent.putExtras(bundle);
        sendBroadcast(intent);
  	}
  	
  	 // 升级文件下载完成
	 // result_code  0表示下载成功，其它表示失败
	 // 0   成功
	 // 2   未知错误
	 // 3   当前请求需要用户验证401
	 // 4   下载文件写失败，没有写权限/空间不足/文件路径不正确
	 // 5   网络异常
	 // 7   升级文件包不存在404
	 // 8   服务器当前无法处理请求503
	 // 9   下载被手q用户中止
     // 10  参数错误，url不合法
	 // 11  升级包md5值校验失败，下载可能被劫持
  	private void onDownloadComplete(int resultCode) {
  		Intent intent = new Intent();
        intent.setAction(TXDeviceService.OTAOnDownloadComplete);
        Bundle bundle = new Bundle();
        bundle.putInt("resultCode", resultCode);
        intent.putExtras(bundle);
        sendBroadcast(intent);
  	}
  	
	// 在这里进行替换文件升级操作，具体的升级过程需要使用者自己来实现，sdk只提供下载升级文件功能
  	private void onUpdateConfirm() {
  		Intent intent = new Intent();
        intent.setAction(TXDeviceService.OTAOnUpdateConfirm);
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);
        sendBroadcast(intent);
  	}

	// Toast提示
    private void showToastMessage(final String strMsg)
    {
        mToastMessageHandler.post(new Runnable() {

        	@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), strMsg, Toast.LENGTH_LONG).show();
			}
        	 
        });
    }
    

    //以下是视频通话相关的接口
    public static final String OnSendVideoCall		= "OnSendVideoCall";
    public static final String OnSendVideoCallM2M	= "OnSendVideoCallM2M";
    public static final String OnSendVideoCMD		= "OnSendVideoCMD";
    public static final String OnReceiveVideoBuffer	= "OnReceiveVideoBuffer";
    public static final String StartVideoChatActivity= "StartVideoChatActivity";
    
	public static native void   nativeSendVideoCall(long peerUin, byte[] msg);
	public static native void   nativeSendVideoCallM2M(long peerUin, byte[] msg);
	public static native void   nativeSendVideoCMD(long peerUin, byte[] msg);
	public static native long   nativeGetSelfDin();
	public static native byte[] nativeGetVideoChatSignature();
	
	private void onSendVideoCall(byte[] msg) { 
		callbackVideoMsg(TXDeviceService.OnSendVideoCall, msg);
	}
	private void onSendVideoCallM2M(byte[] msg) {
		callbackVideoMsg(TXDeviceService.OnSendVideoCallM2M, msg);
	}
	private void onSendVideoCMD(byte[] msg) {
		callbackVideoMsg(TXDeviceService.OnSendVideoCMD, msg);
	}
	private void onReceiveVideoBuffer(byte[] msg) {
		callbackVideoMsg(TXDeviceService.OnReceiveVideoBuffer, msg);
	}
	private void callbackVideoMsg(String action, byte[]msg) { 
  		Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("msg", msg);
        invokeVideoMsg(intent);       
	}
	
	public void startVideoChatActivity(long peerId)
	{
  		Intent intent = new Intent();
        intent.setAction(TXDeviceService.StartVideoChatActivity);
        intent.putExtra("peerid", peerId);
        invokeVideoMsg(intent);
	}
	
	List<Intent> mPendingIntent = new ArrayList<Intent>(); 
	private void invokeVideoMsg(Intent intent)
	{
		if (this.isVideoServiceRunning()) {
			sendBroadcast(intent);
		}
		else {
			mPendingIntent.add(intent);
			startService(new Intent(this, VideoService.class));
		}
	}
	
	private boolean isVideoServiceRunning()
	{
        boolean isServiceRunning = false;
        boolean isProcessRunning = false;
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        for (int i = 0; i < serviceList.size(); i++) 
        {
            if (serviceList.get(i).service.getClassName().equals("com.tencent.av.VideoService") == true) 
            {
            		isServiceRunning = true;
                break;
            }
        }
        
        List<RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processList.size(); ++i)
        {
        		if (processList.get(i).processName.equals("com.tencent.devicedemo:video"))
        		{
        			isProcessRunning = true;
        			break;
        		}
        }
        return isServiceRunning & isProcessRunning;
    }
	
	public void invokePendingIntent()
	{
		for (int i = 0; i < mPendingIntent.size(); ++i)
		{
			sendBroadcast(mPendingIntent.get(i));
		}
		mPendingIntent.clear();
	}

	
	//-------------------------------------------- 跨进程Service ------------------------------------------------------  
    public class TXDeviceServiceBinder extends ITXDeviceService.Stub {
		@Override
		public void sendVideoCall(long peerUin, byte[] msg)
				throws RemoteException {
			// TODO Auto-generated method stub
			TXDeviceService.nativeSendVideoCall(peerUin, msg);
		}

		@Override
		public void sendVideoCallM2M(long peerUin, byte[] msg)
				throws RemoteException {
			// TODO Auto-generated method stub
			TXDeviceService.nativeSendVideoCallM2M(peerUin, msg);
		}

		@Override
		public void sendVideoCMD(long peerUin, byte[] msg) 
				throws RemoteException {
			// TODO Auto-generated method stub
			TXDeviceService.nativeSendVideoCMD(peerUin, msg);
		}

		@Override
		public long getSelfDin() throws RemoteException {
			// TODO Auto-generated method stub
			long selfDin = TXDeviceService.nativeGetSelfDin();
			return selfDin;
		}

		@Override
		public void notifyVideoServiceStarted() throws RemoteException {
			// TODO Auto-generated method stub
			TXDeviceService.getInstance().invokePendingIntent();
		}

		@Override
		public byte[] getVideoChatSignature() throws RemoteException {
			// TODO Auto-generated method stub
			return TXDeviceService.nativeGetVideoChatSignature();
		}
    }

    
	//-------------------------------------------- 弹幕 ------------------------------------------------------    
    
    // 监听列表
    private ArrayList<WeakReference<IBarrageListener>> mWeakListenerArrayList;
    
    // 辅助回收, 防止内存泄露
    private ReferenceQueue<IBarrageListener> mListenerReferenceQueue;

    // 弹幕开关列表
    private final HashMap<Long, Boolean> mBarrageSwitchMap = new HashMap<Long, Boolean>();
    
    // 语音开关列表
    private final HashMap<Long, Boolean> mVoiceSwitchMap = new HashMap<Long, Boolean>();
            
    /**
     * 接收到弹幕消息
     * call back from native sdk
     */
    public void onReceiveBarrageMsg(BarrageMsg msg) {
        Log.v(TAG, "onReceiveBarrageMsg " + msg.toString());
        
        //语音开关处理
        if(msg != null && msg.msgList != null && msg.msgList.size() > 0) {
            //ArrayList<GroupMsg> tmpList = new ArrayList<BarrageMsg.GroupMsg>();
            for(GroupMsg groupMsg : msg.msgList) {
                if(groupMsg.msgType == BarrageContext.MSG_VOICE && groupMsg.subContent == 0) {
                    msg.msgList.clear();
                }
                break;
                //tmpList.add(groupMsg);
            }
            //msg.msgList = tmpList;
        }
        if(msg != null && msg.msgList != null && msg.msgList.size() > 0 && mWeakListenerArrayList != null) {
            for (WeakReference<IBarrageListener> linstenerWeakReference : mWeakListenerArrayList)
            {
                IBarrageListener listener = linstenerWeakReference.get();
                if (null != listener) {
                    listener.onReceiveMsg(msg);
                }
            }
        }
    }
    
    
    
    /**
     * 注册弹幕回调对象，对listener的持有为弱持有,需为全局变量
     */
    public synchronized boolean registerBarrageListener(IBarrageListener listener) {
        if(mWeakListenerArrayList == null) {
            mWeakListenerArrayList = new ArrayList<WeakReference<IBarrageListener>>(); 
            mListenerReferenceQueue = new ReferenceQueue<IBarrageListener>();
        }
        
        if (listener == null) {
            return false;
        }

        // 每次注册的时候清理已经被系统回收的对象
        Reference<? extends IBarrageListener> releaseListener = null;
        while ((releaseListener = mListenerReferenceQueue.poll()) != null) {
            mWeakListenerArrayList.remove(releaseListener);
        }

        // 弱引用处理
        for (WeakReference<IBarrageListener> weakListener : mWeakListenerArrayList) {
            IBarrageListener listenerItem = weakListener.get();
            if (listenerItem == listener) {
                return true;
            }
        }
        
        WeakReference<IBarrageListener> weakListener =
                new WeakReference<IBarrageListener>(listener, mListenerReferenceQueue);
        this.mWeakListenerArrayList.add(weakListener);

        return true;
    }

    /**
     * 取消弹幕回调对象的注册
     * 
     * @param listener
     * @return 是否反注册成功
     */
    public synchronized boolean unRegisterBarrageListener(IBarrageListener listener) {
        if(mWeakListenerArrayList == null) {
            mWeakListenerArrayList = new ArrayList<WeakReference<IBarrageListener>>(); 
            mListenerReferenceQueue = new ReferenceQueue<IBarrageListener>();
        }
        
        if (listener == null) {
            return false;
        }

        // 弱引用处理
        for (WeakReference<IBarrageListener> weakListener : mWeakListenerArrayList) {
            IBarrageListener listenerItem = weakListener.get();
            
            if (listenerItem == listener) {
                mWeakListenerArrayList.remove(weakListener);
                return true;
            }
        }
        return false;
    }
    
    /**
     * 设置弹幕消息语音开关
     * 
     * @param gourpId 群组ID
     * @param isBarrageOn 弹幕开关
     * @param isVoiceOn 声音开关
     */
    public void setBarrageSwitch(long groupId, boolean isBarrageOn, boolean isVoiceOn) {
        if(isBarrageOn) {
            mBarrageSwitchMap.put(groupId, isBarrageOn);
            mVoiceSwitchMap.put(groupId, isVoiceOn);
        } else {
            mBarrageSwitchMap.put(groupId, isBarrageOn);
            mVoiceSwitchMap.put(groupId, false);
        }
        
        if(mWeakListenerArrayList != null) {
            for (WeakReference<IBarrageListener> linstenerWeakReference : mWeakListenerArrayList)
            {
                IBarrageListener listener = linstenerWeakReference.get();
                if (null != listener) {
                    listener.onBarrageSwitched(groupId, isBarrageOn, isVoiceOn);
                }
            }
        }
    }
    
    /**
     * 获取弹幕开关
     * @return
     */
    public boolean getBarrageSwitch(long groupId) {
        boolean isBarrageOn = true;
        if(mBarrageSwitchMap.containsKey(groupId)) {
            isBarrageOn = mBarrageSwitchMap.get(groupId);
        } else {
            isBarrageOn = false;
        }
        return isBarrageOn;
    }
    
    /**
     * 获取语音开关
     * @return 
     */
    public boolean getVoiceSwitch(long groupId) {
        boolean isVoiceOn = true;
        if(mVoiceSwitchMap.containsKey(groupId)) {
            isVoiceOn = mVoiceSwitchMap.get(groupId);
        } else {
            isVoiceOn = false;
        }
        return isVoiceOn;
    }
   
}
