package com.androidex.ble;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RadioButton;

/**
 * Created by cts on 17/6/3.
 */

public class MyRadioButton extends RadioButton {
    //图片大小
    //private int drawableSize;

    public MyRadioButton(Context context) {
        this(context,null);
    }

    public MyRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyRadioButton);
        //drawableSize = a.getDimensionPixelSize(R.styleable.MyRadioButton_rbDrawableTopSize, 50);
        Drawable drawableTop = a.getDrawable(R.styleable.MyRadioButton_rbDrawableTop);

        //释放资源
        a.recycle();

        setCompoundDrawablesWithIntrinsicBounds(null,drawableTop,null,null);
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        if(top != null){
            //这里只要改后面两个参数就好了，一个宽一个是高，如果想知道为什么可以查找源码
            top.setBounds(0,0,100,100);
        }
        setCompoundDrawables(left,top,right,bottom);
    }

    @Override
    public void setButtonDrawable(Drawable drawable) {


        super.setButtonDrawable(drawable);
    }
}
