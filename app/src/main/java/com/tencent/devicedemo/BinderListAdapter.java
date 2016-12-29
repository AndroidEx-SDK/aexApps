package com.tencent.devicedemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BinderListAdapter extends BaseAdapter {
	private static String TAG = "BinderListAdapter";
	private Context mContext;
	private List<TXBinderInfo> mListBinder;
	private String mHeadPicPath;
	private Handler mHandler = null;
	private Set<Long> mSetFetching = new HashSet<Long>();
	
	public BinderListAdapter(Context applicationContext) {
		// TODO Auto-generated constructor stub
		mContext = applicationContext;
		mListBinder = new ArrayList<TXBinderInfo>();
		mHeadPicPath = mContext.getCacheDir().getAbsolutePath() + "/head";
		File file = new File(mHeadPicPath);
		if (!file.exists()){
			file.mkdirs();
		}
		
		mHandler = new Handler(mContext.getMainLooper()){
			public void handleMessage(Message msg){
				BinderListAdapter.this.notifyDataSetChanged();
			}
		};
	}
	
	public void freshBinderList(List<TXBinderInfo> binderList){
		mListBinder.clear();
		for (int i = 0; i < binderList.size(); ++i){
			TXBinderInfo  binder = binderList.get(i);
			mListBinder.add(binder);
		}
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListBinder.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mListBinder.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.binderlayout, null);
		}
		
		TextView nickName = (TextView) convertView.findViewById(R.id.nick_name);
		String strNickName = mListBinder.get(position).getNickName();
		nickName.setText(strNickName);

		ImageView head = (ImageView)convertView.findViewById(R.id.headpic);
		Bitmap bitmap = getBinderHeadPic(mListBinder.get(position).tinyid);
		if (bitmap == null){
			bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.binder_default_head);
			head.setImageBitmap(bitmap);
			fetchBinderHeadPic(mListBinder.get(position).tinyid, mListBinder.get(position).head_url);
		}
		else{
			head.setImageBitmap(bitmap);
		}
		
		convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				long tinyid = mListBinder.get(position).tinyid;
				String nickname = mListBinder.get(position).getNickName(); 
				Intent binder = new Intent(mContext, BinderActivity.class);
				binder.putExtra("tinyid", tinyid);
				binder.putExtra("nickname", nickname);
		        mContext.startActivity(binder);
			}
		});
		
		return convertView;
	}
	
	public Bitmap getBinderHeadPic(long uin){
		Bitmap bitmap = null;
		try{
			String strHeadPic = mHeadPicPath + "/" + uin + ".png";
		    bitmap =  BitmapFactory.decodeFile(strHeadPic);
			
		}
		catch (Exception e){
			Log.i(TAG, e.toString());
		}
		return bitmap;
	}
	
	public void saveBinderHeadPic(long uin, Bitmap bitmap)
	{
		if (bitmap == null){
			return;
		}
			
		String strHeadPic = mHeadPicPath + "/" + uin + ".png";
		File file = new File(strHeadPic);
		if (file.exists()){
			file.delete();
		}
		
		try{
			FileOutputStream stream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
			stream.flush();
			stream.close();
		}
		catch(Exception e){
			Log.i(TAG, e.toString());
		}
	}

	public void fetchBinderHeadPic(final long uin, final String strUrl){
		synchronized(mSetFetching){
			if (mSetFetching.contains(uin)){
				return;
			}
			else{
				mSetFetching.add(uin);
			}
		}
		
		new Thread(){
			public void run() {
				try{
					URL url = new URL(strUrl);
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream stream = conn.getInputStream();
					Bitmap bitmap = BitmapFactory.decodeStream(stream);
					saveBinderHeadPic(uin, bitmap);
					synchronized(mSetFetching){
						mSetFetching.remove(uin);
					}
					mHandler.sendEmptyMessage(0);
				}
				catch (Exception e){
					Log.i(TAG, e.toString());
				}
			}	
		}.start();
	}
}
