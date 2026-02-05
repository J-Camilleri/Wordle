# Wordle

---

## Kurze Beschreibung
***Wordle*** ist ein konsolenbasiertes Spiel, in dem der Spieler versucht,  
durch Eingabe gültiger deutscher Wörter, ein täglich wechselndes Wort zu erraten.  

Nach jedem Versuch werden dem Spieler Hinweise darüber gegeben,  
ob Buchstaben im gesuchten Wort enthalten sind  
und ob sie sich an der richtigen Stelle befinden.  



## Funktionalitäten:
- Benutzerverwaltung mit Username und Passwort 
- Jeder Spieler besitzt einen individuellen Score  
- Persistente Speicherung von Benutzern, Scores und Spielständen in einer SQLite-Datenbank  
- Laden gespeicherter Spielstände und Scores aus der Datenbank  
- Tägliche Auswahl eines neuen Wortes aus der Datenbank  
- Validierung der Benutzereingaben (nur gültige Wörter)  
- Anzeige von Hinweisen nach jeder Eingabe  
- Scoreboard zur Anzeige aller Spieler mit ihren Punkten  
- Automatisierte Tests  
- CI-Pipeline zur Qualitätssicherung  

## Projektstruktur
```
src/main/java/de/htw/saar/wordle
├── game
│   ├── Database
│   │   ├── Score
│   │   │   └── ScoreEntry.java
│   │   ├── Words
│   │   │   ├── Word.java
│   │   │   ├── WordProvider.java
│   │   │   └── WordSeeder.java
│   │   ├── DailyWordleRepository.java
│   │   ├── DatabaseManager.java
│   │   ├── GameRepository.java
│   │   ├── PracticeWordleRepository.java
│   │   ├── ScoreboardRepository.java
│   │   └── UserRepository.java
│   ├── Exceptions
│   │   └── DataAccessException.java
│   ├── Logic
│   │   ├── DailyWordle.java
│   │   ├── Difficulty.java
│   │   ├── GameConfig.java
│   │   ├── GameUI.java
│   │   ├── Grid.java
│   │   ├── LetterStatus.java
│   │   ├── PracticeWordle.java
│   │   └── Wordle.java
│   ├── LoginSystem
│   │   ├── User.java
│   │   ├── AuthenticationService.java
│   │   └── PasswordHashing.java
│   └── Presentation
│       ├── Dialog.java
│       ├── State.java
│       ├── UserInterface.java
│       └── WordleStart.java
```

## Voraussetzungen
- Java Development Kit (JDK) **21**  
- Apache **Maven**  
  (Build, Tests und Start erfolgen über Maven)  
- Git (optional, aber empfohlen)  
  (für das Klonen/Arbeiten im Repository)  
- SQLite-Datenbankdatei `wordle.db` im Projektverzeichnis  
wird für den jOOQ-Codegenerator verwendet (`jdbc:sqlite:wordle.db`).  
- IDE (empfohlen): IntelliJ IDEA oder vergleichbar  

## Installation & Start:
1) Projekt Klonen: ```bash
git clone https://github.com/J-Camilleri/Wordle```
```cd Wordle```
2) Projekt bauen: ```mvn clean install``` 
3) Anwendung starten: ```mvn exec:java``` 

*Alternativ kann das Projekt direkt in IntelliJ importiert und gestartet werden*

## Tests
Automatisierte Tests werden mit **JUnit 5** durchgeführt: mvn test  
(Alle Tests werden zusätzlich über eine CI-Pipeline bei jedem Push ausgeführt)   

## Datenbank
Verwendet wird **SQLite**  
(Die Datenbank wird über die Datei Wordle.db mitgeliefert)  
Diese Datenbank enthält die Tabellen: 
- words  
- daily_words  
- games  
- users  
- scores  

## Verwendete Technologien
- Java (JDK 21)
- Maven
- JUnit 5
- SQLite
- jOOQ
- LanguageTool (Deutsch)
- BCrypt (Passwort-Hashing)
- Git / GitHub
- GitHub Actions (CI)

