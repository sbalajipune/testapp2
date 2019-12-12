package com.housingsociety.parkingservice.controller;

import com.housingsociety.parkingservice.dao.ParkingDAO;
import com.housingsociety.parkingservice.model.Parking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/parkings")
public class ParkingController {
    @Autowired
    private RestTemplate restTemplate;

    private ParkingDAO parkingDAO;

    @PostConstruct
    public void init() {
        parkingDAO = new ParkingDAO();
    }

    @GetMapping("/healthcheck")
    public String healthcheck()
    {
        return "success";
    }

    @GetMapping("/")
    public List<Parking> getParkingDetails()
    {
        List<Parking> parkings = parkingDAO.getParkingDetails();
        return parkings;
    }

    @GetMapping("/parkingId/{parkingId}")
    public Parking getParkingDetailsByParkingId(@PathVariable("parkingId") String parkingId)
    {
        return parkingDAO.getParkingDetailsByParkingId(parkingId);
    }

    @GetMapping("/ownerId/{ownerId}")
    public List<Parking> getParkingDetailsByOwnerId(@PathVariable("ownerId") String ownerId)
    {
        return parkingDAO.getParkingDetailsByOwnerId(ownerId);
    }

    @GetMapping("/apartmentId/{apartmentId}")
    public List<Parking> getParkingDetailsByApartmentId(@PathVariable("apartmentId") String apartmentId)
    {
        return parkingDAO.getParkingDetailsByApartmentId(apartmentId);
    }
}
