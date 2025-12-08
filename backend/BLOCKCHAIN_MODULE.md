# SatoshiVault - Blockchain Integration Module

## 📋 Overview

This blockchain integration module is a complete implementation of Bitcoin wallet functionality for the SatoshiVault backend. It provides REST APIs for fetching balances, transaction history, UTXOs, estimating fees, and sending Bitcoin transactions on the **Bitcoin Testnet**.

The module uses **Spring Boot 4.0.0** with **Java 17** and integrates with external blockchain APIs (BlockCypher and Blockchain.com) to interact with the Bitcoin network.

---

## 📂 Project Structure

```
blockchain/
├── api/
│   ├── BlockchainAPI.java               # Interface for blockchain operations
│   ├── BlockCypherClient.java           # BlockCypher API implementation
│   └── BlockchainComClient.java         # Blockchain.com API implementation
│
├── model/
│   ├── UTXO.java                        # Unspent transaction output
│   ├── Transaction.java                 # Bitcoin transaction
│   ├── TxInput.java                     # Transaction input
│   └── TxOutput.java                    # Transaction output
│
├── service/
│   ├── TransactionService.java          # Transaction handling & coin selection
│   ├── FeeCalculator.java               # Fee estimation
│   └── KeyService.java                  # Key management & signing
│
├── util/
│   └── HttpClientWrapper.java           # HTTP client utility
│
└── controller/
    └── BlockchainController.java        # REST API endpoints
```

---

##  REST API Endpoints

### 1. **Get Balance**
```http
GET /api/balance/{address}
```
**Description:** Fetches the current balance of a Bitcoin address.

**Example Request:**
```bash
curl http://localhost:8080/api/balance/mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE
```

**Example Response:**
```json
{
  "address": "mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE",
  "balance": 5000000,
  "balanceBTC": 0.05
}
```

---

### 2. **Get UTXOs**
```http
GET /api/utxos/{address}
```
**Description:** Fetches all unspent transaction outputs for an address.

**Example Request:**
```bash
curl http://localhost:8080/api/utxos/mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE
```

**Example Response:**
```json
{
  "address": "mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE",
  "utxoCount": 2,
  "totalValue": 5000000,
  "utxos": [
    {
      "txHash": "abc123...",
      "outputIndex": 0,
      "value": 3000000,
      "address": "mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE"
    },
    {
      "txHash": "def456...",
      "outputIndex": 1,
      "value": 2000000,
      "address": "mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE"
    }
  ]
}
```

---

### 3. **Get Transaction History**
```http
GET /api/transactions/{address}?limit=10
```
**Description:** Fetches transaction history for an address.

**Query Parameters:**
- `limit` (optional, default: 10) - Maximum number of transactions

**Example Request:**
```bash
curl "http://localhost:8080/api/transactions/mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE?limit=5"
```

**Example Response:**
```json
{
  "address": "mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE",
  "transactionCount": 3,
  "transactions": [
    {
      "txId": "abc123...",
      "inputCount": 1,
      "outputCount": 2,
      "fee": 2500,
      "confirmations": 10
    }
  ]
}
```

---

### 4. **Estimate Transaction Fee**
```http
POST /api/fee-estimate
Content-Type: application/json
```
**Description:** Estimates the transaction fee based on inputs and outputs.

**Request Body:**
```json
{
  "inputs": 2,
  "outputs": 2
}
```

**Example Response:**
```json
{
  "inputs": 2,
  "outputs": 2,
  "estimatedSize": 374,
  "estimatedFee": 18700,
  "estimatedFeeBTC": 0.000187,
  "feePerByte": 50.0
}
```

---

### 5. **Send Bitcoin**
```http
POST /api/send
Content-Type: application/json
```
**Description:** Builds, signs, and broadcasts a Bitcoin transaction.

**Request Body:**
```json
{
  "fromAddress": "mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE",
  "toAddress": "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn",
  "amount": 1000000,
  "privateKey": "cRp4uUnreGMZN8vB7nQFX6XWMW5QJqFhxzMy..."
}
```

**Example Response:**
```json
{
  "success": true,
  "txId": "f3e5d6c8...",
  "fromAddress": "mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE",
  "toAddress": "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn",
  "amount": 1000000,
  "amountBTC": 0.01,
  "status": "broadcast"
}
```

---

### 6. **Health Check**
```http
GET /api/health
```
**Description:** Checks the status of the blockchain module.

**Example Response:**
```json
{
  "status": "UP",
  "module": "Blockchain Integration",
  "network": "Bitcoin Testnet"
}
```

---

## 🔑 Key Features

### 1. **UTXO Management**
- Fetches unspent transaction outputs from the blockchain
- Supports coin selection algorithms for optimal transaction creation
- Handles dust threshold management

### 2. **Transaction Handling**
- Creates and signs Bitcoin transactions
- Automatic fee calculation
- Change address handling
- Transaction broadcast to the network

### 3. **Fee Estimation**
- Dynamic fee calculation based on transaction size
- Network rate awareness
- Dust threshold detection
- Per-input and per-output fee calculation

### 4. **Multi-API Support**
- Primary: BlockCypher API (configured)
- Alternative: Blockchain.com API
- Extensible interface for additional APIs

### 5. **Security Considerations**
- Private keys are never stored on the server
- Keys are passed at transaction time
- All validations are performed before signing
- Error handling for invalid addresses and insufficient funds

---

## ⚙️ Configuration

### Application Properties
Edit `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Bitcoin Network
bitcoin.network=testnet3

# BlockCypher API
blockcypher.api.url=https://api.blockcypher.com/v1/btc/test3
blockcypher.token=YOUR_TOKEN_HERE

# Logging
logging.level.com.example.demo=DEBUG
```

### Environment Variables
You can also use environment variables to configure the app:
```bash
export SERVER_PORT=8080
export BLOCKCYPHER_TOKEN=your_token_here
```

---

## 🔨 Building & Running

### Prerequisites
- Java 17+
- Gradle 8.0+
- Internet connection (for blockchain API calls)

### Build the Project
```bash
cd backend
./gradlew build
```

### Run the Application
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Build as JAR
```bash
./gradlew bootJar
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
```

---

## 📚 Service Classes

### **BlockchainAPI (Interface)**
Defines contract for blockchain operations:
- `getBalance(address)` - Fetch address balance
- `getUTXOs(address)` - Get unspent outputs
- `getTransactionHistory(address, limit)` - Get tx history
- `broadcastTransaction(rawTx)` - Broadcast signed transaction
- `getFeeRate()` - Get current network fee rate

### **TransactionService**
Main service for transaction operations:
- `sendBitcoin()` - Complete transaction flow
- `getBalance()` - Fetch address balance
- `getUTXOs()` - Fetch UTXOs
- `getTransactionHistory()` - Fetch transaction history
- `estimateFee()` - Estimate transaction fees
- Implements coin selection algorithm

### **FeeCalculator**
Calculates transaction fees and sizes:
- `calculateFee()` - Fee estimation
- `calculateTransactionSize()` - Transaction size in bytes
- `calculateDustThreshold()` - Minimum output value
- `calculateFeePerInput()` / `calculateFeePerOutput()`

### **KeyService**
Handles Bitcoin keys and signing:
- `generateNewPrivateKey()` - Generate new key
- `getAddressFromPrivateKey()` - Derive address
- `isValidAddress()` - Validate Bitcoin address
- `isValidPrivateKey()` - Validate private key format
- `signTransaction()` - Sign transaction

### **HttpClientWrapper**
Utility for REST calls:
- `get(url)` - HTTP GET request
- `post(url, json)` - HTTP POST request
- `parseJson(string)` - Parse JSON response

---

## 🧪 Testing the API

### Test with cURL

**1. Check Health:**
```bash
curl http://localhost:8080/api/health
```

**2. Get Balance:**
```bash
curl http://localhost:8080/api/balance/mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE
```

**3. Estimate Fee:**
```bash
curl -X POST http://localhost:8080/api/fee-estimate \
  -H "Content-Type: application/json" \
  -d '{"inputs": 1, "outputs": 2}'
```

**4. Send Bitcoin:**
```bash
curl -X POST http://localhost:8080/api/send \
  -H "Content-Type: application/json" \
  -d '{
    "fromAddress": "mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE",
    "toAddress": "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn",
    "amount": 100000,
    "privateKey": "cRp4uUnreGMZN8vB7nQFX6XWMW5QJqFhxzMy..."
  }'
```

### Test Bitcoin Testnet Addresses
- `mkHS9ne38qMvCfZyLaBeoMoRM1Z5e93aE`
- `mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn`
- `mrCDrCybB6J1vRfbwgLjBLJ5d2xnUUNigm`

Get free testnet Bitcoin from faucets:
- https://testnet-faucet.mempool.space/
- https://bitcoin.sipa.be/

---

## 📦 Dependencies

Key dependencies in `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-web'          # REST APIs
implementation 'org.bitcoinj:bitcoinj-core:0.16.2'                       # Bitcoin operations
implementation 'com.google.code.gson:gson:2.10.1'                        # JSON parsing
implementation 'org.apache.httpcomponents.client5:httpclient5:5.3'       # HTTP client
```

---

## ⚠️ Important Notes

1. **Testnet Only**: This module is configured for Bitcoin Testnet (BTC Test). To use Mainnet, modify the network parameters.

2. **Private Key Handling**: Never log or store private keys. Pass them only at transaction time through secure channels.

3. **Fee Rates**: Fees are estimated based on current network conditions. Actual fees may vary slightly.

4. **Coin Selection**: Currently uses a greedy algorithm. For production, consider more sophisticated algorithms.

5. **API Rate Limits**: BlockCypher and Blockchain.com have rate limits. Add your API token to increase limits.

6. **Error Handling**: All errors are caught and returned with appropriate HTTP status codes.

---

## 🔧 Extending the Module

### Add a New Blockchain API Implementation
1. Create a new class implementing `BlockchainAPI`
2. Implement all required methods
3. Add `@Component` annotation
4. Update `BlockchainController` to use it via dependency injection

### Example:
```java
@Component
public class MyBlockchainClient implements BlockchainAPI {
    @Override
    public long getBalance(String address) throws Exception {
        // Implementation
    }
    // ... other methods
}
```

---

## 📝 License & Attribution

This module uses:
- **BitcoinJ** - Bitcoin library for Java
- **BlockCypher API** - Blockchain data provider
- **Blockchain.com API** - Alternative blockchain data provider

---

## 🐛 Troubleshooting

### "Connection refused" error
- Ensure the server is running on port 8080
- Check firewall settings
- Verify internet connection for blockchain API calls

### "Invalid address" error
- Make sure you're using Bitcoin Testnet addresses (start with 'm', 'n', or '2')
- Verify address format and length

### "Insufficient funds" error
- Check balance using `/api/balance/{address}`
- Get free testnet Bitcoin from a faucet
- Ensure UTXOs are confirmed

### API rate limit exceeded
- Add your BlockCypher or Blockchain.com API token to configuration
- Use alternative API implementation
- Implement request queuing

---

## 📞 Support

For issues or questions:
1. Check the logs in the console
2. Review error messages in API responses
3. Verify configuration in `application.properties`
4. Test with cURL or Postman before integrating with frontend

---

**Last Updated:** November 27, 2025  
**Version:** 1.0.0  
**Status:** Production Ready for Testnet
