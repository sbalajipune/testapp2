package com.housingsociety.parkingservice.dao;

import com.housingsociety.parkingservice.model.Parking;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ParkingRowMapper implements RowMapper {
    public Parking mapRow(ResultSet rs, int rowNum) throws SQLException {
        Parking parking = new Parking(
                rs.getString("parkingId"),
                rs.getString("apartmentId"),
                rs.getString("ownerId"),
                rs.getInt("lvl"),
                rs.getString("vehicles")
        );
        return parking;
    }
}
