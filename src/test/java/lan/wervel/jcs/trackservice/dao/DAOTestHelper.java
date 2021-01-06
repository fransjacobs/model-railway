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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import lan.wervel.jcs.trackservice.dao.util.DatabaseCreator;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class DAOTestHelper extends DatabaseCreator {

    public static void insertLocoData() {
        Logger.debug("Inserting Locomotives...");
        try {
            try (Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                //stmt.executeUpdate("set SCHEMA JCS");
                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,1,'V200 027','V200','3021','mm2_prg','Forwards',0,null,14,0,null,5,'10000',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,2,'BR81 002','BR 81 002','30321','mm2_dil8','Forwards',0,null,14,0,null,1,'1',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,3,'BR 1022','BR1022','3795.10','mm2_prg','Forwards',0,null,14,0,null,5,'001-0',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,6,'BR 44 690','BR 44 690','3047','mm2_prg','Forwards',0,null,14,0,null,5,'10000',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,8,'NS 6502','DHG 700C NS 6502','29159.1','mm2_dil8','Forwards',0,null,14,0,null,1,'1',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,11,'NS 1205','NS 1205','3055','mm2_prg','Forwards',0,null,14,0,null,5,'100000',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,12,'BR141 015','E 141 015-8','3034.10','mm2_prg','Forwards',0,null,14,0,null,5,'100000',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,14,'V36','V36/BR236','3142','mm2_prg','Forwards',0,null,14,0,null,5,'100000',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,17,'1855','NS 1855','37263','mm2_prg','Forwards',0,null,14,0,null,5,'10000',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,6,'NS E186','NS E186','36629','mfx','Forwards',0,null,14,0,null,16,'1000000000000000',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,25,'ER 20','Hercules Police','36793','mm2_prg','Forwards',0,null,14,0,null,5,'100000',null,'Forwards',null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME) "
                        + "VALUES (loco_seq.nextval,5,'BR 152','BR 152','39850','mfx','Forwards',0,null,14,0,null,16,'1000000000000000',null,'Forwards',null)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertTurnoutData() {
        Logger.debug("Inserting Turnouts...");
        try {
            try (Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
                        + "VALUES(soac_seq.nextval,1,'T 1','T 1','5117 R','T','G',null,2,200)");

                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
                        + "VALUES(soac_seq.nextval,2,'T 2','T 2','5117 L','T','R',null,2,250)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertSignalData() {
        Logger.debug("Inserting Signals...");
        try {
            try (Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
                        + "VALUES(soac_seq.nextval,3,'S 3','S 3','home made','S','G',null,2,200)");

                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
                        + "VALUES(soac_seq.nextval,4,'S 4','S 4','3942','S','R',null,2,200)");

                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
                        + "VALUES(soac_seq.nextval,5,'S 5','S 5','3943','S','G',null,4,200)");

                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
                        + "VALUES(soac_seq.nextval,6,'S 5','S 5','3943','S','R',(soac_seq.currval - 1),4,200)");

                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE,SWITCH_TIME) "
                        + "VALUES(soac_seq.nextval,7,'S 7','S 7','dual','S','G',null,4,'Hp2',200)");

                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE,SWITCH_TIME) "
                        + "VALUES(soac_seq.nextval,8,'S 7','S 7','dual','S','G',(soac_seq.currval - 1),4,'Hp2',200)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertSensorData() {
        Logger.debug("Inserting Sensors...");
        try {
            try (Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO sensors (id,address,device_id,name,description,value,previous_value,millis,lastupdated) "
                        + "VALUES(sens_seq.nextval,1,0,'M1P1','M1P1',0,1,0,null)");

                stmt.executeUpdate("INSERT INTO sensors (id,address,device_id,name,description,value,previous_value,millis,lastupdated) "
                        + "VALUES(sens_seq.nextval,2,0,'M1P2','M1P2',1,0,10,null)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertLayoutTileData() {
        Logger.debug("Inserting LayoutTiles...");
        try {
            try (Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO LAYOUTTILES(ID,TILETYPE,ORIENTATION,DIRECTION,X,Y) "
                        + "VALUES(lati_seq.nextval,'TurnoutTile','East','Left', 30, 30)");

                stmt.executeUpdate("INSERT INTO LAYOUTTILES(ID,TILETYPE,ORIENTATION,DIRECTION,X,Y) "
                        + "VALUES(lati_seq.nextval,'DiagonalTrack','South','Center', 40, 50)");

                stmt.executeUpdate("INSERT INTO LAYOUTTILES(ID,TILETYPE,ORIENTATION,DIRECTION,X,Y) "
                        + "VALUES(lati_seq.nextval,'StraightTrack','West','Center', 50, 60)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertDriveWayData() {
        Logger.debug("Inserting DriveWays...");
        try {
            try (Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO DRIVEWAYS (ID,ADDRESS,NAME,DESCRIPTION,FROM_LATI_ID,TO_LATI_ID,LOCO_ID,ACTIVE,RESERVED,OCCUPIED) "
                        + "VALUES (drwa_seq.nextval,1,'Blk 1','Block 1',null,null,null,1,0,0)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertRouteData() {
        Logger.debug("Inserting Routes...");
        try {
            try (Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO ROUTES (ID,ADDRESS,NAME,DESCRIPTION,DRWA_ID,LATI_ID) "
                        + "VALUES (rout_seq.nextval,1,'Rt 1','Route 1',1,1)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertJCSPropertiesData() {
        Logger.debug("Inserting JCSProperties...");
        try {
            try (Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval,'k1','v1')");

                stmt.executeUpdate("INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval,'k2','v2')");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void setConnectProperties() {
        Configurator.defaultConfig().level(org.pmw.tinylog.Level.DEBUG).activate();
        Logger.info("Test Mode Active!");
        System.setProperty("db.path", DB_PATH);
        System.setProperty("db.name", TEST_DB_NAME);
        System.setProperty("db.mode", DB_MODE);
        System.setProperty("db.user", JCS_USER);
        System.setProperty("db.pass", JCS_PWD);
        //System.setProperty("db.schema", SCHEMA);

        System.setProperty("db.sa.user", ADMIN_USER);
        System.setProperty("db.sa.pass", JCS_PWD);
    }

    public static void createNewDatabase() {
        DatabaseCreator.recreateTest();
    }
}
