package com.szty.h5xinfa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import androidx.appcompat.widget.AppCompatButton;

public class DownloadZipDialogManager {

    private android.widget.TextView tvshowprogress;
    private android.widget.ProgressBar progressBar;
    private androidx.appcompat.widget.AppCompatButton btncommit;
    private MaterialDialog materialDialog;

    public DownloadZipDialogManager(Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_download, null);
        this.btncommit = (AppCompatButton) view.findViewById(R.id.btn_commit);
        this.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        this.tvshowprogress = (TextView) view.findViewById(R.id.tv_show_progress);
        builder.customView(view, false);
        builder.cancelable(false);
        builder.canceledOnTouchOutside(false);
        progressBar.setMax(100);
        progressBar.setIndeterminate(false);
        //设置不能点击
        btncommit.setEnabled(false);
        btncommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        materialDialog = builder.build();
    }
    public void showDialog(){
        if (materialDialog!=null) {
            materialDialog.show();
        }
    }
    public void setProgress(int num){
        progressBar.setProgress(num);
    }
    public void setContent(String content){
        tvshowprogress.setText(content);
    }
    public boolean isShowing(){
        if (materialDialog!=null) {
            return materialDialog.isShowing();
        }else{
            return false;
        }
    }
    public void dismiss(){
        if (materialDialog!=null) {
            materialDialog.dismiss();
        }
    }
}
