package com.housingsociety.parkingservice.dao;

import com.housingsociety.parkingservice.model.Parking;
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
public class ParkingDAO {
    private static final Logger LOG = LoggerFactory.getLogger(ParkingDAO.class);
    private static final String DATA_FILENAME = "parkings.json";

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
                itemCount = jdbc.queryForObject("select count(*) from parking", Integer.class);

                if (itemCount == 0) {
                    loadParkings();
                }
            } catch (DataAccessException e) {
                LOG.warn(e.getMessage());
            }
        }
    }

    private void createTables() {
        try {
            LOG.info("Creating table -> parking");
            // create table
            jdbc.execute("CREATE TABLE parking (" +
                    "id SERIAL NOT NULL, " +
                    "parkingId character varying(15) NOT NULL, " +
                    "apartmentId character varying(15), " +
                    "ownerId character varying(15), " +
                    "lvl int," +
                    "vehicles character varying(50), " +
                    "CONSTRAINT parkingId_pk PRIMARY KEY (parkingId));");
            LOG.info("Table created successfully!");
        } catch (Exception ex) {
            LOG.info("Failed to create parking table, it might already exist!");
            LOG.info(ex.getMessage());
        }
    }

    public void loadParkings() {
        LOG.info("Importing parkings data");

        try {
            // import json data
            String json = getJsonContent(DATA_FILENAME);
            JsonParser parser = JsonParserFactory.getJsonParser();
            List<Object> jsonObjects = parser.parseList(json);
            List<Parking> parkings = new LinkedList<Parking>();
            for (Object o : jsonObjects) {
                Map<String, String> map = (Map<String, String>) o;

                String parkingId = map.get("parkingId");
                String apartmentId = map.get("apartmentId");
                String ownerId = map.get("ownerId");
                int level = Integer.parseInt(map.get("level"));
                String vehicles = map.get("vehicles");
                LOG.info("parkingId = " + parkingId + " apartmentId = " + apartmentId + " level " + level + " vehicles = " + vehicles);
                parkings.add(new Parking(parkingId, apartmentId, ownerId, level, vehicles));
            }

            jdbc.batchUpdate("insert into parking (parkingId, apartmentId, ownerId, lvl, vehicles) values (?,?,?,?,?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Parking parking = parkings.get(i);
                            ps.setString(1, parking.getParkingId());
                            ps.setString(2, parking.getApartmentId());
                            ps.setString(3, parking.getOwnerId());
                            ps.setInt(4, parking.getLevel());
                            ps.setString(5, parking.getVehicles());
                        }

                        @Override
                        public int getBatchSize() {
                            return parkings.size();
                        }
                    });
            LOG.info("{} parkings data imported", parkings.size());

        } catch (Exception ex) {
            LOG.error("Failed to parse parkings data from {}", CLASSPATH_URL_PREFIX + DATA_FILENAME);
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

    public List<Parking> getParkingDetails() {
        return jdbc.query(
                "select parkingId, apartmentId, ownerId, lvl, vehicles from parking ORDER by parkingId DESC LIMIT 100",
                (rs, i) -> new Parking(rs.getString("parkingId"),
                        rs.getString("apartmentId"),
                        rs.getString("ownerId"),
                        rs.getInt("lvl"),
                        rs.getString("vehicles")));
    }

    public List<Parking> getParkingDetailsByApartmentId(String apartmentId) {
        return jdbc.query(
                "select parkingId, apartmentId, ownerId, lvl, vehicles from parking where apartmentId = '" + apartmentId + "' ORDER by parkingId DESC LIMIT 100",
                (rs, i) -> new Parking(rs.getString("parkingId"),
                        rs.getString("apartmentId"),
                        rs.getString("ownerId"),
                        rs.getInt("lvl"),
                        rs.getString("vehicles")));
    }

    public Parking getParkingDetailsByParkingId(String parkingId) {
        String sql = "select parkingId, apartmentId, ownerId, lvl, vehicles from parking where parkingId = ?";
        return (Parking) jdbc.queryForObject(sql, new ParkingRowMapper(), parkingId);
    }

    public List<Parking> getParkingDetailsByOwnerId(String ownerId) {
        return jdbc.query(
                "select parkingId, apartmentId, ownerId, lvl, vehicles from parking where apartmentId = '" + ownerId + "' ORDER by parkingId DESC LIMIT 100",
                (rs, i) -> new Parking(rs.getString("parkingId"),
                        rs.getString("apartmentId"),
                        rs.getString("ownerId"),
                        rs.getInt("lvl"),
                        rs.getString("vehicles")));
    }
}
