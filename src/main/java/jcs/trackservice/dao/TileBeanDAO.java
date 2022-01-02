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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import jcs.entities.TileBean;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.SignalType;
import jcs.entities.enums.TileType;
import static jcs.trackservice.dao.AbstractDAO.connection;
import jcs.ui.layout.tiles.enums.Direction;
import org.tinylog.Logger;

/**
 * DAO retrieve and store Tile properties
 *
 * @author frans
 */
public class TileBeanDAO extends AbstractDAO<TileBean> {

    private static final String INS_TILE_STMT = "insert into tiles(tileType,orientation,direction,x,y,signalType,id) values(?,?,?,?,?,?,?)";
    private static final String UPD_TILE_STMT = "update tiles set tileType = ?,orientation = ?,direction = ?,x = ?,y = ?, signalType =? where id = ?";

    public TileBeanDAO() {
        super();
    }

    @Override
    protected TileBean map(ResultSet rs) throws SQLException {
        String id = rs.getString("ID");
        String tt = rs.getString("TILETYPE");
        TileType tileType = TileType.get(tt);
        String or = rs.getString("ORIENTATION");
        Orientation orientation = Orientation.get(or);
        String d = rs.getString("DIRECTION");
        Direction direction = Direction.get(d);
        Integer x = rs.getInt("X");
        Integer y = rs.getInt("Y");
        String st = rs.getString("SIGNALTYPE");
        SignalType signalType = SignalType.get(st);

        TileBean tb = new TileBean(tileType, orientation, direction, x, y, id, signalType);
        return tb;
    }

    @Override
    protected void bind(PreparedStatement ps, TileBean tileBean) throws SQLException {
        ps.setString(1, tileBean.getTileType().getTileType());
        ps.setString(2, tileBean.getOrientation().getOrientation());
        ps.setString(3, tileBean.getDirection().getDirection());
        ps.setInt(4, tileBean.getX());
        ps.setInt(5, tileBean.getY());
        if (tileBean.getSignalType() != null) {
            ps.setString(6, tileBean.getSignalType().getSignalType());
        } else {
            ps.setNull(6, Types.VARCHAR);
        }

        ps.setString(7, tileBean.getId());
    }

    @Override
    public List<TileBean> findAll() {
        String stmt = "select * from tiles order by x,y";

        return this.findAll(stmt);
    }

    //@Override
    public TileBean findById(Object id) {
        String stmt = "select * from tiles where id = ?";

        return this.findById(id, stmt);
    }

    public List<TileBean> findByTileType(String tileType) {
        String stmt = "select * from tiles where tiletype = ? order by x,y";

        return this.findBy(tileType, stmt);
    }

    public TileBean findByXY(Integer x, Integer y) {
        String stmt = "select * from tiles where x = ? and y = ?";

        TileBean layoutTile = null;

        try ( PreparedStatement preparedStatement = connection.prepareStatement(stmt)) {
            preparedStatement.setInt(1, x);
            preparedStatement.setInt(2, y);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                layoutTile = map(rs);
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

        return layoutTile;
    }

    @Override
    public Object persist(TileBean tileBean) {
        TileBean tb = null;
        if (tileBean != null && tileBean.getId() != null) {
            tb = this.findById(tileBean.getId());
        } else if (tileBean != null && tileBean.getX() != 0 && tileBean.getY() != 0) {
            tb = findByXY(tileBean.getX(), tileBean.getY());
        }

        String statement;
        if (tb == null) {
            statement = INS_TILE_STMT;
        } else {
            statement = UPD_TILE_STMT;
        }

        upsert(tileBean, statement);

        if (tileBean != null && tileBean.getId() != null) {
            return tileBean.getId();
        } else {
            return null;
        }
    }

    @Override
    public void remove(TileBean tileBean) {
        String stmt = "delete from tiles where id = ?";

        if (tileBean.getId() == null) {
            TileBean tb = this.findByXY(tileBean.getX(), tileBean.getY());
            this.remove(tb, stmt);
        } else {
            this.remove(tileBean, stmt);
        }
    }

    public void remove(Integer x, Integer y) {
        TileBean tileBean = this.findByXY(x, y);

        if (tileBean != null) {
            this.remove(tileBean);
        }
    }

}
