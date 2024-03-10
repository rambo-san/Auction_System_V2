package packs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ConcurrentHashMap<Integer, BidInfo> bids; // Use the current auction's bidId as the key
    private AdminOperations adminOperations;

    public ClientHandler(Socket socket, ConcurrentHashMap<Integer, BidInfo> bids, AdminOperations adminOperations) {
        System.out.println("The client is here");
        this.clientSocket = socket;
        this.bids = bids;
        this.adminOperations = adminOperations;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if ("START_AUCTION".equalsIgnoreCase(inputLine)) {
                    adminOperations.startAuction();
                } else if (inputLine.startsWith("BID")) {
                    handleBidCommand(inputLine, out);
                } else if ("END_AUCTION".equalsIgnoreCase(inputLine)) {
                    adminOperations.endAuction();
                } else {
                    out.println("Unknown command");
                }
            }
        } catch (IOException e) {
            System.out.println("Exception in ClientHandler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void handleBidCommand(String inputLine, PrintWriter out) {
        String[] parts = inputLine.split(" ");
        if (parts.length == 3) { // The command is in the format "BID buyerId amount"
            try {
                int buyerId = Integer.parseInt(parts[1]);
                int bidAmount = Integer.parseInt(parts[2]);

                // Assuming there's only one active auction at a time and its ID is known to AdminOperations
                int currentBidId = adminOperations.getCurrentBidId(); // This method needs to be implemented in AdminOperations

                if (currentBidId != -1) { // -1 or another value could indicate no active auction
                    BidInfo currentBidInfo = bids.get(currentBidId);

                    if (currentBidInfo == null || bidAmount > currentBidInfo.getBidAmount()) {
                        bids.put(currentBidId, new BidInfo(buyerId, bidAmount));
                        out.println("Bid accepted for " + bidAmount);
                    } else {
                        out.println("Bid not accepted. Current highest bid is higher or equal.");
                    }
                } else {
                    out.println("No active auction to place bids.");
                }

            } catch (NumberFormatException e) {
                out.println("Invalid bid format.");
            }
        } else {
            out.println("Invalid bid command.");
        }
    }
}
