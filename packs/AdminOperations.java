package packs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminOperations {
    private Timer auctionTimer;
    private boolean auctionActive = false;
    private ExecutorService clientHandlerExecutor;
    private ConcurrentHashMap<String, Integer> bids; // A thread-safe collection to store bids
    private ServerSocket serverSocket;
    private boolean running = true; // Control the server loop

    public static void main(String[] args) {
        AdminOperations server = new AdminOperations();
        server.initializeServer();
    }

    public void initializeServer() {
        System.out.println("Server initializing...");
        clientHandlerExecutor = Executors.newCachedThreadPool();
        bids = new ConcurrentHashMap<>();
        // Initialize server resources, e.g., server socket
        try {
            serverSocket = new ServerSocket(12345); // Use your desired port
            startServer();
        } catch (IOException e) {
            System.out.println("Could not listen on the specified port: " + e.getMessage());
            return;
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

    // Access modifier changed to public to allow calling from ClientHandler
    public void startAuction() {
        if (!auctionActive) {
            auctionActive = true;
            System.out.println("Auction has started. Accepting bids for 1 minute.");
            startAuctionTimer();
            simulateBidding(); // This is for demonstration; real bids come from clients.
        } else {
            System.out.println("Auction is already in progress.");
        }
    }

    private void simulateBidding() {
        // This method simulates bid handling. In a real application, bids would come from clients.
        clientHandlerExecutor.submit(() -> {
            // Simulate bid processing in a separate thread
            try {
                Thread.sleep(10000); // Simulate delay
                bids.put("User1", 100);
                Thread.sleep(5000); // Simulate another bid coming in later
                bids.put("User2", 150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
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
        auctionTimer.cancel();
        System.out.println("Auction ended.");

        // Determine the winning bid
        bids.entrySet().stream().max((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                .ifPresent(entry -> System.out.println("Winner is " + entry.getKey() + " with a bid of " + entry.getValue()));

        // Reset for the next auction
        bids.clear();
        clientHandlerExecutor.shutdownNow();
        // Reinitialize the executor for the next auction
        clientHandlerExecutor = Executors.newCachedThreadPool();
    }
}
