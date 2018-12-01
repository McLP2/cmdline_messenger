package com.netzwerk.savechat.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private int port;
    private Set<User> users = new HashSet<>();
    private Set<UserThread> userThreads = new HashSet<>();

    private Server(int port) {
        this.port = port;
    }

    private void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Chat Server is listening on port " + port);


            //noinspection InfiniteLoopStatement
            while (true) {
                Socket socket = serverSocket.accept();

                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();

            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 22713;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);
        server.execute();
    }

    User getUserByName(String username) {
        for (User user : users) {
            if (user.getName().equals(username)) return user.isOnline() ? user : null;
        }
        return null;
    }

    void addUser(User user) {
        users.add(user);
    }

    User getUserByHash(String hash) {
        for (User user : users) {
            if (user.getHash().equals(hash)) return user;
        }
        return null;
    }
}
