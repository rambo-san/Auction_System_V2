package packs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class SellerOperations {
    public static void login(Scanner scanner) {
        System.out.println("Seller Login");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM seller WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Login successful.");
                    sellerMenu(scanner, username);
                } else {
                    System.out.println("Login failed. Incorrect username or password.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database error.");
        }
    }

    public static void signup(Scanner scanner) {
        System.out.println("Seller Signup");
        System.out.print("Choose a username: ");
        String username = scanner.nextLine();
        System.out.print("Choose a password: ");
        String password = scanner.nextLine();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO seller (username, password) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Signup successful. Please log in.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database error or username already exists.");
        }
    }
    public static void sellerMenu(Scanner scanner, String username) {
        System.out.println("Welcome to the Seller Dashboard, " + username + "!");
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("1. List a product for auction");
            System.out.println("2. View my listed products");
            System.out.println("3. View my sold items"); // Added option for viewing sold items
            System.out.println("4. Logout");

            System.out.print("Choose an option: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    listProductForAuction(scanner, username);
                    break;
                case "2":
                    viewListedProducts(scanner, username);
                    break;
                case "3":
                    viewSoldItems(scanner, username); // Implement this method
                    break;
                case "4":
                    loggedIn = false;
                    System.out.println("You have been logged out.");
                    break;
                default:
                    System.out.println("Invalid option, please choose again.");
            }
        }
    }
    private static void listProductForAuction(Scanner scanner, String username) {
        System.out.println("Listing a product for auction...");
        System.out.print("Product name: ");
        String productName = scanner.nextLine();
        System.out.print("Product description: ");
        String productDescription = scanner.nextLine();

        // Assuming 'seller_id' can be retrieved based on the 'username'
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First, insert the product
            String queryProduct = "INSERT INTO item (name, description, seller_id) VALUES (?, ?, (SELECT seller_id FROM seller WHERE username = ?))";
            try (PreparedStatement stmtProduct = conn.prepareStatement(queryProduct, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmtProduct.setString(1, productName);
                stmtProduct.setString(2, productDescription);
                stmtProduct.setString(3, username);
                int rowsAffectedProduct = stmtProduct.executeUpdate();

                if (rowsAffectedProduct > 0) {
                    System.out.println("Product listed successfully.");
                    // You could also initiate an auction here if needed
                } else {
                    System.out.println("Failed to list product.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database error.");
        }
    }

    private static void viewListedProducts(Scanner scanner, String username) {
        System.out.println("Your listed products:");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT p.item_id, p.name, p.description FROM item p INNER JOIN seller s ON p.seller_id = s.seller_id WHERE s.username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("Product ID: " + rs.getInt("item_id") + ", Name: " + rs.getString("name") + ", Description: " + rs.getString("description"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database error.");
        }
    }
    private static void viewSoldItems(Scanner scanner, String username) {
        System.out.println("Your sold products:");
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Adjust the query to select only items with status = true (1 for sold, assuming boolean is stored as 0/1)
            String query = "SELECT p.item_id, p.name, p.description FROM item p INNER JOIN seller s ON p.seller_id = s.seller_id WHERE s.username = ? AND p.status = 1";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                boolean hasProducts = false;
                while (rs.next()) {
                    hasProducts = true;
                    System.out.println("Product ID: " + rs.getInt("item_id") + ", Name: " + rs.getString("name") + ", Description: " + rs.getString("description"));
                }
                if (!hasProducts) {
                    System.out.println("No sold products found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database error.");
        }
    }
}
