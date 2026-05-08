package com.example.busticketpro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Entity
@Table(name = "buses")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Biển số xe không được để trống")
    @Size(max = 20, message = "Biển số xe tối đa 20 ký tự")
    private String licensePlate;

    @NotBlank(message = "Loại xe không được để trống")
    private String type; // Ví dụ: 29 chỗ, 45 chỗ

    @NotNull(message = "Số ghế không được để trống")
    @Min(value = 10, message = "Xe phải có ít nhất 10 ghế")
    @Max(value = 60, message = "Xe không được quá 60 ghế")
    private Integer seatCount;

    @NotBlank(message = "Tên tài xế không được để trống")
    @Size(max = 50, message = "Tên tài xế tối đa 50 ký tự")
    private String driverName;

    // ===== Getter & Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getSeatCount() { return seatCount; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
}
