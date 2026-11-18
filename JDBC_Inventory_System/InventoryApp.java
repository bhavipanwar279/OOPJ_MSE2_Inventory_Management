import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * MSE-2 Sample Question 1: Inventory Management System (Single-File Version)
 * Course Outcome: CO4 (JDBC with MySQL)
 *
 * Database: retail_db
 * Table: Product (product_id INT PRIMARY KEY, product_name VARCHAR(100),
 *                 category VARCHAR(50), quantity INT, price DOUBLE)
 */
public class InventoryApp {

    // Database Credentials - update if necessary
    private static final String URL = "jdbc:mysql://localhost:3306/retail_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Example@2026";

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice = -1;

        System.out.println("==================================================================");
        System.out.println("             INVENTORY MANAGEMENT SYSTEM (JDBC + MySQL)           ");
        System.out.println("==================================================================");

        do {
            System.out.println("\n------------------------- MENU OPTIONS ---------------------------");
            System.out.println("1. (a) Insert Product Records (PreparedStatement & Duplicate Handling)");
            System.out.println("2. (b) Conditional Retrieval and Sorting (Qty < 20, Sorted by Price DESC)");
            System.out.println("3. (c) Update Product Stock (Add Quantity to Existing Stock)");
            System.out.println("4. (d) Search and Modify Product Details (Update Price if Price > 0)");
            System.out.println("5. (e) Aggregate Query and Conditional Deletion (SUM Value & Delete Qty=0)");
            System.out.println("6. Display All Products (Helper View)");
            System.out.println("0. Exit");
            System.out.println("------------------------------------------------------------------");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
                switch (choice) {
                    case 1:
                        insertRecords(sc);
                        break;
                    case 2:
                        conditionalRetrievalAndSorting(sc);
                        break;
                    case 3:
                        updateStock(sc);
                        break;
                    case 4:
                        searchAndModifyPrice(sc);
                        break;
                    case 5:
                        aggregateAndConditionalDelete(sc);
                        break;
                    case 6:
                        displayAll();
                        break;
                    case 0:
                        System.out.println("\nExiting application. Goodbye!");
                        break;
                    default:
                        System.out.println("[INVALID] Choice must be between 0 and 6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[INVALID] Please enter a valid numeric choice.");
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }

        } while (choice != 0);

        sc.close();
    }

    // -------------------------------------------------------------------------
    // (a) Insert Product Records — 2 Marks
    // Insert at least 3 records using PreparedStatement & handle duplicates
    // -------------------------------------------------------------------------
    private static void insertRecords(Scanner sc) {
        System.out.println("\n--- [a] INSERT PRODUCT RECORDS ---");
        System.out.println("1. Insert 3 Pre-defined Sample Products");
        System.out.println("2. Insert a Custom Product");
        System.out.print("Select (1 or 2): ");
        String subChoice = sc.nextLine().trim();

        String sql = "INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";

        if ("1".equals(subChoice)) {
            Object[][] sampleData = {
                {101, "Wireless Mouse", "Electronics", 15, 650.0},
                {102, "Mechanical Keyboard", "Electronics", 25, 2999.0},
                {103, "USB Cable", "Electronics", 8, 250.0},
                {104, "Ballpoint Pen Pack", "Stationery", 12, 120.0},
                {105, "Clearance Stock Adapter", "Electronics", 0, 399.0}
            };

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                for (Object[] row : sampleData) {
                    int id = (int) row[0];
                    String name = (String) row[1];
                    String cat = (String) row[2];
                    int qty = (int) row[3];
                    double pr = (double) row[4];

                    ps.setInt(1, id);
                    ps.setString(2, name);
                    ps.setString(3, cat);
                    ps.setInt(4, qty);
                    ps.setDouble(5, pr);

                    try {
                        ps.executeUpdate();
                        System.out.println("Inserted: Product ID=" + id + " (" + name + ")");
                    } catch (SQLIntegrityConstraintViolationException e) {
                        System.out.println("Duplicate ID " + id + " skipped (already exists).");
                    }
                }
                System.out.println("[SUCCESS] Sample records processed.");
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }

        } else if ("2".equals(subChoice)) {
            try {
                System.out.print("Enter Product ID: ");
                int id = Integer.parseInt(sc.nextLine().trim());
                System.out.print("Enter Product Name: ");
                String name = sc.nextLine().trim();
                System.out.print("Enter Category: ");
                String cat = sc.nextLine().trim();
                System.out.print("Enter Quantity: ");
                int qty = Integer.parseInt(sc.nextLine().trim());
                System.out.print("Enter Price: ");
                double pr = Double.parseDouble(sc.nextLine().trim());

                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, id);
                    ps.setString(2, name);
                    ps.setString(3, cat);
                    ps.setInt(4, qty);
                    ps.setDouble(5, pr);

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        System.out.println("[SUCCESS] Product record inserted successfully.");
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("[DUPLICATE ERROR] Product ID " + id + " already exists in table!");
                }
            } catch (Exception e) {
                System.out.println("[ERROR] Invalid input or DB error: " + e.getMessage());
            }
        } else {
            System.out.println("[ERROR] Invalid option.");
        }
    }

    // -------------------------------------------------------------------------
    // (b) Conditional Retrieval and Sorting — 2 Marks
    // Accept category, display products with quantity < 20, sorted by price DESC
    // -------------------------------------------------------------------------
    private static void conditionalRetrievalAndSorting(Scanner sc) {
        System.out.println("\n--- [b] CONDITIONAL RETRIEVAL AND SORTING ---");
        System.out.print("Enter Category name: ");
        String category = sc.nextLine().trim();

        String query = "SELECT * FROM Product WHERE category = ? AND quantity < 20 ORDER BY price DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {
                printHeader();
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    printRow(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getDouble(5));
                }
                printFooter();
                if (!found) {
                    System.out.println("[INFO] No records matched category '" + category + "' with quantity < 20.");
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // (c) Update Product Stock — 2 Marks
    // Accept product ID and quantity to add. Update stock or show message if not exists
    // -------------------------------------------------------------------------
    private static void updateStock(Scanner sc) {
        System.out.println("\n--- [c] UPDATE PRODUCT STOCK ---");
        try {
            System.out.print("Enter Product ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Enter Quantity to Add: ");
            int addQty = Integer.parseInt(sc.nextLine().trim());

            String sql = "UPDATE Product SET quantity = quantity + ? WHERE product_id = ?";

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, addQty);
                ps.setInt(2, id);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("[SUCCESS] Stock updated. Added " + addQty + " units to Product ID " + id);
                } else {
                    System.out.println("[NOT FOUND] Product ID " + id + " does not exist!");
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // (d) Search and Modify Product Details — 2 Marks
    // Retrieve product, update price only if exists and new price > 0, display updated details
    // -------------------------------------------------------------------------
    private static void searchAndModifyPrice(Scanner sc) {
        System.out.println("\n--- [d] SEARCH AND MODIFY PRODUCT DETAILS ---");
        try {
            System.out.print("Enter Product ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Enter New Price: ");
            double newPrice = Double.parseDouble(sc.nextLine().trim());

            if (newPrice <= 0) {
                System.out.println("[INVALID] New price must be greater than zero! Update cancelled.");
                return;
            }

            try (Connection con = getConnection()) {
                // 1. Search if product exists
                String searchSql = "SELECT * FROM Product WHERE product_id = ?";
                try (PreparedStatement psSearch = con.prepareStatement(searchSql)) {
                    psSearch.setInt(1, id);
                    try (ResultSet rs = psSearch.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("[NOT FOUND] Product ID " + id + " does not exist!");
                            return;
                        }
                        System.out.println("Existing details: ID=" + rs.getInt(1) + ", Name=" + rs.getString(2)
                                + ", Old Price=" + rs.getDouble(5));
                    }
                }

                // 2. Update price
                String updateSql = "UPDATE Product SET price = ? WHERE product_id = ?";
                try (PreparedStatement psUpdate = con.prepareStatement(updateSql)) {
                    psUpdate.setDouble(1, newPrice);
                    psUpdate.setInt(2, id);
                    psUpdate.executeUpdate();
                }

                System.out.println("[SUCCESS] Price updated successfully.");

                // 3. Display updated details
                try (PreparedStatement psFetch = con.prepareStatement(searchSql)) {
                    psFetch.setInt(1, id);
                    try (ResultSet rs = psFetch.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("\nUpdated Product Details:");
                            printHeader();
                            printRow(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getDouble(5));
                            printFooter();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // (e) Aggregate Query and Conditional Deletion — 2 Marks
    // Display total inventory value SUM(quantity * price).
    // Delete product only if its quantity is 0, else display appropriate message.
    // -------------------------------------------------------------------------
    private static void aggregateAndConditionalDelete(Scanner sc) {
        System.out.println("\n--- [e] AGGREGATE QUERY & CONDITIONAL DELETION ---");
        try (Connection con = getConnection()) {
            // Aggregate query
            String aggSql = "SELECT SUM(quantity * price) AS total_val FROM Product";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(aggSql)) {
                if (rs.next()) {
                    System.out.println("--------------------------------------------------");
                    System.out.printf("Total Inventory Value [SUM(quantity * price)]: %.2f\n", rs.getDouble("total_val"));
                    System.out.println("--------------------------------------------------");
                }
            }

            // Conditional deletion
            System.out.print("\nEnter Product ID to delete: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            String checkSql = "SELECT quantity, product_name FROM Product WHERE product_id = ?";
            try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                psCheck.setInt(1, id);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("[NOT FOUND] Product ID " + id + " does not exist!");
                        return;
                    }

                    int currentQty = rs.getInt("quantity");
                    String name = rs.getString("product_name");

                    if (currentQty > 0) {
                        System.out.println("[CANNOT DELETE] Product '" + name + "' (ID: " + id
                                + ") has quantity " + currentQty + ". Deletion allowed only when quantity is 0!");
                        return;
                    }

                    // Proceed to delete
                    String deleteSql = "DELETE FROM Product WHERE product_id = ?";
                    try (PreparedStatement psDelete = con.prepareStatement(deleteSql)) {
                        psDelete.setInt(1, id);
                        psDelete.executeUpdate();
                        System.out.println("[SUCCESS] Product '" + name + "' (ID: " + id + ") has quantity 0 and was deleted.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // Helper: Display all
    private static void displayAll() {
        System.out.println("\n--- ALL PRODUCTS ---");
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Product ORDER BY product_id")) {
            printHeader();
            boolean found = false;
            while (rs.next()) {
                found = true;
                printRow(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getDouble(5));
            }
            printFooter();
            if (!found) {
                System.out.println("[INFO] Table is currently empty.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void printHeader() {
        System.out.println("+------------+-------------------------+-----------------+----------+------------+");
        System.out.println("| Product ID | Product Name            | Category        | Quantity | Price (INR)|");
        System.out.println("+------------+-------------------------+-----------------+----------+------------+");
    }

    private static void printRow(int id, String name, String cat, int qty, double pr) {
        System.out.printf("| %-10d | %-23s | %-15s | %-8d | %-10.2f |\n", id, name, cat, qty, pr);
    }

    private static void printFooter() {
        System.out.println("+------------+-------------------------+-----------------+----------+------------+");
    }
}
