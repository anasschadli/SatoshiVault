# SatoshiVault API Quick Reference

## Running the Application

```bash
cd backend
java -jar target/satoshivault-blockchain-0.0.1-SNAPSHOT.jar
```

The API will be available at: **http://localhost:8080**

## API Endpoints

### 1. Health Check
**Endpoint:** `GET /api/health`

**Description:** Verify the API is running and healthy

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-12-08T15:34:18.000Z"
}
```

---

### 2. Get Balance
**Endpoint:** `GET /api/balance/{address}`

**Parameters:**
- `address` (path): Bitcoin address (Testnet3)

**Example:**
```
GET http://localhost:8080/api/balance/mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn
```

**Response:**
```json
{
  "address": "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn",
  "balance": 50000000,
  "unit": "satoshis"
}
```

---

### 3. Get UTXOs (Unspent Outputs)
**Endpoint:** `GET /api/utxos/{address}`

**Parameters:**
- `address` (path): Bitcoin address (Testnet3)

**Example:**
```
GET http://localhost:8080/api/utxos/mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn
```

**Response:**
```json
{
  "address": "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn",
  "utxoCount": 2,
  "totalValue": 50000000,
  "utxos": [
    {
      "txid": "abc123...",
      "vout": 0,
      "amount": 30000000,
      "confirmations": 5
    },
    {
      "txid": "def456...",
      "vout": 1,
      "amount": 20000000,
      "confirmations": 10
    }
  ]
}
```

---

### 4. Get Transaction History
**Endpoint:** `GET /api/transactions/{address}`

**Parameters:**
- `address` (path): Bitcoin address (Testnet3)
- `limit` (query, optional): Maximum number of transactions (default: 10)

**Example:**
```
GET http://localhost:8080/api/transactions/mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn?limit=5
```

**Response:**
```json
{
  "address": "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn",
  "transactionCount": 2,
  "transactions": [
    {
      "txid": "abc123...",
      "amount": 50000000,
      "confirmations": 5
    },
    {
      "txid": "def456...",
      "amount": 25000000,
      "confirmations": 10
    }
  ]
}
```

---

### 5. Estimate Fee
**Endpoint:** `GET /api/estimate-fee`

**Parameters:**
- `inputs` (query): Number of transaction inputs
- `outputs` (query): Number of transaction outputs

**Example:**
```
GET http://localhost:8080/api/estimate-fee?inputs=2&outputs=2
```

**Response:**
```json
{
  "inputs": 2,
  "outputs": 2,
  "estimatedFeeRate": 5,
  "estimatedFee": 1500,
  "unit": "satoshis"
}
```

**Calculation:** Fee = (inputs × 250 + outputs × 50) × feeRate
- inputs × 250 bytes
- outputs × 50 bytes  
- feeRate = 5 sat/byte (testnet)

---

### 6. Send Bitcoin
**Endpoint:** `POST /api/send`

**Request Body:**
```json
{
  "fromAddress": "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn",
  "toAddress": "2N8hwP1NsZrc9n6w6prL75LPnYuG123xyz",
  "amount": 10000000,
  "privateKeyWif": "cRp4uUnreGMZN8vB7nQFX6XSVS..."
}
```

**Parameters:**
- `fromAddress` (string): Sender's Bitcoin address
- `toAddress` (string): Recipient's Bitcoin address
- `amount` (number): Amount in satoshis
- `privateKeyWif` (string): Private key in WIF format (for signing)

**Response:**
```json
{
  "status": "broadcast",
  "txid": "1234567890abcdef...",
  "fromAddress": "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn",
  "toAddress": "2N8hwP1NsZrc9n6w6prL75LPnYuG123xyz",
  "amount": 10000000,
  "estimatedFee": 500,
  "confirmations": 0
}
```

**Error Response (Insufficient Funds):**
```json
{
  "error": "Insufficient funds: need 10500 satoshis but only have 5000",
  "timestamp": "2025-12-08T15:34:18.000Z"
}
```

---

## Error Responses

All endpoints return standard error responses on failure:

**400 Bad Request:**
```json
{
  "error": "Invalid Bitcoin address format",
  "timestamp": "2025-12-08T15:34:18.000Z"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Failed to connect to blockchain service",
  "timestamp": "2025-12-08T15:34:18.000Z"
}
```

---

## Testing with cURL

```bash
# Get balance
curl http://localhost:8080/api/balance/mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn

# Get UTXOs
curl http://localhost:8080/api/utxos/mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn

# Get transaction history
curl http://localhost:8080/api/transactions/mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn

# Estimate fee
curl "http://localhost:8080/api/estimate-fee?inputs=2&outputs=2"

# Send Bitcoin
curl -X POST http://localhost:8080/api/send \
  -H "Content-Type: application/json" \
  -d '{
    "fromAddress": "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn",
    "toAddress": "2N8hwP1NsZrc9n6w6prL75LPnYuG123xyz",
    "amount": 10000000,
    "privateKeyWif": "cRp4uUnreGMZN8vB7nQFX6XSVS..."
  }'
```

---

## Blockchain API Implementations

### BlockCypher (Default)
- **Base URL:** `https://api.blockcypher.com/v1/btc/test3`
- **Requires:** API Token (configured in `application.properties`)
- **Status:** ✅ Primary implementation

### Blockchain.com (Fallback)
- **Base URL:** `https://blockchain.info`
- **Requires:** No authentication
- **Status:** ✅ Available as fallback

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# BlockCypher API Token
blockcypher.api.url=https://api.blockcypher.com/v1/btc/test3
blockcypher.token=YOUR_TOKEN_HERE

# Server
server.port=8080
server.servlet.context-path=/

# Logging
logging.level.com.example.demo=INFO
logging.level.org.springframework.web=DEBUG
```

---

## Testnet Faucets

To get test Bitcoin for development:
- **BlockCypher Testnet Faucet:** https://www.blockcypher.com/tbtc/testnet
- **Bitcoin Testnet Faucet:** https://testnet-faucet.mempool.co
- **Binance Testnet Faucet:** https://testnet-faucet.mempool.co

---

## Network Information

- **Network:** Bitcoin Testnet3
- **Network ID:** `testnet3`
- **Difficulty:** Much lower than mainnet (easier to mine)
- **Block Time:** ~10 minutes average
- **Fee Market:** Dynamic, typically 1-50 sat/byte
- **Confirmations:** Usually 1-6 blocks (10-60 minutes)

