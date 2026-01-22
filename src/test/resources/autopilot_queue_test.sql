delete from route_elements;
delete from routes;
delete from station_blocks;
delete from stations;
delete from blocks;
delete from tiles;

delete from locomotive_functions;
delete from locomotives;
delete from sensors;
delete from accessories;
delete from command_stations;

commit;

INSERT INTO command_stations (id,description,short_name,class_name,connect_via,serial_port,ip_address,network_port,ip_auto_conf,supports_decoder_control,supports_accessory_control,supports_feedback,supports_loco_synch,supports_accessory_synch,supports_loco_image_synch,supports_loco_function_synch,protocols,default_cs,enabled,last_used_serial,sup_conn_types,feedback_module_id,feedback_bus_count,feedback_bus_0_module_count,feedback_bus_1_module_count,feedback_bus_2_module_count,feedback_bus_3_module_count) 
VALUES ('virtual','Virtual CS','VIR','jcs.commandStation.virtual.VirtualCommandStationImpl','NETWORK',NULL,'127.0.0.1',0,false,true,true,true,false,false,false,false,'dcc',true,true,'1','NETWORK','0',1,1,0,0,0);

commit;

INSERT INTO jcs.accessories (id,address,name,"type",state,states,switch_time,protocol,decoder,accessory_group,icon,icon_file,imported,command_station_id,synchronize,address2) VALUES
	 ('001',1,'W01','rechtsweiche',0,2,200,'dcc',NULL,'other',NULL,NULL,NULL,'virtual',false,NULL),
	 ('002',2,'W02','linksweiche',1,2,200,'dcc',NULL,'other',NULL,NULL,NULL,'virtual',false,NULL),
	 ('003',3,'W03','rechtsweiche',0,2,200,'dcc',NULL,'other',NULL,NULL,NULL,'virtual',false,NULL),
	 ('004',4,'W04','linksweiche',1,2,200,'dcc',NULL,'other',NULL,NULL,NULL,'virtual',false,NULL);

commit;

INSERT INTO locomotives (id,name,uid,address,icon,decoder_type,tacho_max,v_min,velocity,locomotive_direction,synchronize,imported,commuter,show,command_station_id) 
VALUES (7,'NS DHG 6505',8,8,'/home/frans/jcs/cache/dcc-ex/ns dhg 6505.png','dcc',100,0,0,'BACKWARDS',false,'Manual Updated',true,true,'virtual'),
       (23,'BR 101 003-2',23,23,'/home/frans/jcs/cache/cs/db br 101 109-7.png','dcc',200,0,0,'FORWARDS',false,'Manual Updated',true,true,'virtual'),
       (39,'NS 1631',39,39,'/home/frans/jcs/cache/cs/ns 1652.png','dcc',120,0,0,'FORWARDS',false,'Manual Updated',true,true,'virtual');

INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary)
VALUES (7,0,50,0,NULL,false),
       (7,1,51,0,NULL,false),
       (7,2,52,0,NULL,false),
       (7,3,53,0,NULL,false),
       (7,4,54,0,NULL,false);

INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary)
 VALUES (23,0,1,1,'fkticon_a_001',false),
	(23,1,51,0,NULL,false),
	(23,2,4,0,'fkticon_i_004',false),
	(23,3,8,0,'fkticon_i_008',false),
	(23,4,18,0,'fkticon_i_018',false);

INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) 
VALUES (39,0,1,1,'fkticon_a_001',false),
       (39,1,51,0,NULL,false),
       (39,2,52,0,NULL,false),
       (39,3,8,0,'fkticon_i_008',false),
       (39,4,18,0,'fkticon_i_018',false);
commit;

INSERT INTO sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated,command_station_id)
VALUES (2,'M00-C02',0,2,0,1,NULL,NULL,'virtual'),
       (13,'M00-C13',0,13,0,1,NULL,NULL,'virtual'),
       (1,'M00-C01',0,1,0,1,0,NULL,'virtual'),
       (12,'M00-C12',0,12,0,1,NULL,NULL,'virtual'),
       (4,'M00-C04',0,4,0,1,NULL,NULL,'virtual'),
       (15,'M00-C15',0,15,0,NULL,NULL,NULL,'virtual'),
       (3,'M00-C03',0,3,0,1,0,NULL,'virtual'),
       (14,'M00-C14',0,14,0,NULL,NULL,NULL,'virtual'),
       (6,'M00-C06',0,6,0,NULL,NULL,NULL,'virtual'),
       (5,'M00-C05',0,5,0,NULL,NULL,NULL,'virtual');
INSERT INTO sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated,command_station_id)
VALUES (16,'M00-C16',0,16,0,NULL,NULL,NULL,'virtual'),
       (8,'M00-C08',0,8,0,NULL,NULL,NULL,'virtual'),
       (7,'M00-C07',0,7,0,NULL,NULL,NULL,'virtual'),
       (9,'M00-C09',0,9,0,NULL,NULL,NULL,'virtual'),
       (11,'M00-C11',0,11,0,1,1,null,'virtual'),
       (10,'M00-C10',0,10,0,1,1,null,'virtual');

commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('bk-1','Block','East','Center',380,100,NULL,NULL,NULL),
	 ('bk-2','Block','East','Center',380,140,NULL,NULL,NULL),
	 ('se-1','Sensor','East','Center',300,100,NULL,NULL,0),
	 ('se-2','Sensor','East','Center',460,100,NULL,NULL,1),
	 ('se-3','Sensor','East','Center',300,140,NULL,NULL,2),
	 ('se-4','Sensor','East','Center',460,140,NULL,NULL,3),
	 ('sw-1','Switch','West','Left',220,140,'NONE','001',NULL),
	 ('sw-2','Switch','East','Right',540,140,'NONE','002',NULL),
	 ('ct-1','Curved','East','Center',220,100,NULL,NULL,NULL),
	 ('ct-2','Curved','South','Center',540,100,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-1','Straight','West','Center',260,100,NULL,NULL,NULL),
	 ('st-2','Straight','West','Center',500,100,NULL,NULL,NULL),
	 ('st-3','Straight','West','Center',260,140,NULL,NULL,NULL),
	 ('st-4','Straight','West','Center',500,140,NULL,NULL,NULL),
	 ('st-5','Straight','West','Center',180,140,NULL,NULL,NULL),
	 ('st-6','Straight','West','Center',580,140,NULL,NULL,NULL),
	 ('ct-3','Curved','East','Center',140,140,NULL,NULL,NULL),
	 ('ct-4','Curved','South','Center',620,140,NULL,NULL,NULL),
	 ('st-7','Straight','South','Center',140,180,NULL,NULL,NULL),
	 ('st-8','Straight','South','Center',620,180,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('bk-3','Block','West','Center',380,220,NULL,NULL,NULL),
	 ('bk-4','Block','West','Center',380,260,NULL,NULL,NULL),
	 ('se-5','Sensor','West','Center',300,220,NULL,NULL,4),
	 ('se-6','Sensor','West','Center',460,220,NULL,NULL,5),
	 ('se-7','Sensor','West','Center',300,260,NULL,NULL,6),
	 ('se-8','Sensor','West','Center',460,260,NULL,NULL,7),
	 ('sw-3','Switch','East','Left',540,220,'NONE','004',NULL),
	 ('sw-4','Switch','West','Right',220,220,'NONE','003',NULL),
	 ('ct-5','Curved','North','Center',220,260,NULL,NULL,NULL),
	 ('ct-6','Curved','West','Center',540,260,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-9','Straight','West','Center',260,220,NULL,NULL,NULL),
	 ('st-10','Straight','West','Center',260,260,NULL,NULL,NULL),
	 ('st-11','Straight','West','Center',500,260,NULL,NULL,NULL),
	 ('st-12','Straight','West','Center',500,220,NULL,NULL,NULL),
	 ('st-13','Straight','West','Center',580,220,NULL,NULL,NULL),
	 ('st-14','Straight','West','Center',180,220,NULL,NULL,NULL),
	 ('ct-7','Curved','North','Center',140,220,NULL,NULL,NULL),
	 ('ct-8','Curved','West','Center',620,220,NULL,NULL,NULL);

commit;

INSERT INTO jcs.blocks (id,tile_id,description,plus_sensor_id,min_sensor_id,plus_signal_id,min_signal_id,locomotive_id,allow_non_commuter_only,status,incoming_suffix,min_wait_time,max_wait_time,random_wait,always_stop,allow_commuter_only,logical_direction,allow_direction_change) VALUES
	 ('bk-1','bk-1','spoor 1',1,0,NULL,NULL,NULL,false,'Free',NULL,10,NULL,false,true,false,NULL,false),
	 ('bk-2','bk-2','spoor 2',3,2,NULL,NULL,NULL,false,'Free',NULL,10,NULL,false,true,false,NULL,false),
	 ('bk-3','bk-3','blok 3',4,5,NULL,NULL,23,false,'Occupied','-',10,NULL,false,true,false,NULL,false),
	 ('bk-4','bk-4','blok 4',6,7,NULL,NULL,39,false,'Occupied','-',10,NULL,false,true,false,NULL,false);

commit;

INSERT INTO jcs.stations (id,name,min_locs,loc_count,use_fifo) VALUES
	 ('station-1','Station',2,0,true);

INSERT INTO jcs.station_blocks (id,station_id,block_id,last_updated) VALUES
	 ('station-1~bk-2','station-1','bk-2','2026-01-22 19:32:02.049'),
	 ('station-1~bk-1','station-1','bk-1','2026-01-22 19:32:02.049');

commit;

INSERT INTO jcs.routes (id,from_tile_id,from_suffix,to_tile_id,to_suffix,route_color,locked,status) VALUES
	 ('[bk-3+]->[bk-1-]','bk-3','+','bk-1','-',NULL,false,NULL),
	 ('[bk-1-]->[bk-3+]','bk-1','-','bk-3','+',NULL,false,NULL),
	 ('[bk-3-]->[bk-2+]','bk-3','-','bk-2','+',NULL,false,NULL),
	 ('[bk-1-]->[bk-4+]','bk-1','-','bk-4','+',NULL,false,NULL),
	 ('[bk-3+]->[bk-2-]','bk-3','+','bk-2','-',NULL,false,NULL),
	 ('[bk-1+]->[bk-3-]','bk-1','+','bk-3','-',NULL,false,NULL),
	 ('[bk-4-]->[bk-1+]','bk-4','-','bk-1','+',NULL,false,NULL),
	 ('[bk-4-]->[bk-2+]','bk-4','-','bk-2','+',NULL,false,NULL),
	 ('[bk-2-]->[bk-4+]','bk-2','-','bk-4','+',NULL,false,NULL),
	 ('[bk-2-]->[bk-3+]','bk-2','-','bk-3','+',NULL,false,NULL);
INSERT INTO jcs.routes (id,from_tile_id,from_suffix,to_tile_id,to_suffix,route_color,locked,status) VALUES
	 ('[bk-2+]->[bk-4-]','bk-2','+','bk-4','-',NULL,false,NULL),
	 ('[bk-4+]->[bk-2-]','bk-4','+','bk-2','-',NULL,false,NULL),
	 ('[bk-2+]->[bk-3-]','bk-2','+','bk-3','-',NULL,false,NULL),
	 ('[bk-4+]->[bk-1-]','bk-4','+','bk-1','-',NULL,false,NULL),
	 ('[bk-1+]->[bk-4-]','bk-1','+','bk-4','-',NULL,false,NULL),
	 ('[bk-3-]->[bk-1+]','bk-3','-','bk-1','+',NULL,false,NULL);

INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3+]->[bk-1-]','bk-3','bk-3',NULL,0,NULL),
	 ('[bk-3+]->[bk-1-]','bk-3+','bk-3',NULL,1,NULL),
	 ('[bk-3+]->[bk-1-]','se-5','se-5',NULL,2,'East'),
	 ('[bk-3+]->[bk-1-]','st-9','st-9',NULL,3,'East'),
	 ('[bk-3+]->[bk-1-]','sw-4','sw-4','G',4,'East'),
	 ('[bk-3+]->[bk-1-]','st-14','st-14',NULL,5,'East'),
	 ('[bk-3+]->[bk-1-]','ct-7','ct-7',NULL,6,'East'),
	 ('[bk-3+]->[bk-1-]','st-7','st-7',NULL,7,'South'),
	 ('[bk-3+]->[bk-1-]','ct-3','ct-3',NULL,8,'South'),
	 ('[bk-3+]->[bk-1-]','st-5','st-5',NULL,9,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3+]->[bk-1-]','sw-1','sw-1','R',10,'West'),
	 ('[bk-3+]->[bk-1-]','ct-1','ct-1',NULL,11,'South'),
	 ('[bk-3+]->[bk-1-]','st-1','st-1',NULL,12,'West'),
	 ('[bk-3+]->[bk-1-]','se-1','se-1',NULL,13,'West'),
	 ('[bk-3+]->[bk-1-]','bk-1-','bk-1',NULL,14,'West'),
	 ('[bk-1-]->[bk-3+]','bk-1','bk-1',NULL,0,NULL),
	 ('[bk-1-]->[bk-3+]','bk-1-','bk-1',NULL,1,NULL),
	 ('[bk-1-]->[bk-3+]','se-1','se-1',NULL,2,'East'),
	 ('[bk-1-]->[bk-3+]','st-1','st-1',NULL,3,'East'),
	 ('[bk-1-]->[bk-3+]','ct-1','ct-1',NULL,4,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1-]->[bk-3+]','sw-1','sw-1','R',5,'North'),
	 ('[bk-1-]->[bk-3+]','st-5','st-5',NULL,6,'East'),
	 ('[bk-1-]->[bk-3+]','ct-3','ct-3',NULL,7,'East'),
	 ('[bk-1-]->[bk-3+]','st-7','st-7',NULL,8,'North'),
	 ('[bk-1-]->[bk-3+]','ct-7','ct-7',NULL,9,'North'),
	 ('[bk-1-]->[bk-3+]','st-14','st-14',NULL,10,'West'),
	 ('[bk-1-]->[bk-3+]','sw-4','sw-4','G',11,'West'),
	 ('[bk-1-]->[bk-3+]','st-9','st-9',NULL,12,'West'),
	 ('[bk-1-]->[bk-3+]','se-5','se-5',NULL,13,'West'),
	 ('[bk-1-]->[bk-3+]','bk-3+','bk-3',NULL,14,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3-]->[bk-2+]','bk-3','bk-3',NULL,0,NULL),
	 ('[bk-3-]->[bk-2+]','bk-3-','bk-3',NULL,1,NULL),
	 ('[bk-3-]->[bk-2+]','se-6','se-6',NULL,2,'West'),
	 ('[bk-3-]->[bk-2+]','st-12','st-12',NULL,3,'West'),
	 ('[bk-3-]->[bk-2+]','sw-3','sw-3','G',4,'West'),
	 ('[bk-3-]->[bk-2+]','st-13','st-13',NULL,5,'West'),
	 ('[bk-3-]->[bk-2+]','ct-8','ct-8',NULL,6,'West'),
	 ('[bk-3-]->[bk-2+]','st-8','st-8',NULL,7,'South'),
	 ('[bk-3-]->[bk-2+]','ct-4','ct-4',NULL,8,'South'),
	 ('[bk-3-]->[bk-2+]','st-6','st-6',NULL,9,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3-]->[bk-2+]','sw-2','sw-2','G',10,'East'),
	 ('[bk-3-]->[bk-2+]','st-4','st-4',NULL,11,'East'),
	 ('[bk-3-]->[bk-2+]','se-4','se-4',NULL,12,'East'),
	 ('[bk-3-]->[bk-2+]','bk-2+','bk-2',NULL,13,'East'),
	 ('[bk-1-]->[bk-4+]','bk-1','bk-1',NULL,0,NULL),
	 ('[bk-1-]->[bk-4+]','bk-1-','bk-1',NULL,1,NULL),
	 ('[bk-1-]->[bk-4+]','se-1','se-1',NULL,2,'East'),
	 ('[bk-1-]->[bk-4+]','st-1','st-1',NULL,3,'East'),
	 ('[bk-1-]->[bk-4+]','ct-1','ct-1',NULL,4,'East'),
	 ('[bk-1-]->[bk-4+]','sw-1','sw-1','R',5,'North');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1-]->[bk-4+]','st-5','st-5',NULL,6,'East'),
	 ('[bk-1-]->[bk-4+]','ct-3','ct-3',NULL,7,'East'),
	 ('[bk-1-]->[bk-4+]','st-7','st-7',NULL,8,'North'),
	 ('[bk-1-]->[bk-4+]','ct-7','ct-7',NULL,9,'North'),
	 ('[bk-1-]->[bk-4+]','st-14','st-14',NULL,10,'West'),
	 ('[bk-1-]->[bk-4+]','sw-4','sw-4','R',11,'West'),
	 ('[bk-1-]->[bk-4+]','ct-5','ct-5',NULL,12,'North'),
	 ('[bk-1-]->[bk-4+]','st-10','st-10',NULL,13,'West'),
	 ('[bk-1-]->[bk-4+]','se-7','se-7',NULL,14,'West'),
	 ('[bk-1-]->[bk-4+]','bk-4+','bk-4',NULL,15,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3+]->[bk-2-]','bk-3','bk-3',NULL,0,NULL),
	 ('[bk-3+]->[bk-2-]','bk-3+','bk-3',NULL,1,NULL),
	 ('[bk-3+]->[bk-2-]','se-5','se-5',NULL,2,'East'),
	 ('[bk-3+]->[bk-2-]','st-9','st-9',NULL,3,'East'),
	 ('[bk-3+]->[bk-2-]','sw-4','sw-4','G',4,'East'),
	 ('[bk-3+]->[bk-2-]','st-14','st-14',NULL,5,'East'),
	 ('[bk-3+]->[bk-2-]','ct-7','ct-7',NULL,6,'East'),
	 ('[bk-3+]->[bk-2-]','st-7','st-7',NULL,7,'South'),
	 ('[bk-3+]->[bk-2-]','ct-3','ct-3',NULL,8,'South'),
	 ('[bk-3+]->[bk-2-]','st-5','st-5',NULL,9,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3+]->[bk-2-]','sw-1','sw-1','G',10,'West'),
	 ('[bk-3+]->[bk-2-]','st-3','st-3',NULL,11,'West'),
	 ('[bk-3+]->[bk-2-]','se-3','se-3',NULL,12,'West'),
	 ('[bk-3+]->[bk-2-]','bk-2-','bk-2',NULL,13,'West'),
	 ('[bk-1+]->[bk-3-]','bk-1','bk-1',NULL,0,NULL),
	 ('[bk-1+]->[bk-3-]','bk-1+','bk-1',NULL,1,NULL),
	 ('[bk-1+]->[bk-3-]','se-2','se-2',NULL,2,'West'),
	 ('[bk-1+]->[bk-3-]','st-2','st-2',NULL,3,'West'),
	 ('[bk-1+]->[bk-3-]','ct-2','ct-2',NULL,4,'West'),
	 ('[bk-1+]->[bk-3-]','sw-2','sw-2','R',5,'North');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1+]->[bk-3-]','st-6','st-6',NULL,6,'West'),
	 ('[bk-1+]->[bk-3-]','ct-4','ct-4',NULL,7,'West'),
	 ('[bk-1+]->[bk-3-]','st-8','st-8',NULL,8,'North'),
	 ('[bk-1+]->[bk-3-]','ct-8','ct-8',NULL,9,'North'),
	 ('[bk-1+]->[bk-3-]','st-13','st-13',NULL,10,'East'),
	 ('[bk-1+]->[bk-3-]','sw-3','sw-3','G',11,'East'),
	 ('[bk-1+]->[bk-3-]','st-12','st-12',NULL,12,'East'),
	 ('[bk-1+]->[bk-3-]','se-6','se-6',NULL,13,'East'),
	 ('[bk-1+]->[bk-3-]','bk-3-','bk-3',NULL,14,'East'),
	 ('[bk-4-]->[bk-1+]','bk-4','bk-4',NULL,0,NULL);
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4-]->[bk-1+]','bk-4-','bk-4',NULL,1,NULL),
	 ('[bk-4-]->[bk-1+]','se-8','se-8',NULL,2,'West'),
	 ('[bk-4-]->[bk-1+]','st-11','st-11',NULL,3,'West'),
	 ('[bk-4-]->[bk-1+]','ct-6','ct-6',NULL,4,'West'),
	 ('[bk-4-]->[bk-1+]','sw-3','sw-3','R',5,'South'),
	 ('[bk-4-]->[bk-1+]','st-13','st-13',NULL,6,'West'),
	 ('[bk-4-]->[bk-1+]','ct-8','ct-8',NULL,7,'West'),
	 ('[bk-4-]->[bk-1+]','st-8','st-8',NULL,8,'South'),
	 ('[bk-4-]->[bk-1+]','ct-4','ct-4',NULL,9,'South'),
	 ('[bk-4-]->[bk-1+]','st-6','st-6',NULL,10,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4-]->[bk-1+]','sw-2','sw-2','R',11,'East'),
	 ('[bk-4-]->[bk-1+]','ct-2','ct-2',NULL,12,'South'),
	 ('[bk-4-]->[bk-1+]','st-2','st-2',NULL,13,'East'),
	 ('[bk-4-]->[bk-1+]','se-2','se-2',NULL,14,'East'),
	 ('[bk-4-]->[bk-1+]','bk-1+','bk-1',NULL,15,'East'),
	 ('[bk-4-]->[bk-2+]','bk-4','bk-4',NULL,0,NULL),
	 ('[bk-4-]->[bk-2+]','bk-4-','bk-4',NULL,1,NULL),
	 ('[bk-4-]->[bk-2+]','se-8','se-8',NULL,2,'West'),
	 ('[bk-4-]->[bk-2+]','st-11','st-11',NULL,3,'West'),
	 ('[bk-4-]->[bk-2+]','ct-6','ct-6',NULL,4,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4-]->[bk-2+]','sw-3','sw-3','R',5,'South'),
	 ('[bk-4-]->[bk-2+]','st-13','st-13',NULL,6,'West'),
	 ('[bk-4-]->[bk-2+]','ct-8','ct-8',NULL,7,'West'),
	 ('[bk-4-]->[bk-2+]','st-8','st-8',NULL,8,'South'),
	 ('[bk-4-]->[bk-2+]','ct-4','ct-4',NULL,9,'South'),
	 ('[bk-4-]->[bk-2+]','st-6','st-6',NULL,10,'East'),
	 ('[bk-4-]->[bk-2+]','sw-2','sw-2','G',11,'East'),
	 ('[bk-4-]->[bk-2+]','st-4','st-4',NULL,12,'East'),
	 ('[bk-4-]->[bk-2+]','se-4','se-4',NULL,13,'East'),
	 ('[bk-4-]->[bk-2+]','bk-2+','bk-2',NULL,14,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2-]->[bk-4+]','bk-2','bk-2',NULL,0,NULL),
	 ('[bk-2-]->[bk-4+]','bk-2-','bk-2',NULL,1,NULL),
	 ('[bk-2-]->[bk-4+]','se-3','se-3',NULL,2,'East'),
	 ('[bk-2-]->[bk-4+]','st-3','st-3',NULL,3,'East'),
	 ('[bk-2-]->[bk-4+]','sw-1','sw-1','G',4,'East'),
	 ('[bk-2-]->[bk-4+]','st-5','st-5',NULL,5,'East'),
	 ('[bk-2-]->[bk-4+]','ct-3','ct-3',NULL,6,'East'),
	 ('[bk-2-]->[bk-4+]','st-7','st-7',NULL,7,'North'),
	 ('[bk-2-]->[bk-4+]','ct-7','ct-7',NULL,8,'North'),
	 ('[bk-2-]->[bk-4+]','st-14','st-14',NULL,9,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2-]->[bk-4+]','sw-4','sw-4','R',10,'West'),
	 ('[bk-2-]->[bk-4+]','ct-5','ct-5',NULL,11,'North'),
	 ('[bk-2-]->[bk-4+]','st-10','st-10',NULL,12,'West'),
	 ('[bk-2-]->[bk-4+]','se-7','se-7',NULL,13,'West'),
	 ('[bk-2-]->[bk-4+]','bk-4+','bk-4',NULL,14,'West'),
	 ('[bk-2-]->[bk-3+]','bk-2','bk-2',NULL,0,NULL),
	 ('[bk-2-]->[bk-3+]','bk-2-','bk-2',NULL,1,NULL),
	 ('[bk-2-]->[bk-3+]','se-3','se-3',NULL,2,'East'),
	 ('[bk-2-]->[bk-3+]','st-3','st-3',NULL,3,'East'),
	 ('[bk-2-]->[bk-3+]','sw-1','sw-1','G',4,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2-]->[bk-3+]','st-5','st-5',NULL,5,'East'),
	 ('[bk-2-]->[bk-3+]','ct-3','ct-3',NULL,6,'East'),
	 ('[bk-2-]->[bk-3+]','st-7','st-7',NULL,7,'North'),
	 ('[bk-2-]->[bk-3+]','ct-7','ct-7',NULL,8,'North'),
	 ('[bk-2-]->[bk-3+]','st-14','st-14',NULL,9,'West'),
	 ('[bk-2-]->[bk-3+]','sw-4','sw-4','G',10,'West'),
	 ('[bk-2-]->[bk-3+]','st-9','st-9',NULL,11,'West'),
	 ('[bk-2-]->[bk-3+]','se-5','se-5',NULL,12,'West'),
	 ('[bk-2-]->[bk-3+]','bk-3+','bk-3',NULL,13,'West'),
	 ('[bk-2+]->[bk-4-]','bk-2','bk-2',NULL,0,NULL);
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-4-]','bk-2+','bk-2',NULL,1,NULL),
	 ('[bk-2+]->[bk-4-]','se-4','se-4',NULL,2,'West'),
	 ('[bk-2+]->[bk-4-]','st-4','st-4',NULL,3,'West'),
	 ('[bk-2+]->[bk-4-]','sw-2','sw-2','G',4,'West'),
	 ('[bk-2+]->[bk-4-]','st-6','st-6',NULL,5,'West'),
	 ('[bk-2+]->[bk-4-]','ct-4','ct-4',NULL,6,'West'),
	 ('[bk-2+]->[bk-4-]','st-8','st-8',NULL,7,'North'),
	 ('[bk-2+]->[bk-4-]','ct-8','ct-8',NULL,8,'North'),
	 ('[bk-2+]->[bk-4-]','st-13','st-13',NULL,9,'East'),
	 ('[bk-2+]->[bk-4-]','sw-3','sw-3','R',10,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-4-]','ct-6','ct-6',NULL,11,'North'),
	 ('[bk-2+]->[bk-4-]','st-11','st-11',NULL,12,'East'),
	 ('[bk-2+]->[bk-4-]','se-8','se-8',NULL,13,'East'),
	 ('[bk-2+]->[bk-4-]','bk-4-','bk-4',NULL,14,'East'),
	 ('[bk-4+]->[bk-2-]','bk-4','bk-4',NULL,0,NULL),
	 ('[bk-4+]->[bk-2-]','bk-4+','bk-4',NULL,1,NULL),
	 ('[bk-4+]->[bk-2-]','se-7','se-7',NULL,2,'East'),
	 ('[bk-4+]->[bk-2-]','st-10','st-10',NULL,3,'East'),
	 ('[bk-4+]->[bk-2-]','ct-5','ct-5',NULL,4,'East'),
	 ('[bk-4+]->[bk-2-]','sw-4','sw-4','R',5,'South');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4+]->[bk-2-]','st-14','st-14',NULL,6,'East'),
	 ('[bk-4+]->[bk-2-]','ct-7','ct-7',NULL,7,'East'),
	 ('[bk-4+]->[bk-2-]','st-7','st-7',NULL,8,'South'),
	 ('[bk-4+]->[bk-2-]','ct-3','ct-3',NULL,9,'South'),
	 ('[bk-4+]->[bk-2-]','st-5','st-5',NULL,10,'West'),
	 ('[bk-4+]->[bk-2-]','sw-1','sw-1','G',11,'West'),
	 ('[bk-4+]->[bk-2-]','st-3','st-3',NULL,12,'West'),
	 ('[bk-4+]->[bk-2-]','se-3','se-3',NULL,13,'West'),
	 ('[bk-4+]->[bk-2-]','bk-2-','bk-2',NULL,14,'West'),
	 ('[bk-2+]->[bk-3-]','bk-2','bk-2',NULL,0,NULL);
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-3-]','bk-2+','bk-2',NULL,1,NULL),
	 ('[bk-2+]->[bk-3-]','se-4','se-4',NULL,2,'West'),
	 ('[bk-2+]->[bk-3-]','st-4','st-4',NULL,3,'West'),
	 ('[bk-2+]->[bk-3-]','sw-2','sw-2','G',4,'West'),
	 ('[bk-2+]->[bk-3-]','st-6','st-6',NULL,5,'West'),
	 ('[bk-2+]->[bk-3-]','ct-4','ct-4',NULL,6,'West'),
	 ('[bk-2+]->[bk-3-]','st-8','st-8',NULL,7,'North'),
	 ('[bk-2+]->[bk-3-]','ct-8','ct-8',NULL,8,'North'),
	 ('[bk-2+]->[bk-3-]','st-13','st-13',NULL,9,'East'),
	 ('[bk-2+]->[bk-3-]','sw-3','sw-3','G',10,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-3-]','st-12','st-12',NULL,11,'East'),
	 ('[bk-2+]->[bk-3-]','se-6','se-6',NULL,12,'East'),
	 ('[bk-2+]->[bk-3-]','bk-3-','bk-3',NULL,13,'East'),
	 ('[bk-4+]->[bk-1-]','bk-4','bk-4',NULL,0,NULL),
	 ('[bk-4+]->[bk-1-]','bk-4+','bk-4',NULL,1,NULL),
	 ('[bk-4+]->[bk-1-]','se-7','se-7',NULL,2,'East'),
	 ('[bk-4+]->[bk-1-]','st-10','st-10',NULL,3,'East'),
	 ('[bk-4+]->[bk-1-]','ct-5','ct-5',NULL,4,'East'),
	 ('[bk-4+]->[bk-1-]','sw-4','sw-4','R',5,'South'),
	 ('[bk-4+]->[bk-1-]','st-14','st-14',NULL,6,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4+]->[bk-1-]','ct-7','ct-7',NULL,7,'East'),
	 ('[bk-4+]->[bk-1-]','st-7','st-7',NULL,8,'South'),
	 ('[bk-4+]->[bk-1-]','ct-3','ct-3',NULL,9,'South'),
	 ('[bk-4+]->[bk-1-]','st-5','st-5',NULL,10,'West'),
	 ('[bk-4+]->[bk-1-]','sw-1','sw-1','R',11,'West'),
	 ('[bk-4+]->[bk-1-]','ct-1','ct-1',NULL,12,'South'),
	 ('[bk-4+]->[bk-1-]','st-1','st-1',NULL,13,'West'),
	 ('[bk-4+]->[bk-1-]','se-1','se-1',NULL,14,'West'),
	 ('[bk-4+]->[bk-1-]','bk-1-','bk-1',NULL,15,'West'),
	 ('[bk-1+]->[bk-4-]','bk-1','bk-1',NULL,0,NULL);
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1+]->[bk-4-]','bk-1+','bk-1',NULL,1,NULL),
	 ('[bk-1+]->[bk-4-]','se-2','se-2',NULL,2,'West'),
	 ('[bk-1+]->[bk-4-]','st-2','st-2',NULL,3,'West'),
	 ('[bk-1+]->[bk-4-]','ct-2','ct-2',NULL,4,'West'),
	 ('[bk-1+]->[bk-4-]','sw-2','sw-2','R',5,'North'),
	 ('[bk-1+]->[bk-4-]','st-6','st-6',NULL,6,'West'),
	 ('[bk-1+]->[bk-4-]','ct-4','ct-4',NULL,7,'West'),
	 ('[bk-1+]->[bk-4-]','st-8','st-8',NULL,8,'North'),
	 ('[bk-1+]->[bk-4-]','ct-8','ct-8',NULL,9,'North'),
	 ('[bk-1+]->[bk-4-]','st-13','st-13',NULL,10,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1+]->[bk-4-]','sw-3','sw-3','R',11,'East'),
	 ('[bk-1+]->[bk-4-]','ct-6','ct-6',NULL,12,'North'),
	 ('[bk-1+]->[bk-4-]','st-11','st-11',NULL,13,'East'),
	 ('[bk-1+]->[bk-4-]','se-8','se-8',NULL,14,'East'),
	 ('[bk-1+]->[bk-4-]','bk-4-','bk-4',NULL,15,'East'),
	 ('[bk-3-]->[bk-1+]','bk-3','bk-3',NULL,0,NULL),
	 ('[bk-3-]->[bk-1+]','bk-3-','bk-3',NULL,1,NULL),
	 ('[bk-3-]->[bk-1+]','se-6','se-6',NULL,2,'West'),
	 ('[bk-3-]->[bk-1+]','st-12','st-12',NULL,3,'West'),
	 ('[bk-3-]->[bk-1+]','sw-3','sw-3','G',4,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3-]->[bk-1+]','st-13','st-13',NULL,5,'West'),
	 ('[bk-3-]->[bk-1+]','ct-8','ct-8',NULL,6,'West'),
	 ('[bk-3-]->[bk-1+]','st-8','st-8',NULL,7,'South'),
	 ('[bk-3-]->[bk-1+]','ct-4','ct-4',NULL,8,'South'),
	 ('[bk-3-]->[bk-1+]','st-6','st-6',NULL,9,'East'),
	 ('[bk-3-]->[bk-1+]','sw-2','sw-2','R',10,'East'),
	 ('[bk-3-]->[bk-1+]','ct-2','ct-2',NULL,11,'South'),
	 ('[bk-3-]->[bk-1+]','st-2','st-2',NULL,12,'East'),
	 ('[bk-3-]->[bk-1+]','se-2','se-2',NULL,13,'East'),
	 ('[bk-3-]->[bk-1+]','bk-1+','bk-1',NULL,14,'East');

commit;

