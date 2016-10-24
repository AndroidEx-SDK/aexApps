package com.androidex.apps.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.androidex.aexlibs.WebJavaBridge.OnJavaBridgeListener;
import com.androidex.common.DummyContent;


public class DevicesFragment extends Fragment implements OnJavaBridgeListener{
	public View 			psetview		=null;
	
	private ImageView card_button_test;
	private ImageView serial_button_test;
	private ImageView vgatohdmi_button_test;
	private ImageView print_button_test;
	private ImageView mifareonecard_button_test;
	private ImageView devicesmanager_button_test;
	private ImageView readidcard_button_test;
	private ImageView readidusbcard_button_test;
	private ImageView onekeyboard_button_test;
	private ImageView keyboard_button_test;
	private ImageView zigbee_button_test;
	private ImageView rfm13card_button_test;
	private ImageView mf30card_button_test;
	private ImageView button_setup_apk;
	private ImageView s3card_button_test;
	private ImageView button_control_io;
	private ImageView coffee_button_test;

    public static final String ARG_ITEM_ID = "item_id";
    DummyContent.DummyItem mItem;
    

    public DevicesFragment() {
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
//        if (mItem != null) {
//        	this.getActivity().getActionBar().setTitle(mItem.content);
//        }
		super.onCreateView(inflater, container, savedInstanceState);
		LinearLayout.LayoutParams wvParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		if (psetview == null) {
			psetview		= inflater.inflate(R.layout.device_main, null);  
		
	        //外围设备按钮响应
	        addClickListen();
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
    
    public void addClickListen(){
    	
        
        //标准串口测试按键相应事件
        serial_button_test=(ImageView)psetview.findViewById(R.id.ImageView_serial);
        serial_button_test.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DevicesSerialTool");
            }
        }); 

        //打印机测试按键相应事件
        print_button_test=(ImageView)psetview.findViewById(R.id.ImageView_print);
        print_button_test.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DevicesPrintMain");
            }
        }); 
        
        // 密码键盘测试
        keyboard_button_test = (ImageView)psetview.findViewById(R.id.ImageView_keyboard);
        keyboard_button_test.setOnClickListener(new OnClickListener() {
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DeviceskeyboardMain");
            }
        }); 
        
        // 华视串口型读取身份证信息测试
        readidcard_button_test = (ImageView)psetview.findViewById(R.id.ImageView_idcard_huashi);
        readidcard_button_test.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DevicesIDReaderMain");
            }
        });
        
        // 新中新USB型读取身份证信息测试
        readidusbcard_button_test = (ImageView)psetview.findViewById(R.id.ImageView_idcard_xzx);
        readidusbcard_button_test.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DevicesIDCardMain");
            }
        });
        
        // Mifareone读卡器测试
        mifareonecard_button_test = (ImageView)psetview.findViewById(R.id.ImageView_mifare);
        mifareonecard_button_test.setOnClickListener(new OnClickListener() {
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DevicesMifareCardReaderMain");
            }
        }); 
        
        //zigbee透传串口测试
        zigbee_button_test = (ImageView)psetview.findViewById(R.id.ImageView_zigbee);
        zigbee_button_test.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DevicesZigbeeMain");
            }
        });
        
        // mf30card读卡器测试
        mf30card_button_test = (ImageView)psetview.findViewById(R.id.ImageView_mf30card);
        mf30card_button_test.setOnClickListener(new OnClickListener() {
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DevicesMf30CardMain");
            }
        }); 
        
        // s3 card读卡器测试
        s3card_button_test= (ImageView)psetview.findViewById(R.id.ImageView_s3card);
        s3card_button_test.setOnClickListener(new OnClickListener() {
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DevicesS3CardMain");
            }
        }); 
        
        
        // 智能中断IO控制测试
        button_control_io  = (ImageView)psetview.findViewById(R.id.ImageView_control_io);
        button_control_io.setOnClickListener(new OnClickListener() {
            @Override  
            public void onClick(View v) {
            startPluginActivity(psetview,"com.eztor.devices.DevicesControlIo");
            }
        }); 
        
        
    }

    private void startPluginActivity(View v,String cls) {
        Intent intent = new Intent();
        try{
            // 构造的参数为当前Context和目标组件的类路径名
            ComponentName cn = new ComponentName(v.getContext(), cls);
            intent.setComponent(cn);
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
