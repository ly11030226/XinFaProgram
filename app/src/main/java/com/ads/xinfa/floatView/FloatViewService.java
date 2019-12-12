package com.ads.xinfa.floatView;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.ads.xinfa.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


/**
 * 操作悬浮窗口的Service
 */
public class FloatViewService extends Service{

    private static final String TAG = "FloatViewService";
    //定义浮动窗口布局
    private LinearLayout mFloatLayout;
    private WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;

    private ImageButton mFloatView;

    /**
     * 设置能否拖动
     */
    private final static boolean canTouch = false;


    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(TAG, "onCreate");
        createFloatView();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @SuppressWarnings("static-access")
    @SuppressLint("InflateParams")
    private void createFloatView(){
        wmParams = new WindowManager.LayoutParams();
        //通过getApplication获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        //设置window type
        if (Build.VERSION.SDK_INT > 25) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
//        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
//        wmParams.format = PixelFormat.RGBA_8888;
         wmParams.format = PixelFormat.TRANSLUCENT;     //设置图片格式，效果为背景透明
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为右侧置顶
        wmParams.gravity = Gravity.RIGHT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 152;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.alert_window_menu, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        //浮动窗口按钮
        mFloatView = (ImageButton) mFloatLayout.findViewById(R.id.alert_window_imagebtn);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        if (canTouch) {
            //设置监听浮动窗口的触摸移动
            mFloatView.setOnTouchListener(new View.OnTouchListener(){

                boolean isClick;

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //                        MyLogger.i(TAG,"MotionEvent.ACTION_DOWN");
                            mFloatView.setBackgroundResource(R.mipmap.float_view_press);
                            isClick = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            //                        MyLogger.i(TAG,"MotionEvent.ACTION_MOVE");
                            isClick = true;
                            // getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                            wmParams.x = (int) event.getRawX()
                                    - mFloatView.getMeasuredWidth() / 2;
                            // 减25为状态栏的高度
                            wmParams.y = (int) event.getRawY()
                                    - mFloatView.getMeasuredHeight() / 2 - 75;
                            // 刷新
                            mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                            return false;
                        case MotionEvent.ACTION_UP:
                            //                        MyLogger.i(TAG,"MotionEvent.ACTION_UP");
                            mFloatView.setBackgroundResource(R.mipmap.float_view_normal);
                            return false;// 此处返回false则属于移动事件，返回true则释放事件，可以出发点击否。

                        default:
                            break;
                    }
                    return false;
                }
            });
        }
        mFloatView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                new Thread(){
                    @Override
                    public void run() {
//                        execByRuntime("input keyevent 4");
                        execShellCmd("input keyevent 4");
                    }
                }.start();
            }
        });
    }


    /**
     * 执行shell命令
     *
     * @param cmd
     */
    private void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    /**
     * 执行shell 命令， 命令中不必再带 adb shell
     *
     * @param cmd
     * @return Sting  命令执行在控制台输出的结果
     */
    public static String execByRuntime(String cmd) {
        Process process = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);

            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = bufferedReader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != inputStreamReader) {
                try {
                    inputStreamReader.close();
                } catch (Throwable t) {
                    //
                }
            }
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (Throwable t) {
                    //
                }
            }
            if (null != process) {
                try {
                    process.destroy();
                } catch (Throwable t) {
                    //
                }
            }
        }
    }


    @Override
    public void onDestroy(){
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        if(mFloatLayout != null){
            //移除悬浮窗口
            mWindowManager.removeView(mFloatLayout);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

