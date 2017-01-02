package com.tencent.devicedemo;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xinshuhao on 16/7/24.
 */
public class Bg_Adapter<T> extends PagerAdapter {

    private List<T> mBannerList;
    private Context mContext;
    private ArrayList<Integer> imageViews=new ArrayList<>(Arrays.asList(R.mipmap.bg1,R.mipmap.bg2,R.mipmap.bg3,R.mipmap.bg4,R.mipmap.bg5));

    public Bg_Adapter(Context context, List<T> bannerList) {

        this.mContext = context;
        this.mBannerList = bannerList;

    }

    @Override
    public int getCount() {
        return this.mBannerList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView=new ImageView(mContext);
        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);

        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        T object=this.mBannerList.get(position);

        int imID=imageViews.get(position);
        /*String imageUrl=null;
        if(onject instanceof  Banner.DataEntity){
            imageUrl=((Banner.DataEntity)object).getUrl();
        }*/
        /*加载网络图片 可替换接口实现*/
       // BaseApplication.getApplication().getImageLoader().displayImage(imageUrl,imageView,BaseApplication.getApplication().getDisplayOptions());
       imageView.setBackground(mContext.getResources().getDrawable(imID));/**显示本地图片*/
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
        container.removeView((View)object);
    }
}