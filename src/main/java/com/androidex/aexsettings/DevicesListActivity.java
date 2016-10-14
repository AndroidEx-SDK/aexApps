package com.androidex.aexsettings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.androidex.aexlibs.WebJavaBridge.OnJavaBridgeListener;
import com.androidex.common.DummyContent;
import com.androidex.common.DummyContent.DummyItem;
import com.androidex.plugins.OnBackCall;
import com.eztor.plugins.define;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DevicesListActivity extends FragmentActivity
        implements DevicesListFragment.Callbacks,OnJavaBridgeListener ,OnBackCall{
	

    private boolean mTwoPane;
    public OnJavaBridgeListener jbListener = mjbListener;
    //DevicesManager m_devices = null;
    public MyVideoControlHandler myhandler = null;
    //public ServerControlMent    mycontrolment = null;
    static final String ACTION_RECV_GPIO_BROADCAST   = "com.eztor.action.ACTION_RECV_GPIO_BROADCAST";
    
	JSONObject jsconfig=null;
	
	String  defaultapp = "";
    
        
    static private OnJavaBridgeListener mjbListener = new OnJavaBridgeListener(){

		@Override
		public void onDeviceEvent(int code, String args) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSendJavaScript(String jscode) {
			// TODO Auto-generated method stub
			
		}

    };

    public OnJavaBridgeListener getSummary(){
    	DummyItem aItem = DummyContent.findItemByTag("javatest");
    	if(aItem != null)
    		return (OnJavaBridgeListener) aItem.getView();
    	else
    		return this.mjbListener;
    }

    public OnJavaBridgeListener getParkStatus(){
    	DummyItem aItem = DummyContent.findItemByTag("webtest");
    	if(aItem != null)
    		return (OnJavaBridgeListener) aItem.getView();
    	else
    		return this.mjbListener;
    }

    public OnJavaBridgeListener getParkDetailRecord(){
    	DummyItem aItem = DummyContent.findItemByTag("order");
    	if(aItem != null)
    		return (OnJavaBridgeListener) aItem.getView();
    	else
    		return this.mjbListener;
    }
    
    public OnJavaBridgeListener getMeteredLog(){
    	DummyItem aItem = DummyContent.findItemByTag("setting");
    	if(aItem != null)
    		return (OnJavaBridgeListener) aItem.getView();
    	else
    		return this.mjbListener;
    }
    
    public OnJavaBridgeListener getPlayPic(){
    	DummyItem aItem = DummyContent.findItemByTag("playpic");
    	if(aItem != null)
    		return (OnJavaBridgeListener) aItem.getView();
    	else
    		return this.mjbListener;
    }
    
    public OnJavaBridgeListener getPlayVideo(){
    	DummyItem aItem = DummyContent.findItemByTag("playvideo");
    	if(aItem != null)
    		return (OnJavaBridgeListener) aItem.getView();
    	else
    		return this.mjbListener;
    }
    
    public OnJavaBridgeListener getWebView(){
    	DummyItem aItem = DummyContent.findItemByTag("webview");
    	if(aItem != null)
    		return (OnJavaBridgeListener) aItem.getView();
    	else
    		return this.mjbListener;
    }
    
    public boolean findMeteredLog(){
    	DummyItem aItem = DummyContent.findItemByTag("test");
    	if(aItem != null)
    		return  aItem.hasView();
    	else
    		return false;
    }
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		System.out.println("DevicesListActivity onDestroy");
		System.exit(0);
		//mycontrolment.closeServer();
		super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	DummyContent.setContext(this);
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.activity_item_list);
        //全屏设置，隐藏窗口所有装饰
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //m_devices = new DevicesManager(this);
        //mycontrolment = new ServerControlMent(this);
        myhandler = new MyVideoControlHandler();
        //读取配置文件
        loadConfig(DEF.zbdevicesConf); 

        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            ((DevicesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
            LoadDefaultApp();
        }
    }

    void showFragment(int res,DummyItem info) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		if(info.hasView()){
			if(fragmentManager.popBackStackImmediate(info.id, 0)){
				return;
			}		}
		ft.replace(res,info.getView(),info.id);
		ft.addToBackStack(info.id);
		ft.commit();
	}

    @Override
    public void onItemSelected(String id) {
        DummyItem aItem = DummyContent.findItemByTag(id);
        if(aItem != null){
        	if (mTwoPane) {
	            showFragment(R.id.item_detail_container,aItem);	
	            jbListener = (OnJavaBridgeListener) aItem.getView();
	            DummyContent.setActive(aItem);
		    } else {
	            Intent detailIntent = new Intent(this, DetailActivity.class);
	            detailIntent.putExtra(DetailFragment.ARG_ITEM_ID, id);
	            startActivity(detailIntent);
	        }
        }
    }


    /**
     *	为了区分onBackCallEvent函数,此函数只传递数据给界面做显示
     */
    @Override
	public void onDeviceEvent(int code, String args) {
		// TODO Auto-generated method stub
    	//先交给pm处理，pm处理结束后根据返回值再由当前显示界面对象处理，以便显示
		Log.v("DevicesListActivity12", args);
	}


	@Override
	public void onSendJavaScript(String jscode) {
		// TODO Auto-generated method stub
		this.jbListener.onSendJavaScript(jscode);
	}
	
	
	public void LoadDefaultApp(){
		int id =getDefaultAppPosition(defaultapp);
		
		switch(id){
		case 0:
            onItemSelected("setting");
			break;
		case 1:
			onItemSelected("javatest");
			break;
		case 2:
			onItemSelected("webtest");
			break;
		case 3:
			onItemSelected("order");
			break;
		case 4:
			onItemSelected("playpic");
			break;
		case 5:
			onItemSelected("playvideo");
			break;
		case 6:
			onItemSelected("webview");
			break;
		default:
			onItemSelected("setting");
			break;
		}
	}
	
    

	/**
	 *	用于处理各个模块之间的调用
	 */
	@Override
	public void onBackCallEvent(int code, String args) {
		Log.v("DevicesListActivity34", args);
		Message msg=new Message();
		msg.what=code;
		myhandler.sendMessage(msg);
		
		//同时发送广播
		Intent intent = new Intent();  
        intent.setAction(ACTION_RECV_GPIO_BROADCAST); 
        intent.putExtra("gpio_id", code);
        sendBroadcast(intent);  
	}
    
    
    public void showExitDialog(){
        new AlertDialog.Builder(this)   
        .setTitle("确认")  
        .setMessage("确定退出程序吗？")  
        .setPositiveButton("退出",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
        })  
        .setNegativeButton("取消", null)  
        .show();  
    }
    
    public void fullScreenChange() {
	    SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    boolean fullScreen = mPreferences.getBoolean("fullScreen", false);
	    WindowManager.LayoutParams attrs = getWindow().getAttributes(); 
	    System.out.println("fullScreen的值:" + fullScreen);
	    if (fullScreen) {
		    attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		    getWindow().setAttributes(attrs); 
		    //取消全屏设置
		    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		    mPreferences.edit().putBoolean("fullScreen", false).commit() ;
	    } 
	    else {
		    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN; 
		    getWindow().setAttributes(attrs); 
		    getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); 
		    mPreferences.edit().putBoolean("fullScreen", true).commit();
	    }
    }
    
    @Override  
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_UP){ 
        	//System.out.printf("keycode up:%d\r\n",event.getKeyCode());
        	switch(event.getKeyCode()){
        	case 4:		//后退键
        		//showExitDialog();
        		return true;
        		
        	}
        } 
       
        return super.dispatchKeyEvent(event);  
    } 
    
	public class MyVideoControlHandler extends Handler {
		public MyVideoControlHandler() {
		}

		public MyVideoControlHandler(Looper L) {
			super(L);
		}

		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 此处可以更新UI
			int code = msg.what;
			
			switch(code){
			case define.video_play:{
				onItemSelected("playvideo");
			    jbListener.onDeviceEvent(code, "");
				break;
			}
			case define.pic_play:{
				onItemSelected("playpic");
			    jbListener.onDeviceEvent(code, "");
			    break;
			}
			default:
			   jbListener.onDeviceEvent(code, "");
			}
		   }
		}
	
	
	
    public void loadConfig(String path){
		jsconfig= loadConfigFromFile(path);
		JSONObject js=jsconfig;
		if(js!=null){
			defaultapp=js.optString("defaultapp");
			
		}
	}
    
    public int getDefaultAppPosition(String str){
    	int inum= define.app_list.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(define.app_list[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
    /**
	 *	从文件获取配置
	 */
	public JSONObject loadConfigFromFile(String confFile){
		File ffs = new File(confFile);
		if (ffs.exists()){
			try {
				byte[] buffer = new byte[4096];
				FileInputStream conf = new FileInputStream(ffs);
				try {
					long byteCount = ffs.length();
					if(byteCount > 4096){
//						LOG("PM","配置文件太大，文件大小：%d>4096", byteCount);
						byteCount = 4096;
					}
					conf.read(buffer,0,(int) byteCount);
					String confContext = new String(buffer);
					JSONObject js=new JSONObject(confContext);
					conf.close();
					return js;
				} catch (IOException e1) {
					e1.printStackTrace();
				}catch(JSONException e2){
					e2.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}	
		return null;
	}

    

}
