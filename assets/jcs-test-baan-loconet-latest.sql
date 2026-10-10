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
-- 60 +/- SELECT COUNT(*) FROM jcs.tiles;      
INSERT INTO "jcs"."tiles" VALUES
('st-1', 'Straight', 'East', 'Center', 380, 60, NULL, NULL, NULL),
('bk-1', 'Block', 'South', 'Center', 540, 220, NULL, NULL, NULL),
('bk-2', 'Block', 'West', 'Center', 260, 340, NULL, NULL, NULL),
('bk-3', 'Block', 'North', 'Center', 100, 220, NULL, NULL, NULL),
('bk-4', 'Block', 'East', 'Center', 260, 100, NULL, NULL, NULL),
('ct-1', 'Curved', 'East', 'Center', 100, 100, NULL, NULL, NULL),
('ct-2', 'Curved', 'South', 'Center', 540, 100, NULL, NULL, NULL),
('ct-3', 'Curved', 'West', 'Center', 540, 340, NULL, NULL, NULL),
('ct-4', 'Curved', 'North', 'Center', 100, 340, NULL, NULL, NULL),
('se-1', 'Sensor', 'South', 'Center', 540, 140, NULL, NULL, 1),
('se-2', 'Sensor', 'South', 'Center', 540, 300, NULL, NULL, 2),
('se-3', 'Sensor', 'West', 'Center', 340, 340, NULL, NULL, 3),
('se-4', 'Sensor', 'West', 'Center', 180, 340, NULL, NULL, 4),
('se-5', 'Sensor', 'North', 'Center', 100, 300, NULL, NULL, 5),
('se-6', 'Sensor', 'North', 'Center', 100, 140, NULL, NULL, 6),
('se-7', 'Sensor', 'East', 'Center', 180, 100, NULL, NULL, 7),
('se-8', 'Sensor', 'East', 'Center', 340, 100, NULL, NULL, 8),
('sw-1', 'Switch', 'West', 'Left', 460, 100, 'NONE', '011', NULL),
('sw-2', 'Switch', 'East', 'Left', 460, 60, 'NONE', '012', NULL),
('si-1', 'Signal', 'West', 'Center', 140, 340, 'HP01', '002', NULL),
('si-3', 'Signal', 'East', 'Center', 140, 100, 'HP01', '003', NULL),
('si-4', 'Signal', 'East', 'Center', 380, 100, 'HP01', '007', NULL),
('si-5', 'Signal', 'East', 'Center', 420, 60, 'HP01', '016', NULL),
('si-6', 'Signal', 'West', 'Center', 140, 60, 'HP01', '004', NULL),
('si-7', 'Signal', 'East', 'Center', 140, 380, 'HP01', '001', NULL),
('si-8', 'Signal', 'East', 'Center', 380, 380, 'HP01', '005', NULL),
('sw-3', 'Switch', 'West', 'Right', 460, 340, 'NONE', '010', NULL),
('sw-4', 'Switch', 'East', 'Right', 460, 380, 'NONE', '009', NULL),
('si-9', 'Signal', 'West', 'Center', 500, 100, 'HP01', '015', NULL),
('si-10', 'Signal', 'West', 'Center', 500, 60, 'HP01', '008', NULL),
('st-2', 'Straight', 'West', 'Center', 420, 100, NULL, NULL, NULL),
('ct-5', 'Curved', 'East', 'Center', 60, 60, NULL, NULL, NULL),
('se-9', 'Sensor', 'East', 'Center', 340, 60, NULL, NULL, 9),
('bk-5', 'Block', 'North', 'Center', 580, 220, NULL, NULL, NULL),
('bk-6', 'Block', 'East', 'Center', 260, 380, NULL, NULL, NULL),
('bk-7', 'Block', 'South', 'Center', 60, 220, NULL, NULL, NULL),
('bk-8', 'Block', 'West', 'Center', 260, 60, NULL, NULL, NULL),
('si-11', 'Signal', 'West', 'Center', 500, 340, 'HP01', '006', NULL),
('si-12', 'Signal', 'West', 'Center', 420, 340, 'HP01', '013', NULL),
('si-13', 'Signal', 'West', 'Center', 500, 380, 'HP01', '014', NULL),
('se-10', 'Sensor', 'West', 'Center', 180, 60, NULL, NULL, 10),
('se-11', 'Sensor', 'North', 'Center', 60, 140, NULL, NULL, 11),
('se-12', 'Sensor', 'North', 'Center', 60, 300, NULL, NULL, 12),
('se-13', 'Sensor', 'East', 'Center', 180, 380, NULL, NULL, 13),
('se-14', 'Sensor', 'East', 'Center', 340, 380, NULL, NULL, 14),
('se-15', 'Sensor', 'South', 'Center', 580, 300, NULL, NULL, 15),
('se-16', 'Sensor', 'South', 'Center', 580, 140, NULL, NULL, 16),
('ct-6', 'Curved', 'West', 'Center', 580, 380, NULL, NULL, NULL),
('ct-7', 'Curved', 'South', 'Center', 580, 60, NULL, NULL, NULL),
('ct-8', 'Curved', 'North', 'Center', 60, 380, NULL, NULL, NULL),
('st-3', 'Straight', 'East', 'Center', 100, 60, NULL, NULL, NULL),
('st-4', 'Straight', 'East', 'Center', 540, 60, NULL, NULL, NULL),
('st-5', 'Straight', 'East', 'Center', 540, 380, NULL, NULL, NULL),
('st-6', 'Straight', 'East', 'Center', 100, 380, NULL, NULL, NULL),
('st-7', 'Straight', 'South', 'Center', 60, 340, NULL, NULL, NULL),
('st-8', 'Straight', 'South', 'Center', 60, 100, NULL, NULL, NULL),
('st-9', 'Straight', 'South', 'Center', 580, 100, NULL, NULL, NULL),
('st-10', 'Straight', 'South', 'Center', 580, 340, NULL, NULL, NULL),
('st-11', 'Straight', 'West', 'Center', 380, 340, NULL, NULL, NULL),
('st-12', 'Straight', 'West', 'Center', 420, 380, NULL, NULL, NULL);       
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
('001', 41, 'Sein 41', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('002', 42, 'Sein 42', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('003', 43, 'Sein 43', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('004', 44, 'Sein 44', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('005', 45, 'Sein 45', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('006', 46, 'Sein 46', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('007', 47, 'Sein 47', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('008', 48, 'Sein 48', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('009', 49, 'Wissel 49', 'rechtsweiche', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('010', 50, 'Wissel 50', 'rechtsweiche', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('011', 51, 'Wissel 51', 'linksweiche', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('012', 52, 'Wissel 52', 'linksweiche', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('013', 57, 'Sein 57', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('014', 58, 'Sein 58', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('015', 59, 'Sein 59', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL),
('016', 60, 'Sein 60', 'lichtsignal_HP01', 0, 2, 200, 'mm', NULL, 'other', NULL, NULL, 'Manual Inserted', 'intellibox2', FALSE, NULL);   
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
-- 8 +/- SELECT COUNT(*) FROM jcs.blocks;      
INSERT INTO "jcs"."blocks" VALUES
('bk-1', 'bk-1', 'Blok 1', 2, 1, '006', '015', NULL, FALSE, 'Free', NULL, 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-2', 'bk-2', 'Blok 2', 4, 3, '002', '013', NULL, FALSE, 'Free', NULL, 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-3', 'bk-3', 'Blok 3', 6, 5, '003', NULL, NULL, FALSE, 'Free', NULL, 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-4', 'bk-4', 'Blok 4', 8, 7, '007', NULL, 12, FALSE, 'Occupied', '-', 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-5', 'bk-5', 'Blok 5', 16, 15, '008', '014', NULL, FALSE, 'Free', NULL, 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-6', 'bk-6', 'Blok 6', 14, 13, '005', NULL, 72, FALSE, 'Occupied', '+', 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-7', 'bk-7', 'Blok 7', 12, 11, '001', NULL, NULL, FALSE, 'Free', NULL, 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE),
('bk-8', 'bk-8', 'Blok 8', 10, 9, '004', '016', 52, FALSE, 'Occupied', '-', 10, NULL, FALSE, TRUE, FALSE, NULL, FALSE);        
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
-- 48 +/- SELECT COUNT(*) FROM jcs.sensors;    
INSERT INTO "jcs"."sensors" VALUES
(1, 'M01-C01', 1, 1, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(2, 'M01-C02', 1, 2, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(3, 'M01-C03', 1, 3, 0, 1, 412, DATE '2026-10-10', 0, 0, 'intellibox2'),
(4, 'M01-C04', 1, 4, 0, 1, 1609, DATE '2026-10-10', 0, 0, 'intellibox2'),
(5, 'M01-C05', 1, 5, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(6, 'M01-C06', 1, 6, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(7, 'M01-C07', 1, 7, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(8, 'M01-C08', 1, 8, 0, 1, 51, DATE '2026-10-10', 0, 0, 'intellibox2'),
(9, 'M01-C09', 1, 9, 0, 1, 86, DATE '2026-10-10', 0, 0, 'intellibox2'),
(10, 'M01-C10', 1, 10, 0, 1, 933, DATE '2026-10-10', 0, 0, 'intellibox2'),
(11, 'M01-C11', 1, 11, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(12, 'M01-C12', 1, 12, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(13, 'M01-C13', 1, 13, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(14, 'M01-C14', 1, 14, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(15, 'M01-C15', 1, 15, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(16, 'M01-C16', 1, 16, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(17, 'M02-C01', 2, 1, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(18, 'M02-C02', 2, 2, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(19, 'M02-C03', 2, 3, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(20, 'M02-C04', 2, 4, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(21, 'M02-C05', 2, 5, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(22, 'M02-C06', 2, 6, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(23, 'M02-C07', 2, 7, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(24, 'M02-C08', 2, 8, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(25, 'M02-C09', 2, 9, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(26, 'M02-C10', 2, 10, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(27, 'M02-C11', 2, 11, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(28, 'M02-C12', 2, 12, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(29, 'M02-C13', 2, 13, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(30, 'M02-C14', 2, 14, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(31, 'M02-C15', 2, 15, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(32, 'M02-C16', 2, 16, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(33, 'M03-C01', 3, 1, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(34, 'M03-C02', 3, 2, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(35, 'M03-C03', 3, 3, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(36, 'M03-C04', 3, 4, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(37, 'M03-C05', 3, 5, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(38, 'M03-C06', 3, 6, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(39, 'M03-C07', 3, 7, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(40, 'M03-C08', 3, 8, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(41, 'M03-C09', 3, 9, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(42, 'M03-C10', 3, 10, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(43, 'M03-C11', 3, 11, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(44, 'M03-C12', 3, 12, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(45, 'M03-C13', 3, 13, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(46, 'M03-C14', 3, 14, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(47, 'M03-C15', 3, 15, 0, 0, NULL, NULL, 0, 0, 'intellibox2'),
(48, 'M03-C16', 3, 16, 0, 0, NULL, NULL, 0, 0, 'intellibox2');      
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."sens_pk_idx" ON "jcs"."sensors"("id" NULLS FIRST);   
CREATE CACHED TABLE "jcs"."locomotive_functions"(
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1 RESTART WITH 66) NOT NULL,
    "locomotive_id" BIGINT NOT NULL,
    "f_number" INTEGER NOT NULL,
    "f_type" INTEGER NOT NULL,
    "f_value" INTEGER,
    "f_icon" CHARACTER VARYING(255),
    "momentary" BOOLEAN DEFAULT FALSE NOT NULL
);  
ALTER TABLE "jcs"."locomotive_functions" ADD CONSTRAINT "jcs"."lofu_pk" PRIMARY KEY("id");     
-- 25 +/- SELECT COUNT(*) FROM jcs.locomotive_functions;       
INSERT INTO "jcs"."locomotive_functions" VALUES
(41, 12, 0, 0, 0, NULL, FALSE),
(42, 12, 1, 1, 0, NULL, FALSE),
(43, 12, 2, 2, 0, NULL, FALSE),
(44, 12, 3, 3, 0, NULL, FALSE),
(45, 12, 4, 4, 0, NULL, FALSE),
(46, 52, 0, 0, 0, NULL, FALSE),
(47, 52, 1, 1, 0, NULL, FALSE),
(48, 52, 2, 2, 0, NULL, FALSE),
(49, 52, 3, 3, 0, NULL, FALSE),
(50, 52, 4, 4, 0, NULL, FALSE),
(51, 52, 5, 5, 0, NULL, FALSE),
(52, 52, 6, 6, 0, NULL, FALSE),
(53, 52, 7, 7, 0, NULL, FALSE),
(54, 52, 8, 8, 0, NULL, FALSE),
(55, 52, 9, 9, NULL, NULL, FALSE),
(56, 52, 10, 10, NULL, NULL, FALSE),
(57, 52, 11, 11, NULL, NULL, FALSE),
(58, 52, 12, 12, NULL, NULL, FALSE),
(59, 52, 13, 13, NULL, NULL, FALSE),
(60, 52, 14, 14, NULL, NULL, FALSE),
(61, 72, 0, 0, 0, NULL, FALSE),
(62, 72, 1, 1, 0, NULL, FALSE),
(63, 72, 2, 2, 0, NULL, FALSE),
(64, 72, 3, 3, 0, NULL, FALSE),
(65, 72, 4, 4, 0, NULL, FALSE);    
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
(12, 'DB 141 015-8', 12, 12, '/Users/fransjacobs/jcs/images/db br 141 136-2.png', 'mm', 100, 0, 0, FALSE, 'Manual Updated', FALSE, TRUE, 'intellibox2', NULL, 'FORWARDS', 30, 50, 70, 90),
(52, 'DB 152 119-4', 52, 52, '/Users/fransjacobs/jcs/images/db br 152 119-4.png', 'mm', 100, 0, 0, FALSE, 'Manual Updated', FALSE, TRUE, 'intellibox2', NULL, 'FORWARDS', 20, 50, 70, 90),
(72, 'DB 216 059-6', 72, 72, '/Users/fransjacobs/jcs/images/db br 216 059-6.png', 'mm', 100, 0, 0, FALSE, 'Manual Updated', FALSE, TRUE, 'intellibox2', NULL, 'FORWARDS', 20, 50, 70, 90);        
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
('virtual', 'Virtual CS', 'VIR', 'jcs.commandStation.virtual.VirtualCommandStationImpl', 'NETWORK', NULL, '127.0.0.1', 0, FALSE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, 'DCC', FALSE, FALSE, '1', 'NETWORK', '0', 1, 1, 0, 0, 0, TRUE),
('esu-ecos', 'ESU ECoS', 'ECoS', 'jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl', 'NETWORK', NULL, NULL, 15471, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 'DCC,MFX,MM', FALSE, FALSE, '1', 'NETWORK', '0', 0, 0, 0, 0, 0, FALSE),
('intellibox2', 'Uhlenbrock Intellibox 2', 'Loconet', 'jcs.commandStation.loconet.Intellibox2Impl', 'SERIAL', 'tty.BLTH', 'AUTO', NULL, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, 'DCC,MM', TRUE, TRUE, '1', 'SERIAL', '0', 0, 0, 0, 0, 0, FALSE);  
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
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1 RESTART WITH 205) NOT NULL,
    "route_id" CHARACTER VARYING(255) NOT NULL,
    "node_id" CHARACTER VARYING(255) NOT NULL,
    "tile_id" CHARACTER VARYING(255) NOT NULL,
    "accessory_value" CHARACTER VARYING(255),
    "order_seq" INTEGER DEFAULT 0 NOT NULL,
    "incoming_side" CHARACTER VARYING(255)
);    
ALTER TABLE "jcs"."route_elements" ADD CONSTRAINT "jcs"."roel_pk" PRIMARY KEY("id");           
-- 204 +/- SELECT COUNT(*) FROM jcs.route_elements;            
INSERT INTO "jcs"."route_elements" VALUES
(1, '[bk-5+]->[bk-4+]', 'bk-5', 'bk-5', NULL, 0, NULL),
(2, '[bk-5+]->[bk-4+]', 'bk-5+', 'bk-5', NULL, 1, NULL),
(3, '[bk-5+]->[bk-4+]', 'se-16', 'se-16', NULL, 2, 'South'),
(4, '[bk-5+]->[bk-4+]', 'st-9', 'st-9', NULL, 3, 'South'),
(5, '[bk-5+]->[bk-4+]', 'ct-7', 'ct-7', NULL, 4, 'South'),
(6, '[bk-5+]->[bk-4+]', 'st-4', 'st-4', NULL, 5, 'East'),
(7, '[bk-5+]->[bk-4+]', 'si-10', 'si-10', NULL, 6, 'East'),
(8, '[bk-5+]->[bk-4+]', 'sw-2', 'sw-2', 'R', 7, 'East'),
(9, '[bk-5+]->[bk-4+]', 'sw-1', 'sw-1', 'R', 8, 'North'),
(10, '[bk-5+]->[bk-4+]', 'st-2', 'st-2', NULL, 9, 'East'),
(11, '[bk-5+]->[bk-4+]', 'si-4', 'si-4', NULL, 10, 'East'),
(12, '[bk-5+]->[bk-4+]', 'se-8', 'se-8', NULL, 11, 'East'),
(13, '[bk-5+]->[bk-4+]', 'bk-4+', 'bk-4', NULL, 12, 'East'),
(14, '[bk-8-]->[bk-5+]', 'bk-8', 'bk-8', NULL, 0, NULL),
(15, '[bk-8-]->[bk-5+]', 'bk-8-', 'bk-8', NULL, 1, NULL),
(16, '[bk-8-]->[bk-5+]', 'se-9', 'se-9', NULL, 2, 'West'),
(17, '[bk-8-]->[bk-5+]', 'st-1', 'st-1', NULL, 3, 'West'),
(18, '[bk-8-]->[bk-5+]', 'si-5', 'si-5', NULL, 4, 'West'),
(19, '[bk-8-]->[bk-5+]', 'sw-2', 'sw-2', 'G', 5, 'West'),
(20, '[bk-8-]->[bk-5+]', 'si-10', 'si-10', NULL, 6, 'West'),
(21, '[bk-8-]->[bk-5+]', 'st-4', 'st-4', NULL, 7, 'West'),
(22, '[bk-8-]->[bk-5+]', 'ct-7', 'ct-7', NULL, 8, 'West'),
(23, '[bk-8-]->[bk-5+]', 'st-9', 'st-9', NULL, 9, 'North'),
(24, '[bk-8-]->[bk-5+]', 'se-16', 'se-16', NULL, 10, 'North'),
(25, '[bk-8-]->[bk-5+]', 'bk-5+', 'bk-5', NULL, 11, 'North'),
(26, '[bk-3-]->[bk-2+]', 'bk-3', 'bk-3', NULL, 0, NULL),
(27, '[bk-3-]->[bk-2+]', 'bk-3-', 'bk-3', NULL, 1, NULL),
(28, '[bk-3-]->[bk-2+]', 'se-5', 'se-5', NULL, 2, 'North'),
(29, '[bk-3-]->[bk-2+]', 'ct-4', 'ct-4', NULL, 3, 'North'),
(30, '[bk-3-]->[bk-2+]', 'si-1', 'si-1', NULL, 4, 'West'),
(31, '[bk-3-]->[bk-2+]', 'se-4', 'se-4', NULL, 5, 'West'),
(32, '[bk-3-]->[bk-2+]', 'bk-2+', 'bk-2', NULL, 6, 'West'),
(33, '[bk-3+]->[bk-4-]', 'bk-3', 'bk-3', NULL, 0, NULL),
(34, '[bk-3+]->[bk-4-]', 'bk-3+', 'bk-3', NULL, 1, NULL),
(35, '[bk-3+]->[bk-4-]', 'se-6', 'se-6', NULL, 2, 'South'),
(36, '[bk-3+]->[bk-4-]', 'ct-1', 'ct-1', NULL, 3, 'South'),
(37, '[bk-3+]->[bk-4-]', 'si-3', 'si-3', NULL, 4, 'West'),
(38, '[bk-3+]->[bk-4-]', 'se-7', 'se-7', NULL, 5, 'West'),
(39, '[bk-3+]->[bk-4-]', 'bk-4-', 'bk-4', NULL, 6, 'West'),
(40, '[bk-1-]->[bk-4+]', 'bk-1', 'bk-1', NULL, 0, NULL),
(41, '[bk-1-]->[bk-4+]', 'bk-1-', 'bk-1', NULL, 1, NULL),
(42, '[bk-1-]->[bk-4+]', 'se-1', 'se-1', NULL, 2, 'South'),
(43, '[bk-1-]->[bk-4+]', 'ct-2', 'ct-2', NULL, 3, 'South'),
(44, '[bk-1-]->[bk-4+]', 'si-9', 'si-9', NULL, 4, 'East'),
(45, '[bk-1-]->[bk-4+]', 'sw-1', 'sw-1', 'G', 5, 'East'),
(46, '[bk-1-]->[bk-4+]', 'st-2', 'st-2', NULL, 6, 'East'),
(47, '[bk-1-]->[bk-4+]', 'si-4', 'si-4', NULL, 7, 'East'),
(48, '[bk-1-]->[bk-4+]', 'se-8', 'se-8', NULL, 8, 'East'),
(49, '[bk-1-]->[bk-4+]', 'bk-4+', 'bk-4', NULL, 9, 'East'),
(50, '[bk-1+]->[bk-2-]', 'bk-1', 'bk-1', NULL, 0, NULL),
(51, '[bk-1+]->[bk-2-]', 'bk-1+', 'bk-1', NULL, 1, NULL),
(52, '[bk-1+]->[bk-2-]', 'se-2', 'se-2', NULL, 2, 'North'),
(53, '[bk-1+]->[bk-2-]', 'ct-3', 'ct-3', NULL, 3, 'North'),
(54, '[bk-1+]->[bk-2-]', 'si-11', 'si-11', NULL, 4, 'East'),
(55, '[bk-1+]->[bk-2-]', 'sw-3', 'sw-3', 'G', 5, 'East'),
(56, '[bk-1+]->[bk-2-]', 'si-12', 'si-12', NULL, 6, 'East'),
(57, '[bk-1+]->[bk-2-]', 'st-11', 'st-11', NULL, 7, 'East'),
(58, '[bk-1+]->[bk-2-]', 'se-3', 'se-3', NULL, 8, 'East'),
(59, '[bk-1+]->[bk-2-]', 'bk-2-', 'bk-2', NULL, 9, 'East'),
(60, '[bk-7-]->[bk-8+]', 'bk-7', 'bk-7', NULL, 0, NULL),
(61, '[bk-7-]->[bk-8+]', 'bk-7-', 'bk-7', NULL, 1, NULL),
(62, '[bk-7-]->[bk-8+]', 'se-11', 'se-11', NULL, 2, 'South'),
(63, '[bk-7-]->[bk-8+]', 'st-8', 'st-8', NULL, 3, 'South'),
(64, '[bk-7-]->[bk-8+]', 'ct-5', 'ct-5', NULL, 4, 'South'),
(65, '[bk-7-]->[bk-8+]', 'st-3', 'st-3', NULL, 5, 'West'),
(66, '[bk-7-]->[bk-8+]', 'si-6', 'si-6', NULL, 6, 'West'),
(67, '[bk-7-]->[bk-8+]', 'se-10', 'se-10', NULL, 7, 'West'),
(68, '[bk-7-]->[bk-8+]', 'bk-8+', 'bk-8', NULL, 8, 'West'),
(69, '[bk-5-]->[bk-6+]', 'bk-5', 'bk-5', NULL, 0, NULL);    
INSERT INTO "jcs"."route_elements" VALUES
(70, '[bk-5-]->[bk-6+]', 'bk-5-', 'bk-5', NULL, 1, NULL),
(71, '[bk-5-]->[bk-6+]', 'se-15', 'se-15', NULL, 2, 'North'),
(72, '[bk-5-]->[bk-6+]', 'st-10', 'st-10', NULL, 3, 'North'),
(73, '[bk-5-]->[bk-6+]', 'ct-6', 'ct-6', NULL, 4, 'North'),
(74, '[bk-5-]->[bk-6+]', 'st-5', 'st-5', NULL, 5, 'East'),
(75, '[bk-5-]->[bk-6+]', 'si-13', 'si-13', NULL, 6, 'East'),
(76, '[bk-5-]->[bk-6+]', 'sw-4', 'sw-4', 'G', 7, 'East'),
(77, '[bk-5-]->[bk-6+]', 'st-12', 'st-12', NULL, 8, 'East'),
(78, '[bk-5-]->[bk-6+]', 'si-8', 'si-8', NULL, 9, 'East'),
(79, '[bk-5-]->[bk-6+]', 'se-14', 'se-14', NULL, 10, 'East'),
(80, '[bk-5-]->[bk-6+]', 'bk-6+', 'bk-6', NULL, 11, 'East'),
(81, '[bk-6+]->[bk-5-]', 'bk-6', 'bk-6', NULL, 0, NULL),
(82, '[bk-6+]->[bk-5-]', 'bk-6+', 'bk-6', NULL, 1, NULL),
(83, '[bk-6+]->[bk-5-]', 'se-14', 'se-14', NULL, 2, 'West'),
(84, '[bk-6+]->[bk-5-]', 'si-8', 'si-8', NULL, 3, 'West'),
(85, '[bk-6+]->[bk-5-]', 'st-12', 'st-12', NULL, 4, 'West'),
(86, '[bk-6+]->[bk-5-]', 'sw-4', 'sw-4', 'G', 5, 'West'),
(87, '[bk-6+]->[bk-5-]', 'si-13', 'si-13', NULL, 6, 'West'),
(88, '[bk-6+]->[bk-5-]', 'st-5', 'st-5', NULL, 7, 'West'),
(89, '[bk-6+]->[bk-5-]', 'ct-6', 'ct-6', NULL, 8, 'West'),
(90, '[bk-6+]->[bk-5-]', 'st-10', 'st-10', NULL, 9, 'South'),
(91, '[bk-6+]->[bk-5-]', 'se-15', 'se-15', NULL, 10, 'South'),
(92, '[bk-6+]->[bk-5-]', 'bk-5-', 'bk-5', NULL, 11, 'South'),
(93, '[bk-4+]->[bk-5+]', 'bk-4', 'bk-4', NULL, 0, NULL),
(94, '[bk-4+]->[bk-5+]', 'bk-4+', 'bk-4', NULL, 1, NULL),
(95, '[bk-4+]->[bk-5+]', 'se-8', 'se-8', NULL, 2, 'West'),
(96, '[bk-4+]->[bk-5+]', 'si-4', 'si-4', NULL, 3, 'West'),
(97, '[bk-4+]->[bk-5+]', 'st-2', 'st-2', NULL, 4, 'West'),
(98, '[bk-4+]->[bk-5+]', 'sw-1', 'sw-1', 'R', 5, 'West'),
(99, '[bk-4+]->[bk-5+]', 'sw-2', 'sw-2', 'R', 6, 'South'),
(100, '[bk-4+]->[bk-5+]', 'si-10', 'si-10', NULL, 7, 'West'),
(101, '[bk-4+]->[bk-5+]', 'st-4', 'st-4', NULL, 8, 'West'),
(102, '[bk-4+]->[bk-5+]', 'ct-7', 'ct-7', NULL, 9, 'West'),
(103, '[bk-4+]->[bk-5+]', 'st-9', 'st-9', NULL, 10, 'North'),
(104, '[bk-4+]->[bk-5+]', 'se-16', 'se-16', NULL, 11, 'North'),
(105, '[bk-4+]->[bk-5+]', 'bk-5+', 'bk-5', NULL, 12, 'North'),
(106, '[bk-8+]->[bk-7-]', 'bk-8', 'bk-8', NULL, 0, NULL),
(107, '[bk-8+]->[bk-7-]', 'bk-8+', 'bk-8', NULL, 1, NULL),
(108, '[bk-8+]->[bk-7-]', 'se-10', 'se-10', NULL, 2, 'East'),
(109, '[bk-8+]->[bk-7-]', 'si-6', 'si-6', NULL, 3, 'East'),
(110, '[bk-8+]->[bk-7-]', 'st-3', 'st-3', NULL, 4, 'East'),
(111, '[bk-8+]->[bk-7-]', 'ct-5', 'ct-5', NULL, 5, 'East'),
(112, '[bk-8+]->[bk-7-]', 'st-8', 'st-8', NULL, 6, 'North'),
(113, '[bk-8+]->[bk-7-]', 'se-11', 'se-11', NULL, 7, 'North'),
(114, '[bk-8+]->[bk-7-]', 'bk-7-', 'bk-7', NULL, 8, 'North'),
(115, '[bk-2-]->[bk-5-]', 'bk-2', 'bk-2', NULL, 0, NULL),
(116, '[bk-2-]->[bk-5-]', 'bk-2-', 'bk-2', NULL, 1, NULL),
(117, '[bk-2-]->[bk-5-]', 'se-3', 'se-3', NULL, 2, 'West'),
(118, '[bk-2-]->[bk-5-]', 'st-11', 'st-11', NULL, 3, 'West'),
(119, '[bk-2-]->[bk-5-]', 'si-12', 'si-12', NULL, 4, 'West'),
(120, '[bk-2-]->[bk-5-]', 'sw-3', 'sw-3', 'R', 5, 'West'),
(121, '[bk-2-]->[bk-5-]', 'sw-4', 'sw-4', 'R', 6, 'North'),
(122, '[bk-2-]->[bk-5-]', 'si-13', 'si-13', NULL, 7, 'West'),
(123, '[bk-2-]->[bk-5-]', 'st-5', 'st-5', NULL, 8, 'West'),
(124, '[bk-2-]->[bk-5-]', 'ct-6', 'ct-6', NULL, 9, 'West'),
(125, '[bk-2-]->[bk-5-]', 'st-10', 'st-10', NULL, 10, 'South'),
(126, '[bk-2-]->[bk-5-]', 'se-15', 'se-15', NULL, 11, 'South'),
(127, '[bk-2-]->[bk-5-]', 'bk-5-', 'bk-5', NULL, 12, 'South'),
(128, '[bk-2-]->[bk-1+]', 'bk-2', 'bk-2', NULL, 0, NULL),
(129, '[bk-2-]->[bk-1+]', 'bk-2-', 'bk-2', NULL, 1, NULL),
(130, '[bk-2-]->[bk-1+]', 'se-3', 'se-3', NULL, 2, 'West'),
(131, '[bk-2-]->[bk-1+]', 'st-11', 'st-11', NULL, 3, 'West'),
(132, '[bk-2-]->[bk-1+]', 'si-12', 'si-12', NULL, 4, 'West'),
(133, '[bk-2-]->[bk-1+]', 'sw-3', 'sw-3', 'G', 5, 'West'),
(134, '[bk-2-]->[bk-1+]', 'si-11', 'si-11', NULL, 6, 'West'),
(135, '[bk-2-]->[bk-1+]', 'ct-3', 'ct-3', NULL, 7, 'West'),
(136, '[bk-2-]->[bk-1+]', 'se-2', 'se-2', NULL, 8, 'South'),
(137, '[bk-2-]->[bk-1+]', 'bk-1+', 'bk-1', NULL, 9, 'South');            
INSERT INTO "jcs"."route_elements" VALUES
(138, '[bk-5-]->[bk-2-]', 'bk-5', 'bk-5', NULL, 0, NULL),
(139, '[bk-5-]->[bk-2-]', 'bk-5-', 'bk-5', NULL, 1, NULL),
(140, '[bk-5-]->[bk-2-]', 'se-15', 'se-15', NULL, 2, 'North'),
(141, '[bk-5-]->[bk-2-]', 'st-10', 'st-10', NULL, 3, 'North'),
(142, '[bk-5-]->[bk-2-]', 'ct-6', 'ct-6', NULL, 4, 'North'),
(143, '[bk-5-]->[bk-2-]', 'st-5', 'st-5', NULL, 5, 'East'),
(144, '[bk-5-]->[bk-2-]', 'si-13', 'si-13', NULL, 6, 'East'),
(145, '[bk-5-]->[bk-2-]', 'sw-4', 'sw-4', 'R', 7, 'East'),
(146, '[bk-5-]->[bk-2-]', 'sw-3', 'sw-3', 'R', 8, 'South'),
(147, '[bk-5-]->[bk-2-]', 'si-12', 'si-12', NULL, 9, 'East'),
(148, '[bk-5-]->[bk-2-]', 'st-11', 'st-11', NULL, 10, 'East'),
(149, '[bk-5-]->[bk-2-]', 'se-3', 'se-3', NULL, 11, 'East'),
(150, '[bk-5-]->[bk-2-]', 'bk-2-', 'bk-2', NULL, 12, 'East'),
(151, '[bk-4-]->[bk-3+]', 'bk-4', 'bk-4', NULL, 0, NULL),
(152, '[bk-4-]->[bk-3+]', 'bk-4-', 'bk-4', NULL, 1, NULL),
(153, '[bk-4-]->[bk-3+]', 'se-7', 'se-7', NULL, 2, 'East'),
(154, '[bk-4-]->[bk-3+]', 'si-3', 'si-3', NULL, 3, 'East'),
(155, '[bk-4-]->[bk-3+]', 'ct-1', 'ct-1', NULL, 4, 'East'),
(156, '[bk-4-]->[bk-3+]', 'se-6', 'se-6', NULL, 5, 'North'),
(157, '[bk-4-]->[bk-3+]', 'bk-3+', 'bk-3', NULL, 6, 'North'),
(158, '[bk-2+]->[bk-3-]', 'bk-2', 'bk-2', NULL, 0, NULL),
(159, '[bk-2+]->[bk-3-]', 'bk-2+', 'bk-2', NULL, 1, NULL),
(160, '[bk-2+]->[bk-3-]', 'se-4', 'se-4', NULL, 2, 'East'),
(161, '[bk-2+]->[bk-3-]', 'si-1', 'si-1', NULL, 3, 'East'),
(162, '[bk-2+]->[bk-3-]', 'ct-4', 'ct-4', NULL, 4, 'East'),
(163, '[bk-2+]->[bk-3-]', 'se-5', 'se-5', NULL, 5, 'South'),
(164, '[bk-2+]->[bk-3-]', 'bk-3-', 'bk-3', NULL, 6, 'South'),
(165, '[bk-4+]->[bk-1-]', 'bk-4', 'bk-4', NULL, 0, NULL),
(166, '[bk-4+]->[bk-1-]', 'bk-4+', 'bk-4', NULL, 1, NULL),
(167, '[bk-4+]->[bk-1-]', 'se-8', 'se-8', NULL, 2, 'West'),
(168, '[bk-4+]->[bk-1-]', 'si-4', 'si-4', NULL, 3, 'West'),
(169, '[bk-4+]->[bk-1-]', 'st-2', 'st-2', NULL, 4, 'West'),
(170, '[bk-4+]->[bk-1-]', 'sw-1', 'sw-1', 'G', 5, 'West'),
(171, '[bk-4+]->[bk-1-]', 'si-9', 'si-9', NULL, 6, 'West'),
(172, '[bk-4+]->[bk-1-]', 'ct-2', 'ct-2', NULL, 7, 'West'),
(173, '[bk-4+]->[bk-1-]', 'se-1', 'se-1', NULL, 8, 'North'),
(174, '[bk-4+]->[bk-1-]', 'bk-1-', 'bk-1', NULL, 9, 'North'),
(175, '[bk-6-]->[bk-7+]', 'bk-6', 'bk-6', NULL, 0, NULL),
(176, '[bk-6-]->[bk-7+]', 'bk-6-', 'bk-6', NULL, 1, NULL),
(177, '[bk-6-]->[bk-7+]', 'se-13', 'se-13', NULL, 2, 'East'),
(178, '[bk-6-]->[bk-7+]', 'si-7', 'si-7', NULL, 3, 'East'),
(179, '[bk-6-]->[bk-7+]', 'st-6', 'st-6', NULL, 4, 'East'),
(180, '[bk-6-]->[bk-7+]', 'ct-8', 'ct-8', NULL, 5, 'East'),
(181, '[bk-6-]->[bk-7+]', 'st-7', 'st-7', NULL, 6, 'South'),
(182, '[bk-6-]->[bk-7+]', 'se-12', 'se-12', NULL, 7, 'South'),
(183, '[bk-6-]->[bk-7+]', 'bk-7+', 'bk-7', NULL, 8, 'South'),
(184, '[bk-7+]->[bk-6-]', 'bk-7', 'bk-7', NULL, 0, NULL),
(185, '[bk-7+]->[bk-6-]', 'bk-7+', 'bk-7', NULL, 1, NULL),
(186, '[bk-7+]->[bk-6-]', 'se-12', 'se-12', NULL, 2, 'North'),
(187, '[bk-7+]->[bk-6-]', 'st-7', 'st-7', NULL, 3, 'North'),
(188, '[bk-7+]->[bk-6-]', 'ct-8', 'ct-8', NULL, 4, 'North'),
(189, '[bk-7+]->[bk-6-]', 'st-6', 'st-6', NULL, 5, 'West'),
(190, '[bk-7+]->[bk-6-]', 'si-7', 'si-7', NULL, 6, 'West'),
(191, '[bk-7+]->[bk-6-]', 'se-13', 'se-13', NULL, 7, 'West'),
(192, '[bk-7+]->[bk-6-]', 'bk-6-', 'bk-6', NULL, 8, 'West'),
(193, '[bk-5+]->[bk-8-]', 'bk-5', 'bk-5', NULL, 0, NULL),
(194, '[bk-5+]->[bk-8-]', 'bk-5+', 'bk-5', NULL, 1, NULL),
(195, '[bk-5+]->[bk-8-]', 'se-16', 'se-16', NULL, 2, 'South'),
(196, '[bk-5+]->[bk-8-]', 'st-9', 'st-9', NULL, 3, 'South'),
(197, '[bk-5+]->[bk-8-]', 'ct-7', 'ct-7', NULL, 4, 'South'),
(198, '[bk-5+]->[bk-8-]', 'st-4', 'st-4', NULL, 5, 'East'),
(199, '[bk-5+]->[bk-8-]', 'si-10', 'si-10', NULL, 6, 'East'),
(200, '[bk-5+]->[bk-8-]', 'sw-2', 'sw-2', 'G', 7, 'East'),
(201, '[bk-5+]->[bk-8-]', 'si-5', 'si-5', NULL, 8, 'East'),
(202, '[bk-5+]->[bk-8-]', 'st-1', 'st-1', NULL, 9, 'East'),
(203, '[bk-5+]->[bk-8-]', 'se-9', 'se-9', NULL, 10, 'East'),
(204, '[bk-5+]->[bk-8-]', 'bk-8-', 'bk-8', NULL, 11, 'East');      
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
