package com.housingsociety.parkingservice.model;

import java.io.Serializable;
import java.util.Objects;

public class Parking implements Serializable {
    private String parkingId;
    private String apartmentId;
    private String ownerId;
    private int level;
    private String vehicles;

    public Parking(){

    }

    public Parking(String parkingId, String apartmentId, String ownerId, int level, String vehicles) {
        this.parkingId = parkingId;
        this.apartmentId = apartmentId;
        this.ownerId = ownerId;
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

    public String getOwnerId() {
        return ownerId;
    }

    public void setApartmentId(String apartmentId) {
        this.apartmentId = apartmentId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getVehicles() {
        return vehicles;
    }

    public void setVehicles(String vehicles) {
        this.vehicles = vehicles;
    }
}
