-- H2 2.4.240; 
;              
CREATE USER IF NOT EXISTS "JCS" SALT '97c24b7639edd403' HASH '46948dab3826080a9ed5952da99dd5a4da71754d924ff969ebdac26be1d4baf2' ADMIN;         
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
-- 0 +/- SELECT COUNT(*) FROM jcs.tiles;       
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
-- 0 +/- SELECT COUNT(*) FROM jcs.accessories; 
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
-- 0 +/- SELECT COUNT(*) FROM jcs.blocks;      
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
-- 0 +/- SELECT COUNT(*) FROM jcs.sensors;     
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."sens_pk_idx" ON "jcs"."sensors"("id" NULLS FIRST);   
CREATE CACHED TABLE "jcs"."locomotive_functions"(
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL,
    "locomotive_id" BIGINT NOT NULL,
    "f_number" INTEGER NOT NULL,
    "f_type" INTEGER NOT NULL,
    "f_value" INTEGER,
    "f_icon" CHARACTER VARYING(255),
    "momentary" BOOLEAN DEFAULT FALSE NOT NULL
);  
ALTER TABLE "jcs"."locomotive_functions" ADD CONSTRAINT "jcs"."lofu_pk" PRIMARY KEY("id");     
-- 0 +/- SELECT COUNT(*) FROM jcs.locomotive_functions;        
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
-- 0 +/- SELECT COUNT(*) FROM jcs.locomotives; 
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
-- 0 +/- SELECT COUNT(*) FROM jcs.routes;      
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."rout_pk_idx" ON "jcs"."routes"("id" NULLS FIRST);    
CREATE UNIQUE NULLS DISTINCT INDEX "jcs"."rout_from_to_un_idx" ON "jcs"."routes"("from_tile_id" NULLS FIRST, "from_suffix" NULLS FIRST, "to_tile_id" NULLS FIRST, "to_suffix" NULLS FIRST);    
CREATE CACHED TABLE "jcs"."route_elements"(
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL,
    "route_id" CHARACTER VARYING(255) NOT NULL,
    "node_id" CHARACTER VARYING(255) NOT NULL,
    "tile_id" CHARACTER VARYING(255) NOT NULL,
    "accessory_value" CHARACTER VARYING(255),
    "order_seq" INTEGER DEFAULT 0 NOT NULL,
    "incoming_side" CHARACTER VARYING(255)
);     
ALTER TABLE "jcs"."route_elements" ADD CONSTRAINT "jcs"."roel_pk" PRIMARY KEY("id");           
-- 0 +/- SELECT COUNT(*) FROM jcs.route_elements;              
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
