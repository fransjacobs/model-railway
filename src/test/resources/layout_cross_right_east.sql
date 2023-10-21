delete from route_elements;
delete from routes;
delete from blocks;
delete from tiles;
commit;

INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('cs-2','Cross','East','Right',540,300,NULL,NULL,NULL),
	 ('bk-1','Block','East','Center',380,300,NULL,NULL,NULL),
	 ('st-6','Straight','East','Center',660,300,NULL,NULL,NULL),
	 ('st-2','Straight','East','Center',500,300,NULL,NULL,NULL),
	 ('st-25','Straight','South','Center',580,340,NULL,NULL,NULL),
	 ('st-5','Straight','East','Center',620,300,NULL,NULL,NULL),
	 ('st-23','Straight','West','Center',820,300,NULL,NULL,NULL),
	 ('bk-2','Block','East','Center',740,300,NULL,NULL,NULL),
	 ('et-1','End','West','Center',260,300,NULL,NULL,NULL),
	 ('et-2','End','East','Center',860,300,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('bk-4','Block','North','Center',540,140,NULL,NULL,NULL),
	 ('st-26','Straight','South','Center',580,380,NULL,NULL,NULL),
	 ('st-27','Straight','South','Center',580,540,NULL,NULL,NULL),
	 ('et-3','End','South','Center',580,580,NULL,NULL,NULL),
	 ('bk-3','Block','North','Center',580,460,NULL,NULL,NULL),
	 ('et-4','End','North','Center',540,20,NULL,NULL,NULL),
	 ('st-11','Straight','West','Center',300,300,NULL,NULL,NULL),
	 ('st-1','Straight','East','Center',460,300,NULL,NULL,NULL),
	 ('st-29','Straight','North','Center',540,220,NULL,NULL,NULL),
	 ('st-24','Straight','South','Center',540,60,NULL,NULL,NULL);
INSERT INTO jcs.tiles (id,tile_type,orientation,direction,x,y,signal_type,accessory_id,sensor_id) VALUES
	 ('st-30','Straight','North','Center',540,260,NULL,NULL,NULL);

commit;