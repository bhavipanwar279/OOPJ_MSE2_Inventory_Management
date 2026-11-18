# MSE-2 Question 1: Inventory Management System (JDBC with MySQL)

## 📌 Problem Overview
- **Scenario:** A retail store inventory management application.
- **Database:** `retail_db`
- **Table:** `Product`
  - `product_id` : `INT PRIMARY KEY`
  - `product_name` : `VARCHAR(100)`
  - `category` : `VARCHAR(50)`
  - `quantity` : `INT`
  - `price` : `DOUBLE`
- **Total Marks:** 10 Marks (CO4)

---

## 📁 Project Structure

```text
JDBC_Inventory_System/
│
├── database_setup.sql                 # MySQL schema creation script
├── InventoryApp.java                  # Standalone single-file Java program (Easy to compile)
├── README.md                          # Complete documentation and execution guide
└── src/
    └── com/
        └── inventory/
            ├── Product.java                   # Product entity model
            └── InventoryManagementSystem.java # Full packaged menu-driven application
```

---

## 🗄️ 1. Database Setup in MySQL

Open MySQL Workbench or MySQL Command Line Client and execute:

```sql
CREATE DATABASE IF NOT EXISTS retail_db;
USE retail_db;

CREATE TABLE IF NOT EXISTS Product (
    product_id INT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL
);
```
*(Or simply run the included [database_setup.sql](database_setup.sql) script).*

---

## ⚙️ 2. Database Connection Configuration

In `InventoryApp.java` (and `InventoryManagementSystem.java`), verify or change the MySQL credentials:

```java
private static final String URL = "jdbc:mysql://localhost:3306/retail_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "root"; // <-- Change to your MySQL password
```

---

## 🚀 3. How to Compile and Run

### Requirement:
- Java Development Kit (JDK 8 or higher)
- MySQL Connector/J (`mysql-connector-j-8.x.x.jar` or `mysql-connector-java-8.x.x.jar`)

### Option A: Running the Standalone Single File (`InventoryApp.java`)

1. Place your `mysql-connector-j.jar` in the project folder or reference its path.
2. Compile:
   ```bash
   javac -cp .;mysql-connector-j-8.0.33.jar InventoryApp.java
   ```
3. Run:
   ```bash
   java -cp .;mysql-connector-j-8.0.33.jar InventoryApp
   ```

### Option B: Running the Packaged Version (`InventoryManagementSystem.java`)

1. Compile:
   ```bash
   javac -cp .;mysql-connector-j-8.0.33.jar src/com/inventory/*.java
   ```
2. Run:
   ```bash
   java -cp .;src;mysql-connector-j-8.0.33.jar com.inventory.InventoryManagementSystem
   ```

*(Note for Linux/macOS: replace semicolon `;` with colon `:` in classpath).*

---

## 📝 4. Solution Breakdown for Each Question

### (a) Insert Product Records (2 Marks)
- **Requirement:** Insert at least three product records using `PreparedStatement`. Handle duplicate product IDs appropriately.
- **Implementation:**
  - SQL: `INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)`
  - Parameterized via `PreparedStatement.setInt()`, `setString()`, `setDouble()`.
  - Duplicate IDs are intercepted and handled cleanly using `catch (SQLIntegrityConstraintViolationException e)` without crashing the program.

### (b) Conditional Retrieval and Sorting (2 Marks)
- **Requirement:** Accept a category from the user and display all products belonging to that category whose quantity is less than 20. Sort results by price in descending order.
- **Implementation:**
  - SQL: `SELECT * FROM Product WHERE category = ? AND quantity < 20 ORDER BY price DESC`
  - Parameter `category` is bound via `pstmt.setString(1, category)`.
  - Results are displayed in a clean tabular format.

### (c) Update Product Stock (2 Marks)
- **Requirement:** Accept a product ID and a quantity to be added to the existing stock. Update the quantity of the specified product and display an appropriate message if the product does not exist.
- **Implementation:**
  - SQL: `UPDATE Product SET quantity = quantity + ? WHERE product_id = ?`
  - Uses `pstmt.executeUpdate()` which returns the count of affected rows.
  - If `rowsAffected == 0`, displays: `[ERROR] Product with ID <id> does not exist!`.
  - If updated, displays the updated quantity and product details.

### (d) Search and Modify Product Details (2 Marks)
- **Requirement:** Accept a product ID and a new price from the user. Retrieve the product details and update its price only if the product exists and the new price is greater than zero. Display the updated product details.
- **Implementation:**
  - Validates `newPrice > 0`. If `newPrice <= 0`, displays an error message and skips updating.
  - Searches database for `product_id`. If not found, notifies the user.
  - Executes `UPDATE Product SET price = ? WHERE product_id = ?`.
  - Retrieves and displays the updated product row.

### (e) Aggregate Query and Conditional Deletion (2 Marks)
- **Requirement:** Display the total inventory value using `SUM(quantity * price)`. Then accept a product ID and delete the corresponding record only if its quantity is zero. Display an appropriate message if the product cannot be deleted.
- **Implementation:**
  - Aggregate SQL: `SELECT SUM(quantity * price) AS total_val FROM Product`
  - Fetches existing `quantity` for the specified `product_id`.
  - If `quantity > 0`: blocks deletion with message `Cannot delete: Quantity is > 0`.
  - If `quantity == 0`: executes `DELETE FROM Product WHERE product_id = ?` and confirms deletion.

---

## 🖥️ 5. Sample Menu Interface

```text
==================================================================
             INVENTORY MANAGEMENT SYSTEM (JDBC + MySQL)           
==================================================================

------------------------- MENU OPTIONS ---------------------------
1. (a) Insert Product Records (PreparedStatement & Duplicate Handling)
2. (b) Conditional Retrieval and Sorting (Qty < 20, Sorted by Price DESC)
3. (c) Update Product Stock (Add Quantity to Existing Stock)
4. (d) Search and Modify Product Details (Update Price if Price > 0)
5. (e) Aggregate Query and Conditional Deletion (SUM Value & Delete Qty=0)
6. Display All Products (Helper View)
0. Exit
------------------------------------------------------------------
Enter your choice: 
```
