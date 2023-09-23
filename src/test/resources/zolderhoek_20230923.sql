delete from blocks;
delete from route_elements;
delete from routes;
delete from tiles;
delete from sensors;
delete from accessories;

commit;


INSERT INTO jcs.accessories (address,name,"type","position",states,switch_time,decoder_type,decoder,accessory_group,icon,icon_file) VALUES
	 (20,'S  20','lichtsignal_HP01',0,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_00.svg'),
	 (2,'W 2R','rechtsweiche',1,2,200,'mm','ein_alt','weichen','005','magicon_a_005_01.svg'),
	 (16,'S 16','lichtsignal_SH01',0,2,200,'mm','ein_alt','lichtsignale','019','magicon_a_019_00.svg'),
	 (3,'W 3R','rechtsweiche',0,2,200,'mm','ein_alt','weichen','005','magicon_a_005_00.svg'),
	 (47,'S 20','lichtsignal_HP01',0,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_00.svg'),
	 (4,'W 4R','rechtsweiche',0,2,200,'mm','ein_alt','weichen','005','magicon_a_005_00.svg'),
	 (22,'S 22','lichtsignal_SH01',0,2,200,'mm','ein_alt','lichtsignale','019','magicon_a_019_00.svg'),
	 (5,'W 5R','rechtsweiche',0,2,200,'mm','ein_alt','weichen','005','magicon_a_005_00.svg'),
	 (24,'S 24','lichtsignal_SH01',0,2,200,'mm','ein_alt','lichtsignale','019','magicon_a_019_00.svg'),
	 (6,'W 6R','rechtsweiche',0,2,200,'mm','ein_alt','weichen','005','magicon_a_005_00.svg');
INSERT INTO jcs.accessories (address,name,"type","position",states,switch_time,decoder_type,decoder,accessory_group,icon,icon_file) VALUES
	 (27,'S 27/28','urc_lichtsignal_HP012_SH01',0,4,200,'mm','ein_alt','lichtsignale','027','magicon_a_027_00.svg'),
	 (7,'W 7L','linksweiche',1,2,200,'mm','ein_alt','weichen','006','magicon_a_006_01.svg'),
	 (31,'S 31/32','urc_lichtsignal_HP012_SH01',0,4,200,'mm','ein_alt','lichtsignale','027','magicon_a_027_00.svg'),
	 (8,'W 8L','linksweiche',1,2,200,'mm','ein_alt','weichen','006','magicon_a_006_01.svg'),
	 (35,'S 35','lichtsignal_HP01',0,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_00.svg'),
	 (9,'W 9L','linksweiche',0,2,200,'mm','ein_alt','weichen','006','magicon_a_006_00.svg'),
	 (41,'S 41','urc_lichtsignal_HP012',0,3,200,'mm','ein_alt','lichtsignale','026','magicon_a_026_00.svg'),
	 (10,'W 10L','linksweiche',1,2,200,'mm','ein_alt','weichen','006','magicon_a_006_01.svg'),
	 (49,'S 49','urc_lichtsignal_HP012',0,3,200,'mm','ein_alt','lichtsignale','026','magicon_a_026_00.svg'),
	 (11,'W 11L','linksweiche',0,2,200,'mm','ein_alt','weichen','006','magicon_a_006_00.svg');
INSERT INTO jcs.accessories (address,name,"type","position",states,switch_time,decoder_type,decoder,accessory_group,icon,icon_file) VALUES
	 (53,'S 53','lichtsignal_HP01',0,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_00.svg'),
	 (12,'W 12L','linksweiche',0,2,200,'mm','ein_alt','weichen','006','magicon_a_006_00.svg'),
	 (13,'W 13L','linksweiche',1,2,200,'mm','ein_alt','weichen','006','magicon_a_006_01.svg'),
	 (14,'W 14L','linksweiche',0,2,200,'mm','ein_alt','weichen','006','magicon_a_006_00.svg'),
	 (17,'W 17R','rechtsweiche',1,2,200,'mm','ein_alt','weichen','005','magicon_a_005_01.svg'),
	 (18,'W 18R','rechtsweiche',1,2,200,'mm','ein_alt','weichen','005','magicon_a_005_01.svg'),
	 (1,'W 1R','rechtsweiche',1,2,200,'mm','ein_alt','weichen','005','magicon_a_005_01.svg'),
	 (15,'S 15','lichtsignal_SH01',1,2,200,'mm','ein_alt','lichtsignale','019','magicon_a_019_01.svg'),
	 (19,'S 19','lichtsignal_HP01',0,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_00.svg'),
	 (21,'S 21','lichtsignal_SH01',0,2,200,'mm','ein_alt','lichtsignale','019','magicon_a_019_00.svg');
INSERT INTO jcs.accessories (address,name,"type","position",states,switch_time,decoder_type,decoder,accessory_group,icon,icon_file) VALUES
	 (23,'S 23','lichtsignal_SH01',0,2,200,'mm','ein_alt','lichtsignale','019','magicon_a_019_00.svg'),
	 (25,'S 25/26','urc_lichtsignal_HP012_SH01',0,4,200,'mm','ein_alt','lichtsignale','027','magicon_a_027_00.svg'),
	 (29,'S 29/30','urc_lichtsignal_HP012_SH01',0,4,200,'mm','ein_alt','lichtsignale','027','magicon_a_027_00.svg'),
	 (33,'S 33','lichtsignal_HP01',1,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_01.svg'),
	 (36,'S 36','lichtsignal_HP01',0,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_00.svg'),
	 (43,'S 43','lichtsignal_HP01',0,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_00.svg'),
	 (51,'S 51','lichtsignal_HP01',0,2,200,'mm','ein_alt','lichtsignale','015','magicon_a_015_00.svg'),
	 (34,'W 34','linksweiche',1,2,200,'mm','ein_alt','weichen','006','magicon_a_006_01.svg');

commit;

INSERT INTO jcs.sensors (name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('M1001',65,1001,0,1,200,'2023-09-23'),
	 ('M2019',65,2019,0,1,5800,'2023-09-23'),
	 ('M2020',65,2020,0,1,200,'2023-09-23'),
	 ('M2001',65,2001,0,1,2500,'2023-09-23'),
	 ('M2002',65,2002,0,1,3000,'2023-09-23'),
	 ('M2009',65,2009,0,1,1100,'2023-09-23'),
	 ('M2010',65,2010,0,1,5300,'2023-09-23'),
	 ('M1',65,1,0,1,200,'2023-09-23'),
	 ('M2',65,2,0,1,7100,'2023-09-23'),
	 ('M6',65,6,0,1,100,'2023-09-23');
INSERT INTO jcs.sensors (name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('M5',65,5,0,1,200,'2023-09-23'),
	 ('M13',65,13,1,0,655350,'2023-09-23'),
	 ('M7',65,7,0,1,300,'2023-09-23'),
	 ('M14',65,14,0,1,51100,'2023-09-23'),
	 ('M15',65,15,0,1,200,'2023-09-23'),
	 ('M16',65,16,0,1,300,'2023-09-23'),
	 ('M1007',65,1007,0,1,5100,'2023-09-23'),
	 ('M1008',65,1008,0,1,4300,'2023-09-23'),
	 ('M1009',65,1009,0,1,4700,'2023-09-23'),
	 ('M1005',65,1005,0,1,4900,'2023-09-23');
INSERT INTO jcs.sensors (name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('M2014',65,2014,0,1,5300,'2023-09-23'),
	 ('M2013',65,2013,1,0,655350,'2023-09-23'),
	 ('M2016',65,2016,0,1,300,'2023-09-23'),
	 ('M2015',65,2015,0,1,500,'2023-09-23'),
	 ('M2006',65,2006,0,1,6200,'2023-09-23'),
	 ('M2005',65,2005,1,0,1300,'2023-09-23'),
	 ('M2008',65,2008,0,1,600,'2023-09-23'),
	 ('M2007',65,2007,0,1,37800,'2023-09-23'),
	 ('M2031',65,2031,0,1,5100,'2023-09-23'),
	 ('M2012',65,1012,1,0,900,'2023-09-23');
INSERT INTO jcs.sensors (name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('M2011',65,1011,0,1,100,'2023-09-23'),
	 ('M2032',65,2032,0,1,5100,'2023-09-23'),
	 ('M2028',65,2028,NULL,NULL,NULL,NULL),
	 ('M2021',65,2021,0,1,200,'2023-09-23'),
	 ('M2022',65,2022,0,1,5500,'2023-09-23'),
	 ('M1002',65,1002,0,1,200,'2023-09-23'),
	 ('M1013',65,1013,0,1,5100,'2023-09-23'),
	 ('M1014',65,1014,0,1,1000,'2023-09-23'),
	 ('M11',65,11,0,1,200,'2023-09-23'),
	 ('M12',65,12,NULL,NULL,NULL,NULL);
INSERT INTO jcs.sensors (name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('M3',65,3,0,1,39500,'2023-09-23'),
	 ('M4',65,4,NULL,NULL,NULL,NULL),
	 ('M8',65,8,0,1,400,'2023-09-23'),
	 ('M9',65,9,NULL,NULL,NULL,NULL),
	 ('M1006',65,1006,0,1,1600,'2023-09-23'),
	 ('M1004',65,1004,NULL,NULL,NULL,NULL),
	 ('M2017',65,2017,0,1,10600,'2023-09-23'),
	 ('M10',65,10,0,1,1900,'2023-09-23'),
	 ('M2025',65,2025,1,0,107200,'2023-09-23'),
	 ('M1003',65,1003,0,1,200,'2023-09-23');
INSERT INTO jcs.sensors (name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('M2003',65,2003,0,1,3000,'2023-09-23'),
	 ('M2004',65,2004,0,1,2800,'2023-09-23'),
	 ('M2012',65,2012,0,1,5300,'2023-09-23'),
	 ('M2011',65,2011,0,1,1200,'2023-09-23');
commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-11','Curved','West','Center',700,420,NULL,NULL,NULL),
	 ('ct-1','Curved','South','Center',580,140,NULL,NULL,NULL),
	 ('se-7','Sensor','East','Center',340,420,NULL,NULL,NULL),
	 ('sd-4','StraightDirection','West','Center',540,180,NULL,NULL,NULL),
	 ('ct-3','Curved','East','Center',100,180,NULL,NULL,NULL),
	 ('st-37','Straight','East','Center',540,420,NULL,NULL,NULL),
	 ('st-11','Straight','South','Center',660,220,NULL,NULL,NULL),
	 ('st-27','Straight','East','Center',260,420,NULL,NULL,NULL),
	 ('st-17','Straight','East','Center',300,380,NULL,NULL,NULL),
	 ('se-8','Sensor','East','Center',500,420,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-6','Curved','West','Center',660,380,NULL,NULL,NULL),
	 ('st-30','Straight','East','Center',140,420,NULL,NULL,NULL),
	 ('st-28','Straight','East','Center',220,420,NULL,NULL,NULL),
	 ('st-38','Straight','East','Center',660,420,NULL,NULL,NULL),
	 ('st-5','Straight','East','Center',620,180,NULL,NULL,NULL),
	 ('sd-1','StraightDirection','East','Center',300,140,NULL,NULL,NULL),
	 ('bk-3','Block','West','Center',420,380,NULL,NULL,NULL),
	 ('ct-4','Curved','South','Center',660,180,NULL,NULL,NULL),
	 ('st-25','Straight','East','Center',180,380,NULL,NULL,NULL),
	 ('se-3','Sensor','West','Center',500,140,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-2','Sensor','East','Center',500,180,NULL,NULL,NULL),
	 ('st-23','Straight','South','Center',140,300,NULL,NULL,NULL),
	 ('ct-8','Curved','East','Center',140,220,NULL,NULL,NULL),
	 ('st-15','Straight','West','Center',220,380,NULL,NULL,NULL),
	 ('sd-2','StraightDirection','West','Center',300,180,NULL,NULL,NULL),
	 ('bk-4','Block','East','Center',420,420,NULL,NULL,NULL),
	 ('st-26','Straight','East','Center',300,420,NULL,NULL,NULL),
	 ('st-36','Straight','East','Center',580,420,NULL,NULL,NULL),
	 ('se-5','Sensor','West','Center',340,380,NULL,NULL,NULL),
	 ('st-22','Straight','South','Center',140,260,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-12','Straight','South','Center',700,300,NULL,NULL,NULL),
	 ('st-29','Straight','East','Center',180,420,NULL,NULL,NULL),
	 ('st-18','Straight','East','Center',540,380,NULL,NULL,NULL),
	 ('cs-1','Cross','East','Left',220,180,NULL,NULL,NULL),
	 ('st-14','Straight','North','Center',660,340,NULL,NULL,NULL),
	 ('st-32','Straight','North','Center',700,340,NULL,NULL,NULL),
	 ('st-7','Straight','South','Center',100,220,NULL,NULL,NULL),
	 ('ct-9','Curved','North','Center',140,380,NULL,NULL,NULL),
	 ('ct-7','Curved','West','Center',220,220,NULL,NULL,NULL),
	 ('st-10','Straight','South','Center',100,340,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-24','Straight','South','Center',140,340,NULL,NULL,NULL),
	 ('se-1','Sensor','East','Center',340,180,NULL,NULL,NULL),
	 ('st-20','Straight','East','Center',620,380,NULL,NULL,NULL),
	 ('bk-2','Block','East','Center',420,140,NULL,NULL,NULL),
	 ('sw-1','Switch','North','Left',660,260,NULL,NULL,NULL),
	 ('st-9','Straight','South','Center',100,300,NULL,NULL,NULL),
	 ('st-19','Straight','East','Center',580,380,NULL,NULL,NULL),
	 ('se-4','Sensor','West','Center',340,140,NULL,NULL,NULL),
	 ('st-34','Straight','North','Center',100,380,NULL,NULL,NULL),
	 ('ct-10','Curved','South','Center',700,260,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-5','Curved','North','Center',100,420,NULL,NULL,NULL),
	 ('sd-3','StraightDirection','East','Center',540,140,NULL,NULL,NULL),
	 ('sw-2','Switch','East','Right',580,180,NULL,NULL,NULL),
	 ('st-33','Straight','North','Center',700,380,NULL,NULL,NULL),
	 ('se-6','Sensor','West','Center',500,380,NULL,NULL,NULL),
	 ('st-13','Straight','North','Center',660,300,NULL,NULL,NULL),
	 ('st-35','Straight','East','Center',620,420,NULL,NULL,NULL),
	 ('bk-1','Block','East','Center',420,180,NULL,NULL,NULL),
	 ('st-16','Straight','West','Center',260,380,NULL,NULL,NULL),
	 ('st-21','Straight','West','Center',180,220,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-31','Straight','East','Center',140,180,NULL,NULL,NULL),
	 ('st-6','Straight','East','Center',180,180,NULL,NULL,NULL),
	 ('ct-2','Curved','East','Center',260,140,NULL,NULL,NULL),
	 ('st-8','Straight','South','Center',100,260,NULL,NULL,NULL);

commit;

