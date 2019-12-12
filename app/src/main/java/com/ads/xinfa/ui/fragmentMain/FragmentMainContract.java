package com.ads.xinfa.ui.fragmentMain;

import com.ads.xinfa.base.MvpBasePresenter;
import com.ads.xinfa.base.MvpBaseView;
import com.ads.xinfa.bean.MyBean;

import java.util.HashMap;

public class FragmentMainContract {
    public interface FragmentMainView extends MvpBaseView<FragmentMainPresenter>{
        void updateView(HashMap<String, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean> map);
        //下载成功xml文件会继续下载图片或者视频文件
        void downloadFileSuccess(MyBean bean);
        void downloadFileFail(String str);
        void downloadImageAndVideoFail();
        void downloadImageAndVideoSuccess();
    }

    public interface FragmentMainPresenter extends MvpBasePresenter {
        //开启UDP连接
        void doUDPConnect();
        //关闭UDP
        void clear();
        //下载文件
        void downloadFile();
        //播放视图
        void playView();
        //下载apk
        void downloadApk();
        //上传图片
        void uploadFile();
        //下载图片或者视频
        void downloadImageOrVideo(MyBean myBean);

    }

}
