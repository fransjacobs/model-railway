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
import jcs.entities.LayoutTile;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import static jcs.trackservice.dao.AbstractDAO.connection;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class LayoutTileDAO extends AbstractDAO<LayoutTile> {

    private static final String INS_LT_STMT = "insert into layouttiles (TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID,ID) values(?,?,?,?,?,?,?,?)";
    private static final String UPD_LT_STMT = "update layouttiles set TILETYPE = ?,ORIENTATION = ?,DIRECTION = ?,X = ?,Y = ?,SOAC_ID = ?,SENS_ID = ? where ID = ?";

    public LayoutTileDAO() {
        super();
    }

    @Override
    protected LayoutTile map(ResultSet rs) throws SQLException {
        BigDecimal id = rs.getBigDecimal("ID");
        String tt = rs.getString("TILETYPE");
        TileType tileType = TileType.get(tt);
        String or = rs.getString("ORIENTATION");
        Orientation orientation = Orientation.get(or);
        String direction = rs.getString("DIRECTION");
        Integer x = rs.getInt("X");
        Integer y = rs.getInt("Y");
        BigDecimal soacId = rs.getBigDecimal("SOAC_ID");
        BigDecimal sensId = rs.getBigDecimal("SENS_ID");

        LayoutTile lt = new LayoutTile(id, tileType, orientation, direction, x, y, soacId, sensId);
        return lt;
    }

    @Override
    protected void bind(PreparedStatement ps, LayoutTile layoutTile) throws SQLException {
        ps.setString(1, layoutTile.getTiletype().getTileType());
        ps.setString(2, layoutTile.getOrientation().getOrientation());
        ps.setString(3, layoutTile.getDirection());
        ps.setInt(4, layoutTile.getX());
        ps.setInt(5, layoutTile.getY());
        if (layoutTile.getSoacId() != null) {
            ps.setBigDecimal(6, layoutTile.getSoacId());
        } else {
            ps.setNull(6, Types.DECIMAL);
        }
        if (layoutTile.getSensId() != null) {
            ps.setBigDecimal(7, layoutTile.getSensId());
        } else {
            ps.setBigDecimal(7, null);
        }

        ps.setBigDecimal(8, layoutTile.getId());
    }

    @Override
    public List<LayoutTile> findAll() {
        String stmt = "select * from layouttiles order by x,y";

        return this.findAll(stmt);
    }

    //@Override
    public LayoutTile find(Integer id) {
        String stmt = "select * from layouttiles where id = ?";

        return this.find(id, stmt);
    }

    public LayoutTile findById(BigDecimal id) {
        String stmt = "select * from layouttiles where id = ?";

        return this.findById(id, stmt);
    }

    public LayoutTile findBySoacId(BigDecimal soacId) {
        String stmt = "select * from layouttiles where soac_id = ?";

        return this.findById(soacId, stmt);
    }

    public List<LayoutTile> findByTileType(String tileType) {
        String stmt = "select * from layouttiles where tiletype = ? order by x,y";

        return this.findBy(tileType, stmt);
    }

    public LayoutTile findByXY(Integer x, Integer y) {
        String stmt = "select * from layouttiles where x = ? and y = ?";

        LayoutTile layoutTile = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(stmt)) {
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

//  public List<LayoutTile> findCandidateBlockStart() {
//    String stmt = "select * from layouttiles lt where lt.tiletype in ('StraightTrack','FeedbackPort') "
//            + "and exists (select 1 from layouttiles ltt where ltt.tiletype = 'TurnoutTile' "
//            + "and ltt.rotation in ('R0','R180') "
//            + "and (lt.x + 60 = ltt.x or lt.x - 60 = ltt.x) and (lt.y + 20 = ltt.y or lt.y - 20 = ltt.y)) "
//            + "union "
//            + "select * from layouttiles lt where lt.tiletype = 'DiagonalTrack' "
//            + "and exists (select 1 from layouttiles ltt where ltt.tiletype = 'TurnoutTile' "
//            + "and ltt.rotation in ('R0','R180') "
//            + "and (lt.x + 60 = ltt.x or lt.x - 60 = ltt.x) and (lt.y + 40 = ltt.y or lt.y - 40 = ltt.y)) "
//            + "union "
//            + "select * from layouttiles lt where lt.tiletype in ('StraightTrack','FeedbackPort') "
//            + "and exists (select 1 from layouttiles ltt where ltt.tiletype = 'TurnoutTile' "
//            + "and ltt.rotation in ('R90','R270') "
//            + "and (lt.x + 20 = ltt.x or lt.x - 20 = ltt.x) and (lt.y + 60 = ltt.y or lt.y - 60 = ltt.y)) "
//            + "union "
//            + "select * from layouttiles lt where lt.tiletype = 'DiagonalTrack' "
//            + "and exists (select 1 from layouttiles ltt where ltt.tiletype = 'TurnoutTile' "
//            + "and ltt.rotation in ('R90','R270') "
//            + "and (lt.x + 40 = ltt.x or lt.x - 40 = ltt.x) and (lt.y + 60 = ltt.y or lt.y - 60 = ltt.y))";
//
//    return this.findAll(stmt);
//  }
    @Override
    public BigDecimal persist(LayoutTile layoutTile) {
        //Check whether the layoutTile exists, based on logical key...
        LayoutTile lt = null;
        if (layoutTile != null && layoutTile.getId() != null) {
            lt = this.findById(layoutTile.getId());
        } else if (layoutTile != null && layoutTile.getX() != null && layoutTile.getY() != null) {
            lt = findByXY(layoutTile.getX(), layoutTile.getY());
        }

        String statement;
        if (lt == null) {
            statement = INS_LT_STMT;
        } else {
            statement = UPD_LT_STMT;
        }

        upsert(layoutTile, statement);

        if (layoutTile != null && layoutTile.getId() != null) {
            return layoutTile.getId();
        } else {
            return null;
        }
    }

    @Override
    public void remove(LayoutTile layoutTile) {
        String stmt = "delete from layouttiles where id = ?";

        if (layoutTile.getId() == null) {
            LayoutTile lt = this.findByXY(layoutTile.getX(), layoutTile.getY());
            this.remove(lt, stmt);
        } else {
            this.remove(layoutTile, stmt);
        }
    }
}
