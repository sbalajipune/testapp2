package com.housingsociety.vehicleservice.model;

import java.io.Serializable;
import java.util.Objects;

public class Member implements Serializable {
    private String memberId;
    private char gender;
    private String memberFirstName;
    private String memberLastName;
    private int age;
    private String profession;

    public Member(){

    }

    public Member(String memberId, char gender, String memberFirstName, String memberLastName, int age, String profession) {
        this.memberId = memberId;
        this.gender = gender;
        this.memberFirstName = memberFirstName;
        this.memberLastName = memberLastName;
        this.age = age;
        this.profession = profession;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public String getMemberFirstName() {
        return memberFirstName;
    }

    public void setMemberFirstName(String memberFirstName) {
        this.memberFirstName = memberFirstName;
    }

    public String getMemberLastName() {
        return memberLastName;
    }

    public void setMemberLastName(String memberLastName) {
        this.memberLastName = memberLastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return gender == member.gender &&
                age == member.age &&
                memberId.equals(member.memberId) &&
                Objects.equals(memberFirstName, member.memberFirstName) &&
                Objects.equals(memberLastName, member.memberLastName) &&
                Objects.equals(profession, member.profession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, gender, memberFirstName, memberLastName, age, profession);
    }
}
