package com.loginSystem.client.ui;

import com.loginSystem.common.Message;
import com.loginSystem.common.RequestType;
import com.loginSystem.common.User;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/**
 * Panel quản lý người dùng cho Admin
 * Hiển thị danh sách user với CRUD operations
 */
public class AdminUserManagementPanel extends JPanel {
    private final JFrame parentFrame;
    private final Function<Message, Message> requestSender;
    private final Gson gson;
    private final DefaultTableModel model;
    private final JTable table;
    private final Timer autoRefreshTimer;

    public AdminUserManagementPanel(JFrame parentFrame, Function<Message, Message> requestSender, Gson gson) {
        this.parentFrame = parentFrame;
        this.requestSender = requestSender;
        this.gson = gson;

        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header panel with gradient background
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(59, 130, 246), 
                                                      getWidth(), 0, new Color(147, 51, 234));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 120));
        headerPanel.setBorder(new EmptyBorder(25, 30, 30, 30));
        
        JLabel titleLabel = new JLabel("👥 Quản lý người dùng");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Quản lý thông tin và quyền của người dùng trong hệ thống");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        
        JPanel titleContainer = new JPanel(new BorderLayout(0, 8));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel, BorderLayout.NORTH);
        titleContainer.add(subtitleLabel, BorderLayout.CENTER);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Table model
        model = new DefaultTableModel(
                new Object[]{"👤 Username", "📝 Họ tên", "📧 Email", "📱 Điện thoại", "🟢 Trạng thái", "👑 Admin", "🔒 Khóa"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5 || columnIndex == 6)
                    return Boolean.class;
                return String.class;
            }
        };

        // Table styling
        table = new JTable(model);
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.setRowHeight(45);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(new Color(30, 58, 138));
        table.setGridColor(new Color(229, 231, 235));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setShowVerticalLines(false);

        // Header styling
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(51, 65, 85));
        table.getTableHeader().setPreferredSize(new Dimension(0, 48));
        table.getTableHeader().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(190);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createLineBorder(Color.WHITE, 3)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Control buttons panel with modern design
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 1, new Color(226, 232, 240)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JButton refreshBtn = createModernButton("🔄 Làm mới", new Color(59, 130, 246), Color.WHITE, 140, 42);
        JButton editBtn = createModernButton("✏️ Chỉnh sửa", new Color(16, 185, 129), Color.WHITE, 150, 42);
        JButton deleteBtn = createModernButton("🗑️ Xóa", new Color(239, 68, 68), Color.WHITE, 120, 42);

        // Auto refresh timer (10 seconds)
        autoRefreshTimer = new Timer(10000, e -> loadUsers());
        autoRefreshTimer.start();

        // Button actions
        refreshBtn.addActionListener(e -> {
            loadUsers();
            JOptionPane.showMessageDialog(parentFrame, "✅ Đã làm mới danh sách người dùng",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        editBtn.addActionListener(e -> handleEditUser());
        deleteBtn.addActionListener(e -> handleDeleteUser());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        // Status panel with modern styling
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(226, 232, 240)),
            new EmptyBorder(0, 20, 12, 20)
        ));
        JLabel statusLabel = new JLabel("🔄 Tự động cập nhật mỗi 10 giây");
        statusLabel.setFont(new Font("Inter", Font.ITALIC, 13));
        statusLabel.setForeground(new Color(100, 116, 139));
        statusPanel.add(statusLabel);

        // Main content with card design
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Wrapper panel for spacing
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(249, 250, 251));
        wrapperPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        wrapperPanel.add(contentPanel, BorderLayout.CENTER);

        add(wrapperPanel, BorderLayout.CENTER);

        // Load initial data
        loadUsers();
    }

    private void loadUsers() {
        // Get all users
        Message resp = requestSender.apply(new Message(RequestType.GET_ALL_USERS, ""));
        
        if (resp != null && resp.isSuccess()) {
            model.setRowCount(0);
            List<User> list = gson.fromJson(resp.getContent(),
                    new com.google.gson.reflect.TypeToken<List<User>>() {
                    }.getType());
            for (User u : list) {
                String status = u.isOnline() ? "🟢 Online" : "⚫ Offline";
                model.addRow(new Object[]{u.getUsername(), u.getFullName(), u.getEmail(),
                        u.getPhone(), status, u.isAdmin(), u.isLocked()});
            }
        }
    }

    private void handleEditUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(parentFrame, "❌ Vui lòng chọn một người dùng để chỉnh sửa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) model.getValueAt(row, 0);
        String fullName = (String) model.getValueAt(row, 1);
        String email = (String) model.getValueAt(row, 2);
        String phone = (String) model.getValueAt(row, 3);
        // Skip status column at index 4
        boolean isAdmin = (Boolean) model.getValueAt(row, 5);
        boolean isLocked = (Boolean) model.getValueAt(row, 6);

        // Create modern dialog
        JDialog dialog = new JDialog(parentFrame, "Chỉnh sửa người dùng", true);
        dialog.setSize(650, 850);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setResizable(false);
        
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(249, 250, 251));
        
        // Header with gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(99, 102, 241),
                    getWidth(), 0, new Color(168, 85, 247));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(550, 100));
        headerPanel.setLayout(new BorderLayout());
        
        JPanel headerContent = new JPanel();
        headerContent.setOpaque(false);
        headerContent.setLayout(new BoxLayout(headerContent, BoxLayout.Y_AXIS));
        headerContent.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("✏️ Chỉnh sửa người dùng");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Chỉnh sửa: " + username);
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        headerContent.add(titleLabel);
        headerContent.add(subtitleLabel);
        headerPanel.add(headerContent, BorderLayout.CENTER);
        
        // Form panel with modern styling
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(20, 30, 20, 30),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(25, 30, 25, 30)
            )
        ));
        
        JTextField usernameField = createModernTextField();
        JTextField fnField = createModernTextField();
        JTextField emailField = createModernTextField();
        JTextField phoneField = createModernTextField();
        usernameField.setText(username);
        fnField.setText(fullName);
        emailField.setText(email);
        phoneField.setText(phone);

        // Modern styled checkboxes
        JCheckBox adminBox = createModernCheckbox("👑 Quyền Administrator", isAdmin);
        JCheckBox lockedBox = createModernCheckbox("🔒 Khóa tài khoản", isLocked);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username (có thể chỉnh sửa bởi admin)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel userLabel = new JLabel("👤 Username:");
        userLabel.setFont(new Font("Inter", Font.BOLD, 14));
        userLabel.setForeground(new Color(51, 65, 85));
        formPanel.add(userLabel, gbc);
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        formPanel.add(usernameField, gbc);

        // Họ tên
        gbc.gridy = 2;
        gbc.insets = new Insets(18, 0, 8, 0);
        JLabel fnLabel = new JLabel("📝 Họ tên:");
        fnLabel.setFont(new Font("Inter", Font.BOLD, 14));
        fnLabel.setForeground(new Color(51, 65, 85));
        formPanel.add(fnLabel, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(fnField, gbc);

        // Email
        gbc.gridy = 4;
        gbc.insets = new Insets(18, 0, 8, 0);
        JLabel emailLbl = new JLabel("📧 Email:");
        emailLbl.setFont(new Font("Inter", Font.BOLD, 14));
        emailLbl.setForeground(new Color(51, 65, 85));
        formPanel.add(emailLbl, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(emailField, gbc);

        // Điện thoại
        gbc.gridy = 6;
        gbc.insets = new Insets(18, 0, 8, 0);
        JLabel phoneLbl = new JLabel("📱 Điện thoại:");
        phoneLbl.setFont(new Font("Inter", Font.BOLD, 14));
        phoneLbl.setForeground(new Color(51, 65, 85));
        formPanel.add(phoneLbl, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(phoneField, gbc);

        // Checkboxes section
        gbc.gridy = 8;
        gbc.insets = new Insets(25, 0, 8, 0);
        formPanel.add(adminBox, gbc);
        gbc.gridy = 9;
        gbc.insets = new Insets(12, 0, 8, 0);
        formPanel.add(lockedBox, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(5, 30, 30, 30));
        
        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(new Font("Inter", Font.BOLD, 14));
        cancelBtn.setPreferredSize(new Dimension(100, 42));
        cancelBtn.setForeground(new Color(71, 85, 105));
        cancelBtn.setBackground(new Color(241, 245, 249));
        cancelBtn.setBorder(BorderFactory.createEmptyBorder());
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        styleModernButton(cancelBtn, new Color(241, 245, 249));
        
        JButton okBtn = new JButton("💾 Lưu thay đổi");
        okBtn.setFont(new Font("Inter", Font.BOLD, 14));
        okBtn.setPreferredSize(new Dimension(160, 42));
        okBtn.setForeground(Color.WHITE);
        okBtn.setBackground(new Color(59, 130, 246));
        okBtn.setBorder(BorderFactory.createEmptyBorder());
        okBtn.setFocusPainted(false);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        styleModernButton(okBtn, new Color(59, 130, 246));
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(okBtn);
        
        // Wrap form in scroll pane for better visibility
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        
        // Assemble dialog
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        dialog.setContentPane(mainPanel);
        
        final boolean[] result = {false};
        
        okBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        
        dialog.setVisible(true);
        
        if (result[0]) {
            // Validation
            String newUsername = usernameField.getText().trim();
            
            if (newUsername.isEmpty()) {
                showValidationError("Username không được để trống!");
                return;
            }
            if (newUsername.length() < 3) {
                showValidationError("Username phải có ít nhất 3 ký tự!");
                return;
            }
            if (!newUsername.matches("^[a-zA-Z0-9_]+$")) {
                showValidationError("Username chỉ được chứa chữ cái, số và dấu gạch dưới!");
                return;
            }
            if (!isValidFullName(fnField.getText())) {
                showValidationError("Họ tên không hợp lệ!");
                return;
            }
            if (!isValidEmail(emailField.getText())) {
                showValidationError("Email không hợp lệ!");
                return;
            }
            if (!isValidPhone(phoneField.getText())) {
                showValidationError("Số điện thoại không hợp lệ!");
                return;
            }

            // Update username if changed
            String targetUsername = username; // Track current username for subsequent requests
            if (!newUsername.equals(username)) {
                Message usernameReq = new Message(RequestType.UPDATE_USERNAME, username + "," + newUsername);
                Message usernameResp = requestSender.apply(usernameReq);
                
                if (usernameResp == null || !usernameResp.isSuccess()) {
                    JOptionPane.showMessageDialog(parentFrame, 
                        "❌ Không thể cập nhật username: " + 
                        (usernameResp != null ? usernameResp.getError() : "Lỗi kết nối"),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                targetUsername = newUsername; // Update target for subsequent operations
            }

            // Update user info
            User u = new User();
            u.setUsername(targetUsername);
            u.setFullName(fnField.getText().trim());
            u.setEmail(emailField.getText().trim());
            u.setPhone(phoneField.getText().trim());
            u.setPassword("");

            Message req = new Message(RequestType.UPDATE_USER, "");
            req.setUser(u);
            requestSender.apply(req);

            // Update role
            Message roleReq = new Message(RequestType.SET_ADMIN, targetUsername + "," + adminBox.isSelected());
            requestSender.apply(roleReq);

            // Lock/unlock
            if (lockedBox.isSelected()) {
                requestSender.apply(new Message(RequestType.LOCK_USER, targetUsername));
            } else {
                requestSender.apply(new Message(RequestType.UNLOCK_USER, targetUsername));
            }

            loadUsers();
            JOptionPane.showMessageDialog(parentFrame, "✅ Cập nhật thông tin thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDeleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(parentFrame, "❌ Vui lòng chọn một người dùng để xóa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(parentFrame,
                "⚠️ Bạn có chắc chắn muốn xóa người dùng '" + username + "' không?\n" +
                        "Hành động này không thể hoàn tác!",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Message resp = requestSender.apply(new Message(RequestType.DELETE_USER, username));
            if (resp != null && resp.isSuccess()) {
                loadUsers();
                JOptionPane.showMessageDialog(parentFrame, "✅ Đã xóa người dùng thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "❌ Không thể xóa người dùng!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Helper methods
    private JButton createModernButton(String text, Color bg, Color fg, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalBg = bg;
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBg);
            }
        });
        
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                super.paint(g, c);
            }
        });
        return button;
    }

    private JTextField createModernTextField() {
        JTextField field = new JTextField(25);
        field.setFont(new Font("Inter", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(480, 48));
        field.setMaximumSize(new Dimension(480, 48));
        field.setForeground(new Color(30, 41, 59));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        
        // Add focus effect
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(99, 102, 241), 2, true),
                        BorderFactory.createEmptyBorder(10, 16, 10, 16)));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                        BorderFactory.createEmptyBorder(10, 16, 10, 16)));
            }
        });
        return field;
    }
    
    private JCheckBox createModernCheckbox(String text, boolean selected) {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setSelected(selected);
        checkbox.setFont(new Font("Inter", Font.PLAIN, 15));
        checkbox.setForeground(new Color(51, 65, 85));
        checkbox.setBackground(Color.WHITE);
        checkbox.setFocusPainted(false);
        checkbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkbox.setPreferredSize(new Dimension(480, 42));
        checkbox.setMaximumSize(new Dimension(480, 42));
        checkbox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        // Hover effect
        checkbox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (checkbox.isEnabled()) {
                    checkbox.setBackground(new Color(248, 250, 252));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                checkbox.setBackground(Color.WHITE);
            }
        });
        
        return checkbox;
    }
    
    private void styleModernButton(JButton button, Color bgColor) {
        final Color originalBg = bgColor;
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBg);
            }
        });
        
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                super.paint(g, c);
            }
        });
    }

    // Validation methods
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return false;
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            return false;
        String phoneRegex = "^0[3|5|7|8|9][0-9]{8}$";
        return phone.matches(phoneRegex);
    }

    private boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            return false;
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length < 2)
            return false;
        for (String part : parts) {
            if (part.length() < 2 || part.length() > 50)
                return false;
            if (!part.matches("^[a-zA-ZÀ-ỹ\\s]+$"))
                return false;
        }
        return true;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Lỗi xác thực", JOptionPane.ERROR_MESSAGE);
    }

    public void stopTimers() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
    }
}
