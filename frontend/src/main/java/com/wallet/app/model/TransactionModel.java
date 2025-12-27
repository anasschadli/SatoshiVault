package com.wallet.app.model;

import java.time.LocalDateTime;

public class TransactionModel {
    public enum Status {
        SENT, RECEIVED, PENDING
    }

    private String txId;
    private LocalDateTime date;
    private double amount;
    private Status status;
    private String address;

    public TransactionModel() {
    }

    public TransactionModel(String txId, LocalDateTime date, double amount, Status status) {
        this.txId = txId;
        this.date = date;
        this.amount = amount;
        this.status = status;
    }
    
    public TransactionModel(String txId, LocalDateTime date, double amount, Status status, String address) {
        this.txId = txId;
        this.date = date;
        this.amount = amount;
        this.status = status;
        this.address = address;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
}
