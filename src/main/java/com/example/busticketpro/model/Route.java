package com.example.busticketpro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_location_id")
    private Location fromLocation;

    @ManyToOne
    @JoinColumn(name = "to_location_id")
    private Location toLocation;

    private Integer distanceKm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Location getFromLocation() { return fromLocation; }
    public void setFromLocation(Location fromLocation) { this.fromLocation = fromLocation; }
    public Location getToLocation() { return toLocation; }
    public void setToLocation(Location toLocation) { this.toLocation = toLocation; }
    public Integer getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Integer distanceKm) { this.distanceKm = distanceKm; }
}