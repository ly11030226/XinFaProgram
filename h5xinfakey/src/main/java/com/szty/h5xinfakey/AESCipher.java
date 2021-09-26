package com.szty.h5xinfakey;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCipher {

    private static String key = "sztiye_20210911_";
    private static final String IV_STRING = "A-16-Byte-String";
    private static final String charset = "UTF-8";

    public static String EnCode(String content) {
        try {
            byte[] contentBytes = content.getBytes(charset);
            byte[] keyBytes = key.getBytes(charset);
            byte[] encryptedBytes = aesEncryptBytes(contentBytes, keyBytes);
            return Base64Util.encode(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String DeCode(String content) {
        try {
            byte[] encryptedBytes = Base64Util.decode(content);
            byte[] keyBytes = key.getBytes(charset);
            byte[] decryptedBytes = aesDecryptBytes(encryptedBytes, keyBytes);
            return new String(decryptedBytes, charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] aesEncryptBytes(byte[] contentBytes, byte[] keyBytes) {
        try {
            return cipherOperation(contentBytes, keyBytes, Cipher.ENCRYPT_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] aesDecryptBytes(byte[] contentBytes, byte[] keyBytes) {
        try {
            return cipherOperation(contentBytes, keyBytes, Cipher.DECRYPT_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] cipherOperation(byte[] contentBytes, byte[] keyBytes, int mode) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        byte[] initParam = IV_STRING.getBytes(charset);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, secretKey, ivParameterSpec);
        return cipher.doFinal(contentBytes);
    }

}
