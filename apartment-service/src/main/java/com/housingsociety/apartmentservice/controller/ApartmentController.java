package com.housingsociety.apartmentservice.controller;

import com.housingsociety.apartmentservice.dao.ApartmentDAO;
import com.housingsociety.apartmentservice.model.Apartment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/apartments")
public class ApartmentController {
    @Autowired
    private RestTemplate restTemplate;

    private ApartmentDAO apartmentDAO;

    @PostConstruct
    public void init() {
        apartmentDAO = new ApartmentDAO();
    }

    @GetMapping("/healthcheck")
    public String healthcheck()
    {
        return "success";
    }

    @GetMapping("/")
    public List<Apartment> getApartmentDetails()
    {
        return apartmentDAO.getApartmentDetails();
    }

    @GetMapping("/apartmentId/{apartmentId}")
    public Apartment getApartmentDetailsById(@PathVariable("apartmentId") String apartmentId)
    {
        return apartmentDAO.getApartmentById(apartmentId);
    }

    @GetMapping("/ownerId/{ownerId}")
    public List<Apartment> getApartmentDetails(@PathVariable("ownerId") String ownerId)
    {
        return apartmentDAO.getApartmentsByOwnerId(ownerId);
    }

}
