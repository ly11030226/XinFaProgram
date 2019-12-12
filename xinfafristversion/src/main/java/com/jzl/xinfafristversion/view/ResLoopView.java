package com.jzl.xinfafristversion.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.jzl.xinfafristversion.R;
import com.jzl.xinfafristversion.XMLDataManager;
import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.bean.MyBean;
import com.jzl.xinfafristversion.bean.MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean;
import com.jzl.xinfafristversion.ui.aboutVideoList.MyLinearLayoutManager;
import com.jzl.xinfafristversion.ui.aboutVideoList.ResListAdapter;
import com.jzl.xinfafristversion.ui.aboutVideoList.ScrollHelper;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ResLoopView extends FrameLayout {
    private static final String TAG = "ResLoopView";
    private MyLinearLayoutManager mLayoutManager;
    private ResListAdapter resListAdapter;
    private RecyclerView rv;
    private Context context;
    private static boolean isUsePagerSnapHelper = true;
    private int playIndex = 0;
    public volatile static int NUMBER = 1;
    private final int PLAY_NEXT = NUMBER++;
    private ArrayList<FileBean> fileBeanArrayList;
    private MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PLAY_NEXT) {
                int posTarget = (int) msg.obj;
                rv.smoothScrollToPosition(posTarget);
            }
        }
    };

    public ResLoopView(@NonNull Context context) {
        this(context,null);
    }

    public ResLoopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ResLoopView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.item_res_loop,this,true);
        rv = findViewById(R.id.rcv_loop);
        initRecyclerView();
    }

    private void initRecyclerView() {
        mLayoutManager = new MyLinearLayoutManager(context);
        rv.setLayoutManager(mLayoutManager);
        rv.setHasFixedSize(true);
        mLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        if (isUsePagerSnapHelper) {
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(rv);
        }
    }

    public void initData(MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean) {
        MyLogger.i(TAG,"play next ... "+PLAY_NEXT);
        this.areaBean = areaBean;
        this.fileBeanArrayList = areaBean.getFiles().getFile();
        if (areaBean==null) {
            MyLogger.e(TAG,"ResLoopView initData parameter is error");
            return;
        }
        if (fileBeanArrayList == null || fileBeanArrayList.size() <= 0) {
            MyLogger.e(TAG,"ResLoopView initData parameter is error");
            return;
        }else{
            filterNoExistFile();
        }
        resListAdapter = new ResListAdapter(context, fileBeanArrayList, new PlayCompleteCallBack() {
            @Override
            public void playComplete(int pos) {
                MyLogger.i(TAG,"play complete old pos ... "+pos);
                rv.smoothScrollToPosition(pos+1);
            }
        });
        rv.setAdapter(resListAdapter);
        //自定播放帮助类
        ScrollHelper mScrolHelper = new ScrollHelper(R.id.video_player,R.id.iv_item);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int firstVisibleItem, lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                try {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                        MyLogger.i(TAG,"onScrollStateChanged");
                        if (isUsePagerSnapHelper) {
                            if (firstVisibleItem == lastVisibleItem) {
                                handler.removeMessages(PLAY_NEXT);
//                                MyLogger.i(TAG, "firstVisibleItem ... " + firstVisibleItem + "  lastVisibleItem ... " + lastVisibleItem);
                                FileBean fileBean = fileBeanArrayList.get(firstVisibleItem%fileBeanArrayList.size());
                                String format = fileBean.getFormat();
                                MyLogger.i(TAG,"position ... "+firstVisibleItem+" "+format);
                                if ("视频".equals(format)) {
                                    playVideo(mScrolHelper);
                                }else {
                                    //如果显示图片的前一个或者后一个是视频 那么就release它
                                    FileBean preFileBean = fileBeanArrayList.get((firstVisibleItem + 1)%fileBeanArrayList.size());
                                    FileBean nextFileBean = fileBeanArrayList.get((firstVisibleItem - 1)%fileBeanArrayList.size());
                                    if ("视频".equals(preFileBean.getFormat()) || "视频".equals(nextFileBean.getFormat())) {
                                        releaseVideoAndNotify();
                                        mScrolHelper.releaseVideo();
                                    }
                                    int duration = Integer.valueOf(fileBean.getTime());
                                    ifImageSendMsg(firstVisibleItem + 1, duration * 1000);
                                }
                            }
                        }else{
                            mScrolHelper.onScrollStateChanged(recyclerView,newState);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //                MyLogger.i(TAG,"onScrolled");
                if (mLayoutManager!=null) {
                    firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                    if (isUsePagerSnapHelper) {
                        mScrolHelper.onScroll(firstVisibleItem,lastVisibleItem,dx,dy);
                    }
                }
            }
        });
        //如果第一项是图片则要触发下一张图片的轮播动作
        FileBean fileBean = fileBeanArrayList.get(0);
        String format = fileBean.getFormat();
        if ("图片".equals(format)) {
            int duration = Integer.valueOf(fileBean.getTime());
            ifImageSendMsg(1,duration*1000);
        }
    }

    /**
     * 如果是图片，则定义显示多长时间换下一张
     * @param targetPos  显示下一张图片的position
     * @param duration   停留时长
     */
    private void ifImageSendMsg(int targetPos,int duration){
        Message msg = handler.obtainMessage();
        msg.what = PLAY_NEXT;
        msg.obj = targetPos;
        handler.sendMessageDelayed(msg,duration);
    }

    /**
     * 过滤不存在的file
     */
    private void filterNoExistFile() {
        if (XMLDataManager.getInstance().isDefaultData()) {
            return;
        }
        ArrayList<FileBean> temp = new ArrayList<>();
        for (int i = 0; i < fileBeanArrayList.size(); i++) {
            FileBean fileBean = fileBeanArrayList.get(i);
            String path = fileBean.getPath();
            File file = new File(path);
            if (!file.exists()) {
                temp.add(fileBean);
            }
        }
        fileBeanArrayList.removeAll(temp);
    }

    private void playVideo(ScrollHelper mScrolHelper) {
        if (mLayoutManager!=null) {
            //            int childCount = mLayoutManager.getChildCount();
            GSYBaseVideoPlayer mGSYBaseVideoPlayer = mLayoutManager.getChildAt(0).findViewById(R.id.video_player);
            int state = mGSYBaseVideoPlayer.getCurrentState();
            //            MyLogger.i(TAG, "onScrollStateChanged state ... " + state);
            int visiableState = mGSYBaseVideoPlayer.getVisibility();
            if (visiableState == View.VISIBLE) {
                mScrolHelper.handleHavePagerSnapHelper(mGSYBaseVideoPlayer);
            } else {
                releaseVideoAndNotify();
                mScrolHelper.releaseVideo();
            }
        }
    }

    private void releaseVideoAndNotify(){
        GSYVideoManager.releaseAllVideos();
        resListAdapter.notifyDataSetChanged();
    }

    public void clear(){
        handler.removeCallbacksAndMessages(null);
        GSYVideoManager.releaseAllVideos();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        MyLogger.i(TAG,"left ... "+left+" top ... "+top+" right ... "+right+" bottom ... "+bottom);
        super.onLayout(changed,left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (areaBean == null) {
            return;
        }
        int width = Integer.valueOf(areaBean.getInfo().getWidth());
        int height = Integer.valueOf(areaBean.getInfo().getHeight());
        measureChildren(
                MeasureSpec.makeMeasureSpec(width,MeasureSpec.getMode(widthMeasureSpec)),
                MeasureSpec.makeMeasureSpec(height,MeasureSpec.getMode(heightMeasureSpec)));
        setMeasuredDimension(width,height);
    }

    public MyBean.GroupsBean.GroupBean.AreasBean.AreaBean getAreaBean(){
        return areaBean;
    }

    public interface PlayCompleteCallBack{
        void playComplete(int pos);
    }

}
