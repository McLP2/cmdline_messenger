package com.netzwerk.savechat.keygen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

public class KeyPairGeneratorPrinter {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(2048);
        KeyPair rsaKeys = keygen.genKeyPair();

        try {
            Files.write(Paths.get("svrkey"), rsaKeys.getPublic().getEncoded());
            Files.write(Paths.get("prvkey"), rsaKeys.getPrivate().getEncoded());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
