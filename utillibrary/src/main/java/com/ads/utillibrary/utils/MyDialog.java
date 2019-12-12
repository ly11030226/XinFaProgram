package com.ads.utillibrary.utils;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.ads.utillibrary.R;

import androidx.annotation.NonNull;

public class MyDialog extends Dialog {
    private Context context;
    private ImageView iv;
    private TextView tv;
    private WindowManager.LayoutParams params;
    public MyDialog(@NonNull Context context) {
        this(context,R.style.float_dialog);
        this.context = context;
        init();
    }

    public MyDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        init();
    }

    public void setContent(String content){
        tv.setText(content);
    }

    private void init(){
        View view = LayoutInflater.from(context).inflate(R.layout.view_load_wait,null);
        iv = view.findViewById(R.id.iv_load);
        tv = view.findViewById(R.id.tv_content);
        tv.setVisibility(View.GONE);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        setContentView(view,lp);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        //弹出一个窗口，让背后的窗口变暗一点
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        params = getWindow().getAttributes();
        params.dimAmount=0.6f;
        params.gravity = Gravity.CENTER;
        params.width = Tools.dpToPx(context,160);
        params.height = Tools.dpToPx(context,130);
    }


    public String getRemindContent(){
        String content = tv.getText().toString();
        if (TextUtils.isEmpty(content)) {
            return "";
        }else{
            return content;
        }
    }

    public void showDialog(String content) {
        if (TextUtils.isEmpty(content)) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(content);
            tv.setVisibility(View.VISIBLE);
        }
        startRotate(iv);
        show();
    }

    public void hideDialog() {
        if (isShowing()) {
            stopRotate(iv);
            dismiss();
        }
    }

    /**
     * 开启动画
     */
    private void startRotate(ImageView iv) {
        Animation operatingAnim = AnimationUtils.loadAnimation(context, R.anim.load_wait);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (operatingAnim != null) {
            iv.startAnimation(operatingAnim);
        }
    }

    /**
     * 关闭动画
     */
    private void stopRotate(ImageView iv) {
        iv.clearAnimation();
    }

}
