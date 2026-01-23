package client.grpc;

import common.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SimpleGrpcClient {

    private final ManagedChannel channel;
    private final ChatServiceGrpc.ChatServiceBlockingStub blockingStub;
    private String username;

    public SimpleGrpcClient(String host, int port) {
        try {
            // ✅ Force IPv4 stack before creating channel
            System.setProperty("java.net.preferIPv4Stack", "true");
            System.setProperty("java.net.preferIPv4Addresses", "true");

            System.out.println("[CLIENT] Connecting to gRPC server...");
            System.out.println("[CLIENT] Host: " + host);
            System.out.println("[CLIENT] Port: " + port);

            // ✅ Create channel with proper settings
            this.channel = ManagedChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext()
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(10, TimeUnit.SECONDS)
                    .keepAliveWithoutCalls(true)
                    .build();

            System.out.println("[CLIENT] Channel created successfully");

            this.blockingStub = ChatServiceGrpc.newBlockingStub(channel);

        } catch (Exception e) {
            System.err.println("[CLIENT ERROR] Failed to create channel: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot create gRPC channel", e);
        }
    }

    public void shutdown() throws InterruptedException {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public boolean authenticate(String username) {
        this.username = username;

        try {
            System.out.println("\n[CLIENT] === AUTHENTICATION ===");
            System.out.println("[CLIENT] Username: " + username);

            // Wait for channel to be ready
            System.out.println("[CLIENT] Waiting for channel to connect...");
            boolean connected = false;
            for (int i = 0; i < 10; i++) {
                io.grpc.ConnectivityState state = channel.getState(true);
                System.out.println("[CLIENT] Channel state: " + state);
                
                if (state == io.grpc.ConnectivityState.READY) {
                    connected = true;
                    break;
                }
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (!connected) {
                System.err.println("[CLIENT ERROR] Channel not ready after 5 seconds");
                System.err.println("[CLIENT ERROR] Make sure server is running on port 9090");
                return false;
            }

            // Build request
            AuthRequest request = AuthRequest.newBuilder()
                    .setUsername(username)
                    .setPassword("demo")
                    .build();

            System.out.println("[CLIENT] Sending auth request...");

            // Call server with timeout
            AuthResponse response;
            try {
                response = blockingStub
                        .withDeadlineAfter(10, TimeUnit.SECONDS)
                        .authenticate(request);
            } catch (io.grpc.StatusRuntimeException e) {
                System.err.println("[CLIENT ERROR] RPC failed: " + e.getStatus());
                System.err.println("[CLIENT ERROR] Cause: " + e.getCause());
                throw e;
            }

            System.out.println("[CLIENT] ✅ Response received");
            System.out.println("[CLIENT] Success: " + response.getSuccess());
            System.out.println("[CLIENT] Message: " + response.getMessage());

            if (response.getSuccess()) {
                System.out.println("\n✅ " + response.getMessage() + "\n");
            } else {
                System.err.println("\n❌ " + response.getMessage() + "\n");
            }

            return response.getSuccess();

        } catch (io.grpc.StatusRuntimeException e) {
            System.err.println("\n[CLIENT ERROR] === gRPC EXCEPTION ===");
            System.err.println("[CLIENT ERROR] Status: " + e.getStatus().getCode());
            System.err.println("[CLIENT ERROR] Description: " + e.getStatus().getDescription());

            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("[CLIENT ERROR] Cause: " + cause.getClass().getSimpleName());
                System.err.println("[CLIENT ERROR] Message: " + cause.getMessage());

                // Print first 5 stack trace elements
                System.err.println("[CLIENT ERROR] Stack trace:");
                StackTraceElement[] stack = cause.getStackTrace();
                for (int i = 0; i < Math.min(5, stack.length); i++) {
                    System.err.println("  at " + stack[i]);
                }
            }

            System.err.println("\n❌ Connection failed");
            System.err.println("💡 Troubleshooting:");
            System.err.println("   1. Check if server is running: netstat -ano | findstr :9090");
            System.err.println("   2. Check firewall settings");
            System.err.println("   3. Try running client and server separately first\n");
            return false;

        } catch (Exception e) {
            System.err.println("\n[CLIENT ERROR] Unexpected exception");
            System.err.println("[CLIENT ERROR] " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(String content) {
        try {
            ChatMessage message = ChatMessage.newBuilder()
                    .setSender(username)
                    .setContent(content)
                    .setTimestamp(System.currentTimeMillis())
                    .setType(MessageType.BROADCAST)
                    .build();

            MessageResponse response = blockingStub.sendMessage(message);

            if (response.getSuccess()) {
                System.out.println("  ✓ Sent");
            } else {
                System.err.println("  ✗ Failed: " + response.getMessage());
            }
        } catch (Exception e) {
            System.err.println("  ✗ Error: " + e.getMessage());
        }
    }

    public void getOnlineUsers() {
        try {
            Empty request = Empty.newBuilder().build();
            UserList response = blockingStub.getOnlineUsers(request);

            System.out.println("\n📋 Online users (" + response.getCount() + "):");
            for (String user : response.getUsernamesList()) {
                System.out.println("  • " + user);
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("❌ Failed to get users: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // ✅ Set IPv4 preference BEFORE anything else
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");

        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║     gRPC Chat Client - Test Mode     ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println("  Server: 127.0.0.1:9090\n");

        SimpleGrpcClient client = null;

        try {
            // Create client with 127.0.0.1 to force IPv4
            client = new SimpleGrpcClient("127.0.0.1", 9090);

            try (Scanner scanner = new Scanner(System.in)) {

                System.out.print("👤 Username: ");
                String username = scanner.nextLine().trim();

                if (username.isEmpty()) {
                    username = "Guest" + (System.currentTimeMillis() % 1000);
                    System.out.println("   Using: " + username);
                }

                // Authenticate
                if (!client.authenticate(username)) {
                    System.out.println("❌ Authentication failed");
                    System.out.println("   Check if server is running:");
                    System.out.println("   netstat -ano | findstr :9090");
                    return;
                }

                // Connected!
                System.out.println("📡 Connected! Commands:");
                System.out.println("  /users - List online users");
                System.out.println("  /quit  - Exit");
                System.out.println("  <text> - Send message\n");

                // Main loop
                while (true) {
                    System.out.print(username + " > ");
                    String input = scanner.nextLine().trim();

                    if (input.isEmpty()) {
                        continue;
                    }

                    if (input.equalsIgnoreCase("/quit") || input.equalsIgnoreCase("/q")) {
                        break;
                    } else if (input.equalsIgnoreCase("/users") || input.equalsIgnoreCase("/u")) {
                        client.getOnlineUsers();
                    } else if (input.equalsIgnoreCase("/help") || input.equalsIgnoreCase("/h")) {
                        System.out.println("\nCommands:");
                        System.out.println("  /users - List online users");
                        System.out.println("  /quit  - Exit");
                        System.out.println("  /help  - Show this help\n");
                    } else {
                        client.sendMessage(input);
                    }
                }

                System.out.println("\n👋 Goodbye!\n");
            }

        } catch (Exception e) {
            System.err.println("\n❌ Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.shutdown();
                } catch (InterruptedException e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
            }
        }
    }
}