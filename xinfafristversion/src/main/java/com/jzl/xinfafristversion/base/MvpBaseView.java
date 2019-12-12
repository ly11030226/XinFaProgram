package com.jzl.xinfafristversion.base;

import android.content.Context;

/**
 * Created by Ly on 2018/4/10.
 */

public interface MvpBaseView<T> {

    void setPresenter(T presenter);
    void showTip(String msg);
    void showError(String error);
    boolean isActive();
    Context getContext();

}
