package com.example.demo.blockchain.controller;

import com.example.demo.blockchain.model.Transaction;
import com.example.demo.blockchain.model.UTXO;
import com.example.demo.blockchain.service.FeeCalculator;
import com.example.demo.blockchain.service.KeyService;
import com.example.demo.blockchain.service.TransactionService;
import com.google.gson.JsonObject;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for blockchain operations.
 * Exposes endpoints for balance, transactions, UTXOs, fees, and sending Bitcoin.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BlockchainController {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainController.class);

    private final TransactionService transactionService;
    private final FeeCalculator feeCalculator;
    private final KeyService keyService;

    @Autowired
    public BlockchainController(TransactionService transactionService, FeeCalculator feeCalculator, KeyService keyService) {
        this.transactionService = transactionService;
        this.feeCalculator = feeCalculator;
        this.keyService = keyService;
    }

    /**
     * GET /api/balance/{address}
     * Fetches the current balance of a Bitcoin address.
     *
     * @param address The Bitcoin address
     * @return The balance in satoshis
     */
    @GetMapping("/balance/{address}")
    public ResponseEntity<?> getBalance(@PathVariable String address) {
        logger.info("GET /api/balance/{}", address);
        try {
            long balance = transactionService.getBalance(address);

            Map<String, Object> response = new HashMap<>();
            response.put("address", address);
            response.put("balance", balance);
            response.put("balanceBTC", balance / 100_000_000.0); // Convert to BTC

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching balance for address: {}", address, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Error fetching balance: " + e.getMessage()));
        }
    }

    /**
     * GET /api/utxos/{address}
     * Fetches UTXOs (unspent transaction outputs) for an address.
     *
     * @param address The Bitcoin address
     * @return List of UTXOs
     */
    @GetMapping("/utxos/{address}")
    public ResponseEntity<?> getUTXOs(@PathVariable String address) {
        logger.info("GET /api/utxos/{}", address);
        try {
            List<UTXO> utxos = transactionService.getUTXOs(address);

            Map<String, Object> response = new HashMap<>();
            response.put("address", address);
            response.put("utxoCount", utxos.size());
            response.put("utxos", utxos);
            response.put("totalValue", utxos.stream().mapToLong(UTXO::getValue).sum());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching UTXOs for address: {}", address, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Error fetching UTXOs: " + e.getMessage()));
        }
    }

    /**
     * GET /api/transactions/{address}
     * Fetches transaction history for an address.
     *
     * @param address The Bitcoin address
     * @param limit   Maximum number of transactions to fetch (default: 10)
     * @return List of transactions
     */
    @GetMapping("/transactions/{address}")
    public ResponseEntity<?> getTransactionHistory(
            @PathVariable String address,
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("GET /api/transactions/{} with limit {}", address, limit);
        try {
            List<Transaction> transactions = transactionService.getTransactionHistory(address);
            // Limit results to requested amount
            if (transactions.size() > limit) {
                transactions = transactions.subList(0, limit);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("address", address);
            response.put("transactionCount", transactions.size());
            response.put("transactions", transactions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching transaction history for address: {}", address, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Error fetching transaction history: " + e.getMessage()));
        }
    }

    /**
     * POST /api/fee-estimate
     * Estimates transaction fee based on number of inputs and outputs.
     *
     * @param payload JSON with "inputs" and "outputs" fields
     * @return Estimated fee in satoshis
     */
    @PostMapping("/fee-estimate")
    public ResponseEntity<?> estimateFee(@RequestBody Map<String, Integer> payload) {
        logger.info("POST /api/fee-estimate");
        try {
            int inputs = payload.getOrDefault("inputs", 1);
            int outputs = payload.getOrDefault("outputs", 2);

            if (inputs <= 0 || outputs <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Inputs and outputs must be greater than 0"));
            }

            long estimatedFee = transactionService.estimateFee(inputs, outputs);
            int txSize = feeCalculator.calculateTransactionSize(inputs, outputs);

            Map<String, Object> response = new HashMap<>();
            response.put("inputs", inputs);
            response.put("outputs", outputs);
            response.put("estimatedSize", txSize);
            response.put("estimatedFee", estimatedFee);
            response.put("estimatedFeeBTC", estimatedFee / 100_000_000.0);
            response.put("feePerByte", estimatedFee / (double) txSize);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error estimating fee", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error estimating fee: " + e.getMessage()));
        }
    }

    /**
     * POST /api/send
     * Sends Bitcoin from one address to another.
     * Request body should contain: fromAddress, toAddress, amount, privateKey
     *
     * @param payload JSON payload with transaction details
     * @return Transaction ID on success
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendBitcoin(@RequestBody Map<String, Object> payload) {
        logger.info("POST /api/send");
        try {
            String fromAddress = (String) payload.get("fromAddress");
            String toAddress = (String) payload.get("toAddress");
            long amount = ((Number) payload.get("amount")).longValue();
            String privateKey = (String) payload.get("privateKey");

            // Validate inputs
            if (fromAddress == null || toAddress == null || privateKey == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Missing required fields: fromAddress, toAddress, privateKey"));
            }

            if (amount <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Amount must be greater than 0"));
            }

            logger.info("Initiating transaction from {} to {} for {} satoshis", fromAddress, toAddress, amount);
            String txId = transactionService.sendBitcoin(fromAddress, toAddress, amount, privateKey);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("txId", txId);
            response.put("fromAddress", fromAddress);
            response.put("toAddress", toAddress);
            response.put("amount", amount);
            response.put("amountBTC", amount / 100_000_000.0);
            response.put("status", "broadcast");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for send transaction: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Runtime error during send transaction", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error sending Bitcoin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error sending Bitcoin: " + e.getMessage()));
        }
    }

    /**
     * GET /api/health
     * Health check endpoint for the blockchain module.
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        logger.info("GET /api/health");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("module", "Blockchain Integration");
        response.put("network", "Bitcoin Testnet");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to create error response JSON.
     *
     * @param message Error message
     * @return Error response map
     */
    /**
     * POST /api/generate-keypair
     * Generates a new Bitcoin keypair (address and private key).
     * Useful for creating new wallets.
     *
     * @return New Bitcoin address and private key (WIF format)
     */
    @PostMapping("/generate-keypair")
    public ResponseEntity<?> generateKeypair() {
        logger.info("POST /api/generate-keypair");
        try {
            // Generate new random private key
            ECKey key = new ECKey();
            
            // Get the private key in WIF format
            String privateKeyWIF = key.getPrivateKeyAsWiF(TestNet3Params.get());
            
            // Get the address from the private key
            String address = keyService.getAddressFromPrivateKey(privateKeyWIF);
            
            // Get the public key
            String publicKey = key.getPublicKeyAsHex();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("address", address);
            response.put("privateKey", privateKeyWIF);
            response.put("publicKey", publicKey);
            response.put("network", "testnet");
            response.put("message", "Save your private key securely! Do not share it with anyone.");

            logger.info("Generated new keypair - Address: {}", address);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating keypair", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error generating keypair: " + e.getMessage()));
        }
    }

    /**
     * POST /api/get-address
     * Derives the Bitcoin address from a private key.
     * Useful for verifying which address belongs to a private key.
     *
     * @param payload JSON with "privateKey" field (WIF format)
     * @return The Bitcoin address corresponding to the private key
     */
    @PostMapping("/get-address")
    public ResponseEntity<?> getAddress(@RequestBody Map<String, String> payload) {
        logger.info("POST /api/get-address");
        try {
            String privateKey = payload.get("privateKey");

            if (privateKey == null || privateKey.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Missing required field: privateKey"));
            }

            // Validate the private key format
            if (!keyService.isValidPrivateKey(privateKey)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Invalid private key format"));
            }

            // Derive the address from the private key
            String address = keyService.getAddressFromPrivateKey(privateKey);
            String publicKey = keyService.getPublicKeyFromPrivateKey(privateKey);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("privateKey", privateKey);
            response.put("address", address);
            response.put("publicKey", publicKey);
            response.put("network", "testnet");

            logger.info("Derived address from private key: {}", address);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deriving address", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deriving address: " + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}
