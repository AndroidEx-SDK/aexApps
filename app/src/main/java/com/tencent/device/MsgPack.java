package com.tencent.device;

import java.io.Serializable;

public class MsgPack implements Serializable {
	private static final long serialVersionUID = 1L;
	public int dwMsgSequence;
	public long uSendUin;			//发送者Uin
	public boolean bSendResult;		//消息发送结果
	public String strText;			//消息正文
	public boolean bIsSelf;			//是否自己发送的消息，用于展示排布
	public byte[] buffer;			//rawmsg buffer
	public int length;				//rawmsg length
	
	public void initReceivedMsg(int dwMsgSequence, long uSendUin, String strText)
	{
		this.dwMsgSequence = dwMsgSequence;
		this.uSendUin = uSendUin;
		this.strText = strText;
	}
	
	public void initSendMsgResult(int dwMsgSequence, long uSendUin, boolean bSendResult)
	{
		this.dwMsgSequence = dwMsgSequence;
		this.uSendUin = uSendUin;
		this.bSendResult = bSendResult;
	}
	
	public void initRawMsg(int dwMsgSequence, long uSendUin, byte[] buffer, int length)
	{
		this.dwMsgSequence = dwMsgSequence;
		this.uSendUin = uSendUin;
		this.buffer = buffer;
		this.length = length;
	}
}
