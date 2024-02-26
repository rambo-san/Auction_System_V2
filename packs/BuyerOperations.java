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

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM buyer WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Login successful.");
                    buyerMenu(scanner, rs.getInt("buyer_id"));
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
        System.out.println("Buyer Signup");
        System.out.print("Choose a username: ");
        String username = scanner.nextLine();
        System.out.print("Choose a password: ");
        String password = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO buyer (username, password) VALUES (?, ?)";
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

    public static void buyerMenu(Scanner scanner, int buyerId) {
        System.out.println("Welcome to the Buyer Dashboard!");
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("1. View available auctions");
            System.out.println("2. Place a bid");
            System.out.println("3. View my won auctions");
            System.out.println("4. Logout");

            System.out.print("Choose an option: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    viewAvailableAuctions(scanner);
                    break;
                case "2":
                    placeBid(scanner, buyerId);
                    break;
                case "3":
                    viewWonAuctions(scanner, buyerId);
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

    private static void viewAvailableAuctions(Scanner scanner) {
        System.out.println("Available auctions:");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT a.auction_id, p.name, a.start_time, a.end_time, a.starting_bid FROM auctions a JOIN products p ON a.product_id = p.product_id WHERE a.end_time > NOW()";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("Auction ID: " + rs.getInt("auction_id") + ", Product: " + rs.getString("name") + ", Start Time: " + rs.getTimestamp("start_time") + ", End Time: " + rs.getTimestamp("end_time") + ", Starting Bid: $" + rs.getBigDecimal("starting_bid"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database error.");
        }
    }

    private static void placeBid(Scanner scanner, int buyerId) {
        System.out.println("Place a bid on an auction:");
        System.out.print("Enter Auction ID: ");
        int auctionId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter your bid amount: ");
        double bidAmount = Double.parseDouble(scanner.nextLine());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO bids (auction_id, buyer_id, bid_amount, bid_time) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, auctionId);
                stmt.setInt(2, buyerId);
                stmt.setDouble(3, bidAmount);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Bid placed successfully.");
                } else {
                    System.out.println("Failed to place bid.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database error or auction not found.");
        }
    }

    private static void viewWonAuctions(Scanner scanner, int buyerId) {
        System.out.println("Your won auctions:");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT a.auction_id, p.name, b.bid_amount FROM auctions a JOIN bids b ON a.auction_id = b.auction_id JOIN products p ON a.product_id = p.product_id WHERE b.bid_amount = (SELECT MAX(bid_amount) FROM bids WHERE auction_id = a.auction_id) AND b.buyer_id = ? AND a.end_time < NOW()";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, buyerId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("Auction ID: " + rs.getInt("auction_id") + ", Product: " + rs.getString("name") + ", Winning Bid: $" + rs.getBigDecimal("bid_amount"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database error.");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Auction System");
        System.out.println("1. Buyer Login\n2. Buyer Sign Up");
        String choice = scanner.nextLine();

        if ("1".equals(choice)) {
            login(scanner);
        } else if ("2".equals(choice)) {
            signup(scanner);
        } else {
            System.out.println("Invalid choice");
        }
    }
}
