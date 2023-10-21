delete from route_elements;
delete from routes;
delete from blocks;
delete from tiles;
commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-11','Straight','West','Center',300,340,NULL,NULL,NULL),
	 ('st-24','Straight','South','Center',580,100,NULL,NULL,NULL),
	 ('st-5','Straight','East','Center',620,340,NULL,NULL,NULL),
	 ('bk-4','Block','North','Center',580,180,NULL,NULL,NULL),
	 ('st-1','Straight','East','Center',460,340,NULL,NULL,NULL),
	 ('et-1','End','West','Center',260,340,NULL,NULL,NULL),
	 ('bk-1','Block','East','Center',380,340,NULL,NULL,NULL),
	 ('st-30','Straight','North','Center',580,300,NULL,NULL,NULL),
	 ('st-29','Straight','North','Center',580,260,NULL,NULL,NULL),
	 ('st-6','Straight','East','Center',660,340,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-27','Straight','South','Center',540,580,NULL,NULL,NULL),
	 ('st-26','Straight','South','Center',540,420,NULL,NULL,NULL),
	 ('et-4','End','North','Center',580,60,NULL,NULL,NULL),
	 ('bk-3','Block','North','Center',540,500,NULL,NULL,NULL),
	 ('st-23','Straight','West','Center',820,340,NULL,NULL,NULL),
	 ('bk-2','Block','East','Center',740,340,NULL,NULL,NULL),
	 ('et-2','End','East','Center',860,340,NULL,NULL,NULL),
	 ('st-2','Straight','East','Center',500,340,NULL,NULL,NULL),
	 ('cs-3','Cross','East','Left',540,340,NULL,NULL,NULL),
	 ('et-3','End','South','Center',540,620,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-25','Straight','South','Center',540,380,NULL,NULL,NULL);

commit;