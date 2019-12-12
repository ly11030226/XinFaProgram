package com.ads.clientconnection.ui.resourceControl;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ads.clientconnection.R;
import com.ads.clientconnection.adapter.ShowResAdapter;
import com.ads.clientconnection.base.BaseActivity;
import com.ads.clientconnection.base.Constant;
import com.ads.clientconnection.base.MyLogger;
import com.ads.clientconnection.entity.ImageAndVideoEntity;
import com.ads.clientconnection.matisse.GifSizeFilter;
import com.ads.clientconnection.matisse.Glide4Engine;
import com.ads.clientconnection.utils.BaseUtils;
import com.ads.utillibrary.utils.ConvertUtils;
import com.ads.utillibrary.utils.MyDialog;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gongw.remote.Tools;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.client.ClientByteSocketManager;
import com.google.gson.Gson;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 控制资源文件界面
 */
@RuntimePermissions
public class ResourceControlActivity extends BaseActivity {
    private static final String TAG = "ResourceControlActivity";
    @BindView(R.id.btn_control_commit)
    Button btnCommit;
    @BindView(R.id.rl_empty)
    RelativeLayout rlEmpty;
    @BindView(R.id.rcv_control)
    RecyclerView rcv;


    private ServerSocketListener ssl;
    private IntentFilter filter;
    private ImageAndVideoEntity mImageAndVideoEntity;
    private ShowResAdapter mShowResAdapter;
    private ArrayList<ImageAndVideoEntity.FileEntity> mFileEntityList = new ArrayList<>();
    private ArrayList<ImageAndVideoEntity.FileEntity> mTempFileEntityList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MaterialDialog noPermissionDialog, noAskDialog;
    private int REQUEST_CODE_CHOOSE = 0x11;
    private Handler mHandler = new Handler();
    private Handler controlHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CommunicationKey.FLAG_CLIENT_UPDATE_SUCCESS) {
                for (ImageAndVideoEntity.FileEntity entity:mFileEntityList){
                    if (entity.isAdd()) {
                        entity.setAdd(false);
                    }
                }
                mShowResAdapter.notifyDataSetChanged();
                mydialog.hideDialog();
                mTempFileEntityList.clear();
                mTempFileEntityList.addAll(mFileEntityList);
                Toast.makeText(ResourceControlActivity.this,"上传成功",Toast.LENGTH_LONG).show();
            }
        }
    };
    private MyDialog mydialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recource_control);
        ButterKnife.bind(this);
        try {
            mydialog = new MyDialog(ResourceControlActivity.this);
            ClientByteSocketManager.getInstance().addHandler(controlHandler);
            initRecyclerView();
            initIntentData();
            addListener();
            registeReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registeReceiver() {
        ssl = new ServerSocketListener();
        filter = new IntentFilter(Constant.ACTION_FINISH_ACTIVITY);
        LocalBroadcastManager.getInstance(ResourceControlActivity.this).registerReceiver(ssl,filter);
    }

    private void addListener() {
//        btnCommit = findViewById(R.id.btn_commit);
        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mydialog.showDialog(BaseUtils.getStringByResouceId(R.string.is_uploading));
                    mImageAndVideoEntity.setFiles((ArrayList<ImageAndVideoEntity.FileEntity>) mFileEntityList);
                    Gson gson = new Gson();
                    String resultJson = gson.toJson(mImageAndVideoEntity);
                    byte[] resultJsonBytes = resultJson.getBytes("UTF-8");
                    int dataLength = resultJsonBytes.length;
                    int byteAllLength = 5+dataLength;
//                    MyLogger.i(TAG,"commit data length ... "+dataLength);
//                    MyLogger.i(TAG,"commit data content ... "+resultJson);
                    byte[] transBytes = new byte[byteAllLength];
                    System.arraycopy(resultJsonBytes, 0, transBytes, 5, dataLength);
                    //填充操作type
                    transBytes[0] = CommunicationKey.REQUEST_UPDATE_LIST;
                    //填充dataLength
                    Tools.int2BytesExtra(dataLength,transBytes);
                    //发送字节数组
                    ClientByteSocketManager.getInstance().sendMsg(transBytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void initIntentData() {
        if (getIntent() != null && getIntent().hasExtra(Constant.JUMP_RESOURCE_CONTROL)) {
            mImageAndVideoEntity = (ImageAndVideoEntity) getIntent().getSerializableExtra(Constant.JUMP_RESOURCE_CONTROL);
            if (mImageAndVideoEntity == null || mImageAndVideoEntity.getFiles() == null || mImageAndVideoEntity.getFiles().size() == 0) {
                setEmptyView();
                MyLogger.e(TAG, "getIntent data is null");
                return;
            } else {
                noftifyUpdateData();
            }
        }
    }

    private void noftifyUpdateData() {
        mTempFileEntityList.clear();
        mTempFileEntityList.addAll(mImageAndVideoEntity.getFiles());
        mFileEntityList.clear();
        mFileEntityList.addAll(mImageAndVideoEntity.getFiles());
        mShowResAdapter.notifyDataSetChanged();
    }


    private void initRecyclerView() {
        mShowResAdapter = new ShowResAdapter(this, mFileEntityList);
        mLinearLayoutManager = new LinearLayoutManager(this);
        rcv.setLayoutManager(mLinearLayoutManager);
        rcv.setAdapter(mShowResAdapter);

        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            private RecyclerView.ViewHolder vh;
            @Override
            public int getMovementFlags(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                // 拖拽的标记，这里允许上下左右四个方向
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT |
                        ItemTouchHelper.RIGHT;
                // 滑动的标记，这里允许左右滑动
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            /**
             *  这个方法会在某个Item被拖动和移动的时候回调，这里我们用来播放动画，
             *  当viewHolder不为空时为选中状态否则为释放状态
             * @param viewHolder
             * @param actionState
             */
            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (viewHolder != null) {
                    vh = viewHolder;
                    pickUpAnimation(viewHolder.itemView);
                } else {
                    if (vh != null) {
                        putDownAnimation(vh.itemView);
                    }
                }
            }

            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                // 移动时更改列表中对应的位置并返回true
                Collections.swap(mFileEntityList, viewHolder.getAdapterPosition(), target
                        .getAdapterPosition());
                return true;
            }

            @Override
            public void onMoved(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @androidx.annotation.NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                // 移动完成后刷新列表
                mShowResAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target
                        .getAdapterPosition());
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 将数据集中的数据移除
                mFileEntityList.remove(viewHolder.getAdapterPosition());
                // 刷新列表
                mShowResAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        }).attachToRecyclerView(rcv);

    }
    private void pickUpAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationZ", 1f, 10f);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }
    private void putDownAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationZ", 10f, 1f);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    @OnClick({R.id.ll_back, R.id.ll_add})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                back();
                break;
            case R.id.ll_add:
                ResourceControlActivityPermissionsDispatcher.checkPermissionWithPermissionCheck(this);
                break;
            default:
                break;
        }
    }
    private void back(){
        Intent intent = new Intent();
        intent.putExtra(Constant.ACTION_BACK, (Serializable) mTempFileEntityList);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void setEmptyView() {
        rcv.setVisibility(View.GONE);
        rlEmpty.setVisibility(View.VISIBLE);
        btnCommit.setVisibility(View.GONE);
    }

    private void setNotEmptyView() {
        rcv.setVisibility(View.VISIBLE);
        rlEmpty.setVisibility(View.GONE);
        btnCommit.setVisibility(View.VISIBLE);
    }


    private void selectImage() {
        Matisse.from(ResourceControlActivity.this).choose(MimeType.of(MimeType.JPEG, MimeType.MP4)).countable(true).maxSelectable(9).addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K)).gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size)).restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED).thumbnailScale(0.85f).imageEngine(new Glide4Engine()).forResult(REQUEST_CODE_CHOOSE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> stringList = Matisse.obtainPathResult(data);
            for (String path : stringList){
                addData(path);
            }
            if (mFileEntityList.size()>0) {
                setNotEmptyView();
            }
        }
    }

    /**
     * 检查是否有重名
     * @param path /storage/emulated/0/1520834082496.jpg
     * @return
     */
    private boolean haveSameName(String path){
        String str = path.substring(path.lastIndexOf("/")+1);
//        MyLogger.i(TAG,"haveSameName str ... "+str);
        for (ImageAndVideoEntity.FileEntity fileEntity:mFileEntityList){
            if (fileEntity.getName().contains(str)) {
                Toast.makeText(ResourceControlActivity.this,"文件名【"+str+"】重复，请修改文件名称",Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }

    private void addData(String path){
        File file = new File(path);
        if (file.exists()&&!haveSameName(path)) {
            try {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while (fis.read(buffer) != -1) {
                        bos.write(buffer, 0, buffer.length);
                    }
                    byte[] results = bos.toByteArray();
                    ImageAndVideoEntity.FileEntity fileEntity = new ImageAndVideoEntity.FileEntity();
                    fileEntity.setAdd(true);
                    fileEntity.setPath(path);
                    String name = file.getName();
                    String format = "";
                    int minute = 0,sec = 0;
                    String finalTime = "";
                    if (name.contains(".mp4")) {
                        format = "视频";
                        long time = getMediaLength(path);
                        minute = (int) (time / 1000 / 60);
                        sec = (int) (time / 1000 % 60);
                        //        MyLogger.i(TAG, "minute: " + minute + "  sec: " + sec);
                        String minuteStr = String.valueOf(minute);
                        if (minuteStr.length() == 1) {
                            minuteStr = "0" + minuteStr;
                        }
                        String secStr = String.valueOf(sec);
                        if (secStr.length() == 1) {
                            secStr = "0" + secStr;
                        }
                        finalTime = minuteStr + ":" + secStr;
                        fileEntity.setPlayTime(finalTime);
                        //停留时长
                        MyLogger.i(TAG,"time ... "+time);
                        fileEntity.setTime((time/1000)+"");
                    }else if (name.contains(".jpg")) {
                        format = "图片";
                        //停留时长
                        fileEntity.setTime("10");
                    }else if (name.contains(".jpeg")) {
                        format = "图片";
                        //停留时长
                        fileEntity.setTime("10");
                    }
                    fileEntity.setName(name);
                    fileEntity.setFormat(format);
                    fileEntity.setSize(ConvertUtils.byte2FitMemorySize(results.length));
                    mFileEntityList.add(fileEntity);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mShowResAdapter.notifyDataSetChanged();
                    }
                });

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    public long getMediaLength(String strMediaPath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(strMediaPath);
        String time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time);
        return timeInmillisec;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (noPermissionDialog != null) {
            noPermissionDialog = null;
        }
        if (noAskDialog != null) {
            noAskDialog = null;
        }
        if (ssl!=null) {
            LocalBroadcastManager.getInstance(ResourceControlActivity.this).unregisterReceiver(ssl);
        }
        controlHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    class ServerSocketListener extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.ACTION_FINISH_ACTIVITY.equals(intent.getAction())) {
                ResourceControlActivity.this.setResult(RESULT_CANCELED);
                finish();
            }
        }
    }


    ////////////////////////////权限相关///////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void checkPermission() {
        try {
            MyLogger.i(TAG, "checkPermission");
            selectImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
        void noPermission(PermissionRequest request) {
            MyLogger.i(TAG, "noPermission");
            if (noPermissionDialog==null) {
                noPermissionDialog = new MaterialDialog.Builder(this)
                        .title(R.string.dialog_title)
                        .content(R.string.res_no_permission)
                        .positiveText(R.string.dialog_commit)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                                request.proceed();
                            }
                        })
                        .negativeText(R.string.dialog_cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                                request.cancel();
                            }
                        })
                        .build();
            }
            noPermissionDialog.show();
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void reject() {
        MyLogger.i(TAG, "reject");
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void rejectAndNOAsk() {
        MyLogger.i(TAG, "rejectAndNOAsk");
        if (noAskDialog==null) {
            noAskDialog = new MaterialDialog.Builder(this)
                    .title(R.string.dialog_title)
                    .content(R.string.res_no_permission)
                    .positiveText(R.string.dialog_confirm)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                            BaseUtils.jumpSystemSet(ResourceControlActivity.this);
                        }
                    })
                    .negativeText(R.string.dialog_do_not)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                            noAskDialog.dismiss();
                        }
                    })
                    .build();
        }
        noAskDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ResourceControlActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    ////////////////////////////权限相关///////////////////////////////////
    //////////////////////////////////////////////////////////////////////


}
