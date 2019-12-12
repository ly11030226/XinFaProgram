package com.ads.xinfa.myInterface;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ads.xinfa.base.FileManager;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.bean.MyBean;
import com.bumptech.glide.Glide;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;

public class ImageTypeImpl extends IDisplayType implements OnBannerListener {
    private static final String TAG = "ImageTypeImpl";
    public ImageTypeImpl(int index,Context context) {
        super(index,context);
    }

    @Override
    public ViewGroup display(MyBean.GroupsBean.GroupBean.AreasBean areas) {
        //File[] files =new File[areas.getArea().get(a).getFiles().getFile().size()];
        ArrayList<String> files = new ArrayList<String>();
        final int mIndex = index;
        final int length = areas.getArea().get(mIndex).getFiles().getFile().size();
        int[] time = new int[length];

        for (int i = 0; i < length; i++) {
            ArrayList<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean> fileBeanArrayList =
                    areas.getArea().get(mIndex).getFiles().getFile();
            String path = areas.getArea().get(mIndex).getFiles().getFile().get(i).getPath();
            String fileName = fileBeanArrayList.get(i).getPath().substring(path.lastIndexOf("/"));
            //File file =new File(Resource_DIR+fileName);
            MyLogger.i(TAG,"path ... " + FileManager.Resource_DIR + fileName);
            files.add(FileManager.Resource_DIR + fileName);
            String s = areas.getArea().get(mIndex).getFiles().getFile().get(i).getTime();
            time[i] = Integer.parseInt(s) * 1000;
        }
        MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.InfoBean infoBean
                = areas.getArea().get(mIndex).getInfo();
        ViewGroup vg = craeteView(infoBean.getLeft(), infoBean.getTop(), infoBean.getWidth(), infoBean.getHeight(), files, time);
        return vg;
    }

    private ViewGroup craeteView(String left, String top, String width, String height, ArrayList<String> files,int[] times){
        Banner banner =new Banner(context);
        LinearLayout linearLayout=new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Integer.valueOf(width),
                Integer.valueOf(height));
        banner.setLayoutParams(params);
        //设置内置样式，共有六种可以点入方法内逐一体验使用。
        banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
        //设置图片加载器，图片加载器在下方
        banner.setImageLoader(new MyLoader());
        //设置图片网址或地址的集合
        banner.setImages(files);
        //设置轮播的动画效果，内含多种特效，可点入方法内查找后内逐一体验
        banner.setBannerAnimation(Transformer.Default);
        //设置轮播图的标题集合
        //        banner.setBannerTitles(list2);
        //设置轮播间隔时间

        banner.setDelayTime(times[0]);
        //设置是否为自动轮播，默认是“是”。
        banner.isAutoPlay(true);
        //设置指示器的位置，小点点，左中右。
        banner
                //               .setIndicatorGravity(BannerConfig.CENTER)
                //以上内容都可写成链式布局，这是轮播图的监听。比较重要。方法在下面。
                .setOnBannerListener(this)
                //必须最后调用的方法，启动轮播图。
                .start();
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(banner.getLayoutParams());
        margin.leftMargin = Integer.valueOf(left);
        margin.topMargin = Integer.valueOf(top);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        layoutParams.height = Integer.valueOf(height);//设置图片的高度
        layoutParams.width = Integer.valueOf(width);//设置图片的宽度
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.addView(banner);
        return linearLayout;
    }

    //轮播图的监听方法
    @Override
    public void OnBannerClick(int position) {
        MyLogger.i(TAG, "你点了第"+position+"张轮播图");
    }
    //自定义的图片加载器
    private class MyLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(context).load((String) path).into(imageView);
        }
    }
}
