package com.example.demo.blockchain.service;

import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.stereotype.Service;

@Service
public class KeyService {

    private static final NetworkParameters NETWORK = TestNet3Params.get();

    /**
     * Get address from private key (WIF format)
     */
    public String getAddressFromPrivateKey(String privateKeyWIF) {
        try {
            DumpedPrivateKey dumpedKey = DumpedPrivateKey.fromBase58(NETWORK, privateKeyWIF);
            ECKey key = dumpedKey.getKey();
            return LegacyAddress.fromKey(NETWORK, key).toString();
        } catch (Exception e) {
            throw new RuntimeException("Invalid private key: " + e.getMessage());
        }
    }

    /**
     * Validate if private key is valid WIF format
     */
    public boolean isValidPrivateKey(String privateKeyWIF) {
        try {
            DumpedPrivateKey.fromBase58(NETWORK, privateKeyWIF);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate if address is valid Bitcoin address
     */
    public boolean isValidAddress(String address) {
        try {
            Address.fromString(NETWORK, address);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get public key from private key (hex format)
     */
    public String getPublicKeyFromPrivateKey(String privateKeyWIF) {
        try {
            DumpedPrivateKey dumpedKey = DumpedPrivateKey.fromBase58(NETWORK, privateKeyWIF);
            ECKey key = dumpedKey.getKey();
            return key.getPublicKeyAsHex();
        } catch (Exception e) {
            throw new RuntimeException("Invalid private key: " + e.getMessage());
        }
    }

    /**
     * Sign message with private key
     */
    public String signMessage(String message, String privateKeyWIF) {
        try {
            DumpedPrivateKey dumpedKey = DumpedPrivateKey.fromBase58(NETWORK, privateKeyWIF);
            ECKey key = dumpedKey.getKey();
            String signature = key.signMessage(message);
            return signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign message: " + e.getMessage());
        }
    }

    /**
     * Verify message signature
     */
    public boolean verifyMessage(String message, String signature, String address) {
        try {
            ECKey key = ECKey.signedMessageToKey(message, signature);
            String recoveredAddress = LegacyAddress.fromKey(NETWORK, key).toString();
            return recoveredAddress.equals(address);
        } catch (Exception e) {
            return false;
        }
    }
}
