package com.jzl.xinfafristversion.ui.aboutVideoList;import android.content.Context;import android.graphics.Bitmap;import android.view.View;import android.view.ViewGroup;import android.widget.ImageView;import com.bumptech.glide.Glide;import com.jzl.xinfafristversion.R;import com.jzl.xinfafristversion.XMLDataManager;import com.jzl.xinfafristversion.base.MyLogger;import com.jzl.xinfafristversion.bean.MyBean;import com.jzl.xinfafristversion.utils.Tools;import com.jzl.xinfafristversion.view.ResLoopView;import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;import java.io.File;import java.net.URI;import java.util.ArrayList;import butterknife.BindView;import butterknife.ButterKnife;/** * @author Ly */public class RecyclerItemNormalHolder extends RecyclerItemBaseHolder {    public final static String TAG = "RecyclerItemNormalHolder";    protected Context context = null;    @BindView(R.id.video_player)    //    MyGSYVideoPlayer gsyVideoPlayer;            SampleVideo sampleVideo;    @BindView(R.id.iv_item)    ImageView ivImg;    ImageView thumbIv;    GSYVideoOptionBuilder gsyVideoOptionBuilder;    public RecyclerItemNormalHolder(Context context, View v) {        super(v);        this.context = context;        ButterKnife.bind(this, v);        ivImg.setScaleType(ImageView.ScaleType.FIT_XY);        //设置封面图        thumbIv = new ImageView(context);        gsyVideoOptionBuilder = new GSYVideoOptionBuilder();    }    public void onBind(final int position, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean fileBean, ResLoopView.PlayCompleteCallBack callBack) {        String format = fileBean.getFormat();        MyLogger.i(TAG,"onBind format ... "+format+" position ... "+position);        if (thumbIv.getParent() != null) {            ViewGroup viewGroup = (ViewGroup) thumbIv.getParent();            viewGroup.removeView(thumbIv);        }        //显示视频的界面        if ("视频".equals(fileBean.getFormat())) {            sampleVideo.setVisibility(View.VISIBLE);            ivImg.setVisibility(View.GONE);            displayVideo(position, fileBean,callBack);        }//显示图片的界面        else {            sampleVideo.setVisibility(View.GONE);            ivImg.setVisibility(View.VISIBLE);            String path = fileBean.getPath();//            File file = new File(path);//            if (file.exists()) {//                Glide.with(context).load(fileBean.getPath()).into(ivImg);//            }else{//                Glide.with(context).load(R.mipmap.image_empty).into(ivImg);//            }            if (XMLDataManager.getInstance().isDefaultData()) {                Glide.with(context).load(getDefaultResId(position%3)).into(ivImg);            }else{                Glide.with(context).load(path).into(ivImg);            }        }    }    /**     * 获取显示默认图片的resId 目前只有三张图片为默认轮播图     * @param pos     * @return     */    private int getDefaultResId(int pos){        int resId = R.mipmap.default_img_1;        switch (pos) {            case 0:                resId =  R.mipmap.default_img_1;                break;            case 1:                resId = R.mipmap.default_img_2;                break;            case 2:                resId = R.mipmap.default_img_3;                break;            default:                break;        }        return resId;    }    /**     * 显示图文混播的方法     *     * @param position     * @param fileBean     */    private void displayVideo(int position, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean fileBean, ResLoopView.PlayCompleteCallBack callBack) {        String path = fileBean.getPath();        File file = new File(path);        URI uri = file.toURI();//        MyLogger.i(TAG,"display Video path ... "+path);        thumbIv.setScaleType(ImageView.ScaleType.CENTER_CROP);        //        String localUrl = "/storage/emulated/0/SZTY/FileDownloader/a2.mp4";        Bitmap bitmap = Tools.getLocalVideoBitmap(path);        if (bitmap != null) {            thumbIv.setImageBitmap(bitmap);        } else {            MyLogger.e(TAG, "getLocalVideoBitmap is null");        }        String url = uri.toString();//        String temp = "http://188.131.246.25/test/a1.mp4";        ArrayList<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean> list = new ArrayList<>();        MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean fileBean1 = new MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean();        //如果是本地文件直接用url 因为需要uri转化，如果是网络文件则直接将path加入//        fileBean1.setPath(temp);//        list.add(fileBean1);//        sampleVideo.setUp(list, true, "");        //增加封面        ImageView imageView = new ImageView(context);        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);        imageView.setImageBitmap(bitmap);        GSYVideoOptionBuilder builder = new GSYVideoOptionBuilder();        builder.setThumbImageView(imageView)                .setLooping(false)                .setPlayPosition(position)                .setPlayTag(TAG)                .setUrl(url);        builder.setVideoAllCallBack(new GSYSampleCallBack(){            @Override            public void onAutoComplete(String url, Object... objects) {                super.onAutoComplete(url, objects);                if (callBack!=null) {                    callBack.playComplete(position);                }            }        }).build(sampleVideo);        if (position == 0) {            sampleVideo.startPlayLogic();        }    }    /**     * 全屏幕按键处理     */    private void resolveFullBtn(final StandardGSYVideoPlayer standardGSYVideoPlayer) {        standardGSYVideoPlayer.startWindowFullscreen(context, true, true);    }}