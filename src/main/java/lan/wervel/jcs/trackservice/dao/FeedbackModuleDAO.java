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
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import lan.wervel.jcs.entities.FeedbackModule;

/**
 *
 * @author frans
 */
public class FeedbackModuleDAO extends AbstractDAO<FeedbackModule> {

  private static final String INS_FEMO_STMT = "insert into FEEDBACKMODULES (address,name,description,catalognumber,ports,msb,lsb,lastupdated,port1,port2,port3,port4,port5,port6,port7,port8,port9,port10,port11,port12,port13,port14,port15,port16,id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String UPD_FEMO_STMT = "update FEEDBACKMODULES set ADDRESS = ?,NAME = ?,DESCRIPTION = ?,CATALOGNUMBER = ?,PORTS = ?,MSB = ?,LSB = ?,LASTUPDATED = ?,PORT1 = ?,PORT2 = ?,PORT3 = ?,PORT4 = ?,PORT5 = ?,PORT6 = ?,PORT7 = ?,PORT8 = ?,PORT9 = ?,PORT10 = ?,PORT11 = ?,PORT12 = ?,PORT13 = ?,PORT14 = ?,PORT15 = ?,PORT16 = ? where ID = ?";

  public FeedbackModuleDAO() {
    super();
  }

  @Override
  protected FeedbackModule map(ResultSet rs) throws SQLException {
    BigDecimal id = rs.getBigDecimal("ID");
    Integer address = rs.getInt("ADDRESS");
    String name = rs.getString("NAME");
    String description = rs.getString("DESCRIPTION");
    String catalogNumber = rs.getString("CATALOGNUMBER");
    Integer ports = rs.getInt("PORTS");
    Integer msb = rs.getInt("MSB");
    Integer lsb = rs.getInt("LSB");
    Date lastupdated = rs.getTimestamp("LASTUPDATED");

    FeedbackModule femo = new FeedbackModule(address, catalogNumber, ports);
    femo.setName(name);

    femo.setId(id);
    femo.setDescription(description);
    femo.setResponse(new Integer[]{lsb, msb});
    femo.setLastUpdated(lastupdated);
    return femo;
  }

  @Override
  protected void bind(PreparedStatement ps, FeedbackModule femo) throws SQLException {
    ps.setInt(1, femo.getAddress());
    ps.setString(2, femo.getName());
    ps.setString(3, femo.getDescription());
    ps.setString(4, femo.getCatalogNumber());
    ps.setInt(5, femo.getPorts());
    Integer[] pv = femo.getResponse();
    if (pv != null && pv.length == 2) {
      ps.setInt(6, pv[1]);
      ps.setInt(7, pv[0]);
    } else {
      ps.setInt(6, 0);
      ps.setInt(7, 0);
    }
    if (femo.getLastUpdated() != null) {
      Timestamp ts = new Timestamp(femo.getLastUpdated().getTime());
      ps.setTimestamp(8, ts);
    } else {
      ps.setTimestamp(8, null);
    }
    ps.setBoolean(9, femo.isPort1());
    ps.setBoolean(10, femo.isPort2());
    ps.setBoolean(11, femo.isPort3());
    ps.setBoolean(12, femo.isPort4());
    ps.setBoolean(13, femo.isPort5());
    ps.setBoolean(14, femo.isPort6());
    ps.setBoolean(15, femo.isPort7());
    ps.setBoolean(16, femo.isPort8());
    ps.setBoolean(17, femo.isPort9());
    ps.setBoolean(18, femo.isPort10());
    ps.setBoolean(19, femo.isPort11());
    ps.setBoolean(20, femo.isPort12());
    ps.setBoolean(21, femo.isPort13());
    ps.setBoolean(22, femo.isPort14());
    ps.setBoolean(23, femo.isPort15());
    ps.setBoolean(24, femo.isPort16());

    ps.setBigDecimal(25, femo.getId());
  }

  @Override
  public List<FeedbackModule> findAll() {
    String stmt = "select * from feedbackmodules order by address asc";

    return this.findAll(stmt);
  }

  @Override
  public FeedbackModule find(Integer address) {
    String stmt = "select * from feedbackmodules where address = ?";

    return this.find(address, stmt);
  }

  public FeedbackModule findById(BigDecimal id) {
    String stmt = "select * from feedbackmodules where id = ?";

    return this.findById(id, stmt);
  }

  
  @Override
  public BigDecimal persist(FeedbackModule feedbackModule) {
    //Check whether the feedbackModule exists...
    FeedbackModule femo = this.find(feedbackModule.getAddress());

    boolean update = false;
    String statement;
    if (femo == null) {
      statement = INS_FEMO_STMT;
    } else {
      statement = UPD_FEMO_STMT;
      update = true;
    }

    //Logger.debug("old: " + femo);
    //Logger.debug("new: " + feedbackModule);
    synchronized (this) {
      upsert(feedbackModule, statement);
    }

    //Send send the updated status in case of a port change
    if (update && !feedbackModule.equalsPorts(femo)) {
    }
    return feedbackModule.getId();
  }

  @Override
  public void remove(FeedbackModule feedbackModule) {
    String stmt = "delete from feedbackmodules where id = ?";
    this.remove(feedbackModule, stmt);
  }
}
