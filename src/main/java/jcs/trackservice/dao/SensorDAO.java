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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import jcs.entities.SensorBean;

/**
 *
 * @author frans
 */
public class SensorDAO extends AbstractDAO<SensorBean> {

    //private static final String INS_SENS_STMT = "insert into SENSORS (name,deviceid,contactid,status,previousstatus,millis,lastupdated,id) values(?,?,?,?,?,?,?,?)";
    private static final String INS_SENS_STMT = "insert into SENSORS (name,deviceid,contactid,status,previousstatus,millis,lastupdated) values(?,?,?,?,?,?,?)";
    private static final String UPD_SENS_STMT = "update SENSORS set NAME = ?,DEVICEID = ?,CONTACTID = ?,STATUS = ?,PREVIOUSSTATUS = ?,MILLIS = ?,LASTUPDATED = ? where ID = ?";

    public SensorDAO() {
        super();
    }

    @Override
    protected SensorBean map(ResultSet rs) throws SQLException {
        String name = rs.getString("NAME");
        Integer deviceId = rs.getInt("DEVICEID");
        Integer contactId = rs.getInt("CONTACTID");
        Integer status = rs.getInt("STATUS");
        Integer previousStatus = rs.getInt("PREVIOUSSTATUS");
        Integer millis = rs.getInt("MILLIS");
        Date lastUpdated = rs.getTimestamp("LASTUPDATED");
        BigDecimal id = rs.getBigDecimal("ID");

        SensorBean sensor = new SensorBean(id, name, deviceId, contactId, status, previousStatus, millis, lastUpdated);

        return sensor;
    }

    @Override
    protected void bind(PreparedStatement ps, SensorBean sensor, boolean insert) throws SQLException {
        ps.setString(1, sensor.getName());
        if (sensor.getDeviceId() != null) {
            ps.setInt(2, sensor.getDeviceId());
        } else {
            ps.setNull(2, Types.INTEGER);
        }
        if (sensor.getContactId() != null) {
            ps.setInt(3, sensor.getContactId());
        } else {
            ps.setNull(3, Types.INTEGER);
        }
        if (sensor.getStatus() != null) {
            ps.setInt(4, sensor.getStatus());
        } else {
            ps.setNull(4, Types.INTEGER);
        }
        if (sensor.getPreviousStatus() != null) {
            ps.setInt(5, sensor.getPreviousStatus());
        } else {
            ps.setNull(5, Types.INTEGER);
        }
        if (sensor.getMillis() != null) {
            ps.setInt(6, sensor.getMillis());
        } else {
            ps.setNull(6, Types.INTEGER);
        }
        if (sensor.getLastUpdated() != null) {
            Timestamp ts = new Timestamp(sensor.getLastUpdated().getTime());
            ps.setTimestamp(7, ts);
        } else {
            ps.setNull(7, Types.TIMESTAMP);
        }
        if (!insert) {
            ps.setBigDecimal(8, sensor.getId());
        }
    }

    @Override
    public List<SensorBean> findAll() {
        String stmt = "select * from sensors order by id asc";

        return this.findAll(stmt);
    }

    public SensorBean find(Integer deviceId, Integer contactId) {
        String stmt = "select * from sensors where deviceId = ? and contactId = ?";

        return this.find(deviceId, contactId, stmt);
    }

    public SensorBean findById(BigDecimal id) {
        String stmt = "select * from sensors where id = ?";

        return this.findById(id, stmt);
    }

    @Override
    public BigDecimal persist(SensorBean sensor) {
        SensorBean sens = this.find(sensor.getDeviceId(), sensor.getContactId());
        if (sens == null) {
            sens = this.findById(sensor.getId());
        }

        String statement;
        if (sens == null) {
            statement = INS_SENS_STMT;
        } else {
            statement = UPD_SENS_STMT;
        }
        upsert(sensor, statement);
        return sensor.getId();
    }

    @Override
    public void remove(SensorBean sensor) {
        String stmt = "delete from sensors where id = ?";
        this.remove(sensor, stmt);
    }
}
