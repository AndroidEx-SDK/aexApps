package com.androidex.aexsettings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class MyVideoView extends VideoView {

	public MyVideoView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		/**//*
			 * if (mVideoWidth > 0 && mVideoHeight > 0) { if ( mVideoWidth *
			 * height > width * mVideoHeight ) { //Log.i("@@@",
			 * "image too tall, correcting"); height = width * mVideoHeight
			 * / mVideoWidth; } else if ( mVideoWidth * height < width *
			 * mVideoHeight ) { //Log.i("@@@",
			 * "image too wide, correcting"); width = height * mVideoWidth /
			 * mVideoHeight; } else { //Log.i("@@@",
			 * "aspect ratio is correct: " + //width+"/"+height+"="+
			 * //mVideoWidth+"/"+mVideoHeight); } }
			 */
		// Log.i("@@@@@@@@@@", "setting size: " + width + 'x' + height);
		setMeasuredDimension(width, height);
	}
}
