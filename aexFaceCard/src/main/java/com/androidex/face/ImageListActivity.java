package com.androidex.face;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.androidex.face.db.FaceDao;
import com.androidex.face.utils.UserInfo;

import java.util.ArrayList;

/**
 * 查看存入的人脸信息界面
 */
public class ImageListActivity extends AppCompatActivity {
    private ArrayList<UserInfo> arrayList;
    public FaceDao faceDao;
    public MyAdapter myAdapter;
    public GridView gv_face;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagelist);
        faceDao = FaceDao.getInstance(this);
        gv_face = (GridView) findViewById(R.id.gv_face);
        //获取数据源
        arrayList = faceDao.getUserinfo();
        myAdapter = new MyAdapter();
        gv_face.setAdapter(myAdapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_1){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView==null){
                convertView = LayoutInflater.from(ImageListActivity.this).inflate(R.layout.layout,parent,false);
                holder = new Holder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageview);
                convertView.setTag(holder);
            }else{
                holder = (Holder) convertView.getTag();
            }
            String path = arrayList.get(position).getFacepath();
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            holder.imageView.setImageBitmap(bitmap);
            return convertView;
        }
        class Holder{
            ImageView imageView;
        }

    }
}
