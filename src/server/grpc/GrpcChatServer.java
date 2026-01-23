package server.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import server.core.ServerContext;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GrpcChatServer {

    private final ServerContext context;
    private final int port;
    private Server server;

    public GrpcChatServer(ServerContext context, int port) {
        this.context = context;
        this.port = port;
        System.out.println("[gRPC] GrpcChatServer instance created for port " + port);
    }

    public void start() throws IOException {
        System.out.println("[gRPC] Starting server...");

        try {
            // Force IPv4 and bind to all interfaces
            System.setProperty("java.net.preferIPv4Stack", "true");
            System.setProperty("java.net.preferIPv4Addresses", "true");

            server = ServerBuilder.forPort(port)
                    .addService(new ChatServiceImpl(context))
                    .build()
                    .start();

            System.out.println("═══════════════════════════════════════");
            System.out.println("  gRPC Server Started");
            System.out.println("  Port: " + port);
            System.out.println("  Listening on: 0.0.0.0:" + port);
            System.out.println("  Connect via: localhost:" + port);
            System.out.println("═══════════════════════════════════════");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.err.println("[gRPC] Shutting down gRPC server");
                try {
                    GrpcChatServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
            }));

        } catch (Exception e) {
            System.err.println("[gRPC] FAILED TO START: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            System.out.println("[gRPC] Stopping server...");
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            System.out.println("[gRPC] Server stopped");
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}