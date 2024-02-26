package packs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ConcurrentHashMap<String, Integer> bids;
    private AdminOperations adminOperations;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, Integer> bids, AdminOperations adminOperations) {
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
                    // AdminOperations class should handle whether it's appropriate to start the auction
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
        if (parts.length == 3) { // Assuming the command is in the format "BID username amount"
            try {
                int bidAmount = Integer.parseInt(parts[2]);
                String username = parts[1];
                // Update the bid for this user, or add if not present
                bids.merge(username, bidAmount, Integer::max); // This will keep the highest bid only
                out.println("Bid accepted for " + username + " with amount " + bidAmount);
            } catch (NumberFormatException e) {
                out.println("Invalid bid amount");
            }
        } else {
            out.println("Invalid bid command");
        }
    }
}