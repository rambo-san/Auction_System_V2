package packs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminOperations {
    private Timer auctionTimer;
    private boolean auctionActive = false;
    private ExecutorService clientHandlerExecutor;
    private ConcurrentHashMap<String, Integer> bids; // A thread-safe collection to store bids
    private ServerSocket serverSocket;
    private boolean running = true; // Control the server loop
    private CountDownLatch auctionEndLatch = new CountDownLatch(1); // Initialize the auctionEndLatch variable


    public static void main(String[] args) {
        AdminOperations server = new AdminOperations();
        server.initializeServer();
    }

    public void initializeServer() {
        System.out.println("Server initializing...");
        clientHandlerExecutor = Executors.newCachedThreadPool();
        bids = new ConcurrentHashMap<>();
        try {
            serverSocket = new ServerSocket(12345); // Use your desired port
            startServer();
        } catch (IOException e) {
            System.out.println("Could not listen on the specified port: " + e.getMessage());
        }
    }

    private void startServer() {
        System.out.println("Server started. Waiting for admin to start the auction...");
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept(); // Accept client connection
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                // Handle client connection in a separate thread
                clientHandlerExecutor.submit(new ClientHandler(clientSocket, bids, this)); // Pass 'this' to access auction control methods.
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    
private CountDownLatch auctionLatch = new CountDownLatch(1); // Initialize the auctionLatch variable

public void startAuction() {
    if (!auctionActive) {
        auctionActive = true;
        int bidId = generateBidId();

        auctionLatch = new CountDownLatch(1); // Reset the latch for the new auction
        

        System.out.println("Auction has started. Accepting bids for 1 minute. Bid ID: " + bidId);
        bids.put("Auction ID: " + bidId, 0); // Add the auction ID to the bids map
        startAuctionTimer();
        try {
            auctionLatch.await(); // Pause other threads until the auction ends
        } catch (InterruptedException e) {
            System.out.println("Auction interrupted: " + e.getMessage());
        }
        displayBids(); // Display the bids live on the admin's panel
    } else {
        System.out.println("Auction is already in progress.");
    }
}



    private void displayBids() {
        System.out.println("Current bids:");
        for (String bidId : bids.keySet()) {
            int bidAmount = bids.get(bidId);
            System.out.println("Bid ID: " + bidId + ", Amount: " + bidAmount);
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
        }, 60000); // Auction duration is 1 minute
    }

    public void endAuction() {
        auctionActive = false;
        if (auctionTimer != null) {
            auctionTimer.cancel();
        }
        System.out.println("Auction ended.");
    
        // Determine the winning bid
        bids.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                .ifPresent(entry -> System.out.println("Winner is " + entry.getKey() + " with a bid of " + entry.getValue()));
    
        // Reset for the next auction
        bids.clear();
        // Do not shut down the executor if you plan to use it for future auctions
        // clientHandlerExecutor.shutdownNow(); // Commented out to keep the executor alive for future client handling
        // clientHandlerExecutor = Executors.newCachedThreadPool(); // Not needed if we keep the executor alive
    
        auctionLatch.countDown(); // Release the latch to allow other threads to continue
    }
    
    

    public void logout() {
        System.out.println("Admin logged out.");
        running = false; // Stop the server loop
        try {
            serverSocket.close(); // Close the server socket
        } catch (IOException e) {
            System.out.println("Error closing server socket: " + e.getMessage());
        }
        auctionEndLatch.countDown(); // Release the latch to allow other threads to continue
    }

}




