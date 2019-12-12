package com.jzl.xinfafristversion.ui.main;

import com.jzl.xinfafristversion.base.MvpBasePresenter;
import com.jzl.xinfafristversion.base.MvpBaseView;
import com.jzl.xinfafristversion.download.DownloadInfo;
import com.jzl.xinfafristversion.view.ResViewGroup;


public class MainContract {

    public interface MainView extends MvpBaseView<MainPresenter> {
        void showDialog();
        void hideDialog();
//        void updateView(ViewGroup vg);
        void showDownloadNum(String num);
        void updateProgress(DownloadInfo value);
        void downloadFileSuccess(String[] results);
        void downloadFileFail();
        void setDefaultData();
    }
    public interface MainPresenter extends MvpBasePresenter {
        //开启UDP连接
        void doUDPConnect();
        //下载文件
        void downloadFile();
        //播放视图
        void playView(ResViewGroup resViewGroup);
        //下载apk
        void downloadApk(String url);
        //上传图片
        void uploadFile();
        //将下载的文件写入File
        void writeTxtToFile(String content, String filePath, String fileName);
        //做注销操作
        void clear();
    }
}
