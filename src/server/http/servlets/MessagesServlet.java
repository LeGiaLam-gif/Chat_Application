package server.http.servlets;

import server.core.ServerContext;
import server.http.dto.MessageDTO;
import common.protocol.Message;
import common.protocol.MessageType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
// import java.util.List;
// import java.util.stream.Collectors;

/**
 * Servlet xử lý /api/messages endpoints
 * 
 * GET /api/messages → Get message history (TODO: requires database)
 * POST /api/messages → Send message
 */
public class MessagesServlet extends BaseServlet {

    public MessagesServlet(ServerContext context) {
        super(context);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        enableCORS(response);

        try {
            // Parse request body
            MessageDTO dto = readJson(request, MessageDTO.class);

            // Validate
            if (dto.getSender() == null || dto.getSender().isEmpty()) {
                sendError(response, "INVALID_SENDER",
                        "Sender is required", 400);
                return;
            }

            if (dto.getContent() == null || dto.getContent().isEmpty()) {
                sendError(response, "INVALID_CONTENT",
                        "Content is required", 400);
                return;
            }

            // Determine message type
            MessageType type;
            if (dto.getReceiver() != null && !dto.getReceiver().isEmpty()) {
                type = MessageType.PRIVATE;
            } else {
                type = MessageType.CHAT;
            }

            // Create message
            Message message = new Message(type, dto.getSender(),
                    dto.getReceiver(), dto.getContent());

            // Route message through existing system
            var handler = context.getHandler(dto.getSender());
            if (handler == null) {
                sendError(response, "SENDER_NOT_ONLINE",
                        "Sender is not online", 400);
                return;
            }

            context.getMessageRouter().route(message, handler);

            // Return success
            sendCreated(response, "Message sent successfully");

        } catch (Exception e) {
            sendError(response, "SERVER_ERROR",
                    "Failed to send message: " + e.getMessage(), 500);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        enableCORS(response);

        // TODO: Implement when database is added
        sendError(response, "NOT_IMPLEMENTED",
                "Message history requires database", 501);
    }
}