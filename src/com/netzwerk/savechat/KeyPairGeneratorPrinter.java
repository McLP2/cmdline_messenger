package com.netzwerk.savechat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

public class KeyPairGeneratorPrinter {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(4096);
        KeyPair rsaKeys = keygen.genKeyPair();

        try {
            Files.write(Paths.get("pubkey"), rsaKeys.getPublic().getEncoded());
            Files.write(Paths.get("prvkey"), rsaKeys.getPrivate().getEncoded());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
