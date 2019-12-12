package com.ads.xinfa.myInterface;

import android.content.Context;
import android.view.ViewGroup;

import com.ads.xinfa.bean.MyBean;

public abstract class IDisplayType {
    public int index;
    public Context context;
    public IDisplayType(int index,Context context) {
        this.index = index;
        this.context = context;
    }
    public abstract ViewGroup display(MyBean.GroupsBean.GroupBean.AreasBean areas);
}
