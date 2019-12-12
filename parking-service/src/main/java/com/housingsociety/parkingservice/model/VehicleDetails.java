package com.housingsociety.parkingservice.model;

import java.io.Serializable;

public class VehicleDetails implements Serializable {
    private String registrationId;
    private Member owner;
    private String parkingId;
    private String model;
    private String wheelsType;

    public VehicleDetails(){

    }
    public VehicleDetails(String registrationId, Member owner, String parkingId, String model, String wheelsType) {
        this.registrationId = registrationId;
        this.owner = owner;
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

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
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
}
