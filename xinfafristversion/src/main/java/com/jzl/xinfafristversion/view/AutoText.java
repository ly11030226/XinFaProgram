package com.jzl.xinfafristversion.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class AutoText extends AppCompatTextView implements Runnable {
    private int currentScrollX;// 当前滚动的位置
    private boolean isStop = false;
    private int textWidth;
    private boolean isMeasure = false;
    private  int textSpeed=4;
    public AutoText(Context context, int textSpeed) {
        super(context);
        this.textSpeed=textSpeed;
        // TODO Auto-generated constructor stub
    }

    public AutoText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if (!isMeasure) {// 文字宽度只需获取一次就可以了
            getTextWidth();
            isMeasure = true;
        }
    }

    /**
     * 获取文字宽度
     */
    private void getTextWidth() {
        Paint paint = this.getPaint();
        String str = this.getText().toString();
        textWidth = (int) paint.measureText(str);
    }

    // 重写setText 在setText的时候重新计算text的宽度
    @Override
    public void setText(CharSequence text, BufferType type) {
        // TODO Auto-generated method stub
        super.setText(text, type);
        this.isMeasure = false;
    }
    public void  setTextSpeed(int textSpeed){this.textSpeed=textSpeed;

    }
    @Override
    public void run() {
        currentScrollX -= textSpeed/10;// 滚动速度
//        Log.e("textSpeed",textSpeed+"");
        scrollTo(currentScrollX, 0);
        if (isStop) {
            return;
        }
        if (getScrollX() <= -(this.getWidth())) {
            scrollTo(textWidth, 0);
            currentScrollX = textWidth;
            // return;
        }
        postDelayed(this, 5);
    }

    // 开始滚动
    public void startScroll() {
        isStop = false;
        this.removeCallbacks(this);
        post(this);
    }

    // 停止滚动
    public void stopScroll() {
        isStop = true;
        // textWidth=currentScrollX; //随时停止
    }

    // 从头开始滚动
    public void startFor0() {
        currentScrollX = 0;
        startScroll();
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
