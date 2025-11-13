delete from route_elements;
delete from routes;
delete from blocks;
delete from tiles;
commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('tw-1','ThreeWay','North','Center',500,340,NULL,NULL,NULL),
	 ('bk-1','Block','South','Center',500,180,NULL,NULL,NULL),
	 ('bk-2','Block','South','Center',500,540,NULL,NULL,NULL),
	 ('bk-3','Block','North','Center',420,540,NULL,NULL,NULL),
	 ('bk-4','Block','North','Center',580,540,NULL,NULL,NULL),
	 ('ct-1','Curved','South','Center',580,380,NULL,NULL,NULL),
	 ('ct-2','Curved','West','Center',460,380,NULL,NULL,NULL),
	 ('ct-3','Curved','East','Center',460,340,NULL,NULL,NULL),
	 ('se-1','Sensor','South','Center',500,620,NULL,NULL,NULL),
	 ('se-2','Sensor','South','Center',580,460,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-3','Sensor','South','Center',500,460,NULL,NULL,NULL),
	 ('se-4','Sensor','South','Center',420,620,NULL,NULL,NULL),
	 ('se-5','Sensor','South','Center',580,620,NULL,NULL,NULL),
	 ('se-6','Sensor','South','Center',420,460,NULL,NULL,NULL),
	 ('se-7','Sensor','South','Center',500,100,NULL,NULL,NULL),
	 ('se-8','Sensor','South','Center',500,260,NULL,NULL,NULL),
	 ('st-1','Straight','South','Center',500,380,NULL,NULL,NULL),
	 ('st-2','Straight','South','Center',580,420,NULL,NULL,NULL),
	 ('st-3','Straight','South','Center',500,420,NULL,NULL,NULL),
	 ('st-4','Straight','South','Center',420,420,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-5','Straight','South','Center',420,660,NULL,NULL,NULL),
	 ('st-6','Straight','South','Center',500,660,NULL,NULL,NULL),
	 ('st-7','Straight','South','Center',580,660,NULL,NULL,NULL),
	 ('st-8','Straight','South','Center',500,300,NULL,NULL,NULL),
	 ('st-9','Straight','South','Center',500,60,NULL,NULL,NULL),
	 ('et-1','End','North','Center',500,20,NULL,NULL,NULL),
	 ('et-2','End','South','Center',420,700,NULL,NULL,NULL),
	 ('et-3','End','South','Center',500,700,NULL,NULL,NULL),
	 ('et-4','End','South','Center',580,700,NULL,NULL,NULL),
	 ('ct-4','Curved','North','Center',540,380,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-5','Curved','South','Center',540,340,NULL,NULL,NULL),
	 ('ct-6','Curved','East','Center',420,380,NULL,NULL,NULL);


commit;