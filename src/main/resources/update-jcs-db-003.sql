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




update jcs_version set db_version = '0.0.4', app_version = '0.0.4';
commit;