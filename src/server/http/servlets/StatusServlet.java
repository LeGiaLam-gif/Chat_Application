package server.http.servlets;

import server.core.ServerContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// import java.util.List;
// import java.util.stream.Collectors;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet xử lý /api/status endpoint
 * Health check và server statistics
 */
public class StatusServlet extends BaseServlet {

    public StatusServlet(ServerContext context) {
        super(context);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        enableCORS(response);

        Map<String, Object> status = new HashMap<>();

        status.put("status", "online");
        status.put("version", "2.0.0");
        status.put("onlineUsers", context.getOnlineUserCount());
        status.put("totalRooms", context.getRooms().size());
        status.put("uptime", getUptime());

        // Memory info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        status.put("memory", memory);

        sendSuccess(response, status);
    }

    private long getUptime() {
        // TODO: Track server start time
        return System.currentTimeMillis();
    }
}