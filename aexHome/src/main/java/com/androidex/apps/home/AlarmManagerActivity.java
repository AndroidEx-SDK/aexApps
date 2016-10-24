package com.androidex.apps.home;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.androidex.plugins.OnBackCall;
import com.eztor.plugins.hwdevices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public class AlarmManagerActivity extends Activity implements OnBackCall {

	String action = "";
	String strBootHour = "";
	String strBootMinute = "";
	String strShutDownHour = "";
	String strShutDownMinute = "";
	JSONObject jsconfig=null;
	public hwdevices m_hwdevices = new hwdevices();

    @Override
    public void onCreate(Bundle savedInstanceState) {
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        
        //读取配置文件
        loadConfig(DEF.zbdevicesConf);
        
        Intent intent = getIntent();
        action = intent.getStringExtra("action"); 
         
        
        if(action.equals("lcd_close")){
        	m_hwdevices.lcd_control(0);
        	setNextLcdClose();
        	finish();
        	
        }else if(action.equals("lcd_open")){
        	m_hwdevices.lcd_control(1);
        	setNextLcdOpen();
        	finish();
        }
        
        
    }

    
    
    public void loadConfig(String path){
		jsconfig= loadConfigFromFile(path);
		JSONObject js=jsconfig;
		if(js!=null){
			strBootHour = js.optString("boothour");
		    strBootMinute = js.optString("bootminute");
			strShutDownHour  = js.optString("shutdownhour");
		    strShutDownMinute  = js.optString("shutdownminute");
		}
	}
    
    public void setNextLcdClose(){
        int hour = Integer.parseInt(strShutDownHour);
    	int minute = Integer.parseInt(strShutDownMinute);
    	
    	Calendar calendar = Calendar.getInstance();
    	long nowTime  = calendar.getTimeInMillis();
    	
    	Calendar myCal = Calendar.getInstance();
    	myCal.set(Calendar.HOUR_OF_DAY,hour);

    	myCal.set(Calendar.MINUTE,minute);
    	
    	myCal.set(Calendar.SECOND,0);

    	long shutdownTime = myCal.getTimeInMillis();
    	
    	if(nowTime > shutdownTime){
    		 myCal.add(Calendar.HOUR_OF_DAY, 24);
    	     myCal.set(Calendar.MINUTE,minute);	
    	     myCal.set(Calendar.SECOND,0);
    		 shutdownTime = myCal.getTimeInMillis();
    	}
    	
    	AlarmManager am = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(
                "android.intent.action.LCD_CLOSE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, shutdownTime, pendingIntent);
    }
    
    public void setNextLcdOpen(){
    	int hour = Integer.parseInt(strBootHour);
    	int minute = Integer.parseInt(strBootMinute);
    	
    	Calendar calendar = Calendar.getInstance();
    	long nowTime  = calendar.getTimeInMillis();
    	
    	Calendar myCal = Calendar.getInstance();
    	myCal.set(Calendar.HOUR_OF_DAY,hour);

    	myCal.set(Calendar.MINUTE,minute);
    	
    	myCal.set(Calendar.SECOND,0);

    	long bootTime = myCal.getTimeInMillis();
    	
    	if(nowTime > bootTime){
    	   myCal.add(Calendar.HOUR_OF_DAY, 24);
   		   bootTime = myCal.getTimeInMillis();
  	       myCal.set(Calendar.MINUTE,minute);	
  	       myCal.set(Calendar.SECOND,0);
   	    }
    	
    	AlarmManager am = (AlarmManager) this.
                getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(
                "android.intent.action.LCD_OPEN");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, bootTime , pendingIntent);
    }
    
    /**
  		 *	从文件获取咪表配置
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
//  							LOG("PM","配置文件太大，文件大小：%d>4096", byteCount);
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

	@Override
	public void onBackCallEvent(int code, String msg) {
		// TODO Auto-generated method stub
		
		Log.v("recv", msg);
		
	}

}
