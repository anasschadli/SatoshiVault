package com.example.demo.blockchain.api;

import com.example.demo.blockchain.model.Transaction;
import com.example.demo.blockchain.model.UTXO;
import com.example.demo.blockchain.util.HttpClientWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BlockchainComClient implements BlockchainAPI {

    @Autowired
    private HttpClientWrapper httpClientWrapper;

    private static final String API_URL = "https://blockchain.info";

    @Override
    public long getBalance(String address) throws Exception {
        String url = API_URL + "/q/addressbalance/" + address;
        String response = httpClientWrapper.get(url);
        return Long.parseLong(response.trim());
    }

    @Override
    public List<UTXO> getUTXOs(String address) throws Exception {
        String url = API_URL + "/unspent?active=" + address;
        String response = httpClientWrapper.get(url);
        JsonObject jsonObject = httpClientWrapper.parseJson(response);

        List<UTXO> utxos = new ArrayList<>();
        if (jsonObject.has("unspent_outputs")) {
            JsonArray outputs = jsonObject.getAsJsonArray("unspent_outputs");
            for (int i = 0; i < outputs.size(); i++) {
                JsonObject out = outputs.get(i).getAsJsonObject();
                UTXO utxo = new UTXO();
                utxo.setTxid(out.get("tx_hash").getAsString());
                utxo.setVout(out.get("tx_output_n").getAsInt());
                utxo.setAmount(out.get("value").getAsLong());
                utxo.setConfirmations(out.get("confirmations").getAsInt());
                utxos.add(utxo);
            }
        }
        return utxos;
    }

    @Override
    public List<Transaction> getTransactionHistory(String address) throws Exception {
        String url = API_URL + "/address/" + address + "?format=json";
        String response = httpClientWrapper.get(url);
        JsonObject jsonObject = httpClientWrapper.parseJson(response);

        List<Transaction> transactions = new ArrayList<>();
        if (jsonObject.has("txs")) {
            JsonArray txs = jsonObject.getAsJsonArray("txs");
            for (int i = 0; i < txs.size(); i++) {
                JsonObject tx = txs.get(i).getAsJsonObject();
                Transaction transaction = new Transaction();
                transaction.setTxid(tx.get("hash").getAsString());
                transaction.setAmount(tx.get("result").getAsLong());
                transaction.setConfirmations(tx.get("confirmations").getAsInt());
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    @Override
    public long estimateFee(int inputs, int outputs) throws Exception {
        int estimatedSize = (inputs * 250) + (outputs * 50);
        return estimatedSize * 5; // 5 satoshis per byte
    }

    @Override
    public String broadcastTransaction(String signedTx) throws Exception {
        String url = API_URL + "/pushtx";
        JsonObject payload = new JsonObject();
        payload.addProperty("tx", signedTx);

        String response = httpClientWrapper.post(url, payload.toString());
        return response.replaceAll("\"", "");
    }

    @Override
    public boolean isValidAddress(String address) throws Exception {
        try {
            String url = API_URL + "/address/" + address + "?format=json";
            httpClientWrapper.get(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
