package com.tencent.devicedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.av.VideoController;
import com.tencent.av.core.VideoConstants;
import com.tencent.av.mediacodec.surface2buffer.VideoDecoder;
import com.tencent.av.mediacodec.surface2buffer.VideoDrawer;
import com.tencent.av.mediacodec.surface2buffer.VideoEncoder;
import com.tencent.av.opengl.GLVideoView;
import com.tencent.av.opengl.GraphicRenderMgr;
import com.tencent.av.opengl.ui.GLRootView;
import com.tencent.av.opengl.ui.GLView;
import com.tencent.device.QLog;
import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoChatActivityHW extends Activity implements Renderer, SurfaceTexture.OnFrameAvailableListener{
	private static final String TAG = "VideoChatActivityHW";
    
	String		mPeerId;
	String      mSelfDin;
	boolean		mIsReceiver = false;
	boolean		mVideoConnected = false;

	TextView 	mLogInfo;
	Button 		mAccept; 
	Button 		mReject;
	Button 		mClose;

	BroadcastHandler mBroadcastHandler;
	
	//===============================================  
	private VideoEncoder		mVideoEncoder = null;
	private VideoDecoder		mVideoDecoder = null;
	private VideoDrawer		mVideoDrawer  = null;
	private Object			mObjectMutex  = new Object();
	private boolean			mResetEncoder = false;
	
	private GLSurfaceView	mGLSurfaceView;
	private SurfaceTexture	mSurfaceTexture;
	private int				mTextureID = -1;
	private TextureView 		mPeerTextureView; 
	private ImageView		mPeerViewBkg;
	private int         		mPeerVideoAngle = -1;
    private Camera 			mCamera;
	
    // parameters for the encoder 
    private static final String MIME_TYPE	= "video/avc";
    private static int encWidth				= 640;  
    private static int encHeight				= 480;
    private static int encBitRate			= 1000*350;    
    private static int encFrameRate			= 10;             
    private static int encIFrameInterval		= 5;	  
	//==================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
        //全屏设置，隐藏窗口所有装饰
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//清除FLAG
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//super.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //if(getIntent().getBooleanExtra("back", false))
        {
            if(getActionBar() != null)
                getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        super.setContentView(R.layout.activity_videochat_hardcodec);
		super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		Intent intent = super.getIntent();
		mPeerId = intent.getStringExtra("peerid");
		mSelfDin = VideoController.getInstance().GetSelfDin();
		mIsReceiver = intent.getBooleanExtra("receive", false);
		
		mBroadcastHandler = new BroadcastHandler();
		IntentFilter filter = new IntentFilter();
		filter.addAction(VideoConstants.ACTION_STOP_VIDEO_CHAT);
		filter.addAction(VideoController.ACTION_NETMONIOTR_INFO);
		filter.addAction(VideoController.ACTION_CHANNEL_READY);
		filter.addAction(VideoController.ACTION_VIDEOFRAME_INCOME);
		filter.addAction(VideoController.ACTION_VIDEO_QOS_NOTIFY);
		filter.addAction(VideoDecoder.VIDEO_ASPECT_RATIO_CHANGE);
		filter.addAction(VideoDecoder.VIDEO_RENDER_FIRST_FRAME);
		filter.addAction(TXDeviceService.BinderListChange);
		filter.addAction(TXDeviceService.OnEraseAllBinders);
		registerReceiver(mBroadcastHandler, filter);
		
		//local view
		mGLSurfaceView = (GLSurfaceView)findViewById(R.id.camera_textureview);
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(this);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
		//peer view 
		if (VideoController.isHardwareDecoderEnabled()) {
			initPeerTextureView();
		}
		else {
			initPeerQQGlView();
		}

		mLogInfo = (TextView) findViewById(R.id.logInfo_surface);
		mAccept  = (Button) findViewById(R.id.av_video_accept_surface);
		mReject  = (Button) findViewById(R.id.av_video_reject_surface);
		mClose   = (Button) findViewById(R.id.av_video_close_surface);
		
		if (mIsReceiver) {
			mAccept.setVisibility(View.VISIBLE);
			mReject.setVisibility(View.VISIBLE);
		} 
		else {
			mClose.setVisibility(View.VISIBLE);
			mClose.setText("取消");
			if (Long.parseLong(mPeerId) != 0) {
				VideoController.getInstance().request(mPeerId);
			}
		}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return false;
    }

    @Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		
		//startRing必须和stopRing成对调用（使用MediaPlayer）； startRing2必须和stopRing2成对调用（使用SoundPool）；
		if (mIsReceiver) {
			VideoController.getInstance().startRing(R.raw.qav_video_incoming, -1, null);
			//VideoController.getInstance().startRing2(R.raw.qav_video_incoming, -1);
		} else {
			VideoController.getInstance().startRing(R.raw.qav_video_request, -1, null);
			//VideoController.getInstance().startRing2(R.raw.qav_video_request, -1);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		
		if (mIsReceiver) {  
			if (mVideoConnected) {
				VideoController.getInstance().closeVideo(mPeerId);
			} 
			else {
				VideoController.getInstance().rejectRequest(mPeerId);
			}
		} 
		else {
			VideoController.getInstance().closeVideo(mPeerId);
		}
		
		closeCamera();
		stopEncoder();
		stopDecoder();
		VideoController.getInstance().stopRing();
		//VideoController.getInstance().stopRing2();
		GraphicRenderMgr.getInstance().setGlRender(mPeerId, null);
		
		mVideoConnected = false;
		
		VideoController.getInstance().exitProcess();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		super.unregisterReceiver(mBroadcastHandler);
	}

	@Override
	public void finish() {
		Log.i(TAG, "VideoActivity:finish");
		super.finish();
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			showQuitDialog();
			return true;
		case KeyEvent.KEYCODE_SEARCH:
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			break;
		case KeyEvent.KEYCODE_MENU:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	void showQuitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("确认退出吗?");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				VideoChatActivityHW.this.finish();
			}
		});

		builder.setNegativeButton("取消", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private long mTimeClick = 0;
	private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			// TODO Auto-generated method stub
			long timeNow = System.currentTimeMillis();
	        if(MotionEvent.ACTION_DOWN == event.getAction()){   
	            if(timeNow - mTimeClick < 600){  
		            if (mLogInfo != null) {
		            		mLogInfo.setVisibility(View.VISIBLE);
		            } 
	            } 
	            else {
		            if (mLogInfo != null) {
	            			mLogInfo.setVisibility(View.INVISIBLE);
		            } 
	            }
	            mTimeClick = timeNow; 
	        }  
	        return true;  
		}
	};
	
	void initPeerQQGlView() {
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "initQQGlView");
		}
		
		GLRootView  glRootView = (GLRootView) findViewById(R.id.peer_gl_root_view);
		glRootView.setVisibility(View.VISIBLE);
		glRootView.setOnTouchListener(mTouchListener);
		GLVideoView glPeerVideoView = new GLVideoView(this);
		glRootView.setContentPane(glPeerVideoView);
		glPeerVideoView.setIsPC(false);
		glPeerVideoView.enableLoading(false);
		glPeerVideoView.setMirror(true);
		glPeerVideoView.setNeedRenderVideo(true);
		glPeerVideoView.setVisibility(GLView.VISIBLE);
		glPeerVideoView.setScaleType(ScaleType.CENTER_CROP);
		glPeerVideoView.setBackground(R.drawable.qav_video_bg_s);

		GraphicRenderMgr.getInstance().setGlRender(mPeerId, glPeerVideoView.getYuvTexture());
	}

	void initPeerTextureView() {
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.layout_peer_textureView);
		layout.setVisibility(View.VISIBLE);
		mPeerViewBkg = (ImageView)findViewById(R.id.imageView_peer_textureView);
		mPeerTextureView = (TextureView)findViewById(R.id.peer_textureView);
		mPeerTextureView.setOnTouchListener(mTouchListener);
		mPeerTextureView.setSurfaceTextureListener(new SurfaceTextureListener() {
			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture arg0,
					int arg1, int arg2) {
				// TODO Auto-generated method stub
				mVideoDecoder = new VideoDecoder(new Surface(arg0));
				mVideoDecoder.resetDecoder(MIME_TYPE, encWidth, encHeight);
				adjustVideoAspectRatio(encWidth, encHeight);
			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture arg0,
					int arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	class BroadcastHandler extends BroadcastReceiver { 
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(VideoConstants.ACTION_STOP_VIDEO_CHAT)) {
				int reason = intent.getIntExtra("reason", VideoConstants.VOIP_REASON_OTHERS);
				if (reason == VideoConstants.VOIP_REASON_REJECT_BY_FRIEND) {
					//发起视频请求之后，对方拒绝
				} else if (reason == VideoConstants.VOIP_REASON_SELF_WAIT_RELAYINFO_TIMEOUT) {
					//发起视频请求之后，对方一直不接听，最后超时 
				} else if (reason == VideoConstants.VOIP_REASON_CLOSED_BY_FRIEND) {
					//连通之后，对方主动关闭
				} else {
					//其它原因
				}
				
				Log.d(TAG, "recv broadcast : AVSessionClose reason = " + reason);
				finish();
			} else if (intent.getAction().equalsIgnoreCase(VideoController.ACTION_CHANNEL_READY)) { 
				VideoController.getInstance().stopRing();
				//VideoController.getInstance().stopRing2();
				VideoController.getInstance().stopShake();
				VideoController.getInstance().startShake();
				mVideoConnected = true;
				mClose.setText("关闭");
			} else if (intent.getAction().equalsIgnoreCase(VideoController.ACTION_NETMONIOTR_INFO)) {
				String msg = intent.getStringExtra("msg");
				if (mLogInfo != null) {
					mLogInfo.setText("Video Info \r\n" + msg);
					mLogInfo.invalidate();
				}
			} else if (intent.getAction().equalsIgnoreCase(VideoController.ACTION_VIDEOFRAME_INCOME)) {
				if (mPeerTextureView != null && mVideoDecoder != null) {
					int peerVideoAngle = intent.getIntExtra("angle", 0);
					if (mPeerVideoAngle != peerVideoAngle) {
						mPeerVideoAngle = peerVideoAngle;
						mPeerTextureView.setRotation(peerVideoAngle * 90);
					}
					mVideoDecoder.onFrameAvailable(intent.getByteArrayExtra("buffer"));
				}
			} else if (intent.getAction().equalsIgnoreCase(VideoDecoder.VIDEO_ASPECT_RATIO_CHANGE)) {
				int width = intent.getIntExtra("width", 0);
				int height = intent.getIntExtra("height", 0); 
				adjustVideoAspectRatio(width, height);
			} else if (intent.getAction().equalsIgnoreCase(VideoDecoder.VIDEO_RENDER_FIRST_FRAME)) {
				if (mPeerViewBkg != null) {
					mPeerViewBkg.setVisibility(View.INVISIBLE);
				}
			} else if (intent.getAction().equalsIgnoreCase(VideoController.ACTION_VIDEO_QOS_NOTIFY)) {
				int width = intent.getIntExtra("width", 0);
				int height = intent.getIntExtra("height", 0);
				int bitrate = intent.getIntExtra("bitrate", 0) * 1000;
				int fps = intent.getIntExtra("fps", 0); 
				if (width != 0 && height != 0 && bitrate != 0 && fps != 0) {
					if (encWidth != width || encHeight != height || encBitRate != bitrate || encFrameRate != fps) {
						encWidth = width;
						encHeight = height;
						encBitRate = bitrate;
						encFrameRate = fps;
						synchronized (mObjectMutex) {
							if (mVideoEncoder != null) {
								mResetEncoder = true;
							}
						}
					}
				}
			} else if (intent.getAction() == TXDeviceService.BinderListChange) {
				boolean bFind = false;
				Parcelable[] listBinder = intent.getExtras().getParcelableArray("binderlist");
				for (int i = 0; i < listBinder.length; ++i){
					TXBinderInfo  binder = (TXBinderInfo)(listBinder[i]);
					if (binder.tinyid == Long.parseLong(mPeerId)) {
						bFind = true;
						break;
					}
				}
				if (bFind == false) {
					finish();
				}
			} else if (intent.getAction() == TXDeviceService.OnEraseAllBinders) {
				finish();
			}
		}
	}
	
	public void onBtnClose(View view) {
		finish();
	}

	public void onBtnReject(View view) {
		finish();
	}

	public void onBtnAccept(View view) {        
		VideoController.getInstance().stopRing();

		if (Long.parseLong(mPeerId) != 0) {
			VideoController.getInstance().acceptRequest(mPeerId);
			mAccept.setVisibility(View.GONE);
			mReject.setVisibility(View.GONE);
			mClose.setVisibility(View.VISIBLE);
		}
	}
	
	//==========================================================================
	//==========================================================================
	//==========================================================================
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onSurfaceCreated...");
		mTextureID = VideoEncoder.createTextureID();
		mSurfaceTexture = new SurfaceTexture(mTextureID);
		mSurfaceTexture.setOnFrameAvailableListener(this);
		mVideoDrawer = new VideoDrawer(mTextureID);
		openCamera(encWidth, encHeight);
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onSurfaceChanged...");
		mVideoDrawer.adjustTextureCoord(width, height, encWidth, encHeight);
		//mVideoDrawer.rotateTextureCoordHorizontal();	这一行可以在水平方向上旋转本地画面，必须在adjustTextureCoord之后调用
		//mVideoDrawer.rotateTextureCOordVertical();		这一行可以在垂直方向上旋转本地画面，必须在adjustTextureCoord之后调用
		GLES20.glViewport(0, 0, width, height);
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub  
		Log.i(TAG, "onDrawFrame...");
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		mSurfaceTexture.updateTexImage();
		float[] mtx = new float[16];
		mSurfaceTexture.getTransformMatrix(mtx);
		mVideoDrawer.draw(mtx);
		
		if (mVideoConnected == true) { 
			synchronized (mObjectMutex) { 
				if (mVideoEncoder == null) {
					mVideoEncoder = new VideoEncoder(Long.parseLong(mPeerId));
					mVideoEncoder.prepareEncoder(MIME_TYPE, encWidth, encHeight, encBitRate, encFrameRate, encIFrameInterval);
					mVideoEncoder.startEncoder(EGL14.eglGetCurrentContext(), mTextureID);
				}
				
				if (mResetEncoder){
					mVideoEncoder.stopEncode();
	    				mVideoEncoder.prepareEncoder(MIME_TYPE, encWidth, encHeight, encBitRate, encFrameRate, encIFrameInterval);
	    				mVideoEncoder.startEncoder(EGL14.eglGetCurrentContext(), mTextureID);
	    				mResetEncoder = false;
				}
				
				mVideoEncoder.onFrameAvailable(mSurfaceTexture); 
			}
		} 
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onFrameAvailable...");
		
		mGLSurfaceView.requestRender(); 
	}
		
    public void openCamera(int encWidth, int encHeight) {
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized");
        }

        Camera.CameraInfo info = new Camera.CameraInfo();

        // Try to find a front-facing camera (e.g. for videoconferencing).
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
                break;
            }
        }
        if (mCamera == null) {
            Log.d(TAG, "No front-facing camera found; opening default");
            mCamera = Camera.open();    // opens first back-facing camera
        }
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }

        Camera.Parameters parms = mCamera.getParameters();
        choosePreviewSize(parms, encWidth, encHeight);	
        chooseFrameRate(parms, encFrameRate * 1000);
        parms.setRecordingHint(true);
        mCamera.setParameters(parms);
        mCamera.setDisplayOrientation(0);
        Camera.Size size = parms.getPreviewSize();
        Log.d(TAG, "Camera preview size is " + size.width + "x" + size.height);
        
        try {
        		mCamera.setPreviewTexture(mSurfaceTexture);
        		mCamera.startPreview();
        }
        catch (Exception e) {
        		Log.d(TAG, "Camera startPreview failed");
        }   
    }

    private static void choosePreviewSize(Camera.Parameters parms, int width, int height) {
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
        if (ppsfv != null) {
            Log.d(TAG, "Camera preferred preview size for video is " + ppsfv.width + "x" + ppsfv.height);
        } 
        
        for (Camera.Size size : parms.getSupportedPreviewSizes()) {
            if (size.width == width && size.height == height) {
                parms.setPreviewSize(width, height);
                return;
            }
        }

        Log.w(TAG, "Unable to set preview size to " + width + "x" + height);
        if (ppsfv != null) {
            parms.setPreviewSize(ppsfv.width, ppsfv.height);
        }
    }
    
    private static void chooseFrameRate(Camera.Parameters parms, int frameRate) {
    		List<int[]> fpsRanges = parms.getSupportedPreviewFpsRange();
    		for (int i = 0; i < fpsRanges.size(); ++i) {
    			int [] range = fpsRanges.get(i);
    			if (range != null) {
	    			int fpsMin = range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
	    			int fpsMax = range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
	    			if (fpsMin <= frameRate && frameRate <= fpsMax) {
	    				parms.setPreviewFpsRange(fpsMin, fpsMax);
	    				return;
	    			}
    			}
    		}
    		
    		if (fpsRanges.size() > 0) {
    			int [] range = fpsRanges.get(0);
    			if (range != null) {
    				parms.setPreviewFpsRange(range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
    				return;
    			}
    		}
    		
    		parms.setPreviewFpsRange(frameRate, frameRate);
    }

    private void closeCamera() {
        Log.d(TAG, "closing camera");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    
    private void stopEncoder() {
		Log.d(TAG, "stoping encoder");
		synchronized (mObjectMutex) { 
			if (mVideoEncoder != null) {
				mVideoEncoder.stopEncode();
				mVideoEncoder = null;
			}
		}
    }

	private void stopDecoder() {
		if (mVideoDecoder != null) {
			mVideoDecoder.releaseDecoder();
			mVideoDecoder = null;
		}
	}
	 
	private void adjustVideoAspectRatio(int videoWidth, int videoHeight) {
		if (videoWidth == 0 || videoHeight == 0) {
			return ;
		}
		
        int viewWidth = mPeerTextureView.getWidth();
        int viewHeight = mPeerTextureView.getHeight(); 
        double aspectRatio = (double) videoHeight / videoWidth;
        
        Log.d(TAG, "adjustVideoAspectRatio: videoWidth = " + videoWidth + " videoHeight = " + videoHeight + "viewWidth = " + viewWidth + " viewHeight = " + viewHeight);
        
        int newWidth, newHeight; 
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
         
        int xoffset = (viewWidth - newWidth) / 2;
        int yoffset = (viewHeight - newHeight) / 2;
        Matrix txform = new Matrix();
        mPeerTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoffset, yoffset);
        mPeerTextureView.setTransform(txform);
        
//        mPeerTextureView.setScaleX((float) newWidth / viewWidth);
//        mPeerTextureView.setScaleY((float) newHeight / viewHeight);
	}
}