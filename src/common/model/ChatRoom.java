package common.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatRoom - Model class đại diện cho một chat room/channel
 * Thread-safe implementation cho concurrent access
 * 
 * Design Pattern: Thread-safe Collection Wrapper
 */
public class ChatRoom {
    private final String name;
    private final String description;
    private final LocalDateTime createdAt;
    
    // Thread-safe set of members
    private final Set<String> members;
    
    // Room configuration
    private final int maxMembers;
    private boolean isPrivate;
    private String owner;
    
    /**
     * Constructor for public room
     */
    public ChatRoom(String name, String description) {
        this(name, description, 100, false, null);
    }
    
    /**
     * Full constructor
     */
    public ChatRoom(String name, String description, int maxMembers, 
                    boolean isPrivate, String owner) {
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.maxMembers = maxMembers;
        this.isPrivate = isPrivate;
        this.owner = owner;
        
        // Use ConcurrentHashMap.newKeySet() for thread-safe Set
        this.members = ConcurrentHashMap.newKeySet();
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getMaxMembers() { return maxMembers; }
    public boolean isPrivate() { return isPrivate; }
    public String getOwner() { return owner; }
    public int getMemberCount() { return members.size(); }
    
    /**
     * Get unmodifiable view of members for thread safety
     */
    public Set<String> getMembers() {
        return Collections.unmodifiableSet(members);
    }
    
    // Member management
    
    /**
     * Add member to room
     * @return true if added, false if room is full or user already in room
     */
    public boolean addMember(String username) {
        if (members.size() >= maxMembers) {
            return false;
        }
        return members.add(username);
    }
    
    /**
     * Remove member from room
     */
    public boolean removeMember(String username) {
        return members.remove(username);
    }
    
    /**
     * Check if user is member
     */
    public boolean hasMember(String username) {
        return members.contains(username);
    }
    
    /**
     * Check if room is full
     */
    public boolean isFull() {
        return members.size() >= maxMembers;
    }
    
    /**
     * Check if room is empty
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }
    
    /**
     * Transfer ownership (for private rooms)
     */
    public void setOwner(String newOwner) {
        if (isPrivate && members.contains(newOwner)) {
            this.owner = newOwner;
        }
    }
    
    @Override
    public String toString() {
        return String.format("ChatRoom[name=%s, members=%d/%d, private=%s]",
                name, members.size(), maxMembers, isPrivate);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoom)) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return name.equals(chatRoom.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
