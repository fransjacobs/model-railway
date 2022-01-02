/*
 * Copyright (C) 2019 Frans Jacobs.
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
import jcs.entities.SwitchBean;
import jcs.entities.enums.AccessoryValue;

/**
 *
 * @author frans
 */
public class SwitchDAO extends AbstractDAO<SwitchBean> {

    private static final String INS_SA_STMT = "insert into solenoidaccessories (ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME,ID) values(?,?,?,?,?,?,?,?,?,?)";
    private static final String UPD_SA_STMT = "update solenoidaccessories set ADDRESS = ?,NAME = ?,DESCRIPTION = ?,CATALOG_NUMBER = ?,ACCESSORY_TYPE = ?,CURRENT_STATUS_TYPE = ?, SOAC_ID = ?, LIGHT_IMAGES = ?, SWITCH_TIME = ? where ID = ?";

    public SwitchDAO() {
        super();
    }

    @Override
    protected SwitchBean map(ResultSet rs) throws SQLException {
        BigDecimal id = rs.getBigDecimal("ID");
        Integer address = rs.getInt("ADDRESS");
        String name = rs.getString("NAME");
        String description = rs.getString("DESCRIPTION");
        String catalogNumber = rs.getString("CATALOG_NUMBER");
        String currentValue = rs.getString("CURRENT_STATUS_TYPE");
        Integer switchTime = rs.getInt("SWITCH_TIME");

        AccessoryValue value;
        if (currentValue != null) {
            value = AccessoryValue.dbGet(currentValue);
        } else {
            value = AccessoryValue.OFF;
        }

        SwitchBean sa = new SwitchBean(address, description, catalogNumber, id, value);
        sa.setName(name);
        sa.setSwitchTime(switchTime);

        return sa;
    }

    @Override
    protected void bind(PreparedStatement ps, SwitchBean turnout) throws SQLException {
        ps.setInt(1, turnout.getAddress());
        ps.setString(2, turnout.getName());
        ps.setString(3, turnout.getDescription());
        ps.setString(4, turnout.getCatalogNumber());
        ps.setString(5, "T");

        if (turnout.getValue() == null) {
            ps.setString(6, AccessoryValue.OFF.getDBValue());
        } else {
            ps.setString(6, turnout.getValue().getDBValue());
        }

        if (turnout.getSoacId() != null) {
            ps.setBigDecimal(7, turnout.getSoacId());
        } else {
            ps.setNull(7, Types.DECIMAL);
        }
        ps.setInt(8, 2);

        if(turnout.getSwitchTime() != null) {
          ps.setInt(9, turnout.getSwitchTime());
        } else {
            ps.setNull(9, Types.INTEGER);
        }  

        ps.setBigDecimal(10, turnout.getId());
    }

    @Override
    public List<SwitchBean> findAll() {
        String stmt = "select * from solenoidaccessories where accessory_type = 'T' order by address asc";

        return this.findAll(stmt);
    }

    //@Override
    public SwitchBean find(Integer address
    ) {
        String stmt = "select * from solenoidaccessories where accessory_type = 'T' and address = ?";

        return this.find(address, stmt);
    }

    public SwitchBean findById(BigDecimal id) {
        String stmt = "select * from solenoidaccessories where id = ?";

        return this.findById(id, stmt);
    }

    @Override
    public BigDecimal persist(SwitchBean accessoiry) {
        //Check whether the accessoiry exists..
        SwitchBean sa = this.find(accessoiry.getAddress());

        String statement;
        if (sa == null) {
            statement = INS_SA_STMT;
        } else {
            statement = UPD_SA_STMT;
        }

        upsert(accessoiry, statement);

        return accessoiry.getId();
    }

    @Override
    public void remove(SwitchBean accessoiry) {
        String stmt = "delete from solenoidaccessories where id = ?";
        this.remove(accessoiry, stmt);
    }
}
