package com.example.tangjiting.miniweather;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by tangjiting on 2017/11/30.
 */

public class ClearEditText extends android.support.v7.widget.AppCompatEditText implements View.OnFocusChangeListener,TextWatcher {

//    删除按钮的引用
    private Drawable mClearDrawable;

    public ClearEditText(Context context){
        this(context,null);
    }


    public ClearEditText(Context context, AttributeSet attrs) {
        //不加这个方法很多属性不能在xml中定义
        this(context,attrs,android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
        init();
    }

    private void init(){
        //获取EditText的DrawableRight,假如没有设置我们就用默认的图片
        // getCompoundDrawables() Returns drawables for the left(0), top(1), right(2) and bottom(3)
        mClearDrawable = getCompoundDrawables()[2];
//        if (mClearDrawable == null){
//            mClearDrawable = getResources().getDrawable(R.drawable.emotionstore_processncelbtn);
//        }
//        mClearDrawable.setBounds(0,0,mClearDrawable.getIntrinsicWidth(),mClearDrawable.getIntrinsicHeight());
        setClearIconVisible(false);
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

//    不能直接给EditText设置点击事件，记住按下的位置来模拟点击事件
//    当我们按下的位置在EditText的宽度-图标到控件右边的间距-图标的宽度
//    和 EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向没有考虑
    public boolean onTouchEvent(MotionEvent event){
        if (getCompoundDrawables()[2] != null){
            if (event.getAction() == MotionEvent.ACTION_UP){
                boolean touchable = event.getX()>(getWidth() - getPaddingRight() - mClearDrawable.getIntrinsicWidth())
                        &&(event.getX()<(getWidth() - getPaddingRight()));
                if (touchable){
                    this.setText("");
                }
            }
        }
        return super.onTouchEvent(event);
    }

//    当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     @Override
    public void onFocusChange(View v, boolean hasFocus){
        if (hasFocus){
            setClearIconVisible(getText().length()>0);
        }else{
            setClearIconVisible(false);
        }

    }

//    设置清除图标的显示和隐藏，调用setCompoundDrawables为EditText绘制上去
    protected void setClearIconVisible(boolean visible){
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],getCompoundDrawables()[1],right,getCompoundDrawables()[3]);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
//    当输入框中内容发生变化时回调的方法
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        setClearIconVisible(charSequence.length()>0);

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

//    设置晃动动画
//    public  void setShakeAnimation(){
//        this.setAnimation(shakeAnimation(5));
//    }
}
