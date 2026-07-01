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

java -version

Dit zou ongeveer het volgende moeten teruggeven (Window):

C:\Users\frans>java -version
openjdk version "25.0.2" 2026-01-20 LTS
OpenJDK Runtime Environment Temurin-25.0.2+10 (build 25.0.2+10-LTS)
OpenJDK 64-Bit Server VM Temurin-25.0.2+10 (build 25.0.2+10-LTS, mixed mode, sharing)

#### Controleer Maven

mvn -version

Dit zou ongeveer het volgende moeten teruggeven (Window):

C:\Users\frans>mvn -version
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: C:\ProgramFiles\apache-maven-3.9.16
Java version: 25.0.2, vendor: Eclipse Adoptium, runtime: C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
Default locale: en_US, platform encoding: UTF-8
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"

## Bouwen

Ga naar de map waarin de broncode staat. Deze map moet `pom.xml`
bevatten.

mvn package -P package-app

Na enige tijd verschijnt:

[INFO] BUILD SUCCESS

In de map `target/jcs` staat vervolgens het uitvoerbare programma (onder
Windows `jcs.exe`).

## JCS uitvoeren

De configuratie- en baangegevens worden opgeslagen in een ingebouwde
H2-database in `$HOME/jcs`.

Bestaat de database nog niet, dan wordt deze automatisch aangemaakt bij
de eerste keer opstarten.

Start de klasse `jcs.JCS`.

## Debuggen

Extra instellingen worden opgeslagen in `user.home/jcs/jcs.properties`.

### Locomotieven ophalen

``` properties
locomotive.list.via=can
locomotive.list.via=http
```

### Accessoires ophalen

``` properties
accessory.list.via=can
accessory.list.via=http
accessory.list.via=JSON
```

### Zlib-debug

``` properties
inflate.debug=true
```

Standaard:

``` properties
inflate.debug=false
```

## Debugdatabase

JCS gebruikt een ingebouwde
[H2](https://h2database.com/html/main.html)-database.

Bij de eerste start wordt automatisch de database, het schema `jcs` en
gebruiker `jcs` (wachtwoord `repo`) aangemaakt.

## Datamodel

![UI screenshot: JCS Datamodel](assets/jcs_datamodel.png?raw=true)

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