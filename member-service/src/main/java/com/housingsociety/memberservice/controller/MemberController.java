package com.housingsociety.memberservice.controller;

import com.housingsociety.memberservice.dao.MemberDAO;
import com.housingsociety.memberservice.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {
    @Autowired
    private RestTemplate restTemplate;

    private MemberDAO memberDAO;

    @PostConstruct
    public void init() {
        memberDAO = new MemberDAO();
    }

    @GetMapping("/healthcheck")
    public String healthcheck()
    {
        return "success";
    }

    @GetMapping("/")
    public List<Member> getMemberDetails()
    {
        List<Member> members = memberDAO.getMembers();
        return members;
    }

    @GetMapping("/memberId/{memberId}")
    public Member getMemberDetailsById(@PathVariable("memberId") String memberId)
    {
        return memberDAO.getMemberById(memberId);
    }

    @GetMapping("/member/{memberFirstName}/{memberLastName}")
    public Member getMemberDetailsByName(@PathVariable("memberFirstName") String memberFirstName, @PathVariable("memberLastName") String memberLastName)
    {
        return memberDAO.getMemberByName(memberFirstName, memberLastName);
    }

    @GetMapping("/apartment/{apartmentId}")
    public List<Member> getMemberDetailsByName(@PathVariable("apartmentId") String apartmentId)
    {
        return memberDAO.getMembersByApartmentId(apartmentId);
    }

    /*
    private Member getTestMember()
    {
        Member member = new Member("B-1002-1", 'M', "Balaji", "Londhe",35, "Engineer");
        return member;
    }
    */
}
