package com.jzl.xinfafristversion.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ads.utillibrary.utils.Tools;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gongw.remote.RemoteConst;
import com.jzl.xinfafristversion.R;
import com.jzl.xinfafristversion.base.Constant;
import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.utils.BaseUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CustomMaterialDialog{
    private static final String TAG = "CustomMaterialDialog";
    private Context mContext;
    private MaterialDialog mMaterialDialog;
    private ImageView ivLeft,ivRight,ivMiddle;
    private TextView tvTitle,tvLeft,tvRight,tvMiddle;
    private static final String APK_NAME = Constant.APK_NAME;


    public CustomMaterialDialog(@NotNull Context windowContext) {
        this.mContext = windowContext;
        mMaterialDialog = new MaterialDialog.Builder(windowContext).build();
        init();
    }

    private void init() {
        try {
            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_material,null);
            mMaterialDialog.setCanceledOnTouchOutside(true);
            mMaterialDialog.setCancelable(true);
            WindowManager.LayoutParams params = mMaterialDialog.getWindow().getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = Tools.dpToPx(mContext,600);
            params.height = Tools.dpToPx(mContext,340);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                    );
            mMaterialDialog.addContentView(view, lp);
            ivLeft = view.findViewById(R.id.iv_left);
            ivRight = view.findViewById(R.id.iv_right);
            ivMiddle = view.findViewById(R.id.iv_middle);
            tvTitle = view.findViewById(R.id.tv_title);
            tvLeft = view.findViewById(R.id.tv_left);
            tvRight = view.findViewById(R.id.tv_right);
            tvMiddle = view.findViewById(R.id.tv_middle);
            //设置标题
            tvTitle.setText(BaseUtils.getStringByResouceId(R.string.custom_dialog_main_title));
            //设置左二维码返回数据  ftp://admin:123456@192.168.0.189:2221
            String ftp = "ftp://"+
                    Constant.FTP_USER+":"+
                    Constant.FTP_PWD+"@"+
                    BaseUtils.getHostIP()+":"+
                    Constant.FTP_PORT+
                    File.separator+APK_NAME;
            MyLogger.d(TAG,"ftp url ... "+ftp);
            BaseUtils.showQRCode(ivLeft,ftp);
            //设置右二维码返回数据
            BaseUtils.showQRCode(ivRight,BaseUtils.getHostIP()+":"+ RemoteConst.DEVICE_SEARCH_PORT);
            BaseUtils.showQRCode(ivMiddle,Constant.URL_DOWNLOAD_APK);
            //左边小标题
            tvLeft.setText(BaseUtils.getStringByResouceId(R.string.custom_dialog_left_title));
            //右边小标题
            tvRight.setText(BaseUtils.getStringByResouceId(R.string.custom_dialog_right_title));
            //中间小标题
            tvMiddle.setText(BaseUtils.getStringByResouceId(R.string.custom_dialog_middle_title));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show(){
        if (!mMaterialDialog.isShowing()) {
            mMaterialDialog.show();
        }
    }
    public void dismiss(){
        mMaterialDialog.dismiss();
    }
}
