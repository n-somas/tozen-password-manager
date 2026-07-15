# PMX v1.0.0

PMX ist ein lokaler JavaFX Passwortmanager als Desktopanwendung.  
Der Fokus dieser Version liegt auf lokaler Datenhaltung, Verschlüsselung, Benutzerführung, Backup Funktion, Sicherheitslogik und nachvollziehbarer GitHub Dokumentation.

## Highlights

- Lokale Desktop Anwendung ohne Cloud Synchronisierung
- Benutzerregistrierung, Anmeldung und Recovery Funktion
- Tresoransicht mit Suche, Hinzufügen, Bearbeiten und Löschen
- Passwortgenerator
- AES-GCM Verschlüsselung sensibler Daten
- PBKDF2-HMAC-SHA-256 zur Schlüsselableitung
- Zeitlich begrenztes Anzeigen von Passwörtern
- Automatisches Leeren der Zwischenablage nach dem Kopieren
- Passwort Alter Status für Tresoreinträge
- Verschlüsselter Backup Export und Import
- Auto Lock nach Inaktivität
- Login Sperre nach mehreren Fehlversuchen
- Leerer Tresor Zustand für eine bessere Benutzerführung
- GitHub Actions CI mit automatischem Maven Build und Tests

## Projektstatus

Diese Version markiert den ersten stabilen Stand des Projekts.  
PMX ist nicht für den produktiven Einsatz als echter Passwortmanager freigegeben und wurde nicht extern auditiert.

## Lokaler Start

```bash
mvn clean test
mvn javafx:run
```
