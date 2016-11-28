package com.androidex.apps.home.utils;

import android.content.Context;
import android.content.res.AssetManager.AssetInputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;

public class AssetsUtil {
	private static final String TAG = "AssetsUtil";

	/**
	 * 从assets中取文本文件
	 * @param context
	 * @param fileName
       * @return
       */
	public static String getTxtFromAssets(Context context, String fileName) {
		String result = "";
		try {
			InputStream is = context.getAssets().open(fileName);
			int lenght = is.available();
			byte[]  buffer = new byte[lenght];
			is.read(buffer);
			result = new String(buffer, "utf8");
		} catch (Exception e) {
			Log.d(TAG,"==getTxtFromAssets()中抛异常");
		}
		return result;
	}

	/**
	 * 从assets中取图片
	 * @param context
	 * @param fileName
       * @return
       */
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
			return bitmap;
		} catch (Exception e) {
			Log.d(TAG,"==getImgFromAssets()中抛异常");
		}
		return bitmap;
	}


	/**
	 * 从文件中读取图片
	 * @param dst
	 * @param width
	 * @param height
       * @return
       */
	public static Bitmap getBitmapFromFile(String dst, int width, int height) {
		if (null != dst) {
			BitmapFactory.Options opts = null;
			if (width > 0 && height > 0) {
				opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(dst, opts);
				// 计算图片缩放比例
				final int minSideLength = Math.min(width, height);
				opts.inSampleSize = computeSampleSize(opts, minSideLength,
					width * height);
				opts.inJustDecodeBounds = false;
				opts.inInputShareable = true;
				opts.inPurgeable = true;
			}
			try {
				return BitmapFactory.decodeFile(dst, opts);
			} catch (OutOfMemoryError e) {
				Log.d(TAG,"==getBitmapFromFile()中抛异常");
			}
		}
		return null;
	}

	private static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}
	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
			.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
			.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
}
