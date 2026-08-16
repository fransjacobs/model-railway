create table stations (
  id         varchar(255) not null,
  name       varchar(255) not null,
  min_locs   integer not null default 0,
  loc_count integer not null default 0,
  use_fifo   bool not null default false,
  constraint stat_pk primary key (id)
);

drop table if exists station_blocks;
create table station_blocks (
  id            varchar(255) not null,
  station_id    varchar(255) not null,
  block_id      varchar(255) not null,
  last_updated  timestamp,
  constraint stbl_pk primary key (id)
);

create unique index stbl_stat_blck_un_idx on station_blocks (station_id,block_id);

alter table station_blocks add constraint stat_stbl_fk foreign key (station_id) references stations (id);

alter table station_blocks add constraint blck_stbl_fk foreign key (block_id) references blocks(id);

alter table routes add departure_signal_value varchar(255);

update tiles set tile_type = 'CrossSwitch' where tile_type = 'Cross';

insert into command_stations(id, description, short_name, class_name, connect_via, serial_port, ip_address, network_port, ip_auto_conf, supports_decoder_control, supports_accessory_control, supports_feedback, supports_loco_synch, supports_accessory_synch, supports_loco_image_synch, supports_loco_function_synch, protocols, default_cs, enabled, last_used_serial, sup_conn_types, feedback_module_id, feedback_bus_count, feedback_bus_0_module_count, feedback_bus_1_module_count, feedback_bus_2_module_count, feedback_bus_3_module_count, virtual)
values('intellibox2', 'Uhlenbrock Intellibox 2', 'Loconet', 'jcs.commandStation.loconet.Intellibox2Impl', 'SERIAL', null, 'AUTO', null, true, true, true, false, false, false, false, false, 'DCC,MM', false, true, '1', 'SERIAL', '0', 0, 0, 0, 0, 0, false);
commit;

update jcs_version set db_version = '0.0.4', app_version = '0.0.4';
commit;