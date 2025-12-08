package com.example.demo.blockchain.model;

/**
 * Represents an unspent transaction output (UTXO).
 * POJO class for transferring UTXO data.
 */
public class UTXO {
    private String txHash;
    private int outputIndex;
    private long value;
    private String scriptPubKey;
    private String address;
    private String txid;
    private int vout;
    private long amount;
    private int confirmations;

    public UTXO() {
    }

    public UTXO(String txHash, int outputIndex, long value, String scriptPubKey, String address) {
        this.txHash = txHash;
        this.outputIndex = outputIndex;
        this.value = value;
        this.scriptPubKey = scriptPubKey;
        this.address = address;
    }

    // Original getters/setters
    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public void setOutputIndex(int outputIndex) {
        this.outputIndex = outputIndex;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // New alternative getters/setters (API compatibility)
    public String getTxid() {
        return txid != null ? txid : txHash;
    }

    public void setTxid(String txid) {
        this.txid = txid;
        this.txHash = txid;
    }

    public int getVout() {
        return vout > 0 ? vout : outputIndex;
    }

    public void setVout(int vout) {
        this.vout = vout;
        this.outputIndex = vout;
    }

    public long getAmount() {
        return amount > 0 ? amount : value;
    }

    public void setAmount(long amount) {
        this.amount = amount;
        this.value = amount;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    @Override
    public String toString() {
        return "UTXO{" +
                "txHash='" + txHash + '\'' +
                ", outputIndex=" + outputIndex +
                ", value=" + value +
                ", address='" + address + '\'' +
                '}';
    }
}
