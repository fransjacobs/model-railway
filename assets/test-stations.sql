-- H2 2.3.232; 
;              
CREATE USER IF NOT EXISTS "JCS" SALT 'ea22afc18092a543' HASH '201ce5a9c0ea7e3b8552b74b680f26faec7f3c3293e7978d87567cb05d73bbfb' ADMIN;         
CREATE USER IF NOT EXISTS "SA" SALT '837eb7c2d82cff22' HASH 'ca96fdbe71509cc06ddfa8434e75e41d68f4c4fc3db03284fb3b8932032102c3' ADMIN;          
CREATE SCHEMA IF NOT EXISTS "jcs" AUTHORIZATION "JCS";         
DROP TABLE IF EXISTS "jcs"."tiles" CASCADE;    
DROP TABLE IF EXISTS "jcs"."stations" CASCADE; 
DROP TABLE IF EXISTS "jcs"."station_blocks" CASCADE;           
DROP TABLE IF EXISTS "jcs"."accessories" CASCADE;              
DROP TABLE IF EXISTS "jcs"."blocks" CASCADE;   
DROP TABLE IF EXISTS "jcs"."sensors" CASCADE;  
DROP TABLE IF EXISTS "jcs"."locomotive_functions" CASCADE;     
DROP TABLE IF EXISTS "jcs"."locomotives" CASCADE;              
DROP TABLE IF EXISTS "jcs"."command_stations" CASCADE;         
DROP TABLE IF EXISTS "jcs"."route_elements" CASCADE;           
DROP TABLE IF EXISTS "jcs"."jcs_properties" CASCADE;           
DROP TABLE IF EXISTS "jcs"."jcs_version" CASCADE;              
DROP TABLE IF EXISTS "jcs"."routes" CASCADE;   
CREATE CACHED TABLE "jcs"."tiles"(
    "id" CHARACTER VARYING(255) NOT NULL,
    "tile_type" CHARACTER VARYING(255) NOT NULL,
    "orientation" CHARACTER VARYING(255) NOT NULL,
    "direction" CHARACTER VARYING(255) NOT NULL,
    "x" INTEGER NOT NULL,
    "y" INTEGER NOT NULL,
    "signal_type" CHARACTER VARYING(255),
    "accessory_id" CHARACTER VARYING(255),
    "sensor_id" INTEGER
);          
ALTER TABLE "jcs"."tiles" ADD CONSTRAINT "jcs"."tile_pk" PRIMARY KEY("id");    
-- 38 +/- SELECT COUNT(*) FROM jcs.tiles;      
INSERT INTO "jcs"."tiles" VALUES
('bk-1', 'Block', 'East', 'Center', 380, 100, NULL, NULL, NULL),
('bk-2', 'Block', 'East', 'Center', 380, 140, NULL, NULL, NULL),
('se-1', 'Sensor', 'East', 'Center', 300, 100, NULL, NULL, 0),
('se-2', 'Sensor', 'East', 'Center', 460, 100, NULL, NULL, 1),
('se-3', 'Sensor', 'East', 'Center', 300, 140, NULL, NULL, 2),
('se-4', 'Sensor', 'East', 'Center', 460, 140, NULL, NULL, 3),
('sw-1', 'Switch', 'West', 'Left', 220, 140, 'NONE', '001', NULL),
('sw-2', 'Switch', 'East', 'Right', 540, 140, 'NONE', '002', NULL),
('ct-1', 'Curved', 'East', 'Center', 220, 100, NULL, NULL, NULL),
('ct-2', 'Curved', 'South', 'Center', 540, 100, NULL, NULL, NULL),
('st-1', 'Straight', 'West', 'Center', 260, 100, NULL, NULL, NULL),
('st-2', 'Straight', 'West', 'Center', 500, 100, NULL, NULL, NULL),
('st-3', 'Straight', 'West', 'Center', 260, 140, NULL, NULL, NULL),
('st-4', 'Straight', 'West', 'Center', 500, 140, NULL, NULL, NULL),
('st-5', 'Straight', 'West', 'Center', 180, 140, NULL, NULL, NULL),
('st-6', 'Straight', 'West', 'Center', 580, 140, NULL, NULL, NULL),
('ct-3', 'Curved', 'East', 'Center', 140, 140, NULL, NULL, NULL),
('ct-4', 'Curved', 'South', 'Center', 620, 140, NULL, NULL, NULL),
('st-7', 'Straight', 'South', 'Center', 140, 180, NULL, NULL, NULL),
('st-8', 'Straight', 'South', 'Center', 620, 180, NULL, NULL, NULL),
('bk-3', 'Block', 'West', 'Center', 380, 220, NULL, NULL, NULL),
('bk-4', 'Block', 'West', 'Center', 380, 260, NULL, NULL, NULL),
('se-5', 'Sensor', 'West', 'Center', 300, 220, NULL, NULL, 4),
('se-6', 'Sensor', 'West', 'Center', 460, 220, NULL, NULL, 5),
('se-7', 'Sensor', 'West', 'Center', 300, 260, NULL, NULL, 6),
('se-8', 'Sensor', 'West', 'Center', 460, 260, NULL, NULL, 7),
('sw-3', 'Switch', 'East', 'Left', 540, 220, 'NONE', '004', NULL),
('sw-4', 'Switch', 'West', 'Right', 220, 220, 'NONE', '003', NULL),
('ct-5', 'Curved', 'North', 'Center', 220, 260, NULL, NULL, NULL),
('ct-6', 'Curved', 'West', 'Center', 540, 260, NULL, NULL, NULL),
('st-9', 'Straight', 'West', 'Center', 260, 220, NULL, NULL, NULL),
('st-10', 'Straight', 'West', 'Center', 260, 260, NULL, NULL, NULL),
('st-11', 'Straight', 'West', 'Center', 500, 260, NULL, NULL, NULL),
('st-12', 'Straight', 'West', 'Center', 500, 220, NULL, NULL, NULL),
('st-13', 'Straight', 'West', 'Center', 580, 220, NULL, NULL, NULL),
('st-14', 'Straight', 'West', 'Center', 180, 220, NULL, NULL, NULL),
('ct-7', 'Curved', 'North', 'Center', 140, 220, NULL, NULL, NULL),
('ct-8', 'Curved', 'West', 'Center', 620, 220, NULL, NULL, NULL);  
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."tile_pk_idx" ON "jcs"."tiles"("id" NULLS FIRST);     
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."tile_x_y_un_idx" ON "jcs"."tiles"("x" NULLS FIRST, "y" NULLS FIRST); 
CREATE CACHED TABLE "jcs"."stations"(
    "id" CHARACTER VARYING(255) NOT NULL,
    "name" CHARACTER VARYING(255) NOT NULL,
    "min_locs" INTEGER DEFAULT 0 NOT NULL,
    "loc_count" INTEGER DEFAULT 0 NOT NULL,
    "use_fifo" BOOLEAN DEFAULT FALSE NOT NULL
);            
ALTER TABLE "jcs"."stations" ADD CONSTRAINT "jcs"."stat_pk" PRIMARY KEY("id"); 
-- 1 +/- SELECT COUNT(*) FROM jcs.stations;    
INSERT INTO "jcs"."stations" VALUES
('station-1', 'Station', 2, 0, TRUE);      
CREATE CACHED TABLE "jcs"."station_blocks"(
    "id" CHARACTER VARYING(255) NOT NULL,
    "station_id" CHARACTER VARYING(255) NOT NULL,
    "block_id" CHARACTER VARYING(255) NOT NULL,
    "last_updated" TIMESTAMP
);        
ALTER TABLE "jcs"."station_blocks" ADD CONSTRAINT "jcs"."stbl_pk" PRIMARY KEY("id");           
-- 2 +/- SELECT COUNT(*) FROM jcs.station_blocks;              
INSERT INTO "jcs"."station_blocks" VALUES
('station-1~bk-2', 'station-1', 'bk-2', TIMESTAMP '2026-01-22 19:32:02.049'),
('station-1~bk-1', 'station-1', 'bk-1', TIMESTAMP '2026-01-22 19:32:02.049');          
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."stbl_stat_blck_un_idx" ON "jcs"."station_blocks"("station_id" NULLS FIRST, "block_id" NULLS FIRST);  
CREATE CACHED TABLE "jcs"."accessories"(
    "id" CHARACTER VARYING(255) NOT NULL,
    "address" INTEGER NOT NULL,
    "name" CHARACTER VARYING(255) NOT NULL,
    "type" CHARACTER VARYING(255) NOT NULL,
    "state" INTEGER,
    "states" INTEGER,
    "switch_time" INTEGER,
    "protocol" CHARACTER VARYING(255),
    "decoder" CHARACTER VARYING(255),
    "accessory_group" CHARACTER VARYING(255),
    "icon" CHARACTER VARYING(255),
    "icon_file" CHARACTER VARYING(255),
    "imported" CHARACTER VARYING(255),
    "command_station_id" CHARACTER VARYING(255) NOT NULL,
    "synchronize" BOOLEAN DEFAULT FALSE NOT NULL,
    "address2" INTEGER
);            
ALTER TABLE "jcs"."accessories" ADD CONSTRAINT "jcs"."acce_pk" PRIMARY KEY("id");              
-- 4 +/- SELECT COUNT(*) FROM jcs.accessories; 
INSERT INTO "jcs"."accessories" VALUES
('001', 1, 'W01', 'rechtsweiche', 0, 2, 200, 'dcc', NULL, 'other', NULL, NULL, NULL, 'virtual', FALSE, NULL),
('002', 2, 'W02', 'linksweiche', 1, 2, 200, 'dcc', NULL, 'other', NULL, NULL, NULL, 'virtual', FALSE, NULL),
('003', 3, 'W03', 'rechtsweiche', 0, 2, 200, 'dcc', NULL, 'other', NULL, NULL, NULL, 'virtual', FALSE, NULL),
('004', 4, 'W04', 'linksweiche', 1, 2, 200, 'dcc', NULL, 'other', NULL, NULL, NULL, 'virtual', FALSE, NULL);   
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."acce_address_un_idx" ON "jcs"."accessories"("address" NULLS FIRST, "protocol" NULLS FIRST, "command_station_id" NULLS FIRST);        
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."acce_pk_idx" ON "jcs"."accessories"("id" NULLS FIRST);               
CREATE CACHED TABLE "jcs"."blocks"(
    "id" CHARACTER VARYING(255) NOT NULL,
    "tile_id" CHARACTER VARYING(255) NOT NULL,
    "description" CHARACTER VARYING(255),
    "plus_sensor_id" INTEGER,
    "min_sensor_id" INTEGER,
    "plus_signal_id" CHARACTER VARYING(255),
    "min_signal_id" CHARACTER VARYING(255),
    "locomotive_id" BIGINT,
    "allow_non_commuter_only" BOOLEAN DEFAULT FALSE NOT NULL,
    "status" CHARACTER VARYING(255),
    "incoming_suffix" CHARACTER VARYING(255),
    "min_wait_time" INTEGER DEFAULT 10 NOT NULL,
    "max_wait_time" INTEGER,
    "random_wait" BOOLEAN DEFAULT FALSE NOT NULL,
    "always_stop" BOOLEAN DEFAULT FALSE NOT NULL,
    "allow_commuter_only" BOOLEAN DEFAULT FALSE NOT NULL,
    "logical_direction" CHARACTER VARYING(255),
    "allow_direction_change" BOOLEAN DEFAULT TRUE NOT NULL
);              
ALTER TABLE "jcs"."blocks" ADD CONSTRAINT "jcs"."bloc_pk" PRIMARY KEY("id");   
-- 4 +/- SELECT COUNT(*) FROM jcs.blocks;      
INSERT INTO "jcs"."blocks" VALUES
('bk-1', 'bk-1', 'spoor 1', 1, 0, NULL, NULL, NULL, FALSE, 'Free', NULL, 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-2', 'bk-2', 'spoor 2', 3, 2, NULL, NULL, NULL, FALSE, 'Free', NULL, 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-3', 'bk-3', 'blok 3', 4, 5, NULL, NULL, 23, FALSE, 'Occupied', '-', 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-4', 'bk-4', 'blok 4', 6, 7, NULL, NULL, 39, FALSE, 'Occupied', '-', 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE);          
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."bloc_pk_idx" ON "jcs"."blocks"("id" NULLS FIRST);    
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."bloc_tile_idx" ON "jcs"."blocks"("tile_id" NULLS FIRST);             
CREATE CACHED TABLE "jcs"."sensors"(
    "id" INTEGER NOT NULL,
    "name" CHARACTER VARYING(255) NOT NULL,
    "device_id" INTEGER,
    "contact_id" INTEGER,
    "status" INTEGER,
    "previous_status" INTEGER,
    "millis" INTEGER,
    "last_updated" DATE,
    "node_id" INTEGER,
    "bus_nr" INTEGER DEFAULT 0 NOT NULL,
    "command_station_id" CHARACTER VARYING(255) NOT NULL
); 
ALTER TABLE "jcs"."sensors" ADD CONSTRAINT "jcs"."sens_pk" PRIMARY KEY("id");  
-- 16 +/- SELECT COUNT(*) FROM jcs.sensors;    
INSERT INTO "jcs"."sensors" VALUES
(0, 'M01-C01', 1, 1, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(1, 'M01-C02', 1, 2, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(2, 'M01-C03', 1, 3, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(3, 'M01-C04', 1, 4, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(4, 'M01-C05', 1, 5, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(5, 'M01-C06', 1, 6, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(6, 'M01-C07', 1, 7, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(7, 'M01-C08', 1, 8, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(8, 'M01-C09', 1, 9, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(9, 'M01-C10', 1, 10, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(10, 'M01-C11', 1, 11, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(11, 'M01-C12', 1, 12, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(12, 'M01-C13', 1, 13, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(13, 'M01-C14', 1, 14, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(14, 'M01-C15', 1, 15, 0, 0, NULL, NULL, 0, 0, 'virtual'),
(15, 'M01-C16', 1, 16, 0, 0, NULL, NULL, 0, 0, 'virtual');
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."sens_pk_idx" ON "jcs"."sensors"("id" NULLS FIRST);   
CREATE CACHED TABLE "jcs"."locomotive_functions"(
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1 RESTART WITH 16) NOT NULL,
    "locomotive_id" BIGINT NOT NULL,
    "f_number" INTEGER NOT NULL,
    "f_type" INTEGER NOT NULL,
    "f_value" INTEGER,
    "f_icon" CHARACTER VARYING(255),
    "momentary" BOOLEAN DEFAULT FALSE NOT NULL
);  
ALTER TABLE "jcs"."locomotive_functions" ADD CONSTRAINT "jcs"."lofu_pk" PRIMARY KEY("id");     
-- 15 +/- SELECT COUNT(*) FROM jcs.locomotive_functions;       
INSERT INTO "jcs"."locomotive_functions" VALUES
(1, 7, 0, 50, 0, NULL, FALSE),
(2, 7, 1, 51, 0, NULL, FALSE),
(3, 7, 2, 52, 0, NULL, FALSE),
(4, 7, 3, 53, 0, NULL, FALSE),
(5, 7, 4, 54, 0, NULL, FALSE),
(6, 23, 0, 1, 1, 'fkticon_a_001', FALSE),
(7, 23, 1, 51, 0, NULL, FALSE),
(8, 23, 2, 4, 0, 'fkticon_i_004', FALSE),
(9, 23, 3, 8, 0, 'fkticon_i_008', FALSE),
(10, 23, 4, 18, 0, 'fkticon_i_018', FALSE),
(11, 39, 0, 1, 1, 'fkticon_a_001', FALSE),
(12, 39, 1, 51, 0, NULL, FALSE),
(13, 39, 2, 52, 0, NULL, FALSE),
(14, 39, 3, 8, 0, 'fkticon_i_008', FALSE),
(15, 39, 4, 18, 0, 'fkticon_i_018', FALSE);       
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."lofu_pk_idx" ON "jcs"."locomotive_functions"("id" NULLS FIRST);      
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."lofu_loid_fnum_un_idx" ON "jcs"."locomotive_functions"("locomotive_id" NULLS FIRST, "f_number" NULLS FIRST);         
CREATE CACHED TABLE "jcs"."locomotives"(
    "id" BIGINT NOT NULL,
    "name" CHARACTER VARYING(255) NOT NULL,
    "uid" BIGINT,
    "address" INTEGER NOT NULL,
    "icon" CHARACTER VARYING(255),
    "decoder_type" CHARACTER VARYING(255) NOT NULL,
    "tacho_max" INTEGER,
    "v_min" INTEGER,
    "velocity" INTEGER,
    "synchronize" BOOLEAN DEFAULT FALSE NOT NULL,
    "imported" CHARACTER VARYING(255),
    "commuter" BOOLEAN DEFAULT FALSE NOT NULL,
    "show" BOOLEAN DEFAULT TRUE NOT NULL,
    "command_station_id" CHARACTER VARYING(255) NOT NULL,
    "dispatcher_direction" CHARACTER VARYING(255),
    "locomotive_direction" CHARACTER VARYING(255),
    "speed_1" INTEGER,
    "speed_2" INTEGER,
    "speed_3" INTEGER,
    "speed_4" INTEGER
);  
ALTER TABLE "jcs"."locomotives" ADD CONSTRAINT "jcs"."loco_pk" PRIMARY KEY("id");              
-- 3 +/- SELECT COUNT(*) FROM jcs.locomotives; 
INSERT INTO "jcs"."locomotives" VALUES
(7, 'NS DHG 6505', 8, 8, '/home/frans/jcs/cache/dcc-ex/ns dhg 6505.png', 'dcc', 100, 0, 0, FALSE, 'Manual Updated', TRUE, TRUE, 'virtual', NULL, 'BACKWARDS', NULL, NULL, NULL, NULL),
(23, 'BR 101 003-2', 23, 23, '/home/frans/jcs/cache/cs/db br 101 109-7.png', 'dcc', 200, 0, 0, FALSE, 'Manual Updated', TRUE, TRUE, 'virtual', NULL, 'FORWARDS', NULL, NULL, NULL, NULL),
(39, 'NS 1631', 39, 39, '/home/frans/jcs/cache/cs/ns 1652.png', 'dcc', 120, 0, 0, FALSE, 'Manual Updated', TRUE, TRUE, 'virtual', NULL, 'FORWARDS', NULL, NULL, NULL, NULL);           
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."loco_pk_idx" ON "jcs"."locomotives"("id" NULLS FIRST);               
CREATE CACHED TABLE "jcs"."command_stations"(
    "id" CHARACTER VARYING(255) NOT NULL,
    "description" CHARACTER VARYING(255) NOT NULL,
    "short_name" CHARACTER VARYING(255) NOT NULL,
    "class_name" CHARACTER VARYING(255) NOT NULL,
    "connect_via" CHARACTER VARYING(255) NOT NULL,
    "serial_port" CHARACTER VARYING(255),
    "ip_address" CHARACTER VARYING(255),
    "network_port" INTEGER,
    "ip_auto_conf" BOOLEAN DEFAULT FALSE NOT NULL,
    "supports_decoder_control" BOOLEAN DEFAULT TRUE NOT NULL,
    "supports_accessory_control" BOOLEAN DEFAULT TRUE NOT NULL,
    "supports_feedback" BOOLEAN DEFAULT TRUE NOT NULL,
    "supports_loco_synch" BOOLEAN DEFAULT FALSE NOT NULL,
    "supports_accessory_synch" BOOLEAN DEFAULT FALSE NOT NULL,
    "supports_loco_image_synch" BOOLEAN DEFAULT FALSE NOT NULL,
    "supports_loco_function_synch" BOOLEAN DEFAULT FALSE NOT NULL,
    "protocols" CHARACTER VARYING(255) DEFAULT 'DCC' NOT NULL,
    "default_cs" BOOLEAN DEFAULT FALSE NOT NULL,
    "enabled" BOOLEAN DEFAULT FALSE NOT NULL,
    "last_used_serial" CHARACTER VARYING(255),
    "sup_conn_types" CHARACTER VARYING(255) NOT NULL,
    "feedback_module_id" CHARACTER VARYING(255),
    "feedback_bus_count" INTEGER,
    "feedback_bus_0_module_count" INTEGER,
    "feedback_bus_1_module_count" INTEGER,
    "feedback_bus_2_module_count" INTEGER,
    "feedback_bus_3_module_count" INTEGER,
    "virtual" BOOLEAN DEFAULT FALSE NOT NULL
);         
ALTER TABLE "jcs"."command_stations" ADD CONSTRAINT "jcs"."command_station_pk" PRIMARY KEY("id");              
-- 5 +/- SELECT COUNT(*) FROM jcs.command_stations;            
INSERT INTO "jcs"."command_stations" VALUES
('marklin.cs', 'Marklin Central Station 2/3', 'CS', 'jcs.commandStation.marklin.cs.MarklinCentralStationImpl', 'NETWORK', NULL, NULL, 15731, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 'DCC,MFX,MM', FALSE, TRUE, NULL, 'NETWORK', NULL, NULL, NULL, NULL, NULL, NULL, FALSE),
('dcc-ex', 'DCC-EX', 'dcc-ex', 'jcs.commandStation.dccex.DccExCommandStationImpl', 'NETWORK', NULL, NULL, 2560, FALSE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, 'DCC', FALSE, FALSE, NULL, 'NETWORK,SERIAL', NULL, NULL, NULL, NULL, NULL, NULL, FALSE),
('hsi-s88', 'HSI S88', 'HSI', 'jcs.commandStation.hsis88.HSIImpl', 'SERIAL', NULL, NULL, 0, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, '', FALSE, TRUE, NULL, 'SERIAL', '0', 1, 6, 0, 0, 0, FALSE),
('virtual', 'Virtual CS', 'VIR', 'jcs.commandStation.virtual.VirtualCommandStationImpl', 'NETWORK', NULL, '127.0.0.1', 0, FALSE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, 'dcc', TRUE, TRUE, '1', 'NETWORK', '0', 1, 1, 0, 0, 0, TRUE),
('esu-ecos', 'ESU ECoS', 'ECoS', 'jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl', 'NETWORK', NULL, NULL, 15471, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 'DCC,MFX,MM', TRUE, TRUE, '1', 'NETWORK', '0', 0, 0, 0, 0, 0, FALSE);     
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."command_station_pk_idx" ON "jcs"."command_stations"("id" NULLS FIRST);               
CREATE CACHED TABLE "jcs"."route_elements"(
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1 RESTART WITH 481) NOT NULL,
    "route_id" CHARACTER VARYING(255) NOT NULL,
    "node_id" CHARACTER VARYING(255) NOT NULL,
    "tile_id" CHARACTER VARYING(255) NOT NULL,
    "accessory_value" CHARACTER VARYING(255),
    "order_seq" INTEGER DEFAULT 0 NOT NULL,
    "incoming_side" CHARACTER VARYING(255)
);    
ALTER TABLE "jcs"."route_elements" ADD CONSTRAINT "jcs"."roel_pk" PRIMARY KEY("id");           
-- 240 +/- SELECT COUNT(*) FROM jcs.route_elements;            
INSERT INTO "jcs"."route_elements" VALUES
(241, '[bk-3+]->[bk-1-]', 'bk-3', 'bk-3', NULL, 0, NULL),
(242, '[bk-3+]->[bk-1-]', 'bk-3+', 'bk-3', NULL, 1, NULL),
(243, '[bk-3+]->[bk-1-]', 'se-5', 'se-5', NULL, 2, 'East'),
(244, '[bk-3+]->[bk-1-]', 'st-9', 'st-9', NULL, 3, 'East'),
(245, '[bk-3+]->[bk-1-]', 'sw-4', 'sw-4', 'G', 4, 'East'),
(246, '[bk-3+]->[bk-1-]', 'st-14', 'st-14', NULL, 5, 'East'),
(247, '[bk-3+]->[bk-1-]', 'ct-7', 'ct-7', NULL, 6, 'East'),
(248, '[bk-3+]->[bk-1-]', 'st-7', 'st-7', NULL, 7, 'South'),
(249, '[bk-3+]->[bk-1-]', 'ct-3', 'ct-3', NULL, 8, 'South'),
(250, '[bk-3+]->[bk-1-]', 'st-5', 'st-5', NULL, 9, 'West'),
(251, '[bk-3+]->[bk-1-]', 'sw-1', 'sw-1', 'R', 10, 'West'),
(252, '[bk-3+]->[bk-1-]', 'ct-1', 'ct-1', NULL, 11, 'South'),
(253, '[bk-3+]->[bk-1-]', 'st-1', 'st-1', NULL, 12, 'West'),
(254, '[bk-3+]->[bk-1-]', 'se-1', 'se-1', NULL, 13, 'West'),
(255, '[bk-3+]->[bk-1-]', 'bk-1-', 'bk-1', NULL, 14, 'West'),
(256, '[bk-1-]->[bk-3+]', 'bk-1', 'bk-1', NULL, 0, NULL),
(257, '[bk-1-]->[bk-3+]', 'bk-1-', 'bk-1', NULL, 1, NULL),
(258, '[bk-1-]->[bk-3+]', 'se-1', 'se-1', NULL, 2, 'East'),
(259, '[bk-1-]->[bk-3+]', 'st-1', 'st-1', NULL, 3, 'East'),
(260, '[bk-1-]->[bk-3+]', 'ct-1', 'ct-1', NULL, 4, 'East'),
(261, '[bk-1-]->[bk-3+]', 'sw-1', 'sw-1', 'R', 5, 'North'),
(262, '[bk-1-]->[bk-3+]', 'st-5', 'st-5', NULL, 6, 'East'),
(263, '[bk-1-]->[bk-3+]', 'ct-3', 'ct-3', NULL, 7, 'East'),
(264, '[bk-1-]->[bk-3+]', 'st-7', 'st-7', NULL, 8, 'North'),
(265, '[bk-1-]->[bk-3+]', 'ct-7', 'ct-7', NULL, 9, 'North'),
(266, '[bk-1-]->[bk-3+]', 'st-14', 'st-14', NULL, 10, 'West'),
(267, '[bk-1-]->[bk-3+]', 'sw-4', 'sw-4', 'G', 11, 'West'),
(268, '[bk-1-]->[bk-3+]', 'st-9', 'st-9', NULL, 12, 'West'),
(269, '[bk-1-]->[bk-3+]', 'se-5', 'se-5', NULL, 13, 'West'),
(270, '[bk-1-]->[bk-3+]', 'bk-3+', 'bk-3', NULL, 14, 'West'),
(271, '[bk-3-]->[bk-2+]', 'bk-3', 'bk-3', NULL, 0, NULL),
(272, '[bk-3-]->[bk-2+]', 'bk-3-', 'bk-3', NULL, 1, NULL),
(273, '[bk-3-]->[bk-2+]', 'se-6', 'se-6', NULL, 2, 'West'),
(274, '[bk-3-]->[bk-2+]', 'st-12', 'st-12', NULL, 3, 'West'),
(275, '[bk-3-]->[bk-2+]', 'sw-3', 'sw-3', 'G', 4, 'West'),
(276, '[bk-3-]->[bk-2+]', 'st-13', 'st-13', NULL, 5, 'West'),
(277, '[bk-3-]->[bk-2+]', 'ct-8', 'ct-8', NULL, 6, 'West'),
(278, '[bk-3-]->[bk-2+]', 'st-8', 'st-8', NULL, 7, 'South'),
(279, '[bk-3-]->[bk-2+]', 'ct-4', 'ct-4', NULL, 8, 'South'),
(280, '[bk-3-]->[bk-2+]', 'st-6', 'st-6', NULL, 9, 'East'),
(281, '[bk-3-]->[bk-2+]', 'sw-2', 'sw-2', 'G', 10, 'East'),
(282, '[bk-3-]->[bk-2+]', 'st-4', 'st-4', NULL, 11, 'East'),
(283, '[bk-3-]->[bk-2+]', 'se-4', 'se-4', NULL, 12, 'East'),
(284, '[bk-3-]->[bk-2+]', 'bk-2+', 'bk-2', NULL, 13, 'East'),
(285, '[bk-1-]->[bk-4+]', 'bk-1', 'bk-1', NULL, 0, NULL),
(286, '[bk-1-]->[bk-4+]', 'bk-1-', 'bk-1', NULL, 1, NULL),
(287, '[bk-1-]->[bk-4+]', 'se-1', 'se-1', NULL, 2, 'East'),
(288, '[bk-1-]->[bk-4+]', 'st-1', 'st-1', NULL, 3, 'East'),
(289, '[bk-1-]->[bk-4+]', 'ct-1', 'ct-1', NULL, 4, 'East'),
(290, '[bk-1-]->[bk-4+]', 'sw-1', 'sw-1', 'R', 5, 'North'),
(291, '[bk-1-]->[bk-4+]', 'st-5', 'st-5', NULL, 6, 'East'),
(292, '[bk-1-]->[bk-4+]', 'ct-3', 'ct-3', NULL, 7, 'East'),
(293, '[bk-1-]->[bk-4+]', 'st-7', 'st-7', NULL, 8, 'North'),
(294, '[bk-1-]->[bk-4+]', 'ct-7', 'ct-7', NULL, 9, 'North'),
(295, '[bk-1-]->[bk-4+]', 'st-14', 'st-14', NULL, 10, 'West'),
(296, '[bk-1-]->[bk-4+]', 'sw-4', 'sw-4', 'R', 11, 'West'),
(297, '[bk-1-]->[bk-4+]', 'ct-5', 'ct-5', NULL, 12, 'North'),
(298, '[bk-1-]->[bk-4+]', 'st-10', 'st-10', NULL, 13, 'West'),
(299, '[bk-1-]->[bk-4+]', 'se-7', 'se-7', NULL, 14, 'West'),
(300, '[bk-1-]->[bk-4+]', 'bk-4+', 'bk-4', NULL, 15, 'West'),
(301, '[bk-3+]->[bk-2-]', 'bk-3', 'bk-3', NULL, 0, NULL),
(302, '[bk-3+]->[bk-2-]', 'bk-3+', 'bk-3', NULL, 1, NULL),
(303, '[bk-3+]->[bk-2-]', 'se-5', 'se-5', NULL, 2, 'East'),
(304, '[bk-3+]->[bk-2-]', 'st-9', 'st-9', NULL, 3, 'East'),
(305, '[bk-3+]->[bk-2-]', 'sw-4', 'sw-4', 'G', 4, 'East'),
(306, '[bk-3+]->[bk-2-]', 'st-14', 'st-14', NULL, 5, 'East'),
(307, '[bk-3+]->[bk-2-]', 'ct-7', 'ct-7', NULL, 6, 'East'),
(308, '[bk-3+]->[bk-2-]', 'st-7', 'st-7', NULL, 7, 'South');           
INSERT INTO "jcs"."route_elements" VALUES
(309, '[bk-3+]->[bk-2-]', 'ct-3', 'ct-3', NULL, 8, 'South'),
(310, '[bk-3+]->[bk-2-]', 'st-5', 'st-5', NULL, 9, 'West'),
(311, '[bk-3+]->[bk-2-]', 'sw-1', 'sw-1', 'G', 10, 'West'),
(312, '[bk-3+]->[bk-2-]', 'st-3', 'st-3', NULL, 11, 'West'),
(313, '[bk-3+]->[bk-2-]', 'se-3', 'se-3', NULL, 12, 'West'),
(314, '[bk-3+]->[bk-2-]', 'bk-2-', 'bk-2', NULL, 13, 'West'),
(315, '[bk-1+]->[bk-3-]', 'bk-1', 'bk-1', NULL, 0, NULL),
(316, '[bk-1+]->[bk-3-]', 'bk-1+', 'bk-1', NULL, 1, NULL),
(317, '[bk-1+]->[bk-3-]', 'se-2', 'se-2', NULL, 2, 'West'),
(318, '[bk-1+]->[bk-3-]', 'st-2', 'st-2', NULL, 3, 'West'),
(319, '[bk-1+]->[bk-3-]', 'ct-2', 'ct-2', NULL, 4, 'West'),
(320, '[bk-1+]->[bk-3-]', 'sw-2', 'sw-2', 'R', 5, 'North'),
(321, '[bk-1+]->[bk-3-]', 'st-6', 'st-6', NULL, 6, 'West'),
(322, '[bk-1+]->[bk-3-]', 'ct-4', 'ct-4', NULL, 7, 'West'),
(323, '[bk-1+]->[bk-3-]', 'st-8', 'st-8', NULL, 8, 'North'),
(324, '[bk-1+]->[bk-3-]', 'ct-8', 'ct-8', NULL, 9, 'North'),
(325, '[bk-1+]->[bk-3-]', 'st-13', 'st-13', NULL, 10, 'East'),
(326, '[bk-1+]->[bk-3-]', 'sw-3', 'sw-3', 'G', 11, 'East'),
(327, '[bk-1+]->[bk-3-]', 'st-12', 'st-12', NULL, 12, 'East'),
(328, '[bk-1+]->[bk-3-]', 'se-6', 'se-6', NULL, 13, 'East'),
(329, '[bk-1+]->[bk-3-]', 'bk-3-', 'bk-3', NULL, 14, 'East'),
(330, '[bk-4-]->[bk-1+]', 'bk-4', 'bk-4', NULL, 0, NULL),
(331, '[bk-4-]->[bk-1+]', 'bk-4-', 'bk-4', NULL, 1, NULL),
(332, '[bk-4-]->[bk-1+]', 'se-8', 'se-8', NULL, 2, 'West'),
(333, '[bk-4-]->[bk-1+]', 'st-11', 'st-11', NULL, 3, 'West'),
(334, '[bk-4-]->[bk-1+]', 'ct-6', 'ct-6', NULL, 4, 'West'),
(335, '[bk-4-]->[bk-1+]', 'sw-3', 'sw-3', 'R', 5, 'South'),
(336, '[bk-4-]->[bk-1+]', 'st-13', 'st-13', NULL, 6, 'West'),
(337, '[bk-4-]->[bk-1+]', 'ct-8', 'ct-8', NULL, 7, 'West'),
(338, '[bk-4-]->[bk-1+]', 'st-8', 'st-8', NULL, 8, 'South'),
(339, '[bk-4-]->[bk-1+]', 'ct-4', 'ct-4', NULL, 9, 'South'),
(340, '[bk-4-]->[bk-1+]', 'st-6', 'st-6', NULL, 10, 'East'),
(341, '[bk-4-]->[bk-1+]', 'sw-2', 'sw-2', 'R', 11, 'East'),
(342, '[bk-4-]->[bk-1+]', 'ct-2', 'ct-2', NULL, 12, 'South'),
(343, '[bk-4-]->[bk-1+]', 'st-2', 'st-2', NULL, 13, 'East'),
(344, '[bk-4-]->[bk-1+]', 'se-2', 'se-2', NULL, 14, 'East'),
(345, '[bk-4-]->[bk-1+]', 'bk-1+', 'bk-1', NULL, 15, 'East'),
(346, '[bk-4-]->[bk-2+]', 'bk-4', 'bk-4', NULL, 0, NULL),
(347, '[bk-4-]->[bk-2+]', 'bk-4-', 'bk-4', NULL, 1, NULL),
(348, '[bk-4-]->[bk-2+]', 'se-8', 'se-8', NULL, 2, 'West'),
(349, '[bk-4-]->[bk-2+]', 'st-11', 'st-11', NULL, 3, 'West'),
(350, '[bk-4-]->[bk-2+]', 'ct-6', 'ct-6', NULL, 4, 'West'),
(351, '[bk-4-]->[bk-2+]', 'sw-3', 'sw-3', 'R', 5, 'South'),
(352, '[bk-4-]->[bk-2+]', 'st-13', 'st-13', NULL, 6, 'West'),
(353, '[bk-4-]->[bk-2+]', 'ct-8', 'ct-8', NULL, 7, 'West'),
(354, '[bk-4-]->[bk-2+]', 'st-8', 'st-8', NULL, 8, 'South'),
(355, '[bk-4-]->[bk-2+]', 'ct-4', 'ct-4', NULL, 9, 'South'),
(356, '[bk-4-]->[bk-2+]', 'st-6', 'st-6', NULL, 10, 'East'),
(357, '[bk-4-]->[bk-2+]', 'sw-2', 'sw-2', 'G', 11, 'East'),
(358, '[bk-4-]->[bk-2+]', 'st-4', 'st-4', NULL, 12, 'East'),
(359, '[bk-4-]->[bk-2+]', 'se-4', 'se-4', NULL, 13, 'East'),
(360, '[bk-4-]->[bk-2+]', 'bk-2+', 'bk-2', NULL, 14, 'East'),
(361, '[bk-2-]->[bk-4+]', 'bk-2', 'bk-2', NULL, 0, NULL),
(362, '[bk-2-]->[bk-4+]', 'bk-2-', 'bk-2', NULL, 1, NULL),
(363, '[bk-2-]->[bk-4+]', 'se-3', 'se-3', NULL, 2, 'East'),
(364, '[bk-2-]->[bk-4+]', 'st-3', 'st-3', NULL, 3, 'East'),
(365, '[bk-2-]->[bk-4+]', 'sw-1', 'sw-1', 'G', 4, 'East'),
(366, '[bk-2-]->[bk-4+]', 'st-5', 'st-5', NULL, 5, 'East'),
(367, '[bk-2-]->[bk-4+]', 'ct-3', 'ct-3', NULL, 6, 'East'),
(368, '[bk-2-]->[bk-4+]', 'st-7', 'st-7', NULL, 7, 'North'),
(369, '[bk-2-]->[bk-4+]', 'ct-7', 'ct-7', NULL, 8, 'North'),
(370, '[bk-2-]->[bk-4+]', 'st-14', 'st-14', NULL, 9, 'West'),
(371, '[bk-2-]->[bk-4+]', 'sw-4', 'sw-4', 'R', 10, 'West'),
(372, '[bk-2-]->[bk-4+]', 'ct-5', 'ct-5', NULL, 11, 'North'),
(373, '[bk-2-]->[bk-4+]', 'st-10', 'st-10', NULL, 12, 'West'),
(374, '[bk-2-]->[bk-4+]', 'se-7', 'se-7', NULL, 13, 'West'),
(375, '[bk-2-]->[bk-4+]', 'bk-4+', 'bk-4', NULL, 14, 'West');           
INSERT INTO "jcs"."route_elements" VALUES
(376, '[bk-2-]->[bk-3+]', 'bk-2', 'bk-2', NULL, 0, NULL),
(377, '[bk-2-]->[bk-3+]', 'bk-2-', 'bk-2', NULL, 1, NULL),
(378, '[bk-2-]->[bk-3+]', 'se-3', 'se-3', NULL, 2, 'East'),
(379, '[bk-2-]->[bk-3+]', 'st-3', 'st-3', NULL, 3, 'East'),
(380, '[bk-2-]->[bk-3+]', 'sw-1', 'sw-1', 'G', 4, 'East'),
(381, '[bk-2-]->[bk-3+]', 'st-5', 'st-5', NULL, 5, 'East'),
(382, '[bk-2-]->[bk-3+]', 'ct-3', 'ct-3', NULL, 6, 'East'),
(383, '[bk-2-]->[bk-3+]', 'st-7', 'st-7', NULL, 7, 'North'),
(384, '[bk-2-]->[bk-3+]', 'ct-7', 'ct-7', NULL, 8, 'North'),
(385, '[bk-2-]->[bk-3+]', 'st-14', 'st-14', NULL, 9, 'West'),
(386, '[bk-2-]->[bk-3+]', 'sw-4', 'sw-4', 'G', 10, 'West'),
(387, '[bk-2-]->[bk-3+]', 'st-9', 'st-9', NULL, 11, 'West'),
(388, '[bk-2-]->[bk-3+]', 'se-5', 'se-5', NULL, 12, 'West'),
(389, '[bk-2-]->[bk-3+]', 'bk-3+', 'bk-3', NULL, 13, 'West'),
(390, '[bk-2+]->[bk-4-]', 'bk-2', 'bk-2', NULL, 0, NULL),
(391, '[bk-2+]->[bk-4-]', 'bk-2+', 'bk-2', NULL, 1, NULL),
(392, '[bk-2+]->[bk-4-]', 'se-4', 'se-4', NULL, 2, 'West'),
(393, '[bk-2+]->[bk-4-]', 'st-4', 'st-4', NULL, 3, 'West'),
(394, '[bk-2+]->[bk-4-]', 'sw-2', 'sw-2', 'G', 4, 'West'),
(395, '[bk-2+]->[bk-4-]', 'st-6', 'st-6', NULL, 5, 'West'),
(396, '[bk-2+]->[bk-4-]', 'ct-4', 'ct-4', NULL, 6, 'West'),
(397, '[bk-2+]->[bk-4-]', 'st-8', 'st-8', NULL, 7, 'North'),
(398, '[bk-2+]->[bk-4-]', 'ct-8', 'ct-8', NULL, 8, 'North'),
(399, '[bk-2+]->[bk-4-]', 'st-13', 'st-13', NULL, 9, 'East'),
(400, '[bk-2+]->[bk-4-]', 'sw-3', 'sw-3', 'R', 10, 'East'),
(401, '[bk-2+]->[bk-4-]', 'ct-6', 'ct-6', NULL, 11, 'North'),
(402, '[bk-2+]->[bk-4-]', 'st-11', 'st-11', NULL, 12, 'East'),
(403, '[bk-2+]->[bk-4-]', 'se-8', 'se-8', NULL, 13, 'East'),
(404, '[bk-2+]->[bk-4-]', 'bk-4-', 'bk-4', NULL, 14, 'East'),
(405, '[bk-4+]->[bk-2-]', 'bk-4', 'bk-4', NULL, 0, NULL),
(406, '[bk-4+]->[bk-2-]', 'bk-4+', 'bk-4', NULL, 1, NULL),
(407, '[bk-4+]->[bk-2-]', 'se-7', 'se-7', NULL, 2, 'East'),
(408, '[bk-4+]->[bk-2-]', 'st-10', 'st-10', NULL, 3, 'East'),
(409, '[bk-4+]->[bk-2-]', 'ct-5', 'ct-5', NULL, 4, 'East'),
(410, '[bk-4+]->[bk-2-]', 'sw-4', 'sw-4', 'R', 5, 'South'),
(411, '[bk-4+]->[bk-2-]', 'st-14', 'st-14', NULL, 6, 'East'),
(412, '[bk-4+]->[bk-2-]', 'ct-7', 'ct-7', NULL, 7, 'East'),
(413, '[bk-4+]->[bk-2-]', 'st-7', 'st-7', NULL, 8, 'South'),
(414, '[bk-4+]->[bk-2-]', 'ct-3', 'ct-3', NULL, 9, 'South'),
(415, '[bk-4+]->[bk-2-]', 'st-5', 'st-5', NULL, 10, 'West'),
(416, '[bk-4+]->[bk-2-]', 'sw-1', 'sw-1', 'G', 11, 'West'),
(417, '[bk-4+]->[bk-2-]', 'st-3', 'st-3', NULL, 12, 'West'),
(418, '[bk-4+]->[bk-2-]', 'se-3', 'se-3', NULL, 13, 'West'),
(419, '[bk-4+]->[bk-2-]', 'bk-2-', 'bk-2', NULL, 14, 'West'),
(420, '[bk-2+]->[bk-3-]', 'bk-2', 'bk-2', NULL, 0, NULL),
(421, '[bk-2+]->[bk-3-]', 'bk-2+', 'bk-2', NULL, 1, NULL),
(422, '[bk-2+]->[bk-3-]', 'se-4', 'se-4', NULL, 2, 'West'),
(423, '[bk-2+]->[bk-3-]', 'st-4', 'st-4', NULL, 3, 'West'),
(424, '[bk-2+]->[bk-3-]', 'sw-2', 'sw-2', 'G', 4, 'West'),
(425, '[bk-2+]->[bk-3-]', 'st-6', 'st-6', NULL, 5, 'West'),
(426, '[bk-2+]->[bk-3-]', 'ct-4', 'ct-4', NULL, 6, 'West'),
(427, '[bk-2+]->[bk-3-]', 'st-8', 'st-8', NULL, 7, 'North'),
(428, '[bk-2+]->[bk-3-]', 'ct-8', 'ct-8', NULL, 8, 'North'),
(429, '[bk-2+]->[bk-3-]', 'st-13', 'st-13', NULL, 9, 'East'),
(430, '[bk-2+]->[bk-3-]', 'sw-3', 'sw-3', 'G', 10, 'East'),
(431, '[bk-2+]->[bk-3-]', 'st-12', 'st-12', NULL, 11, 'East'),
(432, '[bk-2+]->[bk-3-]', 'se-6', 'se-6', NULL, 12, 'East'),
(433, '[bk-2+]->[bk-3-]', 'bk-3-', 'bk-3', NULL, 13, 'East'),
(434, '[bk-4+]->[bk-1-]', 'bk-4', 'bk-4', NULL, 0, NULL),
(435, '[bk-4+]->[bk-1-]', 'bk-4+', 'bk-4', NULL, 1, NULL),
(436, '[bk-4+]->[bk-1-]', 'se-7', 'se-7', NULL, 2, 'East'),
(437, '[bk-4+]->[bk-1-]', 'st-10', 'st-10', NULL, 3, 'East'),
(438, '[bk-4+]->[bk-1-]', 'ct-5', 'ct-5', NULL, 4, 'East'),
(439, '[bk-4+]->[bk-1-]', 'sw-4', 'sw-4', 'R', 5, 'South'),
(440, '[bk-4+]->[bk-1-]', 'st-14', 'st-14', NULL, 6, 'East'),
(441, '[bk-4+]->[bk-1-]', 'ct-7', 'ct-7', NULL, 7, 'East'),
(442, '[bk-4+]->[bk-1-]', 'st-7', 'st-7', NULL, 8, 'South'),
(443, '[bk-4+]->[bk-1-]', 'ct-3', 'ct-3', NULL, 9, 'South');         
INSERT INTO "jcs"."route_elements" VALUES
(444, '[bk-4+]->[bk-1-]', 'st-5', 'st-5', NULL, 10, 'West'),
(445, '[bk-4+]->[bk-1-]', 'sw-1', 'sw-1', 'R', 11, 'West'),
(446, '[bk-4+]->[bk-1-]', 'ct-1', 'ct-1', NULL, 12, 'South'),
(447, '[bk-4+]->[bk-1-]', 'st-1', 'st-1', NULL, 13, 'West'),
(448, '[bk-4+]->[bk-1-]', 'se-1', 'se-1', NULL, 14, 'West'),
(449, '[bk-4+]->[bk-1-]', 'bk-1-', 'bk-1', NULL, 15, 'West'),
(450, '[bk-1+]->[bk-4-]', 'bk-1', 'bk-1', NULL, 0, NULL),
(451, '[bk-1+]->[bk-4-]', 'bk-1+', 'bk-1', NULL, 1, NULL),
(452, '[bk-1+]->[bk-4-]', 'se-2', 'se-2', NULL, 2, 'West'),
(453, '[bk-1+]->[bk-4-]', 'st-2', 'st-2', NULL, 3, 'West'),
(454, '[bk-1+]->[bk-4-]', 'ct-2', 'ct-2', NULL, 4, 'West'),
(455, '[bk-1+]->[bk-4-]', 'sw-2', 'sw-2', 'R', 5, 'North'),
(456, '[bk-1+]->[bk-4-]', 'st-6', 'st-6', NULL, 6, 'West'),
(457, '[bk-1+]->[bk-4-]', 'ct-4', 'ct-4', NULL, 7, 'West'),
(458, '[bk-1+]->[bk-4-]', 'st-8', 'st-8', NULL, 8, 'North'),
(459, '[bk-1+]->[bk-4-]', 'ct-8', 'ct-8', NULL, 9, 'North'),
(460, '[bk-1+]->[bk-4-]', 'st-13', 'st-13', NULL, 10, 'East'),
(461, '[bk-1+]->[bk-4-]', 'sw-3', 'sw-3', 'R', 11, 'East'),
(462, '[bk-1+]->[bk-4-]', 'ct-6', 'ct-6', NULL, 12, 'North'),
(463, '[bk-1+]->[bk-4-]', 'st-11', 'st-11', NULL, 13, 'East'),
(464, '[bk-1+]->[bk-4-]', 'se-8', 'se-8', NULL, 14, 'East'),
(465, '[bk-1+]->[bk-4-]', 'bk-4-', 'bk-4', NULL, 15, 'East'),
(466, '[bk-3-]->[bk-1+]', 'bk-3', 'bk-3', NULL, 0, NULL),
(467, '[bk-3-]->[bk-1+]', 'bk-3-', 'bk-3', NULL, 1, NULL),
(468, '[bk-3-]->[bk-1+]', 'se-6', 'se-6', NULL, 2, 'West'),
(469, '[bk-3-]->[bk-1+]', 'st-12', 'st-12', NULL, 3, 'West'),
(470, '[bk-3-]->[bk-1+]', 'sw-3', 'sw-3', 'G', 4, 'West'),
(471, '[bk-3-]->[bk-1+]', 'st-13', 'st-13', NULL, 5, 'West'),
(472, '[bk-3-]->[bk-1+]', 'ct-8', 'ct-8', NULL, 6, 'West'),
(473, '[bk-3-]->[bk-1+]', 'st-8', 'st-8', NULL, 7, 'South'),
(474, '[bk-3-]->[bk-1+]', 'ct-4', 'ct-4', NULL, 8, 'South'),
(475, '[bk-3-]->[bk-1+]', 'st-6', 'st-6', NULL, 9, 'East'),
(476, '[bk-3-]->[bk-1+]', 'sw-2', 'sw-2', 'R', 10, 'East'),
(477, '[bk-3-]->[bk-1+]', 'ct-2', 'ct-2', NULL, 11, 'South'),
(478, '[bk-3-]->[bk-1+]', 'st-2', 'st-2', NULL, 12, 'East'),
(479, '[bk-3-]->[bk-1+]', 'se-2', 'se-2', NULL, 13, 'East'),
(480, '[bk-3-]->[bk-1+]', 'bk-1+', 'bk-1', NULL, 14, 'East'); 
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."roel_pk_idx" ON "jcs"."route_elements"("id" NULLS FIRST);            
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."roel_rout_node_tile_un_idx" ON "jcs"."route_elements"("route_id" NULLS FIRST, "node_id" NULLS FIRST, "tile_id" NULLS FIRST);         
CREATE CACHED TABLE "jcs"."jcs_properties"(
    "p_key" CHARACTER VARYING(255) NOT NULL,
    "p_value" CHARACTER VARYING(255) NOT NULL
);      
ALTER TABLE "jcs"."jcs_properties" ADD CONSTRAINT "jcs"."prop_pk" PRIMARY KEY("p_key");        
-- 3 +/- SELECT COUNT(*) FROM jcs.jcs_properties;              
INSERT INTO "jcs"."jcs_properties" VALUES
('jcs.version', '1.0.0'),
('jcs.db.version', '1.0.0'),
('default.switchtime', '500');
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."prop_pk_idx" ON "jcs"."jcs_properties"("p_key" NULLS FIRST);         
CREATE CACHED TABLE "jcs"."jcs_version"(
    "db_version" CHARACTER VARYING(255) NOT NULL,
    "app_version" CHARACTER VARYING(255) NOT NULL
);
-- 1 +/- SELECT COUNT(*) FROM jcs.jcs_version; 
INSERT INTO "jcs"."jcs_version" VALUES
('0.0.4', '0.0.4');     
CREATE CACHED TABLE "jcs"."routes"(
    "id" CHARACTER VARYING(255) NOT NULL,
    "from_tile_id" CHARACTER VARYING(255) NOT NULL,
    "from_suffix" CHARACTER VARYING(255) NOT NULL,
    "to_tile_id" CHARACTER VARYING(255) NOT NULL,
    "to_suffix" CHARACTER VARYING(255) NOT NULL,
    "route_color" CHARACTER VARYING(255),
    "locked" BOOLEAN DEFAULT FALSE NOT NULL,
    "status" CHARACTER VARYING(255)
);          
ALTER TABLE "jcs"."routes" ADD CONSTRAINT "jcs"."rout_pk" PRIMARY KEY("id");   
-- 16 +/- SELECT COUNT(*) FROM jcs.routes;     
INSERT INTO "jcs"."routes" VALUES
('[bk-3+]->[bk-1-]', 'bk-3', '+', 'bk-1', '-', NULL, FALSE, NULL),
('[bk-1-]->[bk-3+]', 'bk-1', '-', 'bk-3', '+', NULL, FALSE, NULL),
('[bk-3-]->[bk-2+]', 'bk-3', '-', 'bk-2', '+', NULL, FALSE, NULL),
('[bk-1-]->[bk-4+]', 'bk-1', '-', 'bk-4', '+', NULL, FALSE, NULL),
('[bk-3+]->[bk-2-]', 'bk-3', '+', 'bk-2', '-', NULL, FALSE, NULL),
('[bk-1+]->[bk-3-]', 'bk-1', '+', 'bk-3', '-', NULL, FALSE, NULL),
('[bk-4-]->[bk-1+]', 'bk-4', '-', 'bk-1', '+', NULL, FALSE, NULL),
('[bk-4-]->[bk-2+]', 'bk-4', '-', 'bk-2', '+', NULL, FALSE, NULL),
('[bk-2-]->[bk-4+]', 'bk-2', '-', 'bk-4', '+', NULL, FALSE, NULL),
('[bk-2-]->[bk-3+]', 'bk-2', '-', 'bk-3', '+', NULL, FALSE, NULL),
('[bk-2+]->[bk-4-]', 'bk-2', '+', 'bk-4', '-', NULL, FALSE, NULL),
('[bk-4+]->[bk-2-]', 'bk-4', '+', 'bk-2', '-', NULL, FALSE, NULL),
('[bk-2+]->[bk-3-]', 'bk-2', '+', 'bk-3', '-', NULL, FALSE, NULL),
('[bk-4+]->[bk-1-]', 'bk-4', '+', 'bk-1', '-', NULL, FALSE, NULL),
('[bk-1+]->[bk-4-]', 'bk-1', '+', 'bk-4', '-', NULL, FALSE, NULL),
('[bk-3-]->[bk-1+]', 'bk-3', '-', 'bk-1', '+', NULL, FALSE, NULL);              
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."rout_pk_idx" ON "jcs"."routes"("id" NULLS FIRST);    
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."rout_from_to_un_idx" ON "jcs"."routes"("from_tile_id" NULLS FIRST, "from_suffix" NULLS FIRST, "to_tile_id" NULLS FIRST, "to_suffix" NULLS FIRST);    
ALTER TABLE "jcs"."tiles" ADD CONSTRAINT "jcs"."tile_acce_sens_arc_ck" CHECK((("accessory_id" IS NOT NULL)
    AND ("sensor_id" IS NULL))
    OR (("accessory_id" IS NULL)
    AND (("sensor_id" IS NOT NULL)
    OR ("sensor_id" IS NULL)))) NOCHECK;         
ALTER TABLE "jcs"."sensors" ADD CONSTRAINT "jcs"."sens_deid_coid_un" UNIQUE NULLS DISTINCT ("device_id", "contact_id", "bus_nr", "command_station_id");        
ALTER TABLE "jcs"."route_elements" ADD CONSTRAINT "jcs"."roel_rout_node_tile_un" UNIQUE NULLS DISTINCT ("route_id", "node_id", "tile_id");     
ALTER TABLE "jcs"."blocks" ADD CONSTRAINT "jcs"."bloc_tile_un" UNIQUE NULLS DISTINCT ("tile_id");              
ALTER TABLE "jcs"."accessories" ADD CONSTRAINT "jcs"."acce_address_un" UNIQUE NULLS DISTINCT ("protocol", "address", "command_station_id");    
ALTER TABLE "jcs"."tiles" ADD CONSTRAINT "jcs"."tile_x_y_un" UNIQUE NULLS DISTINCT ("x", "y"); 
ALTER TABLE "jcs"."locomotive_functions" ADD CONSTRAINT "jcs"."lofu_loid_fnum_un" UNIQUE NULLS DISTINCT ("locomotive_id", "f_number");         
ALTER TABLE "jcs"."routes" ADD CONSTRAINT "jcs"."rout_from_to_un" UNIQUE NULLS DISTINCT ("from_tile_id", "from_suffix", "to_tile_id", "to_suffix");            
ALTER TABLE "jcs"."blocks" ADD CONSTRAINT "jcs"."blck_sens_plus_fk" FOREIGN KEY("plus_sensor_id") REFERENCES "jcs"."sensors"("id") NOCHECK;    
ALTER TABLE "jcs"."routes" ADD CONSTRAINT "jcs"."rout_tile_to_fk" FOREIGN KEY("to_tile_id") REFERENCES "jcs"."tiles"("id") NOCHECK;            
ALTER TABLE "jcs"."blocks" ADD CONSTRAINT "jcs"."blck_tile_fk" FOREIGN KEY("tile_id") REFERENCES "jcs"."tiles"("id") NOCHECK;  
ALTER TABLE "jcs"."accessories" ADD CONSTRAINT "jcs"."acce_cost_fk" FOREIGN KEY("command_station_id") REFERENCES "jcs"."command_stations"("id") NOCHECK;       
ALTER TABLE "jcs"."locomotive_functions" ADD CONSTRAINT "jcs"."lofu_loco_fk" FOREIGN KEY("locomotive_id") REFERENCES "jcs"."locomotives"("id") NOCHECK;        
ALTER TABLE "jcs"."station_blocks" ADD CONSTRAINT "jcs"."blck_stbl_fk" FOREIGN KEY("block_id") REFERENCES "jcs"."blocks"("id") NOCHECK;        
ALTER TABLE "jcs"."blocks" ADD CONSTRAINT "jcs"."blck_acce_sig_plus_fk" FOREIGN KEY("plus_signal_id") REFERENCES "jcs"."accessories"("id") NOCHECK;            
ALTER TABLE "jcs"."route_elements" ADD CONSTRAINT "jcs"."roel_tile_fk" FOREIGN KEY("tile_id") REFERENCES "jcs"."tiles"("id") NOCHECK;          
ALTER TABLE "jcs"."routes" ADD CONSTRAINT "jcs"."rout_tile_from_fk" FOREIGN KEY("from_tile_id") REFERENCES "jcs"."tiles"("id") NOCHECK;        
ALTER TABLE "jcs"."locomotives" ADD CONSTRAINT "jcs"."loco_cost_fk" FOREIGN KEY("command_station_id") REFERENCES "jcs"."command_stations"("id") NOCHECK;       
ALTER TABLE "jcs"."blocks" ADD CONSTRAINT "jcs"."blck_sens_min_fk" FOREIGN KEY("min_sensor_id") REFERENCES "jcs"."sensors"("id") NOCHECK;      
ALTER TABLE "jcs"."station_blocks" ADD CONSTRAINT "jcs"."stat_stbl_fk" FOREIGN KEY("station_id") REFERENCES "jcs"."stations"("id") NOCHECK;    
ALTER TABLE "jcs"."route_elements" ADD CONSTRAINT "jcs"."roel_rout_fk" FOREIGN KEY("route_id") REFERENCES "jcs"."routes"("id") NOCHECK;        
ALTER TABLE "jcs"."blocks" ADD CONSTRAINT "jcs"."blck_acce_sig_min_fk" FOREIGN KEY("min_signal_id") REFERENCES "jcs"."accessories"("id") NOCHECK;              
ALTER TABLE "jcs"."blocks" ADD CONSTRAINT "jcs"."blck_loco_fk" FOREIGN KEY("locomotive_id") REFERENCES "jcs"."locomotives"("id") NOCHECK;      
ALTER TABLE "jcs"."tiles" ADD CONSTRAINT "jcs"."tile_acc_fk" FOREIGN KEY("accessory_id") REFERENCES "jcs"."accessories"("id") NOCHECK;         
ALTER TABLE "jcs"."tiles" ADD CONSTRAINT "jcs"."tile_sens_fk" FOREIGN KEY("sensor_id") REFERENCES "jcs"."sensors"("id") NOCHECK;               
