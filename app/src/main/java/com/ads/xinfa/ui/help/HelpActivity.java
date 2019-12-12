package com.ads.xinfa.ui.help;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.utils.BaseUtils;
import com.ads.xinfa.utils.SystemUtil;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.search.DeviceSearchResponser;
import com.google.gson.Gson;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HelpActivity extends BaseActivity {
    private static final String TAG = "HelpActivity";
    @BindView(R.id.iv_download)
    ImageView ivDownload;
    @BindView(R.id.iv_connect)
    ImageView ivConnect;
    @BindView(R.id.iv_http)
    ImageView ivHttp;
    @BindView(R.id.ll_back_up)
    LinearLayout llBackup;
    @BindView(R.id.iv_bg)
    ImageView ivBg;

    Gson gson;
    private HelpHandleSocketData myBaseHandleSocketData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);
        try {
            //TODO 暂时不显示自定义布局 只是显示一张布局好的图片
            llBackup.setVisibility(View.GONE);
            ivBg.setVisibility(View.VISIBLE);

            //设置左二维码返回数据  ftp://admin:123456@192.168.0.189:2221
            String ftp = "ftp://" + Constant.FTP_USER + ":" + Constant.FTP_PWD + "@" + BaseUtils.getHostIP() + ":" + Constant.FTP_PORT + File.separator + Constant.APK_NAME;
            MyLogger.d(TAG, "ftp url ... " + ftp);
            BaseUtils.showQRCode(ivDownload, ftp);
            //设置右二维码返回数据
            BaseUtils.showQRCode(ivConnect, BaseUtils.getHostIP() + ":" + RemoteConst.DEVICE_SEARCH_PORT);
            BaseUtils.showQRCode(ivHttp, RemoteConst.URL_HTTP_DOWNLOAD);
            gson = new Gson();
            //开始响应搜索
            DeviceSearchResponser.open(SystemUtil.getSystemModelExtra());
            //开始接受通信命令
            myBaseHandleSocketData = new HelpHandleSocketData(HelpActivity.this);
            myBaseHandleSocketData.createServerIfRunnableIsNull();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (myBaseHandleSocketData != null) {
                myBaseHandleSocketData.clear();
                myBaseHandleSocketData = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
