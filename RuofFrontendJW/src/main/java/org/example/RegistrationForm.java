// Package and imports
package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;

// Registration form class allowing users to create a new account
public class RegistrationForm {

    // Fields
    private JFrame frame;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel lengthConditionLabel;
    private JLabel complexityConditionLabel;

    // Constructor to initialize and display the registration form
    public RegistrationForm() {
        // Setup main frame
        frame = new JFrame("Register");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

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
        backgroundPanel.setLayout(new BorderLayout()); // Use BorderLayout for top/bottom/center layout
        frame.setContentPane(backgroundPanel);

        // ---- Logo Panel (top-left corner) ----
        ImageIcon logoIcon = new ImageIcon("src/main/resources/logo.png"); // ✅ Change to your actual path
        Image scaledLogo = logoIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setOpaque(false);
        logoPanel.add(logoLabel);
        backgroundPanel.add(logoPanel, BorderLayout.NORTH);

        // ---- Form Panel (center of screen) ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false); // let background gradient show
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("Sign Up to Get Started");
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
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Bigger label
        formPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16)); // Bigger input text
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);


        usernameField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);


        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);


        // Register Button
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 16));
        registerButton.setBackground(new Color(100, 150, 250));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.addActionListener(e -> registerUser());

        // Password Conditions Panel
        JPanel conditionPanel = new JPanel();
        conditionPanel.setLayout(new BoxLayout(conditionPanel, BoxLayout.Y_AXIS));
        conditionPanel.setBackground(Color.white);
        conditionPanel.setOpaque(true);
        conditionPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 55));

        lengthConditionLabel = new JLabel("• At least 8 characters");
        lengthConditionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        lengthConditionLabel.setForeground(Color.RED);

        complexityConditionLabel = new JLabel("• Includes letters and digits");
        complexityConditionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        complexityConditionLabel.setForeground(Color.RED);

        conditionPanel.add(lengthConditionLabel);
        conditionPanel.add(Box.createVerticalStrut(5));
        conditionPanel.add(complexityConditionLabel);

       // Add condition panel BEFORE the register button
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        formPanel.add(conditionPanel, gbc);






        // Password field listener to update conditions
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updatePasswordConditions();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updatePasswordConditions();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updatePasswordConditions();
            }
        });


        // ---- Register Button ----
        registerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                registerButton.setBackground(new Color(80, 130, 220));
            }

            public void mouseExited(MouseEvent evt) {
                registerButton.setBackground(new Color(100, 150, 250));
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(registerButton, gbc);


        // Login Link
        JButton loginLink = new JButton("Already have an account? Login here");
        loginLink.setFont(new Font("Arial", Font.PLAIN, 14));
        loginLink.setBackground(new Color(245, 245, 245));
        loginLink.setForeground(new Color(100, 150, 250));
        loginLink.setFocusPainted(false);
        loginLink.addActionListener(e -> openLoginForm());

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(loginLink, gbc);


        Dimension buttonSize = loginLink.getPreferredSize();

        registerButton.setPreferredSize(buttonSize);
        loginLink.setPreferredSize(buttonSize);


        // Shift form panel upward
        JPanel verticalWrapper = new JPanel();
        verticalWrapper.setLayout(new BoxLayout(verticalWrapper, BoxLayout.Y_AXIS));
        verticalWrapper.setOpaque(false);
        verticalWrapper.add(Box.createVerticalStrut(-50));
        verticalWrapper.add(formPanel);
        verticalWrapper.add(Box.createVerticalGlue());

        backgroundPanel.add(verticalWrapper, BorderLayout.CENTER);


        frame.setVisible(true);
    }
    // Handle user registration
    private void registerUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            String url = "http://localhost:8080/req/signup";
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("email", email);
            json.put("password", password);

            String response = sendPostRequest(url, json.toString());
            JSONObject responseJson = new JSONObject(response);

            if (responseJson.has("error")) {
                JOptionPane.showMessageDialog(frame, responseJson.getString("error"));
            } else if (responseJson.has("message")) {
                JOptionPane.showMessageDialog(frame, responseJson.getString("message"));
                frame.dispose();
                new LoginForm();
            } else {
                JOptionPane.showMessageDialog(frame, "Unexpected server response.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error connecting to the server.");
        }
    }

    // Update password condition labels (color change based on validity)
    private void updatePasswordConditions() {
        String password = new String(passwordField.getPassword());


        if (password.length() >= 8) {
            lengthConditionLabel.setForeground(new Color(0, 153, 0));  // Green
        } else {
            lengthConditionLabel.setForeground(Color.RED);
        }

        // Check for letters and digits
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        if (hasLetter && hasDigit) {
            complexityConditionLabel.setForeground(new Color(0, 153, 0));  // Green
        } else {
            complexityConditionLabel.setForeground(Color.RED);
        }
    }

    // Open login form
    private void openLoginForm() {
        frame.dispose();
        new LoginForm();
    }

    // Send HTTP POST request to server
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
