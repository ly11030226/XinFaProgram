package com.ads.xinfa.ui.aboutVideoList;import android.content.Context;import android.graphics.Bitmap;import android.net.Uri;import android.view.View;import android.view.ViewGroup;import android.widget.ImageView;import com.ads.xinfa.R;import com.ads.xinfa.base.FileManager;import com.ads.xinfa.entity.ImageAndVideoEntity;import com.ads.xinfa.ui.displayVideoAndImage.DisplayVideoAndImageFragment;import com.ads.xinfa.utils.Tools;import com.bumptech.glide.Glide;import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;import java.io.File;import java.net.URI;import butterknife.BindView;import butterknife.ButterKnife;/** * @author Ly */public class RecyclerItemNormalHolder extends RecyclerItemBaseHolder {    public final static String TAG = "RecyclerItemNormalHolder";    protected Context context = null;    @BindView(R.id.video_player)    //    MyGSYVideoPlayer gsyVideoPlayer;            SampleVideo sampleVideo;    @BindView(R.id.iv_item)    ImageView ivImg;    ImageView thumbIv;    GSYVideoOptionBuilder gsyVideoOptionBuilder;    public RecyclerItemNormalHolder(Context context, View v) {        super(v);        this.context = context;        ButterKnife.bind(this, v);        ivImg.setScaleType(ImageView.ScaleType.FIT_XY);        //设置封面图        thumbIv = new ImageView(context);        gsyVideoOptionBuilder = new GSYVideoOptionBuilder();    }    public void onBind(boolean isOne, final int position, ImageAndVideoEntity.FileEntity videoModel, DisplayVideoAndImageFragment.PlayCompleteCallBack callBack) {        if (thumbIv.getParent() != null) {            ViewGroup viewGroup = (ViewGroup) thumbIv.getParent();            viewGroup.removeView(thumbIv);        }        //显示视频的界面        if ("视频".equals(videoModel.getFormat())) {            sampleVideo.setVisibility(View.VISIBLE);            ivImg.setVisibility(View.GONE);            displayVideo(isOne, position, videoModel, callBack);        }//显示图片的界面        else {            sampleVideo.setVisibility(View.GONE);            ivImg.setVisibility(View.VISIBLE);            String path = FileManager.UPLOAD_DIR + videoModel.getName();            File file = new File(path);            if (file.exists()) {                Glide.with(context).load(path).into(ivImg);            } else {                Glide.with(context).load(R.mipmap.image_empty).into(ivImg);            }        }    }    /**     * 显示图文混播的方法     *     * @param position     * @param videoModel     */    private void displayVideo(boolean isOne, int position, ImageAndVideoEntity.FileEntity videoModel, DisplayVideoAndImageFragment.PlayCompleteCallBack callBack) {        String path = FileManager.UPLOAD_DIR + videoModel.getName();        File file = new File(path);        URI uri = file.toURI();        //        MyLogger.i(TAG,"display Video path ... "+path);        thumbIv.setScaleType(ImageView.ScaleType.CENTER_CROP);        //        String localUrl = "/storage/emulated/0/SZTY/FileDownloader/a2.mp4";        Uri uri1;        Bitmap bitmap = Tools.getLocalVideoBitmap(path);        //        if (bitmap != null) {        //            thumbIv.setImageBitmap(bitmap);        //        } else {        //            MyLogger.e(TAG, "getLocalVideoBitmap is null");        //        }        String url = uri.toString();        //        ArrayList<ImageAndVideoEntity.FileEntity> list = new ArrayList<>();        //        ImageAndVideoEntity.FileEntity entitty = new ImageAndVideoEntity.FileEntity();        //        entitty.setPath(url);        //        list.add(entitty);        //        sampleVideo.setUp(list, true, "");        //增加封面        ImageView imageView = new ImageView(context);        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);        imageView.setImageBitmap(bitmap);        sampleVideo.setThumbImageView(imageView);        sampleVideo.setLooping(false);        sampleVideo.setPlayTag(TAG);        sampleVideo.setPlayPosition(position);        //获取自动播放完毕的接口        gsyVideoOptionBuilder.setUrl(url).setVideoAllCallBack(new GSYSampleCallBack() {            @Override            public void onAutoComplete(String url, Object... objects) {                if (callBack != null) {                    callBack.playComplete(position);                }            }        }).build(sampleVideo);        if (position == 0) {            if (isOne) {                sampleVideo.setLooping(true);            }            sampleVideo.startPlayLogic();        }    }    /**     * 全屏幕按键处理     */    private void resolveFullBtn(final StandardGSYVideoPlayer standardGSYVideoPlayer) {        standardGSYVideoPlayer.startWindowFullscreen(context, true, true);    }}