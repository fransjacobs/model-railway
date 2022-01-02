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
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import jcs.entities.TrackPower;

/**
 *
 * @author frans
 */
public class TrackPowerDAO extends AbstractDAO<TrackPower> {

  private static final String INS_TRPO_STMT = "insert into TRACKPOWER (STATUS,FEEDBACKSOURCE,LASTUPDATED,ID) values(?,?,?,?)";
  private static final String UPD_TRPO_STMT = "update TRACKPOWER set STATUS = ?, FEEDBACKSOURCE = ?, LASTUPDATED = ? where ID = ?";

  public TrackPowerDAO() {
    super();
  }

  @Override
  protected TrackPower map(ResultSet rs) throws SQLException {
    BigDecimal id = rs.getBigDecimal("ID");
    String status = rs.getString("STATUS");
    String feedbacksource = rs.getString("FEEDBACKSOURCE");
    Date lastupdated = rs.getTimestamp("LASTUPDATED");

    TrackPower trpo = new TrackPower(TrackPower.getStatusType(status), TrackPower.getFeedbackSource(feedbacksource));
    trpo.setLastUpdated(lastupdated);
    trpo.setAddress(1);
    trpo.setId(id);
    return trpo;
  }

  @Override
  protected void bind(PreparedStatement ps, TrackPower trpo) throws SQLException {
    ps.setString(1, trpo.getStatus().toString());
    ps.setString(2, trpo.getFeedbackSource().toString());
    if (trpo.getLastUpdated() != null) {
      Timestamp ts = new Timestamp(trpo.getLastUpdated().getTime());
      ps.setTimestamp(3, ts);
    } else {
      ps.setTimestamp(3, null);
    }

    ps.setBigDecimal(4, trpo.getId());
  }

  @Override
  public List<TrackPower> findAll() {
    String stmt = "select * from trackpower order by id asc";

    return this.findAll(stmt);
  }

  //@Override
  public TrackPower find(Integer address) {
    String stmt = "select * from trackpower where id = ?";
    TrackPower tp;
    synchronized (this) {
      tp = this.find(address, stmt);
    }
    return tp;
  }

  @Override
  public BigDecimal persist(TrackPower trackPower) {
    //Check whether the TrackPower exists...
    TrackPower trpo = this.find(trackPower.getAddress());

    boolean update = false;
    String statement;
    if (trpo == null) {
      statement = INS_TRPO_STMT;
    } else {
      statement = UPD_TRPO_STMT;
      update = true;
    }

    synchronized (this) {
      upsert(trackPower, statement);
    }

    return trackPower.getId();
  }

  @Override
  public void remove(TrackPower feedbackModule) {
    String stmt = "delete from feedbackmodules where id = ?";
    this.remove(feedbackModule, stmt);
  }

}
