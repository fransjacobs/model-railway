delete from locomotive_functions;
delete from locomotives;

alter table locomotive_functions alter column id restart with 1;


INSERT INTO jcs.locomotives (id,name,uid,mfx_uid,address,icon,decoder_type,mfx_sid,tacho_max,v_min,velocity,richtung,commuter,"length",show,imported) VALUES
	 (2,'BR 81 002',2,NULL,2,'DB BR 81 008','mm','0x2',80,1,0,0,false,0,true,'CS3-2374'),
	 (3,'DB bzf 761',3,NULL,3,'DB bzf 761','mm','0x3',120,0,0,0,false,NULL,true,'CS3-2374'),
	 (12,'BR 141 015-08',12,NULL,12,'DB BR 141 136-2','mm','0xc',120,0,0,0,false,NULL,true,'CS3-2374'),
	 (23,'BR 101 003-2',23,NULL,23,'DB BR 101 109-7','mm','0x17',200,0,0,1,false,NULL,true,'CS3-2374'),
	 (36,'ICE 406',36,NULL,36,'NS BR 406 ICE3','mm','0x24',120,0,0,0,false,NULL,true,'CS3-2374'),
	 (37,'NS 1720',37,NULL,37,'NS 1773','mm','0x25',120,0,0,1,false,NULL,true,'CS3-2374'),
	 (61,'BR 610',61,NULL,61,'DB BR 610 015-0','mm','0x3d',120,0,0,0,false,NULL,true,'CS3-2374'),
	 (63,'NS 6513',63,NULL,63,'NS 6513','mm','0x3f',120,0,0,0,false,NULL,true,'CS3-2374'),
	 (68,'NS 1309',68,NULL,68,'NS 1309','mm','0x44',120,0,0,0,false,NULL,true,'CS3-2374'),
	 (16389,'193 304-3 DB AG',16389,NULL,5,'DB BR 193 304-3','mfx','0x4005',160,5,0,0,false,NULL,true,'CS3-2374');
INSERT INTO jcs.locomotives (id,name,uid,mfx_uid,address,icon,decoder_type,mfx_sid,tacho_max,v_min,velocity,richtung,commuter,"length",show,imported) VALUES
	 (16390,'152 119-4 DBAG',16390,NULL,6,'DB BR 152 119-4','mfx+','0x4006',140,4,0,0,false,NULL,true,'CS3-2374'),
	 (16391,'DB 640 017-9',16391,NULL,7,'DB BR 640 017-9','mfx','0x4007',100,8,0,0,false,NULL,true,'CS3-2374'),
	 (16392,'BR 44 690',16392,NULL,8,'DB BR 44 100','mfx','0x4008',80,5,0,0,false,NULL,true,'CS3-2374'),
	 (16393,'Rheingold 1',16393,NULL,9,'DB BR 18 537','mfx','0x4009',81,4,0,0,false,NULL,true,'CS3-2374'),
	 (16394,'561-05 RRF',16394,NULL,10,'56-05 RRF','mfx+','0x400a',120,5,0,1,false,NULL,true,'CS3-2374'),
	 (16395,'E 186 007-8 NS',16395,NULL,11,'NS 186 012-8','mfx','0x400b',140,5,0,0,false,NULL,true,'CS3-2374'),
	 (16396,'BR 216 059-6',16396,NULL,12,'DB BR 216 059-6','mfx','0x400c',120,5,0,0,false,NULL,true,'CS3-2374'),
	 (16397,'NS 1139',16397,NULL,13,'NS 1136','mfx','0x400d',140,6,0,0,false,NULL,true,'CS3-2374'),
	 (16398,'Rheingold 2',16398,NULL,14,'DB BR 18 473','mfx','0x400e',81,4,0,0,false,NULL,true,'CS3-2374'),
	 (16399,'Bpmbdzf 107-5',16399,NULL,15,'DB Bpmbdzf 296.1','mfx','0x400f',121,4,0,0,false,NULL,true,'CS3-2374');
INSERT INTO jcs.locomotives (id,name,uid,mfx_uid,address,icon,decoder_type,mfx_sid,tacho_max,v_min,velocity,richtung,commuter,"length",show,imported) VALUES
	 (16400,'SVT 137',16400,NULL,16,'DRG SVT 137 150','mfx','0x4010',100,3,0,0,false,NULL,true,'CS3-2374'),
	 (16401,'BR 01-097',16401,NULL,17,'DB BR 01 047','mfx','0x4011',120,4,0,0,false,NULL,true,'CS3-2374'),
	 (16402,'SBB Ce 6/8 14305',16402,NULL,18,'SBB Ce 6_8 14305 Hist','mfx','0x4012',75,4,0,0,false,NULL,true,'CS3-2374'),
	 (16403,'193 733-3 NS',16403,NULL,19,'NS 193 733-3','mfx+','0x4013',160,4,0,0,false,NULL,true,'CS3-2374'),
	 (16404,'NS 6503',16404,NULL,20,'NS 6409','mfx','0x4014',120,3,0,0,false,NULL,true,'CS3-2374'),
	 (16405,'NS 6505',16405,NULL,21,'NS DHG 6505','mfx','0x4015',80,3,0,1,false,NULL,true,'CS3-2374'),
	 (16406,'DHG 700 VW',16406,NULL,22,'DHG 700 VW','mfx','0x4016',100,3,0,0,false,NULL,true,'CS3-2374'),
	 (16407,'1102 RRF G2000',16407,NULL,23,'RRF 272 001-9','mfx+','0x4017',140,5,0,1,false,NULL,true,'CS3-2374'),
	 (16408,'1707 NS',16408,NULL,24,'NS 1707','mfx+','0x4018',140,4,0,1,false,NULL,true,'CS3-2374'),
	 (49156,'NS Plan Y',49156,NULL,4,'NS Plan Y','dcc','0xc004',120,0,0,1,false,NULL,true,'CS3-2374');
INSERT INTO jcs.locomotives (id,name,uid,mfx_uid,address,icon,decoder_type,mfx_sid,tacho_max,v_min,velocity,richtung,commuter,"length",show,imported) VALUES
	 (49159,'NS Plan V',49159,NULL,7,'NS Plan V','dcc','0xc007',120,0,0,0,false,NULL,true,'CS3-2374'),
	 (49163,'NS 1205',49163,NULL,11,'NS 1211','dcc','0xc00b',120,0,0,0,false,NULL,true,'CS3-2374'),
	 (49166,'V36 236 002',49166,NULL,14,'FS D.236.002','dcc','0xc00e',80,0,0,0,false,NULL,true,'CS3-2374');


commit;

INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16407,0,1,1,'fkticon_a_001',false),
	 (16407,1,48,0,'fkticon_i_048',false),
	 (16407,2,23,0,'fkticon_i_023',false),
	 (16407,3,10,0,'fkticon_i_010',false),
	 (16407,4,48,0,'fkticon_i_048',false),
	 (16407,5,20,0,'fkticon_i_020',false),
	 (16407,6,41,0,'fkticon_i_041',false),
	 (16407,7,10,0,'fkticon_i_010',true),
	 (16407,8,42,0,'fkticon_i_042',false),
	 (16407,9,18,0,'fkticon_i_018',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16407,10,108,0,'fkticon_i_108',true),
	 (16407,11,43,0,'fkticon_i_043',false),
	 (16407,12,29,0,'fkticon_i_029',false),
	 (16407,13,92,0,'fkticon_i_092',true),
	 (16407,14,44,0,'fkticon_i_044',true),
	 (16407,15,112,1,'fkticon_i_112',false),
	 (16407,16,5,0,'fkticon_i_005',false),
	 (16407,17,11,0,'fkticon_i_011',false),
	 (16407,18,8,0,'fkticon_i_008',false),
	 (16407,19,118,0,'fkticon_i_118',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16390,0,282,1,'fkticon_a_282',false),
	 (16390,1,4,0,'fkticon_i_004',false),
	 (16390,2,23,0,'fkticon_i_023',false),
	 (16390,3,10,0,'fkticon_i_010',false),
	 (16390,4,18,0,'fkticon_i_018',false),
	 (16390,5,20,0,'fkticon_i_020',false),
	 (16390,6,41,0,'fkticon_i_041',false),
	 (16390,7,10,0,'fkticon_i_010',false),
	 (16390,8,42,0,'fkticon_i_042',false),
	 (16390,9,116,0,'fkticon_i_116',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16390,10,92,0,'fkticon_i_092',true),
	 (16390,11,25,0,'fkticon_i_025',true),
	 (16390,12,11,0,'fkticon_i_011',false),
	 (16390,13,25,0,'fkticon_i_025',true),
	 (16390,14,112,1,'fkticon_a_112',false),
	 (16408,0,1,1,'fkticon_a_001',false),
	 (16408,1,84,0,'fkticon_i_084',false),
	 (16408,2,23,0,'fkticon_i_023',false),
	 (16408,3,10,0,'fkticon_i_010',false),
	 (16408,4,85,0,'fkticon_i_085',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16408,5,18,0,'fkticon_i_018',false),
	 (16408,6,20,0,'fkticon_i_020',false),
	 (16408,7,48,0,'fkticon_i_048',false),
	 (16408,8,41,1,'fkticon_a_041',false),
	 (16408,9,250,0,'fkticon_i_250',true),
	 (16408,10,8,0,'fkticon_i_008',false),
	 (16408,11,42,0,'fkticon_i_042',false),
	 (16408,12,43,0,'fkticon_i_043',true),
	 (16408,13,43,0,'fkticon_i_043',true),
	 (16408,14,29,0,'fkticon_i_029',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16408,15,112,0,'fkticon_i_112',false),
	 (16408,16,116,0,'fkticon_i_116',false),
	 (16408,17,92,0,'fkticon_i_092',true),
	 (16408,18,25,0,'fkticon_i_025',true),
	 (16408,19,11,0,'fkticon_i_011',true),
	 (16408,20,118,0,'fkticon_i_118',false),
	 (16408,21,118,0,'fkticon_i_118',false),
	 (16408,22,118,0,'fkticon_i_118',false),
	 (16408,23,146,0,'fkticon_i_146',false),
	 (16408,24,108,0,'fkticon_i_108',true);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16408,25,105,0,'fkticon_i_105',false),
	 (16408,26,3,0,'fkticon_i_003',false),
	 (16389,0,1,1,'fkticon_a_001',false),
	 (16389,1,44,0,'fkticon_i_044',true),
	 (16389,2,23,0,'fkticon_i_023',false),
	 (16389,3,10,0,'fkticon_i_010',false),
	 (16389,4,18,0,'fkticon_i_018',false),
	 (16389,5,20,0,'fkticon_i_020',false),
	 (16389,6,41,0,'fkticon_i_041',false),
	 (16389,7,10,0,'fkticon_i_010',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16389,8,42,0,'fkticon_i_042',false),
	 (16389,9,43,0,'fkticon_i_043',true),
	 (16389,10,43,0,'fkticon_i_043',true),
	 (16389,11,29,0,'fkticon_i_029',false),
	 (16389,12,11,0,'fkticon_i_011',false),
	 (16389,13,116,0,'fkticon_i_116',false),
	 (16389,14,92,0,'fkticon_i_092',true),
	 (16389,15,10,0,'fkticon_i_010',true),
	 (16403,0,1,1,'fkticon_a_001',false),
	 (16403,1,48,0,'fkticon_i_048',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16403,2,23,0,'fkticon_i_023',false),
	 (16403,3,251,0,'fkticon_i_251',false),
	 (16403,4,18,0,'fkticon_i_018',false),
	 (16403,5,20,0,'fkticon_i_020',false),
	 (16403,6,41,0,'fkticon_i_041',false),
	 (16403,7,251,0,'fkticon_i_251',false),
	 (16403,8,42,0,'fkticon_i_042',false),
	 (16403,9,4,0,'fkticon_i_004',false),
	 (16403,10,118,0,'fkticon_i_118',false),
	 (16403,11,118,0,'fkticon_i_118',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16403,12,3,0,'fkticon_i_003',false),
	 (16403,13,47,0,'fkticon_i_047',false),
	 (16403,14,250,0,'fkticon_i_250',true),
	 (16403,15,112,0,'fkticon_i_112',false),
	 (16403,16,8,0,'fkticon_i_008',false),
	 (16403,17,29,0,'fkticon_i_029',false),
	 (16403,18,116,0,'fkticon_i_116',false),
	 (16403,19,92,0,'fkticon_i_092',true),
	 (16403,20,108,0,'fkticon_i_108',true),
	 (16403,21,146,0,'fkticon_i_146',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16403,22,244,0,'fkticon_i_244',false),
	 (16403,23,132,0,'fkticon_i_132',true),
	 (16403,24,132,0,'fkticon_i_132',true),
	 (16403,25,118,0,'fkticon_i_118',false),
	 (16403,26,43,0,'fkticon_i_043',true),
	 (16403,27,43,0,'fkticon_i_043',true),
	 (16394,0,282,1,'fkticon_a_282',false),
	 (16394,1,7,0,'fkticon_i_007',false),
	 (16394,2,23,0,'fkticon_i_023',false),
	 (16394,3,10,0,'fkticon_i_010',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16394,4,18,0,'fkticon_i_018',false),
	 (16394,5,20,0,'fkticon_i_020',false),
	 (16394,6,41,0,'fkticon_i_041',false),
	 (16394,7,10,0,'fkticon_i_010',true),
	 (16394,8,42,0,'fkticon_i_042',false),
	 (16394,9,48,0,'fkticon_i_048',false),
	 (16394,10,29,0,'fkticon_i_029',false),
	 (16394,11,117,0,'fkticon_a_117',false),
	 (16394,12,116,0,'fkticon_i_116',false),
	 (16394,13,118,0,'fkticon_i_118',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16394,14,118,0,'fkticon_i_118',false),
	 (16394,15,112,0,'fkticon_i_112',false),
	 (16394,16,10,0,'fkticon_i_010',false),
	 (16394,17,10,0,'fkticon_i_010',true),
	 (16394,18,8,0,'fkticon_i_008',false),
	 (16394,19,92,0,'fkticon_i_092',true),
	 (16394,20,108,0,'fkticon_i_108',true),
	 (16394,21,43,0,'fkticon_i_043',true),
	 (16394,22,5,0,'fkticon_i_005',false),
	 (16394,23,43,0,'fkticon_i_043',true);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16394,24,132,0,'fkticon_i_132',false),
	 (16394,25,132,0,'fkticon_i_132',false),
	 (16394,26,28,0,'fkticon_i_028',false),
	 (16401,0,1,1,'fkticon_a_001',false),
	 (16401,1,7,0,'fkticon_i_007',false),
	 (16401,2,23,0,'fkticon_i_023',false),
	 (16401,3,12,0,'fkticon_i_012',false),
	 (16401,4,18,0,'fkticon_i_018',false),
	 (16401,5,20,0,'fkticon_i_020',false),
	 (16401,6,136,0,'fkticon_i_136',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16401,7,31,0,'fkticon_i_031',false),
	 (16401,8,106,0,'fkticon_i_106',false),
	 (16401,9,91,0,'fkticon_i_091',true),
	 (16401,10,26,0,'fkticon_i_026',false),
	 (16401,11,36,0,'fkticon_i_036',false),
	 (16401,12,111,0,'fkticon_i_111',false),
	 (16401,13,49,0,'fkticon_i_049',false),
	 (16401,14,12,0,'fkticon_i_012',true),
	 (16401,15,112,1,'fkticon_a_112',false),
	 (16401,16,42,0,'fkticon_i_042',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16401,17,41,0,'fkticon_i_041',false),
	 (16401,18,13,0,'fkticon_i_013',false),
	 (16401,19,108,0,'fkticon_i_108',true),
	 (16401,20,47,0,'fkticon_i_047',false),
	 (16401,21,8,0,'fkticon_i_008',false),
	 (16401,22,22,0,'fkticon_i_022',false),
	 (16401,23,25,0,'fkticon_i_025',true),
	 (16401,24,37,0,'fkticon_i_037',false),
	 (16401,25,123,0,'fkticon_i_123',false),
	 (16401,26,43,0,'fkticon_i_043',true);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16401,27,43,0,'fkticon_i_043',true),
	 (16401,28,11,0,'fkticon_i_011',false),
	 (23,0,1,1,'fkticon_a_001',false),
	 (23,2,4,0,'fkticon_i_004',false),
	 (23,4,18,0,'fkticon_i_018',false),
	 (12,0,1,1,'fkticon_a_001',false),
	 (12,3,8,0,'fkticon_i_008',false),
	 (12,4,18,0,'fkticon_i_018',false),
	 (16396,0,282,1,'fkticon_a_282',false),
	 (16396,4,18,0,'fkticon_i_018',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16392,0,1,1,'fkticon_a_001',false),
	 (16392,1,7,0,'fkticon_i_007',false),
	 (16392,2,23,0,'fkticon_i_023',false),
	 (16392,3,12,0,'fkticon_i_012',false),
	 (16392,4,18,0,'fkticon_i_018',false),
	 (16392,5,20,0,'fkticon_i_020',false),
	 (16392,6,82,0,'fkticon_i_082',false),
	 (16392,7,12,0,'fkticon_i_012',true),
	 (16392,8,31,0,'fkticon_i_031',false),
	 (16392,9,106,0,'fkticon_i_106',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16392,10,91,0,'fkticon_i_091',true),
	 (16392,11,26,0,'fkticon_i_026',false),
	 (16392,12,36,0,'fkticon_i_036',false),
	 (16392,13,111,0,'fkticon_i_111',false),
	 (16392,14,49,0,'fkticon_i_049',false),
	 (16392,16,5,0,'fkticon_i_005',false),
	 (16392,17,5,0,'fkticon_i_005',false),
	 (16392,18,5,0,'fkticon_i_005',false),
	 (16392,19,108,0,'fkticon_i_108',true),
	 (16392,20,43,0,'fkticon_i_043',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16392,21,8,0,'fkticon_i_008',false),
	 (16392,22,22,0,'fkticon_i_022',false),
	 (16392,24,31,0,'fkticon_i_031',false),
	 (16392,25,37,0,'fkticon_i_037',false),
	 (16392,26,123,0,'fkticon_i_123',true),
	 (61,0,1,1,'fkticon_a_001',false),
	 (2,0,1,1,'fkticon_a_001',false),
	 (2,4,18,0,'fkticon_i_018',false),
	 (16399,0,1,1,'fkticon_a_001',false),
	 (16399,1,4,0,'fkticon_i_004',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16399,2,32,1,'fkticon_a_032',false),
	 (16399,3,48,0,'fkticon_i_048',false),
	 (16399,4,32,1,'fkticon_a_032',false),
	 (16391,0,282,1,'fkticon_a_282',false),
	 (16391,2,23,0,'fkticon_i_023',false),
	 (16391,3,10,0,'fkticon_i_010',true),
	 (16391,4,18,0,'fkticon_i_018',false),
	 (3,1,32,1,'fkticon_a_032',false),
	 (3,2,32,1,'fkticon_a_032',false),
	 (16406,0,1,1,'fkticon_a_001',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16406,1,47,0,'fkticon_i_047',false),
	 (16406,2,118,0,'fkticon_i_118',false),
	 (16406,3,8,0,'fkticon_i_008',false),
	 (16406,4,18,0,'fkticon_i_018',false),
	 (16406,5,42,0,'fkticon_i_042',false),
	 (16406,6,41,0,'fkticon_i_041',false),
	 (16395,0,1,1,'fkticon_a_001',false),
	 (16395,1,44,0,'fkticon_i_044',true),
	 (16395,2,23,0,'fkticon_i_023',false),
	 (16395,3,10,0,'fkticon_i_010',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16395,4,18,0,'fkticon_i_018',false),
	 (16395,5,20,0,'fkticon_i_020',false),
	 (16395,6,41,0,'fkticon_i_041',false),
	 (16395,7,10,0,'fkticon_i_010',true),
	 (16395,8,42,0,'fkticon_i_042',false),
	 (16395,9,43,0,'fkticon_i_043',true),
	 (16395,10,43,0,'fkticon_i_043',true),
	 (16395,11,92,0,'fkticon_i_092',true),
	 (16395,12,29,0,'fkticon_i_029',false),
	 (16395,13,11,0,'fkticon_i_011',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16395,14,37,0,'fkticon_i_037',false),
	 (36,0,1,1,'fkticon_a_001',false),
	 (36,2,4,0,'fkticon_i_004',false),
	 (36,4,18,0,'fkticon_i_018',false),
	 (16397,0,1,1,'fkticon_a_001',false),
	 (16397,1,48,0,'fkticon_i_048',false),
	 (16397,2,48,0,'fkticon_i_048',false),
	 (16397,3,8,0,'fkticon_i_008',false),
	 (16397,4,18,0,'fkticon_i_018',false),
	 (16397,5,42,0,'fkticon_i_042',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16397,6,41,0,'fkticon_i_041',false),
	 (16397,7,118,0,'fkticon_i_118',false),
	 (49163,0,1,1,'fkticon_a_001',false),
	 (49163,1,118,0,'fkticon_i_118',false),
	 (49163,2,47,0,'fkticon_i_047',false),
	 (49163,3,8,0,'fkticon_i_008',false),
	 (49163,4,18,0,'fkticon_i_018',false),
	 (49163,5,41,1,'fkticon_a_041',false),
	 (49163,6,42,0,'fkticon_i_042',false),
	 (68,0,1,1,'fkticon_a_001',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (68,3,8,0,'fkticon_i_008',false),
	 (68,4,18,0,'fkticon_i_018',false),
	 (37,0,1,1,'fkticon_a_001',false),
	 (37,2,10,0,'fkticon_i_010',true),
	 (37,3,10,0,'fkticon_i_010',true),
	 (37,4,18,0,'fkticon_i_018',false),
	 (16404,0,1,1,'fkticon_a_001',false),
	 (16404,1,48,0,'fkticon_i_048',false),
	 (16404,2,23,0,'fkticon_i_023',false),
	 (16404,3,8,1,'fkticon_a_008',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16404,4,18,0,'fkticon_i_018',false),
	 (16404,5,42,0,'fkticon_i_042',false),
	 (16404,6,41,0,'fkticon_i_041',false),
	 (16404,7,10,0,'fkticon_i_010',false),
	 (16404,8,10,0,'fkticon_i_010',false),
	 (16404,9,29,0,'fkticon_i_029',false),
	 (16404,10,116,0,'fkticon_i_116',false),
	 (16404,11,49,0,'fkticon_i_049',true),
	 (16404,12,10,0,'fkticon_i_010',true),
	 (16404,13,10,0,'fkticon_i_010',true);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16404,14,11,0,'fkticon_i_011',false),
	 (16404,15,28,0,'fkticon_i_028',true),
	 (16404,16,43,0,'fkticon_i_043',true),
	 (16404,17,5,0,'fkticon_i_005',true),
	 (16404,18,20,0,'fkticon_i_020',false),
	 (16404,19,37,0,'fkticon_i_037',false),
	 (16404,20,129,0,'fkticon_i_129',false),
	 (16404,22,125,0,'fkticon_i_125',false),
	 (16404,24,108,0,'fkticon_i_108',true),
	 (16404,31,5,0,'fkticon_i_005',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16405,0,1,0,'fkticon_i_001',false),
	 (16405,1,212,0,'fkticon_i_212',false),
	 (16405,2,118,0,'fkticon_i_118',false),
	 (16405,3,8,0,'fkticon_i_008',false),
	 (16405,4,18,0,'fkticon_i_018',false),
	 (16405,5,42,0,'fkticon_i_042',false),
	 (16405,6,41,0,'fkticon_i_041',false),
	 (63,0,1,1,'fkticon_a_001',false),
	 (63,4,18,0,'fkticon_i_018',false),
	 (49159,0,282,1,'fkticon_a_282',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (49159,1,48,1,'fkticon_a_048',false),
	 (49159,2,48,1,'fkticon_a_048',false),
	 (49159,3,18,0,'fkticon_i_018',false),
	 (49156,0,1,1,'fkticon_a_001',false),
	 (49156,1,2,1,'fkticon_a_002',false),
	 (49156,2,2,1,'fkticon_a_002',false),
	 (49156,3,8,0,'fkticon_i_008',false),
	 (49156,4,18,0,'fkticon_i_018',false),
	 (16393,0,1,1,'fkticon_a_001',false),
	 (16393,1,7,0,'fkticon_i_007',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16393,2,23,0,'fkticon_i_023',false),
	 (16393,3,8,0,'fkticon_i_008',false),
	 (16393,4,18,0,'fkticon_i_018',false),
	 (16393,5,12,0,'fkticon_i_012',true),
	 (16393,6,12,0,'fkticon_i_012',false),
	 (16393,7,26,0,'fkticon_i_026',false),
	 (16393,8,201,0,'fkticon_i_201',false),
	 (16393,9,13,0,'fkticon_i_013',false),
	 (16393,10,111,0,'fkticon_i_111',false),
	 (16393,11,36,0,'fkticon_i_036',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16393,12,45,0,'fkticon_i_045',true),
	 (16393,13,45,0,'fkticon_i_045',true),
	 (16393,14,122,0,'fkticon_i_122',true),
	 (16393,15,37,0,'fkticon_i_037',false),
	 (16398,0,1,1,'fkticon_a_001',false),
	 (16398,1,7,0,'fkticon_i_007',false),
	 (16398,2,23,0,'fkticon_i_023',false),
	 (16398,3,8,0,'fkticon_i_008',false),
	 (16398,4,18,0,'fkticon_i_018',false),
	 (16398,5,12,0,'fkticon_i_012',true);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16398,6,12,0,'fkticon_i_012',false),
	 (16398,7,26,0,'fkticon_i_026',false),
	 (16398,8,201,0,'fkticon_i_201',false),
	 (16398,9,13,0,'fkticon_i_013',false),
	 (16398,10,111,0,'fkticon_i_111',false),
	 (16398,11,36,0,'fkticon_i_036',false),
	 (16398,12,45,0,'fkticon_i_045',true),
	 (16398,13,45,0,'fkticon_i_045',true),
	 (16398,14,122,0,'fkticon_i_122',true),
	 (16398,15,37,0,'fkticon_i_037',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16402,0,1,1,'fkticon_a_001',false),
	 (16402,1,12,0,'fkticon_i_012',true),
	 (16402,2,23,0,'fkticon_i_023',false),
	 (16402,3,8,0,'fkticon_i_008',false),
	 (16402,4,18,0,'fkticon_i_018',false),
	 (16402,5,41,0,'fkticon_i_041',false),
	 (16402,6,42,0,'fkticon_i_042',false),
	 (16402,7,12,0,'fkticon_i_012',false),
	 (16402,8,141,0,'fkticon_i_141',false),
	 (16402,9,20,0,'fkticon_i_020',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16402,10,112,0,'fkticon_i_112',false),
	 (16402,11,134,0,'fkticon_i_134',true),
	 (16402,12,25,0,'fkticon_i_025',true),
	 (16402,13,29,0,'fkticon_i_029',false),
	 (16402,14,101,0,'fkticon_i_101',false),
	 (16402,15,129,0,'fkticon_i_129',false),
	 (16402,16,108,0,'fkticon_i_108',true),
	 (16402,17,37,0,'fkticon_i_037',false),
	 (16402,18,92,0,'fkticon_i_092',true),
	 (16402,19,242,0,'fkticon_i_242',false);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16402,20,89,0,'fkticon_i_089',false),
	 (16402,21,11,0,'fkticon_i_011',true),
	 (16402,22,6,0,'fkticon_i_006',false),
	 (16402,23,5,0,'fkticon_i_005',false),
	 (16402,24,28,0,'fkticon_i_028',false),
	 (16400,0,282,0,'fkticon_i_282',false),
	 (16400,1,23,0,'fkticon_i_023',false),
	 (16400,2,251,0,'fkticon_i_251',true),
	 (16400,3,37,0,'fkticon_i_037',false),
	 (16400,4,28,0,'fkticon_i_028',true);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (16400,5,2,1,'fkticon_a_002',false),
	 (16400,6,8,0,'fkticon_i_008',false),
	 (16400,7,11,0,'fkticon_i_011',true),
	 (16400,8,25,0,'fkticon_i_025',true),
	 (16400,9,125,0,'fkticon_i_125',false),
	 (16400,10,5,0,'fkticon_i_005',true),
	 (16400,11,48,0,'fkticon_i_048',false),
	 (16400,12,18,0,'fkticon_i_018',false),
	 (16400,13,10,0,'fkticon_i_010',true),
	 (16400,14,5,0,'fkticon_i_005',true);
INSERT INTO jcs.locomotive_functions (locomotive_id,f_number,f_type,f_value,f_icon,momentary) VALUES
	 (49166,0,1,1,'fkticon_a_001',false),
	 (49166,1,42,0,'fkticon_i_042',false),
	 (49166,2,41,0,'fkticon_i_041',false),
	 (49166,3,8,0,'fkticon_i_008',false),
	 (49166,4,18,0,'fkticon_i_018',false),
	 (49166,5,47,0,'fkticon_i_047',false),
	 (49166,6,56,0,'fkticon_i_056',false),
	 (49166,7,57,0,'fkticon_i_057',false);


commit;