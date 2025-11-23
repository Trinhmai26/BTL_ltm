package com.loginSystem.common;

public class LoginRecord {
    private String username;
    private String time;
    private boolean success;
    private String ip;

    public LoginRecord() {}

    public LoginRecord(String username, String time, boolean success, String ip) {
        this.username = username;
        this.time = time;
        this.success = success;
        this.ip = ip;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
