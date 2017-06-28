package com.androidex.utils;

import android.os.Environment;

import com.androidex.config.DeviceConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtils {
    public static String readMyInputStream(InputStream is) {
        byte[] result;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer))!=-1) {
                baos.write(buffer,0,len);
            }
            is.close();
            baos.close();
            result = baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            String errorStr = "获取数据失败。";
            return errorStr;
        }
        return new String(result);
    }

    public static String downloadFile(String url)throws Exception {
        String localFile=null;
        int lastIndex=url.lastIndexOf("/");
        String fileName=url.substring(lastIndex+1);
        OutputStream output=null;
        try {
                /*
                 * 通过URL取得HttpURLConnection
                 * 要网络连接成功，需在AndroidMainfest.xml中进行权限配置
                 * <uses-permission android:name="android.permission.INTERNET" />
                 */
            URL urlObject=new URL(convertImageUrl(url));
            HttpURLConnection conn=(HttpURLConnection)urlObject.openConnection();
            //取得inputStream，并将流中的信息写入SDCard

                /*
                 * 写前准备
                 * 1.在AndroidMainfest.xml中进行权限配置
                 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                 * 取得写入SDCard的权限
                 * 2.取得SDCard的路径： Environment.getExternalStorageDirectory()
                 * 3.检查要保存的文件上是否已经存在
                 * 4.不存在，新建文件夹，新建文件
                 * 5.将input流中的信息写入SDCard
                 * 6.关闭流
                 */
            String SDCard= Environment.getExternalStorageDirectory()+"";
            localFile=SDCard+"/"+ DeviceConfig.LOCAL_FILE_PATH+"/"+fileName+".temp";//文件存储路径
            File file=new File(localFile);
            InputStream input=conn.getInputStream();
            if(!file.exists()){
                String dir=SDCard+"/"+DeviceConfig.LOCAL_FILE_PATH;
                new File(dir).mkdir();//新建文件夹
                file.createNewFile();//新建文件
            }
            output=new FileOutputStream(file);
            //读取大文件
            byte[] buffer = new byte[1024 * 8];
            BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
            BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
            int count = 0, n = 0;
            try {
                while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
                    out.write(buffer, 0, n);
                    count += n;
                }
                out.flush();
            } catch (IOException e) {
                throw e;
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }finally{
            try {
                output.close();
                System.out.println("success");
            } catch (IOException e) {
                System.out.println("fail");
                e.printStackTrace();
            }
        }
        return localFile;
    }

    public static String convertImageUrl(String url){
        String newUrl=url;
        if(url.length()>4){
            String head=url.substring(0,3).toLowerCase();
            if(!head.equals("http")){
                newUrl=DeviceConfig.SERVER_URL+url;
            }
        }
        return newUrl;
    }

    public static File[] getAllLocalFiles(){
        File[] files=new File[0];
        String SDCard= Environment.getExternalStorageDirectory()+"";
        String dir=SDCard+"/"+DeviceConfig.LOCAL_FILE_PATH;
        File path=new File(dir);
        if(path.isDirectory()){
            files=path.listFiles();
        }
        return files;
    }

    public static String getLocalFileFromUrl(String url){
        int lastIndex=url.lastIndexOf("/");
        String fileName=url.substring(lastIndex+1);
        return getLocalFile(fileName);
    }

    public static String getLocalFile(String fileName){
        String SDCard= Environment.getExternalStorageDirectory()+"";
        String fileString=SDCard+ File.separator+DeviceConfig.LOCAL_FILE_PATH+ File.separator+fileName;
        String result=null;
        File file=new File(fileString);
        if(file.exists()){
            if(file.isFile()){
                result=fileString;
            }
        }
        return result;
    }
}
