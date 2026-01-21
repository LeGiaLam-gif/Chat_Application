package server.service;

import server.core.ServerContext;
import common.model.ChatRoom;

public class RoomService {
    private final ServerContext context;
    
    public RoomService(ServerContext context) {
        this.context = context;
    }
    
    public boolean createRoom(String name, String description, String creator) {
        if (context.getRoom(name) != null) {
            return false;
        }
        ChatRoom room = new ChatRoom(name, description);
        room.addMember(creator);
        context.addRoom(name, room);
        return true;
    }
    
    public boolean joinRoom(String username, String roomName) {
        ChatRoom room = context.getRoom(roomName);
        if (room == null || room.isFull()) {
            return false;
        }
        var session = context.getSession(username);
        if (session != null) {
            session.joinRoom(roomName);
            return room.addMember(username);
        }
        return false;
    }
    
    public boolean leaveRoom(String username, String roomName) {
        ChatRoom room = context.getRoom(roomName);
        if (room != null) {
            var session = context.getSession(username);
            if (session != null) {
                session.leaveRoom(roomName);
            }
            return room.removeMember(username);
        }
        return false;
    }
}
