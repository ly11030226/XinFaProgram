package com.jzl.xinfafristversion.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.bean.MyBean;

/**
 * 同一个画布中显示所有的View
 */
public class ResViewGroup extends FrameLayout {
    private static final String TAG = "ResViewGroup";
    public ResViewGroup(Context context) {
        super(context);
    }

    public ResViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量子View
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            int width = view.getMeasuredWidth();
            int height = view.getMeasuredHeight();
            if (view instanceof ResLoopView) {
                MyLogger.i(TAG,"ResLoopView width ... "+width +" height ... "+height);
            }else{
                MyLogger.i(TAG,"MyMarqueeView width ... "+width +" height ... "+height);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        MyLogger.i(TAG,"onLayout child left ... "+left
        +" top ... "+top+" right ... "+right+" bottom ...."+bottom);
        int chileCount = getChildCount();
        MyLogger.i(TAG,"onLayout child count ... "+chileCount);
        if (chileCount > 0) {
            for (int i = 0; i < chileCount; i++) {
                View view = getChildAt(i);
                MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean = null;
                if (view instanceof ResLoopView) {
                    areaBean = ((ResLoopView)view).getAreaBean();
                }else if (view instanceof TestMarqueeView){
                    areaBean = ((TestMarqueeView)view).getAreaBean();
                }
                if (areaBean != null) {
                    int l = Integer.valueOf(areaBean.getInfo().getLeft());
                    int t = Integer.valueOf(areaBean.getInfo().getTop());
                    int w = view.getMeasuredWidth();
                    int h = view.getMeasuredHeight();
                    int r,b;
                    //宽度差值
                    int dvWidth = right - l;
                    if (w > dvWidth) {
                        r = right;
                    }else{
                        r = l + w;
                    }
                    //高度差值
                    int dvHeight = bottom - t;
                    if (h > dvHeight) {
                        b = bottom;
                    }else{
                        b = t + h;
                    }
                    view.layout(l,t,r,b);
                }
            }
        }
    }

    public void startFlipping(){
        final int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i) instanceof MyMarqueeView) {
                    ((MyMarqueeView)getChildAt(i)).startFlipping();
                }
            }
        }
    }

    public void stopFlipping(){
        final int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i) instanceof MyMarqueeView) {
                    ((MyMarqueeView)getChildAt(i)).stopFlipping();
                }
            }
        }
    }
}
