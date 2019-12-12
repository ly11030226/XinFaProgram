package com.jzl.xinfafristversion.view;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;


//import android.widget.VideoView;


/**
 * Created by Administrator on 2016/8/16.
 */
public class FullScreenVideoView extends VideoView implements MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener{
    public MediaPlayer mediaPlayer = null;
    public static int height;
    public static int width;
    public Float volume = 0f;



    public FullScreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int WIDTH = getDefaultSize(width,widthMeasureSpec);
        int HEIGHT = getDefaultSize(height,heightMeasureSpec);
        setMeasuredDimension(WIDTH,HEIGHT);
    }


//    @Override
//    public void onPrepared(MediaPlayer mp) {
//        mp.setVolume(volume,volume);
//    }

    public void volume(Float volume){
        this.volume = volume;
        this.setOnPreparedListener(this);

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setVolume(volume,volume);
        this.setOnInfoListener(this);
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        Log.e("TRANSPARENT","1111");
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            this.setBackgroundColor(Color.TRANSPARENT);
            Log.e("TRANSPARENT","2222");
        }
        return true;
    }
}
