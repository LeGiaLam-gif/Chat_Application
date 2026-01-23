package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

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
        
         
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane chatScroll = new JScrollPane(chatArea);
         
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(150, 0));
        userScroll.setBorder(BorderFactory.createTitledBorder("Online Users"));
         
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
         
        statusLabel = new JLabel("Connected");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
         
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