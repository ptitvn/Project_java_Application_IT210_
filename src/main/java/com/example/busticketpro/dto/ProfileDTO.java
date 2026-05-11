package com.example.busticketpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
// dữ liệu trung gian để hiển thị và cập nhật hồ sơ cá nhân của người dùng.
public class ProfileDTO {
    @NotBlank(message = "không được để trống max 50 ký tự")
    @Size(max = 50)
    private String fullName;

    @NotBlank(message = "không được để trống")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "phải là 10 số")
    private String phone;

    @NotBlank(message = "không được để trống")
    @Email
    private String email;

    // Getter & Setter
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
