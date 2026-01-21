package server.config;

import common.protocol.ProtocolConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ServerConfig - Configuration loader and holder
 * Loads configuration từ file hoặc defaults
 * 
 * Design Pattern: Configuration Object Pattern
 */
public class ServerConfig {
    
    // Network settings
    private int port;
    private int backlog;
    private int socketTimeout;
    
    // SSL settings
    private boolean sslEnabled;
    private String keystorePath;
    private String keystorePassword;
    
    // Thread pool settings
    private int maxClients;
    
    // Heartbeat settings
    private int pingInterval;
    private int pongTimeout;
    private int maxMissedPings;
    
    // File transfer settings
    private int chunkSize;
    private long maxFileSize;
    
    /**
     * Load configuration from file
     */
    public static ServerConfig loadFromFile(String configPath) throws IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(configPath)) {
            props.load(input);
        }
        
        ServerConfig config = new ServerConfig();
        config.port = Integer.parseInt(props.getProperty("server.port", 
                String.valueOf(ProtocolConstants.DEFAULT_PORT)));
        config.backlog = Integer.parseInt(props.getProperty("server.backlog", 
                String.valueOf(ProtocolConstants.SOCKET_BACKLOG)));
        config.socketTimeout = Integer.parseInt(props.getProperty("server.socket.timeout", 
                String.valueOf(ProtocolConstants.SOCKET_TIMEOUT)));
        
        config.sslEnabled = Boolean.parseBoolean(props.getProperty("ssl.enabled", "false"));
        config.keystorePath = props.getProperty("ssl.keystore.path", "server.jks");
        config.keystorePassword = props.getProperty("ssl.keystore.password", "password");
        
        config.maxClients = Integer.parseInt(props.getProperty("server.max.clients", 
                String.valueOf(ProtocolConstants.MAX_CLIENTS)));
        
        config.pingInterval = Integer.parseInt(props.getProperty("heartbeat.ping.interval", 
                String.valueOf(ProtocolConstants.PING_INTERVAL)));
        config.pongTimeout = Integer.parseInt(props.getProperty("heartbeat.pong.timeout", 
                String.valueOf(ProtocolConstants.PONG_TIMEOUT)));
        config.maxMissedPings = Integer.parseInt(props.getProperty("heartbeat.max.missed", 
                String.valueOf(ProtocolConstants.MAX_MISSED_PINGS)));
        
        config.chunkSize = Integer.parseInt(props.getProperty("file.chunk.size", 
                String.valueOf(ProtocolConstants.CHUNK_SIZE)));
        config.maxFileSize = Long.parseLong(props.getProperty("file.max.size", 
                String.valueOf(ProtocolConstants.MAX_FILE_SIZE)));
        
        return config;
    }
    
    /**
     * Create default configuration
     */
    public static ServerConfig createDefault() {
        ServerConfig config = new ServerConfig();
        config.port = ProtocolConstants.DEFAULT_PORT;
        config.backlog = ProtocolConstants.SOCKET_BACKLOG;
        config.socketTimeout = ProtocolConstants.SOCKET_TIMEOUT;
        config.sslEnabled = false;
        config.keystorePath = "server.jks";
        config.keystorePassword = "password";
        config.maxClients = ProtocolConstants.MAX_CLIENTS;
        config.pingInterval = ProtocolConstants.PING_INTERVAL;
        config.pongTimeout = ProtocolConstants.PONG_TIMEOUT;
        config.maxMissedPings = ProtocolConstants.MAX_MISSED_PINGS;
        config.chunkSize = ProtocolConstants.CHUNK_SIZE;
        config.maxFileSize = ProtocolConstants.MAX_FILE_SIZE;
        return config;
    }
    
    // Getters
    public int getPort() { return port; }
    public int getBacklog() { return backlog; }
    public int getSocketTimeout() { return socketTimeout; }
    public boolean isSslEnabled() { return sslEnabled; }
    public String getKeystorePath() { return keystorePath; }
    public String getKeystorePassword() { return keystorePassword; }
    public int getMaxClients() { return maxClients; }
    public int getPingInterval() { return pingInterval; }
    public int getPongTimeout() { return pongTimeout; }
    public int getMaxMissedPings() { return maxMissedPings; }
    public int getChunkSize() { return chunkSize; }
    public long getMaxFileSize() { return maxFileSize; }
    
    // Setters
    public void setPort(int port) { this.port = port; }
    public void setSslEnabled(boolean sslEnabled) { this.sslEnabled = sslEnabled; }
    public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
    public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }
    
    @Override
    public String toString() {
        return String.format("ServerConfig[port=%d, ssl=%s, maxClients=%d]",
                port, sslEnabled, maxClients);
    }
}
