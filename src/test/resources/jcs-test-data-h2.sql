delete from jcs_properties;
delete from locomotive_functions;
delete from locomotives;
delete from route_elements;
delete from routes;
delete from tiles;
delete from sensors;
delete from accessories;

commit;

alter table sensors alter column id restart with 1;
alter table locomotive_functions alter column id restart with 1;
alter table accessories alter column id restart with 1;
alter table route_elements alter column id restart with 1;

insert into jcs_properties(p_key,p_value) values
('CS3', 'jcs.controller.cs3.MarklinCS3'),
('k1', 'v1'),
('k2', 'v2');

commit;

insert into sensors (name,device_id,contact_id,status,previous_status,millis,last_updated) values
	 ('M1',65,1,0,0,0,NULL),
	 ('M2',65,2,1,1,0,NULL);
	
commit;

insert into locomotives (id,name,uid,mfx_uid,address,icon,decoder_type,mfx_sid,tacho_max,v_min,velocity,richtung,commuter,length,show) values 
(2,'BR 81 002',2,0,2,'DB BR 81 008','mm_prg',null,120,1,0,0,false,null,true),
(8,'NS  6505',8, null, 8,'NS DHG 6505','mm_prg',NULL,120,0,0,0,false,null,true),
(12,'BR 141 015-08',12,null,12,'DB BR 141 136-2','mm_prg',null ,120,0,0,0,false,NULL,true),
(16389,'193 304-3 DB AG',16389,1945312555,5,'DB BR 193 304-3','mfx','0x5',160,5,0,0,false,null,true),   
(49156,'NS Plan Y',49156,null, 4,'NS Plan Y','dcc',null,120,1,0,0,false,null,true);

insert into locomotive_functions (locomotive_id,f_number,f_type,f_value) values
(16389,5,20,0),
(16389,6,41,0),
(16389,7,10,0),
(16389,8,42,0),
(16389,9,171,0),
(16389,10,171,0),
(16389,11,29,0),
(16389,12,11,0),
(16389,13,116,0),
(16389,14,220,0),
(12,0,1,1),
(12,3,8,0),
(12,4,18,0),
(2,0,1,1),
(2,4,18,0),
(8,0,1,1),
(8,1,1,0),
(49156,0,1,1),
(49156,1,2,1),
(49156,2,2,1),
(49156,3,8,0),
(49156,4,18,0);

commit;	

insert into accessories (address,name,type,position,states,switch_time,decoder_type,decoder,accessory_group,icon,icon_file) values
	 (1,'W 1R','rechtsweiche',1,2,200,'mm','ein_alt','weichen','005','magicon_a_005_01.svg'),
	 (2,'W 2L','linksweiche',1,2,200,'mm','ein_alt','weichen','006','magicon_a_006_01.svg'),
	 (6,'W 6R','rechtsweiche',1,2,200,'mm','ein_alt','weichen','005','magicon_a_005_01.svg'),
	 (7,'W 7L','linksweiche',1,2,200,'mm','ein_alt','weichen','006','magicon_a_006_01.svg'),
	 (15,'S 15','lichtsignal_SH01',0,2,200,'mm','ein_alt','lichtsignale','019','magicon_a_019_00.svg'),
	 (19,'S 19','lichtsignal_HP01',0,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_00.svg'),
	 (25,'S 25/26','urc_lichtsignal_HP012_SH01',0,4,200,'mm','ein_alt','lichtsignale','027','magicon_a_027_00.svg'),
	 (41,'S 41','urc_lichtsignal_HP012',0,3,200,'mm','ein_alt','lichtsignale','026','magicon_a_026_00.svg');

commit;

insert into tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) values
	 ('bk-1','Block','East','Center',320,140,null,null,null),
	 ('bk-2','Block','East','Center',420,140,null,null,null),
	 ('ct-2','Curved','East','Center',260,140,null,null,null),
	 ('ct-5','Curved','South','Center',180,380,null,null,null),
	 ('se-5','Sensor','North','Center',340,380,null,NULL,2),
	 ('se-6','Sensor','West','Center',500,380,null,null,1),
	 ('si-3','Signal','East','Center',300,140,null,5,null),
	 ('st-1','Straight','East','Center',300,180,null,null,null),
	 ('sw-1','Switch','West','Left',260,180,null,2,null),
	 ('sw-2','Switch','East','Right',580,180,null,null,null);

insert into routes (id,from_tile_id,from_suffix,to_tile_id, to_suffix, route_color, locked) values
   ('[bk-1+]->[bk-2-]', 'bk-1','+','bk-2','-','red', 0),
   ('[bk-2-]->[bk-1+]', 'bk-2','-','bk-1','+','yellow',0);

insert into route_elements (route_id, node_id, tile_id, accessory_value,order_seq) values
   ('[bk-1+]->[bk-2-]', 'bk-1+','bk-1',null,0),
   ('[bk-1+]->[bk-2-]', 'ct-2','ct-2',null,1),
   ('[bk-1+]->[bk-2-]', 'st-1','st-1',null,2),
   ('[bk-1+]->[bk-2-]', 'bk2-','bk-2',null,3);

commit;