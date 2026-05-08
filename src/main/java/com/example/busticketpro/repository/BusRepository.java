package com.example.busticketpro.repository;

import com.example.busticketpro.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByLicensePlate(String licensePlate);
}
