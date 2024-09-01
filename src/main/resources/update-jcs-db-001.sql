create table jcs_version (
  db_version varchar(255) not null,
  app_version varchar(255) not null
);

commit;

--Use the new to be adde virtual command station as default
update command_stations set default_cs = false;

insert into command_stations(id, description, short_name, class_name, connect_via, serial_port, ip_address, network_port, ip_auto_conf, supports_decoder_control, supports_accessory_control, supports_feedback, supports_loco_synch, supports_accessory_synch, supports_loco_image_synch, supports_loco_function_synch, protocols, default_cs, enabled, last_used_serial, sup_conn_types, feedback_module_id, feedback_bus_count, feedback_bus_0_module_count, feedback_bus_1_module_count, feedback_bus_2_module_count, feedback_bus_3_module_count)
values('virtual', 'Virtual CS', 'VIR', 'jcs.commandStation.virtual.VirtualCommandStationImpl', 'NETWORK', NULL, '127.0.0.1', 0, false, true, true, true, false, false, false, false, 'dcc', true, true, '1', 'NETWORK', '0', 1, 1, 0, 0, 0);
commit;

alter table routes add status varchar(255);

alter table blocks add status varchar(255);
alter table blocks add incoming_suffix varchar(255);
alter table blocks add min_wait_time integer not null default 10;
alter table blocks add max_wait_time integer null;
alter table blocks add random_wait bool not null default false;
alter table blocks add always_stop bool not null default false;
alter table blocks add allow_commuter_only bool not null default false;

alter table locomotives add dispatcher_direction varchar(255);
alter table locomotives add locomotive_direction varchar(255);

update locomotives set locomotive_direction = case when richtung = 2 then 'BACKWARDS' else 'FORWARDS' end;  
commit;

alter table locomotives drop richtung;


insert into jcs_version (db_version,app_version) values ('0.0.2','0.0.2');
commit;