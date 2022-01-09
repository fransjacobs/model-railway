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

                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16390,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16389,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16394,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,23,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,12,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16396,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16392,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,2,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16391,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16395,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16397,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,11,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,37,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,63,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,49156,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16393,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (0,16398,0,1,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (1,16390,1,4,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (1,16389,1,172,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (1,16394,1,7,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (1,16392,1,7,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (1,16395,1,172,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (1,16397,1,48,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (1,49156,1,2,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (1,16393,1,7,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (1,16398,1,7,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,16390,2,23,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,16389,2,23,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,16394,2,23,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,23,2,4,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,16392,2,23,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,16391,2,23,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,16395,2,23,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,16397,2,48,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,37,2,10,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,49156,2,2,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,16393,2,23,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (2,16398,2,23,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,16390,3,10,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,16389,3,10,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,16394,3,10,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,12,3,8,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,16392,3,12,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,16391,3,138,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,16395,3,10,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,16397,3,8,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,11,3,8,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,37,3,10,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,49156,3,8,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,16393,3,8,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (3,16398,3,8,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16390,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16389,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16394,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,23,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,12,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16396,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16392,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,2,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16391,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16395,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16397,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,11,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,37,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,63,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,49156,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16393,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (4,16398,4,18,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (5,16390,5,20,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (5,16389,5,20,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (5,16394,5,20,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (5,16392,5,20,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (5,16395,5,20,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (5,16397,5,42,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (5,16393,5,140,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (5,16398,5,140,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (6,16390,6,41,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (6,16389,6,41,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (6,16394,6,41,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (6,16392,6,82,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (6,16395,6,41,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (6,16397,6,41,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (6,16393,6,12,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (6,16398,6,12,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (7,16390,7,10,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (7,16389,7,10,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (7,16394,7,138,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (7,16392,7,140,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (7,16395,7,138,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (7,16397,7,118,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (7,16393,7,26,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (7,16398,7,26,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (8,16390,8,42,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (8,16389,8,42,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (8,16394,8,42,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (8,16392,8,31,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (8,16395,8,42,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (8,16393,8,5,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (8,16398,8,5,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (9,16390,9,116,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (9,16389,9,171,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (9,16394,9,48,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (9,16392,9,106,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (9,16395,9,171,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (9,16393,9,13,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (9,16398,9,13,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (10,16390,10,220,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (10,16389,10,171,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (10,16394,10,29,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (10,16392,10,219,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (10,16395,10,171,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (10,16393,10,111,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (10,16398,10,111,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (11,16390,11,153,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (11,16389,11,29,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (11,16394,11,117,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (11,16392,11,26,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (11,16395,11,220,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (11,16393,11,36,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (11,16398,11,36,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (12,16390,12,11,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (12,16389,12,11,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (12,16394,12,116,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (12,16392,12,36,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (12,16395,12,29,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (12,16393,12,173,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (12,16398,12,173,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (13,16390,13,153,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (13,16389,13,116,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (13,16394,13,118,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (13,16392,13,111,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (13,16395,13,11,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (13,16393,13,173,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (13,16398,13,173,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (14,16390,14,112,1)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (14,16389,14,220,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (14,16394,14,118,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (14,16392,14,49,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (14,16395,14,37,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (14,16393,14,173,0)");
                stmt.executeUpdate("INSERT INTO FUNCTIONS (ID,LOCOID,NUMBER,TYPE,VALUE) VALUES (14,16398,14,173,0)");

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
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
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
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
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
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO LAYOUTTILES(ID,TILETYPE,ORIENTATION,DIRECTION,X,Y) "
                        + "VALUES(lati_seq.nextval,'SwitchTile','East','Left', 30, 30)");

                stmt.executeUpdate("INSERT INTO LAYOUTTILES(ID,TILETYPE,ORIENTATION,DIRECTION,X,Y) "
                        + "VALUES(lati_seq.nextval,'DiagonalTrack','South','Center', 40, 50)");

                stmt.executeUpdate("INSERT INTO LAYOUTTILES(ID,TILETYPE,ORIENTATION,DIRECTION,X,Y) "
                        + "VALUES(lati_seq.nextval,'StraightTrack','West','Center', 50, 60)");

//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (1,'StraightTrack','East','Center',200,140,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (2,'DiagonalTrack','North','Center',160,160,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (3,'DiagonalTrack','East','Center',240,160,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (4,'StraightTrack','North','Center',140,200,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (5,'StraightTrack','South','Center',260,200,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (6,'DiagonalTrack','West','Center',160,240,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (7,'DiagonalTrack','South','Center',240,240,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (8,'StraightTrack','West','Center',200,260,null,null);
                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

//        private static final String INS_TILE_STMT = "insert into tiles(tileType,orientation,direction,x,y,signalType,id) values(?,?,?,?,?,?,?)";
//    private static final String UPD_TILE_STMT = "update tiles set tileType = ?,orientation = ?,direction = ?,x = ?,y = ?, signalType =? where id = ?";
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

//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (1,'StraightTrack','East','Center',200,140,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (2,'DiagonalTrack','North','Center',160,160,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (3,'DiagonalTrack','East','Center',240,160,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (4,'StraightTrack','North','Center',140,200,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (5,'StraightTrack','South','Center',260,200,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (6,'DiagonalTrack','West','Center',160,240,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (7,'DiagonalTrack','South','Center',240,240,null,null);
//INSERT INTO "JCS"."LAYOUTTILES" (ID,TILETYPE,ORIENTATION,DIRECTION,X,Y,SOAC_ID,SENS_ID) VALUES (8,'StraightTrack','West','Center',200,260,null,null);
                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void insertDriveWayData() {
        Logger.debug("Inserting DriveWays...");
        try {
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
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
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
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
            try ( Connection c = connect(JCS_USER, JCS_PWD, true, true)) {
                Statement stmt = c.createStatement();
                stmt.executeUpdate("INSERT INTO JCSPROPERTIES (KEY1,VALUE1) VALUES ('k1','v1')");

                stmt.executeUpdate("INSERT INTO JCSPROPERTIES (KEY1,VALUE1) VALUES ('k2','v2')");

                c.commit();
            }
        } catch (SQLException ex) {
            Logger.error(ex);
        }
    }

    public static void setConnectProperties() {
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
