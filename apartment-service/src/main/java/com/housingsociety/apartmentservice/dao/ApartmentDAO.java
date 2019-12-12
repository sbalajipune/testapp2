package com.housingsociety.apartmentservice.dao;

import com.housingsociety.apartmentservice.model.Apartment;
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
public class ApartmentDAO {
    private static final Logger LOG = LoggerFactory.getLogger(ApartmentDAO.class);
    private static final String DATA_FILENAME = "apartments.json";

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
                itemCount = jdbc.queryForObject("select count(*) from apartment", Integer.class);

                if (itemCount == 0) {
                    loadApartments();
                }
            } catch (DataAccessException e) {
                LOG.warn(e.getMessage());
            }
        }
    }

    private void createTables() {
        try {
            LOG.info("Creating table -> apartment");
            // create table
            jdbc.execute("CREATE TABLE apartment (" +
                    "id SERIAL NOT NULL, " +
                    "apartmentId character varying(15) NOT NULL, " +
                    "apartmentType character varying(15), " +
                    "ownerId character varying(10), " +
                    "parkingId character varying(20)," +
                    "membersCnt int, " +
                    "memberIds character varying(255)," +
                    "CONSTRAINT apartmentId_pk PRIMARY KEY (apartmentId));");
            LOG.info("Table created successfully!");
        } catch (Exception ex) {
            LOG.info("Failed to create apartment table, it might already exist!");
            LOG.info(ex.getMessage());
        }
    }

    public void loadApartments() {
        LOG.info("Importing apartments data");

        try {
            // import json data
            String json = getJsonContent(DATA_FILENAME);
            JsonParser parser = JsonParserFactory.getJsonParser();
            List<Object> jsonObjects = parser.parseList(json);
            List<Apartment> apartments = new LinkedList<Apartment>();
            for (Object o : jsonObjects) {
                Map<String, String> map = (Map<String, String>) o;

                String apartmentId = map.get("apartmentId");
                String apartmentType = map.get("apartmentType");
                String ownerId = map.get("ownerId");
                String parkingId = map.get("parkingId");
                int membersCnt = Integer.parseInt(map.get("membersCnt"));
                String memberIds = map.get("memberIds");
                LOG.info("apartmentId = " + apartmentId + "apartmentType = " + apartmentType +" ownerId = " + ownerId + " parkingId " + parkingId + " membersCnt = " + membersCnt + " memberIds = " + memberIds);
                apartments.add(new Apartment(apartmentId, apartmentType, ownerId, parkingId, membersCnt, memberIds));
            }

            jdbc.batchUpdate("insert into apartment (apartmentId, apartmentType, ownerId, parkingId, membersCnt, memberIds) values (?,?,?,?,?,?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Apartment apartment = apartments.get(i);
                            ps.setString(1, apartment.getApartmentId());
                            ps.setString(2, apartment.getApartmentType());
                            ps.setString(3, apartment.getOwnerId());
                            ps.setString(4, apartment.getParkingId());
                            ps.setInt(5, apartment.getMembersCnt());
                            ps.setString(6, apartment.getMemberIds());
                        }

                        @Override
                        public int getBatchSize() {
                            return apartments.size();
                        }
                    });
            LOG.info("{} apartments data imported", apartments.size());

        } catch (Exception ex) {
            LOG.error("Failed to parse apartments data from {}", CLASSPATH_URL_PREFIX + DATA_FILENAME);
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

    public List<Apartment> getApartmentDetails() {
        return jdbc.query(
                "select apartmentId, apartmentType, ownerId, parkingId, membersCnt, memberIds from apartment ORDER by apartmentId DESC LIMIT 100",
                (rs, i) -> new Apartment(rs.getString("apartmentId"),
                        rs.getString("apartmentType"),
                        rs.getString("ownerId"),
                        rs.getString("parkingId"),
                        rs.getInt("membersCnt"),
                        rs.getString("memberIds")));
    }

    public List<Apartment> getApartmentsByOwnerId(String ownerId) {
        return jdbc.query(
                "select apartmentId, apartmentType, ownerId, parkingId, membersCnt, memberIds from apartment where ownerId LIKE '" + ownerId + "%' ORDER by apartmentId DESC LIMIT 100",
                (rs, i) -> new Apartment(rs.getString("apartmentId"),
                        rs.getString("apartmentType"),
                        rs.getString("ownerId"),
                        rs.getString("parkingId"),
                        rs.getInt("membersCnt"),
                        rs.getString("memberIds")));
    }

    public Apartment getApartmentById(String apartmentId) {
        String sql = "select apartmentId, apartmentType, ownerId, parkingId, membersCnt, memberIds from apartment where apartmentId = ?";
        return (Apartment) jdbc.queryForObject(sql, new ApartmentRowMapper(), apartmentId);
    }

}
