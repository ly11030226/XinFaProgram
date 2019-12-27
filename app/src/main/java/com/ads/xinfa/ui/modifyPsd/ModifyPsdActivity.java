package com.ads.xinfa.ui.modifyPsd;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.ads.utillibrary.utils.MyDialog;
import com.ads.utillibrary.utils.ToastUtils;
import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.SettingManager;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 修改连接机器的密码
 * @author Ly
 */
public class ModifyPsdActivity extends BaseActivity{

    @BindView(R.id.et_old)
    EditText etOld;
    @BindView(R.id.et_new)
    EditText etNew;
    @BindView(R.id.et_new1)
    EditText etNew1;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.btn_commit)
    Button btnCommit;
    MyDialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motify_psd);
        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ButterKnife.bind(this);
        try {
            myDialog = new MyDialog(ModifyPsdActivity.this,R.style.float_dialog);

            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            btnCommit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkPsd();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查输入的密码
     */
    private void checkPsd() {
        String oldStr = etOld.getText().toString().trim();
        String newStr = etNew.getText().toString().trim();
        String new1Str = etNew1.getText().toString().trim();
        if (TextUtils.isEmpty(oldStr)) {
            ToastUtils.showToast(ModifyPsdActivity.this,"请输入旧密码");
            return;
        }
        if (TextUtils.isEmpty(newStr)) {
            ToastUtils.showToast(ModifyPsdActivity.this,"请输入新密码");
            return;
        }
        if (TextUtils.isEmpty(new1Str)) {
            ToastUtils.showToast(ModifyPsdActivity.this,"请再次输入新密码");
            return;
        }
        if (!oldStr.equals(RemoteConst.CONNECT_PSD)) {
            ToastUtils.showToast(ModifyPsdActivity.this,"输入旧密码有误");
            return;
        }
        if (!newStr.equals(new1Str)) {
            ToastUtils.showToast(ModifyPsdActivity.this,"两次输入的新秘密不一致");
            return;
        }
        if (oldStr.equals(newStr)) {
            ToastUtils.showToast(ModifyPsdActivity.this,"新密码与旧密码相同");
            return;
        }
        myDialog.showDialog("请稍后 ... ");
        //修改本地文件
        SettingManager.getInstance().modifyPsd(ModifyPsdActivity.this,newStr,new MyHandler(ModifyPsdActivity.this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDialog = null;
    }

    static class MyHandler extends Handler{
        private final WeakReference<ModifyPsdActivity> activity;
        public MyHandler(ModifyPsdActivity activity) {
            super();
            this.activity = new WeakReference<ModifyPsdActivity>(activity);
    }

        @Override
        public void handleMessage(@NonNull Message msg) {
            ModifyPsdActivity modifyPsdActivity = activity.get();
            if (modifyPsdActivity!=null) {
                try {
                    modifyPsdActivity.myDialog.hideDialog();
                    if (msg.what == RemoteConst.MODIFY_PSD_SUCCESS) {
                        ToastUtils.showToast(modifyPsdActivity,"修改密码成功");
                        modifyPsdActivity.finish();
                    }else if (msg.what == RemoteConst.MODIFY_PSD_FAIL) {
                        ToastUtils.showToast(modifyPsdActivity,"修改密码失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
