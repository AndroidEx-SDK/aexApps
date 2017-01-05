package com.tencent.sharp.jni;

import com.tencent.device.QLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

public class TraeAudioSession extends BroadcastReceiver {

	public interface ITraeAudioCallback {
		public void onServiceStateUpdate(boolean on);

		public void onDeviceListUpdate(String[] strDeviceList,
				String strConnectedDeviceName, String strPrevConnectedDeviceName,String strBluetoothNameIFHAS);// 1.getlist
																					// 2.event
		// public void onConnectedDeviceUpdate( String strDeviceName);

		public void onDeviceChangabledUpdate(boolean bCanChangabled);
		public void onStreamTypeUpdate(int streamType);
		// public void onRingUpdate( boolean bCanChangabled );

		// res
		public void onGetDeviceListRes(int err, String[] strDeviceList,
				String strConnectedDeviceName, String strPrevConnectedDeviceName,String strBluetoothNameIFHAS);

		// public void onConnectDeviceRes( String strDeviceName, boolean
		// bIsConnected,String err );
		public void onConnectDeviceRes(int err, String strDeviceName,
				boolean bIsConnected);

		public void onIsDeviceChangabledRes(int err, boolean bCanChangabled);

		public void onGetConnectedDeviceRes(int err, String strDeviceName);

		public void onGetConnectingDeviceRes(int err, String strDeviceName);
		public void onGetStreamTypeRes(int err, int streamType);
		
		public void onRingCompletion(int err, String userData);
		/*
		 * public void onGetDeviceList( String[] strDeviceList );
		 * 
		 * public void onDisconnectDevice( String strDeviceName, int nStatus );
		 * public void onIsDeviceConnected( String strDeviceName, boolean
		 * bIsConnected );
		 * 
		 * public void onGetConnectedDevice( String[] strDeviceList ); public
		 * void onGetConnectingDevice( String[] strDeviceList );
		 */
		public void onVoicecallPreprocessRes(int err);

		public void onAudioRouteSwitchStart(String fromDev,String toDev);
		public void onAudioRouteSwitchEnd(String connectedDev,long timeMs);
		
	}

	// Indicating this instance is created in host-process or not
	private boolean mIsHostside = false;

	// Session id
	private long mSessionId = Long.MIN_VALUE;

	private ITraeAudioCallback mCallback;
	private Context mContext;
	private String _connectedDev = TraeAudioManager.DEVICE_NONE;
	private boolean _canSwtich2Earphone = true;
	static int s_nSessionIdAllocator = 0;
/*
	public static boolean ExIsHandfree(String dev){
		if(dev==null)
			return false;
		if(dev.equals(TraeAudioManager.DEVICE_SPEAKERPHONE))
			return true;
		return false;
	}
	
	public static boolean ExHandfreeEnable(String[] strDevlist){
		boolean hf = true;
		if(strDevlist==null)
			return false;
		
		for(int i=0;i<strDevlist.length;i++){
			if(strDevlist[i].equals(TraeAudioManager.DEVICE_WIREDHEADSET)||
					strDevlist[i].equals(TraeAudioManager.DEVICE_BLUETOOTHHEADSET)){
				hf = false;
				break;
			}
		}
		return hf;
	}
	*/
	/*
	public boolean ExIsHandfree(){
		if(_connectedDev.equals(TraeAudioManager.DEVICE_SPEAKERPHONE))
			return true;
		/
        if(_connectedDev.equals(TraeAudioManager.DEVICE_WIREDHEADSET)
                ||_connectedDev.equals(TraeAudioManager.DEVICE_BLUETOOTHHEADSET)){
            return true;
        }
        /
        return false;
	}
	
	public boolean ExCanSwitchToEarphone(){
		return _canSwtich2Earphone;
	}
	
	public String ExGetConnectedDevice(){
		return _connectedDev;
	}
	*/
	
	static public long requestSessionId() {
		return ((long) ((long) android.os.Process.myPid()) << 32)
				+ ((long) ++s_nSessionIdAllocator);
	}

	static public void ExConnectDevice(Context ctx, String strDevice) {
		if (null == ctx || null == strDevice || strDevice.length()<=0) {
			return;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, Long.MIN_VALUE);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_CONNECTDEVICE);
		intent.putExtra(TraeAudioManager.CONNECTDEVICE_DEVICENAME, strDevice);

		ctx.sendBroadcast(intent);
	}
	final String TRAE_ACTION_PHONE_STATE ="android.intent.action.PHONE_STATE" ;
	public TraeAudioSession(Context ctx, ITraeAudioCallback cb) {
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"TraeAudioSession create");
		mIsHostside = (android.os.Process.myPid() == TraeAudioManager._gHostProcessId);
		mSessionId = requestSessionId();
		mCallback = cb;
		mContext = ctx;

		if (null == ctx) {
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession | Invalid parameters: ctx = "
							+ (null == ctx ? "null" : "{object}") + "; cb = "
							+ (null == cb ? "null" : "{object}"));
		}

		// if ( !mIsHostside ) {

		IntentFilter filter = new IntentFilter();
		filter.addAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_RES);
		filter.addAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_NOTIFY);
		//filter.addAction(TRAE_ACTION_PHONE_STATE); if add this action,can't receive any event on Xiaomi 2S 4.1.1
		if(ctx != null){
			if (ctx.registerReceiver(this, filter) == null) {
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession | registerReceiver failed");
			}
		}
		// }

		registerAudioSession(true);
	}

	public void release() {
		if (null != mContext) {
		    try {
		        mContext.unregisterReceiver(this);
            } catch (Exception e) {
                
            }
		}
		registerAudioSession(false);

		mContext = null;
		mCallback = null;
	}

	public void setCallback(ITraeAudioCallback cb) {
		mCallback = cb;
	}

	private int registerAudioSession(boolean bRegister) {

		if (null == mContext) {
			return -1;
		}

		if (mIsHostside) {
			return TraeAudioManager.registerAudioSession(bRegister, mSessionId,
					mContext);
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_REGISTERAUDIOSESSION);
		intent.putExtra(TraeAudioManager.REGISTERAUDIOSESSION_ISREGISTER,
				bRegister);

		mContext.sendBroadcast(intent);

		return 0;
	}

	/*
	 * devicePriority: Using like: { {DEVICE_SPEAKERPHONE, "1"},
	 * {DEVICE_BLUETOOTHHEADSET, "2"} }
	 * 
	 * No return.
	 */
	public int startService(String deviceConfig) {

		if (mIsHostside) {
			return TraeAudioManager.startService(
					TraeAudioManager.OPERATION_STARTSERVICE, mSessionId,
					mIsHostside, deviceConfig);
		}

		if (null == mContext || null == deviceConfig || deviceConfig.length()<=0) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_STARTSERVICE);
		intent.putExtra(TraeAudioManager.EXTRA_DATA_DEVICECONFIG, deviceConfig);

		mContext.sendBroadcast(intent);

		return 0;
	}
	
	public int stopService() {

		if (mIsHostside) {
			return TraeAudioManager.stopService(
					TraeAudioManager.OPERATION_STOPSERVICE, mSessionId,
					mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_STOPSERVICE);

		mContext.sendBroadcast(intent);

		return 0;
	}

	/*
	 * Return by callback.onGetDeviceList(strDeviceList[])
	 */
	public int getDeviceList() {

		if (mIsHostside) {
			return TraeAudioManager.getDeviceList(
					TraeAudioManager.OPERATION_GETDEVICELIST, mSessionId,
					mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_GETDEVICELIST);

		mContext.sendBroadcast(intent);

		return 0;
	}

	public int getStreamType() {

		if (mIsHostside) {
			return TraeAudioManager.getStreamType(
					TraeAudioManager.OPERATION_GETSTREAMTYPE, mSessionId,
					mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_GETSTREAMTYPE);

		mContext.sendBroadcast(intent);

		return 0;
	}
	/*
	 * Return by callback.onConnectDevice(strDeviceName, nStatus)
	 */
	public int connectDevice(String strDevice) {
		if (mIsHostside) {
			return TraeAudioManager.connectDevice(TraeAudioManager.OPERATION_CONNECTDEVICE, mSessionId,mIsHostside, strDevice);
		}
		if (null == mContext || null == strDevice || strDevice.length()<=0) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_CONNECTDEVICE);
		intent.putExtra(TraeAudioManager.CONNECTDEVICE_DEVICENAME, strDevice);

		mContext.sendBroadcast(intent);

		return 0;
	}
	
	public int connectHighestPriorityDevice() {
		if (mIsHostside) {
			return TraeAudioManager.connectHighestPriorityDevice(TraeAudioManager.OPERATION_CONNECT_HIGHEST_PRIORITY_DEVICE, mSessionId,mIsHostside);
		}
		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_CONNECT_HIGHEST_PRIORITY_DEVICE);

		mContext.sendBroadcast(intent);

		return 0;
	}	
	
	public int EarAction(int earAction){
		if (mIsHostside) {
			return TraeAudioManager.earAction(TraeAudioManager.OPERATION_EARACTION, mSessionId,mIsHostside, earAction);
		}
		if (null == mContext || ( earAction != TraeAudioManager.EARACTION_AWAY && earAction != TraeAudioManager.EARACTION_CLOSE )) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_EARACTION);
		intent.putExtra(TraeAudioManager.EXTRA_EARACTION, earAction);

		mContext.sendBroadcast(intent);

		return 0;
	}

	/*
	 * Return by callback.onDisconnectDevice(strDeviceName, nStatus)
	 */
	/*
	 * public int disconnectDevice( String strDevice ) {
	 * 
	 * if ( mIsHostside ) { return TraeAudioManager.disconnectDevice( mContext,
	 * mSessionId, mIsHostside, strDevice ); }
	 * 
	 * if ( null == mContext || null == strDevice || strDevice.length()<=0 ) {
	 * return -1; }
	 * 
	 * Intent intent = new Intent();
	 * intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER);
	 * intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
	 * intent.putExtra(TraeAudioManager.PARAM_OPERATION,
	 * TraeAudioManager.OPERATION_DISCONNECTDEVICE);
	 * intent.putExtra(TraeAudioManager.DISCONNECTDEVICE_DEVICENAME, strDevice);
	 * 
	 * mContext.sendBroadcast(intent);
	 * 
	 * return 0; }
	 */

	/*
	 * Return by callback.onIsDeviceConnected(strDeviceName, bIsConnected)
	 */
	/*
	 * public int isDeviceConnected( String strDevice ) {
	 * 
	 * if ( mIsHostside ) { return TraeAudioManager.isDeviceConnected( mContext,
	 * mSessionId, mIsHostside, strDevice ); }
	 * 
	 * if ( null == mContext || null == strDevice || strDevice.length()<=0 ) {
	 * return -1; }
	 * 
	 * Intent intent = new Intent();
	 * intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER);
	 * intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
	 * intent.putExtra(TraeAudioManager.PARAM_OPERATION,
	 * TraeAudioManager.OPERATION_ISDEVICECONNECTED);
	 * intent.putExtra(TraeAudioManager.ISDEVICECONNECTED_DEVICENAME,
	 * strDevice);
	 * 
	 * mContext.sendBroadcast(intent);
	 * 
	 * return 0; }
	 */

	/*
	 * Return by callback.onIsDeviceChangabled(bIsChangabled)
	 */
	public int isDeviceChangabled() {

		if (mIsHostside) {
			return TraeAudioManager.isDeviceChangabled(
					TraeAudioManager.OPERATION_ISDEVICECHANGABLED, mSessionId,
					mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_ISDEVICECHANGABLED);

		mContext.sendBroadcast(intent);

		return 0;
	}

	/*
	 * Return by callback.onGetConnectedDevice( strDeviceList[] )
	 */
	public int getConnectedDevice() {

		if (mIsHostside) {
			return TraeAudioManager.getConnectedDevice(
					TraeAudioManager.OPERATION_GETCONNECTEDDEVICE, mSessionId,
					mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_GETCONNECTEDDEVICE);

		mContext.sendBroadcast(intent);

		return 0;
	}

	/*
	 * Return by callback.onGetConnectingDevice( strDeviceList[] )
	 */
	public int getConnectingDevice() {

		if (mIsHostside) {
			return TraeAudioManager.getConnectingDevice(
					TraeAudioManager.OPERATION_GETCONNECTINGDEVICE, mSessionId,
					mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_GETCONNECTINGDEVICE);

		mContext.sendBroadcast(intent);

		return 0;
	}

	
	public int voiceCallPreprocess(int modePolicy,int streamType) {
		if (mIsHostside) {
			return TraeAudioManager.voicecallPreprocess(
					TraeAudioManager.OPERATION_VOICECALL_PREPROCESS,
					mSessionId, mIsHostside,modePolicy,streamType);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_MODEPOLICY, modePolicy);
		intent.putExtra(TraeAudioManager.PARAM_STREAMTYPE, streamType);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_VOICECALL_PREPROCESS);

		mContext.sendBroadcast(intent);
		return 0;
	}

	public int voiceCallPostprocess() {
		if (mIsHostside) {
			return TraeAudioManager.voicecallPostprocess(
					TraeAudioManager.OPERATION_VOICECALL_POSTPROCESS,
					mSessionId, mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_VOICECALL_POSTPROCESS);

		mContext.sendBroadcast(intent);
		return 0;
	}
	
	public int voiceCallAudioParamChanged(int modePolicy,int streamType) {
		if (mIsHostside) {
			return TraeAudioManager.voiceCallAudioParamChanged(
					TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST,
					mSessionId, mIsHostside,modePolicy,streamType);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_MODEPOLICY, modePolicy);
		intent.putExtra(TraeAudioManager.PARAM_STREAMTYPE, streamType);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_VOICECALL_AUDIOPARAM_CHANGED);

		mContext.sendBroadcast(intent);
		return 0;
	}
	public int startRing(int dataSource, int rsId, Uri res, String strFilePath,
			boolean bLoop) {
		if (mIsHostside) {
			return TraeAudioManager.startRing(
					TraeAudioManager.OPERATION_STARTRING, mSessionId,
					mIsHostside, dataSource, rsId, res, strFilePath, bLoop,1,"normal-ring",false);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_RING_DATASOURCE, dataSource);
		intent.putExtra(TraeAudioManager.PARAM_RING_RSID, rsId);
		intent.putExtra(TraeAudioManager.PARAM_RING_URI, res);
		intent.putExtra(TraeAudioManager.PARAM_RING_FILEPATH, strFilePath);
		intent.putExtra(TraeAudioManager.PARAM_RING_LOOP, bLoop);
		intent.putExtra(TraeAudioManager.PARAM_RING_MODE, false);
		intent.putExtra(TraeAudioManager.PARAM_RING_USERDATA_STRING,
				"normal-ring");
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_STARTRING);

		mContext.sendBroadcast(intent);
		return 0;
	}

	public int startRing(int dataSource, int rsId, Uri res, String strFilePath,
			boolean bLoop,int loopCount, String userData) {
		if (mIsHostside) {
			return TraeAudioManager.startRing(
					TraeAudioManager.OPERATION_STARTRING, mSessionId,
					mIsHostside, dataSource, rsId, res, strFilePath, bLoop,loopCount,userData,false);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_RING_DATASOURCE, dataSource);
		intent.putExtra(TraeAudioManager.PARAM_RING_RSID, rsId);
		intent.putExtra(TraeAudioManager.PARAM_RING_URI, res);
		intent.putExtra(TraeAudioManager.PARAM_RING_FILEPATH, strFilePath);
		intent.putExtra(TraeAudioManager.PARAM_RING_LOOP, bLoop);
		intent.putExtra(TraeAudioManager.PARAM_RING_LOOPCOUNT, loopCount);
		intent.putExtra(TraeAudioManager.PARAM_RING_MODE, false);
		
		intent.putExtra(TraeAudioManager.PARAM_RING_USERDATA_STRING, userData);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_STARTRING);

		mContext.sendBroadcast(intent);
		return 0;
	}
	public int startRing(int dataSource, int rsId, Uri res, String strFilePath,
			boolean bLoop,int loopCount, String userData,boolean ringMode) {
		if (mIsHostside) {
			return TraeAudioManager.startRing(
					TraeAudioManager.OPERATION_STARTRING, mSessionId,
					mIsHostside, dataSource, rsId, res, strFilePath, bLoop,loopCount,userData,ringMode);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_RING_DATASOURCE, dataSource);
		intent.putExtra(TraeAudioManager.PARAM_RING_RSID, rsId);
		intent.putExtra(TraeAudioManager.PARAM_RING_URI, res);
		intent.putExtra(TraeAudioManager.PARAM_RING_FILEPATH, strFilePath);
		intent.putExtra(TraeAudioManager.PARAM_RING_LOOP, bLoop);
		intent.putExtra(TraeAudioManager.PARAM_RING_LOOPCOUNT, loopCount);
		intent.putExtra(TraeAudioManager.PARAM_RING_MODE, ringMode);
		
		intent.putExtra(TraeAudioManager.PARAM_RING_USERDATA_STRING, userData);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_STARTRING);

		mContext.sendBroadcast(intent);
		return 0;
	}	

	public int stopRing() {
		if (mIsHostside) {
			return TraeAudioManager.stopRing(
					TraeAudioManager.OPERATION_STOPRING, mSessionId,
					mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_STOPRING);

		mContext.sendBroadcast(intent);
		return 0;
	}
	public int requestReleaseAudioFocus(){
		if (mIsHostside) {
			return TraeAudioManager.requestReleaseAudioFocus(
					TraeAudioManager.OPERATION_REQUEST_RELEASE_AUDIO_FOCUS, mSessionId,
					mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_REQUEST_RELEASE_AUDIO_FOCUS);

		mContext.sendBroadcast(intent);
		return 0;
	}
	public int recoverAudioFocus(){
		if (mIsHostside) {
			return TraeAudioManager.recoverAudioFocus(
					TraeAudioManager.OPERATION_RECOVER_AUDIO_FOCUS, mSessionId,
					mIsHostside);
		}

		if (null == mContext) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST);
		intent.putExtra(TraeAudioManager.PARAM_SESSIONID, mSessionId);
		intent.putExtra(TraeAudioManager.PARAM_OPERATION,
				TraeAudioManager.OPERATION_RECOVER_AUDIO_FOCUS);

		mContext.sendBroadcast(intent);
		return 0;
	}
	/*
	private void phoneState(Context context, Intent intent){
		 TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
			
         String Tag = "trae";
         String call_number = "";
         Log.d(Tag, "getCallState:"+tm.getCallState());
         switch (tm.getCallState()) {

         

         case TelephonyManager.CALL_STATE_RINGING:

             call_number = intent.getStringExtra("incoming_number");

             Log.d(Tag, String.format("call Ringing : %s", call_number));

             break;

         

         case TelephonyManager.CALL_STATE_OFFHOOK:

             Log.d(Tag, String.format("call Offhook : %s", call_number));

             break;

        

         case TelephonyManager.CALL_STATE_IDLE:

             Log.d(Tag, "call Idle");

             break;
         default:
         	
         	break;
         }
	}
	*/
	@Override
	public void onReceive(Context context, Intent intent) {
		int errCode = 0;
		try{
			if (null == intent) {
				return;
			}
	/*
			 if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession| nSessinId = " + mSessionId
			 + " onReceive::intent:" +
			 intent.toString()+" intent.getAction():"+intent.getAction());
			 */
			long nSessionId = intent.getLongExtra(TraeAudioManager.PARAM_SESSIONID,
					Long.MIN_VALUE);
			String strOperation = intent
					.getStringExtra(TraeAudioManager.PARAM_OPERATION);
			errCode = intent.getIntExtra(TraeAudioManager.PARAM_RES_ERRCODE, 0);
			// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession| nSessinId = " + mSessionId
			// + " onReceive:: Action"+intent.getAction()+" opt:"+strOperation);
			/*if(TRAE_ACTION_PHONE_STATE.equals(intent.getAction())){
				phoneState(context,intent);
			}
			else 
			*/
			if (TraeAudioManager.ACTION_TRAEAUDIOMANAGER_NOTIFY.equals(intent
					.getAction())) {
				/*
				 * public static final String NOTIFY_SERVICE_STATE =
				 * "NOTIFY_SERVICE_STATE"; public static final String
				 * NOTIFY_SERVICE_STATE_DATE = "NOTIFY_SERVICE_STATE_DATE";
				 */

				if (TraeAudioManager.NOTIFY_SERVICE_STATE.equals(strOperation)) {
					boolean on = intent.getBooleanExtra(
							TraeAudioManager.NOTIFY_SERVICE_STATE_DATE, false);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onServiceStateUpdate]"
									+ (on ? "on" : "off"));
					if (null != mCallback) {
						mCallback.onServiceStateUpdate(on);
					}
				} else if (TraeAudioManager.NOTIFY_DEVICELIST_UPDATE
						.equals(strOperation)) {
					String[] strDeviceList = intent
							.getStringArrayExtra(TraeAudioManager.EXTRA_DATA_AVAILABLEDEVICE_LIST);
					String con = intent
							.getStringExtra(TraeAudioManager.EXTRA_DATA_CONNECTEDDEVICE);
					String prevCon = intent
							.getStringExtra(TraeAudioManager.EXTRA_DATA_PREV_CONNECTEDDEVICE);
					String btName = intent
							.getStringExtra(TraeAudioManager.EXTRA_DATA_IF_HAS_BLUETOOTH_THIS_IS_NAME);
					
					String str = "\n";
					boolean _cs2earphone = true;
					for (int i = 0; i < strDeviceList.length; i++) {
						str += "AudioSession|    " + i + " " + strDeviceList[i]
								+ "\n";
						if(strDeviceList[i].equals(TraeAudioManager.DEVICE_WIREDHEADSET)||
								strDeviceList[i].equals(TraeAudioManager.DEVICE_BLUETOOTHHEADSET))
							_cs2earphone = false;
					}
					str += "\n";

					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onDeviceListUpdate] "
							+ " connected:" + con + " prevConnected:" + prevCon
							+ " bt:"+btName+" Num:" + strDeviceList.length + str);
					_canSwtich2Earphone = _cs2earphone;
					_connectedDev = con;
					if (null != mCallback) {

						mCallback.onDeviceListUpdate(strDeviceList, con, prevCon,btName);
					}
				} else if (TraeAudioManager.NOTIFY_DEVICECHANGABLE_UPDATE
						.equals(strOperation)) {
					boolean bIsChangabled = intent.getBooleanExtra(
							TraeAudioManager.NOTIFY_DEVICECHANGABLE_UPDATE_DATE,
							true);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onDeviceChangabledUpdate]"
									+ bIsChangabled);
					if (null != mCallback) {
						mCallback.onDeviceChangabledUpdate(bIsChangabled);
					}
				}else if (TraeAudioManager.NOTIFY_STREAMTYPE_UPDATE
						.equals(strOperation)) {
					int st = intent
							.getIntExtra(TraeAudioManager.EXTRA_DATA_STREAMTYPE,-1);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onStreamTypeUpdate] err:"
									+ errCode + " st:" + st);

					if (null != mCallback) {
						mCallback.onStreamTypeUpdate(st);
					}
				}else if (TraeAudioManager.NOTIFY_ROUTESWITCHSTART.equals(strOperation)) {
					String from;
					String to;
					
					from = intent.getStringExtra(TraeAudioManager.EXTRA_DATA_ROUTESWITCHSTART_FROM);
					to = intent.getStringExtra(TraeAudioManager.EXTRA_DATA_ROUTESWITCHSTART_TO);
					if ( (null != mCallback) && (from!=null) && (to!=null) ) {
						mCallback.onAudioRouteSwitchStart(from, to);
					}
					
				}else if (TraeAudioManager.NOTIFY_ROUTESWITCHEND.equals(strOperation)) {
					String d;
					long t;
					d = intent.getStringExtra(TraeAudioManager.EXTRA_DATA_ROUTESWITCHEND_DEV);
					t = intent.getLongExtra(TraeAudioManager.EXTRA_DATA_ROUTESWITCHEND_TIME, -1);
					if ( (null != mCallback) && (d!=null) && (t!=-1) ) {
						mCallback.onAudioRouteSwitchEnd(d, t);
					}
				}
				

				
			} else if (TraeAudioManager.ACTION_TRAEAUDIOMANAGER_RES.equals(intent
					.getAction())) {

				if (mSessionId != nSessionId) {
					return;
				}
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession| Opt:"+strOperation+" res:"+errCode);
				if (TraeAudioManager.OPERATION_GETDEVICELIST.equals(strOperation)) {
					String[] strDeviceList = intent
							.getStringArrayExtra(TraeAudioManager.EXTRA_DATA_AVAILABLEDEVICE_LIST);
					String con = intent
							.getStringExtra(TraeAudioManager.EXTRA_DATA_CONNECTEDDEVICE);
					String prevCon = intent
							.getStringExtra(TraeAudioManager.EXTRA_DATA_PREV_CONNECTEDDEVICE);
					String btName = intent
							.getStringExtra(TraeAudioManager.EXTRA_DATA_IF_HAS_BLUETOOTH_THIS_IS_NAME);
					
					String str = "\n";
					boolean _cs2earphone = true;
					for (int i = 0; i < strDeviceList.length; i++) {
						str += "AudioSession|    " + i + " " + strDeviceList[i]
								+ "\n";
						if(strDeviceList[i].equals(TraeAudioManager.DEVICE_WIREDHEADSET)||
								strDeviceList[i].equals(TraeAudioManager.DEVICE_BLUETOOTHHEADSET))
							_cs2earphone = false;
					}
					str += "\n";
					_canSwtich2Earphone = _cs2earphone;
					_connectedDev = con;
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onGetDeviceListRes] err:"
									+ errCode + " connected:" + con
									+ " prevConnected:" + prevCon+" bt:"+btName + " Num:"
									+ strDeviceList.length + str);

					if (null != mCallback) {
						mCallback.onGetDeviceListRes(errCode, strDeviceList, con,
								prevCon,btName);
					}
				} else if (TraeAudioManager.OPERATION_CONNECTDEVICE.equals(strOperation)) {
					String strDeviceName = intent
							.getStringExtra(TraeAudioManager.CONNECTDEVICE_RESULT_DEVICENAME);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onConnectDeviceRes] err:"
									+ errCode + " dev:" + strDeviceName);
					if (null != mCallback) {
						mCallback.onConnectDeviceRes(errCode, strDeviceName,
								errCode == TraeAudioManager.RES_ERRCODE_NONE);
					}
				} else if(TraeAudioManager.OPERATION_EARACTION.equals(strOperation)){//
					int _earAction = intent.getIntExtra(TraeAudioManager.EXTRA_EARACTION, -1);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onConnectDeviceRes] err:"
									+ errCode + " earAction:" + _earAction);
					if (null != mCallback) {
				//		mCallback.onConnectDeviceRes(errCode, strDeviceName,
				//				errCode == TraeAudioManager.RES_ERRCODE_NONE);
					}
				} 
				else if (TraeAudioManager.OPERATION_ISDEVICECHANGABLED
						.equals(strOperation)) {
					boolean bIsChangabled = intent
							.getBooleanExtra(
									TraeAudioManager.ISDEVICECHANGABLED_RESULT_ISCHANGABLED,
									false);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onIsDeviceChangabledRes] err:"
									+ errCode + " Changabled:"
									+ (bIsChangabled ? "Y" : "N"));

					if (null != mCallback) {
						mCallback.onIsDeviceChangabledRes(errCode, bIsChangabled);
					}
				} else if (TraeAudioManager.OPERATION_GETCONNECTEDDEVICE
						.equals(strOperation)) {
					String strDeviceName = intent
							.getStringExtra(TraeAudioManager.GETCONNECTEDDEVICE_RESULT_LIST);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onGetConnectedDeviceRes] err:"
									+ errCode + " dev:" + strDeviceName);
					
					if (null != mCallback) {
						mCallback.onGetConnectedDeviceRes(errCode, strDeviceName);
					}
				} else if (TraeAudioManager.OPERATION_GETCONNECTINGDEVICE
						.equals(strOperation)) {
					String strDeviceName = intent
							.getStringExtra(TraeAudioManager.GETCONNECTINGDEVICE_RESULT_LIST);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onGetConnectingDeviceRes] err:"
									+ errCode + " dev:" + strDeviceName);

					if (null != mCallback) {
						mCallback.onGetConnectingDeviceRes(errCode, strDeviceName);
					}
				} //
				else if (TraeAudioManager.OPERATION_GETSTREAMTYPE
						.equals(strOperation)) {
					int st = intent
							.getIntExtra(TraeAudioManager.EXTRA_DATA_STREAMTYPE,-1);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onGetStreamTypeRes] err:"
									+ errCode + " st:" + st);

					if (null != mCallback) {
						mCallback.onGetStreamTypeRes(errCode, st);
					}
				}
				else if (TraeAudioManager.NOTIFY_RING_COMPLETION
						.equals(strOperation)) {
					String userData = intent
							.getStringExtra(TraeAudioManager.PARAM_RING_USERDATA_STRING);
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onRingCompletion] err:"
									+ errCode + " userData:" + userData);

					if (null != mCallback) {
						mCallback.onRingCompletion(errCode, userData);
					}
				}else if(TraeAudioManager.OPERATION_VOICECALL_PREPROCESS.equals(strOperation)){
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onVoicecallPreprocess] err:"
							+ errCode);
					if (null != mCallback) {
						mCallback.onVoicecallPreprocessRes(errCode);
					}
				}

				//
			}
		}catch(Exception e){
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"AudioSession| nSessinId = " + mSessionId
					 + " onReceive::intent:" +
					 intent.toString()+" intent.getAction():"+intent.getAction()+" Exception:"+e.getMessage());
		}

	}
}
