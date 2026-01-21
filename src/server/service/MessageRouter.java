package server.service;

import common.protocol.Message;
import common.protocol.MessageType;
import server.core.ServerContext;
import server.core.ClientHandler;
import java.io.IOException;

public class MessageRouter {
    private final ServerContext context;

    public MessageRouter(ServerContext context) {
        this.context = context;
    }

    public void route(Message msg, ClientHandler sender) {
        switch (msg.getType()) {
            case CHAT:
                handleBroadcast(msg);
                break;
            case PRIVATE:
                handlePrivate(msg);
                break;
            case COMMAND:
                handleCommand(msg, sender);
                break;
            case PING:
                handlePing(msg, sender);
                break;
            case PONG:
                handlePong(msg);
                break;
            case FILE_META:
                context.getFileTransferService().handleFileMeta(msg);
                break;
            case FILE_CHUNK:
                context.getFileTransferService().handleFileChunk(msg);
                break;
            case FILE_ACK:
                context.getFileTransferService().handleFileAck(msg);
                break;
            case CONNECT:
                System.err.println("[WARNING] CONNECT after handshake from: " + msg.getSender());
                break;
            case ACCEPT:
                System.err.println("[WARNING] ACCEPT on server from: " + msg.getSender());
                break;
            case REJECT:
                System.err.println("[WARNING] REJECT on server from: " + msg.getSender());
                break;
            case DISCONNECT:
                sender.disconnect();
                break;
            case SERVER:
                System.err.println("[WARNING] SERVER message on server from: " + msg.getSender());
                break;
            default:
                System.err.println("[WARNING] Unknown message type: " + msg.getType());
                break;
        }
    }

    private void handleBroadcast(Message msg) {
        context.getHandlers().values().forEach(handler -> {
            try {
                handler.send(msg);
            } catch (IOException e) {
                // Client disconnected
            }
        });
    }

    private void handlePrivate(Message msg) {
        ClientHandler target = context.getHandler(msg.getReceiver());
        if (target != null) {
            try {
                target.send(msg);
            } catch (IOException e) {
                // Target disconnected
            }
        }
    }

    private void handlePing(Message msg, ClientHandler sender) {
        try {
            Message pong = new Message(MessageType.PONG, "SERVER", msg.getSender(), "");
            sender.send(pong);
        } catch (IOException e) {
            // Ignore
        }
    }

    private void handleCommand(Message msg, ClientHandler sender) {
        String cmd = msg.getContent();
        try {
            if (cmd.startsWith("/who")) {
                String users = String.join(", ", context.getAllUsernames());
                Message response = new Message(MessageType.SERVER, "SERVER",
                        sender.getUsername(), "Online users: " + users);
                sender.send(response);
            } else if (cmd.startsWith("/rooms")) {
                String rooms = String.join(", ", context.getAllRoomNames());
                Message response = new Message(MessageType.SERVER, "SERVER",
                        sender.getUsername(), "Available rooms: " + rooms);
                sender.send(response);
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    private void handlePong(Message msg) {
        String username = msg.getSender();
        var session = context.getSession(username);
        if (session != null) {
            session.updateActivity();
        }
    }

    public void broadcastServerMessage(String content) {
        Message msg = new Message(MessageType.SERVER, "SERVER", content);
        handleBroadcast(msg);
    }
}