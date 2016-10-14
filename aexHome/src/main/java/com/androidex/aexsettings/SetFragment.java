package com.androidex.aexsettings;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.aexlibs.WebJavaBridge.OnJavaBridgeListener;
import com.androidex.common.DummyContent;
import com.eztor.plugins.define;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Timer;

public class SetFragment extends Fragment implements OnJavaBridgeListener  {
	public View 			psetview		=null;
	//public RootCmd          m_rootcmd       =  new RootCmd();
	//public hwconfig         m_config        = null;
	//public MLog             m_mylog        =  null;
	//public hwlog            m_hwlog        =  null;
	
    public  JSONObject jsconfig = new JSONObject();
	
	public Spinner			pspin_card	=null;		    //读卡器类型
	public Spinner			pspin_dev_card	=null;		//读卡器端口

	public Spinner			pspin_print	=null;		    //打印机类型
	public Spinner			pspin_dev_print	=null;		//打印机端口
	
	public Spinner			pspin_keyboard	=null;		    //密码键盘类型
	public Spinner			pspin_dev_keyboard	=null;		//密码键盘端口
	
	public Spinner			pspin_idcard	=null;		    //身份证阅读器类型
	public Spinner			pspin_dev_idcard	=null;		//身份证阅读器端口
	
	public Spinner			pspin_font	        =null;		// 字体大小
	public Spinner			pspin_statusbar  	=null;		// 状态栏是否隐藏  0表示不隐藏、1表示隐藏状态栏  
	public Spinner			pspin_screen	    =null;		// 屏幕旋转角度
	
	public Spinner			pspin_boothour	    =null;		// 小时
	public Spinner			pspin_bootminute	=null;		// 分
	
	public Spinner			pspin_shutdownhour	    =null;		// 小时
	public Spinner			pspin_shutdownminute	=null;		// 分
	
	
	public Spinner			pspin_defaultapp    	=null;     //默认启动程序
	
	public Spinner          pspin_gpsset           = null;
	
//	public CheckBox         checkbox_font         = null;          //屏幕字体配置
//	public CheckBox         checkbox_statusbar         = null;     //状态栏配置
//	public CheckBox         checkbox_screen         = null;        //旋转角度配置
//	public CheckBox         checkbox_autoboot         = null;      //定时开机配置
//	public CheckBox         checkbox_autoshutdown         = null;   //定时关机配置
//	public CheckBox         checkbox_defaultapp         = null;      //默认启动配置
	
	
	public EditText			pedit_defaultip	=null;		//默认服务器地址 用于底层通讯
	
	public Button			pbtn_save		=null;		//保存
	public Button			pbtn_load		=null;		//重新载入
	
	private TextView       textview_device_guid = null;
	
    private Timer timer = new Timer();		//定时器
	public  MyHandler       myHandler = new MyHandler();
    //private kkuevent uevent;

	private String device_guid = null;

	public String	serialtype[]={
			"/dev/ttyS0",
			"/dev/ttyS1",
			"/dev/ttyS2",
			"/dev/ttyS3",
			"/dev/ttyS4",
			"/dev/ttyS5",
			"/dev/ttyS6",
			"/dev/ttyS7",
			"/dev/ttyUSB0",
			"/dev/ttyUSB1",
			"/dev/ttyUSB2",
			"/dev/ttyUSB3",
			"/dev/ttyUSB4",
			"/dev/ttyUSB5"
			};
	
	public String	fonttype[]={
			"120",
			"160",
			"240"
			};
	
	public String	statusbartype[]={
			"隐藏状态栏",
			"显示状态栏"
			};
	
	public String	screentype[]={
			"横屏",
			"竖屏"
			};
	
	public String	gpsset[]={
			"海威讯GPS接口方式",
			"串口GPS接口方式串口1",
			"串口GPS接口方式串口2",
			"串口GPS接口方式串口3",
			"串口GPS接口方式串口4",
			};
	
	public String	hourtype[]={
			"00",
			"01",
			"02",
			"03",
			"04",
			"05",
			"06",
			"07",
			"08",
			"09",
			"10",
			"11",
			"12",
			"13",
			"14",
			"15",
			"16",
			"17",
			"18",
			"19",
			"20",
			"21",
			"22",
			"23"
			};
	
	public String	minutetype[]={
			"00","01","02","03","04","05","06","07","08","09",
			"10","11","12","13","14","15","16","17","18","19",
			"20","21","22","23","24","25","26","27","28","29",
			"30","31","32","33","34","35","36","37","38","39",
			"40","41","42","43","44","45","46","47","48","49",
			"50","51","52","53","54","55","56","57","58","59"
			};

    public static final String ARG_ITEM_ID = "item_id";
    DummyContent.DummyItem mItem;

    public SetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //m_config = new hwconfig();
        
//        m_mylog = new MyLog.MLog();
//        m_mylog.Log();
        
//        m_hwlog =  new hwlog();
//        m_hwlog.writeSystemLog();
        
//        if (getArguments().containsKey(ARG_ITEM_ID)) {
//            mItem = DummyContent.findItemByTag(getArguments().getString(ARG_ITEM_ID));
//        }
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
			psetview		= inflater.inflate(R.layout.activity_item_set, null);
			
			pspin_card	=(Spinner)psetview.findViewById(R.id.card);
			pspin_dev_card	=(Spinner)psetview.findViewById(R.id.dev_card);
			
			pspin_print	=(Spinner)psetview.findViewById(R.id.print);
			pspin_dev_print	=(Spinner)psetview.findViewById(R.id.dev_print);
			
			pspin_keyboard	=(Spinner)psetview.findViewById(R.id.keyboard);
			pspin_dev_keyboard	=(Spinner)psetview.findViewById(R.id.dev_keyboard);
			
			pspin_idcard	=(Spinner)psetview.findViewById(R.id.idcard);
			pspin_dev_idcard	=(Spinner)psetview.findViewById(R.id.dev_idcard);
			
			
			pspin_font = (Spinner)psetview.findViewById(R.id.font);
			pspin_statusbar = (Spinner)psetview.findViewById(R.id.statusbar);
			pspin_screen = (Spinner)psetview.findViewById(R.id.screenconfig);
			
			pspin_boothour = (Spinner)psetview.findViewById(R.id.boot_hour);
			pspin_bootminute = (Spinner)psetview.findViewById(R.id.boot_minute);
			pspin_shutdownhour = (Spinner)psetview.findViewById(R.id.shutdown_hour);
			pspin_shutdownminute = (Spinner)psetview.findViewById(R.id.shutdown_minute);
			
			pspin_defaultapp = (Spinner)psetview.findViewById(R.id.defaultstart);
			
			pspin_gpsset = (Spinner)psetview.findViewById(R.id.spinner_gpsset);
			
			
			textview_device_guid = (TextView)psetview.findViewById(R.id.textview_device_guid);
			
//			checkbox_font = (CheckBox)psetview.findViewById(R.id.checkbox_font);
//			checkbox_statusbar = (CheckBox)psetview.findViewById(R.id.checkbox_statusbar);
//			checkbox_screen = (CheckBox)psetview.findViewById(R.id.checkbox_screenconfig);
//			checkbox_autoboot = (CheckBox)psetview.findViewById(R.id.checkbox_autoboot);
//			checkbox_autoshutdown = (CheckBox)psetview.findViewById(R.id.checkbox_shutdown);
//		    checkbox_defaultapp = (CheckBox)psetview.findViewById(R.id.checkbox_defaultstart);
		    
			//pedit_defaultip	=(EditText)psetview.findViewById(R.id.metered_serveraddr1);
			
			pbtn_save		=(Button)psetview.findViewById(R.id.metered_save);
			pbtn_load		=(Button)psetview.findViewById(R.id.metered_reload);
		
	        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, serialtype);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_dev_card.setAdapter((SpinnerAdapter)adapter);
	        
	        pspin_dev_print.setAdapter((SpinnerAdapter)adapter);
	        
	        pspin_dev_keyboard.setAdapter((SpinnerAdapter)adapter);
	 
	        pspin_dev_idcard.setAdapter((SpinnerAdapter)adapter);
	        
	        
	        //打印机类型
	        ArrayAdapter<String> adapter_print=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, define.print_list);
	        adapter_print.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_print.setAdapter((SpinnerAdapter)adapter_print);

	        //读卡器类型
	        ArrayAdapter<String> adapter_card=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, define.card_list);
	        adapter_card.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_card.setAdapter((SpinnerAdapter)adapter_card);
	        
	        //密码键盘类型
	        ArrayAdapter<String> adapter_keyboard=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, define.keyboard_list);
	        adapter_keyboard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_keyboard.setAdapter((SpinnerAdapter)adapter_keyboard);
	        
	        //身份证阅读器类型
	        ArrayAdapter<String> adapter_idcard=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, define.idcard_list);
	        adapter_idcard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_idcard.setAdapter((SpinnerAdapter)adapter_idcard);
	        
	        
	        //字体大小选择
	        ArrayAdapter<String> adapter_font=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, fonttype);
	        adapter_font.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_font.setAdapter((SpinnerAdapter)adapter_font);
	        
	        //状态栏是否隐藏
	        ArrayAdapter<String> adapter_statusbar=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, statusbartype);
	        adapter_statusbar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_statusbar.setAdapter((SpinnerAdapter)adapter_statusbar);
	        pspin_statusbar.setSelection(1);
	        
	        //屏幕旋转角度
	        ArrayAdapter<String> adapter_screen=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, screentype);
	        adapter_screen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_screen.setAdapter((SpinnerAdapter)adapter_screen);
	        
	        
	        //开机小时
	        ArrayAdapter<String> adapter_boothour=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, hourtype);
	        adapter_boothour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_boothour.setAdapter((SpinnerAdapter)adapter_boothour);
	        
	        //开机分钟
	        ArrayAdapter<String> adapter_bootminute=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, minutetype);
	        adapter_bootminute.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_bootminute.setAdapter((SpinnerAdapter)adapter_bootminute);
	        
	        //关机小时
	        ArrayAdapter<String> adapter_shutdownhour=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, hourtype);
	        adapter_shutdownhour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_shutdownhour.setAdapter((SpinnerAdapter)adapter_shutdownhour);
	        
	        //关机分钟
	        ArrayAdapter<String> adapter_shutdownminute=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, minutetype);
	        adapter_shutdownminute.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_shutdownminute.setAdapter((SpinnerAdapter)adapter_shutdownminute);
	        
	        //默认启动程序
	        ArrayAdapter<String> adapter_defaultapp=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, define.app_list);
	        adapter_defaultapp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_defaultapp.setAdapter((SpinnerAdapter)adapter_defaultapp);
	        
	        // GPS 设置
	        ArrayAdapter<String> adapter_gpsset=new ArrayAdapter<String>(getKKMain(), android.R.layout.simple_spinner_item, gpsset);
	        adapter_gpsset.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        pspin_gpsset.setAdapter((SpinnerAdapter)adapter_gpsset);
	        
			pbtn_save.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog("你确定要保存该信息吗?");
				}
			});
			pbtn_load.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					loadConfig(DEF.zbdevicesConf);
				}
			});
			loadConfig(DEF.zbdevicesConf);
            Time time = new Time();
            time.setToNow();
            int year = time.year;
            int month = time.month+1;
            int day = time.monthDay;
            int hour = time.hour;
            int minute = time.minute;
            int sec = time.second;
            Log.v("recv", String.format("当前时间为：" + year +
                                "年 " + month +
                                "月 " + day +
                                "日 " + hour +
                                "时 " + minute +
                                "分 " + sec +
                                "秒"));
            device_guid = Secure.getString(psetview.getContext().getContentResolver(), Secure.ANDROID_ID);

            textview_device_guid.setText(String.format("本机序列号为: %s",device_guid));
            Log.v("recv", device_guid);
            checkfile();
		}
		return psetview;
    }

    @Override 
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem m4 = menu.add("退出");
		m4.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
				DevicesListActivity pActivity = (DevicesListActivity) getKKMain();
				pActivity.showExitDialog();
				return true;
			}
		});
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


    public boolean saveConfig(String path){
    	if(psetview!=null){
    		String str="";
			
			String print = "";
			String dev_print = "";
			String card = "";
			String dev_card = "";
			String keyboard = "";
			String dev_keyboard = "";
			String idcard = "";
			String dev_idcard = "";
			
			String strFont = "";
			int   nCheckFont = 0;
			String strStatusBar = "";
			int   nCheckStatusBar = 0;
			String strScreen = "";
			int   nCheckScreen = 0;
			int   nCheckBoot = 0;
			String strBootHour = "";
			String strBootMinute = "";
			int   nCheckShutDown = 0;
			String strShutDownHour = "";
			String strShutDownMinute = "";
			int   nCheckDefaultApp = 0;
			String strDefaultApp = "";
			int   nGpsSet = 0;
			
			dev_card = pspin_dev_card.getSelectedItem().toString();
			dev_print = pspin_dev_print.getSelectedItem().toString();
			dev_keyboard = pspin_dev_keyboard.getSelectedItem().toString();
			dev_idcard = pspin_dev_idcard.getSelectedItem().toString();
			
			print = pspin_print.getSelectedItem().toString();
			card = pspin_card.getSelectedItem().toString();
			keyboard = pspin_keyboard.getSelectedItem().toString();
			idcard = pspin_idcard.getSelectedItem().toString();
			
			// 系统参数设置
			strFont = pspin_font.getSelectedItem().toString();
			//property_set("ro.sf.lcd_density",strFont);
			
			String strHideStatusBar = "0";
			strStatusBar = pspin_statusbar.getSelectedItem().toString();
			if(strStatusBar.equals("隐藏状态栏")){
				
				
				Intent intent = new Intent();  
	            intent.setAction("com.android.action.hide_navigationbar");  
	            this.getActivity().sendBroadcast(intent);  
			}
			else{
				
				Intent intent = new Intent();  
	            intent.setAction("com.android.action.display_navigationbar");  
	            this.getActivity().sendBroadcast(intent);  
			}
			//property_set("ro.sf.fullscreen",strHideStatusBar);
			
			strScreen = pspin_screen.getSelectedItem().toString();
			//property_set("ro.sf.hwrotation",strScreen);
			
			if(strScreen.equals("竖屏")){
				this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏 
			}else{
				this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏 
			}
			
			strBootHour = pspin_boothour.getSelectedItem().toString();
			strBootMinute = pspin_bootminute.getSelectedItem().toString();
			setAutoBoot(strBootHour,strBootMinute);
			
			strShutDownHour = pspin_shutdownhour.getSelectedItem().toString();
			strShutDownMinute = pspin_shutdownminute.getSelectedItem().toString();
			setAutoShutdown(strShutDownHour,strShutDownMinute);
			strDefaultApp = pspin_defaultapp.getSelectedItem().toString();
			nGpsSet = pspin_gpsset.getSelectedItemPosition();
    		try{
    			jsconfig.put("print",  print);
    			jsconfig.put("dev_print", dev_print);
    			jsconfig.put("card", card);
    			jsconfig.put("dev_card", dev_card);
    			jsconfig.put("keyboard", keyboard);
    			jsconfig.put("dev_keyboard", dev_keyboard);
    			jsconfig.put("idcard", idcard);
    			jsconfig.put("dev_idcard", dev_idcard);
    			jsconfig.put("boothour", strBootHour);
    			jsconfig.put("bootminute", strBootMinute);
    			jsconfig.put("shutdownhour", strShutDownHour);
    			jsconfig.put("shutdownminute", strShutDownMinute);
    			jsconfig.put("screen", strScreen);
    			jsconfig.put("statusbar", strStatusBar);
    			jsconfig.put("defaultapp", strDefaultApp);
    			
    			str=jsconfig.toString(4);
    			Log.v("debug", str);
    		}catch(JSONException e){
    			e.printStackTrace();
    		}
    		if(str!=null){
    			//if(writeStringTofile(str,path)){
    			/*if(m_config.writeStringTofile(str,path)){
    		        // 更新系统缓冲区
    		        String cmdline1 = "sync";
    		        m_rootcmd.execRootCmdSilent(cmdline1);
    		        
    				//showDialogReboot("生效配置需要重启，你确定要重启系统吗?"); 		        
    				return true;
    			}*/
    		}
    	}
    	return false;
    	
    }
    
    
    /*
     * 	检查/sdcard/wltlib/下   license.lic base.dat 是否存在不存在则从armeabi下复制
     */
    public	void  checkfile(){
		String apppath= psetview.getContext().getFilesDir().getParent();
		apppath+="/lib/";
		

		String strDefaultGps = apppath+"libgps.exDroiddefault.so";
		String strUart1 = apppath+"libgps.uartexDroidS1.so";
		String strUart2 = apppath+"libgps.uartexDroidS2.so";
		String strUart3 = apppath+"libgps.uartexDroidS3.so";
		String strUart4 = apppath+"libgps.uartexDroidS4.so";
		
		String cmdline = "busybox cp " + strDefaultGps + " /system/lib/hw/gps.exDroiddefault.so";
        String cmdline1 = "busybox cp " + strUart1 + " /system/lib/hw/gps.uartexDroidS1.so";
        String cmdline2 = "busybox cp " + strUart2 + " /system/lib/hw/gps.uartexDroidS2.so";
        String cmdline3 = "busybox cp " + strUart3 + " /system/lib/hw/gps.uartexDroidS3.so";
        String cmdline4 = "busybox cp " + strUart4 + " /system/lib/hw/gps.uartexDroidS4.so";
        String cmdline5 = "sync";
        //m_rootcmd.execRootCmdSilent(cmdline);
        //m_rootcmd.execRootCmdSilent(cmdline1);
        //m_rootcmd.execRootCmdSilent(cmdline2);
        //m_rootcmd.execRootCmdSilent(cmdline3);
        //m_rootcmd.execRootCmdSilent(cmdline4);
        //m_rootcmd.execRootCmdSilent(cmdline5);
    	return;
    }
    
    
    public void loadConfig(String path){
    	if(psetview!=null){
    		/*jsconfig= m_config.loadConfigFromFile(path);
    		if(jsconfig!=null){
    			String print=jsconfig.optString("print");
    			String dev_print=jsconfig.optString("dev_print");
    			
    			String card=jsconfig.optString("card");
    			String dev_card=jsconfig.optString("dev_card");
    			
    			String keyboard=jsconfig.optString("keyboard");
    			String dev_keyboard=jsconfig.optString("dev_keyboard");
    			
    			String idcard=jsconfig.optString("idcard");
    			String dev_idcard=jsconfig.optString("dev_idcard");
    			
    			
    			//系统参数配置
//    			int  nCheckFont = js.optInt("checkfont");
//    			int  nCheckStatusBar = js.optInt("checkstatusbar");
//    			int  nCheckScreen = js.optInt("checkscreen");
//    			int  nCheckBoot = js.optInt("checkboot");
//    			int  nCheckShutDown = js.optInt("checkshutdown");
//    			int  nCheckDefaultApp = js.optInt("checkdefaultapp");
    			
    			String strFont= "160";
    			
    			//strFont = property_get("ro.sf.lcd_density",strFont);
    			//strStatusBar = property_get("ro.sf.fullscreen",strStatusBar);
    			//strScreen = property_get("ro.sf.hwrotation",strScreen);
    			
    			String strBootHour = jsconfig.optString("boothour");
    			String strBootMinute = jsconfig.optString("bootminute");
    			String strShutDownHour  = jsconfig.optString("shutdownhour");
    			String strShutDownMinute  = jsconfig.optString("shutdownminute");
    			String strDefaultApp = jsconfig.optString("defaultapp");
    			String strScreen = jsconfig.optString("screen");
    			String strStatusBar = jsconfig.optString("statusbar");
    			
    			int iposition= 0;
				iposition = getFontPosition(strFont);
    			if(iposition!=-1){
    				pspin_font.setSelection(iposition);
    			}
    			
				iposition = getStatusBarPosition(strStatusBar);
    			if(iposition!=-1){
    				pspin_statusbar.setSelection(iposition);
    			}
    			
				iposition = getScreenPosition(strScreen);
    			if(iposition!=-1){
    				pspin_screen.setSelection(iposition);
    			}
    			
    			if(strScreen.equals("竖屏")){
    				this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏 
    			}else{
    				this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏 
    			}
    			
				iposition = getHourPosition(strBootHour);
    			if(iposition!=-1){
    				pspin_boothour.setSelection(iposition);
    			}
				iposition = getMinutePosition(strBootMinute);
    			if(iposition!=-1){
    				pspin_bootminute.setSelection(iposition);
    			}
    			
				iposition = getHourPosition(strShutDownHour);
    			if(iposition!=-1){
    				pspin_shutdownhour.setSelection(iposition);
    			}
				iposition = getMinutePosition(strShutDownMinute);
    			if(iposition!=-1){
    				pspin_shutdownminute.setSelection(iposition);
    			}
    			
				iposition = getDefaultAppPosition(strDefaultApp);
    			if(iposition!=-1){
    				pspin_defaultapp.setSelection(iposition);
    			}
    			
    			//打印机配置
    			iposition=getPrintPosition(print);
    			if(iposition!=-1){
    				pspin_print.setSelection(iposition);
    			}
    			iposition=getStringPosition(dev_print);
    			if(iposition!=-1){
    				pspin_dev_print.setSelection(iposition);
    			}
    			
    			//读卡器配置
    			iposition=getCardPosition(card);
    			if(iposition!=-1){
    				pspin_card.setSelection(iposition);
    			}
    			iposition=getStringPosition(dev_card);
    			if(iposition!=-1){
    				pspin_dev_card.setSelection(iposition);
    			}
    			
    			//密码键盘配置
    			iposition=getKeyboardPosition(keyboard);
    			if(iposition!=-1){
    				pspin_keyboard.setSelection(iposition);
    			}
    			iposition=getStringPosition(dev_keyboard);
    			if(iposition!=-1){
    				pspin_dev_keyboard.setSelection(iposition);
    			}
    			
    			//身份证阅读器配置
    			iposition=getIdcardPosition(idcard);
    			if(iposition!=-1){
    				pspin_idcard.setSelection(iposition);
    			}
    			iposition=getStringPosition(dev_idcard);
    			if(iposition!=-1){
    				pspin_dev_idcard.setSelection(iposition);
    			}			
    		}*/
    	}
    }
    
    
    public int getStringPosition(String str){
    	int inum=this.serialtype.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(serialtype[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public int getFontPosition(String str){
    	int inum= fonttype.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(fonttype[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public int getStatusBarPosition(String str){
    	int inum= statusbartype.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(statusbartype[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public int getScreenPosition(String str){
    	int inum= screentype.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(screentype[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public int getHourPosition(String str){
    	int inum= hourtype.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(hourtype[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public int getMinutePosition(String str){
    	int inum= minutetype.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(minutetype[i])){
    			return i;
    		}
    	}
    	return -1;
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
    
    
    public int getPrintPosition(String str){
    	int inum=define.print_list.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(define.print_list[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public int getCardPosition(String str){
    	int inum=define.card_list.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(define.card_list[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public int getKeyboardPosition(String str){
    	int inum=define.keyboard_list.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(define.keyboard_list[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
    public int getIdcardPosition(String str){
    	int inum=define.idcard_list.length;
    	for(int i=0;i<inum;i++){
    		if(str.equals(define.idcard_list[i])){
    			return i;
    		}
    	}
    	return -1;
    }
    
//	/**
//	 *	从文件获取配置
//	 */
//	public JSONObject loadConfigFromFile(String confFile){
//		File ffs = new File(confFile);
//		if (ffs.exists()){
//			try {
//				byte[] buffer = new byte[2048];
//				FileInputStream conf = new FileInputStream(ffs);
//				try {
//					long byteCount = ffs.length();
//					if(byteCount > 2048){
////						LOG("PM","配置文件太大，文件大小：%d>2048", byteCount);
//						byteCount = 2048;
//					}
//					conf.read(buffer,0,(int) byteCount);
//					String confContext = new String(buffer);
//					JSONObject js=new JSONObject(confContext);
//					conf.close();
//					return js;
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}catch(JSONException e2){
//					e2.printStackTrace();
//				}
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}else{
//			try {
//				ffs.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}

//	public boolean writeStringTofile(String strConf,String confFile){
//		File ffs = new File(confFile);
//		if (!ffs.exists()) {
//			try {
//				ffs.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		try {
//			FileOutputStream conf = new FileOutputStream(ffs);
//			try {
//				conf.write(strConf.getBytes());
//				conf.close();
//				return true;
//			} catch (IOException e2) {
//				// TODO Auto-generated catch block
//				e2.printStackTrace();
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return false;
//	}
	
    public void showDialog(String msg){
        new AlertDialog.Builder(getKKMain())   
        .setTitle("确认")  
        .setMessage(msg)  
        .setPositiveButton("保存",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(saveConfig(DEF.zbdevicesConf)){
					Toast.makeText(getKKMain(), "保存成功!", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getKKMain(), "保存失败!", Toast.LENGTH_SHORT).show();
				}
			}
        })  
        .setNegativeButton("取消", null)  
        .show();  
    }
    
    public void showDialogReboot(String msg){
        new AlertDialog.Builder(getKKMain())   
        .setTitle("确认")  
        .setMessage(msg)  
        .setPositiveButton("重启",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        // 更新系统缓冲区
		        String cmdline1 = "sync";
		        String cmdline2 = "reboot";
		        //m_rootcmd.execRootCmdSilent(cmdline1);
		        //m_rootcmd.execRootCmdSilent(cmdline2);
		        
			}
        })  
        .setNegativeButton("取消", null)  
        .show();  
    }
	
    public void setAutoBoot(String strhour, String strminute){
        
    	int hour = Integer.parseInt(strhour);
    	int minute = Integer.parseInt(strminute);
    	
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
    	
    	
		AlarmManager am_start = (AlarmManager) psetview.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent_start;
        PendingIntent pendingIntent_start;

        intent_start = new Intent(psetview.getContext(),AlarmManagerActivity.class);
        pendingIntent_start = PendingIntent.getBroadcast(psetview.getContext(), 0, intent_start, PendingIntent.FLAG_CANCEL_CURRENT);	

        
		Log.v("info",String.format("设置自动开机时间 : %d时%d分", hour,minute));
		// 1分钟 60*1000 为系统启动时间, 准确时间应该加上 1分钟
		
		//am_start.set(4, bootTime + 60000,pendingIntent_start);
		
		
/*		
		// 之前测试 set 第一个参数设置为4 自动开关机验证ok。  在android5.1上运行报错。现在改为一个
		常量AlarmManager.RTC_WAKEUP. 

*/
		am_start.set(AlarmManager.RTC_WAKEUP, bootTime + 60000,pendingIntent_start);
    }
    
    public void setAutoShutdown(String strhour, String strminute){
        
        int hour = Integer.parseInt(strhour);
    	int minute = Integer.parseInt(strminute);
    	
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
    	
		//自动关机实现
		AlarmManager am = (AlarmManager) psetview.getContext()
                .getSystemService(Context.ALARM_SERVICE);

        Intent intent1 = new Intent("com.eztor.action.ACTION_REQUEST_SHUTDOWN");
  

        PendingIntent pendingIntent = PendingIntent.getBroadcast(psetview.getContext(), 0,
        		intent1, PendingIntent.FLAG_CANCEL_CURRENT);
        am = (AlarmManager) psetview.getContext()
                .getSystemService(Context.ALARM_SERVICE);
        
		Log.v("info",String.format("设置自动关机时间 : %d时%d分", hour,minute));
		
		
        am.set(AlarmManager.RTC_WAKEUP, shutdownTime, pendingIntent);
    }
    
    
    public void StartTimer(int sec, int fd){
        timer.cancel();
        timer.purge();
        timer = new Timer();
        System.out.format("启动定时:%d ms\n", sec * 1000);
        timer.schedule(new TimeoutTask(), sec*1000);
    }

    public class TimeoutTask extends java.util.TimerTask{
        private Activity ctx;
        public TimeoutTask() {

        }

        @Override
        public void run() {
            SendMessage(1001);
            timer.cancel();
        }
    }
    
	public void SendMessage(int id)
	{
 		
		Message msgpwd=new Message();
		msgpwd.what=id;
		myHandler.sendMessage(msgpwd);
	}
	
	  public class MyHandler extends Handler {
			public MyHandler() {
			}

			public MyHandler(Looper L) {
				super(L);
			}

			// 子类必须重写此方法,接受数据
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				// 此处可以更新UI
				switch (msg.what) {
				case 1001:
					//执行重启
			        String cmd_sync = "sync";
			        String cmd_reboot = "reboot";
			        //m_rootcmd.execRootCmdSilent(cmd_sync);
			        //m_rootcmd.execRootCmdSilent(cmd_reboot);
					break;
				}
			}
	    }
}
