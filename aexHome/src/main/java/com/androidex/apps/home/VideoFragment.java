package com.androidex.apps.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

import com.androidex.aexlibs.WebJavaBridge.OnJavaBridgeListener;
import com.androidex.common.DummyContent;

import java.io.File;
import java.util.ArrayList;

public class VideoFragment extends Fragment implements OnJavaBridgeListener ,
    MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener{
	public View 			psetview		=null;
	
    public static final String ARG_ITEM_ID = "item_id";
    DummyContent.DummyItem mItem;
    
    
	static final String TAG = "AndroidMixPlayer";
	
	String filePath = "/mnt/usbhost1/Movies";
	String strPath  = "/mnt/usbhost1/Movies/strad.txt";
	
	String sdPath	= "/mnt/sdcard/Movies";
	String sdstrPath= "/mnt/sdcard/Movies/strad.txt";
	
	Bitmap bm = null ;
	SDcardLinsenerReceiver receiver;
	ArrayList<String> picfilelist = new ArrayList<String>();
	ArrayList<String> viofilelist = new ArrayList<String>();
	int picpoint = 0;
	int viopoint = 0;
	MyVideoView vv;
    

    public VideoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = DummyContent.findItemByTag(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (mItem != null) {
        	this.getActivity().getActionBar().setTitle(mItem.content);
        }
		super.onCreateView(inflater, container, savedInstanceState);
		LinearLayout.LayoutParams wvParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		if (psetview == null) {
			psetview		= inflater.inflate(R.layout.mixplay, null);
		}

        //播放视频
		startplayvideo();
		return psetview;
    }

    @Override 
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem m1 = menu.add("退出");
		m1.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
				DevicesListActivity pActivity = (DevicesListActivity) getKKMain();
				pActivity.showExitDialog();
				return true;
			}
		});
		
//		MenuItem m2 = menu.add("全屏");
//		m2.setOnMenuItemClickListener(new OnMenuItemClickListener(){
//			public boolean onMenuItemClick(MenuItem item) {
//				DevicesListActivity pActivity = (DevicesListActivity) getKKMain();
//				pActivity.fullScreenChange();
//				return true;
//			}
//		});
    }
    
	@Override
	public void onDestroyView() {
		if(psetview != null){
			ViewGroup parentViewGroup = (ViewGroup) (psetview.getParent());
			if( null != parentViewGroup ) {
				parentViewGroup.removeView( psetview );
			}
		}
		super.onDestroyView();
	}

	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		// We have a menu item to show in action bar.
        setHasOptionsMenu(true);
    }
	
	
	@Override
	public void onDeviceEvent(final int code, final String args) {
	}

	@Override
	public void onSendJavaScript(String jscode) {
		// TODO Auto-generated method stub
		
	}

	
	protected Activity getKKMain() {
		Activity kkmain = (Activity)getActivity();
		return kkmain;
	}
	
    public void showDialog(String msg){
        new AlertDialog.Builder(getKKMain())   
        .setTitle("确认")  
        .setMessage(msg)  
        .setPositiveButton("保存",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
        })  
        .setNegativeButton("取消", null)  
        .show();  
    } 
    
    public void startplayvideo(){	
    	
		vv = (MyVideoView) psetview.findViewById(R.id.vidmix);
		
		File ff = new File(sdPath);
		if (!ff.exists()) {
			Log.e("adplay", String.format("File %s not exists,Create it now.", sdPath));
			if(!ff.mkdirs()){
				Log.e("adplay", String.format("Create %s fail.", sdPath));
			}else{
			}
		} 
		int i = initFile(sdPath);
		if (i == 1) {
			play();
		}
    }
    
    public void play() {
		System.out.println("picfilelist.size="+picfilelist.size());
		if(!viofilelist.isEmpty()){
			String viopath = viofilelist.get(viopoint);	
			if (!viopath.isEmpty()) {
				playVio(viopath);
			}
		}
	}

	private void playVio(String filename) {
		Uri mUri = Uri.parse(filename);
		// Create media controller
		MediaController mMediaController = new MediaController(psetview.getContext());
		// 设置MediaController
		vv.setMediaController(mMediaController);
		vv.setOnCompletionListener(this);
		vv.setOnErrorListener(this);
		vv.setVideoURI(mUri);
		vv.start();
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		Log.i(TAG, "AndroidMixPlayer is onError");
		viopoint = 0;
		viofilelist.clear();
		return true;
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.i(TAG, "AndroidMixPlayer is onCompletion");
		viopoint++;
		if (viopoint == viofilelist.size()) {
			viopoint = 0;
		}
		if (viofilelist.size() != 0 && !viofilelist.get(viopoint).isEmpty()) {
			playVio(viofilelist.get(viopoint));
		}
	}

	public int initFile(String file) {
		File myFile = new File(file);
		if (myFile.exists()) {
			checkFiles(myFile.listFiles());
			return 1;
		} else {
			Toast.makeText(psetview.getContext().getApplicationContext(), "U盘中没有advert目录，请重新创建后再次插入",
					Toast.LENGTH_LONG).show();
			return 0;
		}
	}

	private void checkFiles(File[] files) {
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile() && isVideo(files[i])) {
					viofilelist.add(files[i].getAbsolutePath());
				} else {
					checkFiles(files[i].listFiles());
				}
			}
		}
	}

	private boolean isVideo(File f) {
		boolean re = false;
		if (f.isFile()) {
			String fName = f.getAbsolutePath();
			String end = fName.substring(fName.lastIndexOf(".") + 1,
					fName.length()).toLowerCase();
			if (end.equals("rmvb") || end.equals("avi") || end.equals("mkv")
					|| end.equals("rm") || end.equals("mp4")
					|| end.equals("flv")) {
				re = true;
			} else {
				re = false;
			}
		}
		return re;
	}

	/**
	 *** 监听U盘插拔
	 */
	private class SDcardLinsenerReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			if (intent.getAction()
					.equals("android.intent.action.MEDIA_MOUNTED")) {
				Log.i(TAG, "U盘插入");
				int i = initFile(strPath);
				if (i == 1) {
					play();
				}
				// 加载字幕广告
				//loadStr(tv, strPath);
			} else if (intent.getAction().equals(
					"android.intent.action.MEDIA_REMOVED")) {
				Log.i(TAG, "U盘移出！");
				//unloadStr(tv);
			} else if (intent.getAction().equals(
					"android.intent.action.MEDIA_UNMOUNTED")) {
				Log.i(TAG, "U盘插入异常");
			} else if (intent.getAction().equals(
					"android.intent.action.MEDIA_BAD_REMOVAL")) {
				Log.i(TAG, "U盘移出异常！");
				//unloadStr(tv);
			}

		}
	}
}
