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
import jcs.entities.LocomotiveBean;

/**
 *
 * @author frans
 */
public class LocomotiveBeanDAO extends AbstractDAO<LocomotiveBean> {

    private static final String INS_LOC_STMT = "insert into locomotives (NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS,ID) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPD_LOC_STMT = "update locomotives set NAME = ?,PREVIOUSNAME = ?,UID = ?,MFXUID = ?,ADDRESS = ?,ICON = ?,DECODERTYPE = ?,MFXSID = ?,TACHOMAX = ?,VMIN = ?,ACCELERATIONDELAY = ?,BRAKEDELAY = ?,VOLUME = ?,SPM = ?,VELOCITY = ?,DIRECTION = ?,MFXTYPE = ?,BLOCKS = ? where id = ?";

    public LocomotiveBeanDAO() {
        super();
    }

    @Override
    protected LocomotiveBean map(ResultSet rs) throws SQLException {
        String name = rs.getString("NAME");
        String previousName = rs.getString("PREVIOUSNAME");
        Long uid = rs.getLong("UID");
        Long mfxUid = rs.getLong("MFXUID");
        Integer address = rs.getInt("ADDRESS");
        String icon = rs.getString("ICON");
        String decoderType = rs.getString("DECODERTYPE");
        String mfxSid = rs.getString("MFXSID");
        Integer tachoMax = rs.getInt("TACHOMAX");
        Integer vMin = rs.getInt("VMIN");
        Integer accelerationDelay = rs.getInt("ACCELERATIONDELAY");
        Integer brakeDelay = rs.getInt("BRAKEDELAY");
        Integer volume = rs.getInt("VOLUME");
        String spm = rs.getString("SPM");
        Integer velocity = rs.getInt("VELOCITY");
        Integer direction = rs.getInt("DIRECTION");
        String mfxType = rs.getString("MFXTYPE");
        String blocks = rs.getString("BLOCKS");

        BigDecimal id = new BigDecimal(rs.getLong("ID"));

        return new LocomotiveBean(id, name, previousName, uid, mfxUid, address, icon, decoderType,
                mfxSid, tachoMax, vMin, accelerationDelay, brakeDelay, volume, spm,
                velocity, direction, mfxType, blocks);
    }

    @Override
    protected void bind(PreparedStatement ps, LocomotiveBean loc) throws SQLException {
        ps.setString(1, loc.getName());
        ps.setString(2, loc.getPreviousName());
        ps.setLong(3, loc.getUid());
        if (loc.getMfxUid() != null) {
            ps.setLong(4, loc.getMfxUid());
        } else {
            ps.setNull(4, Types.INTEGER);
        }
        ps.setInt(5, loc.getAddress());
        ps.setString(6, loc.getIcon());
        ps.setString(7, loc.getDecoderType());
        ps.setString(8, loc.getMfxSid());
        ps.setInt(9, loc.getTachoMax());
        if (loc.getvMin() != null) {
            ps.setInt(10, loc.getvMin());
        } else {
            ps.setNull(10, Types.INTEGER);
        }

        if (loc.getAccelerationDelay() != null) {
            ps.setInt(11, loc.getAccelerationDelay());
        } else {
            ps.setNull(11, Types.INTEGER);
        }

        if (loc.getBrakeDelay() != null) {
            ps.setInt(12, loc.getBrakeDelay());
        } else {
            ps.setNull(12, Types.INTEGER);
        }

        if (loc.getVolume() != null) {
            ps.setInt(13, loc.getVolume());
        } else {
            ps.setNull(13, Types.INTEGER);
        }

        ps.setString(14, loc.getSpm());

        if (loc.getVelocity() != null) {
            ps.setInt(15, loc.getVelocity());
        } else {
            ps.setNull(15, Types.INTEGER);
        }

        if (loc.getRichtung() != null) {
            ps.setInt(16, loc.getRichtung());
        } else {
            ps.setNull(16, Types.INTEGER);
        }

        ps.setString(17, loc.getMfxType());
        ps.setString(18, loc.getBlocks());
        ps.setBigDecimal(19, loc.getId());
    }

    @Override
    public List<LocomotiveBean> findAll() {
        String stmt = "select * from locomotives order by id asc";
        return this.findAll(stmt);
    }

    @Override
    public LocomotiveBean find(Integer address, String decoderType) {
        String stmt = "select * from locomotives where address = ? and decodertype = ?";
        return this.find(address, decoderType, stmt);
    }

    public LocomotiveBean findById(BigDecimal id) {
        String stmt = "select * from locomotives where id = ?";
        return this.findById(id, stmt);
    }

    @Override
    public BigDecimal persist(LocomotiveBean locomotive) {
        //Check whether the loc exists..
        LocomotiveBean loc = this.findById(locomotive.getId());
//        if(loc == null) {
//            //extra check....
//            loc = this.find(locomotive.getAddress(),locomotive.getDecoderType());
//        }

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
    public void remove(LocomotiveBean locomotive) {
        String stmt = "delete from locomotives where id = ?";
        this.remove(locomotive, stmt);
    }

}
