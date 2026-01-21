package common.protocol;

/**
 * Protocol constants và network configuration
 * Centralized configuration cho network parameters
 */
public class ProtocolConstants {
    
    // Network Settings
    public static final int DEFAULT_PORT = 5000;
    public static final int SSL_PORT = 5443;
    public static final String DEFAULT_HOST = "localhost";
    
    // Socket Timeouts (milliseconds)
    public static final int SOCKET_TIMEOUT = 30000;          // 30 seconds - read timeout
    public static final int CONNECT_TIMEOUT = 10000;         // 10 seconds - connection timeout
    public static final int SOCKET_BACKLOG = 50;             // Max queued connections
    
    // Buffer Sizes
    public static final int BUFFER_SIZE = 8192;              // 8KB buffer cho I/O operations
    public static final int MAX_MESSAGE_SIZE = 1024 * 1024;  // 1MB max message size
    
    // File Transfer
    public static final int CHUNK_SIZE = 64 * 1024;          // 64KB chunks (optimal for TCP window)
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024L; // 100MB max file size
    public static final int FILE_TRANSFER_TIMEOUT = 60000;   // 60 seconds per chunk
    public static final int MAX_RETRY_ATTEMPTS = 3;          // Retry failed chunks 3 times
    
    // Heartbeat / Keep-alive
    public static final int PING_INTERVAL = 30000;           // Send PING every 30 seconds
    public static final int PONG_TIMEOUT = 10000;            // Wait 10 seconds for PONG response
    public static final int MAX_MISSED_PINGS = 3;            // Disconnect after 3 missed PONGs
    
    // Thread Pool
    public static final int CORE_POOL_SIZE = 10;             // Minimum threads
    public static final int MAX_POOL_SIZE = 100;             // Maximum threads
    public static final int THREAD_KEEP_ALIVE_TIME = 60;     // Seconds for idle thread
    
    // Session Management
    public static final int MAX_CLIENTS = 1000;              // Maximum concurrent clients
    public static final int SESSION_TIMEOUT = 300000;        // 5 minutes idle timeout
    
    // Logging
    public static final String LOG_DIR = "logs";
    public static final String SERVER_LOG = "server.log";
    public static final String TRANSFER_LOG = "transfer.log";
    
    // Protocol Version
    public static final String PROTOCOL_VERSION = "2.0";
    
    // Server Info
    public static final String SERVER_NAME = "ChatAppV2 Server";
    public static final String SERVER_VERSION = "2.0.0";
    
    /**
     * Private constructor - utility class không cần instantiate
     */
    private ProtocolConstants() {
        throw new AssertionError("Cannot instantiate ProtocolConstants");
    }
}
