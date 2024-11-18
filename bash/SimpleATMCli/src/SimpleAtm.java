import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Scanner;

public class SimpleAtm {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static String token = null;

    public SimpleAtm() {
    }

    public static void main(String[] var0) throws IOException, InterruptedException {
        Scanner var1 = new Scanner(System.in);
        System.out.println("Welcome to Simple ATM CLI!");

        while(true) {
            System.out.print("$ ");
            String var2 = var1.nextLine().trim();

            try {
                String[] var3;
                String var4;
                if (var2.startsWith("login")) {
                    var3 = var2.split(" ");
                    if (var3.length < 2) {
                        System.out.println("Usage: login <username>");
                    } else {
                        var4 = var3[1].trim();
                        login(var4);
                    }
                } else {
                    BigDecimal var8;
                    if (var2.startsWith("deposit")) {
                        var3 = var2.split(" ");
                        if (var3.length < 2) {
                            System.out.println("Usage: deposit <amount>");
                        } else {
                            var8 = new BigDecimal(var3[1].trim());
                            deposit(var8);
                        }
                    } else if (var2.startsWith("withdraw")) {
                        var3 = var2.split(" ");
                        if (var3.length < 2) {
                            System.out.println("Usage: withdraw <amount>");
                        } else {
                            var8 = new BigDecimal(var3[1].trim());
                            withdraw(var8);
                        }
                    } else if (var2.startsWith("transfer")) {
                        var3 = var2.split(" ");
                        if (var3.length < 3) {
                            System.out.println("Usage: transfer <username> <amount>");
                        } else {
                            var4 = var3[1].trim();
                            BigDecimal var5 = new BigDecimal(var3[2].trim());
                            transfer(var4, var5);
                        }
                    } else {
                        if (var2.equalsIgnoreCase("logout")) {
                            logout();
                            break;
                        }

                        System.out.println("Unknown command. Try 'login', 'deposit', 'transfer', or 'logout'.");
                    }
                }
            } catch (NumberFormatException var6) {
                System.out.println("Invalid amount format. Please enter a valid number.");
            } catch (Exception var7) {
                System.out.println("An error occurred: " + var7.getMessage());
            }
        }

        System.out.println("Goodbye!");
    }

    private static void login(String var0) throws IOException, InterruptedException {
        Scanner var1 = new Scanner(System.in);
        System.out.print("Password: ");
        String var2 = var1.nextLine().trim();
        HttpRequest var3 = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/login")).header("Content-Type", "application/json").POST(BodyPublishers.ofString("{\"username\":\"" + var0 + "\",\"password\":\"" + var2 + "\"}")).build();
        HttpResponse var4 = httpClient.send(var3, BodyHandlers.ofString());
        if (var4.statusCode() == 200) {
            token = parseToken((String)var4.body());
            System.out.println("Hello, " + var0 + "!");
            System.out.println("Your balance is $" + parseBalance((String)var4.body()));
        } else {
            System.out.println("Login failed: " + (String)var4.body());
        }

    }

    private static void deposit(BigDecimal amount) throws IOException, InterruptedException {
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
            BigDecimal newBalance = parseBalance(response.body());
            System.out.println("Deposit successful. Your new balance is: $" + newBalance);
        } else {
            System.out.println("Deposit failed: " + response.body());
        }
    }

    private static void withdraw(BigDecimal var0) throws IOException, InterruptedException {
        if (token == null) {
            System.out.println("You must login first.");
        } else {
            HttpRequest var1 = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/transactions/withdraw")).header("Authorization", "Bearer " + token).header("Content-Type", "application/json").POST(BodyPublishers.ofString("{\"amount\":" + var0 + "}")).build();
            HttpResponse var2 = httpClient.send(var1, BodyHandlers.ofString());
            if (var2.statusCode() == 200) {
                System.out.println("Withdrawal successful.");
                System.out.println("Your balance is $" + parseBalance((String)var2.body()));
            } else {
                System.out.println("Withdrawal failed: " + (String)var2.body());
            }

        }
    }

    private static void transfer(String var0, BigDecimal var1) throws IOException, InterruptedException {
        if (token == null) {
            System.out.println("You must login first.");
        } else {
            HttpRequest var2 = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/transactions/transfer")).header("Authorization", "Bearer " + token).header("Content-Type", "application/json").POST(BodyPublishers.ofString("{\"receiverUsername\":\"" + var0 + "\",\"amount\":" + var1 + "}")).build();
            HttpResponse var3 = httpClient.send(var2, BodyHandlers.ofString());
            if (var3.statusCode() == 200) {
                System.out.println("Transfer successful: " + (String)var3.body());
            } else {
                System.out.println("Transfer failed: " + (String)var3.body());
            }

        }
    }

    private static void logout() throws IOException, InterruptedException {
        if (token == null) {
            System.out.println("You are not logged in.");
        } else {
            HttpRequest var0 = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/logout")).header("Authorization", "Bearer " + token).POST(BodyPublishers.noBody()).build();
            HttpResponse var1 = httpClient.send(var0, BodyHandlers.ofString());
            if (var1.statusCode() == 200) {
                System.out.println("Goodbye!");
                token = null;
            } else {
                System.out.println("Logout failed: " + (String)var1.body());
            }

        }
    }

    private static String parseToken(String var0) {
        int var1 = var0.indexOf("\"token\":\"") + 9;
        int var2 = var0.indexOf("\"", var1);
        return var0.substring(var1, var2);
    }

    private static BigDecimal parseBalance(String var0) {
        int var1 = var0.indexOf("\"balance\":") + 10;
        int var2 = var0.indexOf(",", var1);
        if (var2 == -1) {
            var2 = var0.indexOf("}", var1);
        }

        return new BigDecimal(var0.substring(var1, var2));
    }

    private static BigDecimal parseBalanceDeposit(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseBody);

        // Navigate to the "balanceAfterTransaction" field
        JsonNode balanceNode = rootNode
                .path("data")
                .path("transaction")
                .path("balanceAfterTransaction");

        // Return the balance as a BigDecimal
        if (!balanceNode.isMissingNode()) {
            return balanceNode.decimalValue();
        } else {
            throw new IOException("balanceAfterTransaction not found in the response");
        }
    }

}
