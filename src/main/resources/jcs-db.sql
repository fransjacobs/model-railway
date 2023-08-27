drop table if exists accessories cascade;
drop table if exists locomotive_functions cascade;
drop table if exists locomotives cascade;
drop table if exists tiles cascade;
drop table if exists sensors cascade;
drop table if exists blocks cascade;
drop table if exists routes cascade;
drop table if exists route_elements cascade;
drop table if exists jcs_properties cascade;

create table accessories (
  id                 identity not null,
  address            integer not null,
  name               varchar(255) not null,
  type               varchar(255) not null,
  position           integer,
  states             integer,
  switch_time        integer,
  decoder_type       varchar(255),
  decoder            varchar(255),
  accessory_group    varchar(255),
  icon               varchar(255),
  icon_file          varchar(255),
  constraint acce_pk primary key (id),
  constraint acce_address_un unique (decoder_type,address)
);

create unique index acce_address_un_idx on accessories (address, decoder_type);
create unique index acce_pk_idx on accessories (id);

create table locomotives (
  id                 bigint not null,
  name               varchar(255) not null,
  uid                bigint,
  mfx_uid            bigint,
  address            integer not null,
  icon               varchar(255),
  decoder_type       varchar(255) not null,
  mfx_sid            varchar(255),
  tacho_max          integer,
  v_min              integer,
  velocity           integer,
  richtung           integer,
  commuter           bool not null default false,
  length             integer,
  show               bool not null default true,
  constraint loco_pk primary key (id),
  constraint loco_addr_dety_un unique (address,decoder_type)
);

create unique index loco_pk_idx on locomotives (id);
create unique index loco_addr_dety_un_idx on locomotives (address, decoder_type);

create table locomotive_functions (
  id                 identity not null,
  locomotive_id      bigint not null,
  f_number           integer not null,
  f_type             integer not null,
  f_value            integer,
  constraint lofu_pk primary key (id),
  constraint lofu_loid_fnum_un unique (locomotive_id,f_number)
);

create unique index lofu_pk_idx on locomotive_functions (id);
create unique index lofu_loid_fnum_un_idx on locomotive_functions (locomotive_id,f_number);

alter table locomotive_functions add constraint lofu_loco_fk foreign key (locomotive_id) references locomotives(id);

create table sensors (
  id                 identity not null,
  name               varchar(255) not null,
  device_id          integer,
  contact_id         integer,
  status             integer,
  previous_status    integer,
  millis             integer,
  last_updated       date,
  constraint sens_pk primary key (id),
  constraint sens_deid_coid_un unique (device_id,contact_id)
);

create unique index sens_pk_idx on sensors (id);
create unique index sens_deid_coid_un_idx on sensors (device_id,contact_id);

create table tiles (
  id                 varchar(255) not null,
  tile_type          varchar(255) not null,
  orientation        varchar(255) not null,
  direction          varchar(255) not null,
  x                  integer not null,
  y                  integer not null,
  signal_type        varchar(255),
  accessory_id       bigint,
  sensor_id          bigint,
  constraint tile_pk primary key (id),
  constraint tile_x_y_un unique (x,y)
);

create unique index tile_pk_idx on tiles (id);
create unique index tile_x_y_un_idx on tiles (x,y);

alter table tiles add constraint tile_acce_sens_arc_ck check (
  (accessory_id is not null and sensor_id is null) or (accessory_id is null and sensor_id is not null) or (accessory_id is null and sensor_id is null) 
);

alter table tiles add constraint tile_acc_fk foreign key (accessory_id) references accessories(id);
alter table tiles add constraint tile_sens_fk foreign key (sensor_id) references sensors(id);

create table blocks (
  id                 identity not null,
  tile_id            varchar(255) not null,
  description        varchar(255),
  plus_sensor_id     bigint,
  min_sensor_id      bigint,
  plus_signal_id     bigint,
  min_signal_id      bigint,
  locomotive_id      bigint,
  constraint bloc_pk primary key (id),
  constraint bloc_tile_un unique (tile_id)
);

create unique index bloc_pk_idx on blocks (id);
create unique index bloc_tile_idx on blocks (tile_id);

alter table blocks add constraint blck_tile_fk foreign key (tile_id) references tiles(id);

alter table blocks add constraint blck_sens_plus_fk foreign key (plus_sensor_id) references sensors(id);
alter table blocks add constraint blck_sens_min_fk foreign key (min_sensor_id) references sensors(id);
alter table blocks add constraint blck_acce_sig_plus_fk foreign key (plus_signal_id) references accessories(id);
alter table blocks add constraint blck_acce_sig_min_fk foreign key (min_signal_id) references accessories(id);
alter table blocks add constraint blck_loco_fk foreign key ( locomotive_id ) references locomotives (id);

create table routes (
  id                 varchar(255) not null,
  from_tile_id       varchar(255) not null,
  from_suffix        varchar(255) not null,
  to_tile_id         varchar(255) not null,
  to_suffix          varchar(255) not null,
  route_color        varchar(255),
  locked             bool not null default false,
  constraint rout_pk primary key (id),  
  constraint rout_from_to_un unique (from_tile_id,from_suffix,to_tile_id,to_suffix)
);

create unique index rout_pk_idx on routes (id);
create unique index rout_from_to_un_idx on routes (from_tile_id,from_suffix,to_tile_id,to_suffix);

alter table routes add constraint rout_tile_from_fk foreign key (from_tile_id) references tiles(id);
alter table routes add constraint rout_tile_to_fk foreign key (to_tile_id) references tiles(id);

create table route_elements (
  id                 identity not null,
  route_id           varchar(255) not null,
  node_id            varchar(255) not null,
  tile_id            varchar(255) not null,
  accessory_value    varchar(255),
  order_seq          integer not null default 0,
  constraint roel_pk primary key (id),
  constraint roel_rout_node_tile_un unique (route_id,node_id,tile_id)
);

create unique index roel_pk_idx on route_elements (id);
create unique index roel_rout_node_tile_un_idx on route_elements (route_id,node_id,tile_id);

alter table route_elements add constraint roel_rout_fk foreign key (route_id) references routes(id);
alter table route_elements add constraint roel_tile_fk foreign key (tile_id) references tiles(id);

create table jcs_properties (
  p_key    varchar(255) not null,
  p_value  varchar(255) not null,
  constraint prop_pk primary key ( p_key )
);

create unique index prop_pk_idx on jcs_properties (p_key);

insert into jcs_properties (p_key,p_value) values ('vendorController','jcs.controller.cs.MarklinCentralStationImpl');

commit;