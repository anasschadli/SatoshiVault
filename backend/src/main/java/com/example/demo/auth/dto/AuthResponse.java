package com.example.demo.auth.dto;

public class AuthResponse {
    private String walletAddress;
    private String token;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String walletAddress, String token, String message) {
        this.walletAddress = walletAddress;
        this.token = token;
        this.message = message;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}