# SatoshiVault API - Complete Test Demo Guide

## Setup Instructions

### Prerequisites
1. Java 17+ installed
2. Maven installed
3. Backend running: `mvn spring-boot:run` (from backend folder)
4. API base URL: `http://localhost:8080/api`
5. Postman or cURL installed (for testing)

### Getting Testnet Bitcoin

Before testing, you need testnet funds:

1. Generate testnet address with private key:
   - Use blockchain.info testnet wallet or
   - Use SatoshiVault frontend to generate key pair

2. Request free testnet BTC from faucet:
   - https://testnet-faucet.mempool.co
   - https://bitcoinfaucet.uo1.net/send?address=YOUR_ADDRESS

3. Wait 5-10 minutes for confirmations

---

## API Endpoints Test Suite

### 1. GET /api/balance/{address}

**Purpose:** Fetch current balance of a Bitcoin address

#### Test Case 1.1: Valid Address with Funds

**Request:**
```bash
curl -X GET "http://localhost:8080/api/balance/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj"
```

**Expected Response (200 OK):**
```json
{
  "address": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "balance": 50000000,
  "balanceBTC": 0.5
}
```

**Validation Points:**
- ✓ Status code is 200
- ✓ Response contains address, balance, balanceBTC
- ✓ balanceBTC = balance / 100,000,000
- ✓ Address matches request

---

#### Test Case 1.2: Invalid Address Format

**Request:**
```bash
curl -X GET "http://localhost:8080/api/balance/not_a_valid_address"
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Error fetching balance: Invalid address"
}
```

**Validation Points:**
- ✓ Status code is 400 (not 200)
- ✓ Error message explains the problem
- ✓ Server doesn't crash

---

#### Test Case 1.3: Empty Address

**Request:**
```bash
curl -X GET "http://localhost:8080/api/balance/"
```

**Expected Response (404 Not Found):**
```json
{
  "timestamp": "2025-12-08T10:00:00.000+00:00",
  "status": 404,
  "error": "Not Found"
}
```

**Validation Points:**
- ✓ Status code is 404
- ✓ Proper error response

---

### 2. GET /api/utxos/{address}

**Purpose:** Get all unspent transaction outputs (UTXOs) for an address

#### Test Case 2.1: Address with Multiple UTXOs

**Request:**
```bash
curl -X GET "http://localhost:8080/api/utxos/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj"
```

**Expected Response (200 OK):**
```json
{
  "address": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "utxoCount": 2,
  "totalValue": 50000000,
  "utxos": [
    {
      "txid": "abc123def456789abcdef456789abcdef456789abcdef456789abcdef456",
      "vout": 0,
      "value": 30000000,
      "confirmations": 5,
      "amount": 30000000
    },
    {
      "txid": "ghi789jkl012345ghi789jkl012345ghi789jkl012345ghi789jkl012",
      "vout": 1,
      "value": 20000000,
      "confirmations": 3,
      "amount": 20000000
    }
  ]
}
```

**Validation Points:**
- ✓ Response contains array of UTXOs
- ✓ Each UTXO has txid, vout, value, confirmations
- ✓ totalValue = sum of all UTXO values
- ✓ utxoCount = number of UTXOs

---

#### Test Case 2.2: Address with No UTXOs

**Request:**
```bash
curl -X GET "http://localhost:8080/api/utxos/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj"
```

**Expected Response (200 OK):**
```json
{
  "address": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "utxoCount": 0,
  "totalValue": 0,
  "utxos": []
}
```

**Validation Points:**
- ✓ Status code is still 200 (empty list is valid)
- ✓ utxoCount is 0
- ✓ utxos array is empty
- ✓ totalValue is 0

---

### 3. GET /api/transactions/{address}

**Purpose:** Fetch transaction history for an address

#### Test Case 3.1: Address with Transactions

**Request:**
```bash
curl -X GET "http://localhost:8080/api/transactions/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj?limit=5"
```

**Expected Response (200 OK):**
```json
{
  "address": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "transactionCount": 2,
  "transactions": [
    {
      "txId": "abc123def456789abcdef456789abcdef456789abcdef456789abcdef456",
      "txid": "abc123def456789abcdef456789abcdef456789abcdef456789abcdef456",
      "inputs": [
        {
          "previousTxid": "older_tx_hash",
          "previousVout": 0,
          "amount": 30000000
        }
      ],
      "outputs": [
        {
          "address": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
          "value": 10000000
        }
      ],
      "fee": 1130,
      "confirmations": 2,
      "amount": 30000000
    },
    {
      "txId": "ghi789jkl012345ghi789jkl012345ghi789jkl012345ghi789jkl012",
      "txid": "ghi789jkl012345ghi789jkl012345ghi789jkl012345ghi789jkl012",
      "confirmations": 1,
      "amount": 20000000
    }
  ]
}
```

**Validation Points:**
- ✓ Returns array of transactions
- ✓ Each transaction has txId, inputs, outputs
- ✓ Number of transactions ≤ limit parameter
- ✓ transactionCount matches array size

---

#### Test Case 3.2: With Limit Parameter

**Request:**
```bash
curl -X GET "http://localhost:8080/api/transactions/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj?limit=2"
```

**Expected Response (200 OK):**
```json
{
  "address": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "transactionCount": 2,
  "transactions": [
    { "txId": "tx1", "amount": 30000000, "confirmations": 5 },
    { "txId": "tx2", "amount": 20000000, "confirmations": 3 }
  ]
}
```

**Validation Points:**
- ✓ transactionCount never exceeds limit
- ✓ Default limit = 10
- ✓ Returns most recent transactions first

---

### 4. POST /api/fee-estimate

**Purpose:** Calculate transaction fees based on inputs and outputs

#### Test Case 4.1: Standard Transaction (1 input, 2 outputs)

**Request:**
```bash
curl -X POST "http://localhost:8080/api/fee-estimate" \
  -H "Content-Type: application/json" \
  -d '{
    "inputs": 1,
    "outputs": 2
  }'
```

**Expected Response (200 OK):**
```json
{
  "inputs": 1,
  "outputs": 2,
  "estimatedSize": 226,
  "estimatedFee": 1130,
  "estimatedFeeBTC": 0.0000113,
  "feePerByte": 5.0
}
```

**Validation Points:**
- ✓ estimatedSize = 10 + (1 × 148) + (2 × 34) = 226 bytes
- ✓ estimatedFee = 226 × 5 = 1130 satoshis
- ✓ estimatedFeeBTC = 1130 / 100,000,000
- ✓ feePerByte = 5.0 (hardcoded for testnet)

---

#### Test Case 4.2: Multiple Inputs (3 inputs, 2 outputs)

**Request:**
```bash
curl -X POST "http://localhost:8080/api/fee-estimate" \
  -H "Content-Type: application/json" \
  -d '{
    "inputs": 3,
    "outputs": 2
  }'
```

**Expected Response (200 OK):**
```json
{
  "inputs": 3,
  "outputs": 2,
  "estimatedSize": 508,
  "estimatedFee": 2540,
  "estimatedFeeBTC": 0.0000254,
  "feePerByte": 5.0
}
```

**Formula Verification:**
- Size = 10 + (3 × 148) + (2 × 34) = 10 + 444 + 68 = 522 bytes

**Validation Points:**
- ✓ More inputs = larger transaction = higher fee
- ✓ Fee scales linearly with size
- ✓ Each additional input costs ~740 satoshis (148 bytes × 5 sat/byte)

---

#### Test Case 4.3: Invalid Input (0 inputs)

**Request:**
```bash
curl -X POST "http://localhost:8080/api/fee-estimate" \
  -H "Content-Type: application/json" \
  -d '{
    "inputs": 0,
    "outputs": 2
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Inputs and outputs must be greater than 0"
}
```

**Validation Points:**
- ✓ Status code is 400
- ✓ Rejects invalid input (0 inputs)
- ✓ Error message is clear

---

#### Test Case 4.4: Many Outputs (1 input, 10 outputs)

**Request:**
```bash
curl -X POST "http://localhost:8080/api/fee-estimate" \
  -H "Content-Type: application/json" \
  -d '{
    "inputs": 1,
    "outputs": 10
  }'
```

**Expected Response (200 OK):**
```json
{
  "inputs": 1,
  "outputs": 10,
  "estimatedSize": 498,
  "estimatedFee": 2490,
  "estimatedFeeBTC": 0.0000249,
  "feePerByte": 5.0
}
```

**Formula Verification:**
- Size = 10 + (1 × 148) + (10 × 34) = 10 + 148 + 340 = 498 bytes

**Validation Points:**
- ✓ Each additional output adds ~170 satoshis to fee
- ✓ Output-heavy transactions cost more

---

### 5. POST /api/send (The Main Event!)

**Purpose:** Send Bitcoin from one address to another

#### Test Case 5.1: Successful Bitcoin Transfer

**Prerequisites:**
- Source address has at least 11 million satoshis (10M + fee)
- You have the private key for source address
- Both addresses are valid testnet addresses

**Request:**
```bash
curl -X POST "http://localhost:8080/api/send" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
    "toAddress": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
    "amount": 10000000,
    "privateKey": "cVjyv..."
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "txId": "5e4a8f3c2b1a9d7e8f6c5a4b3c2d1e0f9a8b7c6d5e4a3b2c1d0e9f8a7b6c",
  "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "toAddress": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
  "amount": 10000000,
  "amountBTC": 0.1,
  "fee": 1130,
  "status": "broadcast"
}
```

**What Happened Behind the Scenes:**
```
1. Validated fromAddress and toAddress
2. Verified amount > 0
3. Called BlockCypher API to get UTXOs from fromAddress
4. Found UTXO: {txid: "abc123...", vout: 0, value: 30000000}
5. Created transaction with:
   - Input: the 30,000,000 sat UTXO
   - Output 1: 10,000,000 to toAddress
   - Output 2: 18,998,870 change back to fromAddress
   - Fee: 1,130 satoshis
6. Signed transaction with private key using BitcoinJ
7. Broadcast signed transaction to BlockCypher API
8. BlockCypher broadcast to Bitcoin testnet
9. Returned txId for tracking
```

**Validation Points:**
- ✓ success = true
- ✓ txId is 64 hex characters (256 bits)
- ✓ amount = 10,000,000 (what was requested)
- ✓ amountBTC = amount / 100,000,000 = 0.1
- ✓ fee = approximately 1,130 satoshis
- ✓ status = "broadcast"
- ✓ fromAddress and toAddress match request

**Track the Transaction:**
```bash
# Check balance of recipient (should increase after confirmations)
curl -X GET "http://localhost:8080/api/balance/mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW"

# Check transaction history (transaction will appear here)
curl -X GET "http://localhost:8080/api/transactions/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj"

# View on blockchain explorer
# https://testnet.blockexplore.com/tx/5e4a8f3c2b1a9d7e8f6c5a4b3c2d1e0f9a8b7c6d5e4a3b2c1d0e9f8a7b6c
```

---

#### Test Case 5.2: Invalid Recipient Address

**Request:**
```bash
curl -X POST "http://localhost:8080/api/send" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
    "toAddress": "invalid_address_123",
    "amount": 10000000,
    "privateKey": "cVjyv..."
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Error: Invalid Bitcoin address format"
}
```

**Validation Points:**
- ✓ Status code is 400
- ✓ Transaction is NOT created
- ✓ No BTC is sent
- ✓ Clear error message

---

#### Test Case 5.3: Insufficient Funds

**Request:**
```bash
curl -X POST "http://localhost:8080/api/send" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
    "toAddress": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
    "amount": 100000000,
    "privateKey": "cVjyv..."
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Error: Insufficient funds: need 101000000 satoshis but only have 50000000"
}
```

**Validation Points:**
- ✓ Status code is 400
- ✓ Transaction is NOT created
- ✓ Error shows required amount vs available
- ✓ No BTC is sent

---

#### Test Case 5.4: Invalid Private Key

**Request:**
```bash
curl -X POST "http://localhost:8080/api/send" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
    "toAddress": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
    "amount": 10000000,
    "privateKey": "not_a_valid_key_123"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Error: Invalid private key format"
}
```

**Validation Points:**
- ✓ Status code is 400
- ✓ Transaction is NOT created
- ✓ No BTC is sent
- ✓ Private key is validated before use

---

#### Test Case 5.5: Missing Required Field

**Request:**
```bash
curl -X POST "http://localhost:8080/api/send" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
    "toAddress": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
    "amount": 10000000
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Error: Missing required fields: fromAddress, toAddress, privateKey"
}
```

**Validation Points:**
- ✓ Status code is 400
- ✓ Clear message about missing fields
- ✓ Transaction is NOT created

---

#### Test Case 5.6: Zero or Negative Amount

**Request:**
```bash
curl -X POST "http://localhost:8080/api/send" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
    "toAddress": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
    "amount": 0,
    "privateKey": "cVjyv..."
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Error: Amount must be greater than 0"
}
```

**Validation Points:**
- ✓ Status code is 400
- ✓ Rejects zero amounts
- ✓ Rejects negative amounts
- ✓ No transaction created

---

## Complete Testing Workflow

### Scenario: Alice Sends 0.1 BTC to Bob

**Step 1: Check Alice's Balance**
```bash
curl -X GET "http://localhost:8080/api/balance/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj"

Response:
{
  "address": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "balance": 50000000,
  "balanceBTC": 0.5
}
```
✓ Alice has 0.5 BTC available

**Step 2: Check Alice's UTXOs**
```bash
curl -X GET "http://localhost:8080/api/utxos/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj"

Response:
{
  "utxoCount": 1,
  "totalValue": 50000000,
  "utxos": [
    {
      "txid": "abc123...",
      "vout": 0,
      "value": 50000000,
      "confirmations": 3
    }
  ]
}
```
✓ Alice has 1 UTXO with 0.5 BTC

**Step 3: Estimate Fee**
```bash
curl -X POST "http://localhost:8080/api/fee-estimate" \
  -H "Content-Type: application/json" \
  -d '{"inputs": 1, "outputs": 2}'

Response:
{
  "estimatedSize": 226,
  "estimatedFee": 1130,
  "estimatedFeeBTC": 0.0000113
}
```
✓ Transaction will cost 0.00001130 BTC

**Step 4: Send Bitcoin**
```bash
curl -X POST "http://localhost:8080/api/send" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
    "toAddress": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
    "amount": 10000000,
    "privateKey": "cVjyv..."
  }'

Response:
{
  "success": true,
  "txId": "5e4a8f3c2b1a9d...",
  "amount": 10000000,
  "amountBTC": 0.1,
  "fee": 1130,
  "status": "broadcast"
}
```
✓ Transaction broadcast! txId = 5e4a8f3c2b1a9d...

**Step 5: Check Bob's New Balance (wait 1-2 minutes)**
```bash
curl -X GET "http://localhost:8080/api/balance/mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW"

Response (after 1-2 minutes):
{
  "address": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
  "balance": 10000000,
  "balanceBTC": 0.1
}
```
✓ Bob received 0.1 BTC!

**Step 6: Check Alice's Remaining Balance**
```bash
curl -X GET "http://localhost:8080/api/balance/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj"

Response:
{
  "address": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "balance": 39998870,
  "balanceBTC": 0.39998870
}
```
✓ Alice has: 50,000,000 - 10,000,000 - 1,130 = 39,998,870 satoshis

**Step 7: Check Transaction History**
```bash
curl -X GET "http://localhost:8080/api/transactions/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj"

Response:
{
  "transactionCount": 1,
  "transactions": [
    {
      "txId": "5e4a8f3c2b1a9d...",
      "amount": 10000000,
      "confirmations": 1
    }
  ]
}
```
✓ Transaction appears in history!

---

## Error Scenarios Summary

| Error Case | HTTP Status | When It Happens |
|-----------|------------|-----------------|
| Invalid address | 400 | Wrong address format |
| No UTXOs available | 400 | Address has no coins |
| Insufficient funds | 400 | Need more satoshis |
| Invalid private key | 400 | Wrong WIF format |
| Missing field | 400 | fromAddress, toAddress, etc. missing |
| Zero amount | 400 | amount ≤ 0 |
| Server error | 500 | Internal server crash |
| API rate limit | 429 | Too many BlockCypher requests |

---

## Performance Metrics (Expected)

| Operation | Time | Notes |
|-----------|------|-------|
| Get balance | <1s | Direct API call |
| Get UTXOs | <1s | Fetches available coins |
| Get transactions | <2s | Fetches transaction history |
| Estimate fee | <100ms | Local calculation |
| Send Bitcoin | 2-5s | Includes signing & broadcast |
| Blockchain confirmation | 10-30 min | Testnet blocks every ~10 min |

---

## Demo Talking Points for Professor

1. **Architecture:** "The API is organized in layers - controller handles HTTP, services handle business logic, and the API client communicates with blockchain"

2. **UTXO Model:** "Bitcoin uses UTXOs (like physical coins) instead of account balances. When we spend, we select UTXOs like choosing which coins from a wallet to use"

3. **Transaction Structure:** "Each transaction has inputs (money source) and outputs (destinations). If we spend a 50sat UTXO but only need 30sat, we automatically create a change output for the remaining 20sat minus fees"

4. **Fee Calculation:** "Fees are based on transaction size in bytes, not the amount sent. More inputs/outputs = larger tx = higher fee. This incentivizes efficient transactions"

5. **Security:** "Private keys are never stored on the server. They're only used temporarily for signing and then forgotten. We use BitcoinJ library which is trusted Bitcoin cryptography"

6. **Coin Selection:** "Our algorithm intelligently selects which UTXOs to spend to minimize transaction size and fees while ensuring sufficient funds"

7. **Real Blockchain:** "We broadcast to the actual Bitcoin testnet. That's real Bitcoin! (just worthless testnet Bitcoin for development)"

---

## Conclusion

Your backend is production-grade code that:
- ✅ Handles Bitcoin transactions end-to-end
- ✅ Validates all inputs rigorously
- ✅ Calculates fees accurately
- ✅ Manages security properly
- ✅ Provides comprehensive REST API
- ✅ Integrates with real Bitcoin network
