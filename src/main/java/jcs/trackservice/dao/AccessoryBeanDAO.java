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
import jcs.entities.AccessoryBean;

/**
 *
 * @author frans
 */
public class AccessoryBeanDAO extends AbstractDAO<AccessoryBean> {

    private static final String INS_SA_STMT = "insert into accessories (address,name,type,position,switchtime,decodertype,decoder,agroup,icon,iconfile) values(?,?,?,?,?,?,?,?,?,?)";
    private static final String UPD_SA_STMT = "update accessories set address = ?,name = ?,type = ?,position = ?,switchtime = ?,decodertype = ?,decoder = ?, agroup = ?,icon = ?,iconfile = ? where id = ?";

    public AccessoryBeanDAO() {
        super();
    }

    @Override
    protected AccessoryBean map(ResultSet rs) throws SQLException {
        Integer address = rs.getInt("ADDRESS");
        String name = rs.getString("NAME");
        String type = rs.getString("TYPE");
        Integer position = rs.getInt("POSITION");
        Integer switchTime = rs.getInt("SWITCHTIME");
        String decoderType = rs.getString("DECODERTYPE");
        String decoder = rs.getString("DECODER");
        String group = rs.getString("AGROUP");
        String icon = rs.getString("ICON");
        String iconFile = rs.getString("ICONFILE");

        BigDecimal id = rs.getBigDecimal("ID");

        AccessoryBean sa = new AccessoryBean(id, address, name, type, position, switchTime, decoderType, decoder, group, icon, iconFile);

        return sa;
    }

    @Override
    protected void bind(PreparedStatement ps, AccessoryBean sab, boolean insert) throws SQLException {
        ps.setInt(1, sab.getAddress());
        ps.setString(2, sab.getName());
        ps.setString(3, sab.getType());

        if (sab.getPosition() != null) {
            ps.setInt(4, sab.getPosition());
        } else {
            ps.setNull(4, Types.INTEGER);
        }

        if (sab.getSwitchTime() != null) {
            ps.setInt(5, sab.getSwitchTime());
        } else {
            ps.setNull(5, Types.INTEGER);
        }

        ps.setString(6, sab.getDecoderType());
        ps.setString(7, sab.getDecoder());

        ps.setString(8, sab.getGroup());
        ps.setString(9, sab.getIcon());
        ps.setString(10, sab.getIconFile());

        if (!insert) {
            ps.setBigDecimal(11, sab.getId());
        }
    }

    @Override
    public List<AccessoryBean> findAll() {
        String stmt = "select * from accessories order by id asc";
        return super.findAll(stmt);
    }

    public AccessoryBean find(Integer address) {
        String stmt = "select * from accessories where address = ?";
        return super.find(address, stmt);
    }

    public List<AccessoryBean> findBy(String key) {
        String stmt = "select * from accessories where type like ?";
        return super.findBy(key, stmt);
    }

    public AccessoryBean findById(BigDecimal id) {
        String stmt = "select * from accessories where id = ?";

        return super.findById(id, stmt);
    }

    @Override
    public BigDecimal persist(AccessoryBean accessoiry) {
        //Check whether the accessoiry exists..
        AccessoryBean sa = this.findById(accessoiry.getId());

        String statement;
        if (sa == null) {
            statement = INS_SA_STMT;
        } else {
            statement = UPD_SA_STMT;
        }

        super.upsert(accessoiry, statement);

        return accessoiry.getId();
    }

    @Override
    public void remove(AccessoryBean accessoiry) {
        String stmt = "delete from accessories where id = ?";
        super.remove(accessoiry, stmt);
    }
}
