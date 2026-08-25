# Pokémon Card Tracker

Eine native Android-App (Kotlin + Jetpack Compose), um eine Pokémon-Karten-Sammlung
zu verwalten: Karten mit Foto und Details anlegen, den Wert regelmäßig aktualisieren
und den **Preisverlauf** – pro Karte **und** für die gesamte Sammlung – als Chart
nachvollziehen. Alle Daten bleiben **offline auf dem Gerät**.

## Funktionen

- **Karten anlegen** mit eigenem Foto (Kamera oder Galerie) und Feldern:
  Name, Sprache, Set/Pack, Kartennummer, Zustand, Menge, Kaufpreis, Notizen.
- **Wert erfassen & aktualisieren**: Jede Wertänderung wird mit Datum als eigener
  Eintrag gespeichert – so entsteht eine lückenlose Preis-Historie.
- **Preisverlauf-Charts**: Linien-Chart pro Karte und für den Gesamtwert der Sammlung
  über die Zeit.
- **Übersicht (Dashboard)**: aktueller Gesamtwert, Kartenanzahl und Wertentwicklung.
- **Sammlung**: durchsuchbare Liste (Name, Set, Nummer) mit Miniaturbild und aktuellem Wert.
- **Bearbeiten & Löschen** einzelner Karten und einzelner Preis-Einträge.
- Währung: **Euro (€)**.

## Technischer Aufbau

| Bereich        | Umsetzung |
|----------------|-----------|
| Sprache/UI     | Kotlin, Jetpack Compose, Material 3 |
| Architektur    | MVVM (ViewModels + StateFlow) |
| Datenhaltung   | Room (SQLite), rein lokal |
| Bilder         | Foto im privaten App-Speicher (`files/card_images/`) via `FileProvider` |
| Charts         | Eigene, abhängigkeitsfreie `LineChart`-Komponente (Compose `Canvas`) |
| Bild-Laden     | Coil |
| Navigation     | Navigation-Compose |
| Min. Android   | 8.0 (API 26), Ziel API 35 |

### Datenmodell

- `Card` – die Karte mit ihren Attributen (der aktuelle Wert wird **nicht** direkt
  auf der Karte gespeichert).
- `PriceEntry` – ein Punkt der Preis-Historie (`cardId`, `price`, `date`). Der
  aktuelle Wert einer Karte ist der jüngste `PriceEntry`. Beim Löschen einer Karte
  werden die Einträge per Foreign-Key-`CASCADE` mit entfernt.

Diese Trennung hält die Historie sauber und ist bewusst so gebaut, dass später eine
**automatische Preis-Aktualisierung** (z. B. über eine externe API) ergänzt werden
kann, ohne das Datenmodell zu ändern – neue Preise werden dann einfach als weitere
`PriceEntry` geschrieben.

## Projekt bauen

Das Projekt ist ein Standard-Gradle-Android-Projekt.

**Mit Android Studio (empfohlen):**
1. Projektordner in Android Studio öffnen (Giraffe/Koala oder neuer).
2. Gradle-Sync abwarten (lädt AGP, Compose, Room, Coil automatisch).
3. Auf Gerät/Emulator (Android 8.0+) starten.

**Per Kommandozeile:**
```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```
Voraussetzung: installiertes Android SDK (Platform 35, Build-Tools 35) und ein JDK 17.
Bei Bedarf `local.properties` mit `sdk.dir=/pfad/zum/Android/Sdk` anlegen (Android
Studio erzeugt diese Datei automatisch).

## Projektstruktur

```
app/src/main/java/com/pokemontracker/app/
├─ data/          Room-Entities, DAOs, Datenbank, Repository
├─ ui/
│  ├─ screens/    Übersicht, Sammlung, Detail, Hinzufügen/Bearbeiten
│  ├─ components/ LineChart, CardImage
│  ├─ theme/      Farben, Typografie, Theme
│  └─ *ViewModel  CollectionViewModel, CardDetailViewModel, CardEditViewModel
├─ util/          Formatierung (Euro/Datum), Bildspeicher
├─ MainActivity.kt
└─ PokemonTrackerApp.kt
```

## Mögliche Erweiterungen

- Automatische Preisübernahme aus einer Karten-/Marktpreis-API.
- Datum eines Preis-Eintrags frei wählbar (aktuell wird der Zeitpunkt der Eingabe verwendet).
- Export/Import der Sammlung als Datei sowie Cloud-Backup.
- Filter/Sortierung (nach Wert, Set, Sprache) und Statistiken.
