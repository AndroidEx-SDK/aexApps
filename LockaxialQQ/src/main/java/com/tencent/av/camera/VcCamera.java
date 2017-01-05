package com.tencent.av.camera;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.List;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;


import com.tencent.device.QLog;
//import com.tencent.av.VideoController;
import com.tencent.av.VideoController;
import com.tencent.av.core.VcControllerImpl;
//import com.tencent.av.utils.PhoneStatusTools; 
//import com.tencent.common.app.BaseApplicationImpl;

public class VcCamera extends AndroidCamera{
	
	static{
		PREVIEW_WIDTH = 320;
		PREVIEW_HEIGHT = 240;
	}
	
	public static final String ACTION_PREVIEW_FRAME = "com.tencent.av.camera.VcCamera.previewframe";

	VideoController mVideoCtrl;

	public VcCamera(VideoController qVc) {
		super(qVc.getContext());
		mVideoCtrl = qVc;
		context = mVideoCtrl.getContext();
		devDisplay = ((WindowManager) context
						.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
						
		mbIsTablet = false; //PhoneStatusTools.isTablet(context);
        mIsPreviewInvertedDevices = false; //PhoneStatusTools.isPreviewInvertedDevices();
		
		SDK_VERSION = getVersion();
		DEV_MODEL = android.os.Build.MODEL;
		DEV_MANUFACTURER = android.os.Build.MANUFACTURER;
		Info.orientation = -1;
        Info.rotation = -1;
		
		if (QLog.isColorLevel()) {
		    QLog.d(TAG, QLog.CLR, "Device_Tag = " + DEV_MANUFACTURER + ": " + DEV_MODEL);
		    QLog.d(TAG, QLog.CLR, "Rom_Tag = " + android.os.Build.VERSION.INCREMENTAL);
		}
	}
	
//	public VcCamera(Context context1) {
//		super(context1);
//		context = context1;
//		devDisplay = ((WindowManager) context1
//				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//		SDK_VERSION = getVersion();
//		DEV_MODEL = android.os.Build.MODEL;
//		DEV_MANUFACTURER = android.os.Build.MANUFACTURER;
//		Info.orientation = -1;
//        Info.rotation = -1;
//		
//		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "Device_Tag = " + DEV_MANUFACTURER + ": " + DEV_MODEL);
//		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "Rom_Tag = " + android.os.Build.VERSION.INCREMENTAL);
//	}
	
	@Override
	protected boolean setCameraDisplayOrientation(int cameraId, Camera camera) {
		CameraInformation tempInfo = getCameraDisplayOrientation(cameraId, camera);
//		if (tempInfo.rotation != 0 && mVideoCtrl != null) {
//			mVideoCtrl.getSessionInfo().isCameraLandspace = true;
//		}

		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "getCameraDisplayOrientation orientation:" + tempInfo.orientation + ",rotation:" + tempInfo.rotation);
		}
		int result = 0;

		//预留，新逻辑暂不使用
		CompenSateSendAngle = getSendAngleCompensation();
		CompenSateRecvAngle = getRecvAngleCompensation();
		
		if (tempInfo.facing == 1) {
			result = (getOrientation() + getRotation()) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (getOrientation() - getRotation() + 360) % 360;
		}
		
		if (tempInfo.facing == 1) {
            result = (result + getPreviewAngleForFrontCamera()) % 360;
        } else { // back-facing
            result = (result + getPreviewAngleForBackCamera()) % 360;
        }
		
		// Camera.CameraInfo.CAMERA_FACING_FRONT Constant Value: 1 (0x00000001)

		setDisplayOrientation(camera, result);

		//rotation信息使用sensor得到的，因为sensor先设置rotation，如果此处更新rotation sensor的值不改变90度以上情况下不会重新设置rotation
		Info.facing = tempInfo.facing;
		Info.orientation = tempInfo.orientation;
		
		return true;
	}


//	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void setCameraPara(int w, int h) throws RuntimeException {
		if(camera == null){
			return;
		}
		synchronized (Info) {
			if (fitSdkVersion()) {
				setCameraDisplayOrientation(CameraId, camera);
			} else {
				adjustDirection(camera);
			}
		}
		
		Camera.Parameters parameters = camera.getParameters();
		VcControllerImpl.setCameraParameters(parameters.flatten());
		

		int supportFormat = PixelFormat.UNKNOWN;

		Method getSupportedPreviewFormats = null;
		Method getSupportedPreviewSizes = null;

		try {
			getSupportedPreviewFormats = parameters.getClass().getMethod("getSupportedPreviewFormats", new Class[]{} );
			@SuppressWarnings("unchecked")
			List<Integer> formats = (List<Integer>) getSupportedPreviewFormats.invoke(parameters, (Object[]) null);
			if(formats != null) {
			    for (int i = 0; i < formats.size(); i++) {
	                if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "format: " + formats.get(i));
	            }
				if (formats.contains(17)) //YCbCr_420_SP(deprecated). ImageFormat.NV21
				{
					supportFormat = 17;
				}
				else if (formats.contains(16)) //YCbCr_422_SP(deprecated). ImageFormat.NV16
				{
					supportFormat = 16;
				}
				else if (formats.contains(20)) // YCbCr_422_I(deprecated), ImageFormat.YUY2  YUYV
				{
					supportFormat = 20;
				}
				else if (formats.contains(842094169))  // YV12
				{
					supportFormat = 842094169;
				}			 
				else if (formats.contains(4))//ImageFormat.RGB_565
				{
					supportFormat = 4;
				}
				else if (formats.contains(PixelFormat.YCbCr_420_SP))
				{
					supportFormat = PixelFormat.YCbCr_420_SP;
				}
				else if (formats.contains(PixelFormat.YCbCr_422_SP))
				{
					supportFormat = PixelFormat.YCbCr_422_SP;
				}
				else if (formats.contains(PixelFormat.RGB_888))
				{
					supportFormat = PixelFormat.RGB_888;
				}
				else if (formats.contains(PixelFormat.RGBX_8888))
				{
					supportFormat = PixelFormat.RGBX_8888;
				}
				else if (formats.contains(PixelFormat.RGB_565))
				{
					supportFormat = PixelFormat.RGB_565;
				} 
				// 后面几种实际上不会运行到，先留着
				else if (formats.contains(100)) // YUV420. Tencent custom. To Be Implement
				{
					supportFormat = 100;
				}
				else if (formats.contains(101)) // YVYU. Tencent custom. To Be Implement
				{
					supportFormat = 101;
				}
				else if (formats.contains(102)) // UYVY. Tencent custom. To Be Implement
				{
					supportFormat = 102;
				}
				else if (formats.contains(103)) // VYUY. Tencent custom. To Be Implement
				{
					supportFormat = 103;
				}
				else if (formats.contains(104)) // NV12. Tencent custom. To Be Implement
				{
					supportFormat = 104;
				}
			}
		} catch (Exception e) {
			getSupportedPreviewFormats = null;			
		}

		try {
			getSupportedPreviewSizes = parameters.getClass()
					.getMethod("getSupportedPreviewSizes", new Class[] {});
			if (getSupportedPreviewSizes != null) {
				@SuppressWarnings("unchecked")
				List<Size> frameSizes = (List<Size>) getSupportedPreviewSizes
						.invoke(parameters, (Object[]) null);
				if (frameSizes != null) {
					Size opSize = getOptimalEqualPreviewSize(frameSizes, w, h);
					if (opSize != null) {
						PREVIEW_WIDTH = opSize.width;
						PREVIEW_HEIGHT = opSize.height;
					}
				}
			}
		} catch (Exception e) {
		}

		if (nInFPS == 0) {
			nInFPS = 10;
		}
		
		try {
			Method getSupportedPreviewFrameRates = parameters.getClass()
					.getMethod("getSupportedPreviewFrameRates", new Class[] {});
			if (getSupportedPreviewFrameRates != null) {
				@SuppressWarnings("unchecked")
				List<Integer> frameRates = (List<Integer>) getSupportedPreviewFrameRates
						.invoke(parameters, (Object[]) null);
				if (frameRates != null) {
					int lastValidnInFPS;
					nInFPS = 0;
					for (Integer rate : frameRates) {
						if (rate > 9) {
							lastValidnInFPS = rate;

							if (nInFPS > lastValidnInFPS || nInFPS == 0) {
								nInFPS = lastValidnInFPS;
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}

		if (nInFPS == 0) {
			nInFPS = 10;
		}

		// 后来需要去掉reset
		//VideoChatSettings.reset();
		if (DEV_MANUFACTURER.equalsIgnoreCase("samsung") && DEV_MODEL.equalsIgnoreCase("GT-I9003")
		        || (DEV_MANUFACTURER.equalsIgnoreCase("samsung") && DEV_MODEL.equalsIgnoreCase("GT-I9220"))
		        || (DEV_MANUFACTURER.equalsIgnoreCase("samsung") && DEV_MODEL.equalsIgnoreCase("GT-I7000"))
		        /*||(DEV_MANUFACTURER.equalsIgnoreCase("K-Touch") && DEV_MODEL.contains("T780"))*/
		        ) {
			parameters.setPreviewSize(320, 240);
		} else {
			parameters.setPreviewSize(PREVIEW_WIDTH, 
					PREVIEW_HEIGHT);
		}

		if (supportFormat != PixelFormat.UNKNOWN) {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "supportFormat = " + supportFormat);
			parameters.setPreviewFormat(supportFormat);
		} else {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "supportFormat = 17(default value)");
			supportFormat = 17;
			parameters.setPreviewFormat(supportFormat);
		}

		parameters.setPreviewFrameRate(nInFPS);

		parameters.set("Rotation", 180);
//		if (Build.VERSION.SDK_INT >= 8) {
//			setDisplayOrientation(camera, 90);
//		} else {
//			if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//				parameters.set("orientation", "portrait");
//				parameters.set("rotation", 90);
//			}
//			if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//				parameters.set("orientation", "landscape");
//				parameters.set("rotation", 90);
//			}
//		}
		
		//Enable auto focus when available
//		if(SDK_VERSION >= Build.VERSION_CODES.GINGERBREAD){
//			List<String> modes = camera.getParameters().getSupportedFocusModes();
//			if(modes != null && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
//				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//			}
//		}

		try {
			camera.setParameters(parameters);
		} catch (Exception e) {

		}

		Size frameSize = parameters.getPreviewSize();
		
		int videoFormat = parameters.getPreviewFormat();
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "videoFormat = " + videoFormat);
		PREVIEW_WIDTH = (frameSize.width);
		PREVIEW_HEIGHT = (frameSize.height);
		PREVIEW_FORMAT =(videoFormat);
		
		// 强制修正魅族M9和中兴U880的视频格式
		if (DEV_MANUFACTURER.equalsIgnoreCase("meizu") && DEV_MODEL.equalsIgnoreCase("meizu_m9")) {
			PREVIEW_FORMAT = (18);
		} else if (DEV_MANUFACTURER.equalsIgnoreCase("ZTE") && DEV_MODEL.equalsIgnoreCase("ZTE-T U880")) {
			PREVIEW_FORMAT =(100);
		}
		/*
		if(DEV_MANUFACTURER.equalsIgnoreCase("K-Touch") && DEV_MODEL.contains("T780")){
			//强制修正天宇T780
			VideoChatSettings.format = 4;
		}
		*/
	}

	

	@Override
	protected boolean openFrontCamera() {
		camera = getFrontCamera();
		if (camera == null) {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openFrontCamera camera == null");
			isCameraOpened = false;
			return false;
		}
		
		if (NUM_CAMERA == 0) {
			NUM_CAMERA = 2;
		}
		CUR_CAMERA = FRONT_CAMERA;
		isCameraOpened = true;
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openFrontCamera success");
		return true;
	}
	
	protected boolean openBackCamera() {
		try {
			camera = Camera.open();
		} catch (Exception e) {
			isCameraOpened = false;			
			
			if (camera != null) {
				camera.release();
				camera = null;
			}

			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openBackCamera exception");
			return false;
		}
		if(camera != null){
			CUR_CAMERA = BACK_CAMERA;
			isCameraOpened = true;
			/** 查找设置后置摄像头的CameraId **/
			findBackCameraId();
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openBackCamera success");
			return true;
		}else{
			return false;
		}
	}
	
	/** 查找设置后置摄像头的CameraId add by xuzhouzhang **/
	private void findBackCameraId() {
		CameraId = 0;
		try {
			Class<?> cameraClass = Class.forName("android.hardware.Camera");
			Object cameraInfo = null;
			Field field_facing = null;

			if (NUM_CAMERA == 0) {
				NUM_CAMERA = GetNumberOfCamera();
			}

			if (NUM_CAMERA > 0) {
				Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");

				if (cameraInfoClass != null) {
					cameraInfo = cameraInfoClass.newInstance();
				}

				if (cameraInfo != null) {
					field_facing = cameraInfo.getClass().getField("facing");
				}

				Method getCameraInfo = cameraClass.getMethod("getCameraInfo", int.class, cameraInfoClass);

				if (getCameraInfo != null && cameraInfoClass != null && field_facing != null) {
					for (int camIdx = 0; camIdx < NUM_CAMERA; camIdx++) {
						getCameraInfo.invoke(null, camIdx, cameraInfo);
						int facing = field_facing.getInt(cameraInfo);
						if (facing == 0) { // Camera.CameraInfo.CAMERA_FACING_BACK
							try {
								Method cameraOpen = cameraClass.getMethod("open", int.class);
								if (cameraOpen != null) {
									CameraId = camIdx;
								}
							} catch (RuntimeException e) {
								CameraId = 0;
								if (QLog.isColorLevel()) {
									QLog.e(TAG, QLog.CLR, "openFrontFacingCamera", e);
								}
							}
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			if (QLog.isColorLevel()) {
				QLog.e(TAG, QLog.CLR, "findBackCameraId ClassNotFoundException", e);
			}
		} catch (NoSuchMethodException e) {
			if (QLog.isColorLevel()) {
				QLog.e(TAG, QLog.CLR, "findBackCameraId NoSuchMethodException", e);
			}
		} catch (NoSuchFieldException e) {
			if (QLog.isColorLevel()) {
				QLog.e(TAG, QLog.CLR, "findBackCameraId NoSuchFieldException", e);
			}
		} catch (IllegalAccessException e) {
			if (QLog.isColorLevel()) {
				QLog.e(TAG, QLog.CLR, "findBackCameraId IllegalAccessException", e);
			}
		} catch (InvocationTargetException e) {
			if (QLog.isColorLevel()) {
				QLog.e(TAG, QLog.CLR, "findBackCameraId InvocationTargetException", e);
			}
		} catch (InstantiationException e) {
			if (QLog.isColorLevel()) {
				QLog.e(TAG, QLog.CLR, "findBackCameraId InstantiationException", e);
			}
		} catch (SecurityException e) {
			if (QLog.isColorLevel()) {
				QLog.e(TAG, QLog.CLR, "findBackCameraId SecurityException", e);
			}
		} catch (Exception e) {
			if (QLog.isColorLevel()) {
				QLog.e(TAG, QLog.CLR, "findBackCameraId", e);
			}
		}

	}
	
	public synchronized boolean openCamera(SurfaceHolder holder) {
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openCamera begin.");

//        if (mVideoCtrl != null) {
//            mVideoCtrl.invalidVideoFrameCount = 0;
//            mVideoCtrl.detectCamerahasImage = false;
//        }

		boolean result = false;
		
		do {
			if (isCameraOpened) {
				result = true;
				break;
	        }
			
			if (context == null) {
				if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openCamera context == null");
				result = false;
				break;
			}

			// 第一次打开摄像头
			if (CUR_CAMERA == 0) {
				if (!openFrontCamera()) {
					if(!openBackCamera()) {
						if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openCamera failed");
						result = false;
						break;
					}
				}

				if (camera == null) {
					if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openCamera camera == null");
					result = false;
					break;
				}
				
				setCameraPara(PREVIEW_WIDTH, PREVIEW_HEIGHT);
				result = true;
			} else {
				switch (CUR_CAMERA) {
				case FRONT_CAMERA:
					if (openFrontCamera()) {
						setCameraPara(PREVIEW_WIDTH, PREVIEW_HEIGHT);
						result = true;
					}
					break;
				case BACK_CAMERA:
					if (openBackCamera()) {
						setCameraPara(PREVIEW_WIDTH, PREVIEW_HEIGHT);
						result = true;
					}
					break;
				}
			}
			
			if (!result) {
				CUR_CAMERA = 0;
				break;
			}
			
			try {			
			    camera.setPreviewCallback(cameraCallback);
			    camera.setPreviewDisplay(holder);
			    camera.startPreview();
			    result = true;
			} catch (Exception e) {
				if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "setPreviewDisplay error", e);
				result = false;
			}
		} while (false);
		
		isCameraOpened = result;
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openCamera end.");
		
		return result;
	}
	
	public synchronized boolean switchCamera(SurfaceHolder holder) {
		boolean result = false;
		do {
			if (NUM_CAMERA < 1 || camera == null) {
				result = false;
				break;
			}
			
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR,"switchCamera: " + ((CUR_CAMERA == FRONT_CAMERA) ? "FRONT_CAMERA" : "BACK_CAMERA"));
			closeCamera();
			
			switch(CUR_CAMERA) {
			case FRONT_CAMERA:
				if(openBackCamera()) {
					setCameraPara(PREVIEW_WIDTH, PREVIEW_HEIGHT);
					result = true;
				}
				break;
			case BACK_CAMERA:
				if(openFrontCamera()) {
					setCameraPara(PREVIEW_WIDTH, PREVIEW_HEIGHT);
					result = true;
				}
				break;
			}
			
			if (!result) {
				break;
			}
			
			try {			
			    camera.setPreviewCallback(cameraCallback);
			    camera.setPreviewDisplay(holder);
			    camera.startPreview();
			    result = true;
			} catch (Exception e) {
				if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "setPreviewDisplay error", e);
				result = false;
			}
		} while (false);
		
		return result;
	}
	
	public synchronized boolean reopenCamera(SurfaceHolder holder) {
		boolean result = false;
		do {
			if (NUM_CAMERA < 1 || camera == null) {
				result = false;
				break;
			}
			
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR,"switchCamera: " + ((CUR_CAMERA == FRONT_CAMERA) ? "FRONT_CAMERA" : "BACK_CAMERA"));
			closeCamera();
			
			switch(CUR_CAMERA) {
			case BACK_CAMERA:
				if(openBackCamera()) {
					setCameraPara(PREVIEW_WIDTH, PREVIEW_HEIGHT);
					result = true;
				}
				break;
			case FRONT_CAMERA:
				if(openFrontCamera()) {
					setCameraPara(PREVIEW_WIDTH, PREVIEW_HEIGHT);
					result = true;
				}
				break;
			}
			
			if (!result) {
				break;
			}
			
			try {			
			    camera.setPreviewCallback(cameraCallback);
			    camera.setPreviewDisplay(holder);
			    camera.startPreview();
			    result = true;
			} catch (Exception e) {
				if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "setPreviewDisplay error", e);
				result = false;
			}
		} while (false);
		
		return result;
	}

	public synchronized boolean closeCamera() {
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "closeCamera begin.");
		
		if (camera == null && !isCameraOpened) {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "Camera not open.");
		}
		try {
			if (camera != null) {
				if (isCameraOpened) {
				    camera.setPreviewCallback(null);// 这句也要加，要不然在结束时会crash
					camera.stopPreview();
					camera.release();// 加上这句，就OK
				}
				camera = null;
			}
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "closeCamera Exception", e);
		}
		
		isCameraOpened = false;
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "closeCamera end.");
		return true;
	}
	
	long mStartTime = 0;
	long mTotalFrames = 0;
	long mTotalBytes = 0;
	
	private PreviewCallback cameraCallback = new PreviewCallback() {
        @TargetApi(Build.VERSION_CODES.FROYO)
        public void onPreviewFrame(byte[] data, Camera arg1) {
        		if (context != null) {
	            Intent intent = new Intent(VcCamera.ACTION_PREVIEW_FRAME);
	            intent.putExtra("frame-length", data.length);
	            context.sendBroadcast(intent);
        		}
            
            if (nInFPS > 0) {
                int degree = 0;
                if (fitSdkVersion()) {
                    int result = 0;
                    if (CUR_CAMERA == FRONT_CAMERA) {
                        int rotation = devDisplay.getRotation() * 90;
                        int orientation = getOrientation();
                        result = (rotation + orientation) % 360;
                        result = (360 - result) % 360; // compensate the mirror
                    } else if (CUR_CAMERA == BACK_CAMERA) { // back-facing
                        int rotation = devDisplay.getRotation() * 90;
                        int orientation = getOrientation();
                        result = (orientation - rotation + 360) % 360;
                    }

                    degree = getRotation() + result;
                    
                    if (getOrientation() == 270 || getOrientation() == 90) {
                        if (getRotation() % 180 == 0) {
                            if (CUR_CAMERA == FRONT_CAMERA) {
                                /*
                                //ZTE手机4.0以下不转180
                                if (!DEV_MANUFACTURER.equalsIgnoreCase("ZTE")) {
                                    degree += 180;
                                } else if (SDK_VERSION >= 14) {
                                    degree += 180;
                                }
                                */
//                                if(!PhoneStatusTools.get(context,"ro.qq.orientation").equalsIgnoreCase("ZTE")
//                                        || mIsPreviewInvertedDevices){
//                                    degree += 180;
//                                }
                            }
                        }
                    } else if (getOrientation() == 0 || getOrientation() == 180) {
                        if (getRotation() == 90 || getRotation() == 270) {
                            if (CUR_CAMERA == FRONT_CAMERA && !mbIsTablet) {
                                degree += 180;
                            }
                        }else{
                        	if (CUR_CAMERA == FRONT_CAMERA && mbIsTablet) {
	                        	degree += 180;
                        	}
                        }
                    }
                    
					if (CUR_CAMERA == FRONT_CAMERA) {
						if (mFrontCameraAngle > 0) {
							degree = 360 - mFrontCameraAngle + degree;
						} else {
							degree += getRemoteAngleForFrontCamera(getRotation());
						}
					} else {
						if (mBackCameraAngle > 0) {
							degree += mBackCameraAngle;
						} else {
							degree += getRemoteAngleForBackCamera(getRotation());
						}
					}
                } else {
                    degree = (getRotation() + CompenSateRecvAngle + 90) % 360;

					if (getOrientation() == 270 || getOrientation() == 90) {
						//if (getRotation() % 180 == 0) {
							if (CUR_CAMERA == FRONT_CAMERA) {
								degree += 90;
							} else {
								degree += 180;
							}
						//} else {
						//	if (CUR_CAMERA == FRONT_CAMERA) {
						//		degree += 90;
						//	} else {
						//		degree += 180;
						//	}
						//}
					}
                    
                    if (CUR_CAMERA == FRONT_CAMERA) {
                        /*
                        //ZTE手机4.0以下不转180
                        if (!DEV_MANUFACTURER.equalsIgnoreCase("ZTE")) {
                            degree += 180;
                        } else if (SDK_VERSION >= 14) {
                            degree += 180;
                        }
                        */
                        
//						if (!PhoneStatusTools.get(context, "ro.qq.orientation").equalsIgnoreCase("ZTE")
//                                || mIsPreviewInvertedDevices) {
//							degree += 180;
//						}
                    } else {
                        degree += 180;
                    }
                    
					if (CUR_CAMERA == FRONT_CAMERA) {
						if (mFrontCameraAngle > 0) {
							degree = 360 - mFrontCameraAngle + degree;
						} else {
							degree += getRemoteAngleForFrontCamera(getRotation());
						}
					} else {
						if (mBackCameraAngle > 0) {
							degree += mBackCameraAngle;
						} else {
							degree += getRemoteAngleForBackCamera(getRotation());
						}
					}
                }
                
//				if (mVideoCtrl.getSessionInfo().isCameraLandspace && CUR_CAMERA == FRONT_CAMERA) {
//					degree -= 180;
//					if (degree < 0) {
//						degree += 360;
//					}
//				}
    			
        		degree %= 360;
                degree /= 90;

                if (mVideoCtrl != null) {
                    mVideoCtrl.OnPreviewData(data, degree, nInFPS, CUR_CAMERA == FRONT_CAMERA);
                }
                if (mCallback != null) {
                    mCallback.onPreviewData(data, degree, nInFPS, CUR_CAMERA == FRONT_CAMERA);
                }
                
                if (mStartTime == 0)
                {
                		mStartTime = System.currentTimeMillis();
                }
                
                mTotalFrames++;
                mTotalBytes += data.length;
                long time = (System.currentTimeMillis() - mStartTime) / 1000;
                if (time > 0)
                {
                		Log.d(TAG, "frame-fps: " + mTotalBytes / time + "  " + mTotalFrames / time);
                }
            }
        }
    };
	
	
	
 
}