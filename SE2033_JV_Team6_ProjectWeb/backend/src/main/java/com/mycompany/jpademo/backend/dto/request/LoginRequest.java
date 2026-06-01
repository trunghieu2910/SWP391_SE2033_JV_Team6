package com.mycompany.jpademo.backend.dto.request;

public class LoginRequest {
    private String userName;
    private String password;
    
    // Getters and Setters
    public String getUsername() {
        return userName;
    }
    
    public void setUsername(String username) {
        this.userName = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

