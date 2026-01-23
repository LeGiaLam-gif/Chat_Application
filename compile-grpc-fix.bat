@echo off
cd /d "c:\Users\Admin\Downloads\ChatAppV2 (1)\ChatAppV2"

echo Compiling GrpcChatServer.java...
javac -cp "target\chat-server.jar" -d target\classes src\server\grpc\GrpcChatServer.java

echo Compiling SimpleGrpcClient.java...
javac -cp "target\chat-server.jar" -d target\classes src\client\grpc\SimpleGrpcClient.java

echo Done!
pause
