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
    private Socket socket;
    private Client client;
    private PublicKey svrkey;

    WriteThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }

        try {
            byte[] pubbytes = Files.readAllBytes(Paths.get("pubkey"));
            svrkey = Crypt.publicKeyFromBytes(pubbytes);
        } catch (IOException ex) {
            System.out.println("Error reading the server's public key.");
            System.exit(-1);
        }
    }

    public void run() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // send key
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(4096);
            KeyPair rsaKeys = keygen.genKeyPair();
            client.pubkey = rsaKeys.getPublic();
            client.prvkey = rsaKeys.getPrivate();
            String encodedPubkey = Crypt.encode(client.pubkey.getEncoded());
            writer.println(Crypt.encrypt(encodedPubkey, svrkey));
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("This should never happen.");
            System.exit(-1);
        }
        // send pass
        String pass = "";
        Path path = Paths.get("pass");
        if (Files.exists(path)) {
            try {
                pass = new String(Files.readAllBytes(path));
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
                Files.write(path, pass.getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        writer.println(Crypt.encrypt(pass, svrkey));

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
            } else if (text.equals("!change")) {
                writer.println(Crypt.encrypt("p", svrkey));
                client.ptrkey = null;
            } else if (client.ptrkey == null) {
                writer.println(Crypt.encrypt(text, svrkey));
            } else {
                writer.println(Crypt.encrypt("e" + Crypt.encrypt(text, client.ptrkey), svrkey));
            }


        } while (true);
    }
}