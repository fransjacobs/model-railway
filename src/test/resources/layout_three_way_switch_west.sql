delete from route_elements;
delete from routes;
delete from blocks;
delete from tiles;
commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('tw-1','ThreeWay','West','Center',420,340,NULL,NULL,NULL),
	 ('bk-1','Block','West','Center',220,340,NULL,NULL,NULL),
	 ('bk-2','Block','West','Center',620,340,NULL,NULL,NULL),
	 ('bk-3','Block','East','Center',620,420,NULL,NULL,NULL),
	 ('bk-4','Block','East','Center',620,260,NULL,NULL,NULL),
	 ('ct-1','Curved','South','Center',460,380,NULL,NULL,NULL),
	 ('ct-2','Curved','West','Center',460,300,NULL,NULL,NULL),
	 ('ct-3','Curved','East','Center',420,300,NULL,NULL,NULL),
	 ('se-1','Sensor','West','Center',540,420,NULL,NULL,NULL),
	 ('se-2','Sensor','West','Center',140,340,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('se-3','Sensor','West','Center',700,260,NULL,NULL,NULL),
	 ('se-4','Sensor','West','Center',700,420,NULL,NULL,NULL),
	 ('se-5','Sensor','West','Center',300,340,NULL,NULL,NULL),
	 ('se-6','Sensor','West','Center',540,260,NULL,NULL,NULL),
	 ('se-7','Sensor','West','Center',540,340,NULL,NULL,NULL),
	 ('se-8','Sensor','West','Center',700,340,NULL,NULL,NULL),
	 ('st-1','Straight','West','Center',380,340,NULL,NULL,NULL),
	 ('st-2','Straight','West','Center',500,420,NULL,NULL,NULL),
	 ('st-3','Straight','West','Center',460,340,NULL,NULL,NULL),
	 ('st-4','Straight','West','Center',100,340,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-5','Straight','West','Center',740,420,NULL,NULL,NULL),
	 ('st-6','Straight','West','Center',500,260,NULL,NULL,NULL),
	 ('st-7','Straight','West','Center',740,260,NULL,NULL,NULL),
	 ('st-8','Straight','West','Center',500,340,NULL,NULL,NULL),
	 ('st-9','Straight','West','Center',740,340,NULL,NULL,NULL),
	 ('et-1','End','East','Center',780,420,NULL,NULL,NULL),
	 ('et-2','End','West','Center',60,340,NULL,NULL,NULL),
	 ('et-3','End','East','Center',780,340,NULL,NULL,NULL),
	 ('et-4','End','East','Center',780,260,NULL,NULL,NULL),
	 ('ct-4','Curved','North','Center',420,380,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('ct-5','Curved','East','Center',460,260,NULL,NULL,NULL),
	 ('ct-6','Curved','North','Center',460,420,NULL,NULL,NULL),
	 ('st-10','Straight','West','Center',340,340,NULL,NULL,NULL);

commit;