package com.jzl.xinfafristversion;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.jzl.xinfafristversion.base.Constant;
import com.jzl.xinfafristversion.base.FileManager;
import com.jzl.xinfafristversion.base.MyLogger;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;


public class FtpService extends Service {

    private static final String TAG = "FtpService";
    private FtpServer server;
    private String user = Constant.FTP_USER;
    private String password = Constant.FTP_PWD;
    private static String rootPath;
    private int port = Constant.FTP_PORT;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        rootPath = FileManager.Resource_DIR;
        try {
            init();
            MyLogger.d(TAG,"启动ftp服务成功");
        } catch (FtpException e) {
            e.printStackTrace();
            MyLogger.d(TAG,"启动ftp服务失败");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        MyLogger.d(TAG,"关闭ftp服务");
    }

    /**
     * 初始化
     *
     * @throws FtpException
     */
    public void init() throws FtpException {
        release();
        startFtp();
    }

    private void startFtp() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();

        //设置访问用户名和密码还有共享路径
        BaseUser baseUser = new BaseUser();
        baseUser.setName(user);
        baseUser.setPassword(password);
        baseUser.setHomeDirectory(rootPath);

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        baseUser.setAuthorities(authorities);
        serverFactory.getUserManager().save(baseUser);


        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port); //设置端口号 非ROOT不可使用1024以下的端口
        serverFactory.addListener("default", factory.createListener());

        server = serverFactory.createServer();
        server.start();
    }



    /**
     * 释放资源
     */
    public void release() {
        stopFtp();
    }

    private void stopFtp() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

}
