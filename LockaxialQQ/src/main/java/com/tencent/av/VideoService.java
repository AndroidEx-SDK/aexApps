package com.tencent.av;

import com.tencent.device.ITXDeviceService;
import com.tencent.device.TXDeviceService;
import com.tencent.devicedemo.VideoChatActivityHW;
import com.tencent.devicedemo.VideoChatActivitySF;
import com.tencent.devicedemo.BinderActivity.NotifyReceiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

public class VideoService extends Service { 

	private ITXDeviceService mTXDeviceService = null;
	
	public VideoService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {   
		super.onCreate();
			
		Intent intent = new Intent();
		intent.setAction("com.tencent.device.RemoteTXDeviceService");
		intent.setPackage("com.tencent.devicedemo");
		bindService(intent, mConn, Context.BIND_AUTO_CREATE);
		
	}
	
	@Override
	public void onDestroy() {
		
		super.onDestroy(); 
	}
	
	private ServiceConnection mConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			mTXDeviceService = ITXDeviceService.Stub.asInterface(arg1);
			
			VideoController.getInstance().setTXDeviceService(mTXDeviceService);
	        
			//初始化音视频引擎
	        //bHwEnc和bHwDec可以组成四种组合：软编软解（支持）、软编硬解（不支持）、硬编硬解（支持）、硬编软解（支持）
	        //public void initVcController(Context context, boolean bHwEnc, boolean bHwDec)
			VideoController.getInstance().initVcController(VideoService.this, false, false);
			
			IntentFilter filter = new IntentFilter();
			filter.addAction(TXDeviceService.OnSendVideoCall);
			filter.addAction(TXDeviceService.OnSendVideoCallM2M);
			filter.addAction(TXDeviceService.OnSendVideoCMD);
			filter.addAction(TXDeviceService.OnReceiveVideoBuffer);
			filter.addAction(TXDeviceService.StartVideoChatActivity);
			registerReceiver(mVideoReceiver, filter);
			
			try {
				mTXDeviceService.notifyVideoServiceStarted();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			mTXDeviceService = null;
			unregisterReceiver(mVideoReceiver);
		}	
	};
	
	public BroadcastReceiver mVideoReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			String action = intent.getAction();
			if (action == TXDeviceService.OnSendVideoCall) {
				VideoController.getInstance().onSendVideoCall(intent.getByteArrayExtra("msg"));
			}
			else if (action == TXDeviceService.OnSendVideoCallM2M) {
				VideoController.getInstance().onSendVideoCallM2M(intent.getByteArrayExtra("msg"));
			}
			else if (action == TXDeviceService.OnSendVideoCMD) {
				VideoController.getInstance().onSendVideoCMD(intent.getByteArrayExtra("msg"));
			}
			else if (action == TXDeviceService.OnReceiveVideoBuffer) {
				VideoController.getInstance().onReceiveVideoBuffer(intent.getByteArrayExtra("msg"));
			}
			else if (action == TXDeviceService.StartVideoChatActivity) {
				if (VideoController.getInstance().hasPendingChannel()) {
					Toast.makeText(VideoService.this, "视频监控中，请稍后……", Toast.LENGTH_LONG).show();
				}
				else {
					Intent videoIntent = null;
					if (VideoController.isHardwareEncoderEnabled()) {
						videoIntent = new Intent(VideoService.this, VideoChatActivityHW.class);
					}
					else {
						videoIntent = new Intent(VideoService.this, VideoChatActivitySF.class); 
					}
					videoIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					videoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					videoIntent.putExtra("peerid", String.valueOf(intent.getLongExtra("peerid", 0))); 
			        startActivity(videoIntent);					
				}
			}
		}
	};
}
