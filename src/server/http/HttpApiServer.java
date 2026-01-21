package server.http;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import server.core.ServerContext;
import server.http.servlets.*;

public class HttpApiServer {

    private final ServerContext context;
    private final int port;
    private Server server;

    public HttpApiServer(ServerContext context, int port) {
        this.context = context;
        this.port = port;
    }

    public void start() throws Exception {
        server = new Server(port);

        ServletContextHandler handler = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        handler.setContextPath("/");

        // ✅ FIX: Tạo ServletHolder đúng cách

        // Users servlet
        ServletHolder usersHolder = new ServletHolder();
        usersHolder.setServlet(new UsersServlet(context));
        handler.addServlet(usersHolder, "/api/users/*");

        // Messages servlet
        ServletHolder messagesHolder = new ServletHolder();
        messagesHolder.setServlet(new MessagesServlet(context));
        handler.addServlet(messagesHolder, "/api/messages/*");

        // Rooms servlet
        ServletHolder roomsHolder = new ServletHolder();
        roomsHolder.setServlet(new RoomsServlet(context));
        handler.addServlet(roomsHolder, "/api/rooms/*");

        // Status servlet
        ServletHolder statusHolder = new ServletHolder();
        statusHolder.setServlet(new StatusServlet(context));
        handler.addServlet(statusHolder, "/api/status");

        server.setHandler(handler);
        server.start();

        System.out.println("═══════════════════════════════════════");
        System.out.println("  HTTP REST API Server Started");
        System.out.println("  Port: " + port);
        System.out.println("  Base URL: http://localhost:" + port);
        System.out.println("═══════════════════════════════════════");
    }

    public void stop() throws Exception {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }

    public boolean isRunning() {
        return server != null && server.isRunning();
    }
}