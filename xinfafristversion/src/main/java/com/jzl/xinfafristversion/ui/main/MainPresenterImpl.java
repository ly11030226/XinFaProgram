package com.jzl.xinfafristversion.ui.main;


import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;

import com.jzl.xinfafristversion.R;
import com.jzl.xinfafristversion.XMLDataManager;
import com.jzl.xinfafristversion.base.Constant;
import com.jzl.xinfafristversion.base.FileManager;
import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.bean.ConfigBean;
import com.jzl.xinfafristversion.bean.MyBean;
import com.jzl.xinfafristversion.net.OkHttp3Manager;
import com.jzl.xinfafristversion.net.UDPConnectRunnable;
import com.jzl.xinfafristversion.utils.BaseUtils;
import com.jzl.xinfafristversion.utils.ScreenShotUtils;
import com.jzl.xinfafristversion.utils.ToastUtils;
import com.jzl.xinfafristversion.view.ResViewGroup;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private UDPConnectRunnable mUDPConnect;
    private Thread mUDPThread;

    public static final String downloadFilePath = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + "SZTY" + File.separator + "FileDownloader" + File.separator;

    public MainPresenterImpl(MainContract.MainView mMainView,Handler handler) {
        this.mMainView = mMainView;
        this.mContext = mMainView.getContext();
        this.mHandler = handler;
        XMLDataManager.getInstance().getDataFromXML(mContext);
        if (XMLDataManager.getInstance().isDefaultData()) {
            this.mMainView.setDefaultData();
        }
    }

    @Override
    public void doUDPConnect() {
        mUDPConnect = new UDPConnectRunnable(mHandler);
        mUDPThread = new Thread(mUDPConnect);
        mUDPThread.start();
    }

    /**
     * 获取最新的xml文件
     */
    @Override
    public void downloadFile() {
        ConfigBean configBean = XMLDataManager.getInstance().getConfigBean();
        if (configBean != null) {
            ArrayList<ConfigBean.Commands.Command> commands = configBean.getCommands().getCommand();
            String url = "";
            for (int i = 0; i < commands.size(); i++) {
                ConfigBean.Commands.Command command = commands.get(i);
                if (command.getType().contains(Constant.METHOD_UPDATE_XML)) {
                    url = command.getUrl();
                    break;
                }
            }
            if (!TextUtils.isEmpty(url)) {
                /**
                 * 更新xml文件
                 * http://192.168.0.2:16888/interface/interface1.aspx?rt=update_ad&es=131
                 */
                url = url + "&es=" + configBean.getSetting().getConnect().getId();
                MyLogger.i(TAG,"downloadFile url ... "+url);
                Request request = new Request.Builder().url(url).get().build();
                OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (!mMainView.isActive()) {
                            return;
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mMainView.downloadFileFail();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        MyLogger.i(TAG,"downloadFile new xml ... "+result);
                        XStream stream = new XStream();
                        stream.processAnnotations(MyBean.class);
                        //更新 MyBean
                        MyBean myBean = (MyBean) stream.fromXML(result);
                        if (!mMainView.isActive()) {
                            return;
                        }
                        /**
                         * 下载所有文件
                         */
                        downloadAllFile(myBean);
                    }
                });
            }
        }
    }

    private void downloadAllFile(MyBean myBean) {
        List<String> urlList = getImageOrVideoUrl(myBean);
        final FileDownloadListener queueTarget = new FileDownloadListener() {
            private volatile int num;
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
                if (++num == urlList.size()) {
                    updateDataAndXml(myBean);
                }
//                MyLogger.i(TAG,"completed num ... "+num);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                if (++num == urlList.size()) {
                    updateDataAndXml(myBean);
                }
//                MyLogger.i(TAG,"completed num ... "+num);
//                MyLogger.i(TAG,"downloadAllFile error ... "+e.getMessage());
            }


            @Override
            protected void warn(BaseDownloadTask task) {
            }
        };
        for (String url : urlList) {
            String localPath = getLocalPathByUrl(url);
//            MyLogger.i(TAG,"url1 ... "+url);
            try {
                url = URLEncoder.encode(url, "UTF-8").replaceAll("\\+","%20");
//                MyLogger.i(TAG,"url2 ... "+url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            url = url.replaceAll("%3A", ":").replaceAll("%2F", "/");
//            MyLogger.i(TAG,"url3 ... "+url);
            FileDownloader.setup(mContext);
            FileDownloader.getImpl()
                    .create(url)
                    .setPath(localPath)
                    .setCallbackProgressTimes(0) // 由于是队列任务, 这里是我们假设了现在不需要每个任务都回调`FileDownloadListener#progress`, 我们只关系每个任务是否完成, 所以这里这样设置可以很有效的减少ipc.
                    .setListener(queueTarget)
                    .asInQueueTask()
                    .enqueue();
        }
        FileDownloader.getImpl().start(queueTarget, false);
    }

    private void updateDataAndXml(MyBean myBean) {
//        XMLDataManager.getInstance().getMyBean().setPage(myBean.getPage());
//        XMLDataManager.getInstance().getMyBean().setGroups(myBean.getGroups());

        XMLDataManager.getInstance().setMyBean(myBean);
        //更换最新config xml
        ConfigBean configBean = XMLDataManager.getInstance().getConfigBean();
        configBean.setGroups(myBean.getGroups());
        XStream stream = new XStream();
        stream.processAnnotations(ConfigBean.class);
        String configString = stream.toXML(configBean);
        if (!mMainView.isActive()) {
            return;
        }
        XStream mStream = new XStream();
        mStream.processAnnotations(MyBean.class);
        String dataString = mStream.toXML(myBean);
        String results[] = new String[2];
        results[0] = configString;
        results[1] = dataString;
        mMainView.downloadFileSuccess(results);
    }

    private String getLocalPathByUrl(String url){
        String str[] = url.split("/");
        String name = str[str.length - 1];
        return downloadFilePath+name;
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
                                //将本地的目录作为path
                                fileBeanList.get(k).setPath(getLocalPathByUrl(path));
                            }
                        }
                    }
                }
            }
        }
        return urlList;
    }


    private void updateXML(){
        //更换最新config xml
        ConfigBean configBean = XMLDataManager.getInstance().getConfigBean();
        MyBean myBean = XMLDataManager.getInstance().getMyBean();
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
    public void playView(ResViewGroup resViewGroup) {
        XMLDataManager.getInstance().showData(resViewGroup);
    }

    /**
     * 下载apk
     * @param url 下载地址
     * 例http://192.168.0.149:16999/interface/interface1.aspx?rt=update&es=131
     */
    @Override
    public void downloadApk(String url) {
        MyLogger.i(TAG,"downloadApk url ... "+url);
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                MyLogger.i(TAG,"result ... "+result);
//                XStream stream = new XStream();
//                stream.processAnnotations(UpdateBean.class);
//                String mURL = "http://"+XMLDataManager.ip+":"+XMLDataManager.port+result;
//                UpdateManager um = new UpdateManager(mContext,mURL,mHandler);
//                um.showDownloadDialog();
            }
        });
    }

    /**
     * 上传截图文件
     */
    @Override
    public void uploadFile() {
        File file = new File(FileManager.screenShotPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String path = FileManager.screenShotPath + System.currentTimeMillis()+".jpg";
        ScreenShotUtils.savePic(ScreenShotUtils.takeScreenShot((Activity) mContext),path);
        file = new File(path);
        if(file.exists()) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"), file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("ScreenShot", file.getName(), fileBody)
//                    .addFormDataPart("c", "comment")
//                    .addFormDataPart("a", "add")
//                    .addFormDataPart("uid", "1000191")
//                    .addFormDataPart("dataid", "1111")
//                    .addFormDataPart("message", "你好")
//                    .addFormDataPart("datatype", "goodsid")
                    .build();
            Request request = new Request.Builder()
                    .url(XMLDataManager.url)
                    .post(requestBody)
                    .build();
            OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    MyLogger.e(TAG,"upload screen shot fail ... "+e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    MyLogger.i(TAG,"upload screen shot success");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showToast(mContext,BaseUtils.getStringByResouceId(R.string.upload_img_success));
                        }
                    });
                }
            });
        }else{
            MyLogger.e(TAG,"file is not fount, path ... "+path);
        }
    }

    @Override
    public void writeTxtToFile(String content, String filePath, String fileName) {
        FileManager.writeTxtToFile(content,FileManager.XML_DIR,fileName);
    }

    @Override
    public void clear() {
        if (mUDPConnect!=null) {
            mUDPConnect.close();
        }
    }
}
