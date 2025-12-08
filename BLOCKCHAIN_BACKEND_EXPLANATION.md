# SatoshiVault Blockchain Backend - Complete Explanation

## Overview

The SatoshiVault blockchain backend is a **Spring Boot REST API** that integrates Bitcoin blockchain functionality. It allows users to check Bitcoin balances, view transaction history, manage UTXOs (unspent transaction outputs), calculate fees, and most importantly, **send Bitcoin transactions**.

The system acts as a **wallet backend** that communicates with the blockchain using the BlockCypher API.

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────┐
│      REST Controller (HTTP Endpoints)            │
│  BlockchainController.java                      │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│         Business Logic (Services)                │
│  - TransactionService                           │
│  - FeeCalculator                                │
│  - KeyService                                   │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│    Blockchain API Interface & Implementation     │
│  - BlockchainAPI (interface)                    │
│  - BlockCypherClient (implementation)           │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│    External Services & Utilities                 │
│  - HttpClientWrapper                            │
│  - BitcoinJ Library                             │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│         External Blockchain (Internet)           │
│  - BlockCypher API (testnet)                    │
│  - Bitcoin Network                              │
└─────────────────────────────────────────────────┘
```

---

## Key Components

### 1. **BlockchainController.java** 
**Purpose:** Exposes REST API endpoints for all blockchain operations.

**Endpoints:**
- `GET /api/balance/{address}` - Get address balance
- `GET /api/utxos/{address}` - Get unspent transaction outputs
- `GET /api/transactions/{address}` - Get transaction history
- `POST /api/fee-estimate` - Calculate transaction fees
- `POST /api/send` - Send Bitcoin

**Key Responsibility:** Validate HTTP requests, delegate to services, format responses.

---

### 2. **TransactionService.java**
**Purpose:** Core business logic for Bitcoin transactions.

**Key Methods:**

#### `sendBitcoin(fromAddress, toAddress, amount, privateKey)`
This is the **main transaction flow**:

```
STEP 1: Validate Inputs
├─ Check address format validity
└─ Verify amount > 0

STEP 2: Fetch Available UTXOs
├─ Call BlockCypherClient.getUTXOs()
└─ Retrieve unspent outputs from sender's address

STEP 3: Select UTXOs (Coin Selection)
├─ Use selectCoins() algorithm
├─ Choose which outputs to spend
└─ Minimize transaction size

STEP 4: Build Raw Transaction
├─ Create transaction inputs from selected UTXOs
├─ Create transaction outputs:
│  ├─ Output 1: recipient address + amount
│  └─ Output 2: change address + remaining funds
└─ Set fee based on transaction size

STEP 5: Sign Transaction
├─ Use private key to sign
├─ Use BitcoinJ library for signing
└─ Create digital signature

STEP 6: Broadcast to Network
├─ Call BlockCypherClient.broadcastTransaction()
├─ Send signed transaction to BlockCypher
└─ Network includes it in mempool

STEP 7: Return Transaction ID
└─ Return txId to user
```

**Coin Selection Algorithm:**
- Sorts UTXOs by value (largest first)
- Accumulates UTXOs until total ≥ (amount + estimated fee)
- Minimizes unnecessary inputs to reduce transaction size

#### `getBalance(address)`
- Calls BlockCypher API
- Returns total satoshis at address (1 BTC = 100,000,000 satoshis)

#### `getUTXOs(address)`
- Retrieves all unspent transaction outputs
- Used for coin selection in transactions
- Example UTXO: {txid, vout, amount, confirmations}

#### `getTransactionHistory(address)`
- Fetches all past transactions
- Limited to recent transactions
- Returns transaction details (txid, amount, confirmations)

---

### 3. **FeeCalculator.java**
**Purpose:** Calculates Bitcoin transaction fees accurately.

**Key Calculations:**

#### Transaction Size Formula
```
Size (bytes) = BASE_SIZE + (inputs × INPUT_SIZE) + (outputs × OUTPUT_SIZE)
           = 10 + (inputs × 148) + (outputs × 34)
```

**Why?**
- Bitcoin transactions are charged per byte
- More inputs/outputs = larger file size = higher fee
- Fee Rate (sat/byte) × Size (bytes) = Total Fee

#### Example:
- 1 input, 2 outputs = 10 + 148 + 68 = 226 bytes
- Fee rate = 5 sat/byte
- Fee = 226 × 5 = **1,130 satoshis**

#### Additional Calculations:
- **Dust Threshold:** Minimum output size worth including
- **Max Fee:** Prevents accidental overpayment
- **Fee Per Output:** Cost of adding extra outputs

---

### 4. **KeyService.java**
**Purpose:** Handles Bitcoin key operations (signing, validation).

**Key Methods:**

#### `getAddressFromPrivateKey(privateKeyWIF)`
- Converts WIF format private key → Bitcoin address
- Example: `cVjyv...` → `mipcBbFg9...`

#### `isValidPrivateKey(privateKeyWIF)`
- Validates WIF format (testnet only)
- Returns true/false

#### `isValidAddress(address)`
- Validates Bitcoin address format
- Checks testnet addresses start with 'm' or 'n'

#### `signMessage(message, privateKeyWIF)`
- Signs arbitrary message with private key
- Used for authentication/verification

#### `getPublicKeyFromPrivateKey(privateKeyWIF)`
- Derives public key from private key
- Hex format output

---

### 5. **BlockCypherClient.java**
**Purpose:** Implementation of blockchain API using BlockCypher service.

**What it does:**
- Makes HTTP requests to BlockCypher API
- Parses JSON responses
- Converts raw API data into our models (UTXO, Transaction, etc.)

**Why BlockCypher?**
- Free Bitcoin testnet API
- No setup required
- Provides balance, transaction history, UTXOs
- Can broadcast transactions to network

**Example API Call:**
```
GET https://api.blockcypher.com/v1/btc/test3/addrs/mipcBbFg9...?token=YOUR_TOKEN

Response:
{
  "address": "mipcBbFg9...",
  "balance": 50000000,  // 0.5 BTC
  "txrefs": [
    {"tx_hash": "abc123...", "tx_output_n": 0, "output_value": 30000000},
    {"tx_hash": "def456...", "tx_output_n": 1, "output_value": 20000000}
  ]
}
```

---

### 6. **Data Models**

#### UTXO (Unspent Transaction Output)
```java
class UTXO {
  String txid;           // Previous transaction hash
  int vout;              // Output index in that transaction
  long amount;           // Value in satoshis
  int confirmations;     // How many blocks confirmed
}
```
**Why?** These are the "coins" you spend in a transaction.

#### Transaction
```java
class Transaction {
  String txId;           // Unique transaction hash
  List<TxInput> inputs;  // Where money came from
  List<TxOutput> outputs;// Where money goes
  long fee;              // Network fee paid
  int confirmations;     // How many blocks confirmed
}
```

#### TxInput
```java
class TxInput {
  String previousTxid;   // Which UTXO we're spending
  int previousVout;      // Which output of that transaction
  String scriptSig;      // Digital signature (proof of ownership)
}
```

#### TxOutput
```java
class TxOutput {
  String address;        // Recipient address
  long value;            // Amount in satoshis
  String script;         // Locking script
}
```

---

## How Bitcoin Transactions Work (Simplified)

### Bitcoin Transaction Structure

```
┌─────────────────────────────────────────┐
│         Input (Money Source)            │
├─────────────────────────────────────────┤
│ Previous Tx Hash: abc123...             │
│ Previous Output Index: 0                │
│ Amount: 100,000 satoshis ◄─── UTXO    │
│ Signature: xyz789... ◄─────── Proof     │
└─────────────────────────────────────────┘
                    ↓
          [Bitcoin Network]
                    ↓
┌─────────────────────────────────────────┐
│        Outputs (Money Destinations)     │
├─────────────────────────────────────────┤
│ Output 1:                               │
│  ├─ Recipient: 1A1z7...                │
│  └─ Amount: 50,000 satoshis            │
├─────────────────────────────────────────┤
│ Output 2 (Change):                      │
│  ├─ Recipient: mipcBbFg9...            │
│  └─ Amount: 48,870 satoshis            │
│     (100,000 - 50,000 - 1,130 fee)    │
└─────────────────────────────────────────┘
```

### Why 2 Outputs?
- **Output 1:** Actual payment to recipient
- **Output 2:** Change back to sender (if any)
- Like cash: if you spend $100 bill for $30, you get $70 change

### Fee Calculation in Transaction
```
Total Input:  100,000 satoshis
Total Output: 50,000 + 48,870 = 98,870 satoshis
Fee:          100,000 - 98,870 = 1,130 satoshis
```

---

## Request & Response Examples

### Example 1: Check Balance

**Request:**
```http
GET /api/balance/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj
```

**Response:**
```json
{
  "address": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "balance": 50000000,
  "balanceBTC": 0.5
}
```

**What it means:**
- This address has 50,000,000 satoshis = 0.5 BTC

---

### Example 2: Get UTXOs

**Request:**
```http
GET /api/utxos/mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj
```

**Response:**
```json
{
  "address": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "utxoCount": 2,
  "totalValue": 50000000,
  "utxos": [
    {
      "txid": "abc123def456...",
      "vout": 0,
      "value": 30000000,
      "confirmations": 5
    },
    {
      "txid": "ghi789jkl012...",
      "vout": 1,
      "value": 20000000,
      "confirmations": 3
    }
  ]
}
```

**What it means:**
- 2 unspent outputs available (coins to spend)
- Total 50 million satoshis available

---

### Example 3: Send Bitcoin

**Request:**
```http
POST /api/send
Content-Type: application/json

{
  "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "toAddress": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
  "amount": 10000000,
  "privateKey": "cVjyv..."
}
```

**Response (Success):**
```json
{
  "success": true,
  "txId": "5e4a8f3c2b1a9d...",
  "fromAddress": "mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj",
  "toAddress": "mjXx2NvfB6vbvmxM5B1YLPAVJyh7xkfJBW",
  "amount": 10000000,
  "amountBTC": 0.1,
  "fee": 1130,
  "status": "broadcast"
}
```

**What happened:**
1. Found sender's UTXOs
2. Selected 30,000,000 sat UTXO (enough for 10,000,000 + 1,130 fee)
3. Created transaction with 2 outputs:
   - 10,000,000 to recipient
   - 18,998,870 change back to sender
4. Signed with private key
5. Broadcast to Bitcoin network
6. Returned txId for tracking

---

## Security Considerations

### Private Key Handling
- Private keys are **NEVER stored** on server
- Only **transmitted** from frontend in request
- Server uses them **temporarily** for signing only
- **Immediately forgotten** after use

### Validation
- Address format validation (Bitcoin addresses only)
- Amount validation (> 0)
- Private key format validation (WIF only)
- Sufficient funds check before transaction

### Testnet Only
- Uses Bitcoin **testnet** (not mainnet)
- Testnet BTC are worthless (free play money)
- For development/testing only
- Can request free testnet funds from faucets

---

## Dependencies

| Library | Purpose |
|---------|---------|
| **Spring Boot 3.2.0** | Web framework, REST API |
| **BitcoinJ 0.16.2** | Bitcoin cryptography, key management |
| **Google GSON** | JSON parsing |
| **Apache HttpClient 5** | HTTP requests |
| **SLF4J** | Logging |

---

## Workflow Summary

```
User Interface
    ↓
[HTTP Request to REST Endpoint]
    ↓
BlockchainController
    ↓
Service Layer (TransactionService, FeeCalculator, KeyService)
    ↓
BlockchainAPI Implementation (BlockCypherClient)
    ↓
External API (BlockCypher)
    ↓
Bitcoin Network / Blockchain
    ↓
[HTTP Response back to UI]
    ↓
User Interface Updated
```

---

## Troubleshooting

| Error | Cause | Solution |
|-------|-------|----------|
| "Invalid Bitcoin address" | Wrong address format | Use valid testnet address (starts with m/n) |
| "No UTXOs available" | No funds at address | Request testnet funds from faucet |
| "Insufficient funds" | Not enough satoshis | Need more funds for amount + fee |
| "Invalid private key" | Wrong WIF format | Use correct private key format |
| "API rate limit exceeded" | Too many BlockCypher requests | Wait a moment, try again |

---

## Conclusion

Your blockchain backend is a **complete Bitcoin wallet backend** that:
- ✅ Queries blockchain for balances and transactions
- ✅ Manages UTXOs (unspent outputs)
- ✅ Calculates accurate transaction fees
- ✅ Creates and signs transactions
- ✅ Broadcasts transactions to network
- ✅ Provides REST API for frontend integration

All of this is built on industry-standard libraries (BitcoinJ) and external services (BlockCypher) for reliability and security.
