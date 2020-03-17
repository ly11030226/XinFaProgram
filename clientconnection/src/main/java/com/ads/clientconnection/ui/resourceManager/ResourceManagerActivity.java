package com.ads.clientconnection.ui.resourceManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ads.clientconnection.R;
import com.ads.clientconnection.base.BaseActivity;
import com.ads.clientconnection.base.Constant;
import com.ads.clientconnection.base.MyLogger;
import com.ads.clientconnection.entity.ImageAndVideoEntity;
import com.ads.clientconnection.entity.PlayListEntity;
import com.ads.clientconnection.matisse.GifSizeFilter;
import com.ads.clientconnection.matisse.Glide4Engine;
import com.ads.clientconnection.ui.resourceList.ResourceListActivity;
import com.ads.clientconnection.utils.BaseUtils;
import com.ads.clientconnection.utils.PreferencesUtils;
import com.ads.clientconnection.view.PlayListView;
import com.ads.utillibrary.utils.ConvertUtils;
import com.ads.utillibrary.utils.MyProgressbar;
import com.ads.utillibrary.utils.ToastUtils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.Tools;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.client.ClientByteSocketManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
 * 资源管理器 界面
 */
@RuntimePermissions
public class ResourceManagerActivity extends BaseActivity {
    private static final String TAG = "ResourceManagerActivity";
    //播放列表data
    List<PlayListEntity> playListEntityList = new ArrayList<>();
    //存放所有视频
    List<ImageAndVideoEntity.FileEntity> videos = new ArrayList<>();
    //存放所有图片
    List<ImageAndVideoEntity.FileEntity> images = new ArrayList<>();

    private static final int REQUEST_CODE_FOR_LIST = 999;

    private ArrayList<ImageAndVideoEntity.FileEntity> fileEntityArrayList = new ArrayList<>();
    private ArrayList<ImageAndVideoEntity.FileEntity> mTempFileEntityList = new ArrayList<>();
    private MyProgressbar myProgressbar;
    private static final String DEFAULT_IMAGE_DURATION = "10";

    @BindView(R.id.plv)
    PlayListView plv;

    private ServerSocketListener ssl;
    private IntentFilter filter;
    private ImageAndVideoEntity mImageAndVideoEntity;
    private MaterialDialog noPermissionDialog, noAskDialog;
    private int REQUEST_CODE_CHOOSE = 0x11;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CommunicationKey.FLAG_DOWNLOAD_FILE_IS_NULL) {
                myProgressbar.hideBar();
                ToastUtils.showToast(ResourceManagerActivity.this, BaseUtils.getStringByResouceId(R.string.server_has_no_res));
            } else if (msg.what == CommunicationKey.FLAG_DOWNLOAD_FILE_IS_FINISH) {
                myProgressbar.hideBar();
                ToastUtils.showToast(ResourceManagerActivity.this, BaseUtils.getStringByResouceId(R.string.download_res_is_finish));
            } else if (msg.what == CommunicationKey.FLAG_FILE_IS_EXIST) {
                myProgressbar.hideBar();
                ToastUtils.showToast(ResourceManagerActivity.this, BaseUtils.getStringByResouceId(R.string.res_is_exist));
            }
        }
    };
    private Handler controlHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CommunicationKey.FLAG_CLIENT_UPDATE_SUCCESS) {
                for (ImageAndVideoEntity.FileEntity entity : fileEntityArrayList) {
                    if (entity.isAdd()) {
                        entity.setAdd(false);
                    }
                }
                myProgressbar.hideBar();
                mTempFileEntityList.clear();
                mTempFileEntityList.addAll(fileEntityArrayList);
                Toast.makeText(ResourceManagerActivity.this, "上传成功", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_manager);
        ButterKnife.bind(this);
        try {
            //读本地保存的sharedpreference (名字是非DEFAULT的 播放列表)
            //            readSharedpreference();
            //注册广播 用来监听 如果服务器端close，本界面finish
            myProgressbar = new MyProgressbar(ResourceManagerActivity.this);
            registeReceiver();
            initIntentData();
            ClientByteSocketManager.getInstance().addHandler(controlHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setImageAndVideoList() {
        images.clear();
        videos.clear();
        for (ImageAndVideoEntity.FileEntity fileEntity : fileEntityArrayList) {
            if (fileEntity.getFormat().equals(RemoteConst.IMAGE)) {
                images.add(fileEntity);
            } else if (fileEntity.getFormat().equals(RemoteConst.VIDEO)) {
                videos.add(fileEntity);
            }
        }
    }

    private void readSharedpreference() {
        Gson gson = new Gson();
        String str = PreferencesUtils.getString(ResourceManagerActivity.this, Constant.KEY_PLAY_DATA, "");
        if (!TextUtils.isEmpty(str)) {
            List<PlayListEntity> playList = gson.fromJson(str, new TypeToken<List<PlayListEntity>>() {
            }.getType());
            if (playList.size() > 1 && playList != null) {
                playListEntityList.clear();
                playListEntityList.addAll(playList);
            }
        }
    }

    private void registeReceiver() {
        ssl = new ResourceManagerActivity.ServerSocketListener();
        filter = new IntentFilter(Constant.ACTION_FINISH_ACTIVITY);
        filter.addAction(Constant.ACTION_SHOW_SETTING_FROM_RESMANAGER);
        filter.addAction(Constant.ACTION_ADD_IMAGE);
        filter.addAction(Constant.ACTION_SHOW_PROGRESSBAR);
        filter.addAction(Constant.ACTION_REMOVE_RES);
        LocalBroadcastManager.getInstance(ResourceManagerActivity.this).registerReceiver(ssl, filter);
    }

    private void initIntentData() {
        if (getIntent() != null && getIntent().hasExtra(Constant.JUMP_RESOURCE_CONTROL)) {
            mImageAndVideoEntity = (ImageAndVideoEntity) getIntent().getSerializableExtra(Constant.JUMP_RESOURCE_CONTROL);
            if (mImageAndVideoEntity == null || mImageAndVideoEntity.getFiles() == null || mImageAndVideoEntity.getFiles().size() == 0) {
                setEmptyView();
                MyLogger.e(TAG, "getIntent data is null");
            } else {
                setNotEmptyView();
                initData();
            }
        }
        PlayListEntity entity1 = new PlayListEntity();
        entity1.setChoice(true);
        entity1.setName("播放列表");
        //暂时不用数据
        //        entity1.setFileEntityList(fileEntityArrayList);
        playListEntityList.add(entity1);
        //更新PlayList内容
        plv.updatePlayList(playListEntityList);
    }

    private void initData() {
        //测试数据
        //        testData();
        //        PlayListEntity playListEntity = new PlayListEntity();
        //        playListEntity.setName(Constant.PLAY_LIST_DEFAULT_NAME);
        //        playListEntity.setChoice(true);
        //        playListEntity.setFileEntityList((List<ImageAndVideoEntity.FileEntity>) mImageAndVideoEntity);
        //        playListEntityList.add(0, playListEntity);
        fileEntityArrayList.clear();
        fileEntityArrayList.addAll(mImageAndVideoEntity.getFiles());
        mTempFileEntityList.clear();
        mTempFileEntityList.addAll(mImageAndVideoEntity.getFiles());
        notifyUpdateData();
        setImageAndVideoList();
    }


    private void notifyUpdateData() {
        plv.updateFileEntityList(fileEntityArrayList);
    }

    private void back() {
        Intent intent = new Intent();
        intent.putExtra(Constant.ACTION_BACK, (Serializable) mTempFileEntityList);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @OnClick({R.id.ll_video_list, R.id.ll_image_list, R.id.ll_upload, R.id.ll_back})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_video_list:  //视频
                updateDataFromPlv();
                setImageAndVideoList();
                Intent intent = new Intent(ResourceManagerActivity.this, ResourceListActivity.class);
                intent.putExtra(Constant.FLAG_PLAY_TYPE, Constant.PLAY_TYPE_VIDEO);
                //                intent.putExtra(Constant.FLAG_PLAY_DATA, (Serializable) getResFromPlayList(true));
                intent.putExtra(Constant.FLAG_PLAY_DATA, (Serializable) videos);
                startActivityForResult(intent, REQUEST_CODE_FOR_LIST);
                break;
            case R.id.ll_image_list:  //图片
                updateDataFromPlv();
                setImageAndVideoList();
                Intent i = new Intent(ResourceManagerActivity.this, ResourceListActivity.class);
                i.putExtra(Constant.FLAG_PLAY_TYPE, Constant.PLAY_TYPE_IMAGE);
                //                i.putExtra(Constant.FLAG_PLAY_DATA, (Serializable) getResFromPlayList(false));
                i.putExtra(Constant.FLAG_PLAY_DATA, (Serializable) images);
                startActivityForResult(i, REQUEST_CODE_FOR_LIST);
                break;
            case R.id.ll_upload:   //上传
                commit();
                break;
            case R.id.ll_back:   //返回
                back();
                break;
            default:
                break;
        }
    }

    private void commit() {
        try {
            //将更改顺序或者添加视图的list保存到fileEntityArrayList中
            updateDataFromPlv();
            myProgressbar.showBar("正在上传...");
            mImageAndVideoEntity.setFiles((ArrayList<ImageAndVideoEntity.FileEntity>) fileEntityArrayList);
            Gson gson = new Gson();
            String resultJson = gson.toJson(mImageAndVideoEntity);
            byte[] resultJsonBytes = resultJson.getBytes("UTF-8");
            int dataLength = resultJsonBytes.length;
            int byteAllLength = 5 + dataLength;
            //                    MyLogger.i(TAG,"commit data length ... "+dataLength);
            MyLogger.i(TAG, "commit data content ... " + resultJson);
            byte[] transBytes = new byte[byteAllLength];
            System.arraycopy(resultJsonBytes, 0, transBytes, 5, dataLength);
            //填充操作type
            transBytes[0] = CommunicationKey.REQUEST_UPDATE_LIST;
            //填充dataLength
            Tools.int2BytesExtra(dataLength, transBytes);
            //发送字节数组
            ClientByteSocketManager.getInstance().sendMsg(transBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 将播放列表分为 只存储视频的播放列表  或者  只存储图片的播放列表
     *
     * @param isVideo
     * @return
     */
    private List<PlayListEntity> getResFromPlayList(boolean isVideo) {
        List<PlayListEntity> tempPlayListEntity = new ArrayList<>();
        for (PlayListEntity playListEntity : playListEntityList) {
            List<ImageAndVideoEntity.FileEntity> tempVideo = new ArrayList<>();
            List<ImageAndVideoEntity.FileEntity> tempImage = new ArrayList<>();
            PlayListEntity tempPlayList = new PlayListEntity();
            for (ImageAndVideoEntity.FileEntity fileEntity : playListEntity.getFileEntityList()) {
                if ("视频".equals(fileEntity.getFormat())) {
                    tempVideo.add(fileEntity);
                } else if ("图片".equals(fileEntity.getFormat())) {
                    tempImage.add(fileEntity);
                }
            }
            if (isVideo) {
                tempPlayList.setFileEntityList(tempVideo);
            } else {
                tempPlayList.setFileEntityList(tempImage);
            }
            tempPlayList.setChoice(playListEntity.isChoice());
            tempPlayList.setName(playListEntity.getName());
            tempPlayList.setCreateTime(playListEntity.getCreateTime());
        }
        return tempPlayListEntity;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ssl != null) {
            LocalBroadcastManager.getInstance(ResourceManagerActivity.this).unregisterReceiver(ssl);
        }
        handler.removeCallbacksAndMessages(null);
    }

    private void setEmptyView() {
        plv.setEmpty();
    }

    private void setNotEmptyView() {
        plv.setNoEmpty();
    }

    private void selectImage() {
        Matisse.from(ResourceManagerActivity.this).
                choose(MimeType.of(MimeType.JPEG, MimeType.MP4)).
                countable(true).
                maxSelectable(9).
                addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K)).
                gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size)).
                restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED).thumbnailScale(0.85f).
                imageEngine(new Glide4Engine()).
                forResult(REQUEST_CODE_CHOOSE);
    }

    class ServerSocketListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.ACTION_FINISH_ACTIVITY.equals(intent.getAction())) {
                ResourceManagerActivity.this.setResult(RESULT_CANCELED);
                finish();
            } else if (Constant.ACTION_SHOW_SETTING_FROM_RESMANAGER.equals(intent.getAction())) {
                int position = intent.getIntExtra(Constant.KEY_SHOW_SETTING, -1);
                if (position >= 0) {
                    //                    ToastUtils.showToast(context,"onReceive pos ... "+position);
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(ResourceManagerActivity.this);
                    MaterialDialog alertDialog = builder.canceledOnTouchOutside(true)
                            .cancelable(true)
                            .items(R.array.setting)
                            .itemsColor(ContextCompat.getColor(ResourceManagerActivity.this,R.color.device_text))
                            .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int pos, CharSequence text) {
                            //编辑显示时长
                            if (pos == 0) {
                                plv.openEditDialog(position);
                            }
                            //下载
                            else if (pos == 1) {
                                ClientByteSocketManager.getInstance().addDownloadFileHandler(handler);
                                plv.openDownloadDialog(position);
                            }
                            dialog.dismiss();
                        }
                    }).build();
                    alertDialog.show();
                }
            } else if (Constant.ACTION_ADD_IMAGE.equals(intent.getAction())) {
                ResourceManagerActivityPermissionsDispatcher.checkPermissionWithPermissionCheck(ResourceManagerActivity.this);
            } else if (Constant.ACTION_SHOW_PROGRESSBAR.equals(intent.getAction())) {
                myProgressbar.showBar(BaseUtils.getStringByResouceId(R.string.download_remind));
            } else if (Constant.ACTION_REMOVE_RES.equals(intent.getAction())) { //删除的资源文件
                ImageAndVideoEntity.FileEntity fileEntity = (ImageAndVideoEntity.FileEntity) intent.getSerializableExtra(Constant.FLAG_REMOVE_IMAGE);
                if (fileEntity.getFormat().equals("视频") && videos.contains(fileEntity) && fileEntityArrayList.contains(fileEntity)) {
                    videos.remove(fileEntity);
                    fileEntityArrayList.remove(fileEntity);
                } else if (fileEntity.getFormat().equals("图片") && images.contains(fileEntity) && fileEntityArrayList.contains(fileEntity)) {
                    images.remove(fileEntity);
                    fileEntityArrayList.remove(fileEntity);
                }
                if (fileEntityArrayList.size() == 0) {
                    setEmptyView();
                } else {
                    setNotEmptyView();
                }
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
                    BaseUtils.jumpSystemSet(ResourceManagerActivity.this);
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
        ResourceManagerActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    ////////////////////////////权限相关///////////////////////////////////
    //////////////////////////////////////////////////////////////////////


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {

        } else if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> uriList = Matisse.obtainResult(data);
            boolean isNeedRemind = false;
            for (Uri uri : uriList) {
                if (uri == null) {
                    continue;
                }
                final String scheme = uri.getScheme();
                if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    String mime = BaseUtils.getMimeTypeByUri(ResourceManagerActivity.this,uri);
                    long length = BaseUtils.getFileLengthByUri(ResourceManagerActivity.this,uri);
                    long num = (length / (1024 * 1024)) + 1;
                    if (RemoteConst.MP4.equals(mime)) {
                        if (num > Constant.UPLOAD_VIDEO_MAX_LENGTH) {
                            isNeedRemind = true;
                            continue;
                        }
                    } else if (RemoteConst.JPG.equals(mime)|| RemoteConst.JPEG.equals(mime)) {
                        if (num > Constant.UPLOAD_IMAGE_MAX_LENGTH) {
                            isNeedRemind = true;
                            continue;
                        }
                    }
//                    addData(path);
                    Uri mIri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    String[] projection = new String[]{MediaStore.Images.Media._ID,MediaStore.Images.Media.DISPLAY_NAME,MediaStore.Images.Media.SIZE};
                    String selection = MediaStore.Images.Media._ID + "=?";
                    int id = (int) ContentUris.parseId(uri);
                    String idStr = String.valueOf(id);
                    String[] args = new String[]{idStr};
                    String format = RemoteConst.IMAGE;
                    if (RemoteConst.MP4.equals(mime)) {
                        format = RemoteConst.VIDEO;
                        mIri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        projection = new String[]{MediaStore.Video.Media._ID,MediaStore.Video.Media.DISPLAY_NAME,MediaStore.Video.Media.SIZE,MediaStore.Video.Media.DURATION};
                        selection = MediaStore.Video.Media._ID + "=?";
                    }
                    ContentResolver cr = ResourceManagerActivity.this.getContentResolver();
                    String displayName = "";
                    long size = 0;
                    int duration = 0;
                    Cursor cursor = cr.query(mIri, projection, selection, args, null);
                    if (cursor != null) {
                        while(cursor.moveToNext()){
                            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                            if (id == _id) {
                                displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                                size= cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                                if (RemoteConst.MP4.equals(mime)) {
                                    duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                                }
                                Log.d(TAG, "displayName ... "+displayName);
                                Log.d(TAG, "size ... "+size);
                                Log.d(TAG, "duration ... "+duration);
                            }
                        }
                        cursor.close();
                    }
                    if (!haveSameName(uri,displayName)) {
                        ImageAndVideoEntity.FileEntity fileEntity = new ImageAndVideoEntity.FileEntity();
                        fileEntity.setAdd(true);
                        fileEntity.setUriStr(uri.toString());
                        int minute = 0, sec = 0;
                        String finalTime = "";
                        if (RemoteConst.VIDEO.equals(format)) {
                            minute = duration / 1000 / 60;
                            sec = duration / 1000 % 60;
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
                            MyLogger.i(TAG, "time ... " + duration);
                            fileEntity.setTime((duration / 1000) + "");
                        } else if (RemoteConst.IMAGE.equals(format)) {
                            //停留时长
                            fileEntity.setTime(DEFAULT_IMAGE_DURATION);
                        }
                        fileEntity.setName(displayName);
                        fileEntity.setFormat(format);
                        fileEntity.setSize(ConvertUtils.byte2FitMemorySize(size));
                        fileEntityArrayList.add(fileEntity);
                    }
                } else {
                    continue;
                }
            }
            plv.updateFileEntityList(fileEntityArrayList);
            if (isNeedRemind) {
                Toast.makeText(ResourceManagerActivity.this, BaseUtils.getStringByResouceId(R.string.file_have_limit), Toast.LENGTH_LONG).show();
            }
            setImageAndVideoList();
            if (fileEntityArrayList.size() > 0) {
                setNotEmptyView();
            }
//            List<String> stringList = Matisse.obtainPathResult(data);
//            boolean isNeedRemind = false;
//            for (String path : stringList) {
//                File file = new File(path);
//                String name = file.getName();
//                long num = (file.length() / (1024 * 1024)) + 1;
//                if (name.contains(".mp4")) {
//                    if (num > Constant.UPLOAD_VIDEO_MAX_LENGTH) {
//                        isNeedRemind = true;
//                        continue;
//                    }
//                } else if (name.contains(".jpg") || name.contains(".jpeg")) {
//                    if (num > Constant.UPLOAD_IMAGE_MAX_LENGTH) {
//                        isNeedRemind = true;
//                        continue;
//                    }
//                }
//                addData(path);
//            }
//            if (isNeedRemind) {
//                Toast.makeText(ResourceManagerActivity.this, BaseUtils.getStringByResouceId(R.string.file_have_limit), Toast.LENGTH_LONG).show();
//            }
//            setImageAndVideoList();
//            if (fileEntityArrayList.size() > 0) {
//                setNotEmptyView();
//            }
        } else if (requestCode == REQUEST_CODE_FOR_LIST && resultCode == RESULT_OK) {
            boolean isVideo = data.getBooleanExtra(Constant.ACTION_IS_VIDEO, true);
            List<ImageAndVideoEntity.FileEntity> list = (ArrayList<ImageAndVideoEntity.FileEntity>) data.getSerializableExtra(Constant.ACTION_BACK);
            updateDataFromPlv();
            if (list != null && list.size() > 0) {
                String format = list.get(0).getFormat();
                ArrayList<ImageAndVideoEntity.FileEntity> temp = new ArrayList<>();
                for (int j = 0; j < fileEntityArrayList.size(); j++) {
                    if (fileEntityArrayList.get(j).getFormat().equals(format)) {
                        boolean isDelete = true;
                        for (int i = 0; i < list.size(); i++) {
                            if (fileEntityArrayList.get(j).getName().equals(list.get(i).getName())) {
                                fileEntityArrayList.get(j).setTime(list.get(i).getTime());
                                isDelete = false;
                                break;
                            }
                        }
                        if (isDelete) {
                            temp.add(fileEntityArrayList.get(j));
                        }
                    }
                }
                fileEntityArrayList.removeAll(temp);
                setImageAndVideoList();
                if (fileEntityArrayList.size() == 0) {
                    setEmptyView();
                } else {
                    setNotEmptyView();
                }
                plv.updateFileEntityList(fileEntityArrayList);
            }
        }
    }

    private void updateDataFromPlv() {
        //将更改顺序或者添加视图的list保存到fileEntityArrayList中
        List<ImageAndVideoEntity.FileEntity> list = plv.getFileEntityList();
        fileEntityArrayList.clear();
        fileEntityArrayList.addAll(list);
    }

    private void addData(String path) {
        File file = new File(path);
        if (file.exists() && !haveSameName(path)) {
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
                    String name = file.getName();
                    String format = "";
                    int minute = 0, sec = 0;
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
                        MyLogger.i(TAG, "time ... " + time);
                        fileEntity.setTime((time / 1000) + "");
                    } else if (name.contains(".jpg")) {
                        format = "图片";
                        //停留时长
                        fileEntity.setTime("10");
                    } else if (name.contains(".jpeg")) {
                        format = "图片";
                        //停留时长
                        fileEntity.setTime("10");
                    }
                    fileEntity.setName(name);
                    fileEntity.setFormat(format);
                    fileEntity.setSize(ConvertUtils.byte2FitMemorySize(results.length));
                    fileEntityArrayList.add(fileEntity);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                plv.updateFileEntityList(fileEntityArrayList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查是否有重名
     *
     * @param path /storage/emulated/0/1520834082496.jpg
     * @return
     */
    private boolean haveSameName(String path) {
        String str = path.substring(path.lastIndexOf("/") + 1);
        //        MyLogger.i(TAG,"haveSameName str ... "+str);
        for (ImageAndVideoEntity.FileEntity fileEntity : fileEntityArrayList) {
            if (fileEntity.getName().contains(str)) {
                Toast.makeText(ResourceManagerActivity.this, "文件名【" + str + "】重复，请修改文件名称", Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }

    private boolean haveSameName(Uri uri,String displayName){
        for (ImageAndVideoEntity.FileEntity fileEntity : fileEntityArrayList) {
                if(displayName.equals(fileEntity.getName())) {
                ToastUtils.showToast(ResourceManagerActivity.this,"文件名【" + displayName + "】重复，请修改文件名称");
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
