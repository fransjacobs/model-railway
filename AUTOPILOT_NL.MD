# Autopilot Module

## Overzicht
De **Autopilot**-module is de motor die treinen automatisch laat rijden binnen het JCS (Java Central Station) systeem.  
Zodra locomotieven aan **blokken** zijn toegewezen en hun **rijrichting** door de gebruiker is ingesteld, kan de Autopilot worden geactiveerd om volledig automatische blok-naar-blok besturing te verzorgen.

Autopilot zorgt ervoor dat:
- Een locomotief alleen kan rijden van het huidige blok naar een **vrij** aangrenzend blok.
- Wanneer een locomotief **aankomt** in het bestemmingsblok:
  - Het **vertrekblok** wordt op **vrij** gezet.
  - Het **bestemmingsblok** wordt op **bezet** gezet.
- Locomotieven wachten als er geen vrij blok beschikbaar is, waardoor botsingen worden voorkomen.

---

## Kernconcepten

- **Blok**: Een spoorsegment met bezetmelders. Er zijn geen wissels toegestaan binnen een blok.
- **Locomotief**: Een bestuurbare trein met richting, snelheid en functies.
- **Dispatcher**: Een runtime-controller die per locomotief wordt aangemaakt wanneer Autopilot actief is. Verantwoordelijk voor routekeuze, blokreservering en treinbesturing.
- **Command Station**: De geabstraheerde hardwarelaag (CS3, ECoS, DCC-EX, enz.) waarlangs Autopilot snelheids-, richtings- en accessoirecommando’s verstuurt.
- **Sensoren**: Terugmelders die vertrek en aankomst bevestigen en zo een veilige blokoverdracht garanderen.

---

## Workflow voor de gebruiker

1. **Voorbereiden van de baan**
   - Definieer blokken met geldige sensoren.
   - Plaats locomotieven in hun startblokken.
   - Stel voor elke locomotief de **rijrichting** in (eventueel via de blokmenu-tools).

2. **Autopilot inschakelen**
   - Druk op de **Pilot**-knop in de UI om Autopilot te activeren.
   - Er wordt een dispatcher aangemaakt voor elke locomotief die op de baan staat.

3. **Starten**
   - Start een enkele locomotief vanuit het **blokmenu**, of
   - Gebruik **Cruise Control → Start All** om alle dispatchers tegelijk te starten.

4. **Tijdens bedrijf**
   - Autopilot voert continu de volgende taken uit:
     - Selecteren van het volgende vrije blok,
     - Instellen van benodigde wissels/rijwegen,
     - Versturen van rijcommando’s naar de locomotief,
     - Bijwerken van blokbezetting zodra de aankomst bevestigd is.

---

## Regels & Veiligheid

- **Altijd blok-naar-blok**: Beweging vindt altijd plaats tussen volledige blokken.
- **Botsingspreventie**: Locomotieven rijden nooit een bezet blok binnen.
- **Wachtstand**: Een locomotief blijft in het huidige blok als er geen aangrenzend blok vrij is.
- **Handmatige bediening**: Locomotieven kunnen op elk moment handmatig gestart of gestopt worden.

---

## Ontwikkelaarsnotities

- **Routeringslogica** kan worden uitgebreid met prioriteiten (bijv. voorkom pendelen, kies langere routes, of respecteer seinen).
- **Snelheidsprofielen** kunnen worden geïntegreerd voor vloeiender optrekken en afremmen.
- **Testen**: Autopilot wordt getest in `AutoPilotTest` en verwante unittests. Breid deze uit bij nieuwe logica.
- **Integratie**: Gebruikt de `JCSCommandStation`-interface om commando’s te versturen en events te ontvangen.

---

## Voorbeeld levenscyclus

```mermaid
sequenceDiagram
    participant Gebruiker
    participant Dispatcher
    participant CommandStation
    participant Blok

    Gebruiker->>Dispatcher: Start locomotief in Blok A
    Dispatcher->>CommandStation: Zet wissels, stel snelheid/richting in
    CommandStation->>Blok: Trein verlaat Blok A
    Blok->>Dispatcher: Vertreksensor geactiveerd
    Dispatcher->>Blok: Reserveer Blok B (bestemming)
    CommandStation->>Blok: Trein komt Blok B binnen
    Blok->>Dispatcher: Aankomstsensor geactiveerd
    Dispatcher->>Blok: Markeer A=Vrij, B=Bezet
    Dispatcher->>Dispatcher: Herhaal cyclus
