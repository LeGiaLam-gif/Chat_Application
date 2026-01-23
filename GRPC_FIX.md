# Hướng dẫn sửa lỗi gRPC Client không kết nối được

## Vấn đề
- Server gRPC chạy được
- Client không connect được với server
- Khi chạy riêng thì cả 2 đều hoạt động

## Nguyên nhân
1. **Timing issue**: Client connect trước khi server sẵn sàng
2. **IPv4/IPv6 conflict**: Java mặc định ưu tiên IPv6
3. **Channel state**: Client không đợi channel READY
4. **Timeout**: Không có timeout cho connection

## Giải pháp đã áp dụng

### 1. Server Side (GrpcChatServer.java)
- ✅ Force IPv4 stack
- ✅ Bind rõ ràng vào 0.0.0.0
- ✅ Log chi tiết connection info

### 2. Client Side (SimpleGrpcClient.java)
- ✅ Force IPv4 stack
- ✅ Thêm keepAlive settings
- ✅ Đợi channel READY trước khi gửi request
- ✅ Thêm timeout cho RPC calls
- ✅ Log chi tiết troubleshooting

## Cách test

### Bước 1: Compile lại project
```bash
cd "c:\Users\Admin\Downloads\ChatAppV2 (1)\ChatAppV2"
mvn clean compile
```

### Bước 2: Chạy Server
```bash
mvn exec:java -Dexec.mainClass="server.core.ChatServer"
```

Chờ đến khi thấy:
```
═══════════════════════════════════════
  gRPC Server Started
  Port: 9090
  Listening on: 0.0.0.0:9090
  Connect via: localhost:9090
═══════════════════════════════════════
```

### Bước 3: Kiểm tra server đang lắng nghe
Mở PowerShell mới và chạy:
```powershell
netstat -ano | findstr :9090
```

Phải thấy dòng như:
```
TCP    0.0.0.0:9090           0.0.0.0:0              LISTENING       12345
```

### Bước 4: Chạy Client (Terminal khác)
```bash
mvn exec:java -Dexec.mainClass="client.grpc.SimpleGrpcClient"
```

Client sẽ:
1. Force IPv4
2. Tạo channel
3. Đợi channel READY (tối đa 5 giây)
4. Gửi authentication request

## Nếu vẫn lỗi

### Kiểm tra 1: Firewall
```powershell
# Tắt Windows Firewall tạm thời để test
netsh advfirewall set allprofiles state off

# Sau khi test xong, bật lại
netsh advfirewall set allprofiles state on
```

### Kiểm tra 2: Port đã bị chiếm
```powershell
netstat -ano | findstr :9090
```

Nếu có process khác đang dùng port 9090, kill nó:
```powershell
taskkill /PID <PID> /F
```

### Kiểm tra 3: IPv6 
Đảm bảo trong code đã có:
```java
System.setProperty("java.net.preferIPv4Stack", "true");
System.setProperty("java.net.preferIPv4Addresses", "true");
```

### Kiểm tra 4: Connection logs
Khi chạy client, bạn sẽ thấy:
```
[CLIENT] Connecting to gRPC server...
[CLIENT] Host: localhost
[CLIENT] Port: 9090
[CLIENT] Channel created successfully
[CLIENT] Waiting for channel to connect...
[CLIENT] Channel state: CONNECTING
[CLIENT] Channel state: READY
[CLIENT] Sending auth request...
```

Nếu state không chuyển sang READY sau 5 giây → Server chưa sẵn sàng

## Debug với IntelliJ IDEA

1. Set breakpoint ở `GrpcChatServer.start()` - line `server = ServerBuilder.forPort(port)`
2. Set breakpoint ở `SimpleGrpcClient.<init>` - line `this.channel = ManagedChannelBuilder`
3. Debug server trước
4. Đợi server qua breakpoint và start xong
5. Debug client
6. Xem giá trị của `channel.getState()`

## Thay đổi port (nếu cần)

Nếu port 9090 bị conflict, đổi trong 2 file:

**ChatServer.java** - line 43:
```java
this.grpcServer = new GrpcChatServer(context, 9095); // Đổi thành 9095
```

**SimpleGrpcClient.java** - line 157:
```java
client = new SimpleGrpcClient("localhost", 9095); // Đổi thành 9095
```

## Kiểm tra kết nối thủ công với grpcurl

Cài đặt grpcurl:
```powershell
choco install grpcurl
```

Test server:
```bash
grpcurl -plaintext localhost:9090 list
```

Nếu server chạy đúng, sẽ thấy:
```
common.grpc.ChatService
grpc.reflection.v1alpha.ServerReflection
```

## Kết luận

Các fix trên sẽ giải quyết:
- ✅ IPv4/IPv6 conflicts
- ✅ Timing issues khi connect
- ✅ Channel state không sẵn sàng
- ✅ Timeout issues
- ✅ Chi tiết error messages để debug

Sau khi compile lại, client sẽ kết nối được với server!
