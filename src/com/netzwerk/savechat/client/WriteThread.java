package com.netzwerk.savechat.client;

import javax.crypto.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class WriteThread extends Thread {
    // CAUTION! Please re-generate before use! The private key must be added in the user thread of the server!
    private static byte[] pubbytes = {48, -126, 2, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 2, 15, 0, 48, -126, 2, 10, 2, -126, 2, 1, 0, -109, -106, 17, 0, 48, 83, -42, -40, 114, -57, 100, -64, -70, -16, 6, 80, -79, 17, -87, -35, -53, 112, 29, 87, -40, -39, -44, -6, -109, -74, -78, -101, -81, -117, -24, -73, -68, -41, 10, -109, -30, 18, 107, 29, -88, 103, -100, -4, -34, 20, -1, -25, 124, -26, -62, -75, 68, 110, -26, -122, -117, 73, -98, 42, 33, 20, -95, 12, 82, -63, -84, -27, 14, -72, -125, 122, -122, 112, -127, -111, 0, -27, 96, -97, -29, 40, -40, -113, -109, -28, -9, -123, 110, 11, 89, 99, 60, -16, -97, -81, -62, -45, 4, 98, -37, -63, -60, -101, 5, 72, -124, -43, 63, 122, 47, -102, 48, -119, 94, 51, 39, 6, 73, 43, -80, -38, 62, 3, -52, -39, 54, 118, -81, 7, 44, 10, -24, -105, -10, -76, -43, -31, -51, 74, -11, -29, 99, 78, 14, -33, -123, -128, 2, 40, 49, -22, 50, 78, 2, 32, -87, 89, 40, -109, 105, 91, 63, 30, -114, -77, 74, -39, -62, -28, -44, 63, -111, -35, 94, 11, 93, 127, -5, -50, -124, 86, 60, 98, 1, 70, 86, -94, 26, -54, 21, -96, -84, 11, 127, 46, 6, -57, -30, 6, -43, -25, -14, -72, 90, 4, -69, 75, -54, 98, -69, -107, -85, -39, 50, -4, -31, -39, 94, 88, 97, 125, 117, 26, 59, -62, 75, 26, 27, 54, -13, 99, -67, 0, -76, 33, -111, 113, 79, -102, -71, -10, 46, 67, 119, 51, 92, 95, -75, -56, 42, -31, 30, -122, -85, 42, -44, 120, 36, -95, 26, 59, -103, 88, -58, -93, 107, -57, -45, -127, 111, -83, -80, -13, 108, 124, -126, -105, -38, -91, 17, -121, 72, -77, 100, 25, -66, 126, 83, -22, 117, -77, 92, -72, 12, 2, -91, -83, -66, 81, 86, 125, -106, -43, 54, 33, 63, 71, 3, -100, 5, -128, -55, 1, 110, -37, -30, -51, -14, 56, 65, -31, 108, 119, -117, 89, 120, -56, -16, 95, -12, 104, 79, 49, -101, 123, -26, -30, -49, 91, 62, 125, -124, -99, 42, 42, 35, -7, -91, -28, -12, -39, 56, -27, -115, 98, 30, -40, 119, -48, 23, -47, 23, -113, -48, -58, -87, -109, -27, -33, -68, 116, 54, -34, -7, -97, 72, 28, 89, -114, -18, -44, -96, -71, -44, 59, 94, -114, 125, 77, 127, 126, 86, -57, 93, -117, -64, -47, 119, 79, -41, 22, -20, -127, -39, 103, 113, 60, -88, -83, -1, 75, 82, -90, -47, 85, -73, 57, -80, -46, -79, -112, -100, 37, -79, 25, -93, 51, 93, -74, 62, -43, 94, 62, -109, 39, 34, 47, -100, 85, -67, -110, 115, 6, -14, -106, 96, 93, -116, -45, 117, -22, -111, -90, 78, 22, 35, 121, -28, -92, -85, 17, 22, -33, -112, 100, 104, -87, 48, -106, 70, -76, -112, 89, 84, -112, 41, -16, -75, -29, 38, -118, -49, -28, -88, 36, -61, -41, 78, -11, 29, 111, -121, 114, -10, -9, -90, -94, -24, 53, -1, -66, -84, 17, 77, 38, 99, -107, 2, 3, 1, 0, 1};

    private PrintWriter writer;
    private Socket socket;
    private Client client;

    WriteThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String encrypt(String string) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] result = new byte[40];
        try {
            PublicKey pubkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubbytes));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);
            result = cipher.doFinal(string.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("WTF how did this happen??! " + ex.getMessage());
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | InvalidKeyException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return encoder.encodeToString(result);
    }

    private String read(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return "Invalid Name";
    }

    public void run() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("\nEnter your name: ");
        String userName = read(reader);
        writer.println(encrypt(userName));

        System.out.print("\nEnter your partner's name: ");
        String partnerName = read(reader);
        writer.println(encrypt(partnerName));

        String text = "";

        do {
            try {
                text = reader.readLine();
            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
            writer.println(encrypt(text));

        } while (!text.equals("!LOGOFF"));

        try {
            socket.close();
        } catch (IOException ex) {

            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }
}