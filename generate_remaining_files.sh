#!/bin/bash

# Generate all remaining server and client files

# Server Services
cat > src/server/service/AuthService.java << 'EOF'
package server.service;

import server.core.ServerContext;
import java.net.Socket;

public class AuthService {
    private final ServerContext context;
    
    public AuthService(ServerContext context) {
        this.context = context;
    }
    
    public boolean authenticate(String username, Socket socket) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (username.length() > 20) {
            return false;
        }
        return context.getSession(username) == null;
    }
}
EOF

cat > src/server/service/MessageRouter.java << 'EOF'
package server.service;

import common.protocol.Message;
import common.protocol.MessageType;
import server.core.ServerContext;
import server.core.ClientHandler;
import java.io.IOException;

public class MessageRouter {
    private final ServerContext context;
    
    public MessageRouter(ServerContext context) {
        this.context = context;
    }
    
    public void route(Message msg, ClientHandler sender) {
        switch (msg.getType()) {
            case CHAT:
                handleBroadcast(msg);
                break;
            case PRIVATE:
                handlePrivate(msg);
                break;
            case COMMAND:
                handleCommand(msg, sender);
                break;
            case PONG:
                handlePong(msg);
                break;
            case FILE_META:
                context.getFileTransferService().handleFileMeta(msg);
                break;
            case FILE_CHUNK:
                context.getFileTransferService().handleFileChunk(msg);
                break;
            case FILE_ACK:
                context.getFileTransferService().handleFileAck(msg);
                break;
            case DISCONNECT:
                sender.disconnect();
                break;
        }
    }
    
    private void handleBroadcast(Message msg) {
        context.getHandlers().values().forEach(handler -> {
            try {
                handler.send(msg);
            } catch (IOException e) {
                // Client disconnected
            }
        });
    }
    
    private void handlePrivate(Message msg) {
        ClientHandler target = context.getHandler(msg.getReceiver());
        if (target != null) {
            try {
                target.send(msg);
            } catch (IOException e) {
                // Target disconnected
            }
        }
    }
    
    private void handleCommand(Message msg, ClientHandler sender) {
        String cmd = msg.getContent();
        try {
            if (cmd.startsWith("/who")) {
                String users = String.join(", ", context.getAllUsernames());
                Message response = new Message(MessageType.SERVER, "SERVER", 
                    sender.getUsername(), "Online users: " + users);
                sender.send(response);
            } else if (cmd.startsWith("/rooms")) {
                String rooms = String.join(", ", context.getAllRoomNames());
                Message response = new Message(MessageType.SERVER, "SERVER",
                    sender.getUsername(), "Available rooms: " + rooms);
                sender.send(response);
            }
        } catch (IOException e) {
            // Ignore
        }
    }
    
    private void handlePong(Message msg) {
        // Reset missed pings for this user
        String username = msg.getSender();
        var session = context.getSession(username);
        if (session != null) {
            session.updateActivity();
        }
    }
    
    public void broadcastServerMessage(String content) {
        Message msg = new Message(MessageType.SERVER, "SERVER", content);
        handleBroadcast(msg);
    }
}
EOF

cat > src/server/service/RoomService.java << 'EOF'
package server.service;

import server.core.ServerContext;
import common.model.ChatRoom;

public class RoomService {
    private final ServerContext context;
    
    public RoomService(ServerContext context) {
        this.context = context;
    }
    
    public boolean createRoom(String name, String description, String creator) {
        if (context.getRoom(name) != null) {
            return false;
        }
        ChatRoom room = new ChatRoom(name, description);
        room.addMember(creator);
        context.addRoom(name, room);
        return true;
    }
    
    public boolean joinRoom(String username, String roomName) {
        ChatRoom room = context.getRoom(roomName);
        if (room == null || room.isFull()) {
            return false;
        }
        var session = context.getSession(username);
        if (session != null) {
            session.joinRoom(roomName);
            return room.addMember(username);
        }
        return false;
    }
    
    public boolean leaveRoom(String username, String roomName) {
        ChatRoom room = context.getRoom(roomName);
        if (room != null) {
            var session = context.getSession(username);
            if (session != null) {
                session.leaveRoom(roomName);
            }
            return room.removeMember(username);
        }
        return false;
    }
}
EOF

cat > src/server/service/FileTransferService.java << 'EOF'
package server.service;

import server.core.ServerContext;
import common.protocol.Message;

public class FileTransferService {
    private final ServerContext context;
    
    public FileTransferService(ServerContext context) {
        this.context = context;
    }
    
    public void handleFileMeta(Message msg) {
        // Forward file offer to receiver
        var handler = context.getHandler(msg.getReceiver());
        if (handler != null) {
            try {
                handler.send(msg);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    public void handleFileChunk(Message msg) {
        // Forward chunk to receiver
        var handler = context.getHandler(msg.getReceiver());
        if (handler != null) {
            try {
                handler.send(msg);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    public void handleFileAck(Message msg) {
        // Forward ACK back to sender
        var handler = context.getHandler(msg.getReceiver());
        if (handler != null) {
            try {
                handler.send(msg);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
EOF

cat > src/server/monitor/HeartbeatService.java << 'EOF'
package server.monitor;

import server.core.ServerContext;
import common.protocol.Message;
import common.protocol.MessageType;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatService {
    private final ServerContext context;
    private ScheduledExecutorService scheduler;
    private volatile boolean running;
    
    public HeartbeatService(ServerContext context) {
        this.context = context;
    }
    
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        running = true;
        
        scheduler.scheduleAtFixedRate(this::sendPings, 
            context.getConfig().getPingInterval(),
            context.getConfig().getPingInterval(),
            TimeUnit.MILLISECONDS);
    }
    
    private void sendPings() {
        context.getHandlers().forEach((username, handler) -> {
            try {
                var session = handler.getSession();
                if (session != null) {
                    if (session.getMissedPings() >= context.getConfig().getMaxMissedPings()) {
                        System.out.println("[HEARTBEAT] Timeout for " + username);
                        handler.disconnect();
                        return;
                    }
                    
                    Message ping = new Message(MessageType.PING, "SERVER", username, "");
                    handler.send(ping);
                    session.incrementMissedPings();
                }
            } catch (Exception e) {
                // Ignore
            }
        });
    }
    
    public void stop() {
        running = false;
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
EOF

cat > src/server/security/SSLConfig.java << 'EOF'
package server.security;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SSLConfig {
    
    public static SSLServerSocketFactory createServerSocketFactory(
            String keystorePath, String password) throws Exception {
        
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, password.toCharArray());
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        
        return sslContext.getServerSocketFactory();
    }
    
    public static SSLSocketFactory createClientSocketFactory() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, getTrustAllManagers(), null);
        return sslContext.getSocketFactory();
    }
    
    private static TrustManager[] getTrustAllManagers() {
        return new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
    }
}
EOF

echo "Server files generated successfully!"

