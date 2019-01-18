package com.netzwerk.savechat;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Crypt {

    private static final int AES_BIT = 256;
    private static final int AES_LEN = AES_BIT * 2;


    public static String decrypt(String base64String, PrivateKey privateKey) {
        byte[] bytes = decode(base64String);
        byte[] iv = new byte[16];
        System.arraycopy(bytes, 0, iv, 0, 16);
        byte[] key = new byte[AES_LEN];
        System.arraycopy(bytes, 16, key, 0, AES_LEN);
        byte[] data = new byte[bytes.length - AES_LEN - 16];
        System.arraycopy(bytes, 16 + AES_LEN, data, 0, bytes.length - AES_LEN - 16);
        String result = "";
        try {
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.UNWRAP_MODE, privateKey);
            SecretKey secKey = (SecretKey) cipher.unwrap(key, "AES", Cipher.SECRET_KEY);

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secKey, ivspec);
            result = new String(cipher.doFinal(data), StandardCharsets.UTF_16);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
            ex.printStackTrace();
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    @Deprecated
    public static String encrypt(String string, PublicKey publicKey) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(AES_BIT);
            SecretKey secKey = generator.generateKey();
            return encrypt(string, publicKey, secKey);
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
            ex.printStackTrace();
        }

        return "Encryption Error";
    }

    public static String encrypt(String string, PublicKey publicKey, SecretKey secKey) {
        byte[] iv = new byte[16];
        byte[] aes_key = new byte[AES_LEN];
        byte[] data = new byte[40];
        try {
            // generate initialization vector
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            // encrypt data
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secKey, ivspec);
            data = cipher.doFinal(string.getBytes(StandardCharsets.UTF_16));
            // wrap key
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.WRAP_MODE, publicKey);
            aes_key = cipher.wrap(secKey);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
            ex.printStackTrace();
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }

        // concatenate encrypted key
        byte[] result = new byte[16 + AES_LEN + data.length];
        System.arraycopy(iv, 0, result, 0, 16);
        System.arraycopy(aes_key, 0, result, 16, AES_LEN);
        System.arraycopy(data, 0, result, AES_LEN + 16, data.length);

        return encode(result);
    }

    public static byte[] decode(String encoded) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(encoded);
    }

    public static String encode(byte[] data) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data);
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
        return publicKeyFromBytes(keyBytes, true);
    }

    public static PublicKey publicKeyFromBytes(byte[] keyBytes, boolean verbose) {
        PublicKey result = null;
        try {
            result = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (NoSuchAlgorithmException ex) {
            if (verbose) {
                System.out.println("WTF how did this happen??! " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (InvalidKeySpecException ex) {
            if (verbose) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
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