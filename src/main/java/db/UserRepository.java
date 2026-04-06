package db;


import util.HashUtil;                       // Hashing, Salt-Generierung, Base64

// Datei- & Pfadverwaltung
import java.io.File;                        // Dateioperationen
import java.nio.file.Files;                 // Verzeichnisse erstellen
import java.nio.file.Path;                  // Pfad-Objekte

// JDBC / SQL
import java.sql.Connection;                 // DB-Verbindung
import java.sql.DriverManager;              // JDBC-Verbindung öffnen
import java.sql.PreparedStatement;          // Parametrisierte Statements
import java.sql.SQLException;               // SQL-Fehler
import java.sql.Statement;                  // Einfache Statements


public class UserRepository {

    // >>> Pfad anpassen, wenn gewünscht
    private static final String BASE_DIR = "C:\\PasswortManager\\data";
    private static final String DB_FILE  = "passwortmanager.db";

    // JDBC-URL erzeugen (stellt sicher, dass BASE_DIR existiert)
    private static String dbUrl() {
        ensureBaseDir();
        return "jdbc:sqlite:" + BASE_DIR + File.separator + DB_FILE;
    }

    // Verzeichnis sicherstellen
    private static void ensureBaseDir() {
        try {
            Files.createDirectories(Path.of(BASE_DIR));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Konstruktor: Tabelle anlegen + fehlende Spalten nachrüsten
    public UserRepository() {
        createTableIfNotExists();
        ensureDekColumns(); // fehlende Spalten nachziehen
    }

    // Tabelle anlegen (idempotent)
    private void createTableIfNotExists() {
        try (Connection conn = DriverManager.getConnection(dbUrl());
             Statement st = conn.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT UNIQUE NOT NULL,
                  passwordHash TEXT NOT NULL,
                  salt TEXT NOT NULL,
                  recoveryHash TEXT NOT NULL,
                  recoverySalt TEXT NOT NULL,
                  -- DEK-Wrapping (können leer sein, werden unten notfalls nachgezogen)
                  dek_pw TEXT,
                  dek_pw_iv TEXT,
                  dek_pw_salt TEXT,
                  dek_rec TEXT,
                  dek_rec_iv TEXT,
                  dek_rec_salt TEXT
                )
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fehlende DEK-Spalten via ALTER TABLE hinzufügen (idempotent)
    private void ensureDekColumns() {
        addColumnIfMissing("dek_pw", "TEXT");
        addColumnIfMissing("dek_pw_iv", "TEXT");
        addColumnIfMissing("dek_pw_salt", "TEXT");
        addColumnIfMissing("dek_rec", "TEXT");
        addColumnIfMissing("dek_rec_iv", "TEXT");
        addColumnIfMissing("dek_rec_salt", "TEXT");
    }

    // Hilfsfunktion: Spalte hinzufügen, wenn nicht vorhanden
    private void addColumnIfMissing(String colName, String type) {
        try (Connection conn = DriverManager.getConnection(dbUrl());
             Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE users ADD COLUMN " + colName + " " + type);
        } catch (SQLException e) {
            // Spalte existiert bereits -> "duplicate column name" ignorieren
            String msg = String.valueOf(e.getMessage()).toLowerCase();
            if (!msg.contains("duplicate column name")) {
                e.printStackTrace();
            }
        }
    }

    // ---------- Basic User ----------

    // Prüfen, ob Benutzer existiert
    public boolean userExists(String username) {
        try (Connection c = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Benutzer anlegen (PW-Hash + Recovery-Hash/-Salt)
    public void createUser(String username, String password, String recoveryHash, String recoverySalt) {
        try (Connection c = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users (username,passwordHash,salt,recoveryHash,recoverySalt) VALUES (?,?,?,?,?)"))
        {
            byte[] salt = HashUtil.generateSalt();
            String passwordHash = HashUtil.hashWithSalt(password, salt);
            String saltB64 = HashUtil.encodeBase64(salt);

            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, saltB64);
            ps.setString(4, recoveryHash);
            ps.setString(5, recoverySalt);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Login prüfen (PW gegen gespeicherten Hash)
    public boolean verifyLogin(String username, String password) {
        try (Connection c = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = c.prepareStatement("SELECT passwordHash,salt FROM users WHERE username=?")) {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            if (!rs.next()) return false;
            String saltB64 = rs.getString("salt");
            String stored = rs.getString("passwordHash");
            String calc = HashUtil.hashWithSalt(password, HashUtil.decodeBase64(saltB64));
            return stored.equals(calc);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Recovery-Hash lesen
    public String getRecoveryHash(String username) {
        try (Connection c = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = c.prepareStatement("SELECT recoveryHash FROM users WHERE username=?")) {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getString("recoveryHash") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Recovery-Salt lesen (Base64)
    public String getRecoverySalt(String username) {
        try (Connection c = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = c.prepareStatement("SELECT recoverySalt FROM users WHERE username=?")) {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getString("recoverySalt") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Passwort aktualisieren (neues Salt + Hash)
    public void updatePassword(String username, String newPassword) {
        try (Connection c = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = c.prepareStatement("UPDATE users SET passwordHash=?, salt=? WHERE username=?")) {
            byte[] salt = HashUtil.generateSalt();
            ps.setString(1, HashUtil.hashWithSalt(newPassword, salt));
            ps.setString(2, HashUtil.encodeBase64(salt));
            ps.setString(3, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // dek_pw* und dek_rec* speichern
    public void saveWrappedKeys(String username,
                                String dekPw, String dekPwIv, String dekPwSalt,
                                String dekRec, String dekRecIv, String dekRecSalt) {
        try (Connection c = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = c.prepareStatement("""
                UPDATE users SET 
                  dek_pw=?, dek_pw_iv=?, dek_pw_salt=?,
                  dek_rec=?, dek_rec_iv=?, dek_rec_salt=?
                WHERE username=?
             """)) {
            ps.setString(1, dekPw);
            ps.setString(2, dekPwIv);
            ps.setString(3, dekPwSalt);
            ps.setString(4, dekRec);
            ps.setString(5, dekRecIv);
            ps.setString(6, dekRecSalt);
            ps.setString(7, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // dek_pw* / dek_rec* laden
    public WrappedKeys loadWrappedKeys(String username) {
        try (Connection c = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = c.prepareStatement("""
                SELECT dek_pw, dek_pw_iv, dek_pw_salt,
                       dek_rec, dek_rec_iv, dek_rec_salt
                FROM users WHERE username=?
             """)) {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            if (!rs.next()) return null;
            return new WrappedKeys(
                    rs.getString("dek_pw"),  rs.getString("dek_pw_iv"),  rs.getString("dek_pw_salt"),
                    rs.getString("dek_rec"), rs.getString("dek_rec_iv"), rs.getString("dek_rec_salt")
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Value-Record für Wrapped Keys
    public record WrappedKeys(
            String dekPw, String dekPwIv, String dekPwSalt,
            String dekRec, String dekRecIv, String dekRecSalt
    ) {}
}
