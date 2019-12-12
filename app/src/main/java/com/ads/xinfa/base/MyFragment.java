package com.ads.xinfa.base;

import com.ads.xinfa.ui.fragmentMain.FragmentMainActivity;

import androidx.fragment.app.Fragment;

public abstract class MyFragment extends Fragment implements FragmentMainActivity.InitListener {

    public String type;

    //启动画面显示
    public abstract void display();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
