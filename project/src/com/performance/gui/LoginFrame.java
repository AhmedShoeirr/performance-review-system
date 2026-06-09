package com.performance.gui;

import com.performance.model.User;
import com.performance.model.Admin;
import com.performance.model.UserStatus;
import com.performance.service.UserService;
import com.performance.util.DataStore;
import com.performance.util.UITheme;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        UITheme.applyGlobalTheme();

        setTitle("Performance Review System");
        setSize(450, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main container with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BACKGROUND);

        // Check for first run
        UserService userService = UserService.getInstance();
        if (userService.getAllUsers().isEmpty()) {
            Admin admin = new Admin("admin", "admin123", "Super Admin");
            DataStore.getInstance().addUser(admin);
            DataStore.getInstance().saveData();
            JOptionPane.showMessageDialog(this,
                    "First run detected.\nDefault Admin created:\nUsername: admin\nPassword: admin123");
        }

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new GridBagLayout());

        JLabel titleLabel = new JLabel("🏆 Performance Review System");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setBackground(UITheme.BACKGROUND);
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 25, 8, 25);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(UITheme.LABEL_FONT);
        userLabel.setForeground(UITheme.TEXT_SECONDARY);
        formPanel.add(userLabel, gbc);

        gbc.gridy = 1;
        usernameField = new JTextField(20);
        UITheme.styleTextField(usernameField);
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridy = 2;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UITheme.LABEL_FONT);
        passLabel.setForeground(UITheme.TEXT_SECONDARY);
        formPanel.add(passLabel, gbc);

        gbc.gridy = 3;
        passwordField = new JPasswordField(20);
        UITheme.styleTextField(passwordField);
        formPanel.add(createPasswordPanel(passwordField), gbc);

        // Buttons
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 25, 5, 25);
        JButton loginButton = new JButton("🔐 Login");
        UITheme.styleButton(loginButton);
        loginButton.setPreferredSize(new Dimension(0, 40));
        formPanel.add(loginButton, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 25, 8, 25);
        JButton registerButton = new JButton("📝 Create Account");
        UITheme.styleSecondaryButton(registerButton);
        registerButton.setPreferredSize(new Dimension(0, 35));
        formPanel.add(registerButton, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        add(mainPanel);

        loginButton.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> showRegisterDialog());

        // Enter key triggers login
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel createPasswordPanel(JPasswordField passField) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BACKGROUND);
        UITheme.styleTextField(passField);
        panel.add(passField, BorderLayout.CENTER);

        // Create eye icon button with custom painting
        JToggleButton showPassBtn = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int eyeW = 18;
                int eyeH = 10;
                int x = (w - eyeW) / 2;
                int y = (h - eyeH) / 2;

                g2.setColor(isSelected() ? UITheme.PRIMARY : UITheme.TEXT_SECONDARY);

                // Draw eye shape (oval)
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, eyeW, eyeH);

                // Draw pupil (filled circle)
                int pupilSize = 6;
                g2.fillOval(x + (eyeW - pupilSize) / 2, y + (eyeH - pupilSize) / 2, pupilSize, pupilSize);

                // Draw slash if hidden (not selected)
                if (!isSelected()) {
                    g2.setStroke(new BasicStroke(2f));
                    g2.setColor(UITheme.DANGER);
                    g2.drawLine(x - 2, y + eyeH + 2, x + eyeW + 2, y - 2);
                }

                g2.dispose();
            }
        };
        showPassBtn.setFocusable(false);
        showPassBtn.setPreferredSize(new Dimension(40, 35));
        showPassBtn.setBackground(UITheme.CARD_BG);
        showPassBtn.setBorderPainted(false);
        showPassBtn.setToolTipText("Show/Hide Password");
        showPassBtn.addActionListener(e -> {
            passField.setEchoChar(showPassBtn.isSelected() ? (char) 0 : '•');
            showPassBtn.repaint();
        });
        panel.add(showPassBtn, BorderLayout.EAST);
        return panel;
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        UserService userService = UserService.getInstance();
        User user = userService.authenticate(username, password);

        if (user != null) {
            if (UserStatus.PENDING == user.getStatus()) {
                JOptionPane.showMessageDialog(this, "Account is PENDING approval.", "Access Denied",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (UserStatus.SUSPENDED == user.getStatus()) {
                JOptionPane.showMessageDialog(this, "Account is SUSPENDED.", "Access Denied",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Welcome " + user.getName() + "!");

            try {
                JFrame frame;
                if (user instanceof Admin) {
                    frame = new JFrame("Admin Dashboard - " + user.getName());
                    frame.add(new AdminPanel());
                    frame.setSize(1100, 750);
                } else {
                    int tier = user.getTierLevel();
                    boolean hasDirectReports = user.getDirectReports() != null && !user.getDirectReports().isEmpty();

                    if (tier == userService.getMaxTiers()) {
                        frame = new JFrame("Highboard Manager - " + user.getName());
                        frame.add(new HighboardManagerPanel(user));
                        frame.setSize(1050, 700);
                    } else if (tier == 1 && !hasDirectReports) {
                        frame = new JFrame("Employee Dashboard - " + user.getName());
                        frame.add(new EmployeePanel(user));
                        frame.setSize(900, 650);
                    } else {
                        frame = new JFrame("Manager Dashboard - " + user.getName());
                        frame.add(new ManagerEmployeePanel(user));
                        frame.setSize(1000, 700);
                    }
                }

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                this.dispose();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error launching dashboard: " + e.getMessage(), "Launch Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Create New Account", true);
        dialog.setSize(380, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridwidth = 2;

        JTextField regUser = new JTextField(20);
        JPasswordField regPass = new JPasswordField(20);
        JTextField regName = new JTextField(20);
        UITheme.styleTextField(regUser);
        UITheme.styleTextField(regPass);
        UITheme.styleTextField(regName);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(createLabel("Full Name"), gbc);
        gbc.gridy = 1;
        panel.add(regName, gbc);
        gbc.gridy = 2;
        panel.add(createLabel("Username"), gbc);
        gbc.gridy = 3;
        panel.add(regUser, gbc);
        gbc.gridy = 4;
        panel.add(createLabel("Password"), gbc);
        gbc.gridy = 5;
        panel.add(createPasswordPanel(regPass), gbc);

        JButton submit = new JButton("✓ Register");
        UITheme.styleButton(submit);
        submit.setPreferredSize(new Dimension(0, 38));
        gbc.gridy = 6;
        gbc.insets = new Insets(15, 0, 5, 0);
        panel.add(submit, gbc);

        dialog.add(panel);

        submit.addActionListener(e -> {
            String u = regUser.getText().trim();
            String p = new String(regPass.getPassword());
            String n = regName.getText().trim();

            if (u.isEmpty() || p.isEmpty() || n.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields required.");
                return;
            }

            if (!isValidPassword(p)) {
                JOptionPane.showMessageDialog(dialog,
                        "Password must contain:\n• At least 8 characters\n• One uppercase letter\n• One lowercase letter\n• One number\n• One symbol");
                return;
            }

            UserService userService = UserService.getInstance();
            if (userService.findByUsername(u) != null) {
                JOptionPane.showMessageDialog(dialog, "Username already taken.");
                return;
            }

            userService.registerUser(u, p, n);

            JOptionPane.showMessageDialog(dialog, "Registration Successful!\nWait for Admin approval.");
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.TEXT_SECONDARY);
        return label;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8)
            return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            else if (Character.isLowerCase(c))
                hasLower = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else
                hasSymbol = true;
        }
        return hasUpper && hasLower && hasDigit && hasSymbol;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
