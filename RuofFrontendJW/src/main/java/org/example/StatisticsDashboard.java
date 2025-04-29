// Package and imports
package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

// Statistics Dashboard to visualize task data
public class StatisticsDashboard extends JFrame {

    // Constructor to setup the statistics dashboard
    public StatisticsDashboard(Map<String, Integer> completionStats,
                               Map<String, Integer> categoryStats,
                               Map<String, Integer> dayStats,
                               String userId,
                               String username)  {
        setTitle("Task Statistics Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ---- Main Panel Layout ----
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);


        // ---- Header Label ----
        JLabel headerLabel = new JLabel("📊 Task Statistics Dashboard", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(33, 150, 243));
        headerLabel.setPreferredSize(new Dimension(0, 70));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // ---- Charts Panel (Center) ----
        JPanel chartsPanel = new JPanel();
        chartsPanel.setLayout(new GridLayout(1, 3, 20, 20));
        chartsPanel.setBackground(Color.WHITE);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Pie Chart: Completed vs. In Progress
        DefaultPieDataset completionDataset = new DefaultPieDataset();
        completionStats.forEach(completionDataset::setValue);
        JFreeChart completionChart = ChartFactory.createPieChart(
                "Completed vs. In Progress", completionDataset, true, true, false);
        completionChart.getTitle().setPaint(new Color(33, 150, 243));

        PiePlot plot1 = (PiePlot) completionChart.getPlot();
        plot1.setSectionPaint("Completed", new Color(33, 150, 243));
        plot1.setSectionPaint("In Progress", new Color(100, 181, 246));
        plot1.setBackgroundPaint(Color.WHITE);

        // Pie Chart: Categories Breakdown
        DefaultPieDataset categoryDataset = new DefaultPieDataset();
        categoryStats.forEach(categoryDataset::setValue);
        JFreeChart categoryChart = ChartFactory.createPieChart(
                "Task Categories", categoryDataset, true, true, false);
        PiePlot plot2 = (PiePlot) categoryChart.getPlot();
        plot2.setBackgroundPaint(Color.WHITE);
        categoryChart.getTitle().setPaint(new Color(33, 150, 243));

        // Apply custom blue shades
        plot2.setSectionPaint("General", new Color(33, 150, 243));
        plot2.setSectionPaint("Holiday", new Color(79, 195, 247));
        plot2.setSectionPaint("Meeting", new Color(3, 169, 244));
        plot2.setSectionPaint("Social", new Color(129, 212, 250));
        plot2.setSectionPaint("Personal", new Color(2, 136, 209));

        plot2.setBackgroundPaint(Color.WHITE);

        // Bar Chart: Most Productive Day
        DefaultCategoryDataset dayDataset = new DefaultCategoryDataset();
        dayStats.forEach((day, count) -> dayDataset.addValue(count, "Tasks", day));
        JFreeChart dayChart = ChartFactory.createBarChart(
                "Most Productive Day", "Day", "Task Count", dayDataset);
        dayChart.getTitle().setPaint(new Color(33, 150, 243));

        CategoryPlot plot3 = (CategoryPlot) dayChart.getPlot();
        plot3.setBackgroundPaint(Color.WHITE);
        BarRenderer renderer = (BarRenderer) plot3.getRenderer();
        renderer.setSeriesPaint(0, new Color(100, 150, 250));

        // Add charts to panel
        chartsPanel.add(new ChartPanel(completionChart));
        chartsPanel.add(new ChartPanel(categoryChart));
        chartsPanel.add(new ChartPanel(dayChart));

        mainPanel.add(chartsPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setVisible(true);

        // Home button panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        JButton goHomeButton = createStyledButton("Go to Home ⾕");
        goHomeButton.addActionListener(e -> {
            dispose();
            new HomePage(userId, username);
        });
        bottomPanel.add(goHomeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
    // Create a custom button with color and hover effects
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

}
