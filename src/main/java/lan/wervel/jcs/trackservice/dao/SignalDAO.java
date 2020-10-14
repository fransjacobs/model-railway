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
package lan.wervel.jcs.trackservice.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import lan.wervel.jcs.entities.Signal;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.SignalValue;

/**
 *
 * @author frans
 */
public class SignalDAO extends AbstractDAO<Signal> {

  private static final String INS_SA_STMT = "insert into solenoidaccessories (ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE,ID) values(?,?,?,?,?,?,?,?,?,?)";
  private static final String UPD_SA_STMT = "update solenoidaccessories set ADDRESS = ?,NAME = ?,DESCRIPTION = ?,CATALOG_NUMBER = ?,ACCESSORY_TYPE = ?,CURRENT_STATUS_TYPE = ?, SOAC_ID = ?, LIGHT_IMAGES = ?, SIGNAL_VALUE = ? where ID = ?";

  public SignalDAO() {
    super();
  }

  @Override
  protected Signal map(ResultSet rs) throws SQLException {
    BigDecimal id = rs.getBigDecimal(1); // rs.getBigDecimal("ID");
    Integer address = rs.getInt(2); // rs.getInt("ADDRESS");
    String name = rs.getString("NAME");
    String description = rs.getString("DESCRIPTION");
    String catalogNumber = rs.getString("CATALOG_NUMBER");
    String currentStatusType = rs.getString("CURRENT_STATUS_TYPE");
    Integer lightImages = rs.getInt("LIGHT_IMAGES");
    String sigVal = rs.getString("SIGNAL_VALUE");

    BigDecimal id2 = rs.getBigDecimal(10); //rs.getBigDecimal("ID2");
    int a2 = rs.getInt(11); // rs.getInt("ADDRESS2");
    Integer address2 = null;
    if (a2 > 0) {
      address2 = a2;
    }

    String currentStatusType2 = rs.getString(12); // rs.getString("CURRENT_STATUS_TYPE2");

    AccessoryValue value = null;
    if (currentStatusType != null) {
      value = AccessoryValue.dbGet(currentStatusType);
    }
    AccessoryValue value2 = null;
    if (currentStatusType2 != null) {
      value2 = AccessoryValue.dbGet(currentStatusType2);
    }

    SignalValue signalValue = null;
    if (sigVal != null) {
      signalValue = SignalValue.get(sigVal);
    }

    Signal signal = new Signal(address, description, catalogNumber, id, value, null, lightImages, id2, address2, value2, signalValue);
    signal.setName(name);

    return signal;
  }

  @Override
  protected void bind(PreparedStatement ps, Signal signal) throws SQLException {
    ps.setInt(1, signal.getAddress());
    ps.setString(2, signal.getName());
    ps.setString(3, signal.getDescription());
    ps.setString(4, signal.getCatalogNumber());

    ps.setString(5, "S");

    if (signal.getValue() == null) {
      ps.setNull(6, Types.VARCHAR);
    } else {
      ps.setString(6, signal.getValue().getDBValue());
    }
    if (signal.getSoacId() != null) {
      ps.setBigDecimal(7, signal.getSoacId());
    } else {
      ps.setNull(7, Types.BIGINT);
    }
    ps.setInt(8, signal.getLightImages());

    if (signal.getSignalValue() != null) {
      ps.setString(9, signal.getSignalValue().getSignalValue());
    } else {
      ps.setNull(9, Types.VARCHAR);
    }
    ps.setBigDecimal(10, signal.getId());
  }

  @Override
  public List<Signal> findAll() {
    String stmt = "select s1.id,s1.address,s1.name,s1.description,s1.catalog_number,s1.accessory_type,s1.current_status_type,s1.light_images,s1.signal_value"
            + ",s2.id as id2,s2.address as address2,s2.current_status_type as current_status_type2 "
            + "from solenoidaccessories as s1 "
            + "left join solenoidaccessories as s2 on s2.soac_id = s1.id "
            + "where s1.accessory_type = 'S' "
            + "and s1.soac_id is null "
            + "order by s1.address";

    return this.findAll(stmt);
  }

  @Override
  public Signal find(Integer address) {
    String stmt = "select s1.id,s1.address,s1.name,s1.description,s1.catalog_number,s1.accessory_type,s1.current_status_type,s1.light_images,s1.signal_value"
            + ",s2.id as id2,s2.address as address2,s2.current_status_type as current_status_type2 "
            + "from solenoidaccessories as s1 "
            + "left join solenoidaccessories as s2 on s2.soac_id = s1.id "
            + "where s1.accessory_type = 'S' "
            + "and s1.soac_id is null and s1.address = ?";

    return this.find(address, stmt);
  }

  public Signal findById(BigDecimal id) {
    //String stmt = "select * from solenoidaccessoiries where id = ?";

    String stmt = "select s1.id,s1.address,s1.name,s1.description,s1.catalog_number,s1.accessory_type,s1.current_status_type,s1.light_images,s1.signal_value"
            + ",s2.id as id2,s2.address as address2,s2.current_status_type as current_status_type2 "
            + "from solenoidaccessories as s1 "
            + "left join solenoidaccessories as s2 on s2.soac_id = s1.id "
            + "where s1.accessory_type = 'S' "
            + "and s1.soac_id is null and s1.id = ?";

    return this.findById(id, stmt);
  }

  @Override
  public BigDecimal persist(Signal signal) {
    //Check whether the signal exists..
    Signal sr = this.find(signal.getAddress());

    String statement;
    if (sr == null) {
      statement = INS_SA_STMT;
    } else {
      statement = UPD_SA_STMT;
    }

    upsert(signal, statement);

    if (signal.getAddress2() != null) {
      //2nd "part" of signal on an even address...
      Signal s2 = new Signal(signal.getAddress2(), signal.getDescription(), signal.getCatalogNumber(), signal.getId2(), signal.getValue2(), signal.getId(), signal.getLightImages(), signal.getSignalValue());
      s2.setName(signal.getName());

      upsert(s2, statement);
      signal.setId2(s2.getId());
    }

    return signal.getId();
  }

  @Override
  public void remove(Signal signal) {
    String stmt = "delete from solenoidaccessories where id = ?";

    if (signal.getAddress2() != null) {
      Signal s2 = new Signal(signal.getAddress2(), signal.getDescription(), signal.getCatalogNumber(), signal.getId2(), signal.getValue2(), signal.getId(), signal.getLightImages(), signal.getSignalValue());
      this.remove(s2, stmt);
    }

    this.remove(signal, stmt);
  }
}
