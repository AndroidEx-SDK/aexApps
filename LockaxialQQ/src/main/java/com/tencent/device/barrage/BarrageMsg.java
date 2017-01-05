package com.tencent.device.barrage;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * 弹幕消息. 目前为群消息相关数据
 * @author dapingyu
 *
 */
public class BarrageMsg implements Parcelable {
    
    public long groupId; //群组ID
    
    public String groupName; //群组名称
    
    public String nickName; //发送者昵称
    
    public String groupNickName; //群名片
    
    public String avatarUrl = "http://q.qlogo.cn/qqapp/222222/5E47CBFC315E02CA4A464D70A35AED5D/100"; //头像URL
    
    public ArrayList<GroupMsg> msgList; //群消息数组

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(groupId);
        dest.writeString(groupName);
        dest.writeString(nickName);
        dest.writeString(groupNickName);
        dest.writeString(avatarUrl);
        dest.writeList(msgList);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("groupId=")
        .append(groupId + "\n")
        .append("groupName=")
        .append(groupName + "\n")
        .append("nickName=")
        .append(nickName + "\n")
        .append("groupNickName=")
        .append(groupNickName + "\n")
        .append("groupName=")
        .append(groupName + "\n")
        .append("avatarUrl=")
        .append(avatarUrl + "\n")
        .append("groupName=")
        .append(groupName + "\n");
        
        if(msgList != null) {
            sb.append("msgListSize=")
            .append(msgList.size() + "\n");
            for(GroupMsg msg : msgList) {
                sb.append(msg.toString());
            }
        }
        
        return sb.toString();
    }
    public BarrageMsg(){
    
    };
    public BarrageMsg(Parcel parcel){
        groupId = parcel.readLong();
        groupName = parcel.readString();
        nickName =parcel.readString();
        groupNickName = parcel.readString();
        avatarUrl = parcel.readString();
        msgList = parcel.readArrayList(ClassLoader.getSystemClassLoader());
    }
    
    public static final Creator<BarrageMsg> CREATOR = new Creator<BarrageMsg>() {
        @Override
        public BarrageMsg createFromParcel(Parcel source) {
            return new BarrageMsg(source);
        }

        @Override
        public BarrageMsg[] newArray(int size) {
            return new BarrageMsg[size];
        }
    };
    
    /**
     * 存储消息数组
     * @author dapingyu
     *
     */
    public static class GroupMsg implements Parcelable{
        public int msgType;
        public String msgContent;
        public int subContent;
        @Override
        public String toString() {          
			return "msgType=" + msgType + " msgContent=" + msgContent + " voiceSwitch=" + subContent + "\n";			
        }
        @Override
        public int describeContents() {
            return 0;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(msgType);
            dest.writeString(msgContent);
        }
        public GroupMsg(){
            
        };
        public GroupMsg(Parcel source) {
            msgType = source.readInt();
            msgContent = source.readString();
        }
        
        public void setMsgContent(byte[] bytes) {
        	try {
				msgContent = new String(bytes, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        }
        
        public static final Creator<GroupMsg> CREATOR = new Creator<GroupMsg>() {
            @Override
            public GroupMsg createFromParcel(Parcel source) {
                return new GroupMsg(source);
            }

            @Override
            public GroupMsg[] newArray(int size) {
                return new GroupMsg[size];
            }
        };
    }
}
