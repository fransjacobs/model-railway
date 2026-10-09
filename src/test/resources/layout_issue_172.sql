delete from route_elements;
delete from routes;
delete from station_blocks;
delete from stations;
delete from blocks;
delete from tiles;
delete from stations;

commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-1','Straight','South','Center',220,380,NULL,NULL,NULL),
	 ('bk-1','Block','South','Center',220,180,NULL,NULL,NULL),
	 ('bk-2','Block','South','Center',260,180,NULL,NULL,NULL),
	 ('bk-3','Block','South','Center',340,180,NULL,NULL,NULL),
	 ('se-1','Sensor','South','Center',220,260,NULL,NULL,NULL),
	 ('se-2','Sensor','South','Center',340,260,NULL,NULL,NULL),
	 ('se-3','Sensor','South','Center',420,260,NULL,NULL,NULL),
	 ('cs-1','CrossSwitch','South','Left',260,300,'NONE',NULL,NULL),
	 ('cs-2','CrossSwitch','North','Left',340,420,'NONE',NULL,NULL),
	 ('tw-1','ThreeWay','South','Center',380,420,'NONE',NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-2','Straight','South','Center',340,460,NULL,NULL,NULL),
	 ('ct-1','Curved','West','Center',420,420,NULL,NULL,NULL),
	 ('st-4','Straight','North','Center',380,380,NULL,NULL,NULL),
	 ('st-5','Straight','North','Center',420,380,NULL,NULL,NULL),
	 ('sw-1','Switch','North','Right',380,300,'NONE',NULL,NULL),
	 ('sw-2','Switch','North','Left',220,300,'NONE',NULL,NULL),
	 ('st-6','Straight','East','Center',260,500,NULL,NULL,NULL),
	 ('st-7','Straight','North','Center',380,340,NULL,NULL,NULL),
	 ('st-8','Straight','North','Center',420,340,NULL,NULL,NULL),
	 ('se-4','Sensor','North','Center',380,260,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-5','Sensor','North','Center',260,260,NULL,NULL,NULL),
	 ('se-6','Sensor','North','Center',-100,180,NULL,NULL,NULL),
	 ('bk-4','Block','South','Center',380,180,NULL,NULL,NULL),
	 ('bk-5','Block','South','Center',420,180,NULL,NULL,NULL),
	 ('bk-6','Block','West','Center',580,460,NULL,NULL,NULL),
	 ('bk-7','Block','North','Center',180,300,NULL,NULL,NULL),
	 ('cx-5','Cross','South','Center',300,340,NULL,NULL,NULL),
	 ('cs-3','CrossSwitch','South','Right',340,300,'NONE',null,NULL),
	 ('cs-10','CrossSwitch','South','Right',260,380,'NONE',NULL,NULL),
	 ('st-9','Straight','South','Center',220,340,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-7','Sensor','South','Center',220,100,NULL,NULL,NULL),
	 ('se-8','Sensor','South','Center',260,100,NULL,NULL,NULL),
	 ('se-9','Sensor','South','Center',340,100,NULL,NULL,NULL),
	 ('se-10','Sensor','South','Center',380,100,NULL,NULL,NULL),
	 ('se-11','Sensor','South','Center',420,100,NULL,NULL,NULL),
	 ('et-1','End','North','Center',220,20,NULL,NULL,NULL),
	 ('et-2','End','North','Center',260,20,NULL,NULL,NULL),
	 ('et-3','End','North','Center',340,20,NULL,NULL,NULL),
	 ('et-4','End','North','Center',380,20,NULL,NULL,NULL),
	 ('et-5','End','North','Center',420,20,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-12','Straight','North','Center',220,60,NULL,NULL,NULL),
	 ('st-13','Straight','North','Center',260,60,NULL,NULL,NULL),
	 ('st-14','Straight','North','Center',340,60,NULL,NULL,NULL),
	 ('st-15','Straight','North','Center',380,60,NULL,NULL,NULL),
	 ('st-16','Straight','North','Center',420,60,NULL,NULL,NULL),
	 ('st-17','Straight','North','Center',420,300,NULL,NULL,NULL),
	 ('sd-1','StraightDirection','South','Center',340,540,NULL,NULL,NULL),
	 ('sd-2','StraightDirection','North','Center',380,460,NULL,NULL,NULL),
	 ('sd-3','StraightDirection','South','Center',180,380,NULL,NULL,NULL),
	 ('sd-4','StraightDirection','West','Center',140,420,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('et-7','End','East','Center',660,460,NULL,NULL,NULL),
	 ('st-20','Straight','West','Center',220,460,NULL,NULL,NULL),
	 ('bk-8','Block','East','Center',500,540,NULL,NULL,NULL),
	 ('bk-9','Block','West','Center',500,580,NULL,NULL,NULL),
	 ('ct-4','Curved','North','Center',380,540,NULL,NULL,NULL),
	 ('ct-5','Curved','North','Center',340,580,NULL,NULL,NULL),
	 ('st-22','Straight','East','Center',300,500,NULL,NULL,NULL),
	 ('st-23','Straight','East','Center',380,580,NULL,NULL,NULL),
	 ('st-24','Straight','East','Center',420,580,NULL,NULL,NULL),
	 ('st-25','Straight','East','Center',420,540,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-26','Straight','East','Center',580,540,NULL,NULL,NULL),
	 ('st-27','Straight','East','Center',580,580,NULL,NULL,NULL),
	 ('cx-8','Cross','East','Center',-140,340,NULL,NULL,NULL),
	 ('et-8','End','East','Center',620,540,NULL,NULL,NULL),
	 ('et-9','End','East','Center',620,580,NULL,NULL,NULL),
	 ('cr-1','Crossing','East','Center',180,420,NULL,NULL,NULL),
	 ('st-28','Straight','West','Center',220,500,NULL,NULL,NULL),
	 ('st-31','Straight','North','Center',100,460,NULL,NULL,NULL),
	 ('ct-8','Curved','East','Center',100,420,NULL,NULL,NULL),
	 ('et-10','End','North','Center',180,220,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('sw-4','Switch','West','Left',220,420,NULL,NULL,NULL),
	 ('ct-9','Curved','North','Center',180,460,NULL,NULL,NULL),
	 ('ct-10','Curved','West','Center',260,460,NULL,NULL,NULL),
	 ('cr-2','Crossing','North','Center',340,500,NULL,NULL,NULL),
	 ('cr-3','Crossing','North','Center',380,500,NULL,NULL,NULL),
	 ('st-34','Straight','West','Center',180,500,NULL,NULL,NULL),
	 ('st-35','Straight','West','Center',140,500,NULL,NULL,NULL),
	 ('st-36','Straight','West','Center',420,500,NULL,NULL,NULL),
	 ('st-37','Straight','West','Center',500,460,NULL,NULL,NULL),
	 ('ct-11','Curved','East','Center',460,460,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-12','Curved','West','Center',460,500,NULL,NULL,NULL),
	 ('ct-13','Curved','North','Center',100,500,NULL,NULL,NULL);

commit;