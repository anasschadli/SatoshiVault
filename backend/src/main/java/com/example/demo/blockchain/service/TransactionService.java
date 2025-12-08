package com.example.demo.blockchain.service;

import com.example.demo.blockchain.api.BlockchainAPI;
import com.example.demo.blockchain.model.Transaction;
import com.example.demo.blockchain.model.TxInput;
import com.example.demo.blockchain.model.TxOutput;
import com.example.demo.blockchain.model.UTXO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling Bitcoin transactions.
 * Handles transaction creation, signing, broadcasting, and UTXO selection.
 */
@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final BlockchainAPI blockchainAPI;
    private final FeeCalculator feeCalculator;
    private final KeyService keyService;

    @Autowired
    public TransactionService(BlockchainAPI blockchainAPI, FeeCalculator feeCalculator, KeyService keyService) {
        this.blockchainAPI = blockchainAPI;
        this.feeCalculator = feeCalculator;
        this.keyService = keyService;
    }

    /**
     * Sends Bitcoin from one address to another.
     * This is the main transaction flow.
     *
     * @param fromAddress    The sender's Bitcoin address
     * @param toAddress      The recipient's Bitcoin address
     * @param amount         The amount to send in satoshis
     * @param privateKeyWif  The sender's private key
     * @return The transaction ID (txId)
     */
    public String sendBitcoin(String fromAddress, String toAddress, long amount, String privateKeyWif) throws Exception {
        logger.info("Starting Bitcoin transfer from {} to {} for {} satoshis", fromAddress, toAddress, amount);

        // Validate addresses
        if (!keyService.isValidAddress(fromAddress) || !keyService.isValidAddress(toAddress)) {
            throw new IllegalArgumentException("Invalid Bitcoin address format");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        // Step 1: Set fee rate (5 sat/byte for testnet)
        long feeRate = 5;
        logger.info("Fee rate: {} sat/byte", feeRate);

        // Step 2: Fetch UTXOs for the sender
        List<UTXO> availableUTXOs = blockchainAPI.getUTXOs(fromAddress);
        logger.info("Available UTXOs: {}", availableUTXOs.size());

        if (availableUTXOs.isEmpty()) {
            throw new RuntimeException("No UTXOs available at address: " + fromAddress);
        }

        // Step 3: Select UTXOs using coin selection algorithm
        long estimatedFee = feeCalculator.calculateFee(1, 2, feeRate);
        CoinSelectionResult coinSelection = selectCoins(availableUTXOs, amount, estimatedFee, feeRate);

        if (coinSelection == null) {
            throw new RuntimeException("Insufficient funds: need " + (amount + estimatedFee) + 
                    " satoshis but only have " + getTotalUTXOValue(availableUTXOs));
        }

        logger.info("Selected {} UTXOs for transaction", coinSelection.selectedUTXOs.size());

        // Step 4: Create transaction inputs and outputs
        List<TxInput> inputs = createTransactionInputs(coinSelection.selectedUTXOs);
        List<TxOutput> outputs = new ArrayList<>();

        // Output to recipient
        outputs.add(new TxOutput(toAddress, amount));

        // Calculate change
        long inputTotal = coinSelection.selectedUTXOs.stream().mapToLong(UTXO::getAmount).sum();
        long actualFee = feeCalculator.calculateFee(inputs.size(), outputs.size() + 1, feeRate);
        long change = inputTotal - amount - actualFee;

        // Only add change output if it's above dust threshold
        if (change > feeCalculator.calculateDustThreshold(feeRate)) {
            outputs.add(new TxOutput(fromAddress, change));
            logger.info("Adding change output: {} satoshis", change);
        } else {
            actualFee = inputTotal - amount;
            logger.info("Change amount below dust threshold, skipping change output");
        }

        // Step 5: Create and populate transaction
        Transaction tx = new Transaction();
        tx.setInputs(inputs);
        tx.setOutputs(outputs);
        tx.setFee(actualFee);
        tx.setStatus("pending");

        logger.info("Transaction created - Inputs: {}, Outputs: {}, Fee: {} satoshis",
                inputs.size(), outputs.size(), actualFee);

        // Step 6: Sign transaction (placeholder - actual signing would use BitcoinJ)
        String signature = keyService.signMessage(tx.toString(), privateKeyWif);
        logger.debug("Transaction signed with signature: {}", signature);

        // Step 7: Broadcast transaction
        String rawTransaction = buildRawTransaction(tx);
        String txId = blockchainAPI.broadcastTransaction(rawTransaction);

        logger.info("Transaction broadcast successfully with ID: {}", txId);
        tx.setTxId(txId);
        tx.setStatus("broadcast");

        return txId;
    }

    /**
     * Fetches the balance of an address.
     *
     * @param address The Bitcoin address
     * @return The balance in satoshis
     */
    public long getBalance(String address) throws Exception {
        logger.info("Fetching balance for address: {}", address);
        return blockchainAPI.getBalance(address);
    }

    /**
     * Fetches the UTXOs for an address.
     *
     * @param address The Bitcoin address
     * @return List of UTXOs
     */
    public List<UTXO> getUTXOs(String address) throws Exception {
        logger.info("Fetching UTXOs for address: {}", address);
        return blockchainAPI.getUTXOs(address);
    }

    /**
     * Fetches transaction history for an address.
     *
     * @param address The Bitcoin address
     * @return List of transactions
     */
    public List<Transaction> getTransactionHistory(String address) throws Exception {
        logger.info("Fetching transaction history for address: {}", address);
        return blockchainAPI.getTransactionHistory(address);
    }

    /**
     * Estimates the fee for a transaction.
     *
     * @param inputs  Number of inputs
     * @param outputs Number of outputs
     * @return Estimated fee in satoshis
     */
    public long estimateFee(int inputs, int outputs) throws Exception {
        logger.info("Estimating fee for {} inputs and {} outputs", inputs, outputs);
        return blockchainAPI.estimateFee(inputs, outputs);
    }

    /**
     * Create and broadcast transaction
     */
    public String createAndBroadcastTransaction(String fromAddress, String toAddress, 
                                               long amount, String privateKeyWif, long feeRate) throws Exception {
        // Validate inputs
        if (!keyService.isValidAddress(fromAddress) || !keyService.isValidAddress(toAddress)) {
            throw new Exception("Invalid address format");
        }

        if (!keyService.isValidPrivateKey(privateKeyWif)) {
            throw new Exception("Invalid private key");
        }

        // Fetch UTXOs
        List<UTXO> utxos = blockchainAPI.getUTXOs(fromAddress);
        if (utxos.isEmpty()) {
            throw new Exception("No available UTXOs for address: " + fromAddress);
        }

        // Select coins
        long totalInput = 0;
        for (UTXO utxo : utxos) {
            totalInput += utxo.getAmount();
            if (totalInput >= amount) {
                break;
            }
        }

        // Calculate fee
        int inputs = 1;
        int outputs = 2; // one for recipient, one for change
        long fee = feeCalculator.calculateFee(inputs, outputs, feeRate);

        // Check if we have enough
        if (totalInput < amount + fee) {
            throw new Exception("Insufficient funds. Need: " + (amount + fee) + " satoshis, Have: " + totalInput);
        }

        // Calculate change
        long change = totalInput - amount - fee;

        // Build transaction (simplified)
        String txHex = buildTransaction(fromAddress, toAddress, amount, change, privateKeyWif);

        // Broadcast
        return blockchainAPI.broadcastTransaction(txHex);
    }

    /**
     * Build raw transaction
     */
    private String buildTransaction(String fromAddress, String toAddress, long amount, 
                                   long change, String privateKeyWif) {
        // Simplified transaction building
        // In production, use bitcoinj's Transaction class
        return "0100000001..."; // Placeholder
    }

    /**
     * Selects coins using a simple greedy algorithm.
     * This is a basic implementation - production code should use more sophisticated algorithms.
     *
     * @param utxos          Available UTXOs
     * @param targetAmount   Amount to send
     * @param estimatedFee   Estimated fee
     * @param feeRate        Current fee rate
     * @return CoinSelectionResult with selected UTXOs and change, or null if insufficient
     */
    private CoinSelectionResult selectCoins(List<UTXO> utxos, long targetAmount, long estimatedFee, long feeRate) {
        logger.debug("Selecting coins for amount: {} with estimated fee: {}", targetAmount, estimatedFee);

        // Sort UTXOs by value (largest first)
        List<UTXO> sorted = utxos.stream()
                .sorted(Comparator.comparingLong(UTXO::getValue).reversed())
                .collect(Collectors.toList());

        List<UTXO> selected = new ArrayList<>();
        long totalSelected = 0;
        long requiredAmount = targetAmount + estimatedFee;

        // Greedy coin selection
        for (UTXO utxo : sorted) {
            selected.add(utxo);
            totalSelected += utxo.getValue();

            // Recalculate fee with actual number of inputs
            long actualFee = feeCalculator.calculateFee(selected.size(), 2, feeRate);
            requiredAmount = targetAmount + actualFee;

            if (totalSelected >= requiredAmount) {
                logger.debug("Coin selection successful with {} UTXOs", selected.size());
                return new CoinSelectionResult(selected, totalSelected - requiredAmount);
            }
        }

        logger.warn("Insufficient coins selected: {} < {}", totalSelected, requiredAmount);
        return null;
    }

    /**
     * Creates transaction inputs from selected UTXOs.
     *
     * @param utxos Selected UTXOs
     * @return List of transaction inputs
     */
    private List<TxInput> createTransactionInputs(List<UTXO> utxos) {
        return utxos.stream()
                .map(utxo -> new TxInput(utxo.getTxHash(), utxo.getOutputIndex(), utxo.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Builds a raw transaction hex string (simplified).
     * In production, this would use BitcoinJ's transaction building.
     *
     * @param tx The transaction
     * @return Raw transaction hex
     */
    private String buildRawTransaction(Transaction tx) {
        logger.debug("Building raw transaction");
        // Simplified - actual implementation would use BitcoinJ
        return "0100000001" + System.nanoTime() + "00000000" + tx.getOutputs().size();
    }

    /**
     * Calculates total value of all UTXOs.
     *
     * @param utxos List of UTXOs
     * @return Total value in satoshis
     */
    private long getTotalUTXOValue(List<UTXO> utxos) {
        return utxos.stream().mapToLong(UTXO::getValue).sum();
    }

    /**
     * Helper class for coin selection results.
     */
    private static class CoinSelectionResult {
        List<UTXO> selectedUTXOs;
        long changeAmount;

        CoinSelectionResult(List<UTXO> selectedUTXOs, long changeAmount) {
            this.selectedUTXOs = selectedUTXOs;
            this.changeAmount = changeAmount;
        }
    }
}
