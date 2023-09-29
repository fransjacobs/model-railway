delete from blocks;
delete from route_elements;
delete from routes;
delete from tiles;
delete from sensors;
delete from accessories;

commit;


INSERT INTO jcs.accessories (id,address,name,"type","position",states,switch_time,decoder_type,decoder,accessory_group,icon,icon_file,imported) VALUES
	 ('15',15,'S 15','lichtsignal_SH01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('16',16,'S 16','lichtsignal_SH01',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('19',19,'S 19','lichtsignal_HP01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('20',20,'S 20','lichtsignal_HP01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('21',21,'S 21','lichtsignal_SH01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('22',22,'S 22','lichtsignal_SH01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('23',23,'S 23','lichtsignal_SH01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('24',24,'S 24','lichtsignal_SH01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('25',25,'S 25/26','urc_lichtsignal_HP012_SH01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('27',27,'S 27/28','urc_lichtsignal_HP012_SH01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL);
INSERT INTO jcs.accessories (id,address,name,"type","position",states,switch_time,decoder_type,decoder,accessory_group,icon,icon_file,imported) VALUES
	 ('29',29,'S 29/30','urc_lichtsignal_HP012_SH01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('31',31,'S 31/32','urc_lichtsignal_HP012_SH01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('33',33,'S 33','lichtsignal_HP01',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('35',35,'S 35','lichtsignal_HP01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('36',36,'S 36','lichtsignal_HP01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('41',41,'S 41','urc_lichtsignal_HP012',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('43',43,'S 43','lichtsignal_HP01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('47',47,'S 47','lichtsignal_HP01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('49',49,'S 49','urc_lichtsignal_HP012',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('51',51,'S 51','lichtsignal_HP01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL);
INSERT INTO jcs.accessories (id,address,name,"type","position",states,switch_time,decoder_type,decoder,accessory_group,icon,icon_file,imported) VALUES
	 ('53',53,'S 53','lichtsignal_HP01',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('10',10,'W 10L','linksweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('11',11,'W 11L','linksweiche',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('12',12,'W 12L','linksweiche',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('13',13,'W 13L','linksweiche',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('14',14,'W 14L','linksweiche',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('17',17,'W 17R','rechtsweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('18',18,'W 18R','rechtsweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('1',1,'W 1R','rechtsweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('2',2,'W 2R','rechtsweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL);
INSERT INTO jcs.accessories (id,address,name,"type","position",states,switch_time,decoder_type,decoder,accessory_group,icon,icon_file,imported) VALUES
	 ('34',34,'W 34','linksweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('3',3,'W 3R','rechtsweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('4',4,'W 4R','rechtsweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('5',5,'W 5R','rechtsweiche',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('6',6,'W 6R','rechtsweiche',0,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('7',7,'W 7L','linksweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('8',8,'W 8L','linksweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL),
	 ('9',9,'W 9L','linksweiche',1,NULL,200,'mm2','ein_alt',NULL,NULL,NULL,NULL);

commit;

INSERT INTO jcs.sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('65-1001','M1001',65,1001,0,1,200,'2023-09-23'),
	 ('65-2019','M2019',65,2019,0,1,5800,'2023-09-23'),
	 ('65-2020','M2020',65,2020,0,1,200,'2023-09-23'),
	 ('65-2001','M2001',65,2001,0,1,2500,'2023-09-23'),
	 ('65-2002','M2002',65,2002,0,1,3000,'2023-09-23'),
	 ('65-2009','M2009',65,2009,0,1,1100,'2023-09-23'),
	 ('65-2010','M2010',65,2010,0,1,5300,'2023-09-23'),
	 ('65-1','M1',65,1,0,1,200,'2023-09-23'),
	 ('65-2','M2',65,2,0,1,7100,'2023-09-23'),
	 ('65-6','M6',65,6,0,1,100,'2023-09-23');
INSERT INTO jcs.sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('65-5','M5',65,5,0,1,200,'2023-09-23'),
	 ('65-13','M13',65,13,1,0,655350,'2023-09-23'),
	 ('65-7','M7',65,7,0,1,300,'2023-09-23'),
	 ('65-14','M14',65,14,0,1,51100,'2023-09-23'),
	 ('65-15','M15',65,15,0,1,200,'2023-09-23'),
	 ('65-16','M16',65,16,0,1,300,'2023-09-23'),
	 ('65-1007','M1007',65,1007,0,1,5100,'2023-09-23'),
	 ('65-1008','M1008',65,1008,0,1,4300,'2023-09-23'),
	 ('65-1009','M1009',65,1009,0,1,4700,'2023-09-23'),
	 ('65-1015','M1005',65,1005,0,1,4900,'2023-09-23');
INSERT INTO jcs.sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('65-2014','M2014',65,2014,0,1,5300,'2023-09-23'),
	 ('65-2013','M2013',65,2013,1,0,655350,'2023-09-23'),
	 ('65-2016','M2016',65,2016,0,1,300,'2023-09-23'),
	 ('65-2015','M2015',65,2015,0,1,500,'2023-09-23'),
	 ('65-2006','M2006',65,2006,0,1,6200,'2023-09-23'),
	 ('65-2005','M2005',65,2005,1,0,1300,'2023-09-23'),
	 ('65-2008','M2008',65,2008,0,1,600,'2023-09-23'),
	 ('65-2007','M2007',65,2007,0,1,37800,'2023-09-23'),
	 ('65-2031','M2031',65,2031,0,1,5100,'2023-09-23'),
	 ('65-1012','M1012',65,1012,1,0,900,'2023-09-23');
INSERT INTO jcs.sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('65-1011','M1011',65,1011,0,1,100,'2023-09-23'),
	 ('65-2032','M2032',65,2032,0,1,5100,'2023-09-23'),
	 ('65-2028','M2028',65,2028,NULL,NULL,NULL,NULL),
	 ('65-2021','M2021',65,2021,0,1,200,'2023-09-23'),
	 ('65-2022','M2022',65,2022,0,1,5500,'2023-09-23'),
	 ('65-1002','M1002',65,1002,0,1,200,'2023-09-23'),
	 ('65-1013','M1013',65,1013,0,1,5100,'2023-09-23'),
	 ('65-1014','M1014',65,1014,0,1,1000,'2023-09-23'),
	 ('65-11','M11',65,11,0,1,200,'2023-09-23'),
	 ('65-12','M12',65,12,NULL,NULL,NULL,NULL);
INSERT INTO jcs.sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('65-3','M3',65,3,0,1,39500,'2023-09-23'),
	 ('65-4','M4',65,4,NULL,NULL,NULL,NULL),
	 ('65-8','M8',65,8,0,1,400,'2023-09-23'),
	 ('65-9','M9',65,9,NULL,NULL,NULL,NULL),
	 ('65-1006','M1006',65,1006,0,1,1600,'2023-09-23'),
	 ('65-1004','M1004',65,1004,NULL,NULL,NULL,NULL),
	 ('65-2017','M2017',65,2017,0,1,10600,'2023-09-23'),
	 ('65-10','M10',65,10,0,1,1900,'2023-09-23'),
	 ('65-2025','M2025',65,2025,1,0,107200,'2023-09-23'),
	 ('65-1003','M1003',65,1003,0,1,200,'2023-09-23');
INSERT INTO jcs.sensors (id,name,device_id,contact_id,status,previous_status,millis,last_updated) VALUES
	 ('65-2003','M2003',65,2003,0,1,3000,'2023-09-23'),
	 ('65-2004','M2004',65,2004,0,1,2800,'2023-09-23'),
	 ('65-2012','M2012',65,2012,0,1,5300,'2023-09-23'),
	 ('65-2011','M2011',65,2011,0,1,1200,'2023-09-23');

commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-6','Sensor','South','Center',140,340,NULL,NULL,'65-2019'),
	 ('st-21','Straight','West','Center',660,340,NULL,NULL,NULL),
	 ('sw-6','Switch','East','Left',740,180,NULL,'13',NULL),
	 ('st-42','Straight','West','Center',620,540,NULL,NULL,NULL),
	 ('sd-14','StraightDirection','East','Center',380,580,NULL,NULL,NULL),
	 ('sw-3','Switch','West','Left',260,340,NULL,'9',NULL),
	 ('st-66','Straight','West','Center',740,540,NULL,NULL,NULL),
	 ('st-24','Straight','West','Center',620,140,NULL,NULL,NULL),
	 ('se-41','Sensor','South','Center',900,500,NULL,NULL,'65-1002'),
	 ('se-9','Sensor','East','Center',300,180,NULL,NULL,'65-1011');
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-40','Sensor','West','Center',620,700,NULL,NULL,'65-9'),
	 ('st-97','Straight','South','Center',980,700,NULL,NULL,NULL),
	 ('se-16','Sensor','West','Center',540,300,NULL,NULL,'65-2008'),
	 ('st-34','Straight','West','Center',700,100,NULL,NULL,NULL),
	 ('st-77','Straight','East','Center',420,740,NULL,NULL,NULL),
	 ('si-14','Signal','South','Center',900,540,NULL,'49',NULL),
	 ('st-25','Straight','West','Center',860,180,NULL,NULL,NULL),
	 ('se-22','Sensor','East','Center',420,380,NULL,NULL,'65-2004'),
	 ('st-32','Straight','West','Center',820,60,NULL,NULL,NULL),
	 ('st-47','Straight','West','Center',900,60,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('bk-2','Block','South','Center',180,220,NULL,NULL,NULL),
	 ('st-53','Straight','South','Center',980,260,NULL,NULL,NULL),
	 ('st-43','Straight','West','Center',660,700,NULL,NULL,NULL),
	 ('st-83','Straight','South','Center',940,620,NULL,NULL,NULL),
	 ('se-27','Sensor','East','Center',660,420,NULL,NULL,'65-2010'),
	 ('se-2','Sensor','South','Center',140,180,NULL,NULL,'65-1001'),
	 ('ct-31','Curved','North','Center',260,660,NULL,NULL,NULL),
	 ('se-37','Sensor','West','Center',460,660,NULL,NULL,'65-3'),
	 ('se-18','Sensor','West','Center',620,300,NULL,NULL,'65-2007'),
	 ('st-79','Straight','East','Center',340,740,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-3','Sensor','East','Center',460,60,NULL,NULL,'65-1008'),
	 ('bk-8','Block','East','Center',500,380,NULL,NULL,NULL),
	 ('sd-20','StraightDirection','East','Center',460,780,NULL,NULL,NULL),
	 ('ct-5','Curved','West','Center',740,220,NULL,NULL,NULL),
	 ('ct-2','Curved','East','Center',180,100,NULL,NULL,NULL),
	 ('sw-5','Switch','East','Left',540,140,NULL,'34',NULL),
	 ('st-14','Straight','West','Center',700,380,NULL,NULL,NULL),
	 ('ct-33','Curved','South','Center',780,660,NULL,NULL,NULL),
	 ('si-8','Signal','East','Center',500,140,NULL,'19',NULL),
	 ('st-62','Straight','South','Center',980,620,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-30','Curved','East','Center',220,540,NULL,NULL,NULL),
	 ('bk-17','Block','East','Center',620,780,NULL,NULL,NULL),
	 ('et-2','End','West','Center',260,180,NULL,NULL,NULL),
	 ('st-87','Straight','West','Center',300,660,NULL,NULL,NULL),
	 ('sd-9','StraightDirection','East','Center',660,580,NULL,NULL,NULL),
	 ('si-12','Signal','West','Center',420,660,NULL,'21',NULL),
	 ('st-45','Straight','West','Center',740,700,NULL,NULL,NULL),
	 ('st-96','Straight','South','Center',980,660,NULL,NULL,NULL),
	 ('sd-21','StraightDirection','East','Center',900,780,NULL,NULL,NULL),
	 ('ct-23','Curved','East','Center',300,420,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-28','Curved','North','Center',340,700,NULL,NULL,NULL),
	 ('st-74','Straight','West','Center',940,780,NULL,NULL,NULL),
	 ('st-39','Straight','West','Center',660,660,NULL,NULL,NULL),
	 ('ct-29','Curved','North','Center',380,780,NULL,NULL,NULL),
	 ('st-3','Straight','East','Center',660,60,NULL,NULL,NULL),
	 ('bk-7','Block','West','Center',460,340,NULL,NULL,NULL),
	 ('se-46','Sensor','West','Center',700,740,NULL,NULL,'65-13'),
	 ('si-11','Signal','East','Center',620,580,NULL,'51',NULL),
	 ('sw-7','Switch','East','Left',820,180,NULL,'11',NULL),
	 ('st-57','Straight','South','Center',980,420,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-20','Sensor','East','Center',340,380,NULL,NULL,'65-2003'),
	 ('ct-22','Curved','East','Center',260,380,NULL,NULL,NULL),
	 ('st-36','Straight','East','Center',340,580,NULL,NULL,NULL),
	 ('st-63','Straight','South','Center',820,420,NULL,NULL,NULL),
	 ('ct-1','Curved','East','Center',140,60,NULL,NULL,NULL),
	 ('bk-15','Block','South','Center',900,420,NULL,NULL,NULL),
	 ('st-52','Straight','South','Center',980,220,NULL,NULL,NULL),
	 ('sw-12','Switch','West','Right',380,740,NULL,'4',NULL),
	 ('st-4','Straight','East','Center',300,100,NULL,NULL,NULL),
	 ('st-91','Straight','South','Center',220,620,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-31','Straight','East','Center',780,380,NULL,NULL,NULL),
	 ('se-36','Sensor','East','Center',380,700,NULL,NULL,'65-12'),
	 ('ct-13','Curved','East','Center',660,260,NULL,NULL,NULL),
	 ('st-78','Straight','East','Center',420,780,NULL,NULL,NULL),
	 ('se-26','Sensor','East','Center',660,380,NULL,NULL,'65-2012'),
	 ('sw-8','Switch','West','Left',220,460,NULL,'10',NULL),
	 ('st-10','Straight','East','Center',620,100,NULL,NULL,NULL),
	 ('sd-11','StraightDirection','East','Center',860,100,NULL,NULL,NULL),
	 ('se-53','Sensor','South','Center',940,180,NULL,NULL,'65-1006'),
	 ('st-22','Straight','West','Center',780,180,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('sd-10','StraightDirection','West','Center',660,540,NULL,NULL,NULL),
	 ('bk-10','Block','West','Center',540,500,NULL,NULL,NULL),
	 ('se-12','Sensor','East','Center',300,300,NULL,NULL,'65-2016'),
	 ('ct-38','Curved','West','Center',900,580,NULL,NULL,NULL),
	 ('se-8','Sensor','West','Center',300,140,NULL,NULL,'65-1012'),
	 ('sw-11','Switch','West','Right',340,660,NULL,'3',NULL),
	 ('st-89','Straight','West','Center',900,100,NULL,NULL,NULL),
	 ('se-32','Sensor','West','Center',500,540,NULL,NULL,'65-1'),
	 ('ct-34','Curved','South','Center',980,60,NULL,NULL,NULL),
	 ('st-72','Straight','West','Center',860,580,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('sd-15','StraightDirection','West','Center',700,660,NULL,NULL,NULL),
	 ('st-61','Straight','South','Center',980,580,NULL,NULL,NULL),
	 ('ct-36','Curved','South','Center',820,380,NULL,NULL,NULL),
	 ('st-13','Straight','East','Center',260,60,NULL,NULL,NULL),
	 ('se-1','Sensor','South','Center',180,140,NULL,NULL,'65-1009'),
	 ('ct-8','Curved','East','Center',260,300,NULL,NULL,NULL),
	 ('st-11','Straight','East','Center',660,100,NULL,NULL,NULL),
	 ('se-23','Sensor','East','Center',420,420,NULL,NULL,'65-2002'),
	 ('st-85','Straight','West','Center',860,700,NULL,NULL,NULL),
	 ('st-82','Straight','South','Center',940,580,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-90','Straight','North','Center',940,220,NULL,NULL,NULL),
	 ('ct-4','Curved','West','Center',540,180,NULL,NULL,NULL),
	 ('sd-3','StraightDirection','West','Center',380,60,NULL,NULL,NULL),
	 ('st-56','Straight','South','Center',980,380,NULL,NULL,NULL),
	 ('sd-7','StraightDirection','East','Center',380,380,NULL,NULL,NULL),
	 ('se-4','Sensor','East','Center',620,60,NULL,NULL,'65-1007'),
	 ('se-43','Sensor','South','Center',900,220,NULL,NULL,'65-2028'),
	 ('se-28','Sensor','West','Center',460,500,NULL,NULL,'65-2025'),
	 ('et-1','End','West','Center',260,140,NULL,NULL,NULL),
	 ('st-40','Straight','West','Center',540,540,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-25','Sensor','East','Center',580,420,NULL,NULL,'65-2009'),
	 ('ct-35','Curved','West','Center',940,700,NULL,NULL,NULL),
	 ('ct-14','Curved','East','Center',740,260,NULL,NULL,NULL),
	 ('bk-1','Block','East','Center',540,60,NULL,NULL,NULL),
	 ('si-16','Signal','East','Center',740,780,NULL,'22',NULL),
	 ('st-44','Straight','West','Center',740,660,NULL,NULL,NULL),
	 ('st-9','Straight','East','Center',580,100,NULL,NULL,NULL),
	 ('si-10','Signal','West','Center',300,540,NULL,'36',NULL),
	 ('se-21','Sensor','East','Center',340,420,NULL,NULL,'65-2001'),
	 ('st-46','Straight','West','Center',860,780,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('bk-16','Block','East','Center',620,740,NULL,NULL,NULL),
	 ('ct-37','Curved','West','Center',820,540,NULL,NULL,NULL),
	 ('se-10','Sensor','West','Center',460,140,NULL,NULL,'65-2031'),
	 ('st-20','Straight','South','Center',140,100,NULL,NULL,NULL),
	 ('sd-6','StraightDirection','West','Center',580,340,NULL,NULL,NULL),
	 ('bk-3','Block','South','Center',140,260,NULL,NULL,NULL),
	 ('ct-3','Curved','North','Center',180,340,NULL,NULL,NULL),
	 ('ct-32','Curved','East','Center',260,580,NULL,NULL,NULL),
	 ('sd-19','StraightDirection','East','Center',460,740,NULL,NULL,NULL),
	 ('se-35','Sensor','East','Center',380,660,NULL,NULL,'65-11');
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-15','Curved','West','Center',820,220,NULL,NULL,NULL),
	 ('st-17','Straight','East','Center',220,60,NULL,NULL,NULL),
	 ('bk-9','Block','East','Center',500,420,NULL,NULL,NULL),
	 ('st-41','Straight','West','Center',580,540,NULL,NULL,NULL),
	 ('se-39','Sensor','West','Center',620,660,NULL,NULL,'65-8'),
	 ('se-34','Sensor','West','Center',580,580,NULL,NULL,'65-1013'),
	 ('se-49','Sensor','East','Center',540,780,NULL,NULL,'65-7'),
	 ('si-4','Signal','West','Center',340,340,NULL,'29',NULL),
	 ('st-28','Straight','East','Center',260,460,NULL,NULL,NULL),
	 ('st-37','Straight','South','Center',260,620,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-7','Straight','East','Center',500,100,NULL,NULL,NULL),
	 ('sd-13','StraightDirection','South','Center',220,580,NULL,NULL,NULL),
	 ('si-2','Signal','South','Center',140,380,NULL,'33',NULL),
	 ('st-51','Straight','South','Center',980,180,NULL,NULL,NULL),
	 ('st-6','Straight','East','Center',460,100,NULL,NULL,NULL),
	 ('ct-43','Curved','West','Center',980,780,NULL,NULL,NULL),
	 ('sd-12','StraightDirection','West','Center',860,60,NULL,NULL,NULL),
	 ('se-30','Sensor','East','Center',260,540,NULL,NULL,'65-6'),
	 ('sd-1','StraightDirection','West','Center',220,340,NULL,NULL,NULL),
	 ('st-33','Straight','West','Center',740,100,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-15','Sensor','West','Center',380,340,NULL,NULL,'65-2013'),
	 ('ct-7','Curved','West','Center',700,260,NULL,NULL,NULL),
	 ('st-8','Straight','East','Center',540,100,NULL,NULL,NULL),
	 ('sw-16','Switch','West','Left',780,100,NULL,'7',NULL),
	 ('ct-18','Curved','North','Center',660,180,NULL,NULL,NULL),
	 ('st-38','Straight','East','Center',300,580,NULL,NULL,NULL),
	 ('sd-4','StraightDirection','East','Center',380,100,NULL,NULL,NULL),
	 ('bk-4','Block','West','Center',380,140,NULL,NULL,NULL),
	 ('st-60','Straight','South','Center',980,540,NULL,NULL,NULL),
	 ('se-33','Sensor','West','Center',420,580,NULL,NULL,'65-1014');
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('si-6','Signal','East','Center',620,420,NULL,'25',NULL),
	 ('st-5','Straight','East','Center',420,100,NULL,NULL,NULL),
	 ('ct-17','Curved','West','Center',780,260,NULL,NULL,NULL),
	 ('bk-11','Block','West','Center',420,540,NULL,NULL,NULL),
	 ('ct-40','Curved','South','Center',820,740,NULL,NULL,NULL),
	 ('ct-41','Curved','South','Center',940,100,NULL,NULL,NULL),
	 ('st-29','Straight','East','Center',300,380,NULL,NULL,NULL),
	 ('se-52','Sensor','South','Center',940,540,NULL,NULL,'65-10'),
	 ('si-7','Signal','West','Center',420,500,NULL,'53',NULL),
	 ('st-27','Straight','East','Center',180,460,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-71','Straight','South','Center',940,140,NULL,NULL,NULL),
	 ('sd-2','StraightDirection','East','Center',260,100,NULL,NULL,NULL),
	 ('sw-10','Switch','East','Left',740,380,NULL,'12',NULL),
	 ('st-35','Straight','West','Center',820,100,NULL,NULL,NULL),
	 ('st-55','Straight','South','Center',980,340,NULL,NULL,NULL),
	 ('bk-14','Block','West','Center',540,700,NULL,NULL,NULL),
	 ('sd-8','StraightDirection','East','Center',380,420,NULL,NULL,NULL),
	 ('st-73','Straight','West','Center',500,780,NULL,NULL,NULL),
	 ('se-42','Sensor','North','Center',900,340,NULL,NULL,'65-2022'),
	 ('st-65','Straight','South','Center',820,500,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('si-5','Signal','East','Center',620,380,NULL,'27',NULL),
	 ('sw-9','Switch','West','Left',300,460,NULL,'14',NULL),
	 ('et-3','End','East','Center',660,500,NULL,NULL,NULL),
	 ('sw-13','Switch','East','Right',820,780,NULL,'6',NULL),
	 ('se-13','Sensor','East','Center',300,340,NULL,NULL,'65-2014'),
	 ('st-92','Straight','South','Center',220,660,NULL,NULL,NULL),
	 ('se-14','Sensor','West','Center',380,300,NULL,NULL,'65-2015'),
	 ('se-45','Sensor','West','Center',780,780,NULL,NULL,'65-16'),
	 ('ct-6','Curved','East','Center',700,220,NULL,NULL,NULL),
	 ('st-30','Straight','East','Center',380,500,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('si-15','Signal','East','Center',740,740,NULL,'24',NULL),
	 ('se-29','Sensor','West','Center',620,500,NULL,NULL,'65-1003'),
	 ('se-5','Sensor','South','Center',180,300,NULL,NULL,'65-1015'),
	 ('sw-2','Switch','East','Right',340,100,NULL,'17',NULL),
	 ('se-31','Sensor','West','Center',340,540,NULL,NULL,'65-2'),
	 ('ct-10','Curved','West','Center',700,340,NULL,NULL,NULL),
	 ('se-11','Sensor','West','Center',460,180,NULL,NULL,'65-2032'),
	 ('sd-5','StraightDirection','West','Center',580,300,NULL,NULL,NULL),
	 ('se-24','Sensor','East','Center',580,380,NULL,NULL,'65-2011'),
	 ('si-17','Signal','South','Center',940,500,NULL,'35',NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-98','Straight','South','Center',980,740,NULL,NULL,NULL),
	 ('st-95','Straight','East','Center',300,740,NULL,NULL,NULL),
	 ('se-17','Sensor','West','Center',540,340,NULL,NULL,'65-2006'),
	 ('st-19','Straight','East','Center',220,100,NULL,NULL,NULL),
	 ('sw-15','Switch','East','Left',780,60,NULL,'8',NULL),
	 ('st-59','Straight','South','Center',980,500,NULL,NULL,NULL),
	 ('st-2','Straight','East','Center',740,60,NULL,NULL,NULL),
	 ('bk-18','Block','South','Center',940,380,NULL,NULL,NULL),
	 ('ct-39','Curved','South','Center',900,180,NULL,NULL,NULL),
	 ('st-67','Straight','West','Center',740,580,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('bk-5','Block','West','Center',380,180,NULL,NULL,NULL),
	 ('st-54','Straight','North','Center',980,300,NULL,NULL,NULL),
	 ('st-50','Straight','South','Center',980,140,NULL,NULL,NULL),
	 ('st-84','Straight','South','Center',940,660,NULL,NULL,NULL),
	 ('ct-19','Curved','South','Center',660,140,NULL,NULL,NULL),
	 ('ct-24','Curved','North','Center',140,460,NULL,NULL,NULL),
	 ('st-69','Straight','West','Center',820,580,NULL,NULL,NULL),
	 ('st-1','Straight','East','Center',700,60,NULL,NULL,NULL),
	 ('ct-26','Curved','South','Center',340,460,NULL,NULL,NULL),
	 ('se-51','Sensor','North','Center',940,460,NULL,NULL,'65-2017');
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('sw-18','Switch','West','Right',700,540,NULL,'2',NULL),
	 ('st-16','Straight','West','Center',580,140,NULL,NULL,NULL),
	 ('se-55','Sensor','North','Center',940,300,NULL,NULL,'65-1004'),
	 ('se-7','Sensor','South','Center',140,420,NULL,NULL,'65-2020'),
	 ('se-48','Sensor','East','Center',540,740,NULL,NULL,'65-5'),
	 ('st-18','Straight','East','Center',180,60,NULL,NULL,NULL),
	 ('st-75','Straight','West','Center',900,700,NULL,NULL,NULL),
	 ('si-3','Signal','West','Center',340,300,NULL,'31',NULL),
	 ('sw-14','Switch','East','Right',700,580,NULL,'1',NULL),
	 ('se-38','Sensor','West','Center',460,700,NULL,NULL,'65-4');
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-26','Straight','North','Center',140,140,NULL,NULL,NULL),
	 ('sw-4','Switch','East','Right',780,700,NULL,'5',NULL),
	 ('st-15','Straight','West','Center',700,420,NULL,NULL,NULL),
	 ('st-76','Straight','East','Center',500,740,NULL,NULL,NULL),
	 ('sd-16','StraightDirection','West','Center',700,700,NULL,NULL,NULL),
	 ('st-64','Straight','West','Center',780,540,NULL,NULL,NULL),
	 ('bk-12','Block','West','Center',500,580,NULL,NULL,NULL),
	 ('bk-13','Block','West','Center',540,660,NULL,NULL,NULL),
	 ('ct-20','Curved','East','Center',220,420,NULL,NULL,NULL),
	 ('ct-16','Curved','East','Center',780,220,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-12','Curved','West','Center',740,300,NULL,NULL,NULL),
	 ('st-48','Straight','West','Center',940,60,NULL,NULL,NULL),
	 ('st-70','Straight','North','Center',940,260,NULL,NULL,NULL),
	 ('ct-42','Curved','North','Center',220,740,NULL,NULL,NULL),
	 ('ct-25','Curved','West','Center',740,420,NULL,NULL,NULL),
	 ('si-1','Signal','West','Center',420,60,NULL,'47',NULL),
	 ('sw-1','Switch','West','Right',340,60,NULL,'18',NULL),
	 ('st-93','Straight','South','Center',220,700,NULL,NULL,NULL),
	 ('ct-27','Curved','North','Center',340,500,NULL,NULL,NULL),
	 ('st-58','Straight','South','Center',980,460,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-9','Curved','West','Center',660,300,NULL,NULL,NULL),
	 ('si-19','Signal','North','Center',900,300,NULL,'41',NULL),
	 ('si-9','Signal','East','Center',500,180,NULL,'20',NULL),
	 ('se-19','Sensor','West','Center',620,340,NULL,NULL,'65-2005'),
	 ('se-54','Sensor','North','Center',900,260,NULL,NULL,'65-2021'),
	 ('si-13','Signal','West','Center',420,700,NULL,'23',NULL),
	 ('sd-18','StraightDirection','South','Center',820,460,NULL,NULL,NULL),
	 ('st-23','Straight','West','Center',700,180,NULL,NULL,NULL),
	 ('ct-21','Curved','West','Center',260,420,NULL,NULL,NULL),
	 ('st-68','Straight','West','Center',780,580,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-12','Straight','East','Center',300,60,NULL,NULL,NULL),
	 ('st-94','Straight','East','Center',260,740,NULL,NULL,NULL),
	 ('bk-6','Block','West','Center',460,300,NULL,NULL,NULL),
	 ('ct-11','Curved','East','Center',700,300,NULL,NULL,NULL),
	 ('se-47','Sensor','West','Center',700,780,NULL,NULL,'65-14'),
	 ('st-49','Straight','South','Center',980,100,NULL,NULL,NULL),
	 ('st-86','Straight','West','Center',820,700,NULL,NULL,NULL),
	 ('se-44','Sensor','West','Center',780,740,NULL,NULL,'65-15');

commit;