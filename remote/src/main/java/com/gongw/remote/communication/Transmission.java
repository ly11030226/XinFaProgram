package com.gongw.remote.communication;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * 传输实体类
 */
public class Transmission implements Parcelable {
    /**
     * 文件名称
     */
    public String fileName;

    /**
     *文件长度
     */
    public long fileLength;

    /**
     *传输类型
     */
    public int transmissionType;

    /**
     *传输内容
     */
    public String content;

    /**
     *传输长度
     */
    public long transLength;

    /**
     * 数据长度
     */

    public int dataLength;

    public Transmission(){

    }

    protected Transmission(Parcel in){
        this.fileName = in.readString();
        this.fileLength = in.readLong();
        this.transmissionType = in.readInt();
        this.content = in.readString();
        this.transLength = in.readLong();
        this.dataLength = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileName);
        dest.writeLong(this.fileLength);
        dest.writeInt(this.transmissionType);
        dest.writeString(this.content);
        dest.writeLong(this.transLength);
        dest.writeInt(this.dataLength);
    }

    public static final Creator<Transmission> CREATOR = new Creator<Transmission>() {
        @Override
        public Transmission createFromParcel(Parcel in) {
            return new Transmission(in);
        }

        @Override
        public Transmission[] newArray(int size) {
            return new Transmission[size];
        }
    };
}

