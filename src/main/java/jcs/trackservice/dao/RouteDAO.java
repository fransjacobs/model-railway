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
import jcs.entities.Route;

/**
 *
 * @author frans
 */
public class RouteDAO extends AbstractDAO<Route> {

    private static final String INS_DW_STMT = "insert into routes (ADDRESS,NAME,DESCRIPTION,DRWA_ID,LATI_ID,ID) values(?,?,?,?,?,?,)";
    private static final String UPD_DW_STMT = "update routes set ADDRESS = ?,NAME = ?,DESCRIPTION = ?,DRWA_ID = ?,LATI_ID = ? where ID = ?";

    public RouteDAO() {
        super();
    }

    @Override
    protected Route map(ResultSet rs) throws SQLException {
        Integer address = rs.getInt("ADDRESS");
        String name = rs.getString("NAME");
        String description = rs.getString("DESCRIPTION");
        BigDecimal drwaId = rs.getBigDecimal("DRWA_ID");
        BigDecimal latiId = rs.getBigDecimal("LATI_ID");
        BigDecimal id = rs.getBigDecimal("ID");

        Route r = null;//new Route(id, address, name, description, drwaId, latiId);
        return r;
    }

    @Override
    protected void bind(PreparedStatement ps, Route route, boolean insert) throws SQLException {
//        ps.setInt(1, route.getAddress());
//        ps.setString(2, route.getName());
//        ps.setString(3, route.getDescription());
//
//        if (route.getDrwaId() == null) {
//            ps.setNull(4, Types.BIGINT);
//        } else {
//            ps.setBigDecimal(4, route.getDrwaId());
//        }
//        if (route.getLatiId() == null) {
//            ps.setNull(5, Types.BIGINT);
//        } else {
//            ps.setBigDecimal(5, route.getLatiId());
//        }
//
//        ps.setBigDecimal(6, route.getId());
    }

    @Override
    public List<Route> findAll() {
        String stmt = "select * from routes order by address asc";

        return this.findAll(stmt);
    }

    //@Override
    public Route find(Integer address) {
        String stmt = "select * from routes where address = ?";

        return this.find(address, stmt);
    }

    public Route findById(BigDecimal id) {
        String stmt = "select * from routes where id = ?";
        return this.findById(id, stmt);
    }

    public List<Route> findByDrwaId(BigDecimal drwaId) {
        String stmt = "select * from routes where drwa_id = ? order by address asc";
        return this.findBy(drwaId, stmt);
    }

    public List<Route> findByLatiId(BigDecimal latiId) {
        String stmt = "select * from routes where lati_id = ? order by address asc";
        return this.findBy(latiId, stmt);
    }

    @Override
    public String persist(Route route) {
        Route dw = null; //this.find(route.getAddress());

        String statement;
        if (dw == null) {
            statement = INS_DW_STMT;
        } else {
            statement = UPD_DW_STMT;
        }

        upsert(route, statement);

        return route.getId();
    }

    @Override
    public void remove(Route route) {
        String stmt = "delete from routes where id = ?";
        this.remove(route, stmt);
    }
}
