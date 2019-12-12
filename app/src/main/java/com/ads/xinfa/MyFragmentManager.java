package com.ads.xinfa;

import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.MyFragment;
import com.ads.xinfa.bean.MyBean;
import com.ads.xinfa.ui.displayImage.DisplayImageFragment;
import com.ads.xinfa.ui.displayMarquee.DisplayMarqueeFragment;
import com.ads.xinfa.ui.displayVideo.DisplayVideoFragment;
import com.ads.xinfa.ui.displayVideoAndImage.DisplayVideoAndImageFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MyFragmentManager {

    private List<MyFragment> mFragmentList = new ArrayList<>();
    private FragmentManager mFm;


    public MyFragmentManager(FragmentManager fragmentManager) {
        this.mFm = fragmentManager;
    }

    public void showFragment(int viewId,HashMap<String, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean> map){
        try {
            clear();
            if (map!=null){
                for (Map.Entry<String, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean> entry : map.entrySet()) {
                    String type = entry.getKey();
                    MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean = entry.getValue();
                    MyFragment myFragment = getMyFragment(type);
                    if (myFragment!=null) {
                        myFragment.doInit(areaBean);
                        mFragmentList.add(myFragment);
                        FragmentTransaction ft = mFm.beginTransaction();
                        ft.add(viewId, myFragment,type);
                        ft.commit();
                    }
                }
                boolean isOnlyShowOne = map.size() == 1?true:false;
                //隐藏所有Fragment
                hideAllFragment();
                if (isOnlyShowOne) {
                    FragmentTransaction ft = mFm.beginTransaction();
                    MyFragment f = mFragmentList.get(0);
                    ft.show(f).commit();
                }else{
                    //多个类型的时候 根据一个默认显示字段去显示Fragment
                }
                //最终调用每个Fragment的显示方法
                //            mFragmentList.get(mDefaultDisplayIndex).display();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideAllFragment(){
        FragmentTransaction ft = mFm.beginTransaction();
        for(MyFragment fragment : mFragmentList){
            if (fragment!=null) {
                ft.hide(fragment);
            }
        }
        ft.commit();
    }
    public void clear(){
        mFragmentList.clear();
    }


    public MyFragment getMyFragment(String type) {
        if (Constant.TYPE_SHOW_MARQUEE.equalsIgnoreCase(type)) {
            return DisplayMarqueeFragment.newInstance(type);
        } else if (Constant.TYPE_SHOW_IMAGE.equalsIgnoreCase(type)) {
            return DisplayImageFragment.newInstance(type);
        } else if (Constant.TYPE_SHOW_VIDEO.equalsIgnoreCase(type)) {
            return DisplayVideoFragment.newInstance(type);
        } else if (Constant.TYPE_SHOW_IMAGE_AND_VIDEO.equalsIgnoreCase(type)) {
            return DisplayVideoAndImageFragment.newInstance(type);
        } else {
            return null;
        }
    }
}
