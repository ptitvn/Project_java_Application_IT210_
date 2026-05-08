package com.example.busticketpro.repository;

import com.example.busticketpro.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
}