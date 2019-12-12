package com.ads.clientconnection.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ads.clientconnection.R;
import com.ads.clientconnection.base.Constant;
import com.ads.clientconnection.base.MyLogger;
import com.ads.clientconnection.entity.ImageAndVideoEntity;
import com.ads.clientconnection.ui.resourceManager.ResourceManagerActivity;
import com.ads.clientconnection.utils.BaseUtils;
import com.ads.utillibrary.utils.ToastUtils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gongw.remote.communication.client.ClientByteSocketManager;
import com.gongw.remote.search.DeviceSearcher;

import androidx.annotation.NonNull;


public class ClientInfoView extends RelativeLayout {
    private static final String TAG = "ClientInfoView";
    private Context mContext;
    private TextView tvSearch;
    private DeviceSearcher.OnSearchListener mSearchListener;
    private TextView tvIp;
    private ImageView ivImg;
    private TextView tvConn;
    private LinearLayout llConnected;
    private TextView tvStop;
    private LinearLayout llStartSearch;
    private ImageView ivScan;
    private String ip;
    private String json;
    private TextView tvManage;
    private ImageAndVideoEntity entity;
    private MaterialDialog materialDialog;

    public ClientInfoView(Context context) {
        this(context,null);
    }

    public ClientInfoView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ClientInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        try {
            init();
            addListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSearchListener(DeviceSearcher.OnSearchListener searchListener){
        this.mSearchListener = searchListener;
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.view_client_info,this,true);
        ivImg = findViewById(R.id.iv_img);
//        ivImg.setImageResource();   //需要设置图片
        tvIp = findViewById(R.id.tv_ip);
        String ip = BaseUtils.getHostIP();
        tvIp.setText(ip);
        tvSearch = findViewById(R.id.btn_start_search);
        llStartSearch = findViewById(R.id.ll_start_search);
        llConnected = findViewById(R.id.ll_connected);
        tvConn = findViewById(R.id.tv_connected);
        tvStop = findViewById(R.id.btn_stop_search);
        ivScan = findViewById(R.id.iv_scanning);
        tvManage = findViewById(R.id.btn_manager_res);
        notConnected();
    }
    private void notConnected(){
        llStartSearch.setVisibility(View.VISIBLE);
        ivScan.setVisibility(View.VISIBLE);
        llConnected.setVisibility(View.GONE);
        tvConn.setVisibility(View.INVISIBLE);
    }
    private void connected(){
        llStartSearch.setVisibility(View.GONE);
        ivScan.setVisibility(View.GONE);
        llConnected.setVisibility(View.VISIBLE);
        tvConn.setText("已连接："+ip);
        tvConn.setVisibility(View.VISIBLE);
    }

    public void setConnectedState(String ip,ImageAndVideoEntity entity){
        this.ip = ip;
        this.entity = entity;
        connected();
    }

    public void setConnectClose(){
        tvConn.setText("");
        notConnected();
    }


    private void addListener() {
        tvSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchListener==null) {
                    MyLogger.e(TAG,"must be implements DeviceSearcher OnSearchListener");
                    return;
                }else{
                    startSearch();
                }
            }
        });
        tvStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (materialDialog==null) {
                    String commind = BaseUtils.getStringByResouceId(R.string.dialog_dis_conn_device);
                    materialDialog = new MaterialDialog.Builder(mContext)
                            .cancelable(false)
                            .canceledOnTouchOutside(false)
                            .title(R.string.dialog_title)
                            .content(commind)
                            .positiveText(R.string.dialog_commit)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    ClientByteSocketManager.getInstance().closeRunnable();
                                }
                            })
                            .negativeText(R.string.dialog_cancel)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    materialDialog.dismiss();
                                }
                            })
                            .build();
                }
                materialDialog.show();
            }
        });
        tvManage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToResActivity();
            }
        });
    }

    /**
     * 开始异步搜索局域网中的设备
     */
    private void startSearch(){
        if (DeviceSearcher.isCloseSocket()) {
            DeviceSearcher.search(mSearchListener);
        }else{
            ToastUtils.showToast(mContext,BaseUtils.getStringByResouceId(R.string.search_ing));
        }
    }

    private void jumpToResActivity(){
        if (entity == null) {
            MyLogger.e(TAG,"jumpToResActivity data is error");
            ToastUtils.showToast(mContext,BaseUtils.getStringByResouceId(R.string.data_is_error));
        } else {
            //            MyLogger.i(TAG, "entity.toString() ... " + entity.toString());
            Intent intent = new Intent(mContext, ResourceManagerActivity.class);
            intent.putExtra(Constant.JUMP_RESOURCE_CONTROL, entity);
            ((Activity)mContext).startActivityForResult(intent,Constant.FLAG_JUMP_TO_RES_CONTROL);
        }
    }
}
