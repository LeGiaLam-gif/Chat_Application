package common.protocol;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Message DTO (Data Transfer Object) cho network communication
 * Sử dụng Java Serialization để truyền qua ObjectOutputStream/ObjectInputStream
 * 
 * Design Pattern: DTO Pattern
 * Network Concept: Application Layer Protocol Message
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Core fields
    private MessageType type;
    private String sender;
    private String receiver;      // null for broadcast messages
    private String content;
    private LocalDateTime timestamp;
    
    // Extensible metadata field - allows adding new fields without breaking compatibility
    // Design Pattern: Key-Value Metadata Pattern
    private Map<String, Object> metadata;
    
    /**
     * Constructor chính
     */
    public Message(MessageType type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }
    
    /**
     * Convenience constructor cho broadcast messages (receiver = null)
     */
    public Message(MessageType type, String sender, String content) {
        this(type, sender, null, content);
    }
    
    // Getters
    public MessageType getType() { return type; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, Object> getMetadata() { return metadata; }
    
    // Setters
    public void setType(MessageType type) { this.type = type; }
    public void setContent(String content) { this.content = content; }
    
    // Metadata helpers for File Transfer
    
    /**
     * Set file metadata cho FILE_META message
     * @param filename Tên file
     * @param fileSize Kích thước file (bytes)
     * @param checksum MD5 hash của toàn bộ file
     */
    public void setFileMetadata(String filename, long fileSize, String checksum) {
        metadata.put("filename", filename);
        metadata.put("fileSize", fileSize);
        metadata.put("checksum", checksum);
    }
    
    public String getFilename() {
        return (String) metadata.get("filename");
    }
    
    public Long getFileSize() {
        return (Long) metadata.get("fileSize");
    }
    
    public String getChecksum() {
        return (String) metadata.get("checksum");
    }
    
    /**
     * Set chunk data cho FILE_CHUNK message
     * @param sequence Số thứ tự chunk (0-based)
     * @param data Dữ liệu chunk (64KB)
     */
    public void setChunkData(int sequence, byte[] data) {
        metadata.put("sequence", sequence);
        metadata.put("data", data);
    }
    
    public Integer getSequence() {
        return (Integer) metadata.get("sequence");
    }
    
    public byte[] getData() {
        return (byte[]) metadata.get("data");
    }
    
    /**
     * Set room/channel info
     */
    public void setRoom(String roomName) {
        metadata.put("room", roomName);
    }
    
    public String getRoom() {
        return (String) metadata.get("room");
    }
    
    /**
     * Generic metadata accessors
     */
    public void putMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    @Override
    public String toString() {
        return String.format("Message[type=%s, sender=%s, receiver=%s, content=%s, timestamp=%s]",
                type, sender, receiver, 
                content.length() > 50 ? content.substring(0, 50) + "..." : content,
                timestamp);
    }
}
