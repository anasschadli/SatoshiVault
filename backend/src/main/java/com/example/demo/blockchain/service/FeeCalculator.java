package com.example.demo.blockchain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for calculating Bitcoin transaction fees.
 * Provides methods to estimate fees based on transaction size and network conditions.
 */
@Service
public class FeeCalculator {
    private static final Logger logger = LoggerFactory.getLogger(FeeCalculator.class);

    // Typical sizes in bytes for Bitcoin transactions
    private static final int BASE_TX_SIZE = 10; // Base transaction overhead
    private static final int INPUT_SIZE = 148;  // Approximate size of one input
    private static final int OUTPUT_SIZE = 34;  // Approximate size of one output

    /**
     * Calculates the estimated fee for a transaction.
     * Formula: (baseSize + (inputs * inputSize) + (outputs * outputSize)) * feeRate
     *
     * @param inputs  Number of inputs in the transaction
     * @param outputs Number of outputs in the transaction
     * @param feeRate Fee rate in satoshis per byte
     * @return Estimated fee in satoshis
     */
    public long calculateFee(int inputs, int outputs, long feeRate) {
        logger.debug("Calculating fee for {} inputs, {} outputs with rate {} sat/byte", inputs, outputs, feeRate);

        int transactionSize = calculateTransactionSize(inputs, outputs);
        long fee = transactionSize * feeRate;

        logger.info("Estimated transaction size: {} bytes, estimated fee: {} satoshis", transactionSize, fee);
        return fee;
    }

    /**
     * Calculates the estimated size of a transaction in bytes.
     *
     * @param inputs  Number of inputs
     * @param outputs Number of outputs
     * @return Estimated size in bytes
     */
    public int calculateTransactionSize(int inputs, int outputs) {
        // Simplified calculation - actual transaction size depends on script types
        // This assumes legacy P2PKH transactions
        int size = BASE_TX_SIZE + (inputs * INPUT_SIZE) + (outputs * OUTPUT_SIZE);
        logger.debug("Calculated transaction size: {} bytes", size);
        return size;
    }

    /**
     * Calculates the minimum change amount that makes sense to include.
     * Change amounts smaller than this may not be worth the fee cost.
     *
     * @param feeRate Fee rate in satoshis per byte
     * @return Minimum change amount in satoshis (dust threshold)
     */
    public long calculateDustThreshold(long feeRate) {
        // Bitcoin's dust threshold is typically 546 satoshis, but we use a higher one
        // to avoid paying more in fees than the output value
        long dustThreshold = (long) (OUTPUT_SIZE * feeRate * 3); // 3x the output fee

        logger.debug("Calculated dust threshold: {} satoshis", dustThreshold);
        return Math.max(dustThreshold, 546L); // Enforce Bitcoin's minimum
    }

    /**
     * Calculates maximum fee for a transaction (to prevent overpaying).
     *
     * @param inputs  Number of inputs
     * @param outputs Number of outputs
     * @return Maximum reasonable fee in satoshis (1% of transaction size)
     */
    public long calculateMaxFee(int inputs, int outputs) {
        int transactionSize = calculateTransactionSize(inputs, outputs);
        // Max fee is 1% of transaction size in satoshis per byte (very conservative)
        long maxFee = transactionSize * 100;

        logger.debug("Calculated max fee: {} satoshis", maxFee);
        return maxFee;
    }

    /**
     * Estimates fee per output for coin selection.
     * Used to calculate the cost of adding an output to a transaction.
     *
     * @param feeRate Fee rate in satoshis per byte
     * @return Cost of adding one output
     */
    public long calculateFeePerOutput(long feeRate) {
        long feePerOutput = OUTPUT_SIZE * feeRate;
        logger.debug("Fee per output: {} satoshis", feePerOutput);
        return feePerOutput;
    }

    /**
     * Estimates fee per input for coin selection.
     * Used to calculate the cost of adding an input (UTXO) to a transaction.
     *
     * @param feeRate Fee rate in satoshis per byte
     * @return Cost of adding one input
     */
    public long calculateFeePerInput(long feeRate) {
        long feePerInput = INPUT_SIZE * feeRate;
        logger.debug("Fee per input: {} satoshis", feePerInput);
        return feePerInput;
    }
}
