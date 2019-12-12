package com.ads.clientconnection.utils;


import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ads.clientconnection.R;
import com.ads.clientconnection.base.MyLogger;
import com.ads.clientconnection.view.ImagePicker.util.ImageUtils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;


public class GlideTools {
    private static final String TAG = "GlideTools";

    /**
     * 设置圆形头像
     * @param context
     * @param url
     * @param iv
     */
    public static void setRoundHeadImage(Context context, String url, ImageView iv){
//        if (TextUtils.isEmpty(url)) {
//            String name = context.getClass().getName();
//            MyLogger.e(TAG," ******"+name+":setRoundHeadImage url is null ******");
//            return;
//        }else{
//            Glide.with(context)
//                    .load(url)
//                    .centerCrop()
//                    .placeholder(R.mipmap.membership_default_header)
//                    .error(R.mipmap.membership_default_header)
//                    .bitmapTransform(new CropCircleTransformation(context))
//                    .crossFade(1000)
//                    .into(iv);
//        }
    }

    /**
     * 显示普通正方形图片
     * @param context
     * @param url
     * @param iv
     */
    public static void setNormalImage(Context context, String url, ImageView iv){
        if (TextUtils.isEmpty(url)) {
            String name = context.getClass().getName();
            MyLogger.e(TAG," ******"+name+":setNormalImage url is null ******");
            return;
        }else{
            Glide.with(context)
                    .load(url)
                    .into(iv);
        }
    }
    /**
     * 显示普通正方形图片
     * @param context
     * @param resId
     * @param iv
     */
    public static void setNormalImage(Context context, int resId, ImageView iv){
        Glide.with(context).load(resId).into(iv);

    }


    /**
     * 显示图片验证码
     * @param context
     * @param url
     * @param iv
     */
    public static void setImageCodePic(Context context, String url, ImageView iv){
        if (TextUtils.isEmpty(url)) {
            String name = context.getClass().getName();
            MyLogger.e(TAG," ******"+name+":setNormalImage url is null ******");
            return;
        }else{
            Glide.with(context)
                    .load(url)
                    .placeholder(R.mipmap.image_code_error)
                    .error(R.mipmap.image_code_error)
                    .into(iv);
        }
    }

    //Glide保存图片
    public static void savePicture(Context mContext, String url){

//        Glide.with(mContext).load(url).asBitmap().toBytes().into(new SimpleTarget<byte[]>() {
//            @Override
//            public void onResourceReady(byte[] bytes, <? super byte[]> glideAnimation) {
//                try {
//                    savaFileToSD(mContext,bytes);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }
    //往SD卡写入文件的方法
    private static void savaFileToSD(Context mContext, byte[] bytes) throws Exception {
        File image_file;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            image_file = new File(
                    Environment.getExternalStorageDirectory() + "/imagepicker",
                    (Math.round((Math.random() * 9 + 1) * 100000))
                            + ".png");

        } else {
            image_file = new File(
                    "/data/data/com.ybej.wallet/imagepicker",
                    (Math.round((Math.random() * 9 + 1) * 100000))
                            + ".png");
            if (!image_file.exists()) {
                image_file.mkdirs();
            }
        }
        //这里就不要用openFileOutput了,那个是往手机内存中写数据的
        FileOutputStream output = new FileOutputStream(image_file);
        output.write(bytes);
        //将bytes写入到输出流中
        output.flush();
        output.close();
        ImageUtils.scanPhoto(mContext, image_file.getAbsolutePath());  //刷新到相册
    }
}
