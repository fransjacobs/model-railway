delete from route_elements;
delete from routes;
delete from tiles;
commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-3','Straight','East','Center',300,140,NULL,NULL,NULL),
	 ('ct-1','Curved','South','Center',580,140,NULL,NULL,NULL),
	 ('st-18','Straight','East','Center',540,380,NULL,NULL,NULL),
	 ('st-14','Straight','North','Center',660,340,NULL,NULL,NULL),
	 ('st-2','Straight','East','Center',540,180,NULL,NULL,NULL),
	 ('sw-1','Switch','West','Left',260,180,NULL,NULL,NULL),
	 ('st-1','Straight','East','Center',300,180,NULL,NULL,NULL),
	 ('ct-3','Curved','East','Center',180,180,NULL,NULL,NULL),
	 ('st-7','Straight','South','Center',180,220,NULL,NULL,NULL),
	 ('st-11','Straight','South','Center',660,220,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-17','Straight','East','Center',300,380,NULL,NULL,NULL),
	 ('ct-6','Curved','West','Center',660,380,NULL,NULL,NULL),
	 ('ct-5','Curved','North','Center',180,380,NULL,NULL,NULL),
	 ('st-8','Straight','South','Center',180,260,NULL,NULL,NULL),
	 ('st-5','Straight','East','Center',620,180,NULL,NULL,NULL),
	 ('se-1','Sensor','East','Center',340,180,NULL,NULL,NULL),
	 ('bk-3','Block','West','Center',420,380,NULL,NULL,NULL),
	 ('st-20','Straight','East','Center',620,380,NULL,NULL,NULL),
	 ('bk-2','Block','East','Center',420,140,NULL,NULL,NULL),
	 ('ct-4','Curved','South','Center',660,180,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-12','Straight','South','Center',660,260,NULL,NULL,NULL),
	 ('se-3','Sensor','West','Center',500,140,NULL,NULL,NULL),
	 ('st-19','Straight','East','Center',580,380,NULL,NULL,NULL),
	 ('se-2','Sensor','East','Center',500,180,NULL,NULL,NULL),
	 ('st-9','Straight','South','Center',180,300,NULL,NULL,NULL),
	 ('st-10','Straight','South','Center',180,340,NULL,NULL,NULL),
	 ('se-4','Sensor','West','Center',340,140,NULL,NULL,NULL),
	 ('st-6','Straight','East','Center',220,180,NULL,NULL,NULL),
	 ('st-15','Straight','West','Center',220,380,NULL,NULL,NULL),
	 ('st-4','Straight','West','Center',540,140,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('sw-2','Switch','East','Right',580,180,NULL,NULL,NULL),
	 ('se-6','Sensor','West','Center',500,380,NULL,NULL,NULL),
	 ('st-13','Straight','North','Center',660,300,NULL,NULL,NULL),
	 ('se-5','Sensor','West','Center',340,380,NULL,NULL,NULL),
	 ('bk-1','Block','East','Center',420,180,NULL,NULL,NULL),
	 ('st-16','Straight','West','Center',260,380,NULL,NULL,NULL),
	 ('ct-2','Curved','East','Center',260,140,NULL,NULL,NULL);


commit;