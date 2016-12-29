package com.tencent.devicedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressLint("NewApi") 
public class VideoMessageActivity extends Activity  implements OnClickListener, SurfaceHolder.Callback{
	private long peerTinyId = 0;
	
	private boolean 			bRecording	= false;
	private int 				width 		= 640;
	private int 				height		= 480;
	private int             fps          = 0;
	
	private Camera 			camera = null;
	private MediaRecorder	mediarecorder;
    private SurfaceView		surfaceview;
	Button btn_record;
	Button btn_send;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //全屏设置，隐藏窗口所有装饰
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//清除FLAG
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //super.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_videomessage);

        //if(getIntent().getBooleanExtra("back", false))
        {
            if(getActionBar() != null)
                getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        peerTinyId = intent.getLongExtra("tinyid", 0);
        
        btn_send = (Button) findViewById(R.id.btn_videomsg_send);
        btn_record = (Button) findViewById(R.id.btn_videomsg_record);
		btn_send.setOnClickListener(this);
		btn_record.setOnClickListener(this);
		
		IntentFilter filter = new IntentFilter(); 
		filter.addAction(TXDeviceService.BinderListChange);
		filter.addAction(TXDeviceService.OnEraseAllBinders);
		registerReceiver(mBroadcastHandler, filter);
		
		surfaceview = (SurfaceView) this.findViewById(R.id.surfaceView);
		SurfaceHolder holder = surfaceview.getHolder();// 取得holder
		holder.addCallback(this); // holder加入回调接口
		// setType必须设置，要不出错.
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.unregisterReceiver(mBroadcastHandler);
		closeCamera();
		if (mediarecorder != null) {
			mediarecorder.stop();
			mediarecorder.release();
			mediarecorder = null;
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

    private BroadcastReceiver mBroadcastHandler = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == TXDeviceService.BinderListChange) {
				boolean bFind = false;
				Parcelable[] listBinder = intent.getExtras().getParcelableArray("binderlist");
				for (int i = 0; i < listBinder.length; ++i){
					TXBinderInfo  binder = (TXBinderInfo)(listBinder[i]);
					if (binder.tinyid == peerTinyId) {
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
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		String videoFile = this.getCacheDir().getAbsolutePath() + "/love.mp4";
		String imageFile = this.getCacheDir().getAbsolutePath() + "/love.png";
		switch(keyCode){
			case KeyEvent.KEYCODE_1:
			case R.id.btn_videomsg_record:
			{
				if (bRecording == false)
				{
					btn_send.setEnabled(false);
					bRecording = true;
					btn_record.setText("结束(3键)");

					File f = new File(videoFile);
					if (f.exists()) {
						f.delete();
					}

					if (camera == null) {
						openCamera(surfaceview.getHolder());
					}

					try {
						camera.unlock();
					}
					catch (Exception e1) {
						e1.printStackTrace();
					}

					mediarecorder = new MediaRecorder();// 创建mediarecorder对象
					// 设置录制视频源为Camera(相机)
					mediarecorder.setCamera(camera);
					mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
					mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

					//mediarecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
					// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
					mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
					// 设置录制的视频编码h263 h264
					//mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
					mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
					//mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
					// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
					mediarecorder.setVideoSize(width, height);
					//mediarecorder.setVideoEncodingBitRate(bitRat);
					// 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
//					if (fps != 0) {
//						mediarecorder.setVideoFrameRate(fps);
//					}
					mediarecorder.setOrientationHint(90);
					mediarecorder.setPreviewDisplay(surfaceview.getHolder().getSurface());
					// 设置视频文件输出的路径
					mediarecorder.setOutputFile(videoFile);

					try {
						mediarecorder.prepare();
						mediarecorder.start();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
				return true;
			case KeyEvent.KEYCODE_2:
				if(btn_send.isEnabled())
			{
				TXDeviceService.sendVideoMsg(videoFile, imageFile, "视频留言", "收到一条视频留言", "点击查看", 1, null);
			}
			return true;

			case KeyEvent.KEYCODE_3:

				bRecording = false;
				btn_record.setText("录制(1键)");
				btn_send.setEnabled(true);
				try {
					if (mediarecorder != null) {
						mediarecorder.stop();
						mediarecorder.release();
						mediarecorder = null;
					}

					camera.lock();
					camera.stopPreview();
					camera.release();
					camera = null;
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				createtVideoThumbnail(videoFile, imageFile, width, height, MediaStore.Images.Thumbnails.MICRO_KIND);

				return  true;

			case KeyEvent.KEYCODE_DEL:
				finish();
				break;
		}

		return super.onKeyUp(keyCode, event);
	}

	@SuppressLint("InlinedApi") @Override
	public void onClick(View v) {
		String videoFile = this.getCacheDir().getAbsolutePath() + "/love.mp4";
		String imageFile = this.getCacheDir().getAbsolutePath() + "/love.png";
		switch (v.getId()) {
			case R.id.btn_videomsg_record:
			{
				if (bRecording == false)
				{ 
					bRecording = true;
					Button btnRecord = (Button)v;
					btnRecord.setText("结束");
					
			        File f = new File(videoFile);
			        if (f.exists()) {
			        		f.delete();
			        }
			        
					if (camera == null) {
						openCamera(surfaceview.getHolder());
					}
					
					try {		
						camera.unlock();
					} 
					catch (Exception e1) {
						e1.printStackTrace();
					}
					
					mediarecorder = new MediaRecorder();// 创建mediarecorder对象
					// 设置录制视频源为Camera(相机)
					mediarecorder.setCamera(camera);
					mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
					mediarecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
					
					//mediarecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
					// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
					mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
					// 设置录制的视频编码h263 h264
					//mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
					mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
					//mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
					// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
					mediarecorder.setVideoSize(width, height);  
					//mediarecorder.setVideoEncodingBitRate(bitRat);
					// 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错 
//					if (fps != 0) {
//						mediarecorder.setVideoFrameRate(fps);
//					}
					mediarecorder.setOrientationHint(90); 
					mediarecorder.setPreviewDisplay(surfaceview.getHolder().getSurface());
					// 设置视频文件输出的路径
					mediarecorder.setOutputFile(videoFile); 

					try {
						mediarecorder.prepare();
						mediarecorder.start();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else
				{
					bRecording = false;
					Button btnRecord = (Button)v;
					btnRecord.setText("录制");
					
					try {
						if (mediarecorder != null) {
							mediarecorder.stop();
							mediarecorder.release();
							mediarecorder = null;
						}
						
						camera.lock();
						camera.stopPreview(); 
						camera.release();
						camera = null;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					createtVideoThumbnail(videoFile, imageFile, width, height, MediaStore.Images.Thumbnails.MICRO_KIND);
				}
			}
			break;
			case R.id.btn_videomsg_send:
			{
				TXDeviceService.sendVideoMsg(videoFile, imageFile, "视频留言", "收到一条视频留言", "点击查看", 1, null);
			} 
			break;
		}
	}
	
	protected void createtVideoThumbnail(String videoPath, String imagePath, int width, int height, int kind) {  
        Bitmap bitmap = null;  
        // 获取视频的缩略图  
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);  
//        System.out.println("w: "+bitmap.getWidth());  
//        System.out.println("h: "+bitmap.getHeight());  
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
        
        File f = new File(imagePath);
        if (f.exists()) {
        		f.delete();
        }
        try {
	         FileOutputStream out = new FileOutputStream(f);
	         bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
	         out.flush();
	         out.close();
        } 
        catch (Exception e) {
        		e.printStackTrace();
        }
    }  
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		openCamera(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}
	
	private void openCamera(SurfaceHolder holder) {
		try {
			Camera.CameraInfo info = new Camera.CameraInfo();
			int numCameras = Camera.getNumberOfCameras();
	        for (int i = 0; i < numCameras; i++) {
	            Camera.getCameraInfo(i, info);
	            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	            	camera = Camera.open(i);
	                break;
	            }
	        }
	        
	        if (camera == null) {
	            camera = Camera.open();
	        }
	        
	        if (camera == null) {
	        		Toast.makeText(getApplicationContext(), "摄像头打开失败", Toast.LENGTH_LONG).show();
	        		return;
	        }

		    Camera.Parameters parms = camera.getParameters();
			for (Camera.Size size : parms.getSupportedPreviewSizes()) {
				if (size.width == width && size.height == height) {
					parms.setPreviewSize(width, height);
					break;
				}
			}
//			Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
//			if (ppsfv != null) {
//				parms.setPreviewSize(ppsfv.width, ppsfv.height);
//				width = ppsfv.width;
//				height = ppsfv.height;
//			}
			
	    		List<int[]> fpsRanges = parms.getSupportedPreviewFpsRange();
	    		if (fpsRanges.size() > 0) {
	    			int [] range = fpsRanges.get(0);
	    			if (range != null) {
	    				parms.setPreviewFpsRange(range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
	    				fps = range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] / 1000;
	    			}
	    		}

			camera.setParameters(parms);
			camera.setDisplayOrientation(0);
		    camera.setPreviewDisplay(holder);
			camera.startPreview();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void closeCamera() {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
			bRecording = false;
		}
	}
}
