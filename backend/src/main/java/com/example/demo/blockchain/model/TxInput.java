package com.example.demo.blockchain.model;

/**
 * Represents a transaction input.
 * POJO class for transferring transaction input data.
 */
public class TxInput {
    private String txHash;
    private int outputIndex;
    private String script;
    private long value;

    public TxInput() {
    }

    public TxInput(String txHash, int outputIndex, long value) {
        this.txHash = txHash;
        this.outputIndex = outputIndex;
        this.value = value;
    }

    public TxInput(String txHash, int outputIndex, String script, long value) {
        this.txHash = txHash;
        this.outputIndex = outputIndex;
        this.script = script;
        this.value = value;
    }

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

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TxInput{" +
                "txHash='" + txHash + '\'' +
                ", outputIndex=" + outputIndex +
                ", value=" + value +
                '}';
    }
}
