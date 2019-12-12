package com.gongw.remote;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Tools {
    /**
     * 验证字符串是否符合json格式
     * @param jsonStr
     * @return
     */
    public static boolean validateJson(String jsonStr) {
        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(jsonStr);
        } catch (Exception e) {
            return false;
        }
        if (jsonElement == null) {
            return false;
        }
        if (!jsonElement.isJsonObject()) {
            return false;
        }
        return true;
    }


    public static byte[] int2Bytes(int i){
        byte[] bytes= new byte[4];
        bytes[3]= (byte) ((byte) i>>24);
        bytes[2]= (byte) ((byte) i>>16);
        bytes[1]= (byte) ((byte) i>>8);
        bytes[0]=(byte) i;
        return bytes;
    }
    public static int bytes2Int(byte[] bytes){
        int int1=bytes[0]&0xff;
        int int2=(bytes[1]&0xff)<<8;
        int int3=(bytes[2]&0xff)<<16;
        int int4=(bytes[3]&0xff)<<24;
        return int1|int2|int3|int4;
    }

    /**
     * 针对 byte【0】 代表其他类型值 从1 到 4 开始算起
     * @param i
     * @param b
     * @return
     */
    public static void int2BytesExtra(int i,byte[]b) {
        if (b != null) {
            b[4] = (byte) (i >> 24);
            b[3] = (byte) (i >> 16);
            b[2] = (byte) (i >> 8);
            b[1] = (byte) i;
        }
    }

    /**
     * 针对 byte【0】 代表其他类型值 从1 到 4 开始算起 （同上）
     * @param bytes
     * @return
     */
    public static int bytes2IntExtra(byte[] bytes){
        int int1=bytes[1]&0xff;
        int int2=(bytes[2]&0xff)<<8;
        int int3=(bytes[3]&0xff)<<16;
        int int4=(bytes[4]&0xff)<<24;
        return int1|int2|int3|int4;
    }

}
