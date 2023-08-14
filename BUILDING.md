# Building JCS

## Requirements
JCS is mainly developed using the [Netbeans IDE](https://netbeans.apache.org)
 
I use the [Adoptium JDK 17](https://adoptium.net/en-GB/)

The program is build using [Maven](https://maven.apache.org) 

## Building 
Create a folder where you clone the repository.
Create a folder in your home folder called jcs (~/jcs).

In Netbeans open the project en perform a build
On the command line cd into the model-railway folder
run mvn install

## Running
The configuration and layout data is stored in an embedded (H2) database.
The database files are placed in the $HOME/jcs directory.
When the database does not exist it is created on startup. 

Run the jcs.JCS class an database should be created on first run.

## Debugging
JCS uses an embedded [H2](https://h2database.com/html/main.html) database.
On de first startup, when ther are no files in the jcs home directory (~/jcs)
database is created. The database is created as user SA (pass jcs) this user
creates the jcs schema and user( user jcs pass repo);
Inside the jcs schema the tables are created.

### Connect to the database
It is possble to connect to the embedded JCS database using an SQL tool like
[Squirrel SQL](http://www.squirrelsql.org/) or [DBeaver Community](https://dbeaver.io/)
Ensure you are using the same driver version as JCS so use the one from the
maven repository like so:
 
Squirrel:
![UI screenshot: Squirrel H2 driver settings](assets/squirrel_driver_settings.png?raw=true) 
DBeaver:
![UI screenshot: DBeaver H2 driver settings](assets/dbeaver_driver_settings.png?raw=true) 

### Database connection
Squirrel:
![UI screenshot: Squirrel jcs schema connection](assets/squirrel_connection_jcs.png?raw=true) 

![UI screenshot: Squirrel SA schema connection](assets/squirrel_connection_sa.png?raw=true) 
DBeaver
![UI screenshot: DBeaver jcs schema connection](assets/dbeaver_connection_jcs.png?raw=true) 

![UI screenshot: DBeaver SA schema connection](assets/dbeaver_connection_sa.png?raw=true) 

### JDBC URLs

JCS: jdbc:h2:/<home folder>/jcs/jcs-db;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE;SCHEMA=jcs
User: jcs pass: repo

SA:  jdbc:h2:/home/frans/jcs/jcs-db;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE
User: sa pass jcs