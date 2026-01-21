package common.protocol;

/**
 * Enum định nghĩa các loại message trong protocol
 * Network Protocol Layer - Application Layer (OSI Model Layer 7)
 */
public enum MessageType {
    // Connection Management
    CONNECT,        // Client -> Server: Yêu cầu kết nối với username
    ACCEPT,         // Server -> Client: Chấp nhận kết nối
    REJECT,         // Server -> Client: Từ chối kết nối (duplicate username)
    
    // Messaging
    CHAT,           // Client -> Server -> All: Broadcast message công khai
    PRIVATE,        // Client -> Server -> Target: Tin nhắn riêng 1-1
    
    // Commands
    COMMAND,        // Client -> Server: Command hệ thống (/who, /join, etc.)
    
    // File Transfer
    FILE_META,      // Sender -> Receiver: Metadata file (name, size, checksum)
    FILE_CHUNK,     // Sender -> Receiver: Chunk dữ liệu file
    FILE_ACK,       // Receiver -> Sender: Xác nhận chunk received
    
    // Heartbeat/Keep-alive
    PING,           // Server -> Client: Heartbeat check
    PONG,           // Client -> Server: Heartbeat response
    
    // Disconnect
    DISCONNECT,     // Client -> Server: Graceful disconnect
    
    // Server Notifications
    SERVER          // Server -> Client: Thông báo hệ thống
}
