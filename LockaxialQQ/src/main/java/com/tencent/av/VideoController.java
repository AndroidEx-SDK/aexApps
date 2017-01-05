package com.tencent.av;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.os.Build;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.tencent.av.camera.AndroidCamera;
import com.tencent.av.camera.VcCamera;
import com.tencent.av.core.AbstractNetChannel;
import com.tencent.av.core.IVideoEventListener;
import com.tencent.av.core.VcCapability;
import com.tencent.av.core.VcControllerImpl;
import com.tencent.av.core.VcSystemInfo;
import com.tencent.av.core.VideoConstants;
import com.tencent.av.opengl.GraphicRenderMgr;

import com.tencent.av.thread.FutureListener;
import com.tencent.av.thread.ThreadPool;
import com.tencent.av.thread.ThreadPool.Job;
import com.tencent.av.thread.ThreadPool.JobContext;
import com.tencent.av.utils.TraeHelper;
import com.tencent.device.ITXDeviceService;
import com.tencent.device.QLog;
import com.tencent.devicedemo.VideoChatActivityHW;
import com.tencent.devicedemo.VideoChatActivitySF;
import com.tencent.devicedemo.VideoMonitorService;
import com.tencent.sharp.jni.TraeAudioManager;

 
public class VideoController extends AbstractNetChannel implements IVideoEventListener{

	static String TAG = "VideoController";    
	   
	public static final String ACTION_NETMONIOTR_INFO = "com.tencent.device.videocontroller.netmonitorinfo";
	
	public static final String ACTION_CHANNEL_READY = "com.tencent.device.videocontroller.channelready";
	
	public static final String ACTION_VIDEOFRAME_INCOME = "com.tencent.device.videocontroller.videoframeincome";
	
	public static final String ACTION_VIDEO_QOS_NOTIFY = "com.tencent.device.videocontroller.videoqosnotify";

	private static boolean mEnableHWEncoder = false;
	    
	private static boolean mEnableHWDecoder = false;
    
	ThreadPool mThreadPool = new ThreadPool(1, 1);
	
    public String deviceName = null;    
    public String[] strDeviceList = null;
    public String mAudioStateBeforePhoneCall = TraeAudioManager.DEVICE_NONE;
    
    TraeHelper mTraeHelper = null;
     
	// camera
	VcCamera mVcCamera = null;

	// controller
	VcControllerImpl mVcCtrl = null;
	
	Context mContext;
	
	String mSelfDin = null;
	Map<String, Boolean> mMapUinPending = new HashMap<String, Boolean>(); 

	static VideoController g_Instance = null;
	
	ITXDeviceService	 mTXDeviceService = null; 

	public static VideoController getInstance() {
		if (g_Instance == null) {
			g_Instance = new VideoController();
		}
		return g_Instance;
	}

	//bHwEnc和bHwDec可以组成四种组合：软编软解（支持）、软编硬解（不支持）、硬编硬解（支持）、硬编软解（支持）
	public void initVcController(Context context, boolean bHwEnc, boolean bHwDec) {
		// 判断设备系统版本
		if (Build.VERSION.SDK_INT < 18) {
			bHwEnc = false;
			bHwDec = false;
		}
		
		// 不支持软件硬解
		if (bHwEnc == false && bHwDec == true) {
			bHwDec = false;
		}
		
		mEnableHWEncoder = bHwEnc;
		mEnableHWDecoder = bHwDec;
		
		String selfDin = GetSelfDin();
		if (TextUtils.isEmpty(selfDin)) {
			return;
		}
		
		if (mSelfDin != null && mVcCtrl != null) {
			if (mSelfDin.equalsIgnoreCase(selfDin)){
				//do nothing
			}
			else {
				mSelfDin = selfDin;
				mVcCtrl.UpdateSelfUin(selfDin);
				GraphicRenderMgr.getInstance().setAccountUin(selfDin);
			}
			return;
		}
		
		mContext = context;

		WindowManager mWindowManager = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displaysMetrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(displaysMetrics);
		int screenWidth = displaysMetrics.widthPixels;
		int screenHeight = displaysMetrics.heightPixels;

//		TraeAudioManager.init(getContext());
//		UITools.initTrae(getContext());

		try {
			int apnType = getApn();
			byte[] signature = getVideoChatSignature();
			VcControllerImpl vcCtrl = new VcControllerImpl(getContext(),
					"537039075", this, this, screenWidth,
					screenHeight, apnType, mEnableHWEncoder, mEnableHWDecoder);

			vcCtrl.init(getContext(), Long.parseLong(selfDin), "", Build.MODEL,
					"537039075", "30",
					"4B2163B5E79E88BB",
					getDeviceIMEI(), VcSystemInfo.getDeviceName(),
					Build.VERSION.RELEASE, Build.VERSION.INCREMENTAL,
					Build.MANUFACTURER, VcSystemInfo.getCPUName(), apnType, signature);
			
			if (mEnableHWEncoder){
				vcCtrl.enableVideoHWCodec(mEnableHWEncoder, mEnableHWDecoder);
			}

			mVcCtrl = vcCtrl;
			mSelfDin = selfDin;
			
			mTraeHelper = TraeHelper.createInstanse(context, this);
			
			GraphicRenderMgr.getInstance().setAccountUin(selfDin);
			setEncodeDecodePtr(false);
		} 
		catch (UnsatisfiedLinkError e) {
			mVcCtrl = null;
		}
	}
	
	public String GetSelfDin()
	{
		String strSelfDin = String.valueOf(nativeGetSelfDin());
		return strSelfDin;
	}
	
	public void setTXDeviceService(ITXDeviceService service) {
		mTXDeviceService = service;
	}
	
	public static boolean isHardwareEncoderEnabled() {
		return mEnableHWEncoder;
	}
	
	public static boolean isHardwareDecoderEnabled() {
		return mEnableHWDecoder;
	}
	
	public int updateSelfUin(String uin) {
		if (mVcCtrl != null) {
			return mVcCtrl.UpdateSelfUin(uin);
		}
		return -1;
	}

	public Context getContext() {
		return mContext;
	}
	
	int getApn(){
		return VcCapability.AP_INTERNET;
	}

	// Modify by shawn, 原先的逻辑在三星i9300中崩溃(debug的逻辑才走到这里)
	String getDeviceIMEI() {
		try {
			return ((TelephonyManager) getContext().getSystemService(
					Context.TELEPHONY_SERVICE)).getDeviceId();
		} catch (Exception e) {
		}

		return "1234567890";
	}

	public VcCamera getCamera() {
		if (mVcCamera == null) {
			mVcCamera = new VcCamera(this);
		}
		return mVcCamera;
	}
	
	public boolean hasPendingChannel() {
		return mMapUinPending.size() > 0;
	}

	// AppType
	public final static int AppType_Audio = 0;
	public final static int AppType_Video = 1;
	public final static int AppType_Audio_SwitchTer = 1;
	public final static int AppType_Video_SwitchTer = 0;
	// RelationType
	public final static int RelationType_Friends = 1;
	public final static int RelationType_Discuss = 2;
	public final static int RelationType_Group = 3;
	public final static int RelationType_Temp = 4;

	private static final byte[] NULL = null;

	public int request(String peerUin) { 
		mMapUinPending.put(peerUin, false);
		requestAudioFocus();
		return mVcCtrl.requestVideo(peerUin, 0,VcCapability.AP_INTERNET,AppType_Video, RelationType_Friends, 
        		"", "", "", 9500, "", "",0, null, "", "");
	}

	public int acceptRequest(String peerUin) { 
		mMapUinPending.put(peerUin, false);
		abandonAudioFocus();
		return mVcCtrl.acceptVideo(peerUin, 0,VcCapability.AP_INTERNET,AppType_Video, RelationType_Friends);
	}

	public int rejectRequest(String peerUin) {
		mMapUinPending.remove(peerUin);
		abandonAudioFocus();
		return mVcCtrl.rejectVideo(peerUin, getApn(), VideoConstants.VOIP_REASON_REJECT_BY_SELF);
	}

	public int ignoreRequest(String peerUin) {  
		mMapUinPending.remove(peerUin);
		abandonAudioFocus();
		return mVcCtrl.ignoreVideo(peerUin, getApn());
	}

	public int closeVideo(String peerUin) {
		mMapUinPending.remove(peerUin);
		abandonAudioFocus();
		return mVcCtrl.closeVideo(peerUin, VideoConstants.VOIP_REASON_CLOSED_BY_SELF);
	}

	public boolean isSharp() {
		if (mVcCtrl == null) {
			return false;
		}
		return mVcCtrl.isSharp();
	}

	public void pauseVideo(String peerUin) {
		mVcCtrl.pauseVideo(peerUin);
	}

	public void resumeVideo(String peerUin) {
		GraphicRenderMgr.getInstance().clearCameraFrames();
		mVcCtrl.resumeVideo(peerUin);
	}
		
	public void setEncodeDecodePtr(boolean clean) {
		GraphicRenderMgr graphicRenderMgr = GraphicRenderMgr.getInstance();
		if (null != mVcCtrl) {
			int ptrDecoder = clean ? 0 : graphicRenderMgr.getRecvDecoderFrameFunctionptr();
			mVcCtrl.setProcessDecoderFrameFunctionptr(ptrDecoder);
			int ptrEncoder = clean ? 0 : mVcCtrl.getEncodeFrameFunctionPtrFunPtr();
			graphicRenderMgr.setProcessEncodeFrameFunctionPtr(ptrEncoder);
		}
	}

	public void OnPreviewData(byte[] data, int angle, long SPF, boolean isFront) {
		if (data == null) {
			return;
		}
		int datalen = data.length;

		if (datalen != AndroidCamera.PREVIEW_WIDTH
				* AndroidCamera.PREVIEW_HEIGHT * 3 / 2) {
			if (QLog.isColorLevel())
				QLog.d("OnPreviewData", QLog.CLR, "datalen != preview size");
			if (datalen == 640 * 480 * 3 / 2) {
				AndroidCamera.PREVIEW_WIDTH = 640;
				AndroidCamera.PREVIEW_HEIGHT = 480;
			}
            
			if (datalen == 320 * 240 * 3 / 2) {
				AndroidCamera.PREVIEW_WIDTH = 320;
				AndroidCamera.PREVIEW_HEIGHT = 240;
			}
		}
		QLog.d("OnPreviewData", QLog.CLR, "format:" + AndroidCamera.PREVIEW_FORMAT + " width:" + AndroidCamera.PREVIEW_WIDTH + " height:" + AndroidCamera.PREVIEW_HEIGHT);
		GraphicRenderMgr.getInstance().sendCameraFrame2Native(data,
				AndroidCamera.PREVIEW_FORMAT, AndroidCamera.PREVIEW_WIDTH,
				AndroidCamera.PREVIEW_HEIGHT, angle,
				System.currentTimeMillis(), isFront);
	}
	
	////////////////////////////////////Sharp Msg Channel begin /////////////////////////////////////////
	private void sendVideoCall(long peerUin, byte[] msg)
	{
		try {
			if (mTXDeviceService != null) {
				mTXDeviceService.sendVideoCall(peerUin, msg);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void sendVideoCallM2M(long peerUin, byte[] msg)
	{
		try {
			if (mTXDeviceService != null) {
				mTXDeviceService.sendVideoCallM2M(peerUin, msg);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void sendVideoCMD(long peerUin, byte[] msg)
	{
		try {
			if (mTXDeviceService != null) {
				mTXDeviceService.sendVideoCMD(peerUin, msg);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private long nativeGetSelfDin()
	{
		try {
			if (mTXDeviceService != null) {
				return mTXDeviceService.getSelfDin();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	private byte[] getVideoChatSignature()
	{
		try {
			if (mTXDeviceService != null) {
				return mTXDeviceService.getVideoChatSignature();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte onSendVideoCall(byte[] msg) { 
		if (g_Instance != null) {
			return g_Instance.receiveVideoCall(nativeGetSelfDin(), msg, NULL);
		}
		return RecvFail;
	}
	public byte onSendVideoCallM2M(byte[] msg) {
		if (g_Instance != null) {
			return g_Instance.receiveVideoCallM2M(nativeGetSelfDin(), msg, NULL);
		}
		return RecvFail;
	}
	public byte onSendVideoCMD(byte[] msg) {
		if (g_Instance != null) {
			return g_Instance.receiveSharpVideoAck(nativeGetSelfDin(), msg, NULL);
		}
		return RecvFail; 
	}
	public byte onReceiveVideoBuffer(byte[] msg) {
		if (g_Instance != null) {
			return g_Instance.receiveSharpVideoCall(nativeGetSelfDin(), msg, NULL);
		}
		return RecvFail;
	}
	
////////////////////////////////////Sharp Msg Channel end /////////////////////////////////////////
	
	////////////////////////////////////AbstractNetChannel begin/////////////////////////////////////////
	@Override
	public void sendVideoCall(byte[] msg, long peerUin) {
		//过滤后台解析错误的sharp包
//		sendVideoCall(peerUin,msg);
	}

	@Override
	public void sendVideoCallM2M(byte[] msg, long peerUin) {
		//过滤后台解析错误的sharp包
//		sendVideoCallM2M(peerUin,msg);
	}

	@Override
	public void sendVideoConfigReq(byte[] msg) {
		// TODO Auto-generated method stub
		
	}
     
	@Override
	public void sendSharpCMD(byte[] msg, long peerUin) {
		sendVideoCMD(peerUin,msg);
	}

	@Override
	public void sendMultiVideoCMD(long groupId, long csCmd, byte[] msg) {
		// TODO Auto-generated method stub
		
	}
	
	////////////////////////////////////AbstractNetChannel end/////////////////////////////////////////

	
	////////////////////////////////////IVideoEventListener begin/////////////////////////////////////////
	@Override
	public void onRequestVideo(int uinType, String fromUin, String extraUin,
			byte[] sig, boolean onlyAudio, String bindID, int bindType) {
		requestAudioFocus();
		
		if (hasPendingChannel()) {
			rejectRequest(fromUin);
		}
		else {

	        if (Long.parseLong(bindID) == 4200) {
	        		QLog.d(TAG, QLog.CLR, "recv video chat request");
				Intent intent = null;
				if (VideoController.isHardwareEncoderEnabled()) {
					intent = new Intent(mContext, VideoChatActivityHW.class);
				} 
				else {
					intent = new Intent(mContext, VideoChatActivitySF.class);
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("receive", true);
				intent.putExtra("peerid", fromUin);
				mContext.startActivity(intent);
	        }
	        else if (Long.parseLong(bindID) == 4100) {
	        		QLog.d(TAG, QLog.CLR, "recv video monitor request");
				Intent intent = new Intent(mContext, VideoMonitorService.class);
				intent.putExtra("peerid", fromUin);
				mContext.startService(intent);
	        }
	        else {
	        		QLog.d(TAG, QLog.CLR, "recv video request, unknow type " + bindID);
	        }

			mMapUinPending.put(fromUin, false);
		}
	}

	@Override
	public void onRejectVideo(String fromUin) {

	}

	@Override
	public void onCancelRequest(String fromUin) {

	}

	@Override
	public void onAcceptedVideo(String fromUin) {

	}

	@Override
	public void onChannelReady(final String fromUin) {
		mTraeHelper.startService(TraeAudioManager.VIDEO_CONFIG);
		mTraeHelper.connectDevice(TraeAudioManager.DEVICE_SPEAKERPHONE);
		
		mMapUinPending.put(fromUin, true);
        Intent intent = new Intent(VideoController.ACTION_CHANNEL_READY);
        intent.putExtra("uin", fromUin);
        mContext.sendBroadcast(intent);
	}
	
	@Override
	public void onRecvVideoData(String fromUin, byte[] data, int frmAngle,
			int width, int height, int colorFmt) {
		// TODO Auto-generated method stub
		
	}
    
	@Override
	public void onCloseVideo(String fromUin, int reason, long extraParam) {
		mTraeHelper.stopSerivce();
		abandonAudioFocus();
		Intent intent = new Intent();
		intent.setAction(VideoConstants.ACTION_STOP_VIDEO_CHAT);
		intent.putExtra("uin", fromUin);
		intent.putExtra("reason", reason);
		mContext.sendBroadcast(intent);
		
		mMapUinPending.remove(fromUin);
	}

	@Override
	public void onPauseVideo(String fromUin) {

	}

	@Override
	public void onResumeVideo(String fromUin) {

	}

	@Override
	public void onPauseAudio(String fromUin) {

	}

	@Override
	public void onResumeAudio(String fromUin) {

	}

	@Override
	public void onApptypeNotSuit(String fromUin) {

	}
	
	@Override
	public void onNeedShowPeerVideo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnotherHaveReject(String fromUin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnotherHaveAccept(String fromUin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConfigSysDealDone(String fromUin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAVShiftEvent(int type, String fromUin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnotherIsRing(String fromUin, boolean isCalling) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOldRequestNotSupportSharp(String fromUin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNotRecvAudioData(boolean bNotRecv) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRecvFirstAudioData(boolean recvFirstAudio) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMediaCameraNotify(byte[] detail, long info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInviteReached(String peerUin, int friend_state,
			long extraParam0, byte[] detail) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDetectAudioDataIssue(int issueType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOtherTerminalChatingStatus(String fromUin, long roomid,
			int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPeerSwitchTerninal(String fromUin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSyncOtherTerminalChatStatus(String fromUin, int time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSwitchTerminalSuccess(String fromUin, int info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPeerSwitchTerminalFail(String fromUin, int info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChangePreviewSize(int w, int h) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSendC2CMsg(String fromUin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNetworkDisconnect(String fromUin) {
		if (QLog.isColorLevel())
			QLog.d(TAG, QLog.CLR, "onNetworkDisconnect fromUin = " + fromUin);
		Intent intent = new Intent();
		intent.setAction(VideoConstants.ACTION_STOP_VIDEO_CHAT);
		intent.putExtra("uin", fromUin);
		intent.putExtra("reason", VideoConstants.VOIP_REASON_NETWORK_DISCONNECT);
		mContext.sendBroadcast(intent);
	}

	@Override
	public int getAPAndGateWayIP() {
		return 0;   
	}

	@Override
	public void onNetworkMonitorInfo(String fromUin, byte[] detail, long info) {
	    String msg = null;
        if (info == 1) 
        {
            try 
            {
                msg = new String(detail, "GBK");
            } 
            catch (NullPointerException e)
            {

            } 
            catch (UnsupportedEncodingException e) 
            {

            }
        } 
        else if (info == 0) 
        {
            msg = new String(detail);
        }
        
        if(QLog.isColorLevel())
        {
        	QLog.d(TAG,QLog.CLR, msg);
        }
        Intent intent = new Intent(VideoController.ACTION_NETMONIOTR_INFO);
        intent.putExtra("uin", fromUin);
        intent.putExtra("msg", msg);
        mContext.sendBroadcast(intent);
	}
	
	@Override
	public void dataTransfered(int direction, long size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNetworkInfo_S2C(String fromUin, byte[] detail, long flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSwitchGroup(String fromUin, byte[] detail, long flag) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSelfNetLevel(int level) {

	}

	@Override
	public void onNetLevel_S2C(String fromUin, int level) {
		
	}
	
	@Override
	public void onReceiveVideoFrame(byte[] buffer, int angle) {
		Log.d(TAG, "ReceiveVideoFrame: len = " + buffer.length + " angle = " + angle);
		if (mContext != null) {
		    Intent intent = new Intent(VideoController.ACTION_VIDEOFRAME_INCOME);
		    intent.putExtra("angle", angle);
		    intent.putExtra("buffer", buffer);
		    mContext.sendBroadcast(intent);
		}
	}
    
	@Override
	public  void onNotifyVideoQos(int width, int height, int bitrate, int fps) {
		Log.d(TAG, "onNotifyVideoQos: width =" + width + " height =" + height + " bitrate = " + bitrate + " fps = " + fps);
		if (mContext != null) {
		    Intent intent = new Intent(VideoController.ACTION_VIDEO_QOS_NOTIFY);
		    intent.putExtra("width", width);
		    intent.putExtra("height", height);
		    intent.putExtra("bitrate", bitrate); 
		    intent.putExtra("fps", fps);
		    mContext.sendBroadcast(intent);
		}
	}

	//###################### Audio Focus ###########################
	
	AudioManager mAudioMgr = null;
	AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = null;

	@SuppressLint("NewApi")
	void requestAudioFocus() {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
			return;
		}
		if (mAudioFocusChangeListener == null) {
			mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
				@Override
				public void onAudioFocusChange(int focusChange) {
					if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
						// Stop playback
					} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
						// Pause playback
					} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
						// Lower the volume
					} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
						// Rusume playback or Raise it back normal
					}
				}
			};
		}
		if (mAudioMgr == null) {
			mAudioMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		}
		if (mAudioMgr != null) {
			int ret = mAudioMgr.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			if (ret != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				if (QLog.isColorLevel())
					QLog.d("AudioManager", QLog.CLR, "request audio focus fail. " + ret);
			}
		}
	}

	@SuppressLint("NewApi")
	void abandonAudioFocus() {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
			return;
		}
		if (mAudioMgr != null) {
			mAudioMgr.abandonAudioFocus(mAudioFocusChangeListener);
			mAudioMgr = null;
		}
	}

	// 异步执行的专用线程
	public void execute(final Runnable r) {
		mThreadPool.submit(new Job<Boolean>() {
			@Override
			public Boolean run(JobContext jc) {
				r.run();
				return true;
			}
		});
	}

	public void execute(final Runnable r, FutureListener<Boolean> listener) {
		mThreadPool.submit(new Job<Boolean>() {
			@Override
			public Boolean run(JobContext jc) {
				r.run();
				return true;
			}
		}, listener);
	}

	// 播放铃声：使用MediaPlayer
	MediaPlayer mediaPlayer = null;
	int loopCount = 0;
	public void startRing(int resId, final int loop, final OnCompletionListener listener)
	{
		try
		{
			if (mediaPlayer != null) 
			{
				if (mediaPlayer.isPlaying()) 
				{
					return;
				}
				else
				{
					try 
					{
						mediaPlayer.release();
					} 
					catch (Exception e) 
					{
					} 
					finally
					{
						mediaPlayer = null;
					}
				}
			}

			mediaPlayer = MediaPlayer.create(mContext, resId);
			if (mediaPlayer == null)
			{
				return;
			}

			loopCount = loop;
			if (loopCount != 0) 
			{
			    loopCount--;
            }
			
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() 
			{
                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    if (loopCount == 0) 
                    {
                        if (listener != null) 
                        {
                            listener.onCompletion(mp);
                        }
                    } 
                    else 
                    {
                        loopCount--;
                        mediaPlayer.start();
                    }
                }
            });
			mediaPlayer.start();
			mediaPlayer.setLooping(false);
		} 
		catch(Exception e)
		{
			Log.i(TAG, e.toString());
		}		
	}
    
	public void stopRing()
	{
		try
		{
			if(mediaPlayer != null)
			{
				mediaPlayer.stop();
				mediaPlayer.release();
			}
		}
		catch (Exception e) 
		{
			
		}
		mediaPlayer = null;
	}
	
	// 播放铃声：使用SoundPool
	SoundPool mSoundPool = null;
	HashMap<Integer, Integer> mSoundRes = new HashMap<Integer, Integer>();
	int mResIdPlaying = -1;
	public void startRing2(int resId, final int loop) {
		if (null == mSoundPool) {
			mSoundPool = new SoundPool(10,AudioManager.STREAM_MUSIC, 0);
		}
		
		int soundId = -1;
		if (mSoundRes.containsKey(resId)) {
			soundId = mSoundRes.get(resId);
		}
		else {
			soundId = mSoundPool.load(getContext(), resId, 1);
			mSoundRes.put(resId, soundId);
		}
		
		mResIdPlaying = mSoundPool.play(soundId, 1, 1, 1, loop, 1);
	}
	
	public void stopRing2() {
		if (mSoundPool != null) {
			if (mResIdPlaying != -1) {
				mSoundPool.stop(mResIdPlaying);
			}
		}
		
		mResIdPlaying = -1;
	}
	
	public void startShake(){
		if (mTraeHelper != null) {
			mTraeHelper.startShake(mContext, true);
		}
	}
	
	public void stopShake(){
		if (mTraeHelper != null) {
			mTraeHelper.stopShake(mContext);
		}
	}
	
	public void sendEncodedVideoFrame(long peerUin, byte[] data,int frameType, int gopIndex, int frameIndex,long timestamp){
		if (frameType == 2 || (peerUin != 0 && mMapUinPending.containsKey(String.valueOf(peerUin)) && mMapUinPending.get(String.valueOf(peerUin)) == true)) {
			if (mVcCtrl != null) {
				mVcCtrl.sendVideoFrame(peerUin, data, frameType, gopIndex, frameIndex, timestamp);
			}
		}
	}
	
	public void exitProcess() {
		new Thread() {
			public void run() {
				try {
					sleep(2000);
					mContext.stopService(new Intent(mContext, VideoService.class));
					Process.killProcess(Process.myPid());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
}
