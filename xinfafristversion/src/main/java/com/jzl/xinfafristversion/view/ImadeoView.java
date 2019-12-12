package com.jzl.xinfafristversion.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.jzl.xinfafristversion.R;

import java.io.File;
import java.util.HashMap;


public class ImadeoView extends RelativeLayout {
    private Animation animation, animation1;
    //根布局
    private RelativeLayout mContainer;
    private Thread thread;
    private ImageView iv1, iv2, iv1_bg, iv2_bg;
    private FullScreenVideoView fsvv1, fsvv2;
    private RelativeLayout rl1, rl2;
    private float x, y, rx, ry, lx, ly, iv1x, iv2x;
    private float w, h, x1, y1;
    private int style = 0;
    private int TOUCH_STYLE = 0;
    //在显示的图片
    private int num = 0;
    private int nextnum;
    private Context context;
    private Boolean isrun = true;
    File[] files;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    rl2.setX(rx);

                    iv2x = rx;
                    //                    Log.e("scroll","iv2");
                    if (nextnum > files.length - 1) {
                        nextnum = 0;
                    }
                    Glide.with(context).load(Uri.fromFile(files[nextnum])).into(iv2);
                    break;
                case 2:
                    rl1.setX(rx);

                    iv1x = rx;
                    if (nextnum > files.length - 1) {
                        nextnum = 0;
                    }
                    //                    Log.e("scroll","iv1");
                    Glide.with(context).load(Uri.fromFile(files[nextnum])).into(iv1);
                    break;
                case 3:
                    //                    iv2_bg.setVisibility(View.VISIBLE);
                    //                    iv2_bg.setImageBitmap(getNetVideoBitmap(files[nextnum].getPath()));
                    //                    fsvv2.setBackgroundColor(Color.WHITE);
                    fsvv2.setBackgroundColor(Color.WHITE);
                    fsvv2.setVisibility(View.VISIBLE);

                    //                    fsvv2.setAnimation(animation);
                    //                    fsvv2.startAnimation(animation);
                    fsvv2.requestFocus();
                    if (nextnum > files.length - 1) {
                        nextnum = 0;
                    }
                    //                    Log.e("此视频播放为3：","files[nextnum].getPath()====="+files[nextnum].getPath());
                    fsvv2.setVideoPath(files[nextnum].getPath());

                    fsvv2.start();
                    break;
                case 4:
                    //                    iv1_bg.setVisibility(View.VISIBLE);
                    //                    iv1_bg.setImageBitmap(getNetVideoBitmap(files[nextnum].getPath()));
                    //                    fsvv1.setBackgroundColor(Color.WHITE);
                    fsvv1.setBackgroundColor(Color.WHITE);
                    fsvv1.setVisibility(View.VISIBLE);

                    //                    fsvv1.setAnimation(animation);
                    //                    fsvv1.startAnimation(animation);
                    //                    Log.e("此视频播放为4：","files[nextnum].getPath()====="+files[nextnum].getPath());
                    fsvv1.setVideoPath(files[nextnum].getPath());
                    fsvv1.requestFocus();
                    fsvv1.start();
                    break;
                case 5:
                    rl2.setX(x);
                    rl1.setX(rx);
                    style = 1;
                    fsvv1.setBackgroundColor(Color.WHITE);
                    fsvv1.setVisibility(View.GONE);
                    break;
                case 6:
                    rl2.setX(rx);
                    rl1.setX(x);
                    style = 0;
                    fsvv2.setBackgroundColor(Color.WHITE);
                    fsvv2.setVisibility(View.GONE);
                    break;
                case 7:
                    rl2.setX(iv2x);
                    rl1.setX(iv1x);
                    break;
            }
        }

        ;
    };

    public ImadeoView(Context context) {
        super(context);
        this.context = context;
        initView(context);
    }


    public ImadeoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(context);
    }

    public ImadeoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context);
    }

    public void setFiles(File[] files) {
        this.files = files;
        //        Log.e("scroll","文件个数："+files.length);
        //        initView(context);
        String name = files[num].getName();
        //        Log.e("scroll","文件名称："+name);

        String last = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();

        Glide.with(context).load(Uri.fromFile(files[num])).into(iv1);
        if (last.equals("mp4") || last.equals("avi") || last.equals("3gp")) {
            //            Log.e("scroll","vv1监听到视频 开始播放:+"+files[num].getPath());
            //            fsvv1.setBackgroundColor(Color.WHITE);
            fsvv1.setVisibility(View.VISIBLE);
            //            fsvv1.setAnimation(animation);
            //            fsvv1.startAnimation(animation);
            fsvv1.requestFocus();
            fsvv1.setVideoPath(files[num].getPath());

            fsvv1.start();

            //                            vv2.pause();
        }

    }


    private void initView(Context context) {
        //         animation = AnimationUtils.loadAnimation(context, R.anim.in_animation);
        //         animation1 = AnimationUtils.loadAnimation(context, R.anim.out_animation);
        //        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mContainer = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.imadeo_layout, this, true);
        //        View view = View.inflate(getContext(),R.layout.layout,this);
        iv1_bg = findViewById(R.id.iv1_bg);
        iv2_bg = findViewById(R.id.iv2_bg);
        iv1 = findViewById(R.id.iv1_1);
        iv2 = findViewById(R.id.iv1_2);
        iv1.setScaleType(ImageView.ScaleType.FIT_XY);
        iv2.setScaleType(ImageView.ScaleType.FIT_XY);
        rl1 = findViewById(R.id.rl1);
        rl2 = findViewById(R.id.rl2);
        rl1.setBackgroundColor(Color.WHITE);
        rl2.setBackgroundColor(Color.WHITE);
        fsvv1 = findViewById(R.id.fsvv1);
        fsvv2 = findViewById(R.id.fsvv2);
        //        fsvv1.setAnimation(animation);
        //        fsvv2.setAnimation(animation);
        //        fsvv1.startAnimation(animation);
        //        fsvv2.startAnimation(animation);
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        x = rl1.getX();
        rx = width;
        lx = -width;
        rl2.setX(rx);
        fsvv1.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                return true;
            }
        });

        fsvv2.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                return true;
            }
        });


    }

    public void autoScroll(final int[] times) {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    while (isrun) {
                        try {
                            Thread.sleep(times[num]);
                        } catch (InterruptedException e) {
                            break;
                        }

                        //                        iv2x = rl2.getX();
                        //                        iv1x = rl1.getX();
                        //                        fsvv1.pause();
                        //                        fsvv2.pause();
                        nextnum = num + 1;
                        if (nextnum > files.length - 1) {
                            nextnum = 0;
                        }
                        String name = files[nextnum].getName();
                        //                        Log.e("scroll","自动播放文件名称："+name);

                        String last = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();


                        if (nextnum > files.length - 1) {
                            nextnum = 0;
                        }
                        if (style == 0) {

                            mHandler.sendEmptyMessage(1);
                            //                            Glide.with(context).load(Uri.fromFile(files[nextnum])).into(iv2);
                            if (last.equals("mp4") || last.equals("avi") || last.equals("3gp")) {
                                //                                Log.e("scroll","vv1监听到视频 开始播放:+"+files[nextnum].getPath());
                                mHandler.sendEmptyMessage(3);
                                //                                fsvv2.setVisibility(View.VISIBLE);
                                //                                fsvv2.requestFocus();
                                //                                fsvv2.setVideoPath(files[nextnum].getPath());
                                //                                fsvv2.start();
                                //                            vv2.pause();
                            }
                        } else {

                            mHandler.sendEmptyMessage(2);
                            //                            Glide.with(context).load(Uri.fromFile(files[nextnum])).into(iv1);
                            if (last.equals("mp4") || last.equals("avi") || last.equals("3gp")) {
                                //                                Log.e("scroll","vv2监听到视频 开始播放:"+files[nextnum].getPath());
                                mHandler.sendEmptyMessage(4);
                                //                                fsvv1.setVisibility(View.VISIBLE);
                                //                                fsvv1.setVideoPath(files[nextnum].getPath());
                                //                                fsvv1.requestFocus();
                                //                                fsvv1.start();
                                //                            vv1.pause();
                            }
                        }
                        mHandler.sendEmptyMessage(7);


                        //                        String name1 = files[nextnum].getName();
                        //                        Log.e("scroll","文件名称："+name1);

                        //                        String last1 = name.substring(name.lastIndexOf(".") + 1, name1.length()).toLowerCase();
                        //                if(x1 - event.getX()>0){
                        //                    iv2.setX(y1 - iv2.getWidth());
                        //                }else if(x1 - event.getX()<0){
                        //                    iv2.setX(y1+iv2.getWidth());
                        //                }

                        if (style == 0) {

                            mHandler.sendEmptyMessage(5);
                            //                            fsvv1.setVisibility(View.GONE);
                            //                            if(last1.equals("mp4")||last.equals("avi")||last.equals("3gp")){
                            //                                Log.e("scroll","vv2监听到视频 开始播放:"+files[nextnum].getPath());
                            //                        vv2.setVisibility(View.VISIBLE);
                            //                        vv2.setVideoPath(files[nextnum].getPath());
                            //                        vv2.requestFocus();
                            //                        vv2.start();
                            //                            }
                        } else {

                            mHandler.sendEmptyMessage(6);
                            //                            fsvv2.setVisibility(View.GONE);
                            //                            if(last1.equals("mp4")||last.equals("avi")||last.equals("3gp")){
                            //                                Log.e("scroll","vv1监听到视频 开始播放:+"+files[nextnum].getPath());
                            //                        vv1.setVisibility(View.VISIBLE);
                            //                        vv1.requestFocus();
                            //                        vv1.setVideoPath(files[nextnum].getPath());
                            //                        vv1.start();
                            //                            }
                        }
                        num = nextnum;


                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }

            }
        });
        thread.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //       Log.e("scroll我点击了","触摸点横坐标："+event.getX()+"；触摸点纵坐标："+event.getY()+"；控件横坐标："+getLeft()
        //                +"；控件纵坐标："+getY()+"；控件宽度："+this.getWidth()+"；控件高度"+this.getHeight());

        isrun = false;

        thread.interrupt();

        //        if(event.getX()>getLeft()&&event.getX()<getRight()&&event.getY()>getTop()&&event.getY()<getBottom()){
        //            Log.e("我进入了","hehe");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //手指落点
                //                    Log.e("我进入了","ACTION_DOWN");
                x1 = event.getX();
                //iv2位置
                iv2x = rl2.getX();
                iv1x = rl1.getX();
                TOUCH_STYLE = 0;
                fsvv1.pause();
                fsvv2.pause();

                break;
            case MotionEvent.ACTION_MOVE:

                if (x1 - event.getX() > 0 && TOUCH_STYLE == 0) {
                    nextnum = num + 1;
                    if (nextnum > files.length - 1) {
                        nextnum = 0;
                    }
                    String name = files[nextnum].getName();
                    //                        Log.e("scroll","文件名称："+name);

                    String last = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();

                    //                        Log.e("scroll","左滑动 ");

                    if (nextnum > files.length - 1) {
                        nextnum = 0;
                    }
                    if (style == 0) {
                        rl2.setX(rx);
                        TOUCH_STYLE = 1;
                        iv2x = rx;
                        Glide.with(context).load(Uri.fromFile(files[nextnum])).into(iv2);
                        if (last.equals("mp4") || last.equals("avi") || last.equals("3gp")) {
                            //                                Log.e("scroll","vv1监听到视频 开始播放:+"+files[nextnum].getPath());
                            //                                fsvv2.setBackgroundColor(Color.WHITE);
                            fsvv2.setVisibility(View.VISIBLE);
                            fsvv2.requestFocus();
                            fsvv2.setVideoPath(files[nextnum].getPath());

                            fsvv2.start();
                            //                            vv2.pause();
                        }
                    } else {
                        rl1.setX(rx);
                        TOUCH_STYLE = 1;
                        iv1x = rx;
                        Glide.with(context).load(Uri.fromFile(files[nextnum])).into(iv1);
                        if (last.equals("mp4") || last.equals("avi") || last.equals("3gp")) {
                            //                                Log.e("scroll","vv2监听到视频 开始播放:"+files[nextnum].getPath());
                            //                                fsvv1.setBackgroundColor(Color.WHITE);
                            fsvv1.setVisibility(View.VISIBLE);
                            fsvv1.setVideoPath(files[nextnum].getPath());
                            fsvv1.requestFocus();

                            fsvv1.start();
                            //                            vv1.pause();
                        }
                    }


                } else if (x1 - event.getX() < 0 && TOUCH_STYLE == 0) {
                    //                        Log.e("scroll","右滑动 ");
                    nextnum = num - 1;

                    if (nextnum < 0) {
                        nextnum = files.length - 1;
                    }
                    String name = files[nextnum].getName();
                    //                        Log.e("scroll","文件名称："+name);

                    String last = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
                    if (nextnum < 0) {
                        nextnum = files.length - 1;
                    }

                    if (style == 0) {
                        rl2.setX(lx);
                        TOUCH_STYLE = 1;
                        iv2x = lx;
                        Glide.with(context).load(Uri.fromFile(files[nextnum])).into(iv2);
                        if (last.equals("mp4") || last.equals("avi") || last.equals("3gp")) {
                            //                                Log.e("scroll","vv1监听到视频 开始播放:+"+files[nextnum].getPath());
                            //                                fsvv2.setBackgroundColor(Color.WHITE);
                            fsvv2.setVisibility(View.VISIBLE);
                            fsvv2.requestFocus();
                            fsvv2.setVideoPath(files[nextnum].getPath());

                            fsvv2.start();
                        }
                    } else {
                        rl1.setX(lx);
                        TOUCH_STYLE = 1;
                        iv1x = lx;
                        Glide.with(context).load(Uri.fromFile(files[nextnum])).into(iv1);
                        if (last.equals("mp4") || last.equals("avi") || last.equals("3gp")) {
                            //                                Log.e("scroll","vv2监听到视频 开始播放:"+files[nextnum].getPath());
                            //                                fsvv1.setBackgroundColor(Color.WHITE);
                            fsvv1.setVisibility(View.VISIBLE);
                            fsvv1.setVideoPath(files[nextnum].getPath());
                            fsvv1.requestFocus();

                            fsvv1.start();
                        }

                    }
                } else {
                    TOUCH_STYLE = 1;
                    fsvv1.resume();
                    fsvv2.resume();
                }
                rl2.setX(iv2x - (x1 - event.getX()));
                rl1.setX(iv1x - (x1 - event.getX()));
                break;
            case MotionEvent.ACTION_UP:
                String name = files[nextnum].getName();
                //                    Log.e("scroll","文件名称："+name);

                String last = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
                //                if(x1 - event.getX()>0){
                //                    iv2.setX(y1 - iv2.getWidth());
                //                }else if(x1 - event.getX()<0){
                //                    iv2.setX(y1+iv2.getWidth());
                //                }
                if (style == 0) {
                    rl2.setX(x);
                    rl1.setX(rx);
                    style = 1;
                    fsvv1.setVisibility(View.GONE);
                    if (last.equals("mp4") || last.equals("avi") || last.equals("3gp")) {
                        //                            Log.e("scroll","vv2监听到视频 开始播放:"+files[nextnum].getPath());
                        //                        vv2.setVisibility(View.VISIBLE);
                        //                        vv2.setVideoPath(files[nextnum].getPath());
                        //                        vv2.requestFocus();
                        //                        vv2.start();
                    }
                } else {
                    rl2.setX(rx);
                    rl1.setX(x);
                    style = 0;
                    fsvv2.setVisibility(View.GONE);
                    if (last.equals("mp4") || last.equals("avi") || last.equals("3gp")) {
                        //                            Log.e("scroll","vv1监听到视频 开始播放:+"+files[nextnum].getPath());
                        //                        vv1.setVisibility(View.VISIBLE);
                        //                        vv1.requestFocus();
                        //                        vv1.setVideoPath(files[nextnum].getPath());
                        //                        vv1.start();
                    }
                }
                num = nextnum;
                break;
        }

        //        }
        isrun = true;
        return true;
    }

    public void move() {

    }

    /**
     * @param volume 音量大小
     */
    public void setVolume(float volume) {

        fsvv1.volume(volume);
        fsvv2.volume(volume);
    }


    public Bitmap getNetVideoBitmap(String videoUrl) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据url获取缩略图
            retriever.setDataSource(videoUrl, new HashMap());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }


}
