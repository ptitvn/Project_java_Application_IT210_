package com.example.busticketpro.dto;

import com.example.busticketpro.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
// dữ liệu để đăng ký tài khoản mới cho người dùng.
public class UserDTO {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 20 )
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8 )
    private String password;

    private Role role = Role.PASSENGER;


    private String confirmPassword;

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    // Getter & Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
