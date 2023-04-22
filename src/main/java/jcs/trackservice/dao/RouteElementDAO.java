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
import jcs.entities.RouteElementBean;
import jcs.entities.enums.AccessoryValue;

/**
 *
 * @author frans
 */
public class RouteElementDAO extends AbstractDAO<RouteElementBean> {

    private static final String INS_DW_STMT = "insert into routeelements(ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) values(?,?,?,?,?)";
    private static final String UPD_DW_STMT = "update routeelements set ROUTEID = ?,NODEID = ?,TILEID = ?, ACCESSORYVALUE = ?, ORDER_SEQ = ? where ID = ?";

    public RouteElementDAO() {
        super();
    }

    @Override
    protected RouteElementBean map(ResultSet rs) throws SQLException {
        String routeId = rs.getString("ROUTEID");
        String nodeId = rs.getString("NODEID");
        String tileId = rs.getString("TILEID");
        String accessoryValue = rs.getString("ACCESSORYVALUE");
        Integer elementOrder = rs.getInt("ORDER_SEQ");

        BigDecimal id = rs.getBigDecimal("ID");

        AccessoryValue av = null;
        if (accessoryValue != null) {
            av = AccessoryValue.dbGet(accessoryValue);
        }
        RouteElementBean r = new RouteElementBean(routeId, nodeId, tileId, av, elementOrder, id);
        return r;
    }

    @Override
    protected void bind(PreparedStatement ps, RouteElementBean routeElement, boolean insert) throws SQLException {
        ps.setString(1, routeElement.getRouteId());
        ps.setString(2, routeElement.getNodeId());
        ps.setString(3, routeElement.getTileId());
        if (routeElement.getAccessoryValue() != null) {
            String dbv = routeElement.getAccessoryValue().getDBValue();
            ps.setString(4, dbv);
        } else {
            ps.setNull(4, Types.VARCHAR);
        }
        ps.setInt(5, routeElement.getElementOrder());

        if (routeElement.getId() != null) {
            ps.setBigDecimal(6, routeElement.getId());
        }
    }

    @Override
    public List<RouteElementBean> findAll() {
        String stmt = "select * from routeelements order by id asc";

        return this.findAll(stmt);
    }

    public RouteElementBean findById(BigDecimal id) {
        String stmt = "select * from routeelements where id = ?";
        return this.findById(id, stmt);
    }

    public List<RouteElementBean> findByRouteId(String key) {
        String stmt = "select * from routeelements where routeid = ? order by ORDER_SEQ";
        return this.findBy(key, stmt);
    }

    @Override
    public BigDecimal persist(RouteElementBean routeElement) {
        RouteElementBean dw = this.findById(routeElement.getId());

        String statement;
        if (dw == null) {
            statement = INS_DW_STMT;
        } else {
            statement = UPD_DW_STMT;
        }

        upsert(routeElement, statement, true);

        return routeElement.getId();
    }

    @Override
    public void remove(RouteElementBean routeElement) {
        String stmt = "delete from routeelements where id = ?";
        this.remove(routeElement, stmt);
    }

    public void removeByRouteId(String routeId) {
        String stmt = "delete from routeelements where routeid = ?";
        this.remove(routeId, stmt);
    }
}
