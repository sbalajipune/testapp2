package com.housingsociety.apartmentservice.model;

import java.io.Serializable;
import java.util.List;

public class ParkingDetails implements Serializable {
    private String parkingId;
    private String apartmentId;
    private Member owner;
    private int level;
    private List<VehicleDetails> vehicles;

    ParkingDetails()
    {

    }

    public ParkingDetails(String parkingId, String apartmentId, Member owner, int level, List<VehicleDetails> vehicles) {
        this.parkingId = parkingId;
        this.apartmentId = apartmentId;
        this.owner = owner;
        this.level = level;
        this.vehicles = vehicles;
    }

    public String getParkingId() {
        return parkingId;
    }

    public void setParkingId(String parkingId) {
        this.parkingId = parkingId;
    }

    public String getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(String apartmentId) {
        this.apartmentId = apartmentId;
    }

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<VehicleDetails> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<VehicleDetails> vehicles) {
        this.vehicles = vehicles;
    }
}
