package server.http.dto;

// import common.protocol.MessageType;
import java.time.LocalDateTime;

/**
 * DTO cho Message
 */
public class MessageDTO {
    private String sender;
    private String receiver; // null for broadcast
    private String content;
    private String type; // "CHAT", "PRIVATE", etc.
    private LocalDateTime timestamp;

    // Constructors
    public MessageDTO() {
    }

    public MessageDTO(String sender, String receiver, String content,
            String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    // Getters/Setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}