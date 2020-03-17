package com.ads.clientconnection.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ads.clientconnection.R;
import com.ads.clientconnection.adapter.ShowFileEntityAdapter;
import com.ads.clientconnection.base.Constant;
import com.ads.clientconnection.base.MyLogger;
import com.ads.clientconnection.entity.ImageAndVideoEntity;
import com.ads.clientconnection.entity.PlayListEntity;
import com.ads.clientconnection.ui.resourceManager.ListNameAdapter;
import com.ads.clientconnection.utils.BaseUtils;
import com.ads.utillibrary.utils.ToastUtils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gongw.remote.Tools;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.client.ClientByteSocketManager;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.layoutmanagers.ScrollSmoothLineaerLayoutManager;
import com.marshalchen.ultimaterecyclerview.swipe.SwipeItemManagerInterface;
import com.marshalchen.ultimaterecyclerview.swipe.SwipeLayout;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 播放列表 view
 */
public class PlayListView extends FrameLayout {
    private static final String TAG = "PlayListView";
    @BindView(R.id.rv_play_list_name)
    RecyclerView rvName;
    @BindView(R.id.rv_play_list_content)
    UltimateRecyclerView rvContent;
    @BindView(R.id.ll_empty)
    LinearLayout llEmpty;
    private Context context;
    private ListNameAdapter listNameAdapter;
    private List<PlayListEntity> playListEntityList = new ArrayList<>();
    private ShowFileEntityAdapter showFileEntityAdapter;
    private List<ImageAndVideoEntity.FileEntity> fileEntityList = new ArrayList<>();
    private ScrollSmoothLineaerLayoutManager lineaerLayoutManager;
    private MaterialDialog materialDialog;
    private ClickLongListener clickLongListener;


    public PlayListView(@NonNull Context context) {
        this(context,null);
    }

    public PlayListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PlayListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.view_play_list,this,true);
        ButterKnife.bind(this,view);
        initNameRecyclerView();
        initContentRecyclerView();
        addListener();
    }
    public void addClickLongListener(ClickLongListener clickLongListener){
        this.clickLongListener = clickLongListener;
    }

    private void initNameRecyclerView() {
        //初始化 显示播放列表名称的列表
        LinearLayoutManager llm_name = new LinearLayoutManager(context);
        llm_name.setOrientation(RecyclerView.HORIZONTAL);
        rvName.setLayoutManager(llm_name);
        listNameAdapter = new ListNameAdapter(playListEntityList,context);
        ListNameAdapter.ButtonPressListener buttonPressListener = new ListNameAdapter.ButtonPressListener() {
            @Override
            public void pressNameButton(View view, int position) {
                rvName.scrollToPosition(position);
                for (int i = 0; i < playListEntityList.size() ; i++) {
                    PlayListEntity playListEntity = playListEntityList.get(i);
                    if (i == position) {
                        playListEntity.setChoice(true);
                    }else{
                        playListEntity.setChoice(false);
                    }
                    listNameAdapter.notifyDataSetChanged();
                    rvName.scrollToPosition(position);
                }
            }

            @Override
            public void pressAddButton() {
                try {
                    LocalBroadcastManager
                            .getInstance(context)
                            .sendBroadcast(new Intent (Constant.ACTION_ADD_IMAGE));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        listNameAdapter.setButtonPressListener(buttonPressListener);
        rvName.setAdapter(listNameAdapter);
    }




    private void initContentRecyclerView() {
        showFileEntityAdapter = new ShowFileEntityAdapter(fileEntityList, Constant.RES_MANAGER_USE_ADAPTER);
        showFileEntityAdapter.setMode(SwipeItemManagerInterface.Mode.Single);

//        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(showFileEntityAdapter);
//        mItemTouchHelper = new ItemTouchHelper(callback);
//        mItemTouchHelper.attachToRecyclerView(rvContent.mRecyclerView);
//        showFileEntityAdapter.setOnDragStartListener(new ShowFileEntityAdapter.OnStartDragListener() {
//            @Override
//            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
//                mItemTouchHelper.startDrag(viewHolder);
//            }
//        });

        //初始化 某个播放列表下所有资源的列表
        lineaerLayoutManager = new ScrollSmoothLineaerLayoutManager(context, LinearLayoutManager.VERTICAL, false, 500);
        rvContent.setLayoutManager(lineaerLayoutManager);
        rvContent.setHasFixedSize(false);
        rvContent.setAdapter(showFileEntityAdapter);
    }
    public List<ImageAndVideoEntity.FileEntity> getFileEntityList(){
        return fileEntityList;
    }



    private void addListener() {
        OnClickCallBack callBack = new OnClickCallBack();
        showFileEntityAdapter.addOnClickListner(callBack);
    }

    public void updateFileEntityList(List<ImageAndVideoEntity.FileEntity> fileEntities){
        try {
            this.fileEntityList.clear();
            this.fileEntityList.addAll(fileEntities);
            showFileEntityAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePlayList(List<PlayListEntity> playList){
        this.playListEntityList.clear();
        this.playListEntityList.addAll(playList);
        listNameAdapter.notifyDataSetChanged();
    }

    class OnClickCallBack implements ShowFileEntityAdapter.OnClickListener{

        @Override
        public void onPressItem(int position) {
//            ToastUtils.showToast(context,"on press item ... "+position);
        }

        @Override
        public void onPressDelete(int postion) {
            openDeleteDialog(postion);
        }

        @Override
        public void onPressDownload(int position) {
            openDownloadDialog(position);
        }

        @Override
        public void onPressEdit(int position) {
            openEditDialog(position);
        }

        @Override
        public void onLongClick(int position) {
            if (clickLongListener!=null) {
                clickLongListener.onLongClickItem(position);
            }
        }
    }



    private void openDeleteDialog(int position) {
        MyLogger.i(TAG,"position ... "+position);
        ImageAndVideoEntity.FileEntity fileEntity = fileEntityList.get(position);
        materialDialog = new MaterialDialog.Builder(context)
                .positiveText("确定")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showFileEntityAdapter.removeAt(position);
                        showFileEntityAdapter.notifyDataSetChanged();
                        SwipeLayout swipeLayout = showFileEntityAdapter.getOpenLayouts().get(position);
                        swipeLayout.close();
                        Intent intent = new Intent(Constant.ACTION_REMOVE_RES);
                        intent.putExtra(Constant.FLAG_REMOVE_IMAGE,fileEntity);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        MyLogger.i(TAG,"swipeLayout isSwipeEnabled() ... "+swipeLayout.isSwipeEnabled());
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        materialDialog.dismiss();
                    }
                }).negativeText("取消")
                .title("提示")
                .content("您确定把该"+fileEntity.getFormat()+"删除吗？")
                .build();
        materialDialog.show();
    }

    public void openDownloadDialog(int position) {
        ImageAndVideoEntity.FileEntity fileEntity = fileEntityList.get(position);
        materialDialog = new MaterialDialog.Builder(context)
                .positiveText("确定")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (fileEntity.isAdd()) {
                            ToastUtils.showToast(context,"该文件广告机上不存在，请下载其他文件");
                            return;
                        }else{
                            byte[] srcBytes = null;
                            try {
                                String name = fileEntity.getName();
                                ClientByteSocketManager.getInstance().currentDownloadName = name;
                                srcBytes = fileEntity.getName().getBytes("UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            if (srcBytes==null) {
                                MyLogger.e(TAG,"download file path【"+fileEntity.getName()+"】is error");
                                return;
                            }
                            int dataLength = srcBytes.length;
                            int byteAllLength = 5 + dataLength;
                            byte[] transBytes = new byte[byteAllLength];
                            System.arraycopy(srcBytes, 0, transBytes, 5, dataLength);
                            //填充操作type
                            transBytes[0] = CommunicationKey.REQUEST_DOWNLOAD_FILE;
                            //填充dataLength
                            Tools.int2BytesExtra(dataLength,transBytes);
                            //显示progressbar
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constant.ACTION_SHOW_PROGRESSBAR));
                            //发送请求
                            ClientByteSocketManager.getInstance().sendMsg(transBytes);
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        materialDialog.dismiss();
                    }
                }).negativeText("取消")
                .title("提示")
                .content("您确定下载该"+fileEntity.getFormat()+"吗？")
                .build();
        materialDialog.show();
    }
    public void openEditDialog(int position) {
        ImageAndVideoEntity.FileEntity fileEntity_ = fileEntityList.get(position);
        int resId;
        boolean isVideo;
        if (fileEntity_.getFormat().equals("图片")) {
            resId = R.array.display_image_duration;
            isVideo = false;
        }else{
            resId = R.array.display_video_duration;
            isVideo = true;
        }
        materialDialog = new MaterialDialog.Builder(context)
                .positiveText("确认")
                .items(resId)
                .itemsColor(context.getResources().getColor(R.color.device_text))
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        String time = text.toString();
                        if (time.contains("默认")) {
                            if (isVideo) {
                                String temp = fileEntity_.getPlayTime();
                                String stayTime = BaseUtils.getStayTimeFromPlayTime(temp);
                                fileEntity_.setTime(stayTime);
                            }else{
                                fileEntity_.setTime("10");
                            }
                        }else{
                            fileEntity_.setTime(time);
                        }
                        showFileEntityAdapter.notifyDataSetChanged();
                        return true;
                    }
                })
                .title("请选择停留时长(单位：秒)")
                .build();
        materialDialog.show();
    }

    public void setNoEmpty(){
        llEmpty.setVisibility(View.GONE);
        rvContent.setVisibility(View.VISIBLE);
    }

    public void setEmpty(){
        llEmpty.setVisibility(View.VISIBLE);
        rvContent.setVisibility(View.GONE);
    }

    public interface ClickLongListener{
        void onLongClickItem(int position);
    }

}
