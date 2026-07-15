# Changelog

## v1.0.0

Erste stabile Projektversion von PMX als lokaler JavaFX Passwortmanager.

### Enthaltene Funktionen

- Benutzer anlegen und anmelden
- Recovery Funktion für das Zurücksetzen des Master Passworts
- Lokale Tresorverwaltung mit Suchen, Hinzufügen, Bearbeiten und Löschen
- Passwortgenerator für neue Zugangsdaten
- Verschlüsselte Speicherung sensibler Daten
- Passwortanzeige mit automatischer Maskierung nach kurzer Zeit
- Kopieren von Passwörtern mit automatischem Leeren der Zwischenablage
- Eintragsmetadaten mit Änderungsdatum und Passwort Alter Status
- Verschlüsselter Backup Export
- Verschlüsselter Backup Import
- Auto Lock nach Inaktivität
- Login Sperre nach mehreren Fehlversuchen
- Leerer Tresor Zustand bei noch nicht vorhandenen Einträgen
- Moderne JavaFX Oberfläche im dunklen PMX Design
- GitHub Actions CI für automatischen Maven Build mit Tests

### Technischer Fokus

- Java 17
- JavaFX, FXML und CSS
- Maven
- SQLite und NitriteDB
- AES-GCM Verschlüsselung
- PBKDF2-HMAC-SHA-256 Schlüsselableitung
- Gson für Backup Verarbeitung
- JUnit 5
- Git und GitHub Actions

### Hinweis

PMX ist ein Demo- und Bewerbungsprojekt. Es ist kein produktiv auditierter Passwortmanager.
