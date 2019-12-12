package com.housingsociety.apartmentservice.controller;

import com.housingsociety.apartmentservice.dao.ApartmentDAO;
import com.housingsociety.apartmentservice.model.Apartment;
import com.housingsociety.apartmentservice.model.ApartmentDetails;
import com.housingsociety.apartmentservice.model.Member;
import com.housingsociety.apartmentservice.model.ParkingDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/apartmentdetails")
public class ApartmentDetailsController {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    private String memberServiceURL;
    private String parkingServiceURL;
    private ApartmentDAO apartmentDAO;

    private static final Logger LOG = LoggerFactory.getLogger(ApartmentDetailsController.class);

    @PostConstruct
    public void init() {
        memberServiceURL = "http://" + env.getProperty("member-service", "member-service") + ":8080/";
        parkingServiceURL = "http://" + env.getProperty("parking-service", "parking-service") + ":8080/";
        apartmentDAO = new ApartmentDAO();
    }

    @GetMapping("/apartmentId/{apartmentId}")
    public ApartmentDetails getApartmentDetailsById(@PathVariable("apartmentId") String apartmentId)
    {
        Apartment apartment = apartmentDAO.getApartmentById(apartmentId);
        return constructApartmentDetails(apartment);
    }

    @GetMapping("/ownerId/{ownerId}")
    public List<ApartmentDetails> getApartmentDetailsByOwnerId(@PathVariable("ownerId") String ownerId)
    {
        List<Apartment> apartments = apartmentDAO.getApartmentsByOwnerId(ownerId);
        List<ApartmentDetails> apartmentDetailsList = new ArrayList<ApartmentDetails>();
        for (Apartment apartment : apartments){
            apartmentDetailsList.add(constructApartmentDetails(apartment));
        }
        return apartmentDetailsList;
    }

    private ApartmentDetails constructApartmentDetails(Apartment apartment)
    {
        LOG.info("Invoking parking-service");
        ParkingDetails parkingDetails = restTemplate.getForObject(parkingServiceURL + "parkingdetails/parkingId/" + apartment.getParkingId(),  ParkingDetails.class);
        LOG.info("Invoking member-service");
        List<Member> memberList = new ArrayList<Member>();
        Member owner = null;
        for (String memberId : apartment.getMemberIds().split(",")) {
            Member member = restTemplate.getForObject(memberServiceURL + "members/memberId/" + memberId, Member.class);
            memberList.add(member);
            if (memberId.equals(member.getMemberId()))
                owner = member;
        }
        ApartmentDetails apartmentDetails = new ApartmentDetails(apartment.getApartmentId(), apartment.getApartmentType(), owner, parkingDetails, apartment.getMembersCnt(), memberList);
        return apartmentDetails;
    }
}
