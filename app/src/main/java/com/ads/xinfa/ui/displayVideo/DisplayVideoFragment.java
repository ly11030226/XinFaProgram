package com.ads.xinfa.ui.displayVideo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ads.xinfa.R;
import com.ads.xinfa.base.MyFragment;
import com.ads.xinfa.bean.MyBean;
import com.ads.xinfa.entity.ImageAndVideoEntity;
import com.ads.xinfa.ui.aboutVideoList.RecyclerItemNormalHolder;
import com.ads.xinfa.ui.aboutVideoList.RecyclerNormalAdapter;
import com.ads.xinfa.ui.aboutVideoList.ScrollCalculatorHelper;
import com.ads.xinfa.ui.displayVideoAndImage.DisplayVideoAndImageFragment;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DisplayVideoFragment extends MyFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private Unbinder unbinder;
    private View view;
    @BindView(R.id.rcv)
    RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerNormalAdapter mRecyclerNormalAdapter;
    private List<ImageAndVideoEntity.FileEntity> mVideoList = new ArrayList<>();
    private boolean mFull = false;
    private ScrollCalculatorHelper mScrollCalculatorHelper;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment DisplayVideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayVideoFragment newInstance(String param1) {
        DisplayVideoFragment fragment = new DisplayVideoFragment();
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
        view = inflater.inflate(R.layout.fragment_display_video, container, false);
        unbinder = ButterKnife.bind(this,view);
        try {
            resolveData();
            initRecycleView();
            initData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void initData() {
        mRecyclerNormalAdapter = new RecyclerNormalAdapter(getActivity(), mVideoList, new DisplayVideoAndImageFragment.PlayCompleteCallBack() {
            @Override
            public void playComplete(int pos) {

            }
        });
        mRecyclerView.setAdapter(mRecyclerNormalAdapter);

        //限定范围为屏幕一半的上下偏移180
        int playTop = CommonUtil.getScreenHeight(getActivity()) / 2 - CommonUtil.dip2px(getActivity(), 180);
        int playBottom = CommonUtil.getScreenHeight(getActivity()) / 2 + CommonUtil.dip2px(getActivity(), 180);
        //自定播放帮助类
        mScrollCalculatorHelper = new ScrollCalculatorHelper(R.id.iv_item,R.id.video_player, playTop, playBottom);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int firstVisibleItem, lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                //大于0说明有播放
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    //当前播放的位置
                    int position = GSYVideoManager.instance().getPlayPosition();
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().getPlayTag().equals(RecyclerItemNormalHolder.TAG)
                            && (position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        if(!GSYVideoManager.isFullState(getActivity())) {
                            GSYVideoManager.releaseAllVideos();
//                            mRecyclerNormalAdapter.notifyDataSetChanged();
                            recyclerView.post(new Runnable() {
                                public void run() {
                                    mRecyclerNormalAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }
        });
    }
    private void initRecycleView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
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

    }
    @Override
    public void display() {
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (newConfig.orientation != ActivityInfo.SCREEN_ORIENTATION_USER) {
            mFull = false;
        } else {
            mFull = true;
        }

    }

    public boolean onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(getActivity())) {
            return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null) {
            unbinder.unbind();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }

    private void resolveData() {
//        for (int i = 0; i < 19; i++) {
//            VideoModel videoModel = new VideoModel();
//            videoModel.setVideo(true);
//            if (i%3 == 0) {
//                File file = new File(FileManager.Resource_DIR+"a2.mp4");
//                URI uri = file.toURI();
//                String url = uri.toString();
//                videoModel.setUrl(url);
//            }else if(i%3 ==1){
//                File file = new File(FileManager.Resource_DIR+"a1.mp4");
//                URI uri = file.toURI();
//                String url = uri.toString();
//                videoModel.setUrl(url);
//            }else{
//                File file = new File(FileManager.Resource_DIR+"a3.mp4");
//                URI uri = file.toURI();
//                String url = uri.toString();
//                videoModel.setUrl(url);
//            }
//            mVideoList.add(videoModel);
//        }
//        if (mRecyclerNormalAdapter != null)
//            mRecyclerNormalAdapter.notifyDataSetChanged();
    }
}
