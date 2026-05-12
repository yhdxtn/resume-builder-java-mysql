package com.example.resumebuilder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterForm {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 60, message = "用户名长度为 3-60 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 80, message = "密码至少 6 位")
    private String password;

    private String fullName;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
