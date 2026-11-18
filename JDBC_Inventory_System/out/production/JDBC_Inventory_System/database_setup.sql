-- ====================================================================
-- MSE-2 Question 1: Inventory Management System Database Script
-- Database: retail_db
-- Table: Product
-- ====================================================================

CREATE DATABASE IF NOT EXISTS retail_db;
USE retail_db;

DROP TABLE IF EXISTS Product;

CREATE TABLE Product (
    product_id INT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL
);

-- Verification:
DESCRIBE Product;
