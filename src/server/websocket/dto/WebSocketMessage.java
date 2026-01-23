package server.websocket.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class WebSocketMessage {
    private String type;
    private Map<String, Object> data;
    private String timestamp; // ← Changed from LocalDateTime to String

    // Custom Gson with LocalDateTime adapter
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .create();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WebSocketMessage() {
        this.timestamp = LocalDateTime.now().format(formatter);
        this.data = new HashMap<>();
    }

    public WebSocketMessage(String type) {
        this();
        this.type = type;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // Helper methods
    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    // JSON serialization
    public String toJson() {
        return gson.toJson(this);
    }

    public static WebSocketMessage fromJson(String json) {
        return gson.fromJson(json, WebSocketMessage.class);
    }

    // Factory methods
    public static WebSocketMessage auth(String username) {
        WebSocketMessage msg = new WebSocketMessage("auth");
        msg.put("username", username);
        return msg;
    }

    public static WebSocketMessage chat(String sender, String content) {
        WebSocketMessage msg = new WebSocketMessage("chat");
        msg.put("sender", sender);
        msg.put("content", content);
        return msg;
    }

    public static WebSocketMessage privateMsg(String sender, String receiver, String content) {
        WebSocketMessage msg = new WebSocketMessage("private");
        msg.put("sender", sender);
        msg.put("receiver", receiver);
        msg.put("content", content);
        return msg;
    }

    public static WebSocketMessage userJoined(String username) {
        WebSocketMessage msg = new WebSocketMessage("user_joined");
        msg.put("username", username);
        return msg;
    }

    public static WebSocketMessage userLeft(String username) {
        WebSocketMessage msg = new WebSocketMessage("user_left");
        msg.put("username", username);
        return msg;
    }

    public static WebSocketMessage userList(String[] users) {
        WebSocketMessage msg = new WebSocketMessage("user_list");
        msg.put("users", users);
        return msg;
    }

    public static WebSocketMessage error(String message) {
        WebSocketMessage msg = new WebSocketMessage("error");
        msg.put("message", message);
        return msg;
    }

    // Custom serializer for LocalDateTime
    private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc,
                JsonSerializationContext context) {
            return new JsonPrimitive(src.format(formatter));
        }
    }
}