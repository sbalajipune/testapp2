package com.housingsociety.apartmentservice.model;

import java.io.Serializable;

public class Apartment implements Serializable {
    private String apartmentId;
    private String apartmentType;
    private String ownerId;
    private String parkingId;
    private int membersCnt;
    private String memberIds;

    public Apartment(){

    }
    public Apartment(String apartmentId, String apartmentType, String ownerId, String parkingId, int membersCnt, String memberIds) {
        this.apartmentId = apartmentId;
        this.apartmentType = apartmentType;
        this.ownerId = ownerId;
        this.parkingId = parkingId;
        this.membersCnt = membersCnt;
        this.memberIds = memberIds;
    }

    public String getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(String apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getApartmentType() {
        return apartmentType;
    }

    public void setApartmentType(String apartmentType) {
        this.apartmentType = apartmentType;
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

    public int getMembersCnt() {
        return membersCnt;
    }

    public void setMembersCnt(int membersCnt) {
        this.membersCnt = membersCnt;
    }

    public String getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(String memberIds) {
        this.memberIds = memberIds;
    }
}
