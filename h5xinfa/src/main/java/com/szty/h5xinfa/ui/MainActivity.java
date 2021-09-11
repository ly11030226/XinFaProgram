package com.szty.h5xinfa.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import com.ads.utillibrary.utils.ToastUtils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.szty.h5xinfa.BuildConfig;
import com.szty.h5xinfa.Constant;
import com.szty.h5xinfa.DownloadZipDialogManager;
import com.szty.h5xinfa.IReplaceType;
import com.szty.h5xinfa.NormalReplaceImpl;
import com.szty.h5xinfa.R;
import com.szty.h5xinfa.baoao.ConfigJsonHandler;
import com.szty.h5xinfa.baoao.DoubleClickListener;
import com.szty.h5xinfa.baoao.SocketService;
import com.szty.h5xinfa.model.ConfigBean;
import com.szty.h5xinfa.model.UpdateBean;
import com.szty.h5xinfa.model.UpgradeBean;
import com.szty.h5xinfa.updateResponse.BaseUpdateResponse;
import com.szty.h5xinfa.updateResponse.NormalUpdateResponseImpl;
import com.szty.h5xinfa.util.BaseUtils;
import com.szty.h5xinfa.util.ZipUtil;
import com.szty.h5xinfa.view.QueueView;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.szty.h5xinfa.baoao.ToolsKt.ACTION_RESULT;
import static com.szty.h5xinfa.baoao.ToolsKt.KEY_RESULT;
import static com.szty.h5xinfa.baoao.ToolsKt.REQUEST_CODE;
import static com.szty.h5xinfa.baoao.ToolsKt.SHOW_QUEUE;
import static com.szty.h5xinfa.baoao.ToolsKt.SHOW_QUEUE_TIME;
import static com.szty.h5xinfa.baoao.ToolsKt.getIMEIDeviceId;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    ConfigBean configBean;
    private WebView webView;
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
    private MyReceiver receiver;
    private QueueView qv;
    private Button btnLeft, btnRight;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        try {
            okHttpClient = new OkHttpClient();
            initView();
            initData();
            initWebView();
            // ../szty/page/index.html 文件不存在 则加载assets下面的index.html文件
            readLocalH5();
            initIntentData();
            startService();
            registerReceiver();
            addListener();
            String imei = getIMEIDeviceId(this);
            Log.i(TAG, "onCreate: imei ... " + imei);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        qv = findViewById(R.id.qv);
        btnLeft = findViewById(R.id.btn_left);
        btnRight = findViewById(R.id.btn_right);
    }

    private void addListener() {
        btnLeft.setOnClickListener(new DoubleClickListener() {
            @Override
            public void doubleClick(@Nullable View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        btnRight.setOnClickListener(new DoubleClickListener() {
            @Override
            public void doubleClick(@Nullable View v) {
                new MaterialDialog.Builder(MainActivity.this).title(R.string.dialog_title).content(R.string.dialog_exit).positiveText(R.string.dialog_confirm).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        System.exit(0);
                    }
                }).negativeText(R.string.dialog_cancel).show();
            }
        });
    }

    private void registerReceiver() {
        receiver = new MyReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void startService() {
        ServiceConnection sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.i(TAG, "onServiceConnected: ");
                SocketService service = ((SocketService.MyBinder) binder).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "onServiceDisconnected: ");
            }
        };
        Intent intent = new Intent(MainActivity.this, SocketService.class);
        bindService(intent, sc, BIND_AUTO_CREATE);
    }

    private void initIntentData() {
        if (getIntent() != null && getIntent().hasExtra(Constant.KEY_HANDLE_CODE)) {
            int handleCode = getIntent().getIntExtra(Constant.KEY_HANDLE_CODE, 0);
            switch (handleCode) {
                case Constant.FILE_NOT_EXIST:
                    ToastUtils.showToast(MainActivity.this, getString(R.string.config_file_not_exist));
                    break;
                case Constant.LOAD_XML_ERROR:
                    ToastUtils.showToast(MainActivity.this, getString(R.string.read_config_file_error));
                    break;
                case Constant.LOAD_XML_FINISH:
                    //                    configBean = XmlManager.getInstance().getConfigBean();
                    configBean = ConfigJsonHandler.Companion.getMConfigBean();
                    //                    break;
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
                sendHeartbeatTime = Integer.parseInt(heartTime) * 1000;
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
        handler = new PollingHandler(MainActivity.this);
    }

    private void readLocalH5() throws MalformedURLException, URISyntaxException {
        String p = MainActivity.this.getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath();
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

    private void initWebView() {
        webView = findViewById(R.id.web_view);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        /**
         * 如果 app 需要自定义 UA，建议采取在 SDK 默认UA 后追加 app UA 的方式
         * APP_NAME_UA 用户自定义名字
         */
        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSetting.setMediaPlaybackRequiresUserGesture(false);
        }
        webSetting.setGeolocationEnabled(true);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setTextZoom(100);
        webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        /**
         * 注：这里如果重写了WebChromeClient的shouldOverrideUrlLoading方法
         * 在某些Android终端上加载iframe时会出现显示错误
         */
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
        webView.setInitialScale(100);
    }

    /**
     * 请求是否有更新文件
     *
     * @param url
     * @param baseUpdateResponse
     */
    private void requestUpdate(String url, BaseUpdateResponse baseUpdateResponse) {
        Log.d(TAG, "requestUpdate() called with: url = [" + url + "]");
        final Request request = new Request.Builder().url(url).get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "requestUpdate onFailure: ");
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
                Log.i(TAG, "requestUpgrade onFailure: ");
                handler.sendEmptyMessageDelayed(Constant.REQUEST_UPDATE, POLLING_TIME);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //返回格式   id|../../..|length ===》  id|zip路径|zip大小 132|/Upload/edition/InformationDisplay_20191221141046221.zip
                String responseStr = response.body().string();
                //                Log.i(TAG, "requestUpgrade onResponse: " + responseStr);
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
        File parent = getExternalFilesDir(Constant.PATH_SZTY);
        if (!parent.exists()) {
            parent.mkdirs();
            Log.e(TAG, "downloadApk: download path is not exist", null);
            handler.sendEmptyMessageDelayed(Constant.REQUEST_UPDATE, POLLING_TIME);
            return;
        }
        File f = new File(parent, Constant.APK_NAME);
        final String path = f.getAbsolutePath();
        if (f.isFile() && f.exists()) {
            f.delete();
        }
        FileDownloader.setup(MainActivity.this);
        FileDownloader.getImpl().create(download).setPath(path).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                Log.i(TAG, "downloadApk connected");
                downloadZipDialogManager = new DownloadZipDialogManager(MainActivity.this);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                //                Log.i(TAG, "downloadApk progress soFarBytes ... " + soFarBytes + "  totalBytes ... " + totalBytes);
                if (downloadZipDialogManager != null) {
                    //                    Log.i(TAG, "downloadApk progress: dialog show");
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
                    //                    Log.i(TAG, "downloadApk completed: dialog dismiss");
                    downloadZipDialogManager.dismiss();
                    downloadZipDialogManager = null;
                }
                //安装升级文件
                //                installApk(path);
                String upgradeEndUrl = getUpgradeEndUrl(id);
                requestUpgradeEnd(upgradeEndUrl, f);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.i(TAG, "paused");
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                Log.i(TAG, "error message --- " + e.getMessage());
                try {
                    if (downloadZipDialogManager.isShowing()) {
                        downloadZipDialogManager.dismiss();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {
                    ToastUtils.showToast(MainActivity.this, getString(R.string.download_apk_error));
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
                handler.sendEmptyMessageDelayed(Constant.REQUEST_UPDATE, POLLING_TIME);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                Log.i(TAG, "requestUpgradeEnd onResponse: " + responseStr);
                //发送upgradeEndUrl后开始安装
                //                BaseUtils.installApk(MainActivity.this, file);
                Log.i(TAG, "requestUpgradeEnd onResponse: " + file.getAbsolutePath());
                //                Intent i = new Intent(Intent.ACTION_VIEW);
                //                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //                i.setDataAndType(Uri.parse("file://"+file.getAbsolutePath()), "application/vnd.android.package-archive");
                //                startActivity(i);
                installAPK(file);
            }
        });
    }

    private void installAPK(File f) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //安装完成后，启动app
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //第二个参数要和Mainfest中<provider>内的android:authorities 保持一致
            Uri uri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", f);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
        }
        startActivity(intent);
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
        FileDownloader.setup(MainActivity.this);
        FileDownloader.getImpl().create(downloadUrl).setPath(path).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                Log.i(TAG, "connected");
                downloadZipDialogManager = new DownloadZipDialogManager(MainActivity.this);
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
                    ToastUtils.showToast(MainActivity.this, getString(R.string.download_zip_error));
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
                //替换更新文件
                replaceOldFile();
            } else {
                Log.e(TAG, "checkZip: " + path + " is not exist", null);
                ToastUtils.showToast(MainActivity.this, getString(R.string.zip_file_is_not_exists));
            }
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.showToast(MainActivity.this, getString(R.string.unzip_error));
        }
    }

    /**
     * 安装升级文件
     */
    private void installApk(String path) {
        Log.i(TAG, "installApk: start install");
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            pb.setVisibility(View.GONE);
            Log.i(TAG, "installApk: apkPath ... " + path);
            try {
                BaseUtils.installApk(MainActivity.this, file);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "installApk: is error", null);
                if (file.exists()) {
                    file.delete();
                }
                handler.sendEmptyMessageDelayed(Constant.REQUEST_UPDATE, POLLING_TIME);
            }
        } else {
            pb.setVisibility(View.GONE);
            ToastUtils.showToast(MainActivity.this, getString(R.string.install_error));
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
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
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
                boolean isReplace = iReplaceType.startReplace(MainActivity.this, unZipFile, resourceFile);
                if (isReplace) {
                    //替换完毕 更新界面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            ToastUtils.showToast(MainActivity.this, getString(R.string.update_finish));
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
                            ToastUtils.showToast(MainActivity.this, getString(R.string.update_error));
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
            String updateUrlLocal;
            updateUrlLocal = getResources().getString(R.string.url_update);
            String s;
            s = updateUrlLocal.replace("{0}", configBean.getId());
            s = s.replace("{1}", clientIp);
            url = Constant.HTTP_CODE + ip + ":" + port + BuildConfig.PATH + s;
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
            String upgradeUrlLocal;
            upgradeUrlLocal = getResources().getString(R.string.url_upgrade);
            String s;
            s = upgradeUrlLocal.replace("{0}", configBean.getId());
            s = s.replace("{1}", clientIp);
            url = Constant.HTTP_CODE + ip + ":" + port + BuildConfig.PATH + s;
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
            String upgradeUrlLocal = getResources().getString(R.string.url_upgrade_end);
            String s;
            s = upgradeUrlLocal.replace("{0}", id);
            url = Constant.HTTP_CODE + ip + ":" + port + BuildConfig.PATH + s;
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
            String updateUrlLocal = getResources().getString(R.string.url_update_end);
            String s = updateUrlLocal.replace("{0}", id);
            url = Constant.HTTP_CODE + ip + ":" + port + BuildConfig.PATH + s;
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
            String localHeartbeatUrl = getResources().getString(R.string.url_heart);
            String clientIp = BaseUtils.getIP();
            String id = configBean.getId();
            String s;
            s = localHeartbeatUrl.replace("{0}", id);
            s = s.replace("{1}", clientIp);
            url = Constant.HTTP_CODE + ip + ":" + port + BuildConfig.PATH + s;
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
        private WeakReference<MainActivity> activity;

        public PollingHandler(MainActivity a) {
            this.activity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MainActivity mainActivity = activity.get();
            if (mainActivity != null) {
                //调用查询升级
                if (msg.what == Constant.REQUEST_UPGRADE) {
                    mainActivity.requestUpgrade(mainActivity.getUpgradeUrl(), mainActivity.baseUpdateResponse);
                }//调用查询更新
                else if (msg.what == Constant.REQUEST_UPDATE) {
                    mainActivity.requestUpdate(mainActivity.getUpdateUrl(), mainActivity.baseUpdateResponse);
                } else if (msg.what == Constant.SEND_HEARTBEAT) {
                    mainActivity.sendHeartBeat();
                } else if (msg.what == SHOW_QUEUE) {
                    mainActivity.qv.setVisibility(View.GONE);
                }
            }
        }
    }


    /////////////////////与js对接///////////////////////

    public class JavaScriptinterface {
        @JavascriptInterface
        public String getIp() {
            String ip = BaseUtils.getIP();
            ToastUtils.showToast(MainActivity.this, "ip 地址 ... " + ip);
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
    public static final int PRESS_BACK_BUTTON_INTERVAL = 2 * 1000;

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            qv.requestLayout();
        }
        Log.i(TAG, "onActivityResult: requestCode ... " + requestCode + " resultCode ... " + resultCode);
    }

    static class MyReceiver extends BroadcastReceiver {
        private WeakReference wr;

        public MyReceiver(MainActivity a) {
            wr = new WeakReference(a);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(KEY_RESULT)) {
                String result = intent.getStringExtra(KEY_RESULT);
                if (wr.get() != null) {
                    MainActivity a = ((MainActivity) wr.get());
                    a.qv.setVisibility(View.VISIBLE);
                    a.handler.removeMessages(SHOW_QUEUE);
                    a.qv.setContent(result);
                    a.handler.sendEmptyMessageDelayed(SHOW_QUEUE, SHOW_QUEUE_TIME);
                }
            }
        }
    }
}
