# Java Central Station

🎯 Een hobbyproject waarmee je modeltreinen zelfstandig kunt laten rijden — of ze zelf kunt besturen, helemaal in Java.

---

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](#license)  
[![Release](https://img.shields.io/github/v/release/fransjacobs/model-railway)](https://github.com/fransjacobs/model-railway/releases)  
![Static Badge](https://img.shields.io/badge/Model_Railroad-Automation-blue)  
![GitHub last commit](https://img.shields.io/github/last-commit/fransjacobs/model-railway)  
![GitHub commit activity](https://img.shields.io/github/commit-activity/w/fransjacobs/model-railway)  
[![Java CI with Maven](https://github.com/fransjacobs/model-railway/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/fransjacobs/model-railway/actions/workflows/maven.yml)  
![GitHub Gist last commit](https://img.shields.io/github/last-commit/fransjacobs/model-railway)  
![GitHub issues](https://img.shields.io/github/issues-raw/fransjacobs/model-railway)  
![GitHub Release](https://img.shields.io/github/v/release/fransjacobs/model-railway)  

---

## 🚂 Over JCS

Java Central Station (JCS) is mijn persoonlijke passieproject om een modelspoorbaan tot leven te brengen met software.  
Met JCS kun je je spoorbaan tekenen, treinen op de rails plaatsen en ze van blok naar blok zien bewegen — handmatig of volledig automatisch.  

Ik ben dit project begonnen uit nieuwsgierigheid (en plezier!) om te zien hoe ver ik kon gaan met automatisering van een modelspoor zonder afhankelijk te zijn van gesloten commerciële systemen.  
Onderweg is het uitgegroeid tot een volwaardige tool die:

- Werkt met echte hardware zoals DCC-EX, Märklin CS2/CS3, ESU ECoS en HSI-S88  
- Je laat rijden in Autopilot-modus of handmatig met een Driver Cab  
- Live feedback van sensoren en wissels toont  
- Een Virtual Command Station bevat, zodat je ook zonder hardware kunt testen  

Het doel is niet om professionele producten te vervangen, maar iets te creëren dat **open, uitbreidbaar en leuk** is — voor iedereen die van treinen en programmeren houdt. 🚉✨

---

## 🎯 Waarom dit project?

De meeste commerciële oplossingen voor modelspoorautomatisering voelen als een **black box** — krachtig, maar gesloten, rigide en soms te complex voor hobbybanen.  

Ik ben **JCS** begonnen omdat ik iets anders wilde:  

- Een project waar ik **kan leren door te bouwen**  
- De kans om **vrij te experimenteren** met nieuwe ideeën  
- En bovenal… **plezier hebben met het besturen van treinen** 🚂✨  

Door JCS open source te maken, hoop ik ook andere hobbyisten te inspireren:  

- Knutselaars die willen zien hoe het werkt  
- Bouwers die het willen uitbreiden met eigen functies  
- Of gewoon iedereen die op zoek is naar een gratis en flexibel alternatief  

---

## ✨ Belangrijkste functies

- **Connectie met command stations**: Märklin CS2/CS3, ESU ECoS, DCC-EX, HSI-S88  
- **Layout editor**: Interactieve grafische editor om sporen, blokken en sensoren te ontwerpen  
- **Automatisch treinen laten rijden**: Autopilot regelt routing en blokcontrole  
- **Handmatig besturen**: Gebruik de ingebouwde Throttle / Driver Cab  
- **Realtime feedback**: Live sensoren, wissels en blokupdates  
- **Testen zonder hardware**: Virtual Command Station voor simulatie  
- **Remote zichtbaarheid**: VNC-viewer voor Märklin CS3 en ESU ECoS  

> Of je nu de controle aan Autopilot geeft of zelf rijdt, JCS brengt je baan tot leven.

---

## Schermafbeeldingen & Demos

- **Throttle / Driver Cab**: Locomotiefbesturing  
- **Layout Editor & Autopilot**: Ontwerp je baan, plaats locomotieven en automatiseer ze  
- **Sensor Monitor**: Live gegevens van sensoren bekijken  
- **Keyboard Panel**: Handmatig accessoires bedienen of sensoren volgen  
- **Preferences / Locomotive Import**: Importeren vanuit Märklin CS2/CS3 of iconen handmatig toevoegen  

---

## Ondersteunde Command Stations

- [Märklin CS-3](https://www.marklin.nl/producten/details/article/60216)  
- [Märklin CS-2](https://www.marklin.nl/producten/details/article/60215) — [Protocoldocumentatie](http://streaming.maerklin.de/public-media/cs2/cs2CAN-Protokoll-2_0.pdf)  
- [ESU ECoS](https://www.esu.eu/) — [Protocoldocumentatie ESU](https://github.com/cbries/railessentials/blob/master/ecoslibNet48/Documentation/ecos_pc_interface3.pdf) — [Community-versie](https://github.com/TabalugaDrache/TCPEcos/files/13458970/Netzwerkspezifikation_2023.pdf)  
- [DCC-EX](https://dcc-ex.com) — kan via seriële poort of netwerk worden aangesloten  
- [HSI-S88](https://www.ldt-infocenter.com/dokuwiki/doku.php?id=en:hsi-88-usb) — of de [DIY-versie](https://mobatron.4lima.de/2020/05/s88-scanner-mit-arduino)  

---

### Huidige status & roadmap

Actief in ontwikkeling:  

- Documentatie verbeteren  
- GUI verbeteren  
- Signaaldiplay in automatische modus toevoegen  
- Internationalisatie (meertalige ondersteuning)  
- Meer unit tests  
- Meer hardware-integraties  

---

## Aan de slag

### Vereisten

- Java 21 (bijv. Temurin OpenJDK)  
- Een ondersteund command station  

### Download een prebuild release

De laatste versie is **v0.0.2** (27 september 2024):  

- Eerste volledig geautomatiseerde rijversie  
- Uitvoerbare bestanden voor Windows, macOS, Linux en Uber-JAR-bundel  
- Volledige changelog: [Releases](https://github.com/fransjacobs/model-railway/releases)  

### Build vanuit bron

Zie [BUILDING.md](BUILDING.md) voor volledige buildinstructies.

### Setup en gebruik

- Walkthrough: [JCS_SETUP.md](JCS_SETUP.md)  
- Rijden & automatiseringshandleiding: [DRIVING.md](DRIVING.md)  
- Interface documentatie: [INTERFACES.md](INTERFACES.md)  

---

## Bijdragen

Bijdragen, bugmeldingen of ideeën zijn zeer welkom!  
Open issues, stel verbeteringen voor of dien pull requests in.

---

## Licentie

Gelicenseerd onder Apache-2.0. Zie [LICENSE](LICENSE) voor details.  

---

Ik hoop dat je geïnspireerd raakt!  

Frans

## Bijdragers

<table>
<tr>
    <td align="center">
        <a href="https://github.com/fransjacobs">
            <img src="https://avatars.githubusercontent.com/u/41232225?v=4" width="100;" alt="frans"/>
            <br />
            <sub><b>Frans Jacobs</b></sub>
        </a>
    </td>
    <td><a href="https://www.buymeacoffee.com/fransjacobs" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" height="41" width="174"></a>
    </td>
</tr>
</table>

## Copyright 2019 - 2025 Frans Jacobs

Hierbij wordt toestemming verleend, kosteloos, aan iedereen die een kopie van deze software en bijbehorende documentatiebestanden verkrijgt (de "Software"),  
om de Software te gebruiken, kopiëren, wijzigen, samenvoegen, publiceren, distribueren, sublicentiëren, en/of verkopen, en om personen aan wie de Software wordt verstrekt dit toe te staan, onder de volgende voorwaarden:

Bovenstaande copyrightmelding en deze toestemmingsverklaring moeten in alle kopieën of substantiële delen van de Software worden opgenomen.

DE SOFTWARE WORDT "AS IS" GELEVERD, ZONDER ENIGE GARANTIE, UITDRUKKELIJK OF IMPLICIET, INCLUSIEF, MAAR NIET BEPERKT TOT DE GARANTIES VAN VERKOOPBAARHEID, GESCHIKTHEID VOOR EEN BEPAALD DOEL EN NIET-SCHENDING. IN GEEN GEVAL ZULLEN DE AUTEURS OF COPYRIGHTHOUDERS AANSPRAKELIJK ZIJN VOOR ENIGE CLAIM, SCHADE OF ANDERE AANSPRAKELIJKHEDEN, HETZIJ IN EEN CONTRACTUELE ACTIE, ONRECHTMATIGE DAAD, OF ANDERSZINS, DIE VOORTVLOEIEN UIT OF IN VERBAND MET DE SOFTWARE OF HET GEBRUIK OF ANDERE HANDELINGEN IN DE SOFTWARE.
