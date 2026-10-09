# Building JCS

[README](README.md)

## Requirements

JCS is primarily developed using the [NetBeans IDE](https://netbeans.apache.org).

The JDK used is [Temurin JDK 25](https://adoptium.net/en-GB/temurin).

The application is built with [Maven](https://maven.apache.org).

## Obtaining the source code

Before you can build JCS, you need to obtain the source code.
The recommended method is to clone the repository from
[GitHub](https://github.com/fransjacobs/model-railway).

Create a directory in which to clone the repository.
Also create a directory named `jcs` in your home directory (`~/jcs`).

You can also download the source code from:
https://github.com/fransjacobs/model-railway/archive/refs/heads/master.zip

### Build tool

JCS is built with [Maven](https://maven.apache.org/download.cgi).
Maven automatically downloads all project dependencies.

### IDE

JCS is primarily developed using 
[NetBeans](https://netbeans.apache.org/front/main/index.html). 
Open the project in NetBeans and run a build.

### Command line

Check that your environment is configured correctly.

-   Windows: use `set`
-   Linux/macOS: use `env`

Check that `mvn` is in your PATH and that `JAVA_HOME` is set.

#### Check the JDK version

```text
java -version
```

This should return something similar to the following (Windows):

```text
C:\Users\frans>java -version
openjdk version "25.0.2" 2026-01-20 LTS
OpenJDK Runtime Environment Temurin-25.0.2+10 (build 25.0.2+10-LTS)
OpenJDK 64-Bit Server VM Temurin-25.0.2+10 (build 25.0.2+10-LTS, mixed mode, sharing)
```

#### Check Maven

```text
mvn -version
```

This should return something similar to the following (Windows):

```text
C:\Users\frans>mvn -version
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: C:\ProgramFiles\apache-maven-3.9.16
Java version: 25.0.2, vendor: Eclipse Adoptium, runtime: C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
Default locale: en_US, platform encoding: UTF-8
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```

## Building

Go to the directory containing the source code. This directory must contain
`pom.xml`.

```text
mvn clean package jpackage:jpackage
```

After a while, you should see:

```text
[INFO] --- jpackage:1.8.0:jpackage (default-cli) @ jcs ---
[INFO] Loaded 24041 auto-discovered prefixes for remote repository maven_central (prefixes-maven_central.txt)
[INFO] Using: C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\jpackage.exe
[INFO] jpackage options:
[INFO]   --name jcs
[INFO]   --dest C:\path\to\model-railway\target\jpackage
[INFO]   --type app-image
[INFO]   --app-version 1.0.0
[INFO]   --copyright Frans Jacobs
[INFO]   --description JCS is model railroad automation software
[INFO]   --input C:\path\to\model-railway\target
[INFO]   --vendor Frans Jacobs
[INFO]   --main-class jcs.JCS
[INFO]   --main-jar jcs-0.0.4-SNAPSHOT.jar
[INFO]   --icon C:\path\to\model-railway\config\jpackage\resources\JCS.ico
[INFO]   --java-options -Dfile.encoding=UTF-8
[INFO]   --java-options -Dtinylog.writer.level=trace
[INFO]   --java-options -Xms256m
[INFO]   --java-options -Xmx1024m
[INFO]   --java-options --enable-native-access=com.fazecast.jSerialComm
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:18 min
[INFO] Finished at: 2026-10-09T21:04:57+02:00
[INFO] ------------------------------------------------------------------------
√ model-railway %
```

The executable application can then be found in the `...\target\jpackage\jcs` directory (on Windows, `jcs.exe`).

### macOS

Follow the same procedure for checking the paths as described above for Windows.

Open the Terminal app and `cd` to the directory containing the source code.
The `pom.xml` file must be present in this directory.


For the JDK, you should see something like:
```text
√ model-railway % java -version
openjdk version "25.0.2" 2026-01-20 LTS
OpenJDK Runtime Environment Temurin-25.0.2+10 (build 25.0.2+10-LTS)
OpenJDK 64-Bit Server VM Temurin-25.0.2+10 (build 25.0.2+10-LTS, mixed mode)
√ model-railway %
```

For Maven, it should look something like this:

```text
√ model-railway % mvn -version
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: /opt/apache-maven-3.9.11
Java version: 25.0.2, vendor: Eclipse Adoptium, runtime: /path/to/java/25.0.2-tem
Default locale: en_NL, platform encoding: UTF-8
OS name: "mac os x", version: "26.7.1", arch: "x86_64", family: "mac"
√ model-railway % 
```

If the JDK and Maven paths are correct, run:

```text
mvn package jpackage:jpackage
```
 
Na enige tijd:
```text
[INFO] 
[INFO] --- jpackage:1.8.0:jpackage (default-cli) @ jcs ---
[INFO] Using: /path/to/bin/jpackage
[INFO] jpackage options:
[INFO]   --name jcs
[INFO]   --dest /path/to/model-railway/target/jpackage
[INFO]   --type app-image
[INFO]   --app-version 1.0.0
[INFO]   --copyright Frans Jacobs
[INFO]   --description JCS is model railroad automation software
[INFO]   --input /path/to/model-railway/target
[INFO]   --vendor Frans Jacobs
[INFO]   --main-class jcs.JCS
[INFO]   --main-jar jcs-0.0.4-SNAPSHOT.jar
[INFO]   --icon /path/to/model-railway/config/jpackage/resources/JCS.icns
[INFO]   --java-options -Dfile.encoding=UTF-8
[INFO]   --java-options -Dtinylog.writer.level=trace
[INFO]   --java-options -Xms256m
[INFO]   --java-options -Xmx1024m
[INFO]   --java-options --enable-native-access=com.fazecast.jSerialComm
[INFO]   --mac-package-identifier jcs
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  47.158 s
[INFO] Finished at: 2026-10-09T21:21:12+02:00
[INFO] ------------------------------------------------------------------------
√ model-railway % 
```

The `jcs.app` file is now located in the `../target/jpackage` directory.
This is the executable application.

When starting the application, you may receive a warning that the app is from an unknown developer and/or has not been "notarized".

## Running JCS

Configuration and layout data are stored in an embedded
H2 database in `$HOME/jcs`.

If the database does not yet exist, it is created automatically
the first time the application starts.

## Debugging the database

JCS uses an embedded
[H2](https://h2database.com/html/main.html) database to store configuration data.

On the first startup, the database, the `jcs` schema, and the
`jcs` user (password `repo`) are created automatically.

## Data model

![Data model: JCS Data Model](assets/datamodel.png?raw=true)

### Connecting to the database

For example, use:

-   [SQuirreL SQL](http://www.squirrelsql.org/)
-   [DBeaver Community](https://dbeaver.io/)

### SQuirreL

![UI screenshot: Squirrel H2 driver
settings](assets/squirrel_driver_settings.png?raw=true)

### DBeaver

![UI screenshot: DBeaver H2 driver
settings](assets/dbeaver_driver_settings.png?raw=true)

### Database connection

#### SQuirreL

![UI screenshot: Squirrel jcs schema
connection](assets/squirrel_connection_jcs.png?raw=true)

![UI screenshot: Squirrel SA schema
connection](assets/squirrel_connection_sa.png?raw=true)

#### DBeaver

![UI screenshot: DBeaver jcs schema
connection](assets/dbeaver_connection_jcs.png?raw=true)

![UI screenshot: DBeaver SA schema
connection](assets/dbeaver_connection_sa.png?raw=true)

### JDBC URLs

``` text
jdbc:h2:/<home folder>/jcs/jcs-db;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE;SCHEMA=jcs
```

User: `jcs`\
Password: `repo`

``` text
jdbc:h2:/home/frans/jcs/jcs-db;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE
```

User: `sa`\
Password: `jcs`
