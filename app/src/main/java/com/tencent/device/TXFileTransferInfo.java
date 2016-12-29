package com.tencent.device;

import android.os.Parcel;
import android.os.Parcelable;

public class TXFileTransferInfo implements Parcelable
{
    public String file_path;                //文件本地路径
    public byte[] file_key;                //文件后台索引
    public int    transfer_type;     //传输类型：1 上传; 2 下载; 3 点对点发送; 4 点对点接收 
    public byte[] buffer_extra;         //额外参数或信息
    public String business_name;    //业务名称：可以根据该字段的值，对接收到的文件做不同的处理
    public byte[] mini_token;            // 小文件的扩展key
    public long 		file_size;		// 文件大小
    public int 			channel_type;	// 通道类型：TXDeviceService.transfer_channeltype_FTN TXDeviceService.transfer_channeltype_MINI
    public int 			file_type;		// 文件类型：TXDeviceService.transfer_filetype_image  TXDeviceService.transfer_filetype_video  TXDeviceService.transfer_filetype_audio  TXDeviceService.transfer_filetype_other
    
    public TXFileTransferInfo() 
    {
        
    }
    
    public TXFileTransferInfo(Parcel parcel)
    {
        this.file_path            = parcel.readString();
        parcel.readByteArray(this.file_key);
        this.transfer_type    = parcel.readInt();
        parcel.readByteArray(this.buffer_extra);
        this.business_name    = parcel.readString();
        parcel.readByteArray(this.mini_token);
        this.file_size    = parcel.readLong();
        this.channel_type    = parcel.readInt();
        this.file_type    = parcel.readInt();
    }
    
    @Override
    public int describeContents() 
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel parcel, int arg1) 
    {
        // TODO Auto-generated method stub
        parcel.writeString(this.file_path);
        parcel.writeByteArray(this.file_key);
        parcel.writeInt(this.transfer_type);
        parcel.writeByteArray(this.buffer_extra);
        parcel.writeString(this.business_name);
        parcel.writeByteArray(this.mini_token);
        parcel.writeLong(this.file_size);
        parcel.writeInt(this.channel_type);
        parcel.writeInt(this.file_type);
    }
    
    public static final Parcelable.Creator<TXFileTransferInfo> CREATOR = new Parcelable.Creator<TXFileTransferInfo>()
    {

        @Override
        public TXFileTransferInfo createFromParcel(Parcel parcel) 
        {
            // TODO Auto-generated method stub
            return new TXFileTransferInfo(parcel);
        }

        @Override
        public TXFileTransferInfo[] newArray(int size) 
        {
            // TODO Auto-generated method stub
            return new TXFileTransferInfo[size];
        }
    };
}
