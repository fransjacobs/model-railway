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
import java.util.List;
import lan.wervel.jcs.entities.LayoutTileGroup;

/**
 *
 * @author frans
 */
public class LayoutTileGroupDAO extends AbstractDAO<LayoutTileGroup> {

  private static final String INS_LTG_STMT = "insert into layouttilegroups (NAME,START_LATI_ID,END_LATI_ID,COLOR,DIRECTION,GROUPNUMBER,ID) values(?,?,?,?,?,?,?)";
  private static final String UPD_LTG_STMT = "update layouttilegroups set NAME = ?,START_LATI_ID = ?,END_LATI_ID = ?,COLOR = ?,DIRECTION = ?, GROUPNUMBER = ? where ID = ?";

  public LayoutTileGroupDAO() {
    super();
  }

  @Override
  protected LayoutTileGroup map(ResultSet rs) throws SQLException {
    BigDecimal id = rs.getBigDecimal("ID");
    String name = rs.getString("NAME");
    BigDecimal startLatiId = rs.getBigDecimal("START_LATI_ID");
    BigDecimal endLatiId = rs.getBigDecimal("END_LATI_ID");
    String color = rs.getString("COLOR");
    String direction = rs.getString("DIRECTION");
    Integer groupNumber = rs.getInt("GROUPNUMBER");

    LayoutTileGroup ltg = new LayoutTileGroup(id, name, color, direction, startLatiId, endLatiId, groupNumber);
    return ltg;
  }

  @Override
  protected void bind(PreparedStatement ps, LayoutTileGroup layoutTileGroup) throws SQLException {
    ps.setString(1, layoutTileGroup.getName());
    if (layoutTileGroup.getStartLatiId() != null) {
      ps.setBigDecimal(2, layoutTileGroup.getStartLatiId());
    } else {
      ps.setBigDecimal(2, null);
    }
    if (layoutTileGroup.getEndLatiId() != null) {
      ps.setBigDecimal(3, layoutTileGroup.getEndLatiId());
    } else {
      ps.setBigDecimal(3, null);
    }

    ps.setString(4, layoutTileGroup.getColor());
    ps.setString(5, layoutTileGroup.getDirection());
    ps.setInt(6, layoutTileGroup.getAddress());

    ps.setBigDecimal(7, layoutTileGroup.getId());
  }

  @Override
  public List<LayoutTileGroup> findAll() {
    String stmt = "select * from layouttilegroups order by id";
    return this.findAll(stmt);
  }

  @Override
  public LayoutTileGroup find(Integer groupnumber) {
    if (groupnumber == null) {
      return null;
    }
    String stmt = "select * from layouttilegroups where groupnumber = ? order by id";
    return this.find(groupnumber, stmt);
  }

  public LayoutTileGroup findById(BigDecimal id) {
    if (id == null) {
      return null;
    }
    String stmt = "select * from layouttilegroups where id = ?";
    return this.findById(id, stmt);
  }

  public LayoutTileGroup findByStartLatiId(BigDecimal startLatiId) {
    String stmt = "select * from layouttilegroups where start_lati_id = ?";

    return this.findById(startLatiId, stmt);
  }

  public LayoutTileGroup findByEndLatiId(BigDecimal endLatiId) {
    String stmt = "select * from layouttilegroups where end_lati_id = ?";

    return this.findById(endLatiId, stmt);
  }

  @Override
  @SuppressWarnings("null")
  public BigDecimal persist(LayoutTileGroup layoutTileGroup) {
    LayoutTileGroup lt = null;
    if (layoutTileGroup != null && layoutTileGroup.getId() != null) {
      lt = this.findById(layoutTileGroup.getId());
    }

    String statement;
    if (lt == null) {
      statement = INS_LTG_STMT;
    } else {
      statement = UPD_LTG_STMT;
    }

    upsert(layoutTileGroup, statement);
    return layoutTileGroup.getId();
  }

  @Override
  public void remove(LayoutTileGroup layoutTileGroup) {
    String stmt = "delete from layouttilegroups where id = ?";
    this.remove(layoutTileGroup, stmt);
  }
}
