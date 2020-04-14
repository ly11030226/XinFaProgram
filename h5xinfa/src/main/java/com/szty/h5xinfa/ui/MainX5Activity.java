package com.szty.h5xinfa.ui;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
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
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainX5Activity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    ConfigBean configBean;
    private com.tencent.smtt.sdk.WebView webView;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_x5);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        try {
            okHttpClient = new OkHttpClient();
            initData();
            initX5WebView();
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
                    ToastUtils.showToast(MainX5Activity.this, getString(R.string.config_file_not_exist));
                    break;
                case Constant.LOAD_XML_ERROR:
                    ToastUtils.showToast(MainX5Activity.this, getString(R.string.read_config_file_error));
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
        handler = new PollingHandler(MainX5Activity.this);
    }

    private void readLocalH5() throws MalformedURLException, URISyntaxException {
        String p = MainX5Activity.this.getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath();
        //获取 android/data/packagename/files/szty/page 目录
        File file = new File(p, Constant.PATH_PAGE);
        if (file.exists()) {
            //如果 ../szty/page 目录存在 则查询../szty/page/index.html 是否存在
            File f = new File(file, Constant.HTML_INDEX);
            if (f.exists()) {
                //如果index.html存在，则webView加载
                String url = "file://" + f.getAbsolutePath();
                Log.i(TAG, "readLocalH5: url ... " + url);
                webView.loadUrl(url);
            } else {
                //如果 ../szty/page/index.html不存在，则加载assets下的index.html
                webView.loadUrl("file:///android_asset/index.html");
            }
        } else {
            //如果 ../szty/page 不存在 则创建page文件夹，然后加载assets下的index.html
            file.mkdirs();
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    private void initX5WebView() {
        webView = findViewById(R.id.web_view);
        /**
         * 如果 app 需要自定义 UA，建议采取在 SDK 默认UA 后追加 app UA 的方式
         * APP_NAME_UA 用户自定义名字
         */
        com.tencent.smtt.sdk.WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);                    //支持Javascript 与js交互
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        settings.setAllowFileAccess(true);                      //设置可以访问文件
        settings.setSupportZoom(true);                          //支持缩放
        settings.setBuiltInZoomControls(true);                  //设置内置的缩放控件
        settings.setUseWideViewPort(true);                      //自适应屏幕
        settings.setSupportMultipleWindows(true);               //多窗口
        settings.setDefaultTextEncodingName("utf-8");            //设置编码格式
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheMaxSize(Long.MAX_VALUE);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);       //缓存模式

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(com.tencent.smtt.sdk.WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
            }

            @Override
            public void onPageFinished(com.tencent.smtt.sdk.WebView webView, String s) {
                super.onPageFinished(webView, s);
            }

            @Override
            public boolean shouldOverrideUrlLoading(com.tencent.smtt.sdk.WebView webView, String url) {
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView webView, com.tencent.smtt.export.external.interfaces.SslErrorHandler sslErrorHandler, com.tencent.smtt.export.external.interfaces.SslError sslError) {
                sslErrorHandler.proceed();//忽略SSL证书错误

            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(com.tencent.smtt.sdk.WebView webView, String s, String s1, JsResult jsResult) {
                return super.onJsAlert(webView, s, s1, jsResult);
            }

            @Override
            public void onReceivedTitle(WebView webView, String s) {
                super.onReceivedTitle(webView, s);
            }

            @Override
            public void onProgressChanged(WebView webView, int progress) {
                super.onProgressChanged(webView, progress);

            }
        });
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
        FileDownloader.setup(MainX5Activity.this);
        FileDownloader.getImpl().create(download).setPath(path).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                Log.i(TAG, "downloadApk connected");
                downloadZipDialogManager = new DownloadZipDialogManager(MainX5Activity.this);
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
                    ToastUtils.showToast(MainX5Activity.this, getString(R.string.download_apk_error));
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
                BaseUtils.installApk(MainX5Activity.this, file);
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
        FileDownloader.setup(MainX5Activity.this);
        FileDownloader.getImpl().create(downloadUrl).setPath(path).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                Log.i(TAG, "connected");
                downloadZipDialogManager = new DownloadZipDialogManager(MainX5Activity.this);
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
                    ToastUtils.showToast(MainX5Activity.this, getString(R.string.download_zip_error));
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
                ToastUtils.showToast(MainX5Activity.this, getString(R.string.zip_file_is_not_exists));
            }
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.showToast(MainX5Activity.this, getString(R.string.unzip_error));
        }
    }

    /**
     * 安装升级文件
     */
    private void installApk() {
        Log.i(TAG, "installApk: start install");
        String unZipPath = getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath() + File.separator + Constant.PATH_TEMP;
        File file = new File(unZipPath);
        if (file.exists() && file.listFiles() != null && file.listFiles().length == 1) {
            File apkFile = file.listFiles()[0];
            pb.setVisibility(View.GONE);
            Log.i(TAG, "installApk: apkPath ... " + apkFile.getAbsolutePath());
            BaseUtils.installApk(MainX5Activity.this, apkFile);
        } else {
            pb.setVisibility(View.GONE);
            ToastUtils.showToast(MainX5Activity.this, getString(R.string.install_error));
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
        if (webView != null) {
            webView.removeAllViews();
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.setTag(null);
            webView.clearHistory();
            webView.destroy();
            webView = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
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
                boolean isReplace = iReplaceType.startReplace(MainX5Activity.this, unZipFile, resourceFile);
                if (isReplace) {
                    //替换完毕 更新界面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            ToastUtils.showToast(MainX5Activity.this, getString(R.string.update_finish));
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
                            ToastUtils.showToast(MainX5Activity.this, getString(R.string.update_error));
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
        private WeakReference<MainX5Activity> activity;

        public PollingHandler(MainX5Activity a) {
            this.activity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MainX5Activity mainActivity = activity.get();
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
            ToastUtils.showToast(MainX5Activity.this, "ip 地址 ... " + ip);
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
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
