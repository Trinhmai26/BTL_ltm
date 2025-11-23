package com.loginSystem.common;

public class Message {
    private String type;
    private String content;
    private User user;
    private boolean success;
    private String error;

    public Message() {}

    public Message(String type, String content) {
        this.type = type;
        this.content = content;
    }

    // Getters & Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
