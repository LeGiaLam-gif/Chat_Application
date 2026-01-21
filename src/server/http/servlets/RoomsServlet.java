package server.http.servlets;

import server.core.ServerContext;
import server.http.dto.RoomDTO;
import common.model.ChatRoom;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet xử lý /api/rooms endpoints
 * 
 * GET /api/rooms → List all rooms
 * POST /api/rooms → Create new room
 * GET /api/rooms/:id → Get room info
 */
public class RoomsServlet extends BaseServlet {

    public RoomsServlet(ServerContext context) {
        super(context);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        enableCORS(response);

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // List all rooms
            listRooms(response);
        } else {
            // Get specific room
            String roomName = pathInfo.substring(1);
            getRoom(roomName, response);
        }
    }

    private void listRooms(HttpServletResponse response) throws IOException {
        List<RoomDTO> rooms = context.getRooms().values().stream()
                .map(RoomDTO::fromRoom)
                .collect(Collectors.toList());

        sendSuccess(response, rooms);
    }

    private void getRoom(String roomName, HttpServletResponse response)
            throws IOException {
        ChatRoom room = context.getRoom(roomName);

        if (room == null) {
            sendError(response, "ROOM_NOT_FOUND",
                    "Room not found", 404);
            return;
        }

        sendSuccess(response, RoomDTO.fromRoom(room));
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        enableCORS(response);

        try {
            RoomDTO dto = readJson(request, RoomDTO.class);

            if (dto.getName() == null || dto.getName().isEmpty()) {
                sendError(response, "INVALID_NAME",
                        "Room name is required", 400);
                return;
            }

            // Check if room exists
            if (context.getRoom(dto.getName()) != null) {
                sendError(response, "ROOM_EXISTS",
                        "Room already exists", 409);
                return;
            }

            // Create room
            ChatRoom room = new ChatRoom(dto.getName(), dto.getDescription());
            context.getRooms().put(dto.getName(), room);

            sendCreated(response, RoomDTO.fromRoom(room));

        } catch (Exception e) {
            sendError(response, "SERVER_ERROR",
                    "Failed to create room: " + e.getMessage(), 500);
        }
    }
}