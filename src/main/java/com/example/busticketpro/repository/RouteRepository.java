package com.example.busticketpro.repository;

import com.example.busticketpro.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}