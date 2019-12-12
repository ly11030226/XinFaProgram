package com.jzl.xinfafristversion;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.jzl.xinfafristversion.application.MyApplication;
import com.jzl.xinfafristversion.base.Constant;
import com.jzl.xinfafristversion.base.FileManager;
import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.bean.ConfigBean;
import com.jzl.xinfafristversion.bean.MyBean;
import com.jzl.xinfafristversion.myInterface.IDisplayType;
import com.jzl.xinfafristversion.myInterface.ImageAndVideoTypeImpl;
import com.jzl.xinfafristversion.myInterface.ImageTypeImpl;
import com.jzl.xinfafristversion.myInterface.MarqueeTypeImpl;
import com.jzl.xinfafristversion.myInterface.VideoTypeImpl;
import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XMLDataOperator {
    private static final String TAG = "XMLDataOperator";
    private IDisplayType mDisplayType;
    private String dataString;
    private String configString;
    private String url = Constant.URL;
    private static final int DEFAULT_INDEX = 0;
    private MyBean.GroupsBean.GroupBean.AreasBean areas;
    private Context context;
    private String ip;
    private String port;
    private ConfigBean configBean;
    private MyBean myBean;
    private List<ConfigBean.Commands.Command> commandList;
    private String updateUrl;  //更新
    private String upgradeUrl; //升级
    private String settingUrl;  //配置
    private String id;
    private String interval;  //发送心跳包间隔时间

    public String getInterval() {
        return interval;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public XMLDataOperator(Context context) {
        this.context = context;
    }

    public boolean getDataFromXML(){
//        dataString = FileManager.loadXMLFromSDCard(FileManager.XML_DATA);
        dataString = FileManager.loadXMLFromSDCard(FileManager.XML_DATA);
        configString = FileManager.loadXMLFromSDCard(FileManager.XML_CONFIG);
//        configString = FileManager.loadXMLFromSDCard(FileManager.TEST_XML_CONFIG);
        if (TextUtils.isEmpty(dataString) || TextUtils.isEmpty(configString)) {
            MyLogger.e(TAG,"dataString or configString is null");
            return false;
        }else{
            return initData();
        }
    }

    private boolean initData() {
        XStream xStream = new XStream();
        xStream.processAnnotations(MyBean.class);
        myBean = (MyBean) xStream.fromXML(dataString);
        XStream configXStream = new XStream();
        configXStream.processAnnotations(ConfigBean.class);
        configBean = (ConfigBean) configXStream.fromXML(configString);
        if (myBean==null | configBean==null) {
            MyLogger.e(TAG,"mybean or configBean is null");
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
        areas = myBean.getGroups().getGroup().get(DEFAULT_INDEX).getAreas();
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

    public HashMap<Integer,String> getType(){
        if (areas==null) {
            return null;
        }
        final int areaSize = areas.getArea().size();
        HashMap<Integer,String> map = new HashMap<>();
        for (int i = 0; i < areaSize; i++) {
            ArrayList<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean>
                    fileBeanArrayList = areas.getArea().get(i).getFiles().getFile();
            if (fileBeanArrayList == null) {
                continue;
            }
            String type = areas.getArea().get(i).getInfo().getTname();
            map.put(i,type);
        }
        return map;
    }

    /**
     * 通过Areas 获取保存AreaBean的map
     * @return
     */
    public HashMap<String , MyBean.GroupsBean.GroupBean.AreasBean.AreaBean> getAreanBean(){
        if (areas==null) {
            return null;
        }
        final int areaSize = areas.getArea().size();
        HashMap<String , MyBean.GroupsBean.GroupBean.AreasBean.AreaBean> map = new HashMap<>();
        for (int i = 0; i < areaSize; i++) {
            ArrayList<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean>
                    fileBeanArrayList = areas.getArea().get(i).getFiles().getFile();
            if (fileBeanArrayList == null) {
                continue;
            }
            String type = areas.getArea().get(i).getInfo().getTname();
            map.put(type,areas.getArea().get(i));
        }
        return map;
    }

    public ViewGroup getViewGroup(){
        if (areas==null) {
            return null;
        }
        final int areaSize = areas.getArea().size();
        for (int i = 0; i < areaSize; i++) {
            ArrayList<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean>
                    fileBeanArrayList = areas.getArea().get(i).getFiles().getFile();
            if (fileBeanArrayList == null) {
                continue;
            }
            String type = areas.getArea().get(i).getInfo().getTname();
            DisPlayTypeFactory disPlayTypeFactory = new DisPlayTypeFactory(i);
            mDisplayType = disPlayTypeFactory.getDisplayType(type);
            if (mDisplayType!=null) {
                ViewGroup vg = mDisplayType.display(areas);
                return vg;
            }else{
                MyLogger.e(TAG,"DisPlayTypeFactory create Type is null");
            }
        }
        return null;
    }

    /**
     * 将更新写入到文件中
     */
    private void updateFileAndWriteFile(){
        writeTxtToFile(configString,FileManager.XML_DIR,FileManager.XML_CONFIG);
        writeTxtToFile(dataString,FileManager.XML_DIR, FileManager.XML_DATA);
    }
    /**
     * 更新内存中dataString 和 configString的数值
     * @param myBeanStr
     */
    public void updateMyBeanData(String myBeanStr) {
        this.dataString = myBeanStr;
        XStream stream = new XStream();
        stream.processAnnotations(MyBean.class);
        //更新 MyBean
        this.myBean = (MyBean) stream.fromXML(myBeanStr);

        //更换最新config xml
        this.configBean.setGroups(myBean.getGroups());
        XStream _stream = new XStream();
        stream.processAnnotations(ConfigBean.class);
        this.configString = _stream.toXML(configBean);
        setData();
        //将更新写入文件
        updateFileAndWriteFile();
    }


    private void writeTxtToFile(String content, String filePath, String fileName) {
        FileManager.writeTxtToFile(content,filePath,fileName);
    }

    class DisPlayTypeFactory{
        private int index;

        public DisPlayTypeFactory(int index) {
            this.index = index;
        }

        public IDisplayType getDisplayType(String type){
            MyLogger.i(TAG,"getDisplayType type ... "+type);
            if (Constant.TYPE_SHOW_MARQUEE.equalsIgnoreCase(type)) {
                return new MarqueeTypeImpl(index,context);
            }else if(Constant.TYPE_SHOW_IMAGE_AND_VIDEO.equalsIgnoreCase(type)){
                return new ImageAndVideoTypeImpl(index,context);
            }else if (Constant.TYPE_SHOW_IMAGE.equalsIgnoreCase(type)) {
                return new ImageTypeImpl(index,context);
            }else if (Constant.TYPE_SHOW_VIDEO.equalsIgnoreCase(type)) {
                return new VideoTypeImpl(index,context);
            }else{
                return null;
            }
        };
    }

    public String getId() {
        return id;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getUpgradeUrl() {
        return upgradeUrl;
    }

    public void setUpgradeUrl(String upgradeUrl) {
        this.upgradeUrl = upgradeUrl;
    }

    public String getSettingUrl() {
        return settingUrl;
    }

    public void setSettingUrl(String settingUrl) {
        this.settingUrl = settingUrl;
    }

    public ConfigBean getConfigBean() {
        return configBean;
    }

    public MyBean getMyBean() {
        return myBean;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
