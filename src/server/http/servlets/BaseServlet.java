package server.http.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import server.core.ServerContext;
import server.http.dto.ApiResponse;

// ❌ SAI - DÙNG jakarta (Jetty 11)
// import jakarta.servlet.http.HttpServlet;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// ✅ ĐÚNG - DÙNG javax (Jetty 9)
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

public abstract class BaseServlet extends HttpServlet {

    protected final ServerContext context;
    protected final Gson gson;

    public BaseServlet(ServerContext context) {
        this.context = context;

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    protected void sendJson(HttpServletResponse response, Object data,
            int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        String json = gson.toJson(data);
        response.getWriter().write(json);
    }

    protected void sendSuccess(HttpServletResponse response, Object data)
            throws IOException {
        sendJson(response, ApiResponse.success(data), 200);
    }

    protected void sendCreated(HttpServletResponse response, Object data)
            throws IOException {
        sendJson(response, ApiResponse.success(data), 201);
    }

    protected void sendError(HttpServletResponse response, String code,
            String message, int statusCode) throws IOException {
        sendJson(response, ApiResponse.error(code, message), statusCode);
    }

    protected <T> T readJson(HttpServletRequest request, Class<T> clazz)
            throws IOException {
        return gson.fromJson(request.getReader(), clazz);
    }

    protected void enableCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization");
    }

    @Override
    protected void doOptions(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        enableCORS(response);
        response.setStatus(200);
    }
}