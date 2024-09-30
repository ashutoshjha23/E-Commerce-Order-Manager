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
            Class.forName("com.mysql.cj.jdbc.Driver");
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
                System.out.println("\n1. Register Customer");
                System.out.println("2. Register Product");
                System.out.println("3. View All Products");
                System.out.println("4. Place Order");
                System.out.println("5. View Order History");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        registerCustomer(connection, scanner);
                        break;
                    case 2:
                        registerProduct(connection, scanner);
                        break;
                    case 3:
                        viewAllProducts(connection);
                        break;
                    case 4:
                        placeOrder(connection, scanner);
                        break;
                    case 5:
                        viewOrderHistory(connection, scanner);
                        break;
                    case 6:
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

    // Function to register a new customer with specified customer ID
    public static void registerCustomer(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter Customer ID (Leave blank for auto-generated): ");
        String customerIdInput = scanner.next();
        Integer customerId = null;

        // Check if input is provided for Customer ID
        if (!customerIdInput.isEmpty()) {
            customerId = Integer.parseInt(customerIdInput);
        }

        System.out.print("Enter Customer Name: ");
        String name = scanner.next();

        System.out.print("Enter Customer Email: ");
        String email = scanner.next();

        String insertCustomerSQL = "INSERT INTO customers (customer_id, name, email) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertCustomerSQL)) {
            if (customerId != null) {
                stmt.setInt(1, customerId);
            } else {
                stmt.setNull(1, Types.INTEGER); 
            }
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.executeUpdate();
            System.out.println("Customer registered successfully!");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Error: Customer ID already exists. Please try again with a different ID.");
        }
    }

    // Function to register a new product
    public static void registerProduct(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter Product Name: ");
        String name = scanner.next();

        System.out.print("Enter Product Price: ");
        double price = scanner.nextDouble();

        System.out.print("Enter Product Quantity: ");
        int quantity = scanner.nextInt();

        String insertProductSQL = "INSERT INTO products (name, price, quantity) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(insertProductSQL)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
            System.out.println("Product registered successfully!");
        }
    }

    // Function to view all products
    public static void viewAllProducts(Connection connection) throws SQLException {
        String query = "SELECT * FROM products";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             
            System.out.println("\nAll Products:");
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");

                System.out.println("Product ID: " + productId + ", Name: " + name + ", Price: " + price + ", Quantity: " + quantity);
            }
        }
    }

    // Function to place an order
    public static void placeOrder(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter Customer ID: ");
        int customerId = scanner.nextInt();
        
        if (!customerExists(connection, customerId)) {
            System.out.println("Invalid Customer ID. Please register the customer first.");
            return;
        }

        System.out.print("Enter Product ID: ");
        int productId = scanner.nextInt();

        if (!productExists(connection, productId)) {
            System.out.println("Invalid Product ID. Please register the product first.");
            return;
        }

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

    // Function to check if a customer exists
    public static boolean customerExists(Connection connection, int customerId) throws SQLException {
        String query = "SELECT COUNT(*) FROM customers WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Function to check if a product exists
    public static boolean productExists(Connection connection, int productId) throws SQLException {
        String query = "SELECT COUNT(*) FROM products WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
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
