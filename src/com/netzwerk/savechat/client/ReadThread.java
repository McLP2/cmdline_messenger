package com.netzwerk.savechat.client;

import com.netzwerk.savechat.Crypt;

import java.io.*;
import java.net.*;

public class ReadThread extends Thread {
    private BufferedReader reader;
    private Client client;

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

    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                String message = Crypt.decrypt(response, client.prvkey);
                switch (message.charAt(0)) {
                    case 'm':
                        System.out.println(message.substring(1));
                        break;
                    case 'k':
                        client.ptrkey = Crypt.publicKeyFromBytes(Crypt.decode(message.substring(1)));
                        byte[] pubbytes = client.ptrkey.getEncoded();
                        byte[] pubbytes2 = client.pubkey.getEncoded();
                        byte[] hashbytes = new byte[pubbytes.length];
                        for (int i = 0; i < pubbytes.length; i++) hashbytes[i] = (byte) (pubbytes[i] ^ pubbytes2[i]);
                        System.out.println("Fingerprint: " + Crypt.hash(hashbytes));
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
}
