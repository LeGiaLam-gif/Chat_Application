package client.gui;

import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
