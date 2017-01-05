package com.tencent.sharp.jni;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.tencent.device.QLog;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class TraeMediaPlayer implements MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener {
	static public final int TRAE_MEDIAPLAER_DATASOURCE_RSID = 0;
	static public final int TRAE_MEDIAPLAER_DATASOURCE_URI = 1;
	static public final int TRAE_MEDIAPLAER_DATASOURCE_FILEPATH = 2;
	static public final int TRAE_MEDIAPLAER_STOP = 100;

	private MediaPlayer mMediaPlay = null;
	private OnCompletionListener mCallback;
	private Context _context;
	private int _streamType = AudioManager.STREAM_VOICE_CALL;
	private boolean _hasCall =false;
	private boolean _loop =false;
	private int _durationMS=-1;
	int _loopCount = 0;
	boolean _ringMode = false;
	
	private Timer _watchTimer = null;
	private TimerTask _watchTimertask = null;
	public static interface OnCompletionListener {
		public void onCompletion();
	}

	public TraeMediaPlayer(Context context, OnCompletionListener cb) {
		_context = context;
		mCallback = cb;
		// start();
	}

	public boolean playRing(int datasource, int rsid, Uri res,String strFilePath, boolean loop,int loopCount,boolean ringMode,boolean hasCall,int callStreamType) {
		if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | playRing datasource:"+ datasource+" rsid:"+rsid+" uri:"+res+" filepath:"+strFilePath+" loop:"+(loop?"Y":"N")+" :loopCount"+loopCount+" ringMode:"+(ringMode?"Y":"N")+" hasCall:"+hasCall+" cst:"+callStreamType);

		if(loop==false && loopCount<=0){
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | playRing err datasource:"+ datasource+" loop:"+(loop?"Y":"N")+" :loopCount"+loopCount);
			return false;
		}
		try {
			try {
				if (mMediaPlay != null) {
					if (mMediaPlay.isPlaying()) {
						return false;
					} else {
						try {
							mMediaPlay.release();
						} catch (Exception e) {
						} finally {
							mMediaPlay = null;
						}
					}
				}
				if(_watchTimer!=null){
					_watchTimer.cancel();
					_watchTimer=null;
					_watchTimertask=null;
				}
				
				AudioManager am = (AudioManager) _context
						.getSystemService(Context.AUDIO_SERVICE);

				// am.setSpeakerphoneOn(speaker);

				mMediaPlay = new MediaPlayer();
				if (null == mMediaPlay) {
					mMediaPlay.release();
					mMediaPlay = null;
					return false;
				}
				mMediaPlay.setOnCompletionListener(this);
				mMediaPlay.setOnErrorListener(this);
				
				
				// if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | rsid:"+rsid);

				// mMediaPlay.reset();
				switch (datasource) {
				case TRAE_MEDIAPLAER_DATASOURCE_RSID:
					if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | rsid:"
							+ rsid);
					AssetFileDescriptor afd = _context.getResources()
							.openRawResourceFd(rsid);
					if (afd == null) {
						if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | afd == null rsid:"
										+ rsid);
						mMediaPlay.release();
						mMediaPlay = null;
						return false;
					}
					mMediaPlay.setDataSource(afd.getFileDescriptor(),
							afd.getStartOffset(), afd.getLength());
					afd.close();
					break;
				case TRAE_MEDIAPLAER_DATASOURCE_URI:
					if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | uri:" + res);
					mMediaPlay.setDataSource(_context, res);
					break;
				case TRAE_MEDIAPLAER_DATASOURCE_FILEPATH:
					if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | FilePath:"
							+ strFilePath);
					mMediaPlay.setDataSource(strFilePath);
					break;
				default:
					if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | err datasource:"
									+ datasource);
					mMediaPlay.release();
					mMediaPlay = null;
					break;
				}

				if (mMediaPlay == null)
					return false;
				_ringMode = ringMode;
				int _mode = AudioManager.MODE_NORMAL;
				
				if(_ringMode){
					_streamType = AudioManager.STREAM_RING;
					_mode = AudioManager.MODE_RINGTONE;
				}else{
					_streamType = AudioManager.STREAM_VOICE_CALL;
					if(android.os.Build.VERSION.SDK_INT >=11)
						_mode = AudioManager.MODE_IN_COMMUNICATION;
				}
				_hasCall = hasCall;
				if(_hasCall){
					_streamType = callStreamType;
				}
				mMediaPlay.setAudioStreamType(_streamType);
				// mMediaPlay.setLooping(false);
				mMediaPlay.prepare();
				mMediaPlay.setLooping(loop);
				mMediaPlay.start();
				
				//volumeDo();
				_loop = loop;
				if(_loop==true){
					_loopCount = 1;
					_durationMS = -1;
				}else{
					_loopCount = loopCount;
					_durationMS = _loopCount*mMediaPlay.getDuration();
				}
				_loopCount--;
				
				if(!_hasCall)
					am.setMode(_mode);
				
				if(_durationMS>0){
					_watchTimer = new Timer();
					_watchTimertask = new TimerTask() {
						public void run() {
							if (mMediaPlay !=null) {
								if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | play timeout");
								if (null != mCallback) {
									mCallback.onCompletion();
								}
							}
						}
					};
					_watchTimer.schedule(_watchTimertask, _durationMS+1000);
				}
				
				if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay | DurationMS:"+mMediaPlay.getDuration()+" loop:"+loop);
				
				
				return true;
				//
			} catch (IllegalStateException ex) {
				if(QLog.isColorLevel()) QLog.d("TRAE",QLog.CLR,"TraeMediaPlay | IllegalStateException: "
								+ ex.getLocalizedMessage() + " "
								+ ex.getMessage());
			} catch (IOException ex) {
				if(QLog.isColorLevel()) QLog.d("TRAE",QLog.CLR,"TraeMediaPlay | IOException: "
						+ ex.getLocalizedMessage() + " " + ex.getMessage());
			} catch (IllegalArgumentException ex) {
				if(QLog.isColorLevel()) QLog.d("TRAE",QLog.CLR,"TraeMediaPlay | IllegalArgumentException: "
								+ ex.getLocalizedMessage() + " "
								+ ex.getMessage());

			} catch (SecurityException ex) {
				if(QLog.isColorLevel()) QLog.d("TRAE",QLog.CLR,"TraeMediaPlay | SecurityException: "
								+ ex.getLocalizedMessage() + " "
								+ ex.getMessage());

				// fall through
			}
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.d("TRAE",QLog.CLR,"TraeMediaPlay | Except: "
					+ e.getLocalizedMessage() + " " + e.getMessage());

		}

		try {
			mMediaPlay.release();
		} catch (Exception e1) {
		}
		mMediaPlay = null;
		return false;
	}

	public void stopRing() {
		if(QLog.isColorLevel()) QLog.d("TRAE",QLog.CLR,"TraeMediaPlay stopRing ");
		if (null == mMediaPlay) {
			return;
		}
		
		if (mMediaPlay.isPlaying())
			mMediaPlay.stop();
		mMediaPlay.reset();
		try {
			if(_watchTimer!=null){
				_watchTimer.cancel();
				_watchTimer=null;
				_watchTimertask=null;
			}
			//volumeUndo();
			mMediaPlay.release();
		} catch (Exception e) {
		}
		mMediaPlay = null;
		_durationMS = -1;
	}
	public int getStreamType(){
		return _streamType;
	}
	public int getDuration(){
		return _durationMS;
	}
	public boolean hasCall(){
		return _hasCall;
	}
	@Override
	public void onCompletion(MediaPlayer arg0) {
		AudioDeviceInterface.LogTraceEntry(" cb:"+mCallback+" loopCount:"+_loopCount+" _loop:"+_loop);
		if(_loop){
			if(QLog.isColorLevel()) QLog.d("TRAE",QLog.CLR,"loop play,continue...");
			return;
		}
		try {
			if(_loopCount<=0){
				volumeUndo();
				if (mMediaPlay.isPlaying())
					mMediaPlay.stop();
				mMediaPlay.reset();
				mMediaPlay.release();
				mMediaPlay = null;
				if (null != mCallback) {
					mCallback.onCompletion();
				}
			}else{
				mMediaPlay.start();
				_loopCount--;
			}
		} catch (Exception e) {
		}
		
		AudioDeviceInterface.LogTraceExit();
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		AudioDeviceInterface.LogTraceEntry(" cb:"+mCallback+" arg1:"+arg1+" arg2:"+arg2);
		try {
			mMediaPlay.release();
		} catch (Exception e) {
		}
		mMediaPlay = null;
		if (null != mCallback) {
			mCallback.onCompletion();
		}
		AudioDeviceInterface.LogTraceExit();
		return false;
	}
	private int _prevVolume= -1;
	
	private void volumeDo(){
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
		if(mMediaPlay==null||_ringMode==false||_streamType==AudioManager.STREAM_RING)
			return;
		try{
			AudioManager am = (AudioManager) _context
					.getSystemService(Context.AUDIO_SERVICE);
			int currV = am.getStreamVolume(_streamType); 
			int maxV = am.getStreamMaxVolume(_streamType);
			int currRV = am.getStreamVolume(AudioManager.STREAM_RING); 
			int maxRV = am.getStreamMaxVolume(AudioManager.STREAM_RING); 
			int setV =(int) (( (currRV*1.0)/maxRV)*maxV);
			
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay volumeDo currV:"+currV
					+" maxV:"+maxV+" currRV:"+currRV+" maxRV:"+maxRV+" setV:"+setV);
			if(setV+1>=maxV)
				setV = maxV;
			else
				setV = setV+1;
			
			am.setStreamVolume(_streamType, setV, 0);
			_prevVolume = currV;
		}catch(Exception e){
			
		}
		
		
	}
	
	private void volumeUndo(){
		if(mMediaPlay==null||_ringMode==false||_streamType==AudioManager.STREAM_RING||_prevVolume==-1)
			return;
		try{
			if(QLog.isColorLevel()) QLog.e("TRAE",QLog.CLR,"TraeMediaPlay volumeUndo _prevVolume:"+_prevVolume);
			AudioManager am = (AudioManager) _context
					.getSystemService(Context.AUDIO_SERVICE);
			am.setStreamVolume(_streamType, _prevVolume, 0);
		}catch(Exception e){
		}

	}
}
