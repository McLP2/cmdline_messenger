package com.netzwerk.savechat.server;

import java.util.Arrays;

class User {
    private String name;
    private byte[] pubkey;
    private boolean online;
    private UserThread userThread;

    User(String name) {
        this.name = name;
        this.online = true;
    }

    public void setThread(UserThread userThread) {
        this.userThread = userThread;
    }

    public UserThread getThread() {
        return userThread;
    }

    public boolean isOnline() {
        return online;
    }

    public String getName() {
        return name;
    }

    public byte[] getPubkey() {
        return pubkey;
    }

    void setOnline(boolean online) {
        this.online = online;
    }

    public boolean setPubkey(byte[] pubkey) {
        if (this.pubkey == null) {
            this.pubkey = pubkey;
            return true;
        } else
            return Arrays.equals(this.pubkey, pubkey);
    }
}
