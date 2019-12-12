package com.ads.xinfa.ui.main;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.ads.xinfa.UpdateManager;
import com.ads.xinfa.XMLDataOperator;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.FileManager;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.bean.ConfigBean;
import com.ads.xinfa.bean.MyBean;
import com.ads.xinfa.bean.UpdateBean;
import com.ads.xinfa.download.DownLoadObserver;
import com.ads.xinfa.download.DownloadInfo;
import com.ads.xinfa.download.DownloadManager;
import com.ads.xinfa.net.OkHttp3Manager;
import com.ads.xinfa.net.UDPConnectRunnable;
import com.ads.xinfa.utils.ScreenShotUtils;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Ly
 */
public class MainPresenterImpl implements MainContract.MainPresenter{
    private static final String TAG = "MainPresenterImpl";
    private MainContract.MainView mMainView;
    private Context mContext;
    private Handler mHandler;
    private XMLDataOperator mXmlDataOperator;
    private UDPConnectRunnable mUDPConnect;
    private Thread mUDPThread;

    public MainPresenterImpl(MainContract.MainView mMainView,Handler handler) {
        this.mMainView = mMainView;
        this.mContext = mMainView.getContext();
        this.mHandler = handler;
        mXmlDataOperator = new XMLDataOperator(mContext);
        mXmlDataOperator.getDataFromXML();
    }

    @Override
    public void doUDPConnect() {
        mUDPConnect = new UDPConnectRunnable(mHandler);
        mUDPThread = new Thread(mUDPConnect);
        mUDPThread.start();
    }
    public XMLDataOperator getmXmlDataOperator() {
        return mXmlDataOperator;
    }

    /**
     * 获取最新的xml文件
     */
    @Override
    public void downloadFile() {
        ConfigBean configBean = mXmlDataOperator.getConfigBean();
        if (configBean != null) {
            /**
             * 更新xml文件
             * http://192.168.0.2:16888/interface/interface1.aspx?rt=update_ad&es=131
             */
            String url = configBean.getCommands().getCommand().get(0).getUrl() + "&es=" + configBean.getSetting().getConnect().getId();

            Request request = new Request.Builder().url(url).get().build();
            OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (!mMainView.isActive()) {
                        return;
                    }
                    mMainView.downloadFileFail();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    MyLogger.i(TAG,"downloadFile new xml ... "+result);
                    XStream stream = new XStream();
                    stream.processAnnotations(MyBean.class);
                    //更新 MyBean
                    MyBean myBean = (MyBean) stream.fromXML(result);
//                    mXmlDataOperator.setMyBean(myBean);
                    List<String> urlList = getImageOrVideoUrl(myBean);
                    if (!mMainView.isActive()) {
                        return;
                    }
                    downloadByUrl(urlList);
                }
            });
        }
    }

    private List<String> getImageOrVideoUrl(MyBean myBean){

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

    /**
     * 根据Path下载 Image或者Video
     * @param urlList
     */
    private void downloadByUrl(List<String> urlList){
        final int[] downloaderfile = {0};
        mMainView.showDownloadNum(downloaderfile[0] + "/" + urlList.size());
        for (int i = 0; i < urlList.size(); i++) {
            downloaderfile[0] = i + 1;
            mMainView.showDownloadNum(downloaderfile[0] + "/" + urlList.size());
            if (urlList.get(i) == null)
                continue;
            DownloadManager.getInstance().download(urlList.get(i), new DownLoadObserver() {
                @Override
                public void onNext(DownloadInfo value) {
                    super.onNext(value);
                    mMainView.updateProgress(value);
                }

                @Override
                public void onComplete() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (downloadInfo != null) {
                        if (downloaderfile[0] == urlList.size()) {
                            //已经下载完成 然后更换新的xml文件
                            updateXML();
                        }
                    }
                }
            });
        }
    }

    private void updateXML(){
        //更换最新config xml
        ConfigBean configBean = mXmlDataOperator.getConfigBean();
        MyBean myBean = mXmlDataOperator.getMyBean();
        configBean.setGroups(myBean.getGroups());
        XStream stream = new XStream();
        stream.processAnnotations(ConfigBean.class);
        String configString = stream.toXML(configBean);
        if (!mMainView.isActive()) {
            return;
        }
        XStream mStream = new XStream();
        stream.processAnnotations(MyBean.class);
        String dataString = mStream.toXML(myBean);
        String results[] = new String[2];
        results[0] = configString;
        results[1] = dataString;
        mMainView.downloadFileSuccess(results);
    }

    @Override
    public void playView() {
        if (mXmlDataOperator!=null) {
            ViewGroup vg = mXmlDataOperator.getViewGroup();
            if (!mMainView.isActive()) {
                return;
            }
            mMainView.updateView(vg);
        }
    }

    /**
     * 下载apk
     */
    @Override
    public void downloadApk() {
        Request request = new Request.Builder()
                .get()
                .url(mXmlDataOperator.getUrl())
                .build();
        OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                XStream stream = new XStream();
                stream.processAnnotations(UpdateBean.class);
                String mURL = "http://"+mXmlDataOperator.getIp()+":"+mXmlDataOperator.getPort()+result;
                UpdateManager um = new UpdateManager(mContext,mURL,mHandler);
                um.showDownloadDialog();
            }
        });
    }

    /**
     * 上传截图文件
     */
    @Override
    public void uploadFile() {
        ScreenShotUtils.takeScreenShot((Activity) mContext);
        String path = FileManager.screenShotPath;
        File file = new File(path);
        if(file.exists()) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"), file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("ScreenShot", file.getName(), fileBody)
                    .addFormDataPart("c", "comment")
                    .addFormDataPart("a", "add")
                    .addFormDataPart("uid", "1000191")
                    .addFormDataPart("dataid", "1111")
                    .addFormDataPart("message", "你好")
                    .addFormDataPart("datatype", "goodsid")
                    .build();
            Request request = new Request.Builder()
                    .url(mXmlDataOperator.getUrl())
                    .post(requestBody)
                    .build();
            OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    MyLogger.i(TAG,"upload screen shot fail ... "+e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    MyLogger.i(TAG,"upload screen shot success");                }
            });
        }else{
            MyLogger.i(TAG,"file is not fount, path ... "+path);
        }
    }

    @Override
    public void writeTxtToFile(String content, String filePath, String fileName) {
        FileManager.writeTxtToFile(content,FileManager.XML_DIR,FileManager.XML_DATA);
    }

    @Override
    public void clear() {
        if (mUDPConnect!=null) {
            mUDPConnect.close();
        }
    }
}
