package server.core;

import common.protocol.Message;
import common.protocol.MessageType;
import common.model.UserSession;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * ClientHandler - Worker thread xử lý I/O cho mỗi client connection
 * 
 * Threading Model: Mỗi ClientHandler chạy trong một thread riêng từ thread pool
 * Lifecycle:
 * 1. Setup I/O streams
 * 2. Handle CONNECT handshake
 * 3. Main message loop
 * 4. Cleanup on disconnect
 */
public class ClientHandler implements Runnable {
    
    private final Socket socket;
    private final ServerContext context;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private UserSession session;
    private volatile boolean running;
    
    public ClientHandler(Socket socket, ServerContext context) {
        this.socket = socket;
        this.context = context;
        this.running = false;
    }
    
    @Override
    public void run() {
        try {
            // Setup I/O streams
            // CRITICAL: Create OutputStream FIRST and flush() để tránh deadlock
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            // Set socket timeout
            socket.setSoTimeout(context.getConfig().getSocketTimeout());
            
            // Handle CONNECT handshake
            if (!handleConnect()) {
                return; // Connection rejected
            }
            
            running = true;
            System.out.println("[AUTH] User '" + username + "' authenticated successfully");
            
            // Main message processing loop
            while (running) {
                try {
                    Message msg = (Message) in.readObject();
                    
                    // Update session activity
                    session.updateActivity();
                    session.incrementMessagesReceived();
                    
                    // Route message to appropriate handler
                    context.getMessageRouter().route(msg, this);
                    
                } catch (ClassNotFoundException e) {
                    System.err.println("[ERROR] Invalid message class from " + username);
                    break;
                }
            }
            
        } catch (EOFException e) {
            // Client disconnected abruptly
            System.out.println("[DISCONNECT] Client disconnected abruptly: " + username);
        } catch (SocketException e) {
            // Socket closed
            System.out.println("[DISCONNECT] Socket closed: " + username);
        } catch (IOException e) {
            System.err.println("[ERROR] I/O error for user " + username + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    /**
     * Handle CONNECT handshake protocol
     * 1. Receive CONNECT message with username
     * 2. Validate username via AuthService
     * 3. Send ACCEPT or REJECT
     * 
     * @return true if connection accepted, false if rejected
     */
    private boolean handleConnect() {
        try {
            // Read CONNECT message
            Message connectMsg = (Message) in.readObject();
            
            if (connectMsg.getType() != MessageType.CONNECT) {
                sendReject("Invalid handshake - expected CONNECT message");
                return false;
            }
            
            username = connectMsg.getSender();
            
            // Validate username via AuthService
            if (!context.getAuthService().authenticate(username, socket)) {
                sendReject("Username '" + username + "' is already taken");
                return false;
            }
            
            // Create session
            session = new UserSession(username, socket);
            context.addSession(username, session);
            context.addHandler(username, this);
            
            // Send ACCEPT
            sendAccept();
            
            // Broadcast join notification
            context.getMessageRouter().broadcastServerMessage(
                username + " joined the chat"
            );
            
            return true;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[ERROR] Handshake failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send ACCEPT message to client
     */
    private void sendAccept() throws IOException {
        Message accept = new Message(MessageType.ACCEPT, "SERVER", username, "Welcome!");
        accept.putMetadata("serverVersion", common.protocol.ProtocolConstants.SERVER_VERSION);
        send(accept);
    }
    
    /**
     * Send REJECT message and close connection
     */
    private void sendReject(String reason) {
        try {
            Message reject = new Message(MessageType.REJECT, "SERVER", null, reason);
            send(reject);
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
    
    /**
     * Send message to this client (synchronized for thread safety)
     */
    public synchronized void send(Message msg) throws IOException {
        if (out != null && !socket.isClosed()) {
            out.writeObject(msg);
            out.flush();
            
            if (session != null) {
                session.incrementMessagesSent();
            }
        }
    }
    
    /**
     * Disconnect client gracefully
     */
    public void disconnect() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore
        }
    }
    
    /**
     * Cleanup resources on disconnect
     */
    private void cleanup() {
        running = false;
        
        if (username != null) {
            System.out.println("[DISCONNECT] Cleaning up for user: " + username);
            
            // Remove from context
            context.removeSession(username);
            
            // Leave all rooms
            if (session != null) {
                for (String roomName : session.getRooms()) {
                    context.getRoomService().leaveRoom(username, roomName);
                }
            }
            
            // Broadcast leave notification
            context.getMessageRouter().broadcastServerMessage(
                username + " left the chat"
            );
        }
        
        // Close streams
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
    
    // Getters
    public String getUsername() {
        return username;
    }
    
    public UserSession getSession() {
        return session;
    }
    
    public boolean isRunning() {
        return running;
    }
}
