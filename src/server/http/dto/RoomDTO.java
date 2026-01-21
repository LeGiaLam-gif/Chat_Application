package server.http.dto;

import common.model.ChatRoom;

/**
 * DTO cho Room information
 */
public class RoomDTO {
    private String name;
    private String description;
    private int memberCount;
    private String[] members;

    public static RoomDTO fromRoom(ChatRoom room) {
        RoomDTO dto = new RoomDTO();
        dto.name = room.getName();
        dto.description = room.getDescription();
        dto.memberCount = room.getMemberCount();
        dto.members = room.getMembers().toArray(new String[0]);
        return dto;
    }

    // Getters/Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String[] getMembers() {
        return members;
    }

    public void setMembers(String[] members) {
        this.members = members;
    }
}