package com.jzl.xinfafristversion.utils;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.jzl.xinfafristversion.R;
import com.jzl.xinfafristversion.application.MyApplication;
import com.jzl.xinfafristversion.base.Constant;
import com.jzl.xinfafristversion.base.FileManager;
import com.jzl.xinfafristversion.base.MyLogger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.R.attr.scheme;


public class BaseUtils {
    private static final String TAG = "BaseUtils";

    public static void hideInputMode(Activity mActivity) {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);
        }
    }


    public static boolean isInstalled(Context context, String packageName) {
        // 微信 com.tencent.mm
        final PackageManager packageManager = context.getPackageManager();//获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);//获取所有已安装程序的包信息
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);//判断pName中是否有目标程序的包名，有TRUE，没有FALSE
    }

    /**
     * 打开已经存在的应用
     *
     * @param context
     * @param mPackageName
     * @param url
     * @return true 成功打开  false打开失败
     */
    public static boolean openExistApp(Context context, String mPackageName, String url) {
        boolean isInstalled = BaseUtils.isInstalled(context, mPackageName);
        MyLogger.i(TAG, "isInstalled ... " + isInstalled);
        if (isInstalled) {
            PackageManager pm = context.getPackageManager(); // 获得PackageManager对象
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 通过查询，获得所有ResolveInfo对象.
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL);
            // 调用系统排序 ， 根据name排序
            // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
            Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
            String name = "";
            String packageName = "";
            for (ResolveInfo reInfo : resolveInfos) {
                if (reInfo.activityInfo.packageName.equals(mPackageName)) {
                    name = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
                    packageName = reInfo.activityInfo.packageName; // 获得应用程序的包名
                    String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
                    Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
                    break;
                }
            }
            if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(name)) {
                ComponentName componentName = new ComponentName(packageName, name);
                Intent intent = new Intent();
                intent.setComponent(componentName);
                if (!TextUtils.isEmpty(url)) {
                    Uri mUri = Uri.parse(url);
                    MyLogger.i(TAG, "scheme ... " + scheme);
                    intent.setData(mUri);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } else {
                return false;
            }
        } else {
            MyLogger.e(TAG, "have no " + mPackageName + " application");
            return false;
        }
    }

    /**
     * dp转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dpToPx(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int pxToDp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */
    public static int pxToSp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param context
     * @param spValue
     * @return
     */
    public static int spToPx(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static String getShowdate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        // 前面的lSysTime是秒数，先乘1000得到毫秒数，再转为java.util.Date类型
        Date dt = new Date(time);
        // String sDateTime = sdf.format(dt);
        Date now = new Date();
        long l = now.getTime() - dt.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        String str = "";
        if (day < 1) {
            if (hour < 1) {
                if (min < 1) {
                    str = "刚刚";
                } else {
                    str = min + "分钟前";
                }
            } else {
                str = hour + "小时前";
            }

        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
            str = dateFormat.format(dt);
        }

        return str;
    }

    public static String longToString(long times) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(times);
        return sdf.format(date);
    }

    public static String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();

            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    strBuf.append(Integer.toHexString(0xff & encryption[i]));
                }
            }

            return strBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    //替换特俗字符
    public static String ignoreTxt(String input) {
        return input.replace("|", "\\|").replace("$", "\\$").replace("*", "\\*").replace("+", "\\+").replace("[", "\\[").replace("?", "\\?").replace("^", "\\^").replace("{", "\\{").replace("}", "\\}").replace("？", "\\？");
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static void operaFileData(String path, String name, byte[] by) {

        try {
            File file = new File(path, name);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(by);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

    }

    public static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static byte[] File2byte(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }


    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return "ip=" + inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return "ip=" + ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
    }

    /**
     * 跳转到系统设置界面
     *
     * @param activity
     */
    public static void jumpSystemSet(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", MyApplication.getInstance().getPackageName(), null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    /**
     * 图片转成string
     *
     * @param bitmap
     * @return
     */
    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);

    }

    /**
     * string转成bitmap
     *
     * @param st
     */
    public static Bitmap convertStringToBitmap(String st) {
        // OutputStream out;
        Bitmap bitmap = null;
        try {
            // out = new FileOutputStream("/sdcard/aa.jpg");
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 正则表达式:验证身份证
     */
    public static final String REGEX_ID_CARD = "(^\\d{15}$)|(^\\d{17}([0-9]|X)$)";

    /**
     * 校验身份证
     *
     * @param idCard
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isIDCard(String idCard) {
        return Pattern.matches(REGEX_ID_CARD, idCard);

    }

    /**
     * 通过resouceid获取字符串
     *
     * @param id
     * @return
     */
    public static String getStringByResouceId(int id) {
        return MyApplication.getInstance().getResources().getString(id);
    }

    /**
     * 切断inputMethod与Activity的关联，让Activity可被回收
     *
     * @param destContext
     */
    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }

        String[] viewArray = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
        Field filed;
        Object filedObject;

        for (String view : viewArray) {
            try {
                filed = inputMethodManager.getClass().getDeclaredField(view);
                if (!filed.isAccessible()) {
                    filed.setAccessible(true);
                }
                filedObject = filed.get(inputMethodManager);
                if (filedObject != null && filedObject instanceof View) {
                    View fileView = (View) filedObject;
                    if (fileView.getContext() == destContext) { // 被InputMethodManager持有引用的context是想要目标销毁的
                        filed.set(inputMethodManager, null); // 置空，破坏掉path to gc节点
                    } else {
                        break;// 不是想要目标销毁的，即为又进了另一层界面了，不要处理，避免影响原逻辑,也就不用继续for循环了
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static LocalBroadcastManager getLocalManagerBroadcast(Context context) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        return lbm;
    }

    public static void registReceiver(BroadcastReceiver receiver, String action) {
        IntentFilter filter = new IntentFilter(action);
        LocalBroadcastManager.getInstance(MyApplication.getInstance()).registerReceiver(receiver, filter);
    }

    public static void unRegistReceiver(BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(MyApplication.getInstance()).unregisterReceiver(receiver);
    }

    public static void sendReceiver(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
    }

    public static void sendReceiver(Intent intent) {
        LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
    }


    /**
     * 得到屏幕信息
     * getScreenDisplayMetrics().heightPixels 屏幕高
     * getScreenDisplayMetrics().widthPixels 屏幕宽
     *
     * @return
     */
    public static DisplayMetrics getScreenDisplayMetrics(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = manager.getDefaultDisplay();
        display.getMetrics(displayMetrics);

        return displayMetrics;

    }

    public static void jumpToSystemContact(Activity activity, int requestCode) {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Intent intent = new Intent(Intent.ACTION_PICK, uri);
        activity.startActivityForResult(intent, requestCode);
    }


    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(context, permission);
                if (rest == PackageManager.PERMISSION_GRANTED) {
                    result = true;
                } else {
                    result = false;
                }
            } catch (Exception e) {
                result = false;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }
        return result;
    }

    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = null;
            if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                device_id = tm.getDeviceId();
            }
            String mac = null;
            FileReader fstream = null;
            try {
                fstream = new FileReader("/sys/class/net/wlan0/address");
            } catch (FileNotFoundException e) {
                fstream = new FileReader("/sys/class/net/eth0/address");
            }
            BufferedReader in = null;
            if (fstream != null) {
                try {
                    in = new BufferedReader(fstream, 1024);
                    mac = in.readLine();
                } catch (IOException e) {
                } finally {
                    if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            json.put("mac", mac);
            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }
            if (TextUtils.isEmpty(device_id)) {
                device_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            json.put("device_id", device_id);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取渠道名
     *
     * @param context 此处习惯性的设置为activity，实际上context就可以
     * @return 如果没有获取成功，那么返回值为空
     */
    public static String getChannelName(Context context) {
        if (context == null) {
            return null;
        }
        String channelName = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，而不是某activity标签中，所以用ApplicationInfo
                ApplicationInfo applicationInfo = packageManager.
                        getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = String.valueOf(applicationInfo.metaData.get("UMENG_CHANNEL"));
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return channelName;
    }

    /**
     * 利用java原生的摘要实现SHA256加密
     *
     * @param str 加密后的报文
     * @return
     */
    public static String getSHA256StrJava(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 将byte转为16进制
     *
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * 获取讯联支付宝预支付的订单号
     * 订单号 规则 ALP{6位随机数}_{借款订单号}
     *
     * @return
     */
    public static String getXLAlipayOrdernum(String orderNo) {
        String randomNum = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        return "ALP" + randomNum + "_" + orderNo;
    }

    /**
     * 获取6位随机数
     *
     * @return
     */
    public static String getSixRandomNum() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }

    /**
     * 获取3位随机数
     *
     * @return
     */
    public static String getThreeRandomNum() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100));
    }

    public static String getRandomStr() {
        Random rd = new Random();
        String str = "";
        for (int i = 0; i < 4; i++) {
            // 你想生成几个字符的，就把9改成几，如果改成１,那就生成一个随机字母．
            str = str + (char) (Math.random() * 26 + 'a');
        }
        return str;
    }

    /**
     * 创建讯联需要的格式（订单金额）
     * 单位为分，不够左边补零，固定12位
     *
     * @param sourceMoney 本订单原有金额 单位元
     * @return 讯联需要传的订单金额 单位为分，12位不够补零
     */
    public static String getMLTxamt(String sourceMoney) {
        //固定12位
        String zeros = "000000000000";
        //转换为分
        String newSourceMoney = String.valueOf((int) (Float.valueOf(sourceMoney) * 100));
        MyLogger.i(TAG, "sourceMoney ... " + sourceMoney + "  newSourceMoney ... " + newSourceMoney);
        int length = 12 - newSourceMoney.length();
        String zero = zeros.substring(0, length);
        String result = zero + newSourceMoney;
        MyLogger.i(TAG, result);
        return result;
    }

    /**
     * 获取猛犸需要的tick
     *
     * @return 时间戳+随机6位数
     */
    public static String getMaxentTick() {
        String maxentTick = System.currentTimeMillis() + getSixRandomNum();
        MyLogger.d(TAG, "getMaxentTick ... " + maxentTick);
        return maxentTick;
    }

    /**
     * 将字符串数组转换成List<String>形式
     *
     * @param arrayId
     * @return
     */
    public static List<String> changeListDataFromArray(Context context, int arrayId) {
        String[] strs = context.getResources().getStringArray(arrayId);
        return Arrays.asList(strs);
    }


    /**
     * 将字符串List集合转换成HashMap
     *
     * @param data
     * @return
     */
    public static LinkedHashMap<String, String> getMapFromArrayListData(List<String> data) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String str : data) {
            if (str.contains("=")) {
                String[] contents = str.split("=");
                map.put(contents[1], contents[0]);  //将 显示内容作为key  数值当做value
            }
        }
        return map;
    }

    /**
     * 获取一个HashMap 从给的array资源中
     *
     * @param context
     * @param arrayId
     * @return
     */
    public static LinkedHashMap<String, String> getMapFromArrayId(Context context, int arrayId) {
        String[] strs = context.getResources().getStringArray(arrayId);
        return getMapFromArrayListData(Arrays.asList(strs));
    }

    /**
     * 得到资源文件中图片的Uri
     *
     * @param context 上下文对象
     * @param id      资源id
     * @return Uri
     */
    public static Uri getUriFromDrawableRes(Context context, int id) {
        Resources resources = context.getResources();
        String path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(id) + "/" + resources.getResourceTypeName(id) + "/" + resources.getResourceEntryName(id);
        return Uri.parse(path);
    }

    public static List<String> getContentBySize(List<String[]> allAttributeList) {
        List<String> result = new ArrayList<String>();
        if (allAttributeList == null && allAttributeList.size() == 0) {
            return result;
        }
        switch (allAttributeList.size()) {
            case 1:
                calculate1Size(allAttributeList, result);
                break;
            case 2:
                calculate2Size(allAttributeList, result);
                break;
            case 3:
                calculate3Size(allAttributeList, result);
                break;
            case 4:

            default:
                break;
        }
        return result;
    }

    private static void calculate3Size(List<String[]> allAttributeList, List<String> result) {
        String[] threestrs1 = allAttributeList.get(0);
        String[] threestrs2 = allAttributeList.get(1);
        String[] threestrs3 = allAttributeList.get(2);
        String s1 = "";
        String s2 = "";
        String s3 = "";

        for (int i = 0; i < threestrs1.length; i++) {
            s1 = threestrs1[i];
            for (int j = 0; j < threestrs2.length; j++) {
                s2 = threestrs2[j];
                for (int k = 0; k < threestrs3.length; k++) {
                    s3 = threestrs3[k];
                    String s = s1 + ";" + s2 + ";" + s3;
                    result.add(s);
                }
            }
        }
    }

    private static void calculate2Size(List<String[]> allAttributeList, List<String> result) {
        String[] twostrs1 = (String[]) allAttributeList.get(0);
        String[] twostrs2 = (String[]) allAttributeList.get(1);
        String s = "";
        String s1 = "";
        String s2 = "";
        for (int i = 0; i < twostrs1.length; i++) {
            s1 = twostrs1[i];
            for (int j = 0; j < twostrs2.length; j++) {
                s2 = twostrs2[j];
                s = s1 + ";" + s2;
                result.add(s);
            }
        }
    }

    private static void calculate1Size(List<String[]> allAttributeList, List<String> result) {
        String[] onestrs1 = (String[]) allAttributeList.get(0);
        for (int i = 0; i < onestrs1.length; i++) {
            result.add((String) onestrs1[i]);
        }
    }

    /**
     * 获取本机ip
     *
     * @return
     */
    public static String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostIp;
    }

    /**
     * 得到基本URL
     * 例如：http://192.168.0.2:16888/interface/interface1.aspx?
     *
     * @param ip
     * @param port
     * @return
     */
    public static String getURL(String ip, String port) {
        String result = Constant.URL_PREFIX + ip + ":" + port + Constant.URL_PATH;
        return result;
    }

    public static String getSMBURL() {
        String url = "";
        return url;
    }
    public static void showQRCode(ImageView ivBarcode, String returnContent){
        if (ivBarcode==null) {
            return;
        }
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap;
        try {
            bitmap = barcodeEncoder.encodeBitmap(
                    returnContent,
                    BarcodeFormat.QR_CODE,
                    Tools.dip2px(ivBarcode.getContext(),140),
                    Tools.dip2px(ivBarcode.getContext(),140));
            if (bitmap!=null) {
                ivBarcode.setImageBitmap(bitmap);
            }

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    public static boolean showQRCodeHaveImg(ImageView ivBarcode, String returnContent) throws FileNotFoundException {
        if (ivBarcode==null) {
            return false;
        }
        final String filePath = FileManager.TEMP_DIR + "qr_" + System.currentTimeMillis() + ".jpg";
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = null;
        Bitmap resultBmp = null;
        try {
            bitmap = barcodeEncoder.encodeBitmap(
                    returnContent,
                    BarcodeFormat.QR_CODE,
                    Tools.dip2px(ivBarcode.getContext(),140),
                    Tools.dip2px(ivBarcode.getContext(),140));
            if (bitmap!=null) {
                Bitmap logoBmp = BitmapFactory.decodeResource(
                        ivBarcode.getResources(), R.mipmap.logo_qr_code);
                resultBmp = addLogo(bitmap,logoBmp);
            }
            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            boolean r = resultBmp != null &&resultBmp.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
            ivBarcode.setImageBitmap(resultBmp);
            return r;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }
}
