package com.loginSystem.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loginSystem.common.Message;
import com.loginSystem.common.RequestType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class UserHistoryPanel extends JPanel {
    private DefaultTableModel historyModel;
    private Function<Message, Message> requestSender;
    private String username;
    private Timer refreshTimer;
    private JTable table;
    private JComboBox<String> filterCombo;
    private JTextField searchField;

    public UserHistoryPanel(String username, Function<Message, Message> requestSender) {
        this.username = username;
        this.requestSender = requestSender;
        initComponents();
        loadHistory();
        startAutoRefresh();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));

        // Main container with padding
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setBackground(new Color(249, 250, 251));
        mainContainer.setBorder(new EmptyBorder(25, 35, 25, 35));

        // Header with title and actions
        JPanel headerPanel = createHeaderPanel();
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // Toolbar with filters and search
        JPanel toolbarPanel = createToolbarPanel();
        mainContainer.add(toolbarPanel, BorderLayout.CENTER);

        // Table in card
        JPanel tableCard = createTableCard();
        
        // Wrap table card
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(new Color(249, 250, 251));
        tableWrapper.add(tableCard, BorderLayout.CENTER);
        
        mainContainer.add(tableWrapper, BorderLayout.SOUTH);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(new Color(249, 250, 251));
        header.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Left: Title section
        JPanel titleSection = new JPanel();
        titleSection.setLayout(new BoxLayout(titleSection, BoxLayout.Y_AXIS));
        titleSection.setBackground(new Color(249, 250, 251));

        JLabel titleLabel = new JLabel("📊 Lịch sử đăng nhập");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 30));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Theo dõi và quản lý các hoạt động đăng nhập");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleSection.add(titleLabel);
        titleSection.add(subtitleLabel);

        // Right: Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setBackground(new Color(249, 250, 251));

        JButton refreshBtn = createActionButton("🔄 Làm mới", new Color(59, 130, 246), Color.WHITE);
        refreshBtn.addActionListener(e -> loadHistory());

        JButton exportBtn = createActionButton("📥 Xuất Excel", new Color(16, 185, 129), Color.WHITE);
        exportBtn.addActionListener(e -> exportToExcel());

        JButton deleteBtn = createActionButton("🗑️ Xóa lịch sử", new Color(239, 68, 68), Color.WHITE);
        deleteBtn.addActionListener(e -> deleteHistory());

        actionPanel.add(refreshBtn);
        actionPanel.add(exportBtn);
        actionPanel.add(deleteBtn);

        header.add(titleSection, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createToolbarPanel() {
        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, false),
                new EmptyBorder(15, 20, 15, 20)));

        // Left: Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("🔍 Lọc theo:");
        filterLabel.setFont(new Font("Inter", Font.BOLD, 13));
        filterLabel.setForeground(new Color(75, 85, 99));

        String[] filterOptions = {"Tất cả", "Hôm nay", "7 ngày qua", "30 ngày qua"};
        filterCombo = new JComboBox<>(filterOptions);
        filterCombo.setFont(new Font("Inter", Font.PLAIN, 13));
        filterCombo.setBackground(Color.WHITE);
        filterCombo.setForeground(new Color(55, 65, 81));
        filterCombo.setPreferredSize(new Dimension(150, 36));
        filterCombo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                new EmptyBorder(5, 10, 5, 10)));

        filterPanel.add(filterLabel);
        filterPanel.add(filterCombo);

        // Right: Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField();
        searchField.setFont(new Font("Inter", Font.PLAIN, 13));
        searchField.setForeground(new Color(55, 65, 81));
        searchField.setPreferredSize(new Dimension(250, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                new EmptyBorder(5, 12, 5, 12)));

        JLabel searchLabel = new JLabel("🔎");
        searchLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        toolbar.add(filterPanel, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, false),
                new EmptyBorder(0, 0, 0, 0)));

        // Create table with modern design
        historyModel = new DefaultTableModel(
                new Object[]{"⏰ Thời gian", "👤 Tài khoản", "💻 Thiết bị", "🌍 IP Address"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(historyModel);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.setRowHeight(52);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 64, 175));
        table.setShowGrid(true);
        table.setGridColor(new Color(243, 244, 246));
        table.setIntercellSpacing(new Dimension(1, 1));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);

        // Custom cell renderer with padding
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(10, 15, 10, 15));
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(249, 250, 251));
                    }
                }
                
                return c;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Custom header with gradient
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Inter", Font.BOLD, 13));
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setBackground(new Color(59, 130, 246));
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 48));
        tableHeader.setBorder(BorderFactory.createEmptyBorder());
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setFont(new Font("Inter", Font.BOLD, 13));
                label.setForeground(Color.WHITE);
                label.setBackground(new Color(59, 130, 246));
                label.setOpaque(true);
                label.setBorder(new EmptyBorder(12, 15, 12, 15));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 13));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setPreferredSize(new Dimension(150, 40));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                JButton btn = (JButton) c;
                if (btn.getModel().isPressed()) {
                    g2d.setColor(bg.darker());
                } else if (btn.getModel().isRollover()) {
                    g2d.setColor(bg.brighter());
                } else {
                    g2d.setColor(c.getBackground());
                }
                
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                super.paint(g, c);
            }
        });

        return button;
    }

    private void loadHistory() {
        Message req = new Message(RequestType.GET_LOGIN_HISTORY, username);
        Message resp = requestSender.apply(req);

        historyModel.setRowCount(0);

        if (resp != null && resp.isSuccess()) {
            String jsonData = resp.getContent();
            if (jsonData != null && !jsonData.isEmpty()) {
                try {
                    Gson gson = new Gson();
                    List<String> logs = gson.fromJson(jsonData, new TypeToken<List<String>>(){}.getType());
                    
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy • HH:mm:ss");
                    
                    for (String log : logs) {
                        String[] parts = log.split("\\|");
                        if (parts.length >= 5) {
                            String timestamp = parts[1].trim();
                            String user = parts[0].trim();
                            String device = parts[4].trim();
                            String ip = parts[3].trim();
                            
                            // Format timestamp to be more readable
                            String formattedTime = timestamp;
                            try {
                                Date date = inputFormat.parse(timestamp);
                                formattedTime = outputFormat.format(date);
                            } catch (Exception e) {
                                // Keep original if parsing fails
                            }
                            
                            historyModel.addRow(new Object[]{
                                    formattedTime,
                                    user,
                                    device,
                                    ip
                            });
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing login history: " + e.getMessage());
                }
            }
        }
    }

    private void exportToExcel() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "login_history_" + sdf.format(new Date()) + ".csv";
            
            FileWriter writer = new FileWriter(fileName);
            
            // Write header
            writer.append("Thời gian,Tài khoản,Thiết bị,IP Address\n");
            
            // Write data
            for (int i = 0; i < historyModel.getRowCount(); i++) {
                for (int j = 0; j < historyModel.getColumnCount(); j++) {
                    String value = historyModel.getValueAt(i, j).toString();
                    // Remove emoji from column headers
                    value = value.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\.]", "");
                    writer.append(value);
                    if (j < historyModel.getColumnCount() - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
            
            writer.flush();
            writer.close();
            
            JOptionPane.showMessageDialog(this,
                    "✅ Đã xuất file thành công!\n📄 File: " + fileName,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                    
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Lỗi khi xuất file: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteHistory() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ Bạn có chắc chắn muốn xóa toàn bộ lịch sử đăng nhập?\nHành động này không thể hoàn tác!",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Message req = new Message(RequestType.DELETE_LOGIN_HISTORY, username);
            Message resp = requestSender.apply(req);

            if (resp != null && resp.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "✅ Đã xóa lịch sử thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                loadHistory();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Xóa lịch sử thất bại: " + (resp != null ? resp.getError() : "Lỗi kết nối"),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(8000, e -> loadHistory());
        refreshTimer.start();
    }

    public void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}
