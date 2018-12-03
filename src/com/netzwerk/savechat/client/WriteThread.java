package com.netzwerk.savechat.client;

import com.netzwerk.savechat.Crypt;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

public class WriteThread extends Thread {

    private PrintWriter writer;
    private Client client;
    private PublicKey svrkey;

    WriteThread(Socket socket, Client client) {
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }

        try {
            byte[] pubbytes = Files.readAllBytes(Paths.get("svrkey"));
            svrkey = Crypt.publicKeyFromBytes(pubbytes);
        } catch (IOException ex) {
            System.out.println("Error reading the server's public key.");
            System.exit(-1);
        }
    }

    public void run() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // send key
        loadKeypair();

        // send pass
        loadUserIdentifier();

        // send console
        String text = "";
        do {
            try {
                text = reader.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (text.equals("!exit")) {
                System.exit(0);
            } else if (text.substring(0,7).equals("!change")) {
                writer.println(Crypt.encrypt("p"+text.substring(8), svrkey));
                client.ptrkey = null;
            } else if (client.ptrkey == null) {
                writer.println(Crypt.encrypt(text, svrkey));
            } else {
                writer.println(Crypt.encrypt("e" + Crypt.encrypt(text, client.ptrkey), svrkey));
            }


        } while (true);
    }

    private void loadUserIdentifier() {
        String pass = "";
        Path passPath = Paths.get("pass");
        if (Files.exists(passPath)) {
            try {
                pass = new String(Files.readAllBytes(passPath));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            SecureRandom random = new SecureRandom();
            char[] randomdata = new char[128];
            for (int i = 0; i < randomdata.length; i++) {
                randomdata[i] = (char) (32 + random.nextInt(90));
            }
            pass = new String(randomdata);
            try {
                Files.write(passPath, pass.getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        writer.println(Crypt.encrypt(pass, svrkey));
    }

    private void loadKeypair() {
        Path pathPublic = Paths.get("pubkey");
        Path pathPrivate = Paths.get("prvkey");
        if (Files.exists(pathPublic) && Files.exists(pathPrivate)) {
            try {
                client.pubkey = Crypt.publicKeyFromBytes(Files.readAllBytes(pathPublic));
                client.prvkey = Crypt.privateKeyFromBytes(Files.readAllBytes(pathPrivate));
            } catch (IOException ex) {
                System.out.println("Error in keypair.");
                System.exit(-1);
                ex.printStackTrace();
            }
        } else {
            try {
                KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
                keygen.initialize(4096);
                KeyPair rsaKeys = keygen.genKeyPair();
                client.pubkey = rsaKeys.getPublic();
                client.prvkey = rsaKeys.getPrivate();

                Files.write(pathPublic, client.pubkey.getEncoded());
                Files.write(pathPrivate, client.prvkey.getEncoded());
            } catch (NoSuchAlgorithmException ex) {
                System.out.println("This should never happen.");
                System.exit(-1);
            } catch (IOException ex) {
                System.out.println("Could not store keypair.");
                ex.printStackTrace();
            }
        }

        String encodedPubkey = Crypt.encode(client.pubkey.getEncoded());
        writer.println(Crypt.encrypt(encodedPubkey, svrkey));
    }
}