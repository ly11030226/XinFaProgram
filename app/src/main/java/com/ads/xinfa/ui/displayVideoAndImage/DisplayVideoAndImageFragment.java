package com.ads.xinfa.ui.displayVideoAndImage;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ads.utillibrary.utils.MyDialog;
import com.ads.xinfa.ClientConnService;
import com.ads.xinfa.DoubleClickListener;
import com.ads.xinfa.R;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.FileManager;
import com.ads.xinfa.base.MyFragment;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.bean.MyBean;
import com.ads.xinfa.entity.ImageAndVideoEntity;
import com.ads.xinfa.ui.aboutVideoList.RecyclerNormalAdapter;
import com.ads.xinfa.ui.aboutVideoList.ScrollHelper;
import com.ads.xinfa.ui.lanConnection.LanConnectionHostActivity;
import com.ads.xinfa.ui.modifyPsd.ModifyPsdActivity;
import com.ads.xinfa.utils.BaseUtils;
import com.ads.xinfa.utils.SystemUtil;
import com.ads.xinfa.utils.ToastUtils;
import com.ads.xinfa.utils.Tools;
import com.ads.xinfa.view.CustomMaterialDialog;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.communication.server.ServerByteSocketManager;
import com.gongw.remote.search.DeviceSearchResponser;
import com.google.gson.Gson;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DisplayVideoAndImageFragment extends MyFragment implements LanConnectionHostActivity.ActionListener {
    private static final String TAG = "DisplayVideoAndImage";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private Unbinder unbinder;
    private View view;
    @BindView(R.id.btn)
    Button btnSearch;
    boolean isOpen;
    @BindView(R.id.rcv_)
    RecyclerView mRecyclerView;
    @BindView(R.id.rl_click)
    RelativeLayout rlClick;
    @BindView(R.id.ll_empty)
    LinearLayout llEmpty;
    @BindView(R.id.rl_click_left)
    RelativeLayout rlClickLeft;
    MyDialog myDialog;
    MaterialDialog materialDialog = null;
    private MyLinearLayoutManager mLayoutManager;
    private RecyclerNormalAdapter mRecyclerNormalAdapter;
    private ArrayList<ImageAndVideoEntity.FileEntity> mVideoList = new ArrayList<>();
    private CustomMaterialDialog dialog;
    private Gson gson;
    private ServiceConnection sc;
    private static final boolean isUsePagerSnapHelper = true;
    private DisplayHandleSocketData displayHandleSocketData;
    private static final int AUTO_PLAY = 789;
    private static final int PLAY_DURATION = 5*1000;
    private AlertDialog ad;

    public DisplayVideoAndImageFragment() {
        // Required empty public constructor
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case Constant.KEY_UPDATE_NEW_LIST:
                        setNotEmpty();
                        handler.removeMessages(AUTO_PLAY);
                        Thread.sleep(200);
                        ArrayList<ImageAndVideoEntity.FileEntity> newList = (ArrayList<ImageAndVideoEntity.FileEntity>) msg.obj;
                        mRecyclerNormalAdapter.notifyData(newList);
                        replay();
                        myDialog.hideDialog();
                        break;
                    case Constant.KEY_TRANFER_SUCCESS:
                        handler.removeMessages(AUTO_PLAY);
                        Thread.sleep(200);
                        ArrayList<ImageAndVideoEntity.FileEntity> l = (ArrayList<ImageAndVideoEntity.FileEntity>) msg.obj;
                        mRecyclerNormalAdapter.notifyData(l);
                        replay();
                        myDialog.hideDialog();
                        break;
                    case Constant.KEY_LIST_IS_EMPTY:
                        setEmpty();
                        myDialog.hideDialog();
                        break;
                    case Constant.KEY_START_UPDATE_LIST:
                        myDialog.showDialog(BaseUtils.getStringByResouceId(R.string.loading));
                        break;
                    case AUTO_PLAY:
                        int pos = (int) msg.obj;
                        mRecyclerView.smoothScrollToPosition(pos+1);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private void replay(){
        mRecyclerView.scrollToPosition(0);
        if (mVideoList.size()>0) {
            ImageAndVideoEntity.FileEntity fileEntity = mVideoList.get(0);
            String format = fileEntity.getFormat();
            if ("图片".equals(format)) {
                int duration = Integer.valueOf(fileEntity.getTime());
                Message msg = handler.obtainMessage();
                msg.what = AUTO_PLAY;
                msg.obj = 0;
                handler.sendMessageDelayed(msg,duration * 1000);
            }
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment DisplayVideoAndImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayVideoAndImageFragment newInstance(String param1) {
        DisplayVideoAndImageFragment fragment = new DisplayVideoAndImageFragment();
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
        view = inflater.inflate(R.layout.fragment_display_video_and_image, container, false);
        unbinder = ButterKnife.bind(this,view);
        try {
            myDialog = new MyDialog(getActivity(),R.style.float_dialog);
            myDialog.showDialog(BaseUtils.getStringByResouceId(R.string.loading));
            initRecycleView();
            initData();
            readData();
            addListener();
            startServer();
            if (mVideoList.size()>0) {
                ImageAndVideoEntity.FileEntity fileEntity = mVideoList.get(0);
                String format = fileEntity.getFormat();
                if ("图片".equals(format)) {
                    int duration = Integer.valueOf(fileEntity.getTime());
                    Message msg = handler.obtainMessage();
                    msg.what = AUTO_PLAY;
                    msg.obj = 0;
                    handler.sendMessageDelayed(msg,duration * 1000);
                }
            }
            myDialog.hideDialog();
            //initService();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void startServer() {
        displayHandleSocketData = new DisplayHandleSocketData(getActivity(),mVideoList,handler);
        displayHandleSocketData.createServerIfRunnableIsNull();
    }



    private void initService() {
       sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyLogger.i(TAG,"onServiceConnected");
                ClientConnService mClientConnService = ((ClientConnService.ClientConnBinder)service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                MyLogger.i(TAG,"onServiceDisconnected");
            }
        };
        Intent intent = new Intent(getActivity(),ClientConnService.class);
        getActivity().bindService(intent,sc,Context.BIND_AUTO_CREATE);
    }

    private void addListener() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    stopSearch();
                } else {
                    startSearch();
                }
            }
        });
        rlClick.setOnClickListener(new DoubleClickListener() {
            @Override
            public void doubleClick(View v) {
//                MyLogger.i(TAG,"DoubleClickListener");
                openExitDialog();
            }
        });
        rlClickLeft.setOnClickListener(new DoubleClickListener() {

            @Override
            public void doubleClick(View v) {
                if(ad!=null && ad.isShowing()){
                    return;
                }
                //todo 暂时不显示搜索按钮
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_left_area,null);
                ImageView ivSet = view.findViewById(R.id.iv_setting);
                ivSet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), ModifyPsdActivity.class));
                    }
                });
                ImageView ivLeft = view.findViewById(R.id.iv_left);
                ImageView ivRight = view.findViewById(R.id.iv_right);
                BaseUtils.showQRCode(ivLeft, RemoteConst.URL_HTTP_DOWNLOAD);
                BaseUtils.showQRCode(ivRight,BaseUtils.getHostIP() + ":" + RemoteConst.DEVICE_SEARCH_PORT);
                MyLogger.i(TAG,RemoteConst.URL_HTTP_DOWNLOAD);
                MyLogger.i(TAG,BaseUtils.getHostIP() + ":" + RemoteConst.DEVICE_SEARCH_PORT);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(view);
                ad = builder.create();
                ad.show();
                int dpWidth = Tools.dip2px(getActivity(),600);
                int dpHeight = Tools.dip2px(getActivity(),400);
                ad.getWindow().setLayout(dpWidth,dpHeight);
            }
        });
    }
    private void openExitDialog(){
        String result = "您确定要退出吗？";
        materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_title)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .content(result)
                .positiveText(R.string.dialog_commit)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                System.exit(0);
            }})
                .negativeText(R.string.dialog_cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                materialDialog.dismiss();
            }
        }).build();
        materialDialog.show();
    }



    private void initRecycleView() {
        mLayoutManager = new MyLinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        if (isUsePagerSnapHelper) {
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    private void initData() {
        gson = new Gson();
        mRecyclerNormalAdapter = new RecyclerNormalAdapter(getActivity(), mVideoList, new PlayCompleteCallBack() {
            @Override
            public void playComplete(int pos) {
                MyLogger.i("onScroll","playComplete");
                mRecyclerView.smoothScrollToPosition(pos+1);
            }
        });
        mRecyclerView.setAdapter(mRecyclerNormalAdapter);
        //自定播放帮助类
        ScrollHelper mScrolHelper = new ScrollHelper(R.id.video_player,R.id.iv_item);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int firstVisibleItem, lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                try {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                        MyLogger.i("onScroll","onScrollStateChanged");
                        if (isUsePagerSnapHelper) {
                            if (firstVisibleItem == lastVisibleItem) {
                                MyLogger.i("onScroll","firstVisibleItem ... "+firstVisibleItem+
                                        "  lastVisibleItem ... "+lastVisibleItem);
                                handler.removeMessages(AUTO_PLAY);
                                playVideo(mScrolHelper,firstVisibleItem);
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
//                    MyLogger.i(TAG,
//                            "onScrolled firstVisibleItem ... "+firstVisibleItem+
//                                    " lastVisibleItem ... "+lastVisibleItem+
//                                    " position ... "+GSYVideoManager.instance().getPlayPosition());
                    if (isUsePagerSnapHelper) {
                        mScrolHelper.onScroll(firstVisibleItem,lastVisibleItem,dx,dy);
                    }
                }
            }
        });
    }

    private void playVideo(ScrollHelper mScrolHelper,int pos) {
        if (mLayoutManager!=null) {
//            int childCount = mLayoutManager.getChildCount();
            GSYBaseVideoPlayer mGSYBaseVideoPlayer = mLayoutManager.getChildAt(0).findViewById(R.id.video_player);
            int state = mGSYBaseVideoPlayer.getCurrentState();
//            MyLogger.i(TAG, "onScrollStateChanged state ... " + state);
            int visiableState = mGSYBaseVideoPlayer.getVisibility();
            if (visiableState == View.VISIBLE) {
                mScrolHelper.handleHavePagerSnapHelper(mGSYBaseVideoPlayer);
            }else {
                int duration = Integer.valueOf(mVideoList.get(pos%mVideoList.size()).getTime());
                MyLogger.i("onScroll","duration ... "+duration);
                Message msg = Message.obtain();
                msg.what = AUTO_PLAY;
                msg.obj = pos;
                handler.sendMessageDelayed(msg,duration * 1000);
                releaseVideoAndNotify();
                mScrolHelper.releaseVideo();
            }
        }
    }


    private void releaseVideoAndNotify(){
        GSYVideoManager.releaseAllVideos();
        mRecyclerNormalAdapter.notifyDataSetChanged();
    }

    private void readData() throws IOException {
        File f = new File(FileManager.UPLOAD_DIR + FileManager.JSON_DATA);
        if (!f.exists()) {
            setEmpty();
        }else{
            FileInputStream fis = new FileInputStream(f);
            byte[] b = new byte[1024*8];
            String str = "";
            int len;
            while ((len = fis.read(b))!= -1) {
                str = new String(b,0,len);
                MyLogger.i(TAG,"str ... "+str);
            }
            fis.close();
            if (!TextUtils.isEmpty(str)) {
                mVideoList.clear();
                Gson gson = new Gson();
                ImageAndVideoEntity entity = gson.fromJson(str,ImageAndVideoEntity.class);
                ArrayList<ImageAndVideoEntity.FileEntity> list = entity.getFiles();
                if (list != null && list.size()>0) {
                    setNotEmpty();
                    mVideoList.addAll(list);
                }else{
                    setEmpty();
                }
            }
        }
    }

    private void startSearch(){
//        myProgressbar.showBar(BaseUtils.getStringByResouceId(R.string.start_search));
        myDialog.showDialog(BaseUtils.getStringByResouceId(R.string.start_search));
        //开始响应搜索
        DeviceSearchResponser.open(SystemUtil.getSystemModelExtra());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                myProgressbar.hideBar();
                myDialog.hideDialog();
                btnSearch.setText("关闭搜索响应");
                isOpen = true;
                ToastUtils.showToast(getActivity(),BaseUtils.getStringByResouceId(R.string.start_search_success));
            }
        },2*1000);
    }
    private void stopSearch(){
//        myProgressbar.showBar(BaseUtils.getStringByResouceId(R.string.stop_search));
        myDialog.showDialog(BaseUtils.getStringByResouceId(R.string.stop_search));
        //停止响应搜索
        DeviceSearchResponser.close();
        //停止接收通信命令
        ServerByteSocketManager.getInstance().closeSerVer();
        MyLogger.i(TAG,"stop search");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                myProgressbar.hideBar();
                myDialog.hideDialog();
                btnSearch.setText("开启搜索响应");
                isOpen = false;
                ToastUtils.showToast(getActivity(),BaseUtils.getStringByResouceId(R.string.stop_search_success));
            }
        },2*1000);
    }

    @Override
    public void getAction(String action) {
        if (Constant.FROM_HELP.equals(action)) {
            isOpen = true;
            btnSearch.setText("关闭搜索响应");
        }
    }
    private void setNotEmpty(){
        mRecyclerView.setVisibility(View.VISIBLE);
        llEmpty.setVisibility(View.GONE);
    }
    private void setEmpty(){
        mVideoList.clear();
        mRecyclerNormalAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.GONE);
        llEmpty.setVisibility(View.VISIBLE);
    }

    public interface PlayCompleteCallBack{
        void playComplete(int pos);
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
    public void onDestroyView() {
        MyLogger.i(TAG,"onDestroyView");

        if (unbinder!=null) {
            unbinder.unbind();
        }
        if (dialog!=null) {
            dialog.dismiss();
            dialog = null;
        }
//        getActivity().unbindService(sc);
        super.onDestroyView();
    }



    @Override
    public void onDestroy() {
        MyLogger.i(TAG,"onDestroy");
        try {
            myDialog = null;
            handler.removeCallbacksAndMessages(null);
            //停止响应搜索
            DeviceSearchResponser.close();
            //停止接收通信命令
            ServerByteSocketManager.getInstance().closeSerVer();
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

}
