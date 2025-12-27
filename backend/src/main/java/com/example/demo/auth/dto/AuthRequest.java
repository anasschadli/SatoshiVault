package com.example.demo.auth.dto;

public class AuthRequest {
    private String passphrase;

    public AuthRequest() {}

    public AuthRequest(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
}