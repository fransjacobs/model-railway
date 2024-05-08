create table jcs_version (
  db_version varchar(255) not null,
  app_version varchar(255) not null
);

insert into jcs_version (db_version,app_version) values ('0.0.2','0.0.2');
commit;

--Use the new to be adde virtual command station as default
update command_stations set default_cs = false;

insert into command_stations(id, description, short_name, class_name, connect_via, serial_port, ip_address, network_port, ip_auto_conf, supports_decoder_control, supports_accessory_control, supports_feedback, supports_loco_synch, supports_accessory_synch, supports_loco_image_synch, supports_loco_function_synch, protocols, default_cs, enabled, last_used_serial, sup_conn_types, feedback_module_id, feedback_bus_count, feedback_bus_0_module_count, feedback_bus_1_module_count, feedback_bus_2_module_count, feedback_bus_3_module_count)
values('virtual', 'Virtual CS', 'VIR', 'jcs.commandStation.virtual.VirtualCommandStationImpl', 'NETWORK', NULL, '127.0.0.1', 0, false, true, true, true, false, false, false, false, 'dcc', true, true, '1', 'NETWORK', '0', 1, 1, 0, 0, 0);
commit;

alter table routes add status varchar(255);

alter table blocks add status varchar(255);
alter table blocks add incoming_side varchar(255);
alter table blocks rename column incoming_side to incoming_suffix;
commit;