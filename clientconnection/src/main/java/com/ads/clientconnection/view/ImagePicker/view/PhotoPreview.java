package com.ads.clientconnection.view.ImagePicker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ads.clientconnection.R;
import com.ads.clientconnection.utils.GlideTools;
import com.ads.clientconnection.view.ImagePicker.model.PhotoModel;


public class PhotoPreview extends LinearLayout implements OnClickListener {
    boolean is_chat = false;
    private ProgressBar pbLoading;
    private OnClickListener l;
    private ImageView ivContent;
    private View save_bt;
    private String path;
    private Context cxt;
    private Bitmap loadedBitamap;
    private View inflate;

    public PhotoPreview(Context context) {
        super(context);
        this.cxt = context;
        inflate = LayoutInflater.from(context).inflate(
                R.layout.view_photopreview, this, true);

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading_vpp);
        ivContent = (ImageView) findViewById(R.id.iv_content_vpp);
        save_bt = findViewById(R.id.save_bt);
        ivContent.setOnClickListener(this);
        save_bt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
//                ImageUtils.loadImage(cxt, path);
                GlideTools.savePicture(cxt,path);
                Toast.makeText(cxt, "保存成功", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public PhotoPreview(Context context, AttributeSet attrs, int defStyle) {
        this(context);
    }

    public PhotoPreview(Context context, AttributeSet attrs) {
        this(context);
    }

    public void loadImage(PhotoModel photoModel, Boolean is_save, Boolean is_chat) {
        is_chat = false;
        this.is_chat = is_chat;

        if (is_chat) {
            ivContent.setClickable(true);
        } else {
            ivContent.setClickable(false);
        }

        if (is_save) {
            save_bt.setVisibility(View.VISIBLE);
            loadImage("file://head" + photoModel.getOriginalPath());
        } else {
            save_bt.setVisibility(View.GONE);
            loadImage("file://head" + photoModel.getOriginalPath());
        }

    }

    private void loadImage(String path) {
        this.path = path;
        pbLoading.setVisibility(View.GONE);
        try {
            GlideTools.setNormalImage(cxt,path,ivContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.l = l;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_content_vpp && l != null) {
            l.onClick(ivContent);
        }
    }

}
