/*
 * 
 * Copyright 2009 Cedric Priscal  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");  
 * you may not use this file except in compliance with the License.  
 * You may obtain a copy of the License at  
 * 
 * http://www.apache.org/licenses/LICENSE-2.0  
 * 
 * Unless required by applicable law or agreed to in writing, software  
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
 * See the License for the specific language governing permissions and  
 * limitations under the License.   
 */ 

package com.androidex.face.idcard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.synjones.multireaderlib.DataTransInterface;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReaderSerialPort implements DataTransInterface {
 
	//private static final String TAG = "SerialPort";          

	private final long Tresponse = 1000;
	
	private boolean serialOpen=false;
	
    Context mContext;
    
	protected OutputStream mOutputStream;
	protected InputStream mInputStream;

	private Bitmap bmp;
  

	public ReaderSerialPort(Activity mContext, OutputStream mOutputStream, InputStream mInputStream )
	{		
		this.mInputStream=mInputStream;
		this.mOutputStream=mOutputStream;
		this.mContext=mContext;
	}
	
	private byte xorchk(byte[] b, int offset, int length) {
		byte chk = 0;
		int i;
		for (i = 0; i < length; i++) {
			chk ^= b[offset + i];
		}
		return chk;
	}

	


	public void closeSerialPort(){
		serialOpen=false;
    	
	}
	
					
	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();
	public native int readCard();
	public native int getField(int fieldId);

	@Override
	public void sendData(byte[] data, int datalen) {
		// TODO Auto-generated method stub
		//Log.e("tag1111", Helper.ByteArrToHex(data));
		//data=new byte[]{0x02,0x30,0x33,0x34,0x31,0x30,0x30,0x34,0x31,0x0c};
		//Log.e("PadWrite", Helper.ByteArrToHex(data));
		//datalen=data.length;
		try {
			mOutputStream.write(data, 0, datalen);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int recvData(byte[] recvbuf, int offset) {
		// TODO Auto-generated method stub
		//Log.e("output " + offset, Helper.ByteArrToHex(recvbuf));
		try{
			if(mInputStream.available()>0)
				return mInputStream.read(recvbuf, offset, recvbuf.length-offset);
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	
		return 0;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		byte buffer[]=new byte[4096];
		try {
			while(mInputStream.available()>0)		
				mInputStream.read(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}


}
