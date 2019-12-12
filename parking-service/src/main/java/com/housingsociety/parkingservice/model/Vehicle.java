package com.housingsociety.parkingservice.model;

import java.io.Serializable;
import java.util.Objects;

public class Vehicle implements Serializable {
    private String registrationId;
    private String ownerId;
    private String parkingId;
    private String model;
    private String wheelsType;

    public Vehicle(){

    }
    public Vehicle(String registrationId, String ownerId, String parkingId, String model, String wheelsType) {
        this.registrationId = registrationId;
        this.ownerId = ownerId;
        this.parkingId = parkingId;
        this.model = model;
        this.wheelsType = wheelsType;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getParkingId() {
        return parkingId;
    }

    public void setParkingId(String parkingId) {
        this.parkingId = parkingId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getWheelsType() {
        return wheelsType;
    }

    public void setWheelsType(String wheelsType) {
        this.wheelsType = wheelsType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(registrationId, vehicle.registrationId) &&
                Objects.equals(ownerId, vehicle.ownerId) &&
                Objects.equals(parkingId, vehicle.parkingId) &&
                Objects.equals(model, vehicle.model) &&
                Objects.equals(wheelsType, vehicle.wheelsType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registrationId, ownerId, parkingId, model, wheelsType);
    }
}
