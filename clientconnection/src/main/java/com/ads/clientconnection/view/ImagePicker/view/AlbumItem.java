package com.ads.clientconnection.view.ImagePicker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ads.clientconnection.R;
import com.ads.clientconnection.utils.GlideTools;
import com.ads.clientconnection.view.ImagePicker.model.AlbumModel;


/**
 * Created by fengyongge on 2016/5/24
 */
public class AlbumItem extends LinearLayout {
    private ImageView ivAlbum;
    private ImageView ivIndex;
    private TextView tvName, tvCount;
    private Context context;

    public AlbumItem(Context context) {
        this(context, null);
    }

    public AlbumItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_album, this, true);
        this.context = context;
        ivAlbum = (ImageView) findViewById(R.id.iv_album_la);
        ivIndex = (ImageView) findViewById(R.id.iv_index_la);
        tvName = (TextView) findViewById(R.id.tv_name_la);
        tvCount = (TextView) findViewById(R.id.tv_count_la);
    }

    public AlbumItem(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    /**
     * 设置相册封面
     */
    public void setAlbumImage(String path) {
        try {
            GlideTools.setNormalImage(context,path,ivAlbum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    public void update(AlbumModel album) {
        setAlbumImage(album.getRecent());
        setName(album.getName());
        setCount(album.getCount());
        isCheck(album.isCheck());
    }

    public void setName(CharSequence title) {
        tvName.setText(title);
    }

    public void setCount(int count) {
        tvCount.setHint(count + "张");
    }

    public void isCheck(boolean isCheck) {
        if (isCheck)
            ivIndex.setVisibility(View.VISIBLE);
        else
            ivIndex.setVisibility(View.GONE);
    }

}
