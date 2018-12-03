package com.netzwerk.savechat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
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
            try {
                port = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("Invalid Port: " + args[0]);
            }
        } else {
            System.out.println("Parameters: 1. Hostname, 2. Port");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String line;
                System.out.println("Enter IP-address/hostname:");
                line = reader.readLine();
                if (line.trim().length() > 0) hostname = line.trim();
                System.out.println("Enter port:");
                line = reader.readLine();
                if (line.trim().length() > 0) port = Integer.parseInt(line);
            } catch (NumberFormatException | IOException ex) {
                ex.printStackTrace();
            }
        }

        Client client = new Client(hostname, port);
        client.execute();
    }
}