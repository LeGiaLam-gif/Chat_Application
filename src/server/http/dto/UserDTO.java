package server.http.dto;

import common.model.UserSession;
import java.time.LocalDateTime;

public class UserDTO {
    private String username;
    private String status;
    private LocalDateTime connectedAt;

    // ❌ SAI - UserSession returns long, DTO has int
    // private int messagesSent;
    // private int messagesReceived;

    // ✅ ĐÚNG - Match với UserSession
    private long messagesSent; // ← THAY ĐỔI
    private long messagesReceived; // ← THAY ĐỔI

    private String[] rooms;

    public static UserDTO fromSession(UserSession session) {
        UserDTO dto = new UserDTO();
        dto.username = session.getUsername();
        dto.status = "online";
        dto.connectedAt = session.getConnectedAt();
        dto.messagesSent = session.getMessagesSent(); // long → long ✅
        dto.messagesReceived = session.getMessagesReceived(); // long → long ✅
        dto.rooms = session.getRooms().toArray(new String[0]);
        return dto;
    }

    // Getters/Setters với long
    public long getMessagesSent() {
        return messagesSent;
    }

    public void setMessagesSent(long messagesSent) {
        this.messagesSent = messagesSent;
    }

    public long getMessagesReceived() {
        return messagesReceived;
    }

    public void setMessagesReceived(long messagesReceived) {
        this.messagesReceived = messagesReceived;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    public String[] getRooms() {
        return rooms;
    }

    public void setRooms(String[] rooms) {
        this.rooms = rooms;
    }
}