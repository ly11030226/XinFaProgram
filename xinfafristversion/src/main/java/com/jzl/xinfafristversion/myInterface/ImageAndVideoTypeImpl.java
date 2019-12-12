package com.jzl.xinfafristversion.myInterface;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.jzl.xinfafristversion.base.FileManager;
import com.jzl.xinfafristversion.bean.MyBean;
import com.jzl.xinfafristversion.view.ImadeoView;

import java.io.File;

public class ImageAndVideoTypeImpl extends IDisplayType {
    public ImageAndVideoTypeImpl(int index,Context context) {
        super(index,context);
    }

    @Override
    public ViewGroup display(MyBean.GroupsBean.GroupBean.AreasBean areas) {
        final int mIndex = index;
        final int fileSize = areas.getArea().get(mIndex).getFiles().getFile().size();
        File[] files = new File[fileSize];
        int[] time = new int[fileSize];
        for (int i = 0; i < files.length; i++) {
            String path = areas.getArea().get(mIndex).getFiles().getFile().get(i).getPath();
            String fileName = path.substring(path.lastIndexOf("/"));
            File file = new File(FileManager.Resource_DIR + fileName);
            files[i] = file;
            String s = areas.getArea().get(mIndex).getFiles().getFile().get(i).getTime();
            time[i] = Integer.parseInt(s) * 1000;
        }
        MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.InfoBean infoBean
                = areas.getArea().get(mIndex).getInfo();
        ViewGroup vg;
        vg = createView(infoBean.getLeft(), infoBean.getTop(), infoBean.getWidth(), infoBean.getHeight(), files, infoBean.getVoice1(), time);
        return vg;
    }

    private ViewGroup createView(String left, String top, String width, String height,  File[] files,String volume,int[] times){
        ImadeoView imadeoView = new ImadeoView(context);
        LinearLayout linearLayout = new LinearLayout(context);
        imadeoView.setFiles(files);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Integer.valueOf(width), Integer.valueOf(height));
        imadeoView.setLayoutParams(params);
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(imadeoView.getLayoutParams());
        margin.leftMargin = Integer.valueOf(left);
        margin.topMargin = Integer.valueOf(top);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        layoutParams.height = Integer.valueOf(height);//设置图片的高度
        layoutParams.width = Integer.valueOf(width);//设置图片的宽度
        linearLayout.setLayoutParams(layoutParams);
        imadeoView.autoScroll(times);
        linearLayout.addView(imadeoView);
        imadeoView.setVolume(Float.parseFloat(volume));
        return linearLayout;

    }
}
