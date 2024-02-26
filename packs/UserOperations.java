package packs;

import java.sql.*;

public class UserOperations {

    public static boolean addNewUser(String username, String password, String role) {
        String query = "";
        switch (role.toLowerCase()) {
            case "admin":
                query = "INSERT INTO admin (username, password) VALUES (?, ?)";
                break;
            case "seller":
                query = "INSERT INTO seller (username, password) VALUES (?, ?)";
                break;
            case "buyer":
                query = "INSERT INTO buyer (username, password) VALUES (?, ?)";
                break;
            default:
                System.out.println("Invalid role specified.");
                return false;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("User added successfully.");
                return true;
            } else {
                System.out.println("User could not be added.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }
    public static void viewUsers() {
        System.out.println("Viewing all users:");
                        try (Connection connection = DatabaseConnection.getConnection()) {
                            // Query and print admins
                            String queryAdmins = "SELECT username FROM admin";
                            try (PreparedStatement stmt = connection.prepareStatement(queryAdmins)) {
                                ResultSet rs = stmt.executeQuery();
                                while (rs.next()) {
                                    String uname = rs.getString("username");
                                    System.out.println("Admin Username: " + uname);
                                }
                            }

                            // Query and print sellers
                            String querySellers = "SELECT username FROM seller";
                            try (PreparedStatement stmt = connection.prepareStatement(querySellers)) {
                                ResultSet rs = stmt.executeQuery();
                                while (rs.next()) {
                                    String adminName = rs.getString("username");
                                    System.out.println("Seller Username: " + adminName);
                                }
                            }
                            try (PreparedStatement stmt = connection.prepareStatement(querySellers)) {
                                ResultSet rs = stmt.executeQuery();
                                while (rs.next()) {
                                    String sellerName = rs.getString("username");
                                    System.out.println("Seller Username: " + sellerName);
                                }
                            }
                    
                            // Query and print buyers
                            String queryBuyers = "SELECT username FROM buyer";
                            try (PreparedStatement stmt = connection.prepareStatement(queryBuyers)) {
                                ResultSet rs = stmt.executeQuery();
                                while (rs.next()) {
                                    String buyerName = rs.getString("username");
                                    System.out.println("Buyer Username: " + buyerName);
                                }
                            }
                        } catch (SQLException e) {
                            System.out.println("Database error: " + e.getMessage());
                        }
    }
}
