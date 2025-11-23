package com.loginSystem.common;

public class User {
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;
    private boolean admin;   // ✅ không đặt isAdmin
    private boolean locked;  // ✅ không đặt isLocked
    private boolean online;  // Trạng thái online/offline
    private String lastLogin;
    private String avatarUrl;

    public User() {}

    public User(String username, String password, String fullName, String phone, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.admin = false;
        this.locked = false;
    }

    // -------- Getters & Setters ----------
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
}
