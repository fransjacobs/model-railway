delete from route_elements;
delete from routes;
delete from blocks;
delete from tiles;
delete from locomotive_functions;
delete from locomotives;
delete from accessories;
delete from sensors;
commit;

delete from command_stations where id = 'esu-ecos';
commit;

insert into command_stations(id, description, short_name, class_name, connect_via, serial_port, ip_address, network_port, ip_auto_conf, supports_decoder_control, supports_accessory_control, supports_feedback, supports_loco_synch, supports_accessory_synch, supports_loco_image_synch, supports_loco_function_synch, protocols, default_cs, enabled, last_used_serial, sup_conn_types, feedback_module_id, feedback_bus_count, feedback_bus_0_module_count, feedback_bus_1_module_count, feedback_bus_2_module_count, feedback_bus_3_module_count, virtual)
values('esu-ecos', 'ESU ECoS', 'ECoS', 'jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl', 'NETWORK', null, null, 15471, true, true, true, true, true, true, true, true, 'DCC,MFX,MM', true, true, '1', 'NETWORK', '0', 0, 0, 0, 0, 0, true);
commit;

update command_stations set default_cs = false, enabled = false;
update command_stations set default_cs = true, enabled = true, virtual = true where id = 'esu-ecos';
commit;

INSERT INTO locomotives (id,name,uid,address,icon,decoder_type,tacho_max,v_min,velocity,synchronize,imported,commuter,show,command_station_id,dispatcher_direction,locomotive_direction) VALUES
	 (1000,'193 304-3 DB AG',1000,0,'/Users/fransjacobs/jcs/cache/ecos/loco_type_e-image_type_int-36.png','mfx',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'BACKWARDS'),
	 (1001,'SNCB/NMBS HLE 27',1001,3,'/Users/fransjacobs/jcs/cache/ecos/loco_type_e-image_type_int-2.png','dcc128',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'FORWARDS'),
	 (1002,'FS236-002',1002,14,'/Users/fransjacobs/jcs/cache/ecos/loco_type_diesel-image_type_int-14.png','dcc28',126,0,0,true,'Manual Updated',true,true,'esu-ecos',NULL,'FORWARDS'),
	 (1003,'NS 6505',1003,8,'/Users/fransjacobs/jcs/cache/ecos/loco_type_diesel-image_type_int-21.png','dcc28',126,0,0,true,'Manual Updated',true,true,'esu-ecos',NULL,'BACKWARDS'),
	 (1004,'NS 1205',1004,11,'/Users/fransjacobs/jcs/cache/ecos/loco_type_e-image_type_int-46.png','dcc28',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'FORWARDS'),
	 (1005,'DB-141-015-8',1005,12,'/Users/fransjacobs/jcs/cache/ecos/loco_type_e-image_type_int-37.png','mm14',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'BACKWARDS'),
	 (1006,'BR-81-002',1006,2,'/Users/fransjacobs/jcs/cache/ecos/loco_type_steam-image_type_int-77.png','mm14',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'FORWARDS'),
	 (1007,'NS 1139',1007,5,'/Users/fransjacobs/jcs/cache/ecos/loco_type_e-image_type_int-37.png','dcc14',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'BACKWARDS'),
	 (1008,'NS 1309',1008,68,'/Users/fransjacobs/jcs/cache/ecos/loco_type_e-image_type_int-35.png','dcc28',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'FORWARDS'),
	 (1009,'Plan V',1009,7,'/Users/fransjacobs/jcs/cache/ecos/loco_type_e-image_type_int-53.png','dcc28',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'BACKWARDS');
INSERT INTO locomotives (id,name,uid,address,icon,decoder_type,tacho_max,v_min,velocity,synchronize,imported,commuter,show,command_station_id,dispatcher_direction,locomotive_direction) VALUES
	 (1010,'BR 18.4 / Bayr. ',1010,9,'/Users/fransjacobs/jcs/cache/ecos/loco_type_steam-image_type_int-0.png','dcc28',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'FORWARDS'),
	 (1011,'ice',1011,60,'/Users/fransjacobs/jcs/cache/ecos/loco_type_steam-image_type_int-63.png','mm14',126,NULL,0,true,'ECoS',false,true,'esu-ecos',NULL,'BACKWARDS');

commit;

INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1008,0,3,0,'3',false),
	 (1008,1,0,0,'0',false),
	 (1008,2,0,0,'0',false),
	 (1008,3,10,0,'10',false),
	 (1008,4,11,0,'11',false),
	 (1008,5,0,0,'0',false),
	 (1008,6,0,0,'0',false),
	 (1008,7,0,0,'0',false),
	 (1008,8,0,0,'0',false),
	 (1008,9,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1008,10,0,0,'0',false),
	 (1008,11,0,0,'0',false),
	 (1008,12,0,0,'0',false),
	 (1008,13,0,0,'0',false),
	 (1008,14,0,0,'0',false),
	 (1008,15,0,0,'0',false),
	 (1008,16,0,0,'0',false),
	 (1008,17,0,0,'0',false),
	 (1008,18,0,0,'0',false),
	 (1008,19,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1008,20,0,0,'0',false),
	 (1008,21,0,0,'0',false),
	 (1008,22,0,0,'0',false),
	 (1008,23,0,0,'0',false),
	 (1008,24,0,0,'0',false),
	 (1008,25,0,0,'0',false),
	 (1008,26,0,0,'0',false),
	 (1008,27,0,0,'0',false),
	 (1008,28,0,0,'0',false),
	 (1008,29,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1008,30,0,0,'0',false),
	 (1009,0,3,0,'3',false),
	 (1009,1,4,0,'4',false),
	 (1009,2,4,0,'4',false),
	 (1009,3,10,0,'10',false),
	 (1009,4,11,0,'11',false),
	 (1009,5,0,0,'0',false),
	 (1009,6,0,0,'0',false),
	 (1009,7,0,0,'0',false),
	 (1009,8,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1009,9,0,0,'0',false),
	 (1009,10,0,0,'0',false),
	 (1009,11,0,0,'0',false),
	 (1009,12,0,0,'0',false),
	 (1009,13,0,0,'0',false),
	 (1009,14,0,0,'0',false),
	 (1009,15,0,0,'0',false),
	 (1009,16,0,0,'0',false),
	 (1009,17,0,0,'0',false),
	 (1009,18,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1009,19,0,0,'0',false),
	 (1009,20,0,0,'0',false),
	 (1009,21,0,0,'0',false),
	 (1009,22,0,0,'0',false),
	 (1009,23,0,0,'0',false),
	 (1009,24,0,0,'0',false),
	 (1009,25,0,0,'0',false),
	 (1009,26,0,0,'0',false),
	 (1009,27,0,0,'0',false),
	 (1009,28,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1009,29,0,0,'0',false),
	 (1009,30,0,0,'0',false),
	 (1010,0,3,0,'3',false),
	 (1010,1,7,0,'7',false),
	 (1010,2,38,0,'38',true),
	 (1010,3,38,0,'38',true),
	 (1010,4,42,0,'42',false),
	 (1010,5,2,0,'2',false),
	 (1010,6,10,0,'10',false),
	 (1010,7,11015,0,'11015',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1010,8,6,0,'6',false),
	 (1010,9,1031,0,'1031',false),
	 (1010,10,2055,0,'2055',true),
	 (1010,11,1287,0,'1287',false),
	 (1010,12,2,0,'2',false),
	 (1010,13,12039,0,'12039',false),
	 (1010,14,9,0,'9',true),
	 (1010,15,33,0,'33',false),
	 (1010,16,1031,0,'1031',true),
	 (1010,17,1543,0,'1543',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1010,18,11783,0,'11783',false),
	 (1010,19,1321,0,'1321',false),
	 (1010,20,8,0,'8',false),
	 (1010,21,8,0,'8',false),
	 (1010,22,1799,0,'1799',true),
	 (1010,23,8,0,'8',false),
	 (1010,24,7,0,'7',false),
	 (1010,25,7,0,'7',false),
	 (1010,26,263,0,'263',false),
	 (1010,27,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1010,28,0,0,'0',false),
	 (1010,29,0,0,'0',false),
	 (1010,30,0,0,'0',false),
	 (1011,0,3,0,'3',false),
	 (1011,1,6,0,'6',false),
	 (1011,2,6,0,'6',false),
	 (1011,3,6,0,'6',false),
	 (1011,4,6,0,'6',false),
	 (1011,5,6,0,'6',false),
	 (1011,6,6,0,'6',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1011,7,6,0,'6',false),
	 (1011,8,6,0,'6',false),
	 (1000,0,3,0,'3',false),
	 (1000,1,3847,0,'3847',true),
	 (1000,2,8,0,'8',false),
	 (1000,3,37,0,'37',false),
	 (1000,4,11,0,'11',false),
	 (1000,5,263,0,'263',false),
	 (1000,6,16387,0,'16387',false),
	 (1000,7,37,0,'37',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1000,8,32771,0,'32771',false),
	 (1000,9,1287,0,'1287',true),
	 (1000,10,1287,0,'1287',true),
	 (1000,11,40,0,'40',false),
	 (1000,12,2055,0,'2055',false),
	 (1000,13,809,0,'809',false),
	 (1000,14,7,0,'7',true),
	 (1000,15,37,0,'37',true),
	 (1000,16,0,0,'0',false),
	 (1000,17,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1000,18,0,0,'0',false),
	 (1000,19,0,0,'0',false),
	 (1000,20,0,0,'0',false),
	 (1000,21,0,0,'0',false),
	 (1000,22,0,0,'0',false),
	 (1000,23,0,0,'0',false),
	 (1000,24,0,0,'0',false),
	 (1000,25,0,0,'0',false),
	 (1000,26,0,0,'0',false),
	 (1000,27,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1000,28,0,0,'0',false),
	 (1000,29,0,0,'0',false),
	 (1000,30,0,0,'0',false),
	 (1001,0,3,0,'3',false),
	 (1001,1,7,0,'7',false),
	 (1001,2,37,0,'37',true),
	 (1001,3,37,0,'37',true),
	 (1001,4,34,0,'34',false),
	 (1001,5,260,0,'260',false),
	 (1001,6,10,0,'10',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1001,7,4,0,'4',false),
	 (1001,8,5,0,'5',false),
	 (1001,9,3,0,'3',false),
	 (1001,10,7,0,'7',false),
	 (1001,11,1287,0,'1287',false),
	 (1001,12,12039,0,'12039',false),
	 (1001,13,2055,0,'2055',true),
	 (1001,14,9,0,'9',true),
	 (1001,15,40,0,'40',false),
	 (1001,16,39,0,'39',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1001,17,12039,0,'12039',false),
	 (1001,18,9,0,'9',true),
	 (1001,19,11527,0,'11527',true),
	 (1001,20,11015,0,'11015',false),
	 (1001,21,8,0,'8',true),
	 (1001,22,9,0,'9',true),
	 (1001,23,1033,0,'1033',true),
	 (1001,24,809,0,'809',false),
	 (1001,25,11783,0,'11783',true),
	 (1001,26,300,0,'300',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1001,27,263,0,'263',false),
	 (1001,28,12039,0,'12039',false),
	 (1001,29,1033,0,'1033',true),
	 (1001,30,1033,0,'1033',true),
	 (1002,0,3,1,'3',false),
	 (1002,1,0,0,'0',false),
	 (1002,2,0,0,'0',false),
	 (1002,3,10,0,'10',false),
	 (1002,4,11,0,'11',false),
	 (1002,5,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1002,6,0,0,'0',false),
	 (1002,7,0,0,'0',false),
	 (1002,8,0,0,'0',false),
	 (1002,9,0,0,'0',false),
	 (1002,10,0,0,'0',false),
	 (1002,11,0,0,'0',false),
	 (1002,12,0,0,'0',false),
	 (1002,13,0,0,'0',false),
	 (1002,14,0,0,'0',false),
	 (1002,15,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1002,16,0,0,'0',false),
	 (1002,17,0,0,'0',false),
	 (1002,18,0,0,'0',false),
	 (1002,19,0,0,'0',false),
	 (1002,20,0,0,'0',false),
	 (1002,21,0,0,'0',false),
	 (1002,22,0,0,'0',false),
	 (1002,23,0,0,'0',false),
	 (1002,24,0,0,'0',false),
	 (1002,25,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1002,26,0,0,'0',false),
	 (1002,27,0,0,'0',false),
	 (1002,28,0,0,'0',false),
	 (1002,29,0,0,'0',false),
	 (1002,30,0,0,'0',false),
	 (1003,0,3,1,'3',false),
	 (1003,1,1541,1,'1541',false),
	 (1003,2,1029,0,'1029',false),
	 (1003,3,10,0,'10',false),
	 (1003,4,11,0,'11',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1003,5,0,0,'0',false),
	 (1003,6,0,0,'0',false),
	 (1003,7,0,0,'0',false),
	 (1003,8,0,0,'0',false),
	 (1003,9,0,0,'0',false),
	 (1003,10,0,0,'0',false),
	 (1003,11,0,0,'0',false),
	 (1003,12,0,0,'0',false),
	 (1003,13,0,0,'0',false),
	 (1003,14,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1003,15,0,0,'0',false),
	 (1003,16,0,0,'0',false),
	 (1003,17,0,0,'0',false),
	 (1003,18,0,0,'0',false),
	 (1003,19,0,0,'0',false),
	 (1003,20,0,0,'0',false),
	 (1003,21,0,0,'0',false),
	 (1003,22,0,0,'0',false),
	 (1003,23,0,0,'0',false),
	 (1003,24,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1003,25,0,0,'0',false),
	 (1003,26,0,0,'0',false),
	 (1003,27,0,0,'0',false),
	 (1003,28,0,0,'0',false),
	 (1003,29,0,0,'0',false),
	 (1003,30,0,0,'0',false),
	 (1004,0,3,0,'3',false),
	 (1004,1,1029,0,'1029',false),
	 (1004,2,1541,0,'1541',false),
	 (1004,3,10,0,'10',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1004,4,11,0,'11',false),
	 (1004,5,32771,0,'32771',false),
	 (1004,6,16387,0,'16387',false),
	 (1004,7,0,0,'0',false),
	 (1004,8,0,0,'0',false),
	 (1004,9,0,0,'0',false),
	 (1004,10,0,0,'0',false),
	 (1004,11,0,0,'0',false),
	 (1004,12,0,0,'0',false),
	 (1004,13,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1004,14,0,0,'0',false),
	 (1004,15,0,0,'0',false),
	 (1004,16,0,0,'0',false),
	 (1004,17,0,0,'0',false),
	 (1004,18,0,0,'0',false),
	 (1004,19,0,0,'0',false),
	 (1004,20,0,0,'0',false),
	 (1004,21,0,0,'0',false),
	 (1004,22,0,0,'0',false),
	 (1004,23,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1004,24,0,0,'0',false),
	 (1004,25,0,0,'0',false),
	 (1004,26,0,0,'0',false),
	 (1004,27,0,0,'0',false),
	 (1004,28,0,0,'0',false),
	 (1004,29,0,0,'0',false),
	 (1004,30,0,0,'0',false),
	 (1005,0,3,0,'3',false),
	 (1005,1,0,0,'0',false),
	 (1005,2,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1005,3,10,0,'10',false),
	 (1005,4,11,0,'11',false),
	 (1005,5,0,0,'0',false),
	 (1005,6,0,0,'0',false),
	 (1005,7,0,0,'0',false),
	 (1005,8,0,0,'0',false),
	 (1006,0,3,0,'3',false),
	 (1006,1,0,0,'0',false),
	 (1006,2,0,0,'0',false),
	 (1006,3,10,0,'10',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1006,4,11,0,'11',false),
	 (1006,5,0,0,'0',false),
	 (1006,6,0,0,'0',false),
	 (1006,7,0,0,'0',false),
	 (1006,8,0,0,'0',false),
	 (1007,0,3,0,'3',false),
	 (1007,1,260,0,'260',false),
	 (1007,2,260,0,'260',false),
	 (1007,3,10,0,'10',false),
	 (1007,4,11,0,'11',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1007,5,3,0,'3',false),
	 (1007,6,3,0,'3',false),
	 (1007,7,1029,0,'1029',false),
	 (1007,8,0,0,'0',false),
	 (1007,9,0,0,'0',false),
	 (1007,10,0,0,'0',false),
	 (1007,11,0,0,'0',false),
	 (1007,12,0,0,'0',false),
	 (1007,13,0,0,'0',false),
	 (1007,14,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1007,15,0,0,'0',false),
	 (1007,16,0,0,'0',false),
	 (1007,17,0,0,'0',false),
	 (1007,18,0,0,'0',false),
	 (1007,19,0,0,'0',false),
	 (1007,20,0,0,'0',false),
	 (1007,21,0,0,'0',false),
	 (1007,22,0,0,'0',false),
	 (1007,23,0,0,'0',false),
	 (1007,24,0,0,'0',false);
INSERT INTO locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (1007,25,0,0,'0',false),
	 (1007,26,0,0,'0',false),
	 (1007,27,0,0,'0',false),
	 (1007,28,0,0,'0',false),
	 (1007,29,0,0,'0',false),
	 (1007,30,0,0,'0',false);

commit;

INSERT INTO accessories (id,address,name,"type",state,states,switch_time,protocol,decoder,accessory_group,icon,icon_file,imported,command_station_id,synchronize) VALUES
	 ('20005',16,'Sein mini','lichtsignal_SH01',1,2,500,'mm',NULL,NULL,'13',NULL,'ECoS','esu-ecos',true),
	 ('20004',14,'Sein 4','lichtsignal_HP012_SH01',1,4,250,'mm',NULL,NULL,'12',NULL,'ECoS','esu-ecos',true),
	 ('20003',12,'Sein 3','lichtsignal_HP012',1,3,250,'mm',NULL,NULL,'11',NULL,'ECoS','esu-ecos',true),
	 ('20002',10,'Sein','lichtsignal_HP01',1,2,250,'mm',NULL,NULL,'9',NULL,'ECoS','esu-ecos',true),
	 ('20001',2,'W2','linksweiche',1,2,250,'dcc',NULL,NULL,'0',NULL,NULL,'esu-ecos',false),
	 ('20000',1,'W1','rechtsweiche',1,2,250,'dcc',NULL,NULL,'1',NULL,NULL,'esu-ecos',false),
	 ('20006',5,'Symbol','lichtsignal_HP01',1,2,250,'dcc',NULL,NULL,'9',NULL,'ECoS','esu-ecos',true);

commit;

INSERT INTO sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('0-0011','B0-S-11',0,11,0,NULL,NULL,NULL),
	 ('0-0010','B0-S-10',0,10,1,NULL,NULL,NULL),
	 ('0-0002','B0-S-2',0,2,0,NULL,NULL,NULL),
	 ('0-0013','B0-S-13',0,13,0,1,NULL,NULL),
	 ('0-0001','B0-S-1',0,1,0,NULL,NULL,NULL),
	 ('0-0012','B0-S-12',0,12,0,NULL,NULL,NULL),
	 ('0-0004','B0-S-4',0,4,0,NULL,NULL,NULL),
	 ('0-0015','B0-S-15',0,15,0,1,NULL,NULL),
	 ('0-0003','B0-S-3',0,3,0,NULL,NULL,NULL),
	 ('0-0014','B0-S-14',0,14,0,1,NULL,NULL);
INSERT INTO sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('0-0006','B0-S-6',0,6,0,1,NULL,NULL),
	 ('0-0005','B0-S-5',0,5,0,1,NULL,NULL),
	 ('0-0016','B0-S-16',0,16,0,1,NULL,NULL),
	 ('0-0008','B0-S-8',0,8,0,1,NULL,NULL),
	 ('0-0007','B0-S-7',0,7,0,1,NULL,NULL),
	 ('0-0009','B0-S-9',0,9,0,NULL,NULL,NULL);

commit;
