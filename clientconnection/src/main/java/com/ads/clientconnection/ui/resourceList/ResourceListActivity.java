package com.ads.clientconnection.ui.resourceList;

import android.Manifest;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ads.clientconnection.R;
import com.ads.clientconnection.adapter.ShowFileEntityAdapter;
import com.ads.clientconnection.base.BaseActivity;
import com.ads.clientconnection.base.Constant;
import com.ads.clientconnection.base.MyLogger;
import com.ads.clientconnection.entity.ImageAndVideoEntity;
import com.ads.clientconnection.matisse.GifSizeFilter;
import com.ads.clientconnection.matisse.Glide4Engine;
import com.ads.clientconnection.utils.BaseUtils;
import com.ads.utillibrary.utils.ConvertUtils;
import com.ads.utillibrary.utils.MyProgressbar;
import com.ads.utillibrary.utils.ToastUtils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gongw.remote.Tools;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.client.ClientByteSocketManager;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.layoutmanagers.ScrollSmoothLineaerLayoutManager;
import com.marshalchen.ultimaterecyclerview.swipe.SwipeItemManagerInterface;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 资源管理列表界面
 */
@RuntimePermissions
public class ResourceListActivity extends BaseActivity {
    private static final String TAG = "ResourceListActivity";
    private MyListener myListener;
    private IntentFilter intentFilter;
    private MaterialDialog materialDialog;
    private boolean isVideo;
    private int REQUEST_CODE_CHOOSE = 0x11;
    private Handler mHandler = new Handler();
    List<ImageAndVideoEntity.FileEntity> fileEntityList;
    @BindView(R.id.rv_res_list)
    UltimateRecyclerView rv;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.rl_empty)
    RelativeLayout rlEmpty;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.ll_save)
    LinearLayout llSave;

    private ShowFileEntityAdapter showFileEntityAdapter;
    private ScrollSmoothLineaerLayoutManager lineaerLayoutManager;
    private MaterialDialog noPermissionDialog, noAskDialog;
    private MyProgressbar myProgressbar;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CommunicationKey.FLAG_DOWNLOAD_FILE_IS_NULL) {
                myProgressbar.hideBar();
                ToastUtils.showToast(ResourceListActivity.this,BaseUtils.getStringByResouceId(R.string.server_has_no_res));
            }else if (msg.what == CommunicationKey.FLAG_DOWNLOAD_FILE_IS_FINISH) {
                myProgressbar.hideBar();
                ToastUtils.showToast(ResourceListActivity.this,BaseUtils.getStringByResouceId(R.string.download_res_is_finish));
            }else if (msg.what == CommunicationKey.FLAG_FILE_IS_EXIST) {
                myProgressbar.hideBar();
                ToastUtils.showToast(ResourceListActivity.this,BaseUtils.getStringByResouceId(R.string.res_is_exist));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_list);
        ButterKnife.bind(this);
        try {
            myProgressbar = new MyProgressbar(ResourceListActivity.this);
            registerReceiver();
            initIntent();
            initRecyclerView();
            addListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerReceiver() {
        myListener = new MyListener();
        intentFilter = new IntentFilter(Constant.ACTION_FINISH_ACTIVITY);
        intentFilter.addAction(Constant.ACTION_SHOW_SETTING_FROM_RESLIST);
        intentFilter.addAction(Constant.ACTION_ADD_IMAGE);
        LocalBroadcastManager.getInstance(ResourceListActivity.this).registerReceiver(myListener, intentFilter);
    }

    private void addListener() {
        ClickListener clickListener = new ClickListener();
        showFileEntityAdapter.addOnClickListner(clickListener);
    }

    private void initRecyclerView() {
        showFileEntityAdapter = new ShowFileEntityAdapter(fileEntityList, Constant.RES_LIST_USE_ADAPTER);
        showFileEntityAdapter.setMode(SwipeItemManagerInterface.Mode.Single);
        lineaerLayoutManager = new ScrollSmoothLineaerLayoutManager(ResourceListActivity.this, LinearLayoutManager.VERTICAL, false, 500);
        rv.setLayoutManager(lineaerLayoutManager);
        rv.setHasFixedSize(false);
        rv.setAdapter(showFileEntityAdapter);
    }

    private void initIntent() {
        if (getIntent() != null) {
            if (getIntent().hasExtra(Constant.FLAG_PLAY_TYPE)) {
                String type = getIntent().getStringExtra(Constant.FLAG_PLAY_TYPE);
                if (Constant.PLAY_TYPE_IMAGE.equals(type)) {
                    isVideo = false;
                    tvTitle.setText("图片列表");
                } else if (Constant.PLAY_TYPE_VIDEO.equals(type)) {
                    isVideo = true;
                    tvTitle.setText("视频列表");
                }
            }
            //            if (getIntent().hasExtra(Constant.FLAG_PLAY_DATA)) {
            //                playListEntityList = (List<PlayListEntity>) getIntent().getSerializableExtra(Constant.FLAG_PLAY_DATA);
            //            }

            if (getIntent().hasExtra(Constant.FLAG_PLAY_DATA)) {
                fileEntityList = (List<ImageAndVideoEntity.FileEntity>) getIntent().getSerializableExtra(Constant.FLAG_PLAY_DATA);
                if (fileEntityList==null||fileEntityList.size() == 0) {
                    fileEntityList = new ArrayList<>();
                    rv.setVisibility(View.GONE);
                    rlEmpty.setVisibility(View.VISIBLE);
                    String str = BaseUtils.getStringByResouceId(R.string.video_or_image_is_empty);
                    String content;
                    if (isVideo) {
                        content = "视频";
                    }else{
                        content = "图片";
                    }
                    String result = String.format(str,content);
                    tvEmpty.setText(result);
                }else{
                    rv.setVisibility(View.VISIBLE);
                    rlEmpty.setVisibility(View.GONE);
                }
            }
        }
    }

    @OnClick({R.id.ll_back})
    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                back();
                break;
            default:
                break;
        }
    }
    private void back(){
        Intent intent = new Intent();
        intent.putExtra(Constant.ACTION_BACK, (Serializable) fileEntityList);
        intent.putExtra(Constant.ACTION_IS_VIDEO,isVideo);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void selectImage() {
        Matisse.from(ResourceListActivity.this).
                choose(MimeType.of(MimeType.JPEG, MimeType.MP4)).
                countable(true).
                maxSelectable(9).
                addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K)).
                gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size)).
                restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f).
                imageEngine(new Glide4Engine()).
                forResult(REQUEST_CODE_CHOOSE);
    }


    class ClickListener implements ShowFileEntityAdapter.OnClickListener {

        @Override
        public void onPressItem(int position) {
        }

        @Override
        public void onPressDelete(int postion) {
            openDeleteDialog(postion);
        }

        @Override
        public void onPressDownload(int position) {
        }

        @Override
        public void onPressEdit(int position) {
        }

        @Override
        public void onLongClick(int position) {

        }
    }

    private void openDeleteDialog(int position) {
        MyLogger.i(TAG, "position ... " + position);
        ImageAndVideoEntity.FileEntity fileEntity = fileEntityList.get(position);
        materialDialog = new MaterialDialog.Builder(ResourceListActivity.this).positiveText("确定").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (fileEntityList.size() == 1 && fileEntityList.contains(fileEntity)) {
                    fileEntityList.remove(fileEntity);
                    setEmptyView();
                }else{
                    showFileEntityAdapter.removeAt(position);
                    showFileEntityAdapter.notifyDataSetChanged();
                }

            }
        }).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                materialDialog.dismiss();
            }
        }).negativeText("取消").title("提示").content("您确定把该" + fileEntity.getFormat() + "删除吗？").build();
        materialDialog.show();
    }

    public void openDownloadDialog(int position) {
        ImageAndVideoEntity.FileEntity fileEntity = fileEntityList.get(position);
        materialDialog = new MaterialDialog.Builder(ResourceListActivity.this).positiveText("确定").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                ClientByteSocketManager.getInstance().addDownloadFileHandler(handler);
                myProgressbar.showBar(BaseUtils.getStringByResouceId(R.string.download_remind));
                if (fileEntity.isAdd()) {
                    ToastUtils.showToast(ResourceListActivity.this,"该文件广告机上不存在，请下载其他文件");
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
                    //发送请求
                    ClientByteSocketManager.getInstance().sendMsg(transBytes);
                }
            }
        }).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                materialDialog.dismiss();
            }
        }).negativeText("取消").title("提示").content("您确定下载该" + fileEntity.getFormat() + "吗？").build();
        materialDialog.show();
    }

    public void openEditDialog(int position) {
        ImageAndVideoEntity.FileEntity fileEntity_ = fileEntityList.get(position);
        int resId;
        boolean isVideo;
        if (fileEntity_.getFormat().equals("图片")) {
            resId = R.array.display_image_duration;
            isVideo = false;
        } else {
            resId = R.array.display_video_duration;
            isVideo = true;
        }
        materialDialog = new MaterialDialog.Builder(ResourceListActivity.this).positiveText("确认").items(resId).itemsColor(ResourceListActivity.this.getResources().getColor(R.color.device_text)).itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
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
                } else {
                    fileEntity_.setTime(time);
                }
                showFileEntityAdapter.notifyDataSetChanged();
                return true;
            }
        }).title("请选择停留时长(单位：秒)").build();
        materialDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myListener != null) {
            LocalBroadcastManager.getInstance(ResourceListActivity.this).unregisterReceiver(myListener);
        }
    }

    class MyListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.ACTION_FINISH_ACTIVITY.equals(intent.getAction())) {
                finish();
            } else if (Constant.ACTION_SHOW_SETTING_FROM_RESLIST.equals(intent.getAction())) {
                int position = intent.getIntExtra(Constant.KEY_SHOW_SETTING, -1);
                if (position >= 0) {
                    //                    ToastUtils.showToast(context,"onReceive pos ... "+position);
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(ResourceListActivity.this);
                    MaterialDialog alertDialog = builder.canceledOnTouchOutside(true).cancelable(true).items(R.array.setting).itemsColor(context.getResources().getColor(R.color.device_text)).itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int pos, CharSequence text) {
                            //编辑显示时长
                            if (pos == 0) {
                                openEditDialog(position);
                            }
                            //下载
                            else if (pos == 1) {
                                openDownloadDialog(position);
                            }
                            dialog.dismiss();
                        }
                    }).build();
                    alertDialog.show();
                }
            }else if (Constant.ACTION_ADD_IMAGE.equals(intent.getAction())) {
                receivePressAddButton();
            }
        }
    }

    public void receivePressAddButton(){
        ResourceListActivityPermissionsDispatcher.checkPermissionWithPermissionCheck(ResourceListActivity.this);
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
        if (noPermissionDialog == null) {
            noPermissionDialog = new MaterialDialog.Builder(this).title(R.string.dialog_title).content(R.string.res_no_permission).positiveText(R.string.dialog_commit).onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                    request.proceed();
                }
            }).negativeText(R.string.dialog_cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                    request.cancel();
                }
            }).build();
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
        if (noAskDialog == null) {
            noAskDialog = new MaterialDialog.Builder(this).title(R.string.dialog_title).content(R.string.res_no_permission).positiveText(R.string.dialog_confirm).onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                    BaseUtils.jumpSystemSet(ResourceListActivity.this);
                }
            }).negativeText(R.string.dialog_do_not).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                    noAskDialog.dismiss();
                }
            }).build();
        }
        noAskDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @io.reactivex.annotations.NonNull String[] permissions, @io.reactivex.annotations.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ResourceListActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void setEmptyView() {
        rv.setVisibility(View.GONE);
        rlEmpty.setVisibility(View.VISIBLE);
        String str = BaseUtils.getStringByResouceId(R.string.video_or_image_is_empty);
        String content;
        if (isVideo) {
            content = "视频";
        }else{
            content = "图片";
        }
        String result = String.format(str,content);
        tvEmpty.setText(result);
    }

    private void setNotEmptyView() {
        rv.setVisibility(View.VISIBLE);
        rlEmpty.setVisibility(View.GONE);

    }


    ////////////////////////////权限相关///////////////////////////////////
    //////////////////////////////////////////////////////////////////////



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> stringList = Matisse.obtainPathResult(data);
            for (String path : stringList){
                addData(path);
            }
            if (fileEntityList.size()>0) {
                setNotEmptyView();
            }
        }
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
//                    fileEntity.setPath(path);
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
                    fileEntityList.add(fileEntity);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showFileEntityAdapter.notifyDataSetChanged();
                    }
                });

            } catch(Exception e){
                e.printStackTrace();
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
        for (ImageAndVideoEntity.FileEntity fileEntity:fileEntityList){
            if (fileEntity.getName().contains(str)) {
                Toast.makeText(ResourceListActivity.this,"文件名【"+str+"】重复，请修改文件名称",Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }
    public long getMediaLength(String strMediaPath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(strMediaPath);
        String time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time);
        return timeInmillisec;
    }
}
