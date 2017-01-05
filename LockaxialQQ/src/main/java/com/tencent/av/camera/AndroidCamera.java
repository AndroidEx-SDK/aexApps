package com.tencent.av.camera;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

//import com.tencent.av.config.ConfigSystemImpl;
//import com.tencent.av.utils.PhoneStatusTools;
//import com.tencent.mobileqq.utils.kapalaiadapter.KapalaiAdapterUtil;
//import com.tencent.mobileqq.utils.kapalaiadapter.MobileIssueSettings;
import com.tencent.device.QLog;

public class AndroidCamera {
	static final String TAG = "VcCamera";

	Camera camera = null;

	static int nInFPS;

	Context context = null;

	static CameraInformation Info = new CameraInformation();

	static boolean isCameraOpened = false;

	final static int FRONT_CAMERA = 1;
	final static int BACK_CAMERA = 2;
	
	int CUR_CAMERA = 0;
	int NUM_CAMERA = 0;
	
	int SDK_VERSION;// = android.os.Build.VERSION.SDK_INT;
	String DEV_MODEL;// = android.os.Build.MODEL;
	String DEV_MANUFACTURER;// = android.os.Build.MANUFACTURER;
	//VideoController mVideoCtrl;
	Display devDisplay;
	int CameraId;

	int CompenSateRecvAngle = 0;
	int CompenSateSendAngle = 0;
	
	//保存在用户本地的sharepreference的配置
	int mFrontCameraAngle = 0;
	int mBackCameraAngle = 0;
	boolean mbIsTablet = false;
	boolean mIsPreviewInvertedDevices = false;
	
	public static int PREVIEW_WIDTH = 640;
	public static int PREVIEW_HEIGHT = 480;
	public static int PREVIEW_FORMAT = 17;
	
	
	public static int getVersion() {
		return android.os.Build.VERSION.SDK_INT;
	}
	
	public int getCompenSateSendAngle() {
		return CompenSateSendAngle;
	}
	
	public int getCompenSateRecvAngle() {
		return CompenSateRecvAngle;
	}
	
	
	public AndroidCamera(Context context1) {
		context = context1;
		devDisplay = ((WindowManager) context1
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		SDK_VERSION = getVersion();
		DEV_MODEL = android.os.Build.MODEL;
		DEV_MANUFACTURER = android.os.Build.MANUFACTURER;
		Info.orientation = -1;
        Info.rotation = -1;
		
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "Device_Tag = " + DEV_MANUFACTURER + ": " + DEV_MODEL);
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "Rom_Tag = " + android.os.Build.VERSION.INCREMENTAL);
	}
	
	//
	// Specify Camera Invoke(Reflect) Function (Manufacture such as Moto, )
	// SDK VERSION 2.2 AND BELOW
	//
	protected static ArrayList<Integer> splitInt(String paramString) {
		if (paramString == null)
			return null;
		StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ",");
		ArrayList<Integer> localArrayList = new ArrayList<Integer>();
		while (localStringTokenizer.hasMoreElements()) {
			Integer localInteger = Integer.valueOf(Integer
					.parseInt(localStringTokenizer.nextToken()));
			localArrayList.add(localInteger);
		}
		return localArrayList;
	}

	protected Camera trySamsungFrontCamera() {
		Camera c = camera;
		try {
			if (c != null) {
			    c.release();
			}
			
			c = Camera.open();
			if (c == null) {
				return null;
			}

			Camera.Parameters params = c.getParameters();
			params.set("camera-id", 2);
			
			c.setParameters(params);
			camera = c;
			return c;
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "trySamsungFrontCamera", e);
		}

		return null;
	}

	protected Camera tryMotoFrontCamera() {
		Camera camera2 = null;
		Camera c = camera;
		try {
		    if (c != null) {
                c.release();
            }
		    
			c = Camera.open();
			if (c == null) {
				return null;
			}

			camera2 = c;
			Method m = c.getClass().getMethod("getCustomParameters",
					new Class[] {});
			Camera.Parameters localParameters = (Camera.Parameters) m.invoke(c,
					new Object[] {});

			ArrayList<Integer> localArrayList = splitInt(localParameters.get("camera-sensor-values"));
			Method m2 = c.getClass().getMethod("setCustomParameters",
					new Class[] { localParameters.getClass() });

			if (localArrayList != null) {
				if (localArrayList.indexOf(1) != -1) {
					localParameters.set("camera-sensor", "1");
					m2.invoke(c, new Object[] { localParameters });
					camera2 = c;
				}
			}
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "tryMotoFrontCamera", e);
			if (c != null) {
				c.release();
			}
			return null;
		}

		return camera2;
	}

	protected Camera getFrontCamera() {
		if (fitSdkVersion()) {
			return openFrontFacingCamera();
		}

		if (DEV_MANUFACTURER.equalsIgnoreCase("motorola")) {
			// if(DEV_MODEL.equalsIgnoreCase("me860") ||
			// DEV_MODEL.equalsIgnoreCase("mb860"))
			{
				return tryMotoFrontCamera();
			}
		} else if (DEV_MANUFACTURER.equalsIgnoreCase("samsung")) {
			return trySamsungFrontCamera();
		}	

		return null;
	}

	protected void adjustDirection(Camera c) {
		try {
			Method m = c.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
			int angle = 0;
            if (CUR_CAMERA == FRONT_CAMERA) {
                angle = getPreviewAngleForFrontCamera() % 360;
            } else { // back-facing
                angle = getPreviewAngleForBackCamera() % 360;
            }
			m.invoke(c, new Object[] {angle});
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "adjustDirection", e);
		}
	}

	//
	// General Camera Invoke Function
	// SDK VERSION 2.3 AND ABOVE
	//
	protected int GetNumberOfCamera() {
//		if(!MobileIssueSettings.isDefaultgetNumberOfCamerasSuccess){
//			return KapalaiAdapterUtil.getKAUInstance().getNumberOfCamera();
//		}
		int CameraCnt = 1;
		//boolean InitState = false;

		try {
			//if (camera == null) {
			//	InitState = true;
			//	camera = Camera.open();
			//}
			//Camera.Parameters params = camera.getParameters();
			//Method NumOfCamera = camera.getClass().getMethod("getNumberOfCameras", new Class[] {});

			//Object o = NumOfCamera.invoke(params, (Object[]) null);
			//CameraCnt = Integer.parseInt(o.toString());
			
			Class<?> cameraClass = Class.forName("android.hardware.Camera");
			Method getNumOfCamera = cameraClass.getMethod("getNumberOfCameras", new Class[] {});
			Object result = getNumOfCamera.invoke(null, (Object[]) null);
			CameraCnt = Integer.parseInt(result.toString());
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "GetNumberOfCamera", e);
		}

		//if (InitState) {
		//	camera.release();
		//	camera = null;
		//}

		return CameraCnt;
	}

	protected void setDisplayOrientation(Camera camera, int degree) {
		try {
			Method m = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
			m.invoke(camera, new Object[] { degree });
		} catch (Exception e) {
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "setDisplayOrientation", e);
		}
	}

	protected Camera openFrontFacingCamera() {
		Camera c = null;
		CameraId = 0;

		// Look for front-facing camera, using the Gingerbread API.
		// Java reflection is used for backwards compatibility with
		// pre-Gingerbread APIs.

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

				Method getCameraInfo = cameraClass.getMethod("getCameraInfo",
						int.class, cameraInfoClass);

				if (getCameraInfo != null && cameraInfoClass != null && field_facing != null) {
					for (int camIdx = 0; camIdx < NUM_CAMERA; camIdx++) {
						getCameraInfo.invoke(null, camIdx, cameraInfo);
						int facing = field_facing.getInt(cameraInfo);
						if (facing == 1) { // Camera.CameraInfo.CAMERA_FACING_FRONT
							try {
								Method cameraOpen = cameraClass.getMethod("open", int.class);
								if (cameraOpen != null) {
									c = (Camera) cameraOpen.invoke(null, camIdx);
									CameraId = camIdx;
								}
							} catch (RuntimeException e) {
								c = null;
								CameraId = 0;
								if(QLog.isColorLevel()){
									QLog.e(TAG, QLog.CLR, "openFrontFacingCamera", e);
								}
							}
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			if(QLog.isColorLevel()){
				QLog.e(TAG, QLog.CLR, "openFrontFacingCamera ClassNotFoundException", e);
			}
		} catch (NoSuchMethodException e) {
			if(QLog.isColorLevel()){
				QLog.e(TAG, QLog.CLR, "openFrontFacingCamera NoSuchMethodException", e);
			}
		} catch (NoSuchFieldException e) {
			if(QLog.isColorLevel()){
				QLog.e(TAG, QLog.CLR, "openFrontFacingCamera NoSuchFieldException", e);
			}
		} catch (IllegalAccessException e) {
			if(QLog.isColorLevel()){
				QLog.e(TAG, QLog.CLR, "openFrontFacingCamera IllegalAccessException", e);
			}
		} catch (InvocationTargetException e) {
			if(QLog.isColorLevel()){
				QLog.e(TAG, QLog.CLR, "openFrontFacingCamera InvocationTargetException", e);
			}
		} catch (InstantiationException e) {
			if(QLog.isColorLevel()){
				QLog.e(TAG, QLog.CLR, "openFrontFacingCamera InstantiationException", e);
			}
		} catch (SecurityException e) {
			if(QLog.isColorLevel()){
				QLog.e(TAG, QLog.CLR, "openFrontFacingCamera SecurityException", e);
			}
		} catch (Exception e) {
			if(QLog.isColorLevel()){
				QLog.e(TAG, QLog.CLR, "openFrontFacingCamera", e);
			}
		}

		return c;
	}

	 static class CameraInformation {
		int facing;
		int orientation;
		int rotation;
	}

	protected CameraInformation getCameraDisplayOrientation(int cameraId, Camera camera) {
		CameraInformation tempInfo = new CameraInformation();
		try {
			Class<?> cameraClass = Class.forName("android.hardware.Camera");
			Object cameraInfo = null;
			Field field_facing = null;
			Field field_orientation = null;

			Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");
			if (cameraInfoClass == null) {
				tempInfo.rotation = -1;
				tempInfo.orientation = -1;
				return tempInfo;
			}

			cameraInfo = cameraInfoClass.newInstance();
			if (cameraInfo == null) {
				tempInfo.rotation = -1;
				tempInfo.orientation = -1;
				return tempInfo;
			}

			field_facing = cameraInfo.getClass().getField("facing");
			field_orientation = cameraInfo.getClass().getField("orientation");

			if (field_facing == null || field_orientation == null) {
				tempInfo.rotation = -1;
				tempInfo.orientation = -1;
				return tempInfo;
			}

			Method getCameraInfo = cameraClass.getMethod("getCameraInfo",
					int.class, cameraInfoClass);

			if (getCameraInfo == null) {
				tempInfo.rotation = -1;
				tempInfo.orientation = -1;
				return tempInfo;
			}

			getCameraInfo.invoke(null, cameraId, cameraInfo);
			tempInfo.facing = field_facing.getInt(cameraInfo);
			tempInfo.orientation = field_orientation.getInt(cameraInfo);
			

			if (devDisplay == null) {
				tempInfo.rotation = -1;
				return tempInfo;
			}

			Method getRotation = devDisplay.getClass().getMethod("getRotation");

			if (getRotation == null) {
				tempInfo.rotation = -1;
				return tempInfo;
			}

			Object rotation = getRotation.invoke(devDisplay, (Object[]) null);

			switch (Integer.parseInt(rotation.toString())) {
			case Surface.ROTATION_0:
				tempInfo.rotation = 0;
				break;
			case Surface.ROTATION_90:
				tempInfo.rotation = 90;
				break;
			case Surface.ROTATION_180:
				tempInfo.rotation = 180;
				break;
			case Surface.ROTATION_270:
				tempInfo.rotation = 270;
				break;
			}		

			return tempInfo;
		} catch (Exception e) {
			tempInfo.rotation = 0;
			//Info.orientation = 270;
			return tempInfo;
		}
	}

	protected int getSendAngleCompensation() {
		return 0;
	}
	
	protected int getRecvAngleCompensation() {
		return 0;
	}
	
	protected boolean setCameraDisplayOrientation(int cameraId, Camera camera) {
		CameraInformation tempInfo = getCameraDisplayOrientation(cameraId, camera);

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

	protected Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;
		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = h;
		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		} // Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	protected Size getOptimalEqualPreviewSize(List<Size> sizes, int w, int h) {
		Size optimalSize = null;
		
		/*
		int MaxLength = Math.min(screen_width, screen_height);
		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			if (size.width == size.height && size.width <= MaxLength) {
				if (optimalSize == null || optimalSize.width < size.width) {
					optimalSize = size;
				}
			}
		}
		*/
		
		for (Size size : sizes) {
			if (size.width == w && size.height == h) {
				optimalSize = size;
				if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "previewsize ,w= " + w +",h=" +h);
				return optimalSize;
			}
		}
		//not find QVGA
		if(w == 320 &&  h == 240){
			w = 640;
			h = 480;
			//find VGA
			for (Size size : sizes) {
				if (size.width == w && size.height == h) {
					optimalSize = size;
					if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "previewsize ,w= " + w +",h=" +h);
					return optimalSize;
				}
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			optimalSize = getOptimalPreviewSize(sizes,
					PREVIEW_WIDTH, PREVIEW_HEIGHT);
		}

		return optimalSize;
	}
	
	/*
	public static final int YV12 = 842094169; //for SDK >= 9
	public static final int I420 = 542094169; //protected data
	public boolean has_previewformat(int f){
		Camera.Parameters parameters = camera.getParameters();
		if(parameters == null){
			return false;
		}
		if(Build.VERSION.SDK_INT <= 8){
			GlStringParser p = new GlStringParser('=',';');
			p.unflatten(parameters.flatten());
			String pfv = p.get("preview-format-values");
			if(pfv == null){
				return false;
			}

			String strf = "yuv420sp";
			switch(f){
			case ImageFormat.NV21:
				break;
			case YV12:
				strf = "yvu420p";
				break;
			case I420:
				strf = "yuv420p";
				break;
			}
			if(pfv.contains(strf)){
				return true;
		  }											
		}else{
			List<Integer> spf =parameters.getSupportedPreviewFormats();
			if(spf == null){
				return false;
			}
			for(int i = 0 ; i < spf.size(); i++){
				if(f == spf.get(i)){
					return true;
				}
			}
		}
		return false;
		
	}
	*/

//	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
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
		//VcControllerImpl.setCameraParameters(parameters.flatten());
		

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
						PREVIEW_WIDTH  = opSize.width;
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

	//
	// General Camera Class Interface
	// For outside call up usage
	//
	protected boolean fitSdkVersion() {
		return (SDK_VERSION >= 10);
	}

	public int getOrientation() {
		if (Info.orientation == -1) {
			if (CUR_CAMERA == FRONT_CAMERA) {
				return 270;
			} else if (CUR_CAMERA == BACK_CAMERA) {
				return 90;
			}
		}
		return Info.orientation;
	}

	public void setRotation(int rotation) {
		Info.rotation = (rotation + CompenSateSendAngle) % 360;//(rotation+270)%360;
	}

	public int getRotation() {
		if (Info.rotation == -1) {
			return 0;
		}
		return Info.rotation;
	}
	
	public Camera getCamera() {
		return camera;
	}
	
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
			CameraId = 0;
			if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openBackCamera success");
			return true;
		}else{
			return false;
		}
	}
	
	public synchronized boolean openCamera(SurfaceHolder holder) {
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openCamera begin.");

     
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
	
	public synchronized boolean openCamera(SurfaceTexture surfaceTexture, int width, int height) {
		if(QLog.isColorLevel()) QLog.d(TAG, QLog.CLR, "openCamera begin.");

     
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
				
				setCameraPara(width, height);
				result = true;
			} else {
				switch (CUR_CAMERA) {
				case FRONT_CAMERA:
					if (openFrontCamera()) {
						setCameraPara(width, height);
						result = true;
					}
					break;
				case BACK_CAMERA:
					if (openBackCamera()) {
						setCameraPara(width, height);
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
				camera.setDisplayOrientation(0);
			    camera.setPreviewTexture(surfaceTexture);
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
	
	protected PreviewCallback cameraCallback = new PreviewCallback() {
        @TargetApi(Build.VERSION_CODES.FROYO)
        public void onPreviewFrame(byte[] data, Camera arg1) {
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
//                                if(!PhoneStatusTools.get(context,"ro.qq.orientation").equalsIgnoreCase("ZTE")){
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
                        
//						if (!PhoneStatusTools.get(context, "ro.qq.orientation").equalsIgnoreCase("ZTE")) {
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

                if (mCallback != null) {
                    mCallback.onPreviewData(data, degree, nInFPS, CUR_CAMERA == FRONT_CAMERA);
                }
            }
        }
    };
	
	public boolean isFrontCamera() {
		//摄像头没初始化前默认为前置，否则会导致其他模块算出的角度默认值有问题
		if(CUR_CAMERA == 0)
		{
			return true;
		}
		return CUR_CAMERA == FRONT_CAMERA;
	}
	
	public int getCameraNum() {
		return GetNumberOfCamera();
	}
	
	
    int getRemoteAngleForFrontCamera(int baseAngle) {
//        byte angle;
//        switch (baseAngle) {
//        case 0:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_0;
//            break;
//        case 90:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_90;
//            break;
//        case 180:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_180;
//            break;
//        case 270:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_270;
//            break;
//        default:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_0;
//            break;
//        }
//        
//        return ConfigSystemImpl.GetAngleForCamera(context, true, false, angle) * 90;
        return 0;
    }
    
    int getRemoteAngleForBackCamera(int baseAngle) {
//        byte angle;
//        switch (baseAngle) {
//        case 0:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_0;
//            break;
//        case 90:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_90;
//            break;
//        case 180:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_180;
//            break;
//        case 270:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_270;
//            break;
//        default:
//            angle = ConfigSystemImpl.DEFLECT_ANGLE_0;
//            break;
//        }
//        
//        return ConfigSystemImpl.GetAngleForCamera(context, false, false, angle) * 90;
        return 0;
    }
    
    int getPreviewAngleForFrontCamera() {
//    	  return (360 - ConfigSystemImpl.GetAngleForCamera(context, true, true, ConfigSystemImpl.DEFLECT_ANGLE_0) * 90);
    		return 0;
    }
    
    int getPreviewAngleForBackCamera() {
//        return ConfigSystemImpl.GetAngleForCamera(context, false, true, ConfigSystemImpl.DEFLECT_ANGLE_0) * 90;
        return 0;
    }

    public void setCameraAngleFix(boolean isFront, int angle) {
        if (isFront) {
            mFrontCameraAngle = angle % 360;
        } else {
            mBackCameraAngle = angle % 360;
        }

		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "mFrontCameraAngle: " + mFrontCameraAngle 
					+ ", mBackCameraAngle: " + mBackCameraAngle);
		}
    }
    
    public int getUserCameraAngle(boolean isFront) {
    	return isFront == true ? mFrontCameraAngle:mBackCameraAngle;
    }

    VcPreviewCallback mCallback = null;
    public interface VcPreviewCallback {
        void onPreviewData(byte[] data, int angle, long SPF, boolean isFront);
    }
    public void setVcPreviewCallback(VcPreviewCallback callback) {
        mCallback = callback;
    }
}
