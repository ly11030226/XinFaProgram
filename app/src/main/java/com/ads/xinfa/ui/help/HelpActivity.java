package com.ads.xinfa.ui.help;

import android.os.Bundle;
import android.widget.ImageView;

import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;
import com.ads.xinfa.utils.BaseUtils;
import com.ads.xinfa.utils.SystemUtil;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.search.DeviceSearchResponser;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 引导界面
 * @author Ly
 */
public class HelpActivity extends BaseActivity {
    private static final String TAG = "HelpActivity";

    @BindView(R.id.iv_connect)
    ImageView ivConnect;
    @BindView(R.id.iv_http)
    ImageView ivHttp;
    Gson gson;
    private HelpHandleSocketData myBaseHandleSocketData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);
        try {
            //设置右二维码返回数据
            BaseUtils.showQRCode(ivConnect, BaseUtils.getHostIP() + ":" + RemoteConst.DEVICE_SEARCH_PORT);
            BaseUtils.showQRCode(ivHttp, RemoteConst.URL_HTTP_DOWNLOAD);
            gson = new Gson();
            //开始响应搜索
            DeviceSearchResponser.open(SystemUtil.getSystemModelExtra());
            //开始接受通信命令
            myBaseHandleSocketData = new HelpHandleSocketData(HelpActivity.this);
            myBaseHandleSocketData.createServerIfRunnableIsNull(HelpActivity.this);

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
