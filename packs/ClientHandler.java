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
                // Example command processing
                if ("START_AUCTION".equalsIgnoreCase(inputLine)) {
                    // Check if the auction can be started
                    adminOperations.startAuction(); // You might need to adjust this based on how your AdminOperations class is structured
                } else if (inputLine.startsWith("BID")) {
                    String[] parts = inputLine.split(" ");
                    if (parts.length == 3) { // Assuming the command is in the format "BID username amount"
                        try {
                            int bidAmount = Integer.parseInt(parts[2]);
                            bids.put(parts[1], bidAmount); // Store or update the bid for this user
                            out.println("Bid accepted for " + parts[1] + " with amount " + bidAmount);
                        } catch (NumberFormatException e) {
                            out.println("Invalid bid amount");
                        }
                    } else {
                        out.println("Invalid bid command");
                    }
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
}
