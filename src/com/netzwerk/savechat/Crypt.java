package com.netzwerk.savechat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Crypt {


    public static String decrypt(String base64String, PrivateKey privateKey) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(base64String);
        String result = "";
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            result = new String(cipher.doFinal(bytes));
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
            ex.printStackTrace();
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    public static String encrypt(String string, PublicKey publicKey) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] result = new byte[40];
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            result = cipher.doFinal(string.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
            ex.printStackTrace();
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return encoder.encodeToString(result);
    }

    public static byte[] decode(String encoded) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(encoded);
    }

    public static String encode(byte[] data) {
        Base64.Encoder encoder = Base64.getEncoder();
        return new String(encoder.encode(data));
    }

    public static PrivateKey privateKeyFromBytes(byte[] keyBytes) {
        PrivateKey result = null;
        try {
            result = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
            ex.printStackTrace();
        } catch (InvalidKeySpecException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    public static PublicKey publicKeyFromBytes(byte[] keyBytes) {
        PublicKey result = null;
        try {
            result = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
            ex.printStackTrace();
        } catch (InvalidKeySpecException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    public static String hash(byte[] data) {
        Base64.Encoder encoder = Base64.getEncoder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            return new String(encoder.encode(md.digest(data)));
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
}