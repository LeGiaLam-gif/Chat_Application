package server.http.dto;

/**
 * Generic API Response wrapper
 * Đảm bảo tất cả responses có format nhất quán
 */
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorInfo error;

    // Success response
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    // Error response
    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorInfo(code, message);
        return response;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ErrorInfo getError() {
        return error;
    }

    // Inner class for error info
    public static class ErrorInfo {
        private String code;
        private String message;

        public ErrorInfo(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}