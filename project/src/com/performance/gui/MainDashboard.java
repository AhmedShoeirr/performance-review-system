package com.performance.gui;

import com.performance.model.*;
import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {
    private User currentUser;

    public MainDashboard(User user) {
        this.currentUser = user;
        setTitle("Dashboard - " + user.getName() + " (" + user.getClass().getSimpleName() + ")");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        if (user instanceof Admin) {
            contentPanel.add(new AdminPanel(), BorderLayout.CENTER);
        } else if (user.getDirectReports() != null && !user.getDirectReports().isEmpty()) {
            if (user.getManager() == null) {
                contentPanel.add(new HighboardManagerPanel(user), BorderLayout.CENTER);
            } else {
                contentPanel.add(new ManagerEmployeePanel(user), BorderLayout.CENTER);
            }
        } else {
            contentPanel.add(new EmployeePanel(user), BorderLayout.CENTER);
        }

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
