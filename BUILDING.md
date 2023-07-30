# Building JCS

## Requirements
*JCS is mainly developed using the [Netbeans IDE](https://netbeans.apache.org). 
*I use the [Adoptium JDK 17](https://adoptium.net/en-GB/).
*The program is build using [Maven](https://maven.apache.org). 

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



