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
package jcs.trackservice.dao.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class DAOTestHelper extends DatabaseCreator {

    public static void insertLocoData() {
        Logger.debug("Inserting Locomotives...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (2,'BR 81 002','BR  81 002','2',null,2,'DB BR 81 008','mm_prg',null,120,1,1,2,64,null,600,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (11,'NS 1205',null,'11',null,11,'NS 1211','mm_prg',null,120,null,0,0,64,null,0,1,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (12,'BR 141 015-08','BR 141 015-08','12',null,12,'DB BR 141 136-2','mm_prg',null,120,null,0,0,64,null,0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (23,'BR 101 003-2','BR 101 003-2','23',null,23,'DB BR 101 109-7','mm_prg',null,200,null,0,0,64,null,0,1,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (37,'NS 1720','S. 1700','37',null,37,'NS 1773','mm_prg',null,120,null,0,0,64,null,0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (63,'NS 6513','NS  6513','63',null,63,'NS 6513','mm_prg',null,120,null,0,0,64,null,0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16389,'193 304-3 DB AG',null,'16389','1945312555',5,'DB BR 193 304-3','mfx','0x5',160,5,15,15,255,null,0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16390,'152 119-4 DBAG',null,'16390','2113628077',6,'DB BR 152 119-4','mfx','0x6',140,4,28,15,255,'0',0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16391,'DB 640 017-9','DB 640 017-9','16391','2097006535',7,'DB BR 640 017-9','mfx','0x7',100,8,15,15,64,null,0,1,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16392,'BR 44 690','BR 44 690','16392','1945180592',8,'DB BR 44 100','mfx','0x8',80,5,21,12,233,null,0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16393,'Rheingold 1','Rheingold 1','16393','1945195567',9,'DB BR 18 537','mfx','0x9',81,4,12,8,255,null,0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16394,'561-05 RRF','561-05 RRF','16394','1945385732',10,'56-05 RRF','mfx','0xa',120,5,31,31,220,'0',0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16395,'E 186 007-8 NS',null,'16395','1945441079',11,'NS 186 012-8','mfx','0xb',140,5,16,16,255,null,0,1,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16396,'BR 216 059-6',null,'16396','1945302187',12,'DB BR 216 059-6','mfx','0xc',120,5,13,13,64,null,0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16397,'NS 1139','NS 1139','16397','4193976353',13,'NS 1136','mfx','0xd',140,6,16,4,64,null,0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (16398,'Rheingold 2','Rheingold 2','16398','1945186577',14,'DB BR 18 473','mfx','0xe',81,4,12,8,255,null,0,0,null,null)");

                stmt.executeUpdate("INSERT INTO LOCOMOTIVES (ID,NAME,PREVIOUSNAME,UID,MFXUID,ADDRESS,ICON,DECODERTYPE,MFXSID,TACHOMAX,VMIN,ACCELERATIONDELAY,BRAKEDELAY,VOLUME,SPM,VELOCITY,DIRECTION,MFXTYPE,BLOCKS) "
                        + "VALUES (49156,'NS Plan Y','DCC Lok 4','49156',null,4,'NS Plan Y','dcc',null,120,null,0,0,64,null,0,0,null,null)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertLocoFuncData() {
        Logger.debug("Inserting Locomotive Functions...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();

                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (2,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (2,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (11,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (11,3,8,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (11,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,1,172,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,2,23,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,5,20,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,6,41,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,7,10,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,8,42,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,9,171,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,10,171,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,11,29,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,12,11,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,13,116,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (LOCOID,NUMBER,TYPE,FVALUE) VALUES (16389,14,220,0)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertTurnoutData() {
        Logger.debug("Inserting Turnouts...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();

                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (1,1,'W 1R','rechtsweiche',1,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (2,2,'W 2L','linksweiche',1,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (3,3,'W 3R','rechtsweiche',0,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (4,4,'W 4R','rechtsweiche',1,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (5,5,'W 5R','rechtsweiche',1,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (6,6,'W 6R','rechtsweiche',0,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (7,7,'W 7L','linksweiche',1,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (8,8,'W 8L','linksweiche',0,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (9,9,'W 9R','rechtsweiche',0,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (10,10,'W 10R','rechtsweiche',0,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (11,11,'W 11L','linksweiche',1,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (12,12,'W 12L','linksweiche',0,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (13,13,'W 13L','linksweiche',0,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (14,14,'W 14R','rechtsweiche',1,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (15,15,'W 17R','rechtsweiche',1,200,'mm2','ein_alt')");
                stmt.executeUpdate("INSERT INTO ACCESSORIES (ID,ADDRESS,NAME,TYPE,POSITION,SWITCHTIME,DECODERTYPE,DECODER) VALUES (16,16,'W 18R','rechtsweiche',1,200,'mm2','ein_alt')");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

//    public static void insertSignalData() {
//        Logger.debug("Inserting Signals...");
//        try {
//            try (Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
//                Statement stmt = c.createStatement();
//
//                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
//                        + "VALUES(3,'S 3','S 3','home made','S','G',null,2,200)");
//
//                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
//                        + "VALUES(soac_seq.nextval,4,'S 4','S 4','3942','S','R',null,2,200)");
//
//                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
//                        + "VALUES(soac_seq.nextval,5,'S 5','S 5','3943','S','G',null,4,200)");
//
//                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SWITCH_TIME) "
//                        + "VALUES(soac_seq.nextval,6,'S 5','S 5','3943','S','R',(soac_seq.currval - 1),4,200)");
//
//                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE,SWITCH_TIME) "
//                        + "VALUES(soac_seq.nextval,7,'S 7','S 7','dual','S','G',null,4,'Hp2',200)");
//
//                stmt.executeUpdate("INSERT INTO SOLENOIDACCESSORIES(ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE,SWITCH_TIME) "
//                        + "VALUES(soac_seq.nextval,8,'S 7','S 7','dual','S','G',(soac_seq.currval - 1),4,'Hp2',200)");
//
//                c.commit();
//            }
//        } catch (SQLException ex) {
//            Logger.error(ex);
//        }
//    }
    public static void insertSensorData() {
        Logger.debug("Inserting Sensors...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO sensors (name,deviceid,contactid,status,previousstatus,millis,lastupdated) "
                        + "VALUES('M1P1',65,1,0,0,0,null)");

                stmt.executeUpdate("INSERT INTO sensors (name,deviceid,contactid,status,previousstatus,millis,lastupdated) "
                        + "VALUES('M1P2',65,2,1,0,0,null)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertTileData() {
        Logger.debug("Inserting TileBeans...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO TILES(TILETYPE,ORIENTATION,DIRECTION,X,Y,SIGNALTYPE,ID) "
                        + "VALUES('Switch','East','Left', 30, 30,null,'sw-1')");

                stmt.executeUpdate("INSERT INTO TILES(TILETYPE,ORIENTATION,DIRECTION,X,Y,SIGNALTYPE,ID) "
                        + "VALUES('Curved','South','Center', 40, 50,null,'ct-5')");

                stmt.executeUpdate("INSERT INTO TILES(TILETYPE,ORIENTATION,DIRECTION,X,Y,SIGNALTYPE,ID) "
                        + "VALUES('Straight','West','Center', 50, 60, null,'st-7')");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertTileLayoutData() {
        Logger.debug("Inserting Layout TileBeans...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO TILES (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SIGNALTYPE,ACCESSORYID,SENSORID) VALUES "
                        + "('bk-2','Block','East','Center',420,140,NULL,NULL,NULL),"
                        + "('st-1','Straight','East','Center',300,180,NULL,NULL,NULL),"
                        + "('ct-2','Curved','East','Center',260,140,NULL,NULL,NULL),"
                        + "('se-6','Sensor','West','Center',500,380,NULL,NULL,NULL),"
                        + "('st-3','Straight','East','Center',300,140,NULL,NULL,NULL),"
                        + "('st-4','Straight','East','Center',540,140,NULL,NULL,NULL),"
                        + "('st-7','Straight','South','Center',180,220,NULL,NULL,NULL),"
                        + "('se-1','Sensor','East','Center',340,180,NULL,NULL,NULL),"
                        + "('st-20','Straight','West','Center',620,380,NULL,NULL,NULL),"
                        + "('se-4','Sensor','East','Center',340,140,NULL,NULL,NULL) ");

                stmt.executeUpdate("INSERT INTO JCS.TILES (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SIGNALTYPE,ACCESSORYID,SENSORID) VALUES "
                        + "('st-19','Straight','West','Center',580,380,NULL,NULL,NULL),"
                        + "('sw-1','Switch','West','Left',260,180,NULL,NULL,NULL),"
                        + "('se-5','Sensor','West','Center',340,380,NULL,NULL,NULL),"
                        + "('ct-5','Curved','North','Center',180,380,NULL,NULL,NULL),"
                        + "('st-18','Straight','West','Center',540,380,NULL,NULL,NULL),"
                        + "('st-2','Straight','East','Center',540,180,NULL,NULL,NULL),"
                        + "('ct-3','Curved','East','Center',180,180,NULL,NULL,NULL),"
                        + "('st-10','Straight','South','Center',180,340,NULL,NULL,NULL),"
                        + "('st-15','Straight','West','Center',220,380,NULL,NULL,NULL),"
                        + "('st-12','Straight','South','Center',660,260,NULL,NULL,NULL)");

                stmt.executeUpdate("INSERT INTO JCS.TILES (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SIGNALTYPE,ACCESSORYID,SENSORID) VALUES "
                        + "('st-16','Straight','West','Center',260,380,NULL,NULL,NULL),"
                        + "('st-9','Straight','South','Center',180,300,NULL,NULL,NULL),"
                        + "('bk-3','Block','West','Center',420,380,NULL,NULL,NULL),"
                        + "('st-6','Straight','East','Center',220,180,NULL,NULL,NULL),"
                        + "('se-3','Sensor','East','Center',500,140,NULL,NULL,NULL),"
                        + "('st-8','Straight','South','Center',180,260,NULL,NULL,NULL),"
                        + "('st-11','Straight','South','Center',660,220,NULL,NULL,NULL),"
                        + "('bk-1','Block','East','Center',420,180,NULL,NULL,NULL),"
                        + "('st-13','Straight','South','Center',660,300,NULL,NULL,NULL),"
                        + "('se-2','Sensor','East','Center',500,180,NULL,NULL,NULL)");

                stmt.executeUpdate("INSERT INTO JCS.TILES (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SIGNALTYPE,ACCESSORYID,SENSORID) VALUES "
                        + "('ct-6','Curved','West','Center',660,380,NULL,NULL,NULL),"
                        + "('st-14','Straight','South','Center',660,340,NULL,NULL,NULL),"
                        + "('ct-4','Curved','South','Center',660,180,NULL,NULL,NULL),"
                        + "('sw-2','Switch','East','Right',580,180,NULL,NULL,NULL),"
                        + "('st-5','Straight','East','Center',620,180,NULL,NULL,NULL),"
                        + "('st-17','Straight','West','Center',300,380,NULL,NULL,NULL),"
                        + "('ct-1','Curved','South','Center',580,140,NULL,NULL,NULL)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }

    }

    public static void insertRouteData() {
        Logger.debug("Inserting Routes...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();

                stmt.executeUpdate("INSERT INTO ROUTES (FROMTILEID,TOTILEID,COLOR,ID) "
                        + "VALUES ('bk-1+','bk-3-','red','bk-1+|bk-3-')");

                stmt.executeUpdate("INSERT INTO ROUTES (FROMTILEID,TOTILEID,COLOR,ID) "
                        + "VALUES ('bk-2+','bk-3-','green','bk-2+|bk-3-')");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertRouteElementData() {
        Logger.debug("Inserting RouteElements...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();

                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','bk-1+','bk-1',null,0)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','se-2','se-2',null,1)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','st-2','st-2',null,2)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','sw-2-G','sw-2','G',3)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','sw-2','sw-2','G',4)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','st-5','st-5',null,5)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','ct-4','ct-4',null,6)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','st-11','st-11',null,7)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','st-12','st-12',null,8)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','st-13','st-13',null,9)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','st-14','st-14',null,10)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','ct-6','ct-6',null,11)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','st-20','st-20',null,12)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','st-19','st-19',null,13)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','st-18','st-18',null,14)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','se-6','se-6',null,15)");
                stmt.executeUpdate("INSERT INTO ROUTEELEMENTS (ROUTEID,NODEID,TILEID,ACCESSORYVALUE,ORDER_SEQ) "
                        + "VALUES ('bk-1+|bk-3-','bk-3-','bk-3',null,16)");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertJCSPropertiesData() {
        Logger.debug("Inserting JCSProperties...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO JCSPROPERTIES (PKEY,PVALUE) VALUES ('k1','v1')");

                stmt.executeUpdate("INSERT INTO JCSPROPERTIES (PKEY,PVALUE) VALUES ('k2','v2')");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void setConnectProperties() {
        RunUtil.loadProperties();
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
