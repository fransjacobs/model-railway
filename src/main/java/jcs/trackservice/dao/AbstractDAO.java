/*
 * Copyright (C) 2019 frans.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.trackservice.dao;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import jcs.entities.ControllableDevice;
import jcs.entities.JCSEntity;
import jcs.trackservice.dao.util.DatabaseCreator;
import org.tinylog.Logger;

/**
 *
 * @author frans
 * @param <T> the ControllableDevice theDAO implements
 */
public abstract class AbstractDAO<T extends JCSEntity> {

    protected static final String JCS = "jcs";

    protected static Connection connection;

    protected AbstractDAO() {
        if (connection == null) {
            connection = connect();
        }
    }

    protected static String getJCSPath() {
        String path = System.getProperty("user.home") + File.separator + JCS;
        return path;
    }

    public static Connection connect() {
        String jdbcURL = null;
        Connection conn = null;
        try {
            if (!databaseFileExists()) {
                Logger.info("Database does not exist. Creating a new one...");

                String path = getJCSPath();
                File jcsPath = new File(path);
                if (jcsPath.mkdir()) {
                    Logger.info("Created new directory " + jcsPath);
                }
                DatabaseCreator.create();
            }

            String dbPath = System.getProperty("db.path");
            String dbName = System.getProperty("db.name");
            String dbMode = System.getProperty("db.mode");
            String dbUser = System.getProperty("db.user");
            String dbPass = System.getProperty("db.pass");
            String dbSchema = System.getProperty("db.schema");
            String jdbc = System.getProperty("db.jdbc.url");

            jdbcURL = jdbc + dbPath + dbName + (dbMode.equals("") ? "" : ";") + dbMode;

            conn = DriverManager.getConnection(jdbcURL, dbUser, dbPass);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("set SCHEMA " + dbSchema);

            String cat = conn.getCatalog();

            Logger.trace("Connected to " + cat);
        } catch (SQLException sqle) {
            Logger.error("Can't connect to: " + jdbcURL);
            Logger.error(sqle);
        }
        return conn;
    }

    public static boolean databaseFileExists() {
        String path = getJCSPath();
        File jcsPath = new File(path);
        String filename = System.getProperty("db.name", "jcs-db") + ".mv.db";
        File jcsDb = new File(path + File.separator + filename);

        boolean exists = jcsPath.exists() && jcsPath.isDirectory() && jcsDb.exists();
        return exists;
    }

    @Deprecated
    protected BigDecimal getNextId(String sequenceName) {
        String stmt = "select " + sequenceName + ".nextval as ID from dual";

        BigDecimal id = null;

        try ( PreparedStatement preparedStatement = connection.prepareStatement(stmt)) {
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                id = rs.getBigDecimal("ID");
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

        return id;
    }

    @Deprecated
    protected static String getSequenceName(ControllableDevice cd) {
        switch (cd.getClass().getSimpleName()) {
            //case "Locomotive":
            //    return "loco_seq";
            //case "Sensor":
            //    return "sens_seq";
            //case "Switch":
            //    return "soac_seq";
            //case "Signal":
            //    return "soac_seq";
            //case "JCSProperty":
            //    return "prop_seq";
            case "LayoutTile":
                return "lati_seq";
            case "DriveWay":
                return "drwa_seq";
            case "Route":
                return "rout_seq";
            default:
                return null;
        }
    }

    protected BigDecimal upsert(T jcsEntity, String statement) {
        return upsert(jcsEntity, statement, true);
    }

    protected BigDecimal upsert(T jcsEntity, String statement, boolean generatedId) {
        BigDecimal id = null;
        boolean insert = statement.toLowerCase().startsWith("insert");

        try ( PreparedStatement ps = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, jcsEntity, insert);

            int rows = ps.executeUpdate();

            if (insert && generatedId) {
                try ( ResultSet rs = ps.getGeneratedKeys();) {
                    while (rs.next()) {
                        id = rs.getBigDecimal(1);
                        jcsEntity.setId(id);
                    }
                } catch (SQLException ex) {
                    Logger.trace("Can't get the generated ID for " + statement);
                }
            }

            if (rows > 0) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            Logger.error(jcsEntity.getClass().getSimpleName() + " " + statement);
            Logger.error(ex);
        }
        return id;
    }

    protected List<T> findAll(String statement) {
        List<T> devices = new LinkedList<>();

        if (connection != null) {
            try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    T device = map(rs);
                    devices.add(device);
                }
            } catch (SQLException ex) {
                Logger.error(ex);
            }
        }

        return devices;
    }

    protected List<T> findBy(BigDecimal id, String statement) {
        List<T> devices = new LinkedList<>();

        try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setBigDecimal(1, id);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                T device = map(rs);
                devices.add(device);
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

        return devices;
    }

    protected List<T> findBy(String key, String statement) {
        List<T> devices = new LinkedList<>();

        try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, key);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                T device = map(rs);
                devices.add(device);
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

        return devices;
    }

    protected <T> T find(Integer key, String statement) {
        T controllableDevice = null;

        if (connection != null) {
            try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setInt(1, key);

                try ( ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        controllableDevice = map(rs);
                    }
                }
            } catch (SQLException ex) {
                Logger.error(ex);
            }
        }

        return controllableDevice;
    }

    protected <T> T find(Integer key1, Integer key2, String statement) {
        T jcsEntity = null;

        if (connection != null) {
            try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setInt(1, key1);
                preparedStatement.setInt(2, key2);

                try ( ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        jcsEntity = map(rs);
                    }
                }
            } catch (SQLException ex) {
                Logger.error(ex);
            }
        }

        return jcsEntity;
    }

    protected <T> T find(Integer key1, String key2, String statement) {
        T jcsEntity = null;

        if (connection != null) {
            try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setInt(1, key1);
                preparedStatement.setString(2, key2);

                try ( ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        jcsEntity = map(rs);
                    }
                }
            } catch (SQLException ex) {
                Logger.error(ex);
            }
        }

        return jcsEntity;
    }

    protected <T> T find(String key, String statement) {
        T jcsEntity = null;

        if (connection != null) {
            try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, key);

                try ( ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        jcsEntity = map(rs);
                    }
                }
            } catch (SQLException ex) {
                Logger.error(ex);
            }
        }

        return jcsEntity;
    }

    protected <T> T findById(Object id, String statement) {
        T jcsEntity = null;

        try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            if (id instanceof BigDecimal) {
                preparedStatement.setBigDecimal(1, (BigDecimal) id);
            } else {
                preparedStatement.setString(1, (String) id);
            }

            try ( ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    jcsEntity = map(rs);
                }
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

        return jcsEntity;
    }

    protected <T> T findById(BigDecimal otherId, Integer otherInt, String statement) {
        T controllableDevice = null;

        try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setBigDecimal(1, otherId);
            preparedStatement.setInt(2, otherInt);

            try ( ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    controllableDevice = map(rs);
                }
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

        return controllableDevice;
    }

    protected int remove(JCSEntity jcsEntity, String statement) {
        int rows = 0;
        try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            if (jcsEntity != null && jcsEntity.getId() != null) {
                if (jcsEntity.getId() instanceof BigDecimal) {
                    preparedStatement.setBigDecimal(1, (BigDecimal) jcsEntity.getId());
                } else {
                    preparedStatement.setString(1, (String) jcsEntity.getId());
                }

                rows = preparedStatement.executeUpdate();
            }

            if (jcsEntity != null) {
                Logger.trace("Removed " + jcsEntity.getClass().getSimpleName() + " ID: " + jcsEntity.getId() + " Rows: " + rows);
            }

            if (rows > 0) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
        return rows;
    }

    protected int remove(BigDecimal id, Integer otherId, String statement) {
        int rows = 0;
        try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setBigDecimal(1, id);
            preparedStatement.setInt(2, otherId);

            rows = preparedStatement.executeUpdate();

            Logger.trace("Removed entity with id" + id + " and otherId " + otherId + " Rows: " + rows);

            if (rows > 0) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
        return rows;
    }

    protected int remove(BigDecimal id, String statement) {
        int rows = 0;
        try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setBigDecimal(1, id);

            rows = preparedStatement.executeUpdate();

            Logger.trace("Removed entities with id" + id + " Rows: " + rows);

            if (rows > 0) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
        return rows;
    }

    protected int remove(String key, String statement) {
        int rows = 0;
        try ( PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, key);

            rows = preparedStatement.executeUpdate();

            Logger.trace("Removed entities with key" + key + " Rows: " + rows);

            if (rows > 0) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
        return rows;
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException ex) {
            Logger.error(ex.getMessage());
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            Logger.error(ex.getMessage());
        }
    }

    protected abstract void bind(PreparedStatement ps, T jcsEntity, boolean insert) throws SQLException;

    protected abstract <T> T map(ResultSet rs) throws SQLException;

    protected abstract List<T> findAll();

    protected abstract Object persist(T device);

    protected abstract void remove(T device);

}
