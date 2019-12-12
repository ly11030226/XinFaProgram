package com.jzl.xinfafristversion.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("root")
public class UpdateBean {
    @XStreamAlias("file")
    public FileBean file;
    public static class FileBean{
        public String id;
        public String name;
        public String format;
        public String download;
        public String save;
        public String hash;
        public String exists;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getDownload() {
            return download;
        }

        public void setDownload(String download) {
            this.download = download;
        }

        public String getSave() {
            return save;
        }

        public void setSave(String save) {
            this.save = save;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getExists() {
            return exists;
        }

        public void setExists(String exists) {
            this.exists = exists;
        }

        @Override
        public String toString() {
            return "FileBean{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", format='" + format + '\'' +
                    ", download='" + download + '\'' +
                    ", save='" + save + '\'' +
                    ", hash='" + hash + '\'' +
                    ", exists='" + exists + '\'' +
                    '}';
        }
    }

}
