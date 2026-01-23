package server.service;

import server.core.ServerContext;
import java.net.Socket;

public class AuthService {
    private final ServerContext context;
    
    public AuthService(ServerContext context) {
        this.context = context;
    }
    
    public boolean authenticate(String username, Socket socket) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (username.length() > 20) {
            return false;
        }
        return context.getSession(username) == null;
    }
}
