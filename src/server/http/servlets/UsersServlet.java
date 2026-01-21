package server.http.servlets;

import server.core.ServerContext;
import server.http.dto.UserDTO;
import common.model.UserSession;

// ✅ ĐÚNG - DÙNG javax
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class UsersServlet extends BaseServlet {

    public UsersServlet(ServerContext context) {
        super(context);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        enableCORS(response);

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            listUsers(request, response);
        } else {
            String username = pathInfo.substring(1);
            getUser(username, response);
        }
    }

    private void listUsers(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        List<UserDTO> users = context.getSessions().values().stream()
                .map(UserDTO::fromSession)
                .collect(Collectors.toList());

        sendSuccess(response, users);
    }

    private void getUser(String username, HttpServletResponse response)
            throws IOException {
        UserSession session = context.getSession(username);

        if (session == null) {
            sendError(response, "USER_NOT_FOUND",
                    "User '" + username + "' is not online", 404);
            return;
        }

        UserDTO user = UserDTO.fromSession(session);
        sendSuccess(response, user);
    }

    @Override
    protected void doDelete(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        enableCORS(response);

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, "BAD_REQUEST", "User ID required", 400);
            return;
        }

        String username = pathInfo.substring(1);

        var handler = context.getHandler(username);
        if (handler == null) {
            sendError(response, "USER_NOT_FOUND", "User not found", 404);
            return;
        }

        handler.disconnect();
        sendSuccess(response, "User disconnected");
    }
}