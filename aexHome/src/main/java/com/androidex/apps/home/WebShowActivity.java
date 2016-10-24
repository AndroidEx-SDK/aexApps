package com.androidex.apps.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class WebShowActivity extends Activity {
	
	private WebView webview; 
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

		webview = (WebView)findViewById(R.id.webview);
		
		webview.getSettings().setPluginState(PluginState.ON);
		webview.getSettings().setJavaScriptEnabled(true); 
		
        //加载需要显示的网页 
        webview.loadUrl("http://www.androidex.cn/"); 
        
        
        webview.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (!webview.hasFocus()) {
						webview.requestFocus();
					}
					break;
				default:
					break;
				}
				
				return false;
			}
		});
		

		// 设置webview为一个单独的client, 这样可以使加载url不调用系统的browser
        webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// Log.i("",
				// ".......EXPID_LOCAL.. shouldOverrideUrlLoading......url=="+url);
				view.loadUrl(url);
				return true;
			}

			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				//Log.e("url", "finish url is " + url);
				webview.requestFocus();
			}
		});
        
    }
    
    @Override  
    public boolean onTouchEvent(MotionEvent event) 
    { 
	    int events[] = {MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, 
	    MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE, 
	    MotionEvent.ACTION_POINTER_DOWN,MotionEvent.ACTION_POINTER_UP, 
	    MotionEvent.EDGE_TOP,MotionEvent.EDGE_BOTTOM,MotionEvent.EDGE_LEFT,MotionEvent.EDGE_RIGHT}; 
	
	    String szEvents[]={"ACTION_DOWN", "ACTION_MOVE", 
	    "ACTION_UP", "ACTION_MOVE", "ACTION_CANCEL", "ACTION_OUTSIDE", 
	    "ACTION_POINTER_DOWN","ACTION_POINTER_UP", 
	    "EDGE_TOP","EDGE_BOTTOM","EDGE_LEFT","EDGE_RIGHT"}; 

	    
	    if(event.getAction() == events[MotionEvent.ACTION_DOWN]){
	    	finish();
	    }
         return super.onTouchEvent(event); 
    } 
    
	// 加载url同时关闭软键盘
	private void loadUrl(EditText urlText) {
		String url = "";
		url = urlText.getText().toString();
		if (!url.toLowerCase().startsWith("http")) {
			url = "http://" + url;
		}

		webview.loadUrl(url);
	}

}
