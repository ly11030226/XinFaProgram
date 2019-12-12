package com.ads.xinfa.ui.help;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.ads.xinfa.R;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.entity.ImageAndVideoEntity;
import com.ads.xinfa.net.BaseHandleSocketData;
import com.ads.xinfa.ui.lanConnection.LanConnectionHostActivity;
import com.ads.xinfa.utils.BaseUtils;
import com.ads.xinfa.utils.ToastUtils;
import com.ads.xinfa.utils.Tools;

import java.util.ArrayList;

public class HelpHandleSocketData extends BaseHandleSocketData {
    private Context context;
    public HelpHandleSocketData(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public String makeJsondDataForResponse() {
        return Tools.makeJsonData(context,new ArrayList<>());
    }

    @Override
    public void updateViewByNewList(ArrayList<ImageAndVideoEntity.FileEntity> videoList) {
//        jumpToVideoActivity(videoList);
    }

    @Override
    public void updateViewByUpload(ArrayList<ImageAndVideoEntity.FileEntity> videoList) {
//        jumpToVideoActivity(videoList);
    }

    @Override
    public void toastEmptyList() {
        ToastUtils.showToast(context, BaseUtils.getStringByResouceId(R.string.upload_no_empty));
    }

    @Override
    public void startUpdateList() {
        //暂不显示
    }

    @Override
    public void haveClientConn() {
        jumpToVideoActivity(new ArrayList<ImageAndVideoEntity.FileEntity>());
    }

    private void jumpToVideoActivity(ArrayList<ImageAndVideoEntity.FileEntity> videoList){
        try {
            Activity activity = (Activity)context;
            Intent intent = new Intent(activity, LanConnectionHostActivity.class);
            intent.putExtra(Constant.ACTION_JUMP_FROM_WHERE,Constant.FROM_HELP);
            activity.startActivity(intent);
            activity.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
