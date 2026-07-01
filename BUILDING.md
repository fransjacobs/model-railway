# Building JCS

[README](README.md)

## Requirements
JCS is mainly developed using the [Netbeans IDE](https://netbeans.apache.org)
 
I use the [Temurin JDK 25](https://adoptium.net/en-GB/temurin)

The program is build using [Maven](https://maven.apache.org) 

## Building 

Before building you have to obtain the source. The best and recommended way is
to clone the repository from [github](https://github.com/fransjacobs/model-railway) .
You can also dowload the source from githu using the  

Create a folder where you clone the repository.
Create a folder in your home folder called jcs (~/jcs).

You can also get the source by using the [download zip]( https://github.com/fransjacobs/model-railway/archive/refs/heads/master.zip) option.

### Build tool

JCS is buid using a build tool called [maven](https://maven.apache.org/download.cgi)
Maven will automatically download all project dependencies.

### IDE

JCS development is maily done with [Netbeans](https://netbeans.apache.org/front/main/index.html) 
In Netbeans open the project en perform a build

### Commandline

Make sure you paths are right.
- Windows use the set command to view
- Linux or MACOS you can use env
Make sure you have set [mvn command](https://maven.apache.org/install.html) in your path.
Make sure you have set the [JAVA_HOME](https://adoptium.net/en-GB/installation/archives) variable.

#### Check the jdk version

execute java -version

This should return something like:

C:\Users\frans>java -version
openjdk version "25.0.2" 2026-01-20 LTS
OpenJDK Runtime Environment Temurin-25.0.2+10 (build 25.0.2+10-LTS)
OpenJDK 64-Bit Server VM Temurin-25.0.2+10 (build 25.0.2+10-LTS, mixed mode, sharing)

#### Check maven

This should return something like:
C:\Users\frans>mvn -version
C:\Users\frans>mvn -version
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: C:\ProgramFiles\apache-maven-3.9.16
Java version: 25.0.2, vendor: Eclipse Adoptium, runtime: C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
Default locale: en_US, platform encoding: UTF-8
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
 
## Building

Cd in to the directory where you have stored the JCS source code
The root of this directory must contain the file pom.xml

run mvn package -P package-app
(For example on windows: C:\Users\frans\tmp\model-railway-master>mvn package -P package-app)

After a while you should see: [INFO] BUILD SUCCESS
(You can ignoe the warnings or errors.

In the folder target is no a folder jcs with an executable (for windows jcs.exe)

You can run this to start JCS.
 

## Running

The configuration and layout data is stored in an embedded (H2) database.
The database files are placed in the $HOME/jcs directory.
When the database does not exist it is created on startup. 

Run the jcs.JCS class an database should be created on first run.

## Debugging

JCS has some extra properties which can control the program.
These properties have to be stored in a properties file in the user.home/jcs
folder and is called jcs.properties.

The following properties are supported

Retrieval of the locomotives either via CAN (default) or http works on both CS-2 and 3
locomotive.list.via=can / http default is can

Accessories retrieval via CAN, http or JSON. the ltter only works on a CS-3
accessory.list.via=can / http / JSON

Debug of the zlib inflator. When files are retrive via CAN this controls
the debugging of the inflation. When true the raw and decompressed files are
written to the file system  
inflate.debug=true (default false)

## Debugging data

JCS uses an embedded [H2](https://h2database.com/html/main.html) database.
On de first startup, when ther are no files in the jcs home directory (~/jcs)
database is created. The database is created as user SA (pass jcs) this user
creates the jcs schema and user( user jcs pass repo);
Inside the jcs schema the tables are created.

## Datamodel

![UI screenshot: JCS Datamodel](assets/jcs_datamodel.png?raw=true) 

### Connect to the database

It is possble to connect to the embedded JCS database using an SQL tool like
[Squirrel SQL](http://www.squirrelsql.org/) or [DBeaver Community](https://dbeaver.io/)
Ensure you are using the same driver version as JCS so use the one from the
maven repository like so:
 
### Squirrel:
![UI screenshot: Squirrel H2 driver settings](assets/squirrel_driver_settings.png?raw=true) 

### DBeaver:
![UI screenshot: DBeaver H2 driver settings](assets/dbeaver_driver_settings.png?raw=true) 

### Database connection

#### Squirrel:

![UI screenshot: Squirrel jcs schema connection](assets/squirrel_connection_jcs.png?raw=true) 

![UI screenshot: Squirrel SA schema connection](assets/squirrel_connection_sa.png?raw=true) 

#### DBeaver

![UI screenshot: DBeaver jcs schema connection](assets/dbeaver_connection_jcs.png?raw=true) 

![UI screenshot: DBeaver SA schema connection](assets/dbeaver_connection_sa.png?raw=true) 

#### JDBC URLs

JCS: jdbc:h2:/<home folder>/jcs/jcs-db;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE;SCHEMA=jcs
User: jcs pass: repo

SA:  jdbc:h2:/home/frans/jcs/jcs-db;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE
User: sa pass jcs