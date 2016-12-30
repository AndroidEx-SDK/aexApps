package com.tencent.sharp.jni;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import com.tencent.device.QLog;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;

/*
 // Usage: 	

 HashMap<String, Object> params = new HashMap<String, Object>();
 params.put(TraeAudioManager.Parameters.CONTEXT, _context);
 params.put(TraeAudioManager.Parameters.MODEPOLICY, -1);
 params.put(TraeAudioManager.Parameters.BLUETOOTHPOLICY, -1);        
 TraeAudioManager.init(params);

 TraeAudioManager.AudioSession as = TraeAudioManager.createAudioSession( ctx, callback );
 as.getDeviceList();
 as.connectDevice( TraeAudioManager.DEVICE_BLUETOOTHHEADSET );
 // Other operations...	
 * 
 * 1、初始化时切换到高优先级设备
 2、设备插入切换到新加入设备
 3、设备拔出时优先使用当前设备，否则切换到高优先级设备
 * 
 * 
 */
@SuppressLint("NewApi")
public class TraeAudioManager extends BroadcastReceiver {

	public static final String ACTION_TRAEAUDIOMANAGER_REQUEST = "com.tencent.sharp.ACTION_TRAEAUDIOMANAGER_REQUEST";
	public static final String ACTION_TRAEAUDIOMANAGER_RES = "com.tencent.sharp.ACTION_TRAEAUDIOMANAGER_RES";
	public static final String ACTION_TRAEAUDIOMANAGER_NOTIFY = "com.tencent.sharp.ACTION_TRAEAUDIOMANAGER_NOTIFY";

	public static final String PARAM_OPERATION = "PARAM_OPERATION";
	public static final String PARAM_SESSIONID = "PARAM_SESSIONID";
	public static final String PARAM_ISHOSTSIDE = "PARAM_ISHOSTSIDE";
	public static final String PARAM_RES_ERRCODE = "PARAM_RES_ERRCODE";

	public static final int RES_ERRCODE_NONE = 0;
	public static final int RES_ERRCODE_SERVICE_OFF = 1;
	public static final int RES_ERRCODE_VOICECALL_EXIST = 2;
	public static final int RES_ERRCODE_VOICECALL_NOT_EXIST = 3;
	public static final int RES_ERRCODE_STOPRING_INTERRUPT = 4;
	public static final int RES_ERRCODE_RING_NOT_EXIST = 5;
	public static final int RES_ERRCODE_VOICECALLPOST_INTERRUPT = 6;
	public static final int RES_ERRCODE_DEVICE_UNKOWN = 7;
	public static final int RES_ERRCODE_DEVICE_NOT_VISIABLE = 8;
	public static final int RES_ERRCODE_DEVICE_UNCHANGEABLE = 9;
	public static final int RES_ERRCODE_DEVICE_BTCONNCECTED_TIMEOUT = 10;

	// public static final String PARAM_CONTEXT = "PARAM_CONTEXT";
	public static final String PARAM_STATUS = "PARAM_STATUS";
	public static final String PARAM_DEVICE = "PARAM_DEVICE";
	public static final String PARAM_ERROR = "PARAM_ERROR";

	public static final String PARAM_MODEPOLICY = "PARAM_MODEPOLICY";
	public static final String PARAM_STREAMTYPE = "PARAM_STREAMTYPE";

	// int dataSource,int rsId,Uri res,String strFilePath, boolean bLoop
	public static final String PARAM_RING_DATASOURCE = "PARAM_RING_DATASOURCE";
	public static final String PARAM_RING_RSID = "PARAM_RING_RSID";
	public static final String PARAM_RING_URI = "PARAM_RING_URI";
	public static final String PARAM_RING_FILEPATH = "PARAM_RING_FILEPATH";
	public static final String PARAM_RING_LOOP = "PARAM_RING_LOOP";
	public static final String PARAM_RING_LOOPCOUNT = "PARAM_RING_LOOPCOUNT";
	public static final String PARAM_RING_MODE = "PARAM_RING_MODE";

	// loopCount
	public static final String PARAM_RING_USERDATA_STRING = "PARAM_RING_USERDATA_STRING";

	/*
	 * public static final String OPERATION_EXSETSPEAKER =
	 * "OPERATION_EXSETSPEAKER"; public static final String EXSETSPEAKER_PARAM =
	 * "EXSETSPEAKER_PARAM"; // boolean
	 * 
	 * public static final String OPERATION_SETSPEAKER = "OPERATION_SETSPEAKER";
	 * public static final String SETSPEAKER_PARAM = "SETSPEAKER_PARAM"; //
	 * boolean
	 * 
	 * public static final String OPERATION_SETPARAM = "OPERATION_SETPARAM";
	 * public static final String SETPARAM_MODEPOLICY = "SETPARAM_MODEPOLICY";
	 * // int public static final String SETPARAM_BLUETOOTHPOLICY =
	 * "SETPARAM_BLUETOOTHPOLICY"; // int
	 * 
	 * public static final String OPERATION_SETMODE = "OPERATION_SETMODE";
	 * public static final String SETMODE_MODE = "SETMODE_MODE"; // int
	 * 
	 * public static final String OPERATION_MARK = "OPERATION_MARK"; public
	 * static final String MARK_STREAMTYPE = "MARK_STREAMTYPE"; // int
	 * 
	 * public static final String OPERATION_RECOVER = "OPERATION_RECOVER";
	 * 
	 * public static final String OPERATION_INITSPEAKER =
	 * "OPERATION_INITSPEAKER"; public static final String INITSPEAKER_PARAM =
	 * "INITSPEAKER_PARAM"; // boolean
	 * 
	 * public static final String OPERATION_AUDIODEVICEUPDATE =
	 * "OPERATION_AUDIODEVICEUPDATE"; public static final String
	 * AUDIODEVICEUPDATE_CURRENTDEVICESET =
	 * "AUDIODEVICEUPDATE_CURRENTDEVICESET"; // int
	 */
	public static final String OPERATION_STARTSERVICE = "OPERATION_STARTSERVICE";
	public static final String EXTRA_DATA_DEVICECONFIG = "EXTRA_DATA_DEVICECONFIG";

	public static final String OPERATION_STOPSERVICE = "OPERATION_STOPSERVICE";

	public static final String OPERATION_REGISTERAUDIOSESSION = "OPERATION_REGISTERAUDIOSESSION";
	public static final String REGISTERAUDIOSESSION_ISREGISTER = "REGISTERAUDIOSESSION_ISREGISTER";

	// public static final String OPERATION_SETDEVICECONFIG =
	// "OPERATION_SETDEVICECONFIG";
	// public static final String SETDEVICECONFIG_CONFIGSTRING =
	// "SETDEVICECONFIG_CONFIGSTRING"; // String Array Array, like: {
	// {DEVICE_SPEAKERPHONE, "1"}, {DEVICE_LISTENPHONE, "2"} }

	/*
	 * public void onGetDeviceListRes( String[] strDeviceList ); // public void
	 * onConnectDeviceRes( String strDeviceName, boolean bIsConnected,String err
	 * ); public void onConnectDeviceRes( String strDeviceName, boolean
	 * bIsConnected); public void onIsDeviceChangabledRes( boolean
	 * bCanChangabled ); public void onGetConnectedDeviceRes( String[]
	 * strDeviceList ); public void onGetConnectingDeviceRes( String[]
	 * strDeviceList );
	 */
	public static final String OPERATION_GETDEVICELIST = "OPERATION_GETDEVICELIST";
	public static final String OPERATION_GETSTREAMTYPE = "OPERATION_GETSTREAMTYPE";
	//
	// public static final String OPERATION_GETDEVICELIST_RESULT =
	// "OPERATION_GETDEVICELIST_RESULT";
	// public static final String GETDEVICELIST_RESULT_LIST =
	// "GETDEVICELIST_RESULT_LIST"; // See to DeviceList, String Array like: {
	// DEVICE_SPEAKERPHONE, DEVICE_LISTENPHONE }

	public static final String OPERATION_CONNECTDEVICE = "OPERATION_CONNECTDEVICE";
	public static final String CONNECTDEVICE_DEVICENAME = "CONNECTDEVICE_DEVICENAME";
	// public static final String OPERATION_CONNECTDEVICE_RESULT =
	// "OPERATION_CONNECTDEVICE_RESULT";
	public static final String CONNECTDEVICE_RESULT_DEVICENAME = "CONNECTDEVICE_RESULT_DEVICENAME"; // See
																									// to
																									// DeviceStatus
	public static final String OPERATION_CONNECT_HIGHEST_PRIORITY_DEVICE = "OPERATION_CONNECT_HIGHEST_PRIORITY_DEVICE";
	public static final String OPERATION_ISDEVICECHANGABLED = "OPERATION_ISDEVICECHANGABLED";
	// public static final String OPERATION_ISDEVICECHANGABLED_REULT =
	// "OPERATION_ISDEVICECHANGABLED_REULT";
	public static final String ISDEVICECHANGABLED_RESULT_ISCHANGABLED = "ISDEVICECHANGABLED_REULT_ISCHANGABLED";

	public static final String OPERATION_GETCONNECTEDDEVICE = "OPERATION_GETCONNECTEDDEVICE";
	// public static final String OPERATION_GETCONNECTEDDEVICE_REULT =
	// "OPERATION_GETCONNECTEDDEVICE_REULT";
	public static final String GETCONNECTEDDEVICE_RESULT_LIST = "GETCONNECTEDDEVICE_REULT_LIST";

	public static final String OPERATION_GETCONNECTINGDEVICE = "OPERATION_GETCONNECTINGDEVICE";
	// public static final String OPERATION_GETCONNECTINGDEVICE_REULT =
	// "OPERATION_GETCONNECTINGDEVICE_REULT";
	public static final String GETCONNECTINGDEVICE_RESULT_LIST = "GETCONNECTINGDEVICE_REULT_LIST";
	public static final String EXTRA_DATA_STREAMTYPE = "EXTRA_DATA_STREAMTYPE";

	public static final String OPERATION_VOICECALL_PREPROCESS = "OPERATION_VOICECALL_PREPROCESS";
	public static final String OPERATION_VOICECALL_POSTPROCESS = "OPERATION_VOICECALL_POSTROCESS";
	public static final String OPERATION_STARTRING = "OPERATION_STARTRING";
	public static final String OPERATION_STOPRING = "OPERATION_STOPRING";
	public static final String OPERATION_REQUEST_RELEASE_AUDIO_FOCUS = "OPERATION_REQUEST_RELEASE_AUDIO_FOCUS";
	public static final String OPERATION_RECOVER_AUDIO_FOCUS = "OPERATION_RECOVER_AUDIO_FOCUS";
	
	public static final String OPERATION_VOICECALL_AUDIOPARAM_CHANGED = "OPERATION_VOICECALL_AUDIOPARAM_CHANGED";
	/*
	 * public void onDeviceListUpdate( String[] strDeviceList );//1.getlist
	 * 2.event public void onConnectedDeviceUpdate( String strDeviceName,
	 * boolean bIsConnected ); public void onDeviceChangabledUpdate( boolean
	 * bCanChangabled );AvailableDeviceList
	 */
	public static final String NOTIFY_SERVICE_STATE = "NOTIFY_SERVICE_STATE";
	public static final String NOTIFY_SERVICE_STATE_DATE = "NOTIFY_SERVICE_STATE_DATE";

	public static final String NOTIFY_DEVICELIST_UPDATE = "NOTIFY_DEVICELISTUPDATE";
	public static final String EXTRA_DATA_AVAILABLEDEVICE_LIST = "EXTRA_DATA_AVAILABLEDEVICE_LIST";
	public static final String EXTRA_DATA_PREV_CONNECTEDDEVICE = "EXTRA_DATA_PREV_CONNECTEDDEVICE";
	public static final String EXTRA_DATA_CONNECTEDDEVICE = "EXTRA_DATA_CONNECTEDDEVICE";
	public static final String EXTRA_DATA_IF_HAS_BLUETOOTH_THIS_IS_NAME = "EXTRA_DATA_IF_HAS_BLUETOOTH_THIS_IS_NAME";

	// public static final String NOTIFY_CONNECTEDDEVICE_UPDATE =
	// "NOTIFY_CONNECTEDDEVICE_UPDATE";
	// public static final String NOTIFY_CONNECTEDDEVICE_UPDATE_DEVICENAME =
	// "NOTIFY_CONNECTEDDEVICE_UPDATE_DEVICENAME";

	public static final String NOTIFY_DEVICECHANGABLE_UPDATE = "NOTIFY_DEVICECHANGABLE_UPDATE";
	public static final String NOTIFY_DEVICECHANGABLE_UPDATE_DATE = "NOTIFY_DEVICECHANGABLE_UPDATE_DATE";
	public static final String NOTIFY_RING_COMPLETION = "NOTIFY_RING_COMPLETION";
	public static final String NOTIFY_STREAMTYPE_UPDATE = "NOTIFY_STREAMTYPE_UPDATE";
	
	public static final String NOTIFY_ROUTESWITCHSTART = "NOTIFY_ROUTESWITCHSTART";
	public static final String EXTRA_DATA_ROUTESWITCHSTART_FROM = "EXTRA_DATA_ROUTESWITCHSTART_FROM";
	public static final String EXTRA_DATA_ROUTESWITCHSTART_TO = "EXTRA_DATA_ROUTESWITCHSTART_TO";
	
	public static final String NOTIFY_ROUTESWITCHEND = "NOTIFY_ROUTESWITCHEND";
	public static final String EXTRA_DATA_ROUTESWITCHEND_DEV = "EXTRA_DATA_ROUTESWITCHEND_DEV";
	public static final String EXTRA_DATA_ROUTESWITCHEND_TIME = "EXTRA_DATA_ROUTESWITCHEND_TIME";


	// public static final String ACTION_TRAE_SETSPEAKER_RESULT =
	// "com.tencent.sharp.ACTION_TRAE_SETSPEAKER_RESULT";
	// public static final String CURRENT_SPEAKER_STATUS =
	// "CurrentSpeakerStatus"; // int: -1 for unknown, 0 for off, 1 for on
	public static final int EARACTION_AWAY = 0;
	public static final int EARACTION_CLOSE = 1;
	public static final String OPERATION_EARACTION = "OPERATION_EARACTION";
	public static final String EXTRA_EARACTION = "EXTRA_EARACTION";

	// Parameters for init()
	public class Parameters {
		public static final String CONTEXT = "com.tencent.sharp.TraeAudioManager.Parameters.CONTEXT"; // Context
		public static final String MODEPOLICY = "com.tencent.sharp.TraeAudioManager.Parameters.MODEPOLICY"; // int
		public static final String BLUETOOTHPOLICY = "com.tencent.sharp.TraeAudioManager.Parameters.BLUETOOTHPOLICY"; // int
		public static final String DEVICECONFIG = "com.tencent.sharp.TraeAudioManager.Parameters.DEVICECONFIG"; // int

	}

	// DeviceList
	public static final String DEVICE_NONE = "DEVICE_NONE";
	public static final String DEVICE_EARPHONE = "DEVICE_EARPHONE";
	public static final String DEVICE_SPEAKERPHONE = "DEVICE_SPEAKERPHONE";
	public static final String DEVICE_WIREDHEADSET = "DEVICE_WIREDHEADSET";
	public static final String DEVICE_BLUETOOTHHEADSET = "DEVICE_BLUETOOTHHEADSET";

	// DeviceStatus
	public static final int DEVICE_STATUS_ERROR = -1;
	public static final int DEVICE_STATUS_DISCONNECTED = 0;
	public static final int DEVICE_STATUS_CONNECTING = 1;
	public static final int DEVICE_STATUS_CONNECTED = 2;
	public static final int DEVICE_STATUS_DISCONNECTING = 3;
	public static final int DEVICE_STATUS_UNCHANGEABLE = 4;

	public static final int AUDIO_MANAGER_ACTIVE_NONE = 0;
	public static final int AUDIO_MANAGER_ACTIVE_VOICECALL = 1;
	public static final int AUDIO_MANAGER_ACTIVE_RING = 2;

	/************************************************************************************************************/

	AudioManager _am = null;
	Context _context = null;

	int _activeMode = AUDIO_MANAGER_ACTIVE_NONE;

	int _prevMode = AudioManager.MODE_NORMAL;
	int _streamType = AudioManager.STREAM_VOICE_CALL;
	int _modePolicy = -1;
	// String _prevDevice = "";

	// int _modePolicy = -1; // -1:auto 0:disable 1:enable
	// int _buletoothPolicy = -1; // -1:auto 0:disable 1:enable

	// boolean _marked = false;
	// int _prevMode = -1;
	// boolean _prevSpeakeron = false;
	// boolean _beforeHeadsetPluginSpeakeron = true;
	public static final String VOICECALL_CONFIG = DEVICE_SPEAKERPHONE + ";"
			+ DEVICE_EARPHONE + ";" + DEVICE_BLUETOOTHHEADSET + ";"
			+ DEVICE_WIREDHEADSET + ";";

	public static final String VIDEO_CONFIG = DEVICE_EARPHONE + ";"
			+ DEVICE_SPEAKERPHONE + ";" + DEVICE_BLUETOOTHHEADSET + ";"
			+ DEVICE_WIREDHEADSET + ";";

	static public boolean checkDevName(String strDeviceName) {
		if (strDeviceName == null)
			return false;
		if ((!DEVICE_SPEAKERPHONE.equals(strDeviceName))
				&& (!DEVICE_EARPHONE.equals(strDeviceName))
				&& (!DEVICE_WIREDHEADSET.equals(strDeviceName))
				&& (!DEVICE_BLUETOOTHHEADSET.equals(strDeviceName))) {
			return false;
		}
		return true;
	}

	static public boolean isHandfree(String strDeviceName) {
		if (!checkDevName(strDeviceName))
			return false;
		if (DEVICE_SPEAKERPHONE.equals(strDeviceName)) {
			return true;
		}
		return false;
	}

	class DeviceConfigManager {

		public class DeviceConfig {

			String deviceName = DEVICE_NONE;
			boolean visible = false;
			// int status = DEVICE_STATUS_DISCONNECTED;
			int priority = 0;

			public DeviceConfig() {
			}

			/*
			 * public DeviceConfig( String strDeviceName, boolean bVisible, int
			 * nStatus ) { setConfig( strDeviceName, bVisible, nStatus ); }
			 * 
			 * public DeviceConfig( String strConfig ) { setConfig( strConfig );
			 * }
			 */

			public boolean init(String strDeviceName, int nPriority) {

				if (null == strDeviceName || strDeviceName.length() <= 0) {
					return false;
				}

				if (checkDevName(strDeviceName) != true) {
					return false;
				}
				/*
				 * if ( DEVICE_STATUS_DISCONNECTED != nStatus &&
				 * DEVICE_STATUS_CONNECTING != nStatus &&
				 * DEVICE_STATUS_CONNECTED != nStatus &&
				 * DEVICE_STATUS_DISCONNECTING != nStatus ) { return false; }
				 */
				deviceName = strDeviceName;
				priority = nPriority;

				/*
				 * if ( DEVICE_WIREDHEADSET.equals(deviceName) ) { visible =
				 * _am.isWiredHeadsetOn(); } else if (
				 * DEVICE_BLUETOOTHHEADSET.equals(deviceName) ) { visible =
				 * _bluetoothdetectproxy.isAnyBluetoothHeadsetAvailabled() ||
				 * _am.isBluetoothA2dpOn() || _am.isBluetoothScoOn(); } else if
				 * ( DEVICE_SPEAKERPHONE.equals(deviceName) ) { visible = true;
				 * } else if ( DEVICE_LISTENPHONE.equals(deviceName) ) { visible
				 * = true; }
				 */

				return true;
			}

			public String getDeviceName() {
				return deviceName;
			}

			public boolean getVisible() {
				return visible;
			}

			// public int getStatus(){
			// return status;
			// }
			public int getPriority() {
				return priority;
			}

			public void setVisible(boolean arg) {
				visible = arg;
			}
			/*
			 * public boolean setStatus(int arg){ if (
			 * DEVICE_STATUS_DISCONNECTED != arg && DEVICE_STATUS_CONNECTING !=
			 * arg && DEVICE_STATUS_CONNECTED != arg &&
			 * DEVICE_STATUS_DISCONNECTING != arg ) { return false; } status =
			 * arg; return true; } 8/ public void setPriority(int arg){ priority
			 * = arg; } /* // strConfig: "deviceName|visible|status" // Use as:
			 * "DEVICE_SPEAKERPHONE|1|2", // "DEVICE_BLUETOOTHHEADSET|1|0"
			 * public boolean init( String strConfig ) {
			 * 
			 * if ( null == strConfig || strConfig.isEmpty() ) { return false; }
			 * 
			 * String[] configFields = strConfig.split("\\|");
			 * 
			 * if ( null == configFields || 3 > configFields.length ) { return
			 * false; }
			 * 
			 * try { // return init( configFields[0],
			 * TraeHelper.intToBoolean(Integer.parseInt(configFields[1])),
			 * Integer.parseInt(configFields[2]) ); return init(
			 * configFields[0], false,DEVICE_STATUS_DISCONNECTED); } catch (
			 * Exception e ) { return false; } }
			 */

		}

		// HashMap<String, Object> deviceList = new
		HashMap<String, DeviceConfig> deviceConfigs = new HashMap<String, DeviceConfig>();
		// Vector<String> devicePrioritys =new Vector();
		String prevConnectedDevice = DEVICE_NONE;
		String connectedDevice = DEVICE_NONE;
		String connectingDevice = DEVICE_NONE;

		// ArrayList<DeviceConfig> deviceConfigs = new
		// ArrayList<DeviceConfig>();
		ReentrantLock mLock = new ReentrantLock();

		public DeviceConfigManager() {
			// setConfigs(VIDEO_CONFIG);
		}

		// strConfig: "deviceName1;deviceName2;
		//
		// Use as: "DEVICE_WIREDHEADSET;DEVICE_BLUETOOTHHEADSET;"

		public boolean init(String strConfigs) {
			AudioDeviceInterface.LogTraceEntry(" strConfigs:" + strConfigs);
			if (null == strConfigs || strConfigs.length() <= 0) {
				return false;
			}

			strConfigs = strConfigs.replace("\n", "");
			strConfigs = strConfigs.replace("\r", "");

			if (null == strConfigs || strConfigs.length() <= 0) {
				return false;
			}

			if (0 > strConfigs.indexOf(";")) {
				strConfigs += ";";
			}

			String[] configs = strConfigs.split(";");

			if (null == configs || 1 > configs.length) {
				return false;
			}
			mLock.lock();
			for (int i = 0; i < configs.length; i++) {
				_addConfig(configs[i], i);
			}
			/*
			 * DeviceConfig _dc=null; Iterator iter =
			 * deviceConfigs.entrySet().iterator(); int nPriority; while
			 * (iter.hasNext()) {
			 * 
			 * HashMap.Entry entry = (HashMap.Entry) iter.next(); // Object key
			 * = entry.getKey(); // Object val = entry.getValue(); if(i==id){
			 * _dc = (DeviceConfig)entry.getValue(); break; } i++; }
			 */
			mLock.unlock();
			printDevices();
			return true;
		}

		boolean visiableUpdate = false;

		boolean _addConfig(String strDeviceName, int nPriority) {
			AudioDeviceInterface.LogTraceEntry(" devName:" + strDeviceName
					+ " priority:" + nPriority);
			DeviceConfig dc = new DeviceConfig();

			if (dc.init(strDeviceName, nPriority)) {
				// mLock.lock();
				if (deviceConfigs.containsKey(strDeviceName)) {

					// mLock.unlock();
					if (QLog.isColorLevel())
						QLog.e("TRAE", QLog.CLR, "err dev exist!");
					return false;
				}
				deviceConfigs.put(strDeviceName, dc);
				// devicePrioritys.add(nPriority, strDeviceName);
				// deviceConfigs.add(dc);
				visiableUpdate = true;
				// mLock.unlock();

				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, " n" + getDeviceNumber() + " 0:"
							+ getDeviceName(0));

				AudioDeviceInterface.LogTraceExit();
				return true;
			}
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR, " err dev init!");
			//
			return false;
		}

		public void clearConfig() {
			mLock.lock();
			deviceConfigs.clear();
			prevConnectedDevice = DEVICE_NONE;
			connectedDevice = DEVICE_NONE;
			connectingDevice = DEVICE_NONE;
			// devicePrioritys.clear();
			mLock.unlock();
		}

		public boolean getVisiableUpdateFlag() {
			boolean res = false;
			mLock.lock();
			res = visiableUpdate;
			mLock.unlock();
			return res;
		}

		public void resetVisiableUpdateFlag() {
			mLock.lock();
			visiableUpdate = false;
			mLock.unlock();
		}

		public boolean setVisible(String strDeviceName, boolean bVisible) {

			boolean bRet = false;

			mLock.lock();

			DeviceConfig dc = deviceConfigs.get(strDeviceName);
			if (dc != null) {
				if (dc.getVisible() != bVisible) {
					dc.setVisible(bVisible);
					visiableUpdate = true;
					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR, " ++setVisible:"
								+ strDeviceName + (bVisible ? " Y" : " N"));
					bRet = true;
				}
			}

			mLock.unlock();

			return bRet;
		}

		String _bluetoothDevName = "unknow";

		public void setBluetoothName(String btName) {
			if(btName==null){
				_bluetoothDevName = "unknow";
				return;
			}
			if(btName.isEmpty())
				_bluetoothDevName = "unknow";
			else
				_bluetoothDevName = btName;
		}

		public String getBluetoothName() {
			return _bluetoothDevName;
		}

		public boolean getVisible(String strDeviceName) {

			boolean bRet = false;

			mLock.lock();

			DeviceConfig dc = deviceConfigs.get(strDeviceName);

			if (null != dc) {
				bRet = dc.getVisible();
			}

			mLock.unlock();

			return bRet;
		}

		/*
		 * public boolean setStatus( String strDeviceName, int nStatus ) {
		 * boolean res = false; mLock.lock();
		 * 
		 * DeviceConfig dc = deviceConfigs.get(strDeviceName);
		 * 
		 * if ( null != dc ) { res = dc.setStatus(nStatus);
		 * 
		 * }
		 * 
		 * mLock.unlock();
		 * 
		 * return res; }
		 * 
		 * public int getStatus( String strDeviceName ) {
		 * 
		 * int nStatus = DEVICE_STATUS_ERROR;
		 * 
		 * mLock.lock();
		 * 
		 * DeviceConfig dc = deviceConfigs.get(strDeviceName);
		 * 
		 * if ( null != dc ) { nStatus = dc.getStatus(); }
		 * 
		 * mLock.unlock();
		 * 
		 * return nStatus; }
		 */

		public int getPriority(String strDeviceName) {

			int nRet = -1;

			mLock.lock();
			DeviceConfig dc = deviceConfigs.get(strDeviceName);
			if (null != dc) {
				nRet = dc.getPriority();
			}

			mLock.unlock();

			return nRet;
		}

		public int getDeviceNumber() {
			int n;
			mLock.lock();
			n = deviceConfigs.size();
			mLock.unlock();
			return n;
		}

		public String getDeviceName(int id) {
			String str = DEVICE_NONE;
			int i = 0;
			mLock.lock();
			DeviceConfig _dc = null;
			Iterator iter = deviceConfigs.entrySet().iterator();
			while (iter.hasNext()) {

				Map.Entry entry = (Map.Entry) iter.next();
				// Object key = entry.getKey();
				// Object val = entry.getValue();
				if (i == id) {
					_dc = (DeviceConfig) entry.getValue();
					break;
				}
				i++;
			}

			// DeviceConfig _dc = deviceConfigs.get(id);
			if (_dc != null) {
				// if(QLog.isColorLevel())
				// QLog.w("TRAE",QLog.CLR,"getDeviceName null i:"+id+" size:"+deviceConfigs.size());
				str = _dc.getDeviceName();
			}
			mLock.unlock();
			return str;
		}

		public String getAvailabledHighestPriorityDevice() {
			DeviceConfig dst = null;
			mLock.lock();

			// DeviceConfig dc = null;
			// int maxPrity = -1;

			DeviceConfig _dc = null;
			Iterator iter = deviceConfigs.entrySet().iterator();
			while (iter.hasNext()) {

				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();

				_dc = (DeviceConfig) entry.getValue();
				if (_dc == null)
					continue;
				if (_dc.getVisible() == false)
					continue;
				if (dst == null) {
					dst = _dc;
				} else {
					if (_dc.getPriority() >= dst.getPriority())
						dst = _dc;
				}
			}
			/*
			 * for ( int i = 0; i < deviceConfigs.size(); i++ ) { DeviceConfig
			 * _dc = deviceConfigs.get(i); if(_dc==null) continue;
			 * if(_dc.getVisible()==false) continue; if(dst==null){ dst = _dc;
			 * }else{ if(_dc.getPriority() >= dst.getPriority()) dst = _dc; } }
			 */
			mLock.unlock();
			return (dst != null ? dst.getDeviceName() : DEVICE_SPEAKERPHONE);
		}

		public String getConnectingDevice() {
			String str = DEVICE_NONE;
			mLock.lock();
			str = null;
			DeviceConfig dc = deviceConfigs.get(connectingDevice);
			if (null != dc) {
				if (dc.getVisible())
					str = connectingDevice;
			}
			mLock.unlock();
			return str;
		}

		public String getConnectedDevice() {
			String str = DEVICE_NONE;
			mLock.lock();
			str = _getConnectedDevice();
			mLock.unlock();
			return str;

		}

		public String getPrevConnectedDevice() {
			String str = DEVICE_NONE;
			mLock.lock();
			str = _getPrevConnectedDevice();
			mLock.unlock();
			return str;
		}

		public boolean setConnecting(String strDeviceName) {
			boolean res = false;
			mLock.lock();
			DeviceConfig dc = deviceConfigs.get(strDeviceName);
			if (dc != null) {
				if (dc.getVisible()) {
					connectingDevice = strDeviceName;
					res = true;
				}
			}
			mLock.unlock();

			return res;
		}

		/*
		 * public boolean isConnecting( String strDeviceName ) { boolean res =
		 * false; mLock.lock(); res = connectingDevice.equals(strDeviceName);
		 * mLock.unlock(); return res; }
		 */

		public boolean setConnected(String strDeviceName) {
			boolean res = false;
			mLock.lock();
			DeviceConfig dc = deviceConfigs.get(strDeviceName);
			if (dc != null) {
				if (dc.getVisible()) {
					if (connectedDevice != null) {
						if (!connectedDevice.equals(strDeviceName)) {
							prevConnectedDevice = connectedDevice;
						}
					}
					connectedDevice = strDeviceName;
					connectingDevice = "";
					res = true;
				}
			}
			mLock.unlock();

			return res;
		}

		/*
		 * public boolean setConnected( String strDeviceName, boolean bConnected
		 * ) { return setStatus( strDeviceName, bConnected ?
		 * DEVICE_STATUS_CONNECTED : DEVICE_STATUS_DISCONNECTED ); }
		 */

		public boolean isConnected(String strDeviceName) {
			boolean res = false;

			mLock.lock();
			DeviceConfig dc = deviceConfigs.get(strDeviceName);
			if (dc != null) {
				if (dc.getVisible()) {
					res = connectedDevice.equals(strDeviceName);
				}
			}
			mLock.unlock();
			return res;
		}

		/*
		 * public boolean setDisconnected( String strDeviceName ) { return
		 * setStatus( strDeviceName, DEVICE_STATUS_DISCONNECTED ); }
		 * 
		 * public boolean isDisconnected( String strDeviceName ) { return (
		 * DEVICE_STATUS_DISCONNECTED == getStatus(strDeviceName) ); }
		 */

		public HashMap<String, Object> getSnapParams() {
			HashMap<String, Object> params = new HashMap<String, Object>();
			mLock.lock();
			params.put(EXTRA_DATA_AVAILABLEDEVICE_LIST,
					_getAvailableDeviceList());
			params.put(EXTRA_DATA_CONNECTEDDEVICE, _getConnectedDevice());
			params.put(EXTRA_DATA_PREV_CONNECTEDDEVICE,
					_getPrevConnectedDevice());
			mLock.unlock();

			return params;
		}

		public ArrayList<String> getAvailableDeviceList() {

			ArrayList<String> list = new ArrayList<String>();

			mLock.lock();
			list = _getAvailableDeviceList();
			mLock.unlock();

			return list;
		}

		ArrayList<String> _getAvailableDeviceList() {

			ArrayList<String> list = new ArrayList<String>();

			DeviceConfig _dc = null;
			Iterator iter = deviceConfigs.entrySet().iterator();
			while (iter.hasNext()) {

				Map.Entry entry = (Map.Entry) iter.next();

				_dc = (DeviceConfig) entry.getValue();
				if (_dc == null)
					continue;
				if (_dc.getVisible() == false)
					continue;
				list.add(_dc.getDeviceName());
			}

			return list;
		}

		String _getConnectedDevice() {
			String str = DEVICE_NONE;
			DeviceConfig dc = deviceConfigs.get(connectedDevice);
			if (null != dc) {
				if (dc.getVisible())
					str = connectedDevice;
			}
			return str;
		}

		String _getPrevConnectedDevice() {
			String str = DEVICE_NONE;
			DeviceConfig dc = deviceConfigs.get(prevConnectedDevice);
			if (null != dc) {
				if (dc.getVisible())
					str = prevConnectedDevice;
			}
			return str;
		}
	}

	TraeAudioSessionHost _audioSessionHost = null;
	DeviceConfigManager _deviceConfigManager = null;
	BluetoohHeadsetCheckInterface _bluetoothCheck = null;

	String sessionConnectedDev = DEVICE_NONE;
	static ReentrantLock _glock = new ReentrantLock();
	static TraeAudioManager _ginstance = null;
	static int _gHostProcessId = -1;

	void printDevices() {
		AudioDeviceInterface.LogTraceEntry("");
		int n = _deviceConfigManager.getDeviceNumber();
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "   ConnectedDevice:"
					+ _deviceConfigManager.getConnectedDevice());
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "   ConnectingDevice:"
					+ _deviceConfigManager.getConnectingDevice());
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "   prevConnectedDevice:"
					+ _deviceConfigManager.getPrevConnectedDevice());
		if (QLog.isColorLevel())
			QLog.w("TRAE",
					QLog.CLR,
					"   AHPDevice:"
							+ _deviceConfigManager
									.getAvailabledHighestPriorityDevice());

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "   deviceNamber:" + n);

		for (int i = 0; i < n; i++) {
			String devName;

			devName = _deviceConfigManager.getDeviceName(i);
			if (QLog.isColorLevel())
				QLog.w("TRAE",
						QLog.CLR,
						"      " + i + " devName:" + devName + " Visible:"
								+ _deviceConfigManager.getVisible(devName)
								+ " Priority:"
								+ _deviceConfigManager.getPriority(devName)

				);

		}
		String[] alist = _deviceConfigManager.getAvailableDeviceList().toArray(
				new String[0]);
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "   AvailableNamber:" + alist.length);
		for (int i = 0; i < alist.length; i++) {
			String devName;

			devName = alist[i];
			if (QLog.isColorLevel())
				QLog.w("TRAE",
						QLog.CLR,
						"      " + i + " devName:" + devName + " Visible:"
								+ _deviceConfigManager.getVisible(devName)
								+ " Priority:"
								+ _deviceConfigManager.getPriority(devName)

				);
		}
		AudioDeviceInterface.LogTraceExit();
	}

	static boolean isCloseSystemAPM(int modePolicy) {
		if (modePolicy != -1)
			return false;
		if (Build.MANUFACTURER.equals("Xiaomi")){
			if (Build.MODEL.equals("MI 2"))
				return true;
			if (Build.MODEL.equals("MI 2A"))
				return true;
			if (Build.MODEL.equals("MI 2S"))
				return true;
			if (Build.MODEL.equals("MI 2SC"))
				return true;
		}else if (Build.MANUFACTURER.equals("samsung")){
			if (Build.MODEL.equals("SCH-I959"))
				return true;
		}
		
		return false;
	}
	
	public static boolean IsEabiLowVersionByAbi(String platform)
    {
        if (platform == null)
        {
            return true;
        }
        if (platform.contains("x86"))
        {
            return false;
        }
        else if (platform.contains("mips"))
        {
            return false;
        }
        else if (platform.equalsIgnoreCase("armeabi"))
        {
            return true;
        }
        else if (platform.equalsIgnoreCase("armeabi-v7a"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
	
	static boolean IsEabiLowVersion(){
		
		String CPU_ABI = android.os.Build.CPU_ABI;  
        String CPU_ABI2 = "unknown";  
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) { // CPU_ABI2  
            // since 2.2 
            try {  
                CPU_ABI2 = (String) android.os.Build.class.getDeclaredField(  
                        "CPU_ABI2").get(null);  
            } catch (Exception e) {  
            	if(IsEabiLowVersionByAbi(CPU_ABI)){
            		return true;
            	}
            	return false;  
            }  
        }  
        if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR,
					"IsEabiVersion CPU_ABI:"+CPU_ABI+ " CPU_ABI2:"+CPU_ABI2);
  
        if (IsEabiLowVersionByAbi(CPU_ABI) && IsEabiLowVersionByAbi(CPU_ABI2)){  
            return true;  
        }
        return false;
	}
    
	static int getAudioSource(int audioSourcePolicy) {
		int source = MediaRecorder.AudioSource.DEFAULT;	
		if(IsEabiLowVersion()){
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"[Config] armeabi low Version, getAudioSource _audioSourcePolicy:"
								+ audioSourcePolicy + " source:" + source);
			return source;
		}
		
		int apiLevel = android.os.Build.VERSION.SDK_INT;
		if (audioSourcePolicy >= 0) {
			source = audioSourcePolicy;
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"[Config] getAudioSource _audioSourcePolicy:"
								+ audioSourcePolicy + " source:" + source);
			return audioSourcePolicy;
		}

		if (apiLevel >= 11)
			source = MediaRecorder.AudioSource.VOICE_COMMUNICATION;

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR,
					"[Config] getAudioSource _audioSourcePolicy:"
							+ audioSourcePolicy + " source:" + source);
		return source;
	}

	static int getAudioStreamType(int audioStreamTypePolicy) {
		int streamType = AudioManager.STREAM_MUSIC;
		if(IsEabiLowVersion()){
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"[Config] armeabi low Version, getAudioStreamType audioStreamTypePolicy:"
								+ audioStreamTypePolicy + " streamType:"
								+ streamType);
			return streamType;
		}
		
		int apiLevel = android.os.Build.VERSION.SDK_INT;
		if (audioStreamTypePolicy >= 0) {
			streamType = audioStreamTypePolicy;
		} else if (apiLevel >= 9) {
			streamType = AudioManager.STREAM_VOICE_CALL;
		}

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR,
					"[Config] getAudioStreamType audioStreamTypePolicy:"
							+ audioStreamTypePolicy + " streamType:"
							+ streamType);
		return streamType;
	}

	static int getCallAudioMode(int modePolicy) {
		int mode = AudioManager.MODE_NORMAL;	
		if(IsEabiLowVersion()){
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"[Config] armeabi low Version, getCallAudioMode modePolicy:" + modePolicy
								+ " mode:" + mode);
			return mode;
		}
		
		int apiLevel = android.os.Build.VERSION.SDK_INT;
		if (modePolicy >= 0) {
			mode = modePolicy;
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"[Config] getCallAudioMode modePolicy:" + modePolicy
								+ " mode:" + mode);
			return mode;
		}

		if (apiLevel >= 11) {
			mode = AudioManager.MODE_IN_COMMUNICATION;
		}

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "[Config] getCallAudioMode _modePolicy:"
					+ modePolicy + " mode:" + mode + "facturer:"
					+ Build.MANUFACTURER + " model:" + Build.MODEL);

		return mode;
	}

	void updateDeviceStatus() {
		// boolean hasUpdate = false;
		boolean setSuccess;
		int n;
		n = _deviceConfigManager.getDeviceNumber();
		// if(QLog.isColorLevel())
		// QLog.w("TRAE",QLog.CLR,"pollUpdateDevice n:"+n);
		// printDevices();
		for (int i = 0; i < n; i++) {
			String devName;

			setSuccess = false;
			devName = _deviceConfigManager.getDeviceName(i);
			if (devName != null) {

				if (devName.equals(DEVICE_BLUETOOTHHEADSET)) {
					if (_bluetoothCheck == null)
						setSuccess = _deviceConfigManager.setVisible(devName,
								false);
					else
						setSuccess = _deviceConfigManager.setVisible(devName,
								_bluetoothCheck.isConnected());// &&
																// _bluetoothCheck.isVisiable()
				} else if (devName.equals(DEVICE_WIREDHEADSET)) {
					setSuccess = _deviceConfigManager.setVisible(devName,
							_am.isWiredHeadsetOn());
				} else if (devName.equals(DEVICE_SPEAKERPHONE)) {
					_deviceConfigManager.setVisible(devName, true);
				}

				// _updateEarphoneVisable();
			}

			if (setSuccess == true) {
				// hasUpdate = true;
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR,
							"pollUpdateDevice dev:" + devName + " Visible:"
									+ _deviceConfigManager.getVisible(devName));
			}
		}

		checkAutoDeviceListUpdate();
	}

	void _updateEarphoneVisable() {

		if (_deviceConfigManager.getVisible(DEVICE_WIREDHEADSET)
		// || _deviceConfigManager.getVisible(DEVICE_BLUETOOTHHEADSET)
		) {
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						" detected headset plugin,so disable earphone");

			_deviceConfigManager.setVisible(DEVICE_EARPHONE, false);
		} else {
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						" detected headset plugout,so enable earphone");

			_deviceConfigManager.setVisible(DEVICE_EARPHONE, true);
		}
	}

	void checkAutoDeviceListUpdate() {
		if (_deviceConfigManager.getVisiableUpdateFlag() == true) {
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"checkAutoDeviceListUpdate got update!");

			_updateEarphoneVisable();

			_deviceConfigManager.resetVisiableUpdateFlag();

			HashMap<String, Object> params = new HashMap<String, Object>();
			internalSendMessage(
					TraeAudioManagerLooper.MESSAGE_AUTO_DEVICELIST_UPDATE,
					params);
		}

	}

	void checkDevicePlug(String devName, boolean isPlugin) {
		if (_deviceConfigManager.getVisiableUpdateFlag() == true) {
			if (QLog.isColorLevel())
				QLog.w("TRAE",
						QLog.CLR,
						"checkDevicePlug got update dev:" + devName
								+ (isPlugin ? " piugin" : " plugout")
								+ " connectedDev:"
								+ _deviceConfigManager.getConnectedDevice());

			_updateEarphoneVisable();

			_deviceConfigManager.resetVisiableUpdateFlag();

			if (isPlugin) {
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put(PARAM_DEVICE, devName);
				internalSendMessage(
						TraeAudioManagerLooper.MESSAGE_AUTO_DEVICELIST_PLUGIN_UPDATE,
						params);
			} else {
				String connectedDev;
				connectedDev = _deviceConfigManager.getConnectedDevice();
				if (connectedDev.equals(devName)
						|| connectedDev.equals(DEVICE_NONE)) {

					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put(PARAM_DEVICE, devName);
					internalSendMessage(
							TraeAudioManagerLooper.MESSAGE_AUTO_DEVICELIST_PLUGOUT_UPDATE,
							params);
				} else {
					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR, " ---No switch,plugout:"
								+ devName + " connectedDev:" + connectedDev);
					HashMap<String, Object> params = new HashMap<String, Object>();
					internalSendMessage(
							TraeAudioManagerLooper.MESSAGE_AUTO_DEVICELIST_UPDATE,
							params);
				}
			}
		}

	}

	class TraeAudioManagerLooper extends Thread {

		public static final int MESSAGE_BEGIN = 0x8000;

		/*
		 * public static final int MESSAGE_SETWIREDHEADSET = MESSAGE_BEGIN + 1;
		 * public static final int MESSAGE_SETBLUETOOTHHEADSET = MESSAGE_BEGIN +
		 * 2; public static final int MESSAGE_SETMODE = MESSAGE_BEGIN + 3;
		 * public static final int MESSAGE_SETSPEAKER = MESSAGE_BEGIN + 4;
		 */
		// public static final int MESSAGE_SETDEVICECONFIG = MESSAGE_BEGIN + 3;
		public static final int MESSAGE_ENABLE = MESSAGE_BEGIN + 4;
		public static final int MESSAGE_DISABLE = MESSAGE_BEGIN + 5;
		public static final int MESSAGE_GETDEVICELIST = MESSAGE_BEGIN + 6;
		public static final int MESSAGE_CONNECTDEVICE = MESSAGE_BEGIN + 7;
		public static final int MESSAGE_EARACTION = MESSAGE_BEGIN + 8;
		public static final int MESSAGE_ISDEVICECHANGABLED = MESSAGE_BEGIN + 9;
		public static final int MESSAGE_GETCONNECTEDDEVICE = MESSAGE_BEGIN + 10;
		public static final int MESSAGE_GETCONNECTINGDEVICE = MESSAGE_BEGIN + 11;
		public static final int MESSAGE_VOICECALLPREPROCESS = MESSAGE_BEGIN + 12;
		public static final int MESSAGE_VOICECALLPOSTPROCESS = MESSAGE_BEGIN + 13;
		public static final int MESSAGE_STARTRING = MESSAGE_BEGIN + 14;
		public static final int MESSAGE_STOPRING = MESSAGE_BEGIN + 15;

		public static final int MESSAGE_GETSTREAMTYPE = MESSAGE_BEGIN + 16;
		
		// public static final int MESSAGE_ONUPDATEDEVICELIST = MESSAGE_BEGIN +
		// 13;

		// public static final int MESSAGE_NOTIFY_DEVICELIST_UPDATE =
		// MESSAGE_BEGIN + 13;
		// public static final int MESSAGE_NOTIFY_DEVICECONNECTED_UPDATE =
		// MESSAGE_BEGIN + 14;
		// public static final int MESSAGE_NOTIFY_DEVICECHANGABLE_UPDATE =
		// MESSAGE_BEGIN + 15;

		// public static final int MESSAGE_SWITCHDEVICE = MESSAGE_BEGIN + 16;
		// public static final int MESSAGE_AUTO_CONNECT = MESSAGE_BEGIN + 17;
		public static final int MESSAGE_AUTO_DEVICELIST_UPDATE = MESSAGE_BEGIN + 17;
		public static final int MESSAGE_AUTO_DEVICELIST_PLUGIN_UPDATE = MESSAGE_BEGIN + 18;
		public static final int MESSAGE_AUTO_DEVICELIST_PLUGOUT_UPDATE = MESSAGE_BEGIN + 19;
		
		public static final int MESSAGE_VOICECALL_AUIDOPARAM_CHANGED = MESSAGE_BEGIN + 20;

		public static final int MESSAGE_CONNECT_HIGHEST_PRIORITY_DEVICE = MESSAGE_BEGIN + 21;
		public static final int MESSAGE_REQUEST_RELEASE_AUDIO_FOCUS = MESSAGE_BEGIN + 22;
		public static final int MESSAGE_RECOVER_AUDIO_FOCUS = MESSAGE_BEGIN + 23;

		Handler mMsgHandler = null;
		TraeMediaPlayer _ringPlayer = null;
		long _ringSessionID = -1;
		String _ringOperation = "";
		String _ringUserdata = "";
		final boolean[] _started = new boolean[] { false };
		boolean _enabled = false;
		TraeAudioManager _parent = null;

		public TraeAudioManagerLooper(TraeAudioManager parent) {
			_parent = parent;
			long lasttime = SystemClock.elapsedRealtime();
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR, "TraeAudioManagerLooper start...");

			start();
			synchronized (_started) {
				if (_started[0] == false) {
					try {
						_started.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR,
						"  start used:"
								+ (SystemClock.elapsedRealtime() - lasttime)
								+ "ms");
		}

		public void quit() {
			AudioDeviceInterface.LogTraceEntry("");
			if (null == mMsgHandler) {
				return;
			}
			long lasttime = SystemClock.elapsedRealtime();
			mMsgHandler.getLooper().quit();

			synchronized (_started) {
				if (_started[0] == true) {
					try {
						_started.wait(10000);
					} catch (InterruptedException e) {
					}
				}
			}
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR,
						"  quit used:"
								+ (SystemClock.elapsedRealtime() - lasttime)
								+ "ms");
			mMsgHandler = null;
			AudioDeviceInterface.LogTraceExit();
		}

		public int sendMessage(int nMsg, HashMap<String, Object> params) {

			if (null == mMsgHandler) {
				AudioDeviceInterface
						.LogTraceEntry(" fail mMsgHandler==null _enabled:"
								+ (_enabled ? "Y" : "N") + " activeMode:"
								+ _activeMode + " msg:" + nMsg);
				return -1;
			}
			/*
			 * if (!_enabled){
			 * AudioDeviceInterface.LogTraceEntry(" fail _enabled:" + (_enabled
			 * ? "Y" : "N") + " activeMode:" + _activeMode+" msg:"+nMsg); return
			 * -1; }
			 */
			Message msg = Message.obtain(mMsgHandler, nMsg, params);

			return mMsgHandler.sendMessage(msg) ? 0 : -1;
		}

		String _lastCfg = "";
		int _preServiceMode = AudioManager.MODE_NORMAL;
		int _preRingMode = AudioManager.MODE_NORMAL;

		// int _prevCallVolume;
		// int _prevCallVolumeSet;
	//	int _prevMusicVolume;
	//	int _prevMusicVolumeSet;

		/*
		 * void ___setSpeakerTest(String log){ if(_context==null) return;
		 * AudioManager am =
		 * (AudioManager)_context.getSystemService(Context.AUDIO_SERVICE);
		 * 
		 * if(QLog.isColorLevel())
		 * QLog.w("TRAE",QLog.CLR,"___setSpeakerTest:"+log
		 * +" setSpeakerphoneOn(false)"); am.setSpeakerphoneOn(false);
		 * if(QLog.isColorLevel())
		 * QLog.w("TRAE",QLog.CLR,"mode:"+am.getMode()+" speaker:"
		 * +(am.isSpeakerphoneOn()?"Y":"N")); if(QLog.isColorLevel())
		 * QLog.w("TRAE"
		 * ,QLog.CLR,"___setSpeakerTest:"+log+(am.isSpeakerphoneOn()
		 * ==false?"success":"fail"));
		 * 
		 * if(QLog.isColorLevel())
		 * QLog.w("TRAE",QLog.CLR,"setSpeakerphoneOn(true)");
		 * am.setSpeakerphoneOn(true); if(QLog.isColorLevel())
		 * QLog.w("TRAE",QLog
		 * .CLR,"mode:"+am.getMode()+" speaker:"+(am.isSpeakerphoneOn
		 * ()?"Y":"N")); if(QLog.isColorLevel())
		 * QLog.w("TRAE",QLog.CLR,"___setSpeakerTest:"
		 * +log+(am.isSpeakerphoneOn()==true?"success":"fail")); }
		 */
		void startService(HashMap<String, Object> params) {

			String strCfg = (String) params.get(EXTRA_DATA_DEVICECONFIG);
			AudioDeviceInterface.LogTraceEntry(" _enabled:"
					+ (_enabled ? "Y" : "N") + " activeMode:" + _activeMode
					+ " cfg:" + strCfg);
			if (strCfg == null || _context == null)
				return;

			// ___setSpeakerTest("1");

			// if(strCfg.isEmpty())
			if (strCfg.length() <= 0)
				return;
			if (_enabled && _lastCfg.equals(strCfg)|| _activeMode != AUDIO_MANAGER_ACTIVE_NONE)
				return;

			if (_enabled) {
				stopService();
			}

			_prev_startService();
			// on purpose,avoid the speaker can not be opened on some hongmi
			AudioManager am = (AudioManager) _context
					.getSystemService(Context.AUDIO_SERVICE);
			// am.setSpeakerphoneOn(false);
			// am.setSpeakerphoneOn(true);
			// ___setSpeakerTest("2");

			_deviceConfigManager.clearConfig();
			_deviceConfigManager.init(strCfg);
			_lastCfg = strCfg;

			// _am = null;//for nexus 5,
			// _am = (AudioManager)
			// _context.getSystemService(Context.AUDIO_SERVICE);
			if (_am != null) {
				_preServiceMode = _am.getMode();
				try {
					// ___setSpeakerTest("3");
					/*
					_prevMusicVolumeSet = -1;
					// _prevCallVolumeSet = -1;

					_prevMusicVolume = _am
							.getStreamVolume(AudioManager.STREAM_MUSIC);
					int maxMusic = _am
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

					if (_prevMusicVolume == 0) {
						_prevMusicVolumeSet = (int) (maxMusic * 0.7);
						_am.setStreamVolume(AudioManager.STREAM_MUSIC,
								_prevMusicVolumeSet, 0);
					}
					*/
					/*
					 * _prevCallVolume =
					 * _am.getStreamVolume(AudioManager.STREAM_VOICE_CALL); int
					 * maxCall =
					 * _am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
					 * 
					 * double delcall; delcall = _prevCallVolume/maxCall;
					 * if(delcall<0.3){ _prevCallVolume=0; }
					 * 
					 * if(_prevCallVolume==0){ _prevCallVolumeSet =
					 * (int)(maxCall*0.7);
					 * _am.setStreamVolume(AudioManager.STREAM_VOICE_CALL
					 * ,_prevCallVolumeSet, 0); }
					 * 
					 * if(QLog.isColorLevel())
					 * QLog.w("TRAE",QLog.CLR,"Volume mark  max:"
					 * +maxCall+" _prevCallVolume:"
					 * +_prevCallVolume+" _prevCallVolumeSet:"
					 * +_prevCallVolumeSet);
					 */
					/*
					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR, "Volume mark  max:" + maxMusic
								+ " _prevMusicVolume:" + _prevMusicVolume
								+ " _prevMusicVolumeSet:" + _prevMusicVolumeSet);
*/
				} catch (Exception e) {
				}

			}

			_enabled = true;
			if (_ringPlayer == null)
				_ringPlayer = new TraeMediaPlayer(_context,
						new TraeMediaPlayer.OnCompletionListener() {

							@Override
							public void onCompletion() {
								if (QLog.isColorLevel())
									QLog.w("TRAE", QLog.CLR,
											"_ringPlayer onCompletion _activeMode:"
													+ _activeMode
													+ " _preRingMode:"
													+ _preRingMode);
								HashMap<String, Object> params = new HashMap<String, Object>();

								// params.put(PARAM_SESSIONID, nSessionId);
								params.put(PARAM_ISHOSTSIDE, true);

								sendMessage(
										TraeAudioManagerLooper.MESSAGE_STOPRING,
										params);
								// TODO Auto-generated method stub
								notifyRingCompletion();
								/*
								 * if(_am!=null && _activeMode ==
								 * AUDIO_MANAGER_ACTIVE_RING){
								 * _am.setMode(_preRingMode);
								 * abandonAudioFocus(); //if(_activeMode ==
								 * AUDIO_MANAGER_ACTIVE_RING) // _activeMode =
								 * AUDIO_MANAGER_ACTIVE_NONE; }
								 */
							}
						});

			notifyServiceState(_enabled);

			updateDeviceStatus();
			// InternalConnectDevice(_deviceConfigManager.getAvailabledHighestPriorityDevice(),null);
			AudioDeviceInterface.LogTraceExit();
		}

		/*
		 * void setMode(int audioMode){ if(_am==null){ if(QLog.isColorLevel())
		 * QLog.w("TRAE",QLog.CLR,"setMode:"+ audioMode +" fail am=null");
		 * return; }
		 * 
		 * // if(isTelphoneRinging()==false) _am.setMode(audioMode);
		 * 
		 * if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"setMode:"+ audioMode
		 * +(_am.getMode()!=audioMode?"fail":"success")); }
		 */

		void stopService() {
			AudioDeviceInterface.LogTraceEntry(" _enabled:"
					+ (_enabled ? "Y" : "N") + " activeMode:" + _activeMode);
			if (!_enabled)
				return;
			if (_activeMode == AUDIO_MANAGER_ACTIVE_VOICECALL) {
				interruptVoicecallPostprocess();
			} else if (_activeMode == AUDIO_MANAGER_ACTIVE_RING) {
				interruptRing();
			}
			if (_switchThread != null) {
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR,
							"_switchThread:" + _switchThread.getDeviceName());
				_switchThread.quit();
				_switchThread = null;
			}
			if (_ringPlayer != null)
				_ringPlayer.stopRing();
			_ringPlayer = null;
			_enabled = false;
			notifyServiceState(_enabled);

			// if(_am!=null && _context!=null && !isTelphoneRinging()){
			if (_am != null && _context != null) {
				try {

					InternalSetMode(_preServiceMode);

					// int callVolume =
					// _am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
					/*
					int musicVolume = _am
							.getStreamVolume(AudioManager.STREAM_MUSIC);
					*/
					/*
					 * if(_prevCallVolumeSet!=-1){
					 * _am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
					 * _prevCallVolume, 0); if(QLog.isColorLevel())
					 * QLog.w("TRAE"
					 * ,QLog.CLR,"Volume recove callVolume:"+callVolume
					 * +" -->"+_prevCallVolume); }
					 */
/*
					if (_prevMusicVolumeSet != -1) {
						_am.setStreamVolume(AudioManager.STREAM_MUSIC,
								_prevMusicVolume, 0);
						if (QLog.isColorLevel())
							QLog.w("TRAE", QLog.CLR,
									"Volume recove musicVolume:" + musicVolume
											+ " -->" + _prevMusicVolume);
					}
					*/

				} catch (Exception e) {
				}
			}

			_post_stopService();
			AudioDeviceInterface.LogTraceExit();
		}

		int notifyServiceState(boolean on) {

			if (null == _context) {

				return -1;
			}
			// boolean bChangabled =
			// _deviceConfigManager.getConnectingDeviceList().size() > 0;
			Intent intent = new Intent();
			intent.setAction(ACTION_TRAEAUDIOMANAGER_NOTIFY);
			intent.putExtra(PARAM_OPERATION, NOTIFY_SERVICE_STATE);
			intent.putExtra(NOTIFY_SERVICE_STATE_DATE, on);
			_context.sendBroadcast(intent);

			return 0;
		}

		public void run() {
			AudioDeviceInterface.LogTraceEntry("");

			Looper.prepare();

			mMsgHandler = new Handler() {
				/*
				 * msg from session|system|
				 */
				public void handleMessage(Message msg) {
					String highestDev = null;
					String connectedDev = null;
					String plugDev = null;
					// _looperlock.lock();

					HashMap<String, Object> params = null;

					try {
						params = (HashMap<String, Object>) msg.obj;
					} catch (Exception e) {
					}
					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR, "TraeAudioManagerLooper msg:"
								+ msg.what + " _enabled:"
								+ (_enabled ? "Y" : "N"));
					/*
					 * if(msg.what==MESSAGE_SETDEVICECONFIG){
					 * InternalSetDeviceConfig(params); updateDeviceStatus();
					 * //broadcast }else
					 */if (msg.what == MESSAGE_ENABLE) {
						startService(params);
						// broadcast
					} else {
						if (!_enabled) {
							if (QLog.isColorLevel())
								QLog.w("TRAE", QLog.CLR,
										"******* disabled ,skip msg******");
							Intent intent = new Intent();
							sendResBroadcast(intent, params,
									RES_ERRCODE_SERVICE_OFF);

						} else {
							switch (msg.what) {
							case MESSAGE_DISABLE:
								stopService();
								break;
							case MESSAGE_GETDEVICELIST: {
								InternalSessionGetDeviceList(params);
								break;
							}

							/*
							 * case MESSAGE_DISCONNECTDEVICE: {
							 * InternalDisconnectDevice(params); break; }
							 * 
							 * 
							 * case MESSAGE_ISDEVICECONNECTED: {
							 * InternalIsDeviceConnected(params); break; }
							 */
							case MESSAGE_ISDEVICECHANGABLED: {
								InternalSessionIsDeviceChangabled(params);
								break;
							}

							case MESSAGE_GETCONNECTEDDEVICE: {
								InternalSessionGetConnectedDevice(params);
								break;
							}

							case MESSAGE_GETCONNECTINGDEVICE: {
								InternalSessionGetConnectingDevice(params);
								break;
							}

							case MESSAGE_VOICECALLPREPROCESS: {
								InternalVoicecallPreprocess(params);
								break;
							}
							case MESSAGE_VOICECALLPOSTPROCESS: {
								InternalVoicecallPostprocess(params);
								break;
							}
							case MESSAGE_VOICECALL_AUIDOPARAM_CHANGED:{
								
								Integer st = (Integer) params.get(PARAM_STREAMTYPE);

								if (st == null) {
									if (QLog.isColorLevel())
										QLog.e("TRAE", QLog.CLR,
												" MESSAGE_VOICECALL_AUIDOPARAM_CHANGED params.get(PARAM_STREAMTYPE)==null!!");
									break;
								}
								_streamType = st;
								InternalNotifyStreamTypeUpdate(st);
								break;
							}
							case MESSAGE_STARTRING: {
								InternalStartRing(params);
								break;
							}
							case MESSAGE_STOPRING: {
								InternalStopRing(params);
								break;
							}
							case MESSAGE_REQUEST_RELEASE_AUDIO_FOCUS: {
								abandonAudioFocus();
								break;
							}
							case MESSAGE_RECOVER_AUDIO_FOCUS: {
								requestAudioFocus(_streamType);
								break;
							}
							case MESSAGE_GETSTREAMTYPE: {
								InternalGetStreamType(params);
								break;
							}
							
							//
							/*
							 * case MESSAGE_NOTIFY_DEVICELIST_UPDATE: {
							 * InternalNotifyDeviceListUpdate(); break; }
							 * 
							 * case MESSAGE_NOTIFY_DEVICECONNECTED_UPDATE: {
							 * InternalNotifyDeviceConnectedUpdate(); break; }
							 * 
							 * 
							 * case MESSAGE_NOTIFY_DEVICECHANGABLE_UPDATE: {
							 * InternalNotifyDeviceChangableUpdate(); break; }
							 * 
							 * case MESSAGE_SWITCHDEVICE: {
							 * InternalSwitchDevice(params); break; }
							 */
							case MESSAGE_CONNECTDEVICE: {
								InternalSessionConnectDevice(params);
								break;
							}
							case MESSAGE_EARACTION: {
								InternalSessionEarAction(params);
								break;
							}
							
							case MESSAGE_CONNECT_HIGHEST_PRIORITY_DEVICE:
							case MESSAGE_AUTO_DEVICELIST_UPDATE:

								highestDev = _deviceConfigManager
										.getAvailabledHighestPriorityDevice();
								connectedDev = _deviceConfigManager
										.getConnectedDevice();

								if (QLog.isColorLevel())
									QLog.w("TRAE", QLog.CLR,
											"MESSAGE_AUTO_DEVICELIST_UPDATE  connectedDev:"
													+ connectedDev
													+ " highestDev"
													+ highestDev);
								// InternalNotifyDeviceListUpdate();
								if (!highestDev.equals(connectedDev))
									InternalConnectDevice(highestDev, null);
								else
									InternalNotifyDeviceListUpdate();
								break;
							case MESSAGE_AUTO_DEVICELIST_PLUGIN_UPDATE:

								// highestDev =
								// _deviceConfigManager.getAvailabledHighestPriorityDevice();
								plugDev = (String) params.get(PARAM_DEVICE);
								// connectedDev =
								// _deviceConfigManager.getConnectedDevice();
								if (InternalConnectDevice(plugDev, null) != 0) {
									if (QLog.isColorLevel())
										QLog.w("TRAE",
												QLog.CLR,
												" plugin dev:"
														+ plugDev
														+ " sessionConnectedDev:"
														+ sessionConnectedDev
														+ " connected fail,auto switch!");
									InternalConnectDevice(
											_deviceConfigManager
													.getAvailabledHighestPriorityDevice(),
											null);
								}
								break;
							case MESSAGE_AUTO_DEVICELIST_PLUGOUT_UPDATE:

								// highestDev =
								// _deviceConfigManager.getAvailabledHighestPriorityDevice();
								// plugDev = (String) params.get(PARAM_DEVICE);
								// connectedDev =
								// _deviceConfigManager.getConnectedDevice();
								if (InternalConnectDevice(sessionConnectedDev,
										null) != 0) {
									plugDev = (String) params.get(PARAM_DEVICE);
									if (QLog.isColorLevel())
										QLog.w("TRAE",
												QLog.CLR,
												" plugout dev:"
														+ plugDev
														+ " sessionConnectedDev:"
														+ sessionConnectedDev
														+ " connected fail,auto switch!");
									InternalConnectDevice(
											_deviceConfigManager
													.getAvailabledHighestPriorityDevice(),
											null);
								}
								break;

							default: {
								break;
							}

							}
						}
					}

					// _looperlock.unlock();
				}

			};

			// startService();
			// _init();
			_init();
			synchronized (_started) {
				_started[0] = true;
				_started.notify();
			}

			Looper.loop();
			
			_uninit();

			synchronized (_started) {
				_started[0] = false;
				_started.notify();
			}
			AudioDeviceInterface.LogTraceExit();
		}

		void _init() {
			AudioDeviceInterface.LogTraceEntry("");
			try {

				_audioSessionHost = new TraeAudioSessionHost();
				_deviceConfigManager = new DeviceConfigManager();
				_gHostProcessId = android.os.Process.myPid();
				_am = (AudioManager) _context
						.getSystemService(Context.AUDIO_SERVICE);

				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======1");
				_bluetoothCheck = CreateBluetoothCheck(_context,
						_deviceConfigManager);
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======2");
				IntentFilter filter = new IntentFilter();
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======3");
				filter.addAction(Intent.ACTION_HEADSET_PLUG);
				filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======4");
				_bluetoothCheck.addAction(filter);
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======5");
				filter.addAction(ACTION_TRAEAUDIOMANAGER_REQUEST);
				_context.registerReceiver(_parent, filter);
				// if (_context.registerReceiver(_parent, filter) == null) {
				// if(QLog.isColorLevel())
				// QLog.w("TRAE",QLog.CLR,"AddRegisterRef:: context.registerReceiver return null");
				// _uninit();
				// return -1;
				// }
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======6");
			} catch (Exception e) {
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "======7");
			}

			AudioDeviceInterface.LogTraceExit();
		}

		void _prev_startService() {
			try {
				_am = (AudioManager) _context
						.getSystemService(Context.AUDIO_SERVICE);

				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======1");
				if (_bluetoothCheck == null)
					_bluetoothCheck = CreateBluetoothCheck(_context,
							_deviceConfigManager);
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======2");
				_context.unregisterReceiver(_parent);
				IntentFilter filter = new IntentFilter();
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======3");
				filter.addAction(Intent.ACTION_HEADSET_PLUG);
				filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======4");
				_bluetoothCheck.addAction(filter);
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======5");
				filter.addAction(ACTION_TRAEAUDIOMANAGER_REQUEST);
				_context.registerReceiver(_parent, filter);
				// if (_context.registerReceiver(_parent, filter) == null) {
				// if(QLog.isColorLevel())
				// QLog.w("TRAE",QLog.CLR,"AddRegisterRef:: context.registerReceiver return null");
				// _uninit();
				// return -1;
				// }
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======6");
			} catch (Exception e) {
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"======7");
			}

		}

		void _post_stopService() {
			try {
				if (_bluetoothCheck != null)
					_bluetoothCheck.release();
				_bluetoothCheck = null;
				if (_context != null) {
					_context.unregisterReceiver(_parent);
					IntentFilter filter = new IntentFilter();
					filter.addAction(ACTION_TRAEAUDIOMANAGER_REQUEST);
					_context.registerReceiver(_parent, filter);
				}

			} catch (Exception e) {

			}
			// _deviceConfigManager.clearConfig();
			// _deviceConfigManager = null;
		}

		void _uninit() {
			AudioDeviceInterface.LogTraceEntry("");
			try {
				stopService();

				if (_bluetoothCheck != null)
					_bluetoothCheck.release();
				_bluetoothCheck = null;
				if (null != _context) {
					_context.unregisterReceiver(_parent);
					_context = null;
				}
				if (_deviceConfigManager != null)
					_deviceConfigManager.clearConfig();
				_deviceConfigManager = null;
			} catch (Exception e) {

			}
			AudioDeviceInterface.LogTraceExit();
		}

		int InternalSessionGetDeviceList(HashMap<String, Object> params) {
			Intent intent = new Intent();

			HashMap<String, Object> resParams = _deviceConfigManager
					.getSnapParams();
			ArrayList<String> list = (ArrayList<String>) resParams
					.get(EXTRA_DATA_AVAILABLEDEVICE_LIST);
			String con = (String) resParams.get(EXTRA_DATA_CONNECTEDDEVICE);
			String prevCon = (String) resParams
					.get(EXTRA_DATA_PREV_CONNECTEDDEVICE);

			intent.putExtra(EXTRA_DATA_AVAILABLEDEVICE_LIST,
					list.toArray(new String[0]));
			intent.putExtra(EXTRA_DATA_CONNECTEDDEVICE, con);
			intent.putExtra(EXTRA_DATA_PREV_CONNECTEDDEVICE, prevCon);
			intent.putExtra(EXTRA_DATA_IF_HAS_BLUETOOTH_THIS_IS_NAME,
					_deviceConfigManager.getBluetoothName());

			//
			sendResBroadcast(intent, params, RES_ERRCODE_NONE);

			return 0;
		}

		/*
		 * boolean isTelphoneRinging(){ boolean bring = false; try{
		 * TelephonyManager tm = (TelephonyManager)
		 * _context.getSystemService(Service.TELEPHONY_SERVICE);
		 * 
		 * String strState=""; switch(tm.getCallState()){ case
		 * TelephonyManager.CALL_STATE_IDLE: strState = "IDLE"; break; case
		 * TelephonyManager.CALL_STATE_RINGING: strState = "RING"; bring = true;
		 * break; case TelephonyManager.CALL_STATE_OFFHOOK: strState =
		 * "OFFHOOK"; break;
		 * 
		 * } if(QLog.isColorLevel())
		 * QLog.w("TRAE",QLog.CLR,"Call:"+tm.getCallState
		 * ()+" :"+strState+" currMode:"+_am.getMode()); tm = null;
		 * }catch(Exception e){ } return bring; }
		 */
		long _voiceCallSessionID = -1;
		String _voiceCallOperation = "";

		AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = null;
		int _focusSteamType = 0;

		@TargetApi(Build.VERSION_CODES.FROYO)
		void requestAudioFocus(int streamType) {
			if (android.os.Build.VERSION.SDK_INT <= 8) {
				return;
			}
			if (mAudioFocusChangeListener == null) {
				mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
					@TargetApi(Build.VERSION_CODES.FROYO)
					@Override
					public void onAudioFocusChange(int focusChange) {

						if (QLog.isColorLevel())
							QLog.w("TRAE", QLog.CLR,
									"focusChange:" + focusChange
											+ " _focusSteamType:"
											+ _focusSteamType + " currMode:"
											+ _am.getMode() + " _activeMode:"
											+ _activeMode);

						if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
							// if(isTelphoneRinging()){
							// abandonAudioFocus();
							// }
							// Stop playback
						} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
							// if(isTelphoneRinging()){
							// abandonAudioFocus();
							// }
							// Pause playback
						} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
							// Lower the volume
							// if(isTelphoneRinging()){
							// abandonAudioFocus();
							// }
						} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
							// Rusume playback or Raise it back normal
						}
					}
				};

				if (_am != null) {
					int ret = _am.requestAudioFocus(mAudioFocusChangeListener,
							streamType, AudioManager.AUDIOFOCUS_GAIN);// AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
					if (ret != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
						if (QLog.isColorLevel())
							QLog.e("TRAE", QLog.CLR,
									"request audio focus fail. " + ret
											+ " mode:" + _am.getMode());
					}
					_focusSteamType = streamType;
					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR,
								"-------requestAudioFocus _focusSteamType:"
										+ _focusSteamType);
				}

			}

		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		void abandonAudioFocus() {
			if (android.os.Build.VERSION.SDK_INT <= 8) {
				return;
			}
			if (_am != null && mAudioFocusChangeListener != null) {
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR,
							"-------abandonAudioFocus _focusSteamType:"
									+ _focusSteamType);
				_am.abandonAudioFocus(mAudioFocusChangeListener);
				mAudioFocusChangeListener = null;
			}
		}

		int InternalVoicecallPreprocess(HashMap<String, Object> params) {
			AudioDeviceInterface.LogTraceEntry(" activeMode:" + _activeMode);
			if (null == params) {
				return -1;
			}
			if (_am == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							" InternalVoicecallPreprocess am==null!!");
				return -1;
			}

			if (_activeMode == AUDIO_MANAGER_ACTIVE_VOICECALL) {
				Intent intent = new Intent();
				sendResBroadcast(intent, params, RES_ERRCODE_VOICECALL_EXIST);
				return -1;
			}
/*
			if (_activeMode == AUDIO_MANAGER_ACTIVE_RING) {
				HashMap<String, Object> _rp = new HashMap<String, Object>();
				_rp.put(PARAM_SESSIONID, (Long) _ringSessionID);
				_rp.put(PARAM_OPERATION, _ringOperation);
				InternalStopRing(_rp);
			}
			*/

			_voiceCallSessionID = (Long) params.get(PARAM_SESSIONID);
			_voiceCallOperation = (String) params.get(PARAM_OPERATION);

			_activeMode = AUDIO_MANAGER_ACTIVE_VOICECALL;

			_prevMode = _am.getMode();
			// _prevDevice = _deviceConfigManager.getConnectedDevice();

			Integer mode = -1;
			Integer streamType = 0;

			mode = (Integer) params.get(PARAM_MODEPOLICY);

			if (mode == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							" params.get(PARAM_MODEPOLICY)==null!!");
				_modePolicy = -1;
			} else {
				_modePolicy = mode;
			}
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR, "  _modePolicy:" + _modePolicy);
			/*
			int maxMusic = _am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

			if (_am.getStreamVolume(AudioManager.STREAM_MUSIC) > (maxMusic * 0.7)) {
				_am.setStreamVolume(AudioManager.STREAM_MUSIC,
						(int) (maxMusic * 0.7), 0);
			}
			*/

			streamType = (Integer) params.get(PARAM_STREAMTYPE);

			if (streamType == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							" params.get(PARAM_STREAMTYPE)==null!!");
				_streamType = 0;
			} else {
				_streamType = streamType;
			}

		//	if (isCloseSystemAPM(_modePolicy)) {
				// do nothing
		//	} else {
				
		//	}
			if (isCloseSystemAPM(_modePolicy) && _activeMode != AUDIO_MANAGER_ACTIVE_RING && _deviceConfigManager != null) {   //special phone such as MI2\MI2S
				if (_deviceConfigManager.getConnectedDevice().equals(DEVICE_SPEAKERPHONE)) {
					InternalSetMode(AudioManager.MODE_NORMAL);
					requestAudioFocus(AudioManager.STREAM_MUSIC);
				} else {
					InternalSetMode(AudioManager.MODE_IN_COMMUNICATION);
					requestAudioFocus(AudioManager.STREAM_VOICE_CALL);
				}
			}
			else
			{
				InternalSetMode(getCallAudioMode(_modePolicy));
				requestAudioFocus(_streamType);
			}
			// requestAudioFocus(streamType);
			
			// requestAudioFocus(AudioManager.STREAM_MUSIC);

			// forceVolumeControlStream(_am,streamType);
/*
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR,
						" InternalVoicecallPreprocess test wait 5s entry!!!");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR,
						" InternalVoicecallPreprocess test wait 5s exit!!!");
			*/
			Intent intent = new Intent();
			sendResBroadcast(intent, params, RES_ERRCODE_NONE);
			

			AudioDeviceInterface.LogTraceExit();
			return 0;
		}

		int InternalVoicecallPostprocess(HashMap<String, Object> params) {
			AudioDeviceInterface.LogTraceEntry(" activeMode:" + _activeMode);
			if (_am == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							" InternalVoicecallPostprocess am==null!!");
				return -1;
			}

			if (_activeMode != AUDIO_MANAGER_ACTIVE_VOICECALL) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " not ACTIVE_VOICECALL!!");
				Intent intent = new Intent();
				sendResBroadcast(intent, params,
						RES_ERRCODE_VOICECALL_NOT_EXIST);
				return -1;
			}

			_activeMode = AUDIO_MANAGER_ACTIVE_NONE;

	//		if (_prevMode != -1)
	//			InternalSetMode(_prevMode);
			abandonAudioFocus();
			// InternalConnectDevice(_prevDevice, null);

			Intent intent = new Intent();
			sendResBroadcast(intent, params, RES_ERRCODE_NONE);

			AudioDeviceInterface.LogTraceExit();
			return 0;
		}

		int interruptVoicecallPostprocess() {

			AudioDeviceInterface.LogTraceEntry(" activeMode:" + _activeMode);
			if (_am == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " am==null!!");
				return -1;
			}

			if (_activeMode != AUDIO_MANAGER_ACTIVE_VOICECALL) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " not ACTIVE_RING!!");
				return -1;
			}
			_activeMode = AUDIO_MANAGER_ACTIVE_NONE;

			if (_prevMode != -1)
				InternalSetMode(_prevMode);
			// InternalConnectDevice(_prevDevice, null);

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(PARAM_SESSIONID, (Long) _voiceCallSessionID);
			params.put(PARAM_OPERATION, _voiceCallOperation);

			Intent intent = new Intent();
			sendResBroadcast(intent, params,
					RES_ERRCODE_VOICECALLPOST_INTERRUPT);

			AudioDeviceInterface.LogTraceExit();
			return 0;
		}

		int InternalStartRing(HashMap<String, Object> params) {
			AudioDeviceInterface.LogTraceEntry(" activeMode:" + _activeMode);
			if (_am == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " InternalStartRing am==null!!");
				return -1;
			}
			if (_activeMode == AUDIO_MANAGER_ACTIVE_RING) {
				interruptRing();
			}

			int dataSource = 0;
			int rsId = -1;
			Uri res = null;
			String strFilePath = null;
			boolean bLoop = false;
			int loopCount = 1;
			boolean ringMode = false;
			
			try {
				_ringSessionID = (Long) params.get(PARAM_SESSIONID);
				_ringOperation = (String) params.get(PARAM_OPERATION);
				_ringUserdata = (String) params.get(PARAM_RING_USERDATA_STRING);

				dataSource = (Integer) params.get(PARAM_RING_DATASOURCE);
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "  dataSource:" + dataSource);
				rsId = (Integer) params.get(PARAM_RING_RSID);
				res = (Uri) params.get(PARAM_RING_URI);
				strFilePath = (String) params.get(PARAM_RING_FILEPATH);
				bLoop = (Boolean) params.get(PARAM_RING_LOOP);
				loopCount = (Integer) params.get(PARAM_RING_LOOPCOUNT);
				ringMode = (Boolean) params.get(PARAM_RING_MODE);
				
			} catch (Exception e) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							" startRing err params");
				return -1;
			}
			/*
			if (_activeMode == AUDIO_MANAGER_ACTIVE_VOICECALL && ringMode==true) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							" ACTIVE_VOICECALL can't play ring mode!!");
				Intent intent = new Intent();
				sendResBroadcast(intent, params, RES_ERRCODE_VOICECALL_EXIST);
				return -1;
			}
			*/
			
			if(_activeMode != AUDIO_MANAGER_ACTIVE_VOICECALL)
				_activeMode = AUDIO_MANAGER_ACTIVE_RING;
			Intent intent = new Intent();
			intent.putExtra(PARAM_RING_USERDATA_STRING, _ringUserdata);
			sendResBroadcast(intent, params, RES_ERRCODE_NONE);
			
			_preRingMode = _am.getMode();
			// stratring...
			_ringPlayer.playRing(dataSource, rsId, res, strFilePath, bLoop,
					loopCount,ringMode,_activeMode == AUDIO_MANAGER_ACTIVE_VOICECALL?true:false,_streamType);
			
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, " _ringUserdata:" + _ringUserdata+" DurationMS:"+_ringPlayer.getDuration());
			
			if(!_ringPlayer.hasCall())
				requestAudioFocus(_ringPlayer.getStreamType());
			// _prevDevice = _deviceConfigManager.getConnectedDevice();
			// play ring
			InternalNotifyStreamTypeUpdate(_ringPlayer.getStreamType());
			AudioDeviceInterface.LogTraceExit();
			return 0;
		}

		int InternalStopRing(HashMap<String, Object> params) {
			AudioDeviceInterface.LogTraceEntry(" activeMode:" + _activeMode
					+ " _preRingMode:" + _preRingMode);
			if (_am == null||_ringPlayer==null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " InternalStopRing am==null!!");
				return -1;
			}
			/*
			if (_activeMode != AUDIO_MANAGER_ACTIVE_RING) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " not ACTIVE_RING!!");
				Intent intent = new Intent();
				sendResBroadcast(intent, params, RES_ERRCODE_RING_NOT_EXIST);
				return -1;
			}
			*/

			_ringPlayer.stopRing();
			if(!_ringPlayer.hasCall()&&_activeMode == AUDIO_MANAGER_ACTIVE_RING){
		//		InternalSetMode(_preRingMode);
				abandonAudioFocus();
				_activeMode = AUDIO_MANAGER_ACTIVE_NONE;
			}
			
			Intent intent = new Intent();
			intent.putExtra(PARAM_RING_USERDATA_STRING, _ringUserdata);
			sendResBroadcast(intent, params, RES_ERRCODE_NONE);
			// InternalConnectDevice(_prevDevice, null);

			AudioDeviceInterface.LogTraceExit();
			return 0;
		}
		
		int InternalGetStreamType(HashMap<String, Object> params) {
			AudioDeviceInterface.LogTraceEntry(" activeMode:" + _activeMode
					+ " _preRingMode:" + _preRingMode);
			if (_am == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " InternalStopRing am==null!!");
				return -1;
			}
			
			int st = -1;
			
			//if (_activeMode == AUDIO_MANAGER_ACTIVE_VOICECALL) {
			//	st = _streamType;
			if(_activeMode == AUDIO_MANAGER_ACTIVE_RING){
				st = _ringPlayer.getStreamType();
			}else{
				st = _streamType;
			}
			// stopring

			Intent intent = new Intent();
			intent.putExtra(EXTRA_DATA_STREAMTYPE, st);
			
			sendResBroadcast(intent, params,RES_ERRCODE_NONE);
			// InternalConnectDevice(_prevDevice, null);

			AudioDeviceInterface.LogTraceExit();
			return 0;
		}
		
		int InternalNotifyStreamTypeUpdate(int st) {
			if (null == _context) {
				return -1;
			}
			// boolean bChangabled =
			// _deviceConfigManager.getConnectingDeviceList().size() > 0;
			Intent intent = new Intent();
			intent.setAction(ACTION_TRAEAUDIOMANAGER_NOTIFY);
			// intent.putExtra(PARAM_SESSIONID, (Long)params.get(PARAM_SESSIONID));
			intent.putExtra(PARAM_OPERATION, NOTIFY_STREAMTYPE_UPDATE);
			intent.putExtra(EXTRA_DATA_STREAMTYPE,
					st);
			_context.sendBroadcast(intent);

			return 0;
		}
		
		int interruptRing() {

			AudioDeviceInterface.LogTraceEntry(" activeMode:" + _activeMode
					+ " _preRingMode:" + _preRingMode);
			if (_am == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " interruptRing am==null!!");
				return -1;
			}

			if (_activeMode != AUDIO_MANAGER_ACTIVE_RING) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " not ACTIVE_RING!!");
				return -1;
			}

			// stopring
			_ringPlayer.stopRing();
		//	InternalSetMode(_preRingMode);
			abandonAudioFocus();
			_activeMode = AUDIO_MANAGER_ACTIVE_NONE;

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(PARAM_SESSIONID, (Long) _ringSessionID);
			params.put(PARAM_OPERATION, _ringOperation);
			Intent intent = new Intent();
			intent.putExtra(PARAM_RING_USERDATA_STRING, _ringUserdata);
			sendResBroadcast(intent, params, RES_ERRCODE_STOPRING_INTERRUPT);
			AudioDeviceInterface.LogTraceExit();
			return 0;
		}

		void notifyRingCompletion() {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(PARAM_SESSIONID, (Long) _ringSessionID);
			params.put(PARAM_OPERATION, NOTIFY_RING_COMPLETION);
			Intent intent = new Intent();
			intent.putExtra(PARAM_RING_USERDATA_STRING, _ringUserdata);
			sendResBroadcast(intent, params, RES_ERRCODE_NONE);
		}
	}

	TraeAudioManagerLooper mTraeAudioManagerLooper = null;

	ReentrantLock _lock = new ReentrantLock();

	/************************************************************************************************************/

	public static int SetSpeakerForTest(Context context, boolean speakerOn) {

		int iRet = -1;

		_glock.lock();

		if (null != _ginstance) {
			iRet = _ginstance.InternalSetSpeaker(context, speakerOn);
		} else {
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"TraeAudioManager|static SetSpeakerForTest|null == _ginstance");
		}

		_glock.unlock();

		return iRet;
	}

	static final String AUDIO_PARAMETER_STREAM_ROUTING = "routing";
	static final int AUDIO_DEVICE_OUT_EARPIECE = 0x1;
	static final int AUDIO_DEVICE_OUT_SPEAKER = 0x2;
	static final int AUDIO_DEVICE_OUT_WIRED_HEADSET = 0x4;
	static final int AUDIO_DEVICE_OUT_WIRED_HEADPHONE = 0x8;
	static final int AUDIO_DEVICE_OUT_BLUETOOTH_SCO = 0x10;
	static final int AUDIO_DEVICE_OUT_BLUETOOTH_SCO_HEADSET = 0x20;
	static final int AUDIO_DEVICE_OUT_BLUETOOTH_SCO_CARKIT = 0x40;
	static final int AUDIO_DEVICE_OUT_BLUETOOTH_A2DP = 0x80;
	static final int AUDIO_DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 0x100;
	static final int AUDIO_DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER = 0x200;

	int InternalSetSpeaker(Context context, boolean speakerOn) {
		if (context == null) {
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR,
						"Could not InternalSetSpeaker - no context");
			return -1;
		}

		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		if (am == null) {
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR,
						"Could not InternalSetSpeaker - no audio manager");
			return -1;
		}

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "InternalSetSpeaker entry:" + "speaker:"
					+ (am.isSpeakerphoneOn() ? "Y" : "N") + "-->:"
					+ (speakerOn ? "Y" : "N"));
		/*
		 * if(Build.MANUFACTURER.equals("Xiaomi") &&
		 * (!Build.MODEL.equals("MI 2SC") ) &&
		 * _streamType==AudioManager.STREAM_MUSIC ){
		 * 
		 * return InternalSetSpeakerXiaomi(am,speakerOn); } //MI 2SC
		 * am.getMode()!=AudioManager.MODE_RINGTONE && 
		 */
		if (isCloseSystemAPM(_modePolicy) && _activeMode != AUDIO_MANAGER_ACTIVE_RING) {

			return InternalSetSpeakerSpe(am, speakerOn);
		}

		if (am.isSpeakerphoneOn() != speakerOn)
			am.setSpeakerphoneOn(speakerOn);
		int res = (am.isSpeakerphoneOn() == speakerOn ? 0 : -1);

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "InternalSetSpeaker exit:" + speakerOn
					+ " res:" + res + " mode:" + am.getMode() /*
															 * +" last:"+(
															 * _speakerSetLastState
															 * ?"Y":"N")
															 */);
		return res;
	}

	int InternalSetSpeakerSpe(AudioManager am, boolean speakerOn) {

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "InternalSetSpeakerSpe fac:"
					+ Build.MANUFACTURER + " model:" + Build.MODEL + " st:"
					+ _streamType + " media_force_use:"
					+ getForceUse(FOR_MEDIA));

		if (speakerOn) {
			InternalSetMode(AudioManager.MODE_NORMAL);

			am.setSpeakerphoneOn(true);
			//
			// setForceUse(FOR_MEDIA,FORCE_NO_BT_A2DP);
			setForceUse(FOR_MEDIA, FORCE_SPEAKER);

			// setPhoneState(AudioManager.MODE_NORMAL);
			// InternalSetMode(AudioManager.MODE_NORMAL);

		} else {
			InternalSetMode(AudioManager.MODE_IN_COMMUNICATION);
			// InternalSetMode(AudioManager.MODE_IN_CALL);
			// setPhoneState(AudioManager.MODE_IN_COMMUNICATION);

			am.setSpeakerphoneOn(false);
			setForceUse(FOR_MEDIA, FORCE_NONE);
			/*
			 * am.setSpeakerphoneOn(false);
			 * if(am.getMode()==AudioManager.MODE_NORMAL
			 * &&android.os.Build.VERSION.SDK_INT>=11){
			 * setPhoneState(AudioManager.MODE_IN_COMMUNICATION); //
			 * InternalSetMode(AudioManager.MODE_IN_COMMUNICATION); }
			 * am.setSpeakerphoneOn(false);
			 */
			// InternalSetMode(AudioManager.MODE_NORMAL);
		}

		// setParameters(AUDIO_PARAMETER_STREAM_ROUTING+"="+(speakerOn?AUDIO_DEVICE_OUT_SPEAKER:AUDIO_DEVICE_OUT_EARPIECE)+";");

		// setForceUse(FOR_MEDIA,speakerOn?FORCE_SPEAKER:FORCE_HEADPHONES);

		// setForceUse(FOR_MEDIA,speakerOn?FORCE_NONE:FORCE_HEADPHONES);
		// setForceUse(FOR_COMMUNICATION,speakerOn?FORCE_SPEAKER:FORCE_NONE);

		int res = (am.isSpeakerphoneOn() == speakerOn ? 0 : -1);

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "InternalSetSpeakerSpe exit:" + speakerOn
					+ " res:" + res + " mode:" + am.getMode() /*
															 * +" last:"+(
															 * _speakerSetLastState
															 * ?"Y":"N")
															 */);
		return res;
	}

	/*
	 * int ExSetWiredHeadset(Context context, boolean bOn) {
	 * 
	 * if(QLog.isColorLevel())
	 * QLog.e("TRAE",QLog.CLR,"ExSetWiredHeadset|Entry");
	 * 
	 * if ( null == mTraeAudioManagerLooper ) { return -1; }
	 * 
	 * //mTraeAudioManagerLooper.sendMessage(TraeAudioManagerLooper.
	 * MESSAGE_SETWIREDHEADSET, context, bOn);
	 * 
	 * return 0; }
	 */
	/*
	 * @SuppressWarnings("deprecation") int InternalSetWiredHeadset(Context
	 * context, boolean bOn) {
	 * 
	 * if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"SetWiredHeadset|Entry");
	 * 
	 * if(context==null) { if(QLog.isColorLevel())
	 * QLog.e("TRAE",QLog.CLR,"Could not SetWiredHeadset - no context"); return
	 * -1; }
	 * 
	 * AudioManager am =
	 * (AudioManager)context.getSystemService(Context.AUDIO_SERVICE); if (am ==
	 * null) { if(QLog.isColorLevel())
	 * QLog.e("TRAE",QLog.CLR,"Could not SetWiredHeadset - no audio manager" );
	 * return -1; }
	 * 
	 * if ( !_deviceConfigManager.getVisible(DEVICE_WIREDHEADSET) ) {
	 * AudioDeviceInterface
	 * .DoLogErr("Could not SetWiredHeadset - !_bWiredHeadsetAvailabled");
	 * return -1; }
	 * 
	 * am.setWiredHeadsetOn(bOn);
	 * 
	 * return 0; }
	 */

	/*
	 * int ExSetBluetoothHeadset(Context context, boolean bOn) {
	 * 
	 * if(QLog.isColorLevel())
	 * QLog.e("TRAE",QLog.CLR,"ExSetBluetoothHeadset|Entry");
	 * 
	 * if ( null == mTraeAudioManagerLooper ) { return -1; }
	 * 
	 * //mTraeAudioManagerLooper.sendMessage(TraeAudioManagerLooper.
	 * MESSAGE_SETBLUETOOTHHEADSET, context, bOn);
	 * 
	 * return 0; }
	 * 
	 * 
	 * int InternalSetBluetoothHeadset(Context context, boolean bOn) {
	 * 
	 * if(QLog.isColorLevel())
	 * QLog.e("TRAE",QLog.CLR,"SetBluetoothHeadset|Entry");
	 * 
	 * if ( bOn ) {
	 * 
	 * if ( null != _am ) { if ( _am.isBluetoothA2dpOn() ||
	 * _am.isBluetoothScoOn() ) { return 0; } }
	 * 
	 * _disableBTCheck(); _enableBTCheck();
	 * 
	 * } else { _disableBTCheck(); }
	 * 
	 * return 0; }
	 */

	void InternalSetMode(int audioMode) {

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "SetMode entry:" + audioMode);

		if (_am == null) {
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, "setMode:" + audioMode
						+ " fail am=null");
			return;
		}
		// if(isTelphoneRinging()==false)
		_am.setMode(audioMode);

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "setMode:" + audioMode
					+ (_am.getMode() != audioMode ? "fail" : "success"));

	}

	/************************************************************************************************************/

	public static int registerAudioSession(boolean bRegister, long nSessionId,
			Context ctx) {

		int iRet = -1;

		_glock.lock();

		if (null != _ginstance) {

			if (bRegister) {
				_ginstance._audioSessionHost.add(nSessionId, ctx);
				//if(QLog.isColorLevel())
	            	//QLog.d("TRAE", QLog.CLR, "[register] add AudioSession: "+nSessionId);
			} else {
				_ginstance._audioSessionHost.remove(nSessionId);
				//if(QLog.isColorLevel())
	            	//QLog.d("TRAE", QLog.CLR, "[register] remove AudioSession: "+nSessionId);
			}

			iRet = 0;
		}

		_glock.unlock();

		return iRet;
	}

	public static int sendMessage(int nMsg, HashMap<String, Object> params) {

		int iRet = -1;

		_glock.lock();

		if (null != _ginstance) {
			iRet = _ginstance.internalSendMessage(nMsg, params);
		}

		_glock.unlock();

		return iRet;
	}

	public static int init(Context ctx) {
		AudioDeviceInterface.LogTraceEntry(" _ginstance:" + _ginstance);
		// int iRet = -1;

		_glock.lock();

		if (null == _ginstance) {
			_ginstance = new TraeAudioManager(ctx);
			// iRet = _ginstance._init(params);
		} else {
		}

		_glock.unlock();
		AudioDeviceInterface.LogTraceExit();
		return 0;
	}

	public static void uninit() {
		AudioDeviceInterface.LogTraceEntry(" _ginstance:" + _ginstance);
		_glock.lock();

		if (null != _ginstance) {
			_ginstance.release();
			// _ginstance._uninit();
			_ginstance = null;
		}

		_glock.unlock();
		AudioDeviceInterface.LogTraceExit();
	}

	/************************************************************************************************************/
	TraeAudioManager(Context ctx) {
		AudioDeviceInterface.LogTraceEntry(" context:" + ctx);
		if (ctx == null)
			return;

		_context = ctx;
		mTraeAudioManagerLooper = new TraeAudioManagerLooper(this);
		if (mTraeAudioManagerLooper != null) {
			// mTraeAudioManagerLooper._init();
		}
		AudioDeviceInterface.LogTraceExit();
	}

	public void release() {
		AudioDeviceInterface.LogTraceEntry("");
		if (null != mTraeAudioManagerLooper) {
			mTraeAudioManagerLooper.quit();

			mTraeAudioManagerLooper = null;
		}

		/*
		 * if (_bluetoothCheck != null) _bluetoothCheck.release();
		 * _bluetoothCheck = null; if (null != _context) {
		 * _context.unregisterReceiver(this); _context = null; }
		 * _deviceConfigManager.clearConfig(); _deviceConfigManager = null;
		 */
		// _deviceStatusList = null;
		AudioDeviceInterface.LogTraceExit();
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		// _lock.lock();
		if(intent == null || context == null){
	     	   if(QLog.isColorLevel())
	            	QLog.d("TRAE", QLog.CLR, "onReceive intent or context is null!");
	     	   return;
	    }
		boolean prevWiredHeadset = false;
		boolean prevBluetoothHeadset = false;
		try{
			String strAction = intent.getAction();
			String strOption = intent
					.getStringExtra(TraeAudioManager.PARAM_OPERATION);
			//
			// if(QLog.isColorLevel())
			// QLog.w("TRAE",QLog.CLR,"TraeAudioManager|onReceive::intent:" +
			// intent.toString()+" intent.getAction():"+intent.getAction() +
			// " strOption = " + strOption );
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, "TraeAudioManager|onReceive::Action:"
						+ intent.getAction());
		if(_deviceConfigManager == null){
			if(QLog.isColorLevel())
            	QLog.d("TRAE", QLog.CLR, "_deviceConfigManager null!");
			 return;
		}
			prevWiredHeadset = _deviceConfigManager.getVisible(DEVICE_WIREDHEADSET);
			prevBluetoothHeadset = _deviceConfigManager
					.getVisible(DEVICE_BLUETOOTHHEADSET);
	
			if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
				onHeadsetPlug(context, intent);
				if (prevWiredHeadset == false
						&& _deviceConfigManager.getVisible(DEVICE_WIREDHEADSET) == true) {
					checkDevicePlug(DEVICE_WIREDHEADSET, true);
				}
	
				if (prevWiredHeadset == true
						&& _deviceConfigManager.getVisible(DEVICE_WIREDHEADSET) == false) {
					checkDevicePlug(DEVICE_WIREDHEADSET, false);
				}
	
			} else if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent
					.getAction())) {
				// onAudioBecomingNoisy(context, intent);
			} else if (TraeAudioManager.ACTION_TRAEAUDIOMANAGER_REQUEST
					.equals(strAction)) {
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "   OPERATION:" + strOption);
				if (TraeAudioManager.OPERATION_REGISTERAUDIOSESSION
						.equals(strOption)) {
					registerAudioSession(intent.getBooleanExtra(
							REGISTERAUDIOSESSION_ISREGISTER, false),
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							context);
				} else if (TraeAudioManager.OPERATION_STARTSERVICE
						.equals(strOption)) {
					startService(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false, intent.getStringExtra(EXTRA_DATA_DEVICECONFIG));
				} else if (TraeAudioManager.OPERATION_STOPSERVICE.equals(strOption)) {
					stopService(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false);
				} else if (TraeAudioManager.OPERATION_GETDEVICELIST
						.equals(strOption)) {
					getDeviceList(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false);
				}else if (TraeAudioManager.OPERATION_GETSTREAMTYPE
						.equals(strOption)) {
					getStreamType(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false);
				}
				
				else if (TraeAudioManager.OPERATION_CONNECTDEVICE
						.equals(strOption)) {
					connectDevice(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false, intent.getStringExtra(CONNECTDEVICE_DEVICENAME));
				}else if (TraeAudioManager.OPERATION_CONNECT_HIGHEST_PRIORITY_DEVICE
						.equals(strOption)) {
					connectHighestPriorityDevice(strOption,intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),false);
				}
				else if (TraeAudioManager.OPERATION_EARACTION.equals(strOption)) {
					earAction(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false, intent.getIntExtra(EXTRA_EARACTION, -1));
				} else if (TraeAudioManager.OPERATION_ISDEVICECHANGABLED
						.equals(strOption)) {
					isDeviceChangabled(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false);
				} else if (TraeAudioManager.OPERATION_GETCONNECTEDDEVICE
						.equals(strOption)) {
					getConnectedDevice(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false);
				} else if (TraeAudioManager.OPERATION_GETCONNECTINGDEVICE
						.equals(strOption)) {
					getConnectingDevice(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false);
				} else if (TraeAudioManager.OPERATION_VOICECALL_PREPROCESS
						.equals(strOption)) {
					int modePolicy;
					int streamType;
					modePolicy = intent.getIntExtra(PARAM_MODEPOLICY, -1);
					streamType = intent.getIntExtra(PARAM_STREAMTYPE, -1);
					voicecallPreprocess(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false, modePolicy, streamType);
				} else if (TraeAudioManager.OPERATION_VOICECALL_POSTPROCESS
						.equals(strOption)) {
					voicecallPostprocess(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false);
				} //
				else if (TraeAudioManager.OPERATION_VOICECALL_AUDIOPARAM_CHANGED
						.equals(strOption)) {
					int modePolicy;
					int streamType;
					modePolicy = intent.getIntExtra(PARAM_MODEPOLICY, -1);
					streamType = intent.getIntExtra(PARAM_STREAMTYPE, -1);
					voiceCallAudioParamChanged(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false, modePolicy, streamType);
				}
				
				else if (TraeAudioManager.OPERATION_STARTRING.equals(strOption)) {
					int dataSource;
					int rsId;
					Uri res = null;
					String strFilePath = null;
					String userData = null;
					boolean bLoop;
					int loopCount;
					boolean ringMode;
					dataSource = intent.getIntExtra(PARAM_RING_DATASOURCE, -1);
					rsId = intent.getIntExtra(PARAM_RING_RSID, -1);
					res = intent.getParcelableExtra(PARAM_RING_URI);
					strFilePath = intent.getStringExtra(PARAM_RING_FILEPATH);
					bLoop = intent.getBooleanExtra(PARAM_RING_LOOP, false);
					userData = intent.getStringExtra(PARAM_RING_USERDATA_STRING);
					loopCount = intent.getIntExtra(PARAM_RING_LOOPCOUNT, 1);
					ringMode = intent.getBooleanExtra(PARAM_RING_MODE, false);
					startRing(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false, dataSource, rsId, res, strFilePath, bLoop,
							loopCount, userData,ringMode);
				} else if (TraeAudioManager.OPERATION_STOPRING.equals(strOption)) {
					stopRing(strOption,
							intent.getLongExtra(PARAM_SESSIONID, Long.MIN_VALUE),
							false);
				}
	
				//
			} else if(_deviceConfigManager!=null){
				if(_bluetoothCheck!=null)
					_bluetoothCheck.onReceive(context, intent, _deviceConfigManager);
				if (prevBluetoothHeadset == false
						&& _deviceConfigManager.getVisible(DEVICE_BLUETOOTHHEADSET) == true) {
					checkDevicePlug(DEVICE_BLUETOOTHHEADSET, true);
				}
	
				if (prevBluetoothHeadset == true
						&& _deviceConfigManager.getVisible(DEVICE_BLUETOOTHHEADSET) == false) {
					checkDevicePlug(DEVICE_BLUETOOTHHEADSET, false);
				}
			}
	
			// checkAutoDeviceListUpdate();
			// _lock.unlock();
		}catch(Exception e){
			
		}
	}

	/*
	 * state - 0 for unplugged, 1 for plugged. name - Headset type, human
	 * readable string microphone - 1 if headset has a microphone, 0 otherwise
	 */
	void onHeadsetPlug(Context context, Intent intent) {

		String name;
		int state;
		int microphone;
		String logs = "";

		name = intent.getStringExtra("name");

		if (name == null) {
			name = "unkonw";
		}

		logs += " [" + name + "] ";

		state = intent.getIntExtra("state", -1);

		if (state != -1) {
			// SetSpeaker(context,state == 0 ? true : false);
			logs += (state == 0 ? "unplugged" : "plugged");
		}

		logs += " mic:";
		microphone = intent.getIntExtra("microphone", -1);

		if (microphone != -1) {
			logs += (microphone == 1 ? "Y" : "unkown");
		}

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "onHeadsetPlug:: " + logs);

		_deviceConfigManager.setVisible(DEVICE_WIREDHEADSET, 1 == state);

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "onHeadsetPlug exit");
	}

	int internalSendMessage(int nMsg, HashMap<String, Object> params) {

		int iRet = -1;

		if (null != mTraeAudioManagerLooper) {
			iRet = mTraeAudioManagerLooper.sendMessage(nMsg, params);
		}

		return iRet;
	}

	static int getDeviceList(String strOption, long nSessionId,
			boolean bHostside) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_GETDEVICELIST, params);
	}
	//
	static int getStreamType(String strOption, long nSessionId,
			boolean bHostside) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_GETSTREAMTYPE, params);
	}
	
	static int startService(String strOption, long nSessionId,
			boolean bHostside, String deviceConfig) {

		if (null == deviceConfig || deviceConfig.length() <= 0) {
			return -1;
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);
		params.put(EXTRA_DATA_DEVICECONFIG, deviceConfig);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_ENABLE, params);
	}

	static int stopService(String strOption, long nSessionId, boolean bHostside) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_DISABLE, params);
	}

	static int connectDevice(String strOption, long nSessionId,
			boolean bHostside, String strDevice) {

		if (strDevice == null)
			return -1;
		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);
		params.put(CONNECTDEVICE_DEVICENAME, strDevice);
		params.put(PARAM_DEVICE, strDevice);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_CONNECTDEVICE, params);
	}
//
	static int connectHighestPriorityDevice(String strOption, long nSessionId,boolean bHostside) {

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_CONNECT_HIGHEST_PRIORITY_DEVICE, params);
	}	
	static int earAction(String strOption, long nSessionId, boolean bHostside,
			int earAction) {

		if (earAction != TraeAudioManager.EARACTION_AWAY
				&& earAction != TraeAudioManager.EARACTION_CLOSE)
			return -1;
		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);
		params.put(TraeAudioManager.EXTRA_EARACTION, earAction);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_EARACTION, params);
	}

	static int isDeviceChangabled(String strOption, long nSessionId,
			boolean bHostside) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_ISDEVICECHANGABLED,
				params);
	}

	static int getConnectedDevice(String strOption, long nSessionId,
			boolean bHostside) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_GETCONNECTEDDEVICE,
				params);
	}

	static int getConnectingDevice(String strOption, long nSessionId,
			boolean bHostside) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_GETCONNECTINGDEVICE,
				params);
	}

	static int voicecallPreprocess(String strOption, long nSessionId,
			boolean bHostside, int modePolicy, int streamType) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);
		params.put(PARAM_MODEPOLICY, modePolicy);
		params.put(PARAM_STREAMTYPE, streamType);
		return sendMessage(TraeAudioManagerLooper.MESSAGE_VOICECALLPREPROCESS,
				params);
	}

	static int voicecallPostprocess(String strOption, long nSessionId,
			boolean bHostside) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_VOICECALLPOSTPROCESS,
				params);
	}
	
	static int voiceCallAudioParamChanged(String strOption, long nSessionId,
			boolean bHostside, int modePolicy, int streamType) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);
		params.put(PARAM_MODEPOLICY, modePolicy);
		params.put(PARAM_STREAMTYPE, streamType);
		return sendMessage(TraeAudioManagerLooper.MESSAGE_VOICECALL_AUIDOPARAM_CHANGED,
				params);
	}
	

	static int startRing(String strOption, long nSessionId, boolean bHostside,
			int dataSource, int rsId, Uri res, String strFilePath,
			boolean bLoop, int loopCount, String userData,boolean ringMode) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		params.put(PARAM_RING_DATASOURCE, dataSource);
		params.put(PARAM_RING_RSID, rsId);
		params.put(PARAM_RING_URI, res);
		params.put(PARAM_RING_FILEPATH, strFilePath);
		params.put(PARAM_RING_LOOP, bLoop);
		params.put(PARAM_RING_LOOPCOUNT, loopCount);
		params.put(PARAM_RING_MODE, ringMode);

		params.put(PARAM_RING_USERDATA_STRING, userData);
		return sendMessage(TraeAudioManagerLooper.MESSAGE_STARTRING, params);
	}

	static int stopRing(String strOption, long nSessionId, boolean bHostside) {

		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_STOPRING, params);
	}
	
	static int requestReleaseAudioFocus(String strOption, long nSessionId, boolean bHostside){
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_REQUEST_RELEASE_AUDIO_FOCUS, params);	
	}

	static int recoverAudioFocus(String strOption, long nSessionId, boolean bHostside){
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(PARAM_SESSIONID, nSessionId);
		params.put(PARAM_OPERATION, strOption);
		params.put(PARAM_ISHOSTSIDE, bHostside);

		return sendMessage(TraeAudioManagerLooper.MESSAGE_RECOVER_AUDIO_FOCUS, params);	
	}
	
	int InternalSessionConnectDevice(HashMap<String, Object> params) {

		AudioDeviceInterface.LogTraceEntry("");

		if (null == params || _context == null) {
			return -1;
		}

		String devName = "unkown";
		devName = (String) params.get(PARAM_DEVICE);
		// boolean err = false;
		int err = RES_ERRCODE_NONE;

		boolean bChangabled = InternalIsDeviceChangeable();

		if (checkDevName(devName) != true) {
			err = RES_ERRCODE_DEVICE_UNKOWN;
		} else if (_deviceConfigManager.getVisible(devName) == false) {
			err = RES_ERRCODE_DEVICE_NOT_VISIABLE;
		} else if (!bChangabled) {
			err = RES_ERRCODE_DEVICE_UNCHANGEABLE;
		}
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR,
					"sessonID:" + (Long) params.get(PARAM_SESSIONID)
							+ " devName:" + devName + " bChangabled:"
							+ (bChangabled ? "Y" : "N") + " err:" + err);

		if (err != RES_ERRCODE_NONE) {
			Intent intent = new Intent();
			intent.putExtra(CONNECTDEVICE_RESULT_DEVICENAME,
					(String) params.get(PARAM_DEVICE));
			sendResBroadcast(intent, params, err);
			return -1;
		}

		if (devName.equals(_deviceConfigManager.getConnectedDevice())) {
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR, " --has connected!");
			Intent intent = new Intent();
			intent.putExtra(CONNECTDEVICE_RESULT_DEVICENAME,
					(String) params.get(PARAM_DEVICE));
			sendResBroadcast(intent, params, err);
			return 0;
		}

		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, " --connecting...");
		InternalConnectDevice(devName, params);
		AudioDeviceInterface.LogTraceExit();
		return 0;// sendMessage( TraeAudioManagerLooper.MESSAGE_SWITCHDEVICE,
					// params );
	}

	int InternalSessionEarAction(HashMap<String, Object> params) {

		return 0;
	}

	int InternalConnectDevice(String devName,
			HashMap<String, Object> needResParams) {
		AudioDeviceInterface.LogTraceEntry(" devName:" + devName);
		if (devName == null)
			return -1;

		if (!_deviceConfigManager.getConnectedDevice().equals(DEVICE_NONE)
				&& devName.equals(_deviceConfigManager.getConnectedDevice()))
			return 0;

		if (checkDevName(devName) != true
				|| _deviceConfigManager.getVisible(devName) != true) {
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR, " checkDevName fail");
			return -1;
		}
		if (InternalIsDeviceChangeable() != true) {
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR, " InternalIsDeviceChangeable fail");
			return -1;
		}

		if (_switchThread != null) {
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"_switchThread:" + _switchThread.getDeviceName());
			_switchThread.quit();
			_switchThread = null;
		}
		// _switchThread.
		if (devName.equals(DEVICE_EARPHONE)) {
			_switchThread = new earphoneSwitchThread();
		} else if (devName.equals(DEVICE_SPEAKERPHONE)) {
			_switchThread = new speakerSwitchThread();
		} else if (devName.equals(DEVICE_WIREDHEADSET)) {
			_switchThread = new headsetSwitchThread();
		} else if (devName.equals(DEVICE_BLUETOOTHHEADSET)) {
			_switchThread = new bluetoothHeadsetSwitchThread();
		}

		if (_switchThread != null) {
			_switchThread.setDeviceConnectParam(needResParams);
			_switchThread.start();
		}
		AudioDeviceInterface.LogTraceExit();
		return 0;
	}

	abstract class switchThread extends Thread {
		boolean _running = true;
		boolean[] _exited = new boolean[] { false };
		HashMap<String, Object> _params = null;
		long _usingtime=0;
		/*
		 * HashMap<String, Object> params = new HashMap<String, Object>();
		 * 
		 * params.put(PARAM_SESSIONID, nSessionId); params.put(PARAM_ISHOSTSIDE,
		 * bHostside); params.put(CONNECTDEVICE_DEVICENAME, strDevice);
		 * params.put(PARAM_CONTEXT, ctx); params.put(PARAM_DEVICE, strDevice);
		 */
		switchThread() {
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, " ++switchThread:" + getDeviceName());
		}

		public void setDeviceConnectParam(HashMap<String, Object> params) {
			_params = params;
		}

		void updateStatus() {
			_deviceConfigManager.setConnected(getDeviceName());
			/*
			 * if(_params==null){ InternalNotifyDeviceListUpdate(); }else{
			 * processDeviceConnectRes(DEVICE_STATUS_CONNECTED);
			 * }RES_ERRCODE_DEVICE_BTCONNCECTED_TIMEOUT
			 */
			processDeviceConnectRes(RES_ERRCODE_NONE);
			//InternalNotifyDeviceChangableUpdate();
		}

		void processDeviceConnectRes(int err) {
			InternalNotifyDeviceChangableUpdate();
			AudioDeviceInterface.LogTraceEntry(getDeviceName() + " err:" + err);
			if (_params == null) {
				InternalNotifyDeviceListUpdate();
				return;
			}
			sessionConnectedDev = _deviceConfigManager.getConnectedDevice();
			Long sid = (Long) _params.get(PARAM_SESSIONID);
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, " sessonID:" + sid);
			if (sid == null || sid == Long.MIN_VALUE) {
				InternalNotifyDeviceListUpdate();
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR,
							"processDeviceConnectRes sid null,don't send res");
				return;
			}

			Intent intent = new Intent();
			intent.putExtra(CONNECTDEVICE_RESULT_DEVICENAME,
					(String) _params.get(PARAM_DEVICE));
			if (sendResBroadcast(intent, _params, err) == 0)
				InternalNotifyDeviceListUpdate();

			AudioDeviceInterface.LogTraceExit();
		}

		public void run() {
			AudioDeviceInterface.LogTraceEntry(getDeviceName());
			// _running = true;
			_deviceConfigManager.setConnecting(getDeviceName());
			InternalNotifyDeviceChangableUpdate();
			
			_run();
			
			
			synchronized (_exited) {
				_exited[0] = true;
				_exited.notify();
			}

			AudioDeviceInterface.LogTraceExit();
		}

		public void quit() {
			AudioDeviceInterface.LogTraceEntry(getDeviceName());
			_running = false;
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, " quit:" + getDeviceName()
						+ " _running:" + _running);
			this.interrupt();
			_quit();
			synchronized (_exited) {
				if (_exited[0] == false) {
					try {
						_exited.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			AudioDeviceInterface.LogTraceExit();
		}

		public abstract String getDeviceName();

		public abstract void _run();

		public abstract void _quit();
		/*
		protected void notifySwitchStart(){
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, "notify switch start:" +_deviceConfigManager.getConnectedDevice()+ "-->"+ getDeviceName());
			
			_usingtime = SystemClock.elapsedRealtime();
			InternalNotifyRouteSwitchStart(_deviceConfigManager.getConnectedDevice(),getDeviceName());
		}
		protected void notifySwitchEnd(){
			
			
			_usingtime = SystemClock.elapsedRealtime()-_usingtime;
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, "notify switch end:" + getDeviceName()+( _deviceConfigManager.getConnectedDevice().equals(getDeviceName())?"success":"fail") +" "+_usingtime+"ms"  );
			InternalNotifyRouteSwitchEnd(_deviceConfigManager.getConnectedDevice(),_usingtime);
		}	
		*/
		
	}
/*
	int InternalNotifyRouteSwitchStart(String from,String to) {
		if (null == _context) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(ACTION_TRAEAUDIOMANAGER_NOTIFY);
		intent.putExtra(PARAM_OPERATION, NOTIFY_ROUTESWITCHSTART);
		intent.putExtra(EXTRA_DATA_ROUTESWITCHSTART_FROM, from);
		intent.putExtra(EXTRA_DATA_ROUTESWITCHSTART_TO, to);

		_context.sendBroadcast(intent);

		return 0;
	}
	

	int InternalNotifyRouteSwitchEnd(String connectedDev,long timeMs) {
		if (null == _context) {
			return -1;
		}

		Intent intent = new Intent();
		intent.setAction(ACTION_TRAEAUDIOMANAGER_NOTIFY);
		intent.putExtra(PARAM_OPERATION, NOTIFY_ROUTESWITCHEND);
		intent.putExtra(EXTRA_DATA_ROUTESWITCHEND_DEV, connectedDev);
		intent.putExtra(EXTRA_DATA_ROUTESWITCHEND_TIME, timeMs);
		_context.sendBroadcast(intent);

		return 0;
	}	
*/
	// 拔插行为表现不一样的
	switchThread _switchThread = null;

	class earphoneSwitchThread extends switchThread {
		@Override
		public void _run() {
			// TODO Auto-generated method stub
			int i = 0;
		//	notifySwitchStart();
			InternalSetSpeaker(_context, false);
			updateStatus();
		//	notifySwitchEnd();

			// while (_running == true && i++ < 5) {
			while (_running == true) {
				if (_am.isSpeakerphoneOn() != false) {
					// InternalNotifyDeviceChangableUpdate();
					InternalSetSpeaker(_context, false);
				} else {
					// break;

				}

				try {
					Thread.sleep(i < 5 ? 1000 : 4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				i++;
			}
		}

		@Override
		public String getDeviceName() {
			// TODO Auto-generated method stub
			return DEVICE_EARPHONE;
		}

		@Override
		public void _quit() {
			// TODO Auto-generated method stub

		}
	}

	class speakerSwitchThread extends switchThread {
		@Override
		public void _run() {
			// TODO Auto-generated method stub
			int i = 0;
		//	notifySwitchStart();
			InternalSetSpeaker(_context, true);

			updateStatus();
		//	notifySwitchEnd();
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, " _run:" + getDeviceName()
						+ " _running:" + _running);
			while (_running == true) {

				if (_am.isSpeakerphoneOn() != true) {
					// if(QLog.isColorLevel())
					// QLog.w("TRAE",QLog.CLR," _run:"+getDeviceName()+" _running:"+_running+" i:"+i);
					InternalSetSpeaker(_context, true);
				} else {
					// break;
				}
				try {
					Thread.sleep(i < 5 ? 1000 : 4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				i++;
			}
			// if(QLog.isColorLevel())
			// QLog.w("TRAE",QLog.CLR," _run:"+getDeviceName()+" _running:"+_running+" exit");
		}

		@Override
		public String getDeviceName() {
			// TODO Auto-generated method stub
			return DEVICE_SPEAKERPHONE;
		}

		@Override
		public void _quit() {
			// TODO Auto-generated method stub
			// InternalSetSpeaker(_context,false);
		}

	}

	class headsetSwitchThread extends switchThread {
		@Override
		public void _run() {
			// TODO Auto-generated method stub
			int i = 0;
		//	notifySwitchStart();
			InternalSetSpeaker(_context, false);
			_am.setWiredHeadsetOn(true);

			updateStatus();
		//	notifySwitchEnd();
			while (_running == true) {
				if (_am.isSpeakerphoneOn() != false) {
					InternalSetSpeaker(_context, false);
				} else {
					// break;
				}
				try {
					Thread.sleep(i < 5 ? 1000 : 4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				i++;
			}
		}

		@Override
		public String getDeviceName() {
			// TODO Auto-generated method stub
			return DEVICE_WIREDHEADSET;
		}

		@Override
		public void _quit() {
			// TODO Auto-generated method stub
			// _am.setWiredHeadsetOn(false);
		}

	}

	class bluetoothHeadsetSwitchThread extends switchThread {
		// boolean[] _startSco = new boolean[] { false };

		// Handler _btHandler = null;
		@Override
		public void _run() {
			// TODO Auto-generated method stub
			// InternalSetSpeaker(_context,false); never do it!
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

			// for test

			// _am.setMode(AudioManager.MODE_IN_COMMUNICATION);
			// if(!_am.isBluetoothScoOn())GALAXY Gear
			boolean skipBT =false;
			
			skipBT = (_deviceConfigManager.getBluetoothName().indexOf("Gear")!=-1)?true:false;
			//if (QLog.isColorLevel() )QLog.e("TRAE", QLog.CLR," indexOf:"+_deviceConfigManager.getBluetoothName().indexOf("Gear"));
			
			if(!skipBT){
		//		notifySwitchStart();
				_startBluetoothSco();
			}
			int i = 0;
			while (_running == true && i++ < 10 && !skipBT) {
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "bluetoothHeadsetSwitchThread i:"
							+ i + " sco:"
							+ (_am.isBluetoothScoOn() ? "Y" : "N")+" :"+_deviceConfigManager.getBluetoothName());
				if (_am.isBluetoothScoOn()) {
					updateStatus();
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
			
			// fail
			// bt reset when check the event again

			if (_am.isBluetoothScoOn() == false) {
				if (QLog.isColorLevel() && !skipBT)QLog.e("TRAE", QLog.CLR,"bluetoothHeadsetSwitchThread sco fail,remove btheadset");

				_deviceConfigManager.setVisible(getDeviceName(), false);
				// if(_bluetoothCheck!=null)
				// _bluetoothCheck.setVisiable(false);
				processDeviceConnectRes(RES_ERRCODE_DEVICE_BTCONNCECTED_TIMEOUT);
				checkAutoDeviceListUpdate();
				// list update
				// _deviceConfigManager.setConnected(getDeviceName());
				// InternalNotifyDeviceConnectedUpdate();
				// InternalNotifyDeviceChangableUpdate();
				// break;
			}
		//	notifySwitchEnd();
			/*
			 * while(_running == true){ try { Thread.sleep(1000); } catch
			 * (InterruptedException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } if(QLog.isColorLevel())
			 * QLog.w("TRAE",QLog.CLR,"bluetoothHeadsetSwitchThread sco:"
			 * +(_am.isBluetoothScoOn()?"Y":"N")
			 * +" a2dp:"+(_am.isBluetoothA2dpOn()?"Y":"N"));
			 * 
			 * 
			 * }
			 */
		}

		@Override
		public String getDeviceName() {
			// TODO Auto-generated method stub
			return DEVICE_BLUETOOTHHEADSET;
		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
		public void _quit() {
			// TODO Auto-generated method stub
			// if(_btHandler==null)
			// return;

			// _btHandler.getLooper().quit();
			// _btHandler = null;
			if (_am == null)
				return;
			_stopBluetoothSco();
		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		void _startBluetoothSco() {
			_am.setBluetoothScoOn(true);
			if (android.os.Build.VERSION.SDK_INT > 8)
				_am.startBluetoothSco();
		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		void _stopBluetoothSco() {
			if (android.os.Build.VERSION.SDK_INT > 8)
				_am.stopBluetoothSco();
			_am.setBluetoothScoOn(false);
		}
	}

	int InternalSessionIsDeviceChangabled(HashMap<String, Object> params) {
		Intent intent = new Intent();
		intent.putExtra(ISDEVICECHANGABLED_RESULT_ISCHANGABLED,
				InternalIsDeviceChangeable());
		sendResBroadcast(intent, params, RES_ERRCODE_NONE);
		return 0;
	}

	boolean InternalIsDeviceChangeable() {
		String str = null;
		str = _deviceConfigManager.getConnectingDevice();
		if (str == null|| str.equals(DEVICE_NONE)||str.equals(""))
			return true;
		// _deviceConfigManager.getVisible(str);
		// if()
		return false;
	}

	int InternalSessionGetConnectedDevice(HashMap<String, Object> params) {
		Intent intent = new Intent();
		intent.putExtra(GETCONNECTEDDEVICE_RESULT_LIST,
				_deviceConfigManager.getConnectedDevice());
		sendResBroadcast(intent, params, RES_ERRCODE_NONE);
		return 0;
	}

	int InternalSessionGetConnectingDevice(HashMap<String, Object> params) {
		Intent intent = new Intent();
		intent.putExtra(GETCONNECTINGDEVICE_RESULT_LIST,
				_deviceConfigManager.getConnectingDevice());
		sendResBroadcast(intent, params, RES_ERRCODE_NONE);
		return 0;
	}

	int sendResBroadcast(Intent intent, HashMap<String, Object> params, int err) {
		if (_context == null)
			return -1;
		Long sid = (Long) params.get(PARAM_SESSIONID);
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, " sessonID:" + sid + " " + (String) params.get(PARAM_OPERATION));
		if (sid == null || sid == Long.MIN_VALUE) {
			InternalNotifyDeviceListUpdate();
			if (QLog.isColorLevel())
				QLog.e("TRAE", QLog.CLR,
						"sendResBroadcast sid null,don't send res");
			return -1;
		}

		intent.setAction(ACTION_TRAEAUDIOMANAGER_RES);
		intent.putExtra(PARAM_SESSIONID, (Long) params.get(PARAM_SESSIONID));
		intent.putExtra(PARAM_OPERATION, (String) params.get(PARAM_OPERATION));
		intent.putExtra(PARAM_RES_ERRCODE, err);
		_context.sendBroadcast(intent);
		return 0;
	}

	int InternalNotifyDeviceListUpdate() {
		AudioDeviceInterface.LogTraceEntry("");
		if (null == _context) {
			return -1;
		}

		
	//	if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InternalNotifyDeviceListUpdate "+Log.getStackTraceString(new Throwable()));
		
		HashMap<String, Object> params = _deviceConfigManager.getSnapParams();
		ArrayList<String> list = (ArrayList<String>) params
				.get(EXTRA_DATA_AVAILABLEDEVICE_LIST);
		String con = (String) params.get(EXTRA_DATA_CONNECTEDDEVICE);
		String prevCon = (String) params.get(EXTRA_DATA_PREV_CONNECTEDDEVICE);

		Intent intent = new Intent();
		intent.setAction(ACTION_TRAEAUDIOMANAGER_NOTIFY);
		// intent.putExtra(PARAM_SESSIONID, (Long)params.get(PARAM_SESSIONID));
		intent.putExtra(PARAM_OPERATION, NOTIFY_DEVICELIST_UPDATE);
		intent.putExtra(EXTRA_DATA_AVAILABLEDEVICE_LIST,
				list.toArray(new String[0]));
		intent.putExtra(EXTRA_DATA_CONNECTEDDEVICE, con);
		intent.putExtra(EXTRA_DATA_PREV_CONNECTEDDEVICE, prevCon);
		intent.putExtra(EXTRA_DATA_IF_HAS_BLUETOOTH_THIS_IS_NAME,
				_deviceConfigManager.getBluetoothName());

		_context.sendBroadcast(intent);
		AudioDeviceInterface.LogTraceExit();
		return 0;
	}

	int InternalNotifyDeviceChangableUpdate() {
		if (null == _context) {
			return -1;
		}
		// boolean bChangabled =
		// _deviceConfigManager.getConnectingDeviceList().size() > 0;
		Intent intent = new Intent();
		intent.setAction(ACTION_TRAEAUDIOMANAGER_NOTIFY);
		// intent.putExtra(PARAM_SESSIONID, (Long)params.get(PARAM_SESSIONID));
		intent.putExtra(PARAM_OPERATION, NOTIFY_DEVICECHANGABLE_UPDATE);
		intent.putExtra(NOTIFY_DEVICECHANGABLE_UPDATE_DATE,
				InternalIsDeviceChangeable());
		_context.sendBroadcast(intent);

		return 0;
	}

	abstract class BluetoohHeadsetCheckInterface {
		// boolean _visiable = true;
		public abstract String interfaceDesc();

		public abstract boolean init(Context ctx, DeviceConfigManager devCfg);

		public abstract void release();

		public abstract boolean isConnected();

		public void addAction(IntentFilter filter) {
			filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			// filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
			// filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

			_addAction(filter);
		}

		abstract void _addAction(IntentFilter filter);

		abstract void _onReceive(Context context, Intent intent);

		public void onReceive(Context context, Intent intent,
				DeviceConfigManager devCfg) {

			// TODO Auto-generated method stub
			// if(QLog.isColorLevel())
			// QLog.w("TRAE",QLog.CLR,"BluetoohHeadsetCheckInterface onReceive:: extras:"+intent.getExtras());
			// if(QLog.isColorLevel())
			// QLog.w("TRAE",QLog.CLR," "+interfaceDesc()+" onReceive:"+intent.getAction());
			if (BluetoothAdapter.ACTION_STATE_CHANGED
					.equals(intent.getAction())) {
				int bt_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						-1);
				int bt_pre_state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);

				if (QLog.isColorLevel())
					QLog.w("TRAE",
							QLog.CLR,
							"BT ACTION_STATE_CHANGED|   EXTRA_STATE "
									+ getBTActionStateChangedExtraString(bt_state));
				if (QLog.isColorLevel())
					QLog.w("TRAE",
							QLog.CLR,
							"BT ACTION_STATE_CHANGED|   EXTRA_PREVIOUS_STATE "
									+ getBTActionStateChangedExtraString(bt_pre_state));

				if (bt_state == BluetoothAdapter.STATE_OFF) {
					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR, "    BT off");
					// _devConnected = false;
					// _btloop.sendmsg(TRAE_BT_STOP);
					// setVisiable(true);
					devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, false);
				} else if (bt_state == BluetoothAdapter.STATE_ON) {
					// setVisiable(true);
					// if(!isVisiable()){
					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR, "BT OFF-->ON,Visiable it...");
					// setVisiable(true);
					// }
				}
			} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent
					.getAction()) && android.os.Build.VERSION.SDK_INT < 11) {
				// _devConnected = true;
				// if(QLog.isColorLevel())
				// QLog.w("TRAE",QLog.CLR,"    startBT after 3 sec,maybe retry...");
				// _btloop.sendmsg(TRAE_BT_START_DELAY_ACL);
				// devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, true);
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent
					.getAction()) && android.os.Build.VERSION.SDK_INT < 11) {
				// _devConnected = false;
				// _btloop.sendmsg(TRAE_BT_STOP);
				// devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, false);
			} else {
				_onReceive(context, intent);
			}

		}

		String getBTActionStateChangedExtraString(int state) {
			String valuestr;
			switch (state) {
			case BluetoothAdapter.STATE_OFF:
				valuestr = "STATE_OFF";
				break;
			case BluetoothAdapter.STATE_TURNING_ON:
				valuestr = "STATE_TURNING_ON";
				break;
			case BluetoothAdapter.STATE_ON:
				valuestr = "STATE_ON";
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				valuestr = "STATE_TURNING_OFF";
				break;
			default:
				valuestr = "unknow";
				break;
			}
			return valuestr + ":" + state;
		}

		String getSCOAudioStateExtraString(int state) {
			String valuestr;
			switch (state) {
			case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
				valuestr = "SCO_AUDIO_STATE_DISCONNECTED";
				break;
			case AudioManager.SCO_AUDIO_STATE_CONNECTED:
				valuestr = "SCO_AUDIO_STATE_CONNECTED";
				break;
			case AudioManager.SCO_AUDIO_STATE_CONNECTING:
				valuestr = "SCO_AUDIO_STATE_CONNECTING";
				break;
			case AudioManager.SCO_AUDIO_STATE_ERROR:
				valuestr = "SCO_AUDIO_STATE_ERROR";
				break;
			default:
				valuestr = "unknow";
				break;
			}
			return valuestr + ":" + state;
		}

		String getBTAdapterConnectionState(int state) {
			String valuestr;
			switch (state) {
			case BluetoothAdapter.STATE_DISCONNECTED:
				valuestr = "STATE_DISCONNECTED";
				break;
			case BluetoothAdapter.STATE_CONNECTING:
				valuestr = "STATE_CONNECTING";
				break;
			case BluetoothAdapter.STATE_CONNECTED:
				valuestr = "STATE_CONNECTED";
				break;
			case BluetoothAdapter.STATE_DISCONNECTING:
				valuestr = "STATE_DISCONNECTING";
				break;
			default:
				valuestr = "unknow";
				break;
			}
			return valuestr + ":" + state;
		}

		// STATE_DISCONNECTED, STATE_CONNECTING, STATE_CONNECTED,
		// STATE_DISCONNECTING.
		// STATE_DISCONNECTED, STATE_CONNECTING, STATE_CONNECTED,
		// STATE_DISCONNECTING
		String getBTHeadsetConnectionState(int state) {
			String valuestr;
			switch (state) {
			case BluetoothHeadset.STATE_DISCONNECTED:
				valuestr = "STATE_DISCONNECTED";
				break;
			case BluetoothHeadset.STATE_CONNECTING:
				valuestr = "STATE_CONNECTING";
				break;
			case BluetoothHeadset.STATE_CONNECTED:
				valuestr = "STATE_CONNECTED";
				break;
			case BluetoothHeadset.STATE_DISCONNECTING:
				valuestr = "STATE_DISCONNECTING";
				break;
			default:
				valuestr = "unknow";
				break;
			}
			return valuestr + ":" + state;
		}

		// STATE_AUDIO_CONNECTED, STATE_AUDIO_DISCONNECTED

		String getBTHeadsetAudioState(int state) {
			String valuestr;
			switch (state) {
			case BluetoothHeadset.STATE_AUDIO_CONNECTED:
				valuestr = "STATE_AUDIO_CONNECTED";
				break;
			case BluetoothHeadset.STATE_AUDIO_DISCONNECTED:
				valuestr = "STATE_AUDIO_DISCONNECTED";
				break;
			default:
				valuestr = "unknow:" + state;
				break;
			}
			return valuestr + ":" + state;
		}
	}

	class BluetoohHeadsetCheckFake extends BluetoohHeadsetCheckInterface {

		@Override
		public boolean init(Context ctx, DeviceConfigManager devCfg) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void release() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isConnected() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		void _addAction(IntentFilter filter) {
			// TODO Auto-generated method stub

		}

		@Override
		void _onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

		}

		@Override
		public String interfaceDesc() {
			// TODO Auto-generated method stub
			return "BluetoohHeadsetCheckFake";
		}

	}

	public BluetoohHeadsetCheckInterface CreateBluetoothCheck(Context context,
			DeviceConfigManager devCfg) {
		BluetoohHeadsetCheckInterface intf = null;
		// android4.3 bluetooth有bug，暂时屏蔽 &&android.os.Build.VERSION.SDK_INT!=18
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			intf = new BluetoohHeadsetCheck();
		} else if (android.os.Build.VERSION.SDK_INT != 18) {
			intf = new BluetoohHeadsetCheckFor2x();
		} else {
			intf = new BluetoohHeadsetCheckFake();
		}

		if (!intf.init(context, devCfg)) {
			intf = new BluetoohHeadsetCheckFake();
		}
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR,
					"CreateBluetoothCheck:"
							+ intf.interfaceDesc()
							+ " skip android4.3:"
							+ (android.os.Build.VERSION.SDK_INT == 18 ? "Y"
									: "N"));
		return intf;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	class BluetoohHeadsetCheck extends BluetoohHeadsetCheckInterface implements
			BluetoothProfile.ServiceListener {
		Context _ctx = null;
		DeviceConfigManager _devCfg = null;
		BluetoothAdapter _adapter = null;
		BluetoothProfile _profile = null;

		// boolean _getProfile = false;
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public boolean init(Context ctx, DeviceConfigManager devCfg) {
			AudioDeviceInterface.LogTraceEntry("");
			// TODO Auto-generated method stub
			if (ctx == null || devCfg == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " err ctx==null||_devCfg==null");
				return false;
			}

			_ctx = ctx;
			_devCfg = devCfg;
			_adapter = BluetoothAdapter.getDefaultAdapter();
			if (_adapter == null) {
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR, " err getDefaultAdapter fail!");
				return false;
			}

			if (_adapter.isEnabled() && _profile == null) {
				if (_adapter.getProfileProxy(_ctx, this,
						BluetoothProfile.HEADSET) == false) {
					if (QLog.isColorLevel())
						QLog.e("TRAE", QLog.CLR,
								"BluetoohHeadsetCheck: getProfileProxy HEADSET fail!");
					// _context.unregisterReceiver(this);
					return false;
				}
			}
			AudioDeviceInterface.LogTraceExit();
			return true;
		}

		@Override
		public void release() {
			AudioDeviceInterface.LogTraceEntry("_profile:" + _profile);
			// TODO Auto-generated method stub
			try {
				if (_adapter != null) {
					if (_profile != null)
						_adapter.closeProfileProxy(BluetoothProfile.HEADSET,
								_profile);
					_profile = null;

				}
			} catch (Exception e) {
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR,
							" closeProfileProxy:e:" + e.getMessage());
			}
			AudioDeviceInterface.LogTraceExit();
		}

		@Override
		public boolean isConnected() {
			// TODO Auto-generated method stub
			boolean bc = false;
			if (_profile != null) {
				List<BluetoothDevice> devs = _profile.getConnectedDevices();
				if(devs==null)
					return false;
				bc = devs.size() > 0 ? true : false;
			}

			return bc;
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			// TODO Auto-generated method stub
			AudioDeviceInterface.LogTraceEntry("_profile:" + _profile
					+ " profile:" + profile + " proxy:" + proxy);
			if (profile == BluetoothProfile.HEADSET) {

				if (_profile != null && _profile != proxy) {
					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR,
								"BluetoohHeadsetCheck: HEADSET Connected proxy:"
										+ proxy + " _profile:" + _profile);
					_adapter.closeProfileProxy(BluetoothProfile.HEADSET,
							_profile);
					_profile = null;
				}

				_profile = proxy;
				List<BluetoothDevice> devs = _profile.getConnectedDevices();
				if(devs!=null){
					

					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR,
								"TRAEBluetoohProxy: HEADSET Connected devs:"
										+ devs.size() + " _profile:" + _profile);

					for (int i = 0; i < devs.size(); i++) {
						BluetoothDevice d;
						d = devs.get(i);
						int state = _profile.getConnectionState(d);

						if (state == BluetoothAdapter.STATE_CONNECTED)
							_devCfg.setBluetoothName(d.getName());
						if (QLog.isColorLevel())
							QLog.w("TRAE", QLog.CLR,
									"   " + i + " " + d.getName()
											+ " ConnectionState:" + state);

					}
				}
				if (_devCfg != null){
				//if(_deviceConfigManager.getBluetoothName().indexOf("Gear")==-1){
					String bluetoothName = null;
					if (_deviceConfigManager != null){
						bluetoothName = _deviceConfigManager.getBluetoothName();
					}
					//如果获取不到蓝牙设备的名称，应该disable掉
					if (TextUtils.isEmpty(bluetoothName)){
						_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET,false);
					}else if (isConnected() && bluetoothName.indexOf("Gear")==-1) {
					_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET,true);
					checkDevicePlug(DEVICE_BLUETOOTHHEADSET, true);
				}else{
					_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET,false);
					}
				}

			}
			AudioDeviceInterface.LogTraceExit();
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onServiceDisconnected(int profile) {
			AudioDeviceInterface.LogTraceEntry("_profile:" + _profile
					+ " profile:" + profile);

			// TODO Auto-generated method stub
			if (profile == BluetoothProfile.HEADSET) {
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR,
							"TRAEBluetoohProxy: HEADSET Disconnected");
				if (isConnected()) {
					checkDevicePlug(DEVICE_BLUETOOTHHEADSET, false);
				}

				if (_profile != null) {
					_adapter.closeProfileProxy(BluetoothProfile.HEADSET,
							_profile);

					_profile = null;

				}
			}
			AudioDeviceInterface.LogTraceExit();
		}

		@Override
		void _addAction(IntentFilter filter) {
			// TODO Auto-generated method stub
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, " " + interfaceDesc() + " _addAction");
			filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
			filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
		}

		@Override
		void _onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int conn_state = -1;
			int conn_pre_state = -1;
			BluetoothDevice dev = null;
			int sco_state = -1;
			int sco_pre_state = -1;
			// if(QLog.isColorLevel())QLog.w("TRAE",QLog.CLR," "+interfaceDesc()+" _onReceive:"+intent.getAction());
			if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent
					.getAction())) {
				conn_state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
				conn_pre_state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);
				dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR,
							"BT ACTION_CONNECTION_STATE_CHANGED|   EXTRA_CONNECTION_STATE "
									+ getBTAdapterConnectionState(conn_state));
				if (QLog.isColorLevel())
					QLog.w("TRAE",
							QLog.CLR,
							"    EXTRA_PREVIOUS_CONNECTION_STATE "
									+ getBTAdapterConnectionState(conn_pre_state));
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "    EXTRA_DEVICE " + dev + " "
							+ (dev != null ? dev.getName() : " "));

				if (conn_state == BluetoothAdapter.STATE_CONNECTED) {
					if (QLog.isColorLevel())
						QLog.w("TRAE", QLog.CLR, "   dev:" + dev.getName()
								+ " connected,start sco...");
					// _devConnected = true;
					// _btloop.sendmsg(TRAE_BT_START_DELAY_NORMAL);
					_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, true);
					_devCfg.setBluetoothName(dev != null ? dev.getName()
							: "unkown");
				} else if (conn_state == BluetoothAdapter.STATE_DISCONNECTED) {
					// if(QLog.isColorLevel())
					// QLog.w("TRAE",QLog.CLR,"   dev:"+dev.getName()+" disconnected _devConnected:"+_devConnected);
					// _devConnected = false;
					// _btloop.sendmsg(TRAE_BT_STOP);
					_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, false);
				}

			} else if (AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED
					.equals(intent.getAction())) {
				sco_state = intent.getIntExtra(
						AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
				sco_pre_state = intent.getIntExtra(
						AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE, -1);
				dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR,
							"BT ACTION_SCO_AUDIO_STATE_UPDATED|   EXTRA_CONNECTION_STATE "
									+ " dev:" + dev);
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "   EXTRA_SCO_AUDIO_STATE "
							+ getSCOAudioStateExtraString(sco_state));
				if (QLog.isColorLevel())
					QLog.w("TRAE",
							QLog.CLR,
							"   EXTRA_SCO_AUDIO_PREVIOUS_STATE "
									+ getSCOAudioStateExtraString(sco_pre_state));
			}
		}

		@Override
		public String interfaceDesc() {
			// TODO Auto-generated method stub
			return "BluetoohHeadsetCheck";
		}

	}

	class BluetoohHeadsetCheckFor2x extends BluetoohHeadsetCheckInterface {
		// for 2.x-
		static public final String ACTION_BLUETOOTHHEADSET_AUDIO_STATE_CHANGED = "android.bluetooth.headset.action.AUDIO_STATE_CHANGED";
		static public final String ACTION_BLUETOOTHHEADSET_STATE_CHANGED = "android.bluetooth.headset.action.STATE_CHANGED";
		static final int STATE_CONNECTED = 0x00000002;
		static final int STATE_DISCONNECTED = 0x00000000;

		public static final int AUDIO_STATE_DISCONNECTED = 0;
		public static final int AUDIO_STATE_CONNECTED = 1;
		Class<?> BluetoothHeadsetClass = null;
		Class<?> ListenerClass = null;
		Object BluetoothHeadsetObj = null;
		Method getCurrentHeadsetMethod = null;

		Context _ctx = null;
		DeviceConfigManager _devCfg = null;

		@Override
		public boolean init(Context ctx, DeviceConfigManager devCfg) {
			AudioDeviceInterface.LogTraceEntry("");
			Object res = null;
			_ctx = ctx;
			_devCfg = devCfg;
			if (_ctx == null || _devCfg == null)
				return false;
			try {
				BluetoothHeadsetClass = Class
						.forName("android.bluetooth.BluetoothHeadset");
			} catch (Exception e) {
				// e.printStackTrace();
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							"BTLooperThread BluetoothHeadset class not found");
			}

			if (BluetoothHeadsetClass == null) {
				// if(QLog.isColorLevel())
				// QLog.w("TRAE",QLog.CLR,"BTLooperThread BluetoothHeadset class  not exit");
				return false;
			}
			/*
			 * try { ListenerClass=Class.forName(
			 * "android.bluetooth.BluetoothHeadset.ServiceListener"); } catch
			 * (ClassNotFoundException e) { // TODO Auto-generated catch block
			 * //e2.printStackTrace(); if(QLog.isColorLevel())
			 * QLog.w("TRAE",QLog.CLR,
			 * "BTLooperThread BluetoothHeadset.ServiceListener class NotFoundException:"
			 * +e); }
			 */
			try {
				ListenerClass = Class
						.forName("android.bluetooth.BluetoothHeadset$ServiceListener");
			} catch (Exception e) {
				// e.printStackTrace();
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							"BTLooperThread BluetoothHeadset.ServiceListener class not found:"
									+ e);
			}

			if (ListenerClass == null) {
				// if(QLog.isColorLevel())
				// QLog.w("TRAE",QLog.CLR,"BTLooperThread BluetoothHeadset class  not exit");
				// return false;
			}
			// public BluetoothDevice More ...getCurrentHeadset()
			try {
				getCurrentHeadsetMethod = BluetoothHeadsetClass
						.getDeclaredMethod("getCurrentHeadset");
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				if (QLog.isColorLevel())
					QLog.e("TRAE",
							QLog.CLR,
							"BTLooperThread BluetoothHeadset method getCurrentHeadset NoSuchMethodException");
			}

			if (getCurrentHeadsetMethod == null) {
				return false;
			}
			try {
				// BluetoothHeadsetObj =BluetoothHeadsetClass.
				BluetoothHeadsetObj = BluetoothHeadsetClass.getConstructor(
						Context.class, ListenerClass).newInstance(ctx, null);
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							"BTLooperThread BluetoothHeadset getConstructor IllegalArgumentException");
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							"BTLooperThread BluetoothHeadset getConstructor InstantiationException");
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							"BTLooperThread BluetoothHeadset getConstructor IllegalAccessException");
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							"BTLooperThread BluetoothHeadset getConstructor InvocationTargetException");
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							"BTLooperThread BluetoothHeadset getConstructor NoSuchMethodException");
			}
			// try {
			// BluetoothHeadsetObj
			// =BluetoothHeadsetClass.getConstructor(Context.class,ListenerClass).newInstance(ctx,null);

			// } catch (Exception e) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
			// if(QLog.isColorLevel())
			// QLog.w("TRAE",QLog.CLR,"BTLooperThread BluetoothHeadset getConstructor Exception:"+e.getMessage()+" "+e.getLocalizedMessage());
			// }

			if (BluetoothHeadsetObj == null) {
				return false;
			}

			_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, isConnected());

			if (isConnected()) {
				_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, true);
				checkDevicePlug(DEVICE_BLUETOOTHHEADSET, true);
			} else {
				_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, false);
			}
			AudioDeviceInterface.LogTraceExit();
			return true;
		}

		@Override
		public void release() {
			AudioDeviceInterface.LogTraceEntry("");
			/*
			 * Class<?> BluetoothHeadsetClass=null; Class<?> ListenerClass =
			 * null; Object BluetoothHeadsetObj = null; Method
			 * getCurrentHeadsetMethod=null;
			 */
			Method m = null;
			if (BluetoothHeadsetObj == null)
				return;
			try {
				m = BluetoothHeadsetClass.getDeclaredMethod("close");
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				if (QLog.isColorLevel())
					QLog.e("TRAE", QLog.CLR,
							"BTLooperThread _uninitHeadsetfor2x method close NoSuchMethodException");
			}
			if (m == null)
				return;
			try {
				m.invoke(BluetoothHeadsetObj);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

			BluetoothHeadsetClass = null;
			ListenerClass = null;
			BluetoothHeadsetObj = null;
			getCurrentHeadsetMethod = null;
			AudioDeviceInterface.LogTraceExit();
		}

		@Override
		public boolean isConnected() {
			Object res = null;
			// BluetoothDevice dev = null;
			if (getCurrentHeadsetMethod == null
					|| getCurrentHeadsetMethod == null)
				return false;

			try {
				res = getCurrentHeadsetMethod.invoke(BluetoothHeadsetObj);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				if (QLog.isColorLevel())
					QLog.w("TRAE",
							QLog.CLR,
							"BTLooperThread BluetoothHeadset method getCurrentHeadset IllegalArgumentException");
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				if (QLog.isColorLevel())
					QLog.w("TRAE",
							QLog.CLR,
							"BTLooperThread BluetoothHeadset method getCurrentHeadset IllegalAccessException");
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				if (QLog.isColorLevel())
					QLog.w("TRAE",
							QLog.CLR,
							"BTLooperThread BluetoothHeadset method getCurrentHeadset InvocationTargetException");
			}
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"BTLooperThread BluetoothHeadset method getCurrentHeadset res:"
								+ (res != null ? " Y" : "N"));
			return res != null ? true : false;
		}

		@Override
		void _addAction(IntentFilter filter) {
			// TODO Auto-generated method stub
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, " " + interfaceDesc() + " _addAction");
			filter.addAction(ACTION_BLUETOOTHHEADSET_AUDIO_STATE_CHANGED);
			filter.addAction(ACTION_BLUETOOTHHEADSET_STATE_CHANGED);
		}

		@Override
		void _onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int _state = -1;
			int _pre_state = -1;
			int _audio_state = -1;
			// BluetoothDevice dev = null;
			// if(QLog.isColorLevel())
			// QLog.w("TRAE",QLog.CLR," "+interfaceDesc()+" _onReceive:"+intent.getAction());
			if (ACTION_BLUETOOTHHEADSET_AUDIO_STATE_CHANGED.equals(intent
					.getAction())) {
				_state = intent.getIntExtra(
						"android.bluetooth.headset.extra.STATE", -2);
				_pre_state = intent.getIntExtra(
						"android.bluetooth.headset.extra.PREVIOUS_STATE", -2);
				_audio_state = intent.getIntExtra(
						"android.bluetooth.headset.extra.AUDIO_STATE", -2);

				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "++ AUDIO_STATE_CHANGED|  STATE "
							+ _state);
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "       PREVIOUS_STATE "
							+ _pre_state);
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "       AUDIO_STATE "
							+ _audio_state);

				if (_audio_state == STATE_CONNECTED) {
					// if(QLog.isColorLevel())
					// QLog.w("TRAE",QLog.CLR,"bt headset connected,start sco...");
					_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, true);
					// _devConnected = true;
					// _btloop.sendmsg(TRAE_BT_START_DELAY_NORMAL);
				} else if (_audio_state == STATE_DISCONNECTED) {
					// if(QLog.isColorLevel())
					// QLog.w("TRAE",QLog.CLR,"bt headset disconnected "+_devConnected);
					// _devConnected = false;
					// _btloop.sendmsg(TRAE_BT_STOP);
					_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, false);
				}

			} else if (ACTION_BLUETOOTHHEADSET_STATE_CHANGED.equals(intent
					.getAction())) {
				_state = intent.getIntExtra(
						"android.bluetooth.headset.extra.STATE", -2);
				_pre_state = intent.getIntExtra(
						"android.bluetooth.headset.extra.PREVIOUS_STATE", -2);
				_audio_state = intent.getIntExtra(
						"android.bluetooth.headset.extra.AUDIO_STATE", -2);

				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "++ STATE_CHANGED|  STATE "
							+ _state);
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "       PREVIOUS_STATE "
							+ _pre_state);
				if (QLog.isColorLevel())
					QLog.w("TRAE", QLog.CLR, "       AUDIO_STATE "
							+ _audio_state);

				if (_audio_state == STATE_CONNECTED) {
					// if(QLog.isColorLevel())
					// QLog.w("TRAE",QLog.CLR,"bt headset connected,start sco...");
					_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, true);
					// _devConnected = true;
					// _btloop.sendmsg(TRAE_BT_START_DELAY_NORMAL);
				} else if (_audio_state == STATE_DISCONNECTED) {
					// if(QLog.isColorLevel())
					// QLog.w("TRAE",QLog.CLR,"bt headset disconnected "+_devConnected);
					// _devConnected = false;
					// _btloop.sendmsg(TRAE_BT_STOP);
					_devCfg.setVisible(DEVICE_BLUETOOTHHEADSET, false);
				}
			}
		}

		@Override
		public String interfaceDesc() {
			// TODO Auto-generated method stub
			return "BluetoohHeadsetCheckFor2x";
		}
	}

	// device categories config for setForceUse, must match
	// AudioSystem::forced_config
	public static final int FORCE_NONE = 0;
	public static final int FORCE_SPEAKER = 1;
	public static final int FORCE_HEADPHONES = 2;
	public static final int FORCE_BT_SCO = 3;
	public static final int FORCE_BT_A2DP = 4;
	public static final int FORCE_WIRED_ACCESSORY = 5;
	public static final int FORCE_BT_CAR_DOCK = 6;
	public static final int FORCE_BT_DESK_DOCK = 7;
	public static final int FORCE_ANALOG_DOCK = 8;
	public static final int FORCE_DIGITAL_DOCK = 9;
	public static final int FORCE_NO_BT_A2DP = 10;
	private static final int NUM_FORCE_CONFIG = 11;
	public static final int FORCE_DEFAULT = FORCE_NONE;

	// usage for setForceUse, must match AudioSystem::force_use
	public static final int FOR_COMMUNICATION = 0;
	public static final int FOR_MEDIA = 1;
	public static final int FOR_RECORD = 2;
	public static final int FOR_DOCK = 3;
	private static final int NUM_FORCE_USE = 4;

	static final String forceName[] = { "FORCE_NONE", "FORCE_SPEAKER",
			"FORCE_HEADPHONES", "FORCE_BT_SCO", "FORCE_BT_A2DP",
			"FORCE_WIRED_ACCESSORY", "FORCE_BT_CAR_DOCK", "FORCE_BT_DESK_DOCK",
			"FORCE_ANALOG_DOCK", "FORCE_NO_BT_A2DP", "FORCE_DIGITAL_DOCK" };

	static String getForceConfigName(int config) {
		if (config >= 0 && config < forceName.length)
			return forceName[config];
		return "unknow";
	}

	static public Object invokeMethod(Object owner, String methodName,
			Object[] args, Class[] argsClass) {
		Object res = null;

		try {
			Class ownerClass = owner.getClass();
			/*
			 * Class[] argsClass = new Class[args.length];
			 * 
			 * for (int i = 0, j = args.length; i < j; i++) { argsClass[i] =
			 * args[i].getClass(); }
			 */

			Method method = ownerClass.getMethod(methodName, argsClass);
			res = method.invoke(owner, args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"invokeMethod Exception:" + e.getMessage());
		}

		return res;
	}

	static public Object invokeStaticMethod(String className,
			String methodName, Object[] args, Class[] argsClass) {
		Object res = null;
		try {
			Class ownerClass = Class.forName(className);
			// Class[] argsClass = new Class[args.length];

			// for (int i = 0, j = args.length; i < j; i++) {
			// argsClass[i] = args[i].getClass();
			// }

			Method method = ownerClass.getMethod(methodName, argsClass);

			res = method.invoke(null, args);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, "ClassNotFound:" + className);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, "NoSuchMethod:" + methodName);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, "IllegalArgument:" + methodName);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, "IllegalAccess:" + methodName);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR, "InvocationTarget:" + methodName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (QLog.isColorLevel())
				QLog.w("TRAE", QLog.CLR,
						"invokeStaticMethod Exception:" + e.getMessage());
		}

		return res;
	}

	static void setParameters(String keyValuePairs) {
		Object[] args = new Object[1];
		args[0] = keyValuePairs;// state;
		Class[] argsClass = new Class[args.length];
		argsClass[0] = String.class;
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "setParameters  :" + keyValuePairs);

		invokeStaticMethod("android.media.AudioSystem", "setParameters", args,
				argsClass);
	}

	static void setPhoneState(int state) {
		Object[] args = new Object[1];
		args[0] = state;// state;
		Class[] argsClass = new Class[args.length];
		argsClass[0] = int.class;
		invokeStaticMethod("android.media.AudioSystem", "setPhoneState", args,
				argsClass);
	}

	static void setForceUse(int usage, int config) {
		Object[] args = new Object[2];
		args[0] = Integer.valueOf(usage);
		args[1] = Integer.valueOf(config);
		Class[] argsClass = new Class[args.length];
		argsClass[0] = int.class;
		argsClass[1] = int.class;

		Object res = invokeStaticMethod("android.media.AudioSystem",
				"setForceUse", args, argsClass);
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "setForceUse  usage:" + usage + " config:"
					+ config + " ->" + getForceConfigName(config) + " res:"
					+ res);

	}

	static int getForceUse(int usage) {
		Integer config = 0;
		Object[] args = new Object[1];
		args[0] = usage;
		Class[] argsClass = new Class[args.length];
		argsClass[0] = int.class;
		Object value = invokeStaticMethod("android.media.AudioSystem",
				"getForceUse", args, argsClass);
		if (value != null)
			config = (Integer) value;
		// getForceConfigName
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "getForceUse  usage:" + usage + " config:"
					+ config + " ->" + getForceConfigName(config));
		return config;
	}

	static void forceVolumeControlStream(AudioManager am, int streamType) {
		Object[] args = new Object[1];
		args[0] = Integer.valueOf(streamType);
		Class[] argsClass = new Class[args.length];
		argsClass[0] = int.class;

		Object res = invokeMethod(am, "forceVolumeControlStream", args,
				argsClass);
		if (QLog.isColorLevel())
			QLog.w("TRAE", QLog.CLR, "forceVolumeControlStream  streamType:"
					+ streamType + " res:" + res);

	}
	/*
	 * static void forceVolumeControlStream(AudioManager am,int streamType){
	 * Class audioManagerClass=AudioManager.class;
	 * 
	 * Method m=null;
	 * 
	 * try { if(audioManagerClass!=null) m =
	 * audioManagerClass.getMethod("forceVolumeControlStream", int.class); }
	 * catch (NoSuchMethodException e) { // TODO Auto-generated catch block
	 * //e.printStackTrace(); if(QLog.isColorLevel())
	 * QLog.w("TRAE",QLog.CLR,"forceVolumeControlStream NoSuchMethod"); }
	 * if(m!=null){ try { m.invoke(am,streamType); if(QLog.isColorLevel())
	 * QLog.w("TRAE",QLog.CLR,"forceVolumeControlStream invoke :"+streamType); }
	 * catch (IllegalArgumentException e) { // TODO Auto-generated catch block
	 * //e.printStackTrace(); if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,
	 * "forceVolumeControlStream invoke IllegalArgumentException:"+streamType);
	 * } catch (IllegalAccessException e) { // TODO Auto-generated catch block
	 * //e.printStackTrace(); if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,
	 * "forceVolumeControlStream invoke IllegalAccessException:"+streamType); }
	 * catch (InvocationTargetException e) { // TODO Auto-generated catch block
	 * //e.printStackTrace(); if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,
	 * "forceVolumeControlStream invoke InvocationTargetException:"+streamType);
	 * } } return; }
	 */
}
