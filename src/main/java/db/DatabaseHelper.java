package db;


import model.VaultEntry;                          // Eintrag im Passwort-Tresor

// Nitrite-Datenbank (Embedded NoSQL)
import org.dizitart.no2.Nitrite;                   // Haupt-DB-Objekt
import org.dizitart.no2.objects.ObjectRepository;  // Objekt-Repository (CRUD)

// Dateioperationen
import java.io.File;                               // Dateipfade & Verzeichnisse

public final class DatabaseHelper {

    // Statische Felder für DB und Repository
    private static Nitrite db;
    private static ObjectRepository<VaultEntry> repo;

    private DatabaseHelper() {} // Utility-Klasse, kein Instanzieren

    // Liefert die DB-Datei für den Benutzer
    private static File fileForUser(String owner) {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs(); // Ordner anlegen, falls nicht vorhanden
        return new File(dir, "vault_" + owner + ".db");
    }

    // DB initialisieren (pro Benutzer eigene Datei)
    public static void initDatabase(String owner) {
        closeDatabase(); // Falls vorher offen, schließen
        db = Nitrite.builder()
                .filePath(fileForUser(owner))
                .openOrCreate(); // WICHTIG: kein Username/Passwort – lokale Verschlüsselung erfolgt separat
        repo = db.getRepository(VaultEntry.class);
    }

    // DB schließen
    public static void closeDatabase() {
        if (db != null && !db.isClosed()) db.close();
    }

    // CRUD-Operationen
    public static Iterable<VaultEntry> findAll() { return repo.find(); }
    public static void addEntry(VaultEntry e) { repo.insert(e); }
    public static void upsert(VaultEntry e) { repo.update(e, true); }
    public static void deleteByWebsite(String website) {
        repo.remove(org.dizitart.no2.objects.filters.ObjectFilters.eq("website", website));
    }
}


