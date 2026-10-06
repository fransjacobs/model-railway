-- H2 2.4.240; 
;              
CREATE USER IF NOT EXISTS "JCS" SALT 'fc40fdb9b1ff5d96' HASH '7442d719ccf651d1601a11f83df8e7b49fa1f589a7a3a9d04d69998e22249fcd' ADMIN;         
CREATE USER IF NOT EXISTS "SA" SALT 'eb1afca2ae28ad08' HASH 'f2c8c398ba306b98b4cb3ecfe0865559d3a221c6c0bc6aaeaef142c3f78b5915' ADMIN;          
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
DROP TABLE IF EXISTS "jcs"."routes" CASCADE;   
DROP TABLE IF EXISTS "jcs"."route_elements" CASCADE;           
DROP TABLE IF EXISTS "jcs"."jcs_properties" CASCADE;           
DROP TABLE IF EXISTS "jcs"."jcs_version" CASCADE;              
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
-- 80 +/- SELECT COUNT(*) FROM jcs.tiles;      
INSERT INTO "jcs"."tiles" VALUES
('st-1', 'Straight', 'East', 'Center', 140, 60, NULL, NULL, NULL),
('ct-1', 'Curved', 'East', 'Center', 60, 60, NULL, NULL, NULL),
('st-2', 'Straight', 'East', 'Center', 100, 60, NULL, NULL, NULL),
('sw-1', 'Switch', 'East', 'Right', 620, 380, 'NONE', '001', NULL),
('sw-2', 'Switch', 'West', 'Left', 620, 100, 'NONE', '003', NULL),
('sw-3', 'Switch', 'West', 'Right', 620, 340, 'NONE', '002', NULL),
('ct-2', 'Curved', 'East', 'Center', 100, 100, NULL, NULL, NULL),
('si-1', 'Signal', 'East', 'Center', 580, 380, 'HP01', '004', NULL),
('bk-1', 'Block', 'South', 'Center', 740, 220, NULL, NULL, NULL),
('se-2', 'Sensor', 'West', 'Center', 500, 60, NULL, NULL, 8),
('si-2', 'Signal', 'East', 'Center', 180, 380, 'HP01', '007', NULL),
('se-3', 'Sensor', 'South', 'Center', 100, 140, NULL, NULL, 5),
('si-3', 'Signal', 'West', 'Center', 660, 340, 'HP01', '015', NULL),
('se-4', 'Sensor', 'South', 'Center', 60, 140, NULL, NULL, 10),
('bk-2', 'Block', 'West', 'Center', 380, 340, NULL, NULL, NULL),
('bk-3', 'Block', 'North', 'Center', 100, 220, NULL, NULL, NULL),
('se-5', 'Sensor', 'North', 'Center', 60, 300, NULL, NULL, 11),
('se-6', 'Sensor', 'North', 'Center', 100, 300, NULL, NULL, 4),
('ct-3', 'Curved', 'North', 'Center', 60, 380, NULL, NULL, NULL),
('ct-4', 'Curved', 'North', 'Center', 100, 340, NULL, NULL, NULL),
('sw-5', 'Switch', 'East', 'Left', 620, 60, 'NONE', '012', NULL),
('si-4', 'Signal', 'West', 'Center', 700, 60, 'HP01', '005', NULL),
('st-3', 'Straight', 'East', 'Center', 660, 380, NULL, NULL, NULL),
('st-4', 'Straight', 'East', 'Center', 580, 100, NULL, NULL, NULL),
('st-5', 'Straight', 'East', 'Center', 660, 60, NULL, NULL, NULL),
('st-6', 'Straight', 'East', 'Center', 220, 100, NULL, NULL, NULL),
('st-8', 'Straight', 'East', 'Center', 140, 340, NULL, NULL, NULL),
('st-9', 'Straight', 'East', 'Center', 220, 380, NULL, NULL, NULL),
('si-5', 'Signal', 'East', 'Center', 540, 100, 'HP01', '014', NULL),
('bk-4', 'Block', 'East', 'Center', 380, 100, NULL, NULL, NULL),
('se-8', 'Sensor', 'East', 'Center', 460, 100, NULL, NULL, 7),
('st-12', 'Straight', 'East', 'Center', 540, 380, NULL, NULL, NULL),
('si-6', 'Signal', 'West', 'Center', 180, 340, 'HP01', '013', NULL),
('st-14', 'Straight', 'East', 'Center', 300, 60, NULL, NULL, NULL),
('st-15', 'Straight', 'East', 'Center', 220, 60, NULL, NULL, NULL),
('st-16', 'Straight', 'East', 'Center', 540, 60, NULL, NULL, NULL),
('se-9', 'Sensor', 'East', 'Center', 300, 340, NULL, NULL, 3),
('bk-5', 'Block', 'North', 'Center', 780, 220, NULL, NULL, NULL),
('bk-6', 'Block', 'East', 'Center', 420, 380, NULL, NULL, NULL),
('st-17', 'Straight', 'East', 'Center', 260, 380, NULL, NULL, NULL),
('st-18', 'Straight', 'East', 'Center', 220, 340, NULL, NULL, NULL),
('st-19', 'Straight', 'East', 'Center', 140, 380, NULL, NULL, NULL),
('st-20', 'Straight', 'East', 'Center', 100, 380, NULL, NULL, NULL),
('se-10', 'Sensor', 'East', 'Center', 340, 380, NULL, NULL, 12),
('se-11', 'Sensor', 'East', 'Center', 500, 380, NULL, NULL, 13),
('se-12', 'Sensor', 'East', 'Center', 460, 340, NULL, NULL, 2),
('si-7', 'Signal', 'West', 'Center', 180, 60, 'HP01', '006', NULL),
('si-8', 'Signal', 'East', 'Center', 180, 100, 'HP01', '016', NULL),
('st-23', 'Straight', 'West', 'Center', 300, 380, NULL, NULL, NULL),
('st-24', 'Straight', 'West', 'Center', 260, 100, NULL, NULL, NULL),
('st-25', 'Straight', 'West', 'Center', 740, 380, NULL, NULL, NULL),
('ct-5', 'Curved', 'South', 'Center', 740, 100, NULL, NULL, NULL),
('se-13', 'Sensor', 'South', 'Center', 740, 140, NULL, NULL, 0),
('se-14', 'Sensor', 'South', 'Center', 740, 300, NULL, NULL, 1),
('bk-7', 'Block', 'South', 'Center', 60, 220, NULL, NULL, NULL),
('ct-6', 'Curved', 'South', 'Center', 780, 60, NULL, NULL, NULL),
('ct-7', 'Curved', 'West', 'Center', 740, 340, NULL, NULL, NULL),
('ct-8', 'Curved', 'West', 'Center', 780, 380, NULL, NULL, NULL),
('se-15', 'Sensor', 'North', 'Center', 780, 140, NULL, NULL, 15),
('se-16', 'Sensor', 'North', 'Center', 780, 300, NULL, NULL, 14),
('bk-8', 'Block', 'West', 'Center', 420, 60, NULL, NULL, NULL);             
INSERT INTO "jcs"."tiles" VALUES
('st-27', 'Straight', 'East', 'Center', 500, 340, NULL, NULL, NULL),
('st-28', 'Straight', 'South', 'Center', 60, 100, NULL, NULL, NULL),
('st-29', 'Straight', 'South', 'Center', 60, 340, NULL, NULL, NULL),
('st-30', 'Straight', 'South', 'Center', 780, 100, NULL, NULL, NULL),
('st-31', 'Straight', 'South', 'Center', 780, 340, NULL, NULL, NULL),
('st-32', 'Straight', 'West', 'Center', 260, 340, NULL, NULL, NULL),
('st-34', 'Straight', 'West', 'Center', 140, 100, NULL, NULL, NULL),
('st-35', 'Straight', 'West', 'Center', 580, 340, NULL, NULL, NULL),
('st-36', 'Straight', 'West', 'Center', 700, 340, NULL, NULL, NULL),
('si-9', 'Signal', 'West', 'Center', 660, 100, 'HP01', '010', NULL),
('si-10', 'Signal', 'West', 'Center', 700, 380, 'HP01', '008', NULL),
('si-11', 'Signal', 'East', 'Center', 580, 60, 'HP01', '011', NULL),
('si-12', 'Signal', 'East', 'Center', 540, 340, 'HP01', '009', NULL),
('st-37', 'Straight', 'East', 'Center', 500, 100, NULL, NULL, NULL),
('st-38', 'Straight', 'East', 'Center', 260, 60, NULL, NULL, NULL),
('st-39', 'Straight', 'East', 'Center', 700, 100, NULL, NULL, NULL),
('st-40', 'Straight', 'East', 'Center', 740, 60, NULL, NULL, NULL),
('se-17', 'Sensor', 'East', 'Center', 340, 60, NULL, NULL, 9),
('se-18', 'Sensor', 'East', 'Center', 300, 100, NULL, NULL, 6);         
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
-- 0 +/- SELECT COUNT(*) FROM jcs.stations;    
CREATE CACHED TABLE "jcs"."station_blocks"(
    "id" CHARACTER VARYING(255) NOT NULL,
    "station_id" CHARACTER VARYING(255) NOT NULL,
    "block_id" CHARACTER VARYING(255) NOT NULL,
    "last_updated" TIMESTAMP
);        
ALTER TABLE "jcs"."station_blocks" ADD CONSTRAINT "jcs"."stbl_pk" PRIMARY KEY("id");           
-- 0 +/- SELECT COUNT(*) FROM jcs.station_blocks;              
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
-- 16 +/- SELECT COUNT(*) FROM jcs.accessories;
INSERT INTO "jcs"."accessories" VALUES
('001', 49, 'W49', 'rechtsweiche', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('002', 50, 'W50', 'linksweiche', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('003', 51, 'W51', 'rechtsweiche', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('004', 45, 'S45', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('005', 48, 'S48', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('006', 44, 'S44', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('007', 41, 'S41', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('008', 58, 'S58', 'lichtsignal_HP01', 1, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('009', 57, 'S57', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('010', 59, 'S59', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('011', 60, 'S60', 'lichtsignal_HP01', 1, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('012', 52, 'W52', 'linksweiche', 1, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('013', 42, 'S42', 'lichtsignal_HP01', 1, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('014', 47, 'S47', 'lichtsignal_HP01', 1, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('015', 46, 'S46', 'lichtsignal_HP01', 1, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('016', 43, 'S43', 'lichtsignal_HP01', 1, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL);           
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."acce_address_un_idx" ON "jcs"."accessories"("address" NULLS FIRST, "protocol" NULLS FIRST, "command_station_id" NULLS FIRST);        
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."acce_pk_idx" ON "jcs"."accessories"("id" NULLS FIRST);               
CREATE CACHED TABLE "jcs"."blocks"(
    "id" CHARACTER VARYING(255) SELECTIVITY 100 NOT NULL,
    "tile_id" CHARACTER VARYING(255) SELECTIVITY 100 NOT NULL,
    "description" CHARACTER VARYING(255) SELECTIVITY 100,
    "plus_sensor_id" INTEGER SELECTIVITY 100,
    "min_sensor_id" INTEGER SELECTIVITY 100,
    "plus_signal_id" CHARACTER VARYING(255) SELECTIVITY 100,
    "min_signal_id" CHARACTER VARYING(255) SELECTIVITY 62,
    "locomotive_id" BIGINT SELECTIVITY 37,
    "allow_non_commuter_only" BOOLEAN DEFAULT FALSE SELECTIVITY 25 NOT NULL,
    "status" CHARACTER VARYING(255) SELECTIVITY 62,
    "incoming_suffix" CHARACTER VARYING(255) SELECTIVITY 37,
    "min_wait_time" INTEGER DEFAULT 10 SELECTIVITY 25 NOT NULL,
    "max_wait_time" INTEGER SELECTIVITY 12,
    "random_wait" BOOLEAN DEFAULT FALSE SELECTIVITY 12 NOT NULL,
    "always_stop" BOOLEAN DEFAULT FALSE SELECTIVITY 25 NOT NULL,
    "allow_commuter_only" BOOLEAN DEFAULT FALSE SELECTIVITY 25 NOT NULL,
    "logical_direction" CHARACTER VARYING(255) SELECTIVITY 37,
    "allow_direction_change" BOOLEAN DEFAULT TRUE SELECTIVITY 12 NOT NULL
);          
ALTER TABLE "jcs"."blocks" ADD CONSTRAINT "jcs"."bloc_pk" PRIMARY KEY("id");   
-- 8 +/- SELECT COUNT(*) FROM jcs.blocks;      
INSERT INTO "jcs"."blocks" VALUES
('bk-1', 'bk-1', 'Blok 1', 1, 0, '015', '010', NULL, TRUE, 'Free', NULL, 2, NULL, FALSE, FALSE, TRUE, NULL, FALSE),
('bk-2', 'bk-2', 'Blok 2', 3, 2, '013', '009', NULL, TRUE, 'Free', NULL, 1, NULL, FALSE, TRUE, TRUE, NULL, FALSE),
('bk-3', 'bk-3', 'Blok 3', 5, 4, '016', NULL, NULL, TRUE, 'Free', NULL, 2, NULL, FALSE, FALSE, TRUE, NULL, FALSE),
('bk-4', 'bk-4', 'Blok 4', 7, 6, '014', NULL, NULL, FALSE, 'Free', NULL, 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-5', 'bk-5', 'Blok 5', 15, 14, '005', '008', NULL, TRUE, 'Free', NULL, 2, NULL, FALSE, FALSE, TRUE, NULL, FALSE),
('bk-6', 'bk-6', 'Blok 6', 13, 12, '004', NULL, 103, TRUE, 'Occupied', '-', 10, NULL, FALSE, TRUE, TRUE, 'Forwards', FALSE),
('bk-7', 'bk-7', 'Blok 7', 11, 10, '007', NULL, NULL, TRUE, 'Free', NULL, 2, NULL, FALSE, FALSE, TRUE, NULL, FALSE),
('bk-8', 'bk-8', 'Blok 8', 9, 8, '006', '011', NULL, TRUE, 'Free', NULL, 1, NULL, FALSE, TRUE, TRUE, NULL, FALSE);    
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."bloc_pk_idx" ON "jcs"."blocks"("id" NULLS FIRST);    
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."bloc_tile_idx" ON "jcs"."blocks"("tile_id" NULLS FIRST);             
CREATE CACHED TABLE "jcs"."sensors"(
    "id" INTEGER SELECTIVITY 100 NOT NULL,
    "name" CHARACTER VARYING(255) SELECTIVITY 100 NOT NULL,
    "device_id" INTEGER SELECTIVITY 12,
    "contact_id" INTEGER SELECTIVITY 100,
    "status" INTEGER SELECTIVITY 12,
    "previous_status" INTEGER SELECTIVITY 12,
    "millis" INTEGER SELECTIVITY 6,
    "last_updated" DATE SELECTIVITY 6,
    "node_id" INTEGER SELECTIVITY 6,
    "bus_nr" INTEGER DEFAULT 0 SELECTIVITY 6 NOT NULL,
    "command_station_id" CHARACTER VARYING(255) SELECTIVITY 6 NOT NULL
);              
ALTER TABLE "jcs"."sensors" ADD CONSTRAINT "jcs"."sens_pk" PRIMARY KEY("id");  
-- 16 +/- SELECT COUNT(*) FROM jcs.sensors;    
INSERT INTO "jcs"."sensors" VALUES
(0, 'M01-C01', 1, 1, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(1, 'M01-C02', 1, 2, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(2, 'M01-C03', 1, 3, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(3, 'M01-C04', 1, 4, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(4, 'M01-C05', 1, 5, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(5, 'M01-C06', 1, 6, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(6, 'M01-C07', 1, 7, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(7, 'M01-C08', 1, 8, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(8, 'M01-C09', 1, 9, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(9, 'M01-C10', 1, 10, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(10, 'M01-C11', 1, 11, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(11, 'M01-C12', 1, 12, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(12, 'M01-C13', 1, 13, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(13, 'M01-C14', 1, 14, 1, 0, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(14, 'M01-C15', 1, 15, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2'),
(15, 'M01-C16', 1, 16, 0, 1, NULL, DATE '2026-10-06', 0, 0, 'intellibox2');
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."sens_pk_idx" ON "jcs"."sensors"("id" NULLS FIRST);   
CREATE CACHED TABLE "jcs"."locomotive_functions"(
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1 RESTART WITH 18) SELECTIVITY 100 NOT NULL,
    "locomotive_id" BIGINT SELECTIVITY 11 NOT NULL,
    "f_number" INTEGER SELECTIVITY 52 NOT NULL,
    "f_type" INTEGER SELECTIVITY 52 NOT NULL,
    "f_value" INTEGER SELECTIVITY 11,
    "f_icon" CHARACTER VARYING(255) SELECTIVITY 5,
    "momentary" BOOLEAN DEFAULT FALSE SELECTIVITY 5 NOT NULL
);          
ALTER TABLE "jcs"."locomotive_functions" ADD CONSTRAINT "jcs"."lofu_pk" PRIMARY KEY("id");     
-- 17 +/- SELECT COUNT(*) FROM jcs.locomotive_functions;       
INSERT INTO "jcs"."locomotive_functions" VALUES
(1, 1205, 0, 50, 1, NULL, FALSE),
(2, 1205, 1, 51, 1, NULL, FALSE),
(3, 1205, 2, 52, 0, NULL, FALSE),
(4, 1205, 3, 53, 0, NULL, FALSE),
(5, 1205, 4, 54, 0, NULL, FALSE),
(6, 1205, 5, 55, 0, NULL, FALSE),
(7, 1205, 6, 56, 0, NULL, FALSE),
(8, 1205, 7, 57, 0, NULL, FALSE),
(9, 1205, 8, 58, 0, NULL, FALSE),
(10, 103, 0, 50, 1, NULL, FALSE),
(11, 103, 1, 51, 0, NULL, FALSE),
(12, 103, 2, 52, 0, NULL, FALSE),
(13, 103, 3, 53, 0, NULL, FALSE),
(14, 103, 4, 54, 0, NULL, FALSE),
(15, 103, 5, 55, 0, NULL, FALSE),
(16, 103, 6, 56, 0, NULL, FALSE),
(17, 103, 7, 57, 0, NULL, FALSE);              
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
-- 2 +/- SELECT COUNT(*) FROM jcs.locomotives; 
INSERT INTO "jcs"."locomotives" VALUES
(103, 'BR 216 059-6', 72, 72, '/Users/fransjacobs/jcs/cache/cs/db br 216 059-6.png', 'mm', 100, 0, 0, FALSE, 'Manual Updated', FALSE, TRUE, 'intellibox2', NULL, 'FORWARDS', 30, 50, 70, 90),
(1205, 'NS 1205', 1205, 1205, '/Users/fransjacobs/jcs/cache/cs/ns 1211.png', 'dcc', 100, 0, 0, FALSE, 'Manual Updated', FALSE, TRUE, 'intellibox2', NULL, 'FORWARDS', 40, 50, 70, 90);    
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
-- 6 +/- SELECT COUNT(*) FROM jcs.command_stations;            
INSERT INTO "jcs"."command_stations" VALUES
('marklin.cs', 'Marklin Central Station 2/3', 'CS', 'jcs.commandStation.marklin.cs.MarklinCentralStationImpl', 'NETWORK', NULL, NULL, 15731, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 'DCC,MFX,MM', FALSE, FALSE, NULL, 'NETWORK', NULL, NULL, NULL, NULL, NULL, NULL, FALSE),
('dcc-ex', 'DCC-EX', 'dcc-ex', 'jcs.commandStation.dccex.DccExCommandStationImpl', 'NETWORK', NULL, NULL, 2560, FALSE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, 'DCC', FALSE, FALSE, NULL, 'NETWORK,SERIAL', NULL, NULL, NULL, NULL, NULL, NULL, FALSE),
('hsi-s88', 'HSI S88', 'HSI', 'jcs.commandStation.hsis88.HSIImpl', 'SERIAL', NULL, NULL, 0, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, '', FALSE, FALSE, NULL, 'SERIAL', '0', 1, 6, 0, 0, 0, FALSE),
('virtual', 'Virtual CS', 'VIR', 'jcs.commandStation.virtual.VirtualCommandStationImpl', 'NETWORK', NULL, '127.0.0.1', 0, FALSE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, 'dcc', FALSE, FALSE, '1', 'NETWORK', '0', 1, 1, 0, 0, 0, TRUE),
('esu-ecos', 'ESU ECoS', 'ECoS', 'jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl', 'NETWORK', NULL, NULL, 15471, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 'DCC,MFX,MM', FALSE, FALSE, '1', 'NETWORK', '0', 0, 0, 0, 0, 0, FALSE),
('intellibox2', 'Uhlenbrock Intellibox 2', 'Loconet', 'jcs.commandStation.loconet.Intellibox2Impl', 'SERIAL', NULL, 'AUTO', NULL, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, 'DCC,MM', TRUE, TRUE, '1', 'SERIAL', '0', 0, 0, 0, 0, 0, FALSE);        
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."command_station_pk_idx" ON "jcs"."command_stations"("id" NULLS FIRST);               
CREATE CACHED TABLE "jcs"."routes"(
    "id" CHARACTER VARYING(255) NOT NULL,
    "from_tile_id" CHARACTER VARYING(255) NOT NULL,
    "from_suffix" CHARACTER VARYING(255) NOT NULL,
    "to_tile_id" CHARACTER VARYING(255) NOT NULL,
    "to_suffix" CHARACTER VARYING(255) NOT NULL,
    "route_color" CHARACTER VARYING(255),
    "locked" BOOLEAN DEFAULT FALSE NOT NULL,
    "status" CHARACTER VARYING(255),
    "departure_signal_value" CHARACTER VARYING(255)
);     
ALTER TABLE "jcs"."routes" ADD CONSTRAINT "jcs"."rout_pk" PRIMARY KEY("id");   
-- 20 +/- SELECT COUNT(*) FROM jcs.routes;     
INSERT INTO "jcs"."routes" VALUES
('[bk-5+]->[bk-4+]', 'bk-5', '+', 'bk-4', '+', NULL, FALSE, NULL, NULL),
('[bk-8-]->[bk-5+]', 'bk-8', '-', 'bk-5', '+', NULL, FALSE, NULL, NULL),
('[bk-3-]->[bk-2+]', 'bk-3', '-', 'bk-2', '+', NULL, FALSE, NULL, NULL),
('[bk-3+]->[bk-4-]', 'bk-3', '+', 'bk-4', '-', NULL, FALSE, NULL, NULL),
('[bk-1-]->[bk-4+]', 'bk-1', '-', 'bk-4', '+', NULL, FALSE, NULL, NULL),
('[bk-1+]->[bk-2-]', 'bk-1', '+', 'bk-2', '-', NULL, FALSE, NULL, NULL),
('[bk-7-]->[bk-8+]', 'bk-7', '-', 'bk-8', '+', NULL, FALSE, NULL, NULL),
('[bk-5-]->[bk-6+]', 'bk-5', '-', 'bk-6', '+', NULL, FALSE, NULL, NULL),
('[bk-6+]->[bk-5-]', 'bk-6', '+', 'bk-5', '-', NULL, FALSE, NULL, NULL),
('[bk-4+]->[bk-5+]', 'bk-4', '+', 'bk-5', '+', NULL, FALSE, NULL, NULL),
('[bk-8+]->[bk-7-]', 'bk-8', '+', 'bk-7', '-', NULL, FALSE, NULL, NULL),
('[bk-2-]->[bk-5-]', 'bk-2', '-', 'bk-5', '-', NULL, FALSE, NULL, NULL),
('[bk-2-]->[bk-1+]', 'bk-2', '-', 'bk-1', '+', NULL, FALSE, NULL, NULL),
('[bk-5-]->[bk-2-]', 'bk-5', '-', 'bk-2', '-', NULL, FALSE, NULL, NULL),
('[bk-4-]->[bk-3+]', 'bk-4', '-', 'bk-3', '+', NULL, FALSE, NULL, NULL),
('[bk-2+]->[bk-3-]', 'bk-2', '+', 'bk-3', '-', NULL, FALSE, NULL, NULL),
('[bk-4+]->[bk-1-]', 'bk-4', '+', 'bk-1', '-', NULL, FALSE, NULL, NULL),
('[bk-6-]->[bk-7+]', 'bk-6', '-', 'bk-7', '+', NULL, FALSE, NULL, NULL),
('[bk-7+]->[bk-6-]', 'bk-7', '+', 'bk-6', '-', NULL, FALSE, NULL, NULL),
('[bk-5+]->[bk-8-]', 'bk-5', '+', 'bk-8', '-', NULL, FALSE, NULL, NULL);          
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."rout_pk_idx" ON "jcs"."routes"("id" NULLS FIRST);    
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."rout_from_to_un_idx" ON "jcs"."routes"("from_tile_id" NULLS FIRST, "from_suffix" NULLS FIRST, "to_tile_id" NULLS FIRST, "to_suffix" NULLS FIRST);    
CREATE CACHED TABLE "jcs"."route_elements"(
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1 RESTART WITH 7565) SELECTIVITY 100 NOT NULL,
    "route_id" CHARACTER VARYING(255) SELECTIVITY 8 NOT NULL,
    "node_id" CHARACTER VARYING(255) SELECTIVITY 38 NOT NULL,
    "tile_id" CHARACTER VARYING(255) SELECTIVITY 32 NOT NULL,
    "accessory_value" CHARACTER VARYING(255) SELECTIVITY 1,
    "order_seq" INTEGER DEFAULT 0 SELECTIVITY 6 NOT NULL,
    "incoming_side" CHARACTER VARYING(255) SELECTIVITY 2
);             
ALTER TABLE "jcs"."route_elements" ADD CONSTRAINT "jcs"."roel_pk" PRIMARY KEY("id");           
-- 252 +/- SELECT COUNT(*) FROM jcs.route_elements;            
INSERT INTO "jcs"."route_elements" VALUES
(5925, '[bk-3+]->[bk-4-]', 'bk-3', 'bk-3', NULL, 0, NULL),
(5926, '[bk-3+]->[bk-4-]', 'bk-3+', 'bk-3', NULL, 1, NULL),
(5927, '[bk-3+]->[bk-4-]', 'se-3', 'se-3', NULL, 2, 'South'),
(5928, '[bk-3+]->[bk-4-]', 'ct-2', 'ct-2', NULL, 3, 'South'),
(5929, '[bk-3+]->[bk-4-]', 'st-34', 'st-34', NULL, 4, 'West'),
(5930, '[bk-3+]->[bk-4-]', 'si-8', 'si-8', NULL, 5, 'West'),
(5931, '[bk-3+]->[bk-4-]', 'st-6', 'st-6', NULL, 6, 'West'),
(5932, '[bk-3+]->[bk-4-]', 'st-24', 'st-24', NULL, 7, 'West'),
(5933, '[bk-3+]->[bk-4-]', 'se-18', 'se-18', NULL, 8, 'West'),
(5934, '[bk-3+]->[bk-4-]', 'bk-4-', 'bk-4', NULL, 9, 'West'),
(5947, '[bk-1+]->[bk-2-]', 'bk-1', 'bk-1', NULL, 0, NULL),
(5948, '[bk-1+]->[bk-2-]', 'bk-1+', 'bk-1', NULL, 1, NULL),
(5949, '[bk-1+]->[bk-2-]', 'se-14', 'se-14', NULL, 2, 'North'),
(5950, '[bk-1+]->[bk-2-]', 'ct-7', 'ct-7', NULL, 3, 'North'),
(5951, '[bk-1+]->[bk-2-]', 'st-36', 'st-36', NULL, 4, 'East'),
(5952, '[bk-1+]->[bk-2-]', 'si-3', 'si-3', NULL, 5, 'East'),
(5953, '[bk-1+]->[bk-2-]', 'sw-3', 'sw-3', 'G', 6, 'East'),
(5954, '[bk-1+]->[bk-2-]', 'st-35', 'st-35', NULL, 7, 'East'),
(5955, '[bk-1+]->[bk-2-]', 'si-12', 'si-12', NULL, 8, 'East'),
(5956, '[bk-1+]->[bk-2-]', 'st-27', 'st-27', NULL, 9, 'East'),
(5957, '[bk-1+]->[bk-2-]', 'se-12', 'se-12', NULL, 10, 'East'),
(5958, '[bk-1+]->[bk-2-]', 'bk-2-', 'bk-2', NULL, 11, 'East'),
(5959, '[bk-7-]->[bk-8+]', 'bk-7', 'bk-7', NULL, 0, NULL),
(5960, '[bk-7-]->[bk-8+]', 'bk-7-', 'bk-7', NULL, 1, NULL),
(5961, '[bk-7-]->[bk-8+]', 'se-4', 'se-4', NULL, 2, 'South'),
(5962, '[bk-7-]->[bk-8+]', 'st-28', 'st-28', NULL, 3, 'South'),
(5963, '[bk-7-]->[bk-8+]', 'ct-1', 'ct-1', NULL, 4, 'South'),
(5964, '[bk-7-]->[bk-8+]', 'st-2', 'st-2', NULL, 5, 'West'),
(5965, '[bk-7-]->[bk-8+]', 'st-1', 'st-1', NULL, 6, 'West'),
(5966, '[bk-7-]->[bk-8+]', 'si-7', 'si-7', NULL, 7, 'West'),
(5967, '[bk-7-]->[bk-8+]', 'st-15', 'st-15', NULL, 8, 'West'),
(5968, '[bk-7-]->[bk-8+]', 'st-38', 'st-38', NULL, 9, 'West'),
(5969, '[bk-7-]->[bk-8+]', 'st-14', 'st-14', NULL, 10, 'West'),
(5970, '[bk-7-]->[bk-8+]', 'se-17', 'se-17', NULL, 11, 'West'),
(5971, '[bk-7-]->[bk-8+]', 'bk-8+', 'bk-8', NULL, 12, 'West'),
(5972, '[bk-5-]->[bk-6+]', 'bk-5', 'bk-5', NULL, 0, NULL),
(5973, '[bk-5-]->[bk-6+]', 'bk-5-', 'bk-5', NULL, 1, NULL),
(5974, '[bk-5-]->[bk-6+]', 'se-16', 'se-16', NULL, 2, 'North'),
(5975, '[bk-5-]->[bk-6+]', 'st-31', 'st-31', NULL, 3, 'North'),
(5976, '[bk-5-]->[bk-6+]', 'ct-8', 'ct-8', NULL, 4, 'North'),
(5977, '[bk-5-]->[bk-6+]', 'st-25', 'st-25', NULL, 5, 'East'),
(5978, '[bk-5-]->[bk-6+]', 'si-10', 'si-10', NULL, 6, 'East'),
(5979, '[bk-5-]->[bk-6+]', 'st-3', 'st-3', NULL, 7, 'East'),
(5980, '[bk-5-]->[bk-6+]', 'sw-1', 'sw-1', 'G', 8, 'East'),
(5981, '[bk-5-]->[bk-6+]', 'si-1', 'si-1', NULL, 9, 'East'),
(5982, '[bk-5-]->[bk-6+]', 'st-12', 'st-12', NULL, 10, 'East'),
(5983, '[bk-5-]->[bk-6+]', 'se-11', 'se-11', NULL, 11, 'East'),
(5984, '[bk-5-]->[bk-6+]', 'bk-6+', 'bk-6', NULL, 12, 'East'),
(5998, '[bk-4+]->[bk-5+]', 'bk-4', 'bk-4', NULL, 0, NULL),
(5999, '[bk-4+]->[bk-5+]', 'bk-4+', 'bk-4', NULL, 1, NULL),
(6000, '[bk-4+]->[bk-5+]', 'se-8', 'se-8', NULL, 2, 'West'),
(6001, '[bk-4+]->[bk-5+]', 'st-37', 'st-37', NULL, 3, 'West'),
(6002, '[bk-4+]->[bk-5+]', 'si-5', 'si-5', NULL, 4, 'West'),
(6003, '[bk-4+]->[bk-5+]', 'st-4', 'st-4', NULL, 5, 'West'),
(6004, '[bk-4+]->[bk-5+]', 'sw-2', 'sw-2', 'R', 6, 'West'),
(6005, '[bk-4+]->[bk-5+]', 'sw-5', 'sw-5', 'R', 7, 'South'),
(6006, '[bk-4+]->[bk-5+]', 'st-5', 'st-5', NULL, 8, 'West'),
(6007, '[bk-4+]->[bk-5+]', 'si-4', 'si-4', NULL, 9, 'West'),
(6008, '[bk-4+]->[bk-5+]', 'st-40', 'st-40', NULL, 10, 'West'),
(6009, '[bk-4+]->[bk-5+]', 'ct-6', 'ct-6', NULL, 11, 'West'),
(6010, '[bk-4+]->[bk-5+]', 'st-30', 'st-30', NULL, 12, 'North'),
(6011, '[bk-4+]->[bk-5+]', 'se-15', 'se-15', NULL, 13, 'North'),
(6012, '[bk-4+]->[bk-5+]', 'bk-5+', 'bk-5', NULL, 14, 'North'),
(6053, '[bk-5-]->[bk-2-]', 'bk-5', 'bk-5', NULL, 0, NULL),
(6054, '[bk-5-]->[bk-2-]', 'bk-5-', 'bk-5', NULL, 1, NULL),
(6055, '[bk-5-]->[bk-2-]', 'se-16', 'se-16', NULL, 2, 'North');               
INSERT INTO "jcs"."route_elements" VALUES
(6056, '[bk-5-]->[bk-2-]', 'st-31', 'st-31', NULL, 3, 'North'),
(6057, '[bk-5-]->[bk-2-]', 'ct-8', 'ct-8', NULL, 4, 'North'),
(6058, '[bk-5-]->[bk-2-]', 'st-25', 'st-25', NULL, 5, 'East'),
(6059, '[bk-5-]->[bk-2-]', 'si-10', 'si-10', NULL, 6, 'East'),
(6060, '[bk-5-]->[bk-2-]', 'st-3', 'st-3', NULL, 7, 'East'),
(6061, '[bk-5-]->[bk-2-]', 'sw-1', 'sw-1', 'R', 8, 'East'),
(6062, '[bk-5-]->[bk-2-]', 'sw-3', 'sw-3', 'R', 9, 'South'),
(6063, '[bk-5-]->[bk-2-]', 'st-35', 'st-35', NULL, 10, 'East'),
(6064, '[bk-5-]->[bk-2-]', 'si-12', 'si-12', NULL, 11, 'East'),
(6065, '[bk-5-]->[bk-2-]', 'st-27', 'st-27', NULL, 12, 'East'),
(6066, '[bk-5-]->[bk-2-]', 'se-12', 'se-12', NULL, 13, 'East'),
(6067, '[bk-5-]->[bk-2-]', 'bk-2-', 'bk-2', NULL, 14, 'East'),
(6078, '[bk-2+]->[bk-3-]', 'bk-2', 'bk-2', NULL, 0, NULL),
(6079, '[bk-2+]->[bk-3-]', 'bk-2+', 'bk-2', NULL, 1, NULL),
(6080, '[bk-2+]->[bk-3-]', 'se-9', 'se-9', NULL, 2, 'East'),
(6081, '[bk-2+]->[bk-3-]', 'st-32', 'st-32', NULL, 3, 'East'),
(6082, '[bk-2+]->[bk-3-]', 'st-18', 'st-18', NULL, 4, 'East'),
(6083, '[bk-2+]->[bk-3-]', 'si-6', 'si-6', NULL, 5, 'East'),
(6084, '[bk-2+]->[bk-3-]', 'st-8', 'st-8', NULL, 6, 'East'),
(6085, '[bk-2+]->[bk-3-]', 'ct-4', 'ct-4', NULL, 7, 'East'),
(6086, '[bk-2+]->[bk-3-]', 'se-6', 'se-6', NULL, 8, 'South'),
(6087, '[bk-2+]->[bk-3-]', 'bk-3-', 'bk-3', NULL, 9, 'South'),
(6088, '[bk-4+]->[bk-1-]', 'bk-4', 'bk-4', NULL, 0, NULL),
(6089, '[bk-4+]->[bk-1-]', 'bk-4+', 'bk-4', NULL, 1, NULL),
(6090, '[bk-4+]->[bk-1-]', 'se-8', 'se-8', NULL, 2, 'West'),
(6091, '[bk-4+]->[bk-1-]', 'st-37', 'st-37', NULL, 3, 'West'),
(6092, '[bk-4+]->[bk-1-]', 'si-5', 'si-5', NULL, 4, 'West'),
(6093, '[bk-4+]->[bk-1-]', 'st-4', 'st-4', NULL, 5, 'West'),
(6094, '[bk-4+]->[bk-1-]', 'sw-2', 'sw-2', 'G', 6, 'West'),
(6095, '[bk-4+]->[bk-1-]', 'si-9', 'si-9', NULL, 7, 'West'),
(6096, '[bk-4+]->[bk-1-]', 'st-39', 'st-39', NULL, 8, 'West'),
(6097, '[bk-4+]->[bk-1-]', 'ct-5', 'ct-5', NULL, 9, 'West'),
(6098, '[bk-4+]->[bk-1-]', 'se-13', 'se-13', NULL, 10, 'North'),
(6099, '[bk-4+]->[bk-1-]', 'bk-1-', 'bk-1', NULL, 11, 'North'),
(6100, '[bk-6-]->[bk-7+]', 'bk-6', 'bk-6', NULL, 0, NULL),
(6101, '[bk-6-]->[bk-7+]', 'bk-6-', 'bk-6', NULL, 1, NULL),
(6102, '[bk-6-]->[bk-7+]', 'se-10', 'se-10', NULL, 2, 'East'),
(6103, '[bk-6-]->[bk-7+]', 'st-23', 'st-23', NULL, 3, 'East'),
(6104, '[bk-6-]->[bk-7+]', 'st-17', 'st-17', NULL, 4, 'East'),
(6105, '[bk-6-]->[bk-7+]', 'st-9', 'st-9', NULL, 5, 'East'),
(6106, '[bk-6-]->[bk-7+]', 'si-2', 'si-2', NULL, 6, 'East'),
(6107, '[bk-6-]->[bk-7+]', 'st-19', 'st-19', NULL, 7, 'East'),
(6108, '[bk-6-]->[bk-7+]', 'st-20', 'st-20', NULL, 8, 'East'),
(6109, '[bk-6-]->[bk-7+]', 'ct-3', 'ct-3', NULL, 9, 'East'),
(6110, '[bk-6-]->[bk-7+]', 'st-29', 'st-29', NULL, 10, 'South'),
(6111, '[bk-6-]->[bk-7+]', 'se-5', 'se-5', NULL, 11, 'South'),
(6112, '[bk-6-]->[bk-7+]', 'bk-7+', 'bk-7', NULL, 12, 'South'),
(6152, '[bk-8-]->[bk-5+]', 'bk-8', 'bk-8', NULL, 0, NULL),
(6153, '[bk-8-]->[bk-5+]', 'bk-8-', 'bk-8', NULL, 1, NULL),
(6154, '[bk-8-]->[bk-5+]', 'se-2', 'se-2', NULL, 2, 'West'),
(6155, '[bk-8-]->[bk-5+]', 'st-16', 'st-16', NULL, 3, 'West'),
(6156, '[bk-8-]->[bk-5+]', 'si-11', 'si-11', NULL, 4, 'West'),
(6157, '[bk-8-]->[bk-5+]', 'sw-5', 'sw-5', 'G', 5, 'West'),
(6158, '[bk-8-]->[bk-5+]', 'st-5', 'st-5', NULL, 6, 'West'),
(6159, '[bk-8-]->[bk-5+]', 'si-4', 'si-4', NULL, 7, 'West'),
(6160, '[bk-8-]->[bk-5+]', 'st-40', 'st-40', NULL, 8, 'West'),
(6161, '[bk-8-]->[bk-5+]', 'ct-6', 'ct-6', NULL, 9, 'West'),
(6162, '[bk-8-]->[bk-5+]', 'st-30', 'st-30', NULL, 10, 'North'),
(6163, '[bk-8-]->[bk-5+]', 'se-15', 'se-15', NULL, 11, 'North'),
(6164, '[bk-8-]->[bk-5+]', 'bk-5+', 'bk-5', NULL, 12, 'North'),
(7118, '[bk-6+]->[bk-5-]', 'bk-6', 'bk-6', NULL, 0, NULL),
(7119, '[bk-6+]->[bk-5-]', 'bk-6+', 'bk-6', NULL, 1, NULL),
(7120, '[bk-6+]->[bk-5-]', 'se-11', 'se-11', NULL, 2, 'West'),
(7121, '[bk-6+]->[bk-5-]', 'st-12', 'st-12', NULL, 3, 'West'),
(7122, '[bk-6+]->[bk-5-]', 'si-1', 'si-1', NULL, 4, 'West'),
(7123, '[bk-6+]->[bk-5-]', 'sw-1', 'sw-1', 'G', 5, 'West');               
INSERT INTO "jcs"."route_elements" VALUES
(7124, '[bk-6+]->[bk-5-]', 'st-3', 'st-3', NULL, 6, 'West'),
(7125, '[bk-6+]->[bk-5-]', 'si-10', 'si-10', NULL, 7, 'West'),
(7126, '[bk-6+]->[bk-5-]', 'st-25', 'st-25', NULL, 8, 'West'),
(7127, '[bk-6+]->[bk-5-]', 'ct-8', 'ct-8', NULL, 9, 'West'),
(7128, '[bk-6+]->[bk-5-]', 'st-31', 'st-31', NULL, 10, 'South'),
(7129, '[bk-6+]->[bk-5-]', 'se-16', 'se-16', NULL, 11, 'South'),
(7130, '[bk-6+]->[bk-5-]', 'bk-5-', 'bk-5', NULL, 12, 'South'),
(7232, '[bk-2-]->[bk-1+]', 'bk-2', 'bk-2', NULL, 0, NULL),
(7233, '[bk-2-]->[bk-1+]', 'bk-2-', 'bk-2', NULL, 1, NULL),
(7234, '[bk-2-]->[bk-1+]', 'se-12', 'se-12', NULL, 2, 'West'),
(7235, '[bk-2-]->[bk-1+]', 'st-27', 'st-27', NULL, 3, 'West'),
(7236, '[bk-2-]->[bk-1+]', 'si-12', 'si-12', NULL, 4, 'West'),
(7237, '[bk-2-]->[bk-1+]', 'st-35', 'st-35', NULL, 5, 'West'),
(7238, '[bk-2-]->[bk-1+]', 'sw-3', 'sw-3', 'G', 6, 'West'),
(7239, '[bk-2-]->[bk-1+]', 'si-3', 'si-3', NULL, 7, 'West'),
(7240, '[bk-2-]->[bk-1+]', 'st-36', 'st-36', NULL, 8, 'West'),
(7241, '[bk-2-]->[bk-1+]', 'ct-7', 'ct-7', NULL, 9, 'West'),
(7242, '[bk-2-]->[bk-1+]', 'se-14', 'se-14', NULL, 10, 'South'),
(7243, '[bk-2-]->[bk-1+]', 'bk-1+', 'bk-1', NULL, 11, 'South'),
(7244, '[bk-1-]->[bk-4+]', 'bk-1', 'bk-1', NULL, 0, NULL),
(7245, '[bk-1-]->[bk-4+]', 'bk-1-', 'bk-1', NULL, 1, NULL),
(7246, '[bk-1-]->[bk-4+]', 'se-13', 'se-13', NULL, 2, 'South'),
(7247, '[bk-1-]->[bk-4+]', 'ct-5', 'ct-5', NULL, 3, 'South'),
(7248, '[bk-1-]->[bk-4+]', 'st-39', 'st-39', NULL, 4, 'East'),
(7249, '[bk-1-]->[bk-4+]', 'si-9', 'si-9', NULL, 5, 'East'),
(7250, '[bk-1-]->[bk-4+]', 'sw-2', 'sw-2', 'G', 6, 'East'),
(7251, '[bk-1-]->[bk-4+]', 'st-4', 'st-4', NULL, 7, 'East'),
(7252, '[bk-1-]->[bk-4+]', 'si-5', 'si-5', NULL, 8, 'East'),
(7253, '[bk-1-]->[bk-4+]', 'st-37', 'st-37', NULL, 9, 'East'),
(7254, '[bk-1-]->[bk-4+]', 'se-8', 'se-8', NULL, 10, 'East'),
(7255, '[bk-1-]->[bk-4+]', 'bk-4+', 'bk-4', NULL, 11, 'East'),
(7366, '[bk-5+]->[bk-4+]', 'bk-5', 'bk-5', NULL, 0, NULL),
(7367, '[bk-5+]->[bk-4+]', 'bk-5+', 'bk-5', NULL, 1, NULL),
(7368, '[bk-5+]->[bk-4+]', 'se-15', 'se-15', NULL, 2, 'South'),
(7369, '[bk-5+]->[bk-4+]', 'st-30', 'st-30', NULL, 3, 'South'),
(7370, '[bk-5+]->[bk-4+]', 'ct-6', 'ct-6', NULL, 4, 'South'),
(7371, '[bk-5+]->[bk-4+]', 'st-40', 'st-40', NULL, 5, 'East'),
(7372, '[bk-5+]->[bk-4+]', 'si-4', 'si-4', NULL, 6, 'East'),
(7373, '[bk-5+]->[bk-4+]', 'st-5', 'st-5', NULL, 7, 'East'),
(7374, '[bk-5+]->[bk-4+]', 'sw-5', 'sw-5', 'R', 8, 'East'),
(7375, '[bk-5+]->[bk-4+]', 'sw-2', 'sw-2', 'R', 9, 'North'),
(7376, '[bk-5+]->[bk-4+]', 'st-4', 'st-4', NULL, 10, 'East'),
(7377, '[bk-5+]->[bk-4+]', 'si-5', 'si-5', NULL, 11, 'East'),
(7378, '[bk-5+]->[bk-4+]', 'st-37', 'st-37', NULL, 12, 'East'),
(7379, '[bk-5+]->[bk-4+]', 'se-8', 'se-8', NULL, 13, 'East'),
(7380, '[bk-5+]->[bk-4+]', 'bk-4+', 'bk-4', NULL, 14, 'East'),
(7411, '[bk-4-]->[bk-3+]', 'bk-4', 'bk-4', NULL, 0, NULL),
(7412, '[bk-4-]->[bk-3+]', 'bk-4-', 'bk-4', NULL, 1, NULL),
(7413, '[bk-4-]->[bk-3+]', 'se-18', 'se-18', NULL, 2, 'East'),
(7414, '[bk-4-]->[bk-3+]', 'st-24', 'st-24', NULL, 3, 'East'),
(7415, '[bk-4-]->[bk-3+]', 'st-6', 'st-6', NULL, 4, 'East'),
(7416, '[bk-4-]->[bk-3+]', 'si-8', 'si-8', NULL, 5, 'East'),
(7417, '[bk-4-]->[bk-3+]', 'st-34', 'st-34', NULL, 6, 'East'),
(7418, '[bk-4-]->[bk-3+]', 'ct-2', 'ct-2', NULL, 7, 'East'),
(7419, '[bk-4-]->[bk-3+]', 'se-3', 'se-3', NULL, 8, 'North'),
(7420, '[bk-4-]->[bk-3+]', 'bk-3+', 'bk-3', NULL, 9, 'North'),
(7421, '[bk-3-]->[bk-2+]', 'bk-3', 'bk-3', NULL, 0, NULL),
(7422, '[bk-3-]->[bk-2+]', 'bk-3-', 'bk-3', NULL, 1, NULL),
(7423, '[bk-3-]->[bk-2+]', 'se-6', 'se-6', NULL, 2, 'North'),
(7424, '[bk-3-]->[bk-2+]', 'ct-4', 'ct-4', NULL, 3, 'North'),
(7425, '[bk-3-]->[bk-2+]', 'st-8', 'st-8', NULL, 4, 'West'),
(7426, '[bk-3-]->[bk-2+]', 'si-6', 'si-6', NULL, 5, 'West'),
(7427, '[bk-3-]->[bk-2+]', 'st-18', 'st-18', NULL, 6, 'West'),
(7428, '[bk-3-]->[bk-2+]', 'st-32', 'st-32', NULL, 7, 'West'),
(7429, '[bk-3-]->[bk-2+]', 'se-9', 'se-9', NULL, 8, 'West'),
(7430, '[bk-3-]->[bk-2+]', 'bk-2+', 'bk-2', NULL, 9, 'West');  
INSERT INTO "jcs"."route_elements" VALUES
(7472, '[bk-2-]->[bk-5-]', 'bk-2', 'bk-2', NULL, 0, NULL),
(7473, '[bk-2-]->[bk-5-]', 'bk-2-', 'bk-2', NULL, 1, NULL),
(7474, '[bk-2-]->[bk-5-]', 'se-12', 'se-12', NULL, 2, 'West'),
(7475, '[bk-2-]->[bk-5-]', 'st-27', 'st-27', NULL, 3, 'West'),
(7476, '[bk-2-]->[bk-5-]', 'si-12', 'si-12', NULL, 4, 'West'),
(7477, '[bk-2-]->[bk-5-]', 'st-35', 'st-35', NULL, 5, 'West'),
(7478, '[bk-2-]->[bk-5-]', 'sw-3', 'sw-3', 'R', 6, 'West'),
(7479, '[bk-2-]->[bk-5-]', 'sw-1', 'sw-1', 'R', 7, 'North'),
(7480, '[bk-2-]->[bk-5-]', 'st-3', 'st-3', NULL, 8, 'West'),
(7481, '[bk-2-]->[bk-5-]', 'si-10', 'si-10', NULL, 9, 'West'),
(7482, '[bk-2-]->[bk-5-]', 'st-25', 'st-25', NULL, 10, 'West'),
(7483, '[bk-2-]->[bk-5-]', 'ct-8', 'ct-8', NULL, 11, 'West'),
(7484, '[bk-2-]->[bk-5-]', 'st-31', 'st-31', NULL, 12, 'South'),
(7485, '[bk-2-]->[bk-5-]', 'se-16', 'se-16', NULL, 13, 'South'),
(7486, '[bk-2-]->[bk-5-]', 'bk-5-', 'bk-5', NULL, 14, 'South'),
(7487, '[bk-5+]->[bk-8-]', 'bk-5', 'bk-5', NULL, 0, NULL),
(7488, '[bk-5+]->[bk-8-]', 'bk-5+', 'bk-5', NULL, 1, NULL),
(7489, '[bk-5+]->[bk-8-]', 'se-15', 'se-15', NULL, 2, 'South'),
(7490, '[bk-5+]->[bk-8-]', 'st-30', 'st-30', NULL, 3, 'South'),
(7491, '[bk-5+]->[bk-8-]', 'ct-6', 'ct-6', NULL, 4, 'South'),
(7492, '[bk-5+]->[bk-8-]', 'st-40', 'st-40', NULL, 5, 'East'),
(7493, '[bk-5+]->[bk-8-]', 'si-4', 'si-4', NULL, 6, 'East'),
(7494, '[bk-5+]->[bk-8-]', 'st-5', 'st-5', NULL, 7, 'East'),
(7495, '[bk-5+]->[bk-8-]', 'sw-5', 'sw-5', 'G', 8, 'East'),
(7496, '[bk-5+]->[bk-8-]', 'si-11', 'si-11', NULL, 9, 'East'),
(7497, '[bk-5+]->[bk-8-]', 'st-16', 'st-16', NULL, 10, 'East'),
(7498, '[bk-5+]->[bk-8-]', 'se-2', 'se-2', NULL, 11, 'East'),
(7499, '[bk-5+]->[bk-8-]', 'bk-8-', 'bk-8', NULL, 12, 'East'),
(7539, '[bk-8+]->[bk-7-]', 'bk-8', 'bk-8', NULL, 0, NULL),
(7540, '[bk-8+]->[bk-7-]', 'bk-8+', 'bk-8', NULL, 1, NULL),
(7541, '[bk-8+]->[bk-7-]', 'se-17', 'se-17', NULL, 2, 'East'),
(7542, '[bk-8+]->[bk-7-]', 'st-14', 'st-14', NULL, 3, 'East'),
(7543, '[bk-8+]->[bk-7-]', 'st-38', 'st-38', NULL, 4, 'East'),
(7544, '[bk-8+]->[bk-7-]', 'st-15', 'st-15', NULL, 5, 'East'),
(7545, '[bk-8+]->[bk-7-]', 'si-7', 'si-7', NULL, 6, 'East'),
(7546, '[bk-8+]->[bk-7-]', 'st-1', 'st-1', NULL, 7, 'East'),
(7547, '[bk-8+]->[bk-7-]', 'st-2', 'st-2', NULL, 8, 'East'),
(7548, '[bk-8+]->[bk-7-]', 'ct-1', 'ct-1', NULL, 9, 'East'),
(7549, '[bk-8+]->[bk-7-]', 'st-28', 'st-28', NULL, 10, 'North'),
(7550, '[bk-8+]->[bk-7-]', 'se-4', 'se-4', NULL, 11, 'North'),
(7551, '[bk-8+]->[bk-7-]', 'bk-7-', 'bk-7', NULL, 12, 'North'),
(7552, '[bk-7+]->[bk-6-]', 'bk-7', 'bk-7', NULL, 0, NULL),
(7553, '[bk-7+]->[bk-6-]', 'bk-7+', 'bk-7', NULL, 1, NULL),
(7554, '[bk-7+]->[bk-6-]', 'se-5', 'se-5', NULL, 2, 'North'),
(7555, '[bk-7+]->[bk-6-]', 'st-29', 'st-29', NULL, 3, 'North'),
(7556, '[bk-7+]->[bk-6-]', 'ct-3', 'ct-3', NULL, 4, 'North'),
(7557, '[bk-7+]->[bk-6-]', 'st-20', 'st-20', NULL, 5, 'West'),
(7558, '[bk-7+]->[bk-6-]', 'st-19', 'st-19', NULL, 6, 'West'),
(7559, '[bk-7+]->[bk-6-]', 'si-2', 'si-2', NULL, 7, 'West'),
(7560, '[bk-7+]->[bk-6-]', 'st-9', 'st-9', NULL, 8, 'West'),
(7561, '[bk-7+]->[bk-6-]', 'st-17', 'st-17', NULL, 9, 'West'),
(7562, '[bk-7+]->[bk-6-]', 'st-23', 'st-23', NULL, 10, 'West'),
(7563, '[bk-7+]->[bk-6-]', 'se-10', 'se-10', NULL, 11, 'West'),
(7564, '[bk-7+]->[bk-6-]', 'bk-6-', 'bk-6', NULL, 12, 'West');        
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
