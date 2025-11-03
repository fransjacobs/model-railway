alter table blocks add logical_direction varchar(255);

alter table command_stations add virtual bool not null default false;

update command_stations set virtual = true where id = 'virtual';
commit;

insert into command_stations(id, description, short_name, class_name, connect_via, serial_port, ip_address, network_port, ip_auto_conf, supports_decoder_control, supports_accessory_control, supports_feedback, supports_loco_synch, supports_accessory_synch, supports_loco_image_synch, supports_loco_function_synch, protocols, default_cs, enabled, last_used_serial, sup_conn_types, feedback_module_id, feedback_bus_count, feedback_bus_0_module_count, feedback_bus_1_module_count, feedback_bus_2_module_count, feedback_bus_3_module_count, virtual)
values('esu-ecos', 'ESU ECoS', 'ECoS', 'jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl', 'NETWORK', null, null, 15471, true, true, true, true, true, true, true, true, 'DCC,MFX,MM', true, true, '1', 'NETWORK', '0', 0, 0, 0, 0, 0, false);
commit;

update blocks set plus_sensor_id = null, min_sensor_id = null;
update tiles set sensor_id = null;

delete from sensors;
commit;

alter table sensors alter id integer;
alter table sensors add node_id integer;
alter table sensors add bus_nr integer not null default 0;

alter table sensors add command_station_id varchar(255) not null;
alter table sensors drop constraint sens_deid_coid_un;

drop index sens_deid_coid_un_idx;

alter table sensors add constraint sens_deid_coid_un unique (device_id,contact_id,bus_nr,command_station_id);

alter table tiles alter sensor_id integer;

alter table blocks alter plus_sensor_id integer;
alter table blocks alter min_sensor_id integer;
alter table blocks alter column reverse_arrival_side rename to allow_non_commuter_only;
alter table blocks add allow_direction_change bool not null default true;

alter table locomotives drop constraint loco_addr_dety_un;
drop index loco_addr_dety_un_idx;
alter table locomotives add speed_1 integer;
alter table locomotives add speed_2 integer;
alter table locomotives add speed_3 integer;
alter table locomotives add speed_4 integer;

alter table accessories add address2 integer;

update jcs_version set db_version = '0.0.3', app_version = '0.0.3';
commit;