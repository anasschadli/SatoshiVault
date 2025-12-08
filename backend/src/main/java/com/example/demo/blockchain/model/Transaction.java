package com.example.demo.blockchain.model;

import java.util.List;

/**
 * Represents a Bitcoin transaction.
 * POJO class for transferring transaction data.
 */
public class Transaction {
    private String txId;
    private String txid;
    private List<TxInput> inputs;
    private List<TxOutput> outputs;
    private long fee;
    private long timestamp;
    private int confirmations;
    private String status;
    private long amount;

    public Transaction() {
    }

    public Transaction(String txId, List<TxInput> inputs, List<TxOutput> outputs, long fee) {
        this.txId = txId;
        this.txid = txId;
        this.inputs = inputs;
        this.outputs = outputs;
        this.fee = fee;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
        this.txid = txId;
    }

    public String getTxid() {
        return txid != null ? txid : txId;
    }

    public void setTxid(String txid) {
        this.txid = txid;
        this.txId = txid;
    }

    public List<TxInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<TxInput> inputs) {
        this.inputs = inputs;
    }

    public List<TxOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TxOutput> outputs) {
        this.outputs = outputs;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "txId='" + txId + '\'' +
                ", inputCount=" + (inputs != null ? inputs.size() : 0) +
                ", outputCount=" + (outputs != null ? outputs.size() : 0) +
                ", fee=" + fee +
                ", confirmations=" + confirmations +
                '}';
    }
}
