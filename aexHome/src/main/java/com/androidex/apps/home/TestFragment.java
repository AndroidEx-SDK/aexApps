package com.androidex.apps.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.androidex.aexlibs.WebJavaBridge.OnJavaBridgeListener;
import com.androidex.common.DummyContent;

import java.util.HashMap;
import java.util.Map;

public class TestFragment extends Fragment implements OnJavaBridgeListener {
	public View 			psetview		=null;
	
    /**
     * 子View管理
     */  
    private Map<String, View> childViews = new HashMap<String, View>();  
    private String currentTag;  
	private LinearLayout container;
	
    public static final String ARG_ITEM_ID = "item_id";
    DummyContent.DummyItem mItem;
    
    

    public TestFragment() {
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
			psetview		= inflater.inflate(R.layout.main, null);
			
			//container = (LinearLayout)psetview.findViewById(R.id.test1); 
			
			try{                                
//		        ComponentName toActivity = new ComponentName("android.intent.category","android.intent.category.HOME");
//
//		        Intent intent = new Intent();
//		        intent.setComponent(toActivity);
//		        intent.setAction("android.intent.action.VIEW");
//
//		        startActivity(intent); 
		        
		        Intent home = new Intent(Intent.ACTION_MAIN);  
		        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
		        home.addCategory(Intent.CATEGORY_HOME);  
		        startActivity(home);
			}
			catch(Exception e)
			{
			   Log.v("go to apk error","------>"+e.toString());
			}
		}
		return psetview;
    }
    
    /**
     * 加载子Activity
     *  
     * @param tag
     * @param intent
     */  
	public void startActivity1(String tag, Intent intent) {  
        currentTag = tag;  
        View originView = childViews.get(tag);  
        /*final Window window = getLocalActivityManager().startActivity(tag,intent);   
        final View decorView = window.getDecorView();  
        if (decorView != originView && originView != null) {  
            if (originView.getParent() != null)  
                ((ViewGroup) originView.getParent()).removeView(originView);  
        }  
        childViews.put(tag, decorView);  
        if (decorView != null) {  
            decorView.setVisibility(View.VISIBLE);  
            decorView.setFocusableInTouchMode(true);  
            ((ViewGroup) decorView)  
                    .setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);  
            if (decorView.getParent() == null) { 
            		container.addView(decorView,  
                        new LinearLayout.LayoutParams(  
                                ViewGroup.LayoutParams.FILL_PARENT,  
                                ViewGroup.LayoutParams.FILL_PARENT));  
            }  
            decorView.requestFocus();  
        } */ 
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
}