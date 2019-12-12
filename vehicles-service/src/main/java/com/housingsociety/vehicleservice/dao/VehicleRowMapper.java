package com.housingsociety.vehicleservice.dao;

import com.housingsociety.vehicleservice.model.Vehicle;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VehicleRowMapper implements RowMapper {
    public Vehicle mapRow(ResultSet rs, int rowNum) throws SQLException {
        Vehicle vehicle = new Vehicle(
                rs.getString("registrationId"),
                rs.getString("ownerId"),
                rs.getString("parkingId"),
                rs.getString("model"),
                rs.getString("wheelsType")
        );
        return vehicle;
    }
}
