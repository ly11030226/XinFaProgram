package com.jzl.xinfafristversion.ui.aboutVideoList;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.utils.BaseUtils;
import com.shuyu.gsyvideoplayer.utils.NetworkUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 计算滑动，自动播放的帮助类
 */

public class ScrollCalculatorHelper {

    private static final String TAG = "ScrollCalculatorHelper" ;
    private int firstVisible = 0;
    private int lastVisible = 0;
    private int visibleCount = 0;
    private int playId;
    private int imageId;
    private int rangeTop;
    private int rangeBottom;
    private PlayRunnable runnable;


    private Handler playHandler = new Handler();

    public ScrollCalculatorHelper(int imageId,int playId, int rangeTop, int rangeBottom) {
        this.imageId = imageId;
        this.playId = playId;
        this.rangeTop = rangeTop;
        this.rangeBottom = rangeBottom;
    }

    public void onScrollStateChanged(RecyclerView view, int scrollState) {
        switch (scrollState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                playVideo(view);
                break;
        }
    }

    public void onScroll(RecyclerView view, int firstVisibleItem, int lastVisibleItem, int visibleItemCount) {
        if (firstVisible == firstVisibleItem) {
            return;
        }
        firstVisible = firstVisibleItem;
        lastVisible = lastVisibleItem;
        visibleCount = visibleItemCount;
//        MyLogger.i(TAG,
//                "onScroll firstVisible ... "+firstVisible+
//                        " lastVisible ... "+lastVisible+
//                        " visibleCount ... "+visibleCount);
    }


    void playVideo(RecyclerView view) {

        if (view == null) {
            return;
        }

        RecyclerView.LayoutManager layoutManager = view.getLayoutManager();

        checkIsNeedPlay(layoutManager);

//        GSYBaseVideoPlayer gsyBaseVideoPlayer = null;
//
//        boolean needPlay = false;
//
//        for (int i = 0; i < visibleCount; i++) {
//            if (layoutManager.getChildAt(i) != null && layoutManager.getChildAt(i).findViewById(playId) != null) {
//                GSYBaseVideoPlayer player = (GSYBaseVideoPlayer) layoutManager.getChildAt(i).findViewById(playId);
//                Rect rect = new Rect();
//                player.getLocalVisibleRect(rect);
//                int height = player.getHeight();
//                //说明第一个完全可视
//                if (rect.top == 0 && rect.bottom == height) {
//                    gsyBaseVideoPlayer = player;
//                    if ((player.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL || player.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_ERROR)) {
//                        needPlay = true;
//                    }
//                    break;
//                }
//
//            }
//        }
//
//        if (gsyBaseVideoPlayer != null && needPlay) {
//            sendPlayMessage(gsyBaseVideoPlayer);
//        }
    }

    /**
     * 查看player是否可以播放
     * @param layoutManager
     */
    private void checkIsNeedPlay(RecyclerView.LayoutManager layoutManager){
        GSYBaseVideoPlayer gsyBaseVideoPlayer = null;
        boolean needPlay = false;

        if (layoutManager==null) {
            MyLogger.e(TAG,"checkIsNeedPlay layoutManager is null");
            return;
        }
        MyLogger.i(TAG,"first position ... "+firstVisible+" last position ... "+lastVisible);

        //由于设置全屏状态播放，layoutManager 最多有两个childCount
        if (layoutManager.getChildCount() == 2) {
            //左边是否是视频
            boolean leftIsVideo = false;
            boolean rightIsVideo = false;
            GSYBaseVideoPlayer leftPlayer = (GSYBaseVideoPlayer)layoutManager.getChildAt(0).findViewById(playId);
            GSYBaseVideoPlayer rightPlayer = (GSYBaseVideoPlayer)layoutManager.getChildAt(1).findViewById(playId);
            ImageView leftImage = layoutManager.getChildAt(0).findViewById(imageId);
            ImageView rightImage = layoutManager.getChildAt(1).findViewById(imageId);
            int screenWidth = BaseUtils.getScreenDisplayMetrics(leftPlayer.getContext()).widthPixels;

            if (leftPlayer.getVisibility() == View.VISIBLE) {
                leftIsVideo = true;
            }else if(leftPlayer.getVisibility() == View.GONE){
                leftIsVideo = false;
            }
            if (rightPlayer.getVisibility() == View.VISIBLE) {
                rightIsVideo = true;
            }else if(rightPlayer.getVisibility() == View.GONE){
                rightIsVideo = false;
            }
            Rect leftRect = new Rect();
            leftPlayer.getLocalVisibleRect(leftRect);
            Rect rightRect = new Rect();
            rightPlayer.getLocalVisibleRect(rightRect);
            MyLogger.i(TAG, "leftRect.left ... " + leftRect.left + " leftRect.right ... " + leftRect.right);
            MyLogger.i(TAG, "rightRect.left ... " + rightRect.left + " rightRect.right ... " + rightRect.right);
            //初始状态 特殊
            if (firstVisible == 0 && lastVisible == 0) {
                //如果右边的是视频也就是 ChildAt（1）是视频
                if (rightIsVideo) {
                    if (leftIsVideo) {
                        if (leftRect.left > screenWidth/2) {
                            MyLogger.i(TAG,"play right");
                            //播放右边的
                            gsyBaseVideoPlayer = rightPlayer;
                            if ((rightPlayer.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL || rightPlayer.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_ERROR)) {
                                needPlay = true;
                            }
                        }else{
                            MyLogger.i(TAG,"play left");
                            //播放左边的
                            gsyBaseVideoPlayer = leftPlayer;
                            if ((leftPlayer.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL || leftPlayer.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_ERROR)) {
                                needPlay = true;
                            }
                        }
                    }//初始的时候左边是图片
                    else{
                        Rect imageRect = new Rect();
                        leftImage.getLocalVisibleRect(imageRect);
                        MyLogger.i(TAG,"left ... "+imageRect.left+" right ... "+imageRect.right);
                        //播放右边
                        if (imageRect.right>screenWidth/2) {

                        }else{
                            //应该暂停播放
                        }
                    }

                } //右边的是图片
                else{
                    //todo 暂不用管
                }

            }//当滑过一屏再回来则是正常状态
            else{
                if (leftIsVideo && rightIsVideo) {

                }


            }
            if (gsyBaseVideoPlayer != null && needPlay) {
                sendPlayMessage(gsyBaseVideoPlayer);
            }
        }else if (layoutManager.getChildCount() == 1) {
            //todo 暂不用管
        }


    }

    private void sendPlayMessage(GSYBaseVideoPlayer gsyBaseVideoPlayer) {
        if (runnable != null) {
            GSYBaseVideoPlayer tmpPlayer = runnable.gsyBaseVideoPlayer;
            playHandler.removeCallbacks(runnable);
            runnable = null;
            if (tmpPlayer == gsyBaseVideoPlayer) {
                return;
            }
        }
        runnable = new PlayRunnable(gsyBaseVideoPlayer);
        //降低频率
        playHandler.postDelayed(runnable, 400);
    }


    private class PlayRunnable implements Runnable {

        GSYBaseVideoPlayer gsyBaseVideoPlayer;

        public PlayRunnable(GSYBaseVideoPlayer gsyBaseVideoPlayer) {
            this.gsyBaseVideoPlayer = gsyBaseVideoPlayer;
        }

        @Override
        public void run() {
            boolean inPosition = false;
            //如果未播放，需要播放
            if (gsyBaseVideoPlayer != null) {
                int[] screenPosition = new int[2];
                gsyBaseVideoPlayer.getLocationOnScreen(screenPosition);
                int halfHeight = gsyBaseVideoPlayer.getHeight() / 2;

                int rangePosition = screenPosition[1] + halfHeight;
                //中心点在播放区域内
                if (rangePosition >= rangeTop && rangePosition <= rangeBottom) {
                    inPosition = true;
                }
                if (inPosition) {
                    startPlayLogic(gsyBaseVideoPlayer, gsyBaseVideoPlayer.getContext());
                    //gsyBaseVideoPlayer.startPlayLogic();
                }
            }
        }
    }


    /***************************************自动播放的点击播放确认******************************************/
    private void startPlayLogic(GSYBaseVideoPlayer gsyBaseVideoPlayer, Context context) {
        if (!com.shuyu.gsyvideoplayer.utils.CommonUtil.isWifiConnected(context)) {
            //这里判断是否wifi
            showWifiDialog(gsyBaseVideoPlayer, context);
            return;
        }
        gsyBaseVideoPlayer.startPlayLogic();
    }

    private void showWifiDialog(final GSYBaseVideoPlayer gsyBaseVideoPlayer, Context context) {
        if (!NetworkUtils.isAvailable(context)) {
            Toast.makeText(context, context.getResources().getString(com.shuyu.gsyvideoplayer.R.string.no_net), Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi));
        builder.setPositiveButton(context.getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                gsyBaseVideoPlayer.startPlayLogic();
            }
        });
        builder.setNegativeButton(context.getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}

