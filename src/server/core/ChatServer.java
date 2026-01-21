package server.core;

import common.protocol.ProtocolConstants;
import server.config.ServerConfig;
import server.security.SSLConfig;
import server.http.HttpApiServer;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ChatServer - Main server entry point
 * 
 * Network Programming Concepts:
 * - ServerSocket: Listens for incoming TCP connections
 * - ExecutorService: Thread pool để handle multiple clients
 * - SSL/TLS: Optional encrypted communication
 * 
 * Architecture:
 * 1. Bind to port và listen
 * 2. Accept connections trong infinite loop
 * 3. Delegate each client to ClientHandler trong thread pool
 * 4. ServerContext quản lý toàn bộ shared state
 */
public class ChatServer {

    private final ServerContext context;
    private ServerSocket serverSocket;
    private HttpApiServer httpServer;
    private volatile boolean running;

    public ChatServer(ServerConfig config) {
        this.context = new ServerContext(config);
        this.httpServer = new HttpApiServer(context, 8080); // ← ADD THIS
        this.running = false;
    }

    /**
     * Start server - Bind to port và accept connections
     */
    public void start() throws IOException {
        ServerConfig config = context.getConfig();

        try {
            httpServer.start();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to start HTTP server: "
                    + e.getMessage());
        }

        try {
            // Create ServerSocket (plain or SSL)
            if (config.isSslEnabled()) {
                System.out.println("Starting server with SSL/TLS...");
                SSLServerSocketFactory factory = SSLConfig.createServerSocketFactory(
                        config.getKeystorePath(),
                        config.getKeystorePassword());

                serverSocket = factory.createServerSocket(
                        config.getPort(),
                        config.getBacklog());

                // Enable all cipher suites (for demo; trong production nên filter)
                ((SSLServerSocket) serverSocket).setEnabledCipherSuites(
                        ((SSLServerSocket) serverSocket).getSupportedCipherSuites());
            } else {
                System.out.println("Starting server without SSL (PLAIN TEXT - NOT SECURE)...");
                serverSocket = new ServerSocket(
                        config.getPort(),
                        config.getBacklog());
            }
        } catch (Exception e) {
            throw new IOException("Failed to create server socket: " + e.getMessage(), e);
        }

        running = true;
        context.start();

        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  " + ProtocolConstants.SERVER_NAME);
        System.out.println("  Version: " + ProtocolConstants.SERVER_VERSION);
        System.out.println("  Port: " + config.getPort());
        System.out.println("  SSL: " + (config.isSslEnabled() ? "ENABLED" : "DISABLED"));
        System.out.println("  Max Clients: " + config.getMaxClients());
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();
        System.out.println("Server is ready and listening for connections...");
        System.out.println();

        // Main accept loop
        while (running) {
            try {
                // Block until client connects
                Socket clientSocket = serverSocket.accept();

                String clientAddr = clientSocket.getRemoteSocketAddress().toString();
                System.out.println("[CONNECTION] Client connected from: " + clientAddr);

                // Check if server is at capacity
                if (context.getOnlineUserCount() >= config.getMaxClients()) {
                    System.out.println("[REJECT] Server at capacity, rejecting: " + clientAddr);
                    clientSocket.close();
                    continue;
                }

                // Create handler and submit to thread pool
                ClientHandler handler = new ClientHandler(clientSocket, context);
                context.getThreadPool().execute(handler);

            } catch (IOException e) {
                if (running) {
                    System.err.println("[ERROR] Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Stop server gracefully
     */
    public void stop() {
        System.out.println("\n[SHUTDOWN] Stopping server...");
        running = false;

        try {
            httpServer.stop();
        } catch (Exception e) {
            System.err.println("[ERROR] Error stopping HTTP server: "
                    + e.getMessage());
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error closing server socket: " + e.getMessage());
        }

        // Disconnect all clients
        context.getHandlers().values().forEach(ClientHandler::disconnect);

        // Stop context (thread pool, heartbeat service)
        context.stop();

        System.out.println("[SHUTDOWN] Server stopped.");
    }

    /**
     * Get server context for monitoring
     */
    public ServerContext getContext() {
        return context;
    }

    /**
     * Main method - Entry point
     */
    public static void main(String[] args) {
        try {
            // Load configuration
            ServerConfig config;
            if (args.length > 0) {
                System.out.println("Loading configuration from: " + args[0]);
                config = ServerConfig.loadFromFile(args[0]);
            } else {
                System.out.println("Using default configuration");
                config = ServerConfig.createDefault();
            }

            // Create and start server
            ChatServer server = new ChatServer(config);

            // Add shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop();
            }));

            // Start server (blocks)
            server.start();

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
