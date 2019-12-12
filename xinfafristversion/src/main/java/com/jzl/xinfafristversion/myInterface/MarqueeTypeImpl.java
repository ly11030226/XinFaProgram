package com.jzl.xinfafristversion.myInterface;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.jzl.xinfafristversion.base.Constant;
import com.jzl.xinfafristversion.bean.MyBean;
import com.jzl.xinfafristversion.utils.ColorTool;
import com.jzl.xinfafristversion.view.AutoText;
import com.jzl.xinfafristversion.view.MarqueeView;
import com.jzl.xinfafristversion.view.TextSwitchView;


public class MarqueeTypeImpl extends IDisplayType {


    public MarqueeTypeImpl(int index,Context context) {
        super(index,context);
    }

    @Override
    public ViewGroup display(MyBean.GroupsBean.GroupBean.AreasBean areas) {
        final int mIndex = index;
        final int length = areas.getArea().get(mIndex).getFiles().getFile().size();
        String[] time = new String[length];
        String[] textString = new String[length];

        for (int b = 0; b < length; b++) {
            textString[b] = areas.getArea().get(mIndex).getFiles().getFile().get(b).getPath();
            time[b] = areas.getArea().get(mIndex).getFiles().getFile().get(b).getTime();
        }
        final String type1 = areas.getArea().get(mIndex).getInfo().getType1();
        MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.InfoBean infoBean = areas.getArea().get(mIndex).getInfo();
        ViewGroup vg;
        if (Constant.MARQUEE_RIGHT.equalsIgnoreCase(type1)) {
            vg = createTextViewRight(infoBean.getLeft(), infoBean.getTop(), infoBean.getWidth(), infoBean.getHeight(), textString, infoBean.getFcolor(), infoBean.getFsize(), time[0], infoBean.getBgcolor());
        } else if (Constant.MARQUEE_LEFT.equalsIgnoreCase(type1)) {
            vg = createMarqueeFromLeft(infoBean.getLeft(), infoBean.getTop(), infoBean.getWidth(), infoBean.getHeight(), textString, infoBean.getFcolor(), infoBean.getFsize(), time[0], infoBean.getBgcolor());
        } else {
            vg = createMarqueeFromUp(infoBean.getLeft(), infoBean.getTop(), infoBean.getWidth(), infoBean.getHeight(), textString, infoBean.getFcolor(), infoBean.getType1(), time, infoBean.getFsize(), infoBean.getBgcolor());
        }
        return vg;
    }

    private ViewGroup createTextViewRight(String left, String top, String width, String height, String[] stringList, String textcolor, String textsize, String textSpeed, String bgcolor) {
        MarqueeView marqueeView = new MarqueeView(context);
        LinearLayout linearLayout = new LinearLayout(context);
        if (stringList.length != 0) {
            marqueeView.setContent(stringList[0]);
        } else {
            marqueeView.setContent("");
        }

        marqueeView.setRepetType(1);
        marqueeView.setTextSize(Integer.valueOf(textsize));
        marqueeView.setTextSpeed(Float.parseFloat(textSpeed));

        try {
            marqueeView.setTextColor(Color.parseColor(new ColorTool().colorChange(textcolor)));
        } catch (Exception e) {

            e.printStackTrace();
        }
        marqueeView.setBackgroundColor(Color.parseColor("#" + bgcolor));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Integer.valueOf(width), Integer.valueOf(height));
        marqueeView.setLayoutParams(params);
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(marqueeView.getLayoutParams());
        margin.leftMargin = Integer.valueOf(left);
        margin.topMargin = Integer.valueOf(top);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        layoutParams.height = Integer.valueOf(height);//设置图片的高度
        layoutParams.width = Integer.valueOf(width);//设置图片的宽度
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.addView(marqueeView);
        return linearLayout;
    }

    private ViewGroup createMarqueeFromLeft(String left, String top, String width, String height, String[] stringList, String textcolor, String textsize, String textSpeed, String bgcolor) {
        AutoText autoText = new AutoText(context,Integer.parseInt(textSpeed));
        LinearLayout linearLayout=new LinearLayout(context);
        if(stringList.length!=0) {
            autoText.setText(stringList[0]);
        }else {
            autoText.setText("");
        }
        //            Log.e("textsize",textsize);
        autoText.setTextSize(Float.valueOf(textsize));
        //        autoText.setTextSpeed(Integer.parseInt(textSpeed));

        try {
            autoText.setTextColor(Color.parseColor(new ColorTool().colorChange(textcolor)));
        }catch (Exception e){

            e.printStackTrace();
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Integer.valueOf(width),
                Integer.valueOf(height));
        autoText.setLayoutParams(params);
        autoText.setBackgroundColor(Color.parseColor("#"+bgcolor));
        //        imageView.setBackgroundColor(colorAccent);
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(autoText.getLayoutParams());
        margin.leftMargin = Integer.valueOf(left);
        margin.topMargin = Integer.valueOf(top);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        layoutParams.height = Integer.valueOf(height);//设置图片的高度
        layoutParams.width = Integer.valueOf(width);//设置图片的宽度

        linearLayout.setLayoutParams(layoutParams);
        linearLayout.addView(autoText);
        autoText.startScroll();
        return linearLayout;
    }


    private ViewGroup createMarqueeFromUp(String left, String top, String width, String height, String[] stringList, String textcolor, String direction, String[] time, String textsize, String bgcolor) {
        TextSwitchView textSwitchView = new TextSwitchView(context,Float.parseFloat(textsize),Color.parseColor(new ColorTool().colorChange(textcolor)),direction);
        LinearLayout linearLayout=new LinearLayout(context);

        textSwitchView.setBackgroundColor(Color.parseColor("#"+bgcolor));
        textSwitchView.setResources(stringList);


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Integer.valueOf(width),
                Integer.valueOf(height));
        textSwitchView.setLayoutParams(params);
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(textSwitchView.getLayoutParams());
        margin.leftMargin = Integer.valueOf(left);
        margin.topMargin = Integer.valueOf(top);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        layoutParams.height = Integer.valueOf(height);//设置图片的高度
        layoutParams.width = Integer.valueOf(width);//设置图片的宽度

        textSwitchView.setTextStillTime(time);

        linearLayout.setLayoutParams(layoutParams);
        linearLayout.addView(textSwitchView);
        return linearLayout;
    }

}
