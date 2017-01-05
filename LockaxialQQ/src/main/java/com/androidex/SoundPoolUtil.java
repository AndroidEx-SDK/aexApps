package com.androidex;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

import com.tencent.devicedemo.R;

/**
 * 播放数字音频工具
 * 
 * @author Kevin
 *
 */
public class SoundPoolUtil {

	private Context mContext;

	private static SoundPoolUtil soundPoolUtil = null;
	private SoundPool soundPool;
	private int streamID;
	private int outgoing;

	public static SoundPoolUtil getSoundPoolUtil() {
		if (soundPoolUtil == null) {
			soundPoolUtil = new SoundPoolUtil();
		}
		return soundPoolUtil;
	}

	// public SoundPoolUtil() {
	// soundPool = new SoundPool(100, AudioManager.STREAM_RING, 0);
	// soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
	//
	// @Override
	// public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
	// {
	// playVoice(sampleId);
	// }
	// });
	// }

	public void loadVoice(Context mContext, int num) {
		this.mContext = mContext;

		if (soundPool != null) {
			soundPool.pause(streamID);
			soundPool.stop(streamID);
			soundPool.release();
			soundPool = null;
		}

		soundPool = new SoundPool(100, AudioManager.STREAM_RING, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				playVoice(sampleId);
			}
		});

        switch(num){
            case 0:
                outgoing = soundPool.load(mContext, R.raw.dw0, 1);
                break;
            case 1:
                outgoing = soundPool.load(mContext, R.raw.dw1, 1);
                break;
            case 2:
                outgoing = soundPool.load(mContext, R.raw.dw2, 1);
                break;
            case 3:
                outgoing = soundPool.load(mContext, R.raw.dw3, 1);
                break;
            case 4:
                outgoing = soundPool.load(mContext, R.raw.dw4, 1);
                break;
            case 5:
                outgoing = soundPool.load(mContext, R.raw.dw5, 1);
                break;
            case 6:
                outgoing = soundPool.load(mContext, R.raw.dw6, 1);
                break;
            case 7:
                outgoing = soundPool.load(mContext, R.raw.dw7, 1);
                break;
            case 8:
                outgoing = soundPool.load(mContext, R.raw.dw8, 1);
                break;
            case 9:
                outgoing = soundPool.load(mContext, R.raw.dw9, 1);
                break;
            case 011111:// 门开了语音
                outgoing = soundPool.load(mContext, R.raw.menjinkaimen, 1);
                break;
        }
	}

	public void playVoice(int outgoing) {
		streamID = soundPool.play(outgoing, 1, 1, 1, 0, 1);
	}

}
