package com.ads.utillibrary.utils;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
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


public class MyProgressbar{
    private Dialog dialog;
    private Context context;
    private ImageView iv;
    private TextView tv;

    public MyProgressbar(Context context) {
        this.context = context;
        init(context);
    }
    //    public MyProgressbar(@NonNull Context context) {
//        super(context);
//        init(context);
//    }
//
//    public MyProgressbar(@NonNull Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        init(context);
//    }
//
//    public MyProgressbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init(context);
//    }

    private void init(Context context){
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.view_load_wait,null);
        iv = view.findViewById(R.id.iv_load);
        tv = view.findViewById(R.id.tv_content);
        tv.setVisibility(View.GONE);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        dialog = new Dialog(context,R.style.float_dialog);
        dialog.setContentView(view,lp);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        //弹出一个窗口，让背后的窗口变暗一点
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.dimAmount=0.6f;

        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = Tools.dpToPx(context,150);
        p.height = Tools.dpToPx(context,130);

    }

    public String getRemindContent(){
        String content = tv.getText().toString();
        if (TextUtils.isEmpty(content)) {
            return "";
        }else{
            return content;
        }
    }

    public void showBar(String content) {
        if (TextUtils.isEmpty(content)) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(content);
            tv.setVisibility(View.VISIBLE);
        }
        startRotate(iv);
        dialog.show();
    }

    public void hideBar() {
        if (dialog != null && dialog.isShowing()) {
            stopRotate(iv);
            dialog.dismiss();
        }
    }

    public boolean progressbarIsShowing(){
        if (dialog!=null && dialog.isShowing()) {
            return true;
        }else{
            return false;
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
