// Package and imports
package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONArray;
import org.json.JSONObject;

// Home page class displaying welcome, tasks overview, pomodoro, and quote
public class HomePage {
    // Fields
    private JFrame frame;
    private String userId;
    private String username;
    private Timer pomodoroTimer;
    private boolean isRunning = false;
    private int sessionCount = 0;
    private JLabel sessionLabel;


    // Constructor to setup the Home Page
    public HomePage(String userId, String username) {
        this.userId = userId;
        this.username = username;


        // Setup main frame
        frame = new JFrame("Home Page");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen

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

        // ---- Center Content Panel (Welcome + Buttons) ----
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Welcome message
        JLabel welcomeMessage = new JLabel("Welcome to Your Calendar, " + username + "!");
        welcomeMessage.setFont(new Font("Arial", Font.BOLD, 36));
        welcomeMessage.setForeground(Color.WHITE);
        welcomeMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeMessage.setHorizontalAlignment(JLabel.CENTER);
        centerPanel.add(Box.createVerticalStrut(50));
        centerPanel.add(welcomeMessage);
        centerPanel.add(Box.createVerticalStrut(50));

        JSONObject counts = fetchTaskCount(userId);
        int completed = counts.optInt("completed", 0);
        int inProgress = counts.optInt("inProgress", 0);
        int totaltask = completed + inProgress;
        int total = counts.optInt("total", totaltask);



        // Compact square panel
        JPanel taskCountPanel = new JPanel();
        taskCountPanel.setOpaque(true);
        taskCountPanel.setBackground(new Color(255, 255, 255, 180)); // Semi-transparent blue
        taskCountPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(33, 150, 243), 2, true),  // Rounded border
                BorderFactory.createEmptyBorder(6, 14, 6, 14)                        // Padding around text
        ));
        taskCountPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        taskCountPanel.setLayout(new BoxLayout(taskCountPanel, BoxLayout.X_AXIS)); // Prevent stretching
        taskCountPanel.setMaximumSize(new Dimension(400, 120)); //adjust width/height as needed


        // Label centered inside the small panel
        JPanel taskTextPanel = new JPanel();
        taskTextPanel.setLayout(new BoxLayout(taskTextPanel, BoxLayout.Y_AXIS));
        taskTextPanel.setOpaque(false);

        JLabel totalLabel = new JLabel("You currently have " + total + " total tasks");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 24));
        totalLabel.setForeground(new Color(33, 150, 243));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel completedLabel = new JLabel("Completed: " + completed);
        completedLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        completedLabel.setForeground(new Color(33, 150, 243));
        completedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel progressLabel = new JLabel("In progress: " + inProgress);
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        progressLabel.setForeground(new Color(33, 150, 243));
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        taskTextPanel.add(totalLabel);
        taskTextPanel.add(Box.createVerticalStrut(5));
        taskTextPanel.add(completedLabel);
        taskTextPanel.add(progressLabel);

        taskCountPanel.add(taskTextPanel);

        // Add to the center panel
        centerPanel.add(taskCountPanel);
        centerPanel.add(Box.createVerticalStrut(38));

        // Buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);

        JButton createTaskButton = createStyledButton("Create Task ✎");
        createTaskButton.addActionListener(e -> openTaskCalendarPage());
        buttonsPanel.add(createTaskButton);
        buttonsPanel.add(Box.createVerticalStrut(15));

        JButton allTasksButton = createStyledButton("All Tasks ☰");
        allTasksButton.addActionListener(e -> openAllTasksPage());
        buttonsPanel.add(allTasksButton);
        buttonsPanel.add(Box.createVerticalStrut(15));

        JButton statsButton = createStyledButton("View Statistics 📊");
        statsButton.addActionListener(e -> openStatisticsDashboard());
        buttonsPanel.add(statsButton);
        buttonsPanel.add(Box.createVerticalStrut(15));


        JButton logoutButton = createStyledButton("Logout ↪");
        logoutButton.addActionListener(e -> logoutUser());
        buttonsPanel.add(logoutButton);

        centerPanel.add(buttonsPanel);

        // ---- Right Sidebar (Pomodoro + Quote) ----
        JPanel verticalWrapper = new JPanel();
        verticalWrapper.setLayout(new BoxLayout(verticalWrapper, BoxLayout.Y_AXIS));
        verticalWrapper.setOpaque(false);

        // Add some space at the top to shift content upward
        verticalWrapper.add(Box.createVerticalStrut(0));
        verticalWrapper.add(centerPanel);
        verticalWrapper.add(Box.createVerticalGlue());

        backgroundPanel.add(verticalWrapper, BorderLayout.CENTER);

        // Show the frame
        frame.setVisible(true);

        // ---- Right Sidebar (Pomodoro + Quote, in separate panels) ----
        // Wrapper panel using BorderLayout and padding to shift left and down
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.setBorder(BorderFactory.createEmptyBorder(60, 0, 0, 10)); // top, left, bottom, right


        // Sidebar container
        JPanel rightSidebar = new JPanel();
        rightSidebar.setLayout(new BoxLayout(rightSidebar, BoxLayout.Y_AXIS));
        rightSidebar.setOpaque(false);
        rightSidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 50)); // Shift slightly left

        // 🕒 Pomodoro Panel
        JPanel pomodoroPanel = new JPanel();
        pomodoroPanel.setPreferredSize(new Dimension(600, 300));
        pomodoroPanel.setMaximumSize(new Dimension(700, 320));
        pomodoroPanel.setLayout(new BoxLayout(pomodoroPanel, BoxLayout.Y_AXIS));
        pomodoroPanel.setBackground(new Color(255, 255, 255, 180));
        pomodoroPanel.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 2, true));

        JLabel timerTitle = new JLabel("Pomodoro Timer");
        timerTitle.setFont(new Font("Arial", Font.BOLD, 20));
        timerTitle.setForeground(new Color(33, 150, 243));
        timerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sessionLabel = new JLabel("Sessions Completed: 0");
        sessionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        sessionLabel.setForeground(new Color(33, 150, 243));
        sessionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel timerDisplay = new JLabel("25:00");
        timerDisplay.setFont(new Font("Arial", Font.BOLD, 28));
        timerDisplay.setForeground(Color.DARK_GRAY);
        timerDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = createCustomButton("Start", new Color(144, 238, 144), new Color(120, 200, 120)); // Light Green -> Slightly Darker
        JButton endButton = createCustomButton("End", new Color(255, 160, 160), new Color(240, 120, 120));     // Light Red -> Slightly Darker
        JButton restButton = createCustomButton("Rest", new Color(211, 211, 211), new Color(169, 169, 169)); // Light gray -> darker gray on hover

        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        endButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        restButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        startButton.addActionListener(e -> {
            if (isRunning) return; // avoid multiple timers
            isRunning = true;
            final int[] secondsLeft = {25 * 60};

            pomodoroTimer = new Timer(1000, evt -> {
                int minutes = secondsLeft[0] / 60;
                int seconds = secondsLeft[0] % 60;
                timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
                secondsLeft[0]--;

                if (secondsLeft[0] < 0) {
                    sessionCount++;
                    sessionLabel.setText("Sessions Completed: " + sessionCount);
                    secondsLeft[0] = 25 * 60; // Restart
                }
            });
            pomodoroTimer.start();
        });

        endButton.addActionListener(e -> {
            if (pomodoroTimer != null) pomodoroTimer.stop();
            isRunning = false;
            timerDisplay.setText("25:00");
        });

        restButton.addActionListener(e -> {
            if (pomodoroTimer != null) pomodoroTimer.stop();
            isRunning = false;
            sessionCount = 0;
            sessionLabel.setText("Sessions Completed: 0");
            timerDisplay.setText("00:00");
        });



        pomodoroPanel.add(Box.createVerticalStrut(10));
        pomodoroPanel.add(timerTitle);
        pomodoroPanel.add(sessionLabel);
        pomodoroPanel.add(Box.createVerticalStrut(10));
        pomodoroPanel.add(timerDisplay);
        pomodoroPanel.add(Box.createVerticalStrut(10));
        pomodoroPanel.add(startButton);
        pomodoroPanel.add(Box.createVerticalStrut(5));
        pomodoroPanel.add(endButton);
        pomodoroPanel.add(Box.createVerticalStrut(5));
        pomodoroPanel.add(restButton);
        pomodoroPanel.add(Box.createVerticalStrut(10));

        // Quote Panel
        JPanel quotePanel = new JPanel();
        quotePanel.setPreferredSize(new Dimension(600, 100));
        quotePanel.setMaximumSize(new Dimension(700, 120));
        quotePanel.setLayout(new BoxLayout(quotePanel, BoxLayout.Y_AXIS));
        quotePanel.setBackground(new Color(255, 255, 255, 180));
        quotePanel.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 2, true));

        JSONObject quoteData = fetchDailyQuote();
        String quoteText = quoteData.optString("q", "Stay positive and keep moving forward.");
        String quoteAuthor = quoteData.optString("a", "Unknown");

        JLabel quoteLabel = new JLabel("<html><div style='text-align: center; max-width: 300px;'>" + quoteText + "</div></html>");
        quoteLabel.setFont(new Font("Serif", Font.ITALIC, 20));
        quoteLabel.setForeground(new Color(33, 150, 243));
        quoteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel authorLabel = new JLabel("- " + quoteAuthor);
        authorLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        authorLabel.setForeground(new Color(33, 150, 243));
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        quotePanel.add(Box.createVerticalStrut(20));

         // Wrap labels in a centered container
        JPanel quoteTextWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        quoteTextWrapper.setOpaque(false);
        quoteTextWrapper.add(quoteLabel);

        JPanel authorTextWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        authorTextWrapper.setOpaque(false);
        authorTextWrapper.add(authorLabel);

        quotePanel.add(quoteTextWrapper);
        quotePanel.add(Box.createVerticalStrut(10));
        quotePanel.add(authorTextWrapper);
        quotePanel.add(Box.createVerticalStrut(20));


        // Add both panels to the right sidebar
        rightSidebar.add(pomodoroPanel);
        rightSidebar.add(Box.createVerticalStrut(20));
        rightSidebar.add(quotePanel);
        rightWrapper.add(rightSidebar); // wrap it
        backgroundPanel.add(rightWrapper, BorderLayout.EAST); // correct way


    }
    // Create a custom button with color and hover effects
    private JButton createCustomButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
        button.setMaximumSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });

        return button;
    }

    // Create a modern styled button
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(100, 150, 250));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
        button.setMaximumSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(80, 130, 220));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(100, 150, 250));
            }
        });

        return button;
    }

    // Fetch task counts (completed, in-progress, total) from the server
    private JSONObject fetchTaskCount(String userId) {
        try {
            String urlString = "http://localhost:8080/api/tasks/count/" + userId;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();

            return new JSONObject(responseBuilder.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();

    }

    // Open Task Calendar Page
    private void openTaskCalendarPage() {
        frame.dispose();
        new TaskCalendarPage(userId, username);
    }

    // Open All Tasks Page
    private void openAllTasksPage() {
        frame.dispose();
        new AllTasksPage(userId, username);
    }

    // Open Statistics Dashboard Page
    private void openStatisticsDashboard() {
        try {
            String urlString = "http://localhost:8080/api/tasks/stats/" + userId;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            JSONObject stats = new JSONObject(responseBuilder.toString());

            // Extract the maps
            JSONObject completionJSON = stats.getJSONObject("completionStats");
            JSONObject categoryJSON = stats.getJSONObject("categoryStats");
            JSONObject dayJSON = stats.getJSONObject("dayStats");

            // Convert to Map<String, Integer>
            java.util.Map<String, Integer> completionMap = new java.util.HashMap<>();
            java.util.Map<String, Integer> categoryMap = new java.util.HashMap<>();
            java.util.Map<String, Integer> dayMap = new java.util.HashMap<>();

            for (String key : completionJSON.keySet()) {
                completionMap.put(key, completionJSON.getInt(key));
            }
            for (String key : categoryJSON.keySet()) {
                categoryMap.put(key, categoryJSON.getInt(key));
            }
            for (String key : dayJSON.keySet()) {
                dayMap.put(key, dayJSON.getInt(key));
            }

            frame.dispose(); // Close home page
            new StatisticsDashboard(completionMap, categoryMap, dayMap,userId,username); // Now call constructor correctly

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading statistics.");
        }
    }


    // Logout and return to Login Form
    private void logoutUser() {
        JOptionPane.showMessageDialog(frame, "You have been logged out.");
        frame.dispose();
        new LoginForm();
    }

    // Fetch a motivational quote from external API
    private JSONObject fetchDailyQuote() {
        try {
            String urlString = "https://zenquotes.io/api/quotes/random";
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(responseBuilder.toString());
            return jsonArray.getJSONObject(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }


}