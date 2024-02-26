package packs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class BuyerOperations {

    public static void login(Scanner scanner) {
        System.out.println("Buyer Login");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM buyer WHERE username = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    System.out.println("Login successful.");
                    // Additional operations upon login can be performed here.
                } else {
                    System.out.println("Login failed. Incorrect username or password.");
                }
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void signup(Scanner scanner) {
        System.out.println("Buyer Signup");
        System.out.print("Choose a username: ");
        String username = scanner.nextLine();
        System.out.print("Choose a password: ");
        String password = scanner.nextLine();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO buyer (username, password) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, password);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Signup successful. Please log in.");
                } else {
                    System.out.println("Signup failed. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
            if (e.getMessage().contains("duplicate key value")) {
                System.out.println("Username already exists. Please choose a different username.");
            }
        }
    }
}
