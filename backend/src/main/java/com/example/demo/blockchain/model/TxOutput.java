package com.example.demo.blockchain.model;

/**
 * Represents a transaction output.
 * POJO class for transferring transaction output data.
 */
public class TxOutput {
    private String address;
    private long value;
    private String scriptPubKey;

    public TxOutput() {
    }

    public TxOutput(String address, long value) {
        this.address = address;
        this.value = value;
    }

    public TxOutput(String address, long value, String scriptPubKey) {
        this.address = address;
        this.value = value;
        this.scriptPubKey = scriptPubKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(String scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    @Override
    public String toString() {
        return "TxOutput{" +
                "address='" + address + '\'' +
                ", value=" + value +
                '}';
    }
}
