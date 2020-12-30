package com.danmoop.novanode.MainApplication.tools;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encrypt {
    public static String toSHA256(String message) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        MessageDigest mg = MessageDigest.getInstance("SHA-256");

        byte[] hash = mg.digest(message.getBytes("UTF-8"));
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static String toMD5(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest mg = MessageDigest.getInstance("MD5");

        byte[] hash = mg.digest(message.getBytes("UTF-8"));
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static String toAES(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec("6f[a^K}#((.1;!To".getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec("Xb@_x=K0nI38f^A1".getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("Encrypted: " + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String fromAES(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec("6f[a^K}#((.1;!To".getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec("Xb@_x=K0nI38f^A1".getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            try {
                byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
                System.out.println("Decrypted: " + new String(original));
                return new String(original);

            } catch (BadPaddingException e) {
                return "???";
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}