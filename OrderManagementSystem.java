import java.sql.*;
import java.util.Scanner;

public class OrderManagementSystem {

    // JDBC URL, username, and password for the database connection
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String USER = "ecommerce";
    private static final String PASS = "ashutosh";

    public static void main(String[] args) {
        // Register MySQL JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Ensure JDBC driver is loaded
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        // Establish connection with the database
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the MySQL database!");

            while (true) {
                System.out.println("\n1. Place Order");
                System.out.println("2. View Order History");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        placeOrder(connection, scanner);
                        break;
                    case 2:
                        viewOrderHistory(connection, scanner);
                        break;
                    case 3:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to place an order
    public static void placeOrder(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter Customer ID: ");
        int customerId = scanner.nextInt();

        System.out.print("Enter Product ID: ");
        int productId = scanner.nextInt();

        System.out.print("Enter Quantity: ");
        int quantity = scanner.nextInt();

        if (checkProductAvailability(connection, productId, quantity)) {
            String orderSQL = "INSERT INTO orders (customer_id, product_id, quantity) VALUES (?, ?, ?)";
            String updateProductSQL = "UPDATE products SET quantity = quantity - ? WHERE product_id = ?";

            try (PreparedStatement orderStmt = connection.prepareStatement(orderSQL);
                 PreparedStatement updateProductStmt = connection.prepareStatement(updateProductSQL)) {

                // Insert order
                orderStmt.setInt(1, customerId);
                orderStmt.setInt(2, productId);
                orderStmt.setInt(3, quantity);
                orderStmt.executeUpdate();

                // Update product inventory
                updateProductStmt.setInt(1, quantity);
                updateProductStmt.setInt(2, productId);
                updateProductStmt.executeUpdate();

                System.out.println("Order placed successfully!");
            }
        } else {
            System.out.println("Insufficient stock for the selected product.");
        }
    }

    // Function to check product availability
    public static boolean checkProductAvailability(Connection connection, int productId, int quantity) throws SQLException {
        String query = "SELECT quantity FROM products WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int availableQuantity = rs.getInt("quantity");
                return availableQuantity >= quantity;
            }
        }
        return false;
    }

    // Function to view order history for a customer
    public static void viewOrderHistory(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter Customer ID: ");
        int customerId = scanner.nextInt();

        String query = "SELECT o.order_id, p.name, o.quantity, o.order_date FROM orders o "
                + "JOIN products p ON o.product_id = p.product_id "
                + "WHERE o.customer_id = ? ORDER BY o.order_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nOrder History:");
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String productName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                Timestamp orderDate = rs.getTimestamp("order_date");

                System.out.println("Order ID: " + orderId + ", Product: " + productName + ", Quantity: " + quantity + ", Date: " + orderDate);
            }
        }
    }
}
