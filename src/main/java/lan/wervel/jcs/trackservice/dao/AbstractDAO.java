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
package lan.wervel.jcs.trackservice.dao;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lan.wervel.jcs.entities.ControllableDevice;
import lan.wervel.jcs.trackservice.dao.util.DatabaseCreator;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 * @param <T> the ControllableDevice theDAO implements
 */
public abstract class AbstractDAO<T extends ControllableDevice> {

    protected static Connection connection;

    protected AbstractDAO() {
        if (connection == null) {
            connection = connect();
        }
    }

    public static Connection connect() {
        if (!databaseFileExists()) {
            Logger.info("Database does not exist. Creating a new one...");
            DatabaseCreator.create();
        }

        String dbPath = System.getProperty("db.path", "~/.jcs/");
        String dbName = System.getProperty("db.name", "jcs-db");
        String dbMode = System.getProperty("db.mode", "AUTO_SERVER=TRUE");
        String dbUser = System.getProperty("db.user", "jcs");
        String dbPass = System.getProperty("db.pass", "repo");
        String dbSchema = System.getProperty("db.schema", "JCS");

        String jdbcURL = "jdbc:h2:" + dbPath + dbName + (dbMode.equals("") ? "" : ";") + dbMode;

        Connection conn = null;

        try {
            conn = DriverManager.getConnection(jdbcURL, dbUser, dbPass);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("set SCHEMA " + dbSchema);

            String cat = conn.getCatalog();

            Logger.debug("Connected to " + cat);
        } catch (SQLException sqle) {
            Logger.error("Can't connect to: " + jdbcURL);
            Logger.error(sqle);
        }
        return conn;
    }

    public static boolean databaseFileExists() {
        String path = System.getProperty("user.home") + File.separator + ".jcs";
        File jcsPath = new File(path);
        String filename = System.getProperty("db.name", "jcs-db") + ".mv.db";
        File jcsDb = new File(path + File.separator + filename);

        boolean exists = jcsPath.exists() && jcsPath.isDirectory() && jcsDb.exists();
        return exists;
    }

    protected BigDecimal getNextId(String sequenceName) {
        String stmt = "select " + sequenceName + ".nextval as ID from dual";

        BigDecimal id = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(stmt)) {
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                id = rs.getBigDecimal("ID");
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

        return id;
    }

    protected static String getSequenceName(ControllableDevice cd) {
        switch (cd.getClass().getSimpleName()) {
            case "Locomotive":
                return "loco_seq";
            case "FeedbackModule":
                return "femo_seq";
            case "Turnout":
                return "soac_seq";
            case "Signal":
                return "soac_seq";
            case "JCSProperty":
                return "prop_seq";
            case "LayoutTile":
                return "lati_seq";
            case "LayoutTileGroup":
                return "ltgr_seq";
            default:
                return null;
        }
    }

    protected void upsert(T controllableDevice, String statement) {
        String oper;

        if (controllableDevice.getId() == null) {
            String sequenceName = getSequenceName(controllableDevice);
            if (sequenceName != null) {
                controllableDevice.setId(getNextId(sequenceName));
            }
            oper = "Insert";
        } else {
            oper = "Update";
        }

        try (PreparedStatement ps = connection.prepareStatement(statement)) {
            bind(ps, controllableDevice);

            int rows = ps.executeUpdate();
            Logger.trace("Executed " + oper + " on: " + controllableDevice.getClass().getSimpleName() + " with Address: " + controllableDevice.getAddress() + " and ID: " + controllableDevice.getId() + " Rows: " + rows);

            if (rows > 0) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    protected List<T> findAll(String statement) {
        List<T> devices = new LinkedList<>();

        if (connection != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
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
        List<T> devices = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
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

    protected <T> T find(Integer address, String statement) {
        T controllableDevice = null;

        if (connection != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setInt(1, address);

                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    controllableDevice = map(rs);
                }
            } catch (SQLException ex) {
                Logger.error(ex);
            }
        }

        return controllableDevice;
    }

    protected <T> T find(Integer address, String key, String statement) {
        T controllableDevice = null;

        if (connection != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setInt(1, address);
                preparedStatement.setString(2, key);

                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    controllableDevice = map(rs);
                }
            } catch (SQLException ex) {
                Logger.error(ex);
            }
        }

        return controllableDevice;
    }

    protected <T> T find(String key, String statement) {
        T controllableDevice = null;

        if (connection != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, key);

                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    controllableDevice = map(rs);
                }
            } catch (SQLException ex) {
                Logger.error(ex);
            }
        }

        return controllableDevice;
    }

    protected <T> T findById(BigDecimal id, String statement) {
        T controllableDevice = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setBigDecimal(1, id);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                controllableDevice = map(rs);
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

        return controllableDevice;
    }

    protected <T> T findById(BigDecimal otherId, Integer otherInt, String statement) {
        T controllableDevice = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setBigDecimal(1, otherId);
            preparedStatement.setInt(2, otherInt);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                controllableDevice = map(rs);
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

        return controllableDevice;
    }

    protected void remove(ControllableDevice device, String statement) {
        remove(device, statement, false);
    }

    protected int remove(String statement, BigDecimal otherId) {
        return remove(null, statement, false, otherId);
    }

    protected void remove(ControllableDevice device, String statement, boolean useKey) {
        remove(device, statement, useKey, null);
    }

    protected int remove(ControllableDevice device, String statement, boolean useKey, BigDecimal otherId) {
        int rows = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            if (useKey) {
                preparedStatement.setString(1, device.getKey());
            } else {
                if (otherId != null || device == null) {
                    preparedStatement.setBigDecimal(1, otherId);
                } else {
                    preparedStatement.setBigDecimal(1, device.getId());
                }
            }
            rows = preparedStatement.executeUpdate();

            if (device != null) {
                Logger.trace("Removed " + device.getClass().getSimpleName() + " with " + device.getAddress() + " and ID: " + device.getId() + " Rows: " + rows);
            } else {
                Logger.trace("Removed " + rows + " with otherId: " + otherId + " and statement: " + statement);
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

    protected abstract void bind(PreparedStatement ps, T controllableDevice) throws SQLException;

    protected abstract <T> T map(ResultSet rs) throws SQLException;

    protected abstract List<T> findAll();

    protected abstract <T> T find(Integer address);

    protected abstract BigDecimal persist(T device);

    protected abstract void remove(T device);

}
