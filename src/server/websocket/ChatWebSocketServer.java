package server.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import server.core.ServerContext;
import server.websocket.dto.WebSocketMessage;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketServer extends WebSocketServer {

    private final ServerContext context;
    private final Map<WebSocket, WebSocketClient> clients;

    public ChatWebSocketServer(int port, ServerContext context) {
        super(new InetSocketAddress(port));
        this.context = context;
        this.clients = new ConcurrentHashMap<>();
        setConnectionLostTimeout(30);
    }

    @Override
    public void onStart() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  WebSocket Server Started");
        System.out.println("  Port: " + getPort());
        System.out.println("  URL: ws://localhost:" + getPort());
        System.out.println("═══════════════════════════════════════");
        System.out.println();
        setReuseAddr(true);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String clientAddress = conn.getRemoteSocketAddress().toString();
        System.out.println("[WS] New connection: " + clientAddress);

        WebSocketClient client = new WebSocketClient(conn);
        clients.put(conn, client);

        WebSocketMessage welcome = new WebSocketMessage("system");
        welcome.put("message", "Welcome! Please authenticate.");
        client.send(welcome);

        System.out.println("[WS] Total: " + clients.size());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        WebSocketClient client = clients.get(conn);
        if (client == null)
            return;

        try {
            WebSocketMessage msg = WebSocketMessage.fromJson(message);
            client.incrementMessagesReceived();

            System.out.println("[WS] Type: " + msg.getType() +
                    " from " + (client.isAuthenticated() ? client.getUsername() : "anonymous"));

            handleMessage(client, msg);

        } catch (Exception e) {
            System.err.println("[WS ERROR] " + e.getMessage());
            client.send(WebSocketMessage.error("Invalid format"));
        }
    }

    private void handleMessage(WebSocketClient client, WebSocketMessage msg) {
        String type = msg.getType();
        if (type == null) {
            client.send(WebSocketMessage.error("Type required"));
            return;
        }

        switch (type) {
            case "auth":
                handleAuth(client, msg);
                break;
            case "chat":
                handleChat(client, msg);
                break;
            case "private":
                handlePrivate(client, msg);
                break;
            case "ping":
                handlePing(client);
                break;
            case "typing":
                handleTyping(client, msg);
                break;
            default:
                client.send(WebSocketMessage.error("Unknown type"));
        }
    }

    private void handleAuth(WebSocketClient client, WebSocketMessage msg) {
        String username = msg.getString("username");

        if (username == null || username.trim().isEmpty()) {
            client.send(WebSocketMessage.error("Username required"));
            return;
        }

        username = username.trim();

        if (username.length() < 3 || username.length() > 20) {
            client.send(WebSocketMessage.error("Username 3-20 chars"));
            return;
        }

        for (WebSocketClient c : clients.values()) {
            if (username.equalsIgnoreCase(c.getUsername())) {
                client.send(WebSocketMessage.error("Username taken"));
                return;
            }
        }

        client.setUsername(username);
        System.out.println("[WS AUTH] " + username);

        WebSocketMessage success = new WebSocketMessage("auth_success");
        success.put("username", username);
        success.put("message", "Welcome, " + username + "!");
        client.send(success);

        client.send(getUserListMessage());
        broadcast(WebSocketMessage.userJoined(username), client);
        broadcastUserList();
    }

    private void handleChat(WebSocketClient client, WebSocketMessage msg) {
        if (!client.isAuthenticated()) {
            client.send(WebSocketMessage.error("Login first"));
            return;
        }

        String content = msg.getString("content");

        if (content == null || content.trim().isEmpty()) {
            client.send(WebSocketMessage.error("Content required"));
            return;
        }

        content = content.trim();

        if (content.length() > 1000) {
            client.send(WebSocketMessage.error("Too long"));
            return;
        }

        WebSocketMessage chat = WebSocketMessage.chat(
                client.getUsername(), content);

        System.out.println("[WS CHAT] " + client.getUsername() +
                ": " + content);

        broadcastToAuthenticated(chat);
    }

    private void handlePrivate(WebSocketClient client, WebSocketMessage msg) {
        if (!client.isAuthenticated()) {
            client.send(WebSocketMessage.error("Login first"));
            return;
        }

        String receiver = msg.getString("receiver");
        String content = msg.getString("content");

        if (receiver == null || content == null) {
            client.send(WebSocketMessage.error("Receiver & content required"));
            return;
        }

        content = content.trim();

        if (content.isEmpty()) {
            client.send(WebSocketMessage.error("Content required"));
            return;
        }

        WebSocketClient target = findClientByUsername(receiver);
        if (target == null) {
            client.send(WebSocketMessage.error("User not found"));
            return;
        }

        WebSocketMessage pm = WebSocketMessage.privateMsg(
                client.getUsername(), receiver, content);

        System.out.println("[WS PM] " + client.getUsername() +
                " → " + receiver);

        target.send(pm);

        WebSocketMessage confirm = new WebSocketMessage("private_sent");
        confirm.put("receiver", receiver);
        confirm.put("content", content);
        client.send(confirm);
    }

    private void handlePing(WebSocketClient client) {
        WebSocketMessage pong = new WebSocketMessage("pong");
        pong.put("timestamp", System.currentTimeMillis());
        client.send(pong);
    }

    private void handleTyping(WebSocketClient client, WebSocketMessage msg) {
        if (!client.isAuthenticated())
            return;

        boolean isTyping = msg.get("isTyping") != null &&
                (boolean) msg.get("isTyping");

        WebSocketMessage typingMsg = new WebSocketMessage("typing");
        typingMsg.put("username", client.getUsername());
        typingMsg.put("isTyping", isTyping);

        broadcast(typingMsg, client);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        WebSocketClient client = clients.remove(conn);

        if (client != null) {
            String username = client.getUsername();
            System.out.println("[WS] Closed: " +
                    (username != null ? username : "anonymous"));

            if (client.isAuthenticated()) {
                System.out.println("[WS] User left: " + username);
                broadcast(WebSocketMessage.userLeft(username), null);
                broadcastUserList();
            }

            System.out.println("[WS] Total: " + clients.size());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[WS ERROR] " + ex.getMessage());

        if (conn != null) {
            WebSocketClient client = clients.get(conn);
            if (client != null) {
                client.send(WebSocketMessage.error("Server error"));
            }
        }
    }

    private void broadcast(WebSocketMessage message, WebSocketClient exclude) {
        String json = message.toJson();

        for (WebSocketClient client : clients.values()) {
            if (client != exclude && client.isAuthenticated() &&
                    client.isConnected()) {
                client.send(json);
            }
        }
    }

    private void broadcastToAuthenticated(WebSocketMessage message) {
        String json = message.toJson();

        for (WebSocketClient client : clients.values()) {
            if (client.isAuthenticated() && client.isConnected()) {
                client.send(json);
            }
        }
    }

    private void broadcastUserList() {
        WebSocketMessage userList = getUserListMessage();
        broadcastToAuthenticated(userList);
    }

    private WebSocketClient findClientByUsername(String username) {
        for (WebSocketClient client : clients.values()) {
            if (username.equalsIgnoreCase(client.getUsername())) {
                return client;
            }
        }
        return null;
    }

    private WebSocketMessage getUserListMessage() {
        String[] users = clients.values().stream()
                .filter(WebSocketClient::isAuthenticated)
                .map(WebSocketClient::getUsername)
                .toArray(String[]::new);

        return WebSocketMessage.userList(users);
    }

    public int getClientCount() {
        return clients.size();
    }

    public void shutdown() {
        try {
            System.out.println("[WS] Shutting down...");

            WebSocketMessage shutdownMsg = new WebSocketMessage("system");
            shutdownMsg.put("message", "Server shutting down");
            broadcastToAuthenticated(shutdownMsg);

            stop(1000);

            System.out.println("[WS] Stopped");
        } catch (Exception e) {
            System.err.println("[WS ERROR] Shutdown: " + e.getMessage());
        }
    }
}