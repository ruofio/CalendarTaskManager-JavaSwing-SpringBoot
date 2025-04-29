// --- Package and Imports ---
package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

// --- AllTasksPage: Displays all user's tasks with filter and notes ---
public class AllTasksPage {
    private JFrame frame;
    private String userId;
    private String username;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel emptyMessageLabel;
    private JPanel tablePanel;
    private JTextArea notesArea;
    private JPanel centerSplitPanel;
    private JPanel mainPanel;
    private JPanel fullEmptyPanel;
    private MultiSelectComboBoxPanel multiSelectPanel;

    // --- Constructor ---
    public AllTasksPage(String userId, String username) {
        this.userId = userId;
        this.username = username;

        // Setup main frame
        frame = new JFrame("All Tasks");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        frame.setLayout(new BorderLayout());

        // Main panel with gradient background
        mainPanel = new JPanel() {
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
        mainPanel.setLayout(new BorderLayout());

        // Header label for "All Tasks"
        JLabel headingLabel = new JLabel("All Tasks", JLabel.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headingLabel.setForeground(Color.WHITE);
        headingLabel.setOpaque(true);
        headingLabel.setBackground(new Color(33, 150, 243));
        headingLabel.setPreferredSize(new Dimension(0, 60));
        mainPanel.add(headingLabel, BorderLayout.NORTH);

        // Filter Panel (for category filtering)
        JPanel filterPanel = new JPanel();
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel filterLabel = new JLabel("Filter by Category: ");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 16));
        filterPanel.add(filterLabel);

         multiSelectPanel = new MultiSelectComboBoxPanel(
                new String[]{"General", "Holiday", "Meeting", "Social", "Personal"},
                this::fetchAndDisplayTasks
        );
        filterPanel.add(multiSelectPanel);
        // Add the filter panel in the NORTH region
        mainPanel.add(filterPanel, BorderLayout.NORTH);

        // Setup task table
        String[] columnNames = {"Completed", "Task Name", "Date", "Category", "Description"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                Object value = getValueAt(row, 1);
                if (value instanceof String && ((String) value).contains("No tasks available")) {
                    return false;
                }
                return column == 0;
            }


            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        taskTable = new JTable(tableModel);
        taskTable.setOpaque(false);
        ((DefaultTableCellRenderer) taskTable.getDefaultRenderer(Object.class)).setOpaque(false);

        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = taskTable.rowAtPoint(e.getPoint());
                int col = taskTable.columnAtPoint(e.getPoint());

                if (col == 1 && row >= 0) {
                    String taskName = (String) taskTable.getValueAt(row, 1);
                    String notes = fetchNotesForTask(taskName);  // Custom method to get notes
                    notesArea.setText(notes != null && !notes.isEmpty() ? notes : "No notes available for this task.");
                }
            }
        });

        taskTable.setRowSelectionAllowed(false);
        taskTable.setCellSelectionEnabled(false);
        taskTable.setFocusable(false);

        taskTable.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column == 0) {
                Boolean isChecked = (Boolean) taskTable.getValueAt(row, 0);
                String taskName = (String) taskTable.getValueAt(row, 1);

                // Send status update to backend
                updateTaskCompletion(taskName, isChecked != null && isChecked);

                // Repaint row to apply blue background or default
                taskTable.repaint();
            }
        });

        // For normal text cells
        taskTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Boolean isCompleted = (Boolean) table.getValueAt(row, 0);

                if (isCompleted != null && isCompleted) {
                    c.setBackground(new Color(181, 181, 181));
                    c.setForeground(new Color(112, 112, 112));
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(180, 210, 255));
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(new Color(190, 220, 255));
                        c.setForeground(Color.BLACK);
                    }
                }

                setHorizontalAlignment(CENTER);
                return c;
            }
        });


        taskTable.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(Boolean.TRUE.equals(value));
                checkBox.setHorizontalAlignment(JLabel.CENTER);
                checkBox.setOpaque(true);

                Boolean isCompleted = (Boolean) table.getValueAt(row, 0);

                if (isCompleted != null && isCompleted) {
                    checkBox.setBackground(new Color(181, 181, 181));
                    checkBox.setForeground(new Color(112, 112, 112));


                } else {
                    if (row % 2 == 0) {
                        checkBox.setBackground(new Color(180, 210, 255));
                    } else {
                        checkBox.setBackground(new Color(190, 220, 255));
                    }
                }

                return checkBox;
            }
        });


        taskTable.setFont(new Font("Arial", Font.PLAIN, 14));
        taskTable.setRowHeight(40);

        // Set blue background color for the header row
        taskTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel headerLabel = new JLabel(value.toString(), JLabel.CENTER);
                headerLabel.setOpaque(true);
                headerLabel.setBackground(new Color(120, 170, 255));
                headerLabel.setForeground(Color.WHITE);
                headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
                return headerLabel;
            }
        });


        // Scroll pane for table
        JScrollPane scrollPane = new JScrollPane(taskTable);


        // Table container panels
        tablePanel = new JPanel(new CardLayout());
        scrollPane = new JScrollPane(taskTable);

        // Empty message panel when no tasks
        emptyMessageLabel = new JLabel("No tasks available for the selected category");
        emptyMessageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        emptyMessageLabel.setForeground(Color.RED);
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        emptyPanel.add(emptyMessageLabel, gbc);
        emptyMessageLabel.setOpaque(true);
        emptyPanel.setBackground(new Color(255, 255, 255, 150));
        emptyMessageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        emptyMessageLabel.setForeground(new Color(255, 80, 80));
        tablePanel.add(scrollPane, "TABLE");
        fullEmptyPanel = new JPanel(new GridBagLayout());
        fullEmptyPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcEmpty = new GridBagConstraints();
        gbcEmpty.gridx = 0;
        gbcEmpty.gridy = 0;
        emptyMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        fullEmptyPanel.add(emptyMessageLabel, gbcEmpty);

        // Notes area setup
        notesArea = new JTextArea(4, 60);
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(new Font("Arial", Font.PLAIN, 14));
        notesArea.setBorder(BorderFactory.createTitledBorder("Task Notes"));
        notesArea.setBackground(Color.WHITE);

        // Split panel for table and notes
        centerSplitPanel = new JPanel(new GridLayout(1, 2));
        centerSplitPanel.setBackground(Color.WHITE);

        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 60, 60, 30),
                BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 2)
        ));

        leftWrapper.setBackground(Color.WHITE);
        leftWrapper.add(tablePanel, BorderLayout.CENTER);
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 30, 60, 60),
                BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 2)
        ));

        rightWrapper.setBackground(Color.WHITE);
        rightWrapper.add(notesArea, BorderLayout.CENTER);
        centerSplitPanel.add(leftWrapper);
        centerSplitPanel.add(rightWrapper);
        // Add split panel to main layout
        mainPanel.add(centerSplitPanel, BorderLayout.CENTER);


        // Footer button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton goHomeButton = createModernButton("Go to Home ⾕");
        goHomeButton.addActionListener(e -> navigateToHomePage());
        buttonPanel.add(goHomeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        frame.add(mainPanel);
        frame.setVisible(true);

        // Fetch tasks initially
        fetchAndDisplayTasks();
    }
    // Fetch tasks from server and display in table
    private void fetchAndDisplayTasks() {
        CardLayout cl = (CardLayout) tablePanel.getLayout();
        try {
            java.util.List<String> selectedCategories = multiSelectPanel.getSelectedItems();
            String finalUrl = "http://localhost:8080/api/tasks/" + userId;
            if (!selectedCategories.contains("All")) {
                try {
                    String encodedParams = selectedCategories.stream()
                            .map(cat -> {
                                try {
                                    return "categories=" + URLEncoder.encode(cat, "UTF-8");
                                } catch (Exception ex) {
                                    return "";
                                }
                            })
                            .collect(Collectors.joining("&"));
                    finalUrl += "?" + encodedParams;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            HttpURLConnection connection = (HttpURLConnection) new URL(finalUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            int data;
            while ((data = reader.read()) != -1) {
                responseBuilder.append((char) data);
            }
            reader.close();

            String rawResponse = responseBuilder.toString();
            if (rawResponse.startsWith("[")) {
                JSONArray tasksArray = new JSONArray(rawResponse);
                tableModel.setRowCount(0);  // Clear previous tasks

                if (tasksArray.length() == 0) {
                    // REMOVE the task/notes section
                    mainPanel.remove(centerSplitPanel);

                    // ADD the full-screen empty state
                    mainPanel.add(fullEmptyPanel, BorderLayout.CENTER);

                    // Refresh UI
                    mainPanel.revalidate();
                    mainPanel.repaint();
                } else {
                    // REMOVE the empty state
                    mainPanel.remove(fullEmptyPanel);

                    // ADD the task/notes panel again
                    mainPanel.add(centerSplitPanel, BorderLayout.CENTER);

                    notesArea.setVisible(true);
                    for (int i = 0; i < tasksArray.length(); i++) {
                        JSONObject taskObject = tasksArray.getJSONObject(i);
                        String taskName = taskObject.getString("name");
                        String taskDate = taskObject.getString("date");
                        String taskCategory = taskObject.getString("category");
                        String taskDescription = taskObject.isNull("description") ? "" : taskObject.getString("description");
                        boolean completed = taskObject.optBoolean("completed", false);

                        tableModel.addRow(new Object[]{completed, taskName, taskDate, taskCategory, taskDescription});
                    }

                    // Refresh UI
                    mainPanel.revalidate();
                    mainPanel.repaint();
                }

            } else {
                JOptionPane.showMessageDialog(frame, "Error fetching tasks.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error fetching tasks.");
        }
    }

    // Create modern styled button
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

    // Update task completion status on backend
    private void updateTaskCompletion(String taskName, boolean completed) {
        try {
            String urlString = "http://localhost:8080/api/tasks/" + userId;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            // Find matching task by name (you can improve by storing IDs)
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
                    task.put("completed", completed);  // Update the field
                    sendPutRequest(taskId, task.toString());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send PUT request to backend to update task
    private void sendPutRequest(Long taskId, String jsonInput) throws IOException {
        String url = "http://localhost:8080/api/tasks/" + taskId;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        connection.getInputStream().close(); // Trigger the request
    }

    // Fetch notes for a selected task
    private String fetchNotesForTask(String taskName) {
        try {
            String urlString = "http://localhost:8080/api/tasks/" + userId;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
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
                    return task.optString("notes", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // Inner class for multi-select dropdown panel
    class MultiSelectComboBoxPanel extends JPanel {
        private final String[] options;
        private final java.util.List<String> selected = new java.util.ArrayList<>();
        private final JLabel displayLabel;
        private final JPopupMenu popupMenu;
        private final JCheckBoxMenuItem allCheckBox;

        public MultiSelectComboBoxPanel(String[] items, Runnable onChangeCallback) {
            this.options = items;
            this.setLayout(new BorderLayout());
            this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            this.setBackground(Color.WHITE);
            this.setPreferredSize(new Dimension(180, 30));

            displayLabel = new JLabel("All");
            displayLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            displayLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            this.add(displayLabel, BorderLayout.CENTER);

            popupMenu = new JPopupMenu();

            // Add 'All' checkbox
            allCheckBox = new JCheckBoxMenuItem("All", true);
            popupMenu.add(allCheckBox);
            allCheckBox.addItemListener(e -> {
                selected.clear();
                if (allCheckBox.isSelected()) {
                    popupMenu.getComponents();
                    for (Component comp : popupMenu.getComponents()) {
                        if (comp instanceof JCheckBoxMenuItem && comp != allCheckBox) {
                            ((JCheckBoxMenuItem) comp).setSelected(false);
                        }
                    }
                    updateLabel();
                    onChangeCallback.run();
                }
            });

            // Add category checkboxes
            for (String option : items) {
                JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem(option);
                popupMenu.add(checkBox);
                checkBox.addItemListener(e -> {
                    if (checkBox.isSelected()) {
                        selected.add(option);
                        allCheckBox.setSelected(false);
                    } else {
                        selected.remove(option);
                        if (selected.isEmpty()) {
                            allCheckBox.setSelected(true);
                        }
                    }
                    updateLabel();
                    onChangeCallback.run();
                });
            }

            // Open dropdown on click
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    popupMenu.show(MultiSelectComboBoxPanel.this, 0, getHeight());
                }
            });

            updateLabel(); // Initialize label
        }

        public java.util.List<String> getSelectedItems() {
            return selected.isEmpty() ? java.util.Arrays.asList("All") : selected;
        }

        private void updateLabel() {
            displayLabel.setText(selected.isEmpty() ? "All" : String.join(", ", selected));
        }
    }
    // Navigate back to HomePage
    private void navigateToHomePage() {
        frame.dispose();
        new HomePage(userId, username);  // Assuming HomePage takes userId as a parameter
    }

}
