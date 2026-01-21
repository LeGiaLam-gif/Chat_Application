package client.core;

import common.protocol.Message;
import common.protocol.MessageType;
import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private volatile boolean connected;
    
    public ChatClient(String host, int port, String username) throws Exception {
        this.username = username;
        this.socket = new Socket(host, port);
        
        // Setup streams (output first!)
        this.out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
        
        // Send CONNECT message
        Message connect = new Message(MessageType.CONNECT, username, "");
        send(connect);
        
        // Wait for ACCEPT/REJECT
        Message response = receive();
        if (response.getType() == MessageType.REJECT) {
            throw new IOException("Connection rejected: " + response.getContent());
        }
        
        connected = true;
    }
    
    public synchronized void send(Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }
    
    public Message receive() throws Exception {
        return (Message) in.readObject();
    }
    
    public void disconnect() {
        try {
            if (connected) {
                send(new Message(MessageType.DISCONNECT, username, ""));
            }
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignore
        }
        connected = false;
    }
    
    public boolean isConnected() {
        return connected && !socket.isClosed();
    }
    
    public String getUsername() {
        return username;
    }
}
