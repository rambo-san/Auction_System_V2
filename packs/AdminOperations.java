package packs;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AdminOperations {

    private Timer auctionTimer;
    private boolean auctionActive = false;
    private ExecutorService clientHandlerExecutor;
    private ConcurrentHashMap<Integer, BidInfo> bids = new ConcurrentHashMap<>();
    private ServerSocket serverSocket = null;  // Initialize to null
    private boolean running = true;
    private CountDownLatch auctionLatch;
    private volatile int currentBidId = -1; // Default to -1 indicating no active auction

    public static void main(String[] args) {
        AdminOperations server = new AdminOperations();
        server.initializeServer();
    }

    public void initializeServer() {
        System.out.println("Server initializing...");
        try {
            // Start server if not already running
            if (serverSocket == null) {
                clientHandlerExecutor = Executors.newCachedThreadPool();
                serverSocket = new ServerSocket(12345); // Use your desired port
                startServer();
            }
        } catch (IOException e) {
            System.out.println("Could not listen on the specified port: " + e.getMessage());
        }
    }

    private void startServer() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                // Handle client connection in a separate thread
                System.out.println("Client connected.");
                ClientHandler clientHandler = new ClientHandler(clientSocket, bids, this);
                clientHandlerExecutor.execute(clientHandler);  // Submit to thread pool
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    public void startAuction() {
        int item_id = -1;
        Scanner scanner = new Scanner(System.in);  // Ensure scanner is declared
        System.out.println("Available products:");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT item_id, name, description, seller_id FROM item WHERE status = 0";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("Item ID: " + rs.getInt("item_id") + ", Name: " + rs.getString("name") + ", Description: " + rs.getString("description") + ", Seller ID: " + rs.getInt("seller_id"));
                }
                System.out.println("Enter the item ID to start the auction for:");
                item_id = scanner.nextInt();
                query="select * from item where item_id="+item_id;
                try (PreparedStatement st = conn.prepareStatement(query)) {
                    ResultSet rst = st.executeQuery();

                    if (!rst.next() || rst.getInt("status") == 1) {
                        System.out.println("Invalid input/Auction ID. Please enter a number.");
                        return;
                    }
            }
                scanner.close();
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }

        if (!auctionActive) {
            auctionActive = true;
            currentBidId = generateBidId();
            auctionLatch = new CountDownLatch(1);
            System.out.println("Auction has started. Accepting bids for 1 minute. Bid ID: " + currentBidId);
            bids.put(currentBidId, new BidInfo(-1, 0.0));
            startAuctionTimer();

            // Start server if not already running (redundant check for clarity)
            if (serverSocket == null) {
                initializeServer();
            }
        } else {
            System.out.println("Auction is already in progress.");
        }
    }

    private int generateBidId() {
        return Math.abs(UUID.randomUUID().hashCode());
    }

    private void startAuctionTimer() {
        auctionTimer = new Timer();
        auctionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                endAuction();
            }
        },15000); // Auction duration is 1 minute (60 seconds)
    }
    public void endAuction() {
        auctionActive = false;
        auctionTimer.cancel();
        currentBidId = -1; // Reset the currentBidId indicating no active auction
        System.out.println("Auction ended.");

        // Find the winning bid
        Optional<BidInfo> winningBid = bids.values().stream()
                .max(Comparator.comparingDouble(BidInfo::getBidAmount));

        if (winningBid.get().getBuyerId()!=-1) {
            System.out.println("Winner is Buyer ID " + winningBid.get().getBuyerId() + " with a bid of " + winningBid.get().getBidAmount());
        } else {
            System.out.println("No bids received for this auction.");
        }

        bids.clear();
        auctionLatch.countDown();
        System.exit(0);
        
    }

    public int getCurrentBidId() {
        return currentBidId;
    }
    public void logout() {
        System.out.println("Admin logged out.");
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing server socket: " + e.getMessage());
        }
    }
}

