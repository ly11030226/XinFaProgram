package com.ads.clientconnection.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ads.clientconnection.R;
import com.ads.clientconnection.adapter.LocalConnAdapter;
import com.ads.clientconnection.base.BaseActivity;
import com.ads.clientconnection.base.Constant;
import com.ads.clientconnection.base.MyLogger;
import com.ads.clientconnection.entity.ImageAndVideoEntity;
import com.ads.clientconnection.ui.qrCode.QRCodeActivity;
import com.ads.clientconnection.utils.BaseUtils;
import com.ads.clientconnection.utils.SMBUtil;
import com.ads.clientconnection.view.ClientInfoView;
import com.ads.utillibrary.utils.MyDialog;
import com.ads.utillibrary.utils.ToastUtils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gongw.remote.Device;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.client.ClientByteSocketManager;
import com.gongw.remote.search.DeviceSearcher;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tamsiree.rxkit.TLog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;

/**
 * 小信发 控制端 主界面
 * @author Ly
 */
public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.rcv_device)
    RecyclerView rcv;
    List<Device> deviceList = new ArrayList<>();
    LocalConnAdapter localConnAdapter;
    @BindView(R.id.client_info_view)
    ClientInfoView civ;
    @BindView(R.id.iv_scanning)
    ImageView ivScanning;
    private static Context context;
    MaterialDialog materialDialog;
    private String currentConnectIp;

    Handler uiHandler = new Handler();

    private ImageAndVideoEntity entity;
    MyDialog myDialog;
    //当前连接设备
    private static Device mCurrentDevice;
    private boolean isConn = false;

    public static final int PRESS_BACK_BUTTON_INTERVAL = 2*1000;
    private long firstPressedTime;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case CommunicationKey.FLAG_CLIENT_READ_DATA:
                        String json = (String) msg.obj;
                        TLog.d(TAG,"获取广告机本地存储的json数据");
                        if (TextUtils.isEmpty(json)) {
                            TLog.e(TAG, "json data is null");
                            return;
                        }
                        Gson gson = new Gson();
                        entity = gson.fromJson(json, ImageAndVideoEntity.class);
                        //与远程终端连接后，更新界面
                        civ.setConnectedState(mCurrentDevice.getIp(), entity);
                        myDialog.hideDialog();
                        isConn = true;
                        ToastUtils.showToast(MainActivity.this, BaseUtils.getStringByResouceId(R.string.socket_connect_success));
                        break;
                    case RemoteConst.FLAG_CLOSE_SOCKET:
                        myDialog.hideDialog();
                        entity = null;
                        civ.setConnectClose();
                        ClientByteSocketManager.getInstance().closeRunnable();
                        isConn = false;
                        ToastUtils.showToast(MainActivity.this, BaseUtils.getStringByResouceId(R.string.socket_is_not_connected));
                        break;
                    case RemoteConst.FLAG_SERVER_CLOSE:
                        entity = null;
                        civ.setConnectClose();
                        ClientByteSocketManager.getInstance().closeRunnable();
                        isConn = false;
                        ToastUtils.showToast(MainActivity.this, BaseUtils.getStringByResouceId(R.string.server_socket_close));
                        //发送广播 关闭界面
                        LocalBroadcastManager.getInstance(MainActivity.this)
                                .sendBroadcast(new Intent(Constant.ACTION_FINISH_ACTIVITY));
                        break;
                    case Constant.FLAG_CLOSE_PROGRESSBAR:
                        //30秒过后如果还是正在连接  已经有另外一个Socket连上了Server，本机的Socket要断开，以防阻塞
                        if (myDialog!=null && myDialog.isShowing()) {
                            if (BaseUtils.getStringByResouceId(R.string.pb_connect_now).equals(myDialog.getRemindContent())) {
                                entity = null;
                                ClientByteSocketManager.getInstance().closeRunnable();
                                myDialog.hideDialog();
                            }
                        }
                        break;
                    case CommunicationKey.FLAG_JSON_TXT_IS_NULL: //展示端没有展示文件
                        myDialog.hideDialog();
                        ToastUtils.showToast(MainActivity.this,BaseUtils.getStringByResouceId(R.string.json_txt_is_null));
                        break;
                    case RemoteConst.GET_CONNECTPSD_SHOW_DIALOG: //获取了连接广告机的密码
                        String psd = (String) msg.obj;
                        openConnDialog(psd);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 打开连接机器的弹框
     * @param psd
     */
    private void openConnDialog(String psd) {
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_connect_psd,null);
        EditText et = view.findViewById(R.id.et_psd);
        new MaterialDialog.Builder(this)
                .title(getString(R.string.connect_psd))
                .customView(view, false)
                .positiveText("确定")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        checkPsdIsCorrect(et.getText().toString().trim(),psd);
                    }
                })
                .show();
    }
    /**
     * 检查密码是否正确
     * @param inputStr
     * @param psd
     */
    private void checkPsdIsCorrect(String inputStr, String psd) {
        if (!ClientByteSocketManager.connect) {
            Toast.makeText(MainActivity.this, R.string.connect_time_out, Toast.LENGTH_SHORT).show();
            return;
        }
        if (psd.equals(inputStr)) {
            byte[] b = new byte[5];
            b[0] = RemoteConst.REQUEST_DATA_LIST;
            ClientByteSocketManager.getInstance().sendMsg(b);
        }else{
            Toast.makeText(MainActivity.this, R.string.psd_is_error, Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_);
        ButterKnife.bind(this);
        context = MainActivity.this;
        try {
            initProgressBar();
            initRecyclerView();
            addListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initProgressBar() {
        myDialog = new MyDialog(MainActivity.this);
    }

    private void initRecyclerView() {
        localConnAdapter = new LocalConnAdapter(this, deviceList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rcv.setLayoutManager(linearLayoutManager);
        rcv.setAdapter(localConnAdapter);
        localConnAdapter.addClickListener(new LocalConnAdapter.ClickListener() {
            @Override
            public void onClick(View v, int position) {
                MyLogger.i(TAG,"localConnAdapter item onClick position ... "+position);
                Device device = deviceList.get(position);
                openDialog(device);
            }
        });
    }


    private void addListener() {
        ivScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(MainActivity.this).setCaptureActivity(QRCodeActivity.class).initiateScan();
            }
        });
        civ.setSearchListener(new DeviceSearcher.OnSearchListener() {
            @Override
            public void onSearchStart() {
                MyLogger.d(TAG, "onSearchStart");
                myDialog.showDialog(BaseUtils.getStringByResouceId(R.string.is_searching));
                deviceList.clear();
                localConnAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSearchedNewOne(Device device) {
                MyLogger.d(TAG, "onSearchedNewOne");
                String ip = BaseUtils.getHostIP();
                if (!ip.contains(device.getIp())) {
                    deviceList.add(device);
                    localConnAdapter.notifyDataSetChanged();
                    myDialog.hideDialog();
                }
            }

            @Override
            public void onSearchFinish() {
                MyLogger.d(TAG, "onSearchFinish");
                myDialog.hideDialog();
                ToastUtils.showToast(MainActivity.this, BaseUtils.getStringByResouceId(R.string.search_end));
            }

            @Override
            public void onSearchException() {
                MyLogger.d(TAG, "onSearchException");
                if (deviceList.size() == 0) {
                    myDialog.hideDialog();
                    ToastUtils.showToast(MainActivity.this, BaseUtils.getStringByResouceId(R.string.search_time_out));
                }
            }

            @Override
            public void onSearchBindFail() {
                myDialog.hideDialog();
                ToastUtils.showToast(MainActivity.this, BaseUtils.getStringByResouceId(R.string.bind_fail));
            }
        });
    }

    private void createConnection(Device device) {
        String ip = device.getIp();
        MyLogger.i(TAG,"createConnection ... "+ip);
        ClientByteSocketManager.getInstance().createConn(MainActivity.this,ip, handler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //ServerSocket 断开
        if (resultCode == RESULT_CANCELED) {
            entity = null;
        }
        //跳转到资源控制界面
        if (requestCode == Constant.FLAG_JUMP_TO_RES_CONTROL && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra(Constant.ACTION_BACK)) {
                ArrayList<ImageAndVideoEntity.FileEntity> fileEntities = (ArrayList<ImageAndVideoEntity.FileEntity>) data.getSerializableExtra(Constant.ACTION_BACK);
                if (fileEntities != null && entity != null) {
                    entity.setFiles(fileEntities);
                    civ.updateEntityData(entity);
                    TLog.i(Constant.TRACK_LIST,"返回到主界面接收到的播放列表 ... "+fileEntities.toString());
                }
            }
        } else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    MyLogger.w(TAG, "cancel");
                } else {
                    String contents = result.getContents();
                    MyLogger.i(TAG, "result ... " + result.getContents());
                    //如果用户扫描了左边二维码 则屏蔽操作，否则异常
                    if (contents.contains("ftp://")) {
                        ToastUtils.showToast(MainActivity.this,BaseUtils.getStringByResouceId(R.string.user_other_software));
                        return;
                    }else if (contents.contains(":")) {
                        String[] strs = contents.split(":");
                        Device device = new Device(strs[0], Integer.valueOf(strs[1]));
                        openDialog(device);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 打开对话框
     *
     * @param mDevice
     */
    private void openDialog(Device mDevice) {
        if (isConn) {
            ToastUtils.showToast(MainActivity.this,BaseUtils.getStringByResouceId(R.string.conn_device));
            return;
        }
        String result = "您确定与IP地址是" + mDevice.getIp() + "的终端链接吗？";
        if (materialDialog == null) {
            materialDialog = new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.dialog_title)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .content(result)
                    .positiveText(R.string.dialog_commit)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            onclick(mDevice);
                        }
                    })
                    .negativeText(R.string.dialog_cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            materialDialog.dismiss();
                        }
                    })
                    .build();
        }else{
            materialDialog.setContent(result);
            materialDialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    onclick(mDevice);
                }
            });
        }
        materialDialog.show();
    }

    private void onclick(Device mDevice){
        myDialog.showDialog(BaseUtils.getStringByResouceId(R.string.pb_connect_now));
        createConnection(mDevice);
        mCurrentDevice = mDevice;
        handler.sendEmptyMessageDelayed(Constant.FLAG_CLOSE_PROGRESSBAR,30*1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        materialDialog = null;
        myDialog = null;
    }

    /**************************************************************************************/

    private void getShareFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = "192.168.0.186";//pc地址
                String username = "Ly";//账户密码
                String password = "123456";
                UniAddress mDomain = null;
                try {
                    //登录授权
                    mDomain = UniAddress.getByName(ip);
                    NtlmPasswordAuthentication mAuthentication = new NtlmPasswordAuthentication(ip, username, password);
                    SmbSession.logon(mDomain, mAuthentication);
                    //登录授权结束
                    String rootPath = "smb://" + ip + "/";
                    SmbFile mRootFolder;
                    try {
                        mRootFolder = new SmbFile(rootPath, mAuthentication);
                        try {
                            SmbFile[] files;
                            files = mRootFolder.listFiles();
                            for (SmbFile smbfile : files) {
                                Log.i(TAG, "文件名称 ... " + smbfile.getCanonicalPath());//这个就能获取到共享文件夹了
                                String result = smbfile.getCanonicalPath();
                                //$代表隐藏的共享文件夹
                                if (!result.contains("$")) {
                                    SmbFile s = new SmbFile(result + "test1.jpg", mAuthentication);
                                    if (s != null && s.isFile()) {
                                        //path ... smb://192.168.0.186/ads/test1.jpg
                                        String path = s.getCanonicalPath();
                                        File file = new File(Constant.TEMP_DIR);
                                        if (!file.exists()) {
                                            file.mkdirs();
                                        }
                                        String localPath = Constant.TEMP_DIR + s.getName();
                                        Log.i(TAG, "localPath ... " + localPath);
                                        SMBUtil.doCopyBySmb(s, localPath);
                                        uiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //                                                Bitmap bitmap = BitmapFactory.decodeFile(localPath);
                                                //                                                ivBarcode.setImageBitmap(bitmap);
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (SmbException e) {
                            // ...
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SmbException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private long firstTime = 0;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > PRESS_BACK_BUTTON_INTERVAL) {                                         //如果两次按键时间间隔大于2秒，则不退出
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;//更新firstTime
                    return true;
                } else {
                    //两次按键小于2秒时，退出应用
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstPressedTime < 2000) {
            super.onBackPressed();
        } else {
            Toast.makeText(MainActivity.this, R.string.press_again_exit, Toast.LENGTH_SHORT).show();
            firstPressedTime = System.currentTimeMillis();
        }
    }
}
