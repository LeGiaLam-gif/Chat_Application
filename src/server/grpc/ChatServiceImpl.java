package server.grpc;

import common.grpc.*;
import io.grpc.stub.StreamObserver;
import server.core.ServerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    private final ServerContext context;
    private final Map<String, StreamObserver<ChatMessage>> activeStreams;

    public ChatServiceImpl(ServerContext context) {
        this.context = context;
        this.activeStreams = new ConcurrentHashMap<>();
        System.out.println("[gRPC] ChatServiceImpl initialized");
    }

    @Override
    public void authenticate(AuthRequest request,
            StreamObserver<AuthResponse> responseObserver) {
        try {
            String username = request.getUsername();
            System.out.println("[gRPC] Auth request from: " + username);

            // Simple validation - always succeed for testing
            boolean valid = username != null && !username.trim().isEmpty();

            AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();

            if (valid) {
                responseBuilder
                        .setSuccess(true)
                        .setMessage("Authentication successful")
                        .setToken("token_" + username);

                System.out.println("[gRPC] User authenticated: " + username);
            } else {
                responseBuilder
                        .setSuccess(false)
                        .setMessage("Invalid username");

                System.out.println("[gRPC] Auth failed: invalid username");
            }

            AuthResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("[gRPC] Exception in authenticate: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }

    @Override
    public void sendMessage(ChatMessage request,
            StreamObserver<MessageResponse> responseObserver) {
        try {
            String sender = request.getSender();
            String content = request.getContent();

            System.out.println("[gRPC] Message from " + sender + ": " + content);

            // Broadcast to all connected gRPC clients
            ChatMessage grpcMsg = ChatMessage.newBuilder()
                    .setSender(sender)
                    .setContent(content)
                    .setTimestamp(System.currentTimeMillis())
                    .setType(MessageType.BROADCAST)
                    .build();

            broadcastToGrpcClients(grpcMsg);

            MessageResponse response = MessageResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Message sent")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("[gRPC] Exception in sendMessage: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }

    @Override
    public void streamMessages(StreamRequest request,
            StreamObserver<ChatMessage> responseObserver) {
        try {
            String username = request.getUsername();

            activeStreams.put(username, responseObserver);

            System.out.println("[gRPC] Stream started for: " + username);

            ChatMessage welcome = ChatMessage.newBuilder()
                    .setSender("SERVER")
                    .setContent("Welcome to gRPC chat, " + username + "!")
                    .setTimestamp(System.currentTimeMillis())
                    .setType(MessageType.SYSTEM)
                    .build();

            responseObserver.onNext(welcome);

        } catch (Exception e) {
            System.err.println("[gRPC] Exception in streamMessages: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }

    @Override
    public void getOnlineUsers(Empty request,
            StreamObserver<UserList> responseObserver) {
        try {
            System.out.println("[gRPC] Get online users request");

            // Get usernames from context (may be empty)
            java.util.List<String> usernames = new java.util.ArrayList<>();

            try {
                usernames.addAll(context.getAllUsernames());
            } catch (Exception e) {
                System.err.println("[gRPC] Warning: Could not get usernames from context");
            }

            // If no users, add test data
            if (usernames.isEmpty()) {
                usernames.add("(No users connected)");
            }

            UserList userList = UserList.newBuilder()
                    .addAllUsernames(usernames)
                    .setCount(usernames.size())
                    .build();

            responseObserver.onNext(userList);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("[gRPC] Exception in getOnlineUsers: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }

    @Override
    public void sendPrivateMessage(PrivateMessage request,
            StreamObserver<MessageResponse> responseObserver) {
        try {
            String sender = request.getSender();
            String receiver = request.getReceiver();
            String content = request.getContent();

            System.out.println("[gRPC] PM from " + sender + " to " + receiver);

            StreamObserver<ChatMessage> receiverStream = activeStreams.get(receiver);

            if (receiverStream != null) {
                ChatMessage pm = ChatMessage.newBuilder()
                        .setSender(sender)
                        .setContent("[PM] " + content)
                        .setTimestamp(System.currentTimeMillis())
                        .setType(MessageType.PRIVATE)
                        .build();

                try {
                    receiverStream.onNext(pm);
                } catch (Exception e) {
                    System.err.println("[gRPC] Failed to send PM: " + e.getMessage());
                }
            }

            MessageResponse response = MessageResponse.newBuilder()
                    .setSuccess(receiverStream != null)
                    .setMessage(receiverStream != null ? "PM sent" : "User offline")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("[gRPC] Exception in sendPrivateMessage: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }

    private void broadcastToGrpcClients(ChatMessage message) {
        activeStreams.forEach((username, stream) -> {
            try {
                stream.onNext(message);
            } catch (Exception e) {
                System.err.println("[gRPC] Failed to send to " + username);
                activeStreams.remove(username);
            }
        });
    }
}