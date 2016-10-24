package com.androidex.apps.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.androidex.aexlibs.WebJavaBridge.OnJavaBridgeListener;
import com.androidex.common.DummyContent;

public class DetailActivity extends FragmentActivity {

    private OnJavaBridgeListener jbListener;

    static {
        DummyContent.addItem(new DummyContent.DummyItem("setting","系统设置","",SetFragment.class,"url=setting",true,0));
        DummyContent.addItem(new DummyContent.DummyItem("javatest","java API设备测试","",DevicesFragment.class,"url=javatest",true,0));
        DummyContent.addItem(new DummyContent.DummyItem("webtest","web API测试测试","",DetailFragment.class,"url=file:///android_asset/index.html",true,0));
    };

    private void showFragment(int res,DummyContent.DummyItem info) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		if(info.hasView()){
			if(fragmentManager.popBackStackImmediate(info.id, 0)){
				return;
			}
			//ft.show(info.getView()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
		}
		ft.replace(res,info.getView(),info.id);
		//ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(info.id);
		ft.commit();
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉头信息
       requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_item_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            DummyContent.DummyItem aItem = DummyContent.findItemByTag(getIntent().getStringExtra(DetailFragment.ARG_ITEM_ID));
            if(aItem != null){
//	            Bundle arguments = new Bundle();
//	            arguments.putString(ParkDetailFragment.ARG_ITEM_ID,
//	                    getIntent().getStringExtra(ParkDetailFragment.ARG_ITEM_ID));
//	            ParkDetailFragment fragment = new ParkDetailFragment();
//	            fragment.setArguments(arguments);
//	            getSupportFragmentManager().beginTransaction()
//	                    .add(R.id.item_detail_container, fragment)
//	                    .commit();
	            showFragment(R.id.item_detail_container,aItem);	
	            jbListener = (OnJavaBridgeListener) aItem.getView();            	
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, DevicesListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
