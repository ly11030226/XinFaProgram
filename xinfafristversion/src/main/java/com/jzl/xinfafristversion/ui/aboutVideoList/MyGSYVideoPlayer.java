package com.jzl.xinfafristversion.ui.aboutVideoList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jzl.xinfafristversion.R;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

public class MyGSYVideoPlayer extends StandardGSYVideoPlayer {

    ImageView mCoverImage;

    String mCoverOriginUrl;

    int mDefaultRes;

    public MyGSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public MyGSYVideoPlayer(Context context) {
        super(context);
    }

    public MyGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public int getLayoutId() {
        return R.layout.view_display_video;
    }

    public void startPlay(Context context) {
        CommonUtil.hideNavKey(context);
        //默认开始播放
        clickStartIcon();
    }

    @Override
    public void onSurfaceUpdated(Surface surface) {
        super.onSurfaceUpdated(surface);
        if (mThumbImageViewLayout != null && mThumbImageViewLayout.getVisibility() == VISIBLE) {
            mThumbImageViewLayout.setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void setViewShowState(View view, int visibility) {
        if (view == mThumbImageViewLayout && visibility != VISIBLE) {
            return;
        }
        super.setViewShowState(view, visibility);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mCoverImage = findViewById(R.id.thumbImage);

        if (mThumbImageViewLayout != null && (mCurrentState == -1 || mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR)) {
            mThumbImageViewLayout.setVisibility(VISIBLE);
        }

    }
    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        MyGSYVideoPlayer sampleCoverVideo = (MyGSYVideoPlayer) gsyBaseVideoPlayer;
        sampleCoverVideo.loadCoverImage(mCoverOriginUrl, mDefaultRes);
        return gsyBaseVideoPlayer;
    }
    public void loadCoverImage(String url, int res) {
        mCoverOriginUrl = url;
        mDefaultRes = res;
        Glide.with(getContext().getApplicationContext())
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(1000000)
                                .centerCrop()
                                .error(res)
                                .placeholder(res))
                .load(url)
                .into(mCoverImage);
    }
    @Override
    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {
        super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
        //不给触摸快进，如果需要，屏蔽下方代码即可
        mChangePosition = false;
        //不给触摸音量，如果需要，屏蔽下方代码即可
        mChangeVolume = false;
        //不给触摸亮度，如果需要，屏蔽下方代码即可
        mBrightness = false;
    }

    @Override
    protected void touchDoubleUp() {
        //super.touchDoubleUp();
        //不需要双击暂停
    }
}