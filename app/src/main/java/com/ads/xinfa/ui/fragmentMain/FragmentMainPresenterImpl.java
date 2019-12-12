package com.ads.xinfa.ui.fragmentMain;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.ads.xinfa.UpdateManager;
import com.ads.xinfa.XMLDataOperator;
import com.ads.xinfa.application.XinFaApplication;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.FileManager;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.bean.MyBean;
import com.ads.xinfa.bean.UpdateBean;
import com.ads.xinfa.net.OkHttp3Manager;
import com.ads.xinfa.net.UDPConnectRunnable;
import com.ads.xinfa.utils.ScreenShotUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FragmentMainPresenterImpl implements FragmentMainContract.FragmentMainPresenter {
    private Object mLock = new Object();
    private static final String TAG = "FragmentMainPresenterIm";
    private FragmentMainContract.FragmentMainView mFragmentMainView;
    private Context mContext;
    private UDPConnectRunnable mUDPConnect;
    private Thread mUDPThread;
    private Handler mHandler;
    private XMLDataOperator mXmlDataOperator;


    public FragmentMainPresenterImpl(FragmentMainContract.FragmentMainView mFragmentMainView, Handler handler) {
        this.mFragmentMainView = mFragmentMainView;
        this.mContext = mFragmentMainView.getContext();
        this.mHandler = handler;
        this.mXmlDataOperator = new XMLDataOperator(mContext);
        this.mXmlDataOperator.getDataFromXML();
//        test();
    }


    /**
     * UDP连接
     */
    @Override
    public void doUDPConnect() {
        mUDPConnect = new UDPConnectRunnable(mHandler);
        mUDPThread = new Thread(mUDPConnect);
        mUDPThread.start();
    }

    /**
     * 关闭UDP socket
     */
    @Override
    public void clear() {
        if (mUDPConnect != null) {
            mUDPConnect.close();
        }
    }

    @Override
    public void downloadFile() {
        /**
         * 更新xml文件
         * http://192.168.0.2:16888/interface/interface1.aspx?rt=update_ad&es=131
         */
        String command = XinFaApplication.COMMAND_HASHMAP.get(Constant.COMMAND_UPDATE);
        String url = command + "&es=" + mXmlDataOperator.getId();

        Request request = new Request.Builder().url(url).get().build();
        OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!mFragmentMainView.isActive()) {
                    return;
                }
                mFragmentMainView.downloadFileFail(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                MyLogger.i(TAG, "downloadFile new xml ... " + result);
                mXmlDataOperator.updateMyBeanData(result);
                if (!mFragmentMainView.isActive()) {
                    return;
                }
                mFragmentMainView.downloadFileSuccess(mXmlDataOperator.getMyBean());
            }
        });
    }

    @Override
    public void playView() {
        if (mXmlDataOperator != null) {
            HashMap<String, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean> areaBean = mXmlDataOperator.getAreanBean();
            if (!mFragmentMainView.isActive()) {
                return;
            }
            if (areaBean == null) {
                return;
            }
            mFragmentMainView.updateView(areaBean);
        }
    }

    @Override
    public void downloadApk() {
        Request request = new Request.Builder().get().url(mXmlDataOperator.getUrl()).build();
        OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MyLogger.e(TAG,"download apk is failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MyLogger.e(TAG,"download apk is success");
                String result = response.body().string();
                XStream stream = new XStream();
                stream.processAnnotations(UpdateBean.class);
                String mURL = "http://" + mXmlDataOperator.getIp() + ":" + mXmlDataOperator.getPort() + result;
                UpdateManager um = new UpdateManager(mContext, mURL, mHandler);
                um.showDownloadDialog();
            }
        });
    }



    @Override
    public void uploadFile() {
        ScreenShotUtils.takeScreenShot((Activity) mContext);
        String path = FileManager.screenShotPath;
        File file = new File(path);
        if (file.exists()) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"), file);
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("ScreenShot", file.getName(), fileBody).addFormDataPart("c", "comment").addFormDataPart("a", "add").addFormDataPart("uid", "1000191").addFormDataPart("dataid", "1111").addFormDataPart("message", "你好").addFormDataPart("datatype", "goodsid").build();
            Request request = new Request.Builder().url(mXmlDataOperator.getUrl()).post(requestBody).build();
            OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    MyLogger.i(TAG, "upload screen shot fail ... " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    MyLogger.i(TAG, "upload screen shot success");
                }
            });
        } else {
            MyLogger.i(TAG, "file is not fount, path ... " + path);
        }
    }
    public void test(){
        downloadImageOrVideo(mXmlDataOperator.getMyBean());
    }

    /**
     * 下载data xml中的 图片 或者 视频
     * @param myBean
     */
    @Override
    public void downloadImageOrVideo(MyBean myBean) {
        List<String> urlList = getImageOrVideoUrl(myBean);
        final int total = urlList.size();
        final int[] current = {0};
        //去下载
        FileDownloader.setup(mContext);
        final FileDownloadListener queueTarget = new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
            }

            @Override
            protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                synchronized (mLock){
                    if ((++current[0]) == total) {
                        MyLogger.i(TAG,"total size ... "+current[0]);
                    }
                }
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
            }

            @Override
            protected void warn(BaseDownloadTask task) {
            }
        };
        for (int i = 0; i < urlList.size(); i++) {
            String url = urlList.get(i);
            String path = "";
            if (url.contains("jpg")||url.contains("jpeg")) {
                path = FileManager.Resource_DIR+i+".jpg";
            }else if (url.contains("mp4")) {
                path = FileManager.Resource_DIR+i+".mp4";
            }
            FileDownloader.getImpl().create(url)
                    // 由于是队列任务, 这里是我们假设了现在不需要每个任务都回调`FileDownloadListener#progress`, 我们只关系每个任务是否完成, 所以这里这样设置可以很有效的减少ipc.
                    .setCallbackProgressTimes(0)
                    .setPath(path)
                    .setListener(queueTarget)
                    .asInQueueTask()
                    .enqueue();
        }
        // 并行执行该队列
        FileDownloader.getImpl().start(queueTarget, false);
    }

    public XMLDataOperator getmXmlDataOperator() {
        return mXmlDataOperator;
    }


    private List<String> getImageOrVideoUrl(MyBean myBean) {
        //存储要显示图片或者视频的url
        List<String> urlList = new ArrayList<>();
        final int size = myBean.getGroups().getGroup().size();
        for (int i = 0; i < size; i++) {
            MyBean.GroupsBean.GroupBean.AreasBean areas = myBean.getGroups().getGroup().get(i).getAreas();
            for (int j = 0; j < areas.getArea().size(); j++) {
                //这两句不理解有什么用  先注释
                //                            urlList.add(areas.getArea().get(b).getInfo().getBgimage());
                //                            urlList.add(areas.getArea().get(b).getInfo().getVoice2());
                String type = areas.getArea().get(j).getInfo().getTname();
                if (!Constant.TYPE_SHOW_MARQUEE.equalsIgnoreCase(type)) {
                    ArrayList<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean> fileBeanList = areas.getArea().get(j).getFiles().getFile();
                    if (fileBeanList != null && fileBeanList.size() > 0) {
                        for (int k = 0; k < fileBeanList.size(); k++) {
                            String path = fileBeanList.get(k).getPath();
                            if (!TextUtils.isEmpty(path)) {
                                urlList.add(path);
                            }
                        }
                    }
                }
            }
        }
        return urlList;
    }
}
