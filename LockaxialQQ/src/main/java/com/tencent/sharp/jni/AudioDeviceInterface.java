/*********************************************
 Created on 2013-01-24
 @filename: 
 @author:   alvinma
 **********************************************/

package com.tencent.sharp.jni;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.tencent.av.VideoController;
import com.tencent.device.QLog;
import com.tencent.sharp.jni.TraeAudioSession.ITraeAudioCallback;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.SystemClock;

@TargetApi(16)
public class AudioDeviceInterface {
	// private int apiLevel = 4;
	/*
	 * private String[] versionNames[] = { {"unkown"}, {"BASE",
	 * "Constant Value: 1  (0x00000001) October 2008: The original, first, version of Android. Yay!"
	 * }, {"BASE_1_1",
	 * "Constant Value: 2  (0x00000002) February 2009: First Android update, officially called 1.1."
	 * }, {"CUPCAKE", "Constant Value: 3  (0x00000003) May 2009: Android 1.5."},
	 * {"DONUT",
	 * "Constant Value: 4  (0x00000004) September 2009: Android 1.6."},
	 * {"ECLAIR", "Constant Value: 5  (0x00000005) November 2009: Android 2.0"},
	 * {"ECLAIR_0_1",
	 * "Constant Value: 5  (0x00000005) December 2009: Android 2.0.1"},
	 * {"ECLAIR_MR1",
	 * "Constant Value: 7  (0x00000007) January 2010: Android 2.1},"}, {"FROYO",
	 * "Constant Value: 8  (0x00000008) June 2010: Android 2.2"},
	 * {"GINGERBREAD",
	 * "Constant Value: 9  (0x00000009) November 2010: Android 2.3"},
	 * {"GINGERBREAD_MR1",
	 * "Constant Value: 10 (0x0000000a) February 2011: Android 2.3.3."},
	 * {"HONEYCOMB",
	 * "Constant Value: 11 (0x0000000b) February 2011: Android 3.0."},
	 * {"HONEYCOMB_MR1",
	 * "Constant Value: 12 (0x0000000c) May 2011: Android 3.1."},
	 * {"HONEYCOMB_MR2",
	 * "Constant Value: 13 (0x0000000d) June 2011: Android 3.2."},
	 * {"ICE_CREAM_SANDWICH",
	 * "Constant Value: 14 (0x0000000e) October 2011: Android 4.0."},
	 * {"ICE_CREAM_SANDWICH_MR1",
	 * "Constant Value: 15 (0x0000000f) December 2011: Android 4.0.3."},
	 * {"JELLY_BEAN",
	 * "Constant Value: 16 (0x00000010) June 2012: Android 4.1."},
	 * {"JELLY_BEAN_MR1",
	 * "Constant Value: 17 (0x00000011) Android 4.2: Moar jelly beans!"}//,
	 * //{"JELLY_BEAN_MR2",
	 * "Constant Value: 18 (0x00000012) Android 4.3: Jelly Bean MR2, the revenge of the beans."
	 * }
	
	 * // };
	 */
	/*
	private String[] specialAudioSouceConfig[] = {
	// {"ChanghongC900","7"}

	};
	*/
//	private TraeAudioSession _traeAudioSession = null;
	private AudioTrack _audioTrack = null;
	private AudioRecord _audioRecord = null;
	private int _streamType = AudioManager.STREAM_VOICE_CALL;
	private int _playSamplerate = 8000;
	private int _audioSource = MediaRecorder.AudioSource.DEFAULT;
	private int _sessionId = 0;
	private Context _context = null;
	private int _modePolicy = -1;// -1:auto orther: value
	private int _audioSourcePolicy = -1;
	private int _audioStreamTypePolicy = -1;
//	private int _buletoothPolicy = -1;
	private AudioManager _audioManager = null;
	private ByteBuffer _playBuffer;
	private ByteBuffer _recBuffer;
	private byte[] _tempBufPlay;
	private byte[] _tempBufRec;

	private final ReentrantLock _playLock = new ReentrantLock();
	private final ReentrantLock _recLock = new ReentrantLock();

	private boolean _doPlayInit = true;
	private boolean _doRecInit = true;
	private boolean _isRecording = false;
	private boolean _isPlaying = false;

	private int _bufferedRecSamples = 0;
	private int _bufferedPlaySamples = 0;
	private int _playPosition = 0;

	private File _rec_dump = null;
	private File _play_dump = null;
	private FileOutputStream _rec_out = null;
	private FileOutputStream _play_out = null;

	private static boolean _dumpEnable = false;
	private static boolean _logEnable = true;

	public AudioDeviceInterface() {
		try {
			_playBuffer = ByteBuffer.allocateDirect(2 * 480); // Max 10 ms @ 48
																// kHz
			_recBuffer = ByteBuffer.allocateDirect(2 * 480); // Max 10 ms @ 48
																// kHz
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,e.getMessage());
		}

		_tempBufPlay = new byte[2 * 480];
		_tempBufRec = new byte[2 * 480];

		int apiLevel = android.os.Build.VERSION.SDK_INT;// Integer.parseInt(android.os.Build.VERSION.SDK);
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioDeviceInterface apiLevel:" + apiLevel);
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR," SDK_INT:" + android.os.Build.VERSION.SDK_INT);
		if (apiLevel <= 0) {
			apiLevel = 0;
		}

		/*
		 * if(apiLevel>versionNames.length){ apiLevel = versionNames.length-1;
		 * if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR," apiLevel unkown max:"+versionNames.length); }else{
		 * if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR," os:"+versionNames[apiLevel][0]);
		 * if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR," desp:"+versionNames[apiLevel][1]); }
		 */
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"manufacture:" + Build.MANUFACTURER);
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"MODEL:" + Build.MODEL);

	}

	public void setContext(Context ctx) {
		_context = ctx;
	}

	private int getLowlatencySamplerate() {
		if (_context == null || android.os.Build.VERSION.SDK_INT < 9) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"getLowlatencySamplerate err, _context:" + _context
					+ " api:" + android.os.Build.VERSION.SDK_INT);
			return 0;
		}
		PackageManager pm = _context.getPackageManager();
		boolean claimsFeature = pm
				.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"LOW_LATENCY:" + (claimsFeature == true ? "Y" : "N"));
		if (android.os.Build.VERSION.SDK_INT < 17) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"API Level too low not support PROPERTY_OUTPUT_SAMPLE_RATE");
			return 0;
		}
		if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"getLowlatencySamplerate not support right now!");
		/*
		  //android.media.property.OUTPUT_SAMPLE_RATE
		  //android.media.property.OUTPUT_FRAMES_PER_BUFFER
		AudioManager am =
		  (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
		  String sampleRate =
		  am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE); String
		  framesPerBuffer =
		  am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
		  if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,
		  "LOW_LATENCY  sampleRate:"+sampleRate+" framesPerBuffer:"+framesPerBuffer
		  ); 
		  if(sampleRate==null||framesPerBuffer==null)
			  return 0; 
		  return
		  Integer.parseInt(sampleRate);
		 */
		return 0;
	}

	private int getLowlatencyFramesPerBuffer() {
		if (_context == null || android.os.Build.VERSION.SDK_INT < 9) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"getLowlatencySamplerate err, _context:" + _context
					+ " api:" + android.os.Build.VERSION.SDK_INT);
			return 0;
		}
		PackageManager pm = _context.getPackageManager();
		boolean claimsFeature = pm
				.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"LOW_LATENCY:" + (claimsFeature == true ? "Y" : "N"));
		if (android.os.Build.VERSION.SDK_INT < 17) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"API Level too low not support PROPERTY_OUTPUT_SAMPLE_RATE");
			return 0;
		}
		/*
		AudioManager am = (AudioManager)_context.getSystemService(Context.AUDIO_SERVICE); String sampleRate =
		am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE); String
		framesPerBuffer =
		am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"LOW_LATENCY  sampleRate:"+sampleRate+" framesPerBuffer:"+framesPerBuffer
		); 
		if(sampleRate==null||framesPerBuffer==null) 
			return 0; 
		*/
		return 0;
	}

	@TargetApi(16)
	private int getAudioSessionId(AudioRecord record) {
		// if(apiLevel>=16)
		// return record.getAudioSessionId();
		// else
		return 0;
	}

	@SuppressWarnings("unused")
	private int InitSetting(int audioSourcePolicy, int audioStreamTypePolicy,
			int modePolicy) {
		_audioSourcePolicy = audioSourcePolicy;
		_audioStreamTypePolicy = audioStreamTypePolicy;
		_modePolicy = modePolicy;
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitSetting: _audioSourcePolicy:" + _audioSourcePolicy
				+ " _audioStreamTypePolicy:" + _audioStreamTypePolicy
				+ " _modePolicy:" + _modePolicy);
		return 0;
	}
/*
	private int getSpecialAudioSouceId() {
		String brandName = Build.MODEL;
		for (int i = 0; i < specialAudioSouceConfig.length; i++) {
			// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"[Config] isSpecialSamsungPhone key:"+specialSamsungKeys[i]+" brandName:"+Build.MODEL);
			if (brandName.contains(specialAudioSouceConfig[i][0])) {
				return i;
			}
		}
		return -1;
	}

	private int getSpecialAudioSouce(int id) {
		if (id < 0 || id >= specialAudioSouceConfig.length)
			return -1;
		return Integer.parseInt(specialAudioSouceConfig[id][1]);
	}
*/
	



	@SuppressWarnings("unused")
	private int InitRecording(int sampleRate) {
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitRecording entry:" + sampleRate);
		if (_isRecording || _audioRecord != null) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"InitRecording _isRecording:" + _isRecording);
			return -1;
		}
		// get the minimum buffer size that can be used
		int minRecBufSize = AudioRecord.getMinBufferSize(sampleRate,
				AudioFormat.CHANNEL_IN_MONO,// AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		
		int framesize = (20*sampleRate*1*2)/1000;
		
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitRecording: min rec buf size is " + minRecBufSize + " sr:"
				+ getLowlatencySamplerate() + " fp"
				+ getLowlatencyFramesPerBuffer()+" 20msFZ:"+framesize);
		
		// double size to be more safe
		// int recBufSize = minRecBufSize * 2;
		_bufferedRecSamples = (5 * sampleRate) / 200;
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"  rough rec delay set to " + _bufferedRecSamples);

		// release the object
		if (_audioRecord != null) {
			_audioRecord.release();
			_audioRecord = null;
		}
		
		/*
		 * CAMCORDER Microphone audio source with same orientation as camera if available, the main device microphone otherwise  
int DEFAULT Default audio source  
int MIC Microphone audio source  
int VOICE_CALL Voice call uplink + downlink audio source  
int VOICE_COMMUNICATION 

		 * */
		int as[]={
				MediaRecorder.AudioSource.DEFAULT,
				MediaRecorder.AudioSource.MIC,
				MediaRecorder.AudioSource.CAMCORDER,
				MediaRecorder.AudioSource.DEFAULT
		};
		as[0] =  TraeAudioManager.getAudioSource(_audioSourcePolicy);
		int recBufSize = minRecBufSize;
		for(int i=0;i<as.length && _audioRecord==null ;i++){
			_audioSource = as[i];
			for (int j = 1; j <= 2; j++) {
				recBufSize = minRecBufSize * j;
				if(recBufSize<framesize*4&&j <2){
					continue;
				}
				
				try {
					_audioRecord = new AudioRecord(_audioSource, sampleRate,
							// AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT, recBufSize);

				} catch (Exception e) {
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,e.getMessage() + " _audioRecord:" + _audioRecord);
					if (_audioRecord != null)
						_audioRecord.release();
					_audioRecord = null;
					continue;
				}
				// _sessionId = getAudioSessionId(_audioRecord);
				// androidAEC = new AndroidAcousticEchoCanceler(_sessionId);

				// check that the audioRecord is ready to be used
				if (_audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitRecording:  rec not initialized,try agine,  minbufsize:"
							+ recBufSize + " sr:" + sampleRate+" as:"+_audioSource);
					_audioRecord.release();
					_audioRecord = null;
					continue;
				}

				break;
			}
		}
		
		

		if (_audioRecord == null) {
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitRecording fail!!!");
			return -1;
		}
		/*
		 * if(_traeAudioSession==null&&_context!=null) _traeAudioSession = new
		 * TraeAudioSession( _context, new ITraeAudioCallback(){
		 * 
		 * @Override public void onCallback(HashMap<String, Object> params) { }
		 * 
		 * @Override public void onDeviceListUpdate(String[]
		 * strDeviceList,String strConnectedDeviceName,String
		 * strPrevConnectedDeviceName) { String str="\n"; for(int
		 * i=0;i<strDeviceList.length;i++){
		 * str+="AudioSession|    "+i+" "+strDeviceList[i]+"\n"; } str+="\n";
		 * 
		 * AudioDeviceInterface.if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onDeviceListUpdate]+
		 * " connected:"+strConnectedDeviceName+
		 * " prevConnected:"+strPrevConnectedDeviceName+
		 * strDeviceList.length+str); }
		 * 
		 * @Override public void onDeviceChangabledUpdate(boolean
		 * bCanChangabled) { // TODO Auto-generated method stub
		 * AudioDeviceInterface.if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onDeviceChangabledUpdate]+
		 * bCanChangabled);
		 * 
		 * }
		 * 
		 * @Override public void onGetDeviceListRes(String[]
		 * strDeviceList,String strConnectedDeviceName,String
		 * strPrevConnectedDeviceName) { // TODO Auto-generated method stub
		 * String str="\n"; for(int i=0;i<strDeviceList.length;i++){
		 * str+="AudioSession|    "+i+" "+strDeviceList[i]+"\n"; } str+="\n";
		 * 
		 * AudioDeviceInterface.if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"AudioSession|[onGetDeviceListRes]+
		 * " connected:"+strConnectedDeviceName+
		 * " prevConnected:"+strPrevConnectedDeviceName+
		 * strDeviceList.length+str); }
		 * 
		 * @Override public void onConnectDeviceRes(String strDeviceName,
		 * boolean bIsConnected) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void onIsDeviceChangabledRes(boolean bCanChangabled)
		 * { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void onGetConnectedDeviceRes(String strDeviceName) {
		 * // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void onGetConnectingDeviceRes(String strDeviceName)
		 * { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * 
		 * } );
		 */
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR," [Config] InitRecording: audioSession:" + _sessionId
				+ " audioSource:" + _audioSource + " rec sample rate set to "
				+ sampleRate + " recBufSize:"+recBufSize);
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitRecording exit");
		return _bufferedRecSamples;
	}

	@SuppressWarnings("unused")
	private int InitPlayback(int sampleRate) {
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitPlayback entry: sampleRate " + sampleRate);
		// get the minimum buffer size that can be used
		if (_isPlaying || _audioTrack != null) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"InitPlayback _isPlaying:" + _isPlaying);
			return -1;
		}
		if (_audioManager == null ) {
			try {
				_audioManager = (AudioManager) _context
						.getSystemService(Context.AUDIO_SERVICE);
			} catch (Exception e) {
				if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,e.getMessage());
			//	_audioTrack.release();
			//	_audioTrack = null;
				return -1;
			}
		}
		// if(sampleRate>16000)
		// sampleRate = 16000;
		_playSamplerate = sampleRate;
		int minPlayBufSize = AudioTrack.getMinBufferSize(sampleRate,
		// AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

		int framesize = (20*sampleRate*1*2)/1000;
		
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitPlayback: minPlayBufSize:" + minPlayBufSize+" 20msFz:"+framesize);

		
		
		// if (playBufSize < 6000) {
		// playBufSize *= 2;
		// }
		_bufferedPlaySamples = 0;
		
		// release the object
		if (_audioTrack != null) {
			_audioTrack.release();
			_audioTrack = null;
		}
		
		int st[]={
				AudioManager.STREAM_VOICE_CALL,
				AudioManager.STREAM_VOICE_CALL,
				AudioManager.STREAM_MUSIC,
				AudioManager.STREAM_SYSTEM
		};
		
		_streamType = TraeAudioManager.getAudioStreamType(_audioStreamTypePolicy);

		if(!_audioRouteChanged){
			
		}else{
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"_audioRouteChanged:"+_audioRouteChanged+" _streamType:"+_streamType);
			
			if(_audioManager.getMode()==AudioManager.MODE_NORMAL&&_connectedDev.equals(TraeAudioManager.DEVICE_SPEAKERPHONE))
				_streamType = AudioManager.STREAM_MUSIC;
			else
				_streamType= AudioManager.STREAM_VOICE_CALL;
			
			_audioRouteChanged = false;
		}
		
		st[0] =  _streamType;
		int playBufSize = minPlayBufSize;
		for(int i=0;i<st.length && _audioTrack==null;i++){
			_streamType = st[i];
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitPlayback: min play buf size is " + minPlayBufSize+" hw_sr:"+AudioTrack.getNativeOutputSampleRate(_streamType));
			for (int j = 1; j <= 2; j++) {
				playBufSize = minPlayBufSize * j;
				
				if(playBufSize<framesize*4&&j < 2){
					continue;
				}
				
				try {
					_audioTrack = new AudioTrack(
							_streamType,// AudioManager.STREAM_VOICE_CALL,
							_playSamplerate,
							// AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.CHANNEL_OUT_MONO,
							AudioFormat.ENCODING_PCM_16BIT, playBufSize,
							AudioTrack.MODE_STREAM);
				} catch (Exception e) {
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,e.getMessage() + " _audioTrack:" + _audioTrack);
					if (_audioTrack != null)
						_audioTrack.release();
					_audioTrack = null;
					continue;
					// return -1;
				}

				// check that the audioRecord is ready to be used
				if (_audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitPlayback: play not initialized playBufSize:"
							+ playBufSize + " sr:" + _playSamplerate);
					_audioTrack.release();
					_audioTrack = null;
					continue;
				}

				break;
			}
		}
		

		if (_audioTrack == null) {
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitPlayback fail!!!");
			return -1;
		}
		if(_as!=null && _audioManager!=null)
			_as.voiceCallAudioParamChanged(_audioManager.getMode(), _streamType);
		_playPosition = _audioTrack.getPlaybackHeadPosition();

		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitPlayback exit: streamType:" + _streamType + " samplerate:"
				+ _playSamplerate + " _playPosition:" + _playPosition+" playBufSize:"+playBufSize);

		TraeAudioManager.forceVolumeControlStream(_audioManager,_connectedDev.equals(TraeAudioManager.DEVICE_BLUETOOTHHEADSET)?6: _audioTrack.getStreamType());


		return 0;// _audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
	}

	private String getDumpFilePath(String filename, int mode) {

		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"manufacture:" + Build.MANUFACTURER);
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"MODEL:" + Build.MODEL);

		// String str =
		// "/mnt/sdcard/"+"MF-"+Build.MANUFACTURER+"-M-"+Build.MODEL+"-as-"+getAudioSource()+"-st-"+getAudioStreamType()+"-m-"+mode+"-"+filename;
		String str = android.os.Environment.getExternalStorageDirectory()
				.getPath()
				+ "/MF-"
				+ Build.MANUFACTURER
				+ "-M-"
				+ Build.MODEL
				+ "-as-"
				+ TraeAudioManager.getAudioSource(_audioSourcePolicy)
				+ "-st-"
				+ _streamType
				+ "-m-" + mode + "-" + filename;

		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"dump:" + str);
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"dump replace:" + str.replace(" ", "_"));
		return str.replace(" ", "_");
	}

	@SuppressWarnings("unused")
	private int StartRecording() {
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StartRecording entry");
		//if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StartRecording entry 2");
		if (_isRecording) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StartRecording _isRecording:" + _isRecording);
			return -1;
		}
		//if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StartRecording entry 3");
		if (_audioRecord == null) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StartRecording _audioRecord:" + _audioRecord);
			return -1;
		}
		//if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StartRecording entry 4");
		// audioMonitor.AddRegisterRef(_context,_modePolicy);
		// audioMonitor.pushEvent("record", "preprocess");
		// start recording
		try {
			//if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StartRecording entry  5");
			_audioRecord.startRecording();

		} catch (IllegalStateException e) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StartRecording fail");
			e.printStackTrace();
			return -1;
		}
		//if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StartRecording entry 6");
		if (_dumpEnable) {

			_rec_dump = new File(getDumpFilePath("jnirecord.pcm",
					_audioManager != null ? _audioManager.getMode() : -1));

			try {
				_rec_out = new FileOutputStream(_rec_dump);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

		}
	//	if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StartRecording entry 7");
		_isRecording = true;
		// if(recover!=null){
		// recover.syncAudioStatus();
		// }

		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StartRecording ok");
		return 0;
	}

	@SuppressWarnings("unused")
	private int StartPlayback() {
		if (_isPlaying) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StartPlayback _isPlaying");
			return -1;
		}
		if (_audioTrack == null) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StartPlayback _audioTrack:" + _audioTrack);
			return -1;
		}
		// audioMonitor.AddRegisterRef(_context,_modePolicy);
		// audioMonitor.pushEvent("play", "preprocess");
		// start playout
		try {
			_audioTrack.play();

		} catch (IllegalStateException e) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StartPlayback fail");
			e.printStackTrace();
			return -1;
		}

		if (_dumpEnable) {
			_play_dump = new File(getDumpFilePath("jniplay.pcm",
					_audioManager != null ? _audioManager.getMode() : -1));

			try {
				_play_out = new FileOutputStream(_play_dump);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		_isPlaying = true;
		// if(recover!=null && _context!=null){
		// AudioRouteRecover.SetMode(_context, recover.getCallAudioMode());
		// recover.syncAudioStatus();
		// }
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StartPlayback ok");
		return 0;
	}

	@SuppressWarnings("unused")
	private int StopRecording() {
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopRecording entry");
		if (_audioRecord == null) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"UnintRecord:" + _audioRecord);
			return -1;
		}
		_recLock.lock();
		try {
			if (_audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
				try {
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopRecording stop... state:"+_audioRecord.getRecordingState());
					_audioRecord.stop();
				} catch (IllegalStateException e) {
					if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StopRecording  err");
					e.printStackTrace();

					return -1;
				}
			}
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopRecording releaseing... state:"+_audioRecord.getRecordingState());
			_audioRecord.release();
			_audioRecord = null;
			_isRecording = false;
		} finally {

			_recLock.unlock();
		}
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopRecording exit ok");
		return 0;
	}

	/*
	 * @SuppressWarnings("unused") private int StopRecording() {
	 * if(!_isRecording){ if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StopRecording _isRecording:"+_isRecording);
	 * return -1; } if(_audioRecord==null){
	 * if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StartPlayback StopRecording:"+_audioRecord); return -1; }
	 * if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopRecording ..."); _recLock.lock(); try { // only stop if we are
	 * recording if (_audioRecord.getRecordingState() ==
	 * AudioRecord.RECORDSTATE_RECORDING) { // stop recording try {
	 * if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopRecording stop..."); _audioRecord.stop(); } catch
	 * (IllegalStateException e) { if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StopRecording  err");
	 * e.printStackTrace(); return -1; } }
	 * 
	 * // release the object if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopRecording release...");
	 * _audioRecord.release(); _audioRecord = null;
	 * if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopRecording release ok"); } finally { // Ensure we always
	 * unlock, both for success, exception or error // return.
	 * _recLock.unlock(); } // audioMonitor.pushEvent("record", "postprocess");
	 * 
	 * // audioMonitor.ReleaseRegisterRef(); _isRecording = false;
	 * if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopRecording ok"); return 0; }
	 */
	@SuppressWarnings("unused")
	private int StopPlayback() {
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopPlayback entry _isPlaying:" + _isPlaying);
		if (_audioTrack == null) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StopPlayback _isPlaying:" + _isPlaying + " "
					+ _audioTrack);
			return -1;
		}
		_playLock.lock();
		
		try {
			// only stop if we are playing
			if (_audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
				// stop playout
				try {
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopPlayback stoping...");
					_audioTrack.stop();
				} catch (IllegalStateException e) {
					if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StopPlayback err");
					e.printStackTrace();
					return -1;
				}

				// flush the buffers
				if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopPlayback flushing... state:"+_audioTrack.getPlayState());
			//	_audioTrack.pause();
				_audioTrack.flush();
			}
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopPlayback releaseing... state:"+_audioTrack.getPlayState());
			// release the object
			_audioTrack.release();
			_audioTrack = null;
			_isPlaying = false;
		} finally {
			_playLock.unlock();
		}
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopPlayback exit ok");
		return 0;
	}

	@SuppressWarnings("unused")
	private int PlayAudio(int lengthInBytes) {
		// 如果当前没有活动的sharp连接，忽略收到的音视频数据，无需播放
		if (false == VideoController.getInstance().hasPendingChannel()) {
			return -1;
		}
		int writeBytes = 0;
		if (!_isPlaying | _audioTrack == null) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"PlayAudio: _isPlaying " + _isPlaying + " " + _audioTrack);
			return -1;
		}
		int bufferedSamples = 0;
		// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"PlayAudio: lengthInBytes "+lengthInBytes);
		_playLock.lock();

		try {
			
			
			if (_audioTrack == null) {
				return -2; // We have probably closed down while waiting for
							// play lock
			}

			// Set priority, only do once
			if (_doPlayInit == true) {
				try {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				} catch (Exception e) {
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"Set play thread priority failed: " + e.getMessage());
				}
				_doPlayInit = false;
			}
			
			if (_dumpEnable && _play_out != null) {

				try {
					_play_out.write(_tempBufPlay, 0, writeBytes);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			
			boolean _needResetAudioTrack;
			
			if(!_audioRouteChanged){
				
				_needResetAudioTrack = false;
			}else{
				if (_audioManager == null && _context != null) {
					_audioManager = (AudioManager) _context
							.getSystemService(Context.AUDIO_SERVICE);
				}
				if(_audioManager.getMode()==AudioManager.MODE_NORMAL&&_connectedDev.equals(TraeAudioManager.DEVICE_SPEAKERPHONE))
					_streamType = AudioManager.STREAM_MUSIC;
				else
					_streamType= AudioManager.STREAM_VOICE_CALL;

				
				_needResetAudioTrack = (_streamType==_audioTrack.getStreamType()?false:true);
				
				_audioRouteChanged = false;
			}
			// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"PlayAudio entry");

			_playBuffer.get(_tempBufPlay);

			/*
			 * if(recover!=null){ int mute_count = recover.getNoisyFrame();
			 * if(mute_count>0){ recover.releaseNoisyFrame();
			 * 
			 * } }
			 */
		
			
			
			
			if(_needResetAudioTrack){
				writeBytes = lengthInBytes;
				_playBuffer.rewind();
				
				long lasttime = SystemClock.elapsedRealtime();
				if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR," track resting:"+" _streamType:"+_streamType+" at.st:"+_audioTrack.getStreamType());
				
				if (_audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
					// stop playout
					try {
						if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopPlayback stoping...");
						
						_audioTrack.stop();
				//		_audioTrack.pause();
						_audioTrack.flush();
						if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopPlayback flushing... state:"+_audioTrack.getPlayState());
						
						_audioTrack.release();
						if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"StopPlayback releaseing... state:"+_audioTrack.getPlayState());
						_audioTrack = null;
					} catch (IllegalStateException e) {
						if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"StopPlayback err");
					//	e.printStackTrace();
					}
				}
				

				int minPlayBufSize = AudioTrack.getMinBufferSize(_playSamplerate,
						// AudioFormat.CHANNEL_CONFIGURATION_MONO,
								AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
				
				int st[]={
						AudioManager.STREAM_VOICE_CALL,
						AudioManager.STREAM_VOICE_CALL,
						AudioManager.STREAM_MUSIC,
						AudioManager.STREAM_SYSTEM
				};
				st[0] = _streamType;
				//st[1] =  TraeAudioManager.getAudioSource(_audioSourcePolicy);
				
				//int _streamType = AudioManager.STREAM_VOICE_CALL;
				int framesize = (20*_playSamplerate*1*2)/1000;
				for(int i=0;i<st.length && _audioTrack==null;i++){
					_streamType = st[i];
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitPlayback: min play buf size is " + minPlayBufSize+" hw_sr:"+AudioTrack.getNativeOutputSampleRate(_streamType));
					for (int j = 1; j <= 2; j++) {
						int playBufSize = minPlayBufSize * j;
						if(playBufSize<framesize*4&&j < 2){
							continue;
						}
						try {
							_audioTrack = new AudioTrack(
									_streamType,// AudioManager.STREAM_VOICE_CALL,
									_playSamplerate,
									// AudioFormat.CHANNEL_CONFIGURATION_MONO,
									AudioFormat.CHANNEL_OUT_MONO,
									AudioFormat.ENCODING_PCM_16BIT, playBufSize,
									AudioTrack.MODE_STREAM);
						} catch (Exception e) {
							if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,e.getMessage() + " _audioTrack:" + _audioTrack);
							if (_audioTrack != null)
								_audioTrack.release();
							_audioTrack = null;
							continue;
							// return -1;
						}
						if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR," _audioTrack:" + _audioTrack);
						// check that the audioRecord is ready to be used
						if (_audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
							if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"InitPlayback: play not initialized playBufSize:"
									+ playBufSize + " sr:" + _playSamplerate);
							_audioTrack.release();
							_audioTrack = null;
							continue;
						}

						break;
					}
				}
				if(_audioTrack!=null){
					try{
					_audioTrack.play();
					_as.voiceCallAudioParamChanged(_audioManager.getMode(), _streamType);
					TraeAudioManager.forceVolumeControlStream(_audioManager,_connectedDev.equals(TraeAudioManager.DEVICE_BLUETOOTHHEADSET)?6: _audioTrack.getStreamType());
					}catch (Exception e) {
						
					}
				}
				
				if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"  track reset used:"
						+ (SystemClock.elapsedRealtime() - lasttime) + "ms");
			}else{
				writeBytes = _audioTrack.write(_tempBufPlay, 0, lengthInBytes);
				_playBuffer.rewind(); // Reset the position to start of buffer
				if (writeBytes < 0) {
					if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"Could not write data from sc (write = " + writeBytes
							+ ", length = " + lengthInBytes + ")");
					return -1;
				}
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"PlayAudio exit");
				if (writeBytes != lengthInBytes) {
					if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"Could not write all data from sc (write = "
							+ writeBytes + ", length = " + lengthInBytes + ")");
				}

				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"Wrote data to sndCard");

				// increase by number of written samples
				_bufferedPlaySamples += (writeBytes >> 1);

				// decrease by number of played samples
				int pos = _audioTrack.getPlaybackHeadPosition();
				// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"play lengthInBytes:"+lengthInBytes+" written:"+writeBytes+" ipos:"+(pos-_playPosition)+" pos:"+pos+" pre pos:"+_playPosition);

				if (pos < _playPosition) { // wrap or reset by driver
					_playPosition = 0; // reset
				}
				_bufferedPlaySamples -= (pos - _playPosition);
				_playPosition = pos;

				if (!_isRecording) {
					bufferedSamples = _bufferedPlaySamples;
				}
			}
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"PlayAudio Exception: " + e.getMessage());

		} finally {
			// Ensure we always unlock, both for success, exception or error
			// return.
			_playLock.unlock();
		}
		// updateRoute();
		// return bufferedSamples;
		return writeBytes;
	}
	
	@SuppressWarnings("unused")
	private int OpenslesNeedResetAudioTrack(boolean b_IsFirstStart) {
		try {
			if(!TraeAudioManager.isCloseSystemAPM(_modePolicy))
				return -1;
			if(_audioRouteChanged || b_IsFirstStart){
				if (_audioManager == null && _context != null) {
					_audioManager = (AudioManager) _context
							.getSystemService(Context.AUDIO_SERVICE);
				}
				if(_audioManager == null)
					return AudioManager.STREAM_VOICE_CALL;
				if(_audioManager.getMode()==AudioManager.MODE_NORMAL&&_connectedDev.equals(TraeAudioManager.DEVICE_SPEAKERPHONE))
					_audioStreamTypePolicy = AudioManager.STREAM_MUSIC;
				else
					_audioStreamTypePolicy = AudioManager.STREAM_VOICE_CALL;
				
				_audioRouteChanged = false;
			}	
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"PlayAudio Exception: " + e.getMessage());

		} finally {
			//if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"OpenslesNeedResetAudioTrack: _audioStreamTypePolicy, "+_audioStreamTypePolicy+ " b_IsFirstStart, "+b_IsFirstStart);
		}
		return _audioStreamTypePolicy;
	}

	@SuppressWarnings("unused")
	private int RecordAudio(int lengthInBytes) {
		int readBytes = 0;
		// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"RecordAudio: lengthInBytes "+lengthInBytes);
		if (!_isRecording) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"RecordAudio: _isRecording " + _isRecording);
			return -1;
		}
		_recLock.lock();

		try {
			if (_audioRecord == null) {
				return -2; // We have probably closed down while waiting for rec
							// lock
			}

			// Set priority, only do once
			if (_doRecInit == true) {
				try {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				} catch (Exception e) {
					if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"Set rec thread priority failed: " + e.getMessage());
				}
				_doRecInit = false;
			}

			_recBuffer.rewind(); // Reset the position to start of buffer

			// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"RecordAudio entry lengthInBytes: " + lengthInBytes);
			readBytes = _audioRecord.read(_tempBufRec, 0, lengthInBytes);
			// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"RecordAudio exit lengthInBytes:"+lengthInBytes+" readBytes:"
			// + readBytes + "from SC");
			// if(_tempBufRec.length!=lengthInBytes||_tempBufRec.length!=readBytes){
			// if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"_tempBufRec len err lengthInBytes:"+lengthInBytes+" readBytes:"+readBytes+" _tempBufRec.length:"+_tempBufRec.length);
			// }
			if (readBytes < 0) {
				if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"Could not read data from sc (read = " + readBytes
						+ ", length = " + lengthInBytes + ")");
				return -1;
			}

			_recBuffer.put(_tempBufRec, 0, readBytes);
			if (_dumpEnable && _rec_out != null) {

				try {
					_rec_out.write(_tempBufRec, 0, readBytes);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			if (readBytes != lengthInBytes) {
				if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"Could not read all data from sc (read = " + readBytes
						+ ", length = " + lengthInBytes + ")");
				return -1;
			}

		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"RecordAudio Exception: " + e.getMessage());

		} finally {
			// Ensure we always unlock, both for success, exception or error
			// return.
			_recLock.unlock();
		}

		// return (_bufferedPlaySamples);
		return (readBytes);
	}

	@SuppressWarnings("unused")
	private int SetPlayoutVolume(int level) {

		// create audio manager if needed
		if (_audioManager == null && _context != null) {
			_audioManager = (AudioManager) _context
					.getSystemService(Context.AUDIO_SERVICE);
		}

		int retVal = -1;

		if (_audioManager != null) {
			_audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
					level, 0);
			retVal = 0;
		}

		return retVal;
	}

	@SuppressWarnings("unused")
	private int GetPlayoutVolume() {

		// create audio manager if needed
		if (_audioManager == null && _context != null) {
			_audioManager = (AudioManager) _context
					.getSystemService(Context.AUDIO_SERVICE);
		}

		int level = -1;

		if (_audioManager != null) {
			level = _audioManager
					.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		}

		return level;
	}
/*
	static final String logTag = "TRAEJAVA2";

	public final static void if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,String msg) {
		if (!_logEnable)
			return;
		if(QLog.isColorLevel())
		   QLog.w(logTag,QLog.CLR, msg);
		//if (_dumpEnable)
		//	logFile(msg + "\n");
	}

	public final static void if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,String msg) {
		if (!_logEnable)
			return;
	//	Log.w(logTag, msg);
		if(QLog.isColorLevel())
		   QLog.e(logTag,QLog.CLR, msg);
		//if (_dumpEnable)
		//	logFile(msg + "\n");
	}
*/
	public static String getTraceInfo() {
		StringBuffer sb = new StringBuffer();

		StackTraceElement[] stacks = new Throwable().getStackTrace();
		int stacksLen = stacks.length;
		sb.append("").append(stacks[2].getClassName()).append(".")
				.append(stacks[2].getMethodName()).append(": ")
				.append(stacks[2].getLineNumber());

		return sb.toString();
	}

	public final static void LogTraceEntry(String msg) {
		if (!_logEnable)
			return;
		String str;
		str = getTraceInfo() + " entry:" + msg;
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,str);
		//Log.w(logTag, str);
		//if (_dumpEnable)
		//	logFile(str);
	}

	public final static void LogTraceExit() {
		if (!_logEnable)
			return;
		String str;
		str = getTraceInfo() + " exit";
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,str);
		//Log.w(logTag, str);
		//if (_dumpEnable)
		//	logFile(str + "\n");
	}


	private TraeAudioSession _as = null;
	private String _connectedDev = TraeAudioManager.DEVICE_NONE;
	private boolean _audioRouteChanged = false;
	//private int _streamTypeShouldbe = -1;

	private void onOutputChanage(String strDeviceName){
		setAudioRouteSwitchState(strDeviceName);
		if(!TraeAudioManager.isCloseSystemAPM(_modePolicy))
			return;
		
		_connectedDev = strDeviceName;
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR," onOutputChanage:"+strDeviceName
				+(_audioManager==null?" am==null": (" mode:"+_audioManager.getMode()) )
				+" st:"+_streamType
				+( _audioTrack==null ? "_audioTrack==null": (" at.st:"+_audioTrack.getStreamType() ) ) );

			try {
				if (_audioManager == null ) 
					_audioManager = (AudioManager) _context
						.getSystemService(Context.AUDIO_SERVICE);
				if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR," curr mode:"+strDeviceName
						+(_audioManager==null?"am==null": (" mode:"+_audioManager.getMode()) ));
				
				if(_connectedDev.equals(TraeAudioManager.DEVICE_SPEAKERPHONE)){

					_audioManager.setMode(AudioManager.MODE_NORMAL);
				}else{
					
				}
			} catch (Exception e) {
				if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,e.getMessage());
			}

		_audioRouteChanged = true;

	
		return;
	}
	private ReentrantLock _prelock=new ReentrantLock(); ;
	private Condition _precon=_prelock.newCondition();
	private boolean _preDone = false;
	public int call_preprocess() {

		AudioDeviceInterface.LogTraceEntry("");		
		switchState = 0;
		_streamType = TraeAudioManager.getAudioStreamType(_audioStreamTypePolicy);
		if (_as == null)
			_as = new TraeAudioSession(_context, new ITraeAudioCallback() {

				@Override
				public void onServiceStateUpdate(boolean on) {
					// TODO Auto-generated method stub
					if(!on){
						try{
							_prelock.lock();
							_preDone = true;
							if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"onVoicecallPreprocessRes signalAll");
							_precon.signalAll();
							_prelock.unlock();
						}catch(Exception e){
							
						}
					}
						
				}

				@Override
				public void onDeviceListUpdate(String[] strDeviceList,
						String strConnectedDeviceName,
						String strPrevConnectedDeviceName,String strBluetoothNameIFHAS) {
					// TODO Auto-generated method stub
					if(usingJava)
						onOutputChanage(strConnectedDeviceName);
				}

				@Override
				public void onDeviceChangabledUpdate(boolean bCanChangabled) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onGetDeviceListRes(int err, String[] strDeviceList,
						String strConnectedDeviceName,
						String strPrevConnectedDeviceName,String strBluetoothNameIFHAS) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onConnectDeviceRes(int err, String strDeviceName,
						boolean bIsConnected) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onIsDeviceChangabledRes(int err,
						boolean bCanChangabled) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onGetConnectedDeviceRes(int err,
						String strDeviceName) {
					// TODO Auto-generated method stub
					if(err==TraeAudioManager.RES_ERRCODE_NONE)
						onOutputChanage(strDeviceName);
				}

				@Override
				public void onGetConnectingDeviceRes(int err,
						String strDeviceName) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onRingCompletion(int err, String userData) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onStreamTypeUpdate(int streamType) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onGetStreamTypeRes(int err, int streamType) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onVoicecallPreprocessRes(int err) {
					// TODO Auto-generated method stub
					
					try{
						_prelock.lock();
						_preDone = true;
						if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"onVoicecallPreprocessRes signalAll");
						_precon.signalAll();
						_prelock.unlock();
					}catch(Exception e){
						
					}

				}

				@Override
				public void onAudioRouteSwitchStart(String fromDev, String toDev) {
					// TODO Auto-generated method stub
				//	if(QLog.isColorLevel()) QLog.d("TRAE",QLog.CLR,"java AudioRouteSwitchStart:"+fromDev+" "+toDev);
					//AudioRouteSwitchStart(fromDev,fromDev);
				//	switchState = 1;
				}

				@Override
				public void onAudioRouteSwitchEnd(String connectedDev,
						long timeMs) {
					// TODO Auto-generated method stub
				//	if(QLog.isColorLevel()) QLog.d("TRAE",QLog.CLR,"java AudioRouteSwitchEnd:"+connectedDev+" "+timeMs+"ms");
					//AudioRouteSwitchEnd(connectedDev,timeMs);
				//	switchState = 0;
				}

			});
		_preDone =false;
		
		if (_as != null){
			_prelock.lock();
			 
			try {
	//			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"test 0000  AudioSession| mSessionId:"+_as.mSessionId);
				_as.getConnectedDevice();
				_as.voiceCallPreprocess(_modePolicy,_streamType);
				
			    try {
			    	//if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"call_preprocess wait doing");
						
					int i=7;
					while(i-->0&&_preDone == false){
						_precon.await(1,TimeUnit.SECONDS);
						if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"call_preprocess waiting...  as:"+_as);
					}
					if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"call_preprocess done!");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//	e.printStackTrace();
				}
			    // do something 
			} finally { 
				_prelock.unlock(); 
			}
		}

		AudioDeviceInterface.LogTraceExit();
		return 0;
	}

	public int call_postprocess() {
		AudioDeviceInterface.LogTraceEntry("");
		switchState = 0;
		if (_as != null) {
			_as.voiceCallPostprocess();
			_as.release();
			_as = null;
		}
		
		// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"call_postprocess entry");
		/*
		 * if(recover!=null){ recover.recover(); AudioRouteRecover.releaseRef();
		 * recover = null; }
		 */
		// int mode = _audioManager!=null?_audioManager.getMode():-1;
		// showToast(_context,"[New] stop"+" as:"+getAudioSource()+" st:"+getAudioStreamType()+" m:"+_audioManager.getMode()+"["+Build.MANUFACTURER+"-"+Build.MODEL+"]");
		// AudioRouteRecover.SetMode(_context, AudioManager.MODE_NORMAL);
		// if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"call_postprocess exit ok");
		AudioDeviceInterface.LogTraceExit();
		return 0;
	}
	
	private boolean usingJava = true;
	//0:no 1:yes
	public void setJavaInterface(int flg)
	{
		if(flg==0)
			usingJava = false;
		else
			usingJava = true;
		if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"setJavaInterface flg:"+flg);
	}
	
	/*
	 * 	public static final String DEVICE_NONE = "DEVICE_NONE";
	public static final String DEVICE_EARPHONE = "DEVICE_EARPHONE";
	public static final String DEVICE_SPEAKERPHONE = "DEVICE_SPEAKERPHONE";
	public static final String DEVICE_WIREDHEADSET = "DEVICE_WIREDHEADSET";
	public static final String DEVICE_BLUETOOTHHEADSET = "DEVICE_BLUETOOTHHEADSET";
	0:DEVICE_NONE
	1:DEVICE_EARPHONE
	2:DEVICE_SPEAKERPHONE
	3:DEVICE_WIREDHEADSET
	4:DEVICE_BLUETOOTHHEADSET
	 * */
	private int switchState=0;
	private void setAudioRouteSwitchState(String strDeviceName){
		if(strDeviceName.equals(TraeAudioManager.DEVICE_EARPHONE)){
			switchState = 1;
		}else if(strDeviceName.equals(TraeAudioManager.DEVICE_SPEAKERPHONE)){
			switchState = 2;
		}else if(strDeviceName.equals(TraeAudioManager.DEVICE_WIREDHEADSET)){
			switchState = 3;
		}else if(strDeviceName.equals(TraeAudioManager.DEVICE_BLUETOOTHHEADSET)){
			switchState = 4;
		}else{
			switchState = 0;
		}
	}
	public int getAudioRouteSwitchState(){
		return switchState;
	}
	//res:0:success 1:fail  flg:0:begine,1:end
//	native int AudioRouteSwitchStart(String fromDev,String toDev);
//	native int AudioRouteSwitchEnd(String connectedDev,long timeMs);
	
	private void initTRAEAudioManager()
	{
		if(_context != null)
		{
			TraeAudioManager.init(_context);
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"initTRAEAudioManager , TraeAudioSession startService");
		}
	}
	
	private void uninitTRAEAudioManager()
	{
		if(_context != null)
		{
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"uninitTRAEAudioManager , stopService");
			TraeAudioManager.uninit();
		}	
		else
		{
			if(QLog.isColorLevel()) QLog.w("TRAE",QLog.CLR,"uninitTRAEAudioManager , context null");
		}
	}
	
	
}
