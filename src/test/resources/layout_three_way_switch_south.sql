delete from route_elements;
delete from routes;
delete from blocks;
delete from tiles;
commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('et-1','End','North','Center',260,20,NULL,NULL,NULL),
	 ('et-2','End','North','Center',340,20,NULL,NULL,NULL),
	 ('et-3','End','North','Center',420,20,NULL,NULL,NULL),
	 ('se-1','Sensor','North','Center',260,60,NULL,NULL,NULL),
	 ('se-2','Sensor','North','Center',340,60,NULL,NULL,NULL),
	 ('se-3','Sensor','North','Center',420,60,NULL,NULL,NULL),
	 ('se-4','Sensor','North','Center',260,220,NULL,NULL,NULL),
	 ('se-5','Sensor','North','Center',340,220,NULL,NULL,NULL),
	 ('se-6','Sensor','North','Center',420,220,NULL,NULL,NULL),
	 ('st-1','Straight','North','Center',260,260,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-2','Straight','North','Center',340,260,NULL,NULL,NULL),
	 ('st-3','Straight','North','Center',420,260,NULL,NULL,NULL),
	 ('bk-1','Block','North','Center',340,140,NULL,NULL,NULL),
	 ('tw-1','ThreeWay','South','Center',340,340,NULL,NULL,NULL),
	 ('ct-1','Curved','North','Center',260,300,NULL,NULL,NULL),
	 ('ct-2','Curved','South','Center',300,300,NULL,NULL,NULL),
	 ('ct-3','Curved','North','Center',300,340,NULL,NULL,NULL),
	 ('ct-4','Curved','West','Center',380,340,NULL,NULL,NULL),
	 ('ct-5','Curved','West','Center',420,300,NULL,NULL,NULL),
	 ('ct-6','Curved','East','Center',380,300,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-4','Straight','South','Center',340,300,NULL,NULL,NULL),
	 ('st-5','Straight','South','Center',340,380,NULL,NULL,NULL),
	 ('se-7','Sensor','South','Center',340,420,NULL,NULL,NULL),
	 ('bk-2','Block','South','Center',340,500,NULL,NULL,NULL),
	 ('se-8','Sensor','South','Center',340,580,NULL,NULL,NULL),
	 ('st-6','Straight','South','Center',340,620,NULL,NULL,NULL),
	 ('et-4','End','South','Center',340,660,NULL,NULL,NULL),
	 ('bk-3','Block','South','Center',260,140,NULL,NULL,NULL),
	 ('bk-4','Block','South','Center',420,140,NULL,NULL,NULL);

commit;