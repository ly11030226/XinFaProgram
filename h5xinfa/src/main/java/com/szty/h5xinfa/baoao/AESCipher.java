package com.szty.h5xinfa.baoao;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCipher {

    private static final String IV_STRING = "A-16-Byte-String";
    private static final String charset = "UTF-8";


    public static String DeCode(String content, String psd) {
        try {
            byte[] encryptedBytes = Base64Util.decode(content);
            byte[] keyBytes = psd.getBytes(charset);
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
