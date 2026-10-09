# JCS bouwen

[README](README.md)

## Vereisten

JCS wordt voornamelijk ontwikkeld met de [NetBeans IDE](https://netbeans.apache.org).

De JDK is de [Temurin JDK 25](https://adoptium.net/en-GB/temurin).

Het programma wordt gebouwd met [Maven](https://maven.apache.org).

## Broncode ophalen

Voordat je JCS kunt bouwen, moet je de broncode ophalen.
De aanbevolen manier is om de repository te klonen vanaf
[GitHub](https://github.com/fransjacobs/model-railway).

Maak een map waarin je de repository kloont.
Maak daarnaast een map met de naam `jcs` aan in je thuismap (`~/jcs`).

Je kunt de broncode ook downloaden via:
https://github.com/fransjacobs/model-railway/archive/refs/heads/master.zip

### Build-tool

JCS wordt gebouwd met [Maven](https://maven.apache.org/download.cgi).
Maven download automatisch alle projectafhankelijkheden.

### IDE

De ontwikkeling van JCS gebeurt voornamelijk met 
[NetBeans](https://netbeans.apache.org/front/main/index.html). 
Open het project in NetBeans en voer een build uit.

### Opdrachtregel

Controleer of je omgeving correct is ingesteld.

-   Windows: gebruik `set`
-   Linux/macOS: gebruik `env`

Controleer dat `mvn` in je PATH staat en dat `JAVA_HOME` is ingesteld.

#### Controleer de JDK-versie

```text
java -version
```

Dit zou ongeveer het volgende moeten teruggeven (Window):

```text
C:\Users\frans>java -version
openjdk version "25.0.2" 2026-01-20 LTS
OpenJDK Runtime Environment Temurin-25.0.2+10 (build 25.0.2+10-LTS)
OpenJDK 64-Bit Server VM Temurin-25.0.2+10 (build 25.0.2+10-LTS, mixed mode, sharing)
```

#### Controleer Maven

```text
mvn -version
```

Dit zou ongeveer het volgende moeten teruggeven (Window):

```text
C:\Users\frans>mvn -version
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: C:\ProgramFiles\apache-maven-3.9.16
Java version: 25.0.2, vendor: Eclipse Adoptium, runtime: C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
Default locale: en_US, platform encoding: UTF-8
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```

## Bouwen

Ga naar de map waarin de broncode staat. Deze map moet `pom.xml`
bevatten.

```text
mvn clean package jpackage:jpackage
```

Na enige tijd verschijnt:

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

In de map  ...\target\jpackage\jcs staat vervolgens het uitvoerbare programma (onder Windows `jcs.exe`).

### MAC OS

Volg dezelfde procedure voor het controleren van de paden als hierboven bescheven voor Windows.

Open de Terminal App. cd naar de directory waar de source code staat.
In deze directory moet de file pom.xml staan.


Voor de JDK moet je iets zien als:
```text
√ model-railway % java -version
openjdk version "25.0.2" 2026-01-20 LTS
OpenJDK Runtime Environment Temurin-25.0.2+10 (build 25.0.2+10-LTS)
OpenJDK 64-Bit Server VM Temurin-25.0.2+10 (build 25.0.2+10-LTS, mixed mode)
√ model-railway %
```

Voor Maven ziet het er ongeveer zo uit:

```text
√ model-railway % mvn -version
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: /opt/apache-maven-3.9.11
Java version: 25.0.2, vendor: Eclipse Adoptium, runtime: /path/to/java/25.0.2-tem
Default locale: en_NL, platform encoding: UTF-8
OS name: "mac os x", version: "26.7.1", arch: "x86_64", family: "mac"
√ model-railway % 
```

Als de paden van de JDK en Maven kloppen run:

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

In de directory ../target/jpackage staat nu het bestand jcs.app.
Dit is de uitvoerbare applicatie.

Bij het starten zou je een melding kunnen krijgen dat de App van een onbekende ontwikkelaar is en / of niet "notarized" is.

## JCS uitvoeren

De configuratie- en baangegevens worden opgeslagen in een ingebouwde
H2-database in `$HOME/jcs`.

Bestaat de database nog niet, dan wordt deze automatisch aangemaakt bij
de eerste keer opstarten.

## Debug database

JCS gebruikt een ingebouwde
[H2](https://h2database.com/html/main.html)-database voor de opslag van configuratie data.

Bij de eerste start wordt automatisch de database, het schema `jcs` en
gebruiker `jcs` (wachtwoord `repo`) aangemaakt.

## Datamodel

![Datamodel: JCS Datamodel](assets/datamodel.png?raw=true)

### Verbinding maken met de database

Gebruik bijvoorbeeld:

-   [SQuirreL SQL](http://www.squirrelsql.org/)
-   [DBeaver Community](https://dbeaver.io/)

### SQuirreL

![UI screenshot: Squirrel H2 driver
settings](assets/squirrel_driver_settings.png?raw=true)

### DBeaver

![UI screenshot: DBeaver H2 driver
settings](assets/dbeaver_driver_settings.png?raw=true)

### Databaseverbinding

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

### JDBC-URL's

``` text
jdbc:h2:/<home folder>/jcs/jcs-db;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE;SCHEMA=jcs
```

Gebruiker: `jcs`\
Wachtwoord: `repo`

``` text
jdbc:h2:/home/frans/jcs/jcs-db;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE
```

Gebruiker: `sa`\
Wachtwoord: `jcs`