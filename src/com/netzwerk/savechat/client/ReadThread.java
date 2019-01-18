package com.netzwerk.savechat.client;

import com.netzwerk.savechat.Crypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadThread extends Thread {
    private BufferedReader reader;
    private Client client;
    private WriteThread writeThread;
    private boolean keymode = false;

    ReadThread(Socket socket, Client client) {
        this.client = client;
        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    void setWriteThread(WriteThread writeThread) {
        this.writeThread = writeThread;
    }

    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                if (keymode) {
                    byte[] keybytes = Crypt.decode(response);
                    writeThread.setKey(Crypt.publicKeyFromBytes(keybytes));
                    keymode = false;
                    Path pathServerOld = Paths.get("svrkey.old");
                    Path pathServer = Paths.get("svrkey");
                    if (Files.exists(pathServer))
                        Files.move(pathServer, pathServerOld, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    Files.write(pathServer, keybytes);
                    System.out.println("Server fingerprint: " + Crypt.hash(keybytes));
                    continue;
                }
                String message = Crypt.decrypt(response, client.prvkey);
                switch (message.charAt(0)) {
                    case 'm':
                        System.out.println(message.substring(1));
                        break;
                    case 'k':
                        client.ptrkey = Crypt.publicKeyFromBytes(Crypt.decode(message.substring(1)));
                        writeThread.newSecrets();
                        byte[] pubbytes = client.ptrkey.getEncoded();
                        byte[] pubbytes2 = client.pubkey.getEncoded();
                        byte[] hashbytes = new byte[pubbytes.length];
                        for (int i = 0; i < pubbytes.length; i++) hashbytes[i] = (byte) (pubbytes[i] ^ pubbytes2[i]);
                        System.out.println("Common fingerprint: " + Crypt.hash(hashbytes) + "\n\n");
                        break;
                    case 'e':
                        System.out.println(Crypt.decrypt(message.substring(1), client.prvkey));
                        break;
                }
            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }

    void getKeyMode() {
        keymode = true;
    }
}
