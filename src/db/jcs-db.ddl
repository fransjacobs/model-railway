DROP TABLE if exists accessorytypes CASCADE CONSTRAINTS;
DROP TABLE if exists drivewaytypes CASCADE CONSTRAINTS;
DROP TABLE if exists driveways CASCADE CONSTRAINTS;
DROP TABLE if exists drivewayactivationlogs CASCADE CONSTRAINTS;
DROP TABLE if exists locomotives CASCADE CONSTRAINTS;
DROP TABLE if exists solenoidaccessories CASCADE CONSTRAINTS;
DROP TABLE if exists accessorysettings CASCADE CONSTRAINTS;
DROP TABLE if exists statustypes CASCADE CONSTRAINTS;
DROP TABLE if exists trackpower CASCADE CONSTRAINTS;
DROP TABLE if exists feedbacksource CASCADE CONSTRAINTS;
DROP TABLE if exists layouttiles CASCADE CONSTRAINTS;
DROP TABLE if exists layouttilegroups CASCADE CONSTRAINTS;
DROP TABLE if exists jcsproperties CASCADE CONSTRAINTS;
DROP TABLE if exists signalvalues CASCADE CONSTRAINTS;
DROP TABLE if exists sensors CASCADE CONSTRAINTS;

DROP SEQUENCE if exists drwa_seq;
DROP SEQUENCE if exists dwal_seq;
DROP SEQUENCE if exists loco_seq;
DROP SEQUENCE if exists soac_seq;
DROP SEQUENCE if exists acse_seq;
DROP SEQUENCE if exists trpo_seq;
DROP SEQUENCE if exists feso_seq;
DROP SEQUENCE if exists lati_seq;
DROP SEQUENCE if exists ltgr_seq;
DROP SEQUENCE if exists prop_seq;
DROP SEQUENCE if exists sens_seq;

CREATE SEQUENCE drwa_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE dwal_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE loco_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE soac_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE acse_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE trpo_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE feso_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE lati_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE ltgr_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE prop_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE sens_seq START WITH 1 INCREMENT BY 1;


CREATE TABLE trackpower (
  id             NUMBER NOT NULL,
  status         VARCHAR2(255 CHAR) NOT NULL,
  feedbacksource VARCHAR2(255 CHAR) NOT NULL,
  lastupdated    DATE
);

ALTER TABLE trackpower ADD CONSTRAINT trpo_pk PRIMARY KEY ( id );

CREATE TABLE accessorytypes (
  accessory_type   VARCHAR2(255 CHAR) NOT NULL,
  description      VARCHAR2(255)
);

ALTER TABLE accessorytypes ADD CONSTRAINT acty_pk PRIMARY KEY ( accessory_type );

create table drivewaytypes (
  driveway_type  VARCHAR2(255 CHAR) NOT NULL,
  description    VARCHAR2(255)
);

ALTER TABLE drivewaytypes ADD CONSTRAINT drty_pk PRIMARY KEY ( driveway_type );


CREATE TABLE driveways (
  id             NUMBER NOT NULL,
  address        INTEGER NOT NULL,
  femo_id        NUMBER,
  port           INTEGER,
  driveway_type  VARCHAR2(255 CHAR) NOT NULL, 
  name           VARCHAR2(255 CHAR) NOT NULL,
  description    VARCHAR2(255 CHAR)
);

ALTER TABLE driveways ADD CONSTRAINT drwa_pk PRIMARY KEY ( id );

ALTER TABLE driveways ADD CONSTRAINT drwa_femo_port_un UNIQUE ( femo_id, port );

ALTER TABLE driveways ADD CONSTRAINT drwa_address_un UNIQUE ( address );


CREATE TABLE drivewayactivationlogs (
  id                      NUMBER NOT NULL,
  drwa_id                 NUMBER NOT NULL,
  triggerdatetime         DATE NOT NULL,
  triggerport             INTEGER,
  triggerfeedbackmodule   NUMBER
);

ALTER TABLE drivewayactivationlogs ADD CONSTRAINT dral_pk PRIMARY KEY ( id );

CREATE TABLE sensors (
  id              NUMBER NOT NULL,
  address         INTEGER NOT NULL,
  device_id       INTEGER,
  NAME            VARCHAR2(255 CHAR) NOT NULL,
  DESCRIPTION     VARCHAR2(255 CHAR),
  value           INTEGER,
  previous_value  INTEGER,
  millis          INTEGER,
  lastupdated     DATE
);

ALTER TABLE sensors ADD CONSTRAINT sens_pk PRIMARY KEY ( id );

ALTER TABLE sensors ADD CONSTRAINT sens_address_un UNIQUE ( address );


CREATE TABLE locomotives (
  id                 NUMBER NOT NULL,
  address            INTEGER NOT NULL,
  name               VARCHAR2(255 CHAR) NOT NULL,
  description        VARCHAR2(255 CHAR),
  catalognumber      VARCHAR2(255 CHAR),
  decodertype        VARCHAR2(255 CHAR),
  direction          VARCHAR2(255) NOT NULL,
  speed              INTEGER,
  tachomax           INTEGER,
  speedsteps         INTEGER NOT NULL,
  vmin               INTEGER,
  vmax               INTEGER,
  functioncount      INTEGER NOT NULL,
  functionvalues     VARCHAR2(255),
  functiontypes      VARCHAR2(255),
  default_direction  VARCHAR2(255) NOT NULL,
  iconname           VARCHAR2(255 CHAR)
);

ALTER TABLE locomotives ADD CONSTRAINT loco_pk PRIMARY KEY ( id );

ALTER TABLE locomotives ADD CONSTRAINT loco_address_un UNIQUE ( address, decodertype );


CREATE TABLE solenoidaccessories (
  id                    NUMBER NOT NULL,
  address               INTEGER NOT NULL,
  name                  VARCHAR2(255 CHAR) NOT NULL,
  description           VARCHAR2(255 CHAR),
  catalog_number        VARCHAR2(255 CHAR),
  accessory_type        VARCHAR2(255 CHAR) NOT NULL,
  current_status_type   VARCHAR2(255 CHAR) NOT NULL,
  soac_id               NUMBER,
  light_images          INTEGER NOT NULL DEFAULT 2,
  signal_value          VARCHAR2(255),
  switch_time           INTEGER
);

ALTER TABLE solenoidaccessories ADD CONSTRAINT soac_pk PRIMARY KEY ( id );

ALTER TABLE solenoidaccessories ADD CONSTRAINT soac_address_un UNIQUE ( address );

ALTER TABLE solenoidaccessories
  ADD CONSTRAINT soac_soac_fk FOREIGN KEY ( soac_id )
    REFERENCES solenoidaccessories ( id )
  NOT DEFERRABLE;

CREATE TABLE accessorysettings (
  id                    NUMBER NOT NULL,
  drwa_id               NUMBER NOT NULL,
  accessory_type        VARCHAR2(255 CHAR) NOT NULL,
  soac_id               NUMBER NOT NULL,
  default_status_type   VARCHAR2(255 CHAR),
  default_signal_value  VARCHAR2(255 CHAR),
  femo_id               NUMBER,
  port                  INTEGER,
  port_value            NUMBER 
);

ALTER TABLE accessorysettings ADD CONSTRAINT soas_pk PRIMARY KEY ( id );

ALTER TABLE accessorysettings ADD CONSTRAINT soas_un UNIQUE ( drwa_id, soac_id );

ALTER TABLE accessorysettings ADD CONSTRAINT stty_or_siva_nn check ( (default_status_type is not null and default_signal_value is null) or (default_status_type is null and default_signal_value is not null) );


CREATE TABLE statustypes (
  status_type   VARCHAR2(255 CHAR) NOT NULL,
  description   VARCHAR2(255 CHAR)
);

ALTER TABLE statustypes ADD CONSTRAINT stty_pk PRIMARY KEY ( status_type );

CREATE TABLE signalvalues (
  signal_value    VARCHAR2(255 CHAR) NOT NULL,
  description   VARCHAR2(255 CHAR)
);

ALTER TABLE signalvalues ADD CONSTRAINT siva_pk PRIMARY KEY ( signal_value );

CREATE TABLE jcsproperties (
  id                    NUMBER NOT NULL,
  key        VARCHAR2(255 CHAR) NOT NULL,
  value      VARCHAR2(255 CHAR)
);

ALTER TABLE jcsproperties ADD CONSTRAINT prop_pk PRIMARY KEY ( id );

ALTER TABLE jcsproperties ADD CONSTRAINT prop_key_un UNIQUE ( key );

-- FK's
ALTER TABLE driveways
  ADD CONSTRAINT drwa_femo_fk FOREIGN KEY ( femo_id )
    REFERENCES feedbackmodules ( id )
  NOT DEFERRABLE;

ALTER TABLE driveways
  ADD CONSTRAINT drwa_drty_fk FOREIGN KEY ( driveway_type )
    REFERENCES drivewaytypes ( driveway_type )
  NOT DEFERRABLE;

ALTER TABLE drivewayactivationlogs
  ADD CONSTRAINT dral_drwa_fk FOREIGN KEY ( drwa_id )
    REFERENCES driveways ( id )
  NOT DEFERRABLE;

ALTER TABLE solenoidaccessories
  ADD CONSTRAINT soac_acty_fk FOREIGN KEY ( accessory_type )
    REFERENCES accessorytypes ( accessory_type )
  NOT DEFERRABLE;

ALTER TABLE solenoidaccessories
  ADD CONSTRAINT soac_stty_fk FOREIGN KEY ( current_status_type )
    REFERENCES statustypes ( status_type )
  NOT DEFERRABLE;

ALTER TABLE solenoidaccessories
  ADD CONSTRAINT soac_siva_fk FOREIGN KEY ( signal_value )
    REFERENCES signalvalues ( signal_value )
  NOT DEFERRABLE;

ALTER TABLE accessorysettings
  ADD CONSTRAINT acse_drwa_fk FOREIGN KEY ( drwa_id )
    REFERENCES driveways ( id )
  NOT DEFERRABLE;

ALTER TABLE accessorysettings
  ADD CONSTRAINT acse_soac_fk FOREIGN KEY ( soac_id )
    REFERENCES solenoidaccessories ( id )
  NOT DEFERRABLE;

ALTER TABLE accessorysettings
  ADD CONSTRAINT acse_stty_fk FOREIGN KEY ( default_status_type )
    REFERENCES statustypes ( status_type )
  NOT DEFERRABLE;

ALTER TABLE accessorysettings
  ADD CONSTRAINT acse_femo_fk FOREIGN KEY ( femo_id )
    REFERENCES feedbackmodules ( id )
  NOT DEFERRABLE;

ALTER TABLE accessorysettings
  ADD CONSTRAINT acse_siva_fk FOREIGN KEY ( default_signal_value )
    REFERENCES signalvalues ( signal_value )
  NOT DEFERRABLE;

ALTER TABLE accessorysettings
  ADD CONSTRAINT acse_acty_fk FOREIGN KEY ( accessory_type )
    REFERENCES accessorytypes ( accessory_type )
  NOT DEFERRABLE;


-- Layout
CREATE TABLE layouttiles (
  id                    NUMBER NOT NULL,
  tiletype              VARCHAR2(255 CHAR) NOT NULL,
  orientation           VARCHAR2(255 CHAR) NOT NULL,
  rotation              VARCHAR2(255 CHAR) NOT NULL,
  direction             VARCHAR2(255 CHAR) NOT NULL,
  x                     INTEGER NOT NULL,
  y                     INTEGER NOT NULL,
  soac_id               NUMBER NULL,
  sens_id               NUMBER NULL,
  ltgr_id               NUMBER NULL,
  from_lati_id          NUMBER NULL,
  to_lati_id            NUMBER NULL
);

ALTER TABLE layouttiles ADD CONSTRAINT lati_pk PRIMARY KEY ( id );

ALTER TABLE layouttiles ADD CONSTRAINT lati_x_y_un UNIQUE ( x,y );

ALTER TABLE layouttiles
  ADD CONSTRAINT lati_soac_fk FOREIGN KEY ( soac_id )
    REFERENCES solenoidaccessories ( id )
  NOT DEFERRABLE;

ALTER TABLE layouttiles
  ADD CONSTRAINT lati_sens_fk FOREIGN KEY ( sens_id )
    REFERENCES sensors ( id )
  NOT DEFERRABLE;  

ALTER TABLE layouttiles
  ADD CONSTRAINT from_lati_fk FOREIGN KEY ( from_lati_id )
    REFERENCES layouttiles ( id )
  NOT DEFERRABLE;

ALTER TABLE layouttiles
  ADD CONSTRAINT to_lati_fk FOREIGN KEY ( to_lati_id )
    REFERENCES layouttiles ( id )
  NOT DEFERRABLE;



CREATE TABLE layouttilegroups (
  id                  NUMBER NOT NULL,
  name                VARCHAR2(255 CHAR),
  start_lati_id       NUMBER,
  end_lati_id         NUMBER,
  color               VARCHAR2(255 CHAR),
  direction           VARCHAR2(255 CHAR),
  groupnumber         INTEGER not NULL
);

ALTER TABLE layouttilegroups ADD CONSTRAINT ltgr_pk PRIMARY KEY ( id );

ALTER TABLE layouttilegroups ADD CONSTRAINT ltgr_groupnumber_un UNIQUE ( groupnumber );

ALTER TABLE layouttiles
  ADD CONSTRAINT lati_ltgr_fk FOREIGN KEY ( ltgr_id )
    REFERENCES layouttilegroups ( id )
  NOT DEFERRABLE;

ALTER TABLE layouttilegroups
  ADD CONSTRAINT ltgr_lati_start_fk FOREIGN KEY ( start_lati_id )
    REFERENCES layouttiles ( id )
  NOT DEFERRABLE;

ALTER TABLE layouttilegroups
  ADD CONSTRAINT ltgr_lati_ends_fk FOREIGN KEY ( end_lati_id )
    REFERENCES layouttiles ( id )
  NOT DEFERRABLE;

-- Trackpower
INSERT INTO TRACKPOWER(ID,STATUS,FEEDBACKSOURCE,LASTUPDATED)
VALUES(trpo_seq.nextval,'OFF','OTHER',null);

-- Insert the Solenoid Types
INSERT INTO accessorytypes (accessory_type,description) values ('S','Signal');     
INSERT INTO accessorytypes (accessory_type,description) values ('T','Turnout');     
INSERT INTO accessorytypes (accessory_type,description) values ('G','General');     

-- Insert the Statuses
INSERT INTO statustypes (status_type,description) values('G','Green'); 
INSERT INTO statustypes (status_type,description) values('R','Red'); 
INSERT INTO statustypes (status_type,description) values('O','Off'); 

-- Insert the status types
INSERT INTO SIGNALVALUES (SIGNAL_VALUE,DESCRIPTION) VALUES ('Hp0','Hp0');
INSERT INTO SIGNALVALUES (SIGNAL_VALUE,DESCRIPTION) VALUES ('Hp1','Hp1');
INSERT INTO SIGNALVALUES (SIGNAL_VALUE,DESCRIPTION) VALUES ('Hp2','Hp2');
INSERT INTO SIGNALVALUES (SIGNAL_VALUE,DESCRIPTION) VALUES ('Hp0Sh1','Hp0Sh1');
INSERT INTO SIGNALVALUES (SIGNAL_VALUE,DESCRIPTION) VALUES ('OFF','OFF');

-- Insert the Driveway types
INSERT INTO drivewaytypes (driveway_type,description) values('Manual','Manual'); 
INSERT INTO drivewaytypes (driveway_type,description) values('FeedbackPort','Feedback port'); 
INSERT INTO drivewaytypes (driveway_type,description) values('Timed','Timed'); 


-- Insert the Locomotives
INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,1,'V200 027','V200','3021','mm2_prg','Forwards',0,null,14,0,null,5,'10000',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,2,'BR81 002','BR 81 002','30321','mm2_dil8','Forwards',0,null,14,0,null,1,'1',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,3,'BR 1022','BR1022','3795.10','mm2_prg','Forwards',0,null,14,0,null,5,'101-0',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,6,'BR 44 690','BR 44 690','3047','mm2_prg','Forwards',0,null,14,0,null,5,'10000',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,8,'NS 6502','DHG 700C NS 6502','29159.1','mm2_dil8','Forwards',0,null,14,0,null,1,'1',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,11,'NS 1205','NS 1205','3055','mm2_prg','Forwards',0,null,14,0,null,5,'100000',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,12,'BR141 015','E 141 015-8','3034.10','mm2_prg','Forwards',0,null,14,0,null,5,'100000',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,14,'V36','V36/BR236','3142','mm2_prg','Forwards',0,null,14,0,null,5,'100000',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,17,'1855','NS 1855','37263','mm2_prg','Forwards',0,null,14,0,null,5,'10000',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,6,'NS E186','NS E186','36629','mfx','Forwards',0,null,14,0,null,16,'1000000000000000',null,'Forwards',null);
--VALUES (loco_seq.nextval,24,'NS E186','NS E186','36629','mfx','Forwards',0,null,14,0,null,16,'1000000000000000',null,'Forwards',null); -- MM2 only

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,25,'ER 20','Hercules Police','36793','mm2_prg','Forwards',0,null,14,0,null,5,'100000',null,'Forwards',null);

INSERT INTO LOCOMOTIVES (ID,ADDRESS,NAME,DESCRIPTION,CATALOGNUMBER,DECODERTYPE,DIRECTION,SPEED,TACHOMAX,SPEEDSTEPS,VMIN,VMAX,FUNCTIONCOUNT,FUNCTIONVALUES,FUNCTIONTYPES,DEFAULT_DIRECTION,ICONNAME)
VALUES (loco_seq.nextval,5,'BR 152','BR 152','39850','mfx','Forwards',0,null,14,0,null,16,'1000000000000000',null,'Forwards',null);
--VALUES (loco_seq.nextval,52,'BR 152','BR 152','39850','mfx','Forwards',0,null,14,0,null,16,'1000000000000000',null,'Forwards',null); -- MM2 only

-- Crane...
--INSERT INTO LOCOMOTIVES (ID,ADDRESS,TYPE,NAME,DESCRIPTION,CATALOGNUMBER,SPECIALFUNCTIONS,DEFAULT_DIRECTION,DIRECTION,SPEED,THROTTLE,MINSPEED,SPEEDSTEPS,LOCOTYPE,F0,F1,F2,F3,F4,F0TYPE,F1TYPE,F2TYPE,F3TYPE,F4TYPE)
--VALUES (loco_seq.nextval,30,'C','7051','Marklin 7051 with 7651','7051',1,'F','N',0,0,10,14,'crane',0,0,0,0,0,'magnet','turn','lift',null,null);


-- Accessories
INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,1,'W 1','R','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,2,'W 2','R','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,3,'W 3','R','','T','R',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,4,'W 4','R','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,5,'W 5','R','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,6,'W 6','R','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,7,'W 7','L','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,8,'W 8','L','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,9,'W 9','L','','T','R',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,10,'W 10','L','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,11,'W 11','L','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,12,'W 12','L','','T','G',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,13,'W 13','L','','T','R',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,14,'W 14','L','','T','R',null,2,null);

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,16,'S 16','Midget','','S','G',null,2,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,21,'S 21','Midget','','S','G',null,2,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,22,'S 22','Midget','','S','G',null,2,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,23,'S 23','Midget','','S','G',null,2,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,24,'S 24','Midget','','S','G',null,2,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,25,'S 25/26','Leave','viessmann','S','G',null,4,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,26,'S 25/26','Leave','viessmann','S','R', (soac_seq.currval -1),4,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,27,'S 27/28','Leave','viessmann','S','G',null,4,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,28,'S 27/28','Leave','viessmann','S','G',(soac_seq.currval -1),4,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,29,'S 29/30','Leave','viessmann','S','G',null,4,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,30,'S 29/30','Leave','viessmann','S','R',(soac_seq.currval -1),4,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,31,'S 31/32','Leave','viessmann','S','G',null,4,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,32,'S 31/32','Leave','viessmann','S','G',(soac_seq.currval -1),4,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,33,'S 33','Entry','','S','G',null,2,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,34,'S 34','Entry','','S','G',null,2,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,35,'S 35','Entry','','S','G',null,2,'Hp1');

INSERT INTO SOLENOIDACCESSORIES (ID,ADDRESS,NAME,DESCRIPTION,CATALOG_NUMBER,ACCESSORY_TYPE,CURRENT_STATUS_TYPE,SOAC_ID,LIGHT_IMAGES,SIGNAL_VALUE)
 VALUES (soac_seq.nextval,36,'S 36','Entry','','S','G',null,2,'Hp1');

-- Insert the Feedback modules
INSERT INTO feedbackmodules (id,address,name,description,catalognumber,ports,msb,lsb,lastupdated,port1,port2,port3,port4,port5,port6,port7,port8,port9,port10,port11,port12,port13,port14,port15,port16)
VALUES (femo_seq.nextval,1,'S88_1','S88 1-16','S88',16,0,0,null,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);

INSERT INTO feedbackmodules (id,address,name,description,catalognumber,ports,msb,lsb,lastupdated,port1,port2,port3,port4,port5,port6,port7,port8,port9,port10,port11,port12,port13,port14,port15,port16)
VALUES (femo_seq.nextval,2,'S88_2','S88 17-32','S88',16,0,0,null,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);

INSERT INTO feedbackmodules (id,address,name,description,catalognumber,ports,msb,lsb,lastupdated,port1,port2,port3,port4,port5,port6,port7,port8,port9,port10,port11,port12,port13,port14,port15,port16)
VALUES (femo_seq.nextval,3,'S88_3','S88 33-48','S88',16,0,0,null,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);

-- Properties
INSERT INTO jcsproperties (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'S88-module-count','3');

INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'S88-demo','lan.wervel.jcs.feedback.DemoFeedbackService');
INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'S88-remote','FeedbackService');
INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'S88-CS2','lan.wervel.jcs.feedback.cs2.CS2FeedbackService');
INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'activeFeedbackService','S88-CS2');

INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'M6050-local','lan.wervel.jcs.controller.m6050.M6050Controller');
INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'M6050-remote','ControllerService');
INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'M6050-demo','lan.wervel.jcs.controller.m6050.M6050DemoController');
INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'CS2','lan.wervel.jcs.controller.cs2.CS2Controller');
INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'activeControllerService','CS2');


--INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'remoteControllerService','ControllerService');
--INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'useRemoteControllerService','false');
--INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'demoControllerServiceImpl','lan.wervel.jcs.controller.m6050.M6050DemoController');
--INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'serialControllerServiceImpl','lan.wervel.jcs.controller.m6050.M6050Controller');
--INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'cs2ControllerServiceImpl','lan.wervel.jcs.controller.cs2.CS2Controller');
--INSERT INTO JCSPROPERTIES (ID,KEY,VALUE) VALUES (prop_seq.nextval, 'activeControllerService','cs2ControllerServiceImpl');

--
--INSERT INTO driveways (id,address,name,description,port,type,femo_id) values(drwa_seq.nextval,1,'INIT','Initial track settings',-1,'init',femo_seq.currval);

commit;

--Layout tiles layaout of "Zolderhoek"
INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R90','CENTER',40,280,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R90','CENTER',40,320,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R90','CENTER',40,360,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R90','CENTER',40,400,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R270','CENTER',60,120,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R180','CENTER',60,440,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',100,100,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',100,460,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R270','CENTER',140,240,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R180','CENTER',140,360,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',180,20,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',180,100,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',180,380,0,0,null,(select id from feedbackmodules where address = 3),9,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID)
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',220,20,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.TurnoutTile','R0','LEFT',240,80,0,0,(select id from solenoidaccessories where address = 9),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',260,20,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R0','CENTER',260,220,0,0,(select id from solenoidaccessories where address = 33),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R0','CENTER',260,460,0,0,(select id from solenoidaccessories where address = 35),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',300,20,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',300,60,0,0,null,(select id from feedbackmodules where address = 2),16,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',300,100,0,0,null,(select id from feedbackmodules where address = 2),14,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',300,220,0,0,null,(select id from feedbackmodules where address = 3),4,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R180','CENTER',300,460,0,0,null,(select id from feedbackmodules where address = 1),10,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R180','CENTER',340,60,0,0,(select id from solenoidaccessories where address = 31),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R180','CENTER',340,100,0,0,(select id from solenoidaccessories where address = 29),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',340,420,0,0,null,(select id from feedbackmodules where address = 1),15,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.TurnoutTile','R0','LEFT',360,200,0,0,(select id from solenoidaccessories where address = 10),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.TurnoutTile','R0','RIGHT',360,480,0,0,(select id from solenoidaccessories where address = 5),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R180','CENTER',380,380,0,0,(select id from solenoidaccessories where address = 22),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.OccupancyDetectorTile','R0','CENTER',400,60,0,0,null,(select id from feedbackmodules where address = 2),15,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.OccupancyDetectorTile','R0','CENTER',400,100,0,0,null,(select id from feedbackmodules where address = 2),13,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R90','CENTER',420,160,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',420,220,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',420,460,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',420,500,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.OccupancyDetectorTile','R0','CENTER',440,20,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.OccupancyDetectorTile','R0','CENTER',440,420,0,0,null,(select id from feedbackmodules where address = 1),13,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',460,100,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R180','CENTER',460,460,0,0,null,(select id from feedbackmodules where address = 1),9,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R0','CENTER',500,20,0,0,(select id from solenoidaccessories where address = 16),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',500,60,0,0,null,(select id from feedbackmodules where address = 2),8,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',500,100,0,0,null,(select id from feedbackmodules where address = 2),6,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',500,140,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',500,380,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',500,420,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',500,460,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',500,500,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',540,20,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',540,60,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R180','CENTER',540,180,0,0,null,(select id from feedbackmodules where address = 2),1,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R0','CENTER',540,240,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',540,380,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',540,420,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',540,500,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',580,20,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',580,60,0,0,null,(select id from feedbackmodules where address = 2),7,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',580,100,0,0,null,(select id from feedbackmodules where address = 2),5,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',580,180,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',580,380,0,0,null,(select id from feedbackmodules where address = 1),7,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.OccupancyDetectorTile','R0','CENTER',600,460,0,0,null,(select id from feedbackmodules where address = 1),4,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',620,100,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R180','CENTER',620,260,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',620,380,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',620,420,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.TurnoutTile','R180','LEFT',640,40,0,0,(select id from solenoidaccessories where address = 13),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',660,100,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',660,140,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',660,180,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R0','CENTER',660,460,0,0,(select id from solenoidaccessories where address = 23),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R0','CENTER',660,500,0,0,(select id from solenoidaccessories where address = 21),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.OccupancyDetectorTile','R0','CENTER',680,260,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R90','CENTER',700,80,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.OccupancyDetectorTile','R0','CENTER',720,180,0,0,null,(select id from feedbackmodules where address = 2),9,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',740,260,null,null,null,null,null,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.TurnoutTile','R180','LEFT',760,40,0,0,(select id from solenoidaccessories where address = 11),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.TurnoutTile','R180','RIGHT',760,480,0,0,(select id from solenoidaccessories where address = 3),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R0','CENTER',780,140,0,0,(select id from solenoidaccessories where address = 27),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R0','CENTER',780,180,0,0,(select id from solenoidaccessories where address = 25),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',780,260,null,null,null,null,null,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.SignalTile','R180','CENTER',780,420,0,0,(select id from solenoidaccessories where address = 36),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R0','CENTER',820,20,0,0,null,(select id from feedbackmodules where address = 3),5,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R180','CENTER',820,140,0,0,null,(select id from feedbackmodules where address = 2),12,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R180','CENTER',820,180,0,0,null,(select id from feedbackmodules where address = 2),10,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',820,260,null,null,null,null,null,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.OccupancyDetectorTile','R0','CENTER',840,420,0,0,null,(select id from feedbackmodules where address = 1),2,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',860,500,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.TurnoutTile','R180','LEFT',880,160,0,0,(select id from solenoidaccessories where address = 12),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',900,420,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',900,500,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.OccupancyDetectorTile','R0','CENTER',920,20,0,0,null,(select id from feedbackmodules where address = 3),6,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R0','CENTER',940,160,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R90','CENTER',940,400,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.FeedbackPort','R90','CENTER',960,200,0,0,null,(select id from feedbackmodules where address = 1),1,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R90','CENTER',960,360,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.TurnoutTile','R270','RIGHT',980,260,0,0,(select id from solenoidaccessories where address = 2),null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R0','CENTER',980,500,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.DiagonalTrack','R90','CENTER',1020,480,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R90','CENTER',1040,240,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R90','CENTER',1040,360,0,0,null,null,0,null);

INSERT INTO LAYOUTTILES (ID,TILETYPE,ROTATION,DIRECTION,X,Y,OFFSETX,OFFSETY,SOAC_ID,FEMO_ID,PORT,LTGR_ID) 
VALUES (lati_seq.nextval,'lan.wervel.jcs.ui.layout.tiles.StraightTrack','R90','CENTER',1040,400,0,0,null,null,0,null);

Commit;