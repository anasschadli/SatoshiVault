# SatoshiVault Database Schema Guide

## Overview

This document provides the database schema required for the SatoshiVault project. The database stores wallet information, transactions, UTXOs, and user data related to Bitcoin blockchain operations.

**Database Type:** MySQL/PostgreSQL (recommended: PostgreSQL for reliability)

---

## Database Tables

### 1. **users** Table
Stores user account information.

```sql
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_username (username)
);
```

**Columns:**
- `user_id` - Unique user identifier
- `username` - Account username (unique)
- `email` - Account email (unique)
- `password_hash` - Hashed password (never store plaintext!)
- `full_name` - User's real name
- `phone` - Contact phone number
- `created_at` - Account creation timestamp
- `updated_at` - Last update timestamp
- `is_active` - Account status
- `last_login` - Last login time

**Indexes:** email, username (for quick lookups)

---

### 2. **wallets** Table
Stores Bitcoin wallet information for each user.

```sql
CREATE TABLE wallets (
    wallet_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    wallet_name VARCHAR(255) NOT NULL,
    bitcoin_address VARCHAR(255) UNIQUE NOT NULL,
    public_key VARCHAR(255),
    private_key_encrypted VARCHAR(255),
    network VARCHAR(20) DEFAULT 'testnet',
    balance_satoshis BIGINT DEFAULT 0,
    balance_btc DECIMAL(18, 8) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_synced TIMESTAMP NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    is_watch_only BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_address (user_id, bitcoin_address),
    INDEX idx_user_id (user_id),
    INDEX idx_bitcoin_address (bitcoin_address)
);
```

**Columns:**
- `wallet_id` - Unique wallet identifier
- `user_id` - FK to users table
- `wallet_name` - User-friendly wallet name (e.g., "My Main Wallet")
- `bitcoin_address` - Bitcoin address (testnet format: starts with m/n)
- `public_key` - Public key in hex format
- `private_key_encrypted` - Private key encrypted (IMPORTANT: Never store plaintext)
- `network` - Network type ('testnet' or 'mainnet')
- `balance_satoshis` - Current balance in satoshis
- `balance_btc` - Current balance in BTC (cached, calculated from satoshis)
- `created_at` - Wallet creation date
- `updated_at` - Last update time
- `last_synced` - Last blockchain sync time
- `is_primary` - Mark as primary wallet
- `is_watch_only` - Read-only wallet (can view but not spend)

**Relationships:**
- Foreign Key: `user_id` → `users.user_id`

**Indexes:** user_id, bitcoin_address

---

### 3. **transactions** Table
Stores Bitcoin transactions made from the wallet.

```sql
CREATE TABLE transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    wallet_id INT NOT NULL,
    txid VARCHAR(255) UNIQUE NOT NULL,
    from_address VARCHAR(255) NOT NULL,
    to_address VARCHAR(255) NOT NULL,
    amount_satoshis BIGINT NOT NULL,
    amount_btc DECIMAL(18, 8) NOT NULL,
    fee_satoshis BIGINT NOT NULL,
    fee_btc DECIMAL(18, 8),
    status VARCHAR(50) DEFAULT 'pending',
    confirmations INT DEFAULT 0,
    timestamp BIGINT,
    block_number INT NULL,
    block_height INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    input_count INT,
    output_count INT,
    transaction_size INT,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_txid (txid),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_from_address (from_address),
    INDEX idx_to_address (to_address)
);
```

**Columns:**
- `transaction_id` - Unique transaction identifier
- `wallet_id` - FK to wallets table
- `txid` - Transaction ID (Bitcoin blockchain ID)
- `from_address` - Sender's Bitcoin address
- `to_address` - Recipient's Bitcoin address
- `amount_satoshis` - Amount sent in satoshis
- `amount_btc` - Amount sent in BTC
- `fee_satoshis` - Network fee in satoshis
- `fee_btc` - Network fee in BTC
- `status` - Transaction status ('pending', 'confirmed', 'failed')
- `confirmations` - Number of blockchain confirmations
- `timestamp` - Transaction timestamp from blockchain
- `block_number` - Block number containing transaction
- `block_height` - Block height in blockchain
- `created_at` - When transaction was recorded locally
- `updated_at` - Last update time
- `input_count` - Number of inputs in transaction
- `output_count` - Number of outputs in transaction
- `transaction_size` - Size in bytes

**Relationships:**
- Foreign Key: `wallet_id` → `wallets.wallet_id`

**Indexes:** wallet_id, txid, status, created_at, from_address, to_address

**Sample Data:**
```sql
INSERT INTO transactions 
(wallet_id, txid, from_address, to_address, amount_satoshis, amount_btc, fee_satoshis, fee_btc, status, confirmations)
VALUES 
(1, 'abc123def456...', 'mipcBbFg9...', 'mjXx2Nvf...', 10000000, 0.1, 1130, 0.0000113, 'confirmed', 3);
```

---

### 4. **transaction_inputs** Table
Stores inputs (UTXOs being spent) for each transaction.

```sql
CREATE TABLE transaction_inputs (
    input_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    previous_txid VARCHAR(255) NOT NULL,
    previous_vout INT NOT NULL,
    script_sig VARCHAR(1000),
    value_satoshis BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_previous_txid (previous_txid)
);
```

**Columns:**
- `input_id` - Unique input identifier
- `transaction_id` - FK to transactions table
- `previous_txid` - Hash of previous transaction (UTXO source)
- `previous_vout` - Output index in previous transaction
- `script_sig` - Digital signature script
- `value_satoshis` - Value of this input
- `created_at` - Record creation time

**Relationships:**
- Foreign Key: `transaction_id` → `transactions.transaction_id`

**Purpose:** Each row represents one input (UTXO being spent) in a transaction.

**Example:** If a transaction has 2 inputs, there will be 2 rows in this table.

---

### 5. **transaction_outputs** Table
Stores outputs (destinations) for each transaction.

```sql
CREATE TABLE transaction_outputs (
    output_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    output_index INT NOT NULL,
    recipient_address VARCHAR(255) NOT NULL,
    value_satoshis BIGINT NOT NULL,
    value_btc DECIMAL(18, 8),
    script_pubkey VARCHAR(1000),
    is_change BOOLEAN DEFAULT FALSE,
    spent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_recipient_address (recipient_address),
    INDEX idx_output_index (output_index)
);
```

**Columns:**
- `output_id` - Unique output identifier
- `transaction_id` - FK to transactions table
- `output_index` - Index of this output in transaction
- `recipient_address` - Destination Bitcoin address
- `value_satoshis` - Amount sent to recipient
- `value_btc` - Amount in BTC
- `script_pubkey` - Locking script
- `is_change` - TRUE if this is change output back to sender
- `spent` - TRUE if this output has been spent in another transaction
- `created_at` - Record creation time

**Relationships:**
- Foreign Key: `transaction_id` → `transactions.transaction_id`

**Purpose:** Each row represents one output destination in a transaction.

**Example:** 
- Output 1: 10 BTC to recipient (is_change = FALSE)
- Output 2: 0.1 BTC change back to sender (is_change = TRUE)

---

### 6. **utxos** Table
Stores unspent transaction outputs (available coins).

```sql
CREATE TABLE utxos (
    utxo_id INT PRIMARY KEY AUTO_INCREMENT,
    wallet_id INT NOT NULL,
    txid VARCHAR(255) NOT NULL,
    vout INT NOT NULL,
    address VARCHAR(255) NOT NULL,
    value_satoshis BIGINT NOT NULL,
    value_btc DECIMAL(18, 8),
    script_pubkey VARCHAR(1000),
    confirmations INT DEFAULT 0,
    is_spent BOOLEAN DEFAULT FALSE,
    spent_in_txid VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    UNIQUE KEY unique_utxo (txid, vout, address),
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_address (address),
    INDEX idx_is_spent (is_spent),
    INDEX idx_confirmations (confirmations)
);
```

**Columns:**
- `utxo_id` - Unique UTXO identifier
- `wallet_id` - FK to wallets table
- `txid` - Transaction hash containing this output
- `vout` - Output index in that transaction
- `address` - Owner's Bitcoin address
- `value_satoshis` - Amount of satoshis available
- `value_btc` - Amount in BTC
- `script_pubkey` - Locking script
- `confirmations` - Blockchain confirmations
- `is_spent` - FALSE = unspent (available), TRUE = spent
- `spent_in_txid` - Which transaction spent this UTXO
- `created_at` - When UTXO was recorded
- `updated_at` - Last update time

**Relationships:**
- Foreign Key: `wallet_id` → `wallets.wallet_id`

**Purpose:** Tracks all spendable coins. When a UTXO is spent, set `is_spent = TRUE`.

**Sample Data:**
```sql
INSERT INTO utxos (wallet_id, txid, vout, address, value_satoshis, value_btc, confirmations)
VALUES (1, 'abc123...', 0, 'mipcBbFg9...', 50000000, 0.5, 5);
```

---

### 7. **wallet_addresses** Table (Optional)
Stores derived addresses from a wallet (if using HD wallets).

```sql
CREATE TABLE wallet_addresses (
    address_id INT PRIMARY KEY AUTO_INCREMENT,
    wallet_id INT NOT NULL,
    bitcoin_address VARCHAR(255) UNIQUE NOT NULL,
    public_key VARCHAR(255),
    private_key_encrypted VARCHAR(255),
    derivation_path VARCHAR(100),
    address_index INT,
    balance_satoshis BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_bitcoin_address (bitcoin_address)
);
```

**Columns:**
- `address_id` - Unique address identifier
- `wallet_id` - FK to wallets table
- `bitcoin_address` - Derived Bitcoin address
- `public_key` - Public key for this address
- `private_key_encrypted` - Encrypted private key
- `derivation_path` - HD wallet derivation path (e.g., "m/44'/0'/0'/0/0")
- `address_index` - Index in derivation sequence
- `balance_satoshis` - Balance at this address
- `created_at` - Creation time

**Relationships:**
- Foreign Key: `wallet_id` → `wallets.wallet_id`

**Purpose:** If using HD (Hierarchical Deterministic) wallets, store derived addresses.

---

### 8. **transaction_fees_history** Table (Optional)
Stores fee history for analysis and optimization.

```sql
CREATE TABLE transaction_fees_history (
    fee_id INT PRIMARY KEY AUTO_INCREMENT,
    wallet_id INT NOT NULL,
    transaction_id INT,
    estimated_fee_satoshis BIGINT,
    actual_fee_satoshis BIGINT,
    fee_rate_sat_byte DECIMAL(10, 2),
    transaction_size INT,
    confirmations_time INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE SET NULL,
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_created_at (created_at)
);
```

**Purpose:** Track fee estimates vs actual fees for learning and optimization.

---

## Relationships Diagram

```
users (1)
  ├─── (1:M) ──→ wallets (M)
  │                 ├─── (1:M) ──→ transactions (M)
  │                 │                 ├─── (1:M) ──→ transaction_inputs (M)
  │                 │                 └─── (1:M) ──→ transaction_outputs (M)
  │                 │
  │                 ├─── (1:M) ──→ utxos (M)
  │                 │
  │                 └─── (1:M) ──→ wallet_addresses (M) [OPTIONAL]
```

---

## Key Constraints & Business Rules

### 1. User & Wallet Relationship
- Each user can have multiple wallets
- Each wallet belongs to exactly one user
- If user is deleted, all wallets are deleted (CASCADE)

### 2. Transactions
- Each transaction must belong to a wallet
- Transaction status: 'pending' → 'confirmed' or 'failed'
- Confirmations increase over time (from 0 to 6+)

### 3. UTXOs
- Represents spendable coins
- When spent, set `is_spent = TRUE` and `spent_in_txid`
- Only unspent UTXOs (`is_spent = FALSE`) can be selected for new transactions
- Sum of all unspent UTXOs = wallet balance

### 4. Security Rules
- **Private keys must always be encrypted** before storing
- Never store plaintext private keys in database
- Use encryption library: AES-256 encryption recommended
- Consider using a hardware security module (HSM) for production

### 5. Data Integrity
- Bitcoin addresses must be validated before insertion
- Transaction amounts must be positive
- Confirmations should never decrease

---

## SQL Setup Instructions

### 1. Create Database
```sql
CREATE DATABASE satoshivault;
USE satoshivault;
```

### 2. Run All Table Creation Scripts

See the complete SQL script in section below.

### 3. Add Initial Data (Optional)
```sql
-- Add a test user
INSERT INTO users (username, email, password_hash, full_name)
VALUES ('testuser', 'test@example.com', 'hashed_password', 'Test User');

-- Add a wallet for that user
INSERT INTO wallets (user_id, wallet_name, bitcoin_address, network)
VALUES (1, 'Test Wallet', 'mipcBbFg9gtjxeXJvWZnZwdAKwrG4FCDxj', 'testnet');
```

### 4. Verify Tables
```sql
SHOW TABLES;
DESCRIBE wallets;
DESCRIBE transactions;
```

---

## Complete SQL Script

```sql
-- Create Database
CREATE DATABASE IF NOT EXISTS satoshivault;
USE satoshivault;

-- 1. Users Table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Wallets Table
CREATE TABLE wallets (
    wallet_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    wallet_name VARCHAR(255) NOT NULL,
    bitcoin_address VARCHAR(255) UNIQUE NOT NULL,
    public_key VARCHAR(255),
    private_key_encrypted VARCHAR(255),
    network VARCHAR(20) DEFAULT 'testnet',
    balance_satoshis BIGINT DEFAULT 0,
    balance_btc DECIMAL(18, 8) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_synced TIMESTAMP NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    is_watch_only BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_address (user_id, bitcoin_address),
    INDEX idx_user_id (user_id),
    INDEX idx_bitcoin_address (bitcoin_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Transactions Table
CREATE TABLE transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    wallet_id INT NOT NULL,
    txid VARCHAR(255) UNIQUE NOT NULL,
    from_address VARCHAR(255) NOT NULL,
    to_address VARCHAR(255) NOT NULL,
    amount_satoshis BIGINT NOT NULL,
    amount_btc DECIMAL(18, 8) NOT NULL,
    fee_satoshis BIGINT NOT NULL,
    fee_btc DECIMAL(18, 8),
    status VARCHAR(50) DEFAULT 'pending',
    confirmations INT DEFAULT 0,
    timestamp BIGINT,
    block_number INT NULL,
    block_height INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    input_count INT,
    output_count INT,
    transaction_size INT,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_txid (txid),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_from_address (from_address),
    INDEX idx_to_address (to_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Transaction Inputs Table
CREATE TABLE transaction_inputs (
    input_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    previous_txid VARCHAR(255) NOT NULL,
    previous_vout INT NOT NULL,
    script_sig VARCHAR(1000),
    value_satoshis BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_previous_txid (previous_txid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Transaction Outputs Table
CREATE TABLE transaction_outputs (
    output_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    output_index INT NOT NULL,
    recipient_address VARCHAR(255) NOT NULL,
    value_satoshis BIGINT NOT NULL,
    value_btc DECIMAL(18, 8),
    script_pubkey VARCHAR(1000),
    is_change BOOLEAN DEFAULT FALSE,
    spent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_recipient_address (recipient_address),
    INDEX idx_output_index (output_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. UTXOs Table
CREATE TABLE utxos (
    utxo_id INT PRIMARY KEY AUTO_INCREMENT,
    wallet_id INT NOT NULL,
    txid VARCHAR(255) NOT NULL,
    vout INT NOT NULL,
    address VARCHAR(255) NOT NULL,
    value_satoshis BIGINT NOT NULL,
    value_btc DECIMAL(18, 8),
    script_pubkey VARCHAR(1000),
    confirmations INT DEFAULT 0,
    is_spent BOOLEAN DEFAULT FALSE,
    spent_in_txid VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    UNIQUE KEY unique_utxo (txid, vout, address),
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_address (address),
    INDEX idx_is_spent (is_spent),
    INDEX idx_confirmations (confirmations)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Wallet Addresses Table (Optional - for HD wallets)
CREATE TABLE wallet_addresses (
    address_id INT PRIMARY KEY AUTO_INCREMENT,
    wallet_id INT NOT NULL,
    bitcoin_address VARCHAR(255) UNIQUE NOT NULL,
    public_key VARCHAR(255),
    private_key_encrypted VARCHAR(255),
    derivation_path VARCHAR(100),
    address_index INT,
    balance_satoshis BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_bitcoin_address (bitcoin_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Transaction Fees History Table (Optional)
CREATE TABLE transaction_fees_history (
    fee_id INT PRIMARY KEY AUTO_INCREMENT,
    wallet_id INT NOT NULL,
    transaction_id INT,
    estimated_fee_satoshis BIGINT,
    actual_fee_satoshis BIGINT,
    fee_rate_sat_byte DECIMAL(10, 2),
    transaction_size INT,
    confirmations_time INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE SET NULL,
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Important Notes for Database Developer

### ⚠️ Security Considerations

1. **Private Keys Encryption**
   - Always encrypt private keys before storage
   - Use AES-256 encryption with a strong key
   - Never log or display private keys
   - Consider using environment variables for encryption keys

2. **Data Backups**
   - Regular backups are critical (contains financial data)
   - Encrypted backups recommended
   - Test restore procedures regularly

3. **Access Control**
   - Limit database access to application servers only
   - Use strong database user passwords
   - Log all access for audit trail
   - Consider read replicas for reports/analysis

4. **Data Validation**
   - Validate all Bitcoin addresses before storage
   - Validate transaction amounts (positive)
   - Implement constraints at database level

### 📊 Performance Optimization

1. **Indexes:**
   - All foreign keys have indexes for fast joins
   - Frequently queried columns are indexed
   - Consider composite indexes for complex queries

2. **Partitioning:**
   - For production: partition transactions table by date
   - Improves query speed for historical data

3. **Archiving:**
   - Old transactions can be archived to separate table
   - Keeps active table small and fast

### 🔄 Workflow Integration

**User Creates Wallet:**
```
User Creation → Wallet Creation → Generate Address → Store in DB
```

**User Sends Bitcoin:**
```
API /send endpoint → Select UTXOs → Create Transaction → Sign → Broadcast
                  → Record in transactions table
                  → Update UTXO status (is_spent = TRUE)
                  → Update wallet balance
```

**Sync Blockchain Data:**
```
API /transactions → Query BlockCypher → Update transaction_inputs
                 → Update transaction_outputs
                 → Update utxos table
                 → Recalculate wallet balance
```

---

## Testing Checklist

- [ ] Create database and run all table creation scripts
- [ ] Insert test user and wallet
- [ ] Insert test transaction with inputs/outputs
- [ ] Verify foreign keys work correctly
- [ ] Test cascade delete (delete user → wallets deleted)
- [ ] Test indexes for performance
- [ ] Verify unique constraints (no duplicate addresses)
- [ ] Test with Bitcoin testnet data
- [ ] Load test with 10,000+ transactions

---

## Questions for Database Developer

When implementing, ask:

1. What database system are we using? (MySQL, PostgreSQL, MongoDB?)
2. Should we use ORM (Hibernate/JPA) or raw SQL?
3. What's the data retention policy? (How long to keep old transactions?)
4. Do we need to handle multiple currencies in the database?
5. What's the expected transaction volume per day?
6. Do we need real-time syncing with blockchain?
7. Should wallet balances be cached or calculated on-demand?

---

## Support & Documentation

For questions about this schema:
- Review the Bitcoin transaction model in `TxInput`, `TxOutput` classes
- Check the API responses in `API_TEST_DEMO.md`
- See architecture in `BLOCKCHAIN_BACKEND_EXPLANATION.md`

Good luck! 🚀
