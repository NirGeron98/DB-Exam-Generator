package project_package;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginGUI {
	public static Scanner scn = new Scanner(System.in);


	public static Map<String, Object> createAndShowGUI(final Connection conn) {
        final JFrame frame = new JFrame("Login");
        frame.setSize(300, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        final JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        frame.add(userLabel);

        final JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        frame.add(userText);

        final JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 50, 80, 25);
        frame.add(passwordLabel);

        final JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        frame.add(passwordText);

        final JButton loginButton = new JButton("Login");
        loginButton.setBounds(100, 80, 80, 25);
        frame.add(loginButton);
        frame.getRootPane().setDefaultButton(loginButton);

        final Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("loginSuccess", false); // Default to false

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());

                if (checkCredentials(conn, username, password)) {
                    showMessage(frame, "Login successful!");
                    loginResult.put("loginSuccess", true);
                    loginResult.put("username", username);
                    frame.dispose(); // Close the login window
                } else {
                    showError(frame, "Invalid username or password.");
                }
            }
        });
        
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);

        // Wait until the login process is done
        while (frame.isVisible()) {
            try {
                Thread.sleep(100); // Sleep to reduce CPU usage while waiting
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        return loginResult;
    }


    // Function to check credentials in the database
    private static boolean checkCredentials(Connection conn, String username, String password) {
        String sql = "SELECT * FROM users WHERE uname = ? AND upassword = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Return true if a match is found, false otherwise
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Function to show a message dialog
    private static void showMessage(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    // Function to show an error dialog
    private static void showError(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
