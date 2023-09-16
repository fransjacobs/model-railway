Modelspoor besturing met JCS (Java Central Station)

Wat moet het kunnen?

Het systeem/programma moet er voor zorgen dat er veilig treinen kunnen rijden op een modelspoorbaan. 
Aannames: 
Het digitale signaal dat op de rails staat wordt door een controller gegenereerd.
Het systeem is op een of andere wijze verbonden met de controller.
Terugmeldingen van events gebeurt via de controller.
De controller is, in eerste instantie, een Marklin CS 3 maar dat kan worden uitgebreid. 

Schermen van het systeem

De gebruiker moet in staat zijn het rijden van modeltreinen te volgen. Hiervoor zijn schermen die nodig die verschillende zaken die nodig zijn voor het programma visualiseren:
Weergave van het sporen plan, eventueel alleen schematisch. Het spoorplan kan worden getekend en kan laten zien waar een trein zich bevind.
Weergave van terugmeldingen, in een tabel oid, zodat door de gebruiker het mogelijk is terugmeldingen te testen.
Weergave van accessoires, zoals wissels en seinen, end deze te bedienen.
Besturen van een of meerdere locomotieven.
Importeren van locomotieven vanuit de controller.
Importeren van accessoires vanuit de controller.
Opslag van scherminstellingen.
Systeem configuratie scherm.
Creëren of wijzigen van routes.
Systeem status scherm.
…

Automatisch rijden

Het programma moet de gebruiker in staat stellen om met meerdere treinen tegelijkertijd op de modelbaan te rijden. Dit rijden moet op een veilige manier kunnen zodat er geen botsingen of ongelukken gebeuren. Het besturing programma moet daarom “weten” waar een trein op het sporenplan staat.

Deze “automatische treindienstleider functionaliteit” draagt er zorgt voor dat dat treinen automatisch kunnen rijden. Dit kan op 3 manieren:
Willekeurig, treinen gaan rijden van en naar vrije sporen/blokken.
Met een dienst regeling.
Combinatie van bovenstaande.


In automatic mode many objects are involved to get it running.
Low level objects
Those objects are as close to hardware as possible. There are two categories: input and output.
A list of low level objects directly involved:
sensors
switches
signals
loco decoders
High level objects
The high level objects are using low level objects for getting information from the layout and controlling it.
Main high level objects:
locos
blocks
The LcDriver library
The Loc Driver library is the conductor of the automatic mode.
It uses a lot of objects to get its work done:
locos
blocks 
routes
schedules
groups
Blocks
Blocks are only interested in sensor events which will be evaluated as set in the routes definitions. A block add it self as listener to all sensors defined for it. If a sensor event make sense and the block is reserved by a loco object it will inform that object of the event.
Locos
Every Loco runs in its own thread. Locos are getting informed by block events which are processed in the LcDriver library. The LcDriver will send commands back to the loco object incase the decoder must change velocity, direction or function.
LcDriver overview

lcdriver
The library entry point which runs in the loco's thread context.
events
Events are coming indirectly from block objects from the loco object.
enter
The enter event is evaluated if it fits the current status of the loco involved. If all is OK the loco status is set to ENTER.
exit


Begrippen

Je kan op verschillende manieren een sporenplan indelen. Verschillende besturing programma Hebb allemaal hun eigen filosofie. JCS is daar niet anders in, dus dienen er eerst een aantal definities te worden vastgesteld.

Trein
Combinatie van een locomotief met of zonder wagons. Een trein kan zich over de baan verplaatsen. Een trein heeft een lengte.
Baanvak of Blok
Een stuk van het sporenplan waarin zich precies 1 trein kan bevinden (of geen). Als er een trein in het baanvak is dan is het baanvak bezet. Een baanvak heeft een lengte die langer is dan de trein lengte. Een baanvak wordt begrenst door een wissel of door een sein. Een baanvak heeft minimaal 1 detectie punt, zodat het programma “weet” wat de status van het baanvak is. Een baanvak kan een verplichte rijrichting hebben.
Een baanvak wordt ook wel blok genoemd.

Baanvak statussen:
Vrij, er kan een trein in rijden, alle melder geven “vrij” aan.
Bezet, er is een trein aanwezig in het baanvak. De bezet melder geeft “bezet” aan. Er staat al een trein in het baanvak of de trein die aan het binnenrijden is moet stoppen.
Inrijdend, een trein is het baanvak in gereden. De inrij melder geeft “bezet” aan.
Uitrijdend, een trein rijdt het baanvak uit. De uitrij melder geeft “bezet” aan
Remmen, de trein die het baanvak aan het binnenrijden is moet afremmen. De remmelder geeft “bezet” aan.
Gereserveerd, er is een trein onderweg naar het baanvak. 
Buiten gebruik, er kunnen geen treinen gebruik maken van het baanvak.

Baanvak detectie:
1 detectie punt of terugmelder: bezet of vrij
2 detectie punten of terugmelders: binnenkomend en bezet
3 detectie punten of terugmelders: binnenkomend, bezet en uitrijden
4 detectie punten of terugmelders: binnenkomend, remmen, bezet en uitrijden.
Rijweg 
Een rijweg is de verbinding tussen twee aanliggende baanvakken. Tussen de baanvakken kan een wissel of een sein zitten. In het geval van een wissel heeft de wissel een richting, rechtdoor of afbuigend.
Locatie
Een locatie is een verzameling aan baanvakken is een bepaalde locatie uitbeelden, bijvoorbeeld een station. Een station kan bestaan uit meerdere sporen of te wel baanvakken.
Route
Een route is een verzameling van rijwegen tussen 2 locaties.

Automatisch rijden

Hoe pak je dat aan?
Als eerste moet er een mogelijkheid zijn om een trein aan een baanvak toe bewijzen. M.a.w. de trein staat in het baanvak. Hierbij komt gelijk de vraag, wat is de richting van de trein?
Een baanvak/blok heeft 2 kanten dus die moeten we duidelijk maken door een indicatie bv met een + (plus) en - (min). De trein kan dus van - naar + rijden of tegengesteld van + naar -.

Qua display zou het aardig zijn om een afbeelding van de trein te tonen en de richting van de trein tot uitdrukking te laten komen. Ieder geval zou het baanvak symbool in het scherm de trein naam en richting moeten tonen, plus de + of - van het baanvak.

In het geval van “willekeurig” rijden:
Als de trein in een baanvak staat dan moet er worden gezocht naar een rijweg naar het volgende baanvak. Bij deze zoektocht dient dus rekening te worden gehouden met de richting die de trein opgaat.

Eerste taak is dus het zoeken naar een rijweg in de richting die de trein opgaat. Als die rijweg wordt gevonden dan moet er gecontroleerd worden of de rijweg vrij is. Zo ja dan wordt de rijweg gereserveerd. Een gereserveerde rijweg kan door andere treinen niet meer worden gebruikt.

Nu kan de trein worden gestart. De trein bevindt zich in de rijdende conditie.

Rijweg of blok statussen:
Vrij, bezet, gereserveerd en buiten gebruik.
De bezet status heeft sub statussen: bezet (stilstand). bezet inrijden, bezet uitrijden.
 
Een trein die automatisch rijdt kan net zolang rijden totdat er een inrij melding van het baanbak van de bestemming. Dan dient de trein vaart te minderen, totdat de bezet melding komt van bezetmelder van het baanvak van de bestemming. Als die melding komt dient de trein onmiddellijk te stoppen. De trein is aangekomen op z’n bestemming.

Bij het inrijden van de trein in het baanvak van de bestemming moeten een aantal acties worden gestart:

Moet de trien wachten op de bestemming?
(Bij de instelling van het baanvak is dus een keuze mogelijkheid, wachten niet wachten en hoelang wachten)

Als de trein niet hoeft te wachten wat is dan de volgende bestemming?

Data punten

Blok
Richting; omdat je 2 kanten een blok in of uit kan.
Locomotief; omdat een locomotief in een blok staat over gaat.
Richting van de locomotief, kan ook bij de locomotief worden opgeslagen; om te weten waar de lok heen gaat
Status; vrij, en mag een trien komen, bezet er staat een trein in het blok, gereserveerd, er komt een trein aan, buitengebruik, blok kan niet worden gebruikt
Inrij melder
Bezet melder

Rijwegen aan + kant hoort dit niet bij de rijwegen?
Rijwegen aan - kant, 
Seinen +
Seinen -
Lengte, om….

Locomotief
Address (van controller)
Snelheid (van controller) ook zetbaar
Richting (van controller) ook zetbaar
Functies (van controller)
Naam (van controller)
Afbeelding (van controller)
Lengte
Pendelen


Rijweg
Van blok +/-
Naar blok +/-

Rijweg objecten
Wissel stand (G of R)


Sensoren
Adres
Status

Persistentie laag

Database of persistent implementatie
Zelf doen met jdbc veel “boilerplate” werk plus testen.
Kleine wijzigingen kosten relatief veel tijd. 

Even “tiny” ORM gebruiken dit onderzoeken






