package com.example.busticketpro.repository;

import com.example.busticketpro.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}   