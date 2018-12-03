package com.netzwerk.savechat.server;

import com.netzwerk.savechat.Crypt;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

public class UserThread extends Thread {

    private Socket socket;
    private Server server;
    private PrintWriter writer;
    private User user, partner;
    private PublicKey userKey;
    private PrivateKey prvkey;

    UserThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        try {
            byte[] prvbytes = Files.readAllBytes(Paths.get("prvkey"));
            prvkey = Crypt.privateKeyFromBytes(prvbytes);
        } catch (IOException ex) {
            System.out.println("Error reading private key.");
            System.exit(-1);
        }
    }

    public void run() {
        try {
            // init streams
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            // get key
            String userKeyEncoded = Crypt.decrypt(reader.readLine(), prvkey);
            userKey = Crypt.publicKeyFromBytes(Crypt.decode(userKeyEncoded));
            sendMessage("mWelcome to the server!\n ");
            // get user
            String userHash = Crypt.hash(Crypt.decrypt(reader.readLine(), prvkey).getBytes());
            user = server.getUserByHash(userHash);
            if (user == null) {
                getUsername(reader);
                user.setHash(userHash);
                server.addUser(user);
            }
            user.setThread(this);
            user.setOnline(true);
            user.setPubkey(userKey.getEncoded());

            sendMessage("mHi, " + user.getName() + "!\n");
            sendMessage("mIf you want to connect to a user, type \"!change\" !");

            String clientMessage;

            do {
                clientMessage = Crypt.decrypt(reader.readLine(), prvkey);
                switch (clientMessage.charAt(0)) {
                    case 'e':
                        if (partner != null) {
                            partner.getThread().sendMessage(clientMessage);
                        } else {
                            sendMessage("mYou are not connected to a user, type \"!change\" !");
                        }
                        break;
                    case 'p':
                        getPartner(reader, clientMessage.substring(1));
                        break;
                }
            } while (socket.isConnected());

            user.setOnline(false);
            socket.close();
            loggedOffPartner();

        } catch (SocketException ex) {
            user.setOnline(false);
            if (partner != null) {
                loggedOffPartner();
            }
        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loggedOffPartner() {
        partner.getThread().sendMessage("m\nYour partner logged off.");
        partner.getThread().partner = null;
        partner = null;
    }

    private void getUsername(BufferedReader reader) throws IOException {
        sendMessage("mPlease enter your username:");
        String userName = Crypt.decrypt(reader.readLine(), prvkey);
        if (userName.contains(" ")) {
            sendMessage("mNo spaces allowed.\n");
            getUsername(reader);
        } else if (server.userNameExists(userName)) {
            sendMessage("mThis user already exists. Try a different name.\n");
            getUsername(reader);
        } else {
            user = new User(userName);
        }
    }

    private void getPartner(BufferedReader reader, String partnerName) throws IOException {
        if (partner != null) {
            partner.getThread().sendMessage("m\n\nYour partner left you.");
            partner.getThread().partner = null;
            sendMessage("m\n\nYou left your partner.");
            partner = null;
        }
        if (partnerName.length() > 0) {
            partner = server.getUserByName(partnerName);
            if (partner == null || partner.getThread().partner != null) {
                sendMessage("mThis user is currently not available.");
                getPartner(reader, "");
            } else {
                sendMessage("m\nYou are now connected to " + partner.getName() + "!\n");
                partner.getThread().partner = user;
                partner.getThread().sendMessage("m\nYou are now connected to " + user.getName() + ".\n");
                sendMessage("k" + Crypt.encode(partner.getPubkey()));
                partner.getThread().sendMessage("k" + Crypt.encode(user.getPubkey()));
            }
        } else {
            sendMessage("m\nPlease enter your chat partner's username:");
            partnerName = Crypt.decrypt(reader.readLine(), prvkey);
            if (partner != null) { // partner established connection while user is changing partner
                partner.getThread().sendMessage(partnerName);
            } else {
                getPartner(reader, partnerName);
            }
        }
    }


    private void sendMessage(String message) {
        writer.println(Crypt.encrypt(message, userKey));
    }
}
