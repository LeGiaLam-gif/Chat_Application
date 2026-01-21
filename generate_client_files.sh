#!/bin/bash

# Generate all client files

cat > src/client/core/ChatClient.java << 'EOF'
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
EOF

cat > src/client/core/NetworkListener.java << 'EOF'
package client.core;

import common.protocol.Message;
import javax.swing.SwingUtilities;

public class NetworkListener implements Runnable {
    private final ChatClient client;
    private final MessageCallback callback;
    private volatile boolean running;
    
    public NetworkListener(ChatClient client, MessageCallback callback) {
        this.client = client;
        this.callback = callback;
    }
    
    @Override
    public void run() {
        running = true;
        
        while (running && client.isConnected()) {
            try {
                Message msg = client.receive();
                
                // CRITICAL: Update GUI on Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    callback.onMessageReceived(msg);
                });
                
            } catch (Exception e) {
                if (running) {
                    SwingUtilities.invokeLater(() -> {
                        callback.onConnectionLost();
                    });
                }
                running = false;
            }
        }
    }
    
    public void stop() {
        running = false;
    }
    
    public interface MessageCallback {
        void onMessageReceived(Message msg);
        void onConnectionLost();
    }
}
EOF

cat > src/client/gui/ChatFrame.java << 'EOF'
package client.gui;

import javax.swing.*;
import java.awt.*;

public class ChatFrame extends JFrame {
    public JTextArea chatArea;
    public JTextField inputField;
    public JButton sendButton;
    public JButton fileButton;
    public JList<String> userList;
    public DefaultListModel<String> userListModel;
    public JLabel statusLabel;
    
    public ChatFrame(String username) {
        setTitle("Chat Application - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        
        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(150, 0));
        userScroll.setBorder(BorderFactory.createTitledBorder("Online Users"));
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        inputField = new JTextField();
        sendButton = new JButton("Send");
        fileButton = new JButton("📎 File");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(fileButton);
        buttonPanel.add(sendButton);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Status bar
        statusLabel = new JLabel("Connected");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        // Layout
        add(chatScroll, BorderLayout.CENTER);
        add(userScroll, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
        
        setLocationRelativeTo(null);
    }
    
    public void appendMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    public void updateStatus(String status) {
        statusLabel.setText(status);
    }
}
EOF

cat > src/client/gui/LoginDialog.java << 'EOF'
package client.gui;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JTextField hostField;
    private JTextField portField;
    private boolean confirmed = false;
    
    public LoginDialog(Frame parent) {
        super(parent, "Connect to Server", true);
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField("User" + (int)(Math.random() * 1000));
        panel.add(usernameField);
        
        panel.add(new JLabel("Host:"));
        hostField = new JTextField("localhost");
        panel.add(hostField);
        
        panel.add(new JLabel("Port:"));
        portField = new JTextField("5000");
        panel.add(portField);
        
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(connectButton);
        buttonPanel.add(cancelButton);
        panel.add(new JLabel());
        panel.add(buttonPanel);
        
        add(panel);
        pack();
        setLocationRelativeTo(parent);
    }
    
    public String getUsername() { return usernameField.getText(); }
    public String getHost() { return hostField.getText(); }
    public int getPort() { return Integer.parseInt(portField.getText()); }
    public boolean isConfirmed() { return confirmed; }
}
EOF

cat > src/client/gui/ClientController.java << 'EOF'
package client.gui;

import client.core.ChatClient;
import client.core.NetworkListener;
import client.service.FileSender;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import java.io.File;

public class ClientController implements NetworkListener.MessageCallback {
    private ChatClient client;
    private ChatFrame frame;
    private NetworkListener listener;
    private Thread listenerThread;
    private FileSender fileSender;
    
    public void start() {
        // Show login dialog
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);
        
        if (!loginDialog.isConfirmed()) {
            System.exit(0);
        }
        
        try {
            // Connect to server
            client = new ChatClient(
                loginDialog.getHost(),
                loginDialog.getPort(),
                loginDialog.getUsername()
            );
            
            // Create GUI
            frame = new ChatFrame(loginDialog.getUsername());
            frame.setVisible(true);
            
            // Setup listeners
            frame.sendButton.addActionListener(e -> sendMessage());
            frame.inputField.addActionListener(e -> sendMessage());
            frame.fileButton.addActionListener(e -> sendFile());
            frame.userList.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        startPrivateChat();
                    }
                }
            });
            
            // Start network listener
            listener = new NetworkListener(client, this);
            listenerThread = new Thread(listener);
            listenerThread.start();
            
            // Initialize FileSender
            fileSender = new FileSender(client);
            
            frame.appendMessage("=== Connected to server ===");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Connection failed: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void sendMessage() {
        String text = frame.inputField.getText().trim();
        if (text.isEmpty()) return;
        
        frame.inputField.setText("");
        
        try {
            if (text.startsWith("/pm ")) {
                String[] parts = text.split(" ", 3);
                if (parts.length >= 3) {
                    Message msg = new Message(MessageType.PRIVATE, 
                        client.getUsername(), parts[1], parts[2]);
                    client.send(msg);
                    frame.appendMessage("[PM to " + parts[1] + "] " + parts[2]);
                }
            } else if (text.startsWith("/")) {
                Message msg = new Message(MessageType.COMMAND, 
                    client.getUsername(), text);
                client.send(msg);
            } else {
                Message msg = new Message(MessageType.CHAT, 
                    client.getUsername(), text);
                client.send(msg);
            }
        } catch (Exception e) {
            frame.appendMessage("Error sending message: " + e.getMessage());
        }
    }
    
    private void sendFile() {
        String selectedUser = frame.userList.getSelectedValue();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame, 
                "Please select a user to send file to");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            new Thread(() -> {
                try {
                    fileSender.sendFile(file, selectedUser);
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        frame.appendMessage("File transfer failed: " + e.getMessage());
                    });
                }
            }).start();
        }
    }
    
    private void startPrivateChat() {
        String selectedUser = frame.userList.getSelectedValue();
        if (selectedUser != null) {
            frame.inputField.setText("/pm " + selectedUser + " ");
            frame.inputField.requestFocus();
        }
    }
    
    @Override
    public void onMessageReceived(Message msg) {
        switch (msg.getType()) {
            case CHAT:
                frame.appendMessage("[" + msg.getSender() + "] " + msg.getContent());
                break;
            case PRIVATE:
                frame.appendMessage("[PM from " + msg.getSender() + "] " + msg.getContent());
                break;
            case SERVER:
                frame.appendMessage("*** " + msg.getContent() + " ***");
                updateUserList(msg);
                break;
            case FILE_META:
                handleFileOffer(msg);
                break;
            case FILE_CHUNK:
                // Handle by FileSender
                break;
        }
    }
    
    private void updateUserList(Message msg) {
        if (msg.getContent().contains("joined") || msg.getContent().contains("left")) {
            // Request user list
            try {
                client.send(new Message(MessageType.COMMAND, 
                    client.getUsername(), "/who"));
            } catch (Exception e) {
                // Ignore
            }
        } else if (msg.getContent().startsWith("Online users:")) {
            String[] users = msg.getContent().substring(14).split(", ");
            frame.userListModel.clear();
            for (String user : users) {
                if (!user.equals(client.getUsername())) {
                    frame.userListModel.addElement(user.trim());
                }
            }
        }
    }
    
    private void handleFileOffer(Message msg) {
        String filename = msg.getFilename();
        String sender = msg.getSender();
        
        int choice = JOptionPane.showConfirmDialog(frame,
            sender + " wants to send you: " + filename + "\nAccept?",
            "File Transfer", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            // TODO: Implement file receiving
        }
    }
    
    @Override
    public void onConnectionLost() {
        frame.appendMessage("*** Connection lost ***");
        frame.updateStatus("Disconnected");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientController().start();
        });
    }
}
EOF

cat > src/client/service/FileSender.java << 'EOF'
package client.service;

import client.core.ChatClient;
import common.protocol.Message;
import common.protocol.MessageType;
import common.util.ChecksumUtil;
import java.io.*;
import java.util.Arrays;

public class FileSender {
    private static final int CHUNK_SIZE = 64 * 1024;
    private final ChatClient client;
    
    public FileSender(ChatClient client) {
        this.client = client;
    }
    
    public void sendFile(File file, String receiver) throws Exception {
        String checksum = ChecksumUtil.calculateMD5(file);
        
        // Send metadata
        Message meta = new Message(MessageType.FILE_META, 
            client.getUsername(), receiver, "");
        meta.setFileMetadata(file.getName(), file.length(), checksum);
        client.send(meta);
        
        // Send chunks
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int sequence = 0;
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) > 0) {
                byte[] chunk = Arrays.copyOf(buffer, bytesRead);
                
                Message chunkMsg = new Message(MessageType.FILE_CHUNK,
                    client.getUsername(), receiver, "");
                chunkMsg.setChunkData(sequence, chunk);
                client.send(chunkMsg);
                
                sequence++;
            }
        }
    }
}
EOF

echo "Client files generated successfully!"

