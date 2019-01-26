package com.netzwerk.savechat.client;

import com.netzwerk.savechat.Crypt;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

public class WriteThread extends Thread {

    private PrintWriter writer;
    private Client client;
    private PublicKey svrkey;
    private SecretKey secretKey;
    private SecretKey secretServerKey;
    private ReadThread readThread;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

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
            svrkey = Crypt.publicKeyFromBytes(pubbytes, false);
        } catch (IOException ex) {
            System.out.println("Error reading the server's public key.");
        }
        newSecrets();
    }

    void setKey(PublicKey svrkey) {
        this.svrkey = svrkey;
        unfreeze();
    }

    private void getKey() {
        System.out.println("Asking the server for a key...");
        readThread.getKeyMode();
        writer.println("getkey");
    }

    void setReadThread(ReadThread readThread) {
        this.readThread = readThread;
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        if (svrkey == null) {
            getKey();
            freeze();
            System.out.println("If this is correct fingerprint, enter !accept otherwise !exit.");
            checkFingerprint(reader);
        }

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
            } else if (text.equals("!change")) {
                writer.println(Crypt.encrypt("p", svrkey, secretServerKey));
                client.ptrkey = null;
            } else if (text.length() > 8 && text.substring(0, 7).equals("!change")) {
                writer.println(Crypt.encrypt("p" + text.substring(8), svrkey, secretServerKey));
                client.ptrkey = null;
            } else if (client.ptrkey == null) {
                writer.println(Crypt.encrypt(text, svrkey, secretServerKey));
            } else {
                writer.println(Crypt.encrypt("e" + Crypt.encrypt(text, client.ptrkey, secretKey), svrkey, secretServerKey));
            }


        } while (true);
    }

    private void freeze() {
        synchronized (pauseLock) {
            try {
                pauseLock.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        }
    }

    private void unfreeze() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread
        }
    }

    private void checkFingerprint(BufferedReader reader) {
        try {
            String answer = reader.readLine();
            switch (answer) {
                case "!accept":
                    saveServerKey();
                    return;
                case "!exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please enter !accept or !exit.");
                    checkFingerprint(reader);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveServerKey() {
        try {
            Path pathServerOld = Paths.get("svrkey.old");
            Path pathServer = Paths.get("svrkey");
            if (Files.exists(pathServer))
                Files.move(pathServer, pathServerOld, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.write(pathServer, svrkey.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        writer.println(Crypt.encrypt(pass, svrkey, secretServerKey));
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
            System.out.println("Please wait a moment...");
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
        writer.println(Crypt.encrypt(encodedPubkey, svrkey, secretServerKey));
    }

    void newSecrets() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            secretServerKey = generator.generateKey();
            secretKey = generator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}