// Package and imports
package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;

// Login form class allowing users to log into the application
public class LoginForm {
    // Fields
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Constructor to initialize and display the login form
    public LoginForm() {

        // Setup main frame
        frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);  // Full screen

        // ---- Gradient Background Panel ----
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(173, 216, 230); // Light blue
                Color color2 = new Color(100, 150, 250); // Darker blue
                GradientPaint gradient = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        frame.setContentPane(backgroundPanel);

        // ---- Logo Panel (top-left corner) ----
        ImageIcon logoIcon = new ImageIcon("src/main/resources/logo.png");
        Image scaledLogo = logoIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setOpaque(false);
        logoPanel.add(logoLabel);
        backgroundPanel.add(logoPanel, BorderLayout.NORTH);

        // ---- Form Panel (center content) ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false); // transparent panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("Please Sign In");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(new Color(100, 150, 250));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.addActionListener(e -> loginUser());

        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                loginButton.setBackground(new Color(80, 130, 220));
            }

            public void mouseExited(MouseEvent evt) {
                loginButton.setBackground(new Color(100, 150, 250));
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(loginButton, gbc);


        // Register Link
        JButton registerLink = new JButton("No account? Register here");
        registerLink.setFont(new Font("Arial", Font.PLAIN, 14));
        registerLink.setBackground(new Color(245, 245, 245));
        registerLink.setForeground(new Color(100, 150, 250));
        registerLink.setFocusPainted(false);
        registerLink.addActionListener(e -> openRegistrationForm());

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(registerLink, gbc);



        // Shift form panel upward
        JPanel verticalWrapper = new JPanel();
        verticalWrapper.setLayout(new BoxLayout(verticalWrapper, BoxLayout.Y_AXIS));
        verticalWrapper.setOpaque(false);
        verticalWrapper.add(Box.createVerticalStrut(-50));  // Adjust height as needed
        verticalWrapper.add(formPanel);
        verticalWrapper.add(Box.createVerticalGlue());

        backgroundPanel.add(verticalWrapper, BorderLayout.CENTER);


        frame.setVisible(true);
    }

    // Handle login attempt
    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            String url = "http://localhost:8080/req/login";
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            String response = sendPostRequest(url, json.toString());
            JSONObject responseJson = new JSONObject(response);

            if (responseJson.has("error")) {
                JOptionPane.showMessageDialog(frame, responseJson.getString("error"));
            } else if (responseJson.has("message")) {
                String userId = responseJson.getString("userId");
                String usernameFromResponse = responseJson.getString("username");

                JOptionPane.showMessageDialog(frame, responseJson.getString("message"));
                frame.dispose();
                new HomePage(userId, usernameFromResponse);
            } else {
                JOptionPane.showMessageDialog(frame, "Unexpected server response.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error connecting to the server.");
        }
    }

    // Open the registration form
    private void openRegistrationForm() {
        frame.dispose();
        new RegistrationForm();
    }

    // Send HTTP POST request to backend and return response
    private String sendPostRequest(String urlString, String jsonInputString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Choose input stream based on response code
        InputStream inputStream;
        if (connection.getResponseCode() >= 400) {
            inputStream = connection.getErrorStream(); // <-- Get error body
        } else {
            inputStream = connection.getInputStream(); // <-- Get normal response
        }

        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

}
