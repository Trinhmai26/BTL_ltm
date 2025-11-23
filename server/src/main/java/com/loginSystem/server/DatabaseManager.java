package com.loginSystem.server;

import com.loginSystem.common.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/login_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456"; // ⚠️ đổi theo cấu hình MySQL của bạn

    private Connection connection;

    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            createTables();
            createDefaultAdmin();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            String createUsersTable =
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(15), " +
                    "email VARCHAR(100), " +
                    "avatar_url VARCHAR(500), " +
                    "is_admin BOOLEAN DEFAULT FALSE, " +
                    "is_locked BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "last_login TIMESTAMP NULL" +
                    ")";

            String createLoginHistoryTable =
                    "CREATE TABLE IF NOT EXISTS login_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50), " +
                    "login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "success BOOLEAN, " +
                    "ip_address VARCHAR(45), " +
                    "device VARCHAR(255)" +
                    ")";

            stmt.execute(createUsersTable);
            stmt.execute(createLoginHistoryTable);
            
            // Migration: Add avatar_url column if it doesn't exist
            try {
                String addAvatarColumn = 
                    "ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500) AFTER email";
                stmt.execute(addAvatarColumn);
                System.out.println("✅ Avatar column added successfully!");
            } catch (SQLException e) {
                // Column already exists, ignore error
                if (e.getMessage().contains("Duplicate column")) {
                    System.out.println("ℹ️ Avatar column already exists.");
                } else {
                    throw e;
                }
            }
            
            // Migration: Add device column to login_history if it doesn't exist
            try {
                String addDeviceColumn = 
                    "ALTER TABLE login_history ADD COLUMN device VARCHAR(255) AFTER ip_address";
                stmt.execute(addDeviceColumn);
                System.out.println("✅ Device column added successfully!");
            } catch (SQLException e) {
                if (e.getMessage().contains("Duplicate column")) {
                    System.out.println("ℹ️ Device column already exists.");
                } else {
                    throw e;
                }
            }
            
            // Migration: Add logout_time column to login_history if it doesn't exist
            try {
                String addLogoutTimeColumn = 
                    "ALTER TABLE login_history ADD COLUMN logout_time TIMESTAMP NULL AFTER login_time";
                stmt.execute(addLogoutTimeColumn);
                System.out.println("✅ Logout time column added successfully!");
            } catch (SQLException e) {
                if (e.getMessage().contains("Duplicate column")) {
                    System.out.println("ℹ️ Logout time column already exists.");
                } else {
                    throw e;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }


    private void createDefaultAdmin() {
        try {
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE is_admin = TRUE";
            PreparedStatement pstmt = connection.prepareStatement(checkAdmin);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                User admin = new User("admin", "admin123", "Administrator",
                        "0123456789", "admin@system.com");
                admin.setAdmin(true);
                addUser(admin);
                System.out.println("Default admin created: admin/admin123");
            }
        } catch (SQLException e) {
            System.err.println("Error creating default admin: " + e.getMessage());
        }
    }

    public User authenticate(String username, String password) {
        try {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getBoolean("is_locked")) {
                    return null; // bị khóa
                }

                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setAvatarUrl(rs.getString("avatar_url"));
                user.setAdmin(rs.getBoolean("is_admin"));
                user.setLocked(rs.getBoolean("is_locked"));

                updateLastLogin(username);
                logLoginAttempt(username, true, "127.0.0.1", "Windows Desktop");

                return user;
            } else {
                logLoginAttempt(username, false, "127.0.0.1", "Windows Desktop");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            return null;
        }
    }

    public boolean addUser(User user) {
        try {
            String sql = "INSERT INTO users (username, password, full_name, phone, email, avatar_url, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getAvatarUrl());
            pstmt.setBoolean(7, user.isAdmin());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(User user) {
        try {
            boolean updatePassword = (user.getPassword() != null && !user.getPassword().isEmpty());
            String sql = updatePassword
                    ? "UPDATE users SET full_name=?, phone=?, email=?, avatar_url=?, password=? WHERE username=?"
                    : "UPDATE users SET full_name=?, phone=?, email=?, avatar_url=? WHERE username=?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getPhone());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getAvatarUrl());
            if (updatePassword) {
                pstmt.setString(5, user.getPassword());
                pstmt.setString(6, user.getUsername());
            } else {
                pstmt.setString(5, user.getUsername());
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUsername(String oldUsername, String newUsername) {
        try {
            // Check if new username already exists
            PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT username FROM users WHERE username=?");
            checkStmt.setString(1, newUsername);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.err.println("Username " + newUsername + " already exists");
                return false;
            }
            
            // Update username
            PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE users SET username=? WHERE username=?");
            pstmt.setString(1, newUsername);
            pstmt.setString(2, oldUsername);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating username: " + e.getMessage());
            return false;
        }
    }

    public List<String> getLoginHistory(String usernameOrAll) {
        List<String> logs = new ArrayList<>();
        try {
            PreparedStatement pstmt;
            if (usernameOrAll == null || usernameOrAll.isEmpty() || "ALL".equalsIgnoreCase(usernameOrAll)) {
                pstmt = connection.prepareStatement(
                    "SELECT username, login_time, logout_time, success, ip_address, device FROM login_history ORDER BY id DESC LIMIT 200");
            } else {
                pstmt = connection.prepareStatement(
                    "SELECT username, login_time, logout_time, success, ip_address, device FROM login_history WHERE username=? ORDER BY id DESC LIMIT 200");
                pstmt.setString(1, usernameOrAll);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String logoutTime = rs.getTimestamp("logout_time") != null ? rs.getTimestamp("logout_time").toString() : "NULL";
                String line = String.format("%s | %s | %s | %s | %s | %s",
                        rs.getString("username"),
                        rs.getTimestamp("login_time"),
                        logoutTime,
                        rs.getBoolean("success") ? "OK" : "FAIL",
                        rs.getString("ip_address"),
                        rs.getString("device") != null ? rs.getString("device") : "Unknown");
                logs.add(line);
            }
        } catch (SQLException e) {
            System.err.println("Error get login history: " + e.getMessage());
        }
        return logs;
    }
    
    public void updateLogoutTime(String username) {
        try {
            // Update the most recent login entry for this user that doesn't have a logout time
            String sql = "UPDATE login_history SET logout_time = NOW() WHERE username = ? AND logout_time IS NULL ORDER BY id DESC LIMIT 1";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                System.out.println("✅ Logout time updated for user: " + username);
            }
        } catch (SQLException e) {
            System.err.println("Error updating logout time: " + e.getMessage());
        }
    }

    public boolean deleteUser(String username) {
        try {
            String sql = "DELETE FROM users WHERE username = ? AND is_admin = FALSE";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    public boolean lockUser(String username, boolean lock) {
        try {
            String sql = "UPDATE users SET is_locked = ? WHERE username = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setBoolean(1, lock);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error locking/unlocking user: " + e.getMessage());
            return false;
        }
    }


    public boolean setAdminRole(String username, boolean isAdmin) {
        try {
            String sql = "UPDATE users SET is_admin = ? WHERE username = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setBoolean(1, isAdmin);
            pstmt.setString(2, username);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error setting admin role: " + e.getMessage());
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            String sql = "SELECT * FROM users ORDER BY username";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setAvatarUrl(rs.getString("avatar_url"));
                user.setAdmin(rs.getBoolean("is_admin"));
                user.setLocked(rs.getBoolean("is_locked"));

                Timestamp lastLogin = rs.getTimestamp("last_login");
                if (lastLogin != null) {
                    user.setLastLogin(lastLogin.toString());
                }

                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }

    private void updateLastLogin(String username) {
        try {
            String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE username = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }

    private void logLoginAttempt(String username, boolean success, String ipAddress, String device) {
        try {
            String sql = "INSERT INTO login_history (username, success, ip_address, device) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setBoolean(2, success);
            pstmt.setString(3, ipAddress);
            pstmt.setString(4, device);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging login attempt: " + e.getMessage());
        }
    }

    public String getSystemStats() {
        try {
            StringBuilder stats = new StringBuilder();

            String totalUsersSQL = "SELECT COUNT(*) FROM users";
            PreparedStatement pstmt = connection.prepareStatement(totalUsersSQL);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.append("Tổng số người dùng: ").append(rs.getInt(1)).append("\n");
            }

            String failedLoginsSQL = "SELECT COUNT(*) FROM login_history WHERE success = FALSE AND DATE(login_time) = CURDATE()";
            pstmt = connection.prepareStatement(failedLoginsSQL);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.append("Đăng nhập thất bại hôm nay: ").append(rs.getInt(1)).append("\n");
            }

            String lockedAccountsSQL = "SELECT COUNT(*) FROM users WHERE is_locked = TRUE";
            pstmt = connection.prepareStatement(lockedAccountsSQL);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.append("Tài khoản bị khóa: ").append(rs.getInt(1));
            }

            return stats.toString();
        } catch (SQLException e) {
            System.err.println("Error getting system stats: " + e.getMessage());
            return "Lỗi lấy thống kê hệ thống";
        }
    }
    
    public boolean deleteLoginHistory(String username) {
        try {
            String sql;
            PreparedStatement pstmt;
            
            if (username == null || username.isEmpty() || "ALL".equalsIgnoreCase(username)) {
                sql = "DELETE FROM login_history";
                pstmt = connection.prepareStatement(sql);
            } else {
                sql = "DELETE FROM login_history WHERE username = ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, username);
            }
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting login history: " + e.getMessage());
            return false;
        }
    }
    
    // Advanced statistics methods for dashboard charts
    public String getDetailedStats() {
        StringBuilder stats = new StringBuilder();
        try {
            // Total users (excluding admin)
            String totalUsersSQL = "SELECT COUNT(*) FROM users WHERE is_admin = FALSE";
            PreparedStatement pstmt = connection.prepareStatement(totalUsersSQL);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.append("total_users:").append(rs.getInt(1)).append("\n");
            }
            
            // Today's successful logins (excluding admin)
            String todayLoginsSQL = "SELECT COUNT(*) FROM login_history lh " +
                                   "JOIN users u ON lh.username = u.username " +
                                   "WHERE lh.success = TRUE AND DATE(lh.login_time) = CURDATE() AND u.is_admin = FALSE";
            pstmt = connection.prepareStatement(todayLoginsSQL);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.append("today_logins:").append(rs.getInt(1)).append("\n");
            }
            
            // Locked accounts (excluding admin)
            String lockedAccountsSQL = "SELECT COUNT(*) FROM users WHERE is_locked = TRUE AND is_admin = FALSE";
            pstmt = connection.prepareStatement(lockedAccountsSQL);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.append("locked_accounts:").append(rs.getInt(1)).append("\n");
            }
            
            // Active sessions (logged in but not logged out, excluding admin)
            String activeSessionsSQL = "SELECT COUNT(DISTINCT lh.username) FROM login_history lh " +
                                      "JOIN users u ON lh.username = u.username " +
                                      "WHERE lh.logout_time IS NULL AND DATE(lh.login_time) = CURDATE() AND u.is_admin = FALSE";
            pstmt = connection.prepareStatement(activeSessionsSQL);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.append("active_sessions:").append(rs.getInt(1)).append("\n");
            }
            
            // Last 7 days login trend (excluding admin)
            String trendSQL = "SELECT DATE(lh.login_time) as date, COUNT(*) as count " +
                             "FROM login_history lh " +
                             "JOIN users u ON lh.username = u.username " +
                             "WHERE lh.success = TRUE AND lh.login_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) AND u.is_admin = FALSE " +
                             "GROUP BY DATE(lh.login_time) ORDER BY date DESC LIMIT 7";
            pstmt = connection.prepareStatement(trendSQL);
            rs = pstmt.executeQuery();
            stats.append("login_trend:");
            while (rs.next()) {
                stats.append(rs.getString("date")).append(":").append(rs.getInt("count")).append(",");
            }
            stats.append("\n");
            
            // Success vs Failed logins today (excluding admin)
            String successRateSQL = "SELECT lh.success, COUNT(*) as count FROM login_history lh " +
                                   "JOIN users u ON lh.username = u.username " +
                                   "WHERE DATE(lh.login_time) = CURDATE() AND u.is_admin = FALSE " +
                                   "GROUP BY lh.success";
            pstmt = connection.prepareStatement(successRateSQL);
            rs = pstmt.executeQuery();
            int successCount = 0, failCount = 0;
            while (rs.next()) {
                if (rs.getBoolean("success")) {
                    successCount = rs.getInt("count");
                } else {
                    failCount = rs.getInt("count");
                }
            }
            stats.append("login_success:").append(successCount).append("\n");
            stats.append("login_failed:").append(failCount).append("\n");
            
            // Top 5 most active users this month (excluding admin)
            String topUsersSQL = "SELECT lh.username, COUNT(*) as login_count FROM login_history lh " +
                                "JOIN users u ON lh.username = u.username " +
                                "WHERE lh.success = TRUE AND MONTH(lh.login_time) = MONTH(CURDATE()) AND u.is_admin = FALSE " +
                                "GROUP BY lh.username ORDER BY login_count DESC LIMIT 5";
            pstmt = connection.prepareStatement(topUsersSQL);
            rs = pstmt.executeQuery();
            stats.append("top_users:");
            while (rs.next()) {
                stats.append(rs.getString("username")).append(":").append(rs.getInt("login_count")).append(",");
            }
            stats.append("\n");
            
            return stats.toString();
        } catch (SQLException e) {
            System.err.println("Error getting detailed stats: " + e.getMessage());
            return "error:true";
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
