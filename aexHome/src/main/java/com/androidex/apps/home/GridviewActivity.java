package com.androidex.apps.home;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.compat.PackageManagerCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.androidex.apps.home.R.layout.item;

public class GridviewActivity extends Activity {
    private GridView gview;
    private List<Map<String, Object>> data_list;
    private SimpleAdapter sim_adapter;
    private File[] plugins;
    PackageInfo packageInfo;
    // 图片封装为一个数组
    private int[] icon = { R.drawable.balance, R.drawable.balance,
            R.drawable.balance, R.drawable.balance, R.drawable.balance,
            R.drawable.balance, R.drawable.balance, R.drawable.balance,
            R.drawable.balance, R.drawable.balance, R.drawable.balance,
            R.drawable.balance };
    private String[] iconName = { "textNFC", "aexNFC", "item3", "item4", "item5", "item6", "item7",
            "item8", "item9", "item10", "item11", "item12" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);
        gview = (GridView) findViewById(R.id.gview);
        //新建List
        data_list = new ArrayList<Map<String, Object>>();
        //获取数据
        getData();
        //新建适配器
        String [] from ={"image","text"};
        int [] to = {R.id.image,R.id.text};
        sim_adapter = new SimpleAdapter(this, data_list, item, from, to);
        //配置适配器
        gview.setAdapter(sim_adapter);



        //每一个item的点击事件
        gview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    //打开textNFC
                    case 0:
                        PackageManager pm = getPackageManager();
                        //textNFC的主页面包名
                        Intent intent = pm.getLaunchIntentForPackage("com.example.cts.textnfc");
                        Log.e("===========",String.valueOf(intent));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    //打开aexNFC
                    case 1:
                        PackageManager pm1 = getPackageManager();
                        //aexNFC的主页面包名
                        Intent intent1 = pm1.getLaunchIntentForPackage("com.example.androidex");
                        Log.e("===========",String.valueOf(intent1));
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);
                        break;
                }
            }
        });
        //获取插件
        File file = new File(Environment.getExternalStorageDirectory(), "/plugin");
        plugins = file.listFiles();
        //没有插件
        if (plugins == null || plugins.length == 0) {
            return;
        }
        //安装插件
        else {
            //i的最大值为文件夹内apk的数量
            for (int i=0;i<2;i++){
                try {
                    PluginManager.getInstance().installPackage(plugins[i].getAbsolutePath(), PackageManagerCompat.INSTALL_REPLACE_EXISTING);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public List<Map<String, Object>> getData(){
        //cion和iconName的长度是相同的，这里任选其一都可以
        for(int i=0;i<icon.length;i++){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("image", icon[i]);
            map.put("text", iconName[i]);
            data_list.add(map);
        }
        return data_list;
    }


}
