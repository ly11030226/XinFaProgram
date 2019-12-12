package com.jzl.xinfafristversion.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;

@XStreamAlias("root")
public class MyBean {
    @XStreamAlias("page")
    public PageBean page;
    public static class PageBean{
            public String title;
            public String top;
            public String left;
            public String width;
            public String height;

        @Override
        public String toString() {
            return "PageBean{" +
                    "title='" + title + '\'' +
                    ", top='" + top + '\'' +
                    ", left='" + left + '\'' +
                    ", width='" + width + '\'' +
                    ", height='" + height + '\'' +
                    '}';
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

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
    @XStreamAlias("groups")
    public GroupsBean groups;//任务信息
    public static class GroupsBean{
        @XStreamImplicit(itemFieldName="group")
        public ArrayList<GroupBean> group;//任务内容

        public static class GroupBean {
            @Override
            public String toString() {
                return "GroupBean{" +
                        "info=" + info +
                        ", areas=" + areas +
                        '}';
            }

            @XStreamAlias("info")
            public InfoBean info;//方案信息
            public static class InfoBean {
                public String name;//名称
                public String index;//排序
                public String time;//时间
                public String bgcolor;//背景颜色
                public String bgimage;//背景图片
                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getIndex() {
                    return index;
                }

                public void setIndex(String index) {
                    this.index = index;
                }

                public String getTime() {
                    return time;
                }

                public void setTime(String time) {
                    this.time = time;
                }

                public String getBgcolor() {
                    return bgcolor;
                }

                public void setBgcolor(String bgcolor) {
                    this.bgcolor = bgcolor;
                }

                public String getBgimage() {
                    return bgimage;
                }

                public void setBgimage(String bgimage) {
                    this.bgimage = bgimage;
                }

                @Override
                public String toString() {
                    return "InfoBean{" +
                            "name='" + name + '\'' +
                            ", index='" + index + '\'' +
                            ", time='" + time + '\'' +
                            ", bgcolor='" + bgcolor + '\'' +
                            ", bgimage='" + bgimage + '\'' +
                            '}';
                }
            }
            @XStreamAlias("areas")
            public AreasBean areas;
            public static class AreasBean {

                @XStreamImplicit(itemFieldName="area")
                public ArrayList<AreaBean> area;
                public static class AreaBean {
                    @Override
                    public String toString() {
                        return "AreaBean{" +
                                "info=" + info +
                                ", files=" + files +
                                '}';
                    }

                    @XStreamAlias("info")
                    public InfoBean info;//区域信息
                    public static class InfoBean {
                        public String tname;//名称
                        public String pname;
                        public String type1;//跑马灯：left=从左到右，top=从下到上
                        public String top;//上边距
                        public String left;//左边距
                        public String width;//宽度
                        public String height;//高度
                        public String voice1;//声音大小,0~1
                        public String voice2;//音频文件地址
                        public String ffamily;//字体
                        public String fsize;//字号
                        public String fcolor;//字体颜色
                        public String fitalic;//是否斜体
                        public String fweight;//是否粗体
                        public String fdecoration;//是否下划线
                        public String bgcolor;//背景颜色
                        public String bgimage;//背景图片

                        public String getPname() {
                            return pname;
                        }

                        public void setPname(String pname) {
                            this.pname = pname;
                        }

                        public String getTname() {
                            return tname;
                        }

                        public void setTname(String tname) {
                            this.tname = tname;
                        }

                        public String getType1() {
                            return type1;
                        }

                        public void setType1(String type1) {
                            this.type1 = type1;
                        }

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

                        public String getVoice1() {
                            return voice1;
                        }

                        public void setVoice1(String voice1) {
                            this.voice1 = voice1;
                        }

                        public String getVoice2() {
                            return voice2;
                        }

                        public void setVoice2(String voice2) {
                            this.voice2 = voice2;
                        }

                        public String getFfamily() {
                            return ffamily;
                        }

                        public void setFfamily(String ffamily) {
                            this.ffamily = ffamily;
                        }

                        public String getFsize() {
                            return fsize;
                        }

                        public void setFsize(String fsize) {
                            this.fsize = fsize;
                        }

                        public String getFcolor() {
                            return fcolor;
                        }

                        public void setFcolor(String fcolor) {
                            this.fcolor = fcolor;
                        }

                        public String getFitalic() {
                            return fitalic;
                        }

                        public void setFitalic(String fitalic) {
                            this.fitalic = fitalic;
                        }

                        public String getFweight() {
                            return fweight;
                        }

                        public void setFweight(String fweight) {
                            this.fweight = fweight;
                        }

                        public String getFdecoration() {
                            return fdecoration;
                        }

                        public void setFdecoration(String fdecoration) {
                            this.fdecoration = fdecoration;
                        }

                        public String getBgcolor() {
                            return bgcolor;
                        }

                        public void setBgcolor(String bgcolor) {
                            this.bgcolor = bgcolor;
                        }

                        public String getBgimage() {
                            return bgimage;
                        }

                        public void setBgimage(String bgimage) {
                            this.bgimage = bgimage;
                        }

                        @Override
                        public String toString() {
                            return "InfoBean{" +
                                    "tname='" + tname + '\'' +
                                    ", type1='" + type1 + '\'' +
                                    ", top='" + top + '\'' +
                                    ", left='" + left + '\'' +
                                    ", width='" + width + '\'' +
                                    ", height='" + height + '\'' +
                                    ", voice1='" + voice1 + '\'' +
                                    ", voice2='" + voice2 + '\'' +
                                    ", ffamily='" + ffamily + '\'' +
                                    ", fsize='" + fsize + '\'' +
                                    ", fcolor='" + fcolor + '\'' +
                                    ", fitalic='" + fitalic + '\'' +
                                    ", fweight='" + fweight + '\'' +
                                    ", fdecoration='" + fdecoration + '\'' +
                                    ", bgcolor='" + bgcolor + '\'' +
                                    ", bgimage='" + bgimage + '\'' +
                                    '}';
                        }
                    }
                    @XStreamAlias("files")
                    public FilesBean files;
                    public static class FilesBean {
                        @XStreamImplicit(itemFieldName="file")
                        public ArrayList<FileBean> file;//文件信息

                        @Override
                        public String toString() {
                            return "FilesBean{" +
                                    "file=" + file +
                                    '}';
                        }

                        public static class FileBean {
                            public String id;//编号
                            public String format;//文件类型
                            public String path;//文件路径
                            public String time;//翻页的时间间隔

                            public String getId() {
                                return id;
                            }

                            public void setId(String id) {
                                this.id = id;
                            }

                            public String getFormat() {
                                return format;
                            }

                            public void setFormat(String format) {
                                this.format = format;
                            }

                            public String getPath() {
                                return path;
                            }

                            public void setPath(String path) {
                                this.path = path;
                            }

                            public String getTime() {
                                return time;
                            }

                            public void setTime(String time) {
                                this.time = time;
                            }

                            @Override
                            public String toString() {
                                return "FileBean{" +
                                        "id='" + id + '\'' +
                                        ", format='" + format + '\'' +
                                        ", path='" + path + '\'' +
                                        ", time='" + time + '\'' +
                                        '}';
                            }
                        }

                        public ArrayList<FileBean> getFile() {
                            return file;
                        }

                        public void setFile(ArrayList<FileBean> file) {
                            this.file = file;
                        }
                    }

                    public InfoBean getInfo() {
                        return info;
                    }

                    public void setInfo(InfoBean info) {
                        this.info = info;
                    }

                    public FilesBean getFiles() {
                        return files;
                    }

                    public void setFiles(FilesBean files) {
                        this.files = files;
                    }
                }

                public ArrayList<AreaBean> getArea() {
                    return area;
                }

                public void setArea(ArrayList<AreaBean> area) {
                    this.area = area;
                }

                @Override
                public String toString() {
                    return "AreasBean{" +
                            "area=" + area +
                            '}';
                }
            }


            public InfoBean getInfo() {
                return info;
            }

            public void setInfo(InfoBean info) {
                this.info = info;
            }

            public AreasBean getAreas() {
                return areas;
            }

            public void setAreas(AreasBean areas) {
                this.areas = areas;
            }
        }

        public ArrayList<GroupBean> getGroup() {
            return group;
        }

        public void setGroup(ArrayList<GroupBean> group) {
            this.group = group;
        }

        @Override
        public String toString() {
            return "GroupsBean{" +
                    "group=" + group +
                    '}';
        }

    }

    public PageBean getPage() {
        return page;
    }

    public void setPage(PageBean page) {
        this.page = page;
    }

    public GroupsBean getGroups() {
        return groups;
    }

    public void setGroups(GroupsBean groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "MyBean{" +
                "page=" + page +
                ", groups=" + groups +
                '}';
    }

}
