package com.example.busticketpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileDTO {
    @NotBlank
    @Size(max = 50)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^(0[0-9]{9})$")
    private String phone;

    @NotBlank
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
