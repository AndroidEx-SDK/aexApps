package com.tencent.devicedemo;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.tencent.av.VideoController;
import com.tencent.av.camera.VcCamera;
import com.tencent.av.core.VideoConstants;
import com.tencent.av.mediacodec.surface2buffer.VideoEncoder;
import com.tencent.av.opengl.GLVideoView;
import com.tencent.av.opengl.GraphicRenderMgr;
import com.tencent.av.opengl.ui.GLRootView;
import com.tencent.av.opengl.ui.GLView;
import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;
import com.tencent.devicedemo.R;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;

public class VideoMonitorService extends Service{
	private static final String TAG = "VideoMonitorService";

	IVideoMonitor  	        mVideoMonitor = null;
	private String			mPeerId;
	
	public VideoMonitorService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    @SuppressWarnings("deprecation")
	@Override   
    public void onStart(Intent intent, int startId) {   
    		super.onStart(intent, startId);
		if (intent != null) {
			mPeerId = intent.getStringExtra("peerid");  
			Log.d(TAG, "peerId = " + mPeerId);
			
			IntentFilter filter = new IntentFilter();
			filter.addAction(VideoConstants.ACTION_STOP_VIDEO_CHAT);
			filter.addAction(VideoController.ACTION_NETMONIOTR_INFO);
			filter.addAction(VideoController.ACTION_CHANNEL_READY);
			filter.addAction(VideoController.ACTION_VIDEO_QOS_NOTIFY);  
			filter.addAction(TXDeviceService.BinderListChange);
			filter.addAction(TXDeviceService.OnEraseAllBinders);
			registerReceiver(mBroadcastHandler, filter);
			
			//VideoController.mEnableHWEncoder = false; 
			if (VideoController.isHardwareEncoderEnabled()) {
				mVideoMonitor = new VideoMonitorHW(); 
			}
			else {
				mVideoMonitor = new VideoMonitorSF(); 
			}
			mVideoMonitor.start(this, mPeerId);
			
			VideoController.getInstance().acceptRequest(mPeerId);
		} 
		else {
			this.stopSelf(); 
		}
    }
    
    @Override
    public void onDestroy() {
    		super.unregisterReceiver(mBroadcastHandler);
    		
    		mVideoMonitor.setVideoConnected(false);
    		mVideoMonitor.stop();
    		
    		VideoController.getInstance().exitProcess();
    }
        
    private BroadcastReceiver mBroadcastHandler = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(VideoConstants.ACTION_STOP_VIDEO_CHAT)) {
				Log.d(TAG, "recv broadcast : AVSessionClose");
				mVideoMonitor.setVideoConnected(false);
				VideoMonitorService.this.stopSelf();
			} else if (intent.getAction().equalsIgnoreCase(VideoController.ACTION_CHANNEL_READY)) {
				mVideoMonitor.setVideoConnected(true);
			} else if (intent.getAction().equalsIgnoreCase(VideoController.ACTION_NETMONIOTR_INFO)) {
				String msg = intent.getStringExtra("msg");
				Log.d(TAG, "recv broadcast : video info \r\n" + msg);
			} else if (intent.getAction().equalsIgnoreCase(VideoController.ACTION_VIDEO_QOS_NOTIFY)) {
				int width = intent.getIntExtra("width", 0);
				int height = intent.getIntExtra("height", 0);
				int bitrate = intent.getIntExtra("bitrate", 0) * 1000;
				int fps = intent.getIntExtra("fps", 0); 
				if (width != 0 && height != 0 && bitrate != 0 && fps != 0) {
					mVideoMonitor.resetEncoder(width, height, bitrate, fps);
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
					mVideoMonitor.setVideoConnected(false);
					VideoMonitorService.this.stopSelf();
				}
			} else if (intent.getAction() == TXDeviceService.OnEraseAllBinders) {
				mVideoMonitor.setVideoConnected(false);
				VideoMonitorService.this.stopSelf();
			}
		}
	};
	
	public static interface IVideoMonitor {
		public void start(Service service, String peerId);
		public void stop();
		public void resetEncoder(int width, int height, int bitrate, int fps);
		public void setVideoConnected(boolean videoConnected);
	}
	
	private static class VideoMonitorHW implements IVideoMonitor{
		String					mPeerId;
		boolean					mVideoConnected = false;
		private VideoEncoder		mVideoEncoder 	= null;
		private Object			mObjectMutex  	= new Object();
		private boolean			mResetEncoder 	= false;

		private GLSurfaceView	mGLSurfaceView;
		private SurfaceTexture	mSurfaceTexture;
		private int 				mTextureID 		= -1;
	    private Camera 			mCamera;

	    // parameters for the encoder 
	    private static final String MIME_TYPE	= "video/avc";
	    private static int encWidth				= 640;  
	    private static int encHeight				= 480;
	    private static int encBitRate			= 1000*350;    
	    private static int encFrameRate			= 10;             
	    private static int encIFrameInterval		= 5;	  
	    
		public void start(Service service, String peerId) {
			mPeerId = peerId;
			WindowManager mWindowManager = (WindowManager) service.getApplication()
					.getSystemService(service.getApplication().WINDOW_SERVICE);
			WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
			wmParams.type = LayoutParams.TYPE_PHONE;
			wmParams.format = PixelFormat.RGBA_8888;
			wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
			wmParams.gravity = Gravity.LEFT | Gravity.TOP;
			wmParams.x = -100;
			wmParams.y = -100;
			wmParams.width = 0;
			wmParams.height = 0;

			LayoutInflater inflater = LayoutInflater.from(service.getApplication());
			LinearLayout mFloatLayout = (LinearLayout) inflater.inflate(R.layout.activity_videomonitor_hardcodec, null);
			mWindowManager.addView(mFloatLayout, wmParams);

			mGLSurfaceView = (GLSurfaceView) mFloatLayout.findViewById(R.id.camera_textureview_monitor);
			mGLSurfaceView.setEGLContextClientVersion(2);
			mGLSurfaceView.setRenderer(mRender);
			mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}
		
	    public void stop() {
	        if (mCamera != null) {
	            mCamera.stopPreview();
	            mCamera.release();
	            mCamera = null;
	        }
	        
			synchronized (mObjectMutex) { 
				if (mVideoEncoder != null) {
					mVideoEncoder.stopEncode();
					mVideoEncoder = null;
				}
			}
	    }
	    
	    public void resetEncoder(int width, int height, int bitrate, int fps) {
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
				mResetEncoder = true;
			}
	    }
	    
	    public void setVideoConnected(boolean videoConnected) {
	    		mVideoConnected = videoConnected;
	    }
		
		private GLSurfaceView.Renderer mRender = new GLSurfaceView.Renderer() {
			
			@Override
			public void onSurfaceCreated(GL10 gl, EGLConfig config) {
				// TODO Auto-generated method stub
				mTextureID = VideoEncoder.createTextureID();
				mSurfaceTexture = new SurfaceTexture(mTextureID);
				mSurfaceTexture.setOnFrameAvailableListener(mFrameAvailableListener);
				openCamera(encWidth, encHeight);
			}
			
			@Override
			public void onSurfaceChanged(GL10 gl, int width, int height) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDrawFrame(GL10 gl) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onDrawFrame...");
				GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
				GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
				mSurfaceTexture.updateTexImage();
				
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
		};
		
		private SurfaceTexture.OnFrameAvailableListener mFrameAvailableListener = 
				new SurfaceTexture.OnFrameAvailableListener() {
			@Override
			public void onFrameAvailable(SurfaceTexture arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onFrameAvailable...");
				
				mGLSurfaceView.requestRender(); 
			}
		};
		
	    private void openCamera(int encWidth, int encHeight) {
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
	}
	
	private static class VideoMonitorSF implements IVideoMonitor{
		private VcCamera		    mCamera;
		private SurfaceView		mSurfaceView;
		
		public void start(Service service, String peerId) {
			WindowManager mWindowManager = (WindowManager) service.getApplication()
					.getSystemService(service.getApplication().WINDOW_SERVICE);
			WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
			wmParams.type = LayoutParams.TYPE_PHONE;
			wmParams.format = PixelFormat.RGBA_8888;
			wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
			wmParams.gravity = Gravity.LEFT | Gravity.TOP;
			wmParams.x = -100;
			wmParams.y = -100;
			wmParams.width = 0;
			wmParams.height = 0;

			LayoutInflater inflater = LayoutInflater.from(service.getApplication());
			LinearLayout mFloatLayout = (LinearLayout) inflater.inflate(R.layout.activity_videomonitor_softcodec, null);
			mWindowManager.addView(mFloatLayout, wmParams);
			
			mCamera = VideoController.getInstance().getCamera();

			mSurfaceView = (SurfaceView) mFloatLayout .findViewById(R.id.camera_surfaceView_monitor);
			SurfaceHolder holder = mSurfaceView.getHolder();
			holder.addCallback(mSurfaceHolderListener);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			mSurfaceView.setZOrderMediaOverlay(true);
			
			GLRootView rootView = (GLRootView) mFloatLayout.findViewById(R.id.av_video_gl_root_view_monitor);
			GLVideoView glPeerVideoView = new GLVideoView(service);
			rootView.setContentPane(glPeerVideoView);
			glPeerVideoView.setIsPC(false);
			glPeerVideoView.enableLoading(false);
			glPeerVideoView.setMirror(true);
			glPeerVideoView.setNeedRenderVideo(true);
			glPeerVideoView.setVisibility(GLView.VISIBLE);
			glPeerVideoView.setScaleType(ScaleType.CENTER_CROP);
			glPeerVideoView.setBackground(R.drawable.qav_video_bg_s);
					
			GraphicRenderMgr.getInstance().setGlRender(VideoController.getInstance().GetSelfDin(), glPeerVideoView.getYuvTexture());
		}

		public void stop() {
			VideoController.getInstance().execute(closeCamera, null);
			GraphicRenderMgr.getInstance().setGlRender(VideoController.getInstance().GetSelfDin(), null);
		}

		public void resetEncoder(int width, int height, int bitrate, int fps) {
	
		}

		public void setVideoConnected(boolean videoConnected) {

		}
		
		SurfaceHolder.Callback mSurfaceHolderListener = new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				VideoController.getInstance().execute(openCamera, null);			
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				if (holder.getSurface() == null) {
					return;
				}
				holder.setFixedSize(width, height);
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				
			}
		};
	
	    Runnable openCamera = new Runnable() {
	        @Override
	        public void run() {
	            SurfaceHolder holder = mSurfaceView.getHolder();
	            VideoController.getInstance().execute(new AsyncOpenCamera(holder));
	        }
	    };

	    class AsyncOpenCamera implements Runnable {
	        SurfaceHolder mHolder;
	        public AsyncOpenCamera(SurfaceHolder holder) {
	            mHolder = holder;
	        }

	        @Override
	        public void run() {
	            try {
	                if (!mCamera.openCamera(mHolder)) {
	                    return;
	                } 
	                else {
	                		
	                }
	            } catch (Exception e) {

	            }
	        }
	    }

	    Runnable closeCamera = new Runnable() {
	        @Override
	        public void run() {
	            if (mCamera != null) {
	                mCamera.closeCamera();
	            }
	        }
	    };
	}
}
