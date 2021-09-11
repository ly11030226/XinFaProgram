package com.szty.h5xinfa.ui;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.widget.ProgressBar;

import com.ads.utillibrary.utils.ToastUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.szty.h5xinfa.Constant;
import com.szty.h5xinfa.DownloadZipDialogManager;
import com.szty.h5xinfa.IReplaceType;
import com.szty.h5xinfa.NormalReplaceImpl;
import com.szty.h5xinfa.R;
import com.szty.h5xinfa.XmlManager;
import com.szty.h5xinfa.model.ConfigBean;
import com.szty.h5xinfa.model.UpdateBean;
import com.szty.h5xinfa.model.UpgradeBean;
import com.szty.h5xinfa.updateResponse.BaseUpdateResponse;
import com.szty.h5xinfa.updateResponse.NormalUpdateResponseImpl;
import com.szty.h5xinfa.util.BaseUtils;
import com.szty.h5xinfa.util.ZipUtil;

import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 利用了一款开源的web引擎 Crosswalk
 * @author Ly
 */
public class MainCrosswalkActivity extends XWalkActivity {
    private static final String TAG = "MainCrosswalkActivity";
    ConfigBean configBean;
    private XWalkView mXWalkView;
    private OkHttpClient okHttpClient;
    private DownloadZipDialogManager downloadZipDialogManager;
    private ProgressBar pb;
    private Handler handler;
    //轮询间隔时间
    private static final int POLLING_TIME = 20 * 1000;
    private BaseUpdateResponse baseUpdateResponse;
    //是否是更新文件操作
    private static boolean isUpdate;
    //获取发送心跳url
    private String heartBeatUrl;
    //获取发送心跳间隔
    private int sendHeartbeatTime;
    //默认发送心跳间隔
    private static final int DEFAULT_HEARTBEAT_TIME = 40 * 1000;

    @Override
    protected void onXWalkReady() {
        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);   //开启默认动画
        XWalkSettings xWalkSettings = mXWalkView.getSettings();
        xWalkSettings.setLoadWithOverviewMode(false);
        xWalkSettings.setJavaScriptEnabled(true);               //支持js
        xWalkSettings.setJavaScriptCanOpenWindowsAutomatically(true);    //支持通过JS打开新窗口
        xWalkSettings.setUseWideViewPort(true);    //将图片调整到合适webview的大小
        xWalkSettings.setLoadWithOverviewMode(true);     //缩放至屏幕的大小
        xWalkSettings.setLoadsImagesAutomatically(true);   //支持自动加载图片
        xWalkSettings.setSupportMultipleWindows(true);    //支持多窗口
        xWalkSettings.setSupportZoom(true);
        xWalkSettings.setAllowFileAccess(true);
        xWalkSettings.setDomStorageEnabled(true);
        xWalkSettings.setAllowContentAccess(true);
        xWalkSettings.setDomStorageEnabled(true);
        mXWalkView.requestFocus();
        xWalkSettings.setCacheMode(XWalkSettings.LOAD_NO_CACHE);
        mXWalkView.setResourceClient(new XWalkResourceClient(mXWalkView){
            @Override
            public void onLoadStarted(XWalkView view, String url) {
                super.onLoadStarted(view, url);
            }

            @Override
            public void onLoadFinished(XWalkView view, String url) {
                super.onLoadFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
                if (view!=null) {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
                if (callback!=null) {
                    callback.onReceiveValue(true);
                }
                super.onReceivedSslError(view,callback,error);
            }

            @Override
            public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
                super.onReceivedLoadError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onProgressChanged(XWalkView view, int progressInPercent) {
                super.onProgressChanged(view, progressInPercent);
            }
        });
        mXWalkView.setUIClient(new XWalkUIClient(mXWalkView){
            @Override
            public boolean onJsAlert(XWalkView view, String url, String message, XWalkJavascriptResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public void onReceivedTitle(XWalkView view, String title) {
                super.onReceivedTitle(view, title);
            }

            @Override
            public void openFileChooser(XWalkView view, ValueCallback<Uri> uploadFile, String acceptType, String capture) {
                super.openFileChooser(view, uploadFile, acceptType, capture);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_crosswalk);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        try {
            okHttpClient = new OkHttpClient();
            initData();
            // ../szty/page/index.html 文件不存在 则加载assets下面的index.html文件
            readLocalH5();
            initIntentData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initIntentData() {
        if (getIntent() != null && getIntent().hasExtra(Constant.KEY_HANDLE_CODE)) {
            int handleCode = getIntent().getIntExtra(Constant.KEY_HANDLE_CODE, 0);
            switch (handleCode) {
                case Constant.FILE_NOT_EXIST:
                    ToastUtils.showToast(MainCrosswalkActivity.this, getString(R.string.config_file_not_exist));
                    break;
                case Constant.LOAD_XML_ERROR:
                    ToastUtils.showToast(MainCrosswalkActivity.this, getString(R.string.read_config_file_error));
                    break;
                case Constant.LOAD_XML_FINISH:
                    configBean = XmlManager.getInstance().getConfigBean();
                    break;
                default:
                    break;
            }
            if (configBean == null) {
                return;
            }
            requestNetWort();
        }
    }

    private void requestNetWort() {
        //设置心跳数据
        setHeartbeatData();
        baseUpdateResponse = new NormalUpdateResponseImpl(configBean);
        //查询是否有升级
        handler.sendEmptyMessageDelayed(Constant.REQUEST_UPDATE, POLLING_TIME);
        //发送心跳包
        handler.sendEmptyMessageDelayed(Constant.SEND_HEARTBEAT, sendHeartbeatTime);
    }

    /**
     * 设置心跳数据
     */
    private void setHeartbeatData() {
        heartBeatUrl = getSendHeartbeatUrl();
        String heartTime = configBean.getHeartTime();
        if (TextUtils.isEmpty(heartTime)) {
            sendHeartbeatTime = DEFAULT_HEARTBEAT_TIME;
        } else {
            try {
                sendHeartbeatTime = Integer.valueOf(heartTime) * 1000;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                sendHeartbeatTime = DEFAULT_HEARTBEAT_TIME;
            }
        }
    }

    private String getHeartbeatUrl() {
        return heartBeatUrl;
    }

    private int getHeartbeatTime() {
        return sendHeartbeatTime;
    }

    private void initData() {
        pb = findViewById(R.id.progress);
        pb.setVisibility(View.GONE);
        handler = new PollingHandler(MainCrosswalkActivity.this);
    }

    private void readLocalH5() throws MalformedURLException, URISyntaxException {
        String p = MainCrosswalkActivity.this.getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath();
        //获取 android/data/packagename/files/szty/page 目录
        File file = new File(p, Constant.PATH_PAGE);
        if (file.exists()) {
            //如果 ../szty/page 目录存在 则查询../szty/page/index.html 是否存在
            File f = new File(file, Constant.HTML_INDEX);
            if (f.exists()) {
                //如果index.html存在，则webView加载
                String url = "file://" + f.getAbsolutePath();
                Log.i(TAG, "readLocalH5: url ... " + url);
                mXWalkView.loadUrl(url);
            } else {
                //如果 ../szty/page/index.html不存在，则加载assets下的index.html
                mXWalkView.loadUrl("file:///android_asset/index.html");
            }
        } else {
            //如果 ../szty/page 不存在 则创建page文件夹，然后加载assets下的index.html
            file.mkdirs();
            mXWalkView.loadUrl("file:///android_asset/index.html");
        }
    }


    /**
     * 请求是否有更新文件
     *
     * @param url
     * @param baseUpdateResponse
     */
    private void requestUpdate(String url, BaseUpdateResponse baseUpdateResponse) {
        final Request request = new Request.Builder().url(url).get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: ");
                handler.sendEmptyMessageDelayed(Constant.REQUEST_UPGRADE, POLLING_TIME);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //返回格式   id|../../..|length ===》  id|zip路径|zip大小
                String responseStr = response.body().string();
                //Log.i(TAG, "requestUpdate onResponse: " + responseStr);
                UpdateBean updateBean = baseUpdateResponse.getUpdateBean(responseStr);
                String url = baseUpdateResponse.getUrl(updateBean.getFilepath());
                String id = String.valueOf(updateBean.getId());
                if (!TextUtils.isEmpty(url)) {
                    isUpdate = true;
                    //下载zip文件
                    downloadFile(url, id);
                } else {
                    Log.d(TAG, "没有升级文件", null);
                    handler.sendEmptyMessageDelayed(Constant.REQUEST_UPGRADE, POLLING_TIME);
                }
            }
        });
    }


    /**
     * 请求是否有升级包
     *
     * @param url
     * @param baseUpdateResponse
     */
    private void requestUpgrade(String url, BaseUpdateResponse baseUpdateResponse) {
        Log.d(TAG, "requestUpgrade() called with: url = [" + url + "]");
        final Request request = new Request.Builder().url(url).get().build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: ");
                handler.sendEmptyMessageDelayed(Constant.REQUEST_UPDATE, POLLING_TIME);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //返回格式   id|../../..|length ===》  id|zip路径|zip大小 132|/Upload/edition/InformationDisplay_20191221141046221.zip
                String responseStr = response.body().string();
                Log.i(TAG, "requestUpgrade onResponse: " + responseStr);
                UpgradeBean upgradeBean = baseUpdateResponse.getUpgradeBean(responseStr);
                String url = baseUpdateResponse.getUrl(upgradeBean.getFilepath());
                String id = String.valueOf(upgradeBean.getId());
                if (!TextUtils.isEmpty(url)) {
                    isUpdate = false;
                    //                    downloadFile(url);
                    downloadApk(url, id);
                } else {
                    Log.d(TAG, "没有更新包", null);
                    handler.sendEmptyMessageDelayed(Constant.REQUEST_UPDATE, POLLING_TIME);
                }
            }
        });
    }

    /**
     * 下载apk使用
     *
     * @param download
     * @param id
     */
    private void downloadApk(String download, String id) {
        Log.d(TAG, "downloadApk() called with: download = [" + download + "], id = [" + id + "]");
        String path = getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath() + File.separator + Constant.PATH_DOWNLOAD;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        path = path + File.separator + Constant.APK_NAME;
        File f = new File(path);
        if (f.isFile() && f.exists()) {
            f.delete();
        }
        FileDownloader.setup(MainCrosswalkActivity.this);
        FileDownloader.getImpl().create(download).setPath(path).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                Log.i(TAG, "downloadApk connected");
                downloadZipDialogManager = new DownloadZipDialogManager(MainCrosswalkActivity.this);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.i(TAG, "downloadApk progress soFarBytes ... " + soFarBytes + "  totalBytes ... " + totalBytes);
                if (downloadZipDialogManager != null) {
                    Log.i(TAG, "downloadApk progress: dialog show");
                    downloadZipDialogManager.showDialog();
                    int ratio = (int) (((float) soFarBytes / (float) totalBytes) * 100);
                    downloadZipDialogManager.setProgress(ratio);
                    downloadZipDialogManager.setContent(ratio + "");
                }
            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
                Log.i(TAG, "downloadApk blockComplete");
            }

            @Override
            protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                Log.i(TAG, "downloadApk retry");
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                Log.i(TAG, "downloadApk completed");
                if (downloadZipDialogManager.isShowing()) {
                    Log.i(TAG, "downloadApk completed: dialog dismiss");
                    downloadZipDialogManager.dismiss();
                    downloadZipDialogManager = null;
                }
                String upgradeEndUrl = getUpgradeEndUrl(id);
                requestUpgradeEnd(upgradeEndUrl, f);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.i(TAG, "paused");
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                Log.i(TAG, "error messsage --- " + e.getMessage());
                try {
                    if (downloadZipDialogManager.isShowing()) {
                        downloadZipDialogManager.dismiss();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }finally {
                    ToastUtils.showToast(MainCrosswalkActivity.this, getString(R.string.download_apk_error));
                    handler.sendEmptyMessageDelayed(Constant.REQUEST_UPDATE, POLLING_TIME);
                }
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                Log.i(TAG, "warn");
            }
        }).start();
    }

    private void requestUpgradeEnd(String url, File file) {
        final Request request = new Request.Builder().url(url).get().build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                Log.i(TAG, "requestUpgradeEnd onResponse: " + responseStr);
                //发送upgradeEndUrl后开始安装
                try {
                    BaseUtils.installApk(MainCrosswalkActivity.this, file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestUpdateEnd(String url) {
        final Request request = new Request.Builder().url(url).get().build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                Log.i(TAG, "requestUpdateEnd onResponse: " + responseStr);
            }
        });
    }

    /**
     * 下载zip或者apk文件
     *
     * @param downloadUrl
     */
    private void downloadFile(String downloadUrl, String id) {
        Log.i(TAG, "downloadFile: downloadUrl ... " + downloadUrl);
        String path = getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath() + File.separator + Constant.PATH_DOWNLOAD;
        File file = new File(path);
        //../szty/files/download 目录不存在则创建 各级文件夹
        if (!file.exists()) {
            file.mkdirs();
        }
        path = path + File.separator + Constant.ZIP_NAME;
        File f = new File(path);
        if (f.isFile() && f.exists()) {
            // ..szty/files/download/resource.zip 存在则要删除
            Log.i(TAG, "downloadZip: delete zip");
            f.delete();
        }
        FileDownloader.setup(MainCrosswalkActivity.this);
        FileDownloader.getImpl().create(downloadUrl).setPath(path).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                Log.i(TAG, "connected");
                downloadZipDialogManager = new DownloadZipDialogManager(MainCrosswalkActivity.this);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.i(TAG, "progress soFarBytes ... " + soFarBytes + "  totalBytes ... " + totalBytes);
                if (downloadZipDialogManager != null) {
                    Log.i(TAG, "progress: dialog show");
                    downloadZipDialogManager.showDialog();
                    int ratio = (int) (((float) soFarBytes / (float) totalBytes) * 100);
                    downloadZipDialogManager.setProgress(ratio);
                    downloadZipDialogManager.setContent(ratio + "");
                }
            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
                Log.i(TAG, "blockComplete");
            }

            @Override
            protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                Log.i(TAG, "retry");
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                Log.i(TAG, "completed");
                if (downloadZipDialogManager.isShowing()) {
                    Log.i(TAG, "completed: dialog dismiss");
                    downloadZipDialogManager.dismiss();
                    downloadZipDialogManager = null;
                }
                pb.setVisibility(View.VISIBLE);
                //处理下载完毕的zip包
                checkZip();
                String updateEndUrl = getUpdateEndUrl(id);
                Log.i(TAG, "downloadFile completed: updateEndUrl ... " + updateEndUrl);
                requestUpdateEnd(updateEndUrl);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.i(TAG, "paused");
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                Log.i(TAG, "error messsage --- " + e.getMessage());
                try {
                    if (downloadZipDialogManager.isShowing()) {
                        downloadZipDialogManager.dismiss();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                } finally {
                    ToastUtils.showToast(MainCrosswalkActivity.this, getString(R.string.download_zip_error));
                    handler.sendEmptyMessageDelayed(Constant.REQUEST_UPGRADE, POLLING_TIME);
                }
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                Log.i(TAG, "warn");
            }
        }).start();

    }

    private void checkZip() {
        try {
            String path = getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath() + File.separator + Constant.PATH_DOWNLOAD + File.separator + Constant.ZIP_NAME;
            Log.i(TAG, "checkZip: path ... " + path);
            File file = new File(path);
            // ..szty/files/download/resource.zip  压缩包是否存在
            if (file.exists()) {
                //获取解压目录 ..szty/files/temp
                String unZipPath = getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath() + File.separator + Constant.PATH_TEMP;
                File unZipFile = new File(unZipPath);
                //解压目录存在删除所有子文件，如果不存在直接创建目录
                if (!unZipFile.exists()) {
                    unZipFile.mkdirs();
                } else if (unZipFile.listFiles().length > 0) {
                    for (int i = 0; i < unZipFile.listFiles().length; i++) {
                        File[] childFiles = unZipFile.listFiles();
                        BaseUtils.deleteFolder(childFiles[i].getAbsolutePath());
                    }
                }
                //解压缩文件到temp
                ZipUtil.upZipFileByApache(file, unZipPath);
                Log.i(TAG, "checkZip: end");
                if (isUpdate) {
                    //替换更新文件
                    replaceOldFile();
                } else {
                    //安装升级文件
                    installApk();
                }
            } else {
                Log.e(TAG, "checkZip: " + path + " is not exist", null);
                ToastUtils.showToast(MainCrosswalkActivity.this, getString(R.string.zip_file_is_not_exists));
            }
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.showToast(MainCrosswalkActivity.this, getString(R.string.unzip_error));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 安装升级文件
     */
    private void installApk() throws Exception {
        Log.i(TAG, "installApk: start install");
        String unZipPath = getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath() + File.separator + Constant.PATH_TEMP;
        File file = new File(unZipPath);
        if (file.exists() && file.listFiles() != null && file.listFiles().length == 1) {
            File apkFile = file.listFiles()[0];
            pb.setVisibility(View.GONE);
            Log.i(TAG, "installApk: apkPath ... " + apkFile.getAbsolutePath());
            BaseUtils.installApk(MainCrosswalkActivity.this, apkFile);
        } else {
            pb.setVisibility(View.GONE);
            ToastUtils.showToast(MainCrosswalkActivity.this, getString(R.string.install_error));
            handler.sendEmptyMessageDelayed(Constant.REQUEST_UPDATE, POLLING_TIME);
        }
    }

    /**
     * 替换新的升级文件
     */
    private void replaceOldFile() {
        new Thread(new ReplaceRunnable(new NormalReplaceImpl())).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mXWalkView!=null) {
            XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW,false);
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mXWalkView.getNavigationHistory().canGoBack()) {
            mXWalkView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
        } else {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    class ReplaceRunnable implements Runnable {

        private IReplaceType iReplaceType;

        public ReplaceRunnable(IReplaceType iReplaceType) {
            this.iReplaceType = iReplaceType;
        }

        @Override
        public void run() {
            try {
                Log.i(TAG, "run: start replace");
                //解压的文件目录
                String unZipPath = getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath() + File.separator + Constant.PATH_TEMP;
                File unZipFile = new File(unZipPath);
                //真实播放的文件目录
                String path = getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath() + File.separator + Constant.PATH_PAGE;
                File resourceFile = new File(path);
                boolean isReplace = iReplaceType.startReplace(MainCrosswalkActivity.this, unZipFile, resourceFile);
                if (isReplace) {
                    //替换完毕 更新界面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            ToastUtils.showToast(MainCrosswalkActivity.this, getString(R.string.update_finish));
                            try {
                                readLocalH5();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessageDelayed(Constant.REQUEST_UPGRADE, POLLING_TIME);
                        }
                    });
                } else {
                    //替换失败
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            ToastUtils.showToast(MainCrosswalkActivity.this, getString(R.string.update_error));
                            handler.sendEmptyMessageDelayed(Constant.REQUEST_UPGRADE, POLLING_TIME);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getUpdateUrl() {
        String url = "";
        if (configBean != null) {
            String ip = configBean.getServerIp();
            String port = configBean.getServerPort();
            String clientIp = BaseUtils.getIP();
            String updateUrlLocal = configBean.getUpdateUrl();
            String s;
            s = updateUrlLocal.replace("{0}", configBean.getId());
            s = s.replace("{1}", clientIp);
            url = Constant.HTTP_CODE + ip + ":" + port + s;
        } else {
            Log.e(TAG, "getUpdateUrl: configBean is null", null);
        }
        return url;
    }

    private String getUpgradeUrl() {
        String url = "";
        if (configBean != null) {
            String ip = configBean.getServerIp();
            String port = configBean.getServerPort();
            String clientIp = BaseUtils.getIP();
            String upgradeUrlLocal = configBean.getUpgradeUrl();
            String s;
            s = upgradeUrlLocal.replace("{0}", configBean.getId());
            s = s.replace("{1}", clientIp);
            s = s.replace("{2}", configBean.getVersionNumber());
            url = Constant.HTTP_CODE + ip + ":" + port + s;
        } else {
            Log.e(TAG, "getUpdateUrl: configBean is null", null);
        }
        return url;
    }

    /**
     * /interface/interface1.aspx?at=upCom&amp;esole={0}&amp;ip={1}&amp;vn={2}&amp;id={3}
     *
     * @return
     */
    private String getUpgradeEndUrl(String id) {
        String url = "";
        if (configBean != null) {
            String ip = configBean.getServerIp();
            String port = configBean.getServerPort();
            String clientIp = BaseUtils.getIP();
            String upgradeUrlLocal = configBean.getUpgradeEndUrl();
            String s;
            s = upgradeUrlLocal.replace("{0}", configBean.getId());
            s = s.replace("{1}", clientIp);
            s = s.replace("{2}", configBean.getVersionNumber());
            s = s.replace("{3}", id);
            url = Constant.HTTP_CODE + ip + ":" + port + s;
        } else {
            Log.e(TAG, "getUpdateUrl: configBean is null", null);
        }
        return url;
    }

    /**
     * /interface/interface1.aspx?at=dataCom&amp;esole={0}&amp;ip={1}&amp;id={2}
     *
     * @return
     */
    private String getUpdateEndUrl(String id) {
        String url = "";
        if (configBean != null) {
            String ip = configBean.getServerIp();
            String port = configBean.getServerPort();
            String clientIp = BaseUtils.getIP();
            String updateUrlLocal = configBean.getUpdateEndUrl();
            String s;
            s = updateUrlLocal.replace("{0}", configBean.getId());
            s = s.replace("{1}", clientIp);
            s = s.replace("{2}", id);
            url = Constant.HTTP_CODE + ip + ":" + port + s;
        } else {
            Log.e(TAG, "getUpdateUrl: configBean is null", null);
        }
        return url;
    }

    /**
     * /interface/interface1.aspx?at=heart&amp;esole={0}&amp;ip={1}
     *
     * @return
     */
    private String getSendHeartbeatUrl() {
        String url = "";
        if (configBean != null) {
            String ip = configBean.getServerIp();
            String port = configBean.getServerPort();
            String localHeartbeatUrl = configBean.getHeartUrl();
            String clientIp = BaseUtils.getIP();
            String id = configBean.getId();
            String s;
            s = localHeartbeatUrl.replace("{0}", id);
            s = s.replace("{1}", clientIp);
            url = Constant.HTTP_CODE + ip + ":" + port + s;
        } else {
            Log.e(TAG, "getSendHeartbeatUrl: configBean is null", null);
        }
        return url;
    }


    /**
     * 发送心跳
     */
    private void sendHeartBeat() {
        Log.d(TAG, "sendHeartBeat() called with: url = [" + heartBeatUrl + "]");
        final Request request = new Request.Builder().url(heartBeatUrl).get().build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure() called");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "heartbeat onResponse() called ");
                //                String responseStr = response.body().string();
            }
        });
        handler.sendEmptyMessageDelayed(Constant.SEND_HEARTBEAT, sendHeartbeatTime);
    }

    /**
     * 通过Handler进行轮询
     */
    static class PollingHandler extends Handler {
        private WeakReference<MainCrosswalkActivity> activity;

        public PollingHandler(MainCrosswalkActivity a) {
            this.activity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MainCrosswalkActivity mainActivity = activity.get();
            if (mainActivity != null) {
                //调用查询升级
                if (msg.what == Constant.REQUEST_UPGRADE) {
                    mainActivity.requestUpgrade(mainActivity.getUpgradeUrl(), mainActivity.baseUpdateResponse);
                }//调用查询更新
                else if (msg.what == Constant.REQUEST_UPDATE) {
                    mainActivity.requestUpdate(mainActivity.getUpdateUrl(), mainActivity.baseUpdateResponse);
                } else if (msg.what == Constant.SEND_HEARTBEAT) {
                    mainActivity.sendHeartBeat();
                }
            }
        }
    }


    /////////////////////与js对接///////////////////////

    public class JavaScriptinterface {
        @JavascriptInterface
        public String getIp() {
            String ip = BaseUtils.getIP();
            ToastUtils.showToast(MainCrosswalkActivity.this, "ip 地址 ... " + ip);
            return ip;
        }

        @JavascriptInterface
        public String getPort() {
            String port = "";
            if (configBean != null) {
                port = configBean.getServerPort();
                BaseUtils.getIP();
            }
            return port;
        }
    }

    ///////////////////////////////////////////////////////////


    private long firstTime = 0;
    public static final int PRESS_BACK_BUTTON_INTERVAL = 2*1000;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO: 2020-03-18 由于已经有onKeyDown方法   这里先不执行“再按一次退出程序”操作  
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_BACK:
//                long secondTime = System.currentTimeMillis();
//                if (secondTime - firstTime > PRESS_BACK_BUTTON_INTERVAL) {                                         //如果两次按键时间间隔大于2秒，则不退出
//                    Toast.makeText(this, R.string.press_again_exit, Toast.LENGTH_SHORT).show();
//                    firstTime = secondTime;//更新firstTime
//                    return true;
//                } else {
//                    //两次按键小于2秒时，退出应用
//                    System.exit(0);
//                }
//                break;
//        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mXWalkView!=null) {
            mXWalkView.onNewIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mXWalkView!=null) {
            mXWalkView.pauseTimers();
            mXWalkView.onHide();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mXWalkView!=null) {
            mXWalkView.pauseTimers();
            mXWalkView.onHide();
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
