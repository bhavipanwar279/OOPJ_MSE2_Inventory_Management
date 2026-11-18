package com.inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * MSE-2 Sample Question 1: Inventory Management System
 * Course Outcome: CO4 (JDBC with MySQL)
 *
 * Database: retail_db
 * Table: Product (product_id INT PRIMARY KEY, product_name VARCHAR(100),
 *                 category VARCHAR(50), quantity INT, price DOUBLE)
 */
public class InventoryManagementSystem {

    // Database Configuration (Change password/username according to your MySQL setup)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/retail_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Example@2026"; // Update if your MySQL password is different

    // Method to obtain a database connection
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // Register MySQL JDBC Driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("==================================================================");
        System.out.println("       RETAIL STORE - INVENTORY MANAGEMENT SYSTEM (JDBC)          ");
        System.out.println("==================================================================");

        // Test connection on startup
        try (Connection testConn = getConnection()) {
            System.out.println("[INFO] Connected successfully to database: retail_db\n");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL JDBC Driver not found! Make sure mysql-connector-j jar is added to classpath.");
            System.err.println("Details: " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.err.println("[WARNING] Could not connect to MySQL database.");
            System.err.println("Make sure MySQL service is running and 'retail_db' exists.");
            System.err.println("Database URL: " + DB_URL);
            System.err.println("Details: " + e.getMessage() + "\n");
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("------------------------- MAIN MENU -----------------------------");
            System.out.println("1. (a) Insert Product Records (Insert 3+ Products & Handle Duplicates)");
            System.out.println("2. (b) Conditional Retrieval and Sorting (Qty < 20, Sorted by Price DESC)");
            System.out.println("3. (c) Update Product Stock (Add Quantity to Existing Stock)");
            System.out.println("4. (d) Search and Modify Product Details (Update Price if Price > 0)");
            System.out.println("5. (e) Aggregate Query and Conditional Deletion (SUM Value & Delete Qty=0)");
            System.out.println("6. [*] Display All Products (Helper View)");
            System.out.println("7. [0] Exit Application");
            System.out.println("------------------------------------------------------------------");
            System.out.print("Enter your choice (1-7): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    handleInsertProducts(scanner);
                    break;
                case "2":
                    handleConditionalRetrievalAndSorting(scanner);
                    break;
                case "3":
                    handleUpdateProductStock(scanner);
                    break;
                case "4":
                    handleSearchAndModifyProductDetails(scanner);
                    break;
                case "5":
                    handleAggregateAndConditionalDeletion(scanner);
                    break;
                case "6":
                    displayAllProducts();
                    break;
                case "7":
                case "0":
                    exit = true;
                    System.out.println("\nExiting Inventory Management System. Thank you!");
                    break;
                default:
                    System.out.println("[INVALID] Please enter a valid choice between 1 and 7.");
            }
            System.out.println();
        }

        scanner.close();
    }

    // =========================================================================
    // (a) Insert Product Records — 2 Marks
    // Establish a connection with the MySQL database and insert at least three
    // product records using PreparedStatement. Handle duplicate product IDs appropriately.
    // =========================================================================
    private static void handleInsertProducts(Scanner scanner) {
        System.out.println("\n--- [a] INSERT PRODUCT RECORDS ---");
        System.out.println("1. Auto-insert 3 sample product records using PreparedStatement");
        System.out.println("2. Manually enter a new product record");
        System.out.print("Choose option (1 or 2): ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            insertSampleRecords();
        } else if (choice.equals("2")) {
            insertSingleProduct(scanner);
        } else {
            System.out.println("[ERROR] Invalid selection.");
        }
    }

    /**
     * Inserts at least 3 pre-defined product records using PreparedStatement,
     * handling duplicate product IDs gracefully.
     */
    private static void insertSampleRecords() {
        String insertSql = "INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";

        // 3 Sample products to insert
        Object[][] sampleProducts = {
            {101, "Wireless Optical Mouse", "Electronics", 15, 650.00},
            {102, "Mechanical Keyboard RGB", "Electronics", 25, 2999.00},
            {103, "High-Speed USB-C Hub", "Electronics", 8, 1250.00},
            {104, "A4 Spiral Notebook", "Stationery", 10, 95.00},
            {105, "Clearance Item (Zero Stock)", "Miscellaneous", 0, 199.00}
        };

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            int insertedCount = 0;
            for (Object[] prod : sampleProducts) {
                int id = (int) prod[0];
                String name = (String) prod[1];
                String category = (String) prod[2];
                int qty = (int) prod[3];
                double price = (double) prod[4];

                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, category);
                pstmt.setInt(4, qty);
                pstmt.setDouble(5, price);

                try {
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        System.out.println(" -> Successfully inserted: ID=" + id + ", Name='" + name + "'");
                        insertedCount++;
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println(" -> [DUPLICATE HANDLED] Product ID " + id + " already exists. Skipped.");
                }
            }

            System.out.println("[SUMMARY] Batch insertion finished. " + insertedCount + " new record(s) inserted.");

        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] JDBC Driver missing: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ERROR] Database operation failed: " + e.getMessage());
        }
    }

    /**
     * Accepts a single product record from user input and inserts using PreparedStatement.
     */
    private static void insertSingleProduct(Scanner scanner) {
        try {
            System.out.print("Enter Product ID (int): ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Product Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Category: ");
            String category = scanner.nextLine().trim();

            System.out.print("Enter Quantity (int): ");
            int qty = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Price (double): ");
            double price = Double.parseDouble(scanner.nextLine().trim());

            String sql = "INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, category);
                pstmt.setInt(4, qty);
                pstmt.setDouble(5, price);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("[SUCCESS] Product record inserted successfully!");
                }

            } catch (SQLIntegrityConstraintViolationException e) {
                System.err.println("[ERROR - DUPLICATE ID] Product ID " + id + " already exists in the database! Duplicate IDs are not allowed.");
            }

        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Invalid numeric input! Please enter valid integers/decimals.");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] JDBC Driver missing: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ERROR] Database operation failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // (b) Conditional Retrieval and Sorting — 2 Marks
    // Accept a category from the user and display all products belonging to that
    // category whose quantity is less than 20. Sort the results by price in descending order.
    // =========================================================================
    private static void handleConditionalRetrievalAndSorting(Scanner scanner) {
        System.out.println("\n--- [b] CONDITIONAL RETRIEVAL AND SORTING ---");
        System.out.print("Enter Category to search (e.g., Electronics, Stationery): ");
        String category = scanner.nextLine().trim();

        String query = "SELECT product_id, product_name, category, quantity, price " +
                       "FROM Product " +
                       "WHERE category = ? AND quantity < 20 " +
                       "ORDER BY price DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, category);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\nResults for Category: '" + category + "' with Quantity < 20 (Sorted by Price DESC):");
                printProductTableHeader();

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    int id = rs.getInt("product_id");
                    String name = rs.getString("product_name");
                    String cat = rs.getString("category");
                    int qty = rs.getInt("quantity");
                    double price = rs.getDouble("price");

                    System.out.printf("| %-10d | %-28s | %-15s | %-8d | %-10.2f |\n", id, name, cat, qty, price);
                }
                printProductTableFooter();

                if (!found) {
                    System.out.println("[INFO] No products found in category '" + category + "' with quantity < 20.");
                }
            }

        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] JDBC Driver missing: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ERROR] Query execution failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // (c) Update Product Stock — 2 Marks
    // Accept a product ID and a quantity to be added to the existing stock.
    // Update the quantity of the specified product and display an appropriate
    // message if the product does not exist.
    // =========================================================================
    private static void handleUpdateProductStock(Scanner scanner) {
        System.out.println("\n--- [c] UPDATE PRODUCT STOCK ---");
        try {
            System.out.print("Enter Product ID: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Quantity to add to existing stock: ");
            int qtyToAdd = Integer.parseInt(scanner.nextLine().trim());

            if (qtyToAdd < 0) {
                System.out.println("[WARNING] You entered a negative quantity. This will deduct from existing stock.");
            }

            // Update quantity query
            String updateSql = "UPDATE Product SET quantity = quantity + ? WHERE product_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                pstmt.setInt(1, qtyToAdd);
                pstmt.setInt(2, productId);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("[SUCCESS] Stock updated successfully!");
                    // Display updated stock
                    displaySingleProduct(conn, productId);
                } else {
                    System.out.println("[ERROR] Product with ID " + productId + " does not exist!");
                }

            }

        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Invalid numeric input! Please enter a valid number.");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] JDBC Driver missing: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ERROR] Update failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // (d) Search and Modify Product Details — 2 Marks
    // Accept a product ID and a new price from the user. Retrieve the product
    // details and update its price only if the product exists and the new price
    // is greater than zero. Display the updated product details.
    // =========================================================================
    private static void handleSearchAndModifyProductDetails(Scanner scanner) {
        System.out.println("\n--- [d] SEARCH AND MODIFY PRODUCT DETAILS ---");
        try {
            System.out.print("Enter Product ID: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter New Price: ");
            double newPrice = Double.parseDouble(scanner.nextLine().trim());

            // Validation: new price must be greater than zero
            if (newPrice <= 0) {
                System.out.println("[ERROR] Price must be greater than zero! Update rejected.");
                return;
            }

            try (Connection conn = getConnection()) {
                // Step 1: Check if product exists and retrieve current details
                String searchSql = "SELECT * FROM Product WHERE product_id = ?";
                try (PreparedStatement searchStmt = conn.prepareStatement(searchSql)) {
                    searchStmt.setInt(1, productId);
                    try (ResultSet rs = searchStmt.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("[ERROR] Product with ID " + productId + " does not exist!");
                            return;
                        }

                        System.out.println("\n[CURRENT DETAILS FOUND]");
                        System.out.println("ID       : " + rs.getInt("product_id"));
                        System.out.println("Name     : " + rs.getString("product_name"));
                        System.out.println("Category : " + rs.getString("category"));
                        System.out.println("Quantity : " + rs.getInt("quantity"));
                        System.out.println("Old Price: " + rs.getDouble("price"));
                    }
                }

                // Step 2: Update price
                String updateSql = "UPDATE Product SET price = ? WHERE product_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, newPrice);
                    updateStmt.setInt(2, productId);
                    updateStmt.executeUpdate();
                }

                System.out.println("\n[SUCCESS] Price updated successfully!");

                // Step 3: Retrieve and display updated details
                System.out.println("[UPDATED DETAILS]");
                displaySingleProduct(conn, productId);

            }

        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Invalid numeric input! Please enter a valid number.");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] JDBC Driver missing: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ERROR] Database operation failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // (e) Aggregate Query and Conditional Deletion — 2 Marks
    // Display the total inventory value using SUM(quantity * price).
    // Then accept a product ID and delete the corresponding record only if its
    // quantity is zero. Display an appropriate message if the product cannot be deleted.
    // =========================================================================
    private static void handleAggregateAndConditionalDeletion(Scanner scanner) {
        System.out.println("\n--- [e] AGGREGATE QUERY & CONDITIONAL DELETION ---");

        try (Connection conn = getConnection()) {
            // Part 1: Display total inventory value using SUM(quantity * price)
            String sumQuery = "SELECT SUM(quantity * price) AS total_inventory_value FROM Product";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sumQuery)) {

                if (rs.next()) {
                    double totalValue = rs.getDouble("total_inventory_value");
                    System.out.println("==================================================================");
                    System.out.printf(" TOTAL INVENTORY VALUE [SUM(quantity * price)]: %.2f\n", totalValue);
                    System.out.println("==================================================================");
                }
            }

            // Part 2: Accept product ID and delete only if quantity == 0
            System.out.print("\nEnter Product ID to delete: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            // Check if product exists and inspect quantity
            String checkSql = "SELECT product_name, quantity FROM Product WHERE product_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, productId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("[ERROR] Product with ID " + productId + " does not exist!");
                        return;
                    }

                    String name = rs.getString("product_name");
                    int quantity = rs.getInt("quantity");

                    if (quantity > 0) {
                        System.out.println("[CANNOT DELETE] Product '" + name + "' (ID: " + productId + ") has quantity = "
                                + quantity + ". Deletion is ONLY allowed when quantity is 0!");
                        return;
                    }

                    // Quantity is 0 -> Proceed with deletion
                    String deleteSql = "DELETE FROM Product WHERE product_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, productId);
                        int deleted = deleteStmt.executeUpdate();
                        if (deleted > 0) {
                            System.out.println("[SUCCESS] Product '" + name + "' (ID: " + productId + ") has quantity = 0 and was deleted successfully!");
                        }
                    }
                }
            }

        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Invalid numeric input! Please enter a valid Product ID.");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] JDBC Driver missing: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ERROR] Operation failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Helper to display all products in tabular format.
     */
    private static void displayAllProducts() {
        System.out.println("\n--- CURRENT INVENTORY TABLE (Product) ---");
        String sql = "SELECT product_id, product_name, category, quantity, price FROM Product ORDER BY product_id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            printProductTableHeader();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int id = rs.getInt("product_id");
                String name = rs.getString("product_name");
                String cat = rs.getString("category");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");

                System.out.printf("| %-10d | %-28s | %-15s | %-8d | %-10.2f |\n", id, name, cat, qty, price);
            }
            printProductTableFooter();

            if (!hasData) {
                System.out.println("[INFO] The Product table is currently empty.");
            }

        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] JDBC Driver missing: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ERROR] Could not fetch products: " + e.getMessage());
        }
    }

    /**
     * Helper to print details of a single product.
     */
    private static void displaySingleProduct(Connection conn, int productId) throws SQLException {
        String sql = "SELECT * FROM Product WHERE product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    printProductTableHeader();
                    System.out.printf("| %-10d | %-28s | %-15s | %-8d | %-10.2f |\n",
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("category"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"));
                    printProductTableFooter();
                }
            }
        }
    }

    private static void printProductTableHeader() {
        System.out.println("+------------+------------------------------+-----------------+----------+------------+");
        System.out.println("| Product ID | Product Name                 | Category        | Quantity | Price (INR)|");
        System.out.println("+------------+------------------------------+-----------------+----------+------------+");
    }

    private static void printProductTableFooter() {
        System.out.println("+------------+------------------------------+-----------------+----------+------------+");
    }
}
