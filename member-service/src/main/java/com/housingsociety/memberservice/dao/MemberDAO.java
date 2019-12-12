package com.housingsociety.memberservice.dao;

import com.housingsociety.memberservice.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

@Component
public class MemberDAO {
    private static final Logger LOG = LoggerFactory.getLogger(MemberDAO.class);
    private static final String DATA_FILENAME = "members.json";

    private static JdbcTemplate jdbc;

    @Autowired
    private Environment env;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    protected void init() {
        try {
            String dbName = env.getProperty("POSTGRES_DB", "societydb");
            String dbHost = env.getProperty("POSTGRES_HOST", "postgresql");

            LOG.info("Connecting to database {}:5432/{}", dbHost, dbName);
            DriverManagerDataSource dm = new DriverManagerDataSource();
            dm.setDriverClassName("org.postgresql.Driver");
            dm.setUrl("jdbc:postgresql://" + dbHost + ":5432/" + dbName);
            dm.setUsername(env.getProperty("POSTGRES_USER", "postgres"));
            dm.setPassword(env.getProperty("POSTGRES_PASSWORD", "postgres"));

            jdbc = new JdbcTemplate(dm, false);

            createTables();

        } catch (Exception e) {
            LOG.error("Failed to connect to database", e);
        }

        if (jdbc != null) {
            Integer itemCount = 0;
            try {
                itemCount = jdbc.queryForObject("select count(*) from member", Integer.class);

                if (itemCount == 0) {
                    loadMembers();
                }
            } catch (DataAccessException e) {
                LOG.warn(e.getMessage());
            }
        }
    }

    private void createTables() {
        try {
            LOG.info("Creating table -> member");
            // create table
            jdbc.execute("CREATE TABLE member (" +
                    "id SERIAL NOT NULL, " +
                    "memberId character varying(15) NOT NULL, " +
                    "gender character varying(1), " +
                    "memberFirstName character varying(20), " +
                    "memberLastName character varying(20)," +
                    "age int, " +
                    "profession character varying(256), " +
                    "CONSTRAINT memberId_pk PRIMARY KEY (memberId));");
            LOG.info("Table created successfully!");
        } catch (Exception ex) {
            LOG.info("Failed to create member table, it might already exist!");
            LOG.info(ex.getMessage());
        }
    }

    public void loadMembers() {
        LOG.info("Importing members data");

        try {
            // import json data
            String json = getJsonContent(DATA_FILENAME);
            JsonParser parser = JsonParserFactory.getJsonParser();
            List<Object> jsonObjects = parser.parseList(json);
            List<Member> members = new LinkedList<Member>();
            for (Object o : jsonObjects) {
                Map<String, String> map = (Map<String, String>) o;

                String memberId = map.get("memberId");
                String gender = map.get("gender");
                String memberFirstName = map.get("memberFirstName");
                String memberLastName = map.get("memberLastName");
                int age = Integer.parseInt(map.get("age"));
                String profession = map.get("profession");
                LOG.info("memberId = " + memberId + " gender = " + gender + " memberFirstName " + memberFirstName + " memberLastName = " + memberLastName + " age = " + age + " profession = " + profession);
                members.add(new Member(memberId, gender.charAt(0), memberFirstName, memberLastName, age, profession));
            }

            jdbc.batchUpdate("insert into member (memberId, gender, memberFirstName, memberLastName, age, profession) values (?,?,?,?,?,?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Member member = members.get(i);
                            ps.setString(1, member.getMemberId());
                            ps.setString(2, String.valueOf(member.getGender()));
                            ps.setString(3, member.getMemberFirstName());
                            ps.setString(4, member.getMemberLastName());
                            ps.setInt(5, member.getAge());
                            ps.setString(6, member.getProfession());
                        }

                        @Override
                        public int getBatchSize() {
                            return members.size();
                        }
                    });
            LOG.info("{} members data imported", members.size());

        } catch (Exception ex) {
            LOG.error("Failed to parse members data from {}", CLASSPATH_URL_PREFIX + DATA_FILENAME);
            ex.printStackTrace();
        }
    }

    private String getJsonContent(String fileName) throws IOException {
        StringBuilder json = new StringBuilder("");
        Resource resource = resourceLoader.getResource(CLASSPATH_URL_PREFIX + fileName);
        InputStream is = resource.getInputStream();
        Scanner scanner = new Scanner(is);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            json.append(line).append("\n");
        }

        scanner.close();
        return json.toString();
    }

    public List<Member> getMembers() {
        return jdbc.query(
                "select memberId, gender, memberFirstName, memberLastName, age, profession from member ORDER by memberId DESC LIMIT 100",
                (rs, i) -> new Member(rs.getString("memberId"),
                        rs.getString("gender").charAt(0),
                        rs.getString("memberFirstName"),
                        rs.getString("memberLastName"),
                        rs.getInt("age"),
                        rs.getString("profession")));
    }

    public List<Member> getMembersByApartmentId(String apartmentId) {
        return jdbc.query(
                "select memberId, gender, memberFirstName, memberLastName, age, profession from member where memberId LIKE '" + apartmentId + "%' ORDER by memberId DESC LIMIT 100",
                (rs, i) -> new Member(rs.getString("memberId"),
                        rs.getString("gender").charAt(0),
                        rs.getString("memberFirstName"),
                        rs.getString("memberLastName"),
                        rs.getInt("age"),
                        rs.getString("profession")));
    }

    public Member getMemberById(String memberId) {
        String sql = "select memberId, gender, memberFirstName, memberLastName, age, profession from member where memberId = ?";
        return (Member) jdbc.queryForObject(sql, new MemberRowMapper(), memberId);
    }

    public Member getMemberByName(String memberFirstName, String memberLastName) {
        String sql = "select memberId, gender, memberFirstName, memberLastName, age, profession from member where memberFirstName = ? AND memberLastName = ?";
        return (Member) jdbc.queryForObject(sql, new MemberRowMapper(), memberFirstName, memberLastName);
    }
}
