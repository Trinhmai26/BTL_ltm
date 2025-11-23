package com.loginSystem.client.ui;

import com.loginSystem.common.Message;
import com.loginSystem.common.RequestType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Panel Dashboard thống kê hệ thống cho Admin - Modern UI với biểu đồ
 */
public class AdminDashboardPanel extends JPanel {
    private final Function<Message, Message> requestSender;
    private final Timer autoRefreshTimer;
    private JLabel totalUsersLabel;
    private JLabel activeSessionsLabel;
    private JLabel lockedAccountsLabel;
    private JLabel todayLoginsLabel;
    private BarChartPanel loginTrendChart;
    private PieChartPanel successRateChart;
    private TopUsersPanel topUsersPanel;
    
    private Map<String, Integer> statsData = new HashMap<>();

    public AdminDashboardPanel(Function<Message, Message> requestSender) {
        this.requestSender = requestSender;

        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));

        // Main container with scroll
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(new Color(249, 250, 251));
        mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // Top section: Welcome header
        JPanel welcomePanel = createWelcomePanel();
        welcomePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(welcomePanel);
        mainContent.add(Box.createVerticalStrut(20));
        
        // Middle section: Stats cards
        JPanel statsGrid = createStatsGrid();
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(statsGrid);
        mainContent.add(Box.createVerticalStrut(25));
        
        // Charts section
        JPanel chartsPanel = createChartsPanel();
        chartsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(chartsPanel);
        mainContent.add(Box.createVerticalStrut(25));
        
        // Bottom section: Quick actions
        JPanel actionsPanel = createQuickActionsPanel();
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(actionsPanel);
        mainContent.add(Box.createVerticalStrut(20));

        // Scroll pane with custom settings
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Auto refresh timer (5 seconds)
        autoRefreshTimer = new Timer(5000, e -> loadStats());
        autoRefreshTimer.start();

        // Load initial stats
        loadStats();
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(249, 250, 251));
        panel.setMaximumSize(new Dimension(2000, 80));
        
        JLabel welcomeLabel = new JLabel("📊 Dashboard Tổng Quan");
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(17, 24, 39));
        
        JLabel subtitleLabel = new JLabel("Theo dõi hoạt động hệ thống theo thời gian thực với biểu đồ trực quan");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(249, 250, 251));
        textPanel.add(welcomeLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        // Auto-refresh indicator
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        refreshPanel.setBackground(new Color(249, 250, 251));
        
        JLabel refreshIcon = new JLabel("🔄");
        refreshIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        
        JLabel refreshLabel = new JLabel("Tự động cập nhật mỗi 5s");
        refreshLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        refreshLabel.setForeground(new Color(107, 114, 128));
        
        refreshPanel.add(refreshIcon);
        refreshPanel.add(refreshLabel);
        
        panel.add(refreshPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createStatsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setBackground(new Color(249, 250, 251));
        grid.setMaximumSize(new Dimension(2000, 320));
        
        // Card 1: Tổng người dùng
        JPanel card1 = createStatCard(
            "👥", "Tổng Người Dùng", "0", 
            new Color(59, 130, 246), new Color(219, 234, 254));
        totalUsersLabel = findValueLabel(card1);
        grid.add(card1);
        
        // Card 2: Hoạt động hôm nay
        JPanel card2 = createStatCard(
            "📈", "Đăng Nhập Hôm Nay", "0", 
            new Color(16, 185, 129), new Color(209, 250, 229));
        todayLoginsLabel = findValueLabel(card2);
        grid.add(card2);
        
        // Card 3: Tài khoản bị khóa
        JPanel card3 = createStatCard(
            "🔒", "Tài Khoản Khóa", "0", 
            new Color(239, 68, 68), new Color(254, 226, 226));
        lockedAccountsLabel = findValueLabel(card3);
        grid.add(card3);
        
        // Card 4: Phiên đang hoạt động
        JPanel card4 = createStatCard(
            "✅", "Phiên Hoạt Động", "0", 
            new Color(168, 85, 247), new Color(243, 232, 255));
        activeSessionsLabel = findValueLabel(card4);
        grid.add(card4);
        
        return grid;
    }
    
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 20));
        panel.setBackground(new Color(249, 250, 251));
        panel.setMaximumSize(new Dimension(2000, 380));
        panel.setPreferredSize(new Dimension(1400, 380));
        
        // Chart 1: Login Trend (7 ngày qua)
        loginTrendChart = new BarChartPanel();
        panel.add(loginTrendChart);
        
        // Chart 2: Success Rate (Pie chart)
        successRateChart = new PieChartPanel();
        panel.add(successRateChart);
        
        // Chart 3: Top Users
        topUsersPanel = new TopUsersPanel();
        panel.add(topUsersPanel);
        
        return panel;
    }
    
    private JPanel createStatCard(String icon, String title, String value, Color primaryColor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(25, 25, 25, 25)));
        
        // Icon circle
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iconPanel.setBackground(Color.WHITE);
        
        JLabel iconCircle = new JLabel(icon);
        iconCircle.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconCircle.setOpaque(true);
        iconCircle.setBackground(bgColor);
        iconCircle.setBorder(new EmptyBorder(15, 15, 15, 15));
        iconCircle.setHorizontalAlignment(SwingConstants.CENTER);
        iconCircle.setPreferredSize(new Dimension(70, 70));
        
        // Rounded background for icon
        iconCircle.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 14, 14);
                super.paint(g, c);
            }
        });
        
        iconPanel.add(iconCircle);
        card.add(iconPanel, BorderLayout.WEST);
        
        // Text content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(107, 114, 128));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 36));
        valueLabel.setForeground(new Color(17, 24, 39));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setName("VALUE_LABEL");
        
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(valueLabel);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JLabel findValueLabel(JPanel card) {
        for (Component comp : card.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component inner : ((JPanel) comp).getComponents()) {
                    if (inner instanceof JLabel && "VALUE_LABEL".equals(inner.getName())) {
                        return (JLabel) inner;
                    }
                }
            }
        }
        return null;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(249, 250, 251));
        panel.setMaximumSize(new Dimension(2000, 100));
        
        JLabel titleLabel = new JLabel("⚡ Thao Tác Nhanh");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 17));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonsPanel.setBackground(new Color(249, 250, 251));
        
        JButton refreshBtn = createActionButton("🔄 Làm Mới", new Color(59, 130, 246));
        refreshBtn.addActionListener(e -> loadStats());
        
        JButton usersBtn = createActionButton("👥 Quản Lý Users", new Color(16, 185, 129));
        JButton historyBtn = createActionButton("📊 Xem Lịch Sử", new Color(168, 85, 247));
        
        buttonsPanel.add(refreshBtn);
        buttonsPanel.add(usersBtn);
        buttonsPanel.add(historyBtn);
        
        panel.add(buttonsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                JButton btn = (JButton) c;
                if (btn.getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (btn.getModel().isRollover()) {
                    g2d.setColor(color.brighter());
                } else {
                    g2d.setColor(c.getBackground());
                }
                
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                super.paint(g, c);
            }
        });
        
        return button;
    }

    private void loadStats() {
        new Thread(() -> {
            Message resp = requestSender.apply(new Message(RequestType.GET_DETAILED_STATS, ""));
            if (resp != null && resp.isSuccess()) {
                String content = resp.getContent();
                parseAndUpdateStats(content);
            }
        }).start();
    }
    
    private void parseAndUpdateStats(String content) {
        SwingUtilities.invokeLater(() -> {
            String[] lines = content.split("\n");
            Map<String, String> data = new HashMap<>();
            
            for (String line : lines) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        data.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
            
            // Update stat cards
            if (data.containsKey("total_users")) {
                totalUsersLabel.setText(data.get("total_users"));
            }
            if (data.containsKey("today_logins")) {
                todayLoginsLabel.setText(data.get("today_logins"));
            }
            if (data.containsKey("locked_accounts")) {
                lockedAccountsLabel.setText(data.get("locked_accounts"));
            }
            if (data.containsKey("active_sessions")) {
                activeSessionsLabel.setText(data.get("active_sessions"));
            }
            
            // Update bar chart (login trend)
            if (data.containsKey("login_trend") && loginTrendChart != null) {
                String trendData = data.get("login_trend");
                if (!trendData.isEmpty()) {
                    Map<String, Integer> trendMap = new HashMap<>();
                    String[] entries = trendData.split(",");
                    for (String entry : entries) {
                        if (entry.contains(":")) {
                            String[] kv = entry.split(":");
                            if (kv.length == 2) {
                                // Format date to dd/MM
                                String date = kv[0].trim();
                                if (date.length() >= 10) {
                                    String[] dateParts = date.split("-");
                                    if (dateParts.length == 3) {
                                        date = dateParts[2] + "/" + dateParts[1];
                                    }
                                }
                                trendMap.put(date, Integer.parseInt(kv[1].trim()));
                            }
                        }
                    }
                    loginTrendChart.updateData(trendMap);
                }
            }
            
            // Update pie chart (success rate)
            if (data.containsKey("login_success") && data.containsKey("login_failed") && successRateChart != null) {
                int success = Integer.parseInt(data.get("login_success"));
                int failed = Integer.parseInt(data.get("login_failed"));
                successRateChart.updateData(success, failed);
            }
            
            // Update top users
            if (data.containsKey("top_users") && topUsersPanel != null) {
                String usersData = data.get("top_users");
                if (!usersData.isEmpty()) {
                    Map<String, Integer> usersMap = new HashMap<>();
                    String[] entries = usersData.split(",");
                    for (String entry : entries) {
                        if (entry.contains(":")) {
                            String[] kv = entry.split(":");
                            if (kv.length == 2) {
                                usersMap.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
                            }
                        }
                    }
                    topUsersPanel.updateData(usersMap);
                }
            }
        });
    }

    public void stopTimers() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
    }
    
    // Custom Bar Chart Panel
    class BarChartPanel extends JPanel {
        private Map<String, Integer> data = new HashMap<>();
        private String title = "📊 Xu Hướng Đăng Nhập 7 Ngày";
        
        public BarChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(15, 15, 15, 15)));
        }
        
        public void updateData(Map<String, Integer> newData) {
            this.data = newData;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Title
            g2d.setFont(new Font("Inter", Font.BOLD, 14));
            g2d.setColor(new Color(17, 24, 39));
            g2d.drawString(title, 10, 25);
            
            if (data.isEmpty()) {
                g2d.setFont(new Font("Inter", Font.PLAIN, 12));
                g2d.setColor(new Color(156, 163, 175));
                g2d.drawString("Chưa có dữ liệu", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }
            
            int chartTop = 50;
            int chartBottom = getHeight() - 50;
            int chartHeight = chartBottom - chartTop;
            int barWidth = (getWidth() - 40) / data.size();
            
            int maxValue = data.values().stream().max(Integer::compareTo).orElse(1);
            if (maxValue == 0) maxValue = 1;
            
            int x = 20;
            Color barColor = new Color(59, 130, 246);
            
            List<String> keys = new ArrayList<>(data.keySet());
            java.util.Collections.reverse(keys); // Reverse to show oldest first
            
            for (String key : keys) {
                int value = data.get(key);
                int barHeight = (int) ((value / (double) maxValue) * chartHeight);
                int barY = chartBottom - barHeight;
                
                // Draw bar with gradient
                GradientPaint gradient = new GradientPaint(
                    x, barY, barColor,
                    x, barY + barHeight, barColor.darker());
                g2d.setPaint(gradient);
                g2d.fillRoundRect(x, barY, barWidth - 10, barHeight, 6, 6);
                
                // Draw value on top
                g2d.setColor(new Color(55, 65, 81));
                g2d.setFont(new Font("Inter", Font.BOLD, 11));
                String valueStr = String.valueOf(value);
                int strWidth = g2d.getFontMetrics().stringWidth(valueStr);
                g2d.drawString(valueStr, x + (barWidth - 10 - strWidth) / 2, barY - 5);
                
                // Draw label
                g2d.setFont(new Font("Inter", Font.PLAIN, 10));
                g2d.setColor(new Color(107, 114, 128));
                int labelWidth = g2d.getFontMetrics().stringWidth(key);
                g2d.drawString(key, x + (barWidth - 10 - labelWidth) / 2, chartBottom + 20);
                
                x += barWidth;
            }
        }
    }
    
    // Custom Pie Chart Panel
    class PieChartPanel extends JPanel {
        private int successCount = 0;
        private int failedCount = 0;
        private String title = "🎯 Tỷ Lệ Đăng Nhập Hôm Nay";
        
        public PieChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(15, 15, 15, 15)));
        }
        
        public void updateData(int success, int failed) {
            this.successCount = success;
            this.failedCount = failed;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Title
            g2d.setFont(new Font("Inter", Font.BOLD, 14));
            g2d.setColor(new Color(17, 24, 39));
            g2d.drawString(title, 10, 25);
            
            int total = successCount + failedCount;
            if (total == 0) {
                g2d.setFont(new Font("Inter", Font.PLAIN, 12));
                g2d.setColor(new Color(156, 163, 175));
                g2d.drawString("Chưa có dữ liệu", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2 + 10;
            int radius = Math.min(getWidth(), getHeight()) / 3;
            
            double successAngle = (successCount / (double) total) * 360;
            
            // Draw success slice
            g2d.setColor(new Color(16, 185, 129));
            g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, (int) successAngle);
            
            // Draw failed slice
            g2d.setColor(new Color(239, 68, 68));
            g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, (int) successAngle, 360 - (int) successAngle);
            
            // Draw center circle (donut style)
            g2d.setColor(Color.WHITE);
            int innerRadius = radius / 2;
            g2d.fillOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);
            
            // Draw percentage in center
            g2d.setFont(new Font("Inter", Font.BOLD, 18));
            g2d.setColor(new Color(17, 24, 39));
            String percent = String.format("%.0f%%", (successCount / (double) total) * 100);
            int textWidth = g2d.getFontMetrics().stringWidth(percent);
            g2d.drawString(percent, centerX - textWidth / 2, centerY + 7);
            
            // Legend
            int legendY = getHeight() - 30;
            
            // Success legend
            g2d.setColor(new Color(16, 185, 129));
            g2d.fillRect(20, legendY, 12, 12);
            g2d.setFont(new Font("Inter", Font.PLAIN, 11));
            g2d.setColor(new Color(55, 65, 81));
            g2d.drawString("Thành công: " + successCount, 38, legendY + 10);
            
            // Failed legend
            g2d.setColor(new Color(239, 68, 68));
            g2d.fillRect(getWidth() / 2 + 10, legendY, 12, 12);
            g2d.setColor(new Color(55, 65, 81));
            g2d.drawString("Thất bại: " + failedCount, getWidth() / 2 + 28, legendY + 10);
        }
    }
    
    // Top Users Panel
    class TopUsersPanel extends JPanel {
        private Map<String, Integer> usersData = new HashMap<>();
        private String title = "👑 Top 5 Người Dùng Tháng Này";
        
        public TopUsersPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(15, 15, 15, 15)));
        }
        
        public void updateData(Map<String, Integer> newData) {
            this.usersData = newData;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Title
            g2d.setFont(new Font("Inter", Font.BOLD, 14));
            g2d.setColor(new Color(17, 24, 39));
            g2d.drawString(title, 10, 25);
            
            if (usersData.isEmpty()) {
                g2d.setFont(new Font("Inter", Font.PLAIN, 12));
                g2d.setColor(new Color(156, 163, 175));
                g2d.drawString("Chưa có dữ liệu", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }
            
            int y = 60;
            int rank = 1;
            
            // Sort by value descending
            List<Map.Entry<String, Integer>> sortedUsers = new ArrayList<>(usersData.entrySet());
            sortedUsers.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            for (Map.Entry<String, Integer> entry : sortedUsers) {
                if (rank > 5) break;
                
                // Rank badge
                Color badgeColor = rank == 1 ? new Color(251, 191, 36) : 
                                  rank == 2 ? new Color(156, 163, 175) :
                                  rank == 3 ? new Color(205, 127, 50) :
                                  new Color(59, 130, 246);
                
                g2d.setColor(badgeColor);
                g2d.fillOval(15, y - 12, 22, 22);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Inter", Font.BOLD, 12));
                g2d.drawString(String.valueOf(rank), rank < 10 ? 23 : 20, y + 3);
                
                // Username
                g2d.setColor(new Color(55, 65, 81));
                g2d.setFont(new Font("Inter", Font.PLAIN, 13));
                g2d.drawString(entry.getKey(), 45, y + 2);
                
                // Count
                g2d.setColor(new Color(107, 114, 128));
                g2d.setFont(new Font("Inter", Font.BOLD, 12));
                String count = entry.getValue() + " lần";
                int countWidth = g2d.getFontMetrics().stringWidth(count);
                g2d.drawString(count, getWidth() - countWidth - 15, y + 2);
                
                // Progress bar
                int maxValue = sortedUsers.get(0).getValue();
                int barWidth = (int) ((entry.getValue() / (double) maxValue) * (getWidth() - 70));
                g2d.setColor(new Color(219, 234, 254));
                g2d.fillRoundRect(45, y + 10, getWidth() - 60, 6, 3, 3);
                g2d.setColor(badgeColor);
                g2d.fillRoundRect(45, y + 10, barWidth, 6, 3, 3);
                
                y += 55;
                rank++;
            }
        }
    }
}
