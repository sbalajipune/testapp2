package com.housingsociety.memberservice.dao;

import com.housingsociety.memberservice.model.Member;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberRowMapper implements RowMapper {
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = new Member(
                rs.getString("memberId"),
                rs.getString("gender").charAt(0),
                rs.getString("memberFirstName"),
                rs.getString("memberLastName"),
                rs.getInt("age"),
                rs.getString("profession")
        );
        return member;
    }
}
