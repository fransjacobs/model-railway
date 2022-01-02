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
import java.sql.Types;
import java.util.List;
import jcs.entities.DriveWay;

/**
 *
 * @author frans
 */
public class DriveWayDAO extends AbstractDAO<DriveWay> {

    private static final String INS_DW_STMT = "insert into driveways (ADDRESS,NAME,DESCRIPTION,FROM_LATI_ID,TO_LATI_ID,LOCO_ID,ACTIVE,RESERVED,OCCUPIED,ID) values(?,?,?,?,?,?,?,?,?,?)";
    private static final String UPD_DW_STMT = "update driveways set ADDRESS = ?,NAME = ?,DESCRIPTION = ?,FROM_LATI_ID = ?,TO_LATI_ID = ?,LOCO_ID = ?,ACTIVE = ?,RESERVED = ?,OCCUPIED = ? where ID = ?";

    public DriveWayDAO() {
        super();
    }

    @Override
    protected DriveWay map(ResultSet rs) throws SQLException {
        Integer address = rs.getInt("ADDRESS");
        String name = rs.getString("NAME");
        String description = rs.getString("DESCRIPTION");
        BigDecimal fromLatiId = rs.getBigDecimal("FROM_LATI_ID");
        BigDecimal toLatiId = rs.getBigDecimal("TO_LATI_ID");
        BigDecimal locoId = rs.getBigDecimal("LOCO_ID");
        boolean active = rs.getInt("ACTIVE") == 1;
        boolean reserved = rs.getInt("RESERVED") == 1;
        boolean occupied = rs.getInt("OCCUPIED") == 1;
        BigDecimal id = rs.getBigDecimal("ID");

        DriveWay dw = new DriveWay(id, address, name, description, fromLatiId, toLatiId, locoId, active, reserved, occupied);
        return dw;
    }

    @Override
    protected void bind(PreparedStatement ps, DriveWay driveWay) throws SQLException {
        ps.setInt(1, driveWay.getAddress());
        ps.setString(2, driveWay.getName());
        ps.setString(3, driveWay.getDescription());
        if (driveWay.getFromLatiId() == null) {
            ps.setNull(4, Types.BIGINT);
        } else {
            ps.setBigDecimal(4, driveWay.getFromLatiId());
        }
        if (driveWay.getToLatiId() == null) {
            ps.setNull(5, Types.BIGINT);
        } else {
            ps.setBigDecimal(5, driveWay.getToLatiId());
        }
        if (driveWay.getLocoId() == null) {
            ps.setNull(6, Types.BIGINT);
        } else {
            ps.setBigDecimal(6, driveWay.getLocoId());
        }
        ps.setInt(7, driveWay.isActive() ? 1 : 0);
        ps.setInt(8, driveWay.isReserved() ? 1 : 0);
        ps.setInt(9, driveWay.isOccupied() ? 1 : 0);

        ps.setBigDecimal(10, driveWay.getId());
    }

    @Override
    public List<DriveWay> findAll() {
        String stmt = "select * from driveways order by address asc";

        return this.findAll(stmt);
    }

    //@Override
    public DriveWay find(Integer address) {
        String stmt = "select * from driveways where address = ?";

        return this.find(address, stmt);
    }

    @Override
    public BigDecimal persist(DriveWay driveWay) {
        DriveWay dw = this.find(driveWay.getAddress());

        String statement;
        if (dw == null) {
            statement = INS_DW_STMT;
        } else {
            statement = UPD_DW_STMT;
        }

        upsert(driveWay, statement);

        return driveWay.getId();
    }

    @Override
    public void remove(DriveWay driveWay) {
        String stmt = "delete from driveways where id = ?";
        this.remove(driveWay, stmt);
    }
}
