package common.model;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * UserSession - Model class đại diện cho một user session trên server
 * Chứa thông tin về user và trạng thái connection
 * 
 * Thread Safety: Fields được access bởi nhiều threads nên cần synchronization
 */
public class UserSession {
    private final String username;
    private final Socket socket;
    private final LocalDateTime connectedAt;
    private LocalDateTime lastActivity;
    
    // Rooms that user has joined
    private final Set<String> rooms;
    
    // Session state
    private volatile boolean active;
    private int missedPings;
    
    // Statistics
    private long messagesSent;
    private long messagesReceived;
    private long bytesSent;
    private long bytesReceived;
    
    public UserSession(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
        this.connectedAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.rooms = new HashSet<>();
        this.active = true;
        this.missedPings = 0;
        
        // Add to default "lobby" room
        this.rooms.add("lobby");
    }
    
    // Getters
    public String getUsername() { return username; }
    public Socket getSocket() { return socket; }
    public LocalDateTime getConnectedAt() { return connectedAt; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public Set<String> getRooms() { return new HashSet<>(rooms); } // Return copy for thread safety
    public boolean isActive() { return active; }
    public int getMissedPings() { return missedPings; }
    
    // Session management
    public synchronized void updateActivity() {
        this.lastActivity = LocalDateTime.now();
        this.missedPings = 0; // Reset on any activity
    }
    
    public synchronized void incrementMissedPings() {
        this.missedPings++;
    }
    
    public synchronized void deactivate() {
        this.active = false;
    }
    
    // Room management
    public synchronized boolean joinRoom(String roomName) {
        return rooms.add(roomName);
    }
    
    public synchronized boolean leaveRoom(String roomName) {
        // Cannot leave lobby
        if ("lobby".equals(roomName)) {
            return false;
        }
        return rooms.remove(roomName);
    }
    
    public synchronized boolean isInRoom(String roomName) {
        return rooms.contains(roomName);
    }
    
    // Statistics
    public synchronized void incrementMessagesSent() {
        this.messagesSent++;
    }
    
    public synchronized void incrementMessagesReceived() {
        this.messagesReceived++;
    }
    
    public synchronized void addBytesSent(long bytes) {
        this.bytesSent += bytes;
    }
    
    public synchronized void addBytesReceived(long bytes) {
        this.bytesReceived += bytes;
    }
    
    public synchronized long getMessagesSent() { return messagesSent; }
    public synchronized long getMessagesReceived() { return messagesReceived; }
    public synchronized long getBytesSent() { return bytesSent; }
    public synchronized long getBytesReceived() { return bytesReceived; }
    
    /**
     * Get remote address for logging
     */
    public String getRemoteAddress() {
        return socket.getRemoteSocketAddress().toString();
    }
    
    /**
     * Check if session has timed out (no activity)
     */
    public boolean hasTimedOut(int timeoutMillis) {
        long idleTime = System.currentTimeMillis() - 
                       java.sql.Timestamp.valueOf(lastActivity).getTime();
        return idleTime > timeoutMillis;
    }
    
    @Override
    public String toString() {
        return String.format("UserSession[username=%s, active=%s, rooms=%s, connected=%s]",
                username, active, rooms, connectedAt);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSession)) return false;
        UserSession that = (UserSession) o;
        return username.equals(that.username);
    }
    
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
