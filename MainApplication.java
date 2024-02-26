import packs.*;
import java.util.Scanner;

public class MainApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to the Auction System");
            System.out.println("Are you a seller or a buyer?");
            System.out.println("1. Seller");
            System.out.println("2. Buyer");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int roleChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (roleChoice) {
                case 1:
                    handleSeller(scanner);
                    break;
                case 2:
                    handleBuyer(scanner);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                    break;
            }
        }

        scanner.close();
    }

    private static void handleSeller(Scanner scanner) {
        System.out.println("Seller Operations");
        System.out.println("1. Login");
        System.out.println("2. Signup");
        System.out.println("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                // Perform login
                SellerOperations.login(scanner);
                break;
            case 2:
                // Perform signup
                SellerOperations.signup(scanner);
                break;
            default:
                System.out.println("Invalid choice. Please enter 1 or 2.");
                break;
        }
    }

    private static void handleBuyer(Scanner scanner) {
        System.out.println("Buyer Operations");
        System.out.println("1. Login");
        System.out.println("2. Signup");
        System.out.println("Enter your choice: ");
    
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline
    
        switch (choice) {
            case 1:
                // Perform login
                BuyerOperations.login(scanner);
                break;
            case 2:
                // Perform signup
                BuyerOperations.signup(scanner);
                break;
            default:
                System.out.println("Invalid choice. Please enter 1 or 2.");
                break;
        }
    }
}   
