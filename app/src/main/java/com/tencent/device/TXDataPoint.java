package com.tencent.device;

import android.os.Parcel;
import android.os.Parcelable;

public class TXDataPoint  implements Parcelable
{
	public long 	property_id;			//属性ID，需要向开平申请
	public String 	property_val;			//属性取值，任何类型的数据都必须转化为byte数组
	public int 		sequence;				//序列号，如果填了序列号，后台实现根据序列号记录其操作状态，app可根据序列号来判断这个操作是否成功
	public int		ret_code;				//当apiname为set_data_point_res时，此值有意义，表示设置的返回值
	
	public TXDataPoint()
	{
		
	}
	
	public TXDataPoint(Parcel parcel)
	{
		property_id			= parcel.readLong();
		property_val		= parcel.readString();
		sequence			= parcel.readInt();
		ret_code			= parcel.readInt();
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) 
	{
		// TODO Auto-generated method stub
		parcel.writeLong(this.property_id);
		parcel.writeString(this.property_val);
		parcel.writeInt(this.sequence);
		parcel.writeInt(this.ret_code);
	}
	
	public static final Parcelable.Creator<TXDataPoint> CREATOR = new Parcelable.Creator<TXDataPoint>()
	{
		
		@Override
		public TXDataPoint createFromParcel(Parcel parcel) 
		{
			// TODO Auto-generated method stub
			return new TXDataPoint(parcel);
		}

		@Override
		public TXDataPoint[] newArray(int size)
		{
			// TODO Auto-generated method stub
			return new TXDataPoint[size];
		}
		
	};
	
}
