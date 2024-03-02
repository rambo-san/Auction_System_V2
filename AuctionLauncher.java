import java.sql.*;
import packs.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuctionLauncher {
    private static final AdminOperations adminOperations = new AdminOperations(); // Assuming AdminOperations is in the packs package

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Auction System");
        try {
            System.out.print("Admin Username: ");
            String username = scanner.nextLine();
            System.out.print("Admin Password: ");
            String password = scanner.nextLine();

            if (verifyAdminCredentials(username, password)) {
                System.out.println("Admin authentication successful.");
                ExecutorService service = Executors.newSingleThreadExecutor();
                service.submit(() -> adminOperations.initializeServer()); 
                
                TimeUnit.SECONDS.sleep(2); // Wait for server to start
                boolean running = true;
                while (running) {
                    System.out.println("What would you like to do admin:");
                    System.out.println("1. Create user");
                    System.out.println("2. View users");
                    System.out.println("3. Start auction");
                    System.out.println("4. End auction");
                    System.out.println("5. Logout");
                    System.out.print("Enter choice: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    switch (choice) {
                        case 1:
                        System.out.print("Enter username: ");
                        String uname = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String pass = scanner.nextLine();
                        System.out.print("Enter role (admin, seller, buyer): ");
                        String role = scanner.nextLine();
                        UserOperations.addNewUser(uname, pass, role);
                            break;
                        case 2:
                        UserOperations.viewUsers();
                            break;
                            case 3:
                            adminOperations.startAuction();
                            break;
                        case 4:
                            adminOperations.endAuction();
                            break;
                        
                        case 5:
                            running = false;
                            System.out.println("Logging out...");
                            adminOperations.logout(); // Add a logout method in AdminOperations class to handle the logout logic
                            service.shutdownNow();
                            try {
                                if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                                    System.out.println("Forcing shutdown of remaining tasks...");
                                    service.shutdownNow();
                                    if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                                        System.err.println("Unable to shutdown executor service");
                                    }
                                }
                            } catch (InterruptedException e) {
                                System.err.println("Error occurred while shutting down executor service: " + e.getMessage());
                            }
                            System.out.println("Program closed.");
                            System.exit(0);
                        default:
                            System.out.println("Invalid choice.");
                    }
                }
            } else {
                System.out.println("Authentication failed. Exiting...");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static boolean verifyAdminCredentials(String username, String password) {
       try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT password FROM admin WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return storedPassword.equals(password);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return false;
    }
}
