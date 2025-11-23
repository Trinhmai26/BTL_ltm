package com.loginSystem.client.ui;

import com.loginSystem.common.Message;
import com.loginSystem.common.RequestType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Function;

/**
 * Panel gửi thông báo broadcast cho Admin
 */
public class AdminBroadcastPanel extends JPanel {
    private final JFrame parentFrame;
    private final Function<Message, Message> requestSender;
    private final JTextArea messageArea;

    public AdminBroadcastPanel(JFrame parentFrame, Function<Message, Message> requestSender) {
        this.parentFrame = parentFrame;
        this.requestSender = requestSender;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title
        JLabel titleLabel = new JLabel("📢 Gửi thông báo tới tất cả người dùng", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 22));
        titleLabel.setForeground(new Color(75, 85, 99));
        titleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Message area
        messageArea = new JTextArea(10, 40);
        messageArea.setFont(new Font("Inter", Font.PLAIN, 14));
        messageArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        messageArea.setBackground(new Color(249, 250, 251));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                        "📝 Nội dung thông báo",
                        0, 0,
                        new Font("Inter", Font.BOLD, 14),
                        new Color(107, 114, 128)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        add(scrollPane, BorderLayout.CENTER);

        // Send button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton sendBtn = createStyledButton("📡 Gửi thông báo", new Color(168, 85, 247), Color.WHITE, 180, 50);
        sendBtn.setFont(new Font("Inter", Font.BOLD, 16));

        sendBtn.addActionListener(e -> handleSendBroadcast());

        bottomPanel.add(sendBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void handleSendBroadcast() {
        String message = messageArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Vui lòng nhập nội dung thông báo!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Message req = new Message(RequestType.BROADCAST, message);
        Message resp = requestSender.apply(req);

        if (resp != null && resp.isSuccess()) {
            JOptionPane.showMessageDialog(parentFrame,
                    "✅ " + resp.getContent(),
                    "Gửi thông báo thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            messageArea.setText("");
        } else {
            JOptionPane.showMessageDialog(parentFrame,
                    "❌ " + (resp != null ? resp.getError() : "Lỗi kết nối"),
                    "Gửi thông báo thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createStyledButton(String text, Color bg, Color fg, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                super.paint(g, c);
            }
        });
        return button;
    }
}
