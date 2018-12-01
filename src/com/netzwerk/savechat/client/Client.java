package com.netzwerk.savechat.client;

import java.net.*;
import java.io.*;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Client {
    private String hostname;
    private int port;
    PublicKey pubkey, ptrkey;
    PrivateKey prvkey;

    private Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    private void execute() {
        try {
            System.out.println("Connecting...");

            Socket socket = new Socket(hostname, port);

            System.out.println("Establishing encryption...");

            new ReadThread(socket, this).start();
            new WriteThread(socket, this).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }

    }

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 22713;

        if (args.length == 2) {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }

        Client client = new Client(hostname, port);
        client.execute();
    }
}