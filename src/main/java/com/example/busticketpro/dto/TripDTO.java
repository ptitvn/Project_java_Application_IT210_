package com.example.busticketpro.dto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class TripDTO {

    @NotNull(message = "Vui lòng chọn xe")
    private Long busId;

    @NotNull(message = "Vui lòng chọn tuyến đường")
    private Long routeId;

    @NotNull(message = "Vui lòng chọn ngày giờ khởi hành")
    @Future(message = "Ngày giờ khởi hành phải ở tương lai")
    private LocalDateTime departureTime;

    @NotNull(message = "Vui lòng nhập giá vé")
    @Positive(message = "Giá vé phải lớn hơn 0")
    private Double price;

    public Long getBusId() { return busId; }
    public void setBusId(Long busId) { this.busId = busId; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}