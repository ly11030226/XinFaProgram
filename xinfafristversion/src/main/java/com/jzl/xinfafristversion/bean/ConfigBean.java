package com.jzl.xinfafristversion.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;

@XStreamAlias("root")
public class ConfigBean {
    @XStreamAlias("setting")
    public Setting setting;
    public static class Setting {

        @XStreamAlias("machine")
        public Machine machine;  //设备信息
        public static class Machine {
            public String vendor;//制造厂商
            public String system;//操作系统

            public String getVendor() {
                return vendor;
            }

            public void setVendor(String vendor) {
                this.vendor = vendor;
            }

            public String getSystem() {
                return system;
            }

            public void setSystem(String system) {
                this.system = system;
            }

            @Override
            public String toString() {
                return "Machine{" +
                        "vendor='" + vendor + '\'' +
                        ", system='" + system + '\'' +
                        '}';
            }
        }

        @XStreamAlias("software")
        public Software software;  //软件信息

        public static class Software {
            public String version;//版本号
            public String edition;//软件版本：0=单机版、1=联网版
            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;//软件版本
            }

            public String getEdition() {
                return edition;
            }

            public void setEdition(String edition) {
                this.edition = edition;
            }

            @Override
            public String toString() {
                return "Software{" +
                        "version='" + version + '\'' +
                        ", edition='" + edition + '\'' +
                        '}';
            }
        }

        @XStreamAlias("update")
        public Update update;//更新信息
        public static class Update {
            public String once;//启动时是否更新，0=false，非0=true，更新完成后恢复0
            public String datasouce;//更新时的数据源：server=访问服务器，xml=本地data.xml文件

            public String getOnce() {
                return once;
            }

            public void setOnce(String once) {
                this.once = once;
            }

            public String getDatasouce() {
                return datasouce;
            }

            public void setDatasouce(String datasouce) {
                this.datasouce = datasouce;
            }

            @Override
            public String toString() {
                return "Update{" +
                        "once='" + once + '\'' +
                        ", datasouce='" + datasouce + '\'' +
                        '}';
            }
        }


        @XStreamAlias("tip")
        public Tip tip;//提示信息
        public static class Tip {
            public String nobody;//没有area模块时提示，显示在屏幕正中
            public String nonetwork;//没有网络时提示，显示在屏幕右上角

            public String getNobody() {
                return nobody;
            }

            public void setNobody(String nobody) {
                this.nobody = nobody;
            }

            public String getNonetwork() {
                return nonetwork;
            }

            public void setNonetwork(String nonetwork) {
                this.nonetwork = nonetwork;
            }

            @Override
            public String toString() {
                return "Tip{" +
                        "nobody='" + nobody + '\'' +
                        ", nonetwork='" + nonetwork + '\'' +
                        '}';
            }
        }


        @XStreamAlias("connect")
        public Connect connect;//连接信息

        public static class Connect {
            public String id;//设备编号
            public String cip;//客户端ip地址
            public String cport;//客户端端口号
            public String sip;//服务器ip地址
            public String sport;//服务器端口号
            public String heart;//心跳通信间隔时间

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getCip() {
                return cip;
            }

            public void setCip(String cip) {
                this.cip = cip;
            }

            public String getCport() {
                return cport;
            }

            public void setCport(String cport) {
                this.cport = cport;
            }

            public String getSip() {
                return sip;
            }

            public void setSip(String sip) {
                this.sip = sip;
            }

            public String getSport() {
                return sport;
            }

            public void setSport(String sport) {
                this.sport = sport;
            }

            public String getHeart() {
                return heart;
            }

            public void setHeart(String heart) {
                this.heart = heart;
            }

        }




        @XStreamAlias("power")
        public Power power;//开关机信息

        public static class Power {
            public String state;//启动、禁用开关机设置
            public String on;//开机时间
            public String off;//关机时间

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }

            public String getOn() {
                return on;
            }

            public void setOn(String on) {
                this.on = on;
            }

            public String getOff() {
                return off;
            }

            public void setOff(String off) {
                this.off = off;
            }
        }

        @XStreamAlias("page")
        public Page page;//页面信息
        public static class Page {

            public String top;//上边距
            public String left;//左边距
            public String width;//宽度
            public String height;//高度



            public String getTop() {
                return top;
            }

            public void setTop(String top) {
                this.top = top;
            }

            public String getLeft() {
                return left;
            }

            public void setLeft(String left) {
                this.left = left;
            }

            public String getWidth() {
                return width;
            }

            public void setWidth(String width) {
                this.width = width;
            }

            public String getHeight() {
                return height;
            }

            public void setHeight(String height) {
                this.height = height;
            }
        }

        @Override
        public String toString() {
            return "Setting{" +
                    "machine=" + machine +
                    ", software=" + software +
                    ", update=" + update +
                    ", tip=" + tip +
                    ", connect=" + connect +
                    ", power=" + power +
                    ", page=" + page +
                    '}';
        }

        public Machine getMachine() {
            return machine;
        }

        public void setMachine(Machine machine) {
            this.machine = machine;
        }

        public Software getSoftware() {
            return software;
        }

        public void setSoftware(Software software) {
            this.software = software;
        }

        public Update getUpdate() {
            return update;
        }

        public void setUpdate(Update update) {
            this.update = update;
        }

        public Tip getTip() {
            return tip;
        }

        public void setTip(Tip tip) {
            this.tip = tip;
        }

        public Connect getConnect() {
            return connect;
        }

        public void setConnect(Connect connect) {
            this.connect = connect;
        }

        public Power getPower() {
            return power;
        }

        public void setPower(Power power) {
            this.power = power;
        }

        public Page getPage() {
            return page;
        }

        public void setPage(Page page) {
            this.page = page;
        }
    }
    @XStreamAlias("commands")
    public  Commands commands;//指令集
    public static class Commands {
        @XStreamImplicit(itemFieldName="command")
        public ArrayList<Command> command;
        public static class Command {
            public String type;//命令类型
            public String url;//访问的URL地址

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            @Override
            public String toString() {
                return "Command{" +
                        "type='" + type + '\'' +
                        ", url='" + url + '\'' +
                        '}';
            }

        }

        public ArrayList<Command> getCommand() {
            return command;
        }

        public void setCommand(ArrayList<Command> command) {
            this.command = command;
        }

        @Override
        public String toString() {
            return "Commands{" +
                    "command=" + command +
                    '}';
        }

    }

    public Commands getCommands() {
        return commands;
    }

    public void setCommands(Commands commands) {
        this.commands = commands;
    }

    public MyBean.GroupsBean groups; //方案信息

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public MyBean.GroupsBean getGroups() {
        return groups;
    }

    public void setGroups(MyBean.GroupsBean groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "ConfigBean{" +
                "setting=" + setting +
                ", groups=" + groups +
                '}';
    }


}
