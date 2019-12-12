package com.housingsociety.apartmentservice.model;

import java.io.Serializable;
import java.util.List;

public class ApartmentDetails implements Serializable {
    private String apartmentId;
    private String apartmentType;
    private Member owner;
    ParkingDetails parkingDetails;
    private int membersCnt;
    private List<Member> members;

    public ApartmentDetails(){

    }

    public ApartmentDetails(String apartmentId, String apartmentType, Member owner, ParkingDetails parkingDetails, int membersCnt, List<Member> members) {
        this.apartmentId = apartmentId;
        this.apartmentType = apartmentType;
        this.owner = owner;
        this.parkingDetails = parkingDetails;
        this.membersCnt = membersCnt;
        this.members = members;
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

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }

    public ParkingDetails getParkingDetails(){
        return parkingDetails;
    }

    public void setParkingDetails(ParkingDetails parkingDetails) {
        this.parkingDetails = parkingDetails;
    }

    public int getMembersCnt() {
        return membersCnt;
    }

    public void setMembersCnt(int membersCnt) {
        this.membersCnt = membersCnt;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
}
