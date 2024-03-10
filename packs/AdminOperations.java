package packs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

public class AdminOperations {
    private Timer auctionTimer;
    private boolean auctionActive = false;
    private ExecutorService clientHandlerExecutor;
    private ConcurrentHashMap<Integer, BidInfo> bids = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;
    private boolean running = true;
    private CountDownLatch auctionLatch;
    private volatile int currentBidId = -1; // Default to -1 indicating no active auction


    public static void main(String[] args) {
        AdminOperations server = new AdminOperations();
        server.initializeServer();
    }

    public void initializeServer() {
        System.out.println("Server initializing...");
        clientHandlerExecutor = Executors.newCachedThreadPool();
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
                Socket clientSocket = serverSocket.accept();
                // Handle client connection in a separate thread
                // Placeholder for ClientHandler - Ensure you have a ClientHandler class implemented
                clientHandlerExecutor.submit(new ClientHandler(clientSocket, bids, this));
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    public void startAuction() {
        if (!auctionActive) {
            auctionActive = true;
            currentBidId = generateBidId();
            auctionLatch = new CountDownLatch(1);
            System.out.println("Auction has started. Accepting bids for 1 minute. Bid ID: " + currentBidId);
            bids.put(currentBidId, new BidInfo(-1, 0));
            startAuctionTimer();
            try {
                auctionLatch.await();
            } catch (InterruptedException e) {
                System.out.println("Auction interrupted: " + e.getMessage());
            }
            displayBids();
        } else {
            System.out.println("Auction is already in progress.");
        }
    }

    private void displayBids() {
        System.out.println("Current bids:");
        bids.forEach((bidId, bidInfo) -> System.out.println("Bid ID: " + bidId + ", Buyer ID: " + bidInfo.getBuyerId() + ", Bid Amount: " + bidInfo.getBidAmount()));
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
        auctionTimer.cancel();
        currentBidId = -1; // Reset the currentBidId indicating no active auction
        System.out.println("Auction ended.");

        bids.entrySet().stream()
                .max((entry1, entry2) -> Integer.compare(entry1.getValue().getBidAmount(), entry2.getValue().getBidAmount()))
                .ifPresent(entry -> System.out.println("Winner is Buyer ID " + entry.getValue().getBuyerId() + " with a bid of " + entry.getValue().getBidAmount()));

        bids.clear();
        auctionLatch.countDown();
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
    public int getCurrentBidId() {
        return currentBidId;
    }
}
