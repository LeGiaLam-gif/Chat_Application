package test;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import common.grpc.*;

public class QuickTest {
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");
        
        System.out.println("Creating channel...");
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("127.0.0.1", 9090)
                .usePlaintext()
                .build();
        
        System.out.println("Channel created, state: " + channel.getState(true));
        
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(channel);
        
        try {
            System.out.println("Sending request...");
            AuthRequest request = AuthRequest.newBuilder()
                    .setUsername("TestUser")
                    .setPassword("test")
                    .build();
            
            AuthResponse response = stub.authenticate(request);
            System.out.println("[OK] Success: " + response.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
        } finally {
            channel.shutdown();
        }
    }
}
