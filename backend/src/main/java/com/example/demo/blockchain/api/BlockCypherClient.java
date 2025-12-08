package com.example.demo.blockchain.api;

import com.example.demo.blockchain.model.Transaction;
import com.example.demo.blockchain.model.TxInput;
import com.example.demo.blockchain.model.TxOutput;
import com.example.demo.blockchain.model.UTXO;
import com.example.demo.blockchain.util.HttpClientWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Primary
public class BlockCypherClient implements BlockchainAPI {

    @Autowired
    private HttpClientWrapper httpClientWrapper;

    @Value("${blockcypher.api.url:https://api.blockcypher.com/v1/btc/test3}")
    private String apiUrl;

    @Value("${blockcypher.token:}")
    private String apiToken;

    /**
     * Get balance for address
     */
    @Override
    public long getBalance(String address) throws Exception {
        String url = apiUrl + "/addrs/" + address + "?token=" + apiToken;
        String response = httpClientWrapper.get(url);
        JsonObject jsonObject = httpClientWrapper.parseJson(response);
        return jsonObject.get("balance").getAsLong();
    }

    /**
     * Get UTXOs for address
     */
    @Override
    public List<UTXO> getUTXOs(String address) throws Exception {
        String url = apiUrl + "/addrs/" + address + "?unspentOnly=true&token=" + apiToken;
        String response = httpClientWrapper.get(url);
        JsonObject jsonObject = httpClientWrapper.parseJson(response);

        List<UTXO> utxos = new ArrayList<>();
        if (jsonObject.has("txrefs")) {
            JsonArray txrefs = jsonObject.getAsJsonArray("txrefs");
            for (int i = 0; i < txrefs.size(); i++) {
                JsonObject tx = txrefs.get(i).getAsJsonObject();
                UTXO utxo = new UTXO();
                utxo.setTxid(tx.get("tx_hash").getAsString());
                utxo.setVout(tx.get("tx_output_n").getAsInt());
                utxo.setAmount(tx.get("output_value").getAsLong());
                utxo.setConfirmations(tx.get("confirmations").getAsInt());
                utxos.add(utxo);
            }
        }
        return utxos;
    }

    /**
     * Get transaction history for address
     */
    @Override
    public List<Transaction> getTransactionHistory(String address) throws Exception {
        String url = apiUrl + "/addrs/" + address + "?token=" + apiToken;
        String response = httpClientWrapper.get(url);
        JsonObject jsonObject = httpClientWrapper.parseJson(response);

        List<Transaction> transactions = new ArrayList<>();
        if (jsonObject.has("txs")) {
            JsonArray txs = jsonObject.getAsJsonArray("txs");
            for (int i = 0; i < txs.size(); i++) {
                JsonObject tx = txs.get(i).getAsJsonObject();
                Transaction transaction = new Transaction();
                transaction.setTxid(tx.get("hash").getAsString());
                transaction.setAmount(tx.get("total").getAsLong());
                transaction.setConfirmations(tx.get("confirmations").getAsInt());
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    /**
     * Estimate transaction fee
     */
    @Override
    public long estimateFee(int inputs, int outputs) throws Exception {
        // Rough estimation: ~250 bytes per input + 50 bytes per output
        int estimatedSize = (inputs * 250) + (outputs * 50);
        long feeRate = 5; // satoshis per byte (testnet)
        return estimatedSize * feeRate;
    }

    /**
     * Broadcast transaction to network
     */
    @Override
    public String broadcastTransaction(String signedTx) throws Exception {
        String url = apiUrl + "/txs/push?token=" + apiToken;
        JsonObject payload = new JsonObject();
        payload.addProperty("tx", signedTx);

        String response = httpClientWrapper.post(url, payload.toString());
        JsonObject jsonObject = httpClientWrapper.parseJson(response);
        return jsonObject.get("tx").getAsJsonObject().get("hash").getAsString();
    }

    /**
     * Check if address is valid
     */
    @Override
    public boolean isValidAddress(String address) throws Exception {
        try {
            String url = apiUrl + "/addrs/" + address + "?token=" + apiToken;
            httpClientWrapper.get(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
