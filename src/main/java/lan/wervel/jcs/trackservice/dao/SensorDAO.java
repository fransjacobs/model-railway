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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import lan.wervel.jcs.entities.SensorBean;

/**
 *
 * @author frans
 */
public class SensorDAO extends AbstractDAO<SensorBean> {

    private static final String INS_SENS_STMT = "insert into SENSORS (address,device_id,name,description,value,previous_value,millis,lastupdated,id) values(?,?,?,?,?,?,?,?,?)";
    private static final String UPD_SENS_STMT = "update SENSORS set ADDRESS = ?,DEVICE_ID = ?,NAME = ?,DESCRIPTION = ?,VALUE = ?,PREVIOUS_VALUE = ?,MILLIS = ?,LASTUPDATED = ? where ID = ?";

    public SensorDAO() {
        super();
    }

    @Override
    protected SensorBean map(ResultSet rs) throws SQLException {
        BigDecimal id = rs.getBigDecimal("ID");
        Integer contactId = rs.getInt("ADDRESS");
        Integer deviceId = rs.getInt("DEVICE_ID");
        String name = rs.getString("NAME");
        String description = rs.getString("DESCRIPTION");
        Integer value = rs.getInt("VALUE");
        Integer previousValue = rs.getInt("PREVIOUS_VALUE");
        Integer millis = rs.getInt("MILLIS");
        Date lastUpdated = rs.getTimestamp("LASTUPDATED");

        SensorBean sensor = new SensorBean(id, contactId, name, description, value, previousValue, deviceId, millis, lastUpdated);

        return sensor;
    }

    @Override
    protected void bind(PreparedStatement ps, SensorBean sensor) throws SQLException {
        ps.setInt(1, sensor.getAddress());
        ps.setInt(2, sensor.getDeviceId());
        ps.setString(3, sensor.getName());
        ps.setString(4, sensor.getDescription());
        ps.setInt(5, sensor.getValue());
        ps.setInt(6, sensor.getPreviousValue());
        ps.setInt(7, sensor.getMillis());
        if (sensor.getLastUpdated() != null) {
            Timestamp ts = new Timestamp(sensor.getLastUpdated().getTime());
            ps.setTimestamp(8, ts);
        } else {
            ps.setTimestamp(8, null);
        }
        ps.setBigDecimal(9, sensor.getId());
    }

    @Override
    public List<SensorBean> findAll() {
        String stmt = "select * from sensors order by address asc";

        return this.findAll(stmt);
    }

    //@Override
    public SensorBean find(Integer address) {
        String stmt = "select * from sensors where address = ?";

        return this.find(address, stmt);
    }

    public SensorBean findById(BigDecimal id) {
        String stmt = "select * from sensors where id = ?";

        return this.findById(id, stmt);
    }

    @Override
    public BigDecimal persist(SensorBean sensor) {
        SensorBean sens = this.find(sensor.getAddress());

        boolean update = false;
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
