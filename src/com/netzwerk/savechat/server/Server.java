package com.netzwerk.savechat.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private int port;
    private Set<User> users = new HashSet<>();

    private Server(int port) {
        this.port = port;
    }

    private void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            loadUsers();

            System.out.println("Chat Server is listening on port " + port);


            //noinspection InfiniteLoopStatement
            while (true) {
                Socket socket = serverSocket.accept();

                UserThread newUser = new UserThread(socket, this);
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
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("Invalid Port: " + args[0]);
            }
        } else {
            System.out.println("Parameters: 1. Port");
        }

        Server server = new Server(port);
        server.execute();
    }

    private void saveUsers() {
        File usersFile = new File("users");
        File backupFile = new File("users_backup");

        if (usersFile.exists())
            if (backupFile.exists())
                if (!(backupFile.delete() && usersFile.renameTo(backupFile)))
                    System.out.println("Error creating backup of users-file.");

        StringBuilder data = new StringBuilder();
        for (User user : users) {
            data.append(user.getName()).append(" ").append(user.getHash()).append("\n");
        }

        try (PrintWriter fileWriter = new PrintWriter(usersFile)) {
            fileWriter.println(data);
        } catch (FileNotFoundException ex) {
            System.out.println("Error saving user data.");
        }
    }

    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("users"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(" ");
                if (userData.length == 2) {
                    User newUser = new User(userData[0]);
                    newUser.setHash(userData[1]);
                    users.add(newUser);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Warning: There is no users-file. Every user will be handled as new.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    User getUserByName(String username) {
        for (User user : users) {
            if (user.getName().equals(username)) return user.isOnline() ? user : null;
        }
        return null;
    }

    boolean userNameExists(String username) {
        for (User user : users) {
            if (user.getName().equals(username)) return true;
        }
        return false;
    }

    void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    User getUserByHash(String hash) {
        for (User user : users) {
            if (user.getHash().equals(hash)) return user;
        }
        return null;
    }
}
