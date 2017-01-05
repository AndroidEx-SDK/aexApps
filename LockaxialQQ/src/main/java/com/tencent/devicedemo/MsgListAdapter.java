package com.tencent.devicedemo;

import java.util.ArrayList;
import java.util.List;

import com.tencent.device.MsgPack;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MsgListAdapter extends BaseAdapter{
	private Context context;
	private List<MsgPack> msgPackList;
	
	public MsgListAdapter(Context applicationContext) {
		// TODO Auto-generated constructor stub
		this.context = applicationContext;
		msgPackList = new ArrayList<MsgPack>();
	}
	
	public void addMsgPack(MsgPack msgPack)
	{
		msgPackList.add(msgPack);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return msgPackList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return msgPackList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.msglayout, null);
		}
		
		TextView msgtext = (TextView) convertView.findViewById(R.id.msgcontext);
		String strMsgContext = msgPackList.get(position).strText;
		if (!msgPackList.get(position).bIsSelf) {
			msgtext.setGravity(Gravity.LEFT);
		}
		else {
			msgtext.setGravity(Gravity.RIGHT);
		}
		msgtext.setText(strMsgContext);
		return convertView;
	}

}
