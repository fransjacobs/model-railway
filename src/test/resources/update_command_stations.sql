
ALTER TABLE command_stations ADD loco_synch bool not null default false;
ALTER TABLE command_stations ADD loco_func_synch bool not null default false;
ALTER TABLE command_stations ADD loco_img_synch bool not null default false;
ALTER TABLE command_stations ADD acce_synch bool not null default false;
ALTER TABLE command_stations ADD short_name varchar(255);

ALTER TABLE command_stations ADD last_used_serial varchar(255);


update command_stations set short_name = case name when 'Marklin Central Station 2/3' then 'Central-Station' when 'DCC-EX Serial' then 'dcc-ex' when 'DCC-EX Network' then 'dcc-ex' when 'Marklin 6051' then '6051' else 'images' end;
commit;

ALTER TABLE command_stations alter column short_name varchar(255) not null;




INSERT INTO jcs.command_stations (id,name,class_name,default_cs,connection_type,serial_port,ip_address,network_port,auto_conf,show,protocols,loco_synch,loco_func_synch,loco_img_synch,acce_synch,short_name) VALUES
	 ('cs.Marklin.CentralStation','Marklin Central Station 2/3','jcs.commandStation.marklin.cs.MarklinCentralStationImpl',true,'NETWORK',NULL,NULL,15731,true,true,'''DCC,MFX,MM''',true,true,true,true,'central-station'),
	 ('cs.DccEX.serial','DCC-EX Serial','jcs.commandStation.dccex.DccExCommandStationImpl',false,'SERIAL',NULL,NULL,0,false,true,'DCC',false,false,false,false,'dcc-ex'),
	 ('cs.DccEX.network','DCC-EX Network','jcs.commandStation.dccex.DccExCommandStationImpl',false,'NETWORK',NULL,'192.168.178.73',2560,false,true,'DCC',false,false,false,false,'dcc-ex'),
	 ('cs.Marklin.6051','Marklin 6051','jcs.commandStation.marklin.m6051.M6051Impl',false,'SERIAL',NULL,NULL,NULL,false,false,'MM',false,false,false,false,'6051');
