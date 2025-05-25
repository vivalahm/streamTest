-- DDL for creating tables

-- Create Processor table
CREATE TABLE IF NOT EXISTS Processor (
    processor_id   VARCHAR(20) PRIMARY KEY,
    processor_name VARCHAR(100) NOT NULL,
    partner_code   VARCHAR(30)  NOT NULL
);

-- Create Scheme table
CREATE TABLE IF NOT EXISTS Scheme (
    scheme_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheme_code   VARCHAR(30) UNIQUE NOT NULL,
    scheme_name   VARCHAR(50) NOT NULL
);

-- Create Payment table
CREATE TABLE IF NOT EXISTS Payment (
    payment_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    processor_id   VARCHAR(20)  NOT NULL,
    partner_code   VARCHAR(30)  NOT NULL,
    payment_type   VARCHAR(30)  NOT NULL,
    scheme_code    VARCHAR(30)  NOT NULL,
    FOREIGN KEY (processor_id) REFERENCES Processor(processor_id),
    FOREIGN KEY (scheme_code) REFERENCES Scheme(scheme_code)
);