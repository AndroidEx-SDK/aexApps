package com.androidex.apps.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridviewActivity extends Activity {
    private GridView gview;
    private List<Map<String, Object>> data_list;
    private SimpleAdapter sim_adapter;
    // 图片封装为一个数组
    private int[] icon = { R.drawable.balance, R.drawable.balance,
            R.drawable.balance, R.drawable.balance, R.drawable.balance,
            R.drawable.balance, R.drawable.balance, R.drawable.balance,
            R.drawable.balance, R.drawable.balance, R.drawable.balance,
            R.drawable.balance };
    private String[] iconName = { "item1", "item2", "item3", "item4", "item5", "item6", "item7",
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
        sim_adapter = new SimpleAdapter(this, data_list, R.layout.item, from, to);
        //配置适配器
        gview.setAdapter(sim_adapter);
        gview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // 通过包名获取要跳转的app，创建intent对象
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.devicedemo");
                        Log.e("===========>", String.valueOf(intent));

                        // 这里如果intent为空，就说名没有安装要跳转的应用嘛
                        if (intent != null) {
                            // 这里跟Activity传递参数一样的，不要担心怎么传递参数，还有接收参数也是跟Activity和Activity传参数一样
//                            intent.putExtra("name", "Liu xiang");
//                            intent.putExtra("birthday", "1983-7-13");
                            startActivity(intent);
                            Log.e("------------>", String.valueOf(intent));
                        } else {
                            // 没有安装要跳转的app应用，提醒一下
                            Toast.makeText(getApplicationContext(), "没有找到app,赶紧下载安装这个APP吧", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 1:
                        // 通过包名获取要跳转的app，创建intent对象
                        Intent intent1 = getPackageManager().getLaunchIntentForPackage("xxxxxxxxx");
                        Log.e("===========>", String.valueOf(intent1));

                        // 这里如果intent为空，就说名没有安装要跳转的应用嘛
                        if (intent1 != null) {
                            // 这里跟Activity传递参数一样的，不要担心怎么传递参数，还有接收参数也是跟Activity和Activity传参数一样
//                            intent.putExtra("name", "Liu xiang");
//                            intent.putExtra("birthday", "1983-7-13");
                            startActivity(intent1);
                            Log.e("------------>", String.valueOf(intent1));
                        } else {
                            // 没有安装要跳转的app应用，提醒一下
                            Toast.makeText(getApplicationContext(), "没有找到app,赶紧下载安装这个APP吧", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 2:

                        break;
                    case 3:

                        break;
                    case 4:

                        break;
                    case 5:

                        break;
                    default:
                        break;
                }
            }
        });
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
