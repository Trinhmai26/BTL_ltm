package com.loginSystem.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loginSystem.common.*;
import com.loginSystem.client.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Gson gson;
    private User currentUser;
    
    // Avatar cache
    private java.util.Map<String, ImageIcon> avatarCache = new java.util.HashMap<>();

    // GUI
    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    // panel chứa các card thực sự (CardLayout)
    private JPanel contentPanel;
    // trạng thái toàn màn hình
    private boolean isFullScreen = true;

    public Client() {
        this.gson = new Gson();
        applyLookAndFeel();
        connectToServer();
        setupGUI();
    }

    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            UIManager.put("Button.arc", 20); // bo góc nút nhiều hơn
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.selectedBackground", new Color(45, 52, 54));
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            // font mặc định nhỏ gọn hơn
            UIManager.put("defaultFont", new Font("Inter", Font.PLAIN, 12));
            // Cải thiện table
            UIManager.put("Table.gridColor", new Color(230, 230, 230));
            UIManager.put("Table.selectionBackground", new Color(74, 144, 226));
            UIManager.put("Table.alternateRowColor", new Color(248, 249, 250));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper: style nút với hiệu ứng gradient và shadow hiện đại
    private JButton styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new ModernRoundedBorder(15));
        b.setFont(new Font("Inter", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalBg = b.getBackground();

            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(brightenColor(originalBg, 20));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(originalBg);
            }
        });

        return b;
    }

    // Helper: làm sáng màu
    private Color brightenColor(Color color, int amount) {
        int r = Math.min(255, color.getRed() + amount);
        int g = Math.min(255, color.getGreen() + amount);
        int b = Math.min(255, color.getBlue() + amount);
        return new Color(r, g, b);
    }

    // Helper: tạo styled text field responsive
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Inter", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10) // Reduced padding for compactness
        ));
        field.setBackground(Color.WHITE);
        field.setMinimumSize(new Dimension(200, 32)); // Adjusted dimensions
        field.setPreferredSize(new Dimension(260, 32));

        // Focus highlighting hồng
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(219, 39, 119), 2, true), // #db2777
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(251, 207, 232), 1, true), // #fbcfe8
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });

        return field;
    }

    // Helper: tạo large styled text field responsive
    private JTextField createLargeStyledTextField() {
        JTextField field = new JTextField(30);
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(251, 207, 232), 1, true), // #fbcfe8 - hồng nhạt
                BorderFactory.createEmptyBorder(10, 14, 10, 14) // Reduced padding for compactness
        ));
        field.setBackground(Color.WHITE);
        field.setMinimumSize(new Dimension(220, 36)); // Adjusted dimensions
        field.setMaximumSize(new Dimension(400, 40));

        // Focus highlighting hồng
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(219, 39, 119), 2, true), // #db2777
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(251, 207, 232), 1, true), // #fbcfe8
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }
        });

        return field;
    }

    // Helper: tạo styled password field responsive
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Inter", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(251, 207, 232), 1, true), // #fbcfe8
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
        field.setMinimumSize(new Dimension(180, 36));
        field.setPreferredSize(new Dimension(260, 36));

        // Focus highlighting hồng
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(219, 39, 119), 2, true), // #db2777
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)));
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(251, 207, 232), 1, true), // #fbcfe8
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)));
            }
        });

        return field;
    }

    // Helper: tạo large styled password field responsive
    private JPasswordField createLargeStyledPasswordField() {
        JPasswordField field = new JPasswordField(30);
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(251, 207, 232), 1, true), // #fbcfe8
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        field.setBackground(Color.WHITE);
        field.setMinimumSize(new Dimension(200, 40));
        field.setMaximumSize(new Dimension(420, 44));

        // Focus highlighting hồng
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(219, 39, 119), 2, true), // #db2777
                        BorderFactory.createEmptyBorder(16, 20, 16, 20)));
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(251, 207, 232), 1, true), // #fbcfe8
                        BorderFactory.createEmptyBorder(16, 20, 16, 20)));
            }
        });

        return field;
    }

    // Viền bo tròn hiện đại với shadow nổi bật
    private static class ModernRoundedBorder implements javax.swing.border.Border {
        private final int radius;

        public ModernRoundedBorder(int r) {
            this.radius = r;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(8, 8, 12, 8);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Multiple shadow layers cho hiệu ứng depth
            for (int i = 0; i < 3; i++) {
                g2.setColor(new Color(0, 0, 0, 15 - i * 3));
                g2.fillRoundRect(x + 2 + i, y + 3 + i, width - 4 - i * 2, height - 6 - i * 2, radius, radius);
            }

            // Main white background
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x + 4, y + 4, width - 8, height - 8, radius, radius);

            // Subtle border
            g2.setColor(new Color(229, 231, 235));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(x + 4, y + 4, width - 8, height - 8, radius, radius);
            g2.dispose();
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server");
            startListener();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Không thể kết nối tới server: " + e.getMessage(),
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void setupGUI() {
        mainFrame = new JFrame("🔒 Hệ thống đăng nhập");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setUndecorated(false);
        mainFrame.setSize(1200, 800);
        mainFrame.setMinimumSize(new Dimension(640, 480));
        mainFrame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.contentPanel.setOpaque(false);
        this.contentPanel.setPreferredSize(null);

        this.contentPanel.add(createLoginPanel(), "LOGIN");
        this.contentPanel.add(createRegisterPanel(), "REGISTER");
        this.contentPanel.add(createUserPanel(), "USER");
        this.contentPanel.add(createAdminPanel(), "ADMIN");

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Dùng center wrapper chỉ cho login/register, user/admin sẽ tự quản lý
        mainPanel.add(this.contentPanel, BorderLayout.CENTER);

        // Wrap the entire mainPanel into a bordered panel
        JPanel borderedFrame = new JPanel(new BorderLayout());
        borderedFrame.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(251, 207, 232), 3, true), // #fbcfe8 - hồng pastel
                new EmptyBorder(10, 10, 10, 10)));
        borderedFrame.setBackground(Color.WHITE);
        borderedFrame.add(mainPanel, BorderLayout.CENTER);

        mainFrame.add(borderedFrame);
        mainFrame.setVisible(true);

        enableWindowResizing();

        mainFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "toggleFullscreen");
        mainFrame.getRootPane().getActionMap()
                .put("toggleFullscreen", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        toggleFullScreen();
                    }
                });

        cardLayout.show(this.contentPanel, "LOGIN");
    }

    private void toggleFullScreen() {
        // Toggle giữa fullscreen và windowed mode
        boolean newFull = !isFullScreen;
        mainFrame.dispose();
        mainFrame.setUndecorated(newFull);
        if (newFull) {
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            mainFrame.setExtendedState(JFrame.NORMAL);
            mainFrame.setSize(1200, 800);
            mainFrame.setLocationRelativeTo(null);
        }
        isFullScreen = newFull;
        mainFrame.setVisible(true);
    }

    // ---------------- LOGIN PANEL ----------------
    private JPanel createLoginPanel() {
        // Main wrapper with modern gradient background
        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                // Gradient: #eef4ff -> #ffffff
                GradientPaint gp = new GradientPaint(0, 0, new Color(238, 244, 255), 0, h, Color.WHITE);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        GridBagConstraints wrapperGbc = new GridBagConstraints();
        wrapperGbc.gridx = 0;
        wrapperGbc.gridy = 0;
        wrapperGbc.anchor = GridBagConstraints.CENTER;

    // Modern card with shadow
    JPanel card = new JPanel(new GridBagLayout());
    card.setBackground(Color.WHITE);
    card.setPreferredSize(new Dimension(420, 580));
    // Shadow effect: 0 4px 20px rgba(0,0,0,0.06)
    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 15), 1, true),
            new EmptyBorder(40, 45, 40, 45)));        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

    // Welcome back text - light gray
    gbc.gridy = 0;
    gbc.insets = new Insets(0, 0, 6, 0);
    JLabel welcomeLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
    welcomeLabel.setFont(new Font("Inter", Font.PLAIN, 14));
    welcomeLabel.setForeground(new Color(125, 125, 125)); // #7D7D7D
    card.add(welcomeLabel, gbc);    // Title with primary color
    gbc.gridy = 1;
    gbc.insets = new Insets(0, 0, 32, 0);
    JLabel titleLabel = new JLabel("Đăng nhập", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
    titleLabel.setForeground(new Color(39, 110, 241)); // #276EF1
    card.add(titleLabel, gbc);    // Username label with icon
    gbc.gridy = 2;
    gbc.insets = new Insets(0, 0, 8, 0);
    gbc.anchor = GridBagConstraints.WEST;
    JLabel userLabel = new JLabel("👤 Tên đăng nhập");
    userLabel.setFont(new Font("Inter", Font.BOLD, 13));
    userLabel.setForeground(new Color(77, 77, 77)); // #4D4D4D
    card.add(userLabel, gbc);    // Username field - 44px height, light blue border
    gbc.gridy = 3;
    gbc.insets = new Insets(0, 0, 18, 0);
    gbc.anchor = GridBagConstraints.CENTER;
    JTextField usernameField = createLargeStyledTextField();
    usernameField.setPreferredSize(new Dimension(330, 44));
    usernameField.setFont(new Font("Inter", Font.PLAIN, 14));
    usernameField.setForeground(new Color(51, 51, 51));
    usernameField.setBackground(Color.WHITE);
    usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(161, 196, 253), 2, true), // #a1c4fd
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
    // Focus effect
    usernameField.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusGained(java.awt.event.FocusEvent evt) {
            usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(39, 110, 241), 2, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        }
        public void focusLost(java.awt.event.FocusEvent evt) {
            usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(161, 196, 253), 2, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        }
    });
    card.add(usernameField, gbc);    // Password label with icon
    gbc.gridy = 4;
    gbc.insets = new Insets(0, 0, 8, 0);
    gbc.anchor = GridBagConstraints.WEST;
    JLabel passLabel = new JLabel("🔒 Mật khẩu");
    passLabel.setFont(new Font("Inter", Font.BOLD, 13));
    passLabel.setForeground(new Color(77, 77, 77)); // #4D4D4D
    card.add(passLabel, gbc);        // Password field with toggle button
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Password panel with overlay button inside
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new OverlayLayout(passwordPanel));
        passwordPanel.setOpaque(false);
        
        JPasswordField passwordField = createLargeStyledPasswordField();
        passwordField.setPreferredSize(new Dimension(330, 44));
        passwordField.setMaximumSize(new Dimension(330, 44));
        passwordField.setFont(new Font("Inter", Font.PLAIN, 14));
        passwordField.setForeground(new Color(51, 51, 51));
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(161, 196, 253), 2, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 45)));
        passwordField.setAlignmentX(0.0f);
        passwordField.setAlignmentY(0.5f);
        
        // Focus effect
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(39, 110, 241), 2, true),
                    BorderFactory.createEmptyBorder(10, 14, 10, 45)));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(161, 196, 253), 2, true),
                    BorderFactory.createEmptyBorder(10, 14, 10, 45)));
            }
        });
        
        // Toggle password visibility button - positioned inside field
        JButton toggleBtn = new JButton("👁");
        toggleBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        toggleBtn.setForeground(new Color(125, 125, 125));
        toggleBtn.setBackground(new Color(255, 255, 255, 0)); // Transparent
        toggleBtn.setBorder(new EmptyBorder(0, 0, 0, 8));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setPreferredSize(new Dimension(40, 44));
        toggleBtn.setMaximumSize(new Dimension(40, 44));
        toggleBtn.setAlignmentX(1.0f);
        toggleBtn.setAlignmentY(0.5f);
        
        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('•');
                toggleBtn.setText("👁");
            } else {
                passwordField.setEchoChar((char) 0);
                toggleBtn.setText("🙈");
            }
        });
        
        // Wrapper panel for button positioning
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.setPreferredSize(new Dimension(330, 44));
        buttonWrapper.setMaximumSize(new Dimension(330, 44));
        buttonWrapper.setAlignmentX(0.0f);
        buttonWrapper.setAlignmentY(0.5f);
        buttonWrapper.add(toggleBtn);
        
        passwordPanel.add(buttonWrapper);
        passwordPanel.add(passwordField);
    
        card.add(passwordPanel, gbc);

        // Forgot password link - centered below password field
        gbc.gridy = 6;
        gbc.insets = new Insets(8, 0, 24, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        
        JButton forgotPasswordButton = new JButton("Quên mật khẩu?");
        forgotPasswordButton.setFont(new Font("Inter", Font.PLAIN, 13));
        forgotPasswordButton.setForeground(new Color(39, 110, 241)); // #276EF1
        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);
        forgotPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordButton.setFocusPainted(false);
        forgotPasswordButton.setPreferredSize(new Dimension(130, 25));
        forgotPasswordButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                forgotPasswordButton.setText("<html><span style='text-decoration:underline; white-space:nowrap;'>Quên mật khẩu?</span></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                forgotPasswordButton.setText("Quên mật khẩu?");
            }
        });
        card.add(forgotPasswordButton, gbc);

        // Login button with cyan gradient: #4facfe -> #00f2fe
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
    
    final boolean[] isHovered = {false};
    JButton loginButton = new JButton("Đăng nhập") {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color color1 = isHovered[0] ? new Color(79, 172, 254).brighter() : new Color(79, 172, 254);
            Color color2 = isHovered[0] ? new Color(0, 242, 254).brighter() : new Color(0, 242, 254);
            
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2d.dispose();
            super.paintComponent(g);
        }
    };
    loginButton.setFont(new Font("Inter", Font.BOLD, 15));
    loginButton.setForeground(Color.WHITE);
    loginButton.setPreferredSize(new Dimension(330, 44));
    loginButton.setOpaque(false);
    loginButton.setContentAreaFilled(false);
    loginButton.setBorderPainted(false);
    loginButton.setFocusPainted(false);
    loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    // Hover effect: translateY(-1px)
    loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            isHovered[0] = true;
            loginButton.repaint();
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            isHovered[0] = false;
            loginButton.repaint();
        }
    });
    card.add(loginButton, gbc);

    // Divider: ------ hoặc ------
    gbc.gridy = 8;
    gbc.insets = new Insets(0, 0, 18, 0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    JPanel dividerPanel = new JPanel(new GridBagLayout());
    dividerPanel.setOpaque(false);
    GridBagConstraints divGbc = new GridBagConstraints();
    divGbc.gridy = 0;
    divGbc.fill = GridBagConstraints.HORIZONTAL;
    divGbc.weightx = 1.0;
    divGbc.insets = new Insets(0, 0, 0, 10);
    JSeparator leftSep = new JSeparator();
    leftSep.setForeground(new Color(220, 220, 220));
    dividerPanel.add(leftSep, divGbc);
    divGbc.weightx = 0.0;
    divGbc.insets = new Insets(0, 0, 0, 0);
    JLabel orLabel = new JLabel("hoặc");
    orLabel.setFont(new Font("Inter", Font.PLAIN, 13));
    orLabel.setForeground(new Color(125, 125, 125));
    dividerPanel.add(orLabel, divGbc);
    divGbc.weightx = 1.0;
    divGbc.insets = new Insets(0, 10, 0, 0);
    JSeparator rightSep = new JSeparator();
    rightSep.setForeground(new Color(220, 220, 220));
    dividerPanel.add(rightSep, divGbc);
    card.add(dividerPanel, gbc);

    // Register link - purple/gradient color
    gbc.gridy = 9;
    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    registerPanel.setOpaque(false);
    JLabel noAccountLabel = new JLabel("Chưa có tài khoản?");
    noAccountLabel.setFont(new Font("Inter", Font.PLAIN, 13));
    noAccountLabel.setForeground(new Color(125, 125, 125));
    registerPanel.add(noAccountLabel);
    
    JButton registerButton = new JButton("Tạo ngay");
    registerButton.setFont(new Font("Inter", Font.BOLD, 13));
    registerButton.setForeground(new Color(118, 75, 162)); // #764BA2 secondary
    registerButton.setBorderPainted(false);
    registerButton.setContentAreaFilled(false);
    registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    registerButton.setFocusPainted(false);
    registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            registerButton.setForeground(new Color(102, 126, 234)); // #667eea
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            registerButton.setForeground(new Color(118, 75, 162));
        }
    });
    registerPanel.add(registerButton);
    card.add(registerPanel, gbc);        // thêm card vào wrapper
        wrapper.add(card, wrapperGbc);

        // Action listeners
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (!validateLoginFields(username, password)) {
                return;
            }

            User loginUser = new User();
            loginUser.setUsername(username);
            loginUser.setPassword(password);

            Message request = new Message(RequestType.LOGIN, "");
            request.setUser(loginUser);

            Message response = sendRequest(request);
            if (response != null && response.isSuccess()) {
                currentUser = response.getUser();
                
                // Preload avatar to cache
                if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().trim().isEmpty()) {
                    preloadAvatar(currentUser.getAvatarUrl());
                }
                
                if (currentUser.isAdmin()) {
                    cardLayout.show(contentPanel, "ADMIN");
                } else {
                    contentPanel.remove(contentPanel.getComponent(2));
                    contentPanel.add(createUserPanel(), "USER");
                    cardLayout.show(contentPanel, "USER");
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                        response != null ? response.getError() : "Lỗi kết nối",
                        "Đăng nhập thất bại",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        forgotPasswordButton.addActionListener(e -> {
            String email = JOptionPane.showInputDialog(mainFrame,
                    "Nhập email của bạn để reset mật khẩu:",
                    "Quên mật khẩu",
                    JOptionPane.QUESTION_MESSAGE);
            if (email != null && !email.trim().isEmpty()) {
                if (isValidEmail(email.trim())) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "📧 Liên kết reset mật khẩu đã được gửi đến:\n" + email.trim() +
                                    "\n\n(Demo: Mật khẩu mặc định là 'newpass123')",
                            "Email đã gửi",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showValidationError("Email không hợp lệ!");
                }
            }
        });

        registerButton.addActionListener(e -> cardLayout.show(contentPanel, "REGISTER"));

        if (mainFrame != null)
            mainFrame.getRootPane().setDefaultButton(loginButton);

        return wrapper;
    }

    // ---------------- REGISTER PANEL ----------------
    private JPanel createRegisterPanel() {
        // Main wrapper with modern gradient background
        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                // Gradient: #eef4ff -> #ffffff
                GradientPaint gp = new GradientPaint(0, 0, new Color(238, 244, 255), 0, h, Color.WHITE);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        GridBagConstraints wrapperGbc = new GridBagConstraints();
        wrapperGbc.gridx = 0;
        wrapperGbc.gridy = 0;
        wrapperGbc.anchor = GridBagConstraints.CENTER;

        // Modern card with shadow - 450px width
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(450, 800));
        // Shadow effect: 0 4px 20px rgba(0,0,0,0.08)
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(35, 40, 35, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel titleLabel = new JLabel("Tạo tài khoản", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(new Color(39, 110, 241)); // #276EF1
        card.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Tham gia hệ thống ngay hôm nay", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(125, 125, 125)); // #7D7D7D
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 24, 0);
        card.add(subtitleLabel, gbc);

        // Input fields với màu xanh
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel userLabel = new JLabel("👤 Tên đăng nhập");
        userLabel.setFont(new Font("Inter", Font.BOLD, 13));
        userLabel.setForeground(new Color(55, 65, 81));
        card.add(userLabel, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 18, 0);
        JTextField usernameField = createLargeStyledTextField();
        usernameField.setPreferredSize(new Dimension(430, 38));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253), 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        card.add(usernameField, gbc);

        // Full name
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel nameLabel = new JLabel("🏷️ Họ và tên");
        nameLabel.setFont(new Font("Inter", Font.BOLD, 13));
        nameLabel.setForeground(new Color(55, 65, 81));
        card.add(nameLabel, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 12, 0);
        JTextField fullNameField = createLargeStyledTextField();
        fullNameField.setPreferredSize(new Dimension(430, 38));
        fullNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253), 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        card.add(fullNameField, gbc);

        // Email
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel emailLabel = new JLabel("📧 Email");
        emailLabel.setFont(new Font("Inter", Font.BOLD, 13));
        emailLabel.setForeground(new Color(55, 65, 81));
        card.add(emailLabel, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 12, 0);
        JTextField emailField = createLargeStyledTextField();
        emailField.setPreferredSize(new Dimension(430, 38));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253), 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        card.add(emailField, gbc);

        // Phone
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel phoneLabel = new JLabel("📱 Số điện thoại");
        phoneLabel.setFont(new Font("Inter", Font.BOLD, 13));
        phoneLabel.setForeground(new Color(55, 65, 81));
        card.add(phoneLabel, gbc);
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 12, 0);
        JTextField phoneField = createLargeStyledTextField();
        phoneField.setPreferredSize(new Dimension(430, 38));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253), 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        card.add(phoneField, gbc);

        // Password
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel passLabel = new JLabel("🔒 Mật khẩu");
        passLabel.setFont(new Font("Inter", Font.BOLD, 13));
        passLabel.setForeground(new Color(55, 65, 81));
        card.add(passLabel, gbc);
        gbc.gridy = 11;
        gbc.insets = new Insets(0, 0, 12, 0);
        
        // Password field with overlay button inside
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new OverlayLayout(passwordPanel));
        passwordPanel.setOpaque(false);
        
        JPasswordField passwordField = createLargeStyledPasswordField();
        passwordField.setPreferredSize(new Dimension(430, 42));
        passwordField.setMaximumSize(new Dimension(430, 42));
        passwordField.setFont(new Font("Inter", Font.PLAIN, 15));
        passwordField.setForeground(new Color(51, 51, 51));
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253), 2, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 45)));
        passwordField.setAlignmentX(0.0f);
        passwordField.setAlignmentY(0.5f);
        
        // Show/hide password button
        JButton togglePasswordBtn1 = new JButton("👁");
        togglePasswordBtn1.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        togglePasswordBtn1.setForeground(new Color(125, 125, 125));
        togglePasswordBtn1.setBackground(new Color(255, 255, 255, 0)); // Transparent
        togglePasswordBtn1.setBorder(new EmptyBorder(0, 0, 0, 8));
        togglePasswordBtn1.setFocusPainted(false);
        togglePasswordBtn1.setContentAreaFilled(false);
        togglePasswordBtn1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePasswordBtn1.setPreferredSize(new Dimension(40, 42));
        togglePasswordBtn1.setMaximumSize(new Dimension(40, 42));
        togglePasswordBtn1.setAlignmentX(1.0f);
        togglePasswordBtn1.setAlignmentY(0.5f);
        togglePasswordBtn1.setToolTipText("Hiện/Ẩn mật khẩu");
        
        togglePasswordBtn1.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('•');
                togglePasswordBtn1.setText("👁");
            } else {
                passwordField.setEchoChar((char) 0);
                togglePasswordBtn1.setText("🙈");
            }
        });
        
        // Wrapper panel for button positioning
        JPanel btnWrapper1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrapper1.setOpaque(false);
        btnWrapper1.setPreferredSize(new Dimension(430, 42));
        btnWrapper1.setMaximumSize(new Dimension(430, 42));
        btnWrapper1.setAlignmentX(0.0f);
        btnWrapper1.setAlignmentY(0.5f);
        btnWrapper1.add(togglePasswordBtn1);
        
        passwordPanel.add(btnWrapper1);
        passwordPanel.add(passwordField);
        
        card.add(passwordPanel, gbc);

        // Confirm Password
        gbc.gridy = 12;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel confirmPassLabel = new JLabel("✅ Xác nhận mật khẩu");
        confirmPassLabel.setFont(new Font("Inter", Font.BOLD, 13));
        confirmPassLabel.setForeground(new Color(55, 65, 81));
        card.add(confirmPassLabel, gbc);
        gbc.gridy = 13;
        gbc.insets = new Insets(0, 0, 18, 0);
        
        // Confirm password field with overlay button inside
        JPanel confirmPasswordPanel = new JPanel();
        confirmPasswordPanel.setLayout(new OverlayLayout(confirmPasswordPanel));
        confirmPasswordPanel.setOpaque(false);
        
        JPasswordField confirmPasswordField = createLargeStyledPasswordField();
        confirmPasswordField.setPreferredSize(new Dimension(430, 42));
        confirmPasswordField.setMaximumSize(new Dimension(430, 42));
        confirmPasswordField.setFont(new Font("Inter", Font.PLAIN, 15));
        confirmPasswordField.setForeground(new Color(51, 51, 51));
        confirmPasswordField.setBackground(Color.WHITE);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(251, 182, 206), 2, true), // #fbb6ce
                BorderFactory.createEmptyBorder(10, 12, 10, 45)));
        confirmPasswordField.setAlignmentX(0.0f);
        confirmPasswordField.setAlignmentY(0.5f);
        
        // Show/hide password button
        JButton togglePasswordBtn2 = new JButton("👁");
        togglePasswordBtn2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        togglePasswordBtn2.setForeground(new Color(107, 114, 128));
        togglePasswordBtn2.setBackground(new Color(255, 255, 255, 0)); // Transparent
        togglePasswordBtn2.setBorder(new EmptyBorder(0, 0, 0, 8));
        togglePasswordBtn2.setFocusPainted(false);
        togglePasswordBtn2.setContentAreaFilled(false);
        togglePasswordBtn2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePasswordBtn2.setPreferredSize(new Dimension(40, 42));
        togglePasswordBtn2.setMaximumSize(new Dimension(40, 42));
        togglePasswordBtn2.setAlignmentX(1.0f);
        togglePasswordBtn2.setAlignmentY(0.5f);
        togglePasswordBtn2.setToolTipText("Hiện/Ẩn mật khẩu");
        
        togglePasswordBtn2.addActionListener(e -> {
            if (confirmPasswordField.getEchoChar() == (char) 0) {
                confirmPasswordField.setEchoChar('•');
                togglePasswordBtn2.setText("👁");
            } else {
                confirmPasswordField.setEchoChar((char) 0);
                togglePasswordBtn2.setText("🙈");
            }
        });
        
        // Wrapper panel for button positioning
        JPanel btnWrapper2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrapper2.setOpaque(false);
        btnWrapper2.setPreferredSize(new Dimension(430, 42));
        btnWrapper2.setMaximumSize(new Dimension(430, 42));
        btnWrapper2.setAlignmentX(0.0f);
        btnWrapper2.setAlignmentY(0.5f);
        btnWrapper2.add(togglePasswordBtn2);
        
        confirmPasswordPanel.add(btnWrapper2);
        confirmPasswordPanel.add(confirmPasswordField);
        
        card.add(confirmPasswordPanel, gbc);

        // Avatar URL field
        gbc.gridy = 14;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel avatarLabel = new JLabel("🖼️ URL ảnh avatar");
        avatarLabel.setFont(new Font("Inter", Font.BOLD, 13));
        avatarLabel.setForeground(new Color(55, 65, 81));
        card.add(avatarLabel, gbc);
        
        gbc.gridy = 15;
        gbc.insets = new Insets(0, 0, 18, 0);
        
        // Avatar URL panel with preview button inside
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new OverlayLayout(avatarPanel));
        avatarPanel.setOpaque(false);
        
        JTextField avatarUrlField = createLargeStyledTextField();
        avatarUrlField.setPreferredSize(new Dimension(430, 38));
        avatarUrlField.setMaximumSize(new Dimension(430, 38));
        avatarUrlField.setFont(new Font("Inter", Font.PLAIN, 13));
        avatarUrlField.setForeground(new Color(51, 51, 51));
        avatarUrlField.setBackground(Color.WHITE);
        avatarUrlField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253), 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 50)));
        avatarUrlField.setAlignmentX(0.0f);
        avatarUrlField.setAlignmentY(0.5f);
        avatarUrlField.setToolTipText("Nhập đường dẫn URL của ảnh avatar");
        
        // Preview button
        JButton previewBtn = new JButton("👁");
        previewBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        previewBtn.setForeground(new Color(125, 125, 125));
        previewBtn.setBackground(new Color(255, 255, 255, 0)); // Transparent
        previewBtn.setBorder(new EmptyBorder(0, 0, 0, 8));
        previewBtn.setFocusPainted(false);
        previewBtn.setContentAreaFilled(false);
        previewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        previewBtn.setPreferredSize(new Dimension(40, 38));
        previewBtn.setMaximumSize(new Dimension(40, 38));
        previewBtn.setAlignmentX(1.0f);
        previewBtn.setAlignmentY(0.5f);
        previewBtn.setToolTipText("Xem trước avatar");
        
        previewBtn.addActionListener(e -> {
            String url = avatarUrlField.getText().trim();
            if (url.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Vui lòng nhập URL ảnh trước!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create preview dialog
            JDialog previewDialog = new JDialog(mainFrame, "Xem trước Avatar", true);
            previewDialog.setLayout(new BorderLayout(10, 10));
            previewDialog.setSize(400, 450);
            previewDialog.setLocationRelativeTo(mainFrame);
            
            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            contentPanel.setBackground(Color.WHITE);
            
            JLabel imageLabel = new JLabel("Đang tải...", SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(350, 350));
            imageLabel.setBorder(BorderFactory.createLineBorder(new Color(147, 197, 253), 2));
            contentPanel.add(imageLabel, BorderLayout.CENTER);
            
            JButton closeBtn = new JButton("Đóng");
            closeBtn.setFont(new Font("Inter", Font.BOLD, 14));
            closeBtn.setPreferredSize(new Dimension(100, 36));
            closeBtn.addActionListener(ev -> previewDialog.dispose());
            
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnPanel.setBackground(Color.WHITE);
            btnPanel.add(closeBtn);
            contentPanel.add(btnPanel, BorderLayout.SOUTH);
            
            previewDialog.add(contentPanel);
            
            // Load image in background
            new Thread(() -> {
                try {
                    URL imageUrl = new URL(url);
                    ImageIcon icon = new ImageIcon(imageUrl);
                    
                    // Resize image to fit
                    Image img = icon.getImage();
                    int width = icon.getIconWidth();
                    int height = icon.getIconHeight();
                    
                    if (width > 350 || height > 350) {
                        double scale = Math.min(350.0 / width, 350.0 / height);
                        int newWidth = (int) (width * scale);
                        int newHeight = (int) (height * scale);
                        img = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(img);
                    }
                    
                    ImageIcon finalIcon = icon;
                    SwingUtilities.invokeLater(() -> {
                        imageLabel.setText("");
                        imageLabel.setIcon(finalIcon);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        imageLabel.setText("<html><center>❌<br>Không thể tải ảnh<br>" + ex.getMessage() + "</center></html>");
                        imageLabel.setForeground(Color.RED);
                    });
                }
            }).start();
            
            previewDialog.setVisible(true);
        });
        
        // Wrapper panel for button positioning
        JPanel avatarBtnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        avatarBtnWrapper.setOpaque(false);
        avatarBtnWrapper.setPreferredSize(new Dimension(430, 38));
        avatarBtnWrapper.setMaximumSize(new Dimension(430, 38));
        avatarBtnWrapper.setAlignmentX(0.0f);
        avatarBtnWrapper.setAlignmentY(0.5f);
        avatarBtnWrapper.add(previewBtn);
        
        avatarPanel.add(avatarBtnWrapper);
        avatarPanel.add(avatarUrlField);
        
        card.add(avatarPanel, gbc);

        // Buttons
        gbc.gridy = 16;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Adjust button panel to ensure visibility and alignment
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);

        JButton backButton = new JButton("← Quay lại");
        backButton.setFont(new Font("Inter", Font.BOLD, 15));
        backButton.setForeground(new Color(55, 65, 81));
        backButton.setBackground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 2, true));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> cardLayout.show(contentPanel, "LOGIN"));
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backButton.setBackground(new Color(249, 250, 251));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backButton.setBackground(Color.WHITE);
            }
        });
        backButton.setPreferredSize(new Dimension(205, 46));
        buttonPanel.add(backButton);

        JButton okButton = new JButton("✨ Tạo tài khoản");
        okButton.setFont(new Font("Inter", Font.BOLD, 15));
        okButton.setForeground(Color.WHITE);
        okButton.setPreferredSize(new Dimension(205, 46));
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            private boolean isHovered = false;
            
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                c.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        isHovered = true;
                        c.repaint();
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        isHovered = false;
                        c.repaint();
                    }
                });
            }
            
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color color1 = isHovered ? new Color(219, 39, 119) : new Color(236, 72, 153); // #db2777 : #ec4899
                Color color2 = isHovered ? new Color(244, 114, 182) : new Color(251, 182, 206); // #f472b6 : #fbb6ce
                
                GradientPaint gp = new GradientPaint(0, 0, color1, c.getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                super.paint(g, c);
            }
        });
        buttonPanel.add(okButton);

        gbc.gridy = 17;
        gbc.insets = new Insets(20, 0, 0, 0); // Ensure spacing above buttons
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        card.add(buttonPanel, gbc);

        // Thêm card vào wrapper
        wrapper.add(card, wrapperGbc);

        // actions (giữ nguyên logic)
        okButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String avatarUrl = avatarUrlField.getText().trim();

            // Validation
            if (!validateRegisterFields(username, password, confirmPassword, fullName, phone, email)) {
                return;
            }

            User newUser = new User(username, password, fullName, phone, email);
            
            // Set avatar URL if provided
            if (!avatarUrl.isEmpty()) {
                newUser.setAvatarUrl(avatarUrl);
            }
            
            Message request = new Message(RequestType.REGISTER, "");
            request.setUser(newUser);
            Message response = sendRequest(request);

            if (response != null && response.isSuccess()) {
                // Preload avatar to cache if provided
                if (!avatarUrl.isEmpty()) {
                    preloadAvatar(avatarUrl);
                }
                
                JOptionPane.showMessageDialog(mainFrame,
                        "Đăng ký thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Clear form
                usernameField.setText("");
                passwordField.setText("");
                confirmPasswordField.setText("");
                fullNameField.setText("");
                phoneField.setText("");
                emailField.setText("");
                avatarUrlField.setText("");
                cardLayout.show(contentPanel, "LOGIN");
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                        response != null ? response.getError() : "Lỗi kết nối",
                        "Đăng ký thất bại", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> cardLayout.show(contentPanel, "LOGIN"));

        return wrapper;
    }

    // ---------------- USER PANEL ----------------
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Header panel hiện đại với gradient hồng
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(236, 72, 153), // #ec4899
                        getWidth(), 0, new Color(219, 39, 119)); // #db2777
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 100));
        headerPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Left side - Welcome message
        JPanel leftHeader = new JPanel(new GridLayout(2, 1, 0, 5));
        leftHeader.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Dashboard");
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.WHITE);
        leftHeader.add(welcomeLabel);
        
        JLabel subtitleLabel = new JLabel("Quản lý thông tin cá nhân của bạn");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(251, 207, 232)); // Màu hồng nhạt
        leftHeader.add(subtitleLabel);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);

        // Right side - User info và logout
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightHeader.setOpaque(false);
        
        // User info card with avatar
        JPanel userInfoCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        userInfoCard.setBackground(new Color(255, 255, 255, 30));
        userInfoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1, true),
                new EmptyBorder(5, 15, 5, 15)));
        
        // Small avatar icon
        JLabel userAvatar = new JLabel();
        userAvatar.setPreferredSize(new Dimension(32, 32));
        userAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        userAvatar.setVerticalAlignment(SwingConstants.CENTER);
        userAvatar.setText("👤");
        userAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        
        // Load avatar asynchronously
        if (currentUser != null && currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().trim().isEmpty()) {
            String avatarUrl = currentUser.getAvatarUrl();
            String cacheKey = avatarUrl + "_32";
            
            // Check cache first
            if (avatarCache.containsKey(cacheKey)) {
                userAvatar.setIcon(avatarCache.get(cacheKey));
                userAvatar.setText("");
            } else {
                // Load in background
                new Thread(() -> {
                    ImageIcon avatarIcon = loadSmallAvatar(avatarUrl, 32);
                    if (avatarIcon != null) {
                        SwingUtilities.invokeLater(() -> {
                            userAvatar.setIcon(avatarIcon);
                            userAvatar.setText("");
                        });
                    }
                }).start();
            }
        }
        
        userInfoCard.add(userAvatar);
        
        JLabel userName = new JLabel(currentUser != null ? currentUser.getUsername() : "User");
        userName.setFont(new Font("Inter", Font.BOLD, 15));
        userName.setForeground(Color.WHITE);
        userInfoCard.add(userName);
        
        rightHeader.add(userInfoCard);
        
        // Logout button
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Inter", Font.BOLD, 14));
        logoutButton.setForeground(new Color(219, 39, 119)); // Màu hồng đậm
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setPreferredSize(new Dimension(110, 36));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                super.paint(g, c);
            }
        });
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                sendRequest(new Message(RequestType.LOGOUT, ""));
                currentUser = null;
                cardLayout.show(contentPanel, "LOGIN");
            }
        });
        rightHeader.add(logoutButton);
        
        // Navigation bar - thêm vào cùng header panel
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        navBar.setBackground(Color.WHITE);
        navBar.setBorder(new EmptyBorder(0, 40, 5, 40));
        
        headerPanel.add(rightHeader, BorderLayout.EAST);
        
        // Tạo combined panel cho header + navbar
        JPanel headerWithNav = new JPanel(new BorderLayout());
        headerWithNav.setBackground(Color.WHITE);
        headerWithNav.add(headerPanel, BorderLayout.NORTH);
        headerWithNav.add(navBar, BorderLayout.CENTER);
        
        panel.add(headerWithNav, BorderLayout.NORTH);

        // Main content panel (chỉ content area)
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);

        String[] navItems = {"Thông tin cá nhân", "Lịch sử đăng nhập"};
        JButton[] navButtons = new JButton[2];
        CardLayout contentLayout = new CardLayout();
        JPanel contentArea = new JPanel(contentLayout);
        contentArea.setBackground(Color.WHITE);

        for (int i = 0; i < navItems.length; i++) {
            final int index = i;
            JButton navBtn = new JButton(navItems[i]);
            navBtn.setFont(new Font("Inter", Font.BOLD, 14));
            navBtn.setForeground(i == 0 ? new Color(219, 39, 119) : new Color(107, 114, 128));
            navBtn.setBackground(Color.WHITE);
            navBtn.setBorderPainted(false);
            navBtn.setContentAreaFilled(false);
            navBtn.setFocusPainted(false);
            navBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            navBtn.setPreferredSize(new Dimension(160, 40));
            
            // Active indicator
            navBtn.setBorder(i == 0 ? 
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(219, 39, 119)) :
                BorderFactory.createMatteBorder(0, 0, 3, 0, Color.WHITE));
            
            navBtn.addActionListener(e -> {
                // Update all buttons
                for (int j = 0; j < navButtons.length; j++) {
                    if (j == index) {
                        navButtons[j].setForeground(new Color(219, 39, 119));
                        navButtons[j].setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(219, 39, 119)));
                    } else {
                        navButtons[j].setForeground(new Color(107, 114, 128));
                        navButtons[j].setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.WHITE));
                    }
                }
                contentLayout.show(contentArea, navItems[index]);
            });
            
            navButtons[i] = navBtn;
            navBar.add(navBtn);
        }
        
        // Thêm separator line dưới navbar
        JPanel navSeparator = new JPanel();
        navSeparator.setPreferredSize(new Dimension(0, 2));
        navSeparator.setBackground(new Color(229, 231, 235));
        headerWithNav.add(navSeparator, BorderLayout.SOUTH);

        // Content area - Use new UI panels
        contentArea.add(new UserInfoPanel(currentUser, avatarCache, this::sendRequest), navItems[0]);
        contentArea.add(new UserHistoryPanel(currentUser != null ? currentUser.getUsername() : "", this::sendRequest), navItems[1]);
        
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Color.WHITE);
        contentWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentWrapper.add(contentArea, BorderLayout.CENTER);
        
        mainContentPanel.add(contentWrapper, BorderLayout.CENTER);
        
        panel.add(mainContentPanel, BorderLayout.CENTER);

        return panel;
    }

    // Helper method to load avatar with cache
    private void loadAvatar(JLabel label, String url) {
        if (url == null || url.trim().isEmpty()) {
            label.setIcon(null);
            label.setText("No Avatar");
            return;
        }
        
        // Check cache first
        if (avatarCache.containsKey(url)) {
            label.setIcon(avatarCache.get(url));
            label.setText("");
            return;
        }
        
        // Load in background
        new Thread(() -> {
            try {
                java.net.URL imageUrl = new java.net.URL(url);
                ImageIcon icon = new ImageIcon(imageUrl);
                // Resize to fit
                Image img = icon.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(img);
                
                // Cache it
                avatarCache.put(url, scaledIcon);
                
                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    label.setIcon(scaledIcon);
                    label.setText("");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    label.setIcon(null);
                    label.setText("❌ Lỗi tải ảnh");
                });
            }
        }).start();
    }
    
    // Helper method to load small avatar for header
    private ImageIcon loadSmallAvatar(String url, int size) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = url + "_" + size;
        if (avatarCache.containsKey(cacheKey)) {
            return avatarCache.get(cacheKey);
        }
        
        try {
            java.net.URL imageUrl = new java.net.URL(url);
            ImageIcon icon = new ImageIcon(imageUrl);
            Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            
            // Create circular avatar
            BufferedImage circularImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = circularImg.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw circle
            g2d.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
            g2d.drawImage(img, 0, 0, null);
            g2d.dispose();
            
            ImageIcon circularIcon = new ImageIcon(circularImg);
            avatarCache.put(cacheKey, circularIcon);
            return circularIcon;
        } catch (Exception ex) {
            return null;
        }
    }
    
    // Preload avatar into cache (for multiple sizes)
    private void preloadAvatar(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        
        new Thread(() -> {
            try {
                // Load for small size (header)
                loadSmallAvatar(url, 32);
                
                // Load for large size (profile)
                String cacheKey = url + "_140";
                if (!avatarCache.containsKey(cacheKey)) {
                    java.net.URL imageUrl = new java.net.URL(url);
                    ImageIcon icon = new ImageIcon(imageUrl);
                    Image img = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                    avatarCache.put(cacheKey, new ImageIcon(img));
                }
                
                System.out.println("✅ Avatar preloaded: " + url);
            } catch (Exception ex) {
                System.err.println("❌ Failed to preload avatar: " + ex.getMessage());
            }
        }).start();
    }

    // ---------------- ADMIN PANEL ----------------
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(249, 250, 251));

        // Modern Admin header with gradient
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            new EmptyBorder(20, 40, 20, 40)));

        // Left side: Logo and title
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setBackground(Color.WHITE);
        
        // Admin icon with gradient background
        JLabel adminIcon = new JLabel("👑");
        adminIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        adminIcon.setOpaque(true);
        adminIcon.setHorizontalAlignment(SwingConstants.CENTER);
        adminIcon.setPreferredSize(new Dimension(60, 60));
        
        // Create gradient background for icon
        JPanel iconWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(0, 0, new Color(236, 72, 153), // #ec4899
                                                      getWidth(), getHeight(), new Color(219, 39, 119)); // #db2777
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        iconWrapper.setPreferredSize(new Dimension(60, 60));
        iconWrapper.setLayout(new BorderLayout());
        iconWrapper.add(adminIcon);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel adminLabel = new JLabel("Admin Portal");
        adminLabel.setFont(new Font("Inter", Font.BOLD, 24));
        adminLabel.setForeground(new Color(17, 24, 39));
        adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Quản lý và giám sát hệ thống");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(adminLabel);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitleLabel);
        
        leftHeader.add(iconWrapper);
        leftHeader.add(titlePanel);
        
        // Right side: User info and logout
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightHeader.setBackground(Color.WHITE);
        
        // Admin badge
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        badge.setBackground(new Color(252, 231, 243)); // Màu hồng nhạt
        badge.setBorder(new EmptyBorder(8, 16, 8, 16));
        
        JLabel badgeIcon = new JLabel("⭐");
        badgeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        
        JLabel badgeText = new JLabel("Administrator");
        badgeText.setFont(new Font("Inter", Font.BOLD, 13));
        badgeText.setForeground(new Color(157, 23, 77)); // Màu hồng đậm
        
        badge.add(badgeIcon);
        badge.add(badgeText);
        
        // Rounded badge
        badge.setUI(new javax.swing.plaf.basic.BasicPanelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g, c);
            }
        });
        
        // Logout button
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Inter", Font.BOLD, 13));
        logoutButton.setForeground(new Color(219, 39, 119)); // Màu hồng đậm
        logoutButton.setBackground(new Color(251, 207, 232)); // Màu hồng nhạt
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        logoutButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                JButton btn = (JButton) c;
                if (btn.getModel().isPressed()) {
                    g2d.setColor(new Color(219, 39, 119)); // Hồng đậm khi nhấn
                } else if (btn.getModel().isRollover()) {
                    g2d.setColor(new Color(244, 114, 182)); // Hồng trung bình khi hover
                } else {
                    g2d.setColor(c.getBackground());
                }
                
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                super.paint(g, c);
            }
        });
        
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                sendRequest(new Message(RequestType.LOGOUT, ""));
                currentUser = null;
                cardLayout.show(contentPanel, "LOGIN");
            }
        });
        
        rightHeader.add(badge);
        rightHeader.add(logoutButton);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);

        // Content area with tabs
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(new Color(249, 250, 251));
        contentWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Modern tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Inter", Font.PLAIN, 14));
        tabs.setBackground(new Color(249, 250, 251));
        tabs.setForeground(new Color(75, 85, 99));
        tabs.setBorder(new EmptyBorder(20, 40, 30, 40));
        tabs.setTabPlacement(JTabbedPane.TOP);

        // Custom tab UI
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabAreaInsets.left = 0;
                tabInsets = new Insets(12, 20, 12, 20);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                // No border
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2d.setColor(Color.WHITE);
                    g2d.fillRoundRect(x, y, w, h - 3, 10, 10);
                    
                    // Bottom indicator
                    g2d.setColor(new Color(236, 72, 153));
                    g2d.fillRoundRect(x + 10, y + h - 6, w - 20, 3, 2, 2);
                } else {
                    g2d.setColor(new Color(249, 250, 251));
                    g2d.fillRoundRect(x, y, w, h - 3, 10, 10);
                }
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics fontMetrics,
                    int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(isSelected ? font.deriveFont(Font.BOLD) : font);
                g.setColor(isSelected ? new Color(17, 24, 39) : new Color(107, 114, 128));
                g.drawString(title, textRect.x, textRect.y + fontMetrics.getAscent());
            }
        });

        // Add tabs
        tabs.addTab("📊 Dashboard", new com.loginSystem.client.ui.AdminDashboardPanel(this::sendRequest));
        tabs.addTab("👥 Quản Lý Users", new com.loginSystem.client.ui.AdminUserManagementPanel(
                mainFrame, this::sendRequest, gson));
        tabs.addTab("📈 Lịch Sử Đăng Nhập", new com.loginSystem.client.ui.AdminHistoryPanel(
                mainFrame, this::sendRequest, gson));
        tabs.addTab("📢 Thông Báo", new com.loginSystem.client.ui.AdminBroadcastPanel(
                mainFrame, this::sendRequest));

        contentWrapper.add(tabs, BorderLayout.CENTER);
        panel.add(contentWrapper, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Title
        JLabel titleLabel = new JLabel("📊 Thống kê hệ thống", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        titleLabel.setForeground(new Color(75, 85, 99));
        titleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Stats area with modern styling
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
        area.setBackground(new Color(249, 250, 251));
        area.setBorder(new EmptyBorder(20, 20, 20, 20));
        area.setForeground(new Color(55, 65, 81));

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Auto-refresh indicator
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);
        JLabel statusLabel = new JLabel("🔄 Tự động cập nhật mỗi 3 giây");
        statusLabel.setFont(new Font("Inter", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(107, 114, 128));
        statusPanel.add(statusLabel);
        panel.add(statusPanel, BorderLayout.SOUTH);

        // Timer auto-refresh mỗi 3 giây
        Timer timer = new Timer(3000, e -> {
            Message resp = sendRequest(new Message(RequestType.GET_STATS, ""));
            if (resp != null && resp.isSuccess()) {
                area.setText(resp.getContent());
            }
        });
        timer.start();

        return panel;
    }

    private JPanel createUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Title
        JLabel titleLabel = new JLabel("👥 Quản lý người dùng", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 22));
        titleLabel.setForeground(new Color(75, 85, 99));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Table với styling hiện đại
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "👤 Username", "📝 Họ tên", "📧 Email", "📱 Điện thoại", "👑 Admin", "🔒 Khóa" }, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 5)
                    return Boolean.class;
                return String.class;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.setRowHeight(35);
        table.setSelectionBackground(new Color(236, 72, 153, 30));
        table.setSelectionForeground(new Color(30, 58, 138));
        table.setGridColor(new Color(229, 231, 235));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Header styling
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(249, 250, 251));
        table.getTableHeader().setForeground(new Color(55, 65, 81));
        table.getTableHeader().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(209, 213, 219)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // Username
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Họ tên
        table.getColumnModel().getColumn(2).setPreferredWidth(180); // Email
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Phone
        table.getColumnModel().getColumn(4).setPreferredWidth(80); // Admin
        table.getColumnModel().getColumn(5).setPreferredWidth(80); // Locked

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Button panel với layout đẹp hơn
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JButton refreshBtn = new JButton("🔄 Làm mới");
        JButton editBtn = new JButton("✏️ Chỉnh sửa");
        JButton deleteBtn = new JButton("🗑️ Xóa");

        styleButton(refreshBtn, new Color(236, 72, 153), Color.WHITE); // Màu hồng
        styleButton(editBtn, new Color(16, 185, 129), Color.WHITE);
        styleButton(deleteBtn, new Color(239, 68, 68), Color.WHITE);

        refreshBtn.setPreferredSize(new Dimension(120, 36));
        editBtn.setPreferredSize(new Dimension(130, 36));
        deleteBtn.setPreferredSize(new Dimension(100, 36));

        // Auto refresh users mỗi 10 giây (tăng từ 5 giây)
        Timer timer = new Timer(10000, e -> loadUsers(model));
        timer.start();

        // Refresh button action
        refreshBtn.addActionListener(e -> {
            loadUsers(model);
            JOptionPane.showMessageDialog(mainFrame, "✅ Đã làm mới danh sách người dùng",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        // Edit button action với validation tốt hơn
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(mainFrame, "❌ Vui lòng chọn một người dùng để chỉnh sửa!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String username = (String) model.getValueAt(row, 0);
            String fullName = (String) model.getValueAt(row, 1);
            String email = (String) model.getValueAt(row, 2);
            String phone = (String) model.getValueAt(row, 3);
            boolean isAdmin = (Boolean) model.getValueAt(row, 4);
            boolean isLocked = (Boolean) model.getValueAt(row, 5);

            // Form chỉnh sửa với styling đẹp hơn
            JTextField fnField = createLargeStyledTextField();
            JTextField emailField = createLargeStyledTextField();
            JTextField phoneField = createLargeStyledTextField();
            fnField.setText(fullName);
            emailField.setText(email);
            phoneField.setText(phone);

            JCheckBox adminBox = new JCheckBox("Quyền Administrator", isAdmin);
            JCheckBox lockedBox = new JCheckBox("Khóa tài khoản", isLocked);
            adminBox.setFont(new Font("Inter", Font.PLAIN, 14));
            lockedBox.setFont(new Font("Inter", Font.PLAIN, 14));

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.anchor = GridBagConstraints.WEST;

            // Họ tên
            gbc.gridx = 0;
            gbc.gridy = 0;
            form.add(new JLabel("👤 Họ tên:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(fnField, gbc);

            // Email
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            form.add(new JLabel("📧 Email:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(emailField, gbc);

            // Điện thoại
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            form.add(new JLabel("📱 Điện thoại:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(phoneField, gbc);

            // Checkboxes
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.NONE;
            form.add(adminBox, gbc);
            gbc.gridy = 4;
            form.add(lockedBox, gbc);

            int result = JOptionPane.showConfirmDialog(mainFrame, form,
                    "✏️ Chỉnh sửa người dùng: " + username,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Validation
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

                // 1. Update info
                User u = new User();
                u.setUsername(username);
                u.setFullName(fnField.getText().trim());
                u.setEmail(emailField.getText().trim());
                u.setPhone(phoneField.getText().trim());
                u.setPassword(""); // không đổi mật khẩu

                Message req = new Message(RequestType.UPDATE_USER, "");
                req.setUser(u);
                sendRequest(req);

                // 2. Update role
                Message roleReq = new Message(RequestType.SET_ADMIN, username + "," + adminBox.isSelected());
                sendRequest(roleReq);

                // 3. Lock/unlock
                if (lockedBox.isSelected()) {
                    sendRequest(new Message(RequestType.LOCK_USER, username));
                } else {
                    sendRequest(new Message(RequestType.UNLOCK_USER, username));
                }

                // Refresh lại bảng
                loadUsers(model);
                JOptionPane.showMessageDialog(mainFrame, "✅ Cập nhật thông tin thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Delete button action với xác nhận
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(mainFrame, "❌ Vui lòng chọn một người dùng để xóa!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String username = (String) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "⚠️ Bạn có chắc chắn muốn xóa người dùng '" + username + "' không?\n" +
                            "Hành động này không thể hoàn tác!",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                Message resp = sendRequest(new Message(RequestType.DELETE_USER, username));
                if (resp != null && resp.isSuccess()) {
                    loadUsers(model);
                    JOptionPane.showMessageDialog(mainFrame, "✅ Đã xóa người dùng thành công!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "❌ Không thể xóa người dùng!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(refreshBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setBackground(Color.WHITE);
        JLabel statusLabel = new JLabel("🔄 Tự động cập nhật mỗi 10 giây");
        statusLabel.setFont(new Font("Inter", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(107, 114, 128));
        statusPanel.add(statusLabel);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(statusPanel, BorderLayout.SOUTH);

        panel.add(contentPanel, BorderLayout.CENTER);

        // Lần đầu load
        loadUsers(model);
        return panel;
    }

    private void loadUsers(DefaultTableModel model) {
        Message resp = sendRequest(new Message(RequestType.GET_ALL_USERS, ""));
        if (resp != null && resp.isSuccess()) {
            model.setRowCount(0);
            List<User> list = gson.fromJson(resp.getContent(),
                    new com.google.gson.reflect.TypeToken<List<User>>() {
                    }.getType());
            for (User u : list) {
                model.addRow(new Object[] { u.getUsername(), u.getFullName(), u.getEmail(),
                        u.getPhone(), u.isAdmin(), u.isLocked() });
            }
        }
    }

    private JPanel createAllHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Title
        JLabel titleLabel = new JLabel("📊 Lịch sử đăng nhập hệ thống", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 22));
        titleLabel.setForeground(new Color(75, 85, 99));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Table để hiển thị lịch sử đăng nhập
        DefaultTableModel historyModel = new DefaultTableModel(
                new Object[] { "👤 Người dùng", "🕒 Thời gian đăng nhập", "💻 Trạng thái", "🌐 Phiên làm việc" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép chỉnh sửa
            }
        };

        JTable historyTable = new JTable(historyModel);
        historyTable.setFont(new Font("Inter", Font.PLAIN, 12));
        historyTable.setRowHeight(32);
        historyTable.setSelectionBackground(new Color(236, 72, 153, 20)); // Màu hồng với transparency
        historyTable.setSelectionForeground(new Color(157, 23, 77)); // Màu hồng đậm
        historyTable.setGridColor(new Color(229, 231, 235));
        historyTable.setShowGrid(true);
        historyTable.setIntercellSpacing(new Dimension(1, 1));

        // Header styling
        historyTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(249, 250, 251));
        historyTable.getTableHeader().setForeground(new Color(55, 65, 81));
        historyTable.getTableHeader().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(236, 72, 153)), // Màu hồng
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Username
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Time
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Status
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Session

        JScrollPane tableScrollPane = new JScrollPane(historyTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true));
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        // Button panel for controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JButton refreshBtn = new JButton("🔄 Làm mới");
        JButton exportBtn = new JButton("📊 Xuất báo cáo");
        JButton clearBtn = new JButton("🗑️ Xóa lịch sử");

        styleButton(refreshBtn, new Color(236, 72, 153), Color.WHITE); // Màu hồng
        styleButton(exportBtn, new Color(16, 185, 129), Color.WHITE);
        styleButton(clearBtn, new Color(239, 68, 68), Color.WHITE);

        refreshBtn.setPreferredSize(new Dimension(120, 36));
        exportBtn.setPreferredSize(new Dimension(140, 36));
        clearBtn.setPreferredSize(new Dimension(130, 36));

        // Auto refresh timer mỗi 8 giây
        Timer historyTimer = new Timer(8000, e -> loadLoginHistory(historyModel));
        historyTimer.start();

        // Refresh button action
        refreshBtn.addActionListener(e -> {
            loadLoginHistory(historyModel);
            JOptionPane.showMessageDialog(mainFrame, "✅ Đã làm mới lịch sử đăng nhập!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        // Export button (hiện tại chỉ hiển thị thông báo)
        exportBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainFrame,
                    "📊 Tính năng xuất báo cáo sẽ được phát triển trong phiên bản tiếp theo!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        // Clear history button (hiện tại chỉ hiển thị thông báo)
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "⚠️ Bạn có chắc chắn muốn xóa toàn bộ lịch sử đăng nhập?\n" +
                            "Hành động này không thể hoàn tác!",
                    "Xác nhận xóa lịch sử", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(mainFrame,
                        "🔧 Tính năng xóa lịch sử sẽ được phát triển trong phiên bản tiếp theo!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        controlPanel.add(refreshBtn);
        controlPanel.add(exportBtn);
        controlPanel.add(clearBtn);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setBackground(Color.WHITE);
        JLabel statusLabel = new JLabel("🔄 Tự động cập nhật mỗi 8 giây");
        statusLabel.setFont(new Font("Inter", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(107, 114, 128));
        statusPanel.add(statusLabel);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(controlPanel, BorderLayout.NORTH);
        contentPanel.add(tableScrollPane, BorderLayout.CENTER);
        contentPanel.add(statusPanel, BorderLayout.SOUTH);

        panel.add(contentPanel, BorderLayout.CENTER);

        // Load initial data
        loadLoginHistory(historyModel);
        return panel;
    }

    // Helper method để load lịch sử đăng nhập vào bảng
    private void loadLoginHistory(DefaultTableModel model) {
        Message req = new Message(RequestType.GET_LOGIN_HISTORY, "ALL");
        Message resp = sendRequest(req);
        if (resp != null && resp.isSuccess()) {
            model.setRowCount(0);
            List<String> logs = gson.fromJson(resp.getContent(),
                    new TypeToken<List<String>>() {
                    }.getType());

            if (!logs.isEmpty()) {
                for (String logEntry : logs) {
                    // Parse log entry: format thường là "username - timestamp - action"
                    // Tạm thời sử dụng format đơn giản, có thể cải thiện sau
                    String[] parts = logEntry.split(" - ", 3);
                    if (parts.length >= 2) {
                        String username = parts[0].trim();
                        String timestamp = parts[1].trim();
                        String status = "✅ Đăng nhập thành công";
                        String session = "Session-" + System.currentTimeMillis() % 10000;

                        model.addRow(new Object[] { username, timestamp, status, session });
                    } else {
                        // Fallback nếu format không đúng
                        model.addRow(new Object[] {
                                "Unknown",
                                logEntry,
                                "📝 Hoạt động",
                                "N/A"
                        });
                    }
                }
            } else {
                // Hiển thị thông báo khi chưa có dữ liệu
                model.addRow(new Object[] {
                        "🔍 Chưa có dữ liệu",
                        "Hệ thống chưa ghi nhận lịch sử đăng nhập nào",
                        "ℹ️ Thông tin",
                        "N/A"
                });
            }
        }
    }

    private JPanel createBroadcastTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title
        JLabel titleLabel = new JLabel("📢 Gửi thông báo tới tất cả người dùng", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 22));
        titleLabel.setForeground(new Color(75, 85, 99));
        titleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Message area
        JTextArea msg = new JTextArea(10, 40);
        msg.setFont(new Font("Inter", Font.PLAIN, 14));
        msg.setBorder(new EmptyBorder(20, 20, 20, 20));
        msg.setBackground(new Color(249, 250, 251));
        msg.setLineWrap(true);
        msg.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(msg);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                        "📝 Nội dung thông báo",
                        0, 0,
                        new Font("Inter", Font.BOLD, 14),
                        new Color(107, 114, 128)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Send button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton send = new JButton("📡 Gửi thông báo");
        styleButton(send, new Color(236, 72, 153), Color.WHITE); // Màu hồng
        send.setPreferredSize(new Dimension(180, 50));
        send.setFont(new Font("Inter", Font.BOLD, 16));

        send.addActionListener(e -> {
            String message = msg.getText().trim();
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Vui lòng nhập nội dung thông báo!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Message req = new Message(RequestType.BROADCAST, message);
            Message resp = sendRequest(req);

            if (resp != null && resp.isSuccess()) {
                JOptionPane.showMessageDialog(mainFrame,
                        "✅ " + resp.getContent(),
                        "Gửi thông báo thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                msg.setText("");
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                        "❌ " + (resp != null ? resp.getError() : "Lỗi kết nối"),
                        "Gửi thông báo thất bại",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(send);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ---------------- UTILITIES ----------------
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
        // Vietnamese phone number format: 10-11 digits, start with 0
        String phoneRegex = "^0[3|5|7|8|9][0-9]{8}$";
        return phone.matches(phoneRegex);
    }

    private boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty())
            return false;
        // 3-20 characters, letters, numbers, underscore only
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6)
            return false;
        // At least 6 characters, contains at least one letter and one number
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        return hasLetter && hasDigit;
    }

    private boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            return false;
        // At least 2 words, each word 2-50 characters
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length < 2)
            return false;
        for (String part : parts) {
            if (part.length() < 2 || part.length() > 50)
                return false;
            if (!part.matches("^[a-zA-ZÀ-ỹ\\s]+$"))
                return false; // Vietnamese characters allowed
        }
        return true;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Lỗi xác thực", JOptionPane.ERROR_MESSAGE);
    }

    private boolean validateLoginFields(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            showValidationError("Tên đăng nhập không được để trống!");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            showValidationError("Mật khẩu không được để trống!");
            return false;
        }
        return true;
    }

    private boolean validateRegisterFields(String username, String password, String confirmPassword,
            String fullName, String phone, String email) {
        if (!isValidUsername(username)) {
            showValidationError("Tên đăng nhập phải có 3-20 ký tự, chỉ chứa chữ cái, số và dấu gạch dưới!");
            return false;
        }
        if (!isValidPassword(password)) {
            showValidationError("Mật khẩu phải có ít nhất 6 ký tự, chứa ít nhất 1 chữ cái và 1 số!");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showValidationError("Mật khẩu xác nhận không khớp!");
            return false;
        }
        if (!isValidFullName(fullName)) {
            showValidationError("Họ và tên phải có ít nhất 2 từ, mỗi từ 2-50 ký tự!");
            return false;
        }
        if (!isValidPhone(phone)) {
            showValidationError("Số điện thoại phải là số Việt Nam hợp lệ (10-11 chữ số, bắt đầu bằng 0)!");
            return false;
        }
        if (!isValidEmail(email)) {
            showValidationError("Email không hợp lệ!");
            return false;
        }
        return true;
    }

    private boolean validateUpdateFields(String fullName, String phone, String email, String password) {
        if (!fullName.trim().isEmpty() && !isValidFullName(fullName)) {
            showValidationError("Họ và tên phải có ít nhất 2 từ, mỗi từ 2-50 ký tự!");
            return false;
        }
        if (!phone.trim().isEmpty() && !isValidPhone(phone)) {
            showValidationError("Số điện thoại phải là số Việt Nam hợp lệ!");
            return false;
        }
        if (!email.trim().isEmpty() && !isValidEmail(email)) {
            showValidationError("Email không hợp lệ!");
            return false;
        }
        if (!password.trim().isEmpty() && !isValidPassword(password)) {
            showValidationError("Mật khẩu mới phải có ít nhất 6 ký tự, chứa ít nhất 1 chữ cái và 1 số!");
            return false;
        }
        return true;
    }

    // Hàng đợi để gửi kết quả response về cho sendRequest
    private BlockingQueue<Message> responseQueue = new ArrayBlockingQueue<>(10);

    private Message sendRequest(Message request) {
        try {
            output.println(gson.toJson(request));
            // chờ tối đa 2 giây để lấy response
            Message resp = responseQueue.poll(2, TimeUnit.SECONDS);
            if (resp == null) {
                System.err.println("Timeout waiting for response");
            }
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startListener() {
        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    Message msg = gson.fromJson(line, Message.class);

                    if (RequestType.BROADCAST.equals(msg.getType())) {
                        if (msg.isSuccess() && msg.getContent().startsWith("Đã gửi")) {
                            // Đây là response cho admin
                            responseQueue.offer(msg);
                        } else {
                            // Đây là broadcast push
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame,
                                    msg.getContent(),
                                    "📢 Thông báo",
                                    JOptionPane.INFORMATION_MESSAGE));
                        }
                    } else {
                        responseQueue.offer(msg);
                    }

                }
            } catch (IOException e) {
                System.err.println("Listener stopped: " + e.getMessage());
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }

    // Install mouse listeners to allow resizing by dragging window edges/corners.
    // Works even when the frame is undecorated.
    private void enableWindowResizing() {
        final int BORDER_DRAG_THICKNESS = 8;

        class ResizeMouseListener extends MouseAdapter {
            private boolean dragging = false;
            private Point dragStart = null;
            private Rectangle startBounds = null;
            private int dragRegion = 0;

            private int regionForPoint(Point p) {
                int w = mainFrame.getWidth();
                int h = mainFrame.getHeight();
                int x = p.x;
                int y = p.y;
                int r = 0;
                if (x < BORDER_DRAG_THICKNESS)
                    r |= 1; // left
                if (x > w - BORDER_DRAG_THICKNESS)
                    r |= 2; // right
                if (y < BORDER_DRAG_THICKNESS)
                    r |= 4; // top
                if (y > h - BORDER_DRAG_THICKNESS)
                    r |= 8; // bottom
                return r;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int r = regionForPoint(e.getPoint());
                Cursor cur = Cursor.getDefaultCursor();
                switch (r) {
                    case 1:
                        cur = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                        break;
                    case 2:
                        cur = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                        break;
                    case 4:
                        cur = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                        break;
                    case 8:
                        cur = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                        break;
                    case 5:
                        cur = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                        break; // left+top
                    case 9:
                        cur = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                        break; // left+bottom
                    case 6:
                        cur = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                        break; // right+top
                    case 10:
                        cur = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                        break; // right+bottom
                    default:
                        cur = Cursor.getDefaultCursor();
                }
                mainFrame.setCursor(cur);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragRegion = regionForPoint(e.getPoint());
                if (dragRegion != 0) {
                    dragging = true;
                    dragStart = e.getLocationOnScreen();
                    startBounds = mainFrame.getBounds();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                dragStart = null;
                startBounds = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!dragging || startBounds == null || dragStart == null)
                    return;
                Point cur = e.getLocationOnScreen();
                int dx = cur.x - dragStart.x;
                int dy = cur.y - dragStart.y;
                Rectangle b = new Rectangle(startBounds);

                // left
                if ((dragRegion & 1) != 0) {
                    b.x += dx;
                    b.width -= dx;
                }
                // right
                if ((dragRegion & 2) != 0) {
                    b.width += dx;
                }
                // top
                if ((dragRegion & 4) != 0) {
                    b.y += dy;
                    b.height -= dy;
                }
                // bottom
                if ((dragRegion & 8) != 0) {
                    b.height += dy;
                }

                // enforce minimum size
                Dimension min = mainFrame.getMinimumSize();
                if (min == null)
                    min = new Dimension(400, 300);
                if (b.width < min.width)
                    b.width = min.width;
                if (b.height < min.height)
                    b.height = min.height;

                mainFrame.setBounds(b);
                mainFrame.validate();
            }
        }

        ResizeMouseListener resizeListener = new ResizeMouseListener();
        // Install on the frame and its content/layered panes so edges/corners capture
        // events
        mainFrame.addMouseListener(resizeListener);
        mainFrame.addMouseMotionListener(resizeListener);
        mainFrame.getContentPane().addMouseListener(resizeListener);
        mainFrame.getContentPane().addMouseMotionListener(resizeListener);
        mainFrame.getLayeredPane().addMouseListener(resizeListener);
        mainFrame.getLayeredPane().addMouseMotionListener(resizeListener);
    }
}
