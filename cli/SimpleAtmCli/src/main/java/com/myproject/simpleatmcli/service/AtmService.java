package com.myproject.simpleatmcli.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class AtmService {
    private static String token;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Simple ATM CLI!");

        while (true) {
            System.out.print("$ ");
            String command = scanner.nextLine().trim();

            try {
                if (command.startsWith("login")) {
                    String[] parts = command.split(" ");
                    if (parts.length < 2) {
                        System.out.println("Usage: login <username>");
                    } else {
                        String username = parts[1].trim();
                        login(username);
                    }
                } else if (command.startsWith("deposit")) {
                    String[] parts = command.split(" ");
                    if (parts.length < 2) {
                        System.out.println("Usage: deposit <amount>");
                    } else {
                        BigDecimal amount = new BigDecimal(parts[1].trim());
                        deposit(amount);
                    }
                } else if (command.startsWith("withdraw")) {
                    String[] parts = command.split(" ");
                    if (parts.length < 2) {
                        System.out.println("Usage: withdraw <amount>");
                    } else {
                        BigDecimal amount = new BigDecimal(parts[1].trim());
                        withdraw(amount);
                    }
                } else if (command.startsWith("transfer")) {
                    String[] parts = command.split(" ");
                    if (parts.length < 3) {
                        System.out.println("Usage: transfer <username> <amount>");
                    } else {
                        String receiverUsername = parts[1].trim();
                        BigDecimal amount = new BigDecimal(parts[2].trim());
                        transfer(receiverUsername, amount);
                    }
                } else if (command.startsWith("logout")) {
                    logout();
                } else if (command.startsWith("exit")){
                    break;
                } else {
                    System.out.println("Unknown command. Try 'login', 'deposit', 'withdraw', 'transfer', 'logout', or 'exit'.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount format. Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private void login(String username) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        // Prompt for password
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        // Build and send the login request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/api/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Handle response
        if (response.statusCode() == 200) {
            token = parseToken(response.body());
            LoginResponse loginResponse = parseLoginResponse(response.body());
            System.out.println("Hello, " + loginResponse.getUsername() + "!");
            System.out.println("Your balance is $" + loginResponse.getBalance());
            if (!loginResponse.getOwedDetails().isEmpty()) {
                for (String detail : loginResponse.getOwedDetails()) {
                    System.out.println(detail);
                }
            }
        } else {
            System.out.println("Login failed: " + response.body());
        }
    }

    private static void withdraw(BigDecimal amount) throws IOException, InterruptedException {
        if (token == null) {
            System.out.println("You must login first.");
            return;
        }

        // Build and send the withdraw request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/api/transactions/withdraw"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"amount\":" + amount + "}"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Handle response
        if (response.statusCode() == 200) {
            System.out.println("Your balance is $" + parseDepositBalance(response.body()));
        } else {
            System.out.println("Withdrawal failed: " + response.body());
        }
    }

    private void deposit(BigDecimal amount) throws IOException, InterruptedException {
        if (token == null) {
            System.out.println("You must login first.");
            return;
        }

        // Build and send the deposit request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/api/transactions/deposit"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"amount\":" + amount + "}"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Handle response
        if (response.statusCode() == 200) {
            DepositResponse depositResponse = parseDepositResponse(response.body());
            System.out.println("Your balance is $" + depositResponse.getBalance() );
            if (!depositResponse.getOwedDetails().isEmpty()) {
                for (String detail : depositResponse.getOwedDetails()) {
                    System.out.println(detail);
                }
            }
        } else {
            System.out.println("Deposit failed: " + response.body());
        }
    }

    private void logout() throws IOException {
        if (token == null) {
            System.out.println("You are not logged in.");
            return;
        }

        // Build and send the logout request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/api/logout"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        // Handle response
        if (response.statusCode() == 200) {
            System.out.println(parseLogoutMessage(response.body()));
            token = null;
        } else {
            System.out.println("Logout failed: " + response.body());
        }
    }

    private static void transfer(String receiverUsername, BigDecimal amount) throws IOException, InterruptedException {
        if (token == null) {
            System.out.println("You must login first.");
            return;
        }

        // Build and send the transfer request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/api/transactions/transfer"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"receiverUsername\":\"" + receiverUsername + "\",\"amount\":" + amount + "}"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Handle response
        if (response.statusCode() == 200) {
            TransferResponse transferResponse = parseTransferResponse(response.body());
            System.out.println(transferResponse.getMessage());

            System.out.println("Your balance is $" + transferResponse.getBalance());
            if (transferResponse.getOwed() != null) {
                System.out.println(transferResponse.getOwed());
            }

        } else {
            System.out.println("Transfer failed: " + response.body());
        }
    }
    private static String parseToken(String responseBody) {
        int tokenStart = responseBody.indexOf("\"token\":\"") + 9;
        int tokenEnd = responseBody.indexOf("\"", tokenStart);
        return responseBody.substring(tokenStart, tokenEnd);
    }

    private static String parseLogoutMessage (String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseBody);

        // Navigate to the "balanceAfterTransaction" field
        JsonNode balanceNode = rootNode
                .path("message");

        if (balanceNode.isMissingNode()) {
            throw new IOException("Message not found in the response");
        }

        return balanceNode.asText();
    }
    private static BigDecimal parseDepositBalance(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseBody);

        // Navigate to the "balanceAfterTransaction" field
        JsonNode balanceNode = rootNode
                .path("data")
                .path("transaction")
                .path("account")
                .path("balance");

        if (balanceNode.isMissingNode()) {
            throw new IOException("Balance not found in the response");
        }

        return balanceNode.decimalValue(); // Convert JSON node to BigDecimal
    }

    public class DepositResponse {
        private final BigDecimal balance;
        private final List<String> owedDetails;

        public DepositResponse(BigDecimal balance, List<String> owedDetails) {
            this.balance = balance;
            this.owedDetails = owedDetails;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public List<String> getOwedDetails() {
            return owedDetails;
        }
    }

    public class LoginResponse {
        private final String username;
        private final BigDecimal balance;
        private final List<String> owedDetails;

        public LoginResponse(String username, BigDecimal balance, List<String> owedDetails) {
            this.username = username;
            this.balance = balance;
            this.owedDetails = owedDetails;
        }

        public String getUsername() {
            return username;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public List<String> getOwedDetails() {
            return owedDetails;
        }
    }

    private static class TransferResponse {
        private final String message;
        private final BigDecimal balance;
        private final String owed;

        public TransferResponse(String message, BigDecimal balance, String owed) {
            this.message = message;
            this.balance = balance;
            this.owed = owed;
        }

        public String getMessage() {
            return message;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public String getOwed() {
            return owed;
        }
    }

    private LoginResponse parseLoginResponse(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseBody);

        // Extract "username"
        String username = rootNode.path("data").path("username").asText();

        // Extract "balance"
        BigDecimal balance = BigDecimal.ZERO;
        balance= rootNode.path("data").path("balance").decimalValue(); // Correctly declared and initialized

        // Extract "owedDetails" (if present)
        JsonNode owedDetailsNode = rootNode.path("data").path("owedDetails");
        List<String> owedDetails = new ArrayList<>();
        if (owedDetailsNode.isArray()) {
            for (JsonNode detail : owedDetailsNode) {
                owedDetails.add(detail.asText());
            }
        }

        return new LoginResponse(username, balance, owedDetails);
    }

    private static TransferResponse parseTransferResponse(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseBody);

        // Extract "message" field
        String message = rootNode.path("message").asText();

        // Extract "balance" field
        BigDecimal balance = rootNode.path("data").path("balance").decimalValue();

        // Extract "owed" field if present
        JsonNode owedNode = rootNode.path("data").path("owed");
        String owed = owedNode.isMissingNode() ? null : owedNode.asText();

        return new TransferResponse(message, balance, owed);
    }

    private DepositResponse parseDepositResponse(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseBody);

        // Extract "balance"
        BigDecimal balance = rootNode.path("data")
                .path("transaction")
                .path("account")
                .path("balance")
                .decimalValue();

        // Extract "owed" (if present)
        JsonNode owedNode = rootNode.path("data").path("owed");
        List<String> owedDetails = new ArrayList<>();
        if (owedNode.isArray()) {
            for (JsonNode detail : owedNode) {
                owedDetails.add(detail.asText());
            }
        }

        return new DepositResponse(balance, owedDetails);
    }

}
