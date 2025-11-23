package com.loginSystem.client.ui;

import com.loginSystem.common.Message;
import com.loginSystem.common.RequestType;
import com.loginSystem.common.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.Map;

public class UserInfoPanel extends JPanel {
    private User currentUser;
    private Map<String, ImageIcon> avatarCache;
    private java.util.function.Function<Message, Message> requestSender;
    
    private JLabel avatarLabel;
    private JLabel usernameValue;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passField;
    private JTextField avatarUrlField;
    private JPanel passGroup;
    private JPanel avatarGroup;
    private JButton editBtn;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JButton changeAvatarBtn;

    public UserInfoPanel(User currentUser, Map<String, ImageIcon> avatarCache, 
                         java.util.function.Function<Message, Message> requestSender) {
        this.currentUser = currentUser;
        this.avatarCache = avatarCache;
        this.requestSender = requestSender;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));

        // Main card với shadow effect
        JPanel mainCard = new JPanel(new BorderLayout(0, 25));
        mainCard.setBackground(Color.WHITE);
        mainCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, false),
                new EmptyBorder(30, 40, 30, 40)));

        // Header section
        JPanel headerSection = createHeaderSection();
        mainCard.add(headerSection, BorderLayout.NORTH);

        // Content section
        JPanel contentSection = createContentSection();
        mainCard.add(contentSection, BorderLayout.CENTER);

        // Add ScrollPane to ensure all content is visible
        JScrollPane scrollPane = new JScrollPane(mainCard);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(new Color(249, 250, 251));

        // Wrapper với padding
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(249, 250, 251));
        wrapper.setBorder(new EmptyBorder(20, 30, 20, 30));
        wrapper.add(scrollPane, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel createHeaderSection() {
        JPanel header = new JPanel(new BorderLayout(25, 0));
        header.setBackground(Color.WHITE);

        // Avatar section (Left)
        JPanel avatarSection = new JPanel(new BorderLayout(0, 12));
        avatarSection.setBackground(Color.WHITE);

        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(140, 140));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Modern rounded border with gradient
        avatarLabel.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient border
                GradientPaint gp = new GradientPaint(0, 0, new Color(59, 130, 246),
                        width, height, new Color(147, 51, 234));
                g2d.setPaint(gp);
                g2d.setStroke(new BasicStroke(4f));
                g2d.drawRoundRect(x + 2, y + 2, width - 4, height - 4, 20, 20);
            }
            
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(6, 6, 6, 6);
            }
        });

        loadAvatar(currentUser != null ? currentUser.getAvatarUrl() : null);

        changeAvatarBtn = new JButton("📷 Thay đổi ảnh");
        changeAvatarBtn.setFont(new Font("Inter", Font.PLAIN, 13));
        changeAvatarBtn.setForeground(new Color(59, 130, 246));
        changeAvatarBtn.setBackground(new Color(239, 246, 255));
        changeAvatarBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        changeAvatarBtn.setFocusPainted(false);
        changeAvatarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changeAvatarBtn.setVisible(false);
        
        avatarSection.add(avatarLabel, BorderLayout.CENTER);
        avatarSection.add(changeAvatarBtn, BorderLayout.SOUTH);
        
        // Add click listener to avatar for zoom
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatarLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showAvatarZoom();
            }
        });

        // Title and action section (Right)
        JPanel titleSection = new JPanel(new BorderLayout());
        titleSection.setBackground(Color.WHITE);

        JPanel titleWrapper = new JPanel();
        titleWrapper.setLayout(new BoxLayout(titleWrapper, BoxLayout.Y_AXIS));
        titleWrapper.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Thông tin cá nhân");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 32));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Quản lý thông tin của bạn");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleWrapper.add(titleLabel);
        titleWrapper.add(subtitleLabel);
        titleSection.add(titleWrapper, BorderLayout.CENTER);

        header.add(avatarSection, BorderLayout.WEST);
        header.add(titleSection, BorderLayout.CENTER);

        return header;
    }

    private JPanel createContentSection() {
        JPanel content = new JPanel(new BorderLayout(0, 25));
        content.setBackground(Color.WHITE);

        // Form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        // Username (readonly)
        formPanel.add(createFieldGroup("Tên đăng nhập", 
            usernameValue = createReadonlyLabel(), true));

        // Full name
        formPanel.add(createFieldGroup("Họ và tên", 
            nameField = createModernTextField(), false));

        // Email
        formPanel.add(createFieldGroup("Địa chỉ email", 
            emailField = createModernTextField(), false));

        // Phone
        formPanel.add(createFieldGroup("Số điện thoại", 
            phoneField = createModernTextField(), false));

        // Password (hidden by default)
        passGroup = createFieldGroup("Mật khẩu mới", 
            passField = createModernPasswordField(), false);
        passGroup.setVisible(false);
        formPanel.add(passGroup);

        // Avatar URL (hidden by default)
        avatarGroup = createFieldGroup("URL Avatar", 
            avatarUrlField = createModernTextField(), false);
        avatarGroup.setVisible(false);
        formPanel.add(avatarGroup);

        content.add(formPanel, BorderLayout.CENTER);

        // Action buttons at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        editBtn = createModernButton("✏️ Chỉnh sửa", new Color(59, 130, 246), Color.WHITE);
        editBtn.setPreferredSize(new Dimension(140, 42));
        
        saveBtn = createModernButton("💾 Lưu", new Color(34, 197, 94), Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(140, 42));
        saveBtn.setVisible(false);
        
        cancelBtn = createModernButton("Hủy", new Color(229, 231, 235), new Color(75, 85, 99));
        cancelBtn.setPreferredSize(new Dimension(140, 42));
        cancelBtn.setVisible(false);

        buttonPanel.add(editBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        content.add(buttonPanel, BorderLayout.SOUTH);

        // Load current user data
        loadUserData();

        // Event listeners
        setupEventListeners(cancelBtn, saveBtn);

        return content;
    }

    private JPanel createFieldGroup(String labelText, JComponent field, boolean readonly) {
        JPanel group = new JPanel(new BorderLayout(0, 8));
        group.setBackground(Color.WHITE);
        group.setBorder(new EmptyBorder(0, 0, 18, 0));
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelPanel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Inter", Font.BOLD, 13));
        label.setForeground(new Color(55, 65, 81));
        labelPanel.add(label);

        group.add(labelPanel, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);

        return group;
    }

    private JLabel createReadonlyLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font("Inter", Font.PLAIN, 14));
        label.setForeground(new Color(17, 24, 39));
        label.setBackground(new Color(249, 250, 251));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        label.setOpaque(true);
        label.setPreferredSize(new Dimension(600, 42));
        return label;
    }

    private JTextField createModernTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setForeground(new Color(17, 24, 39));
        field.setBackground(new Color(249, 250, 251));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        field.setPreferredSize(new Dimension(600, 42));
        field.setEditable(false);
        return field;
    }

    private JPasswordField createModernPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setForeground(new Color(17, 24, 39));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 2, true),
                new EmptyBorder(10, 14, 10, 14)));
        field.setPreferredSize(new Dimension(600, 42));
        return field;
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 13));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                super.paint(g, c);
            }
        });
        
        return button;
    }

    private void loadUserData() {
        if (currentUser != null) {
            usernameValue.setText(currentUser.getUsername());
            nameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
        }
    }

    private void setupEventListeners(JButton cancelBtn, JButton saveBtn) {
        // Edit button
        editBtn.addActionListener(e -> {
            toggleEditMode(true);
        });
        
        // Save button
        saveBtn.addActionListener(e -> saveUserData());
        
        // Cancel button
        cancelBtn.addActionListener(e -> {
            loadUserData();
            passField.setText("");
            avatarUrlField.setText("");
            toggleEditMode(false);
        });

        // Change avatar
        changeAvatarBtn.addActionListener(e -> {
            String url = JOptionPane.showInputDialog(this, 
                "Nhập URL ảnh avatar:", 
                avatarUrlField.getText());
            if (url != null && !url.trim().isEmpty()) {
                avatarUrlField.setText(url.trim());
                loadAvatar(url.trim());
            }
        });

        // Avatar URL field
        avatarUrlField.addActionListener(e -> {
            String url = avatarUrlField.getText().trim();
            if (!url.isEmpty()) {
                loadAvatar(url);
            }
        });
    }

    private void toggleEditMode(boolean editMode) {
        if (editMode) {
            editBtn.setVisible(false);
            saveBtn.setVisible(true);
            cancelBtn.setVisible(true);
            
            nameField.setEditable(true);
            nameField.setBackground(Color.WHITE);
            nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254), 2, true),
                    new EmptyBorder(10, 14, 10, 14)));
            
            emailField.setEditable(true);
            emailField.setBackground(Color.WHITE);
            emailField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254), 2, true),
                    new EmptyBorder(10, 14, 10, 14)));
            
            phoneField.setEditable(true);
            phoneField.setBackground(Color.WHITE);
            phoneField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254), 2, true),
                    new EmptyBorder(10, 14, 10, 14)));
            
            passGroup.setVisible(true);
            avatarGroup.setVisible(true);
            changeAvatarBtn.setVisible(true);
        } else {
            editBtn.setVisible(true);
            saveBtn.setVisible(false);
            cancelBtn.setVisible(false);
            
            nameField.setEditable(false);
            nameField.setBackground(new Color(249, 250, 251));
            nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                    new EmptyBorder(10, 14, 10, 14)));
            
            emailField.setEditable(false);
            emailField.setBackground(new Color(249, 250, 251));
            emailField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                    new EmptyBorder(10, 14, 10, 14)));
            
            phoneField.setEditable(false);
            phoneField.setBackground(new Color(249, 250, 251));
            phoneField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                    new EmptyBorder(10, 14, 10, 14)));
            
            passGroup.setVisible(false);
            avatarGroup.setVisible(false);
            changeAvatarBtn.setVisible(false);
        }
        revalidate();
        repaint();
    }
    
    private void showAvatarZoom() {
        if (avatarLabel.getIcon() == null) return;
        
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Avatar", true);
        dialog.setLayout(new BorderLayout());
        
        JLabel zoomLabel = new JLabel(avatarLabel.getIcon());
        zoomLabel.setHorizontalAlignment(SwingConstants.CENTER);
        zoomLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        dialog.add(zoomLabel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void saveUserData() {
        String fullName = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passField.getPassword());
        String newAvatarUrl = avatarUrlField.getText().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng điền đầy đủ thông tin bắt buộc!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User u = new User();
        u.setUsername(currentUser.getUsername());
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setEmail(email);
        u.setPassword(password);
        u.setAvatarUrl(newAvatarUrl.isEmpty() ? null : newAvatarUrl);

        Message req = new Message(RequestType.UPDATE_USER, "");
        req.setUser(u);
        Message resp = requestSender.apply(req);

        if (resp != null && resp.isSuccess()) {
            currentUser.setFullName(fullName);
            currentUser.setPhone(phone);
            currentUser.setEmail(email);
            if (!newAvatarUrl.isEmpty()) {
                currentUser.setAvatarUrl(newAvatarUrl);
            }
            
            JOptionPane.showMessageDialog(this,
                    "✅ Cập nhật thông tin thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            // Preload avatar to cache
            if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().trim().isEmpty()) {
                preloadAvatar(currentUser.getAvatarUrl());
            }
            
            loadAvatar(currentUser.getAvatarUrl());
            toggleEditMode(false);
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌ Cập nhật thất bại: " + (resp != null ? resp.getError() : "Lỗi kết nối"),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAvatar(String url) {
        if (url == null || url.trim().isEmpty()) {
            if (currentUser != null) {
                url = "https://ui-avatars.com/api/?name=" + currentUser.getUsername() + 
                      "&size=140&background=3b82f6&color=fff&bold=true";
            }
        }
        
        if (url == null) {
            avatarLabel.setIcon(null);
            avatarLabel.setText("No Avatar");
            return;
        }
        
        // Check cache
        if (avatarCache.containsKey(url)) {
            avatarLabel.setIcon(avatarCache.get(url));
            avatarLabel.setText("");
            return;
        }
        
        String finalUrl = url;
        new Thread(() -> {
            try {
                URL imageUrl = new URL(finalUrl);
                ImageIcon icon = new ImageIcon(imageUrl);
                Image img = icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(img);
                
                avatarCache.put(finalUrl, scaledIcon);
                
                SwingUtilities.invokeLater(() -> {
                    avatarLabel.setIcon(scaledIcon);
                    avatarLabel.setText("");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    avatarLabel.setIcon(null);
                    avatarLabel.setText("❌");
                    avatarLabel.setFont(new Font("Inter", Font.PLAIN, 36));
                });
            }
        }).start();
    }
    
    private void preloadAvatar(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        
        new Thread(() -> {
            try {
                // Preload small size (32px for header)
                String smallKey = url + "_32";
                if (!avatarCache.containsKey(smallKey)) {
                    URL imageUrl = new URL(url);
                    ImageIcon icon = new ImageIcon(imageUrl);
                    Image img = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                    avatarCache.put(smallKey, new ImageIcon(img));
                }
                
                // Preload large size (140px for profile)
                String largeKey = url + "_140";
                if (!avatarCache.containsKey(largeKey)) {
                    URL imageUrl = new URL(url);
                    ImageIcon icon = new ImageIcon(imageUrl);
                    Image img = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                    avatarCache.put(largeKey, new ImageIcon(img));
                }
                
                System.out.println("✅ Avatar preloaded in UserInfoPanel: " + url);
            } catch (Exception ex) {
                System.err.println("❌ Failed to preload avatar: " + ex.getMessage());
            }
        }).start();
    }
}
