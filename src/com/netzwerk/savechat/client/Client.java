package com.netzwerk.savechat.client;

import java.net.*;
import java.io.*;

public class Client {
    private String hostname;
    private int port;
    private String userName;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);

            System.out.println("Connected to the chat server");

            new ReadThread(socket).start();
            new WriteThread(socket, this).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }

    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    String getUserName() {
        return this.userName;
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