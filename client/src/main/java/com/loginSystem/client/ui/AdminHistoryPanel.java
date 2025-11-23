package com.loginSystem.client.ui;

import com.loginSystem.common.Message;
import com.loginSystem.common.RequestType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Panel lịch sử đăng nhập hệ thống cho Admin
 */
public class AdminHistoryPanel extends JPanel {
    private final JFrame parentFrame;
    private final Function<Message, Message> requestSender;
    private final Gson gson;
    private final DefaultTableModel model;
    private final Timer autoRefreshTimer;
    private final Map<String, SessionData> sessionDataMap = new HashMap<>();
    
    // Inner class to hold session data
    private static class SessionData {
        String username;
        String loginTime;
        String logoutTime;
        String status;
        String ip;
        String device;
        
        SessionData(String username, String loginTime, String logoutTime, String status, String ip, String device) {
            this.username = username;
            this.loginTime = loginTime;
            this.logoutTime = logoutTime;
            this.status = status;
            this.ip = ip;
            this.device = device;
        }
    }

    public AdminHistoryPanel(JFrame parentFrame, Function<Message, Message> requestSender, Gson gson) {
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
                GradientPaint gp = new GradientPaint(0, 0, new Color(79, 70, 229), 
                                                      getWidth(), 0, new Color(219, 39, 119));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 120));
        headerPanel.setBorder(new EmptyBorder(25, 30, 30, 30));
        
        JLabel titleLabel = new JLabel("📊 Lịch sử đăng nhập hệ thống");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Theo dõi hoạt động đăng nhập và phiên làm việc của người dùng");
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
                new Object[]{"👤 Người dùng", "🕒 Thời gian đăng nhập", "💻 Thiết bị", "🌐 Phiên làm việc"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = new JTable(model);
        historyTable.setFont(new Font("Inter", Font.PLAIN, 14));
        historyTable.setRowHeight(45);
        historyTable.setSelectionBackground(new Color(219, 234, 254));
        historyTable.setSelectionForeground(new Color(30, 58, 138));
        historyTable.setGridColor(new Color(229, 231, 235));
        historyTable.setShowGrid(true);
        historyTable.setIntercellSpacing(new Dimension(1, 1));
        historyTable.setShowVerticalLines(false);

        // Header styling
        historyTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 14));
        historyTable.getTableHeader().setBackground(new Color(248, 250, 252));
        historyTable.getTableHeader().setForeground(new Color(51, 65, 85));
        historyTable.getTableHeader().setPreferredSize(new Dimension(0, 48));
        historyTable.getTableHeader().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        // Column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        
        // Add button column renderer
        historyTable.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            JButton button = new JButton("🔍 Chi tiết hoạt động");
            {
                button.setFont(new Font("Inter", Font.BOLD, 12));
                button.setForeground(Color.WHITE);
                button.setBackground(new Color(59, 130, 246));
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                return button;
            }
        });
        
        // Add mouse listener for button clicks
        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = historyTable.rowAtPoint(e.getPoint());
                int col = historyTable.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 3) { // Column "Phiên làm việc"
                    String username = (String) model.getValueAt(row, 0);
                    String timestamp = (String) model.getValueAt(row, 1);
                    AdminHistoryPanel.this.showSessionDetails(username, timestamp);
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(historyTable);
        tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createLineBorder(Color.WHITE, 3)));
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        // Control buttons panel with modern design
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 1, new Color(226, 232, 240)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JButton refreshBtn = createModernButton("🔄 Làm mới", new Color(59, 130, 246), Color.WHITE, 140, 42);
        JButton exportBtn = createModernButton("📊 Xuất báo cáo", new Color(16, 185, 129), Color.WHITE, 160, 42);
        JButton clearBtn = createModernButton("🗑️ Xóa lịch sử", new Color(239, 68, 68), Color.WHITE, 150, 42);

        // Auto refresh timer (8 seconds)
        autoRefreshTimer = new Timer(8000, e -> loadLoginHistory());
        autoRefreshTimer.start();

        // Button actions
        refreshBtn.addActionListener(e -> {
            loadLoginHistory();
            JOptionPane.showMessageDialog(parentFrame, "✅ Đã làm mới lịch sử đăng nhập!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        exportBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(parentFrame,
                    "📊 Tính năng xuất báo cáo sẽ được phát triển trong phiên bản tiếp theo!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(parentFrame,
                    "⚠️ Bạn có chắc chắn muốn xóa toàn bộ lịch sử đăng nhập?\n" +
                            "Hành động này không thể hoàn tác!",
                    "Xác nhận xóa lịch sử", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(parentFrame,
                        "🔧 Tính năng xóa lịch sử sẽ được phát triển trong phiên bản tiếp theo!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(clearBtn);

        // Status panel with modern styling
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(226, 232, 240)),
            new EmptyBorder(0, 20, 12, 20)
        ));
        JLabel statusLabel = new JLabel("🔄 Tự động cập nhật mỗi 8 giây");
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
        contentPanel.add(tableScrollPane, BorderLayout.CENTER);
        contentPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Wrapper panel for spacing
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(249, 250, 251));
        wrapperPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        wrapperPanel.add(contentPanel, BorderLayout.CENTER);

        add(wrapperPanel, BorderLayout.CENTER);

        // Load initial data
        loadLoginHistory();
    }

    private void loadLoginHistory() {
        Message req = new Message(RequestType.GET_LOGIN_HISTORY, "ALL");
        Message resp = requestSender.apply(req);
        if (resp != null && resp.isSuccess()) {
            model.setRowCount(0);
            List<String> logs = gson.fromJson(resp.getContent(),
                    new TypeToken<List<String>>() {
                    }.getType());

            sessionDataMap.clear(); // Clear old data
            
            if (!logs.isEmpty()) {
                for (String logEntry : logs) {
                    // Format: username | login_time | logout_time | status | ip | device
                    String[] parts = logEntry.split(" \\| ");
                    if (parts.length >= 6) {
                        String username = parts[0].trim();
                        String loginTime = parts[1].trim();
                        String logoutTime = parts[2].trim();
                        String status = parts[3].trim();
                        String ip = parts[4].trim();
                        String device = parts[5].trim();
                        
                        // Format datetime for display
                        String displayTime = formatDateTime(loginTime);
                        
                        // Store session data
                        String key = username + "_" + displayTime;
                        sessionDataMap.put(key, new SessionData(username, loginTime, logoutTime, status, ip, device));
                        
                        model.addRow(new Object[]{username, displayTime, device, "Chi tiết"});
                    } else {
                        model.addRow(new Object[]{
                                logEntry,
                                "N/A",
                                "Hệ thống",
                                "Chi tiết"
                        });
                    }
                }
            } else {
                model.addRow(new Object[]{
                        "Chưa có dữ liệu",
                        "N/A",
                        "Trống",
                        "Chưa có lịch sử đăng nhập nào"
                });
            }
        }
    }
    
    private String formatDateTime(String timestamp) {
        try {
            // Input format: yyyy-MM-dd HH:mm:ss.S
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy • HH:mm:ss");
            Date date = inputFormat.parse(timestamp.substring(0, 19)); // Remove milliseconds
            return outputFormat.format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }
    
    private void showSessionDetails(String username, String timestamp) {
        // Get session data
        String key = username + "_" + timestamp;
        SessionData sessionData = sessionDataMap.get(key);
        
        if (sessionData == null) {
            JOptionPane.showMessageDialog(parentFrame, 
                "Không tìm thấy thông tin phiên làm việc!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create dialog
        JDialog dialog = new JDialog(parentFrame, "Chi tiết phiên làm việc", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(parentFrame);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(59, 130, 246));
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel titleLabel = new JLabel("📊 Chi tiết hoạt động phiên làm việc");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel infoLabel = new JLabel("Người dùng: " + username + " | Thời gian: " + timestamp);
        infoLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        infoLabel.setForeground(new Color(219, 234, 254));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(59, 130, 246));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(infoLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Content
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        detailsArea.setBackground(new Color(249, 250, 251));
        detailsArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        detailsArea.setForeground(new Color(55, 65, 81));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        
        // Build session details
        StringBuilder details = new StringBuilder();
        details.append("👤 Người dùng: ").append(sessionData.username).append("\n\n");
        details.append("🕒 Thời gian đăng nhập: ").append(formatDateTime(sessionData.loginTime)).append("\n\n");
        
        // Display logout time
        if (!"NULL".equals(sessionData.logoutTime)) {
            details.append("🔴 Thời gian đăng xuất: ").append(formatDateTime(sessionData.logoutTime)).append("\n\n");
        } else {
            details.append("🔴 Thời gian đăng xuất: Chưa đăng xuất\n\n");
        }
        
        details.append("✅ Trạng thái: ").append("OK".equals(sessionData.status) ? "Đăng nhập thành công" : "Đăng nhập thất bại").append("\n\n");
        details.append("🌐 Địa chỉ IP: ").append(sessionData.ip).append("\n\n");
        details.append("💻 Thiết bị: ").append(sessionData.device).append("\n\n");
        details.append("-".repeat(60)).append("\n\n");
        details.append("📊 THÔNG TIN PHIÊN:\n\n");
        details.append("• Người dùng: ").append(sessionData.username).append("\n");
        details.append("• Đăng nhập: ").append(formatDateTime(sessionData.loginTime)).append("\n");
        
        if (!"NULL".equals(sessionData.logoutTime)) {
            details.append("• Đăng xuất: ").append(formatDateTime(sessionData.logoutTime)).append("\n");
            // Calculate session duration
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date loginDate = format.parse(sessionData.loginTime.substring(0, 19));
                Date logoutDate = format.parse(sessionData.logoutTime.substring(0, 19));
                long durationMillis = logoutDate.getTime() - loginDate.getTime();
                long minutes = (durationMillis / 1000) / 60;
                long hours = minutes / 60;
                minutes = minutes % 60;
                details.append("• Thời lượng: ").append(hours).append(" giờ ").append(minutes).append(" phút\n");
            } catch (Exception e) {
                details.append("• Thời lượng: Không thể tính\n");
            }
        } else {
            details.append("• Đăng xuất: Chưa đăng xuất (phiên đang hoạt động)\n");
        }
        
        details.append("• Địa chỉ IP: ").append(sessionData.ip).append("\n");
        details.append("• Thiết bị: ").append(sessionData.device).append("\n");
        
        detailsArea.setText(details.toString());
        
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setBorder(null);
        
        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footerPanel.setBackground(Color.WHITE);
        
        JButton closeBtn = createModernButton("Đóng", new Color(107, 114, 128), Color.WHITE, 100, 42);
        closeBtn.addActionListener(e -> dialog.dispose());
        footerPanel.add(closeBtn);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(footerPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private JButton createModernButton(String text, Color bg, Color fg, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        final Color originalBg = bg;
        button.addMouseListener(new java.awt.event.MouseAdapter() {
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

    public void stopTimers() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
    }
}
