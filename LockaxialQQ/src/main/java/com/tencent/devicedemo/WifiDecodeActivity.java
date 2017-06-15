package com.tencent.devicedemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidex.GetUserInfo;
import com.androidex.Zxing;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;

import java.util.ArrayList;
import java.util.List;


public class WifiDecodeActivity extends Activity{
	private static final int samplerate = 44100;
	private static final int channel = AudioFormat.CHANNEL_IN_MONO;
	private static final int format = AudioFormat.ENCODING_PCM_16BIT;
	
	
	
	private TextView mwifiinfo;
	private NotifyReceiver mNotifyReceiver;   

	private int bufferSizeInBytes = 0;
	
	private AudioRecord audioRecord;
	
	private int isRecording = 0;
	private Bitmap bitmap;
	
	Handler mHandler = new Handler();


	private View view1, view2, view3,view4,view5;
	private ViewPager viewPager;  //对应的viewPager

	private List<View> viewList;//view数组

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated constructor stub
		super.onCreate(savedInstanceState);
        //全屏设置，隐藏窗口所有装饰
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//清除FLAG
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //super.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_wifidecode);

        //if(getIntent().getBooleanExtra("back", false))
        {
            if(getActionBar() != null)
                getActionBar().setDisplayHomeAsUpEnabled(true);
        }

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		LayoutInflater inflater=getLayoutInflater();
		view1 = inflater.inflate(R.layout.erweima, null);
		ImageView imewm=(ImageView)view1.findViewById(R.id.iv_ewm);
		//扫码绑定
		if (GetUserInfo.getSn()!=null){
			bitmap = Zxing.createQRImage("http://iot.qq.com/add?pid=1700003316&sn=" + GetUserInfo.getSn(), 200, 200, null);
			imewm.setBackgroundColor(Color.WHITE);
			imewm.setImageBitmap(bitmap);
		}else{
			bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.error);
			imewm.setImageBitmap(bitmap);
		}
		view2 = inflater.inflate(R.layout.erweima1, null);
		view3 = inflater.inflate(R.layout.erweima2, null);
		view4 = inflater.inflate(R.layout.erweima3, null);
		view5 = inflater.inflate(R.layout.erweima4, null);

		viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
		viewList.add(view1);
		viewList.add(view2);
		viewList.add(view3);
		viewList.add(view4);
		viewList.add(view5);



		PagerAdapter pagerAdapter = new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				// TODO Auto-generated method stub
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return viewList.size();
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
									Object object) {
				// TODO Auto-generated method stub
				container.removeView(viewList.get(position));
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				// TODO Auto-generated method stub
				container.addView(viewList.get(position));


				return viewList.get(position);
			}
		};

		viewPager.setAdapter(pagerAdapter);
		mwifiinfo = (TextView)findViewById(R.id.wifiinfo);
		IntentFilter filter = new IntentFilter();
		filter.addAction(TXDeviceService.OnReceiveWifiInfo); 
		filter.addAction(TXDeviceService.BinderListChange);
		mNotifyReceiver = new NotifyReceiver();
		registerReceiver(mNotifyReceiver, filter);

		TXDeviceService.startWifiDecoder("TXTEST-axewang-7", samplerate, 3);
		// 创建audiorecoder
		//Log.d( "网址",""+TXDeviceService.getInstance().getQRCodeUrl());
		createAudioRecord();
		startRecord();


	}

	/**
	 * 用字符串生成二维码
	 * @param str
	 * @author zhouzhe@lenovo-cw.com
	 * @return
	 * @throws WriterException
	 */
	public Bitmap Create2DCode(String str) throws WriterException {
//生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
		BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 300, 300);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
//二维矩阵转为一维像素数组,也就是一直横着排了
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(matrix.get(x, y)){
					pixels[y * width + x] = 0xff000000;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//通过像素数组生成bitmap,具体参考api
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
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
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		switch(keyCode){
			case KeyEvent.KEYCODE_4:
				int id=viewPager.getCurrentItem();
				if(id>=1){
					viewPager.setCurrentItem(id-1);
				}
				return true;
			case KeyEvent.KEYCODE_6:
				int id1=viewPager.getCurrentItem();
				if(id1<=3){
					viewPager.setCurrentItem(id1+1);
				}
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}


	private void createAudioRecord()
	{
		bufferSizeInBytes = AudioRecord.getMinBufferSize(samplerate, channel, format);
		if (bufferSizeInBytes % (441*2*2) == 0) {
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplerate, channel, format, bufferSizeInBytes);

		}
		else
		{
			bufferSizeInBytes = (bufferSizeInBytes/(441*2*2)+2)*(441*2*2);
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplerate, channel, format, bufferSizeInBytes);
		}
	}
	
	private void startRecord()
	{
		if (audioRecord != null)
		{
			audioRecord.startRecording();
			isRecording = 1;
			new Thread(new AudioRecordThread()).start();
		}
	}
	
	private void stopRecord()
	{
		if(audioRecord != null)
		{
			isRecording = 0;
			audioRecord.stop();
			audioRecord.release();
			audioRecord = null;
		}	
	}
	
	class AudioRecordThread implements Runnable
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int readsize = 0;
			byte[] audiodata = new byte[bufferSizeInBytes];
			byte[] block = new byte[(441*2*2)];
			byte[] tail_block = null;
			while(isRecording == 1)
			{
				readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
				
				if(readsize == bufferSizeInBytes)
				{
					int i = 0;
					while(readsize>(441*2*2)) //20ms 
					{
						System.arraycopy(audiodata, i*(441*2*2), block, 0, (441*2*2));
						TXDeviceService.fillVoiceWavData(block);
						i = i+1;
						readsize = readsize - (441*2*2);
					}
					
					if(readsize > 0)
					{
						tail_block = new byte[readsize];
						System.arraycopy(audiodata, i*(441*2*2), tail_block, 0, readsize);
						TXDeviceService.fillVoiceWavData(tail_block);
						tail_block = null;
					}
				}
				else
				{
					Log.i("TAG_WifiDecode", "size error");
				}
			}
		}
		
	}
	
	protected void onResume(){
		super.onResume();
	}
	
	protected void onPause(){
		super.onPause();
	}
	
	protected void onDestroy(){
		stopRecord();
		unregisterReceiver(mNotifyReceiver);  
		super.onDestroy();
	}

	public class NotifyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals( TXDeviceService.OnReceiveWifiInfo)){
				// show info
				Bundle bundle = intent.getExtras();
				String ssid = bundle.getString(TXDeviceService.WifiInfo_SSID);
				String pass = bundle.getString(TXDeviceService.WifiInfo_PASS);
				int ip = bundle.getInt(TXDeviceService.WifiInfo_IP);
				int port = bundle.getInt(TXDeviceService.WifiInfo_PORT);
				String info = "ssid:" +ssid+"\npassword:" +pass+ "\nip:" +ip+ "\nport:"+port; 
				
				mwifiinfo.setText(info);
				
				stopRecord();
				
				TXDeviceService.stopWifiDecoder();

				WifiDecodeActivity.this.getSharedPreferences("TXDeviceSDK", 0).edit().putBoolean("NetworkSetted", true);
	
//				mHandler.postDelayed(new Runnable(){
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						finish();
//					}
//				}, 5000);

			} else if (intent.getAction() == TXDeviceService.BinderListChange){
				Parcelable[] listTemp = intent.getExtras().getParcelableArray("binderlist");
				List<TXBinderInfo> binderList = new ArrayList<TXBinderInfo>();
				for (int i = 0; i < listTemp.length; ++i){
					TXBinderInfo  binder = (TXBinderInfo)(listTemp[i]);
					binderList.add(binder);
				}
				if(binderList.size()!=0){
					Intent intent1=new Intent(WifiDecodeActivity.this, MainActivity.class);
					intent1.putExtra("bindnmu","havenum");
					startActivity(intent1);
					finish();
					// startActivity(new Intent(WifiDecodeActivity.this, MainActivity.class));
				}else{

					Intent intent1=new Intent(WifiDecodeActivity.this, MainActivity.class);
					intent1.putExtra("bindnmu","nullnum");
					startActivity(intent1);
					finish();
					//startActivity(new Intent(WifiDecodeActivity.this, MainActivity.class));

				}

			}
		}
	}
  
}