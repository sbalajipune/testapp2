package com.housingsociety.apartmentservice.dao;

import com.housingsociety.apartmentservice.model.Apartment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ApartmentRowMapper implements RowMapper {
    public Apartment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Apartment vehicle = new Apartment(
                rs.getString("apartmentId"),
                rs.getString("apartmentType"),
                rs.getString("ownerId"),
                rs.getString("parkingId"),
                rs.getInt("membersCnt"),
                rs.getString("memberIds")
        );
        return vehicle;
    }
}
