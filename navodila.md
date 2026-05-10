# Problemska Domena: StudyBuddy

**Študenta:** Luka Karoli (63240138) in Aljaž Smole (63240293)

**Naslov:** StudyBuddy – sistem za pomoč pri organizaciji učenja

**Opis:**

StudyBuddy je informacijski sistem, namenjen študentom in drugim uporabnikom, ki si
želijo boljše organizirati svoje učenje in spremljati napredek pri študiju. Velikokrat se
zgodi, da imajo študenti veliko različnih predmetov, nalog in snovi za ponavljanje, zato
hitro izgubijo pregled nad tem, kaj vse morajo še narediti. Namen sistema StudyBuddy
je, da uporabniku pomaga pri organizaciji teh obveznosti na enem mestu.

Sistem omogoča uporabniku, da si ustvari različne predmete, ki predstavljajo
posamezne študijske predmete ali področja učenja. Znotraj vsakega predmeta lahko
uporabnik dodaja teme, ki predstavljajo posamezna poglavja ali dele učne snovi.

Uporabnik lahko v sistemu ustvarja tudi učne naloge, kjer si zapiše, kaj mora še narediti
(npr. reševanje vaj, branje poglavja ali ponavljanje snovi). Pri nalogi lahko spremlja, ali je
naloga že opravljena ali ne.

Ena izmed pomembnih funkcionalnosti sistema je tudi beleženje časa učenja.
Uporabnik lahko začne študijsko sejo, sistem pa beleži, koliko časa je namenil učenju
določenega predmeta. Na ta način lahko kasneje vidi, koliko časa je porabil za učenje.

Na podlagi vseh teh podatkov sistem uporabniku prikaže tudi statistiko učenja, kjer
lahko vidi, koliko časa je porabil za učenje, katere predmete se je največ učil in katere
naloge je že opravil.

## Funkcionalnosti

- registracija uporabnika
- prijava v system
- ustvarjanje predmetov
- pregled in urejanje predmetov
- dodajanje tem k predmetom
- ustvarjanje učnih nalog
- beleženje študijskih sej
- pregled statistike učenja
- pregled in urejanje uporabniškega profila

## Opis toka dogodkov

- uporabnik se prijavi v system
- sistem preveri prijavne podatke


- sistem prikaže pregledno stran z uporabnikovimi predmeti
- uporabnik izbere obstoječ predmet ali ustvari novega
- uporabnik doda temo ali ustvari učno nalogo
- uporabnik začne študijsko sejo
- sistem beleži čas učenja
- sistem shrani podatke o aktivnosti uporabnika
- sistem prikaže statistiko učenja

## Alternativni tok dogodkov

- uporabnik predčasno zaključi sejo
- sistem ga obvesti, da se seja ne bo shranila v statistiko
- uporabnik potrdi da želi zaključiti sejo


## NAVODILA ZA APLIKACIJO

Seminarska naloga RIS 2025 / 2026 – 2. del
Naloga

Za primer uporabe, ki ste ga realizirali v 1. delu seminarske naloge:

    Izberite ustrezen implementacijski jezik (npr. Java 5.0, C#, ipd.) in diagram VOPC iz 1. dela
    seminarske naloge ustrezno dopolnite (atributi, operacije, asociacije, odvisnosti, signature,
    itd .).
    Generirajte kodo za vse načrtovane razrede v okviru primera uporabe in jo uvozite v izbrano
    razvojno okolje (npr. Android Studio, Eclipse ADT, ...).
    Uvožene mejne razrede (<>) ustrezno prilagodite:
    a. Za implementacijo uporabniškega vmesnika boste najverjetneje poleg mejnih
    razredov uporabili še dodatne standardne razrede za prikaz (okna, tipke, vnosna polja
    ipd.), ki jih v analizi niste prikazali. Kljub temu pa pazite: če ste v analizi predvideli le
    en mejni razred, to pomeni le eno zaslonsko masko. Glavni razred uporabniškega
    vmesnika naj bo poimenovan tako kot v načrtu.
    b. Če ste v okviru realizacije primerov uporabe predvideli dostop do zunanjega sistema,
    potem zunanji sistem samo simulirajte. Npr. mejni razred, ki v analizi dostopa do
    zunanjega sistema, naj v implementaciji dostopa do razreda(ov), ki simulira(jo) zunanji
    sistem. Imena razredov, ki simulirajo sistem naj se končajo s »_SIM « (npr.
    SVBanka_SIM). Simulacija naj bo preprosta (npr. vračanje naključne vrednosti).
    Sistemskih storitev ni potrebno zagotavljati. V kolikor je bila za določen razred predvidena
    trajnost, le poskrbite, da se bo razred ob zagonu vedno napolnil z nekaj testnimi podatki, ki
    so lahko zapisani kar v programski kodi (hardcoded) v obliki polja – npr:

int [] [] sudoku = new int[] [] {
{ 0, 5, 0, 4, 0, 2, 0, 6, 0 },
{ 0, 3, 4, 0, 0, 0, 9, 1, 0 },
{ 9, 6, 0, 0, 0, 0, 0, 8, 4 },
{ 0, 0, 0, 2, 3, 6, 0, 0, 0 },
{ 2, 0, 0, 0, 0, 9, 6, 0, 0 },
{ 0, 1, 0, 3, 5, 7, 0, 0, 8 },
{ 8, 4, 0, 0, 0, 0, 0, 7, 5 },
{ 0, 2, 6, 0, 0, 0, 1, 3, 0 },
{ 0, 9, 0, 7, 0, 1, 0, 4, 0 }
} ;

products = new ArrayList<ProductLine>();
products.add(new ProductLine("A0001", 10.90, 9.90, 100, "N/A"));
products.add(new ProductLine("B0010", 12.00, 7.50, 125, "5"));
products.add(new ProductLine("C0100", 3.00, 2.30, 1000, "16"));

Med delovanjem programa pa mora seveda biti mogoče dodajati nove objekte, ki pa se po

izklopu programa ne shranijo.

    V razvojnem okolju pripravite vse potrebne metode (programska koda operacij v razredih),
    pri čemer pa ne smete spreminjati zgradbe razredov, ki ste jo generirali z orodjem Power
    Designer. Metode morajo biti izdelane do stopnje, da program deluje tako kot je zapisano v
    zahtevah (tok dogodkov v primeru uporabe).
    Pripravite vse druge potrebne datoteke in nastavitve (npr. AndroidManifest.xml, APK file), da
    bo program mogoče zagnati.

V okviru naloge realizirajte le izbrani primer uporabe. V kolikor za delovanje primera uporabe obstaja
predpogoj (npr. uporabnik mora biti prijavljen v sistem) naj bo vaše izhodišče, da je predpogoj
izpolnjen. V kolikor so za delovanje primera uporabe potrebni podatki, ki bi nastali v okviru drugega
primera uporabe, ob zagonu kreirajte nekaj primernih objektov (hardcoded).
Oddaja

Oddaja mora obsegati:

    Dopolnjen model v PowerDesignerju .oom.
    Izvorno kodo naloge v celoti (najbolje da mi date javno povezavo na repozitorij Github).
    Prevedeno kodo, ki je mogoče zagnati (npr. .apk, .jar). Zelo dobrodošlo je, da spletno
    aplikacijo vzpostavite na strežniku.

V primeru zamude, boste nalogo še vedno lahko oddali, vendar bo zamuda negativno vplivala na oceno
(-1 točka/dan). Preverite tudi, da je programsko kodo mogoče zagnati neposredno preko izvršljive
datoteke (npr. .jar, .apk) ali po domače -> poskusite tudi odpreti izvršljivo datoteko na drugem
računalniku.

Pri razvoju aplikacije imate odprte roke. Pri oddaji aplikacije bodite pozorni, da »dela tudi na drugem
računalniku«. Najbolje je, da več ljudi testira. Pri javanskih aplikacijah je lahko problem neskladnost
verzij razvojnega okolja (JDK/SDK). Sicer pa lahko izdelate aplikacijo po želji: mobilna OR spletna OR
namizna ...

V kolikor boste izdelali spletno aplikacijo, oddajte povezavo do delujočega mesta v tekstovni datoteki
(.txt) ali v »text area« na eučilnici. Ne rabite oddajati izvornih datotek če mi omogočite dostop do okolja
Github/Gitlab ali podobno. V kolikor to ne želite sicer lahko oddate datoteke preko eučilnice. Če je
izvorna datoteka prevelika, naložite datoteke v svoj oblak in delite povezavo (v .txt).
Ocenjevanje

Pri ocenjevanju bo poudarek na preverjanju skladnosti izdelane izvorne kode z načrtom. Na oceno bo
vplivala tudi skladnost z zajetimi zahtevami iz 1. dela naloge (atributi, operacije, sporočila), urejenost
(npr. programske kode, pripadajoče dokumentacije, poimenovanje razredov ...), zagon kode, izgled,
scenarij delovanja (diagram zaporedja), uporabniška izkušnja ipd. Če seveda ugotovite, da je treba
določene operacije/metode/potek programa drugače zapeljati, lahko to storite brze problema.
Dokumentacije ni potrebno znova oddajati, izboljševati.

## OPIS RAZREDNI DIAGRAM

VOPC RAZREDNI DIAGRAM – StudyBuddy (StudySession use case)

=== BOUNDARY ===
SubjectSelector
  + showSubjects() : int

StudySessionUI
  + showSessionForm() : void
  + updateTimer() : void
  + showStats() : void
  + showError() : void

=== CONTROL ===
StatsController
  + getStudyTimeBySubject() : int

StudySessionController
  + izracunajDuration() : int
  + startSession() : int
  + endSession() : int

=== ENTITY ===
UserInfo
  - User_ID : int
  - Firstname : string
  - Lastname : string
  - Email : string

StudySession
  - Session_ID : int
  - start_time : timestamp
  - duration : int
  - User_ID : int
  - Subject_ID : int

Subject
  - Subject_ID : int
  - Name : string
  - User_ID : int
  + addTopic() : Topic
  + removeTopic() : Topic

Topic
  - Topic_ID : int
  - Name : string
  - Subject_ID : int

=== ASOCIACIJE (multiplicitete) ===
UserInfo        1..1 ──── 1..*  StudySession
StudySession    1..1 ──── 1..*  Subject
Subject         1..1 ──── *     Topic

=== ODVISNOSTI (dependency, dashed) ===
StudySessionUI  --> StudySessionController
SubjectSelector --> StatsController
StudySessionController --> StudySession (1..* konec pri StudySession)
StatsController --> UserInfo