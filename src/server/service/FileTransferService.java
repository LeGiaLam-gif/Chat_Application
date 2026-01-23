package server.service;

import server.core.ServerContext;
import common.protocol.Message;

public class FileTransferService {
    private final ServerContext context;

    public FileTransferService(ServerContext context) {
        this.context = context;
    }

    public void handleFileMeta(Message msg) {
        // Forward file offer to receiver
        var handler = context.getHandler(msg.getReceiver());
        if (handler != null) {
            try {
                handler.send(msg);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public void handleFileChunk(Message msg) {
        // Forward chunk to receiver
        var handler = context.getHandler(msg.getReceiver());
        if (handler != null) {
            try {
                handler.send(msg);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public void handleFileAck(Message msg) {
        // Forward ACK back to sender
        var handler = context.getHandler(msg.getReceiver());
        if (handler != null) {
            try {
                handler.send(msg);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
