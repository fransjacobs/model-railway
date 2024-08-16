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


INSERT INTO accessories (id,address,name,"type",state,states,switch_time,protocol,decoder,accessory_group,icon,icon_file,imported,command_station_id,synchronize)
VALUES ('002',2,'W02','rechtsweiche',0,2,200,'dcc',NULL,'other',NULL,NULL,NULL,'virtual',false), ('001',1,'W01','linksweiche',1,2,200,'dcc',NULL,'other',NULL,NULL,NULL,'virtual',false);
commit;

INSERT INTO locomotives (id,name,uid,address,icon,decoder_type,tacho_max,v_min,velocity,richtung,synchronize,imported,commuter,show,command_station_id) 
VALUES (7,'NS DHG 6505',8,8,'/home/frans/jcs/cache/dcc-ex/ns dhg 6505.png','dcc',100,0,0,2,false,'Manual Updated',true,true,'virtual'),
       (23,'BR 101 003-2',23,23,'/home/frans/jcs/cache/cs/db br 101 109-7.png','mm',200,0,0,2,false,'Manual Updated',true,true,'virtual'),
       (39,'NS 1631',39,39,'/home/frans/jcs/cache/cs/ns 1652.png','dcc',120,0,0,1,false,'Manual Updated',true,true,'virtual');

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


INSERT INTO sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated)
VALUES ('0-0002','B0-S-2',0,2,0,1,NULL,NULL),
       ('0-0013','B0-S-13',0,13,0,1,NULL,NULL),
       ('0-0001','B0-S-1',0,1,0,1,0,NULL),
       ('0-0012','B0-S-12',0,12,0,1,NULL,NULL),
       ('0-0004','B0-S-4',0,4,0,1,NULL,NULL),
       ('0-0015','B0-S-15',0,15,0,NULL,NULL,NULL),
       ('0-0003','B0-S-3',0,3,0,1,0,NULL),
       ('0-0014','B0-S-14',0,14,0,NULL,NULL,NULL),
       ('0-0006','B0-S-6',0,6,0,NULL,NULL,NULL),
       ('0-0005','B0-S-5',0,5,0,NULL,NULL,NULL);
INSERT INTO sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated)
VALUES ('0-0016','B0-S-16',0,16,0,NULL,NULL,NULL),
       ('0-0008','B0-S-8',0,8,0,NULL,NULL,NULL),
       ('0-0007','B0-S-7',0,7,0,NULL,NULL,NULL),
       ('0-0009','B0-S-9',0,9,0,NULL,NULL,NULL),
       ('0-0011','B0-S-11',0,11,0,1,1,null),
       ('0-0010','B0-S-10',0,10,0,1,1,null);
commit;

INSERT INTO tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-1','Sensor','East','Center',260,60,NULL,NULL,'0-0001'),
	 ('ct-2','Curved','South','Center',500,60,NULL,NULL,NULL),
	 ('sw-1','Switch','West','Left',580,100,NULL,'001',NULL),
	 ('bk-2','Block','East','Center',340,100,NULL,NULL,NULL),
	 ('se-2','Sensor','East','Center',420,60,NULL,NULL,'0-0002'),
	 ('st-2','Straight','East','Center',460,60,NULL,NULL,NULL),
	 ('sw-2','Switch','East','Right',500,100,NULL,'002',NULL),
	 ('st-5','Straight','West','Center',220,100,NULL,NULL,NULL),
	 ('st-4','Straight','East','Center',540,100,NULL,NULL,NULL),
	 ('se-4','Sensor','East','Center',420,100,NULL,NULL,'0-0004');
INSERT INTO tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-1','Straight','East','Center',220,60,NULL,NULL,NULL),
	 ('bk-3','Block','East','Center',740,60,NULL,NULL,NULL),
	 ('se-3','Sensor','East','Center',260,100,NULL,NULL,'0-0003'),
	 ('bk-1','Block','West','Center',340,60,NULL,NULL,NULL),
	 ('ct-1','Curved','East','Center',580,60,NULL,NULL,NULL),
	 ('st-3','Straight','East','Center',460,100,NULL,NULL,NULL),
	 ('st-11','Straight','West','Center',860,60,NULL,NULL,NULL),
	 ('st-13','Straight','West','Center',620,60,NULL,NULL,NULL),
	 ('st-14','Straight','West','Center',860,100,NULL,NULL,NULL),
	 ('se-5','Sensor','West','Center',660,60,NULL,NULL,'0-0011');
INSERT INTO tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-6','Sensor','West','Center',820,60,NULL,NULL,'0-0010'),
	 ('et-5','End','West','Center',180,60,NULL,NULL,NULL),
	 ('se-9','Sensor','West','Center',820,100,NULL,NULL,'0-0012'),
	 ('et-7','End','East','Center',900,100,NULL,NULL,NULL),
	 ('et-6','End','East','Center',900,60,NULL,NULL,NULL),
	 ('et-8','End','West','Center',180,100,NULL,NULL,NULL),
	 ('se-10','Sensor','West','Center',660,100,NULL,NULL,'0-0013'),
	 ('st-17','Straight','West','Center',620,100,NULL,NULL,NULL),
	 ('bk-4','Block','West','Center',740,100,NULL,NULL,NULL);
commit;

INSERT INTO blocks (id,tile_id,description,plus_sensor_id,min_sensor_id,plus_signal_id,min_signal_id,locomotive_id,reverse_arrival_side,status,incoming_suffix, always_stop) VALUES
	 ('bk-1','bk-1','Blok 1','0-0001','0-0002',NULL,NULL,NULL,false,'Free',NULL,true),
	 ('bk-2','bk-2','Blok 2','0-0004','0-0003',NULL,NULL,NULL,false,'Free',null,true),
	 ('bk-3','bk-3','Blok 3','0-0010','0-0011',NULL,NULL,NULL,false,'Free',null,true),
	 ('bk-4','bk-4','Blok 4','0-0013','0-0012',NULL,NULL,NULL,false,'Free',NULL,true);
commit;


INSERT INTO routes (id,from_tile_id,from_suffix,to_tile_id,to_suffix,route_color,locked,status) VALUES
	 ('[bk-1-]->[bk-3-]','bk-1','-','bk-3','-',NULL,false,NULL),
	 ('[bk-4+]->[bk-2+]','bk-4','+','bk-2','+',NULL,false,NULL),
	 ('[bk-3-]->[bk-2+]','bk-3','-','bk-2','+',NULL,false,NULL),
	 ('[bk-1-]->[bk-4+]','bk-1','-','bk-4','+',NULL,false,NULL),
	 ('[bk-2+]->[bk-3-]','bk-2','+','bk-3','-',NULL,false,NULL),
	 ('[bk-4+]->[bk-1-]','bk-4','+','bk-1','-',NULL,false,NULL),
	 ('[bk-2+]->[bk-4+]','bk-2','+','bk-4','+',NULL,false,NULL),
	 ('[bk-3-]->[bk-1-]','bk-3','-','bk-1','-',NULL,false,NULL);
commit;

INSERT INTO route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4+]->[bk-2+]','bk-4','bk-4',NULL,0,NULL),
	 ('[bk-4+]->[bk-2+]','bk-4+','bk-4',NULL,1,NULL),
	 ('[bk-4+]->[bk-2+]','se-10','se-10',NULL,2,'East'),
	 ('[bk-4+]->[bk-2+]','st-17','st-17',NULL,3,'East'),
	 ('[bk-4+]->[bk-2+]','sw-1','sw-1','G',4,'East'),
	 ('[bk-4+]->[bk-2+]','st-4','st-4',NULL,5,'East'),
	 ('[bk-4+]->[bk-2+]','sw-2','sw-2','G',6,'East'),
	 ('[bk-4+]->[bk-2+]','st-3','st-3',NULL,7,'East'),
	 ('[bk-4+]->[bk-2+]','se-4','se-4',NULL,8,'East'),
	 ('[bk-4+]->[bk-2+]','bk-2+','bk-2',NULL,9,'East');
INSERT INTO route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-4+]','bk-2','bk-2',NULL,0,NULL),
	 ('[bk-2+]->[bk-4+]','bk-2+','bk-2',NULL,1,NULL),
	 ('[bk-2+]->[bk-4+]','se-4','se-4',NULL,2,'West'),
	 ('[bk-2+]->[bk-4+]','st-3','st-3',NULL,3,'West'),
	 ('[bk-2+]->[bk-4+]','sw-2','sw-2','G',4,'West'),
	 ('[bk-2+]->[bk-4+]','st-4','st-4',NULL,5,'West'),
	 ('[bk-2+]->[bk-4+]','sw-1','sw-1','G',6,'West'),
	 ('[bk-2+]->[bk-4+]','st-17','st-17',NULL,7,'West'),
	 ('[bk-2+]->[bk-4+]','se-10','se-10',NULL,8,'West'),
	 ('[bk-2+]->[bk-4+]','bk-4+','bk-4',NULL,9,'West');
INSERT INTO route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-3-]','bk-2','bk-2',NULL,0,NULL),
	 ('[bk-2+]->[bk-3-]','bk-2+','bk-2',NULL,1,NULL),
	 ('[bk-2+]->[bk-3-]','se-4','se-4',NULL,2,'West'),
	 ('[bk-2+]->[bk-3-]','st-3','st-3',NULL,3,'West'),
	 ('[bk-2+]->[bk-3-]','sw-2','sw-2','G',4,'West'),
	 ('[bk-2+]->[bk-3-]','st-4','st-4',NULL,5,'West'),
	 ('[bk-2+]->[bk-3-]','sw-1','sw-1','R',6,'West'),
	 ('[bk-2+]->[bk-3-]','ct-1','ct-1',NULL,7,'South'),
	 ('[bk-2+]->[bk-3-]','st-13','st-13',NULL,8,'West'),
	 ('[bk-2+]->[bk-3-]','se-5','se-5',NULL,9,'West');
INSERT INTO route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-2+]->[bk-3-]','bk-3-','bk-3',NULL,10,'West'),
	 ('[bk-3-]->[bk-2+]','bk-3','bk-3',NULL,0,NULL),
	 ('[bk-3-]->[bk-2+]','bk-3-','bk-3',NULL,1,NULL),
	 ('[bk-3-]->[bk-2+]','se-5','se-5',NULL,2,'East'),
	 ('[bk-3-]->[bk-2+]','st-13','st-13',NULL,3,'East'),
	 ('[bk-3-]->[bk-2+]','ct-1','ct-1',NULL,4,'East'),
	 ('[bk-3-]->[bk-2+]','sw-1','sw-1','R',5,'North'),
	 ('[bk-3-]->[bk-2+]','st-4','st-4',NULL,6,'East'),
	 ('[bk-3-]->[bk-2+]','sw-2','sw-2','G',7,'East'),
	 ('[bk-3-]->[bk-2+]','st-3','st-3',NULL,8,'East');
INSERT INTO route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3-]->[bk-2+]','se-4','se-4',NULL,9,'East'),
	 ('[bk-3-]->[bk-2+]','bk-2+','bk-2',NULL,10,'East'),
	 ('[bk-4+]->[bk-1-]','bk-4','bk-4',NULL,0,NULL),
	 ('[bk-4+]->[bk-1-]','bk-4+','bk-4',NULL,1,NULL),
	 ('[bk-4+]->[bk-1-]','se-10','se-10',NULL,2,'East'),
	 ('[bk-4+]->[bk-1-]','st-17','st-17',NULL,3,'East'),
	 ('[bk-4+]->[bk-1-]','sw-1','sw-1','G',4,'East'),
	 ('[bk-4+]->[bk-1-]','st-4','st-4',NULL,5,'East'),
	 ('[bk-4+]->[bk-1-]','sw-2','sw-2','R',6,'East'),
	 ('[bk-4+]->[bk-1-]','ct-2','ct-2',NULL,7,'South');
INSERT INTO route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-4+]->[bk-1-]','st-2','st-2',NULL,8,'East'),
	 ('[bk-4+]->[bk-1-]','se-2','se-2',NULL,9,'East'),
	 ('[bk-4+]->[bk-1-]','bk-1-','bk-1',NULL,10,'East'),
	 ('[bk-1-]->[bk-4+]','bk-1','bk-1',NULL,0,NULL),
	 ('[bk-1-]->[bk-4+]','bk-1-','bk-1',NULL,1,NULL),
	 ('[bk-1-]->[bk-4+]','se-2','se-2',NULL,2,'West'),
	 ('[bk-1-]->[bk-4+]','st-2','st-2',NULL,3,'West'),
	 ('[bk-1-]->[bk-4+]','ct-2','ct-2',NULL,4,'West'),
	 ('[bk-1-]->[bk-4+]','sw-2','sw-2','R',5,'North'),
	 ('[bk-1-]->[bk-4+]','st-4','st-4',NULL,6,'West');
INSERT INTO route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1-]->[bk-4+]','sw-1','sw-1','G',7,'West'),
	 ('[bk-1-]->[bk-4+]','st-17','st-17',NULL,8,'West'),
	 ('[bk-1-]->[bk-4+]','se-10','se-10',NULL,9,'West'),
	 ('[bk-1-]->[bk-4+]','bk-4+','bk-4',NULL,10,'West'),
	 ('[bk-1-]->[bk-3-]','bk-1','bk-1',NULL,0,NULL),
	 ('[bk-1-]->[bk-3-]','bk-1-','bk-1',NULL,1,NULL),
	 ('[bk-1-]->[bk-3-]','se-2','se-2',NULL,2,'West'),
	 ('[bk-1-]->[bk-3-]','st-2','st-2',NULL,3,'West'),
	 ('[bk-1-]->[bk-3-]','ct-2','ct-2',NULL,4,'West'),
	 ('[bk-1-]->[bk-3-]','sw-2','sw-2','R',5,'North');
INSERT INTO route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-1-]->[bk-3-]','st-4','st-4',NULL,6,'West'),
	 ('[bk-1-]->[bk-3-]','sw-1','sw-1','R',7,'West'),
	 ('[bk-1-]->[bk-3-]','ct-1','ct-1',NULL,8,'South'),
	 ('[bk-1-]->[bk-3-]','st-13','st-13',NULL,9,'West'),
	 ('[bk-1-]->[bk-3-]','se-5','se-5',NULL,10,'West'),
	 ('[bk-1-]->[bk-3-]','bk-3-','bk-3',NULL,11,'West'),
	 ('[bk-3-]->[bk-1-]','bk-3','bk-3',NULL,0,NULL),
	 ('[bk-3-]->[bk-1-]','bk-3-','bk-3',NULL,1,NULL),
	 ('[bk-3-]->[bk-1-]','se-5','se-5',NULL,2,'East'),
	 ('[bk-3-]->[bk-1-]','st-13','st-13',NULL,3,'East');
INSERT INTO route_elements (route_id,node_id,tile_id,accessory_value,order_seq,incoming_side) VALUES
	 ('[bk-3-]->[bk-1-]','ct-1','ct-1',NULL,4,'East'),
	 ('[bk-3-]->[bk-1-]','sw-1','sw-1','R',5,'North'),
	 ('[bk-3-]->[bk-1-]','st-4','st-4',NULL,6,'East'),
	 ('[bk-3-]->[bk-1-]','sw-2','sw-2','R',7,'East'),
	 ('[bk-3-]->[bk-1-]','ct-2','ct-2',NULL,8,'South'),
	 ('[bk-3-]->[bk-1-]','st-2','st-2',NULL,9,'East'),
	 ('[bk-3-]->[bk-1-]','se-2','se-2',NULL,10,'East'),
	 ('[bk-3-]->[bk-1-]','bk-1-','bk-1',NULL,11,'East');
commit;