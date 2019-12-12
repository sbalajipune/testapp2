package com.housingsociety.vehicleservice.controller;

import com.housingsociety.vehicleservice.dao.VehicleDAO;
import com.housingsociety.vehicleservice.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {
    @Autowired
    private RestTemplate restTemplate;

    private VehicleDAO vehicleDAO;

    @PostConstruct
    public void init() {
        vehicleDAO = new VehicleDAO();
    }

    @GetMapping("/healthcheck")
    public String healthcheck()
    {
        return "success";
    }

    @GetMapping("/")
    public List<Vehicle> getVehicleDetails()
    {
        List<Vehicle> vehicles = vehicleDAO.getVehicles();
        return vehicles;
    }

    @GetMapping("/registrationId/{registrationId}")
    public Vehicle getVehicleDetailsByRegistrationId(@PathVariable("registrationId") String registrationId)
    {
        return vehicleDAO.getVehicleByRegistrationId(registrationId);
    }

    @GetMapping("/parkingId/{parkingId}")
    public List<Vehicle> getVehiclesByParkingId(@PathVariable("parkingId") String parkingId)
    {
        return vehicleDAO.getVehiclesByParkingId(parkingId);
    }

    @GetMapping("/ownerId/{ownerId}")
    public List<Vehicle> getVehiclesByOwnerId(@PathVariable("ownerId") String ownerId)
    {
        return vehicleDAO.getVehiclesByOwnerId(ownerId);
    }

    /*
    private Member getTestMember()
    {
        Member member = new Member("B-1002-1", 'M', "Balaji", "Londhe",35, "Engineer");
        return member;
    }
    */
}
