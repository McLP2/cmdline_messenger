package com.netzwerk.savechat.server;

class User {
    private String name, hash;
    private byte[] pubkey;
    private boolean online;
    private UserThread userThread;

    User(String name) {
        this.name = name;
    }

    void setThread(UserThread userThread) {
        this.userThread = userThread;
    }

    UserThread getThread() {
        return userThread;
    }

    boolean isOnline() {
        return online;
    }

    String getName() {
        return name;
    }

    byte[] getPubkey() {
        return pubkey;
    }

    void setOnline(boolean online) {
        this.online = online;
    }

    void setPubkey(byte[] pubkey) {
        this.pubkey = pubkey;
    }

    void setHash(String hash) {
        this.hash = hash;
    }

    String getHash() {
        return this.hash;
    }
}
