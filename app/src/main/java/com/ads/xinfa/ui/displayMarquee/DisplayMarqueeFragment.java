package com.ads.xinfa.ui.displayMarquee;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ads.xinfa.R;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.MyFragment;
import com.ads.xinfa.bean.MyBean;
import com.ads.xinfa.bean.MyBean.GroupsBean.GroupBean.AreasBean.AreaBean;
import com.ads.xinfa.bean.MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean;
import com.ads.xinfa.bean.MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.InfoBean;
import com.ads.xinfa.utils.ColorTool;
import com.ads.xinfa.view.AutoText;
import com.ads.xinfa.view.MarqueeView;
import com.ads.xinfa.view.MyMarqueeView;
import com.ads.xinfa.view.TextSwitchView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DisplayMarqueeFragment extends MyFragment {
    private static final String TAG = "DisplayMarqueeFragment";
    private static final String ARG_PARAM1 = "param1";
    View view;
    Unbinder unbinder;
    @BindView(R.id.rl)
    RelativeLayout rl;
    @BindView(R.id.marqueeVie_my)
    MyMarqueeView marqueeView;

    private AreaBean areaBean;
    private InfoBean infoBean;
    private String[] times,texts;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment DisplayImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayMarqueeFragment newInstance(String param1) {
        DisplayMarqueeFragment fragment = new DisplayMarqueeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.fragment_marquee,container,false);
            unbinder = ButterKnife.bind(this,view);
            if (infoBean!=null) {
                String type = infoBean.getType1();
                String message = texts[0];
                marqueeView.setTextColor(Color.parseColor(new ColorTool().colorChange(infoBean.getFcolor())));
                marqueeView.setTextSize(Integer.valueOf(Integer.valueOf(infoBean.getFsize())));
                marqueeView.setBackgroundColor(Color.parseColor("#"+infoBean.getBgcolor()));
                marqueeView.setAnimDuration(1000);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) marqueeView.getLayoutParams();
                params.height = Integer.valueOf(infoBean.getHeight());
                if (Constant.MARQUEE_LEFT.equalsIgnoreCase(type)) {
                    marqueeView.startWithText(message,R.anim.anim_left_out,R.anim.anim_right_in);
                }else if (Constant.MARQUEE_RIGHT.equalsIgnoreCase(type)) {
                    marqueeView.startWithText(message,R.anim.anim_right_in,R.anim.anim_left_out);
                }else{
                    marqueeView.startWithText(message,R.anim.anim_bottom_in,R.anim.anim_bottom_out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void displayViewFromLeft(){
        try {
            int t = Integer.valueOf(areaBean.getFiles().getFile().get(0).getTime());
            AutoText autoText = new AutoText(getActivity(),t);
            LinearLayout linearLayout=new LinearLayout(getActivity());
            if(texts.length!=0) {
                autoText.setText(texts[0]);
            }else {
                autoText.setText("");
            }
            autoText.setTextSize(Float.valueOf(infoBean.getFsize()));
            autoText.setTextColor(Color.parseColor(new ColorTool().colorChange(infoBean.getFcolor())));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Integer.valueOf(infoBean.getWidth()),
                    Integer.valueOf(infoBean.getHeight()));
            autoText.setLayoutParams(params);
            autoText.setBackgroundColor(Color.parseColor("#"+infoBean.getBgcolor()));
            ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(autoText.getLayoutParams());
            margin.leftMargin = Integer.valueOf(infoBean.getLeft());
            margin.topMargin = Integer.valueOf(infoBean.getTop());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
            layoutParams.height = Integer.valueOf(infoBean.getHeight());//设置图片的高度
            layoutParams.width = Integer.valueOf(infoBean.getWidth());//设置图片的宽度
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.addView(autoText);
            autoText.startScroll();
            rl.addView(linearLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayViewFromRight(){
        try {
            MarqueeView marqueeView = new MarqueeView(getActivity());
            LinearLayout linearLayout=new LinearLayout(getActivity());
            if(texts.length!=0) {
                marqueeView.setContent(texts[0]);
            }else {
                marqueeView.setContent("");
            }
            marqueeView.setRepetType(1);
            marqueeView.setTextSize(Integer.valueOf(infoBean.getFsize()));
            String time = areaBean.getFiles().getFile().get(0).getTime();
            marqueeView.setTextSpeed(Float.parseFloat(time));
            marqueeView.setTextColor(Color.parseColor(new ColorTool().colorChange(infoBean.getFcolor())));
            marqueeView.setBackgroundColor(Color.parseColor("#"+infoBean.getBgcolor()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Integer.valueOf(infoBean.getWidth()),
                    Integer.valueOf(infoBean.getHeight()));
            marqueeView.setLayoutParams(params);
            ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(marqueeView.getLayoutParams());
            margin.leftMargin = Integer.valueOf(infoBean.getLeft());
            margin.topMargin = Integer.valueOf(infoBean.getTop());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
            layoutParams.height = Integer.valueOf(infoBean.getHeight());//设置图片的高度
            layoutParams.width = Integer.valueOf(infoBean.getWidth());//设置图片的宽度
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.addView(marqueeView);
            rl.addView(linearLayout);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void displayViewFromTop(){
        try {
            TextSwitchView textSwitchView = new TextSwitchView(
                    getActivity(),
                    Float.parseFloat(infoBean.getFsize()),
                    Color.parseColor(new ColorTool().colorChange(
                            infoBean.getFcolor())),infoBean.getType1());
            LinearLayout linearLayout=new LinearLayout(getActivity());
            textSwitchView.setBackgroundColor(Color.parseColor("#"+infoBean.getBgcolor()));
            textSwitchView.setResources(texts);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Integer.valueOf(infoBean.getWidth()),
                    Integer.valueOf(infoBean.getHeight()));
            textSwitchView.setLayoutParams(params);
            ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(textSwitchView.getLayoutParams());
            margin.leftMargin = Integer.valueOf(infoBean.getLeft());
            margin.topMargin = Integer.valueOf(infoBean.getTop());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
            layoutParams.height = Integer.valueOf(infoBean.getHeight());//设置图片的高度
            layoutParams.width = Integer.valueOf(infoBean.getWidth());//设置图片的宽度
            textSwitchView.setTextStillTime(times);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.addView(textSwitchView);
            rl.addView(linearLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void doInit(MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean) {
        this.areaBean = areaBean;
        final int size = areaBean.getFiles().getFile().size();
        this.times = new String[size];
        this.texts = new String[size];
        for (int i = 0; i < size; i++) {
            FileBean fileBean = areaBean.getFiles().getFile().get(i);
            times[i] = fileBean.getTime();
            texts[i] = fileBean.getPath();
        }
        this.infoBean = areaBean.getInfo();
    }

    @Override
    public void display() {

    }

    @Override
    public void onStart() {
        super.onStart();
        marqueeView.startFlipping();
    }

    @Override
    public void onStop() {
        super.onStop();
        marqueeView.stopFlipping();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null) {
            unbinder.unbind();
        }
    }
}
