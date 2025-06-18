-- 创建客户表
CREATE TABLE Customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    address TEXT,
    del_flag TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建账户表
CREATE TABLE Accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    account_type ENUM('SAVINGS', 'CHECKING') NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'CNY',
    balance DECIMAL(15, 2) DEFAULT 0.00 CHECK (balance >= 0),
    del_flag TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_id (customer_id)
);

-- 创建转出交易表
CREATE TABLE OutgoingTransactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    customer_id INT NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL CHECK (amount >= 0),
    to_account_id INT,
    del_flag TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account_id (account_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_to_account_id (to_account_id)
);

-- 创建转入交易表
CREATE TABLE IncomingTransactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    customer_id INT NOT NULL,
    transaction_type ENUM('TRANSFER') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL CHECK (amount >= 0),
    from_account_id INT,
    del_flag TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account_id (account_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_from_account_id (from_account_id)
);

-- 创建交易日志表
CREATE TABLE TransactionLogs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REVERSED') NOT NULL,
    message TEXT,
    del_flag TINYINT NOT NULL DEFAULT 0,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transaction_id (transaction_id)
); 