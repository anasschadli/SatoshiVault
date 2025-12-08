package com.example.demo.blockchain.api;

import com.example.demo.blockchain.model.Transaction;
import com.example.demo.blockchain.model.UTXO;

import java.util.List;

/**
 * Interface for blockchain API operations.
 * Implementations can use different blockchain APIs (BlockCypher, Blockchain.com, etc.)
 */
public interface BlockchainAPI {

    /**
     * Fetches the balance of a Bitcoin address in satoshis.
     *
     * @param address The Bitcoin address
     * @return The balance in satoshis
     */
    long getBalance(String address) throws Exception;

    /**
     * Fetches UTXOs (unspent transaction outputs) for a Bitcoin address.
     *
     * @param address The Bitcoin address
     * @return A list of UTXOs
     */
    List<UTXO> getUTXOs(String address) throws Exception;

    /**
     * Fetches transaction history for a Bitcoin address.
     *
     * @param address The Bitcoin address
     * @return A list of transactions
     */
    List<Transaction> getTransactionHistory(String address) throws Exception;

    /**
     * Broadcasts a signed transaction to the network.
     *
     * @param rawTransaction The raw signed transaction hex
     * @return The transaction ID (txId)
     */
    String broadcastTransaction(String rawTransaction) throws Exception;

    /**
     * Estimates transaction fee.
     *
     * @param inputs  Number of inputs
     * @param outputs Number of outputs
     * @return Estimated fee in satoshis
     */
    long estimateFee(int inputs, int outputs) throws Exception;

    /**
     * Validates if address is valid.
     *
     * @param address The Bitcoin address
     * @return True if valid
     */
    boolean isValidAddress(String address) throws Exception;
}
