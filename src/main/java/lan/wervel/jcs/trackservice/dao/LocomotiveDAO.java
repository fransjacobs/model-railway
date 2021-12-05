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
import java.sql.Types;
import java.util.List;
import lan.wervel.jcs.entities.enums.Direction;
import lan.wervel.jcs.entities.Locomotive;
import lan.wervel.jcs.entities.enums.DecoderType;

/**
 *
 * @author frans
 */
public class LocomotiveDAO extends AbstractDAO<Locomotive> {

  //private static final String INS_LOC_STMT = "insert into LOCOMOTIVES (ADDRESS,TYPE,NAME,DESCRIPTION,CATALOGNUMBER,SPECIALFUNCTIONS,DEFAULT_DIRECTION,DIRECTION,SPEED,THROTTLE,MINSPEED,SPEEDSTEPS,LOCOTYPE,F0,F1,F2,F3,F4,F0TYPE,F1TYPE,F2TYPE,F3TYPE,F4TYPE,ID) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  //private static final String UPD_LOC_STMT = "update LOCOMOTIVES set ADDRESS = ?,TYPE = ?,NAME = ?,DESCRIPTION = ?,CATALOGNUMBER = ?,SPECIALFUNCTIONS = ?,DEFAULT_DIRECTION = ?,DIRECTION = ?,SPEED = ?,THROTTLE = ?,MINSPEED = ?,SPEEDSTEPS = ?,LOCOTYPE = ?,F0 = ?,F1 = ?,F2 = ?,F3 = ?,F4 = ?,F0TYPE = ?,F1TYPE = ?,F2TYPE = ?,F3TYPE = ?,F4TYPE = ? where ID = ?";
  private static final String INS_LOC_STMT = "insert into LOCOMOTIVES (ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME,ID) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String UPD_LOC_STMT = "update LOCOMOTIVES set ADDRESS = ?,NAME = ?,DESCRIPTION = ?,CATALOGNUMBER = ?,DECODERTYPE = ?,DIRECTION = ?,SPEED = ?,TACHOMAX = ?,SPEEDSTEPS = ?,VMIN = ?,VMAX = ?,FUNCTIONCOUNT = ?,FUNCTIONVALUES = ?,FUNCTIONTYPES = ?,DEFAULT_DIRECTION = ?,ICONNAME = ? where ID = ?";

  public LocomotiveDAO() {
    super();
  }

  @Override
  protected Locomotive map(ResultSet rs) throws SQLException {
    Integer address = rs.getInt("ADDRESS");
    String name = rs.getString("NAME");
    String description = rs.getString("DESCRIPTION");
    String catalogNumber = rs.getString("CATALOGNUMBER");
    String decoderType = rs.getString("DECODERTYPE");
    String direction = rs.getString("DIRECTION");
    Integer speed = rs.getInt("SPEED");
    Integer tachoMax = rs.getInt("TACHOMAX");
    Integer speedSteps = rs.getInt("SPEEDSTEPS");
    Integer vMin = rs.getInt("VMIN");
    Integer vMax = rs.getInt("VMAX");
    Integer functionCount = rs.getInt("FUNCTIONCOUNT");
    String functionValues = rs.getString("FUNCTIONVALUES");
    String functionTypes = rs.getString("FUNCTIONTYPES");
    String defaultDirection = rs.getString("DEFAULT_DIRECTION");
    String iconName = rs.getString("ICONNAME");
    BigDecimal id = rs.getBigDecimal("ID");

    Locomotive loc = new Locomotive(address, name, description, catalogNumber, DecoderType.get(decoderType), Direction.get(direction), speed, speedSteps,
            tachoMax, vMin, vMax, functionCount, functionValues, functionTypes, Direction.get(defaultDirection), iconName, id);

    return loc;
  }

  @Override
  protected void bind(PreparedStatement ps, Locomotive loc) throws SQLException {
    ps.setInt(1, loc.getAddress());
    ps.setString(2, loc.getName());
    ps.setString(3, loc.getDescription());
    ps.setString(4, loc.getCatalogNumber());
    ps.setString(5, loc.getDecoderType().getDecoderType());
    ps.setString(6, loc.getDirection().getDirection());

    if (loc.getSpeed() != null) {
      ps.setInt(7, loc.getSpeed());
    } else {
      ps.setInt(7, 0);
    }

    if (loc.getTachoMax() != null) {
      ps.setInt(8, loc.getTachoMax());
    } else {
      ps.setNull(8, Types.INTEGER);
    }

    ps.setInt(9, loc.getSpeedSteps());

    if (loc.getvMin() != null) {
      ps.setInt(10, loc.getvMin());
    } else {
      ps.setNull(10, Types.INTEGER);
    }

    if (loc.getvMax() != null) {
      ps.setInt(11, loc.getvMax());
    } else {
      ps.setNull(11, Types.INTEGER);
    }

    ps.setInt(12, loc.getFunctionCount());

    StringBuilder sb = new StringBuilder();
    int[] fv = loc.getFunctionValues();
    for (int i = 0; i < loc.getFunctionCount(); i++) {
      sb.append(fv[i]);
    }

    ps.setString(13, sb.toString());

    ps.setString(14, loc.getFunctionTypes());

    ps.setString(15, loc.getDefaultDirection().getDirection());

    ps.setString(16, loc.getIconName());

    ps.setBigDecimal(17, loc.getId());
  }

  @Override
  public List<Locomotive> findAll() {
    String stmt = "select * from locomotives order by address, id asc";

    return this.findAll(stmt);
  }

  //@Override
  public Locomotive find(Integer address) {
    String stmt = "select * from locomotives where address = ?";

    return this.find(address, stmt);
  }

  public Locomotive find(Integer address, DecoderType decoderType) {
    return this.find(address, decoderType.getDecoderType());
  }

  @Override
  public Locomotive find(Integer address, String key) {
    String stmt = "select * from locomotives where address = ? and decodertype = ?";

    return this.find(address, key, stmt);
  }

  public Locomotive findById(BigDecimal id) {
    String stmt = "select * from locomotives where id = ?";

    return this.findById(id, stmt);
  }

  @Override
  public BigDecimal persist(Locomotive locomotive) {
    //Check whether the loc exists..
    Locomotive loc = this.find(locomotive.getAddress(), locomotive.getDecoderType());

    String statement;
    if (loc == null) {
      statement = INS_LOC_STMT;
    } else {
      statement = UPD_LOC_STMT;
    }

    upsert(locomotive, statement);

    return locomotive.getId();
  }

  @Override
  public void remove(Locomotive locomotive) {
    String stmt = "delete from locomotives where id = ?";
    this.remove(locomotive, stmt);
  }
}
