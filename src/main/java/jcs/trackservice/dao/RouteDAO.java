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

import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import jcs.entities.RouteBean;

/**
 *
 * @author frans
 */
public class RouteDAO extends AbstractDAO<RouteBean> {

    private static final String INS_DW_STMT = "insert into routes (FROMTILEID,TOTILEID,COLOR,ID) values(?,?,?,?)";
    private static final String UPD_DW_STMT = "update routes set FROMTILEID = ?,TOTILEID = ?,COLOR = ? where ID = ?";

    public RouteDAO() {
        super();
    }

    @Override
    protected RouteBean map(ResultSet rs) throws SQLException {
        String fromTileId = rs.getString("FROMTILEID");
        String toTileId = rs.getString("TOTILEID");
        String color = rs.getString("COLOR");
        String id = rs.getString("ID");

        RouteBean r = new RouteBean(id, fromTileId, toTileId, color);
        return r;
    }

    @Override
    protected void bind(PreparedStatement ps, RouteBean route, boolean insert) throws SQLException {
        ps.setString(1, route.getFromId());
        ps.setString(2, route.getToId());

        Color c = route.getColor();

        if (c != null) {
            String rgb = "RGB:" + c.getRGB();
            ps.setString(3, rgb);
        } else {
            ps.setNull(3, Types.VARCHAR);
        }
        ps.setString(4, route.getId());
    }

    @Override
    public List<RouteBean> findAll() {
        String stmt = "select * from routes order by id asc";

        return this.findAll(stmt);
    }

    public RouteBean findById(String key) {
        String stmt = "select * from routes where id = ?";
        return this.findById(key, stmt);
    }

    @Override
    public String persist(RouteBean route) {
        RouteBean dw = this.findById(route.getId());

        String statement;
        if (dw == null) {
            statement = INS_DW_STMT;
        } else {
            statement = UPD_DW_STMT;
        }

        upsert(route, statement,false);

        return route.getId();
    }

    @Override
    public void remove(RouteBean route) {
        String stmt = "delete from routes where id = ?";
        this.remove(route, stmt);
    }
}
