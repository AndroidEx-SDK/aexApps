package com.androidex.comassistant;

/**
 * Created by Administrator on 2017/4/4.
 */

import com.androidex.logger.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author benjaminwan
 *         数据转换工具
 */
public class MyFunc {
    //-------------------------------------------------------
    // 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    static public int isOdd(int num) {
        return num & 0x1;
    }

    //-------------------------------------------------------
    static public int HexToInt(String inHex)//Hex字符串转int
    {
        return Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------
    static public byte HexToByte(String inHex)//Hex字符串转byte
    {
        return (byte) Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------
    static public String Byte2Hex(Byte inByte)//1字节转2个Hex字符
    {
        return String.format("%02x", inByte).toUpperCase();
    }

    //-------------------------------------------------------
    static public String ByteArrToHex(byte[] inBytArr)//字节数组转转hex字符串
    {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;
        for (int i = 0; i < j; i++) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------//字节数组转转hex字符串，可选长度
    static public String ByteArrToHex(byte[] inBytArr, int offset, int byteCount)
    {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;
        for (int i = offset; i < j; i++) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    //转hex字符串转字节数组
    static public byte[] HexToByteArr(String inHex)//hex字符串转字节数组
    {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {//奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {//偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }
    public static String runShellCommand(String cmd) {
        String ret = "";
        byte[] retBytes = new byte[2048];

         Log.d("xxx", String.format("runShellCommand(%s)", cmd));
        try {
            cmd += "\n";
            Process exeEcho1 = Runtime.getRuntime().exec("su -");
            OutputStream ot = exeEcho1.getOutputStream();
            ot.write(cmd.getBytes());
            DataOutputStream dataOutputStream=new DataOutputStream(ot);
            //将命令写入
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            ot.flush();
            dataOutputStream.close();
            ot.close();
            InputStream in = exeEcho1.getInputStream();
            int r = in.read(retBytes);
            if (r > 0)
                ret = new String(retBytes, 0, r);
        } catch (IOException e) {
            Log.e("AexService", "shell cmd wrong:" + e.toString());
        }
        return ret;
    }
}