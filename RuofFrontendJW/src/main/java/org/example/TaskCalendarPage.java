// --- Package and Imports ---
package org.example;

import com.toedter.calendar.JCalendar;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


// --- Main Class for Calendar Task Page ---
public class TaskCalendarPage {
    // --- Fields ---
    private JFrame frame;
    private String userId;
    private String username;
    private JPanel taskPanel;
    private JCalendar calendar;
    private JLabel currentDayLabel;
    private JDialog notesDialog;
    private JTextArea notesTextArea;

    // --- Task category colors ---
    private final Color COLOR_GENERAL = new Color(255, 255, 153);     // Light Yellow
    private final Color COLOR_HOLIDAY = new Color(204, 255, 204);     // Light Green
    private final Color COLOR_MEETING = new Color(204, 229, 255);     // Light Blue
    private final Color COLOR_SOCIAL = new Color(255, 204, 229);      // Light Pink
    private final Color COLOR_PERSONAL = new Color(255, 229, 204);    // Light Orange


    // --- Constructor ---
    public TaskCalendarPage(String userId, String username) {
        this.userId = userId;
        this.username = username;

        // Frame setup
        frame = new JFrame("Task Calendar");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(false);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());

        // Main panel with white background
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout());

        // Header displaying the current day
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        currentDayLabel = new JLabel(getCurrentDay());
        currentDayLabel.setFont(new Font("Arial", Font.BOLD, 18));
        currentDayLabel.setForeground(new Color(80, 130, 220));
        headerPanel.setOpaque(false);
        headerPanel.add(currentDayLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Calendar Customization ---
        taskPanel = new JPanel();
        // Make Calendar Bigger
        calendar = new JCalendar();
        calendar.setPreferredSize(new Dimension(800, 800));

        // Customize day buttons (numbers) and weekday labels (Sun, Mon...)
        JComponent dayPanel = (JComponent) calendar.getDayChooser().getDayPanel();
        for (Component comp : dayPanel.getComponents()) {
            if (comp instanceof JButton button) {
                String text = button.getText();
                if (text.matches("\\d+")) {
                    button.setOpaque(true);
                    button.setContentAreaFilled(true);
                    button.setBorderPainted(false);
                    button.setFocusPainted(false);
                    button.setBackground(Color.WHITE);
                    button.setForeground(Color.black);
                    button.setFont(new Font("Arial", Font.PLAIN, 16));
                } else {
                    JLabel label = new JLabel(text, SwingConstants.CENTER);
                    label.setOpaque(true);
                    label.setBackground(Color.white);
                    label.setForeground(Color.gray);
                    label.setFont(new Font("Arial", Font.BOLD, 16));

                    // Replace button with label
                    int index = dayPanel.getComponentZOrder(button);
                    dayPanel.remove(button);
                    dayPanel.add(label, index);
                }
            }
        }

        // Highlight today's date with a red line
        highlightTodayWithDot(calendar);

        // Listeners to refresh tasks and highlights when month/year changes
        calendar.getMonthChooser().addPropertyChangeListener("month", e -> {
            fetchTasksAndDisplay(taskPanel);
            highlightTodayWithDot(calendar);
        });

        calendar.getYearChooser().addPropertyChangeListener("year", e -> {
            fetchTasksAndDisplay(taskPanel);
            highlightTodayWithDot(calendar);
        });



        // Handle day selection to open Add Task Dialog
        calendar.getDayChooser().addPropertyChangeListener("day", evt -> {
            java.util.Date selectedDate = calendar.getDate();
            openAddTaskDialog(selectedDate, taskPanel);

        });

        // Calendar appearance
        calendar.setPreferredSize(new Dimension(800, 800));
        mainPanel.add(calendar, BorderLayout.WEST);

        // Make calendar background white
        calendar.setBackground(Color.WHITE);
        calendar.getDayChooser().setBackground(Color.WHITE);
        calendar.getDayChooser().getDayPanel().setBackground(Color.WHITE);
        calendar.getYearChooser().setBackground(Color.WHITE);


        // Re-highlight tasks when calendar month or year changes
        calendar.getMonthChooser().addPropertyChangeListener("month", e -> {
            fetchTasksAndDisplay(taskPanel);
        });
        calendar.getYearChooser().addPropertyChangeListener("year", e -> {
            fetchTasksAndDisplay(taskPanel);
        });

        // --- Task Panel setup ---
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setOpaque(false);
        mainPanel.add(new JScrollPane(taskPanel), BorderLayout.CENTER);

        // --- Bottom Button Panel ---
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);
        JButton addTaskButton = createModernButton("Add Task ✚" );
        addTaskButton.addActionListener(e -> openAddTaskDialog(calendar.getDate(), taskPanel));
        JButton goHomeButton = createModernButton("Go to Home ⾕");
        goHomeButton.addActionListener(e -> navigateToHomePage());
        buttonPanel.add(addTaskButton);
        buttonPanel.add(goHomeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Load tasks initially
        fetchTasksAndDisplay(taskPanel);

        // Frame finalize
        frame.add(mainPanel);
        frame.setVisible(true);
        ToolTipManager.sharedInstance().setInitialDelay(100);

    }

    // --- Utility method to get current day in readable format ---
    private String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, java.util.Locale.getDefault());
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        return dayOfWeek + ", " + dayOfMonth + " " + calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) + " " + year;
    }

    // --- Create styled modern buttons ---
    private JButton createModernButton(String text) {
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

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 130, 220));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 150, 250));
            }
        });

        return button;
    }

    // --- Open Add Task Dialog to create a new task ---
    private void openAddTaskDialog(java.util.Date selectedDate, JPanel taskPanel)
    {
        // Make the dialog fullscreen
        JDialog dialog = new JDialog(frame, "Add Task", true);
        dialog.setUndecorated(true);
        dialog.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        dialog.setLocationRelativeTo(null);  // Center the dialog

        // Set layout for the dialog
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Task name field
        JLabel taskNameLabel = new JLabel("Task Name:");
        taskNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(taskNameLabel, gbc);

        JTextField taskNameField = new JTextField();
        taskNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        dialog.add(taskNameField, gbc);

        // Category combo box
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(categoryLabel, gbc);

        JComboBox<String> categoryComboBox = new JComboBox<>(new String[]{"General", "Holiday", "Meeting", "Social", "Personal"});
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        dialog.add(categoryComboBox, gbc);

        // Description field
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(descriptionLabel, gbc);

        JTextArea descriptionField = new JTextArea(3, 20);
        descriptionField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        dialog.add(new JScrollPane(descriptionField), gbc);

        // Date input field (manual entry)
        JLabel dateLabel = new JLabel("Date (yyyy-MM-dd):");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(dateLabel, gbc);

        JTextField dateField = new JTextField();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        dateField.setText(formatter.format(selectedDate));  // Pre-fill with calendar date
        dateField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        dialog.add(dateField, gbc);

        // Save and Cancel buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton saveButton = createModernButton("Save ✔");
        saveButton.addActionListener(e -> {
            String taskName = taskNameField.getText();
            String category = (String) categoryComboBox.getSelectedItem();
            String description = descriptionField.getText();
            String dateString = dateField.getText();
            LocalDate date = validateAndParseDate(dateString);
            if (date != null) {
                Task newTask = new Task(taskName, date, category, description, Long.parseLong(userId));
                saveTask(newTask, taskPanel);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Please use yyyy-MM-dd.");
            }
        });

        JButton cancelButton = createModernButton("Cancel ✖");
        cancelButton.addActionListener(e -> {
            dialog.dispose();
            fetchTasksAndDisplay(taskPanel);
        });


        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        gbc.gridx = 1;
        gbc.gridy = 4;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    // --- Validate and parse date string into LocalDate ---
    private LocalDate validateAndParseDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            return null;  // Invalid date format
        }
    }

    // --- Save a new task to the backend ---
    private void saveTask(Task task, JPanel taskPanel) {
        try {
            String url = "http://localhost:8080/api/tasks";
            JSONObject json = new JSONObject();
            json.put("name", task.getName());
            json.put("date", task.getDate().toString());
            json.put("category", task.getCategory());
            json.put("description", task.getDescription());
            json.put("userId", task.getUserId());

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                JOptionPane.showMessageDialog(frame, "Task saved successfully!");
                fetchTasksAndDisplay(taskPanel);  // Refresh the task panel to show the new task
            } else {
                JOptionPane.showMessageDialog(frame, "Error saving task.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error communicating with the backend.");
        }
    }

    // --- Highlight specific date in calendar with given color ---
    private void highlightDateInCalendar(java.util.Date taskDate, Color color, String tooltipText) {
        java.util.Calendar taskCal = java.util.Calendar.getInstance();
        taskCal.setTime(taskDate);

        int taskDay = taskCal.get(java.util.Calendar.DAY_OF_MONTH);
        int taskMonth = taskCal.get(java.util.Calendar.MONTH);
        int taskYear = taskCal.get(java.util.Calendar.YEAR);

        int currentMonth = calendar.getMonthChooser().getMonth();
        int currentYear = calendar.getYearChooser().getYear();

        if (taskMonth != currentMonth || taskYear != currentYear) return;

        JComponent dayChooserPanel = (JComponent) calendar.getDayChooser().getDayPanel();
        for (Component day : dayChooserPanel.getComponents()) {
            if (day instanceof JButton button) {
                try {
                    int dayNumber = Integer.parseInt(button.getText());
                    if (dayNumber == taskDay) {
                        // Create a subtle highlight effect without button appearance
                        button.setForeground(new Color(0, 0, 0));
                        button.setOpaque(true);
                        button.setBackground(color);
                        button.setBorderPainted(false);
                        button.setContentAreaFilled(true);
                        button.setToolTipText("Tasks: " + tooltipText);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    // --- Fetch tasks from backend and display them ---
    private void fetchTasksAndDisplay(JPanel taskPanel) {
        try {
            String url = "http://localhost:8080/api/tasks/" + userId;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            StringBuilder responseBuilder = new StringBuilder();
            int data;
            while ((data = reader.read()) != -1) {
                responseBuilder.append((char) data);
            }
            reader.close();

            String rawResponse = responseBuilder.toString();
            if (rawResponse.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(rawResponse);
                List<Task> tasks = new ArrayList<>();

                taskPanel.removeAll();  // Clear current panel

                JComponent dayChooserPanel = (JComponent) calendar.getDayChooser().getDayPanel();
                for (Component day : dayChooserPanel.getComponents()) {
                    if (day instanceof JButton button) {
                        button.setBackground(Color.WHITE);
                        button.setOpaque(true);
                        button.setBorderPainted(true);
                        button.setContentAreaFilled(true);
                        button.setToolTipText(null);
                    }
                }

                java.util.Map<LocalDate, List<String>> dateToTasks = new java.util.HashMap<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject taskObject = jsonArray.getJSONObject(i);
                    String name = taskObject.getString("name");
                    LocalDate date = LocalDate.parse(taskObject.getString("date"));
                    String category = taskObject.getString("category");
                    String description = taskObject.isNull("description") ? "" : taskObject.getString("description");
                    boolean completed = taskObject.optBoolean("completed", false);  // Read completion status

                    Long id = taskObject.getLong("id");
                    Task task = new Task(id, name, date, category, description, Long.parseLong(userId), completed);
                    tasks.add(task);

                    dateToTasks.computeIfAbsent(date, k -> new ArrayList<>()).add(name);
                }

                for (int i = 0; i < tasks.size(); i++) {
                    Task task = tasks.get(i);
                    JPanel taskPanelItem = createTaskPanelItem(task);
                    taskPanel.add(taskPanelItem);


                    if (i < tasks.size() - 1) {
                        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                        separator.setForeground(new Color(200, 200, 200));
                        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                        taskPanel.add(separator);
                        taskPanel.add(Box.createVerticalStrut(10));
                    } else {
                        taskPanel.add(Box.createVerticalStrut(20)); // Space after last item
                    }
                }


                for (LocalDate date : dateToTasks.keySet()) {
                    java.util.Date taskDate = java.sql.Date.valueOf(date);
                    String tooltipText = String.join(", ", dateToTasks.get(date));

                    Task sampleTask = tasks.stream().filter(t -> t.getDate().equals(date)).findFirst().orElse(null);
                    if (sampleTask != null) {
                        Color bgColor = switch (sampleTask.getCategory()) {
                            case "Holiday" -> COLOR_HOLIDAY;
                            case "Meeting" -> COLOR_MEETING;
                            case "Social" -> COLOR_SOCIAL;
                            case "Personal" -> COLOR_PERSONAL;
                            default -> COLOR_GENERAL;
                        };
                        highlightDateInCalendar(taskDate, bgColor, tooltipText);
                    }
                }

                // Push reminder to the bottom using glue
                taskPanel.add(Box.createVerticalGlue());

                // Add reminder label
                JLabel reminderLabel = new JLabel("📝 To add additional notes, click on the task name.");
                reminderLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                reminderLabel.setForeground(new Color(0, 102, 204));
                reminderLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
                reminderLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center if you prefer

                taskPanel.add(reminderLabel);

                // Refresh layout
                taskPanel.revalidate();
                taskPanel.repaint();

            } else {
                JOptionPane.showMessageDialog(frame, "Unexpected response format from server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error fetching tasks.");
        }
    }

    // --- Create a visual panel component for a single task ---
    private JPanel createTaskPanelItem(Task task) {
        JPanel taskPanelItem = new JPanel();
        taskPanelItem.setLayout(new GridBagLayout());
        taskPanelItem.setBorder(BorderFactory.createLineBorder(Color.white, 1));
        taskPanelItem.setMaximumSize(new Dimension(600, 140));  // Adjust height for status
        taskPanelItem.setBackground(Color.white);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Task Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel nameLabel = new JLabel("Task Name: " + task.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nameLabel.setForeground(new Color(33, 150, 243));  // Indicate clickable with blue

        nameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String notes = fetchTaskNotes(task.getName());
                showNotesDialog(task.getName(), notes);
            }
        });

        taskPanelItem.add(nameLabel, gbc);


        // Task Date
        gbc.gridy = 1;
        taskPanelItem.add(new JLabel("Date: " + task.getDate()), gbc);

        
        // Task Category
        gbc.gridy = 2;
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        categoryPanel.setOpaque(false); // Transparent panel

        Color bgColor = switch (task.getCategory()) {
            case "Holiday"  -> COLOR_HOLIDAY;
            case "Meeting"  -> COLOR_MEETING;
            case "Social"   -> COLOR_SOCIAL;
            case "Personal" -> COLOR_PERSONAL;
            default         -> COLOR_GENERAL;
        };

        RoundedLabel categoryLabel = new RoundedLabel("Category: " + task.getCategory(), bgColor);
        categoryLabel.setForeground(Color.BLACK);
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding inside

        categoryPanel.add(categoryLabel);
        taskPanelItem.add(categoryPanel, gbc);


        // Task Description
        gbc.gridy = 3;
        taskPanelItem.add(new JLabel("Description: " + task.getDescription()), gbc);

        // Status Label
        gbc.gridy = 4;
        String statusText = task.isCompleted() ? "Status: Completed ✅" : "Status: In Progress ⏳";
        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(task.isCompleted() ? new Color(0, 153, 0) : new Color(255, 102, 0));  // Green or Orange
        taskPanelItem.add(statusLabel, gbc);


        // Button Panel (Update + Delete)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton updateButton = createModernButton("Update ✎");
        updateButton.setPreferredSize(new Dimension(150, 40));
        updateButton.addActionListener(e -> openUpdateTaskDialog(task, taskPanel));

        JButton deleteButton = createModernButton("Delete ✖");
        deleteButton.setPreferredSize(new Dimension(150, 40));
        deleteButton.addActionListener(e -> deleteTask(task, taskPanelItem));

        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 5;
        gbc.anchor = GridBagConstraints.NORTH;
        taskPanelItem.add(buttonPanel, gbc);

        return taskPanelItem;
    }

    // --- Open update dialog to modify an existing task ---
    private void openUpdateTaskDialog(Task task, JPanel taskPanel) {
        // Make the dialog fullscreen
        JDialog dialog = new JDialog(frame, "Update Task", true);
        dialog.setUndecorated(true);
        dialog.setSize(Toolkit.getDefaultToolkit().getScreenSize());  // Make it cover the whole screen
        dialog.setLocationRelativeTo(null);  // Center the dialog

        // Set layout for the dialog
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Task name field
        JLabel taskNameLabel = new JLabel("Task Name:");
        taskNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(taskNameLabel, gbc);

        JTextField taskNameField = new JTextField(task.getName());
        taskNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        dialog.add(taskNameField, gbc);

        // Category combo box
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(categoryLabel, gbc);

        JComboBox<String> categoryComboBox = new JComboBox<>(new String[]{"General", "Holiday", "Meeting", "Social", "Personal"});
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        categoryComboBox.setSelectedItem(task.getCategory());
        gbc.gridx = 1;
        dialog.add(categoryComboBox, gbc);

        // Description field
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(descriptionLabel, gbc);

        JTextArea descriptionField = new JTextArea(3, 20);
        descriptionField.setFont(new Font("Arial", Font.PLAIN, 16));
        descriptionField.setText(task.getDescription());
        gbc.gridx = 1;
        dialog.add(new JScrollPane(descriptionField), gbc);

        // Date input field (manual entry)
        JLabel dateLabel = new JLabel("Date (yyyy-MM-dd):");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(dateLabel, gbc);

        JTextField dateField = new JTextField(task.getDate().toString());
        dateField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        dialog.add(dateField, gbc);

        // Save and Cancel buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton saveButton = createModernButton("Save ✔");
        saveButton.addActionListener(e -> {
            String taskName = taskNameField.getText();
            String category = (String) categoryComboBox.getSelectedItem();
            String description = descriptionField.getText();
            String dateString = dateField.getText();
            LocalDate date = validateAndParseDate(dateString);
            if (date != null) {
                task.setName(taskName);
                task.setCategory(category);
                task.setDescription(description);
                task.setDate(date);

                // Call updateTask method, passing taskPanel to it
                updateTask(task, taskPanel, dialog);
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Please use yyyy-MM-dd.");
            }
        });

        JButton cancelButton = createModernButton("Cancel ✖");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        gbc.gridx = 1;
        gbc.gridy = 4;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    // --- Update a task and refresh UI ---
    private void updateTask(Task updatedTask, JPanel taskPanel, JDialog dialog) {
        try {
            String url = "http://localhost:8080/api/tasks/" + updatedTask.getId();
            JSONObject json = new JSONObject();
            json.put("name", updatedTask.getName());
            json.put("date", updatedTask.getDate().toString());
            json.put("category", updatedTask.getCategory());
            json.put("description", updatedTask.getDescription());
            json.put("userId", updatedTask.getUserId());

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                JOptionPane.showMessageDialog(dialog, "Task updated successfully!");
                dialog.dispose();  // Close the dialog

                // Directly update the task in the taskPanel
                updateTaskInUI(updatedTask, taskPanel);
                fetchTasksAndDisplay(taskPanel); // Refresh calendar highlights and tasks

            } else {
                JOptionPane.showMessageDialog(dialog, "Error updating task.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error communicating with the backend.");
        }
    }

    // --- Update the task UI element after editing ---
    private void updateTaskInUI(Task updatedTask, JPanel taskPanel) {
        // Find the task panel for the specific task by its ID
        for (Component component : taskPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel taskPanelItem = (JPanel) component;

                // Find the label for task name and category in the panel (assuming they are in the panel)
                for (Component innerComponent : taskPanelItem.getComponents()) {
                    if (innerComponent instanceof JLabel) {
                        JLabel label = (JLabel) innerComponent;

                        // Check if it's the task name label (or any other identifying label)
                        if (label.getText().startsWith("Task Name:")) {
                            // Update the task name label
                            label.setText("Task Name: " + updatedTask.getName());
                        }
                        if (label.getText().startsWith("Category:")) {
                            // Update the category label
                            label.setText("Category: " + updatedTask.getCategory());
                        }
                        if (label.getText().startsWith("Description:")) {
                            // Update the description label
                            label.setText("Description: " + updatedTask.getDescription());
                        }
                        if (label.getText().startsWith("Date:")) {
                            // Update the date label
                            label.setText("Date: " + updatedTask.getDate());
                        }
                    }
                }

                // Once updated, revalidate and repaint the panel to reflect the changes
                taskPanelItem.revalidate();
                taskPanelItem.repaint();
            }
        }
    }

    // --- Delete a task from backend and UI ---
    private void deleteTask(Task task, JPanel taskPanelItem) {
        if (task.getId() == null) {
            JOptionPane.showMessageDialog(frame, "Invalid task ID.");
            return;
        }

        try {
            String url = "http://localhost:8080/api/tasks/" + task.getId();
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                JOptionPane.showMessageDialog(frame, "Task deleted successfully!");

                // Remove only the task panel item from the UI
                Container parent = taskPanelItem.getParent();
                parent.remove(taskPanelItem);
                parent.revalidate();
                parent.repaint();

                // Refresh the calendar & task list
                fetchTasksAndDisplay((JPanel) parent);

            } else {
                JOptionPane.showMessageDialog(frame, "Error deleting task. Response code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error communicating with the backend.");
        }
    }

    // --- Highlight today's date with a small red dot ---
    private void highlightTodayWithDot(JCalendar calendar) {
        java.util.Calendar today = java.util.Calendar.getInstance();
        int todayDay = today.get(java.util.Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.getMonthChooser().getMonth();
        int currentYear = calendar.getYearChooser().getYear();

        JComponent dayPanel = (JComponent) calendar.getDayChooser().getDayPanel();
        //  First, remove all existing red dot panels (if any)
        for (Component comp : dayPanel.getComponents()) {
            if (comp instanceof JButton button) {
                for (Component inner : button.getComponents()) {
                    if (inner instanceof JPanel && Color.RED.equals(inner.getBackground())) {
                        button.remove(inner);  // Remove red dot
                        button.revalidate();
                        button.repaint();
                    }
                }
            }
        }

        //  Only apply red dot if viewing current month/year
        if (today.get(java.util.Calendar.MONTH) != currentMonth || today.get(java.util.Calendar.YEAR) != currentYear) {
            return;
        }

        // Add red dot to today's button
        for (Component comp : dayPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton dayButton = (JButton) comp;
                try {
                    int dayNum = Integer.parseInt(dayButton.getText());
                    if (dayNum == todayDay) {
                        JPanel redDot = new JPanel();
                        redDot.setPreferredSize(new Dimension(6, 6));
                        redDot.setMaximumSize(new Dimension(6, 6));
                        redDot.setBackground(Color.RED);
                        redDot.setOpaque(true);
                        redDot.setBorder(BorderFactory.createEmptyBorder());
                        redDot.setAlignmentX(Component.CENTER_ALIGNMENT);
                        redDot.setAlignmentY(Component.TOP_ALIGNMENT);

                        dayButton.setLayout(new BorderLayout());
                        dayButton.add(redDot, BorderLayout.SOUTH);  // place dot at bottom
                        dayButton.revalidate();
                        dayButton.repaint();
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    // --- Show task notes dialog ---
    private void showNotesDialog(String taskName, String existingNotes) {
        if (notesDialog != null && notesDialog.isVisible()) {
            notesDialog.dispose(); // Close any existing dialog
        }

        notesDialog = new JDialog(frame, "Task Notes - " + taskName, true);
        notesDialog.setSize(400, 300);
        notesDialog.setLayout(new BorderLayout());

        notesTextArea = new JTextArea(existingNotes != null ? existingNotes : "");
        notesTextArea.setLineWrap(true);
        notesTextArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(notesTextArea);
        notesDialog.add(scrollPane, BorderLayout.CENTER);

        JButton saveButton = createModernButton("Save Notes ✔");
        saveButton.addActionListener(e -> {
            String updatedNotes = notesTextArea.getText();
            saveTaskNotes(taskName, updatedNotes);  // Optional: persist to DB
            notesDialog.dispose();
        });

        notesDialog.add(saveButton, BorderLayout.SOUTH);
        notesDialog.setLocationRelativeTo(frame);
        notesDialog.setVisible(true);
    }

    // --- Save task notes to backend ---
    private void saveTaskNotes(String taskName, String notes) {
        try {
            String urlString = "http://localhost:8080/api/tasks/" + userId;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONArray tasksArray = new JSONArray(response.toString());
            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject task = tasksArray.getJSONObject(i);
                if (task.getString("name").equals(taskName)) {
                    Long taskId = task.getLong("id");
                    task.put("notes", notes);  // Add notes field

                    // Send update request
                    HttpURLConnection putConn = (HttpURLConnection) new URL("http://localhost:8080/api/tasks/" + taskId).openConnection();
                    putConn.setRequestMethod("PUT");
                    putConn.setRequestProperty("Content-Type", "application/json");
                    putConn.setDoOutput(true);

                    try (OutputStream os = putConn.getOutputStream()) {
                        byte[] input = task.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    putConn.getInputStream().close();  // To send the request
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Fetch task notes from backend ---
    private String fetchTaskNotes(String taskName) {
        try {
            String urlString = "http://localhost:8080/api/tasks/" + userId;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONArray tasksArray = new JSONArray(response.toString());
            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject task = tasksArray.getJSONObject(i);
                if (task.getString("name").equals(taskName)) {
                    return task.optString("notes", ""); // Return notes if exists
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // --- Rounded background label for category ---
    class RoundedLabel extends JLabel {
        private final Color backgroundColor;
        private final int arcWidth;
        private final int arcHeight;

        public RoundedLabel(String text, Color backgroundColor) {
            super(text);
            this.backgroundColor = backgroundColor;
            this.arcWidth = 20;   // How round (width)
            this.arcHeight = 20;  // How round (height)
            setOpaque(false);     // We handle background manually
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
            super.paintComponent(g2d);
            g2d.dispose();
        }
    }

    // --- Navigate to HomePage ---
    private void navigateToHomePage() {
        frame.dispose();
        new HomePage(userId, username);
    }

}

// --- Model Class for Task Representation ---
class Task {
    private Long id;
    private String name;
    private LocalDate date;
    private String category;
    private String description;
    private Long userId;
    private boolean completed = false;

    public Task(Long id, String name, LocalDate date, String category, String description, Long userId, boolean completed) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.category = category;
        this.description = description;
        this.userId = userId;
        this.completed = completed;
    }

    public Task(Long id, String name, LocalDate date, String category, String description, Long userId) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.category = category;
        this.description = description;
        this.userId = userId;
    }

    public Task(String name, LocalDate date, String category, String description, Long userId) {
        this.name = name;
        this.date = date;
        this.category = category;
        this.description = description;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Long getUserId() {
        return userId;
    }

    // Setter methods for updating the task
    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }


    public boolean isCompleted() {
        return completed;
    }
}
