package com.netzwerk.savechat.client;

import com.netzwerk.savechat.Crypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

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
                    keymode = false;
                    System.out.println("Server fingerprint: " + Crypt.hash(keybytes));
                    writeThread.setKey(Crypt.publicKeyFromBytes(keybytes));
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
                        byte[] pubbytes1 = client.ptrkey.getEncoded();
                        byte[] pubbytes2 = client.pubkey.getEncoded();
                        byte[] hashbytes = new byte[pubbytes1.length + pubbytes2.length];
                        // concat arrays in an order depending on the content
                        if (firstIsSmaller(pubbytes1, pubbytes2)) {
                            System.arraycopy(pubbytes1, 0, hashbytes, 0, pubbytes1.length);
                            System.arraycopy(pubbytes2, 0, hashbytes, pubbytes1.length, pubbytes2.length);
                        } else {
                            System.arraycopy(pubbytes2, 0, hashbytes, 0, pubbytes2.length);
                            System.arraycopy(pubbytes1, 0, hashbytes, pubbytes2.length, pubbytes1.length);
                        }
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

    private boolean firstIsSmaller(byte[] a, byte[] b) {
        int minLength = Math.min(a.length, b.length);
        for (int i = 0; i < minLength; i++) {
            byte valueA = a[i];
            byte valueB = b[i];
            if (valueA != valueB) return valueA < valueB;
        }
        // i don't believe this can happen but...
        return a.length < b.length;
    }

    void getKeyMode() {
        keymode = true;
    }
}
