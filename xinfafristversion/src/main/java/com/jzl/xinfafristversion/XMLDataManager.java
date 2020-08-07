package com.jzl.xinfafristversion;

import android.content.Context;
import android.text.TextUtils;

import com.jzl.xinfafristversion.application.MyApplication;
import com.jzl.xinfafristversion.base.Constant;
import com.jzl.xinfafristversion.base.FileManager;
import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.bean.ConfigBean;
import com.jzl.xinfafristversion.bean.MyBean;
import com.jzl.xinfafristversion.view.ResLoopView;
import com.jzl.xinfafristversion.view.ResViewGroup;
import com.jzl.xinfafristversion.view.TestMarqueeView;
import com.thoughtworks.xstream.XStream;
import com.jzl.xinfafristversion.bean.MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 与XMLDataOperator 相同 这里写成单例模式
 * @author Ly
 */
public class XMLDataManager {
    private static final String TAG = "XMLDataManager";

    private static boolean isDefaultData = true;
    private static String dataString;
    private static String configString;
    private Context context;
    public static String ip;
    public static String port;
    private static ConfigBean configBean;
    private static MyBean myBean;
    private static List<ConfigBean.Commands.Command> commandList;
    private static String updateUrl;  //更新
    private static String upgradeUrl; //升级
    private static String settingUrl;  //配置
    public static String id;
    public static String interval;  //发送心跳包间隔时间
    public static String url = Constant.URL;
    private static final int DEFAULT_INDEX = 0;
    private static MyBean.GroupsBean groupsBean;
    private ArrayList<MyBean.GroupsBean.GroupBean> groupBeanArrayList;
    private ArrayList<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean> areaBeanArrayList;

    private XMLDataManager(){}
    private static XMLDataManager instance;
    public static XMLDataManager getInstance(){
        if (instance == null) {
            synchronized (XMLDataManager.class){
                if (instance == null) {
                    instance = new XMLDataManager();
                }
            }
        }
        return instance;
    }

    public void getDataFromXML(Context context){
        this.context = context;
        dataString = FileManager.loadXMLFromSDCard(FileManager.XML_DATA);
        configString = FileManager.loadXMLFromSDCard(FileManager.XML_CONFIG);
        if (TextUtils.isEmpty(dataString) || TextUtils.isEmpty(configString)) {
            MyLogger.e(TAG,"dataString or configString is null");
            isDefaultData = true;
        }else{
            boolean isInit = initData();
            if (isInit) {
                isDefaultData = false;
            } else {
                isDefaultData = true;
            }
        }
    }

    private boolean initData() {
        XStream xStream = new XStream();
        xStream.processAnnotations(MyBean.class);
        myBean = (MyBean) xStream.fromXML(dataString);
        XStream configXStream = new XStream();
        configXStream.processAnnotations(ConfigBean.class);
        configBean = (ConfigBean) configXStream.fromXML(configString);
        if (myBean==null || configBean==null) {
            MyLogger.e(TAG,"mybean or configBean is null");
            return false;
        }
        if (myBean.getGroups().getGroup()==null ||
                myBean.getGroups().getGroup().size() == 0||
                myBean.getGroups().getGroup().get(0).getAreas() == null ||
                myBean.getGroups().getGroup().get(0).getInfo() == null ) {
            MyLogger.e(TAG,"mybean groups data is null");
            return false;
        }
        setData();
        return true;
    }

    private void setData(){
        interval = configBean.getSetting().getConnect().getHeart();
        id = configBean.getSetting().getConnect().getId();
        ip = configBean.getSetting().getConnect().getSip();
        port = configBean.getSetting().getConnect().getSport();
        url =
                "http://" +
                        configBean.getSetting().getConnect().getSip() +
                        ":" +
                        configBean.getSetting().getConnect().getSport() +
                        "/interface/interface1.aspx?rt=snapshot&es=" +
                        configBean.getSetting().getConnect().getId();
        MyLogger.i(TAG,"url ... "+url);
        //TODO 多个组  可以整体更换组（相当于音乐的播放列表） 目前只显示一个组 因此 DEFAULT_INDEX = 0
        groupBeanArrayList = myBean.getGroups().getGroup();
        MyBean.GroupsBean.GroupBean groupBean = groupBeanArrayList.get(DEFAULT_INDEX);
        areaBeanArrayList = groupBean.getAreas().getArea();
        //获取指令集
        commandList = configBean.getCommands().getCommand();
        MyApplication.COMMAND_HASHMAP.clear();
        if (commandList!=null) {
            for (int i = 0; i < commandList.size(); i++) {
                ConfigBean.Commands.Command command = commandList.get(i);
                MyApplication.COMMAND_HASHMAP.put(command.getType(),command.getUrl());
            }
        }
    }

    public void showData(ResViewGroup rvg){
        try {
            if (areaBeanArrayList!=null && areaBeanArrayList.size()>0) {
                for (int i = 0; i < areaBeanArrayList.size(); i++) {
                    MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean = areaBeanArrayList.get(i);
                    if (areaBean.info.getTname().equals("跑马灯")) {
                        //                    MyMarqueeView myMarqueeView = new MyMarqueeView(context,areaBean);
                        //                    rvg.addView(myMarqueeView);
                        //                    myMarqueeView.startMarqueeView();
                        TestMarqueeView myMarqueeView = new TestMarqueeView(context,areaBean);
                        rvg.addView(myMarqueeView);
                        myMarqueeView.startMarqueeView();
                    }else{
                        ArrayList<FileBean>
                                list = areaBean.getFiles().getFile();
                        for (int j = 0; j < list.size(); j++) {
                            FileBean fileBean = list.get(j);
                            String path = fileBean.getPath();
                        }
                        ResLoopView resLoopView = new ResLoopView(context);
                        resLoopView.initData(areaBean);
                        rvg.addView(resLoopView);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDefaultData() {
        return isDefaultData;
    }

    public String getDatsString(){
        return dataString;
    }

    public String getConfigString(){
        return configString;
    }
    public ConfigBean getConfigBean(){
        return configBean;
    }
    public MyBean getMyBean(){
        return myBean;
    }
    public void setMyBean(MyBean m){
        myBean = m;
    }
}
