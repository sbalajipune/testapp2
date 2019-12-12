package com.housingsociety.vehicleservice.dao;

import com.housingsociety.vehicleservice.model.Vehicle;
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
public class VehicleDAO {
    private static final Logger LOG = LoggerFactory.getLogger(VehicleDAO.class);
    private static final String DATA_FILENAME = "vehicles.json";

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
                itemCount = jdbc.queryForObject("select count(*) from vehicle", Integer.class);

                if (itemCount == 0) {
                    loadVehicles();
                }
            } catch (DataAccessException e) {
                LOG.warn(e.getMessage());
            }
        }
    }

    private void createTables() {
        try {
            LOG.info("Creating table -> vehicle");
            // create table
            jdbc.execute("CREATE TABLE vehicle (" +
                    "id SERIAL NOT NULL, " +
                    "registrationId character varying(15) NOT NULL, " +
                    "ownerId character varying(15), " +
                    "parkingId character varying(10), " +
                    "model character varying(20)," +
                    "wheelsType character varying(15), " +
                    "CONSTRAINT vehicleId_pk PRIMARY KEY (registrationId));");
            LOG.info("Table created successfully!");
        } catch (Exception ex) {
            LOG.info("Failed to create vehicle table, it might already exist!");
            LOG.info(ex.getMessage());
        }
    }

    public void loadVehicles() {
        LOG.info("Importing vehicles data");

        try {
            // import json data
            String json = getJsonContent(DATA_FILENAME);
            JsonParser parser = JsonParserFactory.getJsonParser();
            List<Object> jsonObjects = parser.parseList(json);
            List<Vehicle> vehicles = new LinkedList<Vehicle>();
            for (Object o : jsonObjects) {
                Map<String, String> map = (Map<String, String>) o;

                String registrationId = map.get("registrationId");
                String ownerId = map.get("ownerId");
                String parkingId = map.get("parkingId");
                String model = map.get("model");
                String wheelsType = map.get("wheelsType");
                LOG.info("registrationId = " + registrationId + " ownerId = " + ownerId + " parkingId " + parkingId + " model = " + model + " wheelsType = " + wheelsType);
                vehicles.add(new Vehicle(registrationId, ownerId, parkingId, model, wheelsType));
            }

            jdbc.batchUpdate("insert into vehicle (registrationId, ownerId, parkingId, model, wheelsType) values (?,?,?,?,?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Vehicle vehicle = vehicles.get(i);
                            ps.setString(1, vehicle.getRegistrationId());
                            ps.setString(2, vehicle.getOwnerId());
                            ps.setString(3, vehicle.getParkingId());
                            ps.setString(4, vehicle.getModel());
                            ps.setString(5, vehicle.getWheelsType());
                        }

                        @Override
                        public int getBatchSize() {
                            return vehicles.size();
                        }
                    });
            LOG.info("{} vehicles data imported", vehicles.size());

        } catch (Exception ex) {
            LOG.error("Failed to parse vehicles data from {}", CLASSPATH_URL_PREFIX + DATA_FILENAME);
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

    public List<Vehicle> getVehicles() {
        return jdbc.query(
                "select registrationId, ownerId, parkingId, model, wheelsType from vehicle ORDER by registrationId DESC LIMIT 100",
                (rs, i) -> new Vehicle(rs.getString("registrationId"),
                        rs.getString("ownerId"),
                        rs.getString("parkingId"),
                        rs.getString("model"),
                        rs.getString("wheelsType")));
    }

    public List<Vehicle> getVehiclesByParkingId(String parkingId) {
        return jdbc.query(
                "select registrationId, ownerId, parkingId, model, wheelsType from vehicle where parkingId LIKE '" + parkingId + "%' ORDER by registrationId DESC LIMIT 100",
                (rs, i) -> new Vehicle(rs.getString("registrationId"),
                        rs.getString("ownerId"),
                        rs.getString("parkingId"),
                        rs.getString("model"),
                        rs.getString("wheelsType")));
    }

    public List<Vehicle> getVehiclesByOwnerId(String ownerId) {
        return jdbc.query(
                "select registrationId, ownerId, parkingId, model, wheelsType from vehicle where ownerId LIKE '" + ownerId + "%' ORDER by registrationId DESC LIMIT 100",
                (rs, i) -> new Vehicle(rs.getString("registrationId"),
                        rs.getString("ownerId"),
                        rs.getString("parkingId"),
                        rs.getString("model"),
                        rs.getString("wheelsType")));
    }

    public Vehicle getVehicleByRegistrationId(String registrationId) {
        String sql = "select registrationId, ownerId, parkingId, model, wheelsType from vehicle where registrationId = ?";
        return (Vehicle) jdbc.queryForObject(sql, new VehicleRowMapper(), registrationId);
    }

}
