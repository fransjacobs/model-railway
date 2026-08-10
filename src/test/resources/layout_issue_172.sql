delete from route_elements;
delete from routes;
delete from station_blocks;
delete from stations;
delete from blocks;
delete from tiles;
delete from stations;

commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('bk-2','Block','South','Center',260,180,NULL,NULL,NULL),
	 ('bk-3','Block','South','Center',340,180,NULL,NULL,NULL),
	 ('se-2','Sensor','South','Center',340,260,NULL,NULL,NULL),
	 ('se-3','Sensor','South','Center',420,260,NULL,NULL,NULL),
	 ('cs-2','CrossSwitch','North','Left',340,420,'NONE',NULL,NULL),
	 ('st-2','Straight','West','Center',540,420,NULL,NULL,NULL),
	 ('ct-1','Curved','North','Center',540,380,NULL,NULL,NULL),
	 ('st-5','Straight','North','Center',460,220,NULL,NULL,NULL),
	 ('st-6','Straight','North','Center',540,260,NULL,NULL,NULL),
	 ('st-7','Straight','North','Center',340,460,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-8','Straight','North','Center',420,340,NULL,NULL,NULL),
	 ('se-5','Sensor','North','Center',260,260,NULL,NULL,NULL),
	 ('se-6','Sensor','North','Center',-100,180,NULL,NULL,NULL),
	 ('bk-5','Block','West','Center',460,420,NULL,NULL,NULL),
	 ('cx-5','Cross','South','Center',300,340,NULL,NULL,NULL),
	 ('cs-10','CrossSwitch','North','Right',140,180,'NONE',NULL,NULL),
	 ('st-11','Straight','South','Center',260,420,NULL,NULL,NULL),
	 ('se-8','Sensor','South','Center',260,100,NULL,NULL,NULL),
	 ('se-9','Sensor','South','Center',340,100,NULL,NULL,NULL),
	 ('se-11','Sensor','South','Center',420,100,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('et-2','End','North','Center',260,20,NULL,NULL,NULL),
	 ('et-3','End','North','Center',340,20,NULL,NULL,NULL),
	 ('et-4','End','North','Center',380,20,NULL,NULL,NULL),
	 ('et-5','End','North','Center',420,20,NULL,NULL,NULL),
	 ('st-13','Straight','North','Center',260,60,NULL,NULL,NULL),
	 ('st-14','Straight','North','Center',340,60,NULL,NULL,NULL),
	 ('st-15','Straight','East','Center',380,420,NULL,NULL,NULL),
	 ('st-16','Straight','North','Center',420,60,NULL,NULL,NULL),
	 ('st-17','Straight','North','Center',420,300,NULL,NULL,NULL),
	 ('sd-2','StraightDirection','North','Center',780,420,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('et-6','End','East','Center',580,420,NULL,NULL,NULL),
	 ('et-7','End','South','Center',260,580,NULL,NULL,NULL),
	 ('bk-8','Block','South','Center',260,500,NULL,NULL,NULL),
	 ('bk-9','Block','West','Center',500,540,NULL,NULL,NULL),
	 ('ct-4','Curved','East','Center',260,380,NULL,NULL,NULL),
	 ('ct-5','Curved','North','Center',340,540,NULL,NULL,NULL),
	 ('st-22','Straight','North','Center',340,500,NULL,NULL,NULL),
	 ('st-23','Straight','East','Center',420,540,NULL,NULL,NULL),
	 ('st-24','Straight','East','Center',380,540,NULL,NULL,NULL),
	 ('st-25','Straight','East','Center',660,220,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-26','Straight','East','Center',580,540,NULL,NULL,NULL),
	 ('st-27','Straight','East','Center',580,580,NULL,NULL,NULL),
	 ('et-8','End','East','Center',620,540,NULL,NULL,NULL),
	 ('et-9','End','East','Center',620,580,NULL,NULL,NULL),
	 ('st-29','Straight','North','Center',340,300,NULL,NULL,NULL),
	 ('st-31','Straight','North','Center',260,300,NULL,NULL,NULL),
	 ('ct-7','Curved','North','Center',260,340,NULL,NULL,NULL),
	 ('ct-8','Curved','South','Center',620,340,NULL,NULL,NULL),
	 ('ct-9','Curved','West','Center',700,300,NULL,NULL,NULL),
	 ('sw-1','Switch','North','Right',340,340,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('sw-2','Switch','East','Right',620,460,NULL,NULL,NULL);

commit;