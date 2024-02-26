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
}
