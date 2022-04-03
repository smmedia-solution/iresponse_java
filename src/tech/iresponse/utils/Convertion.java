package tech.iresponse.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Convertion {

    public static String crypt(String text) throws Exception {
        return crypt(text, (String)null);
    }

    public static String decrypt(String encrypted) throws Exception {
        return decrypt(encrypted, null);
    }

    public static String crypt(String text, String secretKey) throws Exception {
        byte[] sKey = (secretKey != null && !"".equals(secretKey)) ? secretKey.getBytes() : "z#SAsZb#@yPf5Jrzz$9Ug%V$V^_zx!68U5HBfK-&r!gNF559@qJzbxP4aVHT7em#".getBytes();
        SecureRandom rndom = new SecureRandom();
        byte[] salt = new byte[32];
        rndom.nextBytes(salt);

        byte[] inBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] passAndSalt = arrayConcat(sKey, salt);
        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];

        for (int b = 0; b < 3 && keyAndIv.length < 48; b++) {
            byte[] hashData = arrayConcat(hash, passAndSalt);
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(hashData);
            keyAndIv = arrayConcat(keyAndIv, hash);
        }

        byte[] keyValue = Arrays.copyOfRange(keyAndIv, 0, 32);
        byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);
        SecretKeySpec key = new SecretKeySpec(keyValue, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(1, key, new IvParameterSpec(iv));
        byte[] data = cipher.doFinal(inBytes);
        data = arrayConcat(salt, data);
        return Base64.getEncoder().encodeToString(data);
    }

    public static String decrypt(String encrypted, String secretKey) throws Exception {
        byte[] sKey = (secretKey != null && !"".equals(secretKey)) ? secretKey.getBytes() : "z#SAsZb#@yPf5Jrzz$9Ug%V$V^_zx!68U5HBfK-&r!gNF559@qJzbxP4aVHT7em#".getBytes();
        byte[] inBytes = Base64.getDecoder().decode(encrypted);
        byte[] salt = Arrays.copyOfRange(inBytes, 0, 32);
        byte[] passAndSalt = arrayConcat(sKey, salt);

        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];

        for (int b = 0; b < 3 && keyAndIv.length < 48; b++) {
            byte[] hashData = arrayConcat(hash, passAndSalt);
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(hashData);
            keyAndIv = arrayConcat(keyAndIv, hash);
        }

        byte[] keyValue = Arrays.copyOfRange(keyAndIv, 0, 32);
        SecretKeySpec key = new SecretKeySpec(keyValue, "AES");
        byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(2, key, new IvParameterSpec(iv));
        byte[] clear = cipher.doFinal(inBytes, 32, inBytes.length - 32);
        return new String(clear, StandardCharsets.UTF_8);
    }

    public static String boucleDecrypt(String encrypted, int max) throws Exception {
        for (int b = 0; b < max; b++){
            encrypted = decrypt(encrypted);
        }
        return encrypted;
    }

    public static String md5(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes());
        byte[] digst = md.digest();
        return DatatypeConverter.printHexBinary(digst).toLowerCase();
    }

    private static byte[] arrayConcat(byte[] aa, byte[] bb) {
        byte[] cc = new byte[aa.length + bb.length];
        System.arraycopy(aa, 0, cc, 0, aa.length);
        System.arraycopy(bb, 0, cc, aa.length, bb.length);
        return cc;
    }
}
