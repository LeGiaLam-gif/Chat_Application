package server.core;

import common.model.UserSession;
import common.model.ChatRoom;
import server.service.*;
import server.monitor.HeartbeatService;
import server.config.ServerConfig;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ServerContext - Container quản lý toàn bộ server state và services
 * 
 * Design Patterns:
 * - Singleton Pattern: Single instance per server
 * - Service Locator Pattern: Centralized access to services
 * - Dependency Injection: Services receive context in constructor
 * 
 * Thread Safety: All collections are ConcurrentHashMap for thread-safe access
 */
public class ServerContext {
    
    // Configuration
    private final ServerConfig config;
    
    // Thread-safe collections for concurrent access by multiple ClientHandler threads
    private final Map<String, UserSession> sessions;          // username -> session
    private final Map<String, ClientHandler> handlers;        // username -> handler
    private final Map<String, ChatRoom> rooms;                // roomName -> room
    
    // Thread pool for client handlers
    private final ExecutorService threadPool;
    
    // Services - Business logic layer
    private final AuthService authService;
    private final MessageRouter messageRouter;
    private final RoomService roomService;
    private final FileTransferService fileTransferService;
    private final HeartbeatService heartbeatService;
    
    // Server state
    private volatile boolean running;
    
    /**
     * Constructor - Initialize server context with configuration
     */
    public ServerContext(ServerConfig config) {
        this.config = config;
        
        // Initialize thread-safe collections
        this.sessions = new ConcurrentHashMap<>();
        this.handlers = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        
        // Create thread pool for handling client connections
        // CachedThreadPool: Creates threads on demand, reuses idle threads
        this.threadPool = Executors.newCachedThreadPool();
        
        // Initialize services
        this.authService = new AuthService(this);
        this.messageRouter = new MessageRouter(this);
        this.roomService = new RoomService(this);
        this.fileTransferService = new FileTransferService(this);
        this.heartbeatService = new HeartbeatService(this);
        
        // Create default lobby room
        ChatRoom lobby = new ChatRoom("lobby", "Default public chat room");
        rooms.put("lobby", lobby);
        
        this.running = false;
    }
    
    // Configuration
    public ServerConfig getConfig() {
        return config;
    }
    
    // Service getters
    public AuthService getAuthService() {
        return authService;
    }
    
    public MessageRouter getMessageRouter() {
        return messageRouter;
    }
    
    public RoomService getRoomService() {
        return roomService;
    }
    
    public FileTransferService getFileTransferService() {
        return fileTransferService;
    }
    
    public HeartbeatService getHeartbeatService() {
        return heartbeatService;
    }
    
    public ExecutorService getThreadPool() {
        return threadPool;
    }
    
    // Session Management
    
    /**
     * Add new session - Atomic operation to prevent duplicate usernames
     * @return true if added, false if username already exists
     */
    public boolean addSession(String username, UserSession session) {
        // putIfAbsent is atomic - returns null if successfully added
        return sessions.putIfAbsent(username, session) == null;
    }
    
    public UserSession getSession(String username) {
        return sessions.get(username);
    }
    
    public void removeSession(String username) {
        sessions.remove(username);
        handlers.remove(username);
    }
    
    public Set<String> getAllUsernames() {
        return sessions.keySet();
    }
    
    public int getOnlineUserCount() {
        return sessions.size();
    }
    
    public Map<String, UserSession> getSessions() {
        return sessions;
    }
    
    // Handler Management
    
    public void addHandler(String username, ClientHandler handler) {
        handlers.put(username, handler);
    }
    
    public ClientHandler getHandler(String username) {
        return handlers.get(username);
    }
    
    public Map<String, ClientHandler> getHandlers() {
        return handlers;
    }
    
    // Room Management
    
    public void addRoom(String name, ChatRoom room) {
        rooms.put(name, room);
    }
    
    public ChatRoom getRoom(String name) {
        return rooms.get(name);
    }
    
    public void removeRoom(String name) {
        // Cannot remove lobby
        if (!"lobby".equals(name)) {
            rooms.remove(name);
        }
    }
    
    public Set<String> getAllRoomNames() {
        return rooms.keySet();
    }
    
    public Map<String, ChatRoom> getRooms() {
        return rooms;
    }
    
    // Server lifecycle
    
    public void start() {
        running = true;
        heartbeatService.start();
    }
    
    public void stop() {
        running = false;
        
        // Stop heartbeat service
        heartbeatService.stop();
        
        // Shutdown thread pool gracefully
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Get server statistics for monitoring
     */
    public ServerStats getStats() {
        return new ServerStats(
            sessions.size(),
            rooms.size(),
            getTotalMessagesSent(),
            getTotalBytesTransferred()
        );
    }
    
    private long getTotalMessagesSent() {
        return sessions.values().stream()
            .mapToLong(UserSession::getMessagesSent)
            .sum();
    }
    
    private long getTotalBytesTransferred() {
        return sessions.values().stream()
            .mapToLong(s -> s.getBytesSent() + s.getBytesReceived())
            .sum();
    }
    
    /**
     * Inner class for server statistics
     */
    public static class ServerStats {
        public final int onlineUsers;
        public final int activeRooms;
        public final long totalMessagesSent;
        public final long totalBytesTransferred;
        
        public ServerStats(int onlineUsers, int activeRooms, 
                          long totalMessagesSent, long totalBytesTransferred) {
            this.onlineUsers = onlineUsers;
            this.activeRooms = activeRooms;
            this.totalMessagesSent = totalMessagesSent;
            this.totalBytesTransferred = totalBytesTransferred;
        }
        
        @Override
        public String toString() {
            return String.format("ServerStats[users=%d, rooms=%d, messages=%d, bytes=%d]",
                    onlineUsers, activeRooms, totalMessagesSent, totalBytesTransferred);
        }
    }
}
