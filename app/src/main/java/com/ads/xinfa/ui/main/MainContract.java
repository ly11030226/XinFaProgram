package com.ads.xinfa.ui.main;

import android.view.ViewGroup;

import com.ads.xinfa.base.MvpBasePresenter;
import com.ads.xinfa.base.MvpBaseView;
import com.ads.xinfa.download.DownloadInfo;

public class MainContract {

    public interface MainView extends MvpBaseView<MainPresenter>{
        void showDialog();
        void hideDialog();
        void updateView(ViewGroup vg);
        void showDownloadNum(String num);
        void updateProgress(DownloadInfo value);
        void downloadFileSuccess(String[] results);
        void downloadFileFail();
    }
    public interface MainPresenter extends MvpBasePresenter{
        //开启UDP连接
        void doUDPConnect();
        //下载文件
        void downloadFile();
        //播放视图
        void playView();
        //下载apk
        void downloadApk();
        //上传图片
        void uploadFile();
        //将下载的文件写入File
        void writeTxtToFile(String content, String filePath, String fileName);
        //做注销操作
        void clear();
    }
}
