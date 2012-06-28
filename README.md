"Minlog" service
====================
Den nyeste udgave at dette dokument kan findes på <https://github.com/trifork/nsi-minlog/>

En pdf udgave af reporten kan laves med <https://github.com/walle/gimli> ved blot at kører *gimli* fra roden af projektet.

Installationsvejledning
-----------------------


### Krav til miljø

Komponenterne er udviklet og testet JBoss 6.0, og kan deployes i produktion på alle nævnte applikationsservere. Dog er der kun skrevet installationsvejledning for deployment på JBoss 6.0, 
da det er denne platform som bruges på den nationale service platform (NSP).

Applikationsserveren kræver SUN/Oracle Java 6.0 eller højere.

#### Krav til Operativsystem
Der stilles ingen krav til operativsystemet, ud over det åbenlyse krav om at Java er understøttet på operativsystemet. 
Ubuntu Linux bruges som operativsystem på NSP’en. Til udvikling af komponenten er OS X anvendt.

#### Krav til database
Komponenten er testet mod MySQL version 5.5.11. Det er den samme MySQL version som bliver brugt på NSP platformen (NIAB version 1.1.3).

#### Krav til hardware
Der er nogle minimumskrav for at kunne afvikle komponenten fornuftigt til testformål.  

Minimumskravene, for fornuftig performance på et test-setup, er:

	• Intel Core 2 eller lignende CPU
	• 8 GB ram
	• Nødvendig harddisk plads for at kunne håndtere alle logs + komponent og server (100+ GB)

### Configuration
De fleste af konfigurationsfilerne skal ligge i jBoss serverinstansens *conf* bibliotek - f.eks. *server/default/conf/log4j-minlog.xml*.

Desuden er web-applikationen konfigureret med standard indstillinger der kan overskrives ved at ligge *minlog."brugernavn".properties* 
og/eller *jdbc."brugernavn".properties* i *conf*. Hvor "brugernavn" er brugeren der kører web-applikationen - f.eks. *server/default/conf/minlog.jboss.properties* som brugeren hedder i NIAB

#### Standard indstillinger
    
    minlog.splunk.host=localhost
    minlog.splunk.port=8089	
    minlog.splunk.user=admin
    minlog.splunk.password=minlog
    minlog.splunk.schema=http

    minlog.import.sleep=2000
    minlog.import.cron=0 * * * * ?
    minlog.import.delay=30000

    minlog.cleanup.cron=0 0 * * * ?  
    
    sosi.production=0
    sosi.canSkipSosi=0    
    
    jdbc.url=jdbc:mysql://localhost:3306/minlog
    jdbc.username=minlog
    jdbc.password=

*min.splunk.\** Bestemmer opsætningen til splunk forbindelsen.

*min.import.sleep* Splunk requestet kører asynkront, der bliver derfor lavet buzy-spinning for at undersøge hvornår jobbet er færdigt. Sleep er tiden jobbet sover imellem hvert tjek.
*min.import.cron* Cron til start af splunk.
*min.import.delay* Splunk henter data fra *nu - delay* se design af splunk-job.

*minlog.cleanup.cron* Cron til start af oprydningsjobbet.

*sosi.production* bestemmer om koden bruger SOSIFederation eller SOSITestFederation.  
*sosi.canSkipSosi* bestemmer om hvorvidt sikkerhedstjekket fejler hvis securityheaderen ikke er korrekt.


*jdbc.\** Opsætning af database forbindelse.

Bemærk at der til *minlog.import.cron* og *minlog.cleanup.cron* bruges Quartz - CronTrigger notation <http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06>


#### Logging
Minlog kræver, at der ligger en *log4j-minlog.xml* og en *log4j.dtd* i *conf*, disse bruges til at konfigurere minlogs log4j.

For at SLALog skal fungere skal der ligge en *nspslalog-minlog.properties* i *conf*

*default.properties*, *jdbc.default.properties*, *log4j-minlog.xml*, *log4j.dtd* samt *nspslalog-minlog.properties* 
ligger alle sammen i artifakten under *WEB-INF/classes* eller i repositoriet under *minlogudtraekservice/src/resources*

### Database
Minlog kræver en mysql database og er testet imod MySQL 5.5.11.

Database opsætningen ligger i *minlogudtraekservice/src/main/resources/db/migration* og skal køres efter versionsnummer.
Scriptsene antager at der i forvejen er oprettet en database ved navn *minlog*

### SmokeTest af WebService
Der kan køres en SmokeTest på */jsp/checkall.jsp*, som tjekker interne afhængigheder.
Hvis der returneres andet end http status *200*, betyder det at applikationen ikke virker som den skal.

### Splunk udtræk
Vejledning til opsættelse af splunk udtræksjob kan ses i slutningen af dette dokument.

Driftsvejledning
----------------
WSDLen bliver udstillet på "*/.wsdl*" og kaldende skal fortages til "*/*".

Ved at køre */jsp/checkall.jsp* kontrollerer applikationen at der er forbindelse til database. 
Hvis denne side viser en fejl eller returnerer andet end http status kode *200*, kører applikationen ikke. 
Se den returnerede beskrivelse af fejlen.

## SLA log
SLA loggen kan findes i jBoss serveren instansens *log*, såfremt *nspslalog-minlog.properties* er blevet lagt i *conf*.

### Whitelist
Whitelist databasen tabellen indholder de whitelistede cvr numre, der kan bruge minlog.

Design og Arkitektur beskrivelse
--------------------------------
Minlog består af 3 dele i samme webapplikation:

### Splunk job
Scriptet mapper data fra Splunk til Skema i MySQL. Mapning er som følger:


    Splunk									    MySQL
    ------------------------------------------------------------------
    PersonCivilRegistration 		 			cprNrBorger
    UserIdentification							bruger
    UserIdentificationOnBehalfOf 				ansvarlig
    HealthcareProfessionalOrganization			orgUsingID
    SourceSystemIdentifier 						systemName
    Activity 									handling
    SessionId									sessionId
    "2012....."  								tidspunkt
 
Med følgende Input til Splunk

    2012-06-18 13:38:10,239Z PersonCivilRegistrationIdentifier="100000003" EventDateTime="2012-06-18T13:38:10.109Z" UserIdentifier="0101001000" UserIdentifierOnBehalfOf="0101001000" HealthcareProfessionalOrganization="SOR:12345678" SourceSystemIdentifier="System name" Activity="Opslag på NPI" SessionId="urn:uuid:145d6c9c-b873-47ab-9203-8eaa306013ad"

#### Opbygning
Jobbet gør følgende:

* Forbinder til Splunk med et konfigurebart interval
* Udføre søgning i Splunk udfra hvor langt man er nået - hvis det er første gang jobbet køre vil det tage op til nu minus 30 sec. Ellers hentes alt der er indexeret siden sidste kørsel til nu minus 30 sec.  
* Overføre data til MySql og opdatere hvor langt jobbet er nået i samme transaction.

#### Antagelser
Jobbet antager at man henter data udfra tidspunktet de er indexeret på i Splunk, da man ikke kan være sikker på at datoen i logs vil være fortløbende - nogle servere kan f.eks. være offline og først aflevere senere og således er den eneste sikre ting at hente data udfra hvornår de er indexeret i Splunk. Derfor bruges denne metode. Desuden er der et delay på (default) 3 sekunder der sikre at man ikke henter data op til det sekund som scriptet køre, da der kan nå at komme mere data i de sekund i Splunk skal skal sikres medtages. Derfor er det nu minus 3 sekunder (default) der hentes for i det man kalder scriptet. 

#### Usikkerhed
Med den antagelse at man henter data ud af Splunk udfra Indextime og at man dermed får alt data med over i MySQL når jobbet kører. 
Der er dog ikke udviklet nogen verifikation af at alle data med sikkerhed er flyttet. Det ville være muligt at forsøge validere at der indenfor tidsrammen indextime-x til indextime-y er så mange entries i Splunk 
Dette er nuværende ikke del af jobbet. 

### WebService
Webservicens opgave er at søge i logninger, ud fra et given cpr nummer, og evt. et dato interval. 
Alle logninger der opfylder de angivne kriterier returneres.
Webservicen udstiller denne funktionallitet via en soap webservice.

### Oprydning job
Oprydningsjobbet kigger i databasen med et konfigurebart interval og slette alle logs der er ældre end 2 år.


Guide til anvendere
-------------------
Minlog bliver udstillet som en standard soap webservice og alle kald kræver "Den gode webservice 1.0.1".

WSDLen bliver udstillet på "*/.wsdl*" og kald skal fortages til "*/*".

De enkelte parametre kan ses i wsdl.



Minlog implementeres i et separat miljø "backoffice", og tilgås gennem NSP i en ren proxy-løsning.
Registreringsfladen til Minlog (Grønne del af minlog kassen) og den grå kasse (med teksten "Log") er proxy-snitfladen på NSP, der viderestiller til det bagvedliggende miljø.
Registreringssnitfladen udstilles på NSP til brug for godkendte it-systemer (f.eks. FMK, DDV, NPI, osv), der indrapporterer dataindhentninger.

Udtrækssnitfladen udstilles også på NSP som proxy til brug for godkendte it-systemer (i første omgang alene Sundhed.dk).

Guide til udviklere
-------------------
Projektet bør køres på en NSP in a box (NIAB) <https://www.nspop.dk/display/public/NSP+In+A+Box>

Projektet kan i sin nuværende tilstand ikke kører på andre server, da der er en masse maven dependencies som bliver eksluderet
for ikke at kollidere med NIAB.

Desuden bør man installere Splunk lokalt <http://www.splunk.com/>

For at teste komponenten:
1. Deploy komponenten på NIAB
2. Opsæt splunk på host maskinen (udenfor vmware).
3. Opbyg en fil med en række logninger
4. Lad Splunk indexere denne fil og loade den ind i MySql.
5. Man vil nu kunne tilgå MinLog service fra en webservice klient.

### Om koden
Applikationen er en standard DAO-Service-Controller arkitektur, hvor service-delen er undladt, da kodebasen er ret begrænset.

Controller er implementeret i Spring Webservice frameworket <http://static.springsource.org/spring-ws/sites/2.0/>

DAO er implementeret i eBean frameworket <http://www.avaje.org/>

### Byg
For bygge skal man have installeret maven og køre "mvn clean install" fra roden af projektet.
Artifakten vil efterfølgende ligge under *minlogudtraekservice/target/minlog.war*

### SosiIdCardTool
Der ligger et tool til at lave "Den gode webservice 1.0.1" headers, så det er nemmere at teste om servicen virker:

    java -Done-jar.main.class=dk.vaccinationsregister.testtools.SosiIdCardUtil -jar SosiIdCardTool.jar

På OS X kan man pipe resultatet over i clipboardet med pbcopy:
    
    Java -Done-jar.main.class=dk.vaccinationsregister.testtools.SosiIdCardUtil -jar SosiIdCardTool.jar | pbcopy

Test vejledning
---------------
Test bliver kørt automatisk når bygger projektet, som beskrevet herover.

### Coverage
For at lave coverage-tests køres *mvn clean install cobertura:cobertura surefire-report:report* fra *minlogudtraekservice/*  
Coverage resultaterne findes i *minlogudtraekservice/target/site/cobertura/index.html*  
Svar på unittests kan ses i *minlogudtraekservice/target/site/surefire-report.html*  

### Funktionelle tests
Formålet med de funktionelle tests er at teste hele vejen gennem komponenten. Fra webserviceklient, igennem webservice, hent data fra MySql, og returner data.

Dette gøres for hver test:
* En ny service og en konkret til denne.
* En embedded mysql database i en ny process. 
* Der populeres data i databasen.
* Der generes en soap security header med saml.
* Der generes en body, med en forspørgelse på test data.
* Svarets body sammenlignes med et statisk body fra en xml.

**NB!** Hvis de funktionelle tests bliver afbrudt, er der en risiko for at man ikke kan starte en mysql server efterfølgende,  
da mysql vil brokke sig over at der er en server instans der ikke er blevet lukket korrekt. 
Dette er en uhensigtsmæssighed/fejl i MySql ved denne anvendelse.
Problemet løses ved genstart af computer eller ved at rydde op i pid/err i mysqls data-folder.

Testrapport til sammenligning
-----------------------------
Test coverage sitet kan findes under *doc/coverage.zip*

Performance tests
-----------------
Sitet med performances-tests ligger under *doc/performance.zip*



Dette afsnit skal skrives om når vi har lavet nye performance tests.
Der findes *benerator* scripts til at generere tilfældige testdata. Se senere beskrivelse om generering af testdata.



Det er ikke lykkes at få serveren til at gå ned, men i tests med et forventet throughput på 1000 request/sec bliver der kun laves 200 requests/sec.
Dette kan skyldes setup'et eller en indstilling i OS'et.









Denne endurence tests laver 2 requests/sec og er lavet over 2 timer. Grafen spiker kl 12:00,  
dette kan skyldes at computeren har et job der bliver eksekveret kl 12:00.  
Derved får garbage collectoren ikke lov til at lave løbende collection.  
Ved 1GB heap rydder garbage collectoren fint op.  

<img src="https://github.com/trifork/nsi-minlog/raw/master/doc/endurence.png" width=600>

### Opsætning
Disse tests er kørt på 
    
    2GHz Intel core i7
    8 GB ram
    Harddisk med 5400 rpm

Opsætning af mysql:

    innodb_data_file_path = ibdata1:10M:autoextend
    innodb_flush_log_at_trx_commit = 1
    innodb_lock_wait_timeout = 50
    innodb_additional_mem_pool_size=512M
    innodb_buffer_pool_size=4096M
    innodb_log_buffer_size=128M
    innodb_log_file_size=1024M
    read_buffer_size= 128M
    sort_buffer_size=4096M
    tmp_table_size= 1024M

Det antages at databasen *minlog* er oprettet med adgang fra brugeren *minlog*
og at der er minlog er blevet sat op med *sosi.production = 0*



### Generering af testdata
Til at genere test-data med er *Benerator* blevet brugt <http://databene.org/databene-benerator>

Alle kommandoer skal køres fra */performance*.

Først køres *benerator benerator/cpr.xml* som laver CPR numre i *data/cpr.csv*
Der generes 50.000 tilfældige CPR numre.

Dernæst *benerator benerator/logentries.xml* som laver 450.000.000 logs i filen *data/logentries.csv*

*import.sql* tilpasses så den absolute sti passer.
**NB!** Dette skal gøres for at slippe for *local* parameteren til *load data* som laver en kopi af csv filen!.

**NB!** Det kan være en fordel at slette alle indekser pånær primary key inden kørsel. Dette optimere indsættelsen væsenligt.
