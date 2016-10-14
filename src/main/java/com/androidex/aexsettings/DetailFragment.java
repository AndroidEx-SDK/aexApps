package com.androidex.aexsettings;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;

import com.androidex.common.DummyContent;
import com.androidex.common.WebviewFragment;

public class DetailFragment extends WebviewFragment {

    public static final String ARG_ITEM_ID = "item_id";

    DummyContent.DummyItem mItem;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = DummyContent.findItemByTag(getArguments().getString(ARG_ITEM_ID));
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);
        if (mItem != null) {
        	//this.getActivity().getActionBar().setTitle(mItem.content);
            //((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.content);
        }
        return super.onCreateView(inflater, container, savedInstanceState);//rootView;
    }

    
}
