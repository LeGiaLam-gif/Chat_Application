package server.websocket;

import org.java_websocket.WebSocket;
import server.websocket.dto.WebSocketMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebSocketClient {

    private final WebSocket connection;
    private String username;
    private String connectedAt; // ← Changed to String
    private int messagesSent;
    private int messagesReceived;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WebSocketClient(WebSocket connection) {
        this.connection = connection;
        this.connectedAt = LocalDateTime.now().format(formatter);
        this.messagesSent = 0;
        this.messagesReceived = 0;
    }

    public WebSocket getConnection() {
        return connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConnectedAt() {
        return connectedAt;
    }

    public int getMessagesSent() {
        return messagesSent;
    }

    public int getMessagesReceived() {
        return messagesReceived;
    }

    public void send(WebSocketMessage message) {
        if (connection.isOpen()) {
            connection.send(message.toJson());
            messagesSent++;
        }
    }

    public void send(String message) {
        if (connection.isOpen()) {
            connection.send(message);
            messagesSent++;
        }
    }

    public void incrementMessagesReceived() {
        messagesReceived++;
    }

    public boolean isAuthenticated() {
        return username != null && !username.isEmpty();
    }

    public String getRemoteAddress() {
        return connection.getRemoteSocketAddress().toString();
    }

    public boolean isConnected() {
        return connection.isOpen();
    }

    public void disconnect() {
        if (connection.isOpen()) {
            connection.close();
        }
    }
}