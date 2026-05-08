package com.example.busticketpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class BusDTO {
    @NotBlank(message = "Biển số xe không được để trống")
    private String licensePlate;

    @NotBlank(message = "Loại xe không được để trống")
    private String type;

    @NotNull(message = "Số ghế không được để trống")
    @Min(value = 10, message = "Số ghế tối thiểu là 10")
    @Max(value = 60, message = "Số ghế tối đa là 60")
    private Integer seatCount;

    @NotBlank(message = "Tên tài xế không được để trống")
    private String driverName;

    // Getter & Setter
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getSeatCount() { return seatCount; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
}
