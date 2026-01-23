# 📥 Hướng dẫn Clone và Chạy ChatAppV2

## Bước 1: Clone Repository

```bash
git clone https://github.com/USERNAME/ChatAppV2.git
cd ChatAppV2
```

## Bước 2: Yêu cầu hệ thống

- ✅ Java 17 hoặc 21 (Khuyến nghị Java 17)
- ✅ Maven 3.6+ (hoặc dùng JAR file có sẵn)
- ✅ Port 5000, 8080, 8081, 9090 phải available

## Bước 3: Compile (nếu cần)

### Có Maven:
```bash
mvn clean compile package
```

### Không có Maven:
Dùng file JAR có sẵn trong `target/chat-server.jar`

## Bước 4: Chạy Server

### Windows:
```powershell
java -cp target\chat-server.jar server.core.ChatServer
```

### Linux/Mac:
```bash
java -cp target/chat-server.jar server.core.ChatServer
```

Bạn sẽ thấy:
```
═══════════════════════════════════════
  HTTP REST API Server Started
  Port: 8080
═══════════════════════════════════════
  WebSocket Server Started
  Port: 8081
═══════════════════════════════════════
  gRPC Server Started
  Port: 9090
═══════════════════════════════════════
  ChatAppV2 Server
  Version: 2.0.0
  Port: 5000
  Max Clients: 1000
═══════════════════════════════════════

Server is ready and listening for connections...
```

## Bước 5: Test các Services

### 1. Web Chat (WebSocket)
Mở file `web-chat.html` trong browser:
```bash
# Windows
start web-chat.html

# Linux
xdg-open web-chat.html

# Mac
open web-chat.html
```

### 2. HTTP REST API
Mở browser và test:
- http://localhost:8080/api/status
- http://localhost:8080/api/users
- http://localhost:8080/api/rooms

Hoặc dùng curl:
```bash
curl http://localhost:8080/api/status
```

### 3. TCP Socket Client
```bash
java -cp target/chat-server.jar client.core.ChatClient
```

### 4. gRPC Client
```bash
java -cp target/chat-server.jar client.grpc.SimpleGrpcClient
```

## 🐛 Troubleshooting

### Lỗi: Port already in use
```bash
# Tìm process đang dùng port
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F
```

### Lỗi: Java version không đúng
```bash
# Kiểm tra version
java -version

# Cần Java 17 hoặc 21
```

### gRPC client không kết nối được
- Đảm bảo dùng Java 17 hoặc 21 (không phải Java 25)
- Xem chi tiết trong `GRPC_FIX.md`

## 📚 Documents

- `README.md` - Tổng quan project
- `RUNNING_STATUS.md` - Kết quả test
- `GRPC_FIX.md` - Fix gRPC issues
- `TEST_RESULT.md` - Chi tiết test results

## 🎯 Demo nhanh

Sau khi server chạy:

1. **Mở 2-3 tab browser** với `web-chat.html`
2. **Nhập username khác nhau** ở mỗi tab
3. **Chat giữa các tabs** - real-time!
4. **Test API** bằng browser hoặc curl

## 🎓 Features

- ✅ TCP Socket Server (Multi-threaded)
- ✅ HTTP REST API (JSON responses)
- ✅ WebSocket Chat (Real-time)
- ✅ gRPC Server (Protocol Buffers)
- ✅ SSL/TLS Support
- ✅ File Transfer
- ✅ Authentication
- ✅ Chat Rooms
- ✅ Heartbeat Monitoring

## 📧 Support

Nếu có vấn đề, check:
1. `RUNNING_STATUS.md` - Status của services
2. `GRPC_FIX.md` - gRPC troubleshooting
3. Server logs trong console

---

**Happy Chatting! 💬🚀**
