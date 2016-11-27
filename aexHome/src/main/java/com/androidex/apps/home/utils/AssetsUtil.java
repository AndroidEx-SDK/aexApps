package com.androidex.apps.home.utils;

import android.content.Context;
import android.content.res.AssetManager.AssetInputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;

public class AssetsUtil {
	private static final String TAG = "AssetsUtil";

	public static String getTxtFromAssets(Context context, String fileName) {
		String result = "";
		try {
			InputStream is = context.getAssets().open(fileName);
			int lenght = is.available();
			byte[]  buffer = new byte[lenght];
			is.read(buffer);
			result = new String(buffer, "utf8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static Bitmap getImgFromAssets(Context context, String fileName) {
		Bitmap bitmap = null;
		try {
			InputStream is = context.getAssets().open(fileName);
			if (is instanceof AssetInputStream) {
				Log.d(TAG, "is instanceof AssetInputStream");
			} else {
				Log.d(TAG, "is not instanceof AssetInputStream");
			}
			bitmap = BitmapFactory.decodeStream(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

}
