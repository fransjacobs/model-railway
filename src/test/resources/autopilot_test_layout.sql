delete from route_elements;
delete from routes;
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

INSERT INTO jcs.accessories (id,address,name,"type",state,states,switch_time,protocol,decoder,accessory_group,icon,icon_file,imported,command_station_id,synchronize) VALUES
	 ('002',2,'W02','rechtsweiche',0,2,200,'dcc',NULL,'other',NULL,NULL,NULL,'virtual',false),
	 ('001',1,'W01','linksweiche',1,2,200,'dcc',NULL,'other',NULL,NULL,NULL,'virtual',false),
	 ('003',3,'W03','linksweiche',1,2,200,'dcc',NULL,'other',NULL,NULL,NULL,'virtual',false);
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
	 ('se-1','Sensor','East','Center',140,60,NULL,NULL,1),
	 ('ct-2','Curved','South','Center',380,60,NULL,NULL,NULL),
	 ('sw-1','Switch','West','Left',540,100,NULL,'001',NULL),
	 ('bk-2','Block','East','Center',220,100,NULL,NULL,NULL),
	 ('se-2','Sensor','East','Center',300,60,NULL,NULL,2),
	 ('st-2','Straight','East','Center',340,60,NULL,NULL,NULL),
	 ('sw-2','Switch','East','Right',380,100,NULL,'002',NULL),
	 ('st-5','Straight','West','Center',100,100,NULL,NULL,NULL),
	 ('st-4','Straight','East','Center',420,100,NULL,NULL,NULL),
	 ('se-4','Sensor','East','Center',300,100,NULL,NULL,4);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-1','Straight','East','Center',100,60,NULL,NULL,NULL),
	 ('bk-3','Block','East','Center',700,60,NULL,NULL,NULL),
	 ('se-3','Sensor','East','Center',140,100,NULL,NULL,3),
	 ('bk-1','Block','West','Center',220,60,NULL,NULL,NULL),
	 ('ct-1','Curved','East','Center',540,60,NULL,NULL,NULL),
	 ('st-3','Straight','East','Center',340,100,NULL,NULL,NULL),
	 ('st-11','Straight','West','Center',820,60,NULL,NULL,NULL),
	 ('st-13','Straight','West','Center',580,60,NULL,NULL,NULL),
	 ('st-14','Straight','West','Center',820,100,NULL,NULL,NULL),
	 ('se-5','Sensor','West','Center',620,60,NULL,NULL,11);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-6','Sensor','West','Center',780,60,NULL,NULL,10),
	 ('et-5','End','West','Center',60,60,NULL,NULL,NULL),
	 ('se-9','Sensor','West','Center',780,100,NULL,NULL,12),
	 ('et-7','End','East','Center',860,100,NULL,NULL,NULL),
	 ('et-6','End','East','Center',860,60,NULL,NULL,NULL),
	 ('et-8','End','West','Center',60,100,NULL,NULL,NULL),
	 ('se-10','Sensor','West','Center',620,100,NULL,NULL,13),
	 ('st-17','Straight','West','Center',580,100,NULL,NULL,NULL),
	 ('bk-4','Block','West','Center',700,100,NULL,NULL,NULL),
	 ('bk-5','Block','South','Center',460,260,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('sw-3','Switch','West','Right',460,100,'NONE','003',NULL),
	 ('st-18','Straight','West','Center',500,100,NULL,NULL,NULL),
	 ('st-19','Straight','North','Center',460,140,NULL,NULL,NULL),
	 ('se-11','Sensor','North','Center',460,180,NULL,NULL,5),
	 ('se-12','Sensor','South','Center',460,340,NULL,NULL,6),
	 ('st-20','Straight','South','Center',460,380,NULL,NULL,NULL),
	 ('se-13','Sensor','South','Center',460,420,NULL,NULL,7),
	 ('bk-6','Block','South','Center',460,500,NULL,NULL,NULL),
	 ('se-14','Sensor','South','Center',460,580,NULL,NULL,8),
	 ('st-22','Straight','South','Center',460,620,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('et-9','End','South','Center',460,660,NULL,NULL,NULL);
commit;

INSERT INTO jcs.blocks (id,tile_id,description,plus_sensor_id,min_sensor_id,plus_signal_id,min_signal_id,locomotive_id,allow_non_commuter_only,status,incoming_suffix,min_wait_time,max_wait_time,random_wait,always_stop,allow_commuter_only,logical_direction) VALUES
	 ('bk-1','bk-1','Blok 1',1,2,NULL,NULL,NULL,false,'Free',NULL,10,NULL,false,true,true,NULL),
	 ('bk-2','bk-2','Blok 2',4,3,NULL,NULL,NULL,false,'Free',NULL,10,NULL,false,true,true,NULL),
	 ('bk-3','bk-3','Blok 3',10,11,NULL,NULL,NULL,false,'Free',NULL,10,NULL,false,true,true,NULL),
	 ('bk-4','bk-4','Blok 4',13,12,NULL,NULL,NULL,false,'Free',NULL,10,NULL,false,true,true,NULL),
	 ('bk-5','bk-5','Blok 5',6,5,NULL,NULL,NULL,true,'Free',NULL,10,NULL,false,false,true,NULL),
	 ('bk-6','bk-6','Blok 6',8,7,NULL,NULL,NULL,false,'Free',NULL,10,NULL,false,true,true,NULL);
commit;

INSERT INTO jcs.routes (id,from_tile_id,from_suffix,to_tile_id,to_suffix,route_color,locked,status) VALUES
	 ('[bk-1-]->[bk-3-]','bk-1','-','bk-3','-',NULL,false,NULL),
	 ('[bk-5+]->[bk-6-]','bk-5','+','bk-6','-',NULL,false,NULL),
	 ('[bk-1-]->[bk-5-]','bk-1','-','bk-5','-',NULL,false,NULL),
	 ('[bk-3-]->[bk-2+]','bk-3','-','bk-2','+',NULL,false,NULL),
	 ('[bk-1-]->[bk-4+]','bk-1','-','bk-4','+',NULL,false,NULL),
	 ('[bk-2+]->[bk-4+]','bk-2','+','bk-4','+',NULL,false,NULL),
	 ('[bk-2+]->[bk-5-]','bk-2','+','bk-5','-',NULL,false,NULL),
	 ('[bk-4+]->[bk-2+]','bk-4','+','bk-2','+',NULL,false,NULL),
	 ('[bk-5-]->[bk-1-]','bk-5','-','bk-1','-',NULL,false,NULL),
	 ('[bk-5-]->[bk-2+]','bk-5','-','bk-2','+',NULL,false,NULL);
INSERT INTO jcs.routes (id,from_tile_id,from_suffix,to_tile_id,to_suffix,route_color,locked,status) VALUES
	 ('[bk-2+]->[bk-3-]','bk-2','+','bk-3','-',NULL,false,NULL),
	 ('[bk-4+]->[bk-1-]','bk-4','+','bk-1','-',NULL,false,NULL),
	 ('[bk-3-]->[bk-1-]','bk-3','-','bk-1','-',NULL,false,NULL),
	 ('[bk-6-]->[bk-5+]','bk-6','-','bk-5','+',NULL,false,NULL);
commit;

INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1-]->[bk-3-]','bk-1','bk-1',NULL,0,NULL),
	 ('[bk-1-]->[bk-3-]','bk-1-','bk-1',NULL,1,NULL),
	 ('[bk-1-]->[bk-3-]','se-2','se-2',NULL,2,'West'),
	 ('[bk-1-]->[bk-3-]','st-2','st-2',NULL,3,'West'),
	 ('[bk-1-]->[bk-3-]','ct-2','ct-2',NULL,4,'West'),
	 ('[bk-1-]->[bk-3-]','sw-2','sw-2','R',5,'North'),
	 ('[bk-1-]->[bk-3-]','st-4','st-4',NULL,6,'West'),
	 ('[bk-1-]->[bk-3-]','sw-3','sw-3','G',7,'West'),
	 ('[bk-1-]->[bk-3-]','st-18','st-18',NULL,8,'West'),
	 ('[bk-1-]->[bk-3-]','sw-1','sw-1','R',9,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1-]->[bk-3-]','ct-1','ct-1',NULL,10,'South'),
	 ('[bk-1-]->[bk-3-]','st-13','st-13',NULL,11,'West'),
	 ('[bk-1-]->[bk-3-]','se-5','se-5',NULL,12,'West'),
	 ('[bk-1-]->[bk-3-]','bk-3-','bk-3',NULL,13,'West'),
	 ('[bk-5+]->[bk-6-]','bk-5','bk-5',NULL,0,NULL),
	 ('[bk-5+]->[bk-6-]','bk-5+','bk-5',NULL,1,NULL),
	 ('[bk-5+]->[bk-6-]','se-12','se-12',NULL,2,'North'),
	 ('[bk-5+]->[bk-6-]','st-20','st-20',NULL,3,'North'),
	 ('[bk-5+]->[bk-6-]','se-13','se-13',NULL,4,'North'),
	 ('[bk-5+]->[bk-6-]','bk-6-','bk-6',NULL,5,'North');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1-]->[bk-5-]','bk-1','bk-1',NULL,0,NULL),
	 ('[bk-1-]->[bk-5-]','bk-1-','bk-1',NULL,1,NULL),
	 ('[bk-1-]->[bk-5-]','se-2','se-2',NULL,2,'West'),
	 ('[bk-1-]->[bk-5-]','st-2','st-2',NULL,3,'West'),
	 ('[bk-1-]->[bk-5-]','ct-2','ct-2',NULL,4,'West'),
	 ('[bk-1-]->[bk-5-]','sw-2','sw-2','R',5,'North'),
	 ('[bk-1-]->[bk-5-]','st-4','st-4',NULL,6,'West'),
	 ('[bk-1-]->[bk-5-]','sw-3','sw-3','R',7,'West'),
	 ('[bk-1-]->[bk-5-]','st-19','st-19',NULL,8,'North'),
	 ('[bk-1-]->[bk-5-]','se-11','se-11',NULL,9,'North');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1-]->[bk-5-]','bk-5-','bk-5',NULL,10,'North'),
	 ('[bk-3-]->[bk-2+]','bk-3','bk-3',NULL,0,NULL),
	 ('[bk-3-]->[bk-2+]','bk-3-','bk-3',NULL,1,NULL),
	 ('[bk-3-]->[bk-2+]','se-5','se-5',NULL,2,'East'),
	 ('[bk-3-]->[bk-2+]','st-13','st-13',NULL,3,'East'),
	 ('[bk-3-]->[bk-2+]','ct-1','ct-1',NULL,4,'East'),
	 ('[bk-3-]->[bk-2+]','sw-1','sw-1','R',5,'North'),
	 ('[bk-3-]->[bk-2+]','st-18','st-18',NULL,6,'East'),
	 ('[bk-3-]->[bk-2+]','sw-3','sw-3','G',7,'East'),
	 ('[bk-3-]->[bk-2+]','st-4','st-4',NULL,8,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3-]->[bk-2+]','sw-2','sw-2','G',9,'East'),
	 ('[bk-3-]->[bk-2+]','st-3','st-3',NULL,10,'East'),
	 ('[bk-3-]->[bk-2+]','se-4','se-4',NULL,11,'East'),
	 ('[bk-3-]->[bk-2+]','bk-2+','bk-2',NULL,12,'East'),
	 ('[bk-1-]->[bk-4+]','bk-1','bk-1',NULL,0,NULL),
	 ('[bk-1-]->[bk-4+]','bk-1-','bk-1',NULL,1,NULL),
	 ('[bk-1-]->[bk-4+]','se-2','se-2',NULL,2,'West'),
	 ('[bk-1-]->[bk-4+]','st-2','st-2',NULL,3,'West'),
	 ('[bk-1-]->[bk-4+]','ct-2','ct-2',NULL,4,'West'),
	 ('[bk-1-]->[bk-4+]','sw-2','sw-2','R',5,'North');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1-]->[bk-4+]','st-4','st-4',NULL,6,'West'),
	 ('[bk-1-]->[bk-4+]','sw-3','sw-3','G',7,'West'),
	 ('[bk-1-]->[bk-4+]','st-18','st-18',NULL,8,'West'),
	 ('[bk-1-]->[bk-4+]','sw-1','sw-1','G',9,'West'),
	 ('[bk-1-]->[bk-4+]','st-17','st-17',NULL,10,'West'),
	 ('[bk-1-]->[bk-4+]','se-10','se-10',NULL,11,'West'),
	 ('[bk-1-]->[bk-4+]','bk-4+','bk-4',NULL,12,'West'),
	 ('[bk-2+]->[bk-4+]','bk-2','bk-2',NULL,0,NULL),
	 ('[bk-2+]->[bk-4+]','bk-2+','bk-2',NULL,1,NULL),
	 ('[bk-2+]->[bk-4+]','se-4','se-4',NULL,2,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-4+]','st-3','st-3',NULL,3,'West'),
	 ('[bk-2+]->[bk-4+]','sw-2','sw-2','G',4,'West'),
	 ('[bk-2+]->[bk-4+]','st-4','st-4',NULL,5,'West'),
	 ('[bk-2+]->[bk-4+]','sw-3','sw-3','G',6,'West'),
	 ('[bk-2+]->[bk-4+]','st-18','st-18',NULL,7,'West'),
	 ('[bk-2+]->[bk-4+]','sw-1','sw-1','G',8,'West'),
	 ('[bk-2+]->[bk-4+]','st-17','st-17',NULL,9,'West'),
	 ('[bk-2+]->[bk-4+]','se-10','se-10',NULL,10,'West'),
	 ('[bk-2+]->[bk-4+]','bk-4+','bk-4',NULL,11,'West'),
	 ('[bk-2+]->[bk-5-]','bk-2','bk-2',NULL,0,NULL);
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-5-]','bk-2+','bk-2',NULL,1,NULL),
	 ('[bk-2+]->[bk-5-]','se-4','se-4',NULL,2,'West'),
	 ('[bk-2+]->[bk-5-]','st-3','st-3',NULL,3,'West'),
	 ('[bk-2+]->[bk-5-]','sw-2','sw-2','G',4,'West'),
	 ('[bk-2+]->[bk-5-]','st-4','st-4',NULL,5,'West'),
	 ('[bk-2+]->[bk-5-]','sw-3','sw-3','R',6,'West'),
	 ('[bk-2+]->[bk-5-]','st-19','st-19',NULL,7,'North'),
	 ('[bk-2+]->[bk-5-]','se-11','se-11',NULL,8,'North'),
	 ('[bk-2+]->[bk-5-]','bk-5-','bk-5',NULL,9,'North'),
	 ('[bk-4+]->[bk-2+]','bk-4','bk-4',NULL,0,NULL);
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4+]->[bk-2+]','bk-4+','bk-4',NULL,1,NULL),
	 ('[bk-4+]->[bk-2+]','se-10','se-10',NULL,2,'East'),
	 ('[bk-4+]->[bk-2+]','st-17','st-17',NULL,3,'East'),
	 ('[bk-4+]->[bk-2+]','sw-1','sw-1','G',4,'East'),
	 ('[bk-4+]->[bk-2+]','st-18','st-18',NULL,5,'East'),
	 ('[bk-4+]->[bk-2+]','sw-3','sw-3','G',6,'East'),
	 ('[bk-4+]->[bk-2+]','st-4','st-4',NULL,7,'East'),
	 ('[bk-4+]->[bk-2+]','sw-2','sw-2','G',8,'East'),
	 ('[bk-4+]->[bk-2+]','st-3','st-3',NULL,9,'East'),
	 ('[bk-4+]->[bk-2+]','se-4','se-4',NULL,10,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4+]->[bk-2+]','bk-2+','bk-2',NULL,11,'East'),
	 ('[bk-5-]->[bk-1-]','bk-5','bk-5',NULL,0,NULL),
	 ('[bk-5-]->[bk-1-]','bk-5-','bk-5',NULL,1,NULL),
	 ('[bk-5-]->[bk-1-]','se-11','se-11',NULL,2,'South'),
	 ('[bk-5-]->[bk-1-]','st-19','st-19',NULL,3,'South'),
	 ('[bk-5-]->[bk-1-]','sw-3','sw-3','R',4,'South'),
	 ('[bk-5-]->[bk-1-]','st-4','st-4',NULL,5,'East'),
	 ('[bk-5-]->[bk-1-]','sw-2','sw-2','R',6,'East'),
	 ('[bk-5-]->[bk-1-]','ct-2','ct-2',NULL,7,'South'),
	 ('[bk-5-]->[bk-1-]','st-2','st-2',NULL,8,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-5-]->[bk-1-]','se-2','se-2',NULL,9,'East'),
	 ('[bk-5-]->[bk-1-]','bk-1-','bk-1',NULL,10,'East'),
	 ('[bk-5-]->[bk-2+]','bk-5','bk-5',NULL,0,NULL),
	 ('[bk-5-]->[bk-2+]','bk-5-','bk-5',NULL,1,NULL),
	 ('[bk-5-]->[bk-2+]','se-11','se-11',NULL,2,'South'),
	 ('[bk-5-]->[bk-2+]','st-19','st-19',NULL,3,'South'),
	 ('[bk-5-]->[bk-2+]','sw-3','sw-3','R',4,'South'),
	 ('[bk-5-]->[bk-2+]','st-4','st-4',NULL,5,'East'),
	 ('[bk-5-]->[bk-2+]','sw-2','sw-2','G',6,'East'),
	 ('[bk-5-]->[bk-2+]','st-3','st-3',NULL,7,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-5-]->[bk-2+]','se-4','se-4',NULL,8,'East'),
	 ('[bk-5-]->[bk-2+]','bk-2+','bk-2',NULL,9,'East'),
	 ('[bk-2+]->[bk-3-]','bk-2','bk-2',NULL,0,NULL),
	 ('[bk-2+]->[bk-3-]','bk-2+','bk-2',NULL,1,NULL),
	 ('[bk-2+]->[bk-3-]','se-4','se-4',NULL,2,'West'),
	 ('[bk-2+]->[bk-3-]','st-3','st-3',NULL,3,'West'),
	 ('[bk-2+]->[bk-3-]','sw-2','sw-2','G',4,'West'),
	 ('[bk-2+]->[bk-3-]','st-4','st-4',NULL,5,'West'),
	 ('[bk-2+]->[bk-3-]','sw-3','sw-3','G',6,'West'),
	 ('[bk-2+]->[bk-3-]','st-18','st-18',NULL,7,'West');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-3-]','sw-1','sw-1','R',8,'West'),
	 ('[bk-2+]->[bk-3-]','ct-1','ct-1',NULL,9,'South'),
	 ('[bk-2+]->[bk-3-]','st-13','st-13',NULL,10,'West'),
	 ('[bk-2+]->[bk-3-]','se-5','se-5',NULL,11,'West'),
	 ('[bk-2+]->[bk-3-]','bk-3-','bk-3',NULL,12,'West'),
	 ('[bk-4+]->[bk-1-]','bk-4','bk-4',NULL,0,NULL),
	 ('[bk-4+]->[bk-1-]','bk-4+','bk-4',NULL,1,NULL),
	 ('[bk-4+]->[bk-1-]','se-10','se-10',NULL,2,'East'),
	 ('[bk-4+]->[bk-1-]','st-17','st-17',NULL,3,'East'),
	 ('[bk-4+]->[bk-1-]','sw-1','sw-1','G',4,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4+]->[bk-1-]','st-18','st-18',NULL,5,'East'),
	 ('[bk-4+]->[bk-1-]','sw-3','sw-3','G',6,'East'),
	 ('[bk-4+]->[bk-1-]','st-4','st-4',NULL,7,'East'),
	 ('[bk-4+]->[bk-1-]','sw-2','sw-2','R',8,'East'),
	 ('[bk-4+]->[bk-1-]','ct-2','ct-2',NULL,9,'South'),
	 ('[bk-4+]->[bk-1-]','st-2','st-2',NULL,10,'East'),
	 ('[bk-4+]->[bk-1-]','se-2','se-2',NULL,11,'East'),
	 ('[bk-4+]->[bk-1-]','bk-1-','bk-1',NULL,12,'East'),
	 ('[bk-3-]->[bk-1-]','bk-3','bk-3',NULL,0,NULL),
	 ('[bk-3-]->[bk-1-]','bk-3-','bk-3',NULL,1,NULL);
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3-]->[bk-1-]','se-5','se-5',NULL,2,'East'),
	 ('[bk-3-]->[bk-1-]','st-13','st-13',NULL,3,'East'),
	 ('[bk-3-]->[bk-1-]','ct-1','ct-1',NULL,4,'East'),
	 ('[bk-3-]->[bk-1-]','sw-1','sw-1','R',5,'North'),
	 ('[bk-3-]->[bk-1-]','st-18','st-18',NULL,6,'East'),
	 ('[bk-3-]->[bk-1-]','sw-3','sw-3','G',7,'East'),
	 ('[bk-3-]->[bk-1-]','st-4','st-4',NULL,8,'East'),
	 ('[bk-3-]->[bk-1-]','sw-2','sw-2','R',9,'East'),
	 ('[bk-3-]->[bk-1-]','ct-2','ct-2',NULL,10,'South'),
	 ('[bk-3-]->[bk-1-]','st-2','st-2',NULL,11,'East');
INSERT INTO jcs.route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3-]->[bk-1-]','se-2','se-2',NULL,12,'East'),
	 ('[bk-3-]->[bk-1-]','bk-1-','bk-1',NULL,13,'East'),
	 ('[bk-6-]->[bk-5+]','bk-6','bk-6',NULL,0,NULL),
	 ('[bk-6-]->[bk-5+]','bk-6-','bk-6',NULL,1,NULL),
	 ('[bk-6-]->[bk-5+]','se-13','se-13',NULL,2,'South'),
	 ('[bk-6-]->[bk-5+]','st-20','st-20',NULL,3,'South'),
	 ('[bk-6-]->[bk-5+]','se-12','se-12',NULL,4,'South'),
	 ('[bk-6-]->[bk-5+]','bk-5+','bk-5',NULL,5,'South');
commit;